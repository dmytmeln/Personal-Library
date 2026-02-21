import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {CollectionService} from '../services/collection.service';
import {Collection} from '../interfaces/collection';
import {CreateCollection} from '../interfaces/create-collection';
import {MatTreeModule} from '@angular/material/tree';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {CollectionDialogComponent, CollectionDialogData} from '../dialogs/collection-dialog/collection-dialog.component';
import {UpdateCollection} from '../interfaces/update-collection';
import {filter} from 'rxjs';
import {MatMenuModule} from '@angular/material/menu';
import {RouterLink} from '@angular/router';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ConfirmationDialogComponent} from '../dialogs/confirmation-dialog/confirmation-dialog.component';
import {SelectedCollection} from '../interfaces/selected-collection';
import {
  CollectionSelectorDialogComponent, CollectionSelectorDialogData
} from '../dialogs/collection-selector-dialog/collection-selector-dialog.component';
import {MatSnackCommon} from '../common/mat-snack-common';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-collections',
  imports: [
    MatTreeModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    RouterLink,
    MatTooltipModule,
  ],
  templateUrl: './collections.component.html',
  styleUrl: './collections.component.scss'
})
export class CollectionsComponent implements OnInit {

  readonly SHOW_DELAY = 100;

  dataSource: Collection[] = [];
  childrenAccessor = (node: Collection) => node.children;
  hasChild = (_: number, node: Collection) => !!node.children && node.children.length > 0;

  private snackCommon: MatSnackCommon;

  constructor(
    private collectionService: CollectionService,
    private dialog: MatDialog,
    private matSnackBar: MatSnackBar,
  ) {
    this.snackCommon = new MatSnackCommon(this.matSnackBar);
  }

  ngOnInit(): void {
    this.getTree();
  }

  private getTree(): void {
    this.collectionService.getTree().subscribe((collections: Collection[]) => {
      this.dataSource = collections;
    });
  }

  openCreateDialog(parentId?: number): void {
    const dialogRef = this.dialog.open(CollectionDialogComponent, {
      data: {
        isEdit: false,
        parentId: parentId
      } as CollectionDialogData
    });

    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe((collection: CreateCollection) => {
      this.collectionService.create(collection).subscribe({
        next: () => {
          this.getTree();
          this.snackCommon.showSuccess('Колекцію створено успішно');
        },
        error: (err) => this.snackCommon.showError(err)
      });
    });
  }

  openUpdateDialog(node: Collection): void {
    this.collectionService.getById(node.id).subscribe(collection => {
      const dialogRef = this.dialog.open(CollectionDialogComponent, {
        data: {
          isEdit: true,
          collection: collection
        } as CollectionDialogData
      });

      dialogRef.afterClosed().pipe(filter(Boolean)).subscribe((updatedCollection: UpdateCollection) => {
        this.collectionService.update(node.id, updatedCollection).subscribe({
          next: () => {
            this.getTree();
            this.snackCommon.showSuccess('Колекцію оновлено успішно');
          },
          error: (err) => this.snackCommon.showError(err)
        });
      });
    });
  }

  deleteCollection(node: Collection): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        message: `Ви впевнені, що хочете видалити колекцію "${node.name}"? Це також призведе до видалення всіх її підколекцій та посилань на книги.`
      }
    });

    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe(() => {
      this.collectionService.delete(node.id).subscribe({
        next: () => {
          this.getTree();
          this.snackCommon.showSuccess('Колекцію видалено успішно');
        },
        error: (err) => this.snackCommon.showError(err)
      });
    });
  }

    addChildCollection(targetParent: Collection): void {
      const disabledIds = [
        targetParent.id,
        ...this.getAncestorIds(targetParent, this.dataSource),
        ...this.getSubtreeIds(targetParent)
      ];

      const dialogRef = this.dialog.open(CollectionSelectorDialogComponent, {
        data: {
          initialSelectionId: null,
          disabledIds: disabledIds,
          showRoot: false
        } as CollectionSelectorDialogData
      });

      dialogRef.afterClosed().pipe(filter(result => result !== undefined)).subscribe((selection: SelectedCollection) => {
        if (selection.id) {
          this.collectionService.move(selection.id, targetParent.id).subscribe({
            next: () => {
              this.getTree();
              this.snackCommon.showSuccess('Колекцію додано успішно');
            },
            error: (err) => this.snackCommon.showError(err)
          });
        }
      });
    }

    moveCollection(collectionToMove: Collection): void {
      const disabledIds = [collectionToMove.id, ...this.getSubtreeIds(collectionToMove)];
      if (collectionToMove.parentId) {
        disabledIds.push(collectionToMove.parentId);
      }

      const dialogRef = this.dialog.open(CollectionSelectorDialogComponent, {
        data: {
          initialSelectionId: collectionToMove.parentId || null,
          disabledIds: disabledIds,
          showRoot: true
        } as CollectionSelectorDialogData
      });

      dialogRef.afterClosed().pipe(filter(result => result !== undefined)).subscribe((selection: SelectedCollection) => {
        this.collectionService.move(collectionToMove.id, selection.id).subscribe({
          next: () => {
            this.getTree();
            this.snackCommon.showSuccess('Колекцію переміщено успішно');
          },
          error: (err) => this.snackCommon.showError(err)
        });
      });
    }
    private getSubtreeIds(node: Collection): number[] {
    let ids: number[] = [];
    if (node.children) {
      for (const child of node.children) {
        ids.push(child.id);
        ids = ids.concat(this.getSubtreeIds(child));
      }
    }
    return ids;
  }

  private getAncestorIds(target: Collection, tree: Collection[]): number[] {
    const path = this.findPath(target.id, tree);
    if (path) {
      return path.map(c => c.id).filter(id => id !== target.id);
    }
    return [];
  }

  private findPath(targetId: number, currentLevel: Collection[], path: Collection[] = []): Collection[] | null {
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
