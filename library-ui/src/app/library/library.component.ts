import {Component, OnInit, signal} from '@angular/core';
import {LibraryBookService} from '../services/library-book.service';
import {getStatusName, LIBRARY_BOOK_STATUSES, LibraryBook, LibraryBookStatus} from '../interfaces/library-book';
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
import {filter} from 'rxjs';
import {SelectedCollection} from '../interfaces/selected-collection';
import {ConfirmationDialogComponent} from '../dialogs/confirmation-dialog/confirmation-dialog.component';
import {
  LibraryBookDetailsDialogComponent,
  LibraryBookDetailsDialogData,
  LibraryBookDetailsDialogResult
} from '../dialogs/library-book-details-dialog/library-book-details-dialog.component';
import {EntityFilterStore} from '../services/entity-filter.store';
import {AutocompleteSearchStore} from '../services/autocomplete-search.store';
import {LibraryFilters} from '../interfaces/filters';
import {Author} from '../interfaces/author';
import {Category} from '../interfaces/category';
import {LanguageWithCount} from '../interfaces/language-with-count';
import {PageEvent} from '@angular/material/paginator';
import {AuthorService} from '../services/author.service';
import {CategoryService} from '../services/category.service';
import {SortOption} from '../interfaces/sort-config';
import {CommonModule} from '@angular/common';
import {
  FilterShellComponent,
  FooterFiltersDirective,
  SecondaryFiltersDirective
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
import {SelectFilterComponent, SelectOption} from '../common/filters/select-filter/select-filter.component';
import {AuthorListComponent} from '../author-list/author-list.component';
import {CategoryListComponent} from '../category-list/category-list.component';
import {UpdateLibraryBookDetails} from '../interfaces/update-library-book-details';

import {BulkActionBarComponent} from '../common/bulk-action-bar/bulk-action-bar.component';
import {SelectionStore} from '../services/selection.store';
import {NoteDialogComponent} from '../dialogs/note-dialog/note-dialog.component';
import {MatButtonToggleModule} from '@angular/material/button-toggle';

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
    SecondaryFiltersDirective,
    FooterFiltersDirective,
    BulkActionBarComponent,
    MatButtonToggleModule,
  ],
  templateUrl: './library.component.html',
  styleUrl: './library.component.scss',
})
export class LibraryComponent implements OnInit {

  readonly bookSortOptions: SortOption[] = [
    {field: 'title', label: 'Назва'},
    {field: 'publishYear', label: 'Рік видання'},
    {field: 'rating', label: 'Оцінка'},
    {field: 'addedAt', label: 'Дата додавання'},
    {field: 'pages', label: 'Сторінки'},
  ];

  booksState = {
    items: [] as LibraryBook[],
    totalElements: 0,
    pageSize: 15,
    currentPage: 0,
    loading: false,
    sort: undefined as string[] | undefined,
  };

  readonly statusOptions: SelectOption[] = LIBRARY_BOOK_STATUSES.map(s => ({
    value: s,
    label: getStatusName(s)
  }));

  readonly selection = new SelectionStore();
  readonly filters = new EntityFilterStore<LibraryFilters>(EMPTY_LIBRARY_FILTERS);
  readonly viewMode = signal<'grid' | 'list'>('grid');

  readonly authorSearch = new AutocompleteSearchStore<Author>(
    (q, p, s) => this.authorService.searchMe({name: q, page: p, size: s}),
    450,
    10
  );
  readonly categorySearch = new AutocompleteSearchStore<Category>(
    (q, p, s) => this.categoryService.searchMe({name: q, page: p, size: s}),
    450,
    10
  );

  languages = signal<LanguageWithCount[]>([]);
  showAllLanguages = signal(false);

  uiState = {
    activeTabIndex: 0,
  };

  private snackCommon: MatSnackCommon;

  constructor(
    private libraryBookService: LibraryBookService,
    private dialog: MatDialog,
    private bookService: BookService,
    private authorService: AuthorService,
    private categoryService: CategoryService,
    private collectionService: CollectionService,
    private collectionBookService: CollectionBookService,
    matSnackBar: MatSnackBar,
  ) {
    this.snackCommon = new MatSnackCommon(matSnackBar);
  }

  ngOnInit(): void {
    this.loadBooks();
    this.loadLanguages();
    this.setupSubscriptions();
  }

  onTabChange(index: number): void {
    this.uiState.activeTabIndex = index;
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
        this.snackCommon.showSuccess('Книгу видалено з бібліотеки');
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  changeLibraryBookStatus(data: [LibraryBook, LibraryBookStatus]): void {
    this.libraryBookService.changeStatus(data[0].id, data[1]).subscribe({
      next: () => {
        this.loadBooks();
        this.snackCommon.showSuccess('Статус змінено');
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  changeLibraryBookRating(data: { libraryBookId: number; rating: number }): void {
    this.libraryBookService.changeRating(data.libraryBookId, data.rating).subscribe({
      next: () => {
        this.loadBooks();
        this.snackCommon.showSuccess('Оцінку змінено');
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  bulkRemoveFromLibrary(): void {
    const ids = this.selection.selectedIds();
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        message: `Ви впевнені, що хочете видалити ${ids.length} вибраних книг з бібліотеки?`
      }
    });

    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe(() => {
      this.libraryBookService.bulkRemove(ids).subscribe({
        next: () => {
          this.loadBooks();
          this.selection.clear();
          this.snackCommon.showSuccess('Книги видалено з бібліотеки');
        },
        error: (err) => this.snackCommon.showError(err)
      });
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
            this.snackCommon.showSuccess('Книги додано до колекції');
          },
          error: (err) => this.snackCommon.showError(err)
        });
      }
    });
  }

  removeFromAllCollections(libraryBook: LibraryBook): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        message: `Ви впевнені, що хочете видалити книгу "${libraryBook.book.title}" з усіх колекцій?`
      }
    });

    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe(() => {
      this.collectionBookService.removeFromAllCollections(libraryBook.id).subscribe({
        next: () => this.snackCommon.showSuccess('Книгу видалено з усіх колекцій'),
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
              this.snackCommon.showSuccess('Книгу додано до колекції');
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
        this.snackCommon.showSuccess('Нотатку збережено');
      } else if (result === 'deleted') {
        this.snackCommon.showSuccess('Нотатку видалено');
      }
    });
  }

  private resetDetails(libraryBook: LibraryBook) {
    this.libraryBookService.resetDetails(libraryBook.id).subscribe({
      next: () => {
        this.loadBooks();
        this.snackCommon.showSuccess('Деталі книги скинуто');
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
        this.snackCommon.showSuccess('Деталі книги оновлено');
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
            this.snackCommon.showSuccess('Книгу додано до бібліотеки');
          },
          error: (err) => this.snackCommon.showError(err)
        });
      }
    });
  }

  private loadLanguages(): void {
    // todo change
    this.bookService.getLanguages().subscribe(langs => this.languages.set(langs));
  }

  private setupSubscriptions(): void {
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
