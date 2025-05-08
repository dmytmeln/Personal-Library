import {Routes} from '@angular/router';
import {LibraryComponent} from './library/library.component';
import {CollectionsComponent} from './collections/collections.component';
import {CollectionComponent} from './collection/collection.component';
import {BookDetailsComponent} from './book-details/book-details.component';
import {AuthorDetailsComponent} from './author-details/author-details.component';

export const routes: Routes = [
  {path: 'library', component: LibraryComponent},
  {path: 'collections', component: CollectionsComponent},
  {path: 'collection-full-data', component: CollectionComponent},
  {path: 'book-details', component: BookDetailsComponent},
  {path: 'author-details', component: AuthorDetailsComponent},
];
