import {Component, input, output} from '@angular/core';
import {Book} from '../interfaces/book';
import {MatCard} from '@angular/material/card';
import {CommonModule, KeyValuePipe, NgOptimizedImage} from '@angular/common';
import {Router} from '@angular/router';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatMenuModule, MatMenuPanel} from '@angular/material/menu';
import {MatIconModule} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';
import {TranslocoDirective} from '@jsverse/transloco';

@Component({
  selector: 'app-book-card',
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
    TranslocoDirective,
  ],
  templateUrl: './book-card.component.html',
  styleUrl: './book-card.component.scss'
})
export class BookCardComponent {

  private readonly TITLE_MAX_LENGTH = 25;

  book = input.required<Book>();
  menuData = input<any>(null);
  actionsMenu = input<MatMenuPanel | null>(null);

  isSelected = input<boolean>(false);
  selectionMode = input<boolean>(false);
  adminMode = input<boolean>(false);
  toggleSelection = output<void>();

  constructor(
    private router: Router,
  ) {
  }

  goToAuthorDetails(authorId: string | number): void {
    this.router.navigate(['/author-details'], {state: {id: Number(authorId)}});
  }

  goToCategoryDetails(): void {
    this.router.navigate(['/category-details'], {state: {id: this.book().categoryId}});
  }

  goToBookDetails(): void {
    this.router.navigate(['/book-details', this.book().id]);
  }

  goToAdminDetails(): void {
    this.router.navigate(['/admin/book', this.book().id]);
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
