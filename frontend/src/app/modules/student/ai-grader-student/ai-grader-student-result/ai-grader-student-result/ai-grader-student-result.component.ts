import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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
    private _messageService?: MessageService,
    private _cacheService?: CacheService
  ) {}

  resultViewData?: any;
  resultId?: any;
  result: AiResultsResponse = null;
  totalPages?: any;
  questionTotalPages?: any;
  questions?: AIResultQuestion[] = [];
  loadingMoreQuestions?: boolean = false;
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
    if (this.resultViewData) {
      this.resultPayload.pageNo = this.resultViewData?.pageNo - 1;
      this.getResultByClassAndAssessmentId();
    }
  }

  getResultByClassAndAssessmentId() {
    this._aiGraderService
      .getResultByClassAndAssessmentId(
        {
          classId: this.resultViewData?.classId,
          assignmentId: this.resultViewData?.assessmentId,
          studEmail: JSON.parse(
            this._cacheService.getDataFromCache('userProfile')
          ).email
        },
        this.resultPayload
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
                : response?.data?.aiResultQueResponseList ?? [];
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

  goBack() {
    this.location.back();
  }
}
