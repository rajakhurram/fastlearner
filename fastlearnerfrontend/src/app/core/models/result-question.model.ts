import { QuestionSourceType } from './manual-question-form.model';

export class AIResultQuestion {
  id?: string | number;
  questionNumber?: any;
  studentAnswer?: any;
  correctAnswer?: any;
  score?: any;
  totalMarks?: any;
  questionStatus?: any;
  feedback?: any;
  confidenceLevel?: any;
  questionSource?: QuestionSourceType | string;
  aiResultId?: any;
  panelOpen?: boolean = false;
  enableQuestionEditing?: boolean = false;
  enableFullQuestionEditing?: boolean = false;
  editedObtainedMarks?: number | null;
  editedOutOfMarks?: number | null;
  editedFeedback?: string;
  editedQuestionNumber?: number | null;
  /** @deprecated use editedObtainedMarks */
  editedQuestionNumberLegacy?: any;
}

export function isInstructorQuestion(question?: AIResultQuestion): boolean {
  return (
    question?.questionSource === 'INSTRUCTOR' ||
    question?.questionSource === 'Instructor'
  );
}
