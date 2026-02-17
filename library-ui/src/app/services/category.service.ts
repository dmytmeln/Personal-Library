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

  search(name: string, page: number = 0, size: number = 10): Observable<Page<Category>> {
    const params: {name?: string, page: number, size: number} = {page, size};
    if (name && name.length > 0) {
      params.name = name;
    }
    return this.apiService.get('/categories', {params});
  }

}
