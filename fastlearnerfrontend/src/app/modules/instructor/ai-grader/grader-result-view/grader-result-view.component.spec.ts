import { ViewContainerRef, CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { ArrowLeftOutline } from '@ant-design/icons-angular/icons';
import { NZ_ICONS, NzIconModule } from 'ng-zorro-antd/icon';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { of, throwError } from 'rxjs';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { ClassUploaderComponent } from 'src/app/modules/dynamic-modals/class-uploader/class-uploader.component';
import { DeletionModalComponent } from 'src/app/modules/dynamic-modals/deletion-modal/deletion-modal.component';
import { SharedModule } from 'src/app/modules/shared/shared.module';
import { GraderResultViewComponent } from './grader-result-view.component';

describe('GraderResultViewComponent', () => {
  let component: GraderResultViewComponent;
  let fixture: ComponentFixture<GraderResultViewComponent>;
  let mockCacheService: jasmine.SpyObj<CacheService>;
  let mockAiGraderService: jasmine.SpyObj<AiGraderService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockMessageService: jasmine.SpyObj<MessageService>;
  let mockModal: jasmine.SpyObj<NzModalService>;

  const resultViewCache = {
    students: [
      { id: 1, status: 'GRADED', grade: 80, score: 100 },
      { id: 2, status: 'INPROCESS', grade: 0, score: 100 },
      { id: 3, status: 'GRADED', grade: 70, score: 100 },
    ],
    index: 0,
    resultId: 1,
    classId: 5,
    assessmentId: 10,
  };

  beforeEach(async () => {
    mockCacheService = jasmine.createSpyObj<CacheService>('CacheService', [
      'getJsonData',
      'saveJsonData',
    ]);
    mockCacheService.getJsonData.and.returnValue(resultViewCache);

    mockAiGraderService = jasmine.createSpyObj<AiGraderService>(
      'AiGraderService',
      [
        'getNoOfPagesUsed',
        'getResultById',
        'getResultQuestions',
        'approveResult',
        'updateQuestion',
        'createManualQuestion',
        'updateManualQuestion',
        'deleteManualQuestion',
        'editEmail',
        'retryGrading',
      ],
    );
    mockAiGraderService.getNoOfPagesUsed.and.returnValue(
      of({ data: { noOfPagesUsed: 10, allowedPages: 100 } }),
    );
    mockAiGraderService.getResultById.and.returnValue(
      of({
        data: {
          id: 1,
          grade: 80,
          score: 100,
          studentName: 'Alice',
          studentEmail: 'alice@test.com',
          resultStatus: 'GRADED',
        },
      }),
    );
    mockAiGraderService.getResultQuestions.and.returnValue(
      of({
        status: 200,
        data: {
          aiResultQueResponseList: [
            { id: 1, questionNumber: 1, score: 5, panelOpen: false },
            { id: 2, questionNumber: 2, score: 3, panelOpen: false },
          ],
          pages: 1,
        },
      }),
    );
    mockAiGraderService.approveResult.and.returnValue(of({ status: 200 }));
    mockAiGraderService.updateQuestion.and.returnValue(
      of({
        status: 200,
        data: {
          obtainedMarks: 8,
          outOfMarks: 10,
          resultObtainedMarks: 88,
          resultOutOfMarks: 100,
        },
      }),
    );
    mockAiGraderService.createManualQuestion.and.returnValue(
      of({
        status: 200,
        data: {
          question: {
            id: 99,
            questionNumber: 3,
            score: 4,
            totalMarks: 5,
            questionSource: 'INSTRUCTOR',
          },
          resultObtainedMarks: 84,
          resultOutOfMarks: 100,
        },
      }),
    );
    mockAiGraderService.updateManualQuestion.and.returnValue(
      of({ status: 200, data: { resultObtainedMarks: 90, resultOutOfMarks: 100 } }),
    );
    mockAiGraderService.deleteManualQuestion.and.returnValue(
      of({
        status: 200,
        data: { resultObtainedMarks: 75, resultOutOfMarks: 100 },
      }),
    );
    mockAiGraderService.editEmail.and.returnValue(of({ status: 200 }));
    mockAiGraderService.retryGrading.and.returnValue(of({ status: 200 }));

    mockRouter = jasmine.createSpyObj<Router>('Router', ['navigate']);
    mockMessageService = jasmine.createSpyObj<MessageService>('MessageService', [
      'success',
      'error',
    ]);
    mockModal = jasmine.createSpyObj<NzModalService>('NzModalService', [
      'create',
    ]);

    await TestBed.configureTestingModule({
      declarations: [GraderResultViewComponent],
      providers: [
        { provide: AiGraderService, useValue: mockAiGraderService },
        { provide: Router, useValue: mockRouter },
        { provide: CacheService, useValue: mockCacheService },
        { provide: MessageService, useValue: mockMessageService },
        { provide: NzModalService, useValue: mockModal },
        { provide: ViewContainerRef, useValue: {} },
        { provide: NZ_ICONS, useValue: [ArrowLeftOutline] },
      ],
      imports: [
        SharedModule,
        NzGridModule,
        NzCardModule,
        NzProgressModule,
        NzIconModule,
        NzToolTipModule,
        NzEmptyModule,
        NzModalModule,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(GraderResultViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load result from cache on init', () => {
    expect(component).toBeTruthy();
    expect(mockAiGraderService.getResultById).toHaveBeenCalledWith(1);
    expect(component.currentIndex).toBe(0);
    expect(component.students.length).toBe(3);
  });

  it('should open share modal with student details', () => {
    component.result = {
      id: 1,
      grade: 80,
      score: 100,
      studentName: 'Alice',
      studentEmail: 'alice@test.com',
    } as any;
    mockModal.create.and.returnValue({ afterClose: of(null) } as any);

    component.share();

    expect(mockModal.create).toHaveBeenCalledWith(
      jasmine.objectContaining({
        nzContent: ClassUploaderComponent,
        nzComponentParams: jasmine.objectContaining({
          studentEmail: 'alice@test.com',
          aiResultId: 1,
        }),
      }),
    );
  });

  it('should approve result after confirmation modal', () => {
    component.result = { id: 1, resultStatus: 'GRADED' } as any;
    component.resultId = 1;
    mockModal.create.and.returnValue({ afterClose: of(true) } as any);

    component.approve();

    expect(mockModal.create).toHaveBeenCalledWith(
      jasmine.objectContaining({ nzContent: DeletionModalComponent }),
    );
    expect(mockAiGraderService.approveResult).toHaveBeenCalledWith(1);
    expect(component.result.resultStatus).toBe('APPROVED');
  });

  it('should expand and collapse all questions', () => {
    component.questions = [
      { id: 1, panelOpen: false },
      { id: 2, panelOpen: false },
    ] as any;

    component.expandAll(true);
    expect(component.expandAllCheck).toBeTrue();
    expect(component.questions.every((q) => q.panelOpen)).toBeTrue();

    component.expandAll(false);
    expect(component.questions.every((q) => !q.panelOpen)).toBeTrue();
  });

  it('should navigate to next student skipping INPROCESS', () => {
    component.students = resultViewCache.students;
    component.currentIndex = 0;
    mockAiGraderService.getResultById.calls.reset();

    component.onPageChange('right');

    expect(component.currentIndex).toBe(2);
    expect(mockAiGraderService.getResultById).toHaveBeenCalledWith(3);
  });

  it('should navigate to previous student', () => {
    component.students = resultViewCache.students;
    component.currentIndex = 2;
    mockAiGraderService.getResultById.calls.reset();

    component.onPageChange('left');

    expect(component.currentIndex).toBe(0);
    expect(mockAiGraderService.getResultById).toHaveBeenCalledWith(1);
  });

  it('should navigate back to results and mark cache for refresh', () => {
    component.result = { id: 1, grade: 85, score: 100 } as any;
    component.resultViewData = resultViewCache;

    component.backToResultScreen();

    expect(mockCacheService.saveJsonData).toHaveBeenCalledWith(
      'graderResultsNeedsRefresh',
      true,
    );
    expect(mockRouter.navigate).toHaveBeenCalledWith(
      ['instructor/ai-grader/results'],
      jasmine.objectContaining({
        queryParams: { id: 10, classId: 5 },
      }),
    );
  });

  it('should toggle individual question panel', () => {
    const panel = { panelOpen: false };
    component.onPanelClick(panel);
    expect(panel.panelOpen).toBeTrue();
    component.onPanelClick(panel);
    expect(panel.panelOpen).toBeFalse();
  });

  it('should validate question marks before update', () => {
    const question = {
      enableQuestionEditing: true,
      editedObtainedMarks: 12,
      editedOutOfMarks: 10,
    } as any;

    component.updateQuestionMarks(question);

    expect(mockMessageService.error).toHaveBeenCalledWith(
      'Obtained marks cannot exceed out of marks.',
    );
    expect(mockAiGraderService.updateQuestion).not.toHaveBeenCalled();
  });

  it('should update question marks and total score', () => {
    component.result = { id: 1, grade: 80, score: 100 } as any;
    component.resultId = 1;
    component.questions = [
      { id: 5, score: 5, totalMarks: 10, enableQuestionEditing: true },
    ] as any;
    const question = {
      id: 5,
      enableQuestionEditing: true,
      editedObtainedMarks: 8,
      editedOutOfMarks: 10,
    } as any;

    component.updateQuestionMarks(question);

    expect(mockAiGraderService.updateQuestion).toHaveBeenCalledWith(5, 8, 10);
    expect(question.score).toBe(8);
    expect(question.enableQuestionEditing).toBeFalse();
  });

  it('should open add manual question modal with next question number', () => {
    component.questions = [
      { id: 1, questionNumber: 1 },
      { id: 2, questionNumber: 2 },
    ] as any;

    component.openAddQuestionModal();

    expect(component.showAddQuestionModal).toBeTrue();
    expect(component.manualQuestionForm.questionNumber).toBe(3);
  });

  it('should recompute next question number after deleting a middle question', () => {
    component.resultId = 1;
    component.result = { id: 1, grade: 100, score: 100 } as any;
    (component as any).highestKnownQuestionNumber = 20;
    component.questions = Array.from({ length: 20 }, (_, i) => ({
      id: i + 1,
      questionNumber: i + 1,
      questionSource: 'INSTRUCTOR',
      score: 5,
      totalMarks: 5,
    })) as any;

    const middle = component.questions[4]; // questionNumber 5
    mockAiGraderService.deleteManualQuestion.and.returnValue(
      of({ status: 200, data: {} }),
    );
    // After reload, remaining contiguous 1..19 (totalElements drives next = 20)
    const remaining = Array.from({ length: 19 }, (_, i) => ({
      id: i + 1,
      questionNumber: i + 1,
      questionSource: 'INSTRUCTOR',
      score: 5,
      totalMarks: 5,
    }));
    mockAiGraderService.getResultQuestions.and.returnValue(
      of({
        status: 200,
        data: {
          aiResultQueResponseList: remaining,
          pages: 1,
          totalElements: 19,
        },
      }),
    );

    component.deleteManualQuestion(middle);

    expect((component as any).highestKnownQuestionNumber).toBe(19);
    component.openAddQuestionModal();
    expect(component.manualQuestionForm.questionNumber).toBe(20);
  });

  it('should save manual question and refresh totals', () => {
    component.resultId = 1;
    component.result = { id: 1, grade: 80, score: 100 } as any;
    component.manualQuestionForm = {
      questionNumber: 3,
      obtainedMarks: 4,
      outOfMarks: 5,
      feedback: 'Good work',
    };

    component.saveManualQuestion();

    expect(mockAiGraderService.createManualQuestion).toHaveBeenCalledWith(
      jasmine.objectContaining({
        aiResultId: 1,
        questionNumber: 3,
        obtainedMarks: 4,
        outOfMarks: 5,
        feedback: 'Good work',
      }),
    );
    expect(mockMessageService.success).toHaveBeenCalledWith(
      'Question added successfully.',
    );
    expect(component.showAddQuestionModal).toBeFalse();
    expect(component.result.grade).toBe(84);
  });

  it('should delete manual question after confirmation', () => {
    component.resultId = 1;
    component.result = { id: 1, grade: 80, score: 100 } as any;
    component.questions = [
      {
        id: 50,
        questionNumber: 2,
        questionSource: 'INSTRUCTOR',
        score: 5,
        totalMarks: 10,
      },
    ] as any;
    mockModal.create.and.returnValue({ afterClose: of(true) } as any);

    component.confirmDeleteManualQuestion(component.questions[0]);

    expect(mockAiGraderService.deleteManualQuestion).toHaveBeenCalledWith(50);
    expect(mockMessageService.success).toHaveBeenCalledWith(
      'Question deleted successfully.',
    );
    expect(component.result.grade).toBe(75);
    expect(mockAiGraderService.getResultQuestions).toHaveBeenCalled();
  });

  it('should not delete non-instructor question', () => {
    component.confirmDeleteManualQuestion({
      id: 1,
      questionSource: 'AI',
    } as any);

    expect(mockModal.create).not.toHaveBeenCalled();
    expect(mockAiGraderService.deleteManualQuestion).not.toHaveBeenCalled();
  });

  it('should apply question update response to result totals', () => {
    component.result = { id: 1, grade: 70, score: 100 } as any;
    component.questions = [{ id: 3, score: 5, totalMarks: 10 }] as any;

    const applied = component.applyQuestionUpdateResponse(3, {
      obtainedMarks: 9,
      outOfMarks: 10,
      resultObtainedMarks: 79,
      resultOutOfMarks: 100,
    });

    expect(applied).toBeTrue();
    expect(component.questions[0].score).toBe(9);
    expect(component.result.grade).toBe(79);
    expect(component.result.score).toBe(100);
  });

  it('should return confidence colors by level', () => {
    expect(component.getConfidenceColor('high')).toBe('#5cb85c');
    expect(component.getConfidenceColor('medium')).toBe('#262261');
    expect(component.getConfidenceColor('low')).toBe('#E23643');
    expect(component.getConfidenceColor('unknown')).toBe('gray');
  });

  it('should reject invalid email on saveEmail', () => {
    const row = { id: 1, studentEmail: 'bad' };
    const event = { target: { value: 'not-valid' } } as unknown as Event;

    component.saveEmail(event, row);

    expect(mockMessageService.error).toHaveBeenCalledWith(
      'Please enter a valid email address.',
    );
    expect(mockAiGraderService.editEmail).not.toHaveBeenCalled();
  });

  it('should save valid email on saveEmail', () => {
    const row = { id: 1, studentEmail: 'new@test.com', isEditingEmail: true };
    const event = { target: { value: 'new@test.com' } } as unknown as Event;

    component.saveEmail(event, row);

    expect(mockAiGraderService.editEmail).toHaveBeenCalledWith(
      1,
      'new@test.com',
    );
    expect(row.isEditingEmail).toBeFalse();
  });

  it('should revert email on saveEmail error', () => {
    mockAiGraderService.editEmail.and.returnValue(
      throwError(() => ({ status: 500 })),
    );
    const row = {
      id: 1,
      studentEmail: 'new@test.com',
      originalEmail: 'old@test.com',
      isEditingEmail: true,
    };
    const event = { target: { value: 'new@test.com' } } as unknown as Event;

    component.saveEmail(event, row);

    expect(row.studentEmail).toBe('old@test.com');
    expect(row.isEditingEmail).toBeFalse();
  });

  it('should cancel email edit and restore original', () => {
    const row = {
      studentEmail: 'edited@test.com',
      originalEmail: 'original@test.com',
      isEditingEmail: true,
    };

    component.cancelEdit(row);

    expect(row.studentEmail).toBe('original@test.com');
    expect(row.isEditingEmail).toBeFalse();
  });

  it('should load more questions on scroll near bottom', () => {
    spyOn(component, 'getResultQuestions');
    component.questionTotalPages = 3;
    component.questionPayload.pageNo = 0;
    component.loadingMoreQuestions = false;

    const element = {
      scrollTop: 90,
      scrollHeight: 100,
      offsetHeight: 10,
    } as HTMLElement;

    component.onScroll({ target: element } as unknown as Event);

    expect(component.loadingMoreQuestions).toBeTrue();
    expect(component.questionPayload.pageNo).toBe(1);
    expect(component.getResultQuestions).toHaveBeenCalledWith(true);
  });

  it('should enable question edit mode with marks prefilled', () => {
    const question = {
      score: 7,
      totalMarks: 10,
      enableQuestionEditing: false,
    } as any;

    component.enableEdit(question);

    expect(question.enableQuestionEditing).toBeTrue();
    expect(question.editedObtainedMarks).toBe(7);
    expect(question.editedOutOfMarks).toBe(10);
  });

  it('should cancel question edit and clear edited marks', () => {
    const question = {
      enableQuestionEditing: true,
      editedObtainedMarks: 8,
      editedOutOfMarks: 10,
    } as any;

    component.cancelQuestionEdit(question);

    expect(question.enableQuestionEditing).toBeFalse();
    expect(question.editedObtainedMarks).toBeNull();
    expect(question.editedOutOfMarks).toBeNull();
  });

  it('should validate manual form before save', () => {
    const error = component.validateManualForm({
      questionNumber: 1,
      obtainedMarks: null,
      outOfMarks: 10,
      feedback: '',
    });

    expect(error).toBe('Obtained marks is required.');
  });

  it('should save manual question edit', () => {
    component.editingQuestionId = 50;
    component.editManualQuestionForm = {
      questionNumber: 2,
      obtainedMarks: 5,
      outOfMarks: 5,
      feedback: 'Updated',
    };

    component.saveManualQuestionEdit();

    expect(mockAiGraderService.updateManualQuestion).toHaveBeenCalledWith(
      50,
      jasmine.objectContaining({
        questionNumber: 2,
        obtainedMarks: 5,
        outOfMarks: 5,
        feedback: 'Updated',
      }),
    );
    expect(mockMessageService.success).toHaveBeenCalledWith(
      'Question updated successfully.',
    );
  });

  it('should retry grading and navigate back to results', () => {
    component.resultId = 5;
    component.result = { id: 5, grade: 0, score: 100 } as any;
    component.resultViewData = resultViewCache;
    mockRouter.navigate.calls.reset();

    component.retryGrading();

    expect(mockAiGraderService.retryGrading).toHaveBeenCalledWith(5);
    expect(mockRouter.navigate).toHaveBeenCalled();
  });

  it('should show upgrade button for free plan users', () => {
    mockCacheService.getJsonData.and.returnValue({
      subscriptionPlanType: 'FREE',
    });

    component.showUpgradePLanButton();

    expect(component.showUpgradePlan).toBeTrue();
  });

  it('should clamp obtained marks when out-of marks is lower on input', () => {
    const question = {
      editedObtainedMarks: 9,
      editedOutOfMarks: 10,
    } as any;
    const input = document.createElement('input');
    input.value = '5';
    const event = { target: input } as unknown as Event;

    component.onOutOfInput(event, question);

    expect(question.editedOutOfMarks).toBe(5);
    expect(question.editedObtainedMarks).toBe(5);
  });
});
