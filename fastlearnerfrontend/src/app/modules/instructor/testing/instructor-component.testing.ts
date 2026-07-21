import { CommonModule } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { of } from 'rxjs';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SubscriptionService } from 'src/app/core/services/subscription.service';
import { createRouterEventsMock } from 'src/app/testing/router.testing';

const successResponse = { status: 200, data: {} };

export function createAiGraderServiceSpy(): jasmine.SpyObj<AiGraderService> {
  const spy = jasmine.createSpyObj<AiGraderService>('AiGraderService', [
    'getClasses',
    'getNoOfPagesUsed',
    'getAssessmentsByClassIdAndAssessmentId',
    'getClassResult',
    'getFilterSearch',
    'editField',
    'deleteStudentResult',
    'retryGrading',
    'exportAiResults',
    'editClass',
    'deleteClass',
    'deleteAssessment',
    'editAssessment',
    'getAssessmentsDetails',
    'getResultById',
    'getResultQuestions',
    'approveResult',
    'startGradingLandingPage',
    'getAssessments',
  ]);

  spy.getClasses.and.returnValue(
    of({
      status: 200,
      data: { aiClasses: [{ id: 1, name: 'Math' }], totalElements: 1 },
    }),
  );
  spy.getNoOfPagesUsed.and.returnValue(
    of({ data: { noOfPagesUsed: 5, allowedPages: 100 } }),
  );
  spy.getAssessmentsByClassIdAndAssessmentId.and.returnValue(
    of({
      status: 200,
      data: {
        assessmentStatusCountResponses: [
          { id: 10, name: 'Quiz 1', process: 0, graded: 1, approved: 1 },
        ],
        totalElements: 1,
      },
    }),
  );
  spy.getClassResult.and.returnValue(
    of({
      status: 200,
      data: {
        aiResultResponseList: [
          {
            id: 1,
            studentName: 'Alice',
            studentEmail: 'alice@test.com',
            resultStatus: 'APPROVED',
            grade: 90,
            score: 90,
            assignmentTitle: 'Quiz',
            className: 'Math',
          },
        ],
        totalElements: 1,
      },
    }),
  );
  spy.getFilterSearch.and.returnValue(of({ data: [] }));
  spy.editField.and.returnValue(of({}));
  spy.deleteStudentResult.and.returnValue(of({}));
  spy.retryGrading.and.returnValue(of({}));
  spy.exportAiResults.and.returnValue(of(new Blob()));
  spy.editClass.and.returnValue(of({ status: 200, data: { name: 'Updated' } }));
  spy.deleteClass.and.returnValue(of({ status: 200 }));
  spy.deleteAssessment.and.returnValue(of({ status: 200 }));
  spy.editAssessment.and.returnValue(of({ status: 200 }));
  spy.getAssessmentsDetails.and.returnValue(
    of({
      status: 200,
      data: {
        data: [
          {
            id: 10,
            name: 'Quiz 1',
            classId: 1,
            className: 'Math',
            process: 2,
            graded: 4,
            approved: 1,
          },
        ],
        totalElements: 1,
      },
    }),
  );
  spy.getResultById.and.returnValue(
    of({
      data: {
        id: 1,
        grade: 80,
        score: 100,
        studentName: 'Alice',
        studentEmail: 'alice@test.com',
        resultStatus: 'GRADED',
        assessmentName: 'Quiz 1',
        className: 'Math',
      },
    }),
  );
  spy.getResultQuestions.and.returnValue(
    of({
      status: 200,
      data: {
        aiResultQueResponseList: [
          { id: 1, questionNumber: 1, score: 5, panelOpen: false },
        ],
        pages: 1,
      },
    }),
  );
  spy.approveResult.and.returnValue(of({ status: 200 }));
  spy.startGradingLandingPage.and.returnValue(
    of({ data: { numberOfFiles: 3, assessmentId: 10, classId: 1 } }),
  );
  spy.getAssessments.and.returnValue(
    of({
      data: {
        aiAssessments: [{ id: 20, name: 'Midterm' }],
        pages: 1,
      },
    }),
  );

  return spy;
}

export function createCacheServiceSpy(): jasmine.SpyObj<CacheService> {
  const spy = jasmine.createSpyObj<CacheService>('CacheService', [
    'getJsonData',
    'removeFromCache',
    'saveJsonData',
    'getDataFromCache',
    'saveInCache',
  ]);

  spy.currentPage = 1;
  spy.getJsonData.and.returnValue(null);
  spy.getDataFromCache.and.returnValue(null);

  return spy;
}

export const instructorComponentTestImports = [
  CommonModule,
  FormsModule,
  BrowserAnimationsModule,
  NzInputModule,
  NzSelectModule,
  NzTableModule,
  NzButtonModule,
  NzPaginationModule,
  NzModalModule,
  NzCardModule,
  NzProgressModule,
  NzToolTipModule,
  NzEmptyModule,
];

export const instructorComponentTestSchemas = [
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA,
];

export async function configureInstructorComponentTest<T>(
  component: new (...args: unknown[]) => T,
  extraProviders: unknown[] = [],
): Promise<{
  fixture: ComponentFixture<T>;
  component: T;
  aiGraderService: jasmine.SpyObj<AiGraderService>;
  cacheService: jasmine.SpyObj<CacheService>;
  messageService: jasmine.SpyObj<MessageService>;
  router: jasmine.SpyObj<Pick<Router, 'navigate' | 'parseUrl'>>;
}> {
  const aiGraderService = createAiGraderServiceSpy();
  const cacheService = createCacheServiceSpy();
  const messageService = jasmine.createSpyObj<MessageService>('MessageService', [
    'error',
    'success',
  ]);
  const router = createRouterEventsMock('/instructor/ai-grader/classes');
  const subscriptionService = jasmine.createSpyObj<SubscriptionService>(
    'SubscriptionService',
    ['fetchCurrentSubscriptionPlanType'],
  );
  subscriptionService.fetchCurrentSubscriptionPlanType.and.returnValue(of({}));

  await TestBed.configureTestingModule({
    declarations: [component],
    imports: instructorComponentTestImports,
    providers: [
      { provide: AiGraderService, useValue: aiGraderService },
      { provide: CacheService, useValue: cacheService },
      { provide: MessageService, useValue: messageService },
      { provide: Router, useValue: router },
      { provide: SubscriptionService, useValue: subscriptionService },
      ...extraProviders,
    ],
    schemas: instructorComponentTestSchemas,
  }).compileComponents();

  const fixture = TestBed.createComponent(component);
  fixture.detectChanges();

  return {
    fixture,
    component: fixture.componentInstance,
    aiGraderService: TestBed.inject(AiGraderService) as jasmine.SpyObj<AiGraderService>,
    cacheService: TestBed.inject(CacheService) as jasmine.SpyObj<CacheService>,
    messageService: TestBed.inject(MessageService) as jasmine.SpyObj<MessageService>,
    router: TestBed.inject(Router) as unknown as jasmine.SpyObj<
      Pick<Router, 'navigate' | 'parseUrl'>
    >,
  };
}
