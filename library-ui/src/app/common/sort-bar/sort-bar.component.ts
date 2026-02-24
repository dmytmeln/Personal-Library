import {Component, input, output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ActiveSort, SortOption} from '../../interfaces/sort-config';

@Component({
  selector: 'app-sort-bar',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
  templateUrl: './sort-bar.component.html',
  styleUrl: './sort-bar.component.scss'
})
export class SortBarComponent {

  private readonly DEFAULT_DIRECTION: 'asc' | 'desc' = 'asc';

  options = input.required<SortOption[]>();
  sortChange = output<string[] | undefined>();

  private _activeSorts: ActiveSort[] = [];

  onSortClick(option: SortOption): void {
    const existingIndex = this._activeSorts.findIndex(s => s.field === option.field);

    if (existingIndex === -1) {
      this._activeSorts.push({field: option.field, direction: this.DEFAULT_DIRECTION});
    } else {
      this.toggleSortDirection(existingIndex);
    }

    this.emitSortChange();
  }

  isIncludedInSorting(option: SortOption): boolean {
    return this._activeSorts.some(s => s.field === option.field);
  }

  getSortDirection(option: SortOption): 'asc' | 'desc' | undefined {
    const sort = this._activeSorts.find(s => s.field === option.field);
    return sort?.direction;
  }

  getSortPriority(option: SortOption): number {
    return this._activeSorts.findIndex(s => s.field === option.field) + 1;
  }

  hasActiveSorts(): boolean {
    return this._activeSorts.length > 0;
  }

  clearAllSorts(): void {
    this._activeSorts = [];
    this.emitSortChange();
  }

  private toggleSortDirection(existingIndex: number): void {
    const existing = this._activeSorts[existingIndex];
    if (existing.direction === 'asc') {
      existing.direction = 'desc';
    } else {
      this._activeSorts.splice(existingIndex, 1);
    }
  }

  private emitSortChange(): void {
    if (this._activeSorts.length === 0) {
      this.sortChange.emit(undefined);
    } else {
      const sortParams = this._activeSorts.map(s => `${s.field};${s.direction}`);
      this.sortChange.emit(sortParams);
    }
  }

}
