import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {LibraryBook, LibraryBookStatus} from '../interfaces/library-book';
import {Page} from '../interfaces/page';

@Injectable({
  providedIn: 'root'
})
export class LibraryBookService {

  constructor(
    private apiService: ApiService,
  ) {
  }

  getAll(page: number = 0, size: number = 10): Observable<Page<LibraryBook>> {
    return this.apiService.get('/users/me/library-books', {params: page, size});
  }

  addBook(bookId: number): Observable<LibraryBook> {
    return this.apiService.post('/users/me/library-books', {params: {bookId}});
  }

  removeBook(bookId: number): Observable<void> {
    return this.apiService.delete(`/users/me/library-books/${bookId}`, {});
  }

  changeStatus(bookId: number, status: LibraryBookStatus): Observable<LibraryBook> {
    return this.apiService.put(`/users/me/library-books/${bookId}/status`, {params: {status}});
  }

  changeRating(bookId: number, rating: number): Observable<LibraryBook> {
    return this.apiService.put(`/users/me/library-books/${bookId}/rating`, {params: {rating}});
  }

}
