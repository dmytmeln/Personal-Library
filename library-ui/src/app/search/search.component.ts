import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
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

  books: Book[] = [];
  readonly pageSizeOptions = [12, 24, 48] as const;
  pageSize = 12;
  totalElements = 0;
  currentPage = 0;
  isLoading = false;

  activeTabIndex = 0;

  readonly sortOptions: SortOption[] = [
    {field: 'title', label: 'Назва'},
    {field: 'publishYear', label: 'Рік видання'},
    {field: 'language', label: 'Мова'},
    {field: 'pages', label: 'Сторінки'},
    {field: 'category.name', label: 'Категорія'},
  ];

  activeSorts: ActiveSort[] = [];

  authorSearchInput = '';
  categorySearchInput = '';
  filteredAuthors: Author[] = [];
  filteredCategories: Category[] = [];
  selectedAuthor: Author | null = null;
  selectedCategory: Category | null = null;

  private authorSearchSubject = new Subject<string>();
  private categorySearchSubject = new Subject<string>();

  constructor(
    private bookService: BookService,
    private authorService: AuthorService,
    private categoryService: CategoryService,
  ) {
  }

  ngOnInit(): void {
    this.loadBooks();
    this.setupSearchSubscriptions();
  }

  private setupSearchSubscriptions(): void {
    this.authorSearchSubject.pipe(
      debounceTime(this.SEARCH_DEBOUNCE),
      distinctUntilChanged(),
      switchMap(query => this.authorService.search(query, 0, this.SEARCH_PAGE_SIZE))
    ).subscribe(page => {
      this.filteredAuthors = page.content;
    });

    this.categorySearchSubject.pipe(
      debounceTime(this.SEARCH_DEBOUNCE),
      distinctUntilChanged(),
      switchMap(query => this.categoryService.search(query, 0, this.SEARCH_PAGE_SIZE))
    ).subscribe(page => {
      this.filteredCategories = page.content;
    });
  }

  onAuthorSearchInput(): void {
    this.authorSearchSubject.next(this.authorSearchInput);
  }

  onCategorySearchInput(): void {
    this.categorySearchSubject.next(this.categorySearchInput);
  }

  onAuthorSelected(author: Author): void {
    this.selectedAuthor = author;
    this.authorSearchInput = author.fullName;
    this.filteredAuthors = [];
    this.currentPage = 0;
    this.loadBooks();
  }

  onCategorySelected(category: Category): void {
    this.selectedCategory = category;
    this.categorySearchInput = category.name;
    this.filteredCategories = [];
    this.currentPage = 0;
    this.loadBooks();
  }

  clearAuthorFilter(): void {
    this.selectedAuthor = null;
    this.authorSearchInput = '';
    this.filteredAuthors = [];
    this.currentPage = 0;
    this.loadBooks();
  }

  clearCategoryFilter(): void {
    this.selectedCategory = null;
    this.categorySearchInput = '';
    this.filteredCategories = [];
    this.currentPage = 0;
    this.loadBooks();
  }

  hasActiveFilters(): boolean {
    return this.selectedAuthor !== null || this.selectedCategory !== null;
  }

  clearAllFilters(): void {
    this.clearAuthorFilter();
    this.clearCategoryFilter();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadBooks();
  }

  onSortClick(option: SortOption): void {
    const existingIndex = this.activeSorts.findIndex(s => s.field === option.field);

    if (existingIndex === -1) {
      this.activeSorts.push({field: option.field, direction: this.DEFAULT_DIRECTION});
    } else {
      this.toggleSortDirection(existingIndex);
    }

    this.currentPage = 0;
    this.loadBooks();
  }

  isIncludedInSorting(option: SortOption): boolean {
    return this.activeSorts.some(s => s.field === option.field);
  }

  getSortDirection(option: SortOption): 'asc' | 'desc' | undefined {
    const sort = this.activeSorts.find(s => s.field === option.field);
    return sort?.direction;
  }

  getSortPriority(option: SortOption): number {
    return this.activeSorts.findIndex(s => s.field === option.field) + 1;
  }

  hasActiveSorts(): boolean {
    return this.activeSorts.length > 0;
  }

  clearAllSorts(): void {
    this.activeSorts = [];
    this.currentPage = 0;
    this.loadBooks();
  }

  private toggleSortDirection(existingIndex: number): void {
    const existing = this.activeSorts[existingIndex];
    if (existing.direction === 'asc') {
      existing.direction = 'desc';
    } else {
      this.activeSorts.splice(existingIndex, 1);
    }
  }

  private loadBooks(): void {
    this.isLoading = true;
    const sort = this.buildSortParam();
    const authorId = this.selectedAuthor?.id;
    const categoryId = this.selectedCategory?.id;

    this.bookService.getAll(this.currentPage, this.pageSize, sort, authorId, categoryId).subscribe({
      next: (page: Page<Book>) => {
        this.books = page.content;
        this.totalElements = page.page.totalElements;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  private buildSortParam(): string[] | undefined {
    if (this.activeSorts.length === 0) {
      return undefined;
    }
    return this.activeSorts.map(s => `${s.field};${s.direction}`);
  }

}
