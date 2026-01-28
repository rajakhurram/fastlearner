import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubmissionModalComponent } from './submission-modal.component';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';

describe('SubmissionModalComponent', () => {
  let component: SubmissionModalComponent;
  let fixture: ComponentFixture<SubmissionModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SubmissionModalComponent],
      providers: [NzModalService],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(SubmissionModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
