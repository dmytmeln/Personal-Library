import {AfterViewInit, Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogTitle,} from '@angular/material/dialog';
import {LibraryBook} from '../../interfaces/library-book';
import {Book} from '../../interfaces/book';
import {MatButton} from '@angular/material/button';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {FormsModule} from '@angular/forms';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {Observable} from 'rxjs';
import {Router} from '@angular/router';
import {Page} from '../../interfaces/page';

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
export class ViewBookListDialog implements AfterViewInit, OnInit {

  readonly SIZE: number = 10;

  totalElements: number = 0;
  private currentPage: number = 0;
  searchText: string = '';

  @ViewChild(MatSort) sort!: MatSort;
  displayedColumns: string[] = ['title', 'categoryName', 'publishYear', 'language', 'pages', 'actions'];
  dataSource = new MatTableDataSource<LibraryBook | Book>();

  constructor(
    @Inject(MAT_DIALOG_DATA) private readonly data: ViewBookListDialogData,
    private router: Router,
  ) {
  }

  ngOnInit(): void {
    this.getBooks();
  }

  ngAfterViewInit() {
    this.dataSource.sort = this.sort;
  }

  isBookInLibrary(book: Book): boolean {
    return this.data.libraryBooks.some(libraryBook => libraryBook.book.id === book.id);
  }

  getItemId(item: Book | LibraryBook): number {
    return item.id;
  }

  getBookFromItem(item: Book | LibraryBook): Book {
    return 'book' in item ? item.book : item;
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  onPageChange(e: PageEvent) {
    this.searchText = '';
    this.dataSource.filter = '';
    this.currentPage = e.pageIndex;
    this.getBooks();
  }

  goToBookDetails(book: Book) {
    this.router.navigate(['book-details'], {state: book}).then(() => {
    });
  }

  private getBooks() {
    this.data.fetchBooksFn(this.currentPage, this.SIZE).subscribe(page => {
      this.totalElements = page.page.totalElements;
      this.dataSource.data = page.content;
    });
  }

}

export interface ViewBookListDialogData {
  libraryBooks: ReadonlyArray<LibraryBook>;
  fetchBooksFn: (page: number, size: number) => Observable<Page<Book> | Page<LibraryBook>>;
}
