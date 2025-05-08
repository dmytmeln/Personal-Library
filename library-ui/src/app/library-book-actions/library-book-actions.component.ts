import {Component, input, output} from '@angular/core';
import {BookRatingComponent} from '../book-rating/book-rating.component';
import {MatMenuModule} from '@angular/material/menu';
import {getStatusName, LIBRARY_BOOK_STATUSES, LibraryBook, LibraryBookStatus} from '../interfaces/library-book';
import {MatIcon} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {Router} from '@angular/router';

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
  protected readonly USER_BOOK_STATUSES = LIBRARY_BOOK_STATUSES;
  protected readonly getStatusName = getStatusName;

  constructor(
    private router: Router,
  ) {
  }

  libraryBook = input.required<LibraryBook>();
  deleteBook = output<LibraryBook>();
  ratingChange = output<{ bookId: number, rating: number }>();
  statusChange = output<[LibraryBook, LibraryBookStatus]>();

  goToBookDetails() {
    this.router.navigate(['book-details'], {state: this.libraryBook().book}).then(() => {
    });
  }

}
