import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { of } from 'rxjs';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { createRouterEventsMock } from 'src/app/testing/router.testing';
import { SharedModule } from '../../shared/shared.module';
import { AiGraderStudentComponent } from './ai-grader-student.component';

describe('AiGraderStudentComponent', () => {
  let component: AiGraderStudentComponent;
  let fixture: ComponentFixture<AiGraderStudentComponent>;
  let mockAiGraderService: jasmine.SpyObj<AiGraderService>;
  let mockCacheService: jasmine.SpyObj<CacheService>;
  let mockRouter: ReturnType<typeof createRouterEventsMock>;

  const classesResponse = {
    status: 200,
    data: {
      aiClasses: [
        { id: 1, name: 'Math', panelOpen: false },
        { id: 2, name: 'Science', panelOpen: false },
      ],
    },
  };

  const assessmentsResponse = {
    status: 200,
    data: {
      assessmentStatusCountResponses: [
        {
          id: 10,
          name: 'Quiz 1',
          grade: 85,
          full_name: 'Mr. Smith',
          sharedEmailDate: '2026-01-15',
        },
      ],
      totalElements: 1,
    },
  };

  beforeEach(async () => {
    mockAiGraderService = jasmine.createSpyObj<AiGraderService>(
      'AiGraderService',
      ['getClassesStudent', 'getAssessmentsByClassIdAndAssessmentId'],
    );
    mockCacheService = jasmine.createSpyObj<CacheService>('CacheService', [
      'getJsonData',
      'removeFromCache',
      'saveJsonData',
      'getDataFromCache',
    ]);
    mockRouter = createRouterEventsMock('/student/grader-results');

    mockAiGraderService.getClassesStudent.and.returnValue(of(classesResponse));
    mockAiGraderService.getAssessmentsByClassIdAndAssessmentId.and.returnValue(
      of(assessmentsResponse),
    );
    mockCacheService.getDataFromCache.and.returnValue(
      JSON.stringify({ email: 'student@test.com' }),
    );
    mockCacheService.getJsonData.and.returnValue(null);

    await TestBed.configureTestingModule({
      declarations: [AiGraderStudentComponent],
      imports: [SharedModule, BrowserAnimationsModule, NzEmptyModule],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: DatePipe, useValue: new DatePipe('en-US') },
        { provide: ActivatedRoute, useValue: {} },
        { provide: CacheService, useValue: mockCacheService },
        { provide: AiGraderService, useValue: mockAiGraderService },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(AiGraderStudentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load classes on init', () => {
    expect(component).toBeTruthy();
    expect(mockAiGraderService.getClassesStudent).toHaveBeenCalled();
    expect(component.classes?.length).toBe(2);
    expect(component.selectedClassId).toBe(1);
  });

  it('should expand all class panels', () => {
    component.classes = [{ panelOpen: false }, { panelOpen: false }] as any;

    component.expandAll();

    expect(component.classes?.every((c) => c.panelOpen)).toBeTrue();
  });

  it('should toggle panel and load assessments', () => {
    const aClass = { id: 2, panelOpen: false };
    component.classes = [aClass] as any;
    mockAiGraderService.getAssessmentsByClassIdAndAssessmentId.calls.reset();

    component.onPanelClick(aClass, true);

    expect(aClass.panelOpen).toBeTrue();
    expect(component.selectedClassId).toBe(2);
    expect(
      mockAiGraderService.getAssessmentsByClassIdAndAssessmentId,
    ).toHaveBeenCalled();
  });

  it('should navigate to result view on table view action', () => {
    component.selectedClassId = 1;
    component.assessmentPayload = { pageNo: 0, pageSize: 10 };

    component.handleTableAction({
      action: 'view',
      row: { id: 10, full_name: 'Mr. Smith', sharedEmailDate: 'Jan 15, 2026' },
      index: 0,
    } as any);

    expect(mockCacheService.removeFromCache).toHaveBeenCalledWith('resultView');
    expect(mockCacheService.saveJsonData).toHaveBeenCalledWith(
      'resultView',
      jasmine.objectContaining({
        classId: 1,
        assessmentId: 10,
        teacherName: 'Mr. Smith',
      }),
    );
    expect(mockRouter.navigate).toHaveBeenCalledWith([
      'student/grader-results/view',
    ]);
  });

  it('should reload assessments when class filter changes', () => {
    mockAiGraderService.getAssessmentsByClassIdAndAssessmentId.calls.reset();

    component.selectedClassCallBack({ id: 2 });

    expect(component.selectedClassId).toBe(2);
    expect(component.classes?.find((c: any) => c.id === 2)?.panelOpen).toBeTrue();
    expect(
      mockAiGraderService.getAssessmentsByClassIdAndAssessmentId,
    ).toHaveBeenCalled();
  });

  it('should clear assessment when class filter is cleared', () => {
    component.selectedAssessmentId = 5;

    component.selectedClassCallBack(null);

    expect(component.selectedClassId).toBeUndefined();
    expect(component.selectedAssessmentId).toBeNull();
  });

  it('should reload assessments when assessment filter changes', () => {
    component.selectedClassId = 1;
    mockAiGraderService.getAssessmentsByClassIdAndAssessmentId.calls.reset();

    component.selectedAssessmentCallBack({ id: 15 });

    expect(component.selectedAssessmentId).toBe(15);
    expect(
      mockAiGraderService.getAssessmentsByClassIdAndAssessmentId,
    ).toHaveBeenCalled();
  });

  it('should update page and refetch assessments', () => {
    mockAiGraderService.getAssessmentsByClassIdAndAssessmentId.calls.reset();

    component.onPageChange(2);

    expect(component.assessmentPayload.pageNo).toBe(1);
    expect(
      mockAiGraderService.getAssessmentsByClassIdAndAssessmentId,
    ).toHaveBeenCalled();
  });

  it('should populate table row data from assessments', () => {
    expect(component.assessments?.length).toBe(1);
    expect(component.tableConfig.rowData.length).toBe(1);
    expect(component.tableConfig.totalElements).toBe(1);
  });

  it('should enter view mode on class view action', () => {
    const aClass: any = { name: 'Math', viewMode: false };

    component.onAction(aClass, component.actions.VIEW);

    expect(aClass.viewMode).toBeTrue();
    expect(aClass.viewingValue).toBe('Math');
  });
});
