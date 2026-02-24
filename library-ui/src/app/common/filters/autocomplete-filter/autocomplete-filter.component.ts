import {Component, input, output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
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
  
  // Two separate inputs for the visual search string and the actual selected object
  searchInput = input<string>('');
  selected = input<any | null>(null);

  searchInputChange = output<string>();
  optionSelected = output<any>();
  clear = output<void>();

  onInputChange(val: string): void {
    this.searchInputChange.emit(val);
  }

  onSelect(option: any): void {
    this.optionSelected.emit(option);
  }

  onClear(): void {
    this.clear.emit();
  }
}
