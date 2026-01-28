import { Component, EventEmitter, Input, OnChanges, Output,ViewContainerRef } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { PreviewReportStatus } from 'src/app/core/enums/preview-report-status.enum';
import { MessageService } from 'src/app/core/services/message.service';
import { NzModalService } from 'ng-zorro-antd/modal';
import { QuizReviewQuestionsService } from 'src/app/core/services/quiz-review-questions.service';
import { ReportPreviewModalComponent } from '../../dynamic-modals/report-preview-modal-component/report-preview-modal-component.component';
import { QuestionType } from 'src/app/core/enums/question-type';

@Component({
  selector: 'app-quiz-review-attempt-questions',
  templateUrl: './quiz-review-attempt-questions.component.html',
  styleUrls: ['./quiz-review-attempt-questions.component.scss']
})
export class QuizReviewAttemptQuestionsComponent implements OnChanges {
  private readonly destroy$ = new Subject<void>();

  @Input() quizData: any;
  @Input() currentSelectedTopic: any;
  @Input() isAllowedToRetake: boolean = false;
  @Output() retakeQuizEvent = new EventEmitter<any>();
  @Output() backToQuizEvent = new EventEmitter<any>();


  isSurvey: boolean = false;
  questionType = QuestionType;

  filteredQuestions: any[] = [];
  selectedFilter: 'all' | 'correct' | 'incorrect' = 'all';

  // Texts to show for different states
  correctAnswerSelectedText = 'Correct Answer Selected';
  incorrectAnswerSelectedText = 'Incorrect Answer Selected';
  nonSelectedCorrecAnswer = 'Correct Answer';
  textBasedCorrectAnswer?: string = 'Your answer is correct';

 

  constructor(    private _messageService: MessageService,
          private _modal: NzModalService,
          private _viewContainerRef: ViewContainerRef,
      private readonly quizReviewQuestionsService: QuizReviewQuestionsService
    ) {}

  ngOnInIt(){
  }

  ngOnChanges() {
  if (!this.quizData?.quiz?.quizQuestions?.length) {
    this.filteredQuestions = [];
    return;
  }
  this.filteredQuestions = [...this.quizData.quiz.quizQuestions];
  this.isSurvey = this.quizData.quiz.testType === "SURVEY";
}


  filterResults(filter: 'all' | 'correct' | 'incorrect') {
  const allQuestions = [...this.quizData.quiz.quizQuestions];

  if (filter === 'all') {
    this.filteredQuestions = allQuestions;
  } else if (filter === 'correct') {
    this.filteredQuestions = allQuestions.filter(q => q.isCorrect);
  } else if (filter === 'incorrect') {
    this.filteredQuestions = allQuestions.filter(q => q.isCorrect === false);
  }
}

  retakeQuiz() {
    
  this.retakeQuizEvent.emit(this.currentSelectedTopic);
  }

  backToPrevousState(){
    this.backToQuizEvent.emit(false);
  }

  viewReport(): void {
      // const reportContent = this.quizData.studentReport;

      this.showReportPreview(this.quizData.studentReport);
        
    }
  
   

  showReportPreview(reportContent: string): void {
    if(!reportContent) {
      this._messageService.error('Report content is not available.');
      return;
    }
    
    this._modal.create({
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

  getAttemptedQuestionsCount(quizQuestions: {selectedAnswerIds: number[]}[]): number {
    if(!quizQuestions) return 0;
    return quizQuestions.filter(q => q.selectedAnswerIds && q.selectedAnswerIds.length > 0).length;
  }
}
