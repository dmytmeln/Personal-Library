import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Collection} from '../interfaces/collection';
import {Observable} from 'rxjs';
import {CreateCollection} from '../interfaces/create-collection';
import {UpdateCollection} from '../interfaces/update-collection';

@Injectable({
  providedIn: 'root'
})
export class CollectionService {

  constructor(private apiService: ApiService) {
  }

  getTree(): Observable<Collection[]> {
    return this.apiService.get('/collections/tree', {});
  }

  getCollectionsContainingBook(libraryBookId: number): Observable<Collection[]> {
    return this.apiService.get('/collections', {params: {libraryBookId}});
  }

  getById(id: number): Observable<Collection> {
    return this.apiService.get(`/collections/${id}`, {});
  }

  create(body: CreateCollection): Observable<Collection> {
    return this.apiService.post('/collections', {body});
  }

  update(id: number, body: UpdateCollection): Observable<Collection> {
    return this.apiService.put(`/collections/${id}`, {body});
  }

  delete(id: number): Observable<void> {
    return this.apiService.delete(`/collections/${id}`, {});
  }

  move(collectionId: number, newParentId: number | null): Observable<void> {
    return this.apiService.patch(`/collections/${collectionId}/move`, {body: {newParentId}});
  }

}
