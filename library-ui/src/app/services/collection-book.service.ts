import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {CollectionBook} from '../interfaces/collection-book';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CollectionBookService {

  constructor(private apiService: ApiService) {
  }

  getCollectionBooks(collectionId: number): Observable<CollectionBook[]> {
    return this.apiService.get(`/collections/${collectionId}/books`, {});
  }

  addBookToCollection(collectionId: number, libraryBookId: number): Observable<CollectionBook> {
    return this.apiService.post(`/collections/${collectionId}/books/${libraryBookId}`, {});
  }

  removeBookFromCollection(collectionId: number, libraryBookId: number): Observable<void> {
    return this.apiService.delete(`/collections/${collectionId}/books/${libraryBookId}`, {});
  }

  removeFromAllCollections(libraryBookId: number): Observable<void> {
    return this.apiService.delete('/collections/books', {
      params: { libraryBookId }
    });
  }

  moveBookToOtherCollection(fromCollectionId: number, libraryBookId: number, toCollectionId: number): Observable<CollectionBook> {
    return this.apiService.post(`/collections/${fromCollectionId}/books/${libraryBookId}/move/${toCollectionId}`, {});
  }

}
