import {Component, input} from '@angular/core';
import {BookComponent} from '../book/book.component';
import {
  MatAccordion,
  MatExpansionPanel,
  MatExpansionPanelHeader,
  MatExpansionPanelTitle
} from '@angular/material/expansion';
import {LibraryBook, LibraryBookStatus} from '../interfaces/library-book';
import {MatMenuPanel} from '@angular/material/menu';

@Component({
  selector: 'app-book-expansion-list',
  imports: [
    BookComponent,
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
  actionsMenu = input<MatMenuPanel<any> | null>(null);
}
