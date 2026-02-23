import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {LibraryBook, LibraryBookStatus} from '../interfaces/library-book';
import {Page} from '../interfaces/page';
import {UpdateLibraryBookDetails} from '../interfaces/update-library-book-details';

@Injectable({
  providedIn: 'root'
})
export class LibraryBookService {

  constructor(
    private apiService: ApiService,
  ) {
  }

  getAll(page: number = 0, size: number = 10): Observable<Page<LibraryBook>> {
    return this.apiService.get('/users/me/library-books', {params: {page, size}});
  }

  addBook(bookId: number): Observable<LibraryBook> {
    return this.apiService.post('/users/me/library-books', {params: {bookId}});
  }

  removeBook(libraryBookId: number): Observable<void> {
    return this.apiService.delete(`/users/me/library-books/${libraryBookId}`, {});
  }

  changeStatus(libraryBookId: number, status: LibraryBookStatus): Observable<LibraryBook> {
    return this.apiService.put(`/users/me/library-books/${libraryBookId}/status`, {params: {status}});
  }

  changeRating(libraryBookId: number, rating: number): Observable<LibraryBook> {
    return this.apiService.put(`/users/me/library-books/${libraryBookId}/rating`, {params: {rating}});
  }

  updateDetails(libraryBookId: number, dto: UpdateLibraryBookDetails): Observable<LibraryBook> {
    return this.apiService.put(`/users/me/library-books/${libraryBookId}/details`, {body: dto});
  }

  resetDetails(libraryBookId: number): Observable<LibraryBook> {
    return this.apiService.put(`/users/me/library-books/${libraryBookId}/details/reset`, {});
  }

}
