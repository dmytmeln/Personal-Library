import {Component, input} from '@angular/core';
import {Book} from '../interfaces/book';
import {MatCard} from '@angular/material/card';
import {KeyValuePipe, NgOptimizedImage} from '@angular/common';
import {Router} from '@angular/router';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatMenuModule, MatMenuPanel} from '@angular/material/menu';
import {MatIconModule} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';
import {LibraryBook} from '../interfaces/library-book';

@Component({
  selector: 'app-book',
  imports: [
    MatCard,
    NgOptimizedImage,
    KeyValuePipe,
    MatTooltipModule,
    MatMenuModule,
    MatIconModule,
    MatIconButton,
  ],
  templateUrl: './book.component.html',
  styleUrl: './book.component.scss'
})
export class BookComponent {

  private readonly TITLE_MAX_LENGTH = 25;

  book = input.required<Book>();
  menuData = input<any>(null);
  actionsMenu = input<MatMenuPanel | null>(null);

  constructor(
    private router: Router,
  ) {
  }

  goToAuthorDetails(authorId: string | number): void {
    this.router.navigate(['/author-details'], {state: {id: Number(authorId)}});
  }

  goToBookDetails(): void {
    this.router.navigate(['/book-details'], {state: this.book()});
  }

  truncateTitle(title: string): string {
    if (title.length <= this.TITLE_MAX_LENGTH) {
      return title;
    }
    return title.substring(0, this.TITLE_MAX_LENGTH) + '...';
  }

  shouldShowTooltip(title: string): boolean {
    return title.length > this.TITLE_MAX_LENGTH;
  }

}
