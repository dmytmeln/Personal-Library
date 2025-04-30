import {Component, input, output} from '@angular/core';
import {BookComponent} from '../book/book.component';
import {LibraryBookActionsComponent} from '../library-book-actions/library-book-actions.component';
import {
  MatAccordion,
  MatExpansionPanel,
  MatExpansionPanelHeader,
  MatExpansionPanelTitle
} from '@angular/material/expansion';
import {UserBook, UserBookStatus} from '../interfaces/user-book';

@Component({
  selector: 'app-book-expansion-list',
  imports: [
    BookComponent,
    LibraryBookActionsComponent,
    MatAccordion,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle
  ],
  templateUrl: './book-expansion-list.component.html',
  styleUrl: './book-expansion-list.component.scss'
})
export class BookExpansionListComponent {
  getTitle = input<(key: any) => string>((key) => String(key));
  entries = input<Map<string | UserBookStatus, UserBook[]>>(new Map());
  deleteBook = output<UserBook>();
  ratingChange = output<{ bookId: number, rating: number }>();
  statusChange = output<[UserBook, UserBookStatus]>();
}
