import {Component, input, output} from '@angular/core';
import {BookRatingComponent} from '../book-rating/book-rating.component';
import {MatMenuModule} from '@angular/material/menu';
import {getStatusName, USER_BOOK_STATUSES, UserBook, UserBookStatus} from '../interfaces/user-book';
import {MatIcon} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';

@Component({
  selector: 'app-library-book-actions',
  imports: [
    MatIcon,
    MatMenuModule,
    MatIconButton,
    MatSelectModule,
    BookRatingComponent,
  ],
  templateUrl: './library-book-actions.component.html',
  styleUrl: './library-book-actions.component.scss'
})
export class LibraryBookActionsComponent {
  protected readonly USER_BOOK_STATUSES = USER_BOOK_STATUSES;
  protected readonly getStatusName = getStatusName;

  userBook = input.required<UserBook>();
  deleteBook = output<UserBook>();
  ratingChange = output<{ bookId: number, rating: number }>();
  statusChange = output<[UserBook, UserBookStatus]>();
}
