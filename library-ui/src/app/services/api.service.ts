import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private readonly baseUrl: string = 'http://localhost:8080/api/v1';
  private readonly headers: any = {};

  constructor(
    private httpClient: HttpClient,
  ) {
  }

  get<T>(url: string, data: any): Observable<T> {
    return this.httpClient.get<T>(this.baseUrl + url, this.getRequestOptions(data));
  }

  post<T>(url: string, data: any): Observable<T> {
    return this.httpClient.post<T>(this.baseUrl + url, this.getRequestBody(data), this.getRequestOptions(data));
  }

  put<T>(url: string, data: any): Observable<T> {
    return this.httpClient.put<T>(this.baseUrl + url, this.getRequestBody(data), this.getRequestOptions(data));
  }

  delete<T>(url: string, data: any): Observable<T> {
    return this.httpClient.delete<T>(this.baseUrl + url, this.getRequestOptions(data));
  }

  private getRequestOptions(data: any) {
    const headers = new HttpHeaders(Object.assign({}, this.headers));

    return {
      params: data.params ? data.params : {},
      headers: Object.assign(headers, data.headers ? data.headers : {}),
      withCredentials: true,
    };
  }

  private getRequestBody(data: any): any {
    return data.body
      ? data.body
      : {};
  }

}
