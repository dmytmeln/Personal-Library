import {Component, computed, inject, input, output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {MatIcon} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';
import {TranslocoService} from '@jsverse/transloco';
import {toSignal} from '@angular/core/rxjs-interop';

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
    MatIcon,
    MatIconButton,
  ],
  templateUrl: './select-filter.component.html',
  styleUrl: './select-filter.component.scss'
})
export class SelectFilterComponent {

  private translocoService = inject(TranslocoService);
  private anyLabel = toSignal(this.translocoService.selectTranslate('common.any'), {initialValue: 'Any'});

  label = input.required<string>();
  value = input<any>(null);
  options = input.required<SelectOption[]>();

  placeholderOverride = input<string | undefined>(undefined, { alias: 'placeholder' });
  placeholder = computed(() => this.placeholderOverride() ?? this.anyLabel());

  clear = output<void>();

  valueChange = output<any>();

  onModelChange(val: any): void {
    this.valueChange.emit(val);
  }

  onClear(): void {
    /*this.value = input(null);
    this.valueChange.emit(null);*/
    this.clear.emit();
  }

}
