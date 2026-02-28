import {Injectable} from '@angular/core';
import {CanActivate, Router, UrlTree} from '@angular/router';
import {map, Observable} from 'rxjs';
import {AuthService} from '../services/auth.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSnackCommon} from '../common/mat-snack-common';
import {TranslocoService} from '@jsverse/transloco';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  private matSnackCommon: MatSnackCommon;

  constructor(
    private authService: AuthService,
    private router: Router,
    manSnackBar: MatSnackBar,
    private translocoService: TranslocoService
  ) {
    this.matSnackCommon = new MatSnackCommon(manSnackBar);
  }

  canActivate(): Observable<boolean | UrlTree> {
    return this.authService.checkAuthStatus().pipe(
      map(isAuthenticated => {
        if (isAuthenticated) {
          return true;
        }
        this.matSnackCommon.showError(this.translocoService.translate('auth.login.required'));
        return this.router.createUrlTree(['/login']);
      })
    );
  }
}
