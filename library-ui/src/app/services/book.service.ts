import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {BookDetails} from '../interfaces/book-details';
import {Page} from '../interfaces/page';
import {Book} from '../interfaces/book';

@Injectable({
  providedIn: 'root'
})
export class BookService {

  constructor(
    private apiService: ApiService,
  ) {
  }

  getAll(authorId: number | null = null, page: number = 0, size: number = 10): Observable<Page<Book>> {
    let params: Params = {page, size};
    if (authorId) {
      params = {
        ...params,
        authorId
      };
    }
    return this.apiService.get('/books', {params});
  }

  getBookDetails(bookId: number): Observable<BookDetails> {
    return this.apiService.get(`/books/${bookId}/details`, {});
  }

}

interface Params {
  page: number,
  size: number,
  authorId?: number,
  categoryId?: number,
}
