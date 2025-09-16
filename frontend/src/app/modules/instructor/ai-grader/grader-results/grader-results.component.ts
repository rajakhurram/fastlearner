import {
  Component,
  ElementRef,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { error } from '@ant-design/icons-angular';
import { NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { SubscriptionPlanType } from 'src/app/core/enums/subscription-plan.enum';
import { AiResultsResponse } from 'src/app/core/models/ai-results-response.model';
import { AiResults } from 'src/app/core/models/ai-results.model';
import { buttonConfig } from 'src/app/core/models/button.model-config';
import { SearchFilterConfig } from 'src/app/core/models/search-filter-config.model';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SubscriptionPlanComponent } from 'src/app/modules/auth/subscription-plan/subscription-plan.component';
import { ClassUploaderComponent } from 'src/app/modules/dynamic-modals/class-uploader/class-uploader.component';
import { DeletionModalComponent } from 'src/app/modules/dynamic-modals/deletion-modal/deletion-modal.component';
import { environment } from 'src/environments/environment.development';
@Component({
  selector: 'app-grader-results',
  templateUrl: './grader-results.component.html',
  styleUrls: ['./grader-results.component.scss'],
})
export class GraderResultsComponent {
  graderServiceBasePath?: any;
  constructor(
    private modal: NzModalService,
    private aiGraderService: AiGraderService,
    private _viewContainerRef: ViewContainerRef,
    private router: Router,
    private route: ActivatedRoute,
    private _router?: Router,
    private _cacheService?: CacheService,
    private _messageService?: MessageService
  ) {
    this.graderServiceBasePath = environment.graderServiceBasePath;
  }

  @ViewChild('emailEdit') emailInputRef!: ElementRef;
  _httpConstants: HttpConstants = new HttpConstants();
  assessmentButtonConfig: buttonConfig = {
    backgroundColor: '#212189',
  };
  selectedSort: string = '';
  aClass?: any;
  assessment?: any;
  isLoading = true;
  payLoad = {
    pageNo: 0,
    pageSize: 5,
    searchInput: '',
    sort: '1',
  };

  aiResults: AiResults = {
    assignmentId: null,
    classId: null,
    search: null,
    aiResultSort: null,
  };
  totalElments?: any = 0;

  aiResultResponse: AiResultsResponse[] = [];
  searchFilter: SearchFilterConfig = {
    placeHolder: 'Search',
    height: '45px',
  };
  searchKeyword: string = '';
  selectedClassId?: any;
  selectedAssessmentId?: any;
  private eventSource: EventSource | undefined;
  timestamp?: any;
  uploading?: any;

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      this.selectedAssessmentId = +params['id'];
      this.selectedClassId = +params['classId'];
      this.uploading = params['uploading'];

      if (this.selectedAssessmentId && this.selectedClassId) {
        this.selectedAssignment.id = this.selectedAssessmentId;
        this.selectedClass.id = this.selectedClassId;

        this.aiResults.assignmentId = this.selectedAssessmentId;
        this.aiResults.classId = this.selectedClassId;
        this.fetchResults();
      } else {
        console.warn('Missing assignmentId or classId in query params.');
      }
    });

    this.getNoOfPages();
    this.showUpgradePLanButton();
  }
  student = {
    name: '',
    email: '',
  };

  selectedClass = {
    id: null,
    name: '',
  };

  selectedAssignment = {
    id: null,
    title: '',
  };

  totalElements: any = 0;
  showUpgradePlan?: boolean = true;
  subscriptionPlanType = SubscriptionPlanType;
  gradedPapers: number = 0;
  allowedPapers?: any = 0;

  onPageChange(page?: any) {
    this.payLoad.pageNo = page - 1;
    this.fetchResults();
  }

  routeToGraderUploader() {
    this.router.navigate(['instructor/ai-grader/uploader'], {});
  }

  fetchResults(): void {
    if (!this.aiResults.assignmentId || !this.aiResults.classId) {
      console.warn('Assignment ID and Class ID are required.');
      return;
    }

    this.isLoading = true;
    this.aiGraderService
      .getClassResult(this.aiResults, this.payLoad)
      .subscribe({
        next: (res) => {
          if (res.status === 200 && res?.data?.aiResultResponseList) {
            this.totalElements = res?.data?.totalElements;
            this.aiResultResponse = res.data?.aiResultResponseList;
            const firstItem = res?.data?.aiResultResponseList[0];
            this.selectedAssignment.title =
              firstItem.assignmentTitle || this.selectedAssignment.title;
            this.selectedClass.name =
              firstItem.className || this.selectedClass.name;
            this.aiResultResponse?.forEach((el?: any) => {
              if (el?.resultStatus == 'INPROCESS') {
                el.valuesLoader = true;
              }
            });
            this.connectResultSSE();
          }
        },
        error: (err) => {
          console.error('Error fetching results', err);
          this.isLoading = false;
        },
      });
  }

  onSortChange() {
    this.aiResults.aiResultSort = this.selectedSort;
    this.fetchResults();
  }

  exportAiResults() {
    if (!this.aiResults.assignmentId || !this.aiResults.classId) {
      console.warn('Assignment ID and Class ID are required.');
      return;
    }

    this.aiGraderService
      .exportAiResults(this.aiResults, this.payLoad)
      .subscribe({
        next: (response: Blob) => {
          const blob = new Blob([response], {
            type: 'application/octet-stream',
          });
          const url = window.URL.createObjectURL(blob);

          const a = document.createElement('a');
          a.href = url;
          a.download = 'ai-results.xlsx'; // or .pdf, depending on your file
          document.body.appendChild(a);
          a.click();

          // Clean up
          document.body.removeChild(a);
          window.URL.revokeObjectURL(url);
        },
        error: (err) => {
          console.error('Export failed', err);
        },
      });
  }

  openClassModal(selectedStudent: any): void {
    const modalRef = this.modal.create({
      nzContent: ClassUploaderComponent,
      nzComponentParams: {
        studentScore: selectedStudent.grade,
        studentEmail: selectedStudent.studentEmail,
        studentName: selectedStudent.studentName,
        aiResultId: selectedStudent.id,
      },
      nzFooter: null,
      nzWidth: 600,
    });
  }

  enableEdit(row: any): void {
    row.originalEmail = row.studentEmail; // Backup original
    row.isEditingEmail = true; // Enter edit mode
    setTimeout(() => {
      this.emailInputRef?.nativeElement?.focus();
    }, 0);
  }

  cancelEdit(row: any): void {
    row.studentEmail = row.originalEmail; // Revert on blur
    row.isEditingEmail = false;
  }

  saveEmail(event: Event, row: any): void {
    const inputElement = event.target as HTMLInputElement;
    const email = inputElement.value.trim();

    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (!emailPattern.test(email)) {
      this._messageService.error('Please enter a valid email address.');
      return;
    }

    this.aiGraderService.editEmail(row.id, row.studentEmail).subscribe({
      next: () => {
        row.isEditingEmail = false;
      },
      error: () => {
        console.error('Error updating email');
        row.studentEmail = row.originalEmail;
        row.isEditingEmail = false;
      },
    });
  }

  getCourseListOfInstructorBySearch() {
    const searchTerm = this.payLoad.searchInput?.trim();

    if (!searchTerm) {
      this.aiResults.search = null;
      this.fetchResults();
      return;
    }

    const payload = {
      classId: this.selectedClass.id,
      assignmentId: this.selectedAssignment.id,
      search: this.payLoad.searchInput,
    };

    this.aiGraderService.getFilterSearch(payload).subscribe({
      next: (res) => {
        this.aiResultResponse = res.data || [];
      },
      error: (err) => {
        console.error('Error fetching courses:', err);
      },
    });
  }

  deleteResult(student: any) {
    const studentId = student.id;
    const modal = this.modal.create({
      nzContent: DeletionModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      nzComponentParams: {
        msg: 'Are you sure you want to delete the selected result?',
      },
    });

    modal.componentInstance?.deleteClick?.subscribe(() => {
      this.aiGraderService.deleteStudentResult(studentId).subscribe({
        next: (res) => {
          this.fetchResults();
        },
        error: (err) => {
          console.error('Error fetching courses:', err);
        },
      });
    });
  }

  get isAllGraded(): boolean {
    return (
      this.aiResultResponse?.length > 0 &&
      this.aiResultResponse.every(
        (result) => result.resultStatus === 'APPROVED'
      )
    );
  }

  searchCallBack(value?: any) {
    this.searchKeyword = value.trim();
    if (value && value.length >= 1) {
      this.payLoad.pageNo = 0;
      this.aiResults.search = this.searchKeyword;
      this.fetchResults();
    }
  }

  clearSearch() {
    this.payLoad.pageNo = 0;
    this.aiResults.search = null;
    this.fetchResults();
  }

  resultView(data: any, counter?: any) {
    if (data?.resultStatus === 'INPROCESS') {
      return; // stop navigation
    } else if (data?.resultStatus === 'ERROR') {
      data.resultStatus = 'INPROCESS';
      data.valuesLoader = true;

      this.retryGrading(data?.id);
      // return;
    } else {
      const resultViewData = {
        classId: this.selectedClassId,
        assessmentId: this.selectedAssessmentId,
        pageNo: this.payLoad?.pageNo * this.payLoad.pageSize + counter,
      };

      this._cacheService.removeFromCache('resultView');
      this._cacheService.saveJsonData('resultView', resultViewData);
      this._router.navigate(['instructor/ai-grader/result/view']);
    }
  }

  getStatusClass(status: string): string {
    switch ((status || '').toLowerCase()) {
      case 'graded':
        return 'tag-needs-approval';
      case 'approved':
        return 'tag-approved';
      case 'inprocess':
        return 'tag-processing';
      case 'error':
        return 'tag-error';
      default:
        return 'tag-default';
    }
  }

  connectResultSSE(): void {
    if (this._cacheService.getDataFromCache('isLoggedIn')) {
      if (this.eventSource) {
        this.eventSource.close();
      }

      const uniqueId = this._cacheService.getDataFromCache('unique-id');

      this.timestamp = uniqueId || this.generateTimeStamp();

      this._cacheService.saveInCache('unique-id', this.timestamp);
      const userProfile = JSON.parse(
        this._cacheService.getDataFromCache('userProfile')
      );

      this.eventSource = new EventSource(
        `${this.graderServiceBasePath}/api/v1/emitter/register?uniqueId=${this.timestamp}&classId=${this.aiResults.classId}&assessmentId=${this.aiResults.assignmentId}&instructorId=${userProfile?.userId}&pageNo=${this.payLoad.pageNo}&pageSize=${this.payLoad.pageSize}`
      );
      this.eventSource.addEventListener('results', (message) => {
        const response = JSON.parse(message.data);
        console.log(response);
        const responseMap = new Map(
          this.aiResultResponse?.map((el: any) => [el.id, el])
        );

        response?.forEach((el: any) => {
          const matched = responseMap.get(el?.id);
          if (matched) {
            matched.studentName = el?.studentName ?? null;
            matched.studentEmail = el?.studentEmail ?? null;
            matched.studentId = el?.studentId ?? null;
            matched.grade = el?.grade;
            if (el?.resultStatus != 'INPROCESS') {
              matched.resultStatus = el?.resultStatus;
              matched.valuesLoader = false;
            }
          }
        });
      });
    }
  }

  retryGrading(resultId?: any) {
    this.aiGraderService.retryGrading(resultId).subscribe({
      next: (res) => {},
      error: (err) => {
        this._messageService.error(err?.error?.message);
      },
    });
  }

  generateTimeStamp() {
    return new Date().getTime();
  }

  getStatusLabel(status: string): string {
    switch ((status || '').toLowerCase()) {
      case 'graded':
        return 'Graded';
      case 'approved':
        return 'Approved';
      case 'inprocess':
        return 'Processing';
      case 'error':
        return 'Error';
      default:
        return 'Pending';
    }
  }

  getNoOfPages() {
    this.aiGraderService.getNoOfPagesUsed().subscribe({
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

  openSubscriptionPlan(): void {
    const modal = this.modal.create({
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
}
