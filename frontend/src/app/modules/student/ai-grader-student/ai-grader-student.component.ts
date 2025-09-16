import { Component, ViewChild } from '@angular/core';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { Actions } from 'src/app/core/enums/actions.enum';
import { AIClass } from 'src/app/core/models/ai-class.model';
import { AIAssessment } from 'src/app/core/models/assessment.model';
import { buttonConfig } from 'src/app/core/models/button.model-config';
import { TableConfig } from 'src/app/core/models/table.model-config';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ClassAssessmentDropdownComponent } from '../../shared/class-assessment-dropdown/class-assessment-dropdown.component';
import { CacheService } from 'src/app/core/services/cache.service';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-ai-grader-student',
  templateUrl: './ai-grader-student.component.html',
  styleUrls: ['./ai-grader-student.component.scss'],
})
export class AiGraderStudentComponent {
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

  classes?: AIClass[] = [];
  assessments?: AIAssessment[] = [];

  tableConfig: TableConfig = {
    columns: [
      {
        header: 'Date',
        field: 'sharedEmailDate',
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Assessment',
        field: 'name',
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Score',
        field: 'grade',
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Teacher name',
        field: 'full_name',
        tooltip: true,
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Action',
        field: 'action',
        actions: true,
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
    ],
    rowData: [],
    headerColor: '#f5f5f5',
    rowColor: '#ffffff',
    paginated: true,
    pageNo: 0,
    pageSize: 10,
    totalElements: 1,
    itemsPerPage: false,
  };

  selectedClassId?: any = null;
  selectedAssessmentId?: any = null;
  classPayload?: any = {
    pageNo: 0,
    pageSize: 10,
  };

  assessmentPayload?: any = {
    pageNo: 0,
    pageSize: 10,
  };

  actions = Actions;
  totalElements?: any = 1;

  constructor(
    private _aiGraderService: AiGraderService,
    private _router: Router,
    private route: ActivatedRoute,
    private _cacheService?: CacheService,
    private datePipe?: DatePipe
  ) {}

  ngOnInit(): void {
    this.getClasses();
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
        this.selectedClassId = aClass?.id;
        aClass.panelOpen = true;
        this.getAssessmentsByClassIdAndAssessmentId();
      }
    }
  }

  closeAllPanel() {
    this.classes?.forEach((aClass?: any) => {
      aClass.panelOpen = false;
    });
  }

  handleTableAction(event: { event; action: string; row: any; index?: any }) {
    if (event.action === 'view') {
      console.log(event);
      // this.router.navigate([event.row.id], { relativeTo: this.route });
      const resultViewData = {
        classId: this.selectedClassId,
        assessmentId: event?.row?.id,
        teacherName: event?.row?.full_name,
        dateGraded: event?.row?.sharedEmailDate,
        pageNo:
          this.assessmentPayload?.pageNo * this.assessmentPayload.pageSize +
          event.index,
      };
      this._cacheService.removeFromCache('resultView');
      this._cacheService.saveJsonData('resultView', resultViewData);
      this._router.navigate(['student/grader-results/view']);
    }
  }

  selectedClassCallBack(event?: any) {
    this.selectedClassId = event?.id;
    if (this.selectedClassId == null) {
      this.selectedAssessmentId = null;
    }
    this.classPayload.classId = this.selectedClassId;
    this.getClasses();
  }

  selectedAssessmentCallBack(event?: any) {
    this.selectedAssessmentId = event != null ? event?.id : null;
    this.getAssessmentsByClassIdAndAssessmentId();
  }

  getClasses(): void {
    this._aiGraderService.getClassesStudent(this.classPayload)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.classes = response?.data?.aiClasses;
          this.classes[0].panelOpen = true;
          this.selectedClassId = this.classes[0]?.id;
          this.getAssessmentsByClassIdAndAssessmentId();
        }
      },
      error: (error: any) => {},
    });
  }

  getAssessmentsByClassIdAndAssessmentId(): void {
    this._aiGraderService
      .getAssessmentsByClassIdAndAssessmentId(
        {
          classId: this.selectedClassId,
          assessmentId: this.selectedAssessmentId,
          studEmail: JSON.parse(
            this._cacheService.getDataFromCache('userProfile')
          ).email,
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
            rawData?.forEach((el?: any) => {
              el.sharedEmailDate = this.datePipe.transform(
                el.sharedEmailDate,
                'MMM d, y'
              );
            });
            this.assessments = rawData.map((item: any) => ({
              ...item,
              showViewIcon: true,
            }));
            this.totalElements = response?.data?.totalElements;
            this.tableConfig.totalElements = this.totalElements;
            this.tableConfig.rowData = this.assessments;
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
    if (action == this.actions.VIEW) {
      aClass.viewMode = true;
      aClass.viewingValue = aClass.name;
    }
  }

  onPageChange(page?: any) {
    this.assessmentPayload.pageNo = page - 1;
    this.getAssessmentsByClassIdAndAssessmentId();
  }
}
