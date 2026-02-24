import {Component, computed, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {MatTab, MatTabGroup} from '@angular/material/tabs';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatInputModule} from '@angular/material/input';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatSelectModule} from '@angular/material/select';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BookService} from '../services/book.service';
import {AuthorService} from '../services/author.service';
import {CategoryService} from '../services/category.service';
import {LibraryBookService} from '../services/library-book.service';
import {Book} from '../interfaces/book';
import {Author} from '../interfaces/author';
import {Category} from '../interfaces/category';
import {Page} from '../interfaces/page';
import {LanguageWithCount} from '../interfaces/language-with-count';
import {CountryWithCount} from '../interfaces/country-with-count';
import {debounceTime, distinctUntilChanged, Subject, switchMap} from 'rxjs';
import {BooksGridComponent} from '../books-grid/books-grid.component';
import {MatSnackCommon} from '../common/mat-snack-common';
import {MatMenu, MatMenuContent, MatMenuItem} from '@angular/material/menu';
import {ActiveSort, SortOption} from '../interfaces/sort-config';
import {SortBarComponent} from '../common/sort-bar/sort-bar.component';
import {BaseBookFilters} from '../interfaces/filters';
import {BookFilterStore} from '../services/book-filter.store';
import {FilterShellComponent} from '../common/filter-shell/filter-shell.component';
import {TextFilterComponent} from '../common/filters/text-filter/text-filter.component';
import {AutocompleteFilterComponent} from '../common/filters/autocomplete-filter/autocomplete-filter.component';
import {RangeFilterComponent} from '../common/filters/range-filter/range-filter.component';
import {LanguageFilterComponent} from '../common/filters/language-filter/language-filter.component';
import {SelectFilterComponent, SelectOption} from '../common/filters/select-filter/select-filter.component';

interface BooksState {
  items: Book[];
  pagination: {
    pageSizeOptions: readonly number[];
    pageSize: number;
    totalElements: number;
    currentPage: number;
  };
  loading: boolean;
}

interface AuthorsState {
  items: Author[];
  pagination: {
    pageSize: number;
    totalElements: number;
    currentPage: number;
  };
  sorting: {
    options: SortOption[];
    active: ActiveSort[];
  };
  search: {
    name: string;
    loaded: boolean;
  };
  filter: {
    country: string | null;
    birthYearMin: number | null;
    birthYearMax: number | null;
    booksCountMin: number | null;
    booksCountMax: number | null;
  };
  loading: boolean;
}

interface CategoriesState {
  items: Category[];
  pagination: {
    pageSize: number;
    totalElements: number;
    currentPage: number;
  };
  sorting: {
    options: SortOption[];
    active: ActiveSort[];
  };
  search: {
    name: string;
    loaded: boolean;
  };
  filter: {
    booksCountMin: number | null;
    booksCountMax: number | null;
  };
  loading: boolean;
}

const EMPTY_BOOK_FILTERS: BaseBookFilters = {
  title: '',
  author: null,
  category: null,
  publishYear: {min: null, max: null},
  pages: {min: null, max: null},
  languages: []
};

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTabGroup,
    MatTab,
    MatPaginatorModule,
    MatProgressSpinner,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatAutocompleteModule,
    MatInputModule,
    MatTooltipModule,
    MatCheckboxModule,
    MatSelectModule,
    BooksGridComponent,
    MatMenu,
    MatMenuContent,
    MatMenuItem,
    SortBarComponent,
    FilterShellComponent,
    TextFilterComponent,
    AutocompleteFilterComponent,
    RangeFilterComponent,
    LanguageFilterComponent,
    SelectFilterComponent,
  ],
  templateUrl: './search.component.html',
  styleUrl: './search.component.scss'
})
export class SearchComponent implements OnInit {

  readonly DEFAULT_SORT_OPTIONS: SortOption[] = [
    {field: 'title', label: 'Назва'},
    {field: 'publishYear', label: 'Рік видання'},
    {field: 'language', label: 'Мова'},
    {field: 'pages', label: 'Сторінки'},
    {field: 'category.name', label: 'Категорія'},
  ];

  private readonly DEFAULT_DIRECTION = 'asc';
  private readonly SEARCH_DEBOUNCE = 450;
  private readonly SEARCH_PAGE_SIZE = 10;

  private libraryBookIds: Set<number> = new Set<number>();
  private snackCommon: MatSnackCommon;
  private booksSort: string[] | undefined;

  booksState: BooksState = {
    items: [],
    pagination: {
      pageSizeOptions: [15, 20, 25, 30, 35, 40, 45, 50] as const,
      pageSize: 15,
      totalElements: 0,
      currentPage: 0,
    },
    loading: false,
  };

  authorsState: AuthorsState = {
    items: [],
    pagination: {
      pageSize: 12,
      totalElements: 0,
      currentPage: 0,
    },
    sorting: {
      options: [
        {field: 'fullName', label: 'ПІБ'},
        {field: 'country', label: 'Країна'},
        {field: 'birthYear', label: 'Рік народження'},
        {field: 'booksCount', label: 'Кількість книг'},
      ],
      active: [],
    },
    search: {
      name: '',
      loaded: false,
    },
    filter: {
      country: null,
      birthYearMin: null,
      birthYearMax: null,
      booksCountMin: null,
      booksCountMax: null,
    },
    loading: false,
  };

  countries = signal<CountryWithCount[]>([]);

  categoriesState: CategoriesState = {
    items: [],
    pagination: {
      pageSize: 12,
      totalElements: 0,
      currentPage: 0,
    },
    sorting: {
      options: [
        {field: 'name', label: 'Назва'},
        {field: 'booksCount', label: 'Кількість книг'},
      ],
      active: [],
    },
    search: {
      name: '',
      loaded: false,
    },
    filter: {
      booksCountMin: null,
      booksCountMax: null,
    },
    loading: false,
  };

  readonly countryOptions = computed<SelectOption[]>(() =>
    this.countries().map(c => ({
      value: c.country,
      label: `${c.country} (${c.count})`
    }))
  );

  readonly bookFilters = new BookFilterStore<BaseBookFilters>(EMPTY_BOOK_FILTERS);

  authorSearchInput = signal('');
  filteredAuthors = signal<Author[]>([]);
  categorySearchInput = signal('');
  filteredCategories = signal<Category[]>([]);

  languages = signal<LanguageWithCount[]>([]);
  showAllLanguages = signal(false);

  private readonly subjects = {
    authorSearch: new Subject<string>(),
    categorySearch: new Subject<string>(),
    authorName: new Subject<string>(),
    categoryName: new Subject<string>(),
    authorRange: new Subject<void>(),
    categoryRange: new Subject<void>(),
  };

  uiState = {
    activeTabIndex: 0,
  };

  constructor(
    private bookService: BookService,
    private authorService: AuthorService,
    private categoryService: CategoryService,
    private libraryBookService: LibraryBookService,
    private router: Router,
    matSnackBar: MatSnackBar,
  ) {
    this.snackCommon = new MatSnackCommon(matSnackBar);
  }

  ngOnInit(): void {
    this.loadBooks();
    this.loadLanguages();
    this.setupSearchSubscriptions();
  }

  //region Books Tab Methods

  onBooksPageChange(event: PageEvent): void {
    this.booksState.pagination.currentPage = event.pageIndex;
    this.booksState.pagination.pageSize = event.pageSize;
    this.loadBooks();
  }

  onBooksSortChange(sort: string[] | undefined): void {
    this.booksSort = sort;
    this.booksState.pagination.currentPage = 0;
    this.loadBooks();
  }

  addBookToLibrary(book: Book): void {
    this.libraryBookService.addBook(book.id).subscribe({
      next: () => {
        this.libraryBookIds.add(book.id);
        this.snackCommon.showSuccess('Книгу додано до бібліотеки');
      },
      error: err => {
        this.snackCommon.showError(err);
        if (err.status === 400) {
          this.libraryBookIds.add(book.id);
        }
      }
    });
  }

  isBookInLibrary(book: Book): boolean {
    return this.libraryBookIds.has(book.id);
  }

  private loadBooks(): void {
    this.booksState.loading = true;
    const filters = this.bookFilters.state();

    this.bookService.getAll({
      page: this.booksState.pagination.currentPage,
      size: this.booksState.pagination.pageSize,
      sort: this.booksSort,
      authorId: filters.author?.id,
      categoryId: filters.category?.id,
      title: filters.title || undefined,
      publishYearMin: filters.publishYear.min ?? undefined,
      publishYearMax: filters.publishYear.max ?? undefined,
      pagesMin: filters.pages.min ?? undefined,
      pagesMax: filters.pages.max ?? undefined,
      languages: filters.languages.length > 0 ? filters.languages : undefined,
    }).subscribe({
      next: (page: Page<Book>) => {
        this.booksState.items = page.content;
        this.booksState.pagination.totalElements = page.page.totalElements;
        this.booksState.loading = false;
      },
      error: () => {
        this.booksState.loading = false;
      }
    });
  }

  private loadLanguages(): void {
    this.bookService.getLanguages().subscribe({
      next: (languages: LanguageWithCount[]) => {
        this.languages.set(languages);
      }
    });
  }

  //endregion

  //region Authors Tab Methods

  onTabChange(index: number): void {
    this.uiState.activeTabIndex = index;
    if (index === 1 && !this.authorsState.search.loaded) {
      this.authorsState.search.loaded = true;
      this.loadAuthors();
      this.loadAuthorCountries();
    }
    if (index === 2 && !this.categoriesState.search.loaded) {
      this.categoriesState.search.loaded = true;
      this.loadCategories();
    }
  }

  onAuthorPageChange(event: PageEvent): void {
    this.authorsState.pagination.currentPage = event.pageIndex;
    this.authorsState.pagination.pageSize = event.pageSize;
    this.loadAuthors();
  }

  onAuthorSortClick(option: SortOption): void {
    const existingIndex = this.authorsState.sorting.active.findIndex(s => s.field === option.field);

    if (existingIndex === -1) {
      this.authorsState.sorting.active.push({field: option.field, direction: this.DEFAULT_DIRECTION});
    } else {
      const existing = this.authorsState.sorting.active[existingIndex];
      if (existing.direction === 'asc') {
        existing.direction = 'desc';
      } else {
        this.authorsState.sorting.active.splice(existingIndex, 1);
      }
    }

    this.authorsState.pagination.currentPage = 0;
    this.loadAuthors();
  }

  isAuthorSorted(option: SortOption): boolean {
    return this.authorsState.sorting.active.some(s => s.field === option.field);
  }

  getAuthorSortDirection(option: SortOption): 'asc' | 'desc' | undefined {
    const sort = this.authorsState.sorting.active.find(s => s.field === option.field);
    return sort?.direction;
  }

  getAuthorSortPriority(option: SortOption): number {
    return this.authorsState.sorting.active.findIndex(s => s.field === option.field) + 1;
  }

  hasAuthorSorts(): boolean {
    return this.authorsState.sorting.active.length > 0;
  }

  clearAuthorSorts(): void {
    this.authorsState.sorting.active = [];
    this.authorsState.pagination.currentPage = 0;
    this.loadAuthors();
  }

  onAuthorNameSearch(): void {
    this.subjects.authorName.next(this.authorsState.search.name);
  }

  clearAuthorNameSearch(): void {
    this.authorsState.search.name = '';
    this.authorsState.pagination.currentPage = 0;
    this.loadAuthors();
  }

  getBooksCountText(count: number): string {
    if (count === 0) return '0 книг';
    if (count === 1) return '1 книга';
    if (count >= 2 && count <= 4) return `${count} книги`;
    return `${count} книг`;
  }

  goToAuthorDetails(author: Author): void {
    this.router.navigate(['/author-details'], {state: {id: author.id}});
  }

  showAuthorBooks(author: Author): void {
    this.uiState.activeTabIndex = 0;
    this.onAuthorSelected(author);
  }

  loadAuthors(): void {
    this.authorsState.loading = true;
    const sort = this.buildAuthorSortParam();

    this.authorService.search({
      name: this.authorsState.search.name,
      page: this.authorsState.pagination.currentPage,
      size: this.authorsState.pagination.pageSize,
      sort: sort,
      country: this.authorsState.filter.country ?? undefined,
      birthYearMin: this.authorsState.filter.birthYearMin ?? undefined,
      birthYearMax: this.authorsState.filter.birthYearMax ?? undefined,
      booksCountMin: this.authorsState.filter.booksCountMin ?? undefined,
      booksCountMax: this.authorsState.filter.booksCountMax ?? undefined,
    }).subscribe({
      next: (page: Page<Author>) => {
        this.authorsState.items = page.content;
        this.authorsState.pagination.totalElements = page.page.totalElements;
        this.authorsState.loading = false;
      },
      error: () => {
        this.authorsState.loading = false;
      }
    });
  }

  private buildAuthorSortParam(): string[] | undefined {
    if (this.authorsState.sorting.active.length === 0) {
      return undefined;
    }
    return this.authorsState.sorting.active.map(s => `${s.field};${s.direction}`);
  }

  private loadAuthorCountries(): void {
    this.authorService.getCountries().subscribe({
      next: (countries: CountryWithCount[]) => {
        this.countries.set(countries);
      }
    });
  }

  onAuthorCountrySelected(country: string | null): void {
    this.authorsState.filter.country = country;
    this.authorsState.pagination.currentPage = 0;
    this.loadAuthors();
  }

  clearAuthorCountryFilter(): void {
    this.authorsState.filter.country = null;
    this.authorsState.pagination.currentPage = 0;
    this.loadAuthors();
  }

  onAuthorBirthYearMinChange(): void {
    this.subjects.authorRange.next();
  }

  onAuthorBirthYearMaxChange(): void {
    this.subjects.authorRange.next();
  }

  onAuthorBooksCountMinChange(): void {
    this.subjects.authorRange.next();
  }

  onAuthorBooksCountMaxChange(): void {
    this.subjects.authorRange.next();
  }

  clearAuthorBirthYearFilters(): void {
    this.authorsState.filter.birthYearMin = null;
    this.authorsState.filter.birthYearMax = null;
    this.authorsState.pagination.currentPage = 0;
    this.loadAuthors();
  }

  clearAuthorBooksCountFilters(): void {
    this.authorsState.filter.booksCountMin = null;
    this.authorsState.filter.booksCountMax = null;
    this.authorsState.pagination.currentPage = 0;
    this.loadAuthors();
  }

  hasAuthorFilters(): boolean {
    return this.authorsState.filter.country !== null
      || this.authorsState.filter.birthYearMin !== null
      || this.authorsState.filter.birthYearMax !== null
      || this.authorsState.filter.booksCountMin !== null
      || this.authorsState.filter.booksCountMax !== null;
  }

  clearAllAuthorFilters(): void {
    this.authorsState.filter.country = null;
    this.authorsState.filter.birthYearMin = null;
    this.authorsState.filter.birthYearMax = null;
    this.authorsState.filter.booksCountMin = null;
    this.authorsState.filter.booksCountMax = null;
    this.authorsState.search.name = '';
    this.authorsState.pagination.currentPage = 0;
    this.loadAuthors();
  }

  //endregion

  //region Categories Tab Methods

  onCategoryPageChange(event: PageEvent): void {
    this.categoriesState.pagination.currentPage = event.pageIndex;
    this.categoriesState.pagination.pageSize = event.pageSize;
    this.loadCategories();
  }

  onCategorySortClick(option: SortOption): void {
    const existingIndex = this.categoriesState.sorting.active.findIndex(s => s.field === option.field);

    if (existingIndex === -1) {
      this.categoriesState.sorting.active.push({field: option.field, direction: this.DEFAULT_DIRECTION});
    } else {
      const existing = this.categoriesState.sorting.active[existingIndex];
      if (existing.direction === 'asc') {
        existing.direction = 'desc';
      } else {
        this.categoriesState.sorting.active.splice(existingIndex, 1);
      }
    }

    this.categoriesState.pagination.currentPage = 0;
    this.loadCategories();
  }

  isCategorySorted(option: SortOption): boolean {
    return this.categoriesState.sorting.active.some(s => s.field === option.field);
  }

  getCategorySortDirection(option: SortOption): 'asc' | 'desc' | undefined {
    const sort = this.categoriesState.sorting.active.find(s => s.field === option.field);
    return sort?.direction;
  }

  getCategorySortPriority(option: SortOption): number {
    return this.categoriesState.sorting.active.findIndex(s => s.field === option.field) + 1;
  }

  hasCategorySorts(): boolean {
    return this.categoriesState.sorting.active.length > 0;
  }

  clearCategorySorts(): void {
    this.categoriesState.sorting.active = [];
    this.categoriesState.pagination.currentPage = 0;
    this.loadCategories();
  }

  onCategoryNameSearch(): void {
    this.subjects.categoryName.next(this.categoriesState.search.name);
  }

  clearCategoryNameSearch(): void {
    this.categoriesState.search.name = '';
    this.categoriesState.pagination.currentPage = 0;
    this.loadCategories();
  }

  goToCategoryBooks(category: Category): void {
    this.uiState.activeTabIndex = 0;
    this.onCategorySelected(category);
  }

  loadCategories(): void {
    this.categoriesState.loading = true;
    const sort = this.buildCategorySortParam();

    this.categoryService.search({
      name: this.categoriesState.search.name,
      page: this.categoriesState.pagination.currentPage,
      size: this.categoriesState.pagination.pageSize,
      sort: sort,
      booksCountMin: this.categoriesState.filter.booksCountMin ?? undefined,
      booksCountMax: this.categoriesState.filter.booksCountMax ?? undefined,
    }).subscribe({
      next: (page: Page<Category>) => {
        this.categoriesState.items = page.content;
        this.categoriesState.pagination.totalElements = page.page.totalElements;
        this.categoriesState.loading = false;
      },
      error: () => {
        this.categoriesState.loading = false;
      }
    });
  }

  onCategoryBooksCountMinChange(): void {
    this.subjects.categoryRange.next();
  }

  onCategoryBooksCountMaxChange(): void {
    this.subjects.categoryRange.next();
  }

  clearCategoryBooksCountFilters(): void {
    this.categoriesState.filter.booksCountMin = null;
    this.categoriesState.filter.booksCountMax = null;
    this.categoriesState.pagination.currentPage = 0;
    this.loadCategories();
  }

  hasCategoryFilters(): boolean {
    return this.categoriesState.filter.booksCountMin !== null
      || this.categoriesState.filter.booksCountMax !== null;
  }

  clearAllCategoryFilters(): void {
    this.categoriesState.filter.booksCountMin = null;
    this.categoriesState.filter.booksCountMax = null;
    this.categoriesState.search.name = '';
    this.categoriesState.pagination.currentPage = 0;
    this.loadCategories();
  }

  private buildCategorySortParam(): string[] | undefined {
    if (this.categoriesState.sorting.active.length === 0) {
      return undefined;
    }
    return this.categoriesState.sorting.active.map(s => `${s.field};${s.direction}`);
  }

  //endregion

  //region Filter Methods

  private setupSearchSubscriptions(): void {
    this.subjects.authorSearch.pipe(
      debounceTime(this.SEARCH_DEBOUNCE),
      distinctUntilChanged(),
      switchMap(query => this.authorService.search({name: query, page: 0, size: this.SEARCH_PAGE_SIZE}))
    ).subscribe(page => {
      this.filteredAuthors.set(page.content);
    });

    this.subjects.categorySearch.pipe(
      debounceTime(this.SEARCH_DEBOUNCE),
      distinctUntilChanged(),
      switchMap(query => this.categoryService.search({name: query, page: 0, size: this.SEARCH_PAGE_SIZE}))
    ).subscribe(page => {
      this.filteredCategories.set(page.content);
    });

    this.subjects.authorName.pipe(
      debounceTime(this.SEARCH_DEBOUNCE),
      distinctUntilChanged()
    ).subscribe(() => {
      this.authorsState.pagination.currentPage = 0;
      this.loadAuthors();
    });

    this.subjects.categoryName.pipe(
      debounceTime(this.SEARCH_DEBOUNCE),
      distinctUntilChanged()
    ).subscribe(() => {
      this.categoriesState.pagination.currentPage = 0;
      this.loadCategories();
    });

    this.subjects.authorRange.pipe(
      debounceTime(this.SEARCH_DEBOUNCE)
    ).subscribe(() => {
      this.authorsState.pagination.currentPage = 0;
      this.loadAuthors();
    });

    this.subjects.categoryRange.pipe(
      debounceTime(this.SEARCH_DEBOUNCE)
    ).subscribe(() => {
      this.categoriesState.pagination.currentPage = 0;
      this.loadCategories();
    });

    this.bookFilters.filtersChanged$.subscribe(() => {
      this.booksState.pagination.currentPage = 0;
      this.loadBooks();
    });
  }

  onAuthorSearchInput(val: string): void {
    this.authorSearchInput.set(val);
    this.subjects.authorSearch.next(val);
  }

  onCategorySearchInput(val: string): void {
    this.categorySearchInput.set(val);
    this.subjects.categorySearch.next(val);
  }

  onAuthorSelected(author: Author): void {
    this.bookFilters.update('author', author);
    this.authorSearchInput.set(author.fullName);
    this.filteredAuthors.set([]);
  }

  onCategorySelected(category: Category): void {
    this.bookFilters.update('category', category);
    this.categorySearchInput.set(category.name);
    this.filteredCategories.set([]);
  }

  clearAuthorFilter(): void {
    this.bookFilters.update('author', null);
    this.authorSearchInput.set('');
    this.filteredAuthors.set([]);
  }

  clearCategoryFilter(): void {
    this.bookFilters.update('category', null);
    this.categorySearchInput.set('');
    this.filteredCategories.set([]);
  }

  hasActiveFilters(): boolean {
    const f = this.bookFilters.state();
    return f.author !== null
      || f.category !== null
      || f.title !== ''
      || f.publishYear.min !== null || f.publishYear.max !== null
      || f.pages.min !== null || f.pages.max !== null
      || f.languages.length > 0;
  }

  toggleLanguage(language: string): void {
    const current = [...this.bookFilters.state().languages];
    const index = current.indexOf(language);
    if (index === -1) {
      current.push(language);
    } else {
      current.splice(index, 1);
    }
    this.bookFilters.update('languages', current);
  }

  clearAllFilters(): void {
    this.bookFilters.reset(EMPTY_BOOK_FILTERS);
    this.authorSearchInput.set('');
    this.categorySearchInput.set('');
  }

  //endregion

}
