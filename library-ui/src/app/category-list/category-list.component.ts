import {Component, computed, DestroyRef, effect, inject, input, OnInit, output, signal, untracked} from '@angular/core';
import {takeUntilDestroyed, toSignal} from '@angular/core/rxjs-interop';
import {CommonModule} from '@angular/common';
import {CategoryService} from '../services/category.service';
import {LibraryCategoryService} from '../services/library-category.service';
import {Category} from '../interfaces/category';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {Router} from '@angular/router';
import {
  FilterShellComponent,
  SecondaryFiltersDirective,
  TopRowFiltersDirective
} from '../common/filter-shell/filter-shell.component';
import {TextFilterComponent} from '../common/filters/text-filter/text-filter.component';
import {RangeFilterComponent} from '../common/filters/range-filter/range-filter.component';
import {SortBarComponent} from '../common/sort-bar/sort-bar.component';
import {CategoryFilters} from '../interfaces/filters';
import {EntityFilterStore} from '../services/entity-filter.store';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {map} from 'rxjs';
import {LibraryStore} from '../services/library.store';

const EMPTY_CATEGORY_FILTERS: CategoryFilters = {
  name: '',
  booksCount: {min: null, max: null},
};

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [
    CommonModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    MatTooltipModule,
    FilterShellComponent,
    TopRowFiltersDirective,
    SecondaryFiltersDirective,
    TextFilterComponent,
    RangeFilterComponent,
    SortBarComponent,
    TranslocoDirective,
  ],
  templateUrl: './category-list.component.html',
  styleUrl: './category-list.component.scss'
})
export class CategoryListComponent implements OnInit {
  mode = input.required<'library' | 'search'>();

  categoryBooks = output<Category>();

  categoriesState = {
    items: [] as Category[],
    totalElements: 0,
    pageSize: 12,
    currentPage: 0,
    loading: false,
    sort: undefined as string[] | undefined,
  };

  readonly filters = new EntityFilterStore<CategoryFilters>(EMPTY_CATEGORY_FILTERS);

  readonly isFiltersExpanded = signal(false);
  readonly activeFiltersCount = computed(() => {
    const f = this.filters.state();
    let count = 0;
    if (f.booksCount.min || f.booksCount.max) count++;
    return count;
  });

  private readonly translocoService = inject(TranslocoService);

  readonly categorySortOptions = toSignal(
    this.translocoService.selectTranslateObject('categories.sort').pipe(
      map(t => [
        {field: 'name', label: t.name},
        {field: 'booksCount', label: t.booksCount},
        {field: 'popularityCount', label: t.popularity},
      ])
    ),
    {initialValue: []}
  );

  constructor(
    private categoryService: CategoryService,
    private libraryCategoryService: LibraryCategoryService,
    private libraryStore: LibraryStore,
    private destroyRef: DestroyRef,
    private router: Router
  ) {
    effect(() => {
      const version = this.libraryStore.refreshVersion();
      untracked(() => {
        if (version !== 0 && this.mode() === 'library') {
          this.loadCategories();
        }
      });
    });
  }

  ngOnInit(): void {
    this.setupSubscriptions();
  }

  onPageChange(event: PageEvent): void {
    this.categoriesState.currentPage = event.pageIndex;
    this.categoriesState.pageSize = event.pageSize;
    this.loadCategories();
  }

  onSortChange(sort: string[] | undefined): void {
    this.categoriesState.sort = sort;
    this.categoriesState.currentPage = 0;
    this.loadCategories();
  }

  hasActiveFilters(): boolean {
    return this.filters.hasActiveFilters(f => {
      return !!f.name || f.booksCount.min != null || f.booksCount.max != null;
    });
  }

  clearAllFilters(): void {
    this.filters.reset(EMPTY_CATEGORY_FILTERS);
  }

  private loadCategories(): void {
    this.categoriesState.loading = true;
    const f = this.filters.state();
    const options = {
      page: this.categoriesState.currentPage,
      size: this.categoriesState.pageSize,
      sort: this.categoriesState.sort,
      name: f.name,
      booksCountMin: f.booksCount.min ?? undefined,
      booksCountMax: f.booksCount.max ?? undefined,
    };

    const request = this.mode() === 'library'
      ? this.libraryCategoryService.getAll(options)
      : this.categoryService.search(options);

    request.subscribe({
      next: page => {
        this.categoriesState.items = page.content;
        this.categoriesState.totalElements = page.page.totalElements;
        this.categoriesState.loading = false;
      },
      error: () => this.categoriesState.loading = false
    });
  }

  private setupSubscriptions(): void {
    this.translocoService.langChanges$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      const hadFilters = this.hasActiveFilters();
      this.clearAllFilters();
      if (!hadFilters) {
        this.loadCategories();
      }
    });

    this.filters.filtersChanged$.subscribe(() => {
      this.categoriesState.currentPage = 0;
      this.loadCategories();
    });
  }

  goToCategoryDetails(category: Category): void {
    this.router.navigate(['/category-details'], {state: {id: category.id}});
  }

  onShowBooks(category: Category): void {
    this.categoryBooks.emit(category);
  }
}
