import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BookService {

  constructor(
    private apiService: ApiService,
  ) {
  }

  getAll(page: number = 0, size: number = 10): Observable<any> {
    return this.apiService.get('/books', {params: {page, size}});
  }

}
