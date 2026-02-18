import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {Author} from '../interfaces/author';
import {Page} from '../interfaces/page';
import {CountryWithCount} from '../interfaces/country-with-count';

@Injectable({
  providedIn: 'root'
})
export class AuthorService {

  constructor(
    private apiService: ApiService,
  ) {
  }

  search(options: {
    name?: string,
    page?: number,
    size?: number,
    sort?: string[],
    country?: string,
    birthYearMin?: number,
    birthYearMax?: number,
    booksCountMin?: number,
    booksCountMax?: number
  } = {}): Observable<Page<Author>> {
    const {
      name,
      page = 0,
      size = 10,
      sort,
      country,
      birthYearMin,
      birthYearMax,
      booksCountMin,
      booksCountMax
    } = options;

    const params: Params = {page, size};
    if (name && name.length > 0) {
      params.name = name;
    }
    if (sort && sort.length > 0) {
      params.sort = sort;
    }
    if (country) {
      params.country = country;
    }
    if (birthYearMin !== undefined) {
      params.birthYearMin = birthYearMin;
    }
    if (birthYearMax !== undefined) {
      params.birthYearMax = birthYearMax;
    }
    if (booksCountMin !== undefined) {
      params.booksCountMin = booksCountMin;
    }
    if (booksCountMax !== undefined) {
      params.booksCountMax = booksCountMax;
    }
    return this.apiService.get('/authors', {params});
  }

  getCountries(): Observable<CountryWithCount[]> {
    return this.apiService.get('/authors/countries', {});
  }

  getById(authorId: number): Observable<Author> {
    return this.apiService.get(`/authors/${authorId}`, {});
  }

}

interface Params {
  page: number;
  size: number;
  name?: string;
  sort?: string[];
  country?: string;
  birthYearMin?: number;
  birthYearMax?: number;
  booksCountMin?: number;
  booksCountMax?: number;
}
