import {Component, Inject} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatInputModule} from '@angular/material/input';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MAT_DIALOG_DATA, MatDialogClose, MatDialogRef} from '@angular/material/dialog';
import {NgStyle} from '@angular/common';
import {CreateCollection} from '../../interfaces/create-collection';

@Component({
  selector: 'app-create-collection-dialog',
  imports: [
    MatInputModule,
    ReactiveFormsModule,
    MatButton,
    MatDialogClose,
    MatIconButton,
    NgStyle,
    FormsModule,
  ],
  templateUrl: './create-collection-dialog.html',
  styleUrl: './create-collection-dialog.scss'
})
export class CreateCollectionDialog {

  collectionForm: FormGroup<{
    name: FormControl<string>
    description: FormControl<string | null>
  }>;

  colors: string[] = ['#4285F4', '#34A853', '#EA4335', '#FBBC04', '#A142F4', '#F44292'];
  selectedColor: string;

  constructor(
    @Inject(MAT_DIALOG_DATA) private collection: CreateCollection | undefined,
    private dialogRef: MatDialogRef<CreateCollectionDialog>,
  ) {
    this.collectionForm = new FormGroup({
      name: new FormControl<string>(this.collection?.name || '', {
        validators: [Validators.required],
        nonNullable: true
      }),
      description: new FormControl<string | null>(this.collection?.description || null),
    });
    this.selectedColor = this.collection?.color || this.colors[0];
  }

  selectColor(color: string): void {
    this.selectedColor = color;
  }

  createCollection(): void {
    const collection: CreateCollection = {
      name: this.collectionForm.getRawValue().name,
      color: this.selectedColor
    };
    if (this.collectionForm.value.description) {
      collection.description = this.collectionForm.value.description;
    }
    this.dialogRef.close(collection);
  }

}
