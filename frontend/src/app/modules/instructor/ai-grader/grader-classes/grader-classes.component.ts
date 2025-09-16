import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { Actions } from 'src/app/core/enums/actions.enum';
import { AIClass } from 'src/app/core/models/ai-class.model';
import { AIAssessment } from 'src/app/core/models/assessment.model';
import { buttonConfig } from 'src/app/core/models/button.model-config';
import { TableConfig } from 'src/app/core/models/table.model-config';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { Router } from '@angular/router';
import { ClassAssessmentDropdownComponent } from 'src/app/modules/shared/class-assessment-dropdown/class-assessment-dropdown.component';
import { MessageService } from 'src/app/core/services/message.service';
import { TableComponent } from 'src/app/modules/shared/table/table.component';
import { NzModalService } from 'ng-zorro-antd/modal';
import { DeletionModalComponent } from '../../../dynamic-modals/deletion-modal/deletion-modal.component';
import { SubscriptionPlanComponent } from 'src/app/modules/auth/subscription-plan/subscription-plan.component';
import { CacheService } from 'src/app/core/services/cache.service';
import { SubscriptionPlanType } from 'src/app/core/enums/subscription-plan.enum';

@Component({
  selector: 'app-grader-classes',
  templateUrl: './grader-classes.component.html',
  styleUrls: ['./grader-classes.component.scss'],
})
export class GraderClassesComponent {
  @ViewChild('tableRef') tableRef!: TableComponent;
  @ViewChild('classEdit') classEditInputRef!: ElementRef;
  _httpConstants: HttpConstants = new HttpConstants();
  @ViewChild(ClassAssessmentDropdownComponent)
  dropdownComponent?: ClassAssessmentDropdownComponent;
  assessmentButtonConfig: buttonConfig = {
    backgroundColor: '#212189',
  };

  expandAllButtonConfig: buttonConfig = {
    backgroundColor: '#FFFFFF',
    color: '#0052F5',
  };
  gradedPapers?: number = 0;
  allowedPapers?: any = 0;
  maxPapers = 500;
  classes?: AIClass[] = [];
  assessments?: AIAssessment[] = [];

  tableConfig: TableConfig = {
    columns: [
      {
        header: 'Assessment name',
        field: 'name',
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
    rowData: [],
    clickableKeys: ['name'],
    headerColor: '#f5f5f5',
    rowColor: '#ffffff',
    paginated: true,
    pageNo: 0,
    pageSize: 5,
    totalElements: 0,
    itemsPerPage: false,
  };

  selectedClassId?: any = null;
  selectedAssessmentId?: any = null;
  classPayload?: any = {
    classId: null,
    pageNo: 0,
    pageSize: 10,
    totalElements: 0,
  };

  assessmentPayload?: any = {
    pageNo: 0,
    pageSize: 5,
    totalElements: 0,
  };

  actions = Actions;
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
    this.getClasses();
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

  expandAll() {
    this.classes?.forEach((classItem: any) => {
      classItem.panelOpen = true;
    });
  }

  onPanelClick(aClass?: any, allow?: boolean) {
    if (allow) {
      aClass.panelOpen = !aClass?.panelOpen;

      if (aClass.panelOpen) {
        this.closeAllPanel();
        this.assessments = [];
        this.assessmentPayload.pageNo = 0;
        this.tableConfig.rowData = this.assessments;
        aClass.panelOpen = true;
        this.getAssessmentsByClassIdAndAssessmentId(aClass?.id, null);
      }
    }
  }

  closeAllPanel() {
    this.classes?.forEach((aClass?: any) => {
      aClass.panelOpen = false;
    });
  }

  selectedClassCallBack(event?: any) {
    this.selectedClassId = event?.id;
    if (this.selectedClassId == null) {
      this.selectedAssessmentId = null;
    }
    this.classPayload.classId = this.selectedClassId;
    this.classPayload.pageNo = 0;
    this.classes = [];
    this.assessments = [];
    this.getClasses();
  }

  selectedAssessmentCallBack(event?: any) {
    this.selectedAssessmentId = event != null ? event?.id : null;
    this.assessments = [];
    this.assessmentPayload.pageNo = 0;
    this.getAssessmentsByClassIdAndAssessmentId(
      this.selectedClassId,
      this.selectedAssessmentId
    );
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
          this.classPayload.totalElements = response?.data?.totalElements;
          this.getAssessmentsByClassIdAndAssessmentId(
            this.classes[0]?.id,
            null
          );
        }
      },
      error: (error: any) => {},
    });
  }

  getAssessmentsByClassIdAndAssessmentId(
    classId?: any,
    assessmentId?: any
  ): void {
    this._aiGraderService
      .getAssessmentsByClassIdAndAssessmentId(
        {
          classId: classId,
          assessmentId: assessmentId,
          studEmail: null,
        },
        this.assessmentPayload
      )
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            const rawData =
              response?.data?.assessmentStatusCountResponses || [];
            this.assessments = rawData.map((item: any) => ({
              ...item,
              showEditIcon: true,
              showDeleteIcon: true,
              editable: false,
              editableKey: [],
            }));
            Object.assign(this.tableConfig, {
              paginated: true,
              totalElements: response?.data?.totalElements ?? 0,
              rowData: this.assessments ?? [],
            });
          }
        },
        error: (error: any) => {
          Object.assign(this.tableConfig, {
            paginated: false,
            rowData: [],
          });
          // this._messageService.error(error?.error?.message);
        },
      });
  }

  onAction(aClass?: any, action?: any) {
    if (action === this.actions.EDIT) {
      aClass.editMode = true;
      aClass.editingValue = aClass.name;
      setTimeout(() => {
        this.classEditInputRef?.nativeElement?.focus();
      }, 0);
    } else if (action === this.actions.DELETE) {
      this.openDeleteModal(() => this.deleteClass(aClass?.id));
    }
  }

  editClassValue(aClass?: any) {
    if (
      aClass?.editingValue != undefined &&
      aClass?.editingValue != null &&
      aClass?.editingValue?.length > 0
    ) {
      this.editClass(aClass?.id, aClass.editingValue);
    }
    aClass.editMode = false;
  }

  editClass(classId?: any, editValue?: any) {
    this._aiGraderService.editClass(classId, editValue)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          const targetClass = this.classes?.find(
            (cls: any) => cls.id === classId
          );
          if (targetClass) {
            targetClass.name = editValue;
          }
          setTimeout(() => {
            this.dropdownComponent?.reloadData({
              base: 'class',
              action: this.actions.EDIT,
              id: classId,
            });
          }, 100);
        }
      },
      error: (error: any) => {},
    });
  }

  deleteClass(classId?: any) {
    this._aiGraderService.deleteClass(classId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.classPayload.pageNo = 0;
          this.classes = [];
          this.getClasses();
          setTimeout(() => {
            this.dropdownComponent?.reloadData({
              base: 'class',
              action: this.actions.DELETE,
              id: classId,
            });
          }, 100);
        }
      },
      error: (error: any) => {},
    });
  }

  handleTableAction(event?: any, aClass?: any) {
    if (event?.action === this.actions.EDIT) {
      this.assessments?.forEach((assessment) => {
        if (assessment?.id === event?.row?.id) {
          if (assessment?.editableKey?.length > 0) {
            assessment.editableKey = [];
          } else {
            assessment.editableKey = ['name'];
          }
        }
      });

      this.tableConfig.rowData = this.assessments;

      // setTimeout(() => {
      //   this.tableRef?.focusFirstEditableInput(); // 👈 call focus after render
      // }, 200);
    } else if (event?.action == this.actions.DELETE) {
      this.deleteAssessment(event?.row?.id, aClass);
    } else if (event?.action == this.actions.VIEW) {
      this._router.navigate(['instructor/ai-grader/results'], {
        queryParams: {
          id: event?.row?.id,
          classId: aClass?.id,
        },
      });
    }
  }

  deleteAssessment(assessmentId?: any, aClass?: any) {
    this._aiGraderService.deleteAssessment(assessmentId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.assessments = [];
          this.tableConfig.rowData = [];
          this.getAssessmentsByClassIdAndAssessmentId(aClass?.id, assessmentId);
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

  onClassPageChange(event?: any) {
    this.classPayload.pageNo = event - 1;
    this.getClasses();
  }

  onAssessmentPageChange(aClass?: any, event?: any) {
    this.assessmentPayload.pageNo = event - 1;
    this.getAssessmentsByClassIdAndAssessmentId(aClass?.id, null);
  }

  cancelClassEdit(aClass?: any) {
    aClass.editMode = false;
  }

  openDeleteModal(onConfirm: () => void) {
    const modal = this._modal.create({
      nzContent: DeletionModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: false,
      nzWidth: '40%',
      nzComponentParams: {
        msg: 'Are you sure you want to delete the selected Class?',
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
