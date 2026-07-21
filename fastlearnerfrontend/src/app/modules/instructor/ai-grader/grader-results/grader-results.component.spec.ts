import { ActivatedRoute } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { of, Subject, throwError } from 'rxjs';
import { ViewContainerRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { GraderResultsComponent } from './grader-results.component';
import {
  configureInstructorComponentTest,
  createAiGraderServiceSpy,
} from '../../testing/instructor-component.testing';

describe('GraderResultsComponent', () => {
  async function setup(queryParams: Record<string, unknown> = {}) {
    const aiGraderService = createAiGraderServiceSpy();
    const cacheService = jasmine.createSpyObj<CacheService>(
      'CacheService',
      ['getJsonData', 'removeFromCache', 'saveJsonData', 'getDataFromCache', 'saveInCache'],
    );
    cacheService.currentPage = 1;
    cacheService.getJsonData.and.returnValue(null);
    cacheService.getDataFromCache.and.returnValue(null);

    const modal = jasmine.createSpyObj<NzModalService>('NzModalService', ['create']);

    return configureInstructorComponentTest(GraderResultsComponent, [
      { provide: NzModalService, useValue: modal },
      { provide: ViewContainerRef, useValue: {} },
      {
        provide: ActivatedRoute,
        useValue: {
          queryParams: of({ id: 10, classId: 5, ...queryParams }),
        },
      },
      { provide: AiGraderService, useValue: aiGraderService },
      { provide: CacheService, useValue: cacheService },
    ]);
  }

  it('should create', async () => {
    const { component } = await setup();
    expect(component).toBeTruthy();
  });

  it('should load results when query params are present', async () => {
    const { component, aiGraderService } = await setup();

    expect(component.selectedAssessmentId).toBe(10);
    expect(component.selectedClassId).toBe(5);
    expect(aiGraderService.getClassResult).toHaveBeenCalled();
    expect(component.allResults.length).toBe(1);
  });

  it('should update displayed results on page change', async () => {
    const { component, cacheService } = await setup();
    component.allResults = Array.from({ length: 12 }, (_, i) => ({
      id: i + 1,
      resultStatus: 'APPROVED',
    })) as any;

    component.onPageChange(2);

    expect(cacheService.currentPage).toBe(2);
    expect(component.currentPage).toBe(2);
    expect(component.displayedResults.length).toBe(5);
  });

  it('should route to classes when source is classes', async () => {
    const { component, router } = await setup();
    component.source = 'classes';

    component.routeToGraderUploader();

    expect(router.navigate).toHaveBeenCalledWith(
      ['instructor/ai-grader/classes'],
      {},
    );
  });

  it('should route to assessments when source is assessment', async () => {
    const { component, router } = await setup();
    component.source = 'assessment';

    component.routeToGraderUploader();

    expect(router.navigate).toHaveBeenCalledWith(
      ['instructor/ai-grader/assessments'],
      {},
    );
  });

  it('should return correct status class and label', async () => {
    const { component } = await setup();

    expect(component.getStatusClass('APPROVED')).toBe('tag-approved');
    expect(component.getStatusClass('INPROCESS')).toBe('tag-processing');
    expect(component.getStatusLabel('graded')).toBe('Graded');
    expect(component.getStatusLabel('unknown')).toBe('Pending');
  });

  it('should detect when all results are approved', async () => {
    const { component } = await setup();
    component.aiResultResponse = [
      { resultStatus: 'APPROVED' },
      { resultStatus: 'APPROVED' },
    ] as any;

    expect(component.isAllGraded).toBeTrue();
  });

  it('should clear search and refetch results', async () => {
    const { component, aiGraderService } = await setup();
    aiGraderService.getClassResult.calls.reset();
    component.aiResults.search = 'test';

    component.clearSearch();

    expect(component.aiResults.search).toBeNull();
    expect(aiGraderService.getClassResult).toHaveBeenCalled();
  });

  it('should search when keyword is provided', async () => {
    const { component, aiGraderService } = await setup();
    aiGraderService.getClassResult.calls.reset();

    component.searchCallBack('alice');

    expect(component.aiResults.search).toBe('alice');
    expect(aiGraderService.getClassResult).toHaveBeenCalled();
  });

  it('should not fetch results without assignment and class ids', async () => {
    const { component, aiGraderService } = await setup();
    aiGraderService.getClassResult.calls.reset();
    component.aiResults.assignmentId = null;

    component.fetchResults();

    expect(aiGraderService.getClassResult).not.toHaveBeenCalled();
  });

  it('should show error when saving empty email field', async () => {
    const { component, messageService } = await setup();
    const row = { id: 1, studentEmail: '   ' };

    component.saveField('email', row);

    expect(messageService.error).toHaveBeenCalledWith('Email cannot be empty.');
  });

  it('should show error for invalid email', async () => {
    const { component, messageService } = await setup();
    const row = { id: 1, studentEmail: 'invalid-email' };

    component.saveField('email', row);

    expect(messageService.error).toHaveBeenCalledWith(
      'Please enter a valid email address.',
    );
  });

  it('should save valid email field', async () => {
    const { component, aiGraderService } = await setup();
    const row = {
      id: 1,
      studentEmail: 'valid@test.com',
      isEditingEmail: true,
    };

    component.saveField('email', row);

    expect(aiGraderService.editField).toHaveBeenCalledWith(
      1,
      'email',
      'valid@test.com',
    );
  });

  it('should hide upgrade button for premium plan', async () => {
    const { component, cacheService } = await setup();
    cacheService.getJsonData.and.returnValue({
      subscriptionPlanType: 'PREMIUM',
    });

    component.showUpgradePLanButton();

    expect(component.showUpgradePlan).toBeFalse();
  });

  it('should load graded paper counts', async () => {
    const { component } = await setup();

    component.getNoOfPages();

    expect(component.gradedPapers).toBe(5);
    expect(component.allowedPapers).toBe(100);
  });

  it('should navigate to result view for graded item', async () => {
    const { component, router, cacheService } = await setup();
    component.aiResultResponse = [
      { id: 7, resultStatus: 'GRADED', grade: 80, score: 80 },
    ] as any;
    component.selectedClassId = 5;
    component.selectedAssessmentId = 10;

    component.resultView(component.aiResultResponse[0], 0);

    expect(cacheService.saveJsonData).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith([
      'instructor/ai-grader/result/view',
    ]);
  });

  it('should retry grading for error status', async () => {
    const { component, aiGraderService } = await setup();
    const row = { id: 3, resultStatus: 'ERROR' };

    component.resultView(row);

    expect(row.resultStatus).toBe('INPROCESS');
    expect(aiGraderService.retryGrading).toHaveBeenCalledWith(3);
  });

  it('should handle fetch error and clear results', async () => {
    const aiGraderService = createAiGraderServiceSpy();
    aiGraderService.getClassResult.and.returnValue(
      throwError(() => ({ status: 500 })),
    );

    const { component } = await configureInstructorComponentTest(
      GraderResultsComponent,
      [
        {
          provide: NzModalService,
          useValue: jasmine.createSpyObj('NzModalService', ['create']),
        },
        { provide: ViewContainerRef, useValue: {} },
        {
          provide: ActivatedRoute,
          useValue: { queryParams: of({ id: 10, classId: 5 }) },
        },
        { provide: AiGraderService, useValue: aiGraderService },
      ],
    );

    component.aiResults.assignmentId = 10;
    component.aiResults.classId = 5;
    component.fetchResults();

    expect(component.allResults).toEqual([]);
    expect(component.displayedResults).toEqual([]);
  });

  it('should block result view navigation while INPROCESS', async () => {
    const { component, router } = await setup();
    const row = { id: 8, resultStatus: 'INPROCESS' };

    component.resultView(row);

    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should save student name field', async () => {
    const { component, aiGraderService } = await setup();
    const row = {
      id: 1,
      studentName: 'Bob',
      isEditingName: true,
    };

    component.saveField('name', row);

    expect(aiGraderService.editField).toHaveBeenCalledWith(1, 'name', 'Bob');
  });

  it('should trigger sort change and refetch results', async () => {
    const { component, aiGraderService } = await setup();
    aiGraderService.getClassResult.calls.reset();
    component.selectedSort = '2';

    component.onSortChange();

    expect(component.aiResults.aiResultSort).toBe('2');
    expect(aiGraderService.getClassResult).toHaveBeenCalled();
  });

  it('should export results via service', async () => {
    const { component, aiGraderService } = await setup();
    spyOn(document, 'createElement').and.returnValue({
      click: jasmine.createSpy('click'),
      href: '',
      download: '',
    } as any);
    spyOn(document.body, 'appendChild');
    spyOn(document.body, 'removeChild');
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:url');
    spyOn(window.URL, 'revokeObjectURL');

    component.exportAiResults();

    expect(aiGraderService.exportAiResults).toHaveBeenCalled();
  });

  it('should delete student result when deletion modal confirms', async () => {
    const deleteClick = new Subject<void>();
    const { component, aiGraderService } = await setup();
    const modal = TestBed.inject(NzModalService) as jasmine.SpyObj<NzModalService>;
    modal.create.and.returnValue({
      componentInstance: {
        deleteClick: deleteClick.asObservable(),
      },
    } as any);
    aiGraderService.getClassResult.calls.reset();

    component.deleteResult({ id: 99 });
    deleteClick.next();

    expect(aiGraderService.deleteStudentResult).toHaveBeenCalledWith(99);
    expect(aiGraderService.getClassResult).toHaveBeenCalled();
  });

  it('should generate timestamp for SSE connection', async () => {
    const { component } = await setup();
    const ts = component.generateTimeStamp();

    expect(typeof ts).toBe('number');
    expect(ts).toBeLessThanOrEqual(Date.now());
  });

  it('should enable and cancel inline field editing', async () => {
    const { component } = await setup();
    const row = { studentEmail: 'a@test.com', isEditingEmail: false };

    component.enableEdit(row, 'email');
    expect(row.isEditingEmail).toBeTrue();

    component.cancelEdit(row, 'email');
    expect(row.isEditingEmail).toBeFalse();
  });

  it('should save roll number field', async () => {
    const { component, aiGraderService } = await setup();
    const row = {
      id: 1,
      studentRollNumber: 'R-42',
      isEditingRollNumber: true,
    };

    component.saveField('rollNumber', row);

    expect(aiGraderService.editField).toHaveBeenCalledWith(
      1,
      'rollNumber',
      'R-42',
    );
  });

  it('should open share modal for selected student', async () => {
    const { component } = await setup();
    const modal = TestBed.inject(NzModalService) as jasmine.SpyObj<NzModalService>;
    const student = {
      id: 4,
      studentEmail: 'share@test.com',
      grade: 90,
      score: 100,
    };

    component.openClassModal(student);

    expect(modal.create).toHaveBeenCalled();
  });

  it('should route to ai grader uploader', async () => {
    const { component, router } = await setup();

    component.routeToAIGraderUploader();

    expect(router.navigate).toHaveBeenCalledWith(
      ['instructor/ai-grader/uploader'],
      {},
    );
  });

  it('should connect SSE and update matching rows', async () => {
    const { component, cacheService } = await setup();
    cacheService.getDataFromCache.and.callFake((key: string) => {
      if (key === 'isLoggedIn') return 'true';
      if (key === 'userProfile')
        return JSON.stringify({ userId: 99, email: 't@test.com' });
      return null;
    });
    component.aiResults = { classId: 5, assignmentId: 10 } as any;
    component.aiResultResponse = [
      {
        id: 1,
        studentName: 'Alice',
        studentEmail: 'alice@test.com',
        resultStatus: 'INPROCESS',
        grade: 0,
        valuesLoader: true,
        isEditingName: false,
        isEditingEmail: false,
        isEditingRollNumber: false,
      },
    ] as any;

    const listeners: Record<string, (event: MessageEvent) => void> = {};
    const mockEventSource = {
      readyState: 0,
      close: jasmine.createSpy('close'),
      addEventListener: jasmine.createSpy('addEventListener').and.callFake(
        (type: string, cb: (event: MessageEvent) => void) => {
          listeners[type] = cb;
        },
      ),
    };
    spyOn(window as any, 'EventSource').and.returnValue(mockEventSource);

    component.connectResultSSE();

    listeners['results']({
      data: JSON.stringify([
        {
          id: 1,
          studentName: 'Alice Updated',
          studentEmail: 'alice@test.com',
          grade: 92,
          resultStatus: 'GRADED',
        },
      ]),
    } as MessageEvent);

    expect(component.aiResultResponse[0].studentName).toBe('Alice Updated');
    expect(component.aiResultResponse[0].grade).toBe(92);
    expect(component.aiResultResponse[0].resultStatus).toBe('GRADED');
    expect(component.aiResultResponse[0].valuesLoader).toBeFalse();
  });
});
