import {Component, input, OnInit, output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CategoryService} from '../services/category.service';
import {Category} from '../interfaces/category';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {FilterShellComponent} from '../common/filter-shell/filter-shell.component';
import {TextFilterComponent} from '../common/filters/text-filter/text-filter.component';
import {RangeFilterComponent} from '../common/filters/range-filter/range-filter.component';
import {SortBarComponent} from '../common/sort-bar/sort-bar.component';
import {SortOption} from '../interfaces/sort-config';
import {CategoryFilters} from '../interfaces/filters';
import {EntityFilterStore} from '../services/entity-filter.store';

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
    TextFilterComponent,
    RangeFilterComponent,
    SortBarComponent,
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

  readonly categorySortOptions: SortOption[] = [
    {field: 'name', label: 'Назва'},
    {field: 'booksCount', label: 'Кількість книг'},
  ];

  constructor(private categoryService: CategoryService) {}

  ngOnInit(): void {
    this.loadCategories();
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

  loadCategories(): void {
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
      ? this.categoryService.searchMe(options)
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
    this.filters.filtersChanged$.subscribe(() => {
      this.categoriesState.currentPage = 0;
      this.loadCategories();
    });
  }

  getBooksCountText(count: number): string {
    if (count === 0) return '0 книг';
    if (count === 1) return '1 книга';
    if (count >= 2 && count <= 4) return `${count} книги`;
    return `${count} книг`;
  }

  onShowBooks(category: Category): void {
    this.categoryBooks.emit(category);
  }
}
