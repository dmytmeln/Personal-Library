import { TestBed } from '@angular/core/testing';

import { CollectionBookService } from './collection-book.service';

describe('CollectionBookService', () => {
  let service: CollectionBookService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CollectionBookService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
