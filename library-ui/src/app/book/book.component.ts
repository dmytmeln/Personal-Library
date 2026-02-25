import {Component, input, output} from '@angular/core';
import {Book} from '../interfaces/book';
import {MatCard} from '@angular/material/card';
import {CommonModule, KeyValuePipe, NgOptimizedImage} from '@angular/common';
import {Router} from '@angular/router';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatMenuModule, MatMenuPanel} from '@angular/material/menu';
import {MatIconModule} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';

@Component({
  selector: 'app-book',
  standalone: true,
  imports: [
    CommonModule,
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

  isSelected = input<boolean>(false);
  selectionMode = input<boolean>(false);
  toggleSelection = output<void>();

  constructor(
    private router: Router,
  ) {
  }

  goToAuthorDetails(authorId: string | number): void {
    this.router.navigate(['/author-details'], {state: {id: Number(authorId)}});
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
