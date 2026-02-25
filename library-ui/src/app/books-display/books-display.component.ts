import {Component, input, output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {Book} from '../interfaces/book';
import {BookCardComponent} from '../book-card/book-card.component';
import {BookListItemComponent} from '../book-list-item/book-list-item.component';
import {MatMenuPanel} from '@angular/material/menu';
import {LibraryBook} from '../interfaces/library-book';
import {SelectionStore} from '../services/selection.store';

const DEFAULT_PAGE_SIZE_OPTIONS = [15, 20, 25, 30, 35, 40, 45, 50] as const;

@Component({
  selector: 'app-books-display',
  standalone: true,
  imports: [
    CommonModule,
    MatPaginatorModule,
    MatProgressSpinner,
    BookCardComponent,
    BookListItemComponent,
  ],
  templateUrl: './books-display.component.html',
  styleUrl: './books-display.component.scss'
})
export class BooksDisplayComponent {

  readonly pageSizeOptions = DEFAULT_PAGE_SIZE_OPTIONS;

  books = input<(Book | LibraryBook)[]>([]);
  totalElements = input<number>(0);
  pageSize = input<number>(15);
  pageIndex = input<number>(0);
  loading = input<boolean>(false);
  actionsMenu = input<MatMenuPanel<any> | null>(null);
  viewMode = input<'grid' | 'list'>('grid');

  selectionStore = input<SelectionStore | null>(null);

  pageChange = output<PageEvent>();

  onPageChange(event: PageEvent): void {
    this.pageChange.emit(event);
  }

  getBook(item: Book | LibraryBook): Book {
    return 'book' in item ? item.book : (item as Book);
  }

  getMenuData(item: Book | LibraryBook): Record<string, Book | LibraryBook> {
    return 'book' in item
      ? {libraryBook: item}
      : {$implicit: item};
  }

}
