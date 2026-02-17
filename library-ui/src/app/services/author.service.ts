import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {Author} from '../interfaces/author';
import {Page} from '../interfaces/page';

@Injectable({
  providedIn: 'root'
})
export class AuthorService {

  constructor(
    private apiService: ApiService,
  ) {
  }

  search(name: string, page: number = 0, size: number = 10): Observable<Page<Author>> {
    const params: {name?: string, page: number, size: number} = {page, size};
    if (name && name.length > 0) {
      params.name = name;
    }
    return this.apiService.get('/authors', {params});
  }

  getById(authorId: number): Observable<Author> {
    return this.apiService.get(`/authors/${authorId}`, {});
  }

}
