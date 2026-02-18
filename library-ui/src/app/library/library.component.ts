import {AfterViewInit, Component} from '@angular/core';
import {LibraryBookService} from '../services/library-book.service';
import {getStatusName, LibraryBook, LibraryBookStatus} from '../interfaces/library-book';
import {MatTab, MatTabGroup} from '@angular/material/tabs';
import {MatButton} from '@angular/material/button';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatDialog} from '@angular/material/dialog';
import {ViewBookListDialog, ViewBookListDialogData} from '../dialogs/view-book-list-dialog/view-book-list-dialog';
import {BookExpansionListComponent} from '../book-expansion-list/book-expansion-list.component';
import {BookService} from '../services/book.service';
import {LibraryBookMenuItemsComponent} from '../library-book-menu-items/library-book-menu-items.component';
import {Book} from '../interfaces/book';
import {MatMenu, MatMenuContent, MatMenuModule} from '@angular/material/menu';

@Component({
  selector: 'app-library',
  imports: [
    MatTabGroup,
    MatTab,
    MatButton,
    MatExpansionModule,
    BookExpansionListComponent,
    LibraryBookMenuItemsComponent,
    MatMenuModule,
  ],
  templateUrl: './library.component.html',
  styleUrl: './library.component.scss',
})
export class LibraryComponent implements AfterViewInit {

  protected readonly getStatusName = getStatusName;

  private libraryBooks: LibraryBook[] = [];
  public libraryBooksByStatus: Map<LibraryBookStatus, LibraryBook[]> = new Map();
  public libraryBooksByCategory: Map<string, LibraryBook[]> = new Map();
  public libraryBooksByAuthor: Map<string, LibraryBook[]> = new Map();

  constructor(
    private libraryBookService: LibraryBookService,
    private dialog: MatDialog,
    private bookService: BookService,
  ) {
  }

  ngAfterViewInit(): void {
    this.libraryBookService.getAll(0, 1000).subscribe(page => {
      this.libraryBooks = page.content;
      this.updateBookGroups();
    });
  }

  deleteLibraryBook(book: Book): void {
    this.libraryBookService.removeBook(book.id).subscribe(() => {
      this.libraryBooks = this.libraryBooks.filter(lb => lb.book.id !== book.id);
      this.updateBookGroups();
    });
  }

  changeLibraryBookStatus(data: [Book, LibraryBookStatus]): void {
    this.libraryBookService.changeStatus(data[0].id, data[1]).subscribe((libraryBook: LibraryBook) => {
      this.updateBook(libraryBook);
    });
  }

  changeLibraryBookRating(data: { bookId: number; rating: number }): void {
    this.libraryBookService.changeRating(data.bookId, data.rating).subscribe((libraryBook: LibraryBook) => {
      this.updateBook(libraryBook);
    });
  }

  getLibraryBookByBookId(bookId: number): LibraryBook | undefined {
    return this.libraryBooks.find(lb => lb.book.id === bookId);
  }

  openViewBookListDialog() {
    const data: ViewBookListDialogData = {
      libraryBooks: this.libraryBooks,
      fetchBooksFn: (page, size) => this.bookService.getAll({page, size}),
    };
    const dialogRef = this.dialog.open(ViewBookListDialog, {data});
    dialogRef.afterClosed().subscribe((bookId: number | undefined) => {
      if (bookId) {
        this.addLibraryBook(bookId);
      }
    });
  }

  private updateBook(libraryBook: LibraryBook) {
    const index = this.libraryBooks.findIndex(lb => lb.book.id === libraryBook.book.id);
    if (index !== -1) {
      this.libraryBooks.splice(index, 1, libraryBook);
      this.updateBookGroups();
    }
  }

  private updateBookGroups() {
    this.libraryBooksByStatus = this.groupBy((ub) => [ub.status]);
    this.libraryBooksByCategory = this.groupBy((ub) => [ub.book.categoryName]);
    this.libraryBooksByAuthor = this.groupBy((ub) => Object.values(ub.book.authors));
  }

  private addLibraryBook(bookId: number) {
    this.libraryBookService.addBook(bookId).subscribe(libraryBook => {
      this.libraryBooks.push(libraryBook);
      this.updateBookGroups();
    });
  }

  private groupBy(keyFn: (item: LibraryBook) => string[]): Map<any, LibraryBook[]> {
    return this.libraryBooks.reduce((acc, item) => {
      const keys = keyFn(item);
      keys.forEach(key => {
        const group = acc.get(key) || [];
        acc.set(key, [...group, item]);
      });
      return acc;
    }, new Map());
  }

}
