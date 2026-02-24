import {Component, input, output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
  selector: 'app-filter-shell',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
  ],
  templateUrl: './filter-shell.component.html',
  styleUrl: './filter-shell.component.scss'
})
export class FilterShellComponent {
  hasActiveFilters = input<boolean>(false);
  resetAll = output<void>();
}
