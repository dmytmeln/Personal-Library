import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookExpansionListComponent } from './book-expansion-list.component';

describe('BookExpansionListComponent', () => {
  let component: BookExpansionListComponent;
  let fixture: ComponentFixture<BookExpansionListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookExpansionListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BookExpansionListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
