import {Component, input, output} from '@angular/core';
import {Book} from '../interfaces/book';
import {CommonModule, KeyValuePipe, NgOptimizedImage} from '@angular/common';
import {Router} from '@angular/router';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatMenuModule, MatMenuPanel} from '@angular/material/menu';
import {MatIconModule} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';

@Component({
  selector: 'app-book-list-item',
  standalone: true,
  imports: [
    CommonModule,
    NgOptimizedImage,
    KeyValuePipe,
    MatTooltipModule,
    MatMenuModule,
    MatIconModule,
    MatIconButton,
  ],
  templateUrl: './book-list-item.component.html',
  styleUrl: './book-list-item.component.scss'
})
export class BookListItemComponent {
  book = input.required<Book>();
  menuData = input<any>(null);
  actionsMenu = input<MatMenuPanel | null>(null);

  isSelected = input<boolean>(false);
  selectionMode = input<boolean>(false);
  toggleSelection = output<void>();

  constructor(private router: Router) {}

  goToAuthorDetails(authorId: string | number): void {
    this.router.navigate(['/author-details'], {state: {id: Number(authorId)}});
  }
}
