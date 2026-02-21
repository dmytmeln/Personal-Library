import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {CreateCollection} from '../interfaces/create-collection';
import {UpdateCollection} from '../interfaces/update-collection';
import {CollectionNode} from '../interfaces/collection-node';
import {BasicCollection} from '../interfaces/basic-collection';
import {CollectionDetails} from '../interfaces/collection-details';

@Injectable({
  providedIn: 'root'
})
export class CollectionService {

  constructor(private apiService: ApiService) {
  }

  getTree(): Observable<CollectionNode[]> {
    return this.apiService.get('/collections/tree', {});
  }

  getCollectionsContainingBook(libraryBookId: number): Observable<BasicCollection[]> {
    return this.apiService.get('/collections', {params: {libraryBookId}});
  }

  getById(id: number): Observable<CollectionDetails> {
    return this.apiService.get(`/collections/${id}`, {});
  }

  create(body: CreateCollection): Observable<BasicCollection> {
    return this.apiService.post('/collections', {body});
  }

  update(id: number, body: UpdateCollection): Observable<BasicCollection> {
    return this.apiService.put(`/collections/${id}`, {body});
  }

  delete(id: number): Observable<void> {
    return this.apiService.delete(`/collections/${id}`, {});
  }

  move(collectionId: number, newParentId: number | null): Observable<void> {
    return this.apiService.patch(`/collections/${collectionId}/move`, {body: {newParentId}});
  }

}
