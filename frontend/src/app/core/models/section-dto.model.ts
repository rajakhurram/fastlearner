export class SectionDto {
  active: boolean = true;
  name: 'Introduction to design thinking'; //default empty
  disabled: boolean = false;
  switchValue: boolean = true;
  generateTopicsPrompt: boolean = false;
  generateTopicBtn1: boolean = true; //default true
  generateTopicBtn2: boolean = false;
  showChatBox: boolean = false;
  topicInput: string;
  showSpinner: boolean = false;
  deleteTopicIcon: boolean = false;
  checkAll: boolean = false;
  questionAnswers: QuestionAnswerDto;
  createTopics: boolean = false;
  topics: TopicDto[];
  quiz: QuizDto;
}

export class QuestionAnswerDto {
  question: string;
  answers: AnswerDto[];
}

export class AnswerDto {
  number: string;
  text: string;
}

export class TopicDto {
  active: boolean = false;
  name: 'Quiz';
  disabled: boolean = false;
  checkTopic: boolean = false;
  topicContainer: boolean = true;
  contentScreen: boolean = false;
  videoSection: boolean = false;
  articleSection: boolean = false;
  quizSection: boolean = true;
  selectedContentType: string = 'Quiz';
  topicStatusImg: '../../../../../assets/icons/topic_incomplete_icon.svg';
  validate: boolean = false;
  completed: boolean = false;
  contentOptions: ['Video', 'Quiz', 'Article'];
  video: VideoDto;
  article: ArticleDto;
}

export class VideoDto {
  fileProcessing: boolean = false;
  videoData: VideoDataDto;
  documentData: DocumentDataDto;
}

export class VideoDataDto {
  videoFileName: string = 'Add Video';
  videoBtnName: string = 'Upload File';
  videoProgress: number = 0;
  videoFileType: string;
  videoTranscript: string;
  videoSubtitles: string;
  videoSummary: string;
  date: string;
  file: string;
  duration: string;
  videoUrl: string;
}

export class DocumentDataDto {
  documentFileName: string = 'Add Resource';
  documentBtnName: string = 'Upload File';
  documents: DocumentDto[];
  showTable: boolean = false;
}

export class DocumentDto {
  documentProgress: 0;
  documentFileName: any;
  documentFileType: any;
  date: any;
  documentSummary: string;
  file: any;
  documentKey: any;
}

export class ArticleDto {
  generateArticleBtn: boolean = true;
  articlePrompt: boolean = false;
  articlePromptInput: string;
  showChatBox: boolean = false;
  showSpinner: boolean = false;
  questionAnswers: QuestionAnswerDto;
  articleFileName: string = 'Add Resource';
  articleBtnName: string = 'Upload File';
  articleFileType: string = '';
  articleDate: string = '';
  articleSummary: string = '';
  articleDocumnetUrl: string = '';
  content: string = '';
}

export class QuizDto {
  title: string;
  questions: QuizQuestionDto[];
}

export class QuizQuestionDto {
  label: string = 'Question';
  ques: string;
  answers: QuizAnswerDto[];
  correctAnswer: QuizAnswerDto;
}

export class QuizAnswerDto {
  ans: string;
}
