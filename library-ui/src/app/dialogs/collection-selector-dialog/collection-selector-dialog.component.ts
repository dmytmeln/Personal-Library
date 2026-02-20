import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {CollectionSelectorComponent} from '../../common/collection-selector/collection-selector.component';
import {SelectedCollection} from '../../interfaces/selected-collection';

export interface CollectionSelectorDialogData {
  initialSelectionId: number | null;
}

@Component({
  selector: 'app-collection-selector-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    CollectionSelectorComponent
  ],
  templateUrl: './collection-selector-dialog.component.html',
  styleUrl: './collection-selector-dialog.component.scss'
})
export class CollectionSelectorDialogComponent {
  tempSelection: SelectedCollection = { id: null, name: 'Root' };

  constructor(
    public dialogRef: MatDialogRef<CollectionSelectorDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: CollectionSelectorDialogData
  ) {}

  onSelect(selection: SelectedCollection): void {
    this.tempSelection = selection;
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
