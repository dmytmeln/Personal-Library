import {BasicCollection} from './basic-collection';
import {LibraryBookStatus} from './library-book';

export interface BookDetails {
  averageRating: number;
  ratingsNumber: number;
  myRating: number;
  collections: BasicCollection[];
  status?: LibraryBookStatus;
  isInLibrary: boolean;
}
