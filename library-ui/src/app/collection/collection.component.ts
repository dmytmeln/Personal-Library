import {Component, OnInit} from '@angular/core';
import {CollectionBookService} from '../services/collection-book.service';
import {CollectionService} from '../services/collection.service';
import {CollectionDetails} from '../interfaces/collection-details';
import {CollectionBook} from '../interfaces/collection-book';
import {NgStyle} from '@angular/common';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSnackCommon} from '../common/mat-snack-common';
import {CreateCollection} from '../interfaces/create-collection';
import {ViewBookListDialog, ViewBookListDialogData} from '../dialogs/view-book-list-dialog/view-book-list-dialog';
import {LibraryBookService} from '../services/library-book.service';
import {LibraryBook, LibraryBookStatus} from '../interfaces/library-book';
import {BookComponent} from '../book/book.component';
import {LibraryBookMenuItemsComponent} from '../library-book-menu-items/library-book-menu-items.component';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {MatMenuModule} from '@angular/material/menu';
import {CollectionDialogComponent, CollectionDialogData} from '../dialogs/collection-dialog/collection-dialog.component';
import {UpdateCollection} from '../interfaces/update-collection';
import {filter} from 'rxjs';
import {ConfirmationDialogComponent} from '../dialogs/confirmation-dialog/confirmation-dialog.component';
import {SelectedCollection} from '../interfaces/selected-collection';
import {CollectionSelectorDialogComponent} from '../dialogs/collection-selector-dialog/collection-selector-dialog.component';
import {CollectionNode} from '../interfaces/collection-node';
import {MatDividerModule} from '@angular/material/divider';

@Component({
  selector: 'app-collection',
  imports: [
    NgStyle,
    MatButton,
    MatIconModule,
    BookComponent,
    LibraryBookMenuItemsComponent,
    MatMenuModule,
    RouterLink,
    MatDividerModule,
    MatIconButton,
  ],
  templateUrl: './collection.component.html',
  styleUrl: './collection.component.scss'
})
export class CollectionComponent implements OnInit {

  collection!: CollectionDetails;
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
      this.loadCollection(collectionId);
    });
  }

  private loadCollection(id: number): void {
    this.collectionService.getById(id).subscribe(collection => {
      this.collection = collection;
      this.initCollectionBooks();
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
          this.loadCollection(this.collection.id);
          this.snackCommon.showSuccess('Колекцію оновлено успішно');
        },
        error: (err) => this.snackCommon.showError(err)
      });
    });
  }

  openCreateSubDialog(): void {
    const dialogRef = this.dialog.open(CollectionDialogComponent, {
      data: {
        isEdit: false,
        parentId: this.collection.id
      } as CollectionDialogData
    });

    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe((collection: CreateCollection) => {
      this.collectionService.create(collection).subscribe({
        next: () => {
          this.loadCollection(this.collection.id);
          this.snackCommon.showSuccess('Підколекцію створено успішно');
        },
        error: (err) => this.snackCommon.showError(err)
      });
    });
  }

  addChildCollection(): void {
    this.collectionService.getTree().subscribe(tree => {
      const disabledIds = [
        this.collection.id,
        ...this.getAncestorIds(this.collection.id, tree),
        ...this.getSubtreeIds(this.collection.id, tree)
      ];

      const dialogRef = this.dialog.open(CollectionSelectorDialogComponent, {
        data: {
          initialSelectionId: null,
          disabledIds: disabledIds,
          showRoot: false
        }
      });

      dialogRef.afterClosed().pipe(filter(result => result !== undefined)).subscribe((selection: SelectedCollection) => {
        if (selection.id) {
          this.collectionService.move(selection.id, this.collection.id).subscribe({
            next: () => {
              this.loadCollection(this.collection.id);
              this.snackCommon.showSuccess('Колекцію додано успішно');
            },
            error: (err) => this.snackCommon.showError(err)
          });
        }
      });
    });
  }

  moveCollection(): void {
    this.collectionService.getTree().subscribe(tree => {
      const disabledIds = [this.collection.id, ...this.getSubtreeIds(this.collection.id, tree)];
      if (this.collection.parentId) {
        disabledIds.push(this.collection.parentId);
      }

      const dialogRef = this.dialog.open(CollectionSelectorDialogComponent, {
        data: {
          initialSelectionId: this.collection.parentId || null,
          disabledIds: disabledIds,
          showRoot: true
        }
      });

      dialogRef.afterClosed().pipe(filter(result => result !== undefined)).subscribe((selection: SelectedCollection) => {
        this.collectionService.move(this.collection.id, selection.id).subscribe({
          next: () => {
            this.loadCollection(this.collection.id);
            this.snackCommon.showSuccess('Колекцію переміщено успішно');
          },
          error: (err) => this.snackCommon.showError(err)
        });
      });
    });
  }

  deleteCollection(): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        message: `Ви впевнені, що хочете видалити колекцію "${this.collection.name}"? Це також призведе до видалення всіх її підколекцій та посилань на книги.`
      }
    });

    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe(() => {
      this.collectionService.delete(this.collection.id).subscribe({
        next: () => {
          this.snackCommon.showSuccess('Колекцію видалено успішно');
          this.goBack();
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
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        message: `Ви впевнені, що хочете видалити книгу "${libraryBook.book.title}" з усіх колекцій?`
      }
    });

    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe(() => {
      this.collectionBookService.removeFromAllCollections(libraryBook.id).subscribe({
        next: () => {
          this.collectionBooks = this.collectionBooks.filter(cb => cb.libraryBook.id !== libraryBook.id);
          this.snackCommon.showSuccess('Книгу видалено з усіх колекцій');
        },
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
        }
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

  moveBookToOtherCollection(libraryBook: LibraryBook): void {
    this.collectionService.getCollectionsContainingBook(libraryBook.id).subscribe(collections => {
      const disabledIds = collections.map(c => c.id);

      const dialogRef = this.dialog.open(CollectionSelectorDialogComponent, {
        data: {
          initialSelectionId: null,
          disabledIds: disabledIds,
          showRoot: false
        }
      });

      dialogRef.afterClosed().pipe(filter(result => result !== undefined)).subscribe((selection: SelectedCollection) => {
        if (selection.id) {
          this.collectionBookService.moveBookToOtherCollection(this.collection.id, libraryBook.id, selection.id).subscribe({
            next: () => {
              this.collectionBooks = this.collectionBooks.filter(cb => cb.libraryBook.id !== libraryBook.id);
              this.snackCommon.showSuccess('Книгу переміщено до іншої колекції');
            },
            error: (err) => this.snackCommon.showError(err)
          });
        }
      });
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

  private getSubtreeIds(targetId: number, tree: CollectionNode[]): number[] {
    const node = this.findNode(targetId, tree);
    if (!node) return [];
    return this.collectSubtreeIds(node);
  }

  private collectSubtreeIds(node: CollectionNode): number[] {
    let ids: number[] = [];
    if (node.children) {
      for (const child of node.children) {
        ids.push(child.id);
        ids = ids.concat(this.collectSubtreeIds(child));
      }
    }
    return ids;
  }

  private getAncestorIds(targetId: number, tree: CollectionNode[]): number[] {
    const path = this.findPath(targetId, tree);
    if (path) {
      return path.map(c => c.id).filter(id => id !== targetId);
    }
    return [];
  }

  private findNode(id: number, currentLevel: CollectionNode[]): CollectionNode | null {
    for (const node of currentLevel) {
      if (node.id === id) return node;
      if (node.children) {
        const found = this.findNode(id, node.children);
        if (found) return found;
      }
    }
    return null;
  }

  private findPath(targetId: number, currentLevel: CollectionNode[], path: CollectionNode[] = []): CollectionNode[] | null {
    for (const col of currentLevel) {
      const newPath = [...path, col];
      if (col.id === targetId) return newPath;
      if (col.children) {
        const found = this.findPath(targetId, col.children, newPath);
        if (found) return found;
      }
    }
    return null;
  }

}
