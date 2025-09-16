import {
  Component,
  ElementRef,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import { Router } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { SubscriptionPlanType } from 'src/app/core/enums/subscription-plan.enum';
import { AiResultsResponse } from 'src/app/core/models/ai-results-response.model';
import { buttonConfig } from 'src/app/core/models/button.model-config';
import { AIResultQuestion } from 'src/app/core/models/result-question.model';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SubscriptionPlanComponent } from 'src/app/modules/auth/subscription-plan/subscription-plan.component';
import { ClassUploaderComponent } from 'src/app/modules/dynamic-modals/class-uploader/class-uploader.component';

@Component({
  selector: 'app-grader-result-view',
  templateUrl: './grader-result-view.component.html',
  styleUrls: ['./grader-result-view.component.scss'],
})
export class GraderResultViewComponent {
  @ViewChild('questionScoreInput') questionScoreInputRef!: ElementRef;
  @ViewChild('emailEdit') emailInputRef!: ElementRef;
  _httpConstants: HttpConstants = new HttpConstants();
  shareButtonConfig: buttonConfig = {
    color: '#FE4A55',
    borderColor: '#FE4A55',
    border: '1px solid',
    backgroundColor: '#fffff',
    height: '37px',
    paddingTop: '5px',
    paddingBottom: '7px',
  };

  approveButtonConfig: buttonConfig = {
    borderColor: '#313131',
    border: '1px solid',
    backgroundColor: '#212189',
    height: '37px',
    paddingTop: '5px',
    paddingBottom: '7px',
  };

  panels = [
    {
      active: true,
      name: 'This is panel header 1',
      disabled: false,
      confidenceLevel: 'High',
    },
    {
      active: false,
      disabled: false,
      name: 'This is panel header 2',
      confidenceLevel: 'High',
    },
    {
      active: false,
      disabled: false,
      name: 'This is panel header 3',
      confidenceLevel: 'High',
    },
  ];

  resultViewData?: any;
  resultId?: any;
  resultPayload = {
    pageNo: 0,
    pageSize: 1,
  };
  questionPayload = {
    pageNo: 0,
    pageSize: 10,
  };
  questions?: AIResultQuestion[] = [];
  result: AiResultsResponse = null;
  totalPages?: any;
  questionTotalPages?: any;
  loadingMoreQuestions?: boolean = false;
  expandAllCheck?: any = false;
  showUpgradePlan?: boolean = true;
  subscriptionPlanType = SubscriptionPlanType;
  gradedPapers: number = 0;
  allowedPapers?: any = 0;

  constructor(
    private _aiGraderService?: AiGraderService,
    private _router?: Router,
    private _cacheService?: CacheService,
    private _messageService?: MessageService,
    private _modal?: NzModalService,
    private _viewContainerRef?: ViewContainerRef
  ) {}

  ngOnInit(): void {
    this.resultViewData = this._cacheService.getJsonData('resultView');
    if (this.resultViewData) {
      this.resultPayload.pageNo = this.resultViewData?.pageNo - 1;
      this.getResultByClassAndAssessmentId(true);
    }

    this.getNoOfPages();
    this.showUpgradePLanButton();
  }

  share() {
    const modalRef = this._modal.create({
      nzContent: ClassUploaderComponent,
      nzComponentParams: {
        studentScore: this.result?.grade,
        studentEmail: this.result?.studentEmail,
        studentName: this.result?.studentName,
        aiResultId: this.result?.id,
      },
      nzFooter: null,
      nzWidth: 600,
    });

    modalRef.afterClose.subscribe((createdClass) => {
      if (createdClass?.name) {
        const newOption = {
          label: createdClass.name,
          value: createdClass.name,
        };
        // handle the new class
      }
    });
  }

  approve() {
    this.result.resultStatus = 'APPROVED';
    this._aiGraderService.approveResult(this.resultId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          // this.onPageChange('right');
          // Object.assign(this.approveButtonConfig, {
          //   cursor: 'no-drop',
          // });
        }
      },
      error: (error: any) => {
        this._messageService.error(error?.error?.message);
        // this.onPageChange('right');
      },
    });
  }
  expandAll(expand?: any) {
    this.expandAllCheck = expand;
    this.questions?.forEach((q?: AIResultQuestion) => {
      q.panelOpen = expand;
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

            if (this.expandAllCheck) {
              this.expandAll(true);
            }
          }
        },
        error: (error: any) => {
          this.questions = [];
        },
      });
  }

  getResultByClassAndAssessmentId(fetchQuestions?: boolean) {
    this._aiGraderService
      .getResultByClassAndAssessmentId(
        {
          classId: this.resultViewData?.classId,
          assignmentId: this.resultViewData?.assessmentId,
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

            if (fetchQuestions) {
              this.getResultQuestions();
            }
          }
        },
        error: (error: any) => {
          this.resultPayload.pageNo -= 1;
          this._messageService.error(error?.error?.message);
        },
      });
  }

  onPanelClick(panel?: any) {
    panel.panelOpen = !panel?.panelOpen;
  }

  onPageChange(type?: any) {
    if (type == 'right') {
      if (this.resultPayload?.pageNo + 1 < this.totalPages) {
        this.resultPayload.pageNo += 1;
        this.getResultByClassAndAssessmentId(true);
      }
    } else if (type == 'left') {
      if (this.resultPayload?.pageNo - 1 >= 0) {
        this.resultPayload.pageNo -= 1;
        this.getResultByClassAndAssessmentId(true);
      }
    }
  }

  backToResultScreen() {
    this._router.navigate(['instructor/ai-grader/results'], {
      queryParams: {
        id: this.resultViewData?.assessmentId,
        classId: this.resultViewData?.classId,
      },
    });
  }

  enableEdit(question?: any) {
    question.enableQuestionEditing = true;
    setTimeout(() => {
      this.questionScoreInputRef?.nativeElement?.focus();
    }, 0);
  }

  cancelQuestionEdit(question?: any) {
    question.enableQuestionEditing = false;
  }

  updateQuestionNumber(question?: any) {
    if (question?.editedQuestionNumber) {
      question.score = question?.editedQuestionNumber;
      this.updateQuestion(question?.id, question?.score);
    }
    question.enableQuestionEditing = false;
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

  enableEditEmail(row: any): void {
    row.originalEmail = row.studentEmail; // Backup original
    row.isEditingEmail = true; // Enter edit mode
    setTimeout(() => {
      this.emailInputRef?.nativeElement?.focus();
    }, 0);
  }

  saveEmail(event: Event, row: any): void {
    const inputElement = event.target as HTMLInputElement;
    const email = inputElement.value.trim();

    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (!emailPattern.test(email)) {
      this._messageService.error('Please enter a valid email address.');
      return;
    }
    this._aiGraderService.editEmail(row.id, row.studentEmail).subscribe({
      next: () => {
        row.isEditingEmail = false;
      },
      error: () => {
        console.error('Error updating email');
        row.studentEmail = row.originalEmail; // Revert if failed
        row.isEditingEmail = false;
      },
    });
  }

  cancelEdit(row: any): void {
    row.studentEmail = row.originalEmail; // Revert on blur
    row.isEditingEmail = false;
  }

  updateQuestion(aiResultQuestionId?: any, score?: any) {
    this._aiGraderService.updateQuestion(aiResultQuestionId, score)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.getResultByClassAndAssessmentId(false);
        }
      },
      error: (error: any) => {},
    });
  }

  onInput(event: Event, revertedValue?: any, max?: any): void {
    const inputElement = event.target as HTMLInputElement;
    let value = inputElement.value;

    // Prevent non-numeric characters except one optional dot
    if (!/^\d*\.?\d*$/.test(value)) {
      inputElement.value = value.slice(0, -1);
      return;
    }

    const num = Number(value);

    // Limit value between 0 and 10
    if (num > max) {
      inputElement.value = revertedValue;
      return;
    } else if (num < 0) {
      inputElement.value = '0';
      return;
    }

    // Restrict decimal precision to only 1 digit after the dot
    if (value.includes('.')) {
      const [integerPart, decimalPart] = value.split('.');
      if (decimalPart.length > 1) {
        inputElement.value = `${integerPart}.${decimalPart.slice(0, 1)}`;
      }
    }
  }

  getConfidenceColor(level: string): string {
    switch (level?.toLowerCase()) {
      case 'high':
        return '#5cb85c';
      case 'medium':
        return '#262261';
      case 'low':
        return '#E23643';
      default:
        return 'gray';
    }
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
}
