import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {UserBook, UserBookStatus} from '../interfaces/user-book';

@Injectable({
  providedIn: 'root'
})
export class UserBookService {

  constructor(
    private apiService: ApiService,
  ) {
  }

  getAll(): Observable<Array<UserBook>> {
    return this.apiService.get('/library/books', {});
  }

  addBookToLibrary(bookId: number): Observable<UserBook> {
    return this.apiService.post(`/library/books/${bookId}`, {});
  }

  deleteBookFromLibrary(bookId: number): Observable<void> {
    return this.apiService.delete(`/library/books/${bookId}`, {});
  }

  changeStatus(bookId: number, status: UserBookStatus): Observable<UserBook> {
    return this.apiService.put(`/library/books/${bookId}/status`, {params: {status}});
  }

  changeRating(bookId: number, rating: number): Observable<UserBook> {
    return this.apiService.put(`/library/books/${bookId}/rating`, {params: {rating}});
  }

}
