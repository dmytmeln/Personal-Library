import {Component, DestroyRef, inject, OnInit, signal, viewChild} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {CommonModule} from '@angular/common';
import {MatTab, MatTabGroup} from '@angular/material/tabs';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatSnackBar} from '@angular/material/snack-bar';
import {LibraryBookService} from '../services/library-book.service';
import {Book} from '../interfaces/book';
import {Author} from '../interfaces/author';
import {Category} from '../interfaces/category';
import {MatSnackCommon} from '../common/mat-snack-common';
import {MatMenu, MatMenuContent, MatMenuItem} from '@angular/material/menu';
import {AuthorListComponent} from '../author-list/author-list.component';
import {CategoryListComponent} from '../category-list/category-list.component';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {BookListComponent} from '../book-list/book-list.component';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [
    CommonModule,
    MatTabGroup,
    MatTab,
    MatIconModule,
    MatButtonModule,
    AuthorListComponent,
    CategoryListComponent,
    MatButtonToggleModule,
    TranslocoDirective,
    BookListComponent,
    MatMenu,
    MatMenuContent,
    MatMenuItem
  ],
  templateUrl: './search.component.html',
  styleUrl: './search.component.scss'
})
export class SearchComponent implements OnInit {

  private translocoService = inject(TranslocoService);
  private libraryBookService = inject(LibraryBookService);
  private destroyRef = inject(DestroyRef);
  private snackCommon: MatSnackCommon;

  readonly bookList = viewChild(BookListComponent);
  readonly viewMode = signal<'grid' | 'list'>('grid');

  private libraryBookIds: Set<number> = new Set<number>();

  uiState = {
    activeTabIndex: 0,
    authorsOpened: false,
    categoriesOpened: false,
  };

  constructor(matSnackBar: MatSnackBar) {
    this.snackCommon = new MatSnackCommon(matSnackBar);
  }

  ngOnInit(): void {
    this.translocoService.langChanges$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.libraryBookIds.clear(); // Could be improved to fetch from server
    });
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
    const list = this.bookList();
    if (!list) return;
    const ids = list.selection.selectedIds();
    this.libraryBookService.bulkAdd(ids).subscribe({
      next: () => {
        ids.forEach(id => this.libraryBookIds.add(id));
        list.selection.clear();
        this.snackCommon.showSuccess(this.translocoService.translate('library.success.bookAdded'));
      },
      error: err => this.snackCommon.showError(err)
    });
  }

  isBookInLibrary(book: Book | any): boolean {
    return this.libraryBookIds.has(book.id);
  }

  onTabChange(index: number): void {
    this.uiState.activeTabIndex = index;
    if (index === 1) this.uiState.authorsOpened = true;
    if (index === 2) this.uiState.categoriesOpened = true;
  }

  showAuthorBooks(author: Author): void {
    this.uiState.activeTabIndex = 0;
    setTimeout(() => {
      const list = this.bookList();
      if (list) {
        list.onAuthorSelected(author);
      }
    });
  }

  goToCategoryBooks(category: Category): void {
    this.uiState.activeTabIndex = 0;
    setTimeout(() => {
      const list = this.bookList();
      if (list) {
        list.onCategorySelected(category);
      }
    });
  }

}
