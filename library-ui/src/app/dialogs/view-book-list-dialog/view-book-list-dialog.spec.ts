import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewBookListDialog } from './view-book-list-dialog';

describe('ViewBookListDialogComponent', () => {
  let component: ViewBookListDialog;
  let fixture: ComponentFixture<ViewBookListDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewBookListDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewBookListDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
