import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {Page} from '../interfaces/page';
import {Category} from '../interfaces/category';
import {CategoryQueryOptions} from './category.service';

@Injectable({
  providedIn: 'root'
})
export class LibraryCategoryService {

  constructor(private apiService: ApiService) {
  }

  getAll(options: CategoryQueryOptions = {}): Observable<Page<Category>> {
    return this.apiService.get('/users/me/library-categories', {params: this.buildParams(options)});
  }

  private buildParams(options: CategoryQueryOptions): CategoryQueryOptions {
    const {page = 0, size = 12, name, sort, booksCountMin, booksCountMax} = options;
    const params: CategoryQueryOptions = {page, size};

    if (name) params.name = name;
    if (sort && sort.length > 0) params.sort = sort;
    if (booksCountMin != null) params.booksCountMin = booksCountMin;
    if (booksCountMax != null) params.booksCountMax = booksCountMax;

    return params;
  }

}
