import { ViewContainerRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DomSanitizer } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { of, throwError } from 'rxjs';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SharedModule } from 'src/app/modules/shared/shared.module';
import { TestRoutingModule } from '../../test/test-routing.module';
import { GraderUploaderComponent } from './grader-uploader.component';
import { FormsModule } from '@angular/forms';

describe('GraderUploaderComponent', () => {
  let component: GraderUploaderComponent;
  let fixture: ComponentFixture<GraderUploaderComponent>;
  let mockAiGraderService: jasmine.SpyObj<AiGraderService>;
  let mockCacheService: jasmine.SpyObj<CacheService>;
  let mockMessageService: jasmine.SpyObj<MessageService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockAiGraderService = jasmine.createSpyObj<AiGraderService>(
      'AiGraderService',
      ['getClasses', 'getNoOfPagesUsed', 'startGradingLandingPage', 'getAssessments'],
    );
    mockCacheService = jasmine.createSpyObj<CacheService>('CacheService', [
      'getJsonData',
    ]);
    mockMessageService = jasmine.createSpyObj<MessageService>('MessageService', [
      'error',
      'success',
    ]);
    mockRouter = jasmine.createSpyObj<Router>('Router', ['navigate']);

    mockAiGraderService.getClasses.and.returnValue(
      of({
        data: {
          aiClasses: [{ id: 1, name: 'Math 101' }],
          pages: 1,
        },
      }),
    );
    mockAiGraderService.getNoOfPagesUsed.and.returnValue(
      of({ data: { noOfPagesUsed: 0, allowedPages: 100 } }),
    );
    mockAiGraderService.getAssessments.and.returnValue(
      of({
        data: {
          aiAssessments: [{ id: 20, name: 'Quiz 1' }],
          pages: 1,
        },
      }),
    );
    mockAiGraderService.startGradingLandingPage.and.returnValue(
      of({
        data: { numberOfFiles: 2, assessmentId: 20, classId: 1 },
      }),
    );
    mockCacheService.getJsonData.and.returnValue(null);

    await TestBed.configureTestingModule({
      declarations: [GraderUploaderComponent],
      imports: [
        TestRoutingModule,
        SharedModule,
        BrowserAnimationsModule,
        NzCardModule,
        NzProgressModule,
        NzSelectModule,
        NzGridModule,
        NzToolTipModule,
        FormsModule,
      ],
      providers: [
        { provide: NzModalService, useValue: {} },
        { provide: AiGraderService, useValue: mockAiGraderService },
        { provide: Router, useValue: mockRouter },
        { provide: MessageService, useValue: mockMessageService },
        { provide: DomSanitizer, useValue: {} },
        { provide: NgxUiLoaderService, useValue: {} },
        { provide: ViewContainerRef, useValue: {} },
        { provide: CacheService, useValue: mockCacheService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(GraderUploaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load classes on init', () => {
    expect(component).toBeTruthy();
    expect(mockAiGraderService.getClasses).toHaveBeenCalled();
    expect(mockAiGraderService.getNoOfPagesUsed).toHaveBeenCalled();
  });

  it('should show error when gradeNow is called without class selection', () => {
    component.selectedClass = null;
    component.selectedAssessment = null;

    component.gradeNow();

    expect(mockMessageService.error).toHaveBeenCalledWith(
      'Please Select A Class',
    );
    expect(mockAiGraderService.startGradingLandingPage).not.toHaveBeenCalled();
  });

  it('should submit grading and navigate to results on success', () => {
    component.selectedClass = 1;
    component.selectedAssessment = 20;
    component.classes = [{ id: 1, name: 'Math 101' }] as any;
    component.assessments = [{ id: 20, name: 'Quiz 1' }] as any;
    component.uploadedFiles = [
      {
        file: new File(['pdf'], 'student.pdf', { type: 'application/pdf' }),
        name: 'student.pdf',
      },
    ];

    component.gradeNow();

    expect(mockAiGraderService.startGradingLandingPage).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(
      ['instructor/ai-grader/results'],
      {
        queryParams: {
          id: 20,
          classId: 1,
          numberOfFiles: 2,
          source: 'uploader',
        },
      },
    );
    expect(component.isProcessing).toBeFalse();
  });

  it('should handle gradeNow server error', () => {
    component.selectedClass = 1;
    component.selectedAssessment = 20;
    mockAiGraderService.startGradingLandingPage.and.returnValue(
      throwError(() => ({ status: 500, error: { message: 'Server error' } })),
    );

    component.gradeNow();

    expect(mockMessageService.error).toHaveBeenCalledWith(
      'Upload failed. Please try again.',
    );
    expect(component.isProcessing).toBeFalse();
  });

  it('should limit file uploads to 100 files', () => {
    const mockFiles = Array.from(
      { length: 101 },
      (_, i) => new File(['x'], `file${i}.pdf`, { type: 'application/pdf' }),
    );

    component.onFileUpload({ target: { files: mockFiles } });

    expect(mockMessageService.error).toHaveBeenCalledWith(
      'You can upload a maximum of 100 files.',
    );
    expect(component.uploadedFiles.length).toBe(0);
  });

  it('should accept PDF answer key files', () => {
    const pdf = new File(['key'], 'rubric.pdf', { type: 'application/pdf' });
    component.onAnswerFileSelected({ target: { files: [pdf], value: '' } });

    expect(component.selectedFileName).toBe('rubric.pdf');
    expect(component.selectedAnswerFile).toBe(pdf);
  });

  it('should reject non-PDF answer key files', () => {
    const txt = new File(['key'], 'rubric.txt', { type: 'text/plain' });
    component.onAnswerFileSelected({ target: { files: [txt], value: '' } });

    expect(mockMessageService.error).toHaveBeenCalledWith(
      'Please upload a pdf file.',
    );
    expect(component.selectedAnswerFile).toBeNull();
  });

  it('should detect zip and rar files', () => {
    expect(
      component.isZipFile(
        new File([''], 'papers.zip', { type: 'application/zip' }),
      ),
    ).toBeTrue();
    expect(
      component.isZipFile(
        new File([''], 'papers.rar', { type: 'application/x-rar-compressed' }),
      ),
    ).toBeTrue();
    expect(
      component.isZipFile(
        new File([''], 'paper.pdf', { type: 'application/pdf' }),
      ),
    ).toBeFalse();
  });

  it('should limit drag-and-drop to 40 files', () => {
    const mockFiles = Array.from(
      { length: 41 },
      (_, i) => new File(['x'], `file${i}.pdf`, { type: 'application/pdf' }),
    );
    const event = {
      preventDefault: jasmine.createSpy('preventDefault'),
      dataTransfer: { files: mockFiles },
    } as unknown as DragEvent;

    component.onDrop(event);

    expect(mockMessageService.error).toHaveBeenCalledWith(
      'You can upload a maximum of 40 files.',
    );
  });

  it('should reset uploaded files', () => {
    component.uploadedFiles = [{ name: 'a.pdf' }];
    component.resetUpload();
    expect(component.uploadedFiles.length).toBe(0);
  });

  it('should hide upgrade button for premium users', () => {
    mockCacheService.getJsonData.and.returnValue({
      subscriptionPlanType: 'PREMIUM',
    });
    component.showUpgradePLanButton();
    expect(component.showUpgradePlan).toBeFalse();
  });
});
