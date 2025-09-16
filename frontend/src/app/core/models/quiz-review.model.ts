export class QuizReview {
  percentage: number;
  passingCriteria: number;
  totalQuestion: number;
  totalCorrectAnswer: number;
  totalAttemptQuestion: number
  questionAnswerResponses: QuestionAnswerResponse[];

  constructor(data?: Partial<QuizReview>) {
    Object.assign(this, data);
  }
}

export class QuestionAnswerResponse {
  questionText: string;
  correctAnswerId: any = [];
  selectedAnswerId: any = [];
  questionType: string;
  answerResponseList: AnswerResponse[];
  correct: boolean;
  explanation?: string;

  constructor(data?: Partial<QuestionAnswerResponse>) {
    Object.assign(this, data);
  }
}

export class AnswerResponse {
  answerId: number;
  answerText: string;
  selected?: boolean = false;
  corrected?: boolean = false;

  constructor(data?: Partial<AnswerResponse>) {
    Object.assign(this, data);
  }
}
