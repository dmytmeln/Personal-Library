import {Component, contentChild, Directive, input, output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';

@Directive({ selector: '[mainFilters]', standalone: true }) export class MainFiltersDirective {}
@Directive({ selector: '[secondaryFilters]', standalone: true }) export class SecondaryFiltersDirective {}
@Directive({ selector: '[footerFilters]', standalone: true }) export class FooterFiltersDirective {}

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

  protected secondaryContent = contentChild(SecondaryFiltersDirective);
  protected footerContent = contentChild(FooterFiltersDirective);
}
