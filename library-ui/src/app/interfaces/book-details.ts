import {BasicCollection} from './basic-collection';
import {LibraryBook} from './library-book';
import {Book} from './book';

export const BOOK_DETAIL_TYPES = {
  GLOBAL: 'GLOBAL',
  LIBRARY: 'LIBRARY',
} as const;

export type BookDetailType = typeof BOOK_DETAIL_TYPES[keyof typeof BOOK_DETAIL_TYPES];

export interface BaseBookDetails {
  type: BookDetailType;
  averageRating: number;
  ratingsNumber: number;
}

export interface GlobalBookDetails extends BaseBookDetails {
  type: typeof BOOK_DETAIL_TYPES.GLOBAL;
  book: Book;
}

export interface LibraryBookDetails extends BaseBookDetails {
  type: typeof BOOK_DETAIL_TYPES.LIBRARY;
  libraryBook: LibraryBook;
  collections: BasicCollection[];
}

export type BookDetails = GlobalBookDetails | LibraryBookDetails;
