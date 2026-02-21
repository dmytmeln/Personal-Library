import {Component, input, OnInit} from '@angular/core';
import {BasicCollection} from '../interfaces/basic-collection';
import {MatCardModule} from '@angular/material/card';
import {MatButton} from '@angular/material/button';
import {NgStyle} from '@angular/common';
import {Router} from '@angular/router';

@Component({
  selector: 'app-collection-card',
  imports: [
    MatCardModule,
    MatButton,
    NgStyle,
  ],
  templateUrl: './collection-card.component.html',
  styleUrl: './collection-card.component.scss'
})
export class CollectionCardComponent implements OnInit {

  collection = input.required<BasicCollection>();
  updatedAt!: string;
  defaultColor = '';

  constructor(
    private router: Router,
  ) {
  }

  ngOnInit(): void {
    if (this.collection().updatedAt) {
      this.updatedAt = new Date(this.collection().updatedAt!).toLocaleString('uk', {
        month: 'long',
        day: 'numeric',
        year: 'numeric'
      });
    }
  }

  goToFullData(): void {
    this.router.navigate(['collections', this.collection().id]);
  }

}
