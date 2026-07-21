import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA, ViewContainerRef } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';
import { MessageService } from 'src/app/core/services/message.service';
import { QuizReviewQuestionsService } from 'src/app/core/services/quiz-review-questions.service';
import { QuizReviewAttemptQuestionsComponent } from './quiz-review-attempt-questions.component';

describe('QuizReviewAttemptQuestionsComponent', () => {
  let component: QuizReviewAttemptQuestionsComponent;
  let fixture: ComponentFixture<QuizReviewAttemptQuestionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [QuizReviewAttemptQuestionsComponent],
      providers: [
        {
          provide: MessageService,
          useValue: jasmine.createSpyObj('MessageService', ['error', 'success']),
        },
        {
          provide: NzModalService,
          useValue: jasmine.createSpyObj('NzModalService', ['create']),
        },
        {
          provide: QuizReviewQuestionsService,
          useValue: jasmine.createSpyObj('QuizReviewQuestionsService', [
            'getQuizReviewQuestions',
          ]),
        },
        {
          provide: ViewContainerRef,
          useValue: jasmine.createSpyObj('ViewContainerRef', ['createComponent']),
        },
      ],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(QuizReviewAttemptQuestionsComponent);
    component = fixture.componentInstance;
    component.quizData = {
      quiz: { title: 'Test Quiz', quizQuestions: [], generateAIReport: false },
      totalQuestion: 0,
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should read question correctness from isCorrect', () => {
    expect(component.isQuestionCorrect({ isCorrect: true })).toBeTrue();
    expect(component.isQuestionCorrect({ isCorrect: false })).toBeFalse();
  });

  it('should fall back to correct when isCorrect is missing', () => {
    expect(component.isQuestionCorrect({ correct: true })).toBeTrue();
    expect(component.isQuestionCorrect({ correct: false })).toBeFalse();
  });

  it('should filter questions by isCorrect', () => {
    component.quizData = {
      quiz: {
        title: 'Quiz',
        quizQuestions: [
          { questionText: 'Q1', isCorrect: true },
          { questionText: 'Q2', isCorrect: false },
        ],
      },
      totalQuestion: 2,
    };
    component.ngOnChanges();

    component.filterResults('correct');
    expect(component.filteredQuestions.length).toBe(1);
    expect(component.filteredQuestions[0].questionText).toBe('Q1');

    component.filterResults('incorrect');
    expect(component.filteredQuestions.length).toBe(1);
    expect(component.filteredQuestions[0].questionText).toBe('Q2');
  });
});
