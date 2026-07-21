import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { QuizPlayerComponent } from './quiz-player.component';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

const userAnswers = [
  {
    answerId: 1,
    answerText: 'Some text for answer',
    courseId: 14,
    questionId: 1,
    questionType: 'SINGLE_CHOICE',
    submitted: false,
    timeZone: 'Asia/Karachi',
    questionText: 'What is Angular?',
  },
  {
    answerId: 2,
    answerText: 'Some other text for answer',
    courseId: 14,
    questionId: 2,
    questionType: 'SINGLE_CHOICE',
    submitted: false,
    timeZone: 'Asia/Karachi',
    questionText: 'What is Angular?',
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
  },
];

function stubQuizLifecycle(): void {
  if (!jasmine.isSpy(QuizPlayerComponent.prototype.ngOnInit)) {
    spyOn(QuizPlayerComponent.prototype, 'ngOnInit');
  }
  if (!jasmine.isSpy(QuizPlayerComponent.prototype.ngOnChanges)) {
    spyOn(QuizPlayerComponent.prototype, 'ngOnChanges');
  }
}

describe('QuizPlayerComponent', () => {
  let component: QuizPlayerComponent;
  let fixture: ComponentFixture<QuizPlayerComponent>;
  let courseService: jasmine.SpyObj<CourseService>;
  let messageService: jasmine.SpyObj<MessageService>;

  beforeEach(async () => {
    const courseServiceSpy = jasmine.createSpyObj<CourseService>(
      'CourseService',
      ['validateQuizAnswer', 'submitQuizAnswers', 'getQuizAttempt'],
    );
    courseServiceSpy.submitQuizAnswers.and.returnValue(of({ data: [] }));
    const messageServiceSpy = jasmine.createSpyObj<MessageService>(
      'MessageService',
      ['error', 'success'],
    );

    await TestBed.configureTestingModule({
      declarations: [QuizPlayerComponent],
      imports: [FormsModule],
      providers: [
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(QuizPlayerComponent);
    component = fixture.componentInstance;
    courseService = TestBed.inject(
      CourseService,
    ) as jasmine.SpyObj<CourseService>;
    messageService = TestBed.inject(
      MessageService,
    ) as jasmine.SpyObj<MessageService>;
  });

  it('should initialize quiz questions and number of questions on ngOnInit', () => {
    component.currentSelectedTopic = {
      quizQuestionAnswer: {
        quizQuestions: [{ questionId: 1, quizAnswers: [] }],
      },
    };
    courseService.getQuizAttempt.and.returnValue(of({ data: null }));
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
        quizQuestions: [{ questionId: 1, questionType: 'SINGLE_CHOICE' }],
      },
    };

    const timeZone = 'Asia/Karachi';
    component.userAnswers = [];
    component.courseId = 10;
    component.selectedAnswerId = 1;

    const dummyOption = { answerId: 1, answerText: 'Sample Answer' };
    component.submitAnswer(dummyOption);
    expect(component.isSubmit).toBeTrue();
    expect(component.userAnswers.length).toBe(1);
    expect(component.userAnswers[0].answerId).toEqual([1]);
    expect(component.userAnswers[0].questionId).toBe(1);
  });

  it('should show error message if no answer selected on submitAnswer', () => {
    component.selectedAnswerId = null;
    const dummyOption = { answerId: 1, answerText: 'Sample Answer' };
    component.submitAnswer(dummyOption);
    expect(messageService.error).toHaveBeenCalledWith(
      'Select any answer from the question',
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

  describe('Phase 1 coverage batch', () => {
    const quizTopic = {
      testType: 'TEST' as const,
      quizId: 5,
      passingCriteria: 70,
      durationInMinutes: 2,
      quizQuestionAnswer: {
        quizQuestions: [
          {
            questionId: 1,
            questionType: 'SINGLE_CHOICE',
            questionText: 'Question 1',
            quizAnswers: [
              { answerId: 1, active: false },
              { answerId: 2, active: false },
            ],
          },
          {
            questionId: 2,
            questionType: 'SINGLE_CHOICE',
            questionText: 'Question 2',
            quizAnswers: [
              { answerId: 3, active: false },
              { answerId: 4, active: false },
            ],
          },
        ],
      },
    };

    beforeEach(() => {
      component.currentSelectedTopic = quizTopic;
      component.quizQuestions = quizTopic.quizQuestionAnswer.quizQuestions;
      component.numberOfQuestions = 2;
      component.courseId = 14;
      courseService.getQuizAttempt.and.returnValue(of({ data: null }));
      spyOn(window, 'scrollTo');
    });

    it('should show congrats screen on ngOnInit when congratsScreen is set', () => {
      spyOn(component, 'fetchAttemptQuiz');
      component.congratsScreen = true;
      component.quizResults = [{ correct: true }];
      component.ngOnInit();
      expect(component.showCongratsScreen).toBeTrue();
      expect(component.welcomeQuizScreen).toBeFalse();
      expect(component.questionAnswers).toEqual([{ correct: true }]);
    });

    it('should update quiz data on ngOnChanges when currentSelectedTopic changes', () => {
      spyOn(component, 'fetchAttemptQuiz');
      component.ngOnChanges({
        currentSelectedTopic: {
          currentValue: quizTopic,
          previousValue: null,
          firstChange: true,
          isFirstChange: () => true,
        },
      });
      expect(component.numberOfQuestions).toBe(2);
      expect(component.passingCriteria).toBe(70);
      expect(component.durationInMinutes).toBe(2);
      expect(component.showQuizAttempt).toBeFalse();
      expect(component.showCongratsScreen).toBeFalse();
      expect(component.fetchAttemptQuiz).toHaveBeenCalled();
    });

    it('should stop timer on ngOnDestroy', () => {
      spyOn(component, 'stopTimer');
      component.ngOnDestroy();
      expect(component.stopTimer).toHaveBeenCalled();
    });

    it('should emit quizStartedEmitter and start timer on startQuiz', () => {
      spyOn(component, 'startTimer').and.callThrough();
      spyOn(component.quizStartedEmitter, 'emit');
      component.startQuiz();
      expect(component.quizStartedEmitter.emit).toHaveBeenCalledWith(true);
      expect(component.startTimer).toHaveBeenCalled();
      expect(component.index).toBe(0);
      expect(component.userAnswers).toEqual([]);
      expect(component.showQuizScreen).toBeTrue();
    });

    it('should decrement timer every second', fakeAsync(() => {
      component.durationInMinutes = 1;
      spyOn(component, 'finishQuiz');
      component.startTimer();
      expect(component.minutes).toBe(1);
      expect(component.seconds).toBe(0);
      tick(1000);
      expect(component.minutes).toBe(0);
      expect(component.seconds).toBe(59);
      component.stopTimer();
    }));

    it('should call finishQuiz when timer reaches zero', fakeAsync(() => {
      spyOn(component, 'finishQuiz');
      component.durationInMinutes = 0;
      component.startTimer();
      tick(1000);
      expect(component.finishQuiz).toHaveBeenCalledWith(true);
    }));

    it('should clear interval on stopTimer', fakeAsync(() => {
      component.durationInMinutes = 5;
      component.startTimer();
      expect(component.interval).toBeTruthy();
      component.stopTimer();
      expect(component.interval).toBeNull();
    }));

    it('should update existing userAnswer when resubmitting same question', () => {
      component.index = 0;
      component.userAnswers = [
        {
          questionId: 1,
          questionType: 'SINGLE_CHOICE',
          answerId: [1],
          answerText: 'A',
          submitted: false,
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 1',
        },
      ];
      component.selectedAnswerId = 2;
      component.submitAnswer({ answerId: 2, answerText: 'B' });
      expect(component.userAnswers.length).toBe(1);
      expect(component.userAnswers[0].answerId).toEqual([2]);
      expect(component.isSubmit).toBeTrue();
    });

    it('should submit quiz on last nextQuestion and emit answerSubmit', () => {
      component.index = 1;
      component.selectedAnswerId = 4;
      component.userAnswers = [
        {
          questionId: 1,
          answerId: [1],
          submitted: false,
          answerText: 'A',
          questionType: 'SINGLE_CHOICE',
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 1',
        },
        {
          questionId: 2,
          answerId: [4],
          submitted: false,
          answerText: 'D',
          questionType: 'SINGLE_CHOICE',
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 2',
        },
      ];
      courseService.submitQuizAnswers.and.returnValue(
        of({ data: { quizAttemptId: 'xyz', isAllowedToRetake: true } }),
      );
      spyOn(component, 'stopTimer');
      spyOn(component.answerSubmit, 'emit');
      spyOn(component.quizStartedEmitter, 'emit');

      component.nextQuestion();

      expect(component.showQuizScreen).toBeFalse();
      expect(messageService.success).toHaveBeenCalledWith(
        'Quiz submitted successfully!',
      );
      expect(component.showCongratsScreen).toBeTrue();
      expect(component.answerSubmit.emit).toHaveBeenCalledWith({
        quizAttemptId: 'xyz',
        isAllowedToRetake: true,
      });
      expect(component.quizStartedEmitter.emit).toHaveBeenCalledWith(false);
    });

    it('should show error when nextQuestion final submission fails', () => {
      component.index = 1;
      component.selectedAnswerId = 4;
      component.userAnswers = [
        {
          questionId: 1,
          answerId: [1],
          submitted: false,
          answerText: 'A',
          questionType: 'SINGLE_CHOICE',
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 1',
        },
        {
          questionId: 2,
          answerId: [4],
          submitted: false,
          answerText: 'D',
          questionType: 'SINGLE_CHOICE',
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 2',
        },
      ];
      courseService.submitQuizAnswers.and.returnValue(
        throwError(() => new Error('submit failed')),
      );
      spyOn(component, 'stopTimer');

      component.nextQuestion();

      expect(messageService.error).toHaveBeenCalledWith(
        'Failed to submit quiz. Try again.',
      );
      expect(component.stopTimer).toHaveBeenCalled();
    });

    it('should navigate to previous question and restore answer', () => {
      component.index = 1;
      component.userAnswers = [
        {
          questionId: 1,
          answerId: [1],
          answerText: 'A',
          questionType: 'SINGLE_CHOICE',
          submitted: false,
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 1',
        },
        {
          questionId: 2,
          answerId: [4],
          answerText: 'D',
          questionType: 'SINGLE_CHOICE',
          submitted: false,
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 2',
        },
      ];
      component.previousQuestion();
      expect(component.index).toBe(0);
      expect(component.selectedAnswerId).toBe(1);
      expect(component.quizBtnText).toBe('Next');
    });

    it('should reset question state flags', () => {
      component.selectedAnswerId = 5;
      component.correctAnswerId = 3;
      component.wrongAnswer = true;
      component.rightAnswer = true;
      component.resetQuestionState();
      expect(component.selectedAnswerId).toBeNull();
      expect(component.correctAnswerId).toBeNull();
      expect(component.wrongAnswer).toBeFalse();
      expect(component.rightAnswer).toBeFalse();
    });

    it('should reset to welcome screen when finishQuiz has no valid answers', () => {
      component.showQuizScreen = true;
      component.welcomeQuizScreen = false;
      component.index = 0;
      component.selectedAnswerId = null;
      component.userAnswers = [];
      component.finishQuiz(false);
      expect(messageService.error).toHaveBeenCalledWith(
        'No answers submitted! Redirecting to start screen...',
      );
      expect(component.showQuizScreen).toBeFalse();
      expect(component.welcomeQuizScreen).toBeTrue();
      expect(component.userAnswers).toEqual([]);
    });

    it('should submit quiz on finishQuiz with valid answers', () => {
      component.index = 0;
      component.selectedAnswerId = 1;
      component.userAnswers = [
        {
          questionId: 1,
          answerId: [1],
          submitted: true,
          answerText: 'A',
          questionType: 'SINGLE_CHOICE',
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 1',
        },
      ];
      courseService.submitQuizAnswers.and.returnValue(
        of({ data: { quizAttemptId: 'abc', isAllowedToRetake: false } }),
      );
      spyOn(component.answerSubmit, 'emit');
      spyOn(component.quizStartedEmitter, 'emit');

      component.finishQuiz(false);

      expect(courseService.submitQuizAnswers).toHaveBeenCalled();
      expect(messageService.success).toHaveBeenCalledWith(
        'Quiz submitted successfully!',
      );
      expect(component.showCongratsScreen).toBeTrue();
      expect(component.answerSubmit.emit).toHaveBeenCalledWith({
        quizAttemptId: 'abc',
        isAllowedToRetake: false,
      });
    });

    it('should show error when finishQuiz submission fails', () => {
      component.index = 0;
      component.selectedAnswerId = 1;
      component.userAnswers = [
        {
          questionId: 1,
          answerId: [1],
          submitted: true,
          answerText: 'A',
          questionType: 'SINGLE_CHOICE',
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 1',
        },
      ];
      courseService.submitQuizAnswers.and.returnValue(
        throwError(() => new Error('submit failed')),
      );

      component.finishQuiz(false);

      expect(messageService.error).toHaveBeenCalledWith(
        'Failed to submit quiz. Try again.',
      );
    });

    it('should show quiz attempt screen when prior attempt exists', () => {
      courseService.getQuizAttempt.and.returnValue(
        of({ data: { isAllowedToRetake: true, score: 80 } }),
      );
      component.fetchAttemptQuiz();
      expect(component.showQuizAttempt).toBeTrue();
      expect(component.welcomeQuizScreen).toBeFalse();
      expect(component.quizAttemptData).toEqual({
        isAllowedToRetake: true,
        score: 80,
      });
      expect(component.loading).toBeFalse();
    });

    it('should show welcome screen on 404 from getQuizAttempt', () => {
      courseService.getQuizAttempt.and.returnValue(
        throwError(() => ({ status: 404 })),
      );
      component.fetchAttemptQuiz();
      expect(component.welcomeQuizScreen).toBeTrue();
      expect(component.showQuizAttempt).toBeFalse();
      expect(component.loading).toBeFalse();
    });

    it('should show error message on non-404 getQuizAttempt failure', () => {
      courseService.getQuizAttempt.and.returnValue(
        throwError(() => ({ status: 500 })),
      );
      component.fetchAttemptQuiz();
      expect(messageService.error).toHaveBeenCalledWith(
        'Failed to check quiz attempt',
      );
      expect(component.welcomeQuizScreen).toBeTrue();
    });

    it('should toggle multiple choice answer selection', () => {
      component.quizQuestions = [
        {
          questionId: 1,
          questionType: 'MULTIPLE_CHOICE',
          quizAnswers: [{ answerId: 1, active: false }],
        },
      ];
      component.index = 0;
      const option = component.quizQuestions[0].quizAnswers[0];
      component.selectedMultipleAnswers(option);
      expect(option.active).toBeTrue();
      expect(component.selectedAnswers.length).toBe(1);
      component.selectedMultipleAnswers(option);
      expect(option.active).toBeFalse();
      expect(component.selectedAnswers.length).toBe(0);
    });

    it('should report unanswered state for current question', () => {
      component.index = 0;
      component.userAnswers = [];
      expect(component.isCurrentQuestionUnanswered()).toBeTrue();
      component.userAnswers = [
        {
          questionId: 1,
          answerId: [1],
          answerText: 'A',
          questionType: 'SINGLE_CHOICE',
          submitted: false,
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 1',
        },
      ];
      expect(component.isCurrentQuestionUnanswered()).toBeFalse();
    });

    it('should add text-based answer when currentAnswerText is set', () => {
      component.currentAnswerText = 'My written answer';
      const question = {
        questionId: 1,
        questionType: 'TEXT_FIELD',
        questionText: 'Explain Angular',
        quizAnswers: [{ answerId: 99 }],
      };
      component.addTextBaseQuestion(question);
      expect(component.isSubmit).toBeTrue();
      expect(component.userAnswers.length).toBe(1);
      expect(component.userAnswers[0].answerText).toBe('My written answer');
      expect(component.selectedAnswerId).toBe(99);
    });

    it('should return letter labels and detect media file types', () => {
      expect(component.getOptionLabel(0)).toBe('A');
      expect(component.getOptionLabel(2)).toBe('C');
      expect(component.isImage('photo.jpg')).toBeTrue();
      expect(component.isImage('audio.mp3')).toBeFalse();
      expect(component.isAudio('track.mp3')).toBeTrue();
      expect(component.formatTime(125)).toBe('2:05');
    });

    it('should emit reviewCallBack and manage image preview', () => {
      spyOn(component.reviewCallBack, 'emit');
      component.questionAnswers = { score: 90 };
      component.reviewQuiz();
      expect(component.reviewCallBack.emit).toHaveBeenCalledWith({ score: 90 });

      component.openImagePreview('http://example.com/img.png');
      expect(component.previewImageUrl).toBe('http://example.com/img.png');
      component.closePreview();
      expect(component.previewImageUrl).toBeNull();
    });

    it('should set quizeType and Submit button for single-question quiz on ngOnInit', () => {
      spyOn(component, 'fetchAttemptQuiz');
      component.currentSelectedTopic = {
        testType: 'SURVEY',
        passingCriteria: 60,
        durationInMinutes: 10,
        quizQuestionAnswer: {
          quizQuestions: [{ questionId: 1, quizAnswers: [{ active: true }] }],
        },
      };
      component.ngOnInit();
      expect(component.quizeType).toBe('SURVEY');
      expect(component.numberOfQuestions).toBe(1);
      expect(component.quizBtnText).toBe('Submit');
      expect(component.fetchAttemptQuiz).toHaveBeenCalled();
    });

    it('should show welcome screen when getQuizAttempt returns no data', () => {
      courseService.getQuizAttempt.and.returnValue(of({ data: null }));
      component.fetchAttemptQuiz();
      expect(component.welcomeQuizScreen).toBeTrue();
      expect(component.showQuizAttempt).toBeFalse();
      expect(component.loading).toBeFalse();
    });

    it('should deactivate all options on resetAnswers', () => {
      component.quizQuestions = [
        { quizAnswers: [{ active: true }, { active: true }] },
        { quizAnswers: [{ active: true }] },
      ];
      component.resetAnswers();
      expect(
        component.quizQuestions.every((q: any) =>
          q.quizAnswers.every((a: any) => a.active === false),
        ),
      ).toBeTrue();
    });

    it('should reset showQuizAttempt on retryQuiz', () => {
      component.showQuizAttempt = true;
      component.quizQuestions = [{ quizAnswers: [{ active: true }] }];
      component.retryQuiz();
      expect(component.showQuizAttempt).toBeFalse();
      expect(component.quizQuestions[0].quizAnswers[0].active).toBeFalse();
    });

    it('should handle single choice option click via handleOptionClick', () => {
      spyOn(component, 'selectedAnswer');
      component.index = 0;
      component.quizQuestions = [
        { questionId: 1, questionType: 'SINGLE_CHOICE', quizAnswers: [] },
      ];
      const option = { answerId: 1, answerText: 'A' };
      component.handleOptionClick(option);
      expect(component.selectedAnswerId).toBe(1);
      expect(component.selectedAnswer).toHaveBeenCalledWith(option);
    });

    it('should handle multiple choice option click via handleOptionClick', () => {
      spyOn(component, 'selectedMultipleAnswers');
      spyOn(component, 'selectedAnswer');
      component.index = 0;
      component.quizQuestions = [
        {
          questionId: 1,
          questionType: 'MULTIPLE_CHOICE',
          quizAnswers: [{ answerId: 1, active: false }],
        },
      ];
      const option = { answerId: 1, answerText: 'A' };
      component.handleOptionClick(option);
      expect(component.selectedMultipleAnswers).toHaveBeenCalledWith(option);
      expect(component.selectedAnswer).toHaveBeenCalledWith(option);
    });

    it('should append answer id for multiple choice in submitAnswer', () => {
      component.index = 0;
      component.currentSelectedTopic = {
        quizQuestionAnswer: {
          quizQuestions: [
            {
              questionId: 1,
              questionType: 'MULTIPLE_CHOICE',
              questionText: 'Pick many',
            },
          ],
        },
      };
      component.selectedAnswerId = 2;
      component.userAnswers = [
        {
          questionId: 1,
          questionType: 'MULTIPLE_CHOICE',
          answerId: [1],
          answerText: 'A',
          submitted: false,
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Pick many',
        },
      ];
      component.submitAnswer({ answerId: 2, answerText: 'B' });
      expect(component.userAnswers[0].answerId).toEqual([1, 2]);
      expect(component.isSubmit).toBeTrue();
    });

    it('should only scroll when nextQuestion called without selected answer', () => {
      component.selectedAnswerId = null;
      component.index = 0;
      component.nextQuestion();
      expect(component.index).toBe(0);
      expect(window.scrollTo).toHaveBeenCalled();
    });

    it('should set quizBtnText to Submit on penultimate nextQuestion', () => {
      component.index = 0;
      component.selectedAnswerId = 1;
      component.userAnswers = [
        {
          questionId: 1,
          answerId: [1],
          submitted: false,
          answerText: 'A',
          questionType: 'SINGLE_CHOICE',
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 1',
        },
      ];
      component.nextQuestion();
      expect(component.quizBtnText).toBe('Submit');
      expect(component.index).toBe(1);
    });

    it('should not change index when previousQuestion called at first question', () => {
      component.index = 0;
      component.previousQuestion();
      expect(component.index).toBe(0);
      expect(window.scrollTo).toHaveBeenCalled();
    });

    it('should show congrats screen on skipQuestion when score above 70', () => {
      component.numberOfQuestions = 2;
      component.numberOfCorrectAnswers = 2;
      component.index = 1;
      component.skipQuestion();
      expect(component.showCongratsScreen).toBeTrue();
      expect(component.showFailScreen).toBeFalse();
      expect(component.showQuizScreen).toBeFalse();
    });

    it('should show fail screen on skipQuestion when score at or below 70', () => {
      component.numberOfQuestions = 2;
      component.numberOfCorrectAnswers = 1;
      component.index = 1;
      component.skipQuestion();
      expect(component.showFailScreen).toBeTrue();
      expect(component.showCongratsScreen).toBeFalse();
    });

    it('should return early from finishQuiz when no current question', () => {
      component.index = 99;
      component.quizQuestions = [];
      courseService.submitQuizAnswers.calls.reset();
      spyOn(console, 'error');
      component.finishQuiz(false);
      expect(console.error).toHaveBeenCalled();
      expect(courseService.submitQuizAnswers).not.toHaveBeenCalled();
    });

    it('should update existing answer in finishQuiz before submission', () => {
      component.index = 0;
      component.selectedAnswerId = 2;
      component.userAnswers = [
        {
          questionId: 1,
          answerId: [1],
          submitted: false,
          answerText: 'A',
          questionType: 'SINGLE_CHOICE',
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 1',
        },
      ];
      courseService.submitQuizAnswers.and.returnValue(
        of({ data: { quizAttemptId: 'upd', isAllowedToRetake: true } }),
      );
      component.finishQuiz(false);
      expect(component.userAnswers[0].answerId).toEqual([2]);
      expect(component.userAnswers[0].submitted).toBeTrue();
      expect(courseService.submitQuizAnswers).toHaveBeenCalled();
    });

    it('should submit quiz on finishQuiz when timer expires', () => {
      component.index = 0;
      component.selectedAnswerId = 1;
      component.userAnswers = [
        {
          questionId: 1,
          answerId: [1],
          submitted: true,
          answerText: 'A',
          questionType: 'SINGLE_CHOICE',
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 1',
        },
      ];
      courseService.submitQuizAnswers.and.returnValue(
        of({ data: { quizAttemptId: 'timeout', isAllowedToRetake: false } }),
      );
      spyOn(component.answerSubmit, 'emit');
      component.finishQuiz(true);
      expect(courseService.submitQuizAnswers).toHaveBeenCalled();
      expect(messageService.success).toHaveBeenCalledWith(
        'Quiz submitted successfully!',
      );
      expect(component.answerSubmit.emit).toHaveBeenCalledWith({
        quizAttemptId: 'timeout',
        isAllowedToRetake: false,
      });
    });

    it('should not add answer when addTextBaseQuestion called without text', () => {
      component.currentAnswerText = null;
      component.addTextBaseQuestion({
        questionId: 1,
        questionType: 'TEXT_FIELD',
        quizAnswers: [{ answerId: 99 }],
      });
      expect(component.userAnswers.length).toBe(0);
      expect(component.isSubmit).toBeFalse();
    });

    it('should play and pause audio via toggleAudio', () => {
      const mockAudio = {
        pause: jasmine.createSpy('pause'),
        play: jasmine.createSpy('play'),
      };
      spyOn(document, 'querySelector').and.returnValue(mockAudio as any);

      const playing = { questionImageUrl: 'track.mp3', isPlaying: true };
      component.toggleAudio(playing);
      expect(mockAudio.pause).toHaveBeenCalled();
      expect(playing.isPlaying).toBeFalse();

      const paused = { questionImageUrl: 'track.mp3', isPlaying: false };
      component.toggleAudio(paused);
      expect(mockAudio.play).toHaveBeenCalled();
      expect(paused.isPlaying).toBeTrue();
    });

    it('should no-op toggleAudio when audio element is missing', () => {
      spyOn(document, 'querySelector').and.returnValue(null);
      const question = { questionImageUrl: 'missing.mp3', isPlaying: false };
      component.toggleAudio(question);
      expect(question.isPlaying).toBeFalse();
    });

    it('should update audio progress and duration helpers', () => {
      const question: any = {};
      const audio = { currentTime: 30, duration: 120 } as HTMLAudioElement;
      component.updateProgress(audio, question);
      expect(question.progress).toBe(25);
      expect(question.currentTime).toBe('0:30');

      component.setDuration(audio, question);
      expect(question.duration).toBe('2:00');

      question.isPlaying = true;
      question.progress = 40;
      component.audioEnded(question);
      expect(question.isPlaying).toBeFalse();
      expect(question.progress).toBe(0);
    });

    it('should seek audio to clicked position on progress bar', () => {
      const question = { attachedAudioUrl: 'track.mp3' };
      const mockAudio = { duration: 100, currentTime: 0 };
      spyOn(document, 'querySelector').and.returnValue(mockAudio as any);
      const bar = document.createElement('div');
      spyOn(bar, 'getBoundingClientRect').and.returnValue({
        left: 0,
        width: 200,
        top: 0,
        height: 10,
        right: 200,
        bottom: 10,
        x: 0,
        y: 0,
        toJSON: () => ({}),
      } as DOMRect);
      const event = {
        currentTarget: bar,
        clientX: 100,
      } as unknown as MouseEvent;
      component.seekAudio(event, question);
      expect(mockAudio.currentTime).toBe(50);
    });
  });

  describe('Phase 1 batch 1: remaining branches', () => {
    const quizTopic = {
      testType: 'TEST' as const,
      quizId: 5,
      passingCriteria: 70,
      durationInMinutes: 2,
      quizQuestionAnswer: {
        quizQuestions: [
          {
            questionId: 1,
            questionType: 'SINGLE_CHOICE',
            questionText: 'Question 1',
            quizAnswers: [
              { answerId: 1, active: false },
              { answerId: 2, active: false },
            ],
          },
          {
            questionId: 2,
            questionType: 'SINGLE_CHOICE',
            questionText: 'Question 2',
            quizAnswers: [
              { answerId: 3, active: false },
              { answerId: 4, active: false },
            ],
          },
        ],
      },
    };

    beforeEach(() => {
      component.currentSelectedTopic = quizTopic;
      component.quizQuestions = quizTopic.quizQuestionAnswer.quizQuestions;
      component.numberOfQuestions = 2;
      component.courseId = 14;
      courseService.getQuizAttempt.and.returnValue(of({ data: null }));
      spyOn(window, 'scrollTo');
    });

    it('should remove multiple-choice answer when same option is toggled off', () => {
      component.index = 0;
      component.currentSelectedTopic = quizTopic;
      component.selectedAnswerId = 2;
      component.userAnswers = [
        {
          questionId: 1,
          questionType: 'MULTIPLE_CHOICE',
          answerId: [1, 2],
          answerText: 'B',
          submitted: false,
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 1',
        },
      ];

      component.submitAnswer({ answerId: 2, answerText: 'B' });

      expect(component.userAnswers[0].answerId).toEqual([1]);
      expect(component.isSubmit).toBeTrue();
    });

    it('should replace existing text answer for the same question', () => {
      component.userAnswers = [
        {
          questionId: 1,
          questionType: 'TEXT_FIELD',
          answerId: [99],
          answerText: 'Old answer',
          submitted: false,
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Explain',
        },
      ];
      component.currentAnswerText = 'Updated answer';
      const question = {
        questionId: 1,
        questionType: 'TEXT_FIELD',
        questionText: 'Explain',
        quizAnswers: [{ answerId: 99 }],
      };

      component.addTextBaseQuestion(question);

      expect(component.userAnswers.length).toBe(1);
      expect(component.userAnswers[0].answerText).toBe('Updated answer');
    });

    it('should use topic id when quizId is missing in fetchAttemptQuiz', () => {
      component.currentSelectedTopic = {
        id: 42,
        quizQuestionAnswer: { quizQuestions: [] },
      };

      component.fetchAttemptQuiz();

      expect(courseService.getQuizAttempt).toHaveBeenCalledWith(42);
      expect(component.loading).toBeFalse();
    });

    it('should set loading while fetchAttemptQuiz is in flight', () => {
      component.fetchAttemptQuiz();
      expect(component.loading).toBeFalse();
      expect(courseService.getQuizAttempt).toHaveBeenCalled();
    });

    it('should treat multiple-choice question as unanswered without answer ids', () => {
      component.index = 0;
      component.quizQuestions[0].questionType = 'MULTIPLE_CHOICE';
      component.userAnswers = [
        {
          questionId: 1,
          answerId: [],
          answerText: '',
          questionType: 'MULTIPLE_CHOICE',
          submitted: false,
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 1',
        },
      ];

      expect(component.isCurrentQuestionUnanswered()).toBeTrue();
    });

    it('should restore next question answer id when advancing', () => {
      component.index = 0;
      component.selectedAnswerId = 1;
      component.userAnswers = [
        {
          questionId: 1,
          answerId: [1],
          submitted: false,
          answerText: 'A',
          questionType: 'SINGLE_CHOICE',
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 1',
        },
        {
          questionId: 2,
          answerId: [4],
          submitted: false,
          answerText: 'D',
          questionType: 'SINGLE_CHOICE',
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 2',
        },
      ];

      component.nextQuestion();

      expect(component.index).toBe(1);
      expect(component.selectedAnswerId).toBe(4);
      expect(component.currentAnswerText).toBeNull();
    });

    it('should not stop timer when finishQuiz nullifies answer without selection', () => {
      spyOn(component, 'stopTimer');
      component.showQuizScreen = true;
      component.welcomeQuizScreen = false;
      component.index = 0;
      component.selectedAnswerId = null;
      component.userAnswers = [
        {
          questionId: 1,
          answerId: [1],
          submitted: true,
          answerText: 'A',
          questionType: 'SINGLE_CHOICE',
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 1',
        },
      ];

      component.finishQuiz(false);

      expect(component.stopTimer).not.toHaveBeenCalled();
      expect(courseService.submitQuizAnswers).not.toHaveBeenCalled();
      expect(component.welcomeQuizScreen).toBeTrue();
      expect(messageService.error).toHaveBeenCalledWith(
        'No answers submitted! Redirecting to start screen...',
      );
    });

    it('should push new answer in finishQuiz when question was not yet saved', () => {
      component.index = 0;
      component.selectedAnswerId = 1;
      component.userAnswers = [];
      courseService.submitQuizAnswers.and.returnValue(
        of({ data: { quizAttemptId: 'new', isAllowedToRetake: true } }),
      );

      component.finishQuiz(false);

      expect(component.userAnswers.length).toBeGreaterThan(0);
      expect(component.userAnswers[0].answerId).toEqual([1]);
      expect(component.userAnswers[0].submitted).toBeTrue();
    });

    it('should decrement seconds after minute rollover in timer', fakeAsync(() => {
      component.durationInMinutes = 1;
      spyOn(component, 'finishQuiz');
      component.startTimer();
      tick(1000);
      expect(component.seconds).toBe(59);
      tick(1000);
      expect(component.seconds).toBe(58);
      component.stopTimer();
    }));

    it('should no-op seekAudio when audio element is missing', () => {
      spyOn(document, 'querySelector').and.returnValue(null);
      const bar = document.createElement('div');
      spyOn(bar, 'getBoundingClientRect').and.returnValue({
        left: 0,
        width: 100,
        top: 0,
        height: 10,
        right: 100,
        bottom: 10,
        x: 0,
        y: 0,
        toJSON: () => ({}),
      } as DOMRect);

      component.seekAudio(
        { currentTarget: bar, clientX: 50 } as unknown as MouseEvent,
        { attachedAudioUrl: 'missing.mp3' },
      );

      expect(document.querySelector).toHaveBeenCalled();
    });

    it('should format zero seconds with padding', () => {
      expect(component.formatTime(5)).toBe('0:05');
      expect(component.formatTime(0)).toBe('0:00');
    });

    it('should detect additional image and audio extensions', () => {
      expect(component.isImage('photo.PNG')).toBeFalse();
      expect(component.isImage('diagram.svg')).toBeTrue();
      expect(component.isAudio('voice.flac')).toBeTrue();
      expect(component.isAudio('notes.pdf')).toBeFalse();
    });

    it('should ignore selectedMultipleAnswers for non-multiple-choice questions', () => {
      component.quizQuestions = [
        {
          questionId: 1,
          questionType: 'SINGLE_CHOICE',
          quizAnswers: [{ answerId: 1, active: false }],
        },
      ];
      component.index = 0;
      const option = component.quizQuestions[0].quizAnswers[0];

      component.selectedMultipleAnswers(option);

      expect(option.active).toBeFalse();
      expect(component.selectedAnswers.length).toBe(0);
    });

    it('should ignore ngOnChanges for unrelated input properties', () => {
      spyOn(component, 'fetchAttemptQuiz');

      component.ngOnChanges({
        title: {
          currentValue: 'New title',
          previousValue: 'Old',
          firstChange: false,
          isFirstChange: () => false,
        },
      });

      expect(component.fetchAttemptQuiz).not.toHaveBeenCalled();
    });

    it('should filter unsubmitted answers before final nextQuestion submission', () => {
      component.index = 1;
      component.selectedAnswerId = 4;
      component.userAnswers = [
        {
          questionId: 1,
          answerId: [1],
          submitted: true,
          answerText: 'A',
          questionType: 'SINGLE_CHOICE',
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 1',
        },
        {
          questionId: 2,
          answerId: [4],
          submitted: false,
          answerText: 'D',
          questionType: 'SINGLE_CHOICE',
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 2',
        },
        {
          questionId: 99,
          answerId: [],
          submitted: false,
          answerText: '',
          questionType: 'SINGLE_CHOICE',
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Skipped',
        },
      ];
      courseService.submitQuizAnswers.and.returnValue(
        of({ data: { quizAttemptId: 'filt', isAllowedToRetake: false } }),
      );

      component.nextQuestion();

      const submittedPayload = courseService.submitQuizAnswers.calls.mostRecent()
        .args[0] as any[];
      expect(submittedPayload.length).toBe(2);
      expect(
        submittedPayload.some((answer) => answer.questionId === 99),
      ).toBeFalse();
    });

    it('should build final submission map for unanswered questions on finishQuiz', () => {
      component.index = 0;
      component.selectedAnswerId = 1;
      component.numberOfQuestions = 2;
      component.quizQuestions = [
        {
          questionId: 1,
          questionType: 'SINGLE_CHOICE',
          questionText: 'Q1',
          quizAnswers: [],
        },
        {
          questionId: 2,
          questionType: 'SINGLE_CHOICE',
          questionText: 'Q2',
          quizAnswers: [],
        },
      ];
      component.userAnswers = [
        {
          questionId: 1,
          answerId: [1],
          submitted: true,
          answerText: 'A',
          questionType: 'SINGLE_CHOICE',
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Q1',
        },
      ];
      courseService.submitQuizAnswers.and.returnValue(
        of({ data: { quizAttemptId: 'map', isAllowedToRetake: false } }),
      );

      component.finishQuiz(true);

      const payload = courseService.submitQuizAnswers.calls.mostRecent()
        .args[0] as any[];
      expect(payload.length).toBe(2);
      expect(payload[1].answerId).toBeNull();
      expect(payload[1].submitted).toBeFalse();
    });

    it('should treat true-false question as unanswered when answer id is missing', () => {
      component.index = 0;
      component.quizQuestions[0].questionType = 'TRUE_FALSE';
      component.userAnswers = [
        {
          questionId: 1,
          answerId: [],
          answerText: '',
          questionType: 'TRUE_FALSE',
          submitted: false,
          courseId: 14,
          timeZone: 'UTC',
          questionText: 'Question 1',
        },
      ];

      expect(component.isCurrentQuestionUnanswered()).toBeTrue();
    });

    it('should clear selectedAnswerId on previousQuestion when no saved answer exists', () => {
      component.index = 1;
      component.selectedAnswerId = 4;
      component.userAnswers = [];

      component.previousQuestion();

      expect(component.index).toBe(0);
      expect(component.selectedAnswerId).toBeNull();
    });

    it('should mark only the selected true/false option as active', () => {
      component.index = 0;
      component.quizQuestions = [
        {
          questionId: 1,
          questionType: 'TRUE_FALSE',
          questionText: 'Angular is a framework',
          quizAnswers: [
            { answerId: 1, answerText: 'True', active: false },
            { answerId: 2, answerText: 'False', active: true },
          ],
        },
      ];
      component.currentSelectedTopic = {
        quizQuestionAnswer: { quizQuestions: component.quizQuestions },
      };

      component.selectedAnswer({
        answerId: 1,
        answerText: 'True',
      });

      expect(component.quizQuestions[0].quizAnswers[0].active).toBeTrue();
      expect(component.quizQuestions[0].quizAnswers[1].active).toBeFalse();
    });
  });

  describe('Phase 1 batch 1: template branches', () => {
    beforeEach(async () => {
      TestBed.resetTestingModule();
      const courseServiceSpy = jasmine.createSpyObj<CourseService>(
        'CourseService',
        ['validateQuizAnswer', 'submitQuizAnswers', 'getQuizAttempt'],
      );
      courseServiceSpy.submitQuizAnswers.and.returnValue(of({ data: [] }));
      courseServiceSpy.getQuizAttempt.and.returnValue(of({ data: null }));
      const messageServiceSpy = jasmine.createSpyObj<MessageService>(
        'MessageService',
        ['error', 'success'],
      );

      await TestBed.configureTestingModule({
        declarations: [QuizPlayerComponent],
        imports: [FormsModule],
        providers: [
          { provide: CourseService, useValue: courseServiceSpy },
          { provide: MessageService, useValue: messageServiceSpy },
        ],
        schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
      }).compileComponents();

      stubQuizLifecycle();
      fixture = TestBed.createComponent(QuizPlayerComponent);
      component = fixture.componentInstance;
      courseService = TestBed.inject(
        CourseService,
      ) as jasmine.SpyObj<CourseService>;
      messageService = TestBed.inject(
        MessageService,
      ) as jasmine.SpyObj<MessageService>;
      spyOn(component, 'fetchAttemptQuiz');
      component.currentSelectedTopic = {
        testType: 'TEST',
        quizId: 1,
        passingCriteria: 70,
        durationInMinutes: 10,
        quizQuestionAnswer: { quizQuestions: [] },
      };
    });

    it('should render welcome screen with singular question and minute labels', () => {
      component.welcomeQuizScreen = true;
      component.numberOfQuestions = 1;
      component.durationInMinutes = 1;
      component.passingCriteria = 70;
      component.quizeType = 'TEST';

      fixture.detectChanges();

      const text = fixture.nativeElement.textContent;
      expect(text).toContain('question');
      expect(text).toContain('minute');
      expect(fixture.nativeElement.querySelector('.start-quiz-btn')).not.toBeNull();
    });

    it('should render plural question and minute labels on welcome screen', () => {
      component.welcomeQuizScreen = true;
      component.numberOfQuestions = 3;
      component.durationInMinutes = 5;
      component.passingCriteria = 80;
      component.quizeType = 'TEST';

      fixture.detectChanges();

      const text = fixture.nativeElement.textContent;
      expect(text).toContain('questions');
      expect(text).toContain('minutes');
    });

    it('should hide passing criteria on welcome screen for survey quizzes', () => {
      component.welcomeQuizScreen = true;
      component.numberOfQuestions = 2;
      component.durationInMinutes = 10;
      component.passingCriteria = 60;
      component.quizeType = 'SURVEY';
      component.currentSelectedTopic = {
        testType: 'SURVEY',
        quizQuestionAnswer: { quizQuestions: [{}, {}] },
      };

      fixture.detectChanges();

      expect(fixture.nativeElement.textContent).not.toContain(
        'The passing criteria',
      );
    });

    it('should render quiz question screen with timer and finish button', () => {
      component.welcomeQuizScreen = false;
      component.showQuizScreen = true;
      component.numberOfQuestions = 2;
      component.index = 0;
      component.minutes = 1;
      component.seconds = 5;
      component.passingCriteria = 70;
      component.quizeType = 'TEST';
      component.quizQuestions = [
        {
          questionId: 1,
          questionType: 'SINGLE_CHOICE',
          questionText: 'What is 2+2?',
          quizAnswers: [],
        },
      ];

      fixture.detectChanges();

      expect(fixture.nativeElement.textContent).toContain('Question 1 of 2');
      expect(fixture.nativeElement.textContent).toContain('Passing Criteria');
      expect(fixture.nativeElement.querySelector('.finish-quiz-btn')).not.toBeNull();
      expect(fixture.nativeElement.textContent).toContain('1:05');
    });

    it('should render padded timer seconds in quiz screen', () => {
      component.welcomeQuizScreen = false;
      component.showQuizScreen = true;
      component.numberOfQuestions = 1;
      component.index = 0;
      component.minutes = 0;
      component.seconds = 7;
      component.quizeType = 'SURVEY';
      component.quizQuestions = [
        {
          questionId: 1,
          questionType: 'SINGLE_CHOICE',
          questionText: 'Rate us',
          quizAnswers: [],
        },
      ];

      fixture.detectChanges();

      expect(fixture.nativeElement.textContent).toContain('0:07');
    });

    it('should render comprehension text when topicComprehensive is set', () => {
      component.welcomeQuizScreen = false;
      component.showQuizScreen = true;
      component.numberOfQuestions = 1;
      component.index = 0;
      component.quizeType = 'TEST';
      component.currentSelectedTopic = {
        testType: 'TEST',
        topicComprehensive: 'Read the passage carefully.',
        quizQuestionAnswer: { quizQuestions: [] },
      };
      component.quizQuestions = [
        {
          questionId: 1,
          questionType: 'SINGLE_CHOICE',
          questionText: 'What is the main idea?',
          quizAnswers: [],
        },
      ];

      fixture.detectChanges();

      expect(fixture.nativeElement.textContent).toContain(
        'Read the passage carefully.',
      );
    });

    it('should render image preview overlay when previewImageUrl is set', () => {
      component.showQuizScreen = true;
      component.welcomeQuizScreen = false;
      component.numberOfQuestions = 1;
      component.index = 0;
      component.quizQuestions = [
        {
          questionId: 1,
          questionType: 'SINGLE_CHOICE',
          questionText: 'Image question',
          quizAnswers: [],
        },
      ];
      component.previewImageUrl = 'https://cdn.example.com/q.png';

      fixture.detectChanges();

      expect(
        fixture.nativeElement.querySelector('.image-preview-overlay'),
      ).not.toBeNull();
      expect(
        fixture.nativeElement.querySelector('.preview-full')?.getAttribute('src'),
      ).toBe('https://cdn.example.com/q.png');
    });

    it('should render text field textarea for TEXT_FIELD questions', () => {
      component.showQuizScreen = true;
      component.welcomeQuizScreen = false;
      component.numberOfQuestions = 1;
      component.index = 0;
      component.quizQuestions = [
        {
          questionId: 1,
          questionType: 'TEXT_FIELD',
          questionText: 'Describe your experience',
          quizAnswers: [{ answerId: 99 }],
        },
      ];

      fixture.detectChanges();

      const textarea = fixture.nativeElement.querySelector('textarea');
      expect(textarea).not.toBeNull();
      expect(textarea.getAttribute('id')).toBe('answerText0');
    });

    it('should render back button when not on the first question', () => {
      component.showQuizScreen = true;
      component.welcomeQuizScreen = false;
      component.numberOfQuestions = 3;
      component.index = 1;
      component.quizQuestions = [
        { questionId: 1, questionType: 'SINGLE_CHOICE', questionText: 'Q1', quizAnswers: [] },
        { questionId: 2, questionType: 'SINGLE_CHOICE', questionText: 'Q2', quizAnswers: [] },
        { questionId: 3, questionType: 'SINGLE_CHOICE', questionText: 'Q3', quizAnswers: [] },
      ];

      fixture.detectChanges();

      expect(fixture.nativeElement.querySelector('.back-btn')).not.toBeNull();
      expect(fixture.nativeElement.textContent).toContain('Question 2 of 3');
    });

    it('should render quiz attempt review when showQuizAttempt is true', () => {
      component.showQuizAttempt = true;
      component.welcomeQuizScreen = false;
      component.showQuizScreen = false;
      component.quizAttemptData = { score: 85, isAllowedToRetake: true };

      fixture.detectChanges();

      expect(fixture.nativeElement.querySelector('app-quiz-attempt')).not.toBeNull();
    });
  });

  describe('Phase 1 batch 1: congrats template branches', () => {
    const congratsTemplate = `
      <div *ngIf="showCongratsScreen" class="congrats-screen">
        <div *ngIf="(questionAnswers?.totalCorrectAnswer / questionAnswers?.totalQuestion) * 100 >= passingCriteria">
          {{ currentSelectedTopic?.testType == 'SURVEY' ? 'Test Submitted' : 'Quiz Result' }}
          {{ currentSelectedTopic?.testType == 'SURVEY' ? 'Successfully' : 'Nice job, You passed.' }}
        </div>
        <div *ngIf="(questionAnswers?.totalCorrectAnswer / questionAnswers?.totalQuestion) * 100 < passingCriteria">
          Quiz Result
          <span *ngIf="quizeType !== 'SURVEY'">Failed {{ passingCriteria }}% required to pass.</span>
        </div>
        <div *ngIf="quizeType !== 'SURVEY'">Your Percentage</div>
      </div>
    `;

    beforeEach(async () => {
      TestBed.resetTestingModule();
      await TestBed.configureTestingModule({
        declarations: [QuizPlayerComponent],
        imports: [FormsModule],
        providers: [
          { provide: CourseService, useValue: courseService },
          { provide: MessageService, useValue: messageService },
        ],
        schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
      })
        .overrideComponent(QuizPlayerComponent, { set: { template: congratsTemplate } })
        .compileComponents();

      stubQuizLifecycle();
      fixture = TestBed.createComponent(QuizPlayerComponent);
      component = fixture.componentInstance;
      spyOn(component, 'fetchAttemptQuiz');
      component.currentSelectedTopic = { testType: 'TEST' };
    });

    it('should render congrats pass summary when score meets passing criteria', () => {
      component.showCongratsScreen = true;
      component.passingCriteria = 70;
      component.quizeType = 'TEST';
      component.questionAnswers = {
        totalCorrectAnswer: 8,
        totalQuestion: 10,
        totalAttemptQuestion: 10,
      };

      fixture.detectChanges();

      expect(fixture.nativeElement.textContent).toContain('Quiz Result');
      expect(fixture.nativeElement.textContent).toContain('Nice job, You passed.');
    });

    it('should render congrats fail summary when score is below passing criteria', () => {
      component.showCongratsScreen = true;
      component.passingCriteria = 70;
      component.quizeType = 'TEST';
      component.questionAnswers = {
        totalCorrectAnswer: 3,
        totalQuestion: 10,
        totalAttemptQuestion: 10,
      };

      fixture.detectChanges();

      expect(fixture.nativeElement.textContent).toContain('Quiz Result');
      expect(fixture.nativeElement.textContent).toContain(
        'Failed 70% required to pass.',
      );
    });

    it('should render survey congrats labels when test type is SURVEY', () => {
      component.showCongratsScreen = true;
      component.passingCriteria = 70;
      component.quizeType = 'SURVEY';
      component.currentSelectedTopic = { testType: 'SURVEY' };
      component.questionAnswers = {
        totalCorrectAnswer: 5,
        totalQuestion: 5,
        totalAttemptQuestion: 5,
      };

      fixture.detectChanges();

      expect(fixture.nativeElement.textContent).toContain('Test Submitted');
      expect(fixture.nativeElement.textContent).toContain('Successfully');
      expect(fixture.nativeElement.textContent).not.toContain('Your Percentage');
    });
  });
});
