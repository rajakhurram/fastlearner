import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuizReviewQuestionsComponent } from './quiz-review-questions.component';

describe('QuizReviewQuestionsComponent', () => {
  let component: QuizReviewQuestionsComponent;
  let fixture: ComponentFixture<QuizReviewQuestionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ QuizReviewQuestionsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuizReviewQuestionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
