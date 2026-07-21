import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { ClassUploaderComponent } from './class-uploader.component';
import { NzModalRef } from 'ng-zorro-antd/modal';

describe('ClassUploaderComponent', () => {
  let component: ClassUploaderComponent;
  let fixture: ComponentFixture<ClassUploaderComponent>;
  let mockModalRef: jasmine.SpyObj<NzModalRef>;
  let mockAiGraderService: jasmine.SpyObj<AiGraderService>;

  beforeEach(async () => {
    mockModalRef = jasmine.createSpyObj<NzModalRef>('NzModalRef', ['destroy']);
    mockAiGraderService = jasmine.createSpyObj<AiGraderService>(
      'AiGraderService',
      ['sendEmail'],
    );
    mockAiGraderService.sendEmail.and.returnValue(of({ status: 200 }));

    await TestBed.configureTestingModule({
      declarations: [ClassUploaderComponent],
      providers: [
        { provide: NzModalRef, useValue: mockModalRef },
        { provide: AiGraderService, useValue: mockAiGraderService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ClassUploaderComponent);
    component = fixture.componentInstance;
    component.studentEmail = 'student@school.com';
    component.aiResultId = 42;
    component.studentScore = 85;
    component.score = 100;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should validate email addresses', () => {
    expect(component.isEmailValid('valid@email.com')).toBeTrue();
    expect(component.isEmailValid('invalid-email')).toBeFalse();
    expect(component.isEmailValid('')).toBeFalse();
  });

  it('should format integer and decimal scores', () => {
    expect(component.formatScore(85)).toBe('85');
    expect(component.formatScore(85.5)).toBe('85.5');
    expect(component.formatScore(null)).toBe('-');
  });

  it('should send email when address is valid', () => {
    component.onSend();

    expect(mockAiGraderService.sendEmail).toHaveBeenCalledWith(
      'student@school.com',
      42,
    );
    expect(component.success).toBeTrue();
  });

  it('should not send email when address is invalid', () => {
    component.studentEmail = 'not-an-email';

    component.onSend();

    expect(mockAiGraderService.sendEmail).not.toHaveBeenCalled();
    expect(component.success).toBeFalse();
  });

  it('should keep success false when send email fails', () => {
    mockAiGraderService.sendEmail.and.returnValue(
      throwError(() => new Error('send failed')),
    );

    component.onSend();

    expect(component.success).toBeFalse();
  });

  it('should close modal on cancel', () => {
    component.onCancel();

    expect(mockModalRef.destroy).toHaveBeenCalled();
  });
});
