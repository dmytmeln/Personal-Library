import {Component, input, output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';

@Component({
  selector: 'app-text-filter',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './text-filter.component.html',
  styleUrl: './text-filter.component.scss'
})
export class TextFilterComponent {
  label = input.required<string>();
  placeholder = input<string>('');
  value = input<string>('');
  valueChange = output<string>();

  onModelChange(val: string): void {
    this.valueChange.emit(val);
  }

  clear(): void {
    this.valueChange.emit('');
  }
}
