import {Component, DestroyRef, inject, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {Book} from '../interfaces/book';
import {NgOptimizedImage} from '@angular/common';
import {BookRatingComponent} from '../book-rating/book-rating.component';
import {BookDetails} from '../interfaces/book-details';
import {BookService} from '../services/book.service';
import {MatAnchor, MatButton} from '@angular/material/button';
import {LibraryBookService} from '../services/library-book.service';
import {BasicCollection} from '../interfaces/basic-collection';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSnackCommon} from '../common/mat-snack-common';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MatIcon} from '@angular/material/icon';
import {MatTooltip} from '@angular/material/tooltip';

@Component({
  selector: 'app-book-details',
  imports: [
    NgOptimizedImage,
    BookRatingComponent,
    MatButton,
    MatAnchor,
    TranslocoDirective,
    MatIcon,
    MatTooltip,
  ],
  templateUrl: './book-details.component.html',
  styleUrl: './book-details.component.scss'
})
export class BookDetailsComponent implements OnInit {

  private snackCommon: MatSnackCommon;
  private destroyRef = inject(DestroyRef);

  bookId: number;
  bookDetails?: BookDetails;

  constructor(
    private router: Router,
    private bookService: BookService,
    private libraryBookService: LibraryBookService,
    matSnackBar: MatSnackBar,
    private translocoService: TranslocoService,
  ) {
    const state = this.router.getCurrentNavigation()?.extras?.state as { id: number };
    this.bookId = state?.id;
    this.snackCommon = new MatSnackCommon(matSnackBar);
  }

  ngOnInit(): void {
    if (!this.bookId) {
      this.router.navigate(['/']);
      return;
    }

    this.translocoService.langChanges$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.loadBookDetails();
    });
  }

  get displayBook(): Book | undefined {
    return this.bookDetails?.libraryBook?.book ?? this.bookDetails?.book;
  }

  get authors(): Array<[number, string]> {
    return Object.entries(this.displayBook?.authors ?? {}) as {} as Array<[number, string]>;
  }

  get myRating(): number {
    return this.bookDetails?.libraryBook?.rating ?? 0;
  }

  addBookToLibrary(): void {
    this.libraryBookService.addBook(this.bookId).subscribe({
      next: (libraryBook) => {
        if (this.bookDetails) {
          this.bookDetails.libraryBook = libraryBook;
          this.bookDetails.book = undefined;
        }
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.bookAdded'));
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  goToCollection(collection: BasicCollection): void {
    this.router.navigate(['/collections', collection.id]);
  }

  goToAuthorDetails(id: number): void {
    this.router.navigate(['/author-details'], {state: {id}});
  }

  changeRating(rating: number): void {
    const libraryBookId = this.bookDetails?.libraryBook?.id;
    if (!libraryBookId) return;

    this.libraryBookService.changeRating(libraryBookId, rating).subscribe({
      next: (libraryBook) => {
        if (this.bookDetails) {
          this.bookDetails.libraryBook = libraryBook;
        }
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.ratingChanged'));
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  private loadBookDetails(): void {
    this.bookService.getBookDetails(this.bookId).subscribe(bookDetails => {
      this.bookDetails = bookDetails;
    });
  }

}
