import {Component} from '@angular/core';
import {MatToolbar} from '@angular/material/toolbar';
import {MatAnchor} from '@angular/material/button';
import {RouterLink, RouterLinkActive} from '@angular/router';

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

  private readonly API_URL = 'http://localhost:8080';

  login() {
    window.location.assign(`${this.API_URL}/oauth2/authorization/google`);
  }

}
