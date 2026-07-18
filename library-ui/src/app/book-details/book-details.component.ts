import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Book} from '../interfaces/book';
import {CommonModule, NgOptimizedImage} from '@angular/common';
import {BookRatingComponent} from '../book-rating/book-rating.component';
import {BOOK_DETAIL_TYPES, BookDetails} from '../interfaces/book-details';
import {LibraryBook} from '../interfaces/library-book';
import {BookService} from '../services/book.service';
import {MatAnchor, MatButton, MatIconButton} from '@angular/material/button';
import {LibraryBookService} from '../services/library-book.service';
import {BasicCollection} from '../interfaces/basic-collection';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSnackCommon} from '../common/mat-snack-common';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MatIcon} from '@angular/material/icon';
import {MatTooltip} from '@angular/material/tooltip';
import {RecommendationService} from '../services/recommendation.service';
import {BookCardComponent} from '../book-card/book-card.component';
import {SelectionStore} from '../services/selection.store';
import {BookListItemComponent} from '../book-list-item/book-list-item.component';
import {BulkActionBarComponent} from '../common/bulk-action-bar/bulk-action-bar.component';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatMenuModule} from '@angular/material/menu';
import {filter, skip} from 'rxjs';
import {QuoteService} from '../services/quote.service';
import {Quote} from '../interfaces/quote';
import {MatDialog} from '@angular/material/dialog';
import {QuoteFormDialogComponent} from '../dialogs/quote-form-dialog/quote-form-dialog.component';
import {ConfirmationDialogComponent} from '../dialogs/confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-book-details',
  imports: [
    CommonModule,
    NgOptimizedImage,
    BookRatingComponent,
    MatButton,
    MatAnchor,
    TranslocoDirective,
    MatIcon,
    MatTooltip,
    BookCardComponent,
    BookListItemComponent,
    BulkActionBarComponent,
    MatButtonToggleModule,
    MatMenuModule,
    MatIconButton,
  ],
  templateUrl: './book-details.component.html',
  styleUrl: './book-details.component.scss'
})
export class BookDetailsComponent implements OnInit {

  private readonly snackCommon: MatSnackCommon;
  private readonly destroyRef = inject(DestroyRef);

  bookId!: number;
  bookDetails?: BookDetails;
  similarBooks = signal<Book[]>([]);
  quotes = signal<Quote[]>([]);
  viewMode = signal<'grid' | 'list'>('grid');
  readonly selection = new SelectionStore();
  private readonly libraryBookIds: Set<number> = new Set<number>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly bookService: BookService,
    private readonly libraryBookService: LibraryBookService,
    private readonly recommendationService: RecommendationService,
    private readonly quoteService: QuoteService,
    matSnackBar: MatSnackBar,
    private readonly translocoService: TranslocoService,
    private readonly dialog: MatDialog,
  ) {
    this.snackCommon = new MatSnackCommon(matSnackBar);
  }

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.bookId = Number(id);
        this.loadAll();
      } else {
        void this.router.navigate(['/']);
      }
    });

    this.translocoService.langChanges$.pipe(skip(1), takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      if (this.bookId) {
        this.loadAll();
      }
    });
  }

  get libraryBook(): LibraryBook | undefined {
    return this.bookDetails?.type === BOOK_DETAIL_TYPES.LIBRARY ? this.bookDetails.libraryBook : undefined;
  }

  get collections(): BasicCollection[] {
    return this.bookDetails?.type === BOOK_DETAIL_TYPES.LIBRARY ? this.bookDetails.collections : [];
  }

  get displayBook(): Book | undefined {
    if (!this.bookDetails) return undefined;
    return this.bookDetails.type === BOOK_DETAIL_TYPES.LIBRARY ? this.bookDetails.libraryBook.book : this.bookDetails.book;
  }

  get authors(): Array<[number, string]> {
    return Object.entries(this.displayBook?.authors ?? {}) as {} as Array<[number, string]>;
  }

  get myRating(): number {
    return this.libraryBook?.rating ?? 0;
  }

  addBookToLibrary(): void {
    this.libraryBookService.addBook(this.bookId).subscribe({
      next: (libraryBook) => {
        if (this.bookDetails) {
          this.bookDetails = {
            type: BOOK_DETAIL_TYPES.LIBRARY,
            libraryBook: libraryBook,
            collections: [],
            averageRating: this.bookDetails.averageRating,
            ratingsNumber: this.bookDetails.ratingsNumber
          };
        }
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.bookAdded'));
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  addSimilarBookToLibrary(book: Book): void {
    this.libraryBookService.addBook(book.id).subscribe({
      next: () => {
        this.libraryBookIds.add(book.id);
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.bookAdded'));
      },
      error: (err) => {
        this.snackCommon.showError(err);
        if (err.status === 400) {
          this.libraryBookIds.add(book.id);
        }
      }
    });
  }

  bulkAddSimilarBooks(): void {
    const ids = this.selection.selectedIds();
    this.libraryBookService.bulkAdd(ids).subscribe({
      next: () => {
        ids.forEach(id => this.libraryBookIds.add(id));
        this.selection.clear();
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.bookAdded'));
      },
      error: err => this.snackCommon.showError(err)
    });
  }

  isSimilarBookInLibrary(book: Book): boolean {
    return this.libraryBookIds.has(book.id);
  }

  goToCollection(collection: BasicCollection): void {
    void this.router.navigate(['/collections', collection.id]);
  }

  goToCategoryDetails(): void {
    if (this.displayBook?.categoryId) {
      void this.router.navigate(['/category-details', this.displayBook.categoryId]);
    }
  }

  goToAuthorDetails(id: string | number): void {
    void this.router.navigate(['/author-details', Number(id)]);
  }

  changeRating(rating: number): void {
    const libraryBookId = this.libraryBook?.id;
    if (!libraryBookId) return;

    this.libraryBookService.changeRating(libraryBookId, rating).subscribe({
      next: (libraryBook) => {
        if (this.bookDetails?.type === BOOK_DETAIL_TYPES.LIBRARY) {
          this.bookDetails.libraryBook = libraryBook;
        }
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.ratingChanged'));
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  private loadAll(): void {
    this.loadBookDetails();
    this.loadSimilarBooks();
    this.selection.clear();
  }

  private loadBookDetails(): void {
    this.bookService.getBookDetails(this.bookId).subscribe(bookDetails => {
      this.bookDetails = bookDetails;
      this.loadQuotes();
    });
  }

  private loadQuotes(): void {
    const libraryBookId = this.libraryBook?.id;
    if (libraryBookId) {
      this.quoteService.getByLibraryBookId(libraryBookId).subscribe(quotes => {
        this.quotes.set(quotes);
      });
    } else {
      this.quotes.set([]);
    }
  }

  openAddQuoteDialog(): void {
    this.openQuoteDialog();
  }

  editQuote(quote: Quote): void {
    this.openQuoteDialog(quote);
  }

  private openQuoteDialog(quote?: Quote): void {
    const libraryBookId = this.libraryBook?.id;
    if (!libraryBookId) return;

    const dialogRef = this.dialog.open(QuoteFormDialogComponent, {
      data: quote ? {libraryBookId, quote} : {libraryBookId},
      width: '500px'
    });

    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe(() => {
      this.loadQuotes();
      this.snackCommon.showSuccess(this.translocoService.translate('library.success.quoteSaved'));
    });
  }

  deleteQuote(quote: Quote): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        message: this.translocoService.translate('dialogs.quotes.deleteConfirm'),
        confirmLabel: 'common.delete'
      }
    });

    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe(() => {
      this.quoteService.delete(quote.id).subscribe(() => {
        this.loadQuotes();
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.quoteDeleted'));
      });
    });
  }

  private loadSimilarBooks(): void {
    this.recommendationService.getSimilar(this.bookId, 10).subscribe(books => {
      this.similarBooks.set(books);
    });
  }

}
