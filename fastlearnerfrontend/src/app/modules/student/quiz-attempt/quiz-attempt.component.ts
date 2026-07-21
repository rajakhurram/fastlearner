import {
  Component,
  EventEmitter,
  Input,
  Output,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import { QuizReviewAttemptQuestionsComponent } from '../quiz-review-attempt-questions/quiz-review-attempt-questions.component';

@Component({
  selector: 'app-quiz-attempt',
  templateUrl: './quiz-attempt.component.html',
  styleUrls: ['./quiz-attempt.component.scss'],
})
export class QuizAttemptComponent {
  @Input() quizData: any;
  @Input() currentSelectedTopic?: any;
  @Input() isAllowedToRetake: boolean = false;
  @Output() retakeQuizEvent = new EventEmitter<any>();
  @Output() backToQuizEvent = new EventEmitter<any>();

  correct = 0;
  incorrect = 0;
  skipped = 0;
  correctPercent = 0;
  incorrectPercent = 0;
  skippedPercent = 0;
  attemptNumber = 0;
  formattedDate = '';
  duration = '';
  isReviewing = false;
  isSurvey: boolean = false;

  ngOnChanges() {
    if (!this.quizData) return;

    this.attemptNumber = this.quizData.totalAttemptCount || 1;
    const total = this.quizData.totalQuestion || 0;
    const correct = this.quizData.totalCorrectAnswer || 0;

    this.correct = correct;
    this.incorrect = this.quizData.totalIncorrectAnswer ?? total - correct;
    this.skipped = total - (this.correct + this.incorrect);

    this.correctPercent = (this.correct / total) * 100;
    this.incorrectPercent = (this.incorrect / total) * 100;
    this.skippedPercent = (this.skipped / total) * 100;
    this.isSurvey = this.quizData.quiz.testType === 'SURVEY';

    this.duration = this.quizData.durationInMinutes
      ? `${this.quizData.durationInMinutes} minutes`
      : '';
    this.formattedDate = new Date(
      this.quizData.attemptDate,
    ).toLocaleDateString();
  }

  retakeQuiz(event: any) {
    this.isReviewing = false;
    console.log(this.currentSelectedTopic);
    this.retakeQuizEvent.emit(this.currentSelectedTopic);
  }

  reviewQuiz() {
    this.isReviewing = true;
  }

  backToPrevousState() {
    this.backToQuizEvent.emit(false);
  }

  backToQuiz(flag: boolean) {
    this.isReviewing = flag;
  }
}
