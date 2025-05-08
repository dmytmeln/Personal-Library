import {Component, OnInit} from '@angular/core';
import {CollectionBookService} from '../services/collection-book.service';
import {CollectionService} from '../services/collection.service';
import {Collection} from '../interfaces/collection';
import {CollectionBook} from '../interfaces/collection-book';
import {NgStyle} from '@angular/common';
import {MatButton} from '@angular/material/button';
import {MatDialog} from '@angular/material/dialog';
import {CreateCollectionDialog} from '../dialogs/create-collection-dialog/create-collection-dialog';
import {CreateCollection} from '../interfaces/create-collection';
import {ViewBookListDialog, ViewBookListDialogData} from '../dialogs/view-book-list-dialog/view-book-list-dialog';
import {LibraryBookService} from '../services/library-book.service';
import {map} from 'rxjs';
import {LibraryBook, LibraryBookStatus} from '../interfaces/library-book';
import {Book} from '../interfaces/book';
import {BookComponent} from '../book/book.component';
import {LibraryBookActionsComponent} from '../library-book-actions/library-book-actions.component';
import {Router} from '@angular/router';

@Component({
  selector: 'app-collection',
  imports: [
    NgStyle,
    MatButton,
    BookComponent,
    LibraryBookActionsComponent,
  ],
  templateUrl: './collection.component.html',
  styleUrl: './collection.component.scss'
})
export class CollectionComponent implements OnInit {

  collection!: Collection;
  collectionBooks: CollectionBook[] = [];

  constructor(
    private router: Router,
    private collectionService: CollectionService,
    private collectionBookService: CollectionBookService,
    private dialog: MatDialog,
    private libraryBookService: LibraryBookService,
  ) {
    this.collection = this.router.getCurrentNavigation()?.extras?.state as Collection;
    if (!this.collection) {
      this.goBack();
    }
  }

  ngOnInit(): void {
    this.initCollectionBooks();
  }

  openCreateCollectionDialog(): void {
    const dialogRef = this.dialog.open(CreateCollectionDialog, {data: this.collection});
    dialogRef.afterClosed().subscribe((result: CreateCollection | undefined) => {
      if (result) {
        this.collectionService.update(this.collection.id, result).subscribe(collection => {
          this.collection = collection;
        });
      }
    });
  }

  openViewBookListDialog(): void {
    const data: ViewBookListDialogData = {
      libraryBooks: this.collectionBooks.map(cd => cd.libraryBook),
      fetchBooksFn: (page, size) =>
        this.libraryBookService.getAll(page, size).pipe(
          map(result => ({
            totalElements: result.totalElements,
            content: result.content.map((lb: LibraryBook): Book => lb.book)
          }))
        ),
    };
    const dialogRef = this.dialog.open(ViewBookListDialog, {data});
    dialogRef.afterClosed().subscribe((bookId: number | undefined) => {
      if (bookId) {
        this.collectionBookService.addBookToCollection(this.collection.id, bookId).subscribe(collectionBook => {
          this.collectionBooks.push(collectionBook);
        });
      }
    });
  }

  deleteBook(libraryBook: LibraryBook): void {
    this.collectionBookService.removeBookFromCollection(this.collection.id, libraryBook.book.id).subscribe(() => {
      this.collectionBooks = this.collectionBooks.filter(cb => cb.libraryBook.book.id !== libraryBook.book.id);
    });
  }

  statusChange(bookAndStatus: [LibraryBook, LibraryBookStatus]): void {
    this.libraryBookService.changeStatus(bookAndStatus[0].book.id, bookAndStatus[1]).subscribe(libraryBook =>
      this.updateBook(libraryBook));
  }

  ratingChange(ratingChange: { bookId: number; rating: number }): void {
    this.libraryBookService.changeRating(ratingChange.bookId, ratingChange.rating).subscribe(libraryBook =>
      this.updateBook(libraryBook));
  }

  goBack() {
    this.router.navigate(['collections']).then(() => {
    });
  }

  private updateBook(libraryBook: LibraryBook) {
    this.collectionBooks.map(cb =>
      cb.libraryBook = cb.libraryBook.book.id === libraryBook.book.id
        ? libraryBook
        : cb.libraryBook);
  }

  private initCollectionBooks() {
    this.collectionBookService.getCollectionBooks(this.collection.id).subscribe(collectionBooks =>
      this.collectionBooks = collectionBooks);
  }

}
