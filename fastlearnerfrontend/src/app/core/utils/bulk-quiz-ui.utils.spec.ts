import {
  assignQuizQuestionClientKeys,
  countActiveQuizQuestions,
  getRemainingQuizQuestionsCount,
  hasMoreQuizQuestionsToLoad,
  loadMoreQuizQuestions,
  resetQuizRenderLimit,
  shouldRenderQuizQuestion,
} from './bulk-quiz-ui.utils';

describe('bulk-quiz-ui.utils', () => {
  it('counts non-deleted quiz questions', () => {
    const topic = {
      quiz: {
        questions: [{ delete: false }, { delete: true }, { delete: false }],
      },
    };
    expect(countActiveQuizQuestions(topic)).toBe(2);
  });

  it('renders only the first batch of active questions', () => {
    const topic = {
      quiz: {
        questions: Array.from({ length: 8 }, () => ({ delete: false })),
      },
    };
    resetQuizRenderLimit(topic);
    expect(shouldRenderQuizQuestion(topic, 0)).toBeTrue();
    expect(shouldRenderQuizQuestion(topic, 4)).toBeTrue();
    expect(shouldRenderQuizQuestion(topic, 5)).toBeFalse();
    expect(hasMoreQuizQuestionsToLoad(topic)).toBeTrue();
    expect(getRemainingQuizQuestionsCount(topic)).toBe(3);
    loadMoreQuizQuestions(topic);
    expect(shouldRenderQuizQuestion(topic, 5)).toBeTrue();
    expect(hasMoreQuizQuestionsToLoad(topic)).toBeFalse();
  });

  it('assigns stable client keys for trackBy', () => {
    const question = {
      answers: [{}, {}],
    };
    assignQuizQuestionClientKeys(question);
    expect((question as any)._clientKey).toBeTruthy();
    expect((question.answers[0] as any)._clientKey).toBeTruthy();
    expect((question.answers[1] as any)._clientKey).toBeTruthy();
  });
});
