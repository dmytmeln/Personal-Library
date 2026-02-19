import {Component, output, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatMenuModule, MatMenuPanel} from '@angular/material/menu';
import {MatIconModule} from '@angular/material/icon';
import {BookRatingComponent} from '../book-rating/book-rating.component';
import {getStatusName, LIBRARY_BOOK_STATUSES, LibraryBook, LibraryBookStatus} from '../interfaces/library-book';
import {Router} from '@angular/router';
import {Book} from '../interfaces/book';

@Component({
  selector: 'app-library-book-menu-items',
  standalone: true,
  imports: [
    CommonModule,
    MatMenuModule,
    MatIconModule,
    BookRatingComponent,
  ],
  templateUrl: './library-book-menu-items.component.html',
  styleUrl: './library-book-menu-items.component.scss'
})
export class LibraryBookMenuItemsComponent {

  protected readonly LIBRARY_BOOK_STATUSES = LIBRARY_BOOK_STATUSES;
  protected readonly getStatusName = getStatusName;

  @ViewChild('rootMenu', {static: true}) menu!: MatMenuPanel<any>;

  deleteBook = output<LibraryBook>();
  statusChange = output<[LibraryBook, LibraryBookStatus]>();
  ratingChange = output<{ libraryBookId: number; rating: number }>();

  constructor(
    private router: Router,
  ) {
  }

  goToBookDetails(book: Book): void {
    this.router.navigate(['/book-details'], {state: book});
  }

}
