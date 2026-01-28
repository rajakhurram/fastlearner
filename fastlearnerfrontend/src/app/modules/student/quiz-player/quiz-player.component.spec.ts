import { ComponentFixture, TestBed } from '@angular/core/testing';
import { QuizPlayerComponent } from './quiz-player.component';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { of, throwError } from 'rxjs';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

const userAnswers = [
  {
    answerId: 1,
    answerText: "Some text for answer",
    courseId: 14,
    questionId: 1,
    questionType: 'SINGLE_CHOICE',
    submitted: false,
    timeZone: 'Asia/Karachi',
  },
  {
    answerId: 2,
    answerText: "Some other text for answer",
    courseId: 14,
    questionId: 2,
    questionType: 'SINGLE_CHOICE',
    submitted: false,
    timeZone: 'Asia/Karachi',
  },
];

const quizQuestions = [
  {
    questionId: 1,
  },
  {
    questionId: 2,
  },
  {
    questionId: 3,
  }
]

describe('QuizPlayerComponent', () => {
  let component: QuizPlayerComponent;
  let fixture: ComponentFixture<QuizPlayerComponent>;
  let courseService: jasmine.SpyObj<CourseService>;
  let messageService: jasmine.SpyObj<MessageService>;

  beforeEach(async () => {
    const courseServiceSpy = jasmine.createSpyObj<CourseService>('CourseService', [
      'validateQuizAnswer',
      'submitQuizAnswers',
      'getQuizAttempt',
    ]);
    courseServiceSpy.submitQuizAnswers.and.returnValue(of({data: []}))
    const messageServiceSpy = jasmine.createSpyObj<MessageService>('MessageService', ['error', 'success']);

    await TestBed.configureTestingModule({
      declarations: [QuizPlayerComponent],
      providers: [
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(QuizPlayerComponent);
    component = fixture.componentInstance;
    courseService = TestBed.inject(
      CourseService
    ) as jasmine.SpyObj<CourseService>;
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
  });

  it('should initialize quiz questions and number of questions on ngOnInit', () => {
    component.currentSelectedTopic = {
      quizQuestionAnswer: {
        quizQuestions: [{ questionId: 1, quizAnswers: [] }],
      },
    };
    courseService.getQuizAttempt.and.returnValue(of({data: null}));
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.numberOfQuestions).toBe(1);
    expect(component.quizQuestions.length).toBe(1);
  });

  // it('should update quiz questions and reset quiz on ngOnChanges', () => {
  //   spyOn(component, 'retryQuiz').and.callThrough();
  //   component.currentSelectedTopic = {
  //     quizQuestionAnswer: {
  //       quizQuestions: [{ questionId: 1, quizAnswers: [] }]
  //     }
  //   };
  //   component.ngOnChanges({
  //     currentSelectedTopic: { currentValue: component.currentSelectedTopic }
  //   });
  //   expect(component.retryQuiz).toHaveBeenCalled();
  //   expect(component.numberOfQuestions).toBe(1);
  //   expect(component.quizQuestions.length).toBe(1);
  // });

  it('should reset the quiz state on retryQuiz', () => {
    component.numberOfCorrectAnswers = 5;
    component.showQuizScreen = true;
    component.showCongratsScreen = true;
    component.retryQuiz();
    expect(component.index).toBe(0);
    expect(component.showQuizScreen).toBeFalse();
    expect(component.welcomeQuizScreen).toBeTrue();
    expect(component.rightAnswer).toBeFalse();
    expect(component.wrongAnswer).toBeFalse();
    expect(component.showCongratsScreen).toBeFalse();
    expect(component.showFailScreen).toBeFalse();
    expect(component.isSubmit).toBeFalse();
    expect(component.numberOfCorrectAnswers).toBe(0);
  });

  it('should start the quiz on startQuiz', () => {
    component.startQuiz();
    expect(component.showQuizScreen).toBeTrue();
    expect(component.welcomeQuizScreen).toBeFalse();
  });

  it('should select an answer on selectedAnswer (SINGLE_CHOICE)', () => {
    component.quizQuestions = [
      {
        questionId: 1,
        questionType: 'SINGLE_CHOICE',
        quizAnswers: [
          { answerId: 1, active: false },
          { answerId: 2, active: false },
        ],
      },
    ];
    const answer = { answerId: 1 };
    component.selectedAnswer(answer);
    expect(component.selectedAnswerId).toBe(1);
    expect(component.quizQuestions[0].quizAnswers[0].active).toBeTrue();
    expect(component.quizQuestions[0].quizAnswers[1].active).toBeFalse();
  });
  
  it('should select an answer on selectedAnswer (TRUE_FALSE)', () => {
    component.quizQuestions = [
      {
        questionId: 1,
        questionType: 'TRUE_FALSE',
        quizAnswers: [
          { answerId: 1, active: false },
          { answerId: 2, active: false },
        ],
      },
    ];
    const answer = { answerId: 1 };
    component.selectedAnswer(answer);
    expect(component.selectedAnswerId).toBe(1);
    expect(component.quizQuestions[0].quizAnswers[0].active).toBeTrue();
    expect(component.quizQuestions[0].quizAnswers[1].active).toBeFalse();
  });

  it('should submit answer and update userAnswers and isSubmit', () => {
    component.index = 0;
    component.currentSelectedTopic = {
      quizQuestionAnswer: {
        quizQuestions: [
          { questionId: 1, questionType: 'SINGLE_CHOICE' }
        ]
      }
    };

    const timeZone = 'Asia/Karachi';
    component.userAnswers = [];
    component.courseId = 10;
    component.selectedAnswerId = 1;

    component.submitAnswer();

    expect(component.isSubmit).toBeTrue();
    expect(component.userAnswers.length).toBe(1);
    expect(component.userAnswers[0].answerId).toEqual([1]);
    expect(component.userAnswers[0].questionId).toBe(1);
  });


  it('should show error message if no answer selected on submitAnswer', () => {
    component.selectedAnswerId = null;
    component.submitAnswer();
    expect(messageService.error).toHaveBeenCalledWith(
      'Select any answer from the question'
    );
  });

  it('should move to the next question or show result on nextQuestion', () => {
    component.userAnswers = userAnswers;
    component.quizQuestions = quizQuestions;
    component.numberOfQuestions = 2;
    component.numberOfCorrectAnswers = 1;
    component.selectedAnswerId = 1;
    component.index = 0;
    component.nextQuestion();
    expect(component.index).toBe(1);
    expect(component.isSubmit).toBeFalse();
    expect(component.rightAnswer).toBeFalse();
    expect(component.wrongAnswer).toBeFalse();
    expect(component.showCongratsScreen).toBeFalse();
    expect(component.showFailScreen).toBeFalse();
  });

  it('should show result if no more questions on nextQuestion', () => {
    component.userAnswers = userAnswers;
    component.quizQuestions = quizQuestions;
    component.numberOfQuestions = 2;
    component.numberOfCorrectAnswers = 1;
    component.selectedAnswerId = 1;
    component.index = 1;
    component.nextQuestion();
    expect(component.correctAnswerId).toBeNull();
  });

  it('should skip to the next question or show result on skipQuestion', () => {
    component.numberOfQuestions = 2;
    component.index = 0;
    component.skipQuestion();
    expect(component.index).toBe(1);
    expect(component.isSubmit).toBeFalse();
    expect(component.rightAnswer).toBeFalse();
    expect(component.wrongAnswer).toBeFalse();
    expect(component.showCongratsScreen).toBeFalse();
    expect(component.showFailScreen).toBeFalse();
  });

  it('should show result if no more questions on skipQuestion', () => {
    component.numberOfQuestions = 2;
    component.index = 1;
    component.skipQuestion();
    expect(component.showQuizScreen).toBeFalse();
    expect(component.correctAnswerId).toBeNull();
  });

  it('should emit continueQuizEmitter on continue', () => {
    spyOn(component.continueQuizEmitter, 'emit');
    component.continue();
    expect(component.continueQuizEmitter.emit).toHaveBeenCalledWith(true);
  });

  it('should emit skipQuizEmitter on skipQuiz', () => {
    spyOn(component.skipQuizEmitter, 'emit');
    component.skipQuiz();
    expect(component.skipQuizEmitter.emit).toHaveBeenCalledWith(true);
  });
});
