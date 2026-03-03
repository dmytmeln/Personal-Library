import {Injectable, signal} from '@angular/core';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {catchError, map, tap} from 'rxjs/operators';
import {environment} from '../../environments/environment';
import {ApiService} from './api.service';
import {UserRegisterRequest} from '../interfaces/user-register-request';
import {UserResponse} from '../interfaces/user-response';
import {AuthenticationRequest} from '../interfaces/authentication-request';
import {UpdateProfileRequest} from '../interfaces/update-profile-request';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private cookieName = environment.cookieName;

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());

  currentUser = signal<UserResponse | null>(null);

  constructor(private apiService: ApiService) {
  }

  login(credentials: AuthenticationRequest): Observable<void> {
    return this.apiService.post<void>('/auth/authenticate', {
      body: credentials
    }).pipe(
      tap(() => {
        this.isAuthenticatedSubject.next(true);
        this.checkAuthStatus().subscribe();
      })
    );
  }

  register(request: UserRegisterRequest): Observable<UserResponse> {
    return this.apiService.post<UserResponse>('/auth/register', {
      body: request
    });
  }

  updateProfile(request: UpdateProfileRequest): Observable<UserResponse> {
    return this.apiService.patch<UserResponse>('/users/me', {
      body: request
    }).pipe(
      tap(user => this.currentUser.set(user))
    );
  }

  logout(): Observable<void> {
    return this.apiService.post<void>('/auth/logout', {}).pipe(
      tap(() => {
        this.isAuthenticatedSubject.next(false);
        this.currentUser.set(null);
      })
    );
  }

  getCurrentUser(): Observable<UserResponse> {
    return this.apiService.get<UserResponse>('/users/me', {}).pipe(
      tap(user => this.currentUser.set(user))
    );
  }

  isAdmin(): boolean {
    return this.currentUser()?.role === 'ADMIN';
  }

  isUser(): boolean {
    return this.currentUser()?.role === 'USER';
  }

  loginWithGoogle(): void {
    window.location.assign(`${environment.apiBaseUrl}/oauth2/authorization/google`);
  }

  checkAuthStatus(): Observable<boolean> {
    return this.getCurrentUser().pipe(
      map(user => {
        this.isAuthenticatedSubject.next(true);
        this.currentUser.set(user);
        return true;
      }),
      catchError(() => {
        this.isAuthenticatedSubject.next(false);
        this.currentUser.set(null);
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
