import {Component, Inject, OnInit} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {NgStyle} from '@angular/common';
import {CreateCollection} from '../../interfaces/create-collection';
import {CollectionDetails} from '../../interfaces/collection-details';
import {UpdateCollection} from '../../interfaces/update-collection';
import {CollectionService} from '../../services/collection.service';
import {MatIconModule} from '@angular/material/icon';
import {SelectedCollection} from '../../interfaces/selected-collection';
import {CollectionSelectorDialogComponent} from '../collection-selector-dialog/collection-selector-dialog.component';
import {filter} from 'rxjs';

export interface CollectionDialogData {
  isEdit: boolean;
  collection?: CollectionDetails;
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
    MatIconModule,
  ],
  templateUrl: './collection-dialog.component.html',
  styleUrl: './collection-dialog.component.scss'
})
export class CollectionDialogComponent implements OnInit {

  form: FormGroup<{
    name: FormControl<string>;
    description: FormControl<string | null>;
  }>;

  colors: string[] = ['#4285F4', '#34A853', '#EA4335', '#FBBC04', '#A142F4', '#F44292'];
  selectedColor: string;
  isEdit: boolean;

  selectedParent: SelectedCollection = {id: null, name: 'Root'};

  constructor(
    @Inject(MAT_DIALOG_DATA) private data: CollectionDialogData,
    private dialogRef: MatDialogRef<CollectionDialogComponent>,
    private collectionService: CollectionService,
    private dialog: MatDialog,
  ) {
    this.isEdit = data.isEdit;

    this.form = new FormGroup({
      name: new FormControl<string>(data.collection?.name || '', {
        validators: [Validators.required],
        nonNullable: true
      }),
      description: new FormControl<string | null>(data.collection?.description || null),
    });
    this.selectedColor = data.collection?.color || this.colors[0];
  }

  ngOnInit(): void {
    if (!this.isEdit && this.data.parentId) {
      this.collectionService.getById(this.data.parentId).subscribe(c => {
        this.selectedParent = {id: c.id, name: c.name};
      });
    }
  }

  openSelector(): void {
    const dialogRef = this.dialog.open(CollectionSelectorDialogComponent, {
      data: {initialSelectionId: this.selectedParent.id}
    });

    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe((selection: SelectedCollection) => {
      this.selectedParent = selection;
    });
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
        parentId: this.selectedParent.id || undefined,
      };
      this.dialogRef.close(collection);
    }
  }

  close(): void {
    this.dialogRef.close();
  }
}
