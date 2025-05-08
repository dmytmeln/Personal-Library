import {Component, input, OnInit} from '@angular/core';
import {Book} from '../interfaces/book';
import {MatCard} from '@angular/material/card';
import {NgOptimizedImage} from '@angular/common';
import {Router} from '@angular/router';

@Component({
  selector: 'app-book',
  imports: [
    MatCard,
    NgOptimizedImage,
  ],
  templateUrl: './book.component.html',
  styleUrl: './book.component.scss'
})
export class BookComponent implements OnInit {

  book = input.required<Book>();
  authors: Array<[number, string]> = [];

  constructor(
    private router: Router,
  ) {
  }

  ngOnInit(): void {
    this.authors = Object.entries(this.book().authors) as {} as Array<[number, string]>;
  }

  goToAuthorDetails(authorId: number): void {
    this.router.navigate(['/author-details'], {state: {id: authorId}}).then(() => {
    });
  }

}
