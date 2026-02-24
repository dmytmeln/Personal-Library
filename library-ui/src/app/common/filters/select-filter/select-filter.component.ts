import {Component, input, output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';

export interface SelectOption {
  value: any;
  label: string;
}

@Component({
  selector: 'app-select-filter',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
  ],
  templateUrl: './select-filter.component.html',
  styleUrl: './select-filter.component.scss'
})
export class SelectFilterComponent {
  label = input.required<string>();
  value = input<any>(null);
  options = input.required<SelectOption[]>();
  placeholder = input<string>('Будь-який');
  
  valueChange = output<any>();

  onModelChange(val: any): void {
    this.valueChange.emit(val);
  }
}
