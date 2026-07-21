import { NzModalService } from 'ng-zorro-antd/modal';
import { of } from 'rxjs';
import { ViewContainerRef } from '@angular/core';
import { Actions } from 'src/app/core/enums/actions.enum';
import { GraderClassesComponent } from './grader-classes.component';
import {
  configureInstructorComponentTest,
  createAiGraderServiceSpy,
} from '../../testing/instructor-component.testing';
describe('GraderClassesComponent', () => {
  async function setup() {
    const modal = jasmine.createSpyObj<NzModalService>('NzModalService', [
      'create',
    ]);
    modal.create.and.returnValue({
      afterClose: of(true),
      componentInstance: {},
    } as any);

    return configureInstructorComponentTest(GraderClassesComponent, [
      { provide: NzModalService, useValue: modal },
      { provide: ViewContainerRef, useValue: {} },
    ]);
  }

  it('should create and load classes on init', async () => {
    const { component, aiGraderService } = await setup();

    expect(component).toBeTruthy();
    expect(aiGraderService.getClasses).toHaveBeenCalled();
    expect(aiGraderService.getNoOfPagesUsed).toHaveBeenCalled();
  });

  it('should expand all class panels', async () => {
    const { component } = await setup();
    component.classes = [{ panelOpen: false }, { panelOpen: false }] as any;

    component.expandAll();

    expect(component.classes?.every((c) => c.panelOpen)).toBeTrue();
  });

  it('should toggle panel and load assessments', async () => {
    const { component, aiGraderService } = await setup();
    const aClass = { id: 2, panelOpen: false };
    component.classes = [aClass] as any;
    aiGraderService.getAssessmentsByClassIdAndAssessmentId.calls.reset();

    component.onPanelClick(aClass, true);

    expect(aClass.panelOpen).toBeTrue();
    expect(
      aiGraderService.getAssessmentsByClassIdAndAssessmentId,
    ).toHaveBeenCalled();
  });

  it('should reload classes when class filter changes', async () => {
    const { component, aiGraderService } = await setup();
    aiGraderService.getClasses.calls.reset();

    component.selectedClassCallBack({ id: 9 });

    expect(component.selectedClassId).toBe(9);
    expect(component.classPayload.classId).toBe(9);
    expect(aiGraderService.getClasses).toHaveBeenCalled();
  });

  it('should clear assessment when class filter is cleared', async () => {
    const { component } = await setup();

    component.selectedClassCallBack(null);

    expect(component.selectedClassId).toBeUndefined();
    expect(component.selectedAssessmentId).toBeNull();
  });

  it('should reload assessments when assessment filter changes', async () => {
    const { component, aiGraderService } = await setup();
    component.selectedClassId = 3;
    aiGraderService.getAssessmentsByClassIdAndAssessmentId.calls.reset();

    component.selectedAssessmentCallBack({ id: 11 });

    expect(component.selectedAssessmentId).toBe(11);
    expect(
      aiGraderService.getAssessmentsByClassIdAndAssessmentId,
    ).toHaveBeenCalledWith(
      jasmine.objectContaining({ classId: 3, assessmentId: 11 }),
      component.assessmentPayload,
    );
  });

  it('should enter edit mode on class edit action', async () => {
    const { component } = await setup();
    const aClass = { name: 'Science', editMode: false };

    component.onAction(aClass, Actions.EDIT);

    expect(aClass.editMode).toBeTrue();
    expect((aClass as any).editingValue).toBe('Science');
  });

  it('should edit class when value is valid', async () => {
    const { component, aiGraderService } = await setup();
    const aClass = { id: 4, editingValue: '  Physics  ', editMode: true };

    component.editClassValue(aClass);

    expect(aiGraderService.editClass).toHaveBeenCalledWith(4, '  Physics  ');
    expect(aClass.editMode).toBeFalse();
  });

  it('should not edit class when value is empty', async () => {
    const { component, aiGraderService } = await setup();
    const aClass = { id: 4, editingValue: '   ', editMode: true };

    component.editClassValue(aClass);

    expect(aiGraderService.editClass).not.toHaveBeenCalled();
  });

  it('should navigate to results on view action', async () => {
    const { component, router } = await setup();
    const aClass = { id: 2 };
    const event = { action: Actions.VIEW, row: { id: 15 } };

    component.handleTableAction(event, aClass);

    expect(router.navigate).toHaveBeenCalledWith(
      ['instructor/ai-grader/results'],
      {
        queryParams: { id: 15, classId: 2, source: 'classes' },
      },
    );
  });

  it('should delete assessment on delete action', async () => {
    const { component, aiGraderService } = await setup();
    const aClass = { id: 2 };
    aiGraderService.deleteAssessment.calls.reset();

    component.handleTableAction(
      { action: Actions.DELETE, row: { id: 20 } },
      aClass,
    );

    expect(aiGraderService.deleteAssessment).toHaveBeenCalledWith(20);
  });

  it('should update page and reload classes', async () => {
    const { component, aiGraderService } = await setup();
    aiGraderService.getClasses.calls.reset();

    component.onClassPageChange(3);

    expect(component.classPayload.pageNo).toBe(2);
    expect(aiGraderService.getClasses).toHaveBeenCalled();
  });

  it('should route to grader uploader', async () => {
    const { component, router } = await setup();

    component.routeToGraderUploader();

    expect(router.navigate).toHaveBeenCalledWith(
      ['instructor/ai-grader/uploader'],
      {},
    );
  });

  it('should hide upgrade button for ultimate plan', async () => {
    const { component, cacheService } = await setup();
    cacheService.getJsonData.and.returnValue({
      subscriptionPlanType: 'ULTIMATE',
    });

    component.showUpgradePLanButton();

    expect(component.showUpgradePlan).toBeFalse();
  });

  it('should load paper usage stats', async () => {
    const { component } = await setup();

    component.getNoOfPages();

    expect(component.gradedPapers).toBe(5);
    expect(component.allowedPapers).toBe(100);
  });

  it('should cancel class edit mode', async () => {
    const { component } = await setup();
    const aClass = { editMode: true };

    component.cancelClassEdit(aClass);

    expect(aClass.editMode).toBeFalse();
  });
});
