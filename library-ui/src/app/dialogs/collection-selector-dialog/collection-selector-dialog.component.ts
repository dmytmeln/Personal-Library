import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {CollectionSelectorComponent} from '../../common/collection-selector/collection-selector.component';
import {SelectedCollection} from '../../interfaces/selected-collection';

export interface CollectionSelectorDialogData {
  initialSelectionId: number | null;
  disabledIds?: number[];
  showRoot?: boolean;
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

  isSelectionValid(): boolean {
    if (this.tempSelection.id === null) {
      return this.data.showRoot !== false;
    }
    return !this.data.disabledIds?.includes(this.tempSelection.id);
  }

  onCancel(): void {
    this.dialogRef.close();
  }

}
