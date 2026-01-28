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
import { DeletionModalComponent } from 'src/app/modules/dynamic-modals/deletion-modal/deletion-modal.component';

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
aiResultResponse: AiResultsResponse[] = [];

  approveButtonConfig: buttonConfig = {
    borderColor: '#313131',
    border: '1px solid',
    backgroundColor: '#212189',
    height: '37px',
    paddingTop: '5px',
    paddingBottom: '7px',
  };

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
  showRetry?: any = false;
  ids: any;
  currentIndex: any;
  students: any;

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

  this.students = this.resultViewData.students;
this.currentIndex = this.resultViewData.index;


  if (this.resultViewData?.resultId) {
    this.getResultById(this.resultViewData.resultId);
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
        score: this.result?.score,
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
      }
    });
  }

  getResultById(id: number) {
  this._aiGraderService.getResultById(id).subscribe(
    res => {
      this.result = res.data;
      this.resultId = this.result.id;
      
      this.getResultQuestions();
    },
    err => console.error('Error fetching result', err)
  );
}

  approve() {
    const modal = this._modal.create({
      nzContent: DeletionModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: false,
      nzWidth: '40%',
      nzComponentParams: {
        msg: 'Once approved, result grading cannot be retried.',
        secondBtnText: 'Ok',
      },
    });

    modal?.afterClose?.subscribe((result) => {
      if (result) {
        this.result.resultStatus = 'APPROVED';
        this._aiGraderService.approveResult(this.resultId)?.subscribe({
          next: (response: any) => {
            if (
              response?.status ===
              this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
            ) {
              this.showRetry = false;
            }
          },
          error: (error: any) => {
            this._messageService.error(error?.error?.message);
          },
        });
      }
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
            this.showRetry = this.result?.resultStatus == 'APPROVED' ? false : true;

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

  onPageChange(direction: 'left' | 'right') {

  if (direction === 'right') {
    let nextIndex = this.currentIndex + 1;

    while (nextIndex < this.students.length && 
           this.students[nextIndex].status === 'INPROCESS') {
      nextIndex++;
    }

    if (nextIndex < this.students.length) {
      this.currentIndex = nextIndex;
      this.getResultById(this.students[this.currentIndex].id);
    }
  }


  if (direction === 'left') {
    let prevIndex = this.currentIndex - 1;

    while (prevIndex >= 0 && 
           this.students[prevIndex].status === 'INPROCESS') {
      prevIndex--;
    }

    if (prevIndex >= 0) {
      this.currentIndex = prevIndex;
      this.getResultById(this.students[this.currentIndex].id);
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
  if (question?.editedQuestionNumber !== undefined && question?.editedQuestionNumber !== null) {
    const oldScore = Number(question.score) || 0;
    const newScore = Number(question.editedQuestionNumber) || 0;
    
    // Store the original total before making changes
    const originalTotal = Number(this.result.grade) || 0;
    
    // Calculate the new total
    const newTotal = originalTotal - oldScore + newScore;
    
    // Update both the question score and total grade
    question.score = newScore;
    this.result.grade = newTotal;
    
    // Update in backend
    this.updateQuestion(question?.id, newScore);
  }
  question.enableQuestionEditing = false;
}

updateQuestion(aiResultQuestionId?: any, score?: any) {
  this._aiGraderService.updateQuestion(aiResultQuestionId, score)?.subscribe({
    next: (response: any) => {
      if (response?.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this._messageService?.success('Grade updated successfully.');

        if (response?.data?.grade !== undefined) {
          const target = this.questions.find(item => item.id === aiResultQuestionId);
          if (target) {
            target.score = Number(response.data.grade) || 0;
          }
          // Recalculate total grade from all questions to ensure consistency with backend
          this.recalculateTotalGrade();
        }
      }
    },
    error: (error: any) => {
      this._messageService?.error('Failed to update grade.');
      // On error, recalculate from all questions to revert any incorrect changes
      this.recalculateTotalGrade();
    },
  });
}

recalculateTotalGrade() {
  if (this.questions && this.questions.length > 0) {
    let total = 0;
    this.questions.forEach(q => {
      const score = Number(q.score) || 0;
      total += score;
    });
    this.result.grade = total;
  }
}

  onScroll(event: Event): void {
    const element = event.target as HTMLElement;

    const scrollTop = element.scrollTop;
    const scrollHeight = element.scrollHeight;
    const offsetHeight = element.offsetHeight;

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
      error: (error) => {
          if (error.status === 409) {
            this._messageService.error(`Email already exists.`);
          } else {
            this._messageService.error(`Error updating ${email}.`);
          }
        row.studentEmail = row.originalEmail; // Revert if failed
        row.isEditingEmail = false;
      },
    });
  }

  cancelEdit(row: any): void {
    row.studentEmail = row.originalEmail; // Revert on blur
    row.isEditingEmail = false;
  }

  onInput(event: Event, question: any): void {
  const inputElement = event.target as HTMLInputElement;
  let value = inputElement.value.trim();

  // Allow empty input
  if (value === '') {
    question.editedQuestionNumber = null;
    return;
  }

  // Allow optional leading minus, digits, and one optional dot
  if (!/^-?\d*\.?\d*$/.test(value)) {
    return; // Ignore invalid characters
  }

  let num = Number(value);

  // Restrict decimal precision to 1 digit
  if (value.includes('.')) {
    const [int, dec] = value.split('.');
    if (dec.length > 1) {
      num = Number(`${int}.${dec.slice(0, 1)}`);
      inputElement.value = num.toString();
    }
  }

  // Clamp negatives to 0
  if (num < 0) {
    num = 0;
    inputElement.value = '0';
  }

  // Clamp value to total marks
  if (num > question.totalMarks) {
    num = question.totalMarks;
    inputElement.value = num.toString();
  }

  // Sync with ngModel
  question.editedQuestionNumber = num;
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

  retryGrading() {
    this._aiGraderService.retryGrading(this.resultId).subscribe();
    this.backToResultScreen();
  }
}
