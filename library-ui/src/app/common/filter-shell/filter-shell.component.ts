import {Component, contentChild, Directive, input, model, output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {TranslocoDirective} from '@jsverse/transloco';

@Directive({ selector: '[topRowFilters]', standalone: true }) export class TopRowFiltersDirective {}
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
    TranslocoDirective,
  ],
  templateUrl: './filter-shell.component.html',
  styleUrl: './filter-shell.component.scss'
})
export class FilterShellComponent {
  hasActiveFilters = input<boolean>(false);
  activeFiltersCount = input<number>(0);
  isExpanded = model<boolean>(false);
  resetAll = output<void>();

  protected mainContent = contentChild(MainFiltersDirective);
  protected secondaryContent = contentChild(SecondaryFiltersDirective);
  protected footerContent = contentChild(FooterFiltersDirective);
}
