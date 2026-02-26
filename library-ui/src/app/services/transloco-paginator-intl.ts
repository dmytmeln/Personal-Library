import {inject, Injectable} from '@angular/core';
import {MatPaginatorIntl} from '@angular/material/paginator';
import {TranslocoService} from '@jsverse/transloco';

@Injectable()
export class TranslocoPaginatorIntl extends MatPaginatorIntl {
  private translocoService = inject(TranslocoService);

  override itemsPerPageLabel = '';
  override nextPageLabel = '';
  override previousPageLabel = '';
  override firstPageLabel = '';
  override lastPageLabel = '';

  constructor() {
    super();
    this.translocoService.langChanges$.subscribe(() => {
      this.localizeLabels();
    });
  }

  private localizeLabels(): void {
    this.itemsPerPageLabel = this.translocoService.translate('common.pagination.itemsPerPage');
    this.nextPageLabel = this.translocoService.translate('common.pagination.nextPage');
    this.previousPageLabel = this.translocoService.translate('common.pagination.previousPage');
    this.firstPageLabel = this.translocoService.translate('common.pagination.firstPage');
    this.lastPageLabel = this.translocoService.translate('common.pagination.lastPage');
    this.changes.next();
  }

  override getRangeLabel = (page: number, pageSize: number, length: number): string => {
    if (length === 0 || pageSize === 0) {
      return this.translocoService.translate('common.pagination.range', {
        startIndex: 0,
        endIndex: 0,
        length: 0
      });
    }
    length = Math.max(length, 0);
    const startIndex = page * pageSize;
    const endIndex = startIndex < length ?
      Math.min(startIndex + pageSize, length) :
      startIndex + pageSize;
    return this.translocoService.translate('common.pagination.range', {
      startIndex: startIndex + 1,
      endIndex,
      length
    });
  };
}
