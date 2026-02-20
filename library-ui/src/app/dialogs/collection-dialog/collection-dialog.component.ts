import {Component, Inject} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {NgStyle} from '@angular/common';
import {CreateCollection} from '../../interfaces/create-collection';
import {Collection} from '../../interfaces/collection';
import {UpdateCollection} from '../../interfaces/update-collection';

export interface CollectionDialogData {
  isEdit: boolean;
  collection?: Collection;
  parentId?: number;
}

@Component({
  selector: 'app-collection-dialog',
  imports: [
    MatInputModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    NgStyle,
    FormsModule,
  ],
  templateUrl: './collection-dialog.component.html',
  styleUrl: './collection-dialog.component.scss'
})
export class CollectionDialogComponent {

  form: FormGroup<{
    name: FormControl<string>;
    description: FormControl<string | null>;
  }>;

  colors: string[] = ['#4285F4', '#34A853', '#EA4335', '#FBBC04', '#A142F4', '#F44292'];
  selectedColor: string;
  isEdit: boolean;
  parentId?: number;

  constructor(
    @Inject(MAT_DIALOG_DATA) private data: CollectionDialogData,
    private dialogRef: MatDialogRef<CollectionDialogComponent>,
  ) {
    this.isEdit = data.isEdit;
    this.parentId = data.parentId;

    this.form = new FormGroup({
      name: new FormControl<string>(data.collection?.name || '', {
        validators: [Validators.required],
        nonNullable: true
      }),
      description: new FormControl<string | null>(data.collection?.description || null),
    });
    this.selectedColor = data.collection?.color || this.colors[0];
  }

  selectColor(color: string): void {
    this.selectedColor = color;
  }

  save(): void {
    if (this.form.invalid) {
      return;
    }

    if (this.isEdit) {
      const collection: UpdateCollection = {
        name: this.form.getRawValue().name,
        color: this.selectedColor,
        description: this.form.value.description || undefined,
      };
      this.dialogRef.close(collection);
    } else {
      const collection: CreateCollection = {
        name: this.form.getRawValue().name,
        color: this.selectedColor,
        description: this.form.value.description || undefined,
        parentId: this.parentId,
      };
      this.dialogRef.close(collection);
    }
  }

  close(): void {
    this.dialogRef.close();
  }
}
