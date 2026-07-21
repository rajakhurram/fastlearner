import { AIResultQuestion, isInstructorQuestion } from './result-question.model';

describe('isInstructorQuestion', () => {
  it('returns true for INSTRUCTOR source', () => {
    const q: AIResultQuestion = { questionSource: 'INSTRUCTOR' };
    expect(isInstructorQuestion(q)).toBeTrue();
  });

  it('returns false for AI source', () => {
    const q: AIResultQuestion = { questionSource: 'AI' };
    expect(isInstructorQuestion(q)).toBeFalse();
  });

  it('returns false when source missing', () => {
    expect(isInstructorQuestion({})).toBeFalse();
  });
});
