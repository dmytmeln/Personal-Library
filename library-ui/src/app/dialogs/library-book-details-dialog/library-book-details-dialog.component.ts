import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {LibraryBook} from '../../interfaces/library-book';
import {UpdateLibraryBookDetails} from '../../interfaces/update-library-book-details';
import {MatTooltip} from '@angular/material/tooltip';

export interface LibraryBookDetailsDialogData {
  libraryBook: LibraryBook;
}

export type LibraryBookDetailsDialogResult =
  | { action: 'save', payload: UpdateLibraryBookDetails }
  | { action: 'reset' }
  | { action: 'cancel' };

@Component({
  selector: 'app-library-book-details-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatTooltip,
  ],
  templateUrl: './library-book-details-dialog.component.html',
  styleUrl: './library-book-details-dialog.component.scss'
})
export class LibraryBookDetailsDialogComponent {
  form: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<LibraryBookDetailsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: LibraryBookDetailsDialogData,
    private fb: FormBuilder
  ) {
    const book = data.libraryBook.book;
    this.form = this.fb.group({
      title: [book.title, Validators.required],
      publishYear: [book.publishYear, [Validators.required, Validators.min(0)]],
      pages: [book.pages, [Validators.required, Validators.min(1)]],
      language: [book.language, Validators.required],
      description: [book.description],
      coverImageUrl: [book.coverImageUrl]
    });
  }

  onSave(): void {
    if (this.form.valid) {
      this.dialogRef.close({
        action: 'save',
        payload: this.form.value as UpdateLibraryBookDetails
      });
    }
  }

  onReset(): void {
    this.dialogRef.close({action: 'reset'});
  }

  onCancel(): void {
    this.dialogRef.close({action: 'cancel'});
  }

}
