import { TestBed } from '@angular/core/testing';

import { QuizReviewQuestionsService } from './quiz-review-questions.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';

describe('QuizReviewQuestionsService', () => {
  let service: QuizReviewQuestionsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
      ],
      providers: [
        HttpClient,
      ]
    });
    service = TestBed.inject(QuizReviewQuestionsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
