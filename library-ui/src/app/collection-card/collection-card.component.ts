import {Component, input, OnInit} from '@angular/core';
import {Collection} from '../interfaces/collection';
import {MatCardModule} from '@angular/material/card';
import {MatButton} from '@angular/material/button';
import {NgStyle} from '@angular/common';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-collection-card',
  imports: [
    MatCardModule,
    MatButton,
    NgStyle,
    RouterLink,
  ],
  templateUrl: './collection-card.component.html',
  styleUrl: './collection-card.component.scss'
})
export class CollectionCardComponent implements OnInit {

  collection = input.required<Collection>();
  updatedAt!: Date;
  defaultColor = '';

  ngOnInit(): void {
    this.updatedAt = new Date(this.collection().updatedAt);
  }

}
