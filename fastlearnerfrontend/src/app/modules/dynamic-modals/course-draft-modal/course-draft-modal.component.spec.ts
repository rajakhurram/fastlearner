import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CourseDraftModalComponent } from './course-draft-modal.component';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('CourseDraftModalComponent', () => {
  let component: CourseDraftModalComponent;
  let fixture: ComponentFixture<CourseDraftModalComponent>;
  let modalRefSpy: jasmine.SpyObj<NzModalRef>;

  beforeEach(async () => {
    const modalSpy = jasmine.createSpyObj('NzModalRef', ['close']);

    await TestBed.configureTestingModule({
      imports: [AntDesignModule , BrowserAnimationsModule],
      declarations: [CourseDraftModalComponent],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
      providers: [
        { provide: NzModalRef, useValue: modalSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CourseDraftModalComponent);
    component = fixture.componentInstance;
    modalRefSpy = TestBed.inject(NzModalRef) as jasmine.SpyObj<NzModalRef>;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should handle msg input property', () => {
    component.msg = 'Test Message';
    fixture.detectChanges();
    expect(component.msg).toBe('Test Message');
  });

  it('should emit cancelClick and close modal on onCancel', () => {
    spyOn(component.cancelClick, 'emit');

    component.onCancel();

    expect(component.cancelClick.emit).toHaveBeenCalled();
    expect(modalRefSpy.close).toHaveBeenCalled();
  });

  it('should emit okClick and close modal on onOk', () => {
    spyOn(component.okClick, 'emit');

    component.onOk();

    expect(component.okClick.emit).toHaveBeenCalled();
    expect(modalRefSpy.close).toHaveBeenCalled();
  });
});
