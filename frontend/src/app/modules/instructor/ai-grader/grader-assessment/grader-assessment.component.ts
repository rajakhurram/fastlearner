import { Component, ViewChild, ViewContainerRef } from '@angular/core';
import { Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { Actions } from 'src/app/core/enums/actions.enum';
import { AIClass } from 'src/app/core/models/ai-class.model';
import { AIAssessment } from 'src/app/core/models/assessment.model';
import { buttonConfig } from 'src/app/core/models/button.model-config';
import { TableConfig } from 'src/app/core/models/table.model-config';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { ClassAssessmentDropdownComponent } from 'src/app/modules/shared/class-assessment-dropdown/class-assessment-dropdown.component';
import { NzModalService } from 'ng-zorro-antd/modal';
import { DeletionModalComponent } from '../../../dynamic-modals/deletion-modal/deletion-modal.component';
import { SubscriptionPlanComponent } from 'src/app/modules/auth/subscription-plan/subscription-plan.component';
import { CacheService } from 'src/app/core/services/cache.service';
import { SubscriptionPlanType } from 'src/app/core/enums/subscription-plan.enum';

@Component({
  selector: 'app-grader-assessment',
  templateUrl: './grader-assessment.component.html',
  styleUrls: ['./grader-assessment.component.scss'],
})
export class GraderAssessmentComponent {
  _httpConstants: HttpConstants = new HttpConstants();
  @ViewChild(ClassAssessmentDropdownComponent)
  dropdownComponent?: ClassAssessmentDropdownComponent;
  assessmentButtonConfig: buttonConfig = {
    backgroundColor: '#212189',
  };

  tableConfig: TableConfig = {
    columns: [
      {
        header: 'Assessment name',
        field: 'name',
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Class name',
        field: 'className',
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Queued',
        field: 'process',
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Graded',
        field: 'graded',
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Approved',
        field: 'approved',
        tooltip: true,
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Actions',
        field: 'actions',
        actions: true,
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
    ],
    rowData: [
      {
        assessmentName: 'Assessment 1',
        queued: 2,
        graded: 4,
        approved: 1,
        showEditIcon: true,
        showDeleteIcon: true,
      },
      {
        assessmentName: 'Assessment 2',
        queued: 3,
        graded: 5,
        approved: 2,
        showEditIcon: true,
        showDeleteIcon: true,
      },
    ],
    clickableKeys: ['name'],
    headerColor: '#f5f5f5',
    rowColor: '#ffffff',
    paginated: true,
    pageNo: 0,
    pageSize: 10,
    totalElements: 0,
    itemsPerPage: false,
  };
  actions = Actions;
  gradedPapers: number = 0;
  allowedPapers?: any = 0;
  maxPapers = 500;
  selectedClassId?: any = null;
  selectedAssessmentId?: any = null;
  classPayload?: any = {
    classId: null,
    pageNo: 0,
    pageSize: 10,
  };

  assessmentPayload?: any = {
    pageNo: 0,
    pageSize: 10,
  };
  classes?: AIClass[] = [];
  assessments?: AIAssessment[] = [];
  showUpgradePlan?: boolean = true;
  subscriptionPlanType = SubscriptionPlanType;

  constructor(
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef,
    private _aiGraderService?: AiGraderService,
    private _router?: Router,
    private _cacheService?: CacheService
  ) {}

  ngOnInit(): void {
    // this.getClasses();
    this.getAssessmentsDetails(this.selectedClassId, this.selectedAssessmentId);
    this.getNoOfPages();
    this.showUpgradePLanButton();
  }

  showUpgradePLanButton() {
    const planType: string = this._cacheService.getJsonData(
      'loggedInUserDetails'
    )?.subscriptionPlanType;

    if (
      planType &&
      (planType.toLowerCase() ===
        this.subscriptionPlanType.PREMIUM.toLowerCase() ||
        planType.toLowerCase() ===
          this.subscriptionPlanType.ULTIMATE.toLowerCase())
    ) {
      this.showUpgradePlan = false;
    } else {
      this.showUpgradePlan = true;
    }
  }

  routeToGraderUploader() {
    this._router.navigate(['instructor/ai-grader/uploader'], {});
  }

  onAction(action?: any) {}

  handleTableAction(event?: any) {
    if (event?.action == this.actions.EDIT) {
      this.assessments?.forEach((assessment) => {
        if (assessment?.id == event?.row?.id) {
          assessment.editableKey.push('name');
        }
      });
      this.tableConfig.rowData = this.assessments;
    } else if (event?.action === this.actions.DELETE) {
      this.openDeleteModal(() =>
        this.deleteAssessment(event?.row?.id, event?.row?.classId)
      );
    } else if (event?.action == this.actions.VIEW) {
      this._router.navigate(['instructor/ai-grader/results'], {
        queryParams: {
          id: event?.row?.id,
          classId: event?.row?.classId,
        },
      });
    }
  }

  deleteAssessment(assessmentId?: any, classId?: any) {
    this._aiGraderService.deleteAssessment(assessmentId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.assessments = [];
          this.tableConfig.rowData = [];
          this.getAssessmentsDetails(
            this.selectedClassId,
            this.selectedAssessmentId
          );
          setTimeout(() => {
            this.dropdownComponent?.reloadData({
              base: 'assessment',
              action: this.actions.DELETE,
              id: assessmentId,
            });
          }, 100);
        }
      },
      error: (error: any) => {},
    });
  }

  selectedClassCallBack(event?: any) {
    this.selectedClassId = event?.id;
    if (this.selectedClassId == null) {
      this.selectedAssessmentId = null;
    }
    this.classPayload.classId = this.selectedClassId;
    this.getAssessmentsDetails(this.selectedClassId, this.selectedAssessmentId);
  }

  selectedAssessmentCallBack(event?: any) {
    this.selectedAssessmentId = event != null ? event?.id : null;
    this.getAssessmentsDetails(this.selectedClassId, this.selectedAssessmentId);
  }

  getClasses(): void {
    this._aiGraderService.getClasses(this.classPayload)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.classes = response?.data?.aiClasses;
          this.classes[0].panelOpen = true;
          this.selectedClassId = this.classes[0]?.id;
          this.getAssessmentsDetails(
            this.selectedClassId,
            this.selectedAssessmentId
          );
        }
      },
      error: (error: any) => {},
    });
  }

  getAssessmentsDetails(classId?: any, assessmentId?: any): void {
    this._aiGraderService
      .getAssessmentsDetails(
        {
          classId: classId,
          assessmentId: assessmentId,
        },
        this.assessmentPayload
      )
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            const rawData = response?.data?.data || [];
            this.assessments = rawData.map((item: any) => ({
              ...item,
              showEditIcon: true,
              showDeleteIcon: true,
              editable: false,
              editableKey: [],
            }));
            this.tableConfig.totalElements = response?.data?.totalElements;
            this.tableConfig.rowData = this.assessments;
          }
        },
        error: (error: any) => {
          this.assessments = [];
          this.tableConfig.totalElements = 0;
          this.tableConfig.rowData = this.assessments;
        },
      });
  }

  editValue(event?: any) {
    this.editAssessment(event?.value, event?.field);
  }

  editAssessment(assessment?: any, field?: any) {
    this._aiGraderService
      .editAssessment(assessment?.id, assessment?.name)
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.assessments.forEach((assessment) => {
              if (assessment?.id == assessment?.id) {
                assessment.name = assessment?.name;
                assessment.editableKey = assessment.editableKey.filter(
                  (k?: any) => k !== field
                );
              }
            });

            setTimeout(() => {
              this.dropdownComponent?.reloadData({
                base: 'assessment',
                action: this.actions.EDIT,
                id: assessment?.id,
              });
            }, 100);

            this.tableConfig.rowData = this.assessments;
          }
        },
        error: (error: any) => {},
      });
  }

  onAssessmentPageChange(event?: any) {
    this.assessmentPayload.pageNo = event - 1;
    this.getAssessmentsDetails();
  }

  openDeleteModal(onConfirm: () => void) {
    const modal = this._modal.create({
      nzContent: DeletionModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: false,
      nzWidth: '40%',
      nzComponentParams: {
        msg: 'Are you sure you want to delete the selected Assessment?',
        secondBtnText: 'Delete',
      },
    });

    modal.afterClose.subscribe((result) => {
      if (result === true) {
        onConfirm();
      }
    });
  }
  openSubscriptionPlan(): void {
    const modal = this._modal.create({
      nzContent: SubscriptionPlanComponent,
      nzComponentParams: {
        fromSubscriptionPlan: true,
        showFreePlan: false,
        showStandardPlan: false,
      },
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: this.fullWidth ? '80%' : '100%',
      nzWidth: '80%',
    });
    modal.afterClose?.subscribe((result) => {
      // this.subscriptionModalOpened = false;
    });
  }

  getNoOfPages() {
    this._aiGraderService.getNoOfPagesUsed().subscribe({
      next: (res) => {
        this.gradedPapers = res?.data.noOfPagesUsed
          ? res?.data.noOfPagesUsed
          : 0;
        this.allowedPapers = res?.data.allowedPages
          ? res?.data.allowedPages
          : 0;
      },
      error: (err) => console.error('Error creating assessment:', err),
    });
  }
}
