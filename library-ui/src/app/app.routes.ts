import { Routes } from '@angular/router';
import { LibraryComponent } from './library/library.component';
import { CollectionsComponent } from './collections/collections.component';
import { CollectionComponent } from './collection/collection.component';
import { BookDetailsComponent } from './book-details/book-details.component';
import { AuthorDetailsComponent } from './author-details/author-details.component';
import { CategoryDetailsComponent } from './category-details/category-details.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { ProfileComponent } from './profile/profile.component';
import { OAuth2RedirectComponent } from './oauth2-redirect/oauth2-redirect.component';
import { SearchComponent } from './search/search.component';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { DashboardComponent } from './dashboard/dashboard.component';
import { RecommendationsComponent } from './recommendations/recommendations.component';

export const routes: Routes = [
  { path: '', redirectTo: '/library', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'recommendations', component: RecommendationsComponent, canActivate: [AuthGuard] },
  { path: 'library', component: LibraryComponent, canActivate: [AuthGuard] },
  { path: 'collections', component: CollectionsComponent, canActivate: [AuthGuard] },
  { path: 'collections/:id', component: CollectionComponent, canActivate: [AuthGuard] },
  { path: 'book-details/:id', component: BookDetailsComponent },
  { path: 'author-details/:id', component: AuthorDetailsComponent },
  { path: 'category-details/:id', component: CategoryDetailsComponent },
  { path: 'search', component: SearchComponent, canActivate: [AuthGuard] },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'oauth2/redirect', component: OAuth2RedirectComponent },
  {
    path: 'admin',
    canActivate: [AdminGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./admin/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent)
      },
      {
        path: 'book/:id',
        loadComponent: () => import('./admin/admin-book-details/admin-book-details.component').then(m => m.AdminBookDetailsComponent)
      },
      {
        path: 'book/new',
        loadComponent: () => import('./admin/admin-book-details/admin-book-details.component').then(m => m.AdminBookDetailsComponent)
      },
      {
        path: 'author/:id',
        loadComponent: () => import('./admin/admin-author-details/admin-author-details.component').then(m => m.AdminAuthorDetailsComponent)
      },
      {
        path: 'author/new',
        loadComponent: () => import('./admin/admin-author-details/admin-author-details.component').then(m => m.AdminAuthorDetailsComponent)
      },
      {
        path: 'category/:id',
        loadComponent: () => import('./admin/admin-category-details/admin-category-details.component').then(m => m.AdminCategoryDetailsComponent)
      },
      {
        path: 'category/new',
        loadComponent: () => import('./admin/admin-category-details/admin-category-details.component').then(m => m.AdminCategoryDetailsComponent)
      }
    ]
  },
];
