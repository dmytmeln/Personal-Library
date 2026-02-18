import {Component, OnInit} from '@angular/core';
import {Author} from '../interfaces/author';
import {Router} from '@angular/router';
import {BookService} from '../services/book.service';
import {AuthorService} from '../services/author.service';
import {Book} from '../interfaces/book';
import {MatIconModule} from '@angular/material/icon';
import {MatMenuModule} from '@angular/material/menu';
import {LibraryBookService} from '../services/library-book.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSnackCommon} from '../common/mat-snack-common';
import {BooksGridComponent} from '../books-grid/books-grid.component';
import {PageEvent} from '@angular/material/paginator';

@Component({
  selector: 'app-author-details',
  imports: [
    MatIconModule,
    MatMenuModule,
    BooksGridComponent,
  ],
  templateUrl: './author-details.component.html',
  styleUrl: './author-details.component.scss'
})
export class AuthorDetailsComponent implements OnInit {

  private authorId: number;
  private libraryBookIds: Set<number> = new Set<number>();
  private snackCommon: MatSnackCommon;
  private currentSort: string[] | undefined;

  author!: Author;
  books: Book[] = [];
  totalElements = 0;
  pageSize = 15;
  pageIndex = 0;
  loading = false;

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
    this.initAuthor();
    this.loadBooks();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadBooks();
  }

  onSortChange(sort: string[] | undefined): void {
    this.currentSort = sort;
    this.pageIndex = 0;
    this.loadBooks();
  }

  addBookToLibrary(book: Book): void {
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

  private initAuthor(): void {
    this.authorService.getById(this.authorId).subscribe(author => {
      this.author = author;
    });
  }

  private loadBooks(): void {
    this.loading = true;
    this.bookService.getAll({
      authorId: this.authorId,
      page: this.pageIndex,
      size: this.pageSize,
      sort: this.currentSort
    }).subscribe({
      next: page => {
        this.books = page.content;
        this.totalElements = page.page.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

}
