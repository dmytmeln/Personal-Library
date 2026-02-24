import {Component, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatTab, MatTabGroup} from '@angular/material/tabs';
import {PageEvent} from '@angular/material/paginator';
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
import {BooksGridComponent} from '../books-grid/books-grid.component';
import {MatSnackCommon} from '../common/mat-snack-common';
import {MatMenu, MatMenuContent, MatMenuItem} from '@angular/material/menu';
import {SortOption} from '../interfaces/sort-config';
import {SortBarComponent} from '../common/sort-bar/sort-bar.component';
import {BaseBookFilters} from '../interfaces/filters';
import {EntityFilterStore} from '../services/entity-filter.store';
import {AutocompleteSearchStore} from '../services/autocomplete-search.store';
import {FilterShellComponent} from '../common/filter-shell/filter-shell.component';
import {TextFilterComponent} from '../common/filters/text-filter/text-filter.component';
import {AutocompleteFilterComponent} from '../common/filters/autocomplete-filter/autocomplete-filter.component';
import {RangeFilterComponent} from '../common/filters/range-filter/range-filter.component';
import {LanguageFilterComponent} from '../common/filters/language-filter/language-filter.component';
import {AuthorListComponent} from '../author-list/author-list.component';
import {CategoryListComponent} from '../category-list/category-list.component';

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
    AuthorListComponent,
    CategoryListComponent,
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

  readonly filters = new EntityFilterStore<BaseBookFilters>(EMPTY_BOOK_FILTERS);

  readonly authorSearch = new AutocompleteSearchStore<Author>(
    (q, p, s) => this.authorService.search({name: q, page: p, size: s}),
    450,
    10
  );
  readonly categorySearch = new AutocompleteSearchStore<Category>(
    (q, p, s) => this.categoryService.search({name: q, page: p, size: s}),
    450,
    10
  );

  languages = signal<LanguageWithCount[]>([]);
  showAllLanguages = signal(false);

  uiState = {
    activeTabIndex: 0,
  };

  constructor(
    private bookService: BookService,
    private authorService: AuthorService,
    private categoryService: CategoryService,
    private libraryBookService: LibraryBookService,
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
    const filters = this.filters.state();

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

  onTabChange(index: number): void {
    this.uiState.activeTabIndex = index;
  }

  showAuthorBooks(author: Author): void {
    this.uiState.activeTabIndex = 0;
    this.onAuthorSelected(author);
  }

  goToCategoryBooks(category: Category): void {
    this.uiState.activeTabIndex = 0;
    this.onCategorySelected(category);
  }

  //region Filter Methods

  private setupSearchSubscriptions(): void {
    this.filters.filtersChanged$.subscribe(() => {
      this.booksState.pagination.currentPage = 0;
      this.loadBooks();
    });
  }

  onAuthorSearchInput(val: string): void {
    this.authorSearch.search(val);
  }

  onCategorySearchInput(val: string): void {
    this.categorySearch.search(val);
  }

  onAuthorSelected(author: Author): void {
    this.filters.update('author', author);
    this.authorSearch.clear();
  }

  onCategorySelected(category: Category): void {
    this.filters.update('category', category);
    this.categorySearch.clear();
  }

  clearAuthorFilter(): void {
    this.filters.update('author', null);
    this.authorSearch.clear();
  }

  clearCategoryFilter(): void {
    this.filters.update('category', null);
    this.categorySearch.clear();
  }

  hasActiveFilters(): boolean {
    return this.filters.hasActiveFilters(f => {
      return f.author !== null
        || f.category !== null
        || f.title !== ''
        || f.publishYear.min !== null || f.publishYear.max !== null
        || f.pages.min !== null || f.pages.max !== null
        || f.languages.length > 0;
    });
  }

  toggleLanguage(language: string): void {
    const current = [...this.filters.state().languages];
    const index = current.indexOf(language);
    if (index === -1) {
      current.push(language);
    } else {
      current.splice(index, 1);
    }
    this.filters.update('languages', current);
  }

  clearAllFilters(): void {
    this.filters.reset(EMPTY_BOOK_FILTERS);
    this.authorSearch.clear();
    this.categorySearch.clear();
  }

  //endregion

}
