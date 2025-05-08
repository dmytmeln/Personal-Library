import { TestBed } from '@angular/core/testing';

import { LibraryBookService } from './library-book.service';

describe('LibraryBookService', () => {
  let service: LibraryBookService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LibraryBookService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
