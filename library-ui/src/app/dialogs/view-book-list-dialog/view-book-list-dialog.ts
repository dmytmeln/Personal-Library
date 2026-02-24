import {AfterViewInit, Component, Inject, OnDestroy, OnInit, viewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule,} from '@angular/material/dialog';
import {LibraryBook} from '../../interfaces/library-book';
import {Book} from '../../interfaces/book';
import {MatButton} from '@angular/material/button';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {MatPaginator, MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {FormsModule} from '@angular/forms';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {debounceTime, distinctUntilChanged, Observable, Subject, Subscription} from 'rxjs';
import {Router} from '@angular/router';
import {Page} from '../../interfaces/page';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
  selector: 'app-view-book-list-dialog',
  imports: [
    MatButton,
    MatTableModule,
    MatFormField,
    MatInput,
    MatLabel,
    MatPaginatorModule,
    FormsModule,
    MatSortModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatDialogModule,
  ],
  templateUrl: './view-book-list-dialog.html',
  styleUrl: './view-book-list-dialog.scss'
})
export class ViewBookListDialog implements AfterViewInit, OnInit, OnDestroy {

  readonly SIZE: number = 10;
  private readonly TITLE_MAX_LENGTH = 25;

  totalElements: number = 0;
  private currentPage: number = 0;
  searchText: string = '';
  private sortParams?: string[];
  loading: boolean = false;

  sort = viewChild.required<MatSort>(MatSort);
  paginator = viewChild.required<MatPaginator>(MatPaginator);

  displayedColumns: string[] = ['title', 'publishYear', 'language', 'pages', 'actions'];
  dataSource = new MatTableDataSource<LibraryBook | Book>();

  private searchSubject = new Subject<string>();
  private searchSubscription?: Subscription;

  constructor(
    @Inject(MAT_DIALOG_DATA) protected readonly data: ViewBookListDialogData,
    private router: Router,
  ) {
    this.displayedColumns.splice(1, 0, data.categoryColumn);
  }

  ngOnInit(): void {
    this.searchSubscription = this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(value => {
      this.searchText = value;
      this.currentPage = 0;
      if (this.paginator()) {
        this.paginator().pageIndex = 0;
      }
      this.getBooks();
    });
    this.getBooks();
  }

  ngAfterViewInit() {
    this.sort().sortChange.subscribe(() => {
      this.currentPage = 0;
      if (this.paginator) {
        this.paginator().pageIndex = 0;
      }
      this.updateSortParams();
      this.getBooks();
    });
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }

  private updateSortParams(): void {
    if (this.sort().active && this.sort().direction) {
      this.sortParams = [`${this.sort().active};${this.sort().direction}`];
    } else {
      this.sortParams = undefined;
    }
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

  truncateTitle(title: string): string {
    if (title.length <= this.TITLE_MAX_LENGTH) {
      return title;
    }
    return title.substring(0, this.TITLE_MAX_LENGTH) + '...';
  }

  shouldShowTooltip(title: string): boolean {
    return title.length > this.TITLE_MAX_LENGTH;
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.searchSubject.next(filterValue.trim().toLowerCase());
  }

  onPageChange(e: PageEvent) {
    this.currentPage = e.pageIndex;
    this.getBooks();
  }

  goToBookDetails(book: Book) {
    this.router.navigate(['book-details'], {state: book}).then(() => {
    });
  }

  private getBooks() {
    this.loading = true;
    this.data.fetchBooksFn({
      page: this.currentPage,
      size: this.SIZE,
      sort: this.sortParams,
      title: this.searchText || undefined
    }).subscribe({
      next: (page) => {
        this.totalElements = page.page.totalElements;
        this.dataSource.data = page.content;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

}

export interface ViewBookListDialogData {
  libraryBooks: ReadonlyArray<LibraryBook>;
  categoryColumn: string;
  fetchBooksFn: (options: {
    page: number,
    size: number,
    sort?: string[],
    title?: string
  }) => Observable<Page<Book> | Page<LibraryBook>>;
}
