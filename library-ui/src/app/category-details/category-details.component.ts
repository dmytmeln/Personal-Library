import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {Category} from '../interfaces/category';
import {Router} from '@angular/router';
import {BookService} from '../services/book.service';
import {CategoryService} from '../services/category.service';
import {Book} from '../interfaces/book';
import {MatIconModule} from '@angular/material/icon';
import {MatMenuModule} from '@angular/material/menu';
import {LibraryBookService} from '../services/library-book.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSnackCommon} from '../common/mat-snack-common';
import {BooksDisplayComponent} from '../books-display/books-display.component';
import {PageEvent} from '@angular/material/paginator';
import {SortBarComponent} from '../common/sort-bar/sort-bar.component';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {SelectionStore} from '../services/selection.store';
import {BulkActionBarComponent} from '../common/bulk-action-bar/bulk-action-bar.component';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {takeUntilDestroyed, toSignal} from '@angular/core/rxjs-interop';
import {map} from 'rxjs';

@Component({
  selector: 'app-category-details',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatMenuModule,
    BooksDisplayComponent,
    SortBarComponent,
    MatButtonToggleModule,
    MatButtonModule,
    BulkActionBarComponent,
    TranslocoDirective,
  ],
  templateUrl: './category-details.component.html',
  styleUrl: './category-details.component.scss'
})
export class CategoryDetailsComponent implements OnInit {

  private translocoService = inject(TranslocoService);
  private destroyRef = inject(DestroyRef);

  protected readonly bookSortOptions = toSignal(
    this.translocoService.selectTranslateObject('search.sort').pipe(
      map(t => [
        {field: 'title', label: t.title},
        {field: 'publishYear', label: t.publishYear},
        {field: 'language', label: t.language},
        {field: 'pages', label: t.pages},
      ])
    ),
    {initialValue: []}
  );

  private categoryId: number;
  private libraryBookIds: Set<number> = new Set<number>();
  private snackCommon: MatSnackCommon;
  private currentSort: string[] | undefined;

  category!: Category;
  books: Book[] = [];
  totalElements = 0;
  pageSize = 15;
  pageIndex = 0;
  loading = false;
  readonly viewMode = signal<'grid' | 'list'>('grid');
  readonly selection = new SelectionStore();

  constructor(
    private router: Router,
    private bookService: BookService,
    private categoryService: CategoryService,
    private libraryBookService: LibraryBookService,
    matSnackBar: MatSnackBar,
  ) {
    this.categoryId = this.router.getCurrentNavigation()?.extras?.state?.['id'] as number;
    this.snackCommon = new MatSnackCommon(matSnackBar);
  }

  ngOnInit() {
    if (!this.categoryId) {
      this.router.navigate(['/']);
      return;
    }

    this.translocoService.langChanges$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.initCategory();
      this.loadBooks();
    });
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

  private initCategory(): void {
    this.categoryService.getById(this.categoryId).subscribe(category => {
      this.category = category;
    });
  }

  private loadBooks(): void {
    this.loading = true;
    this.bookService.getAll({
      categoryId: this.categoryId,
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
