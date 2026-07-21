import {
  Component,
  ElementRef,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { filter, Subscription } from 'rxjs';
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
    private _messageService?: MessageService,
  ) {
    this.graderServiceBasePath = environment.graderServiceBasePath;
    this.showUpgradePLanButton();
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
  isInitialTableLoading = true;
  hasInitialFetchCompleted = false;
  payLoad = {
    pageNo: 0,
    pageSize: 200,
    searchInput: '',
    sort: '1',
  };
  allResults: AiResultsResponse[] = [];
  displayedResults: AiResultsResponse[] = [];
  pageSize: number = 5;
  aiResults: AiResults = {
    assignmentId: null,
    classId: null,
    search: null,
    aiResultSort: null,
  };
  totalElments?: any = 0;
  numberofFiles?: any = 0;
  aiResultResponse: AiResultsResponse[] = [];
  searchFilter: SearchFilterConfig = {
    placeHolder: 'Search',
    height: '45px',
    allowToEmmitWhenInputIsEmpty: true,
  };
  searchKeyword: string = '';
  selectedClassId?: any;
  selectedAssessmentId?: any;
  private eventSource: EventSource | undefined;
  timestamp?: any;
  uploading?: any;
  source?: any = null;
  private readonly initialFetchRetryDelayMs = 2000;
  private readonly initialFetchMaxRetries = 8;
  private initialFetchRetryCount = 0;
  private initialFetchRetryTimer: ReturnType<typeof setTimeout> | null = null;
  currentPage: number = 1;
  private routerSub?: Subscription;
  private previousUrl = '';

  ngOnInit(): void {
    this.previousUrl = this.router.url;
    this.routerSub = this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe((e) => {
        const onResults =
          e.urlAfterRedirects.includes('/ai-grader/results') &&
          !e.urlAfterRedirects.includes('/result/view');
        const fromDetail = this.previousUrl.includes('/result/view');
        const needsRefresh = !!this._cacheService?.getJsonData(
          'graderResultsNeedsRefresh',
        );
        if (
          onResults &&
          this.selectedClassId &&
          this.selectedAssessmentId &&
          (fromDetail || needsRefresh)
        ) {
          if (needsRefresh) {
            this._cacheService?.removeFromCache('graderResultsNeedsRefresh');
          }
          this.fetchResults();
        }
        this.previousUrl = e.urlAfterRedirects;
      });

    this.route.queryParams.subscribe((params) => {
      this.selectedAssessmentId = +params['id'];
      this.selectedClassId = +params['classId'];
      this.uploading = params['uploading'];
      this.numberofFiles = params['numberOfFiles'];
      this.source = params['source'] ? params['source'] : null;

      if (this.selectedAssessmentId && this.selectedClassId) {
        this.selectedAssignment.id = this.selectedAssessmentId;
        this.selectedClass.id = this.selectedClassId;

        this.aiResults.assignmentId = this.selectedAssessmentId;
        this.aiResults.classId = this.selectedClassId;
        if (this._cacheService?.getJsonData('graderResultsNeedsRefresh')) {
          this._cacheService.removeFromCache('graderResultsNeedsRefresh');
        }
        this.fetchResults();
        this.connectResultSSE();
        // if (this.uploading) {
        //   this.connectResultSSE();
        // }
      } else {
        console.warn('Missing assignmentId or classId in query params.');
      }
    });

    this.payLoad.pageNo = this._cacheService.currentPage - 1;
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
  showUpgradePlan = false;
  subscriptionPlanType = SubscriptionPlanType;
  gradedPapers: number = 0;
  allowedPapers?: any = 0;

  onPageChange(page: number) {
    this._cacheService.currentPage = page;
    this.currentPage = page;
    this.payLoad.pageNo = page - 1; // because pageNo is 0-indexed
    this.updateDisplayedResults();
  }

  routeToGraderUploader() {
    if (this.source === 'classes') {
      this.router.navigate(['instructor/ai-grader/classes'], {});
    } else if (this.source === 'assessment') {
      this.router.navigate(['instructor/ai-grader/assessments'], {});
    } else {
      this.router.navigate(['instructor/ai-grader/uploader'], {});
    }
    this._cacheService.currentPage = 1;
  }

  routeToAIGraderUploader() {
    this._router.navigate(['instructor/ai-grader/uploader'], {});
    this._cacheService.currentPage = 1;
  }

  fetchResults(): void {
    if (!this.aiResults.assignmentId || !this.aiResults.classId) {
      console.warn('Assignment ID and Class ID are required.');
      return;
    }

    if (!this.hasInitialFetchCompleted) {
      this.isInitialTableLoading = true;
    }
    this.aiGraderService
      .getClassResult(this.aiResults, this.payLoad)
      .subscribe({
        next: (res) => {
          if (res.status === 200 && res?.data?.aiResultResponseList) {
            this.hasInitialFetchCompleted = true;
            this.isInitialTableLoading = false;
            this.clearInitialFetchRetryTimer();
            this.initialFetchRetryCount = 0;
            this.allResults = [...(res.data.aiResultResponseList ?? [])];
            this.totalElements =
              res.data?.totalElements ?? this.allResults.length;
            this.aiResultResponse = this.allResults;
            this.applyMarksUpdatesFromCache();
            const firstItem = this.allResults[0];
            this.selectedAssignment.title =
              firstItem?.assignmentTitle || this.selectedAssignment.title;
            this.selectedClass.name =
              firstItem?.className || this.selectedClass.name;

            // Update the displayed results for current page
            this.updateDisplayedResults();

            // Keep progress bar in sync while grading is ongoing.
            this.getNoOfPages();

            this.aiResultResponse?.forEach((el?: any) => {
              if (el?.resultStatus == 'INPROCESS') {
                el.valuesLoader = true;
              }
            });
            // this.connectResultSSE();
            // if (
            //   !this.eventSource ||
            //   this.eventSource.readyState === EventSource.CLOSED
            // ) {
            //   this.connectResultSSE();
            // }

            // const hasInProcess = this.aiResultResponse?.some(
            //   (r: any) => r.resultStatus === 'INPROCESS',
            // );
            const isMissingFiles =
              this.aiResultResponse.length < this.numberofFiles;

            if (isMissingFiles) {
              this.startAutoRefresh();
            } else {
              this.stopAutoRefresh();
              // this.eventSource?.close();
            }
          }
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error fetching results', err);
          if (
            err?.status === 404 &&
            this.initialFetchRetryCount < this.initialFetchMaxRetries
          ) {
            this.initialFetchRetryCount += 1;
            this.scheduleInitialFetchRetry();
            return;
          }
          this.hasInitialFetchCompleted = true;
          this.isInitialTableLoading = false;
          this.isLoading = false;
          this.aiResultResponse = [];
          this.allResults = [];
          this.displayedResults = [];
        },
      });
  }

  private refreshInterval: any = null;

  private startAutoRefresh(): void {
    if (this.refreshInterval) return;

    this.refreshInterval = setInterval(() => {
      // const hasInProcess = this.aiResultResponse?.some(
      //   (r: any) => r.resultStatus === 'INPROCESS',
      // );
      const isMissingFiles = this.allResults.length < this.numberofFiles;

      if (isMissingFiles) {
        this.fetchResults();
      } else {
        this.stopAutoRefresh();
        // this.eventSource?.close();
      }
    }, 5000);
  }

  private stopAutoRefresh(): void {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
      this.refreshInterval = null;
    }
  }

  private scheduleInitialFetchRetry(): void {
    this.clearInitialFetchRetryTimer();
    this.initialFetchRetryTimer = setTimeout(() => {
      this.fetchResults();
    }, this.initialFetchRetryDelayMs);
  }

  private clearInitialFetchRetryTimer(): void {
    if (this.initialFetchRetryTimer) {
      clearTimeout(this.initialFetchRetryTimer);
      this.initialFetchRetryTimer = null;
    }
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
        score: selectedStudent?.score,
      },
      nzFooter: null,
      nzWidth: 600,
    });
  }

  enableEdit(row: any, field: 'email' | 'name' | 'rollNumber'): void {
    row[`original${field}`] = row[`student${this.capitalize(field)}`]; // backup
    row[`isEditing${this.capitalize(field)}`] = true;
  }

  cancelEdit(row: any, field: 'email' | 'name' | 'rollNumber'): void {
    row[`student${this.capitalize(field)}`] = row[`original${field}`]; // restore backup
    row[`isEditing${this.capitalize(field)}`] = false;
  }

  saveField(field: 'email' | 'name' | 'rollNumber', row: any): void {
    const value = row[`student${this.capitalize(field)}`]?.trim();

    if (!value) {
      this._messageService.error(`${this.capitalize(field)} cannot be empty.`);
      return;
    }

    if (field === 'email') {
      const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      if (!emailPattern.test(value)) {
        this._messageService.error('Please enter a valid email address.');
        return;
      }
    }

    this.aiGraderService.editField(row.id, field, value).subscribe({
      next: () => {
        row[`isEditing${this.capitalize(field)}`] = false;
      },
      error: (error) => {
        if (error.status === 409) {
          this._messageService.error(
            `${this.capitalize(field)} already exists.`,
          );
        } else {
          this._messageService.error(`Error updating ${field}.`);
        }

        // Revert field and exit edit mode
        row[`student${this.capitalize(field)}`] = row[`original${field}`];
        row[`isEditing${this.capitalize(field)}`] = false;
      },
    });
  }

  // Small helper
  private capitalize(s: string): string {
    return s.charAt(0).toUpperCase() + s.slice(1);
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
        (result) => result.resultStatus === 'APPROVED',
      )
    );
  }

  searchCallBack(value?: any) {
    this.searchKeyword = value.trim();
    if (value && value.length >= 1) {
      this.payLoad.pageNo = 0;
      this.aiResults.search = this.searchKeyword;
      this.fetchResults();
    } else {
      this.clearSearch();
    }
  }

  clearSearch() {
    this.payLoad.pageNo = 0;
    this.aiResults.search = null;
    this.fetchResults();
  }

  resultView(data: any, index?: any) {
    if (data?.resultStatus === 'INPROCESS') {
      return; // stop navigation
    } else if (data?.resultStatus === 'ERROR') {
      data.resultStatus = 'INPROCESS';
      data.valuesLoader = true;

      this.retryGrading(data?.id);
      // return;
    } else {
      const allStudents = this.aiResultResponse.map((x) => ({
        id: x.id,
        status: x.resultStatus,
        grade: x.grade,
        score: x.score,
      }));

      const resultViewData = {
        classId: this.selectedClassId,
        assessmentId: this.selectedAssessmentId,
        resultId: data.id,
        students: allStudents, // <-- store full objects
        index: index,
      };

      this.currentPage = this.payLoad.pageNo + 1;

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
    if (
      this.eventSource &&
      this.eventSource.readyState !== EventSource.CLOSED
    ) {
      return;
    }
    if (this._cacheService.getDataFromCache('isLoggedIn')) {
      if (this.eventSource) {
        this.eventSource.close();
      }

      const uniqueId = this._cacheService.getDataFromCache('unique-id');

      this.timestamp = uniqueId || this.generateTimeStamp();

      this._cacheService.saveInCache('unique-id', this.timestamp);
      const userProfile = JSON.parse(
        this._cacheService.getDataFromCache('userProfile'),
      );

      this.eventSource = new EventSource(
        `${this.graderServiceBasePath}/api/v1/emitter/register?uniqueId=${this.timestamp}&classId=${this.aiResults.classId}&assessmentId=${this.aiResults.assignmentId}&instructorId=${userProfile?.userId}&pageNo=${this.payLoad.pageNo}&pageSize=${this.payLoad.pageSize}`,
      );
      this.eventSource.addEventListener('results', (message) => {
        const response = JSON.parse(message.data);
        const responseMap = new Map(
          this.aiResultResponse?.map((el: any) => [el.id, el]),
        );

        response?.forEach((el: any) => {
          const matched = responseMap.get(el?.id);
          if (matched) {
            // Only update fields if the user is NOT editing them
            if (!matched.isEditingName) {
              matched.studentName = el?.studentName ?? matched.studentName;
            }
            if (!matched.isEditingEmail) {
              matched.studentEmail = el?.studentEmail ?? matched.studentEmail;
            }
            if (!matched.isEditingRollNumber) {
              matched.studentId = el?.studentId ?? matched.studentId;
            }

            matched.grade = el?.grade;
            if (el?.resultStatus !== 'INPROCESS') {
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
      'loggedInUserDetails',
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

  updateDisplayedResults() {
    const startIndex = this.payLoad.pageNo * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedResults = [...this.allResults.slice(startIndex, endIndex)];
  }

  /** Apply marks edited on the detail screen (router state + resultView cache). */
  private applyMarksUpdatesFromCache(): void {
    const navState = history.state?.updatedResult;
    if (navState?.id != null) {
      this.patchResultMarks(navState.id, navState.grade, navState.score);
    }

    const cache = this._cacheService?.getJsonData('resultView');
    if (!cache) {
      return;
    }
    if (cache.lastUpdatedResultId != null) {
      this.patchResultMarks(
        cache.lastUpdatedResultId,
        cache.lastUpdatedGrade,
        cache.lastUpdatedScore,
      );
    }
    if (Array.isArray(cache.students)) {
      cache.students.forEach((student: { id: number; grade?: number; score?: number }) => {
        this.patchResultMarks(student.id, student.grade, student.score);
      });
    }
  }

  private patchResultMarks(
    resultId: number,
    grade?: number,
    score?: number,
  ): void {
    const row = this.allResults?.find((r) => r.id === resultId);
    if (!row) {
      return;
    }
    if (grade !== undefined && grade !== null) {
      row.grade = grade;
    }
    if (score !== undefined && score !== null) {
      row.score = score;
    }
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
    this.clearInitialFetchRetryTimer();
    this.stopAutoRefresh();
    this.eventSource?.close();
  }
}
