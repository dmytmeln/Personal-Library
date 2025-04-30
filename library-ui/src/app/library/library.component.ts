import {AfterViewInit, Component} from '@angular/core';
import {UserBookService} from '../services/user-book.service';
import {getStatusName, UserBook, UserBookStatus} from '../interfaces/user-book';
import {MatTab, MatTabGroup} from '@angular/material/tabs';
import {MatButton} from '@angular/material/button';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatDialog} from '@angular/material/dialog';
import {ViewBookListDialog} from '../dialogs/view-book-list-dialog/view-book-list-dialog';
import {BookExpansionListComponent} from '../book-expansion-list/book-expansion-list.component';

@Component({
  selector: 'app-library',
  imports: [
    MatTabGroup,
    MatTab,
    MatButton,
    MatExpansionModule,
    BookExpansionListComponent,
  ],
  templateUrl: './library.component.html',
  styleUrl: './library.component.scss',
})
export class LibraryComponent implements AfterViewInit {

  protected readonly getStatusName = getStatusName;

  private userBooks: UserBook[] = [];
  public userBooksByStatus: Map<UserBookStatus, UserBook[]> = new Map();
  public userBooksByCategory: Map<string, UserBook[]> = new Map();
  public userBooksByAuthor: Map<string, UserBook[]> = new Map();

  constructor(
    private userBookService: UserBookService,
    private dialog: MatDialog,
  ) {
  }

  ngAfterViewInit(): void {
    this.userBookService.getAll().subscribe(userBooks => {
      this.userBooks = userBooks;
      this.updateBookGroups();
    });
  }

  deleteUserBook(userBook: UserBook): void {
    this.userBookService.deleteBookFromLibrary(userBook.book.id).subscribe(() => {
      this.userBooks = this.userBooks.filter(book => book.book.id !== userBook.book.id);
      this.updateBookGroups();
    });
  }

  openViewBookListDialog() {
    const dialogRef = this.dialog.open(ViewBookListDialog, {
      ...DIALOG_CONFIG,
      data: this.userBooks
    });
    dialogRef.afterClosed().subscribe((bookId: number | undefined) => {
      if (bookId) {
        this.addUserBook(bookId);
      }
    });
  }

  changeUserBookStatus(bookAndStatus: [UserBook, UserBookStatus]) {
    this.userBookService.changeStatus(bookAndStatus[0].book.id, bookAndStatus[1]).subscribe((userBook: UserBook) => {
      this.updateBook(userBook);
    });
  }

  changeUserBookRating(ratingChange: { bookId: number; rating: number }) {
    this.userBookService.changeRating(ratingChange.bookId, ratingChange.rating).subscribe((userBook: UserBook) => {
      this.updateBook(userBook);
    });
  }

  private updateBook(userBook: UserBook) {
    this.userBooks = this.userBooks.filter(book => book.book.id !== userBook.book.id);
    this.userBooks.push(userBook);
    this.updateBookGroups();
  }

  private updateBookGroups() {
    this.userBooksByStatus = this.groupBy((ub) => [ub.status]);
    this.userBooksByCategory = this.groupBy((ub) => [ub.book.categoryName]);
    this.userBooksByAuthor = this.groupBy((ub) => Object.values(ub.book.authors));
  }

  private addUserBook(bookId: number) {
    this.userBookService.addBookToLibrary(bookId).subscribe(userBook => {
      this.userBooks.push(userBook);
      this.updateBookGroups();
    });
  }

  private groupBy(keyFn: (item: UserBook) => string[]): Map<any, UserBook[]> {
    return this.userBooks.reduce((acc, item) => {
      const keys = keyFn(item);
      keys.forEach(key => {
        const group = acc.get(key) || [];
        acc.set(key, [...group, item]);
      });
      return acc;
    }, new Map());
  }

}

const DIALOG_CONFIG = {
  width: '1000px',
  maxWidth: '1440px',
}
