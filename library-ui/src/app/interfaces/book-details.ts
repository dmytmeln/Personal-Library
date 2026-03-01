import {BasicCollection} from './basic-collection';
import {LibraryBook} from './library-book';
import {Book} from './book';

export interface BookDetails {
  book?: Book;
  libraryBook?: LibraryBook;
  averageRating: number;
  ratingsNumber: number;
  collections: BasicCollection[];
}
