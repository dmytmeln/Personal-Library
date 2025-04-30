import {Component, OnInit} from '@angular/core';
import {Collection} from '../interfaces/collection';
import {CollectionCardComponent} from '../collection-card/collection-card.component';
import {MatDialog} from '@angular/material/dialog';
import {CreateCollectionDialog} from '../dialogs/create-collection-dialog/create-collection-dialog';
import {CollectionService} from '../services/collection.service';
import {MatButton} from '@angular/material/button';
import {CreateCollection} from '../interfaces/create-collection';

@Component({
  selector: 'app-collections',
  imports: [
    CollectionCardComponent,
    MatButton,
  ],
  templateUrl: './collections.component.html',
  styleUrl: './collections.component.scss'
})
export class CollectionsComponent implements OnInit {

  collections: Collection[] = [];

  constructor(
    private collectionService: CollectionService,
    private dialog: MatDialog,
  ) {
  }

  ngOnInit(): void {
    this.getCollections();
  }

  private getCollections(): void {
    this.collectionService.getAll().subscribe((collections: Collection[]) => {
      this.collections = collections;
    });
  }

  openCreateCollectionDialog() {
    const dialogRef = this.dialog.open(CreateCollectionDialog);
    dialogRef.afterClosed().subscribe((collection: CreateCollection | undefined) => {
      if (collection) {
        console.log(collection);
        this.collectionService.create(collection).subscribe(collection => {
          this.collections.push(collection);
        });
      }
    });
  }


}
