import {Book} from './book';

export interface UserBook {
  status: UserBookStatus,
  dateAdded: Date,
  rating?: number,
  book: Book
}

export enum UserBookStatus {
  FAVORITE = 'FAVORITE',
  TO_READ = 'TO_READ',
  READ = 'READ',
  READING = 'READING',
  STOP = 'STOP',
  NO_TAG = 'NO_TAG',
}

export const USER_BOOK_STATUSES = [
  UserBookStatus.FAVORITE,
  UserBookStatus.TO_READ,
  UserBookStatus.READ,
  UserBookStatus.READING,
  UserBookStatus.STOP,
  UserBookStatus.NO_TAG,
];

export function getStatusName(status: UserBookStatus): string {
  switch (status) {
    case UserBookStatus.TO_READ:
      return 'Хочу прочитати';
    case UserBookStatus.READING:
      return 'Читаю зараз';
    case UserBookStatus.READ:
      return 'Прочитано';
    case UserBookStatus.NO_TAG:
      return 'Без тегу';
    case UserBookStatus.STOP:
      return 'Перестав читати';
    case UserBookStatus.FAVORITE:
      return 'Улюблені';
    default:
      throw new Error('Unsupported status: ' + status);
  }
}
