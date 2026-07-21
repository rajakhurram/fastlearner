import { QuizType } from '../enums/quiz-type';

export class CreateCourse {
  courseId?: any;
  title?: string;
  courseUrl?: string;
  price?: number;
  description?: string;
  categoryId?: number;
  courseLevelId?: number;
  courseType?: string;
  pricingLocked?: boolean;
  premiumConversionUsed?: boolean;
  about?: string;
  thumbnailUrl?: string;
  previewVideoURL?: string;
  tags?: Tag[];
  prerequisite?: string[];
  courseOutcomes?: string[];
  sections?: Section[];
  certificateEnabled?: boolean = true;
  isActive?: any;
  courseProgress?: any;
  titleExist?: boolean;
  courseStatus?: string;
  previewVideoVttContent?: string;
  contentType?: any;
}

export class Section {
  id?: any;
  delete?: any;
  title?: string;
  isFree?: boolean;
  level?: number;
  topics?: Topic[];
}

export class Topic {
  id?: any;
  delete?: boolean;
  title?: string;
  level?: number;
  topicTypeId?: number;
  duration?: number;
  topicComprehensive?: string;
  video?: Video;
  quiz?: Quiz;
  article?: Article;
  validate?: any;
}

export class Quiz {
  id?: any;
  delete?: boolean;
  title?: string;
  timeInSeconds?: number;
  durationInMinutes?: any;
  passingCriteria?: any;
  randomQuestion?: any;
  generateAIReport?: boolean;
  reportPrompt?: string;
  testType?: QuizType;
  isAllowedToRetake?: boolean;
  questions?: Question[];
}

export class Question {
  id?: any;
  delete?: boolean;
  questionText?: string;
  questionImageUrl?: string;
  questionType?: string;
  explanation?: string;
  surveyQuestionCount?: number;
  surveyAnswers?: Array<{ count: number; answer: string }>;
  answers?: Answer[];
  mediaType?: string;
  // questionAudioUrl?: string;
}

export class Answer {
  id?: any;
  delete?: boolean;
  answerText?: string;
  answerImageUrl?: string;
  isCorrectAnswer?: boolean;
}

export class Video {
  id?: any;
  delete?: boolean;
  filename?: string;
  videoURL?: string;
  summary?: string;
  transcribe?: string;
  vttContent?: string;
  documents?: Document[];
}

export class Document {
  id?: any;
  delete?: boolean;
  summary?: string;
  docName?: string;
  docUrl?: string;
}

export class Tag {
  id?: number;
  name?: string;
  active?: boolean;
}

export class Article {
  id?: any;
  delete?: boolean;
  article?: any;
  documents?: Document[];
}
