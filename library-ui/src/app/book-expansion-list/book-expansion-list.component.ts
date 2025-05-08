import {Component, input, output} from '@angular/core';
import {BookComponent} from '../book/book.component';
import {LibraryBookActionsComponent} from '../library-book-actions/library-book-actions.component';
import {
  MatAccordion,
  MatExpansionPanel,
  MatExpansionPanelHeader,
  MatExpansionPanelTitle
} from '@angular/material/expansion';
import {LibraryBook, LibraryBookStatus} from '../interfaces/library-book';

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
  entries = input<Map<string | LibraryBookStatus, LibraryBook[]>>(new Map());
  deleteBook = output<LibraryBook>();
  ratingChange = output<{ bookId: number, rating: number }>();
  statusChange = output<[LibraryBook, LibraryBookStatus]>();
}
