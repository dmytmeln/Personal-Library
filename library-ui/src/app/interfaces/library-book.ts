import {Book} from './book';

export interface LibraryBook {
  id: number;
  status: LibraryBookStatus,
  addedAt: Date,
  rating?: number,
  book: Book
}

export enum LibraryBookStatus {
  FAVORITE = 'FAVORITE',
  TO_READ = 'TO_READ',
  READ = 'READ',
  READING = 'READING',
  STOP = 'STOP',
  NO_TAG = 'NO_TAG',
}

export const LIBRARY_BOOK_STATUSES = [
  LibraryBookStatus.FAVORITE,
  LibraryBookStatus.TO_READ,
  LibraryBookStatus.READ,
  LibraryBookStatus.READING,
  LibraryBookStatus.STOP,
  LibraryBookStatus.NO_TAG,
];

export function getStatusName(status: LibraryBookStatus): string {
  switch (status) {
    case LibraryBookStatus.TO_READ:
      return 'Хочу прочитати';
    case LibraryBookStatus.READING:
      return 'Читаю зараз';
    case LibraryBookStatus.READ:
      return 'Прочитано';
    case LibraryBookStatus.NO_TAG:
      return 'Без тегу';
    case LibraryBookStatus.STOP:
      return 'Перестав читати';
    case LibraryBookStatus.FAVORITE:
      return 'Улюблені';
    default:
      throw new Error('Unsupported status: ' + status);
  }
}
