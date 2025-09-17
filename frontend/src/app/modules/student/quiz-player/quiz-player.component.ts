import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { QuestionType } from 'src/app/core/enums/question-type';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';

@Component({
  selector: 'app-quiz-player',
  templateUrl: './quiz-player.component.html',
  styleUrls: ['./quiz-player.component.scss'],
})
export class QuizPlayerComponent implements OnInit, OnChanges {
  _httpConstants: HttpConstants = new HttpConstants();

  @Input() currentSelectedTopic: any;
  @Output() skipQuizEmitter = new EventEmitter<any>();
  @Output() continueQuizEmitter = new EventEmitter<any>();
  @Input() quizQuestions: any[] = [];
  @Input() congratsScreen?: any;
  @Input() quizResults?: any;
  @Input() title: any;

  numberOfQuestions: any;
  showQuizScreen: boolean = false;
  numberOfCorrectAnswers: number = 0;
  showCongratsScreen: boolean = false;
  showFailScreen: boolean = false;
  welcomeQuizScreen: boolean = true;
  rightAnswer: boolean = false;
  wrongAnswer: boolean = false;
  isSubmit: boolean = false;
  selectedAnswerId: any;
  index: number = 0;
  passingCriteria: any;
  correctAnswerId?: any = null;
  durationInMinutes: any;
  minutes: number = 1; 
  seconds: number = 30; 
  interval: any;
  userAnswers: Array<{ questionId: number; answerId: any; questionType: any; submitted: boolean }> =
    [];
  selectedAnswers: string[] = [];
  questionAnswers: any = [];
  questionType = QuestionType;
  quizBtnText?: string = 'Next';

  isLastQuestion: any;
  option: any;
  @Output() reviewCallBack = new EventEmitter<any>();

  constructor(
    private _courseService: CourseService,
    private _message: MessageService,
    private _cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (this.congratsScreen) {
      this.showCongratsScreen = this.congratsScreen;
      this.welcomeQuizScreen = false;
      this.questionAnswers = this.quizResults;
    } else {
      this.numberOfQuestions =
        this.currentSelectedTopic?.quizQuestionAnswer?.quizQuestions?.length;
      this.quizQuestions =
        this.currentSelectedTopic?.quizQuestionAnswer?.quizQuestions;
        if(this.numberOfQuestions == 1){
          this.quizBtnText = 'Submit';
        }
        this.passingCriteria = this.currentSelectedTopic?.passingCriteria;
        this.durationInMinutes = this.currentSelectedTopic?.durationInMinutes;
      this.resetAnswers();
      // this.startTimer();
    }
  }

  resetAnswers() {
    this.quizQuestions.forEach((x: any) =>
      x.quizAnswers.forEach((y: any) => (y.active = false))
    );
  }

  retryQuiz() {
    this.index = 0;
    this.showQuizScreen = false;
    this.welcomeQuizScreen = true;
    this.rightAnswer = false;
    this.wrongAnswer = false;
    this.showCongratsScreen = false;
    this.showFailScreen = false;
    this.isSubmit = false;
    this.numberOfCorrectAnswers = 0;
    this.resetAnswers();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['currentSelectedTopic']) {
      this.numberOfQuestions =
        this.currentSelectedTopic?.quizQuestionAnswer?.quizQuestions?.length;
      this.quizQuestions =
      this.currentSelectedTopic?.quizQuestionAnswer?.quizQuestions;
      this.passingCriteria = this.currentSelectedTopic?.passingCriteria;
      this.durationInMinutes = this.currentSelectedTopic?.durationInMinutes;
      this.retryQuiz();
    }
  }

  ngOnDestroy() {
    this.stopTimer();
  }

  startQuiz() {
    this.showQuizScreen = true;
    this.welcomeQuizScreen = false;
    this.index = 0;
    this.numberOfCorrectAnswers = 0;
    this.rightAnswer = false;
    this.wrongAnswer = false;
    this.isSubmit = false;
    this.selectedAnswerId = null;
    this.userAnswers = []; 
    this.selectedAnswers = []; 
    this.questionAnswers = []; 
    this.quizBtnText = this.numberOfQuestions == 1 ? 'Submit' : 'Next';
  
    this.resetAnswers();
  
    this.durationInMinutes;
    this.stopTimer();    
    this.startTimer();  
  }
  
  stopTimer() {
    if (this.interval) {
      clearInterval(this.interval);
      this.interval = null;
    }
  }

  selectedAnswer(option: any) {
    this.selectedAnswerId = option.answerId;
    if (
      this.quizQuestions[this.index].questionType === 'SINGLE_CHOICE' ||
      this.quizQuestions[this.index].questionType === 'TRUE_FALSE'
    ) {
      // Ensure only one is active
      this.quizQuestions[this.index].quizAnswers.forEach((x: any) => {
        x.active = x.answerId === option.answerId;
      });
    }
    this.submitAnswer();
    // else if(this.quizQuestions[this.index].questionType === 'MULTIPLE_CHOICE'){
    //   this.submitAnswer();
    // }
  }

  submitAnswer() {
    if (this.selectedAnswerId) {
      let question =
        this.currentSelectedTopic?.quizQuestionAnswer?.quizQuestions[
          this.index
        ];
      let answer = {
        questionId: question?.questionId,
        questionType: question?.questionType,
        answerId: [],
        submitted: false
      };

      if (
        this.userAnswers?.length > 0 &&
        this.userAnswers?.some(
          (ua?: any) => ua.questionId == question?.questionId
        )
      ) {
        this.userAnswers?.forEach((el?: any) => {
          if (
            el?.questionId ==
            this.currentSelectedTopic?.quizQuestionAnswer?.quizQuestions[
              this.index
            ].questionId
          ) {
            if (el?.questionType != this.questionType?.MULTIPLE_CHOICE) {
              el.answerId = [];
              el.answerId.push(this.selectedAnswerId);
            } else if (
              !el?.answerId?.some((ans?: any) => ans == this.selectedAnswerId)
            ) {
              el.answerId.push(this.selectedAnswerId);
            } else {
              el.answerId = el?.answerId?.filter(
                (num?: any) => num !== this.selectedAnswerId
              );
            }
          }
        });
      } else {
        answer.answerId.push(this.selectedAnswerId);
        this.userAnswers.push(answer);
      }
      this.isSubmit = true;
    } else {
      this._message.error('Select any answer from the question');
    }
  }

  nextQuestion() {
    if (this.selectedAnswerId != null) {
      this.correctAnswerId = null;
  
      this.quizBtnText = this.index == this.numberOfQuestions - 2 ? 'Submit' : 'Next';
      this.userAnswers[this.index].submitted = true;
  
      if (this.index < this.numberOfQuestions - 1) {
        this.index++;
        this.isSubmit = false;
        this.rightAnswer = false;
        this.wrongAnswer = false;
  
        const nextAnswer = this.userAnswers.find(
          (answer: any) =>
            answer.questionId === this.quizQuestions[this.index].questionId
        );
  
        this.selectedAnswerId = nextAnswer ? nextAnswer.answerId[0] : null; 
      } else {
        let percentage = (this.numberOfCorrectAnswers / this.numberOfQuestions) * 100;
        this.rightAnswer = false;
        this.wrongAnswer = false;
        this.showQuizScreen = false;
  
        // Filter only attempted answers
        this.userAnswers = this.userAnswers.filter((ua?: any) => ua.submitted && ua.answerId.length > 0);
        
        this._courseService.submitQuizAnswers(this.userAnswers).subscribe({
          next: (response: any) => {
            this.stopTimer();
            this._message.success('Quiz submitted successfully!');
            this.questionAnswers = response.data;
            this.showCongratsScreen = true;
          },
          error: (error: any) => {
            this.stopTimer();
            this._message.error('Failed to submit quiz. Try again.');
          },
        });
      }
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });

  }
  


  skipQuestion() {
    this.correctAnswerId = null;
    if (this.index < this.numberOfQuestions - 1) {
      this.index++;
      this.isSubmit = false;
      this.rightAnswer = false;
      this.wrongAnswer = false;
    } else {
      let percentage =
        (this.numberOfCorrectAnswers / this.numberOfQuestions) * 100;
      this.rightAnswer = false;
      this.wrongAnswer = false;
      this.showQuizScreen = false;
      percentage > 70
        ? (this.showCongratsScreen = true)
        : (this.showFailScreen = true);
    }
  }

  continue() {
    this.continueQuizEmitter.emit(true);
  }

  skipQuiz() {
    this.skipQuizEmitter.emit(true);
  }


  startTimer() {
    this.stopTimer(); 
  
    this.minutes = this.durationInMinutes ?? 0;
    this.seconds = 0;
  
    this.interval = setInterval(() => {
      if (this.seconds > 0) {
        this.seconds--;
      } else {
        if (this.minutes > 0) {
          this.minutes--;
          this.seconds = 59;
        } else {
          this.stopTimer();
          this.finishQuiz(true); // Handle unanswered questions
        }
      }
      this._cdr.detectChanges(); // Force UI update
    }, 1000);
  }
  
  
  
  

 finishQuiz(isTimeout: boolean = false) {

  if(this.selectedAnswerId !== null){
  this.stopTimer(); 
 }
  const currentQuestion = this.quizQuestions[this.index];

  if (!currentQuestion && !isTimeout) {
      console.error("No current question found at index:", this.index);
      return;
    }

    let existingAnswerIndex = this.userAnswers.findIndex(
      (ans: any) => ans.questionId === currentQuestion?.questionId
    );

  if (existingAnswerIndex !== -1) {
        this.userAnswers[existingAnswerIndex].answerId = this.selectedAnswerId !== null ? [this.selectedAnswerId] : null;
      this.userAnswers[existingAnswerIndex].submitted = true;
    } else if (this.selectedAnswerId !== null) {
      this.userAnswers.push({
        questionId: currentQuestion.questionId,
        answerId: [this.selectedAnswerId],
        questionType: currentQuestion.questionType,
        submitted: true,
      });
    }
  
    // **Check if all answers are empty before submitting**
    const hasValidAnswers = this.userAnswers.some((answer?: any) => answer?.answerId !== null && answer?.submitted);
  
    if (!hasValidAnswers) {
      this._message.error("No answers submitted! Redirecting to start screen...");
      
      // **Reset quiz states and redirect to start screen**
      this.showQuizScreen = false;
      this.welcomeQuizScreen = true;
      this.userAnswers = []; // Reset answers
      return;
    }
  
    if (isTimeout || hasValidAnswers) {
      clearInterval(this.interval);
  
      let finalSubmission = this.quizQuestions.map((q: any) => {
        const userAnswer = this.userAnswers.find((ua: any) => ua.questionId === q.questionId);
        return {
          questionId: q.questionId,
          answerId: userAnswer ? userAnswer.answerId : null,
          questionType: q.questionType,
          submitted: !!userAnswer
        };
      });
      this.rightAnswer = false;
      this.wrongAnswer = false;
      this.showQuizScreen = false;
      this.userAnswers = this.userAnswers.filter((ua?: any) => ua.submitted);
      this._courseService.submitQuizAnswers(finalSubmission).subscribe({ next: (response: any) => {
          this._message.success("Quiz submitted successfully!");
          this.questionAnswers = response.data;
          this.showCongratsScreen = true;
        },
        error: (error: any) => {
          console.error("Quiz submission error:", error);
          this._message.error("Failed to submit quiz. Try again.");
        },
      });
  } else {
    this._message.error("At least 1 question should be submitted");
  }
}

  

  previousQuestion(): void {
    if (this.index > 0) {
      this.quizBtnText = 'Next';
      this.index--;
      this.resetQuestionState();
  
      const previousAnswer = this.userAnswers.find(
        (answer: any) =>
          answer.questionId === this.quizQuestions[this.index].questionId
      );
  
      if (previousAnswer) {
        this.selectedAnswerId = previousAnswer.answerId[0]; 
      }
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });

  }
  

  resetQuestionState(): void {
    this.selectedAnswerId = null;
    this.correctAnswerId = null;
    this.wrongAnswer = false;
    this.rightAnswer = false;
  }
  selectedMultipleAnswers(option: any) {
    if (this.quizQuestions[this.index].questionType === 'MULTIPLE_CHOICE') {
      option.active = !option.active; // Toggle active state

      if (option.active) {
        this.selectedAnswers.push(option.answerId);
      } else {
        this.selectedAnswers = this.selectedAnswers.filter(
          (id) => id !== option.answerId
        );
      }
    }
  }

  reviewQuiz() {
    this.reviewCallBack.emit(this.questionAnswers);
  }

  getOptionLabel(index: number): string {
    return String.fromCharCode(65 + index); // A, B, C, D...
  }

  
  isCurrentQuestionUnanswered(): boolean {
    const currentQuestion = this.quizQuestions[this.index];
    const userAnswer = this.userAnswers[this.index];
  
    if (!userAnswer) return true;
  
    if (
      currentQuestion.questionType === this.questionType.SINGLE_CHOICE ||
      currentQuestion.questionType === this.questionType.TRUE_FALSE
    ) {
      return !userAnswer.answerId || userAnswer.answerId.length === 0;
    }
      return !userAnswer.answerId || userAnswer.answerId.length < 1;
  }
  
  
}

