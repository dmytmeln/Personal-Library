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

  readonly SHOW_DELAY = 500;

  dataSource: Collection[] = [];
  childrenAccessor = (node: Collection) => node.children;
  hasChild = (_: number, node: Collection) => !!node.children && node.children.length > 0;

  constructor(
    private collectionService: CollectionService,
    private dialog: MatDialog,
  ) {
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
      this.collectionService.create(collection).subscribe(() => this.getTree());
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
        this.collectionService.update(node.id, updatedCollection).subscribe(() => this.getTree());
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
      this.collectionService.delete(node.id).subscribe(() => this.getTree());
    });
  }
}
