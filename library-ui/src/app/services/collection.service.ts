import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Collection} from '../interfaces/collection';
import {Observable} from 'rxjs';
import {CreateCollection} from '../interfaces/create-collection';

@Injectable({
  providedIn: 'root'
})
export class CollectionService {

  constructor(private apiService: ApiService) {
  }

  getAll(): Observable<Collection[]> {
    return this.apiService.get('/collections', {});
  }

  getById(id: number): Observable<Collection> {
    return this.apiService.get(`/collections/${id}`, {});
  }

  create(body: CreateCollection): Observable<Collection> {
    return this.apiService.post('/collections', {body});
  }

  update(id: number, body: CreateCollection): Observable<Collection> {
    return this.apiService.put(`/collections/${id}`, {body});
  }

}
