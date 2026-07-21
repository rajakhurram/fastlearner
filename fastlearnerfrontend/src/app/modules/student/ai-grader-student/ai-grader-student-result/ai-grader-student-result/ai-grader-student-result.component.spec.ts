import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { Location } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { of, throwError } from 'rxjs';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { TestRoutingModule } from 'src/app/modules/instructor/test/test-routing.module';
import { SharedModule } from 'src/app/modules/shared/shared.module';
import { AiGraderStudentResultComponent } from './ai-grader-student-result.component';

describe('AiGraderStudentResultComponent', () => {
  let component: AiGraderStudentResultComponent;
  let fixture: ComponentFixture<AiGraderStudentResultComponent>;
  let mockCacheService: jasmine.SpyObj<CacheService>;
  let mockAiGraderService: jasmine.SpyObj<AiGraderService>;
  let mockMessageService: jasmine.SpyObj<MessageService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const resultViewData = {
    classId: 1,
    assessmentId: 10,
    pageNo: 1,
    teacherName: 'Mr. Smith',
  };

  function setupWithCache(cacheData: any = resultViewData) {
    mockCacheService.getJsonData.and.returnValue(cacheData);
    mockCacheService.getDataFromCache.and.returnValue(
      JSON.stringify({ email: 'student@test.com' }),
    );
    mockAiGraderService.getResultByClassAndAssessmentId.and.returnValue(
      of({
        status: 200,
        data: {
          aiResultResponseList: [
            {
              id: 5,
              grade: 88,
              score: 100,
              rubricUrl: 'https://example.com/rubric.pdf',
            },
          ],
          pages: 1,
        },
      }),
    );
    mockAiGraderService.getResultQuestions.and.returnValue(
      of({
        status: 200,
        data: {
          aiResultQueResponseList: [{ id: 1, questionNumber: 1, score: 5 }],
          pages: 2,
        },
      }),
    );

    fixture = TestBed.createComponent(AiGraderStudentResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(async () => {
    mockCacheService = jasmine.createSpyObj<CacheService>('CacheService', [
      'getJsonData',
      'getDataFromCache',
      'saveJsonData',
    ]);
    mockAiGraderService = jasmine.createSpyObj<AiGraderService>(
      'AiGraderService',
      ['getResultByClassAndAssessmentId', 'getResultQuestions'],
    );
    mockMessageService = jasmine.createSpyObj<MessageService>('MessageService', [
      'error',
    ]);
    mockRouter = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [AiGraderStudentResultComponent],
      imports: [
        TestRoutingModule,
        SharedModule,
        BrowserAnimationsModule,
        NzCardModule,
        NzEmptyModule,
        NzModalModule,
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: (key: string) =>
                  key === 'assessmentId' ? '10' : key === 'classId' ? '1' : null,
              },
            },
          },
        },
        { provide: MessageService, useValue: mockMessageService },
        { provide: CacheService, useValue: mockCacheService },
        { provide: AiGraderService, useValue: mockAiGraderService },
        { provide: Router, useValue: mockRouter },
        { provide: Location, useValue: {} },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  it('should create and load result from cache', () => {
    setupWithCache();

    expect(component).toBeTruthy();
    expect(mockAiGraderService.getResultByClassAndAssessmentId).toHaveBeenCalled();
    expect(component.resultId).toBe(5);
    expect(mockAiGraderService.getResultQuestions).toHaveBeenCalled();
  });

  it('should build resultViewData from query params when cache is empty', () => {
    mockCacheService.getJsonData.and.returnValue(null);
    mockCacheService.getDataFromCache.and.returnValue(
      JSON.stringify({ email: 'student@test.com' }),
    );
    mockAiGraderService.getResultByClassAndAssessmentId.and.returnValue(
      of({
        status: 200,
        data: {
          aiResultResponseList: [{ id: 3, grade: 70, score: 100 }],
          pages: 1,
        },
      }),
    );
    mockAiGraderService.getResultQuestions.and.returnValue(
      of({ status: 200, data: { aiResultQueResponseList: [], pages: 1 } }),
    );

    fixture = TestBed.createComponent(AiGraderStudentResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(mockCacheService.saveJsonData).toHaveBeenCalledWith(
      'resultView',
      jasmine.objectContaining({ classId: 1, assessmentId: 10 }),
    );
  });

  it('should show error when result information is missing', async () => {
    await TestBed.configureTestingModule({
      declarations: [AiGraderStudentResultComponent],
      imports: [
        TestRoutingModule,
        SharedModule,
        BrowserAnimationsModule,
        NzCardModule,
        NzEmptyModule,
        NzModalModule,
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: () => null,
              },
            },
          },
        },
        { provide: MessageService, useValue: mockMessageService },
        { provide: CacheService, useValue: mockCacheService },
        { provide: AiGraderService, useValue: mockAiGraderService },
        { provide: Router, useValue: mockRouter },
        { provide: Location, useValue: {} },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    mockCacheService.getJsonData.and.returnValue(null);

    fixture = TestBed.createComponent(AiGraderStudentResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(mockMessageService.error).toHaveBeenCalledWith(
      'Missing result information. Please retry.',
    );
  });

  it('should show error when student email is unavailable', () => {
    mockCacheService.getJsonData.and.returnValue(resultViewData);
    mockCacheService.getDataFromCache.and.returnValue(null);

    fixture = TestBed.createComponent(AiGraderStudentResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(mockMessageService.error).toHaveBeenCalledWith(
      'Unable to load result. Please refresh and try again.',
    );
    expect(
      mockAiGraderService.getResultByClassAndAssessmentId,
    ).not.toHaveBeenCalled();
  });

  it('should report hasRubric when rubric url exists', () => {
    setupWithCache();

    expect(component.hasRubric).toBeTrue();
  });

  it('should close rubric modal', () => {
    setupWithCache();
    component.showRubricModal = true;

    component.closeRubricModal();

    expect(component.showRubricModal).toBeFalse();
  });

  it('should navigate back to student grader list', () => {
    setupWithCache();

    component.goBack();

    expect(mockCacheService.saveJsonData).toHaveBeenCalledWith(
      'studentGraderResultsNeedsRefresh',
      true,
    );
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/student/grader-results']);
  });

  it('should load more questions on scroll', () => {
    setupWithCache();
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

  it('should handle fetch error and show message', () => {
    mockCacheService.getJsonData.and.returnValue(resultViewData);
    mockCacheService.getDataFromCache.and.returnValue(
      JSON.stringify({ email: 'student@test.com' }),
    );
    mockAiGraderService.getResultByClassAndAssessmentId.and.returnValue(
      throwError(() => ({ error: { message: 'Not found' } })),
    );

    fixture = TestBed.createComponent(AiGraderStudentResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(mockMessageService.error).toHaveBeenCalledWith('Not found');
  });
});
