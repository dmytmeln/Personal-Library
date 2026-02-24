import {Component, effect, input, output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormControl, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';

@Component({
  selector: 'app-autocomplete-filter',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './autocomplete-filter.component.html',
  styleUrl: './autocomplete-filter.component.scss'
})
export class AutocompleteFilterComponent {

  label = input.required<string>();
  placeholder = input<string>('');
  options = input<any[]>([]);
  displayField = input.required<string>();

  searchInput = input<string>('');
  selected = input<any | null>(null);

  searchInputChange = output<string>();
  optionSelected = output<any>();
  clear = output<void>();

  inputControl = new FormControl('');

  constructor() {
    effect(() => {
      const val = this.searchInput();
      if (val === '' && this.inputControl.value !== '') {
        this.inputControl.setValue('', {emitEvent: false});
      }
    });
  }

  onInputChange(val: any): void {
    if (typeof val === 'string') {
      this.searchInputChange.emit(val);
    }
  }

  onSelect(option: any): void {
    this.inputControl.setValue(this.displayFn(option), {emitEvent: false});
    this.optionSelected.emit(option);
  }

  onClear(): void {
    this.inputControl.setValue('', {emitEvent: false});
    this.clear.emit();
  }

  displayFn = (option: any): string => {
    if (option && typeof option === 'object') {
      return option[this.displayField()] || '';
    }
    return option || '';
  }

}
