import {Component, input, output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {Range} from '../../../interfaces/filters';

import {TranslocoDirective} from '@jsverse/transloco';

@Component({
  selector: 'app-range-filter',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    TranslocoDirective,
  ],
  templateUrl: './range-filter.component.html',
  styleUrl: './range-filter.component.scss'
})
export class RangeFilterComponent {
  label = input.required<string>();
  range = input.required<Range<number>>();
  minPlaceholder = input<string>('Any');
  maxPlaceholder = input<string>('Any');
  
  rangeChange = output<Range<number>>();

  onMinChange(min: number | null): void {
    this.rangeChange.emit({...this.range(), min});
  }

  onMaxChange(max: number | null): void {
    this.rangeChange.emit({...this.range(), max});
  }

  clear(): void {
    this.rangeChange.emit({min: null, max: null});
  }

  hasValue(): boolean {
    return this.range().min !== null || this.range().max !== null;
  }
}
