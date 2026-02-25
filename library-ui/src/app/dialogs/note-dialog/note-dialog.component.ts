import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {NoteService} from '../../services/note.service';
import {Note} from '../../interfaces/note';
import {CommonModule} from '@angular/common';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

export interface NoteDialogData {
  libraryBookId: number;
  bookTitle: string;
}

@Component({
  selector: 'app-note-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './note-dialog.component.html',
  styleUrl: './note-dialog.component.scss'
})
export class NoteDialogComponent implements OnInit {
  noteControl = new FormControl('');
  note: Note | null = null;
  loading = false;
  saving = false;

  constructor(
    public dialogRef: MatDialogRef<NoteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: NoteDialogData,
    private noteService: NoteService,
  ) {
  }

  ngOnInit(): void {
    this.loadNote();
  }

  private loadNote(): void {
    this.loading = true;
    this.noteService.getByLibraryBookId(this.data.libraryBookId).subscribe({
      next: (note) => {
        this.note = note;
        this.noteControl.setValue(note.content);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  onSave(): void {
    const content = this.noteControl.value?.trim() || '';
    
    if (content === '' && !this.note) {
      this.dialogRef.close();
      return;
    }

    this.saving = true;
    if (content === '') {
      this.noteService.delete(this.data.libraryBookId).subscribe({
        next: () => this.dialogRef.close('deleted'),
        error: () => this.saving = false
      });
    } else {
      this.noteService.upsert({
        libraryBookId: this.data.libraryBookId,
        content: content
      }).subscribe({
        next: () => this.dialogRef.close('saved'),
        error: () => this.saving = false
      });
    }
  }

  onClear(): void {
    this.noteControl.setValue('');
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
