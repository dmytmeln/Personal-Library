import {Component, input, output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {Book} from '../interfaces/book';
import {BookComponent} from '../book/book.component';
import {MatMenuPanel} from '@angular/material/menu';

const DEFAULT_PAGE_SIZE_OPTIONS = [15, 20, 25, 30, 35, 40, 45, 50] as const;

@Component({
  selector: 'app-books-grid',
  standalone: true,
  imports: [
    CommonModule,
    MatPaginatorModule,
    MatProgressSpinner,
    BookComponent,
  ],
  templateUrl: './books-grid.component.html',
  styleUrl: './books-grid.component.scss'
})
export class BooksGridComponent {

  readonly pageSizeOptions = DEFAULT_PAGE_SIZE_OPTIONS;

  books = input<Book[]>([]);
  totalElements = input<number>(0);
  pageSize = input<number>(15);
  pageIndex = input<number>(0);
  loading = input<boolean>(false);
  actionsMenu = input<MatMenuPanel<any> | null>(null);

  pageChange = output<PageEvent>();

  onPageChange(event: PageEvent): void {
    this.pageChange.emit(event);
  }

}
