import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {Author} from '../interfaces/author';
import {Router} from '@angular/router';
import {BookService} from '../services/book.service';
import {AuthorService} from '../services/author.service';
import {Book} from '../interfaces/book';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {LibraryBookService} from '../services/library-book.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSnackCommon} from '../common/mat-snack-common';

@Component({
  selector: 'app-author-details',
  imports: [
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
  ],
  templateUrl: './author-details.component.html',
  styleUrl: './author-details.component.scss'
})
export class AuthorDetailsComponent implements OnInit, AfterViewInit {

  readonly SIZE = 10;

  private authorId: number;
  private libraryBookIds: Set<number> = new Set<number>();
  private snackCommon: MatSnackCommon;

  author!: Author;
  dataSource: MatTableDataSource<Book> = new MatTableDataSource<Book>();
  displayedColumns: string[] = ['title', 'categoryName', 'publishYear', 'language', 'pages', 'actions'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private router: Router,
    private matSnackBar: MatSnackBar,
    private bookService: BookService,
    private authorService: AuthorService,
    private libraryBookService: LibraryBookService,
  ) {
    this.authorId = this.router.getCurrentNavigation()?.extras?.state?.['id'] as number;
    this.snackCommon = new MatSnackCommon(matSnackBar);
  }

  ngOnInit() {
    this.initAuthors();
    this.initBooks();
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  goToBookDetails(book: Book) {
    this.router.navigate(['/book-details'], {state: book}).then(() => {
    });
  }

  addBookToLibrary(book: Book) {
    this.libraryBookService.addBook(book.id).subscribe({
      next: () => {
        this.libraryBookIds.add(book.id);
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

  private initAuthors(): void {
    // todo: author should be fetched together with his books
    this.authorService.getById(this.authorId).subscribe(author => {
      this.author = author;
    });
  }

  private initBooks(): void {
    this.bookService.getAll({authorId: this.authorId}).subscribe(page => {
      this.dataSource.data = page.content;
    });
  }

}
