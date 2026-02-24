import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {Author} from '../interfaces/author';
import {Page} from '../interfaces/page';
import {CountryWithCount} from '../interfaces/country-with-count';

export interface AuthorSearchOptions {
  name?: string;
  page?: number;
  size?: number;
  sort?: string[];
  country?: string;
  birthYearMin?: number;
  birthYearMax?: number;
  booksCountMin?: number;
  booksCountMax?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthorService {

  constructor(
    private apiService: ApiService,
  ) {
  }

  search(options: AuthorSearchOptions = {}): Observable<Page<Author>> {
    return this.apiService.get('/authors', {params: this.buildParams(options)});
  }

  searchMe(options: AuthorSearchOptions = {}): Observable<Page<Author>> {
    return this.apiService.get('/authors/me', {params: this.buildParams(options)});
  }

  getCountries(): Observable<CountryWithCount[]> {
    return this.apiService.get('/authors/countries', {});
  }

  getCountriesMe(): Observable<CountryWithCount[]> {
    return this.apiService.get('/authors/countries/me', {});
  }

  getById(authorId: number): Observable<Author> {
    return this.apiService.get(`/authors/${authorId}`, {});
  }

  private buildParams(options: AuthorSearchOptions): AuthorSearchOptions {
    const {
      page = 0,
      size = 12,
      name,
      sort,
      country,
      birthYearMin,
      birthYearMax,
      booksCountMin,
      booksCountMax
    } = options;

    const params: AuthorSearchOptions = {page, size};

    if (name) params.name = name;
    if (country) params.country = country;
    if (sort && sort.length > 0) params.sort = sort;
    if (birthYearMin != null) params.birthYearMin = birthYearMin;
    if (birthYearMax != null) params.birthYearMax = birthYearMax;
    if (booksCountMin != null) params.booksCountMin = booksCountMin;
    if (booksCountMax != null) params.booksCountMax = booksCountMax;

    return params;
  }

}
