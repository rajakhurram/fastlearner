import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuizReviewAttemptQuestionsComponent } from './quiz-review-attempt-questions.component';

describe('QuizReviewAttemptQuestionsComponent', () => {
  let component: QuizReviewAttemptQuestionsComponent;
  let fixture: ComponentFixture<QuizReviewAttemptQuestionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ QuizReviewAttemptQuestionsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuizReviewAttemptQuestionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
