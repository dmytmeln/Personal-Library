import { Routes } from '@angular/router';
import { LibraryComponent } from './library/library.component';
import { CollectionsComponent } from './collections/collections.component';
import { CollectionComponent } from './collection/collection.component';
import { BookDetailsComponent } from './book-details/book-details.component';
import { AuthorDetailsComponent } from './author-details/author-details.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { ProfileComponent } from './profile/profile.component';
import { OAuth2RedirectComponent } from './oauth2-redirect/oauth2-redirect.component';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/library', pathMatch: 'full' },
  { path: 'library', component: LibraryComponent, canActivate: [AuthGuard] },
  { path: 'collections', component: CollectionsComponent, canActivate: [AuthGuard] },
  { path: 'collection-full-data', component: CollectionComponent, canActivate: [AuthGuard] },
  { path: 'book-details', component: BookDetailsComponent },
  { path: 'author-details', component: AuthorDetailsComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'oauth2/redirect', component: OAuth2RedirectComponent },
];
