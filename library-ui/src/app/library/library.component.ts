import {Component, computed, OnInit, signal} from '@angular/core';
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
import {debounceTime, distinctUntilChanged, filter, Subject, switchMap} from 'rxjs';
import {SelectedCollection} from '../interfaces/selected-collection';
import {ConfirmationDialogComponent} from '../dialogs/confirmation-dialog/confirmation-dialog.component';
import {
  LibraryBookDetailsDialogComponent,
  LibraryBookDetailsDialogData,
  LibraryBookDetailsDialogResult
} from '../dialogs/library-book-details-dialog/library-book-details-dialog.component';
import {BookFilterStore} from '../services/book-filter.store';
import {LibraryFilters} from '../interfaces/filters';
import {Author} from '../interfaces/author';
import {Category} from '../interfaces/category';
import {LanguageWithCount} from '../interfaces/language-with-count';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {AuthorService} from '../services/author.service';
import {CategoryService} from '../services/category.service';
import {SortOption} from '../interfaces/sort-config';
import {CommonModule} from '@angular/common';
import {FilterShellComponent} from '../common/filter-shell/filter-shell.component';
import {TextFilterComponent} from '../common/filters/text-filter/text-filter.component';
import {AutocompleteFilterComponent} from '../common/filters/autocomplete-filter/autocomplete-filter.component';
import {RangeFilterComponent} from '../common/filters/range-filter/range-filter.component';
import {LanguageFilterComponent} from '../common/filters/language-filter/language-filter.component';
import {SortBarComponent} from '../common/sort-bar/sort-bar.component';
import {BooksGridComponent} from '../books-grid/books-grid.component';
import {MatSelectModule} from '@angular/material/select';
import {FormsModule} from '@angular/forms';
import {MatTooltipModule} from '@angular/material/tooltip';
import {Router} from '@angular/router';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {SelectFilterComponent, SelectOption} from '../common/filters/select-filter/select-filter.component';
import {CountryWithCount} from '../interfaces/country-with-count';

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
    BooksGridComponent,
    MatSelectModule,
    MatPaginatorModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    SelectFilterComponent,
  ],
  templateUrl: './library.component.html',
  styleUrl: './library.component.scss',
})
export class LibraryComponent implements OnInit {

  protected readonly getStatusName = getStatusName;
  protected readonly LIBRARY_BOOK_STATUSES = LIBRARY_BOOK_STATUSES;

  readonly bookSortOptions: SortOption[] = [
    {field: 'title', label: 'Назва'},
    {field: 'publishYear', label: 'Рік видання'},
    {field: 'rating', label: 'Оцінка'},
    {field: 'addedAt', label: 'Дата додавання'},
    {field: 'pages', label: 'Сторінки'},
  ];

  readonly authorSortOptions: SortOption[] = [
    {field: 'fullName', label: 'ПІБ'},
    {field: 'country', label: 'Країна'},
    {field: 'birthYear', label: 'Рік народження'},
    {field: 'booksCount', label: 'Кількість книг'},
  ];

  readonly categorySortOptions: SortOption[] = [
    {field: 'name', label: 'Назва'},
    {field: 'booksCount', label: 'Кількість книг'},
  ];

  booksState = {
    items: [] as LibraryBook[],
    totalElements: 0,
    pageSize: 15,
    currentPage: 0,
    loading: false,
    sort: undefined as string[] | undefined,
  };

  authorsState = {
    items: [] as Author[],
    totalElements: 0,
    pageSize: 12,
    currentPage: 0,
    loading: false,
    sort: undefined as string[] | undefined,
    loaded: false,
    filter: {
      name: '',
      country: null as string | null,
      birthYearMin: null as number | null,
      birthYearMax: null as number | null,
      booksCountMin: null as number | null,
      booksCountMax: null as number | null,
    },
  };

  countries = signal<CountryWithCount[]>([]);

  categoriesState = {
    items: [] as Category[],
    totalElements: 0,
    pageSize: 12,
    currentPage: 0,
    loading: false,
    sort: undefined as string[] | undefined,
    loaded: false,
    filter: {
      name: '',
      booksCountMin: null as number | null,
      booksCountMax: null as number | null,
    }
  };

  readonly statusOptions: SelectOption[] = LIBRARY_BOOK_STATUSES.map(s => ({
    value: s,
    label: getStatusName(s)
  }));

  readonly countryOptions = computed<SelectOption[]>(() =>
    this.countries().map(c => ({
      value: c.country,
      label: `${c.country} (${c.count})`
    }))
  );

  readonly filters = new BookFilterStore<LibraryFilters>(EMPTY_LIBRARY_FILTERS);

  authorSearchInput = signal('');
  filteredAuthors = signal<Author[]>([]);
  categorySearchInput = signal('');
  filteredCategories = signal<Category[]>([]);

  languages = signal<LanguageWithCount[]>([]);
  showAllLanguages = signal(false);

  private readonly subjects = {
    authorSearch: new Subject<string>(),
    categorySearch: new Subject<string>(),
    authorName: new Subject<string>(),
    categoryName: new Subject<string>(),
    authorRange: new Subject<void>(),
    categoryRange: new Subject<void>(),
  };

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
    private router: Router,
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
    if (index === 1 && !this.authorsState.loaded) {
      this.loadAuthors();
      this.loadAuthorCountries();
    }
    if (index === 2 && !this.categoriesState.loaded) {
      this.loadCategories();
    }
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

  //region Authors Methods

  onAuthorPageChange(event: PageEvent): void {
    this.authorsState.currentPage = event.pageIndex;
    this.authorsState.pageSize = event.pageSize;
    this.loadAuthors();
  }

  onAuthorSortChange(sort: string[] | undefined): void {
    this.authorsState.sort = sort;
    this.authorsState.currentPage = 0;
    this.loadAuthors();
  }

  onAuthorNameSearch(): void {
    this.subjects.authorName.next(this.authorsState.filter.name);
  }

  onAuthorBirthYearChange(): void {
    this.subjects.authorRange.next();
  }

  onAuthorBooksCountChange(): void {
    this.subjects.authorRange.next();
  }

  onAuthorCountrySelected(country: string | null): void {
    this.authorsState.filter.country = country;
    this.authorsState.currentPage = 0;
    this.loadAuthors();
  }

  hasAuthorFilters(): boolean {
    const f = this.authorsState.filter;
    return !!f.name || !!f.country || f.birthYearMin != null || f.birthYearMax != null || f.booksCountMin != null || f.booksCountMax != null;
  }

  clearAllAuthorFilters(): void {
    this.authorsState.filter = {
      name: '',
      country: null,
      birthYearMin: null,
      birthYearMax: null,
      booksCountMin: null,
      booksCountMax: null
    };
    this.authorsState.currentPage = 0;
    this.loadAuthors();
  }

  private loadAuthors(): void {
    this.authorsState.loading = true;
    const f = this.authorsState.filter;

    this.authorService.searchMe({
      page: this.authorsState.currentPage,
      size: this.authorsState.pageSize,
      sort: this.authorsState.sort,
      name: f.name,
      country: f.country ?? undefined,
      birthYearMin: f.birthYearMin ?? undefined,
      birthYearMax: f.birthYearMax ?? undefined,
      booksCountMin: f.booksCountMin ?? undefined,
      booksCountMax: f.booksCountMax ?? undefined,
    }).subscribe({
      next: page => {
        this.authorsState.items = page.content;
        this.authorsState.totalElements = page.page.totalElements;
        this.authorsState.loading = false;
        this.authorsState.loaded = true;
      },
      error: () => this.authorsState.loading = false
    });
  }

  private loadAuthorCountries(): void {
    this.authorService.getCountriesMe().subscribe(countries => {
      this.countries.set(countries);
    });
  }

  goToAuthorBooks(author: Author): void {
    this.uiState.activeTabIndex = 0;
    this.filters.update('author', author);
    this.authorSearchInput.set(author.fullName);
  }

  getBooksCountText(count: number): string {
    if (count === 0) return '0 книг';
    if (count === 1) return '1 книга';
    if (count >= 2 && count <= 4) return `${count} книги`;
    return `${count} книг`;
  }

  goToAuthorDetails(author: Author): void {
    this.router.navigate(['/author-details'], {state: {id: author.id}});
  }

  //endregion

  //region Categories Methods

  onCategoryPageChange(event: PageEvent): void {
    this.categoriesState.currentPage = event.pageIndex;
    this.categoriesState.pageSize = event.pageSize;
    this.loadCategories();
  }

  onCategorySortChange(sort: string[] | undefined): void {
    this.categoriesState.sort = sort;
    this.categoriesState.currentPage = 0;
    this.loadCategories();
  }

  onCategoryNameSearch(): void {
    this.subjects.categoryName.next(this.categoriesState.filter.name);
  }

  onCategoryBooksCountChange(): void {
    this.subjects.categoryRange.next();
  }

  hasCategoryFilters(): boolean {
    const f = this.categoriesState.filter;
    return !!f.name || f.booksCountMin != null || f.booksCountMax != null;
  }

  clearAllCategoryFilters(): void {
    this.categoriesState.filter = {
      name: '',
      booksCountMin: null,
      booksCountMax: null
    };
    this.categoriesState.currentPage = 0;
    this.loadCategories();
  }

  private loadCategories(): void {
    this.categoriesState.loading = true;
    const f = this.categoriesState.filter;

    this.categoryService.searchMe({
      page: this.categoriesState.currentPage,
      size: this.categoriesState.pageSize,
      sort: this.categoriesState.sort,
      name: f.name,
      booksCountMin: f.booksCountMin ?? undefined,
      booksCountMax: f.booksCountMax ?? undefined,
    }).subscribe({
      next: page => {
        this.categoriesState.items = page.content;
        this.categoriesState.totalElements = page.page.totalElements;
        this.categoriesState.loading = false;
        this.categoriesState.loaded = true;
      },
      error: () => this.categoriesState.loading = false
    });
  }

  goToCategoryBooks(category: Category): void {
    this.uiState.activeTabIndex = 0;
    this.filters.update('category', category);
    this.categorySearchInput.set(category.name);
  }

  //endregion

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
          this.libraryBookService.updateDetails(libraryBook.id, result.payload).subscribe({
            next: () => {
              this.loadBooks();
              this.snackCommon.showSuccess('Деталі книги оновлено');
            },
            error: (err) => this.snackCommon.showError(err)
          });
          break;
        case 'reset':
          this.libraryBookService.resetDetails(libraryBook.id).subscribe({
            next: () => {
              this.loadBooks();
              this.snackCommon.showSuccess('Деталі книги скинуто');
            },
            error: (err) => this.snackCommon.showError(err)
          });
          break;
      }
    });
  }

  openViewBookListDialog() {
    const data: ViewBookListDialogData = {
      libraryBooks: [], // Todo: pass actual library books to show 'Already in library'
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
    this.bookService.getLanguages().subscribe(langs => this.languages.set(langs));
  }

  private setupSubscriptions(): void {
    this.subjects.authorSearch.pipe(
      debounceTime(450),
      distinctUntilChanged(),
      switchMap(query => this.authorService.search({name: query, page: 0, size: 10}))
    ).subscribe(page => this.filteredAuthors.set(page.content));

    this.subjects.categorySearch.pipe(
      debounceTime(450),
      distinctUntilChanged(),
      switchMap(query => this.categoryService.search({name: query, page: 0, size: 10}))
    ).subscribe(page => this.filteredCategories.set(page.content));

    this.subjects.authorName.pipe(
      debounceTime(450),
      distinctUntilChanged()
    ).subscribe(() => {
      this.authorsState.currentPage = 0;
      this.loadAuthors();
    });

    this.subjects.categoryName.pipe(
      debounceTime(450),
      distinctUntilChanged()
    ).subscribe(() => {
      this.categoriesState.currentPage = 0;
      this.loadCategories();
    });

    this.subjects.authorRange.pipe(
      debounceTime(450)
    ).subscribe(() => {
      this.authorsState.currentPage = 0;
      this.loadAuthors();
    });

    this.subjects.categoryRange.pipe(
      debounceTime(450)
    ).subscribe(() => {
      this.categoriesState.currentPage = 0;
      this.loadCategories();
    });

    this.filters.filtersChanged$.subscribe(() => {
      this.booksState.currentPage = 0;
      this.loadBooks();
    });
  }

  onAuthorSearchInput(val: string): void {
    this.authorSearchInput.set(val);
    this.subjects.authorSearch.next(val);
  }

  onCategorySearchInput(val: string): void {
    this.categorySearchInput.set(val);
    this.subjects.categorySearch.next(val);
  }

  onAuthorSelected(author: Author): void {
    this.filters.update('author', author);
    this.authorSearchInput.set(author.fullName);
    this.filteredAuthors.set([]);
  }

  onCategorySelected(category: Category): void {
    this.filters.update('category', category);
    this.categorySearchInput.set(category.name);
    this.filteredCategories.set([]);
  }

  clearAuthorFilter(): void {
    this.filters.update('author', null);
    this.authorSearchInput.set('');
    this.filteredAuthors.set([]);
  }

  clearCategoryFilter(): void {
    this.filters.update('category', null);
    this.categorySearchInput.set('');
    this.filteredCategories.set([]);
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
    const f = this.filters.state();
    return f.author !== null || f.category !== null || f.title !== '' ||
      f.publishYear.min !== null || f.publishYear.max !== null ||
      f.pages.min !== null || f.pages.max !== null ||
      f.languages.length > 0 || f.status !== null ||
      f.rating.min !== null || f.rating.max !== null;
  }

  clearAllFilters(): void {
    this.filters.reset(EMPTY_LIBRARY_FILTERS);
    this.authorSearchInput.set('');
    this.categorySearchInput.set('');
  }

}
