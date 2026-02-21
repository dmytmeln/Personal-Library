import {Component, OnInit} from '@angular/core';
import {CollectionBookService} from '../services/collection-book.service';
import {CollectionService} from '../services/collection.service';
import {Collection} from '../interfaces/collection';
import {CollectionBook} from '../interfaces/collection-book';
import {NgStyle} from '@angular/common';
import {MatButton} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSnackCommon} from '../common/mat-snack-common';
import {ViewBookListDialog, ViewBookListDialogData} from '../dialogs/view-book-list-dialog/view-book-list-dialog';
import {LibraryBookService} from '../services/library-book.service';
import {LibraryBook, LibraryBookStatus} from '../interfaces/library-book';
import {BookComponent} from '../book/book.component';
import {LibraryBookMenuItemsComponent} from '../library-book-menu-items/library-book-menu-items.component';
import {ActivatedRoute, Router} from '@angular/router';
import {MatMenuModule} from '@angular/material/menu';
import {CollectionDialogComponent, CollectionDialogData} from '../dialogs/collection-dialog/collection-dialog.component';
import {UpdateCollection} from '../interfaces/update-collection';
import {filter} from 'rxjs';

@Component({
  selector: 'app-collection',
  imports: [
    NgStyle,
    MatButton,
    MatIconModule,
    BookComponent,
    LibraryBookMenuItemsComponent,
    MatMenuModule,
  ],
  templateUrl: './collection.component.html',
  styleUrl: './collection.component.scss'
})
export class CollectionComponent implements OnInit {

  collection!: Collection;
  collectionBooks: CollectionBook[] = [];
  private snackCommon: MatSnackCommon;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private collectionService: CollectionService,
    private collectionBookService: CollectionBookService,
    private dialog: MatDialog,
    private libraryBookService: LibraryBookService,
    private matSnackBar: MatSnackBar,
  ) {
    this.snackCommon = new MatSnackCommon(matSnackBar);
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const collectionId = +params['id'];
      this.collectionService.getById(collectionId).subscribe(collection => {
        this.collection = collection;
        this.initCollectionBooks();
      });
    });
  }

  openUpdateDialog(): void {
    const dialogRef = this.dialog.open(CollectionDialogComponent, {
      data: {
        isEdit: true,
        collection: this.collection
      } as CollectionDialogData
    });

    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe((updatedCollection: UpdateCollection) => {
      this.collectionService.update(this.collection.id, updatedCollection).subscribe({
        next: (collection) => {
          this.collection = collection;
          this.snackCommon.showSuccess('Колекцію оновлено успішно');
        },
        error: (err) => this.snackCommon.showError(err)
      });
    });
  }

  openViewBookListDialog(): void {
    const data: ViewBookListDialogData = {
      libraryBooks: this.collectionBooks.map(cd => cd.libraryBook),
      fetchBooksFn: (page, size) => this.libraryBookService.getAll(page, size),
    };
    const dialogRef = this.dialog.open(ViewBookListDialog, {data});
    dialogRef.afterClosed().subscribe((libraryBookId: number | undefined) => {
      if (libraryBookId) {
        this.collectionBookService.addBookToCollection(this.collection.id, libraryBookId).subscribe({
          next: (collectionBook) => {
            this.collectionBooks.push(collectionBook);
            this.snackCommon.showSuccess('Книгу додано до колекції');
          },
          error: (err) => this.snackCommon.showError(err)
        });
      }
    });
  }

  deleteBook(libraryBook: LibraryBook): void {
    this.collectionBookService.removeBookFromCollection(this.collection.id, libraryBook.id).subscribe({
      next: () => {
        this.collectionBooks = this.collectionBooks.filter(cb => cb.libraryBook.id !== libraryBook.id);
        this.snackCommon.showSuccess('Книгу видалено з колекції');
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  statusChange(data: [LibraryBook, LibraryBookStatus]): void {
    this.libraryBookService.changeStatus(data[0].id, data[1]).subscribe({
      next: (libraryBook) => {
        this.updateBook(libraryBook);
        this.snackCommon.showSuccess('Статус змінено');
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  ratingChange(data: { libraryBookId: number; rating: number }): void {
    this.libraryBookService.changeRating(data.libraryBookId, data.rating).subscribe({
      next: (libraryBook) => {
        this.updateBook(libraryBook);
        this.snackCommon.showSuccess('Оцінку змінено');
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  removeFromAllCollections(libraryBook: LibraryBook): void {
    this.collectionBookService.removeFromAllCollections(libraryBook.id).subscribe({
      next: () => {
        this.collectionBooks = this.collectionBooks.filter(cb => cb.libraryBook.id !== libraryBook.id);
        this.snackCommon.showSuccess('Книгу видалено з усіх колекцій');
      },
      error: (err) => this.snackCommon.showError(err)
    });
  }

  goBack() {
    this.router.navigate(['collections']);
  }

  private updateBook(libraryBook: LibraryBook) {
    this.collectionBooks.map(cb =>
      cb.libraryBook = cb.libraryBook.id === libraryBook.id
        ? libraryBook
        : cb.libraryBook);
  }

  private initCollectionBooks() {
    this.collectionBookService.getCollectionBooks(this.collection.id).subscribe(collectionBooks =>
      this.collectionBooks = collectionBooks);
  }

}
