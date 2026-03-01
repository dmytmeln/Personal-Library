import {Component, computed, DestroyRef, effect, inject, input, OnInit, output, signal, untracked} from '@angular/core';
import {takeUntilDestroyed, toSignal} from '@angular/core/rxjs-interop';
import {CommonModule} from '@angular/common';
import {AuthorService} from '../services/author.service';
import {LibraryAuthorService} from '../services/library-author.service';
import {Author} from '../interfaces/author';
import {CountryWithCount} from '../interfaces/country-with-count';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatButtonModule} from '@angular/material/button';
import {Router} from '@angular/router';
import {
  FilterShellComponent,
  MainFiltersDirective,
  SecondaryFiltersDirective,
  TopRowFiltersDirective
} from '../common/filter-shell/filter-shell.component';
import {TextFilterComponent} from '../common/filters/text-filter/text-filter.component';
import {SelectFilterComponent, SelectOption} from '../common/filters/select-filter/select-filter.component';
import {RangeFilterComponent} from '../common/filters/range-filter/range-filter.component';
import {SortBarComponent} from '../common/sort-bar/sort-bar.component';
import {AuthorFilters} from '../interfaces/filters';
import {EntityFilterStore} from '../services/entity-filter.store';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {map, skip} from 'rxjs';
import {LibraryStore} from '../services/library.store';

const EMPTY_AUTHOR_FILTERS: AuthorFilters = {
  name: '',
  country: null,
  birthYear: {min: null, max: null},
  booksCount: {min: null, max: null},
};

@Component({
  selector: 'app-author-list',
  standalone: true,
  imports: [
    CommonModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    FilterShellComponent,
    TopRowFiltersDirective,
    MainFiltersDirective,
    SecondaryFiltersDirective,
    TextFilterComponent,
    SelectFilterComponent,
    RangeFilterComponent,
    SortBarComponent,
    TranslocoDirective,
  ],
  templateUrl: './author-list.component.html',
  styleUrl: './author-list.component.scss'
})
export class AuthorListComponent implements OnInit {
  mode = input.required<'library' | 'search'>();

  authorBooks = output<Author>();

  authorsState = {
    items: [] as Author[],
    totalElements: 0,
    pageSize: 12,
    currentPage: 0,
    loading: false,
    sort: undefined as string[] | undefined,
  };

  countries = signal<CountryWithCount[]>([]);

  readonly filters = new EntityFilterStore<AuthorFilters>(EMPTY_AUTHOR_FILTERS);

  readonly isFiltersExpanded = signal(false);
  readonly activeFiltersCount = computed(() => {
    const f = this.filters.state();
    let count = 0;
    if (f.country) count++;
    if (f.birthYear.min || f.birthYear.max) count++;
    if (f.booksCount.min || f.booksCount.max) count++;
    return count;
  });

  private readonly translocoService = inject(TranslocoService);

  readonly authorSortOptions = toSignal(
    this.translocoService.selectTranslateObject('authors.sort').pipe(
      map(t => [
        {field: 'fullName', label: t.fullName},
        {field: 'country', label: t.country},
        {field: 'birthYear', label: t.birthYear},
        {field: 'booksCount', label: t.booksCount},
      ])
    ),
    {initialValue: []}
  );

  readonly countryOptions = computed<SelectOption[]>(() =>
    this.countries().map(c => ({
      value: c.country,
      label: `${c.country} (${c.count})`
    }))
  );

  constructor(
    private authorService: AuthorService,
    private libraryAuthorService: LibraryAuthorService,
    private libraryStore: LibraryStore,
    private destroyRef: DestroyRef,
    private router: Router
  ) {
    effect(() => {
      this.libraryStore.refreshVersion();
      untracked(() => {
        if (this.mode() === 'library') {
          this.loadAuthors();
          this.loadCountries();
        }
      });
    });
  }

  ngOnInit(): void {
    this.setupSubscriptions();
  }

  onPageChange(event: PageEvent): void {
    this.authorsState.currentPage = event.pageIndex;
    this.authorsState.pageSize = event.pageSize;
    this.loadAuthors();
  }

  onSortChange(sort: string[] | undefined): void {
    this.authorsState.sort = sort;
    this.authorsState.currentPage = 0;
    this.loadAuthors();
  }

  hasActiveFilters(): boolean {
    return this.filters.hasActiveFilters(f => {
      return !!f.name || !!f.country || f.birthYear.min != null || f.birthYear.max != null || f.booksCount.min != null || f.booksCount.max != null;
    });
  }

  clearAllFilters(): void {
    this.filters.reset(EMPTY_AUTHOR_FILTERS);
  }

  private loadAuthors(): void {
    this.authorsState.loading = true;
    const f = this.filters.state();
    const options = {
      page: this.authorsState.currentPage,
      size: this.authorsState.pageSize,
      sort: this.authorsState.sort,
      name: f.name,
      country: f.country ?? undefined,
      birthYearMin: f.birthYear.min ?? undefined,
      birthYearMax: f.birthYear.max ?? undefined,
      booksCountMin: f.booksCount.min ?? undefined,
      booksCountMax: f.booksCount.max ?? undefined,
    };

    const request = this.mode() === 'library'
      ? this.libraryAuthorService.getAll(options)
      : this.authorService.search(options);

    request.subscribe({
      next: page => {
        this.authorsState.items = page.content;
        this.authorsState.totalElements = page.page.totalElements;
        this.authorsState.loading = false;
      },
      error: () => this.authorsState.loading = false
    });
  }

  private loadCountries(): void {
    const request = this.mode() === 'library'
      ? this.libraryAuthorService.getCountries()
      : this.authorService.getCountries();

    request.subscribe(countries => this.countries.set(countries));
  }

  private setupSubscriptions(): void {
    this.translocoService.langChanges$.pipe(skip(1), takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      const hadFilters = this.hasActiveFilters();
      this.clearAllFilters();
      if (!hadFilters) {
        this.loadAuthors();
      }
      this.loadCountries();
    });

    this.filters.filtersChanged$.subscribe(() => {
      this.authorsState.currentPage = 0;
      this.loadAuthors();
    });
  }

  goToAuthorDetails(author: Author): void {
    this.router.navigate(['/author-details'], {state: {id: author.id}});
  }

  onShowBooks(author: Author): void {
    this.authorBooks.emit(author);
  }
}
