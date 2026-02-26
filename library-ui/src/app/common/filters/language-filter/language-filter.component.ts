import {Component, input, output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatButtonModule} from '@angular/material/button';
import {LanguageWithCount} from '../../../interfaces/language-with-count';
import {TranslocoDirective, TranslocoPipe} from '@jsverse/transloco';

@Component({
  selector: 'app-language-filter',
  standalone: true,
  imports: [
    CommonModule,
    MatCheckboxModule,
    MatButtonModule,
    TranslocoDirective,
    TranslocoPipe,
  ],
  templateUrl: './language-filter.component.html',
  styleUrl: './language-filter.component.scss'
})
export class LanguageFilterComponent {
  available = input.required<LanguageWithCount[]>();
  selected = input<string[]>([]);
  showAll = input<boolean>(false);

  toggle = output<string>();
  toggleShowAll = output<void>();
  clear = output<void>();

  get displayedLanguages(): LanguageWithCount[] {
    if (this.showAll()) {
      return this.available();
    }
    return this.available().slice(0, 5);
  }

  get hasMore(): boolean {
    return this.available().length > 5;
  }

  isSelected(language: string): boolean {
    return this.selected().includes(language);
  }
}
