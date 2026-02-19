import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';
import {Collection} from '../../interfaces/collection';
import {NgStyle} from '@angular/common';

@Component({
  selector: 'app-delete-library-book-dialog',
  imports: [
    MatButton,
    MatDialogModule,
    NgStyle,
  ],
  templateUrl: './delete-library-book-dialog.html',
  styleUrl: './delete-library-book-dialog.scss'
})
export class DeleteLibraryBookDialog {

  constructor(
    @Inject(MAT_DIALOG_DATA) readonly data: DeleteLibraryBookDialogData,
    private dialogRef: MatDialogRef<DeleteLibraryBookDialog>,
  ) {
  }

  confirmDelete(): void {
    this.dialogRef.close(true);
  }

  cancelDelete(): void {
    this.dialogRef.close(false);
  }

}

export interface DeleteLibraryBookDialogData {
  bookTitle: string;
  collections: Collection[];
}
