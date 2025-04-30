import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LibraryBookActionsComponent } from './library-book-actions.component';

describe('LibraryBookActionsComponent', () => {
  let component: LibraryBookActionsComponent;
  let fixture: ComponentFixture<LibraryBookActionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LibraryBookActionsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LibraryBookActionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
