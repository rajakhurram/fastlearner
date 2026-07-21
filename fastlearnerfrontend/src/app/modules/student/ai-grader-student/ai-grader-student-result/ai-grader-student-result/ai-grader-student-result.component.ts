import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { Location } from '@angular/common';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AiResultsResponse } from 'src/app/core/models/ai-results-response.model';
import { MessageService } from 'src/app/core/services/message.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { AIResultQuestion } from 'src/app/core/models/result-question.model';

@Component({
  selector: 'app-ai-grader-student-result',
  templateUrl: './ai-grader-student-result.component.html',
  styleUrls: ['./ai-grader-student-result.component.scss'],
})
export class AiGraderStudentResultComponent {
  _httpConstants: HttpConstants = new HttpConstants();
  constructor(
    private route: ActivatedRoute,
    private _aiGraderService: AiGraderService,
    private location: Location,
    private router: Router,
    private _messageService?: MessageService,
    private _cacheService?: CacheService,
  ) {}

  resultViewData?: any;
  resultId?: any;
  result: AiResultsResponse = null;
  totalPages?: any;
  questionTotalPages?: any;
  questions?: AIResultQuestion[] = [];
  loadingMoreQuestions?: boolean = false;
  showRubricModal = false;
  resultPayload = {
    pageNo: 0,
    pageSize: 1,
  };
  questionPayload = {
    pageNo: 0,
    pageSize: 10,
  };

  ngOnInit(): void {
    this.resultViewData = this._cacheService.getJsonData('resultView');

    if (!this.resultViewData) {
      const assessmentId =
        this.route.snapshot.queryParamMap.get('assessmentId');
      const classId = this.route.snapshot.queryParamMap.get('classId');

      if (assessmentId && classId) {
        this.resultViewData = {
          assessmentId: +assessmentId,
          classId: +classId,
          pageNo: 1,
        };
        this._cacheService.saveJsonData('resultView', this.resultViewData);
      }
    }
    if (this.resultViewData) {
      this.resultPayload.pageNo = this.resultViewData?.pageNo - 1 || 0;
      this.getResultByClassAndAssessmentId();
    } else {
      this._messageService?.error('Missing result information. Please retry.');
    }
  }

  private getStudentEmail(): string | null {
    // Prefer full user profile if available
    const profileRaw = this._cacheService?.getDataFromCache('userProfile');
    if (profileRaw) {
      try {
        return JSON.parse(profileRaw)?.email ?? null;
      } catch {}
    }

    // Fallback to loggedInUserDetails (always set right after login, including Google)
    const loggedRaw = this._cacheService?.getDataFromCache(
      'loggedInUserDetails',
    );
    if (loggedRaw) {
      try {
        return JSON.parse(loggedRaw)?.email ?? null;
      } catch {}
    }

    return null;
  }

  getResultByClassAndAssessmentId() {
    const studEmail = this.getStudentEmail();
    if (!studEmail) {
      this._messageService?.error(
        'Unable to load result. Please refresh and try again.',
      );
      return;
    }

    this._aiGraderService
      .getResultByClassAndAssessmentId(
        {
          classId: this.resultViewData?.classId,
          assignmentId: this.resultViewData?.assessmentId,
          studEmail,
        },
        this.resultPayload,
      )
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.result = response?.data?.aiResultResponseList[0];
            this.resultId = this.result?.id;
            this.totalPages = response?.data?.pages;
            this.getResultQuestions();
          }
        },
        error: (error: any) => {
          this.resultPayload.pageNo -= 1;
          this._messageService.error(error?.error?.message);
        },
      });
  }

  getResultQuestions(append?: boolean): void {
    this._aiGraderService
      .getResultQuestions(this.resultId, this.questionPayload)
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            if (append) {
              this.questions = append
                ? [
                    ...this.questions,
                    ...response?.data?.aiResultQueResponseList,
                  ]
                : (response?.data?.aiResultQueResponseList ?? []);
              this.loadingMoreQuestions = false;
            } else {
              this.questions = response?.data?.aiResultQueResponseList;
              this.questionTotalPages = response?.data?.pages;
              // this.questions[0].panelOpen = true;
            }
          }
        },
        error: (error: any) => {
          this.questions = [];
        },
      });
  }

  onScroll(event: Event): void {
    const element = event.target as HTMLElement;

    const scrollTop = element.scrollTop;
    const scrollHeight = element.scrollHeight;
    const offsetHeight = element.offsetHeight;

    // Trigger load more when scrolled near bottom (e.g., 50px before)
    const threshold = 10;

    if (
      scrollTop + offsetHeight >= scrollHeight - threshold &&
      !this.loadingMoreQuestions &&
      this.questionPayload?.pageNo + 1 < this.questionTotalPages
    ) {
      this.loadingMoreQuestions = true;
      this.questionPayload.pageNo++;
      this.getResultQuestions(true);
    }
  }

  get hasRubric(): boolean {
    return !!this.result?.rubricUrl?.trim();
  }

  closeRubricModal(): void {
    this.showRubricModal = false;
  }

  goBack() {
    this._cacheService?.saveJsonData('studentGraderResultsNeedsRefresh', true);
    this.router.navigate(['/student/grader-results']);
  }
}
