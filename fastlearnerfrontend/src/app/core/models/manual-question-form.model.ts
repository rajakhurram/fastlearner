export type QuestionSourceType = 'AI' | 'INSTRUCTOR';

export interface ManualQuestionForm {
  questionNumber: number | null;
  obtainedMarks: number | null;
  outOfMarks: number | null;
  feedback: string;
}

export function emptyManualQuestionForm(): ManualQuestionForm {
  return {
    questionNumber: null,
    obtainedMarks: null,
    outOfMarks: null,
    feedback: '',
  };
}
