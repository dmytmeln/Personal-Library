import {LibraryBookStatus} from './library-book';

export interface CreateLocalBook {
  title: string;
  description?: string;
  bookLanguage?: string;
  status: LibraryBookStatus;
  publishYear?: number;
  pages?: number;
  categoryId?: number;
  authorIds?: number[];
  customCategoryName?: string;
  customAuthorName?: string;
}
