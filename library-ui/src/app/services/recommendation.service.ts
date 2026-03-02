import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {Book} from '../interfaces/book';

@Injectable({
  providedIn: 'root'
})
export class RecommendationService {

  private readonly baseUrl = '/recommendations';

  constructor(private apiService: ApiService) {
  }

  getPersonalized(limit?: number): Observable<Book[]> {
    return this.apiService.get<Book[]>(this.baseUrl, {params: {limit}});
  }

  getPopular(limit?: number): Observable<Book[]> {
    return this.apiService.get<Book[]>(`${this.baseUrl}/popular`, {params: {limit}});
  }

  getNewArrivals(limit?: number): Observable<Book[]> {
    return this.apiService.get<Book[]>(`${this.baseUrl}/new`, {params: {limit}});
  }

}
