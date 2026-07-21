import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges, ViewContainerRef } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';
import { QuestionType } from 'src/app/core/enums/question-type';
import { AnswerResponse, QuestionAnswerResponse, QuizReview } from 'src/app/core/models/quiz-review.model';
import { MessageService } from 'src/app/core/services/message.service';
import { ReportPreviewModalComponent } from '../../dynamic-modals/report-preview-modal-component/report-preview-modal-component.component';
import { QuizReviewQuestionsService } from 'src/app/core/services/quiz-review-questions.service';
import { Subject, takeUntil } from 'rxjs';
import { PreviewReportStatus } from 'src/app/core/enums/preview-report-status.enum';

@Component({
  selector: 'app-quiz-review-questions',
  templateUrl: './quiz-review-questions.component.html',
  styleUrls: ['./quiz-review-questions.component.scss'],
})
export class QuizReviewQuestionsComponent implements OnInit, OnChanges, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  correctAnswers: any;
  @Input() quizData: QuizReview;
  @Output() backToQuizEvent = new EventEmitter<any>();
  @Output() retakeQuizEvent = new EventEmitter<any>();
  @Input() currentSelectedTopic?: any;
  @Input() quizAttemptId: string;
  @Input() isAllowedToRetake: boolean = false;

  totalQuestions: any = 4;
  attemptedQuestions: any = 4;
  index: number = 0;
  filteredQuestions: any[] = []; // Stores filtered questions
  selectedFilter: string = 'all'; // Default filter
  showQuizScreen: boolean = false;
  numberOfCorrectAnswers: number = 0;
  welcomeQuizScreen: boolean = true;
  rightAnswer: boolean = false;
  wrongAnswer: boolean = false;
  showCongratsScreen: boolean = false;
  showFailScreen: boolean = false;
  showQuizAttempt: boolean = false;
  isSubmit: boolean = false;
  questionType = QuestionType;
  correctAnswerSelectedText?: string = 'Your selection is corrected';
  incorrectAnswerSelectedText?: string = 'Your answer is incorrect';
  nonSelectedCorrecAnswer?: string = 'Correct answer';
  textBasedCorrectAnswer?: string = 'Your answer is correct';
  isSurvey: boolean = false;

  quizQuestions: Array<any> = [
    {
      questionId: null,
      questionText: '',
      questionType: '',
      quizAnswers: [
        {
          answerId: null,
          answerText: '',
          active: false,
        },
      ],
    },
  ];

  constructor(    private _messageService: MessageService,
        private _modal: NzModalService,
        private _viewContainerRef: ViewContainerRef,
    private readonly quizReviewQuestionsService: QuizReviewQuestionsService
  ) {}

  ngOnInit(): void {
    this.filteredQuestions = this.quizData?.questionAnswerResponses || [];
      this.applyAnswerValidation();
  }

  ngOnChanges(changes: SimpleChanges): void {
    // console.log(changes);
    if('currentSelectedTopic' in changes) {
      this.isSurvey = this.currentSelectedTopic.testType === "SURVEY";
      console.log(this.currentSelectedTopic, 'selected topic');
      console.log(this.isSurvey, 'survey');
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  applyAnswerValidation(){
    this.quizData?.questionAnswerResponses?.forEach((question?: QuestionAnswerResponse) => {
      question?.answerResponseList?.forEach((answer?: AnswerResponse) => {
        answer.selected = question?.selectedAnswerId.includes(answer?.answerId);
        answer.corrected = question?.correctAnswerId.includes(answer?.answerId);
      });
    });
  }

  computeCorrectness(question: any): boolean {
    if (question.type === 'multiple-choice') {
      return (
        question.selectedAnswerIds.length ===
          question.correctAnswerIds.length &&
        question.selectedAnswerIds.every((id) =>
          question.correctAnswerIds.includes(id)
        )
      );
    } else {
      return question.selectedAnswerId === question.correctAnswerId;
    }
  }

  isCorrect(option: any, question: any): boolean {
    return question?.questionType === this.questionType?.MULTIPLE_CHOICE
      ? question.correctAnswerIds.includes(option.answerId)
      : option.answerId === question.correctAnswerId;
  }

  isSelected(option: any, question: any): boolean {
    return question?.questionType === this.questionType?.MULTIPLE_CHOICE
      ? question.selectedAnswerIds.includes(option.answerId)
      : option.answerId === question.selectedAnswerId;
  }

  retakeQuiz() {
    this.index = 0;
    this.showQuizScreen = false;
    this.welcomeQuizScreen = true;
    this.rightAnswer = false;
    this.wrongAnswer = false;
    this.showCongratsScreen = false;
    this.showFailScreen = false;
    this.isSubmit = false;
    this.numberOfCorrectAnswers = 0;
    this.showQuizAttempt = false;
    this.resetAnswers();
    this.retakeQuizEvent.emit(this.currentSelectedTopic);
  }

  backToPrevousState(){
    this.backToQuizEvent.emit(false);
  }

  resetAnswers() {
    this.quizQuestions.forEach((x: any) =>
      x.quizAnswers.forEach((y: any) => (y.active = false))
    );
  }

  filterResults(type: string) {
    this.selectedFilter = type;
    if (type === 'all') {
      this.filteredQuestions = this.quizData?.questionAnswerResponses || [];
    } else if (type === 'correct') {
      this.filteredQuestions = this.quizData?.questionAnswerResponses?.filter(q => q.correct);
    } else if (type === 'incorrect') {
      this.filteredQuestions = this.quizData?.questionAnswerResponses?.filter(q => !q.correct);
    }
  }

  viewReport(): void {
    // const reportContent = this.quizData.studentReport;
    this.quizReviewQuestionsService.fetchReport({
      quizAttemptId: this.quizAttemptId,
    })
    .pipe(takeUntil(this.destroy$))
    .subscribe(res => {
      if (!res.data) {
        this._messageService.error('No report available for this quiz');
        return;
      } else if (res.data.status === PreviewReportStatus.PENDING) {
        this._messageService.info('Report is generating, please try again');
        return;
      } else if (res.data.status === PreviewReportStatus.FAILED) {
        this._messageService.error('Failed to generate report');
        return;
      } else if (res.data.status === PreviewReportStatus.READY) {
        this.showReportPreview(res.data.html);
      }
    });
  }

  showReportPreview(reportContent: string): void {
    const modal = this._modal.create({
      nzContent: ReportPreviewModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        reportContent: reportContent,
        quizTitle: this.currentSelectedTopic?.quiz?.title || 'Quiz Report'
      },
      nzFooter: null,
      nzWidth: '80%',
      nzStyle: { top: '20px' }
    });
  }
}
