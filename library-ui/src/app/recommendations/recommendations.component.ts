import {Component, DestroyRef, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RecommendationService} from '../services/recommendation.service';
import {Book} from '../interfaces/book';
import {BookCardComponent} from '../book-card/book-card.component';
import {BookListItemComponent} from '../book-list-item/book-list-item.component';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {finalize, forkJoin} from 'rxjs';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatIconModule} from '@angular/material/icon';
import {SelectionStore} from '../services/selection.store';
import {LibraryBookService} from '../services/library-book.service';
import {MatSnackCommon} from '../common/mat-snack-common';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BulkActionBarComponent} from '../common/bulk-action-bar/bulk-action-bar.component';
import {MatButtonModule} from '@angular/material/button';
import {MatMenuModule} from '@angular/material/menu';
import {MatExpansionModule} from '@angular/material/expansion';

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [
    CommonModule,
    BookCardComponent,
    BookListItemComponent,
    TranslocoDirective,
    MatProgressSpinner,
    MatButtonToggleModule,
    MatIconModule,
    BulkActionBarComponent,
    MatButtonModule,
    MatMenuModule,
    MatExpansionModule,
  ],
  templateUrl: './recommendations.component.html',
  styleUrl: './recommendations.component.scss'
})
export class RecommendationsComponent implements OnInit {

  private snackCommon: MatSnackCommon;

  personalized = signal<Book[]>([]);
  popular = signal<Book[]>([]);
  newArrivals = signal<Book[]>([]);

  loading = signal<boolean>(true);
  viewMode = signal<'grid' | 'list'>('grid');

  readonly selection = new SelectionStore();
  private libraryBookIds: Set<number> = new Set<number>();

  constructor(
    private recommendationService: RecommendationService,
    private translocoService: TranslocoService,
    private libraryBookService: LibraryBookService,
    private destroyRef: DestroyRef,
    matSnackBar: MatSnackBar,
  ) {
    this.snackCommon = new MatSnackCommon(matSnackBar);
  }

  ngOnInit(): void {
    this.setupLanguageSubscription();
    this.loadAll();
  }

  addBookToLibrary(book: Book): void {
    this.libraryBookService.addBook(book.id).subscribe({
      next: () => {
        this.libraryBookIds.add(book.id);
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.bookAdded'));
      },
      error: err => {
        this.snackCommon.showError(err);
        if (err.status === 400) {
          this.libraryBookIds.add(book.id);
        }
      }
    });
  }

  bulkAddBooks(): void {
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

  isBookInLibrary(book: Book): boolean {
    return this.libraryBookIds.has(book.id);
  }

  private setupLanguageSubscription(): void {
    this.translocoService.langChanges$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.loadAll();
      });
  }

  private loadAll(): void {
    this.loading.set(true);

    forkJoin({
      personalized: this.recommendationService.getPersonalized(20),
      popular: this.recommendationService.getPopular(20),
      newArrivals: this.recommendationService.getNewArrivals(20)
    }).pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: (results) => {
        this.personalized.set(results.personalized);
        this.popular.set(results.popular);
        this.newArrivals.set(results.newArrivals);
      },
      error: (err) => {
        console.error('Error loading recommendations', err);
      }
    });
  }

}
