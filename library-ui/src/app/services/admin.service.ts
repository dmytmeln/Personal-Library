import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {AdminBookDto} from '../interfaces/admin-book-dto';
import {AdminAuthorDto} from '../interfaces/admin-author-dto';
import {AdminCategoryDto} from '../interfaces/admin-category-dto';

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private readonly baseUrl = '/admin';

  constructor(private apiService: ApiService) {
  }

  // Books
  getBook(id: number): Observable<AdminBookDto> {
    return this.apiService.get<AdminBookDto>(`${this.baseUrl}/books/${id}`, {});
  }

  createBook(dto: AdminBookDto): Observable<void> {
    return this.apiService.post<void>(`${this.baseUrl}/books`, { body: dto });
  }

  updateBook(id: number, dto: AdminBookDto): Observable<void> {
    return this.apiService.put<void>(`${this.baseUrl}/books/${id}`, { body: dto });
  }

  deleteBook(id: number): Observable<void> {
    return this.apiService.delete<void>(`${this.baseUrl}/books/${id}`, {});
  }

  // Authors
  getAuthor(id: number): Observable<AdminAuthorDto> {
    return this.apiService.get<AdminAuthorDto>(`${this.baseUrl}/authors/${id}`, {});
  }

  createAuthor(dto: AdminAuthorDto): Observable<void> {
    return this.apiService.post<void>(`${this.baseUrl}/authors`, { body: dto });
  }

  updateAuthor(id: number, dto: AdminAuthorDto): Observable<void> {
    return this.apiService.put<void>(`${this.baseUrl}/authors/${id}`, { body: dto });
  }

  deleteAuthor(id: number): Observable<void> {
    return this.apiService.delete<void>(`${this.baseUrl}/authors/${id}`, {});
  }

  // Categories
  getCategory(id: number): Observable<AdminCategoryDto> {
    return this.apiService.get<AdminCategoryDto>(`${this.baseUrl}/categories/${id}`, {});
  }

  createCategory(dto: AdminCategoryDto): Observable<void> {
    return this.apiService.post<void>(`${this.baseUrl}/categories`, { body: dto });
  }

  updateCategory(id: number, dto: AdminCategoryDto): Observable<void> {
    return this.apiService.put<void>(`${this.baseUrl}/categories/${id}`, { body: dto });
  }

  deleteCategory(id: number): Observable<void> {
    return this.apiService.delete<void>(`${this.baseUrl}/categories/${id}`, {});
  }

}
