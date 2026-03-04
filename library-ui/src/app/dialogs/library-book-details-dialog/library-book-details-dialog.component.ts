import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {LibraryBook} from '../../interfaces/library-book';
import {UpdateLibraryBookDetails} from '../../interfaces/update-library-book-details';
import {MatTooltip} from '@angular/material/tooltip';
import {ConfirmationDialogComponent} from '../confirmation-dialog/confirmation-dialog.component';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

export interface LibraryBookDetailsDialogData {
  libraryBook: LibraryBook;
}

export type LibraryBookDetailsDialogResult =
  | { action: 'save', payload: Partial<UpdateLibraryBookDetails> }
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
    TranslocoDirective,
  ],
  templateUrl: './library-book-details-dialog.component.html',
  styleUrl: './library-book-details-dialog.component.scss'
})
export class LibraryBookDetailsDialogComponent {
  form: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<LibraryBookDetailsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: LibraryBookDetailsDialogData,
    private fb: FormBuilder,
    private dialog: MatDialog,
    private translocoService: TranslocoService,
  ) {
    const book = data.libraryBook.book;
    this.form = this.fb.group({
      title: [book.title, Validators.required],
      publishYear: [book.publishYear, [Validators.required, Validators.min(0)]],
      pages: [book.pages, [Validators.required, Validators.min(1)]],
      language: [book.language, Validators.required],
      description: [book.description]
    });
  }

  onSave(): void {
    if (!this.form.valid)
      return;

    const changes = this.getChangedValues();
    if (Object.keys(changes).length > 0) {
      this.dialogRef.close({action: 'save', payload: changes});
      return;
    }

    const confRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        message: this.translocoService.translate('dialogs.bookDetails.noChangesMessage')
      }
    });

    confRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.dialogRef.close({action: 'cancel'});
      }
    });
  }

  private getChangedValues(): Partial<UpdateLibraryBookDetails> {
    const book = this.data.libraryBook.book;
    const changes: Partial<UpdateLibraryBookDetails> = {};
    const controls = this.form.controls;

    Object.keys(controls).forEach(key => {
      const currentValue = controls[key].value;
      const originalValue = book[key as keyof typeof book];

      const normalizedCurrent = currentValue === '' ? null : currentValue;
      const normalizedOriginal = originalValue === '' ? null : originalValue;

      if (normalizedCurrent !== normalizedOriginal) {
        changes[key as keyof UpdateLibraryBookDetails] = normalizedCurrent;
      }
    });

    return changes;
  }

  onReset(): void {
    this.dialogRef.close({action: 'reset'});
  }

  onCancel(): void {
    this.dialogRef.close({action: 'cancel'});
  }

}
