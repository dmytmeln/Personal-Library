import {Component, computed, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {takeUntilDestroyed, toSignal} from '@angular/core/rxjs-interop';
import {LibraryBookService} from '../services/library-book.service';
import {LIBRARY_BOOK_STATUSES, LibraryBook, LibraryBookStatus} from '../interfaces/library-book';
import {MatTab, MatTabGroup} from '@angular/material/tabs';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatDialog} from '@angular/material/dialog';
import {ViewBookListDialog, ViewBookListDialogData} from '../dialogs/view-book-list-dialog/view-book-list-dialog';
import {BookService} from '../services/book.service';
import {LibraryBookMenuItemsComponent} from '../library-book-menu-items/library-book-menu-items.component';
import {MatMenuModule} from '@angular/material/menu';
import {CollectionService} from '../services/collection.service';
import {CollectionBookService} from '../services/collection-book.service';
import {DeleteLibraryBookDialog} from '../dialogs/delete-library-book-dialog/delete-library-book-dialog';
import {MatSnackCommon} from '../common/mat-snack-common';
import {MatSnackBar} from '@angular/material/snack-bar';
import {
  CollectionSelectorDialogComponent,
  CollectionSelectorDialogData
} from '../dialogs/collection-selector-dialog/collection-selector-dialog.component';
import {filter, map} from 'rxjs';
import {SelectedCollection} from '../interfaces/selected-collection';
import {ConfirmationDialogComponent} from '../dialogs/confirmation-dialog/confirmation-dialog.component';
import {
  LibraryBookDetailsDialogComponent,
  LibraryBookDetailsDialogData,
  LibraryBookDetailsDialogResult
} from '../dialogs/library-book-details-dialog/library-book-details-dialog.component';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {EntityFilterStore} from '../services/entity-filter.store';
import {AutocompleteSearchStore} from '../services/autocomplete-search.store';
import {LibraryFilters} from '../interfaces/filters';
import {Author} from '../interfaces/author';
import {Category} from '../interfaces/category';
import {LanguageWithCount} from '../interfaces/language-with-count';
import {PageEvent} from '@angular/material/paginator';
import {LibraryAuthorService} from '../services/library-author.service';
import {LibraryCategoryService} from '../services/library-category.service';
import {CommonModule} from '@angular/common';
import {
  FilterShellComponent,
  FooterFiltersDirective,
  MainFiltersDirective,
  SecondaryFiltersDirective,
  TopRowFiltersDirective
} from '../common/filter-shell/filter-shell.component';
import {TextFilterComponent} from '../common/filters/text-filter/text-filter.component';
import {AutocompleteFilterComponent} from '../common/filters/autocomplete-filter/autocomplete-filter.component';
import {RangeFilterComponent} from '../common/filters/range-filter/range-filter.component';
import {LanguageFilterComponent} from '../common/filters/language-filter/language-filter.component';
import {SortBarComponent} from '../common/sort-bar/sort-bar.component';
import {BooksDisplayComponent} from '../books-display/books-display.component';
import {MatSelectModule} from '@angular/material/select';
import {FormsModule} from '@angular/forms';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {SelectFilterComponent} from '../common/filters/select-filter/select-filter.component';
import {AuthorListComponent} from '../author-list/author-list.component';
import {CategoryListComponent} from '../category-list/category-list.component';
import {UpdateLibraryBookDetails} from '../interfaces/update-library-book-details';
import {BulkActionBarComponent} from '../common/bulk-action-bar/bulk-action-bar.component';
import {SelectionStore} from '../services/selection.store';
import {NoteDialogComponent} from '../dialogs/note-dialog/note-dialog.component';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {LibraryStore} from '../services/library.store';

const EMPTY_LIBRARY_FILTERS: LibraryFilters = {
  title: '',
  author: null,
  category: null,
  publishYear: {min: null, max: null},
  pages: {min: null, max: null},
  languages: [],
  status: null,
  rating: {min: null, max: null}
};

@Component({
  selector: 'app-library',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTabGroup,
    MatTab,
    MatButtonModule,
    MatIconModule,
    MatExpansionModule,
    LibraryBookMenuItemsComponent,
    MatMenuModule,
    FilterShellComponent,
    TopRowFiltersDirective,
    MainFiltersDirective,
    SecondaryFiltersDirective,
    FooterFiltersDirective,
    TextFilterComponent,
    AutocompleteFilterComponent,
    RangeFilterComponent,
    LanguageFilterComponent,
    SortBarComponent,
    BooksDisplayComponent,
    MatSelectModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    SelectFilterComponent,
    AuthorListComponent,
    CategoryListComponent,
    BulkActionBarComponent,
    MatButtonToggleModule,
    TranslocoDirective,

  ],
  templateUrl: './library.component.html',
  styleUrl: './library.component.scss',
})
export class LibraryComponent implements OnInit {

  private readonly translocoService = inject(TranslocoService);

  readonly bookSortOptions = toSignal(
    this.translocoService.selectTranslateObject('library.sort').pipe(
      map(t => [
        {field: 'title', label: t.title},
        {field: 'publishYear', label: t.publishYear},
        {field: 'rating', label: t.rating},
        {field: 'addedAt', label: t.addedAt},
        {field: 'pages', label: t.pages},
      ])
    ),
    {initialValue: []}
  );

  booksState = {
    items: [] as LibraryBook[],
    totalElements: 0,
    pageSize: 15,
    currentPage: 0,
    loading: false,
    sort: undefined as string[] | undefined,
  };

  readonly statusOptions = toSignal(
    this.translocoService.selectTranslateObject('library.statuses').pipe(
      map(t => LIBRARY_BOOK_STATUSES.map(s => ({
        value: s,
        label: t[s]
      })))
    ),
    {initialValue: []}
  );

  readonly selection = new SelectionStore();
  readonly filters = new EntityFilterStore<LibraryFilters>(EMPTY_LIBRARY_FILTERS);
  readonly viewMode = signal<'grid' | 'list'>('grid');

  readonly isFiltersExpanded = signal(false);
  readonly activeFiltersCount = computed(() => {
    const f = this.filters.state();
    let count = 0;
    if (f.status) count++;
    if (f.author) count++;
    if (f.category) count++;
    if (f.publishYear.min || f.publishYear.max) count++;
    if (f.pages.min || f.pages.max) count++;
    if (f.languages.length > 0) count++;
    if (f.rating.min || f.rating.max) count++;
    return count;
  });

  readonly authorSearch = new AutocompleteSearchStore<Author>(
    (q, p, s) => this.libraryAuthorService.getAll({name: q, page: p, size: s}),
    450,
    10
  );
  readonly categorySearch = new AutocompleteSearchStore<Category>(
    (q, p, s) => this.libraryCategoryService.getAll({name: q, page: p, size: s}),
    450,
    10
  );

  languages = signal<LanguageWithCount[]>([]);
  showAllLanguages = signal(false);

  uiState = {
    activeTabIndex: 0,
    authorsOpened: false,
    categoriesOpened: false,
  };

  private snackCommon: MatSnackCommon;

  constructor(
    private libraryBookService: LibraryBookService,
    private dialog: MatDialog,
    private bookService: BookService,
    private libraryAuthorService: LibraryAuthorService,
    private libraryCategoryService: LibraryCategoryService,
    private collectionService: CollectionService,
    private collectionBookService: CollectionBookService,
    private libraryStore: LibraryStore,
    private destroyRef: DestroyRef,
    matSnackBar: MatSnackBar,
  ) {
    this.snackCommon = new MatSnackCommon(matSnackBar);
  }

  ngOnInit(): void {
    this.setupSubscriptions();
  }

  onTabChange(index: number): void {
    this.uiState.activeTabIndex = index;
    if (index === 1) this.uiState.authorsOpened = true;
    if (index === 2) this.uiState.categoriesOpened = true;
  }

  //region Books Methods

  onPageChange(event: PageEvent): void {
    this.booksState.currentPage = event.pageIndex;
    this.booksState.pageSize = event.pageSize;
    this.loadBooks();
  }

  onSortChange(sort: string[] | undefined): void {
    this.booksState.sort = sort;
    this.booksState.currentPage = 0;
    this.loadBooks();
  }

  private loadBooks(): void {
    this.booksState.loading = true;
    const f = this.filters.state();

    this.libraryBookService.getAll({
      page: this.booksState.currentPage,
      size: this.booksState.pageSize,
      sort: this.booksState.sort,
      title: f.title,
      status: f.status,
      authorId: f.author?.id,
      categoryId: f.category?.id,
      publishYearMin: f.publishYear.min ?? undefined,
      publishYearMax: f.publishYear.max ?? undefined,
      pagesMin: f.pages.min ?? undefined,
      pagesMax: f.pages.max ?? undefined,
      ratingMin: f.rating.min ?? undefined,
      ratingMax: f.rating.max ?? undefined,
      languages: f.languages.length > 0 ? f.languages : undefined,
    }).subscribe({
      next: page => {
        this.booksState.items = page.content;
        this.booksState.totalElements = page.page.totalElements;
        this.booksState.loading = false;
      },
      error: () => this.booksState.loading = false
    });
  }

  //endregion

  goToAuthorBooks(author: Author): void {
    this.uiState.activeTabIndex = 0;
    this.filters.update('author', author);
    this.authorSearch.query.set(author.fullName);
  }

  goToCategoryBooks(category: Category): void {
    this.uiState.activeTabIndex = 0;
    this.filters.update('category', category);
    this.categorySearch.query.set(category.name);
  }

  deleteLibraryBook(libraryBook: LibraryBook): void {
    this.collectionService.getCollectionsContainingBook(libraryBook.id).subscribe(collections => {
      if (collections.length === 0) {
        this.performDelete(libraryBook);
        return;
      }

      const dialogRef = this.dialog.open(DeleteLibraryBookDialog, {
        data: {
          bookTitle: libraryBook.book.title,
          collections
        }
      });

      dialogRef.afterClosed().subscribe((confirmed: boolean) => {
        if (confirmed) {
          this.performDelete(libraryBook);
        }
      });
    });
  }

  private performDelete(libraryBook: LibraryBook): void {
    this.libraryBookService.removeBook(libraryBook.id).subscribe({
      next: () => {
        this.loadBooks();
        this.libraryStore.triggerRefresh();
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.bookRemoved'));
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  changeLibraryBookStatus(data: [LibraryBook, LibraryBookStatus]): void {
    this.libraryBookService.changeStatus(data[0].id, data[1]).subscribe({
      next: () => {
        this.loadBooks();
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.statusChanged'));
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  changeLibraryBookRating(data: { libraryBookId: number; rating: number }): void {
    this.libraryBookService.changeRating(data.libraryBookId, data.rating).subscribe({
      next: () => {
        this.loadBooks();
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.ratingChanged'));
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  bulkRemoveFromLibrary(): void {
    const ids = this.selection.selectedIds();
    const message = this.translocoService.translate('library.bulkRemoveConfirm', {count: ids.length});
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        message,
        confirmLabel: 'common.delete'
      }
    });

    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe(() => {
      this.libraryBookService.bulkRemove(ids).subscribe({
        next: () => {
          this.loadBooks();
          this.libraryStore.triggerRefresh();
          this.selection.clear();
          this.snackCommon.showSuccess(this.translocoService.translate('library.success.booksRemoved'));
        },
        error: (err) => this.snackCommon.showError(err)
      });
    });
  }

  bulkUpdateStatus(status: LibraryBookStatus): void {
    const ids = this.selection.selectedIds();
    this.libraryBookService.bulkUpdateStatus(ids, status).subscribe({
      next: () => {
        this.loadBooks();
        this.selection.clear();
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.statusChanged'));
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  openBulkAddToCollectionDialog(): void {
    const ids = this.selection.selectedIds();
    const dialogRef = this.dialog.open(CollectionSelectorDialogComponent, {
      data: {
        initialSelectionId: null,
        disabledIds: [],
        showRoot: false
      } as CollectionSelectorDialogData
    });

    // todo duplicate code
    dialogRef.afterClosed().pipe(filter(result => result !== undefined)).subscribe((selection: SelectedCollection) => {
      if (selection.id) {
        this.collectionBookService.bulkAdd(selection.id, ids).subscribe({
          next: () => {
            this.selection.clear();
            this.snackCommon.showSuccess(this.translocoService.translate('library.success.booksAddedToCollection'));
          },
          error: (err) => this.snackCommon.showError(err)
        });
      }
    });
  }

  removeFromAllCollections(libraryBook: LibraryBook): void {
    const message = this.translocoService.translate('library.removeFromAllCollectionsConfirm', {title: libraryBook.book.title});
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {message}
    });

    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe(() => {
      this.collectionBookService.removeFromAllCollections(libraryBook.id).subscribe({
        next: () => this.snackCommon.showSuccess(this.translocoService.translate('library.success.bookRemovedFromAllCollections')),
        error: (err) => this.snackCommon.showError(err)
      });
    });
  }

  addToCollection(libraryBook: LibraryBook): void {
    this.collectionService.getCollectionsContainingBook(libraryBook.id).subscribe(collections => {
      const disabledIds = collections.map(c => c.id);

      const dialogRef = this.dialog.open(CollectionSelectorDialogComponent, {
        data: {
          initialSelectionId: null,
          disabledIds: disabledIds,
          showRoot: false
        } as CollectionSelectorDialogData
      });

      // todo duplicate code fragment
      dialogRef.afterClosed().pipe(filter(result => result !== undefined)).subscribe((selection: SelectedCollection) => {
        if (selection.id) {
          this.collectionBookService.addBookToCollection(selection.id, libraryBook.id).subscribe({
            next: () => {
              this.snackCommon.showSuccess(this.translocoService.translate('library.success.bookAddedToCollection'));
            },
            error: (err) => this.snackCommon.showError(err)
          });
        }
      });
    });
  }

  openEditDetailsDialog(libraryBook: LibraryBook): void {
    const dialogRef = this.dialog.open(LibraryBookDetailsDialogComponent, {
      data: {libraryBook} as LibraryBookDetailsDialogData
    });

    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe((result: LibraryBookDetailsDialogResult) => {
      switch (result.action) {
        case 'save':
          this.updateDetails(libraryBook, result);
          break;
        case 'reset':
          this.resetDetails(libraryBook);
          break;
      }
    });
  }

  // todo dulicate code
  openNoteDialog(libraryBook: LibraryBook): void {
    this.dialog.open(NoteDialogComponent, {
      data: {
        libraryBookId: libraryBook.id,
        bookTitle: libraryBook.book.title
      }
    }).afterClosed().pipe(filter(Boolean)).subscribe(result => {
      if (result === 'saved') {
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.noteSaved'));
      } else if (result === 'deleted') {
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.noteDeleted'));
      }
    });
  }

  private resetDetails(libraryBook: LibraryBook) {
    this.libraryBookService.resetDetails(libraryBook.id).subscribe({
      next: () => {
        this.loadBooks();
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.detailsReset'));
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  private updateDetails(libraryBook: LibraryBook, result: {
    action: "save";
    payload: Partial<UpdateLibraryBookDetails>
  }) {
    this.libraryBookService.updateDetails(libraryBook.id, result.payload).subscribe({
      next: () => {
        this.loadBooks();
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.detailsUpdated'));
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  openViewBookListDialog() {
    const data: ViewBookListDialogData = {
      libraryBooks: [], // Todo: pass actual library books to show 'Already in library'
      categoryColumn: 'category.name',
      fetchBooksFn: (options) => this.bookService.getAll(options),
    };
    const dialogRef = this.dialog.open(ViewBookListDialog, {data});
    dialogRef.afterClosed().subscribe((bookId: number | undefined) => {
      if (bookId) {
        this.libraryBookService.addBook(bookId).subscribe({
          next: () => {
            this.loadBooks();
            this.libraryStore.triggerRefresh();
            this.snackCommon.showSuccess(this.translocoService.translate('library.success.bookAdded'));
          },
          error: (err) => this.snackCommon.showError(err)
        });
      }
    });
  }

  private loadLanguages(): void {
    this.libraryBookService.getLanguages().subscribe(langs => this.languages.set(langs));
  }

  private setupSubscriptions(): void {
    this.translocoService.langChanges$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      const hadFilters = this.hasActiveFilters();
      this.clearAllFilters();
      if (!hadFilters) {
        this.loadBooks();
      }
      this.loadLanguages();
    });

    this.filters.filtersChanged$.subscribe(() => {
      this.booksState.currentPage = 0;
      this.loadBooks();
    });
  }

  onAuthorSearchInput(val: string): void {
    this.authorSearch.search(val);
  }

  onCategorySearchInput(val: string): void {
    this.categorySearch.search(val);
  }

  onAuthorSelected(author: Author): void {
    this.filters.update('author', author);
    this.authorSearch.clear();
  }

  onCategorySelected(category: Category): void {
    this.filters.update('category', category);
    this.categorySearch.clear();
  }

  clearAuthorFilter(): void {
    this.filters.update('author', null);
    this.authorSearch.clear();
  }

  clearCategoryFilter(): void {
    this.filters.update('category', null);
    this.categorySearch.clear();
  }

  toggleLanguage(language: string): void {
    const current = [...this.filters.state().languages];
    const index = current.indexOf(language);
    if (index === -1) {
      current.push(language);
    } else {
      current.splice(index, 1);
    }
    this.filters.update('languages', current);
  }

  hasActiveFilters(): boolean {
    return this.filters.hasActiveFilters(f => {
      return f.author !== null || f.category !== null || f.title !== '' ||
        f.publishYear.min !== null || f.publishYear.max !== null ||
        f.pages.min !== null || f.pages.max !== null ||
        f.languages.length > 0 || f.status !== null ||
        f.rating.min !== null || f.rating.max !== null
    });
  }

  clearAllFilters(): void {
    this.filters.reset(EMPTY_LIBRARY_FILTERS);
    this.authorSearch.clear();
    this.categorySearch.clear();
  }

}
