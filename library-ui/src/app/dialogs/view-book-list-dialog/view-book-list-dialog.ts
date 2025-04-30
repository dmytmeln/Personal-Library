import {AfterViewInit, Component, Inject, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogTitle,} from '@angular/material/dialog';
import {UserBook} from '../../interfaces/user-book';
import {Book} from '../../interfaces/book';
import {MatButton} from '@angular/material/button';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {BookService} from '../../services/book.service';
import {FormsModule} from '@angular/forms';
import {MatSort, MatSortModule} from '@angular/material/sort';

@Component({
  selector: 'app-view-book-list-dialog',
  imports: [
    MatDialogTitle,
    MatDialogActions,
    MatButton,
    MatTableModule,
    MatFormField,
    MatInput,
    MatLabel,
    MatPaginatorModule,
    FormsModule,
    MatDialogClose,
    MatSortModule,
  ],
  templateUrl: './view-book-list-dialog.html',
  styleUrl: './view-book-list-dialog.scss'
})
export class ViewBookListDialog implements AfterViewInit {

  totalElements: number = 0;
  page: number = 0;
  size: number = 10;
  searchText: string = '';

  @ViewChild(MatSort) sort!: MatSort;
  displayedColumns: string[] = ['title', 'categoryName', 'publishYear', 'language', 'pages', 'actions'];
  dataSource = new MatTableDataSource<Book>();

  constructor(
    @Inject(MAT_DIALOG_DATA) private readonly userBooks: ReadonlyArray<UserBook>,
    private bookService: BookService,
  ) {
  }

  ngAfterViewInit() {
    this.dataSource.sort = this.sort;
    this.getBooks();
  }

  onDetails(book: Book): void {
    // todo: go to book details
  }

  isBookInLibrary(book: Book): boolean {
    return this.userBooks.some(userBook => userBook.book.id === book.id);
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  onPageChange(e: PageEvent) {
    this.page = e.pageIndex;
    this.size = e.pageSize;
    this.getBooks();
  }

  private getBooks() {
    this.bookService.getAll(this.page, this.size).subscribe(value => {
      this.totalElements = value.totalElements;
      this.dataSource.data = value.content;
      this.searchText = ''
    });
  }

}
