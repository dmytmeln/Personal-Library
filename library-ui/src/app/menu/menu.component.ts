import {Component} from '@angular/core';
import {MatToolbar} from '@angular/material/toolbar';
import {MatAnchor} from '@angular/material/button';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {environment} from '../../environments/environment';

@Component({
  selector: 'app-menu',
  imports: [
    MatToolbar,
    RouterLink,
    MatAnchor,
    RouterLinkActive,
  ],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.scss'
})
export class MenuComponent {

  login() {
    window.location.assign(`${environment.apiBaseUrl}/oauth2/authorization/google`);
  }

}
