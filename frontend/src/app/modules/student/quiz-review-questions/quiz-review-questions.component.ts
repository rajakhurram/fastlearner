import { Component, EventEmitter, Input, Output } from '@angular/core';
import { QuestionType } from 'src/app/core/enums/question-type';
import { AnswerResponse, QuestionAnswerResponse, QuizReview } from 'src/app/core/models/quiz-review.model';

@Component({
  selector: 'app-quiz-review-questions',
  templateUrl: './quiz-review-questions.component.html',
  styleUrls: ['./quiz-review-questions.component.scss'],
})
export class QuizReviewQuestionsComponent {
  correctAnswers: any;
  @Input() quizData: QuizReview;
  @Output() backToQuizEvent = new EventEmitter<any>();
  @Output() retakeQuizEvent = new EventEmitter<any>();
  @Input() currentSelectedTopic?: any;
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
  isSubmit: boolean = false;
  questionType = QuestionType;
  correctAnswerSelectedText?: string = 'Your selection is corrected';
  incorrectAnswerSelectedText?: string = 'Your answer is incorrect';
  nonSelectedCorrecAnswer?: string = 'Correct answer';

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

  constructor() {}

  ngOnInit(): void {
    console.log(this.quizData);
    
  // this.quizData =  {
  //   percentage: 100.0,
  //   passingCriteria: 0.0,
  //   totalQuestion: 2,
  //   totalCorrectAnswer: 2,
  //   questionAnswerResponses: [
  //     {
  //       questionText: 'What is the output of the following code?',
  //       correctAnswerId: [373],
  //       selectedAnswerId: [373],
  //       questionType: 'SINGLE_CHOICE',
  //       explanation: 'ABC',
  //       answerResponseList: [
  //         {
  //           answerId: 371,
  //           answerText: 'a: 3 b: 7 c: 10',
  //         },
  //         {
  //           answerId: 372,
  //           answerText: 'a: 3 b: 5 c: 7',
  //         },
  //         {
  //           answerId: 373,
  //           answerText: 'a: 3 b: 7 c: 5',
  //         },
  //         {
  //           answerId: 374,
  //           answerText: 'a: 7 b: 3 c: 10',
  //         },
  //       ],
  //       correct: false,
  //     },
  //     {
  //       questionText: 'what is java',
  //       correctAnswerId: [848],
  //       selectedAnswerId: [848],
  //       questionType: 'MULTIPLE_CHOICE',
  //       explanation: 'XYZ',
  //       answerResponseList: [
  //         {
  //           answerId: 848,
  //           answerText: 'language',
  //         },
  //         {
  //           answerId: 849,
  //           answerText: 'programming language',
  //         },
  //         {
  //           answerId: 850,
  //           answerText: 'none',
  //         },
  //       ],
  //       correct: true,
  //     },
  //   ],
  // };
  this.filteredQuestions = this.quizData?.questionAnswerResponses || [];
    this.applyAnswerValidation();
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
}
