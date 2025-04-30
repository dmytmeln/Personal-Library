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
    return this.apiService.get('/users/me/collections', {});
  }

  getById(id: number): Observable<Collection> {
    return this.apiService.get(`/api/v1/collections/${id}`, {});
  }

  create(body: CreateCollection): Observable<Collection> {
    return this.apiService.post('/users/me/collections', {body});
  }

}
