import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ApiService } from './api.service';
import { UserRegisterRequest } from '../interfaces/user-register-request';
import { UserResponse } from '../interfaces/user-response';
import { AuthenticationRequest } from '../interfaces/authentication-request';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private cookieName = environment.cookieName;

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private apiService: ApiService) {}

  login(credentials: AuthenticationRequest): Observable<void> {
    return this.apiService.post<void>('/auth/authenticate', {
      body: credentials
    }).pipe(
      tap(() => {
        this.isAuthenticatedSubject.next(true);
      })
    );
  }

  register(request: UserRegisterRequest): Observable<UserResponse> {
    return this.apiService.post<UserResponse>('/auth/register', {
      body: request
    });
  }

  logout(): Observable<void> {
    return this.apiService.post<void>('/auth/logout', {}).pipe(
      tap(() => {
        this.isAuthenticatedSubject.next(false);
      })
    );
  }

  getCurrentUser(): Observable<UserResponse> {
    return this.apiService.get<UserResponse>('/users/me', {});
  }

  isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  loginWithGoogle(): void {
    window.location.assign(`${environment.apiBaseUrl}/oauth2/authorization/google`);
  }

  checkAuthStatus(): Observable<boolean> {
    return this.getCurrentUser().pipe(
      map(() => {
        this.isAuthenticatedSubject.next(true);
        return true;
      }),
      catchError(() => {
        this.isAuthenticatedSubject.next(false);
        return of(false);
      })
    );
  }

  private hasToken(): boolean {
    return document.cookie.split(';').some(cookie => {
      const [name] = cookie.trim().split('=');
      return name === this.cookieName;
    });
  }

}
