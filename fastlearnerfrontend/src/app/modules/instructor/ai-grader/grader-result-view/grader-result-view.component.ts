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
import {
  emptyManualQuestionForm,
  ManualQuestionForm,
} from 'src/app/core/models/manual-question-form.model';
import {
  AIResultQuestion,
  isInstructorQuestion,
} from 'src/app/core/models/result-question.model';
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
  /** Max questionNumber known across pages (not just currently loaded page). */
  private highestKnownQuestionNumber = 0;
  loadingMoreQuestions?: boolean = false;
  isQuestionsLoading = true;
  expandAllCheck?: any = false;
  showUpgradePlan = false;
  subscriptionPlanType = SubscriptionPlanType;
  gradedPapers: number = 0;
  allowedPapers?: any = 0;
  showRetry?: any = false;
  ids: any;
  currentIndex: any;
  students: any;
  showAddQuestionModal = false;
  showEditQuestionModal = false;
  manualQuestionForm: ManualQuestionForm = emptyManualQuestionForm();
  editManualQuestionForm: ManualQuestionForm = emptyManualQuestionForm();
  editingQuestionId: number | null = null;
  isSavingManualQuestion = false;
  isUpdatingManualQuestion = false;
  isInstructorQuestion = isInstructorQuestion;

  constructor(
    private _aiGraderService?: AiGraderService,
    private _router?: Router,
    private _cacheService?: CacheService,
    private _messageService?: MessageService,
    private _modal?: NzModalService,
    private _viewContainerRef?: ViewContainerRef,
  ) {
    this.showUpgradePLanButton();
  }

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
    this.questionPayload.pageNo = 0;
    this.questionTotalPages = 0;
    this.highestKnownQuestionNumber = 0;
    this.loadingMoreQuestions = false;
    this.questions = [];

    this._aiGraderService.getResultById(id).subscribe(
      (res) => {
        this.result = res.data;
        this.resultId = this.result.id;

        this.getResultQuestions();
      },
      (err) => console.error('Error fetching result', err),
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
    if (!append) {
      this.isQuestionsLoading = true;
    }
    this._aiGraderService
      .getResultQuestions(this.resultId, this.questionPayload)
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            if (append) {
              this.questions = this.normalizeQuestionsList([
                ...(this.questions || []),
                ...(response?.data?.aiResultQueResponseList ?? []),
              ]);
              this.loadingMoreQuestions = false;
            } else {
              this.questions = this.normalizeQuestionsList(
                response?.data?.aiResultQueResponseList ?? [],
              );
              this.questionTotalPages = response?.data?.pages;
              this.isQuestionsLoading = false;
              // this.questions[0].panelOpen = true;
            }
            this.trackHighestQuestionNumber(this.questions);
            this.applyPaginationMeta(response?.data);

            if (this.expandAllCheck) {
              this.expandAll(true);
            }
          }
        },
        error: (error: any) => {
          this.questions = [];
          this.isQuestionsLoading = false;
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
            this.showRetry =
              this.result?.resultStatus == 'APPROVED' ? false : true;

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

      while (
        nextIndex < this.students.length &&
        this.students[nextIndex].status === 'INPROCESS'
      ) {
        nextIndex++;
      }

      if (nextIndex < this.students.length) {
        this.currentIndex = nextIndex;
        this.getResultById(this.students[this.currentIndex].id);
      }
    }

    if (direction === 'left') {
      let prevIndex = this.currentIndex - 1;

      while (
        prevIndex >= 0 &&
        this.students[prevIndex].status === 'INPROCESS'
      ) {
        prevIndex--;
      }

      if (prevIndex >= 0) {
        this.currentIndex = prevIndex;
        this.getResultById(this.students[this.currentIndex].id);
      }
    }
  }

  backToResultScreen() {
    this.syncMarksToListCache();
    this._cacheService?.saveJsonData('graderResultsNeedsRefresh', true);
    this._router.navigate(['instructor/ai-grader/results'], {
      queryParams: {
        id: this.resultViewData?.assessmentId,
        classId: this.resultViewData?.classId,
      },
      state: {
        updatedResult: this.result
          ? {
              id: this.result.id,
              grade: this.result.grade,
              score: this.result.score,
            }
          : null,
      },
    });
  }

  /** Keep assessment/student list scores in sync after inline mark edits. */
  private syncMarksToListCache(): void {
    if (!this.result?.id || !this._cacheService) {
      return;
    }
    const cache = this._cacheService.getJsonData('resultView');
    if (!cache || typeof cache !== 'object') {
      return;
    }
    if (Array.isArray(cache.students)) {
      const student = cache.students.find(
        (s: { id: number }) => s.id === this.result.id,
      );
      if (student) {
        student.grade = this.result.grade;
        student.score = this.result.score;
      }
    }
    cache.lastUpdatedResultId = this.result.id;
    cache.lastUpdatedGrade = this.result.grade;
    cache.lastUpdatedScore = this.result.score;
    this._cacheService.saveJsonData('resultView', cache);
  }

  enableEdit(question?: AIResultQuestion) {
    if (isInstructorQuestion(question)) {
      this.openEditManualQuestionModal(question);
      return;
    }
    question.enableQuestionEditing = true;
    question.editedObtainedMarks =
      question.score != null ? Number(question.score) : null;
    question.editedOutOfMarks =
      question.totalMarks != null ? Number(question.totalMarks) : null;
    setTimeout(() => {
      this.questionScoreInputRef?.nativeElement?.focus();
    }, 0);
  }

  openEditManualQuestionModal(question?: AIResultQuestion): void {
    if (!question?.id || !isInstructorQuestion(question)) {
      return;
    }
    question.enableQuestionEditing = false;
    question.enableFullQuestionEditing = false;
    this.editingQuestionId = Number(question.id);
    this.editManualQuestionForm = {
      questionNumber:
        question.questionNumber != null
          ? Number(question.questionNumber)
          : null,
      obtainedMarks: question.score != null ? Number(question.score) : null,
      outOfMarks:
        question.totalMarks != null ? Number(question.totalMarks) : null,
      feedback: question.feedback || '',
    };
    this.showEditQuestionModal = true;
  }

  closeEditManualQuestionModal(): void {
    this.showEditQuestionModal = false;
    this.editingQuestionId = null;
    this.editManualQuestionForm = emptyManualQuestionForm();
    this.isUpdatingManualQuestion = false;
  }

  cancelQuestionEdit(question?: any) {
    question.enableQuestionEditing = false;
    question.editedObtainedMarks = null;
    question.editedOutOfMarks = null;
  }

  validateQuestionMarks(question: AIResultQuestion): string | null {
    const obtained = question.editedObtainedMarks;
    const outOf = question.editedOutOfMarks;

    if (
      obtained === null ||
      obtained === undefined ||
      isNaN(Number(obtained))
    ) {
      return 'Obtained marks is required.';
    }
    if (outOf === null || outOf === undefined || isNaN(Number(outOf))) {
      return 'Out of marks is required.';
    }
    if (Number(obtained) < 0) {
      return 'Obtained marks cannot be negative.';
    }
    if (Number(outOf) < 0) {
      return 'Out of marks cannot be negative.';
    }
    if (Number(obtained) > Number(outOf)) {
      return 'Obtained marks cannot exceed out of marks.';
    }
    return null;
  }

  updateQuestionMarks(question?: AIResultQuestion) {
    if (!question?.enableQuestionEditing) {
      return;
    }

    const validationError = this.validateQuestionMarks(question);
    if (validationError) {
      this._messageService?.error(validationError);
      return;
    }

    const obtainedMarks = Number(question.editedObtainedMarks);
    const outOfMarks = Number(question.editedOutOfMarks);

    question.score = obtainedMarks;
    question.totalMarks = outOfMarks;
    question.enableQuestionEditing = false;

    this.updateQuestion(question.id, obtainedMarks, outOfMarks);
  }

  updateQuestion(
    aiResultQuestionId?: any,
    obtainedMarks?: number,
    outOfMarks?: number,
  ) {
    this._aiGraderService
      .updateQuestion(aiResultQuestionId, obtainedMarks, outOfMarks)
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this._messageService?.success('Marks updated successfully.');
            const applied = this.applyQuestionUpdateResponse(
              aiResultQuestionId,
              response?.data,
            );
            if (!applied) {
              this.getResultById(this.resultId);
            }
            this.syncMarksToListCache();
            this._cacheService?.saveJsonData('graderResultsNeedsRefresh', true);
          }
        },
        error: (error: any) => {
          this._messageService?.error(
            error?.error?.message || 'Failed to update marks.',
          );
          if (this.resultId) {
            this.getResultById(this.resultId);
          }
        },
      });
  }

  applyQuestionUpdateResponse(aiResultQuestionId: any, data: any): boolean {
    if (!data || !this.result) {
      return false;
    }
    if (data.question) {
      this.mergeQuestionFromApi(data.question);
    }
    const target = this.questions.find(
      (item) => item.id === aiResultQuestionId,
    );
    if (target) {
      if (data.obtainedMarks !== undefined && data.obtainedMarks !== null) {
        target.score = Number(data.obtainedMarks);
      }
      if (data.outOfMarks !== undefined && data.outOfMarks !== null) {
        target.totalMarks = Number(data.outOfMarks);
      }
    }
    let appliedTotals = false;
    if (
      data.resultObtainedMarks !== undefined &&
      data.resultObtainedMarks !== null
    ) {
      this.result.grade = Number(data.resultObtainedMarks);
      appliedTotals = true;
    }
    if (data.resultOutOfMarks !== undefined && data.resultOutOfMarks !== null) {
      this.result.score = Number(data.resultOutOfMarks);
      appliedTotals = true;
    }
    return appliedTotals;
  }

  applyMutationResponse(
    data: any,
    options?: { mergeQuestion?: boolean },
  ): void {
    if (!data) {
      return;
    }
    const mergeQuestion = options?.mergeQuestion !== false;
    if (mergeQuestion && data.question) {
      this.mergeQuestionFromApi(data.question);
    }
    if (this.result) {
      if (data.resultObtainedMarks != null) {
        this.result.grade = Number(data.resultObtainedMarks);
      }
      if (data.resultOutOfMarks != null) {
        this.result.score = Number(data.resultOutOfMarks);
      }
    }
    this.syncMarksToListCache();
    this._cacheService?.saveJsonData('graderResultsNeedsRefresh', true);
  }

  private mergeQuestionFromApi(apiQuestion: any): void {
    if (
      apiQuestion?.status === 'INACTIVE' ||
      apiQuestion?.status === 'Inactive'
    ) {
      this.questions = this.questions.filter(
        (q) => Number(q.id) !== Number(apiQuestion.id),
      );
      this.recomputeHighestFromLoadedQuestions();
      return;
    }
    const idx = this.questions.findIndex(
      (q) => Number(q.id) === Number(apiQuestion.id),
    );
    const merged: AIResultQuestion = {
      ...(idx >= 0 ? this.questions[idx] : {}),
      ...apiQuestion,
      panelOpen: idx >= 0 ? this.questions[idx].panelOpen : false,
    };
    if (idx >= 0) {
      this.questions[idx] = merged;
      this.questions = this.normalizeQuestionsList(this.questions);
    } else {
      this.questions = this.normalizeQuestionsList([
        ...(this.questions || []),
        merged,
      ]);
    }
    this.trackHighestQuestionNumber([merged]);
  }

  /**
   * Dedupe by id and sort by questionNumber so merged + paginated appends
   * never show out-of-order lists like 1-10, 13, 11, 12, 13.
   */
  private normalizeQuestionsList(
    questions: AIResultQuestion[] | null | undefined,
  ): AIResultQuestion[] {
    const byId = new Map<number, AIResultQuestion>();
    for (const q of questions || []) {
      if (q == null || q.id == null) {
        continue;
      }
      const id = Number(q.id);
      const existing = byId.get(id);
      byId.set(id, existing ? { ...existing, ...q } : q);
    }
    return Array.from(byId.values()).sort(
      (a, b) => Number(a.questionNumber) - Number(b.questionNumber),
    );
  }

  private applyPaginationMeta(data: any): void {
    if (!data) {
      return;
    }
    if (data.pages != null) {
      this.questionTotalPages = data.pages;
    }
    if (data.totalElements != null) {
      const total = Number(data.totalElements) || 0;
      // Contiguous 1..N papers: totalElements is a safe lower bound for max number.
      this.highestKnownQuestionNumber = Math.max(
        this.highestKnownQuestionNumber,
        total,
      );
      if (this.questionPayload?.pageSize) {
        this.questionTotalPages = Math.max(
          Number(this.questionTotalPages) || 0,
          Math.ceil(total / this.questionPayload.pageSize),
        );
      }
    }
  }

  private hasQuestionNumberGap(): boolean {
    const nums = (this.questions || [])
      .map((q) => Number(q.questionNumber) || 0)
      .filter((n) => n > 0)
      .sort((a, b) => a - b);
    for (let i = 1; i < nums.length; i++) {
      if (nums[i] !== nums[i - 1] + 1) {
        return true;
      }
    }
    return false;
  }

  /**
   * After adding a high question number while only page 0 is loaded (e.g. 1-10 + 13),
   * fetch remaining pages so 11/12 appear and the list stays ordered.
   */
  private fetchMissingQuestionPagesIfNeeded(): void {
    if (
      this.loadingMoreQuestions ||
      !this.hasQuestionNumberGap() ||
      this.questionPayload.pageNo + 1 >= Number(this.questionTotalPages || 0)
    ) {
      return;
    }

    this.loadingMoreQuestions = true;
    this.questionPayload.pageNo++;
    this._aiGraderService
      .getResultQuestions(this.resultId, this.questionPayload)
      ?.subscribe({
        next: (response: any) => {
          this.loadingMoreQuestions = false;
          if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.questions = this.normalizeQuestionsList([
              ...(this.questions || []),
              ...(response?.data?.aiResultQueResponseList ?? []),
            ]);
            this.trackHighestQuestionNumber(this.questions);
            this.applyPaginationMeta(response?.data);
            if (this.expandAllCheck) {
              this.expandAll(true);
            }
            this.fetchMissingQuestionPagesIfNeeded();
          }
        },
        error: () => {
          this.loadingMoreQuestions = false;
          this.questionPayload.pageNo = Math.max(
            0,
            this.questionPayload.pageNo - 1,
          );
        },
      });
  }

  private trackHighestQuestionNumber(
    questions?: AIResultQuestion[] | null,
  ): void {
    for (const q of questions || []) {
      const n = Number(q.questionNumber) || 0;
      if (n > this.highestKnownQuestionNumber) {
        this.highestKnownQuestionNumber = n;
      }
    }
  }

  private recomputeHighestFromLoadedQuestions(): void {
    const nums = (this.questions || [])
      .map((q) => Number(q.questionNumber) || 0)
      .filter((n) => n > 0);
    this.highestKnownQuestionNumber = nums.length ? Math.max(...nums) : 0;
  }

  openAddQuestionModal(): void {
    this.manualQuestionForm = emptyManualQuestionForm();
    this.manualQuestionForm.questionNumber = this.suggestedNextQuestionNumber();
    this.showAddQuestionModal = true;
  }

  closeAddQuestionModal(): void {
    this.showAddQuestionModal = false;
    this.manualQuestionForm = emptyManualQuestionForm();
  }

  suggestedNextQuestionNumber(): number {
    return this.allowedMaxQuestionNumber();
  }

  /** Highest allowed question number (next slot after existing questions). */
  allowedMaxQuestionNumber(excludeQuestionId?: number | null): number {
    const active = (this.questions || []).filter(
      (q) =>
        excludeQuestionId == null || Number(q.id) !== Number(excludeQuestionId),
    );
    const loadedMax = active.length
      ? Math.max(...active.map((q) => Number(q.questionNumber) || 0))
      : 0;
    return Math.max(loadedMax, this.highestKnownQuestionNumber) + 1;
  }

  validateManualForm(
    form: ManualQuestionForm,
    options?: { excludeQuestionId?: number | null },
  ): string | null {
    const obtained = form.obtainedMarks;
    const outOf = form.outOfMarks;
    const questionNumber = form.questionNumber;

    if (
      questionNumber !== null &&
      questionNumber !== undefined &&
      !isNaN(Number(questionNumber))
    ) {
      const allowedMax = this.allowedMaxQuestionNumber(
        options?.excludeQuestionId,
      );
      if (Number(questionNumber) > allowedMax) {
        return `Question number cannot skip ahead. Use question ${allowedMax}.`;
      }
    }

    if (
      obtained === null ||
      obtained === undefined ||
      isNaN(Number(obtained))
    ) {
      return 'Obtained marks is required.';
    }
    if (outOf === null || outOf === undefined || isNaN(Number(outOf))) {
      return 'Out of marks is required.';
    }
    if (Number(outOf) <= 0) {
      return 'Out of marks must be greater than zero.';
    }
    if (Number(obtained) < 0) {
      return 'Obtained marks cannot be negative.';
    }
    if (Number(obtained) > Number(outOf)) {
      return 'Obtained marks cannot exceed out of marks.';
    }
    return null;
  }

  saveManualQuestion(): void {
    this.manualQuestionForm.questionNumber =
      this.manualQuestionForm.questionNumber ??
      this.suggestedNextQuestionNumber();
    const error = this.validateManualForm(this.manualQuestionForm);
    if (error) {
      this._messageService?.error(error);
      return;
    }
    if (!this.resultId) {
      return;
    }

    this.isSavingManualQuestion = true;
    const questionNumber =
      this.manualQuestionForm.questionNumber ??
      this.suggestedNextQuestionNumber();
    const payload = {
      aiResultId: this.resultId,
      questionNumber: questionNumber || undefined,
      // Not shown in UI; satisfies older Ai-Grader builds that still require questionText
      questionText: `Question ${questionNumber}`,
      obtainedMarks: Number(this.manualQuestionForm.obtainedMarks),
      outOfMarks: Number(this.manualQuestionForm.outOfMarks),
      feedback: this.manualQuestionForm.feedback?.trim() || null,
    };

    this._aiGraderService.createManualQuestion(payload).subscribe({
      next: (response: any) => {
        this.isSavingManualQuestion = false;
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this._messageService?.success('Question added successfully.');
          this.highestKnownQuestionNumber = Math.max(
            this.highestKnownQuestionNumber,
            Number(questionNumber) || 0,
          );
          this.applyMutationResponse(response?.data);
          this.applyPaginationMeta(response?.data);
          // Keep current list sorted; if mutation omits pages/total, bump locally.
          if (this.questionPayload?.pageSize) {
            const count = Math.max(
              this.questions?.length || 0,
              this.highestKnownQuestionNumber,
            );
            this.questionTotalPages = Math.max(
              Number(this.questionTotalPages) || 0,
              Math.ceil(count / this.questionPayload.pageSize),
            );
          }
          this.closeAddQuestionModal();
          this.fetchMissingQuestionPagesIfNeeded();
        }
      },
      error: (err: any) => {
        this.isSavingManualQuestion = false;
        this._messageService?.error(
          err?.error?.message || 'Failed to add question.',
        );
      },
    });
  }

  saveManualQuestionEdit(): void {
    if (!this.editingQuestionId) {
      return;
    }
    const error = this.validateManualForm(this.editManualQuestionForm, {
      excludeQuestionId: this.editingQuestionId,
    });
    if (error) {
      this._messageService?.error(error);
      return;
    }

    const questionNumber = this.editManualQuestionForm.questionNumber;
    const payload = {
      questionNumber,
      questionText: `Question ${questionNumber}`,
      obtainedMarks: Number(this.editManualQuestionForm.obtainedMarks),
      outOfMarks: Number(this.editManualQuestionForm.outOfMarks),
      feedback: this.editManualQuestionForm.feedback?.trim() || null,
    };

    this.isUpdatingManualQuestion = true;
    this._aiGraderService
      .updateManualQuestion(this.editingQuestionId, payload)
      .subscribe({
        next: (response: any) => {
          this.isUpdatingManualQuestion = false;
          if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this._messageService?.success('Question updated successfully.');
            this.applyMutationResponse(response?.data);
            this.closeEditManualQuestionModal();
            this.reloadQuestionsFromStart();
          }
        },
        error: (err: any) => {
          this.isUpdatingManualQuestion = false;
          this._messageService?.error(
            err?.error?.message || 'Failed to update question.',
          );
        },
      });
  }

  confirmDeleteManualQuestion(question?: AIResultQuestion): void {
    if (!question?.id || !isInstructorQuestion(question)) {
      return;
    }
    const modal = this._modal.create({
      nzContent: DeletionModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: false,
      nzWidth: '40%',
      nzComponentParams: {
        msg: 'Delete this manual question? Totals will be recalculated.',
        secondBtnText: 'Delete',
      },
    });
    modal?.afterClose?.subscribe((confirmed) => {
      if (confirmed) {
        this.deleteManualQuestion(question);
      }
    });
  }

  deleteManualQuestion(question: AIResultQuestion): void {
    this._aiGraderService.deleteManualQuestion(Number(question.id)).subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this._messageService?.success('Question deleted successfully.');
          this.questions = this.questions.filter(
            (q) => Number(q.id) !== Number(question.id),
          );
          // Always recompute — middle deletes leave highestKnown stale otherwise
          // (e.g. 20 questions, delete Q5 → suggested becomes 21 instead of 20).
          this.recomputeHighestFromLoadedQuestions();
          this.applyMutationResponse(response?.data, { mergeQuestion: false });
          this.reloadQuestionsFromStart();
        }
      },
      error: (err: any) => {
        this._messageService?.error(
          err?.error?.message || 'Failed to delete question.',
        );
      },
    });
  }

  reloadQuestionsFromStart(): void {
    this.questionPayload.pageNo = 0;
    this.loadingMoreQuestions = false;
    this.questions = [];
    // Clear high-water mark so pagination meta + loaded numbers rebuild it
    // after delete (avoids Math.max(staleHighest, totalElements) keeping old max).
    this.highestKnownQuestionNumber = 0;
    this.getResultQuestions();
  }

  recalculateTotalsFromQuestions() {
    this.recalculateTotalGrade();
    this.recalculateTotalOutOf();
  }

  recalculateTotalGrade() {
    if (this.questions && this.questions.length > 0) {
      let total = 0;
      this.questions.forEach((q) => {
        total += Number(q.score) || 0;
      });
      this.result.grade = total;
    }
  }

  recalculateTotalOutOf() {
    if (this.questions && this.questions.length > 0) {
      let total = 0;
      this.questions.forEach((q) => {
        total += Number(q.totalMarks) || 0;
      });
      this.result.score = total;
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

  onObtainedInput(event: Event, question: AIResultQuestion): void {
    const num = this.parseMarkInput(event, question.editedOutOfMarks);
    question.editedObtainedMarks = num;
  }

  onOutOfInput(event: Event, question: AIResultQuestion): void {
    const num = this.parseMarkInput(event);
    question.editedOutOfMarks = num;
    if (
      question.editedObtainedMarks != null &&
      num != null &&
      question.editedObtainedMarks > num
    ) {
      question.editedObtainedMarks = num;
    }
  }

  private parseMarkInput(
    event: Event,
    maxValue?: number | null,
  ): number | null {
    const inputElement = event.target as HTMLInputElement;
    let value = inputElement.value.trim();

    if (value === '') {
      return null;
    }

    if (!/^\d*\.?\d*$/.test(value)) {
      return null;
    }

    let num = Number(value);

    if (value.includes('.')) {
      const [int, dec] = value.split('.');
      if (dec.length > 1) {
        num = Number(`${int}.${dec.slice(0, 1)}`);
        inputElement.value = num.toString();
      }
    }

    if (num < 0) {
      num = 0;
      inputElement.value = '0';
    }

    if (maxValue != null && num > maxValue) {
      num = maxValue;
      inputElement.value = num.toString();
    }

    return num;
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
