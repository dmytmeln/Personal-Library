import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {Page} from '../interfaces/page';
import {Category} from '../interfaces/category';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  constructor(private apiService: ApiService) {
  }

  search(options: {
    name?: string,
    page?: number,
    size?: number,
    sort?: string[],
    booksCountMin?: number,
    booksCountMax?: number
  } = {}): Observable<Page<Category>> {
    const {
      name,
      page = 0,
      size = 10,
      sort,
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
    if (booksCountMin !== undefined) {
      params.booksCountMin = booksCountMin;
    }
    if (booksCountMax !== undefined) {
      params.booksCountMax = booksCountMax;
    }
    return this.apiService.get('/categories', {params});
  }

}

interface Params {
  page: number;
  size: number;
  name?: string;
  sort?: string[];
  booksCountMin?: number;
  booksCountMax?: number;
}
