import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateCollectionDialog } from './create-collection-dialog';

describe('CreateCollectionDialog', () => {
  let component: CreateCollectionDialog;
  let fixture: ComponentFixture<CreateCollectionDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateCollectionDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateCollectionDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
