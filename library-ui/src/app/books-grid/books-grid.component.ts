import {Component, EventEmitter, input, Input, Output, TemplateRef} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {Book} from '../interfaces/book';
import {BookComponent} from '../book/book.component';
import {MatMenuPanel} from '@angular/material/menu';

interface SortOption {
  field: string;
  label: string;
}

interface ActiveSort {
  field: string;
  direction: 'asc' | 'desc';
}

const DEFAULT_SORT_OPTIONS: SortOption[] = [
  {field: 'title', label: 'Назва'},
  {field: 'publishYear', label: 'Рік видання'},
  {field: 'language', label: 'Мова'},
  {field: 'pages', label: 'Сторінки'},
  {field: 'category.name', label: 'Категорія'},
];

const DEFAULT_PAGE_SIZE_OPTIONS = [15, 20, 25, 30, 35, 40, 45, 50] as const;

@Component({
  selector: 'app-books-grid',
  standalone: true,
  imports: [
    CommonModule,
    MatPaginatorModule,
    MatProgressSpinner,
    MatIconModule,
    MatButtonModule,
    BookComponent,
  ],
  templateUrl: './books-grid.component.html',
  styleUrl: './books-grid.component.scss'
})
export class BooksGridComponent {

  private readonly DEFAULT_DIRECTION: 'asc' | 'desc' = 'asc';

  readonly sortOptions = DEFAULT_SORT_OPTIONS;
  readonly pageSizeOptions = DEFAULT_PAGE_SIZE_OPTIONS;

  private _activeSorts: ActiveSort[] = [];

  // todo use input() and output()

  @Input() books: Book[] = [];
  @Input() totalElements = 0;
  @Input() pageSize = 15;
  @Input() pageIndex = 0;
  @Input() loading = false;
  actionsMenu = input<MatMenuPanel<any> | null>(null);

  @Output() pageChange = new EventEmitter<PageEvent>();
  @Output() sortChange = new EventEmitter<string[] | undefined>();

  onSortClick(option: SortOption): void {
    const existingIndex = this._activeSorts.findIndex(s => s.field === option.field);

    if (existingIndex === -1) {
      this._activeSorts.push({field: option.field, direction: this.DEFAULT_DIRECTION});
    } else {
      this.toggleSortDirection(existingIndex);
    }

    this.emitSortChange();
  }

  onPageChange(event: PageEvent): void {
    this.pageChange.emit(event);
  }

  isIncludedInSorting(option: SortOption): boolean {
    return this._activeSorts.some(s => s.field === option.field);
  }

  getSortDirection(option: SortOption): 'asc' | 'desc' | undefined {
    const sort = this._activeSorts.find(s => s.field === option.field);
    return sort?.direction;
  }

  getSortPriority(option: SortOption): number {
    return this._activeSorts.findIndex(s => s.field === option.field) + 1;
  }

  hasActiveSorts(): boolean {
    return this._activeSorts.length > 0;
  }

  clearAllSorts(): void {
    this._activeSorts = [];
    this.emitSortChange();
  }

  private toggleSortDirection(existingIndex: number): void {
    const existing = this._activeSorts[existingIndex];
    if (existing.direction === 'asc') {
      existing.direction = 'desc';
    } else {
      this._activeSorts.splice(existingIndex, 1);
    }
  }

  private emitSortChange(): void {
    if (this._activeSorts.length === 0) {
      this.sortChange.emit(undefined);
    } else {
      const sortParams = this._activeSorts.map(s => `${s.field};${s.direction}`);
      this.sortChange.emit(sortParams);
    }
  }

}
