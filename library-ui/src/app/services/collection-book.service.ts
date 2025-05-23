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

  addBookToCollection(collectionId: number, bookId: number): Observable<CollectionBook> {
    return this.apiService.post(`/collections/${collectionId}/books/${bookId}`, {});
  }

  removeBookFromCollection(collectionId: number, bookId: number): Observable<void> {
    return this.apiService.delete(`/collections/${collectionId}/books/${bookId}`, {});
  }

}
