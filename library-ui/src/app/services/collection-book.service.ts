import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {CollectionBook} from '../interfaces/collection-book';
import {Observable} from 'rxjs';
import {CollectionBookSearchParams} from '../interfaces/collection-book-search-params';
import {Page} from '../interfaces/page';
import {LibraryBook} from '../interfaces/library-book';
import {BulkLibraryBookRequest} from '../interfaces/bulk-library-book-request';

@Injectable({
  providedIn: 'root'
})
export class CollectionBookService {

  constructor(private apiService: ApiService) {
  }

  getCollectionBooks(collectionId: number, params: CollectionBookSearchParams = {}): Observable<Page<LibraryBook>> {
    const cleanParams = Object.fromEntries(
      Object.entries(params).filter(([_, v]) => v !== undefined && v !== null && v !== '')
    );
    return this.apiService.get(`/collections/${collectionId}/books`, {params: cleanParams});
  }

  addBookToCollection(collectionId: number, libraryBookId: number): Observable<CollectionBook> {
    return this.apiService.post(`/collections/${collectionId}/books/${libraryBookId}`, {});
  }

  bulkAdd(collectionId: number, ids: number[]): Observable<void> {
    const body: BulkLibraryBookRequest = {ids};
    return this.apiService.post(`/collections/${collectionId}/books/bulk`, {body});
  }

  removeBookFromCollection(collectionId: number, libraryBookId: number): Observable<void> {
    return this.apiService.delete(`/collections/${collectionId}/books/${libraryBookId}`, {});
  }

  bulkRemove(collectionId: number, ids: number[]): Observable<void> {
    const body: BulkLibraryBookRequest = {ids};
    return this.apiService.post(`/collections/${collectionId}/books/bulk-remove`, {body});
  }

  removeFromAllCollections(libraryBookId: number): Observable<void> {
    return this.apiService.delete('/collections/books', {
      params: {libraryBookId}
    });
  }

  moveBookToOtherCollection(fromCollectionId: number, libraryBookId: number, toCollectionId: number): Observable<CollectionBook> {
    return this.apiService.post(`/collections/${fromCollectionId}/books/${libraryBookId}/move/${toCollectionId}`, {});
  }

}
