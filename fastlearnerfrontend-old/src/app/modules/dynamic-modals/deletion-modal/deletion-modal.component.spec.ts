import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DeletionModalComponent } from './deletion-modal.component';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { NzModalRef } from 'ng-zorro-antd/modal';

describe('DeletionModalComponent', () => {
  let component: DeletionModalComponent;
  let fixture: ComponentFixture<DeletionModalComponent>;
  let modalRefSpy: jasmine.SpyObj<NzModalRef>;

  beforeEach(async () => {
    modalRefSpy = jasmine.createSpyObj('NzModalRef', ['close']);
    await TestBed.configureTestingModule({
      declarations: [DeletionModalComponent],
      providers: [{ provide: NzModalRef, useValue: modalRefSpy }],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(DeletionModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit cancelClick and close the modal on cancel', () => {
    spyOn(component.cancelClick, 'emit');

    component.onCancel();

    expect(component.cancelClick.emit).toHaveBeenCalled();
    expect(modalRefSpy.close).toHaveBeenCalled();
  });

  it('should emit deleteClick and close the modal on delete', () => {
    spyOn(component.deleteClick, 'emit');

    component.onDelete();

    expect(component.deleteClick.emit).toHaveBeenCalled();
    expect(modalRefSpy.close).toHaveBeenCalled();
  });
});
