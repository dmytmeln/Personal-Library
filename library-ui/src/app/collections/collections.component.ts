import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {CollectionService} from '../services/collection.service';
import {Collection} from '../interfaces/collection';
import {CreateCollection} from '../interfaces/create-collection';
import {MatTreeModule} from '@angular/material/tree';
import {FlatTreeControl} from '@angular/cdk/tree';
import {MatTreeFlatDataSource, MatTreeFlattener} from '@angular/material/tree';
import {CollectionNode} from '../interfaces/collection-node';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {CollectionDialogComponent, CollectionDialogData} from '../dialogs/collection-dialog/collection-dialog.component';
import {UpdateCollection} from '../interfaces/update-collection';
import {filter}from 'rxjs';
import {CdkDragDrop, DragDropModule} from '@angular/cdk/drag-drop';
import {MatMenuModule} from '@angular/material/menu';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-collections',
  imports: [
    MatTreeModule,
    MatIconModule,
    MatButtonModule,
    DragDropModule,
    MatMenuModule,
    RouterLink,
  ],
  templateUrl: './collections.component.html',
  styleUrl: './collections.component.scss'
})
export class CollectionsComponent implements OnInit {

  private _transformer = (node: Collection, level: number): CollectionNode => {
    return {
      id: node.id,
      name: node.name,
      expandable: !!node.children && node.children.length > 0,
      level: level,
      parentId: node.parentId || null,
    };
  };

  treeControl = new FlatTreeControl<CollectionNode>(
    node => node.level,
    node => node.expandable,
  );

  treeFlattener = new MatTreeFlattener(
    this._transformer,
    node => node.level,
    node => node.expandable,
    node => node.children,
  );

  dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);

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
      this.dataSource.data = collections;
    });
  }

  hasChild = (_: number, node: CollectionNode) => node.expandable;

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

  openUpdateDialog(node: CollectionNode): void {
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

  deleteCollection(node: CollectionNode): void {
    // This should open a confirmation dialog in a real app
    this.collectionService.delete(node.id).subscribe(() => this.getTree());
  }

  drop(event: CdkDragDrop<CollectionNode[]>): void {
    // This is a simplified implementation. A real implementation would need to
    // handle edge cases and provide better user feedback.
    const nodeToMove = event.item.data as CollectionNode;
    const dropTargetNode = this.treeControl.dataNodes[event.currentIndex];

    let newParentId: number | null = null;
    if (dropTargetNode) {
      newParentId = dropTargetNode.id;
    }

    if (nodeToMove.parentId === newParentId) {
      return; // No change
    }

    this.collectionService.move(nodeToMove.id, newParentId).subscribe(() => {
      this.getTree();
    });
  }
}
