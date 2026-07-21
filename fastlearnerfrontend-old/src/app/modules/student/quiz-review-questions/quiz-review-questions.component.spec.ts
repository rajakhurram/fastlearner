import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuizReviewQuestionsComponent } from './quiz-review-questions.component';
import { MessageService } from 'src/app/core/services/message.service';
import { NzModalService } from 'ng-zorro-antd/modal';
import { ViewContainerRef } from '@angular/core';
import { QuizReviewQuestionsService } from 'src/app/core/services/quiz-review-questions.service';

describe('QuizReviewQuestionsComponent', () => {
  let component: QuizReviewQuestionsComponent;
  let fixture: ComponentFixture<QuizReviewQuestionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ QuizReviewQuestionsComponent ],
      providers: [
        { provide: MessageService, useValue: {} },
        { provide: NzModalService, useValue: {} },
        { provide: ViewContainerRef, useValue: {} },
        { provide: QuizReviewQuestionsService, useValue: {} },
      ]
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
