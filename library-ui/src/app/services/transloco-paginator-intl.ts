import {inject, Injectable} from '@angular/core';
import {MatPaginatorIntl} from '@angular/material/paginator';
import {TranslocoService} from '@jsverse/transloco';
import {combineLatest} from 'rxjs';

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
    this.initializeTranslations();
  }

  private initializeTranslations(): void {
    combineLatest([
      this.translocoService.selectTranslate('common.pagination.itemsPerPage'),
      this.translocoService.selectTranslate('common.pagination.nextPage'),
      this.translocoService.selectTranslate('common.pagination.previousPage'),
      this.translocoService.selectTranslate('common.pagination.firstPage'),
      this.translocoService.selectTranslate('common.pagination.lastPage')
    ]).subscribe(([itemsPerPage, nextPage, previousPage, firstPage, lastPage]) => {
      this.itemsPerPageLabel = itemsPerPage;
      this.nextPageLabel = nextPage;
      this.previousPageLabel = previousPage;
      this.firstPageLabel = firstPage;
      this.lastPageLabel = lastPage;
      this.changes.next();
    });
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
