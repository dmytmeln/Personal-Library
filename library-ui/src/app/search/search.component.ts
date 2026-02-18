import {Component, OnInit} from '@angular/core';
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
import {BookService} from '../services/book.service';
import {AuthorService} from '../services/author.service';
import {CategoryService} from '../services/category.service';
import {Book} from '../interfaces/book';
import {Author} from '../interfaces/author';
import {Category} from '../interfaces/category';
import {Page} from '../interfaces/page';
import {BookComponent} from '../book/book.component';
import {debounceTime, distinctUntilChanged, Subject, switchMap} from 'rxjs';

interface SortOption {
  field: string;
  label: string;
}

interface ActiveSort {
  field: string;
  direction: 'asc' | 'desc';
}

interface BooksState {
  items: Book[];
  pagination: {
    pageSizeOptions: readonly number[];
    pageSize: number;
    totalElements: number;
    currentPage: number;
  };
  sorting: {
    options: SortOption[];
    active: ActiveSort[];
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
  loading: boolean;
}

interface FilterState {
  author: {
    searchInput: string;
    selected: Author | null;
    filtered: Author[];
  };
  category: {
    searchInput: string;
    selected: Category | null;
    filtered: Category[];
  };
}

interface SearchSubjects {
  authorFilter: Subject<string>;
  categoryFilter: Subject<string>;
  authorName: Subject<string>;
}

interface UiState {
  activeTabIndex: number;
}

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
    BookComponent,
  ],
  templateUrl: './search.component.html',
  styleUrl: './search.component.scss'
})
export class SearchComponent implements OnInit {

  private readonly DEFAULT_DIRECTION = 'asc';
  private readonly SEARCH_DEBOUNCE = 450;
  private readonly SEARCH_PAGE_SIZE = 10;

  booksState: BooksState = {
    items: [],
    pagination: {
      pageSizeOptions: [12, 24, 48] as const,
      pageSize: 12,
      totalElements: 0,
      currentPage: 0,
    },
    sorting: {
      options: [
        {field: 'title', label: 'Назва'},
        {field: 'publishYear', label: 'Рік видання'},
        {field: 'language', label: 'Мова'},
        {field: 'pages', label: 'Сторінки'},
        {field: 'category.name', label: 'Категорія'},
      ],
      active: [],
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
    loading: false,
  };

  filterState: FilterState = {
    author: {
      searchInput: '',
      selected: null,
      filtered: [],
    },
    category: {
      searchInput: '',
      selected: null,
      filtered: [],
    },
  };

  searchSubjects: SearchSubjects = {
    authorFilter: new Subject<string>(),
    categoryFilter: new Subject<string>(),
    authorName: new Subject<string>(),
  };

  uiState: UiState = {
    activeTabIndex: 0,
  };

  constructor(
    private bookService: BookService,
    private authorService: AuthorService,
    private categoryService: CategoryService,
    private router: Router,
  ) {
  }

  ngOnInit(): void {
    this.loadBooks();
    this.setupSearchSubscriptions();
  }

  //region Books Tab Methods

  onPageChange(event: PageEvent): void {
    this.booksState.pagination.currentPage = event.pageIndex;
    this.booksState.pagination.pageSize = event.pageSize;
    this.loadBooks();
  }

  onSortClick(option: SortOption): void {
    const existingIndex = this.booksState.sorting.active.findIndex(s => s.field === option.field);

    if (existingIndex === -1) {
      this.booksState.sorting.active.push({field: option.field, direction: this.DEFAULT_DIRECTION});
    } else {
      this.toggleBookSortDirection(existingIndex);
    }

    this.booksState.pagination.currentPage = 0;
    this.loadBooks();
  }

  isIncludedInSorting(option: SortOption): boolean {
    return this.booksState.sorting.active.some(s => s.field === option.field);
  }

  getSortDirection(option: SortOption): 'asc' | 'desc' | undefined {
    const sort = this.booksState.sorting.active.find(s => s.field === option.field);
    return sort?.direction;
  }

  getSortPriority(option: SortOption): number {
    return this.booksState.sorting.active.findIndex(s => s.field === option.field) + 1;
  }

  hasActiveSorts(): boolean {
    return this.booksState.sorting.active.length > 0;
  }

  clearAllSorts(): void {
    this.booksState.sorting.active = [];
    this.booksState.pagination.currentPage = 0;
    this.loadBooks();
  }

  private toggleBookSortDirection(existingIndex: number): void {
    const existing = this.booksState.sorting.active[existingIndex];
    if (existing.direction === 'asc') {
      existing.direction = 'desc';
    } else {
      this.booksState.sorting.active.splice(existingIndex, 1);
    }
  }

  private loadBooks(): void {
    this.booksState.loading = true;
    const sort = this.buildSortParam();
    const authorId = this.filterState.author.selected?.id;
    const categoryId = this.filterState.category.selected?.id;

    this.bookService.getAll({
      page: this.booksState.pagination.currentPage,
      size: this.booksState.pagination.pageSize,
      sort: sort,
      authorId: authorId,
      categoryId: categoryId
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

  private buildSortParam(): string[] | undefined {
    if (this.booksState.sorting.active.length === 0) {
      return undefined;
    }
    return this.booksState.sorting.active.map(s => `${s.field};${s.direction}`);
  }

  //endregion

  //region Authors Tab Methods

  onTabChange(index: number): void {
    this.uiState.activeTabIndex = index;
    if (index === 1 && !this.authorsState.search.loaded) {
      this.authorsState.search.loaded = true;
      this.loadAuthors();
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
    this.searchSubjects.authorName.next(this.authorsState.search.name);
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
    this.filterState.author.selected = author;
    this.filterState.author.searchInput = author.fullName;
    this.booksState.pagination.currentPage = 0;
    this.loadBooks();
  }

  private loadAuthors(): void {
    this.authorsState.loading = true;
    const sort = this.buildAuthorSortParam();

    this.authorService.search(
      this.authorsState.search.name,
      this.authorsState.pagination.currentPage,
      this.authorsState.pagination.pageSize,
      sort
    ).subscribe({
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

  //endregion

  //region Filter Methods

  private setupSearchSubscriptions(): void {
    this.searchSubjects.authorFilter.pipe(
      debounceTime(this.SEARCH_DEBOUNCE),
      distinctUntilChanged(),
      switchMap(query => this.authorService.search(query, 0, this.SEARCH_PAGE_SIZE))
    ).subscribe(page => {
      this.filterState.author.filtered = page.content;
    });

    this.searchSubjects.categoryFilter.pipe(
      debounceTime(this.SEARCH_DEBOUNCE),
      distinctUntilChanged(),
      switchMap(query => this.categoryService.search(query, 0, this.SEARCH_PAGE_SIZE))
    ).subscribe(page => {
      this.filterState.category.filtered = page.content;
    });

    this.searchSubjects.authorName.pipe(
      debounceTime(this.SEARCH_DEBOUNCE),
      distinctUntilChanged()
    ).subscribe(() => {
      this.authorsState.pagination.currentPage = 0;
      this.loadAuthors();
    });
  }

  onAuthorSearchInput(): void {
    this.searchSubjects.authorFilter.next(this.filterState.author.searchInput);
  }

  onCategorySearchInput(): void {
    this.searchSubjects.categoryFilter.next(this.filterState.category.searchInput);
  }

  onAuthorSelected(author: Author): void {
    this.filterState.author.selected = author;
    this.filterState.author.searchInput = author.fullName;
    this.filterState.author.filtered = [];
    this.booksState.pagination.currentPage = 0;
    this.loadBooks();
  }

  onCategorySelected(category: Category): void {
    this.filterState.category.selected = category;
    this.filterState.category.searchInput = category.name;
    this.filterState.category.filtered = [];
    this.booksState.pagination.currentPage = 0;
    this.loadBooks();
  }

  clearAuthorFilter(): void {
    this.filterState.author.selected = null;
    this.filterState.author.searchInput = '';
    this.filterState.author.filtered = [];
    this.booksState.pagination.currentPage = 0;
    this.loadBooks();
  }

  clearCategoryFilter(): void {
    this.filterState.category.selected = null;
    this.filterState.category.searchInput = '';
    this.filterState.category.filtered = [];
    this.booksState.pagination.currentPage = 0;
    this.loadBooks();
  }

  hasActiveFilters(): boolean {
    return this.filterState.author.selected !== null || this.filterState.category.selected !== null;
  }

  clearAllFilters(): void {
    this.clearAuthorFilter();
    this.clearCategoryFilter();
  }

  //endregion

}
