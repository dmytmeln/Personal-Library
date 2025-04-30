import {Routes} from '@angular/router';
import {LibraryComponent} from './library/library.component';
import {CollectionsComponent} from './collections/collections.component';
import {CollectionComponent} from './collection/collection.component';

export const routes: Routes = [
  {path: 'library', component: LibraryComponent},
  {path: 'collections', component: CollectionsComponent},
  {path: 'collections/:id', component: CollectionComponent},
];
