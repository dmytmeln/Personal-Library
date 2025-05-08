import {Collection} from './collection';
import {LibraryBookStatus} from './library-book';

export interface BookDetails {
  averageRating: number;
  ratingsNumber: number;
  myRating: number;
  collections: Collection[];
  status?: LibraryBookStatus;
  isInLibrary: boolean;
}
