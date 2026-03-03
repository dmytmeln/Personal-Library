import { Component, Inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { ReactiveFormsModule } from '@angular/forms';
import { AuthorService } from '../../services/author.service';
import { Author } from '../../interfaces/author';
import { finalize } from 'rxjs';
import { TranslocoDirective } from '@jsverse/transloco';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AutocompleteSearchStore } from '../../services/autocomplete-search.store';

@Component({
  selector: 'app-author-selection-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatListModule,
    MatIconModule,
    MatChipsModule,
    ReactiveFormsModule,
    TranslocoDirective,
    MatProgressSpinnerModule,
  ],
  templateUrl: './author-selection-dialog.component.html',
  styleUrl: './author-selection-dialog.component.scss'
})
export class AuthorSelectionDialogComponent implements OnInit {

  readonly authorSearch: AutocompleteSearchStore<Author>;
  selectedAuthors = signal<Author[]>([]);
  loading = signal(false);

  constructor(
    private authorService: AuthorService,
    private dialogRef: MatDialogRef<AuthorSelectionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) private data: {selectedAuthors: Author[]}
  ) {
    this.authorSearch = new AutocompleteSearchStore<Author>(
      (query: string, page: number, size: number) => {
        this.loading.set(true);
        return this.authorService.search({ name: query, page, size }).pipe(
          finalize(() => this.loading.set(false))
        );
      }
    );
  }

  ngOnInit(): void {
    this.selectedAuthors.set([...this.data.selectedAuthors]);
    this.authorSearch.search('');
  }

  onSearch(query: string): void {
    this.authorSearch.search(query);
  }

  addAuthor(author: Author): void {
    if (!this.selectedAuthors().some(a => a.id === author.id)) {
      this.selectedAuthors.update(list => [...list, author]);
    }
  }

  removeAuthor(authorId: number): void {
    this.selectedAuthors.update(list => list.filter(a => a.id !== authorId));
  }

  confirm(): void {
    this.dialogRef.close(this.selectedAuthors());
  }

}
