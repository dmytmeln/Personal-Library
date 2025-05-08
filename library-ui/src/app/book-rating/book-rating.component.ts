import {Component, input, model} from '@angular/core';
import {MatIconModule} from '@angular/material/icon';
import {NgClass} from '@angular/common';
import {MatIconButton} from '@angular/material/button';

@Component({
  selector: 'app-book-rating',
  imports: [
    MatIconModule,
    NgClass,
    MatIconButton,
  ],
  templateUrl: './book-rating.component.html',
  styleUrl: './book-rating.component.scss'
})
export class BookRatingComponent {
  rating = model<number>(0);
  disabled = input<boolean>(false);
  protected readonly Math = Math;
}
