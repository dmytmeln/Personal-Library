import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {Author} from '../interfaces/author';

@Injectable({
  providedIn: 'root'
})
export class AuthorService {

  constructor(
    private apiService: ApiService,
  ) {
  }

  getById(authorId: number): Observable<Author> {
    return this.apiService.get(`/authors/${authorId}`, {});
  }

}
