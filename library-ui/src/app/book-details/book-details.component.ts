import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {Book} from '../interfaces/book';
import {NgOptimizedImage} from '@angular/common';
import {BookRatingComponent} from '../book-rating/book-rating.component';
import {BookDetails} from '../interfaces/book-details';
import {BookService} from '../services/book.service';
import {getStatusName} from '../interfaces/library-book';
import {MatAnchor, MatButton} from '@angular/material/button';
import {LibraryBookService} from '../services/library-book.service';
import {BasicCollection} from '../interfaces/basic-collection';

@Component({
  selector: 'app-book-details',
  imports: [
    NgOptimizedImage,
    BookRatingComponent,
    MatButton,
    MatAnchor,
  ],
  templateUrl: './book-details.component.html',
  styleUrl: './book-details.component.scss'
})
export class BookDetailsComponent implements OnInit {

  protected readonly getStatusName = getStatusName;

  book: Book;
  authors: Array<[number, string]> = [];
  bookDetails!: BookDetails;

  constructor(
    private router: Router,
    private bookService: BookService,
    private libraryBookService: LibraryBookService,
  ) {
    this.book = this.router.getCurrentNavigation()?.extras?.state as Book;
  }

  ngOnInit(): void {
    this.authors = Object.entries(this.book.authors) as {} as Array<[number, string]>;
    this.bookService.getBookDetails(this.book.id).subscribe(bookDetails => {
      this.bookDetails = bookDetails;
    });
  }

  addBookToLibrary(): void {
    this.libraryBookService.addBook(this.book.id).subscribe(libraryBook => {
      this.book = libraryBook.book;
      this.bookDetails.isInLibrary = true;
    });
  }

  goToCollection(collection: BasicCollection): void {
    this.router.navigate(['/collections', collection.id]);
  }

  goToAuthorDetails(id: number): void {
    this.router.navigate(['/author-details'], {state: {id}}).then(() => {
    });
  }

  changeRating(rating: number): void {
    this.libraryBookService.changeRating(this.book.id, rating).subscribe(libraryBook => {
      this.bookDetails.myRating = libraryBook.rating || 0;
    });
  }

}
