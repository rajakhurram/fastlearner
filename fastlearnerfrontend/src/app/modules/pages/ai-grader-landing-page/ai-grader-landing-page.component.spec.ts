import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError, Observable } from 'rxjs';

import { AiGraderLandingPageComponent } from './ai-grader-landing-page.component';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SharedModule } from '../../shared/shared.module';

describe('AiGraderLandingPageComponent - graderForm Tests', () => {
  let component: AiGraderLandingPageComponent;
  let fixture: ComponentFixture<AiGraderLandingPageComponent>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockCacheService: jasmine.SpyObj<CacheService>;
  let mockMessageService: jasmine.SpyObj<MessageService>;
  let mockAiGraderService: jasmine.SpyObj<AiGraderService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['isLoggedIn']);
    mockCacheService = jasmine.createSpyObj('CacheService', [
      'getJsonData',
      'saveJsonData',
      'saveInCache',
      'removeFromCache'
    ]);
    mockMessageService = jasmine.createSpyObj('MessageService', ['error', 'success']);
    mockAiGraderService = jasmine.createSpyObj('AiGraderService', ['startGradingLandingPage']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [AiGraderLandingPageComponent],
      imports: [SharedModule, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: CacheService, useValue: mockCacheService },
        { provide: MessageService, useValue: mockMessageService },
        { provide: AiGraderService, useValue: mockAiGraderService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AiGraderLandingPageComponent);
    component = fixture.componentInstance;
    mockAuthService.isLoggedIn.and.returnValue(false);
    mockCacheService.getJsonData.and.returnValue(null);
    fixture.detectChanges();
  });

  describe('graderForm Initialization', () => {
    it('should create the form with all required controls', () => {
      expect(component.graderForm).toBeDefined();
      expect(component.graderForm.get('className')).toBeDefined();
      expect(component.graderForm.get('assessmentName')).toBeDefined();
      expect(component.graderForm.get('evaluationCriteria')).toBeDefined();
      expect(component.graderForm.get('userSubmittedAnswerAsText')).toBeDefined();
    });

    it('should have required validators on className', () => {
      const control = component.graderForm.get('className');
      control?.setValue('');
      expect(control?.hasError('required')).toBe(true);
      control?.setValue('Math Class');
      expect(control?.hasError('required')).toBe(false);
    });

    it('should have required validators on assessmentName', () => {
      const control = component.graderForm.get('assessmentName');
      control?.setValue('');
      expect(control?.hasError('required')).toBe(true);
      control?.setValue('Final Exam');
      expect(control?.hasError('required')).toBe(false);
    });

    it('should have required validators on evaluationCriteria', () => {
      const control = component.graderForm.get('evaluationCriteria');
      control?.setValue('');
      expect(control?.hasError('required')).toBe(true);
      control?.setValue('Grade based on accuracy');
      expect(control?.hasError('required')).toBe(false);
    });

    it('should have maxLength validator on userSubmittedAnswerAsText', () => {
      const control = component.graderForm.get('userSubmittedAnswerAsText');
      const longText = 'a'.repeat(12001);
      control?.setValue(longText);
      expect(control?.hasError('maxlength')).toBe(true);
      control?.setValue('Valid answer text');
      expect(control?.hasError('maxlength')).toBe(false);
    });
  });

  describe('Form Validation', () => {
    it('should be invalid when required fields are empty', () => {
      expect(component.graderForm.valid).toBe(false);
    });

    it('should be valid when all required fields are filled', () => {
      component.graderForm.patchValue({
        className: 'Physics 101',
        assessmentName: 'Midterm Exam',
        evaluationCriteria: 'Check for accuracy and completeness'
      });
      expect(component.graderForm.valid).toBe(true);
    });

    it('should mark all fields as touched when markAllFieldsAsTouched is called', () => {
      component.markAllFieldsAsTouched();
      expect(component.graderForm.get('className')?.touched).toBe(true);
      expect(component.graderForm.get('assessmentName')?.touched).toBe(true);
      expect(component.graderForm.get('evaluationCriteria')?.touched).toBe(true);
    });
  });

  describe('File Upload - Student Copies', () => {
    it('should upload PDF files successfully', () => {
      const mockFile = new File(['test'], 'test.pdf', { type: 'application/pdf' });
      const event = { target: { files: [mockFile] } };

      component.onFileUpload(event);

      expect(component.uploadedFiles.length).toBe(1);
      expect(component.uploadedFiles[0].name).toBe('test.pdf');
      expect(component.uploadedFiles[0].type).toBe('application/pdf');
    });

    it('should reject non-PDF files', () => {
      const mockFile = new File(['test'], 'test.txt', { type: 'text/plain' });
      const event = { target: { files: [mockFile] } };

      component.onFileUpload(event);

      expect(mockMessageService.error).toHaveBeenCalledWith('test.txt is not a PDF file.');
      expect(component.uploadedFiles.length).toBe(0);
    });

    it('should limit uploads to 40 files', () => {
      const mockFiles = Array.from({ length: 41 }, (_, i) =>
        new File(['test'], `test${i}.pdf`, { type: 'application/pdf' })
      );
      const event = { target: { files: mockFiles } };

      component.onFileUpload(event);

      expect(mockMessageService.error).toHaveBeenCalledWith('You can upload a maximum of 40 files.');
      expect(component.uploadedFiles.length).toBe(0);
    });

    it('should not upload duplicate files', () => {
      const mockFile = new File(['test'], 'test.pdf', { type: 'application/pdf' });
      const event = { target: { files: [mockFile] } };

      component.onFileUpload(event);
      component.onFileUpload(event);

      expect(component.uploadedFiles.length).toBe(1);
    });

    it('should reset uploaded files when resetUpload is called', () => {
      const mockFile = new File(['test'], 'test.pdf', { type: 'application/pdf' });
      component.uploadedFiles = [{ file: mockFile, name: 'test.pdf' }];

      component.resetUpload();

      expect(component.uploadedFiles.length).toBe(0);
    });
  });

  describe('Answer Key File Upload', () => {
    it('should select answer key PDF file', () => {
      const mockFile = new File(['answer'], 'answer.pdf', { type: 'application/pdf' });
      const event = { target: { files: [mockFile] } };

      component.onAnswerFileSelected(event);

      expect(component.selectedFileName).toBe('answer.pdf');
      expect(component.selectedAnswerFile).toBeDefined();
      expect(component.selectedAnswerFile.name).toBe('answer.pdf');
    });

    it('should clear evaluationCriteria validator when answer file is uploaded', () => {
      const mockFile = new File(['answer'], 'answer.pdf', { type: 'application/pdf' });
      const event = { target: { files: [mockFile] } };

      component.onAnswerFileSelected(event);

      const control = component.graderForm.get('evaluationCriteria');
      control?.setValue('');
      expect(control?.hasError('required')).toBe(false);
    });

    it('should reject non-PDF answer files', () => {
      spyOn(window, 'alert');
      const mockFile = new File(['answer'], 'answer.txt', { type: 'text/plain' });
      const event = { target: { files: [mockFile] } };

      component.onAnswerFileSelected(event);

      expect(window.alert).toHaveBeenCalledWith('Only PDF files are allowed.');
      expect(component.selectedFileName).toBe('No file chosen');
      expect(component.selectedAnswerFile).toBeNull();
    });

    it('should restore evaluationCriteria validator when non-PDF is selected', () => {
      spyOn(window, 'alert');
      const mockFile = new File(['answer'], 'answer.txt', { type: 'text/plain' });
      const event = { target: { files: [mockFile] } };

      component.onAnswerFileSelected(event);

      const control = component.graderForm.get('evaluationCriteria');
      control?.setValue('');
      expect(control?.hasError('required')).toBe(true);
    });
  });

  describe('Form Submission - Logged In User', () => {
    beforeEach(() => {
      component.userLoggedIn = true;
      mockAuthService.isLoggedIn.and.returnValue(true);
      component.graderForm.patchValue({
        className: 'Science 101',
        assessmentName: 'Quiz 1',
        evaluationCriteria: 'Grade on completeness'
      });
    });

    it('should submit form successfully with user answer text', (done) => {
      const mockResponse = {
        data: { assessmentId: '123', classId: '456' }
      };
      mockAiGraderService.startGradingLandingPage.and.returnValue(of(mockResponse));

      component.graderForm.patchValue({
        userSubmittedAnswerAsText: 'This is my answer text'
      });

      component.gradeNow();

      setTimeout(() => {
        expect(mockAiGraderService.startGradingLandingPage).toHaveBeenCalled();
        expect(mockRouter.navigate).toHaveBeenCalledWith(
          ['instructor/ai-grader/results'],
          { queryParams: { id: '123', classId: '456' } }
        );
        expect(component.isProcessing).toBe(false);
        done();
      }, 100);
    });

    it('should submit form successfully with uploaded files', (done) => {
      const mockResponse = {
        data: { assessmentId: '789', classId: '012' }
      };
      mockAiGraderService.startGradingLandingPage.and.returnValue(of(mockResponse));

      const mockFile = new File(['test'], 'student1.pdf', { type: 'application/pdf' });
      component.uploadedFiles = [{ file: mockFile, name: 'student1.pdf', type: 'application/pdf' }];

      component.gradeNow();

      setTimeout(() => {
        expect(mockAiGraderService.startGradingLandingPage).toHaveBeenCalled();
        expect(component.isProcessing).toBe(false);
        done();
      }, 100);
    });

    it('should handle submission error with custom message', (done) => {
      const errorResponse = {
        error: { status: 400, message: 'Invalid file format' }
      };
      mockAiGraderService.startGradingLandingPage.and.returnValue(
        throwError(() => errorResponse)
      );

      component.gradeNow();

      setTimeout(() => {
        expect(mockMessageService.error).toHaveBeenCalledWith('Invalid file format');
        expect(component.isProcessing).toBe(false);
        done();
      }, 100);
    });

    it('should handle 500 server error with generic message', (done) => {
      const errorResponse = {
        error: { status: 500, message: 'Internal server error' }
      };
      mockAiGraderService.startGradingLandingPage.and.returnValue(
        throwError(() => errorResponse)
      );

      component.gradeNow();

      setTimeout(() => {
        expect(mockMessageService.error).toHaveBeenCalledWith('Upload failed. Please try again.');
        expect(component.isProcessing).toBe(false);
        done();
      }, 100);
    });

    it('should set isProcessing to true during submission', () => {
      const mockResponse = {
        data: { assessmentId: '123', classId: '456' }
      };
      // Use a delayed observable to catch isProcessing before completion
      mockAiGraderService.startGradingLandingPage.and.returnValue(
        new Observable(subscriber => {
          setTimeout(() => {
            subscriber.next(mockResponse);
            subscriber.complete();
          }, 100);
        })
      );

      component.gradeNow();
      
      // Check immediately after calling gradeNow
      expect(component.isProcessing).toBe(true);
    });
  });

  describe('Form Submission - Not Logged In User', () => {
    beforeEach(() => {
      component.userLoggedIn = false;
      mockAuthService.isLoggedIn.and.returnValue(false);
      component.graderForm.patchValue({
        className: 'Math 202',
        assessmentName: 'Test 2',
        evaluationCriteria: 'Check accuracy'
      });
    });

    it('should save form data to cache and redirect to login', async () => {
      const mockFile = new File(['test'], 'student.pdf', { type: 'application/pdf' });
      component.uploadedFiles = [{ file: mockFile, name: 'student.pdf', type: 'application/pdf' }];
      component.assessmentName = 'Test 2';
      component.className = 'Math 202';
      component.evaluationCriteria = 'Check accuracy';

      await component.gradeNow();

      expect(mockCacheService.saveJsonData).toHaveBeenCalledWith(
        'graderLandingFormData',
        jasmine.any(Object)
      );
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/sign-in']);
    });

    it('should convert files to base64 before caching', async () => {
      const mockFile = new File(['content'], 'test.pdf', { type: 'application/pdf' });
      component.uploadedFiles = [{ file: mockFile, name: 'test.pdf', type: 'application/pdf' }];
      component.assessmentName = 'Quiz';
      component.className = 'Class';
      component.evaluationCriteria = 'Criteria';

      const result = await component.prepareGraderLandingFormData();

      expect(result.quiz_files[0].base64).toBeDefined();
      expect(typeof result.quiz_files[0].base64).toBe('string');
    });
  });

  describe('handleSubmit Method', () => {
    it('should not submit if form is invalid', () => {
      spyOn(window, 'scrollTo');
      spyOn(component, 'gradeNow');

      component.handleSubmit();

      expect(window.scrollTo).toHaveBeenCalled();
      expect(component.gradeNow).not.toHaveBeenCalled();
    });

    it('should submit if form is valid', () => {
      spyOn(component, 'gradeNow');
      component.graderForm.patchValue({
        className: 'English',
        assessmentName: 'Essay',
        evaluationCriteria: 'Grammar and structure'
      });

      component.handleSubmit();

      expect(component.gradeNow).toHaveBeenCalled();
    });
  });

  describe('Drag and Drop', () => {
    it('should handle file drop event', () => {
      const mockFile = new File(['test'], 'dropped.pdf', { type: 'application/pdf' });
      const dataTransfer = new DataTransfer();
      dataTransfer.items.add(mockFile);
      
      const event = new DragEvent('drop', { dataTransfer });
      spyOn(event, 'preventDefault');

      component.onDrop(event);

      expect(event.preventDefault).toHaveBeenCalled();
      expect(component.isDragging).toBe(false);
    });

    it('should set dragging state on dragover', () => {
      const event = new DragEvent('dragover');
      spyOn(event, 'preventDefault');

      component.onDragOver(event);

      expect(event.preventDefault).toHaveBeenCalled();
      expect(component.isDragging).toBe(true);
    });

    it('should clear dragging state on dragleave', () => {
      component.isDragging = true;

      component.onDragLeave();

      expect(component.isDragging).toBe(false);
    });
  });

  describe('Base64 Conversion Utilities', () => {
    it('should convert file to base64', async () => {
      const mockFile = new File(['test content'], 'test.pdf', { type: 'application/pdf' });
      
      const base64 = await component.convertToBase64(mockFile);

      expect(base64).toContain('data:application/pdf;base64,');
    });

    it('should convert base64 back to file', () => {
      const base64 = 'data:application/pdf;base64,JVBERi0xLjQ=';
      
      const file = component.base64ToFile(base64, 'restored.pdf', 'application/pdf');

      expect(file.name).toBe('restored.pdf');
      expect(file.type).toBe('application/pdf');
      expect(file instanceof File).toBe(true);
    });
  });
});