import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {CollectionBookService} from '../services/collection-book.service';
import {CollectionService} from '../services/collection.service';
import {Collection} from '../interfaces/collection';
import {CollectionBook} from '../interfaces/collection-book';

@Component({
  selector: 'app-collection',
  imports: [],
  templateUrl: './collection.component.html',
  styleUrl: './collection.component.scss'
})
export class CollectionComponent implements OnInit {

  collection!: Collection;
  collectionBooks: CollectionBook[] = [];

  constructor(
    private route: ActivatedRoute,
    private collectionService: CollectionService,
    private collectionBookService: CollectionBookService,
  ) {
  }

  ngOnInit() {
    this.subscribeToRouteParams();
  }

  private subscribeToRouteParams() {
    this.route.paramMap.subscribe(params => {
      const collectionId = Number(params.get('id')!);
      this.collectionService.getById(collectionId).subscribe(collection => {
        this.collection = collection;
      });
      this.collectionBookService.getCollectionBooks(collectionId).subscribe(collectionBooks => {
        this.collectionBooks = collectionBooks;
      });
    });
  }

}
