import { Actions } from 'src/app/core/enums/actions.enum';
import { GraderAssessmentComponent } from './grader-assessment.component';
import {
  configureInstructorComponentTest,
  createAiGraderServiceSpy,
} from '../../testing/instructor-component.testing';
import { NzModalService } from 'ng-zorro-antd/modal';
import { ViewContainerRef } from '@angular/core';
import { of } from 'rxjs';

describe('GraderAssessmentComponent', () => {
  async function setup() {
    const modal = jasmine.createSpyObj<NzModalService>('NzModalService', [
      'create',
    ]);
    modal.create.and.returnValue({
      afterClose: of(true),
      componentInstance: {},
    } as any);

    return configureInstructorComponentTest(GraderAssessmentComponent, [
      { provide: NzModalService, useValue: modal },
      { provide: ViewContainerRef, useValue: {} },
    ]);
  }

  it('should create and load assessments on init', async () => {
    const { component, aiGraderService } = await setup();

    expect(component).toBeTruthy();
    expect(aiGraderService.getAssessmentsDetails).toHaveBeenCalled();
    expect(aiGraderService.getNoOfPagesUsed).toHaveBeenCalled();
    expect(component.assessments?.length).toBe(1);
    expect(component.tableConfig.rowData.length).toBe(1);
  });

  it('should reload assessments when class filter changes', async () => {
    const { component, aiGraderService } = await setup();
    aiGraderService.getAssessmentsDetails.calls.reset();

    component.selectedClassCallBack({ id: 3 });

    expect(component.selectedClassId).toBe(3);
    expect(aiGraderService.getAssessmentsDetails).toHaveBeenCalledWith(
      { classId: 3, assessmentId: component.selectedAssessmentId },
      component.assessmentPayload,
    );
  });

  it('should clear assessment when class filter is cleared', async () => {
    const { component } = await setup();
    component.selectedAssessmentId = 5;

    component.selectedClassCallBack(null);

    expect(component.selectedClassId).toBeUndefined();
    expect(component.selectedAssessmentId).toBeNull();
  });

  it('should reload assessments when assessment filter changes', async () => {
    const { component, aiGraderService } = await setup();
    component.selectedClassId = 2;
    aiGraderService.getAssessmentsDetails.calls.reset();

    component.selectedAssessmentCallBack({ id: 11 });

    expect(component.selectedAssessmentId).toBe(11);
    expect(aiGraderService.getAssessmentsDetails).toHaveBeenCalledWith(
      { classId: 2, assessmentId: 11 },
      component.assessmentPayload,
    );
  });

  it('should navigate to results on view action', async () => {
    const { component, router } = await setup();

    component.handleTableAction({
      action: Actions.VIEW,
      row: { id: 15, classId: 2 },
    });

    expect(router.navigate).toHaveBeenCalledWith(
      ['instructor/ai-grader/results'],
      {
        queryParams: { id: 15, classId: 2, source: 'assessment' },
      },
    );
  });

  it('should delete assessment on delete action', async () => {
    const { component, aiGraderService } = await setup();
    aiGraderService.deleteAssessment.calls.reset();

    component.handleTableAction({
      action: Actions.DELETE,
      row: { id: 20, classId: 2 },
    });

    expect(aiGraderService.deleteAssessment).toHaveBeenCalledWith(20);
  });

  it('should call editAssessment when saving assessment name', async () => {
    const { component, aiGraderService } = await setup();
    const row = { id: 10, name: 'Updated Quiz' };

    component.editAssessment(row, 10, 'name');

    expect(aiGraderService.editAssessment).toHaveBeenCalledWith(10, 'Updated Quiz');
  });

  it('should route to grader uploader', async () => {
    const { component, router } = await setup();

    component.routeToGraderUploader();

    expect(router.navigate).toHaveBeenCalledWith(
      ['instructor/ai-grader/uploader'],
      {},
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

  it('should expose assessment table columns for queued graded approved', async () => {
    const { component } = await setup();
    const headers = component.tableConfig.columns.map((c) => c.header);

    expect(headers).toContain('Queued');
    expect(headers).toContain('Graded');
    expect(headers).toContain('Approved');
    expect(headers).toContain('Assessment name');
  });
});
