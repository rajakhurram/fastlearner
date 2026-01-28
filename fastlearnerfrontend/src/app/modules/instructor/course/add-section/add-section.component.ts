import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnDestroy,
  OnInit,
  Output,
  QueryList,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { InstructorService } from 'src/app/core/services/instructor.service';

import {
  CdkDragDrop,
  CdkDragEnter,
  moveItemInArray,
  CdkDragHandle,
} from '@angular/cdk/drag-drop';
import { MessageService } from 'src/app/core/services/message.service';
import { FileManager } from 'src/app/core/services/file-manager.service';
import { Subscription, interval } from 'rxjs';
import { NzModalService } from 'ng-zorro-antd/modal';
import { SummaryModalComponent } from 'src/app/modules/dynamic-modals/summary-modal/summary-modal.component';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { TranscriptModalComponent } from 'src/app/modules/dynamic-modals/transcript-modal/transcript-modal.component';
import { NzUploadChangeParam, NzUploadXHRArgs } from 'ng-zorro-antd/upload';
import { NzMessageService } from 'ng-zorro-antd/message';
import { CourseService } from 'src/app/core/services/course.service';
import { AuthService } from 'src/app/core/services/auth.service';
import { DeletionModalComponent } from 'src/app/modules/dynamic-modals/deletion-modal/deletion-modal.component';
import { FormGroup } from '@angular/forms';
import { AngularEditorConfig } from '@kolkov/angular-editor';
import { CourseType } from 'src/app/core/enums/course-status';
import { QuestionType } from 'src/app/core/enums/question-type';
import { CourseContentType } from 'src/app/core/enums/course-content-type.enum';
import { Question } from 'src/app/core/models/create-course.model';
import { ReportPreviewModalComponent } from 'src/app/modules/dynamic-modals/report-preview-modal-component/report-preview-modal-component.component';
import { QuizType } from 'src/app/core/enums/quiz-type';
@Component({
  selector: 'app-add-section',
  templateUrl: './add-section.component.html',
  styleUrls: ['./add-section.component.scss'],
})
export class AddSectionComponent implements OnInit, OnDestroy {
  editorConfig: AngularEditorConfig = {
    editable: true,
    spellcheck: true,
    height: 'auto',
    minHeight: '150px',
    maxHeight: 'auto',
    width: 'auto',
    minWidth: '0',
    translate: 'yes',
    enableToolbar: true,
    showToolbar: true,
    placeholder: 'Enter text here...',
    defaultParagraphSeparator: '',
    defaultFontName: '',
    defaultFontSize: '',
    toolbarHiddenButtons: [
      // Hides all other buttons except bold, italic, underline, and image
      [
        'strikeThrough',
        'subscript',
        'superscript',
        'justifyLeft',
        'justifyCenter',
        'justifyRight',
        'justifyFull',
        'indent',
        'outdent',
        'insertOrderedList',
        'insertUnorderedList',
        'heading',
        'fontSize',
        'textColor',
        'backgroundColor',
        'link',
        'unlink',
        'insertVideo',
        'insertHorizontalRule',
        'removeFormat',
        'toggleEditorMode',
        'undo',
        'redo',
        'fontName',
      ],
    ],
  };

  _httpConstants: HttpConstants = new HttpConstants();
  videoFileBtn?: string = 'Upload File';
  private progressIntervalVideo$: Subscription | undefined;
  private progressIntervals: Map<string, Subscription> = new Map<
    string,
    Subscription
  >();
  private uploadedVideos: Map<string, any> = new Map<string, any>();
  private uploadedDocuments: Map<string, any> = new Map<string, any>();
  private uploadedArticleDocuments: Map<string, any> = new Map<string, any>();
  @Output() currentStep = new EventEmitter<string>();
  @Output() sectionsDataOutPut = new EventEmitter<string>();
  @Input() sectionsData: any;
  @Input() courseInformationData: FormGroup;
  @Input() courseId: any;
  @Input() selectedContentType: any;
  @ViewChild('formElement', { static: false }) formElement: ElementRef;
  courseContentType = CourseContentType;
  quizType = QuizType;
  courseChatHistory: Array<any> = [];
  typeVideo?: string = 'Video';
  typeArticle?: string = 'Article';
  typeQuiz?: string = 'Quiz';
  topicStatusIncompleteImg =
    '../../../../../assets/icons/topic_incomplete_icon.svg';
  topicStatusCompleteImg =
    '../../../../../assets/icons/topic_complete_icon.svg';
  sections?: any = [];
  checkAllSection?: any = false;
  showDltSectionBtn?: any = false;
  showSectionDltContainer?: any = true;
  fullWidth: boolean;
  screenWidth: any;
  courseSaved?: any = false;
  isYoutubeLinkPresent = false;
  courseType = CourseType;
  questionType = QuestionType;
  allowToReTakeAssessment: boolean = false;
  surveyDefaults = ["Strongly Disagree", "Disagree", "Neutral", "Agree", "Strongly Agree"];
  questionTypes?: any = [
    {
      key: this.questionType.MULTIPLE_CHOICE,
      value: 'Multiple choices',
      src: '../../../../../assets/icons/multiple_choice.svg',
    },
    {
      key: this.questionType.SINGLE_CHOICE,
      value: 'Single choice',
      src: '../../../../../assets/icons/single_choice.svg',
    },
    {
      key: this.questionType.TRUE_FALSE,
      value: 'True/False',
      src: '../../../../../assets/icons/tf.svg',
    },
    {
      key: this.questionType.TEXT_FIELD,
      value: 'Text field',
      src: '../../../../../assets/icons/tf.svg',
    },
    // { key: this.questionType.TEXT_FIELD, value: 'Text field' },
  ];
  randomQuestionAll?: string = 'All';
  randomQuestionCustom?: string = 'Custom';

  alphabet: string[] = Array.from({ length: 26 }, (_, i) =>
    String.fromCharCode(65 + i)
  );
  private readonly timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
  private timeout: NodeJS.Timeout = null;

  // selectedQuizType: QuizType | undefined;
  public isPublished: boolean = false;

  constructor(
    private _instructorService: InstructorService,
    private _messageService: MessageService,
    private _fileManagerService: FileManager,
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef,
    private communicationService: CommunicationService,
    private msg: NzMessageService,
    private _courseService: CourseService,
    private _authService: AuthService,
    private _cdr: ChangeDetectorRef
  ) { }

  topicTypesOptions = [
    {
      label: "Basic Quiz",
      value: "TEST",
    },
    {
      label: "Survey",
      value: "SURVEY",
    },
  ]

  surveyQuestionCountOptions = [
    {
      label: "3",
      value: 3,
    },
    {
      label: "4",
      value: 4,
    },
    {
      label: "5",
      value: 5,
    },
  ]

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.screenWidth = event.target.innerWidth;
  }

  ngOnDestroy() {
    this.saveAsDraftCourse();
    clearTimeout(this.timeout);
  }

  @HostListener('window:beforeunload', ['$event'])
  handleBeforeUnload(event: Event): void {
    event.preventDefault();
  }

  ngOnInit(): void {
    console.log('courseInformationData: ', this.courseInformationData);
    this.isPublished = this.courseInformationData.get('courseProgress')?.value == 100;
    // console.log('selected quiz type: ', this.selectedQuizType);
    if (this.sectionsData?.length > 0) {
      this.sections = this.sectionsData;
      console.log('sections: ', this.sections);
      this.sections.forEach((section?: any) => {
        section.topics.forEach((topic?: any) => {
          if (topic?.quiz != null) {
            topic.quiz.type = this.topicTypesOptions.find(type => type.value == topic?.quiz?.type)?.value;
            topic.quiz.questions.forEach((question?: any) => {
              question.questionType = this.questionTypes.find((questionType?: any) => questionType?.key == question?.questionType.key);
            });
          }
        });
      });

      this._cdr.detectChanges();
      this.hideDeleteSectionContainer();
    } else if (this.courseId) {
      this.patchSectionData();
    } else {
      this.addSection();
      // this.createTopics(this.sections[0]);
    }

    this.communicationService.documentSummary$?.subscribe(
      (document: any) => { }
    );

    this.communicationService.videoSummary$?.subscribe((videoData: any) => { });

    this.communicationService.articleSummary$?.subscribe((article: any) => { });

    this.communicationService.videoTranscript$?.subscribe(
      (videoData: any) => { }
    );
  }

  patchSectionData() {
    this._courseService.getSectionByCourseId(this.courseId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.uploadSectionData(response?.data);
        }
      },
      error: (error: any) => {
        this.addSection();
      },
    });
  }

  uploadSectionData(sections?: any) {
    sections.forEach((section: any, index: any) => {
      const sec = {
        sectionId: section.sectionId,
        delete: false,
        level: index + 1,
        checkSection: false,
        active: index == 0 ? true : false,
        name: section.sectionName, //default empty
        disabled: false,
        switchValue:
          this.courseInformationData?.value?.courseType ===
            this.courseType.PREMIUM
            ? false
            : section.free,
        generateTopicsPrompt: false,
        generateTopicBtn1: true, //default true
        generateTopicBtn2: false,
        showChatBox: false,
        topicInput: '',
        showSpinner: false,
        deleteTopicIcon: false,
        checkAll: false,
        deleteAll: false,
        questionAnswers: {
          question: '',
          answers: [],
        },
        createTopics: true, // default false
        topics: [],
      };
      this.sections.push(sec);
      this.uploadTopicsData(this.sections[index]);
    });

    // this.uploadTopicsData(this.sections[0]);
  }

  uploadTopicsData(section?: any) {
    this._courseService.getTopicsBySectionId(section?.sectionId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          response?.data?.forEach((topic?: any, index?: any) => {
            // this.selectedQuizType = topic.testType ?? this.quizType.BASIC_QUIZ;
            let video = {
              fileProcessing: false,
              videoData: {
                videoId: '',
                delete: false,
                videoFileName: 'Add Video',
                videoBtnName: 'Upload File',
                videoProgress: 0,
                videoFileType: '',
                videoTranscript: '',
                videoSubtitles: '',
                videoSummary: '',
                date: '',
                file: '',
                duration: '',
                videoUrl: '',
                youtubeVideoUrl: '',
                selectedVideo: null,
              },

              documentData: {
                documentFileName: 'Add Resource',
                documentBtnName: 'Upload File',
                documents: [],
              },
              showTable: false,
            };

            let quiz = {
              quizId: '',
              title: '',
              delete: false,
              durationInMinutes: null,
              passingCriteria: null,
              randomQuestion: topic?.randomQuestion || topic?.quizQuestionAnswer?.quizQuestions.length,
              randomQuestionType: this.randomQuestionAll,
              generateAIReport: false,
              reportPrompt: '',
              type: topic.testType,
              isAllowedToRetake: topic.isAllowedToRetake ?? false,
              questions: [
                {
                  questionId: '',
                  delete: false,
                  label: 'Question ' + 1 + ' ',
                  questionType: topic.quiz?.type !== this.quizType.SURVEY ? this.questionTypes[0] : this.questionTypes[1],
                  ques: '',
                  explanation: '',
                  surveyQuestionCount: 5
                  ,
                  // surveyAnswers: topic.quizQuestionAnswer.quizQuestions?.quizAnswers.length && ,
                  surveyAnswers: [],
                  answers: [
                    {
                      answerId: '',
                      label: '',
                      delete: false,
                      ans: '',
                      exist: false,
                      answerOrder: '',
                      isCorrectAnswer: false,
                    },
                  ],
                  // correctAnswer: { ans: null },
                },
              ],
            };

            let article = {
              articleId: '',
              delete: false,
              generateArticleBtn: true, //default true
              articlePrompt: false, //default false
              articlePromptInput: '', //default empty
              showChatBox: false,
              showSpinner: false,
              uploadArticleDocument: false,
              questionAnswers: {
                question: '',
                answers: [],
              },
              articleFileName: 'Add Resource',
              articleBtnName: 'Upload File',
              articleFileType: '', //default empty
              articleDate: '', //default empty
              articleSummary: '',
              articleDocumnetUrl: '',
              articleDocumnetId: '',
              articleProgressBar: false,
              file: '',
              content: '',
            };

            if (topic.topicType == this.typeVideo) {
              video.showTable = true;
              if (topic.docs != undefined && topic != null) {
                topic.docs.forEach((document: any) => {
                  video.documentData.documents.push({
                    id: document.id,
                    delete: false,
                    documentUrl: document.url,
                    documentProgress: 100,
                    documentFileName: document.name,
                    documentFileType: 'fileType',
                    date: this.formatDate(topic.creationDate),
                    documentSummary: document.summary,
                    file: '',
                    documentKey: '',
                  });
                });
              }
              (video.videoData.videoId = topic.videoId),
                (video.videoData.delete = topic.delete),
                (video.videoData.videoFileName = topic.filename
                  ? topic.filename
                  : 'YOUTUBE'),
                (video.videoData.videoUrl = topic.videoUrl),
                (video.videoData.youtubeVideoUrl =
                  topic.filename == 'YOUTUBE' ? topic.videoUrl : ''),
                (video.videoData.videoSummary = topic.summary),
                (video.videoData.videoTranscript = topic.transcript),
                (video.videoData.videoSubtitles = topic.vttContent),
                (video.videoData.date = this.formatDate(topic.creationDate)),
                (video.videoData.videoFileType = 'Video'),
                (video.videoData.videoProgress = 100),
                (video.videoData.videoBtnName = 'Replace');
            }

            if (topic.topicType == this.typeQuiz) {
              quiz.questions = [];
              quiz.quizId = topic.quizId;
              quiz.title = topic.quizTitle;
              quiz.durationInMinutes = topic.durationInMinutes;
              quiz.passingCriteria = topic.passingCriteria;
              quiz.randomQuestion = topic?.randomQuestion || topic?.quizQuestionAnswer?.quizQuestions.length;
              quiz.generateAIReport = topic?.generateAIReport || false;
              quiz.reportPrompt = topic?.reportPrompt || '';
              quiz.randomQuestionType =
                topic?.randomQuestion ==
                  topic?.quizQuestionAnswer?.quizQuestions?.length ||
                  topic?.randomQuestion == 0 ||
                  topic?.randomQuestion == null
                  ? this.randomQuestionAll
                  : this.randomQuestionCustom;

              topic?.quizQuestionAnswer?.quizQuestions.forEach(
                (value: any, index: any) => {
                  let answers = [];
                  value.quizAnswers.forEach((answer: any, index: any) => {
                    answers.push({
                      answerId: answer.answerId,
                      label: index + 1,
                      delete: false,
                      ans: answer.answerText,
                      answerOrder: '',
                      isCorrectAnswer: topic.testType === this.quizType.SURVEY ? false : answer?.isCorrect,
                    });
                  });

                  let question = {
                    questionId: value.questionId,
                    delete: false,
                    label: 'Question ' + (index + 1),
                    ques: value.questionText,
                    explanation: value?.explanation,
                    surveyQuestionCount: value.quizAnswers?.length ?? 5,
                    surveyAnswers: this.mapSurveyAnswers(value.quizAnswers) || [],
                    questionType: value?.questionType
                      ? this.questionTypes?.find(
                        (type: any) => type?.key == value?.questionType
                      )
                      : this.questionTypes[0],
                    answers: answers,
                    // correctAnswer: {
                    //   ans: value?.quizAnswers?.find(
                    //     (answer: any) => answer?.isCorrect == true
                    //   )?.answerText,
                    // },
                  };
                  quiz.questions.push(question);
                  this.maintainQuizQuestionAnswersOrder(
                    quiz?.questions[index]?.answers
                  );
                }
              );
            }

            if (topic.topicType == this.typeArticle) {
              article.uploadArticleDocument = true;
              (article.articleId = topic.articleId), (article.delete = false);
              article.content = topic?.article;
              article.articleFileName =
                topic.docs == null ? 'Add Resource' : topic?.docs[0]?.name;
              article.articleDocumnetUrl =
                topic.docs == null ? '' : topic?.docs[0]?.url;
              article.articleSummary =
                topic.docs == null ? '' : topic?.docs[0]?.summary;
              article.articleDocumnetId =
                topic.docs == null ? '' : topic?.docs[0]?.id;
            }

            const top = {
              topicId: topic.topicId,
              level: index + 1,
              delete: false,
              topicDuration: topic.topicDuration,
              active: false, // default false
              name: topic.topicName, // default empty
              disabled: false,
              checkTopic: false,
              topicContainer: true, //default false
              contentScreen: false, //default false
              videoSection: topic.topicType == this.typeVideo ? true : false, //default false
              articleSection:
                topic.topicType == this.typeArticle ? true : false, //default false
              quizSection: topic.topicType == this.typeQuiz ? true : false, //default false
              selectedContentType: topic.topicType, //default empty
              topicStatusImg: this.topicStatusCompleteImg,
              validate: true,
              completed: true,
              contentOptions: ['Video', 'Quiz', 'Article'],
              video: video,
              quiz: quiz,
              article: article,
            };
            section.topics.push(top);
            if (topic.topicType == this.typeArticle) {
              this.articleValidation(section.topics[section.topics.length - 1]);
            } else if (topic.topicType == this.typeQuiz) {
              this.quizValidation(section.topics[section.topics.length - 1]);
            } else if (topic.topicType == this.typeVideo) {
              this.videoValidation(section.topics[section.topics.length - 1]);
            }
          });
        }
      },
    });
    this.updateSectionLevels();
    this.deleteSectionIcon();
  }

  mapSurveyAnswers(answers: Array<{ answerId: number; answerText: string; delete: boolean }>): { answerId: number; count: number; answer: string; }[] {
    return answers.map((a, i) => ({
      answerId: a.answerId,
      count: i + 1,
      answer: a.answerText,
      delete: a.delete ?? false,
      ans: a.answerText,
    }))
  }

  showGenerateTopicsPrompt(section?: any) {
    section.generateTopicBtn1 = false;
    section.generateTopicsPrompt = true;
    section.generateTopicsPrompt
      ? (section.generateTopicBtn2 = false)
      : (section.generateTopicBtn2 = true);
  }

  clearTopicChat(section?: any) {
    section.showChatBox = false;
    section.generateTopicsPrompt = true;
    section.questionAnswers.question = '';
    section.questionAnswers.answers = [];
  }

  generateTopics(section?: any) {
    if (section.topicInput) {
      section.questionAnswers.question = section.topicInput;
      section.topicInput = '';
      section.showChatBox = true;
      section.questionAnswers.answers = [];
      section.showSpinner = true;
      section.generateTopicsPrompt = false;
      this._instructorService
        .generator(section.questionAnswers.question)
        ?.subscribe({
          next: (response: any) => {
            if (
              response?.status ==
              this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
            ) {
              section.showSpinner = false;
              const points = response?.data.split('\n').map((point: any) => {
                section.questionAnswers.answers.push(point);
              });
            }
          },
          error: (error: any) => {
            section.showSpinner = false;
          },
        });
    }
  }

  topicInputChange(section?: any) {
    section.topicInput = section.topicInput.trim();
  }

  addSection() {
    this.sections.push({
      sectionId: '',
      checkSection: false,
      level: this.sections[this.sections?.length - 1]?.level + 1,
      delete: false,
      active: true,
      name: '', //default empty
      disabled: false,
      switchValue: true,
      generateTopicsPrompt: false,
      generateTopicBtn1: true, //default true
      generateTopicBtn2: false,
      showChatBox: false,
      topicInput: '',
      showSpinner: false,
      deleteTopicIcon: false,
      checkAll: false,
      deleteAll: false,
      questionAnswers: {
        question: '',
        answers: [],
      },
      createTopics: false, // default false
      topics: [],
    });
    this.updateSectionLevels();
    if (this.selectedContentType == this.courseContentType.TEST) {
      this.createTopics(this.sections[this.sections.length - 1]);
    }
    this.showSectionDltContainer = true;
  }

  createTopics(section?: any) {
    const surveyTopic = section.topics.find((topic: any) => topic.quiz.type === this.quizType.SURVEY);

    section.generateTopicBtn1 = false;
    section.generateTopicsPrompt
      ? (section.generateTopicBtn2 = false)
      : (section.generateTopicBtn2 = true);
    section.deleteAll = false;
    section.deleteTopicIcon = false;
    section.topics.push({
      topicId: '',
      level: section.topics[section.topics.length - 1]?.level + 1,
      delete: false,
      topicDuration: '',
      active:
        this.selectedContentType == this.courseContentType.TEST ? true : false, // default false
      name: '', // default empty
      disabled: false,
      checkTopic: false,
      topicContainer: true, //default false
      contentScreen: false, //default false
      videoSection: false, //default false
      articleSection: false, //default false
      quizSection: false, //default false
      selectedContentType:
        this.selectedContentType == this.courseContentType.TEST ? 'Quiz' : '', //default empty
      topicStatusImg: this.topicStatusIncompleteImg,
      validate: false,
      completed: false,
      contentOptions: ['Video', 'Quiz', 'Article'],
      video: {
        fileProcessing: false,
        videoData: {
          videoId: '',
          delete: false,
          videoFileName: 'Add Video',
          videoBtnName: 'Upload File',
          videoProgress: 0,
          videoFileType: '',
          videoTranscript: '',
          videoSummary: '',
          date: '',
          file: '',
          duration: '',
          videoUrl: '',
          youtubeVideoUrl: '',
          selectedVideo: null,
        },

        documentData: {
          documentFileName: 'Add Resource',
          documentBtnName: 'Upload File',
          documents: [],
        },
        showTable: false,
      },
      article: {
        articleId: '',
        delete: false,
        generateArticleBtn: true, //default true
        articlePrompt: false, //default false
        articlePromptInput: '', //default empty
        showChatBox: false,
        showSpinner: false,
        uploadArticleDocument: false, //default false
        questionAnswers: {
          question: '',
          answers: [],
        },
        articleFileName: 'Add Resource',
        articleBtnName: 'Upload File',
        articleFileType: '', //default empty
        articleDate: '', //default empty
        articleSummary: '',
        articleDocumnetUrl: '',
        articleDocumnetId: '',
        articleProgressBar: false,
        file: '',
        content: '',
      },
      quiz: {
        quizId: '',
        title: '',
        delete: false,
        durationInMinutes: null,
        passingCriteria: null,
        randomQuestion: null,
        randomQuestionType: this.randomQuestionAll,
        generateAIReport: false,
        reportPrompt: '',
        type: this.quizType.BASIC_QUIZ,
        isAllowedToRetake: false,
        questions: [
          {
            questionId: '',
            surveyQuestionCount: 5,
            delete: false,
            label: 'Question ' + 1 + ' ',
            ques: '',
            explanation: '',
            questionType: surveyTopic?.quiz?.type !== this.quizType.SURVEY ? this.questionTypes[0] : this.questionTypes[1],
            answerOrder: this.alphabet[0],
            surveyAnswers: [],
            answers: [
              {
                answerId: '',
                delete: false,
                label: 1,
                ans: '',
                exist: false,
                isCorrectAnswer: false,
              },
            ],
            // correctAnswer: { ans: null },
          },
        ],
      },
    });

    this.updateTopicLevels(section);

    if (this.selectedContentType == this.courseContentType.TEST) {
      this.onSelectContentType(section.topics[section.topics.length - 1]);
    }

    section.createTopics = true;
  }

  openTopicContainer(topic?: any) {
    topic.topicContainer = true;
    topic.contentScreen = true;
  }

  openContent(topic?: any, type?: any) {
    topic.contentScreen = false;
    topic.selectedContentType = type;
    topic.topicStatusImg = this.topicStatusIncompleteImg;
    if (type == this.typeVideo) {
      this.contentSection(topic, true, false, false);
      this.videoValidation(topic);
    } else if (type == this.typeArticle) {
      this.contentSection(topic, false, true, false);
      this.articleValidation(topic);
    } else {
      this.contentSection(topic, false, false, true);
      this.quizValidation(topic);
    }
  }

  onSelectContentType(topic?: any) {
    topic.topicStatusImg = '';
    this.openContent(topic, topic.selectedContentType);
  }

  contentSection(
    topic?: any,
    videoSection?: any,
    articleSection?: any,
    quizSection?: any
  ) {
    topic.videoSection = videoSection;
    topic.articleSection = articleSection;
    topic.quizSection = quizSection;
  }

  addOption(topic?: any, question?: any) {
    if (question?.questionType?.key == this.questionType.TRUE_FALSE) {
      question.answers = [];
      question?.answers.push(
        {
          answerId: '',
          label: '',
          delete: false,
          ans: 'True',
          exist: false,
          answerOrder: '',
          isCorrectAnswer: false,
        },
        {
          answerId: '',
          label: '',
          delete: false,
          ans: 'False',
          exist: false,
          answerOrder: '',
          isCorrectAnswer: false,
        }
      );
    } else if (question?.questionType?.key == this.questionType.TEXT_FIELD) {
      question.answers = [];
      question?.answers.push({
        answerId: '',
        label: '',
        delete: false,
        ans: '',
        exist: false,
        answerOrder: '',
        isCorrectAnswer: false,
      });
    } else {
      question?.answers.push({
        answerId: '',
        label: '',
        delete: false,
        ans: '',
        exist: false,
        answerOrder: '',
        isCorrectAnswer: false,
      });
    }

    this.maintainQuizQuestionAnswersOrder(question?.answers);
    this.quizValidation(topic);
  }

  addQuestion(topic?: any, questions?: any) {
    questions.push({
      questionId: '',
      delete: false,
      label: 'Question ',
      ques: '',
      explanation: '',
      questionType: topic.quiz?.type !== this.quizType.SURVEY ? this.questionTypes[0] : this.questionTypes[1],
      surveyQuestionCount: 5,
      surveyAnswers: this.generateSurveyOptions(5),
      answers: [
        {
          answerId: '',
          label: '',
          delete: false,
          ans: '',
          exist: false,
          isCorrectAnswer: false,
          answerOrder: '',
        },
      ],
      // correctAnswer: { ans: null },
    });
    if (topic?.quiz?.randomQuestionType == this.randomQuestionAll) {
      this.onSelectRandomQuestionType(topic);
    }
    this.maintainQuizQuestionAnswersOrder(
      questions[questions?.length - 1]?.answers
    );
    this.quizValidation(topic);
    console.log(this.sections);
  }

  maintainQuizQuestionAnswersOrder(answers?: any) {
    let counter = 0;
    answers?.forEach((ans: any) => {
      if (!ans?.delete) {
        ans.answerOrder = this.alphabet[counter];
        counter++;
      }
    });
  }

  onQuizInputChange(topic?: any, question?: any) {
    this.quizValidation(topic);

    // If a specific question is provided and it was invalid, check if it's now valid
    if (question && topic.quiz?.invalidQuestions) {
      const questionIndex = topic.quiz.questions.indexOf(question);
      if (questionIndex !== -1) {
        const invalidIndex = topic.quiz.invalidQuestions.indexOf(questionIndex);
        if (invalidIndex !== -1 && question.ques && question.ques.trim() !== '') {
          // Remove from invalid questions if now valid
          topic.quiz.invalidQuestions.splice(invalidIndex, 1);
        }
      }
    }
  }

  quizOptions(question?: any, index?: any) {
    let ansAlreadyExist = false;

    // if (
    //   !question?.answers.find(
    //     (answer: any) => answer.ans == question.correctAnswer?.ans
    //   )
    // ) {
    //   question.correctAnswer.ans = null;
    // }

    question.answers.forEach((answer: any, ind: any) => {
      if (
        ind != index &&
        !answer.delete &&
        answer.ans == question.answers[index].ans
      ) {
        ansAlreadyExist = true;
      }
    });

    if (ansAlreadyExist) {
      question.answers[index].exist = true;
      this._messageService.error('Option already exist in this question');
    } else {
      question.answers[index].exist = false;
    }
  }

  // saveQuiz(topic?: any) {
  //   topic.completed = false;
  //   topic.validate = false;
  //   this.quizValidation(topic);
  //   if (topic.validate) {
  //     this.checkPreviousTopic(topic);
  //     topic.completed = true;
  //     topic.validate = true;
  //     topic.topicStatusImg = this.topicStatusCompleteImg;
  //     topic.active = !topic.active;

  //     if (topic.quiz) {
  //     topic.quiz.generateAIReport = topic.quiz.generateAIReport || false;
  //     topic.quiz.reportPrompt = topic.quiz.reportPrompt || '';
  //   }
  //   }
  // }
  // saveQuiz(topic?: any) {
  //   topic.completed = false;
  //   topic.validate = false;
  //   this.quizValidation(topic);
  //   if (topic.validate) {
  //     this.checkPreviousTopic(topic);
  //     topic.completed = true;
  //     topic.validate = true;
  //     topic.topicStatusImg = this.topicStatusCompleteImg;
  //     topic.active = !topic.active;
  //   }
  // }

  // saveVideo(topic?: any) {
  //   topic.completed = false;
  //   topic.validate = false;
  //   this.videoValidation(topic);
  //   if (topic.validate) {
  //     this.checkPreviousTopic(topic);
  //     topic.completed = true;
  //     topic.topicStatusImg = this.topicStatusCompleteImg;
  //     topic.active = !topic.active;
  //   }
  // }

  // saveArticle(topic?: any) {
  //   topic.completed = false;
  //   topic.validate = false;
  //   this.articleValidation(topic);
  //   if (topic.validate) {
  //     this.checkPreviousTopic(topic);
  //     topic.completed = true;
  //     topic.topicStatusImg = this.topicStatusCompleteImg;
  //     topic.active = !topic.active;
  //   }
  // }

  quizValidation(topic?: any) {
  let questionIteration = 0;
  topic.quiz.invalidQuestions = []; // Track invalid questions
  topic.quiz.validationMessages = []; // Add validation messages for better feedback

  // Reset previous validation messages
  delete topic.quiz.validationError;

  topic.quiz?.questions.forEach((question: any, questionIndex: number) => {
    let answerIteration = 0;
    if (!question.delete) {
      questionIteration += 1;
      question.label = 'Question ' + questionIteration + ' ';
      question.answers.forEach((answer: any) => {
        if (!answer.delete) {
          answerIteration += 1;
          answer.label = answerIteration;
        }
      });
    }
  });

  let outerLoop = true;
  const questions = topic.quiz?.questions.filter(
    (question: any) => question.delete == false
  );

  const quiz = topic?.quiz;
  let passingCriteriaPresent = false;
  let durationInMinutesPresent = false;

  if (quiz?.durationInMinutes && quiz?.durationInMinutes > 0) {
    durationInMinutesPresent = true;
  }

  if (quiz?.passingCriteria && quiz?.passingCriteria > 0) {
    passingCriteriaPresent = true;
  }

  // Check basic quiz requirements
  if (questions?.length > 0 &&
    (topic.quiz.title != '' ||
      this.selectedContentType == this.courseContentType.TEST) &&
    durationInMinutesPresent &&
    passingCriteriaPresent) {

    for (let i = 0; i < questions.length && outerLoop == true; i++) {
      const answers = questions[i].answers.filter(
        (answer: any) => answer.delete == false
      );

      let isQuestionValid = true;
      let questionErrorMessage = '';

      // Check if question text is empty
      if (!questions[i].ques || questions[i].ques.trim() === '') {
        isQuestionValid = false;
        questionErrorMessage = 'Question text cannot be empty';
        topic.quiz.invalidQuestions.push(i);
      }

      // Check answer requirements based on question type
      if (isQuestionValid) {
        const questionType = questions[i].questionType.key;
        
        if (questionType === this.questionType.TEXT_FIELD) {
          // For text field questions, only one answer is allowed
          if (answers.length !== 1) {
            isQuestionValid = false;
            questionErrorMessage = 'Text field questions should have exactly one answer';
            topic.quiz.invalidQuestions.push(i);
          } else if (!answers[0].ans || answers[0].ans.trim() === '' || answers[0].exist) {
            isQuestionValid = false;
            questionErrorMessage = 'Text field answer cannot be empty';
            topic.quiz.invalidQuestions.push(i);
          }
        } 
        else if (questionType === this.questionType.TRUE_FALSE) {
          // True/False questions need exactly 2 answers
          if (answers.length !== 2) {
            isQuestionValid = false;
            questionErrorMessage = 'True/False questions must have exactly two options (True and False)';
            topic.quiz.invalidQuestions.push(i);
          }
        }
        else if (questionType === this.questionType.SINGLE_CHOICE || 
                 questionType === this.questionType.MULTIPLE_CHOICE) {
          
          // For single/multiple choice, check number of options
          if (answers.length < 2) {
            isQuestionValid = false;
            questionErrorMessage = questionType === this.questionType.SINGLE_CHOICE 
              ? 'Single choice questions require at least two options' 
              : 'Multiple choice questions require at least two options';
            topic.quiz.invalidQuestions.push(i);
          }
          
          // Check if answers are valid
          if (isQuestionValid) {
            for (let j = 0; j < answers.length; j++) {
              if (!answers[j].ans || answers[j].ans.trim() === '' || answers[j].exist) {
                isQuestionValid = false;
                questionErrorMessage = 'One or more options are empty or duplicate';
                topic.quiz.invalidQuestions.push(i);
                break;
              }
            }
          }
          
          // Check for correct answers
          if (isQuestionValid) {
            const correctAnswers = answers.filter((answer: any) => answer.isCorrectAnswer);
            
            if (questionType === this.questionType.SINGLE_CHOICE && correctAnswers.length !== 1) {
              isQuestionValid = false;
              questionErrorMessage = 'Single choice questions require exactly one correct answer';
              topic.quiz.invalidQuestions.push(i);
            } 
            else if (questionType === this.questionType.MULTIPLE_CHOICE && correctAnswers.length < 1) {
              isQuestionValid = false;
              questionErrorMessage = 'Multiple choice questions require at least one correct answer';
              topic.quiz.invalidQuestions.push(i);
            }
          }
        }
      }

      if (!isQuestionValid) {
        topic.validate = false;
        topic.topicStatusImg = this.topicStatusIncompleteImg;
        outerLoop = false;
      } else {
        topic.validate = true;
      }
    }
  } else {
    topic.validate = false;
    topic.topicStatusImg = this.topicStatusIncompleteImg;
    
    // Set specific validation messages
    if (!questions?.length) {
      topic.quiz.validationError = 'At least one question is required';
    } else if (!durationInMinutesPresent) {
      topic.quiz.validationError = 'Quiz duration is required';
    } else if (!passingCriteriaPresent && topic.quiz.type !== this.quizType.SURVEY) {
      topic.quiz.validationError = 'Passing criteria is required';
    } else if (!topic.quiz.title?.trim() && this.selectedContentType != this.courseContentType.TEST) {
      topic.quiz.validationError = 'Quiz title is required';
    }
  }
  
  // Remove duplicates from invalid questions array
  topic.quiz.invalidQuestions = [...new Set(topic.quiz.invalidQuestions)];
}

  videoValidation(topic?: any) {
    const documents = topic.video?.documentData?.documents.filter(
      (document: any) => document.documentProgress != 100
    );
    if (
      (topic?.video?.videoData?.videoProgress == 100 &&
        documents?.length == 0) ||
      (topic?.video?.documentData?.documents[0]?.documentProgress &&
        documents?.length == 0)
    ) {
      topic.validate = true;
    } else {
      topic.validate = false;
      topic.topicStatusImg = this.topicStatusIncompleteImg;
    }
  }

  articleValidation(topic?: any) {
    if (
      topic.article.content != '' &&
      topic.article.content != null &&
      !topic.article.articleProgressBar
    ) {
      topic.validate = true;
    } else {
      topic.validate = false;
      topic.topicStatusImg = this.topicStatusIncompleteImg;
    }
  }

  checkFileInProcess(topic?: any) {
    if (
      (topic.video.videoData.videoFileType == '' ||
        topic.video.videoData.videoProgress == 100) &&
      topic.video?.documentData?.documents.every(
        (document: any) => document.documentProgress == 100
      )
    ) {
      topic.video.fileProcessing = true;
    } else {
      topic.video.fileProcessing = false;
    }
  }

  steps(step?: any) {
    console.log(this.sections);
    if (this.sectionValidation()) {
      this.publishCourse(step);
    } else {
      this._messageService.error('Please complete the sections');
    }
  }

  backToPreviousStep(step?: any) {
    this.currentStep.emit(step);
    // this.sectionsDataOutPut.emit(this.sections);
  }

  sectionValidation() {
    let sectionValid = true;
    console.log(this.sections, 'sections');
    if (
      this.sections?.length != 0 &&
      this.sections?.filter((section: any) => !section.delete)?.length > 0
    ) {
      this.sections.forEach((section: any) => {
        if (!section.delete) {
          if (
            !section.deleteAll &&
            section.name != null &&
            section.name != ''
          ) {
            if (section.topics.length != 0) {
              section.topics.forEach((topic: any) => {
                // **NEW: Allow survey topics even if they don't meet strict validation**
                const isSurvey = topic.quiz?.type === this.quizType.SURVEY;
                const isValidOrSurvey = topic.validate || isSurvey;
                this.validateSurvey(topic);

                if (
                  (!topic.delete && !isValidOrSurvey) ||
                  (topic.name == null && !topic.delete) ||
                  (topic.name == '' && !topic.delete)
                ) {
                  sectionValid = false;
                  return;
                }
              });
            } else {
              sectionValid = false;
              return;
            }
          } else {
            sectionValid = false;
            return;
          }
        }

        if (!sectionValid) {
          return;
        }
      });
    } else {
      sectionValid = false;
    }
    return sectionValid;
  }

  validateSurvey(topic: any) {
    // Skip strict validation for surveys
    if (topic.quiz.type === this.quizType.SURVEY) {
      // For surveys, only check if there's at least one question with text
      const nonDeletedQuestions = topic.quiz.questions.filter((q: any) => !q.delete);
      const hasValidQuestion = nonDeletedQuestions.every(
        (q: any) => q.ques && q.ques.trim() !== ''
      );

      const hasValidOptions = nonDeletedQuestions.every((q: any) => {
        return q.surveyAnswers.every((op: any) => op.answer && op.answer.trim() !== '');
      });

      if (hasValidQuestion && hasValidOptions) {
        topic.validate = true;
        topic.topicStatusImg = this.topicStatusCompleteImg;
        return;
      }
      topic.validate = false;
      topic.topicStatusImg = this.topicStatusIncompleteImg;
      this._messageService.error('Survey questions or answers cannot be empty.');
      this.scrollToTopic(topic);
      throw new Error('Survey questions or answers cannot be empty.');
    }
  }

  stopCollapse(event: Event): void {
    event.stopPropagation();
  }

  dropSection(event: CdkDragDrop<string[]>) {
    moveItemInArray(this.sections, event.previousIndex, event.currentIndex);
    this.updateSectionLevels();
  }

  dropTopic(event: CdkDragDrop<string[]>, topics?: any) {
    moveItemInArray(topics, event.previousIndex, event.currentIndex);
  }

  dropQuizQuestion(event: CdkDragDrop<string[]>, topic?: any, questions?: any) {
    moveItemInArray(questions, event.previousIndex, event.currentIndex);
    this.quizValidation(topic);
  }

  onDragSectionStarted(section: any) {
    section.active = false;
  }

  onDragTopicStarted(topic: any) {
    topic.active = false;
  }

  sectionEnter(event: CdkDragEnter<string[]>) {
    event.item.dropContainer._dropListRef.disabled = true; // Disable drop
  }

  topicEnter(event: CdkDragEnter<string[]>) {
    event.item.dropContainer._dropListRef.disabled = true; // Disable drop
  }

  quizQuestionEnter(event: CdkDragEnter<string[]>) {
    event.item.dropContainer._dropListRef.disabled = true; // Disable drop
  }

  startVideoProgressSimulation(topic?: any) {
    this.progressIntervalVideo$ = interval(4000)?.subscribe(() => {
      // Simulating progress increment every second
      topic.video.videoData.videoProgress += 5; // Adjust as needed
    });
  }

  stopVideoProgressSimulation() {
    if (this.progressIntervalVideo$) {
      this.progressIntervalVideo$.unsubscribe();
    }
  }

  startDocumentProgressSimulation(document?: any) {
    const documentKey = document.documentKey;
    document.documentProgress += 1;
    const interval$ = interval(4000)?.subscribe(() => { });
    this.progressIntervals.set(documentKey, interval$); // Store the interval for each document
  }

  stopDocumentProgressSimulation(documentKey: string) {
    const interval = this.progressIntervals.get(documentKey);
    if (interval) {
      interval.unsubscribe(); // Unsubscribe from the specific interval
      this.progressIntervals.delete(documentKey); // Remove the interval from the collection
    }
  }

  documentSummaryModal(document?: any) {
    const modal = this._modal.create({
      nzContent: SummaryModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        document: document,
        file: document.file,
        fileType: 'DOCS',
        documentSummary: true,
        videoSummary: false,
        articleSummary: false,
      },
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: '70%',
    });
  }

  videoSummaryModal(videoData?: any) {
    const modal = this._modal.create({
      nzContent: SummaryModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        videoData: videoData,
        file: videoData.file,
        fileType: 'VIDEO',
        documentSummary: false,
        videoSummary: true,
        articleSummary: false,
      },
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: '70%',
    });
  }

  articleSummaryModal(article?: any) {
    const modal = this._modal.create({
      nzContent: SummaryModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        article: article,
        file: article.file,
        fileType: 'DOCS',
        documentSummary: false,
        videoSummary: false,
        articleSummary: true,
      },
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: '70%',
    });
  }

  videoTranscriptModal(videoData?: any) {
    const modal = this._modal.create({
      nzContent: TranscriptModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        videoData: videoData,
      },
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: '70%',
    });
  }

  allTopicCheck(section?: any, event?: any) {
    // section.deleteTopicIcon = !section.deleteTopicIcon;
    section.topics.forEach((topic: any) => {
      topic.checkTopic = event.target.checked ? true : false;
    });

    this.deleteTopicIcon(section);
  }

  singleTopicCheck(section: any, topic: any, event?: any) {
    if (section.topics.some((topic: any) => topic.checkTopic === false)) {
      section.checkAll = false;
    }
    this.deleteTopicIcon(section);
  }

  deleteTopicIcon(section: any) {
    if (section.topics.some((topic: any) => topic.checkTopic === true)) {
      section.deleteTopicIcon = true;
    } else {
      section.deleteTopicIcon = false;
    }
  }

  deleteTopics(section?: any) {
    if (!section) return;

    const selectedTopics = section.topics.filter((topic: any) => topic.checkTopic);

    if (section.topics.length - selectedTopics.length < 1) {
      this._messageService.error('You cannot delete all topics. At least one topic must remain in the section.');
      return;
    }
    const modal = this._modal.create({
      nzContent: DeletionModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: '40%',
      nzComponentParams: {
        msg: 'Are you sure you want to delete the selected topics?',
      },
    });

    modal.componentInstance?.deleteClick?.subscribe(() => {
      section.topics.forEach((topic: any) => {
        if (topic.checkTopic) {
          topic.delete = true;
        }
      });
      let topics = section.topics.filter(
        (topic: any) => topic.checkTopic === false
      );
      if (topics.length == 0) {
        section.deleteTopicIcon = false;
        section.checkAll = false;
        section.deleteAll = true;
      }
      this.deleteTopicIcon(section);
      this.updateTopicLevels(section);
      section.deleteTopicIcon = false;
    });
  }

  deleteVideoData(topic: any) {
    this.deleteVideoOrDocument(
      topic.video.videoData.videoId,
      topic.video.videoData.videoUrl,
      topic.topicId,
      'VIDEO'
    );

    topic.video.videoData = {
      videoId: '',
      delete: true,
      videoFileName: 'Add Video',
      videoBtnName: 'Upload File',
      videoProgress: 0,
      videoFileType: '',
      videoTranscript: '',
      videoSummary: '',
      date: '',
      file: '',
    };
    topic.video.documentData.documentFileName = 'Add Resource';
    topic.video.documentData.documentBtnName = 'Upload File';
    topic.video.documentData.documents = [];
    const deleteDocuments = topic.video.documentData.documents.filter(
      (document: any) => document.delete == true
    );
    topic.video.documentData.documents.length == deleteDocuments.length &&
      topic.video.videoData.videoFileName == 'Add Video'
      ? (topic.video.showTable = false)
      : (topic.video.showTable = true);

    this.videoValidation(topic);
  }

  deleteDocument(topic?: any, index?: any) {
    this.deleteVideoOrDocument(
      topic.video.documentData.documents[index].id,
      topic.video.documentData.documents[index].documentUrl,
      topic.topicId,
      'DOCS'
    );
    topic.video.documentData.documents[index].id = '';
    topic.video.documentData.documents[index].delete = true;
    const deleteDocuments = topic.video.documentData.documents.filter(
      (document: any) => document.delete == true
    );
    // topic.video.documentData.documents.splice(index, 1);
    topic.video.documentData.documents.length == deleteDocuments.length &&
      topic.video.videoData.videoFileName == 'Add Video'
      ? (topic.video.showTable = false)
      : (topic.video.showTable = true);
    topic.video.documentData.documents.length == deleteDocuments.length
      ? (topic.video.documentData.documentFileName = 'Add Resource')
      : 'Add More';
    this.videoValidation(topic);
  }

  topicPromptKeyUp(topic?: any) {
    topic.topicStatusImg = this.topicStatusIncompleteImg;
  }

  getCurrentDate() {
    const currentDate = new Date();
    const day = String(currentDate.getDate()).padStart(2, '0');
    const month = String(currentDate.getMonth() + 1).padStart(2, '0');
    const year = currentDate.getFullYear();
    return `${month}/${day}/${year}`;
  }

  formatDate(date?: any) {
    const formattedDate = new Date(date);
    const day = String(formattedDate.getDate()).padStart(2, '0');
    const month = String(formattedDate.getMonth() + 1).padStart(2, '0');
    const year = formattedDate.getFullYear();
    const d = `${month}/${day}/${year}`;
    return d;
  }

  openArticlePrompt(article?: any) {
    article.generateArticleBtn = false;
    article.articlePrompt = true;
  }

  articleInputChange(article?: any) {
    article.articlePromptInput = article.articlePromptInput.trim();
  }

  generateArticles(article?: any) {
    article.articlePrompt = false;
    article.questionAnswers.question = article.articlePromptInput;
    article.articlePromptInput = '';
    article.showChatBox = true;
    article.questionAnswers.answers = [];
    article.showSpinner = true;

    this._instructorService
      .generator(article.questionAnswers.question)
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            article.showSpinner = false;
            const points = response?.data.split('\n').map((point: any) => {
              article.questionAnswers.answers.push(point);
            });
          }
        },
        error: (error: any) => {
          article.showSpinner = false;
        },
      });
  }

  clearArticleChat(article?: any) {
    article.showChatBox = false;
    article.articlePrompt = true;
    article.questionAnswers.question = '';
    article.questionAnswers.answers = [];
  }

  deleteArticleDocument(topic?: any) {
    const article = topic.article;
    this.deleteVideoOrDocument(
      article.articleDocumnetId,
      article.articleDocumnetUrl,
      topic.topicId,
      'DOCS'
    );
    // article.articleId = '';
    // article.delete = true;
    article.articleFileName = 'Upload File';
    article.articleSummary = '';
    article.articleDocumnetUrl = '';
    article.articleBtnName = 'Add Resource';
  }

  deleteVideoOrDocument(id?: any, url?: any, topicId?: any, type?: any) {
    this._fileManagerService.deleteFile(id, url, topicId, type)?.subscribe({
      next: (response: any) => {
        if (response == null) {
        }
      },
      error: (error: any) => { },
    });
  }

  openUploadArticleScreen(article?: any) {
    article.uploadArticleDocument = true;
  }

  sectionActive(event?: any) {
    event.preventDefault();
    event.stopPropagation();
  }

  topicActive(event?: any, topic?: any) {
    event.preventDefault();
    event.stopPropagation();
    topic.active = !topic.active;
    console.log('Topic active state:', topic.active, 'Topic name:', topic.name); // Add this for debugging
  }

  customRequestVideo = (item: NzUploadXHRArgs): Subscription => {
    const file = item.file as unknown as File;

    const validPattern = /^[a-zA-Z0-9._\-()@+\[\],\s]+$/;
    if (validPattern.test(file.name)) {
      if (
        file.type.split('/')[0] == 'video' &&
        file.type.split('/')[1] == 'mp4'
      ) {
        return this._fileManagerService.uploadFile(file, 'VIDEO')?.subscribe({
          next: (response: any) => {
            // this.stopVideoProgressSimulation();
            const topic = this.uploadedVideos.get(file.name);
            topic.video.videoData.videoTranscript =
              response?.data?.transcriptData?.transcript;
            topic.video.videoData.videoSubtitles =
              response?.data?.transcriptData?.vttContent;
            topic.video.videoData.videoSummary =
              response?.data?.transcriptData?.summary;
            topic.video.videoData.videoBtnName = 'Replace';

            topic.video.videoData.videoProgress = 100;
            topic.video.videoData.videoUrl = response?.data?.url;
            this.checkFileInProcess(topic);
            this.videoValidation(topic);
          },
          error: (error: any) => {
            // this._messageService.error(error?.error?.message);
            const topic = this.uploadedVideos.get(file.name);
            topic.video.videoData.videoFileName = '';
            topic.video.videoData.videoProgress = 0;
            topic.video.videoData.videoBtnName = 'Replace';
            topic.video.videoData.videoFileType = '';
            topic.video.videoData.date = '';
            topic.video.videoData.videoFileName = 'Add Video';
          },
        });
      }
    } else {
      // this._messageService.error('File name contains special characters.');
      return null;
    }
    return null;
  };

  handleVideoChange(info: NzUploadChangeParam, topic?: any): void {
    const validPattern = /^[a-zA-Z0-9._\-()@+\[\],\s]+$/;
    if (validPattern.test(info.file.name)) {
      if (
        info.file.type.split('/')[0] == 'video' &&
        info.file.type.split('/')[1] == 'mp4'
      ) {
        const maxSizeGB = 4;
        if (info.file.size < maxSizeGB * 1024 * 1024 * 1024) {
          if (topic.video.videoData.videoFileType != '') {
            this.deleteVideoData(topic);
          }

          topic.validate = false;
          (topic.video.videoData.delete = false),
            (topic.video.fileProcessing = false);
          topic.video.videoData.videoProgress = 0;
          topic.video.videoData.videoFileType = '';
          topic.video.videoData.videoFileName = '';
          info.file.type.split('/')[0];
          topic.video.videoData.videoFileName = info.file.name;
          topic.video.videoData.videoFileType = info.file.type.split('/')[0];
          topic.video.videoData.date = this.getCurrentDate();
          topic.video.videoData.file = info.file;
          topic.video.showTable = true;
          topic.video.videoData.videoProgress = 1;
          this.uploadedVideos.set(info.file.name, topic);
          this.getVideoDuration(info.file.originFileObj as unknown as File)
            .then((duration) => {
              topic.topicDuration = duration;
            })
            .catch((error) => { });
        } else {
          this._messageService.error('Size should not exceed 4 GB.');
        }
      } else {
        this._messageService.error('Please upload a video file in MP4 format.');
      }
    } else {
      this._messageService.error('File name contains special characters.');
    }
  }

  getVideoDuration(file: File): Promise<number> {
    return new Promise<number>((resolve, reject) => {
      const video = document.createElement('video');

      video.addEventListener('loadedmetadata', () => {
        resolve(video.duration);
      });
      video.addEventListener('error', (event) => {
        reject(event);
      });
      video.src = URL.createObjectURL(file);
    });
  }

  customRequestDocument = (item: NzUploadXHRArgs): Subscription => {
    const file = item.file as unknown as File;

    const validPattern = /^[a-zA-Z0-9._\-()@+\[\],\s]+$/;
    if (validPattern.test(file.name)) {
      if (file.type.split('/')[1] == 'pdf') {
        return this._fileManagerService.uploadFile(file, 'DOCS')?.subscribe({
          next: (response: any) => {
            if (
              response?.status ===
              this._httpConstants.REQUEST_STATUS.CREATED_201.CODE
            ) {
              const topic = this.uploadedDocuments.get(file.name);
              const objectToUpdate = topic.video.documentData.documents.find(
                (document: any) => document.documentKey === file.name
              );
              if (objectToUpdate) {
                // this.stopDocumentProgressSimulation(file.name); // Stop specific progress
                objectToUpdate.documentProgress = 100;
                objectToUpdate.documentSummary = response?.data?.summary;
                objectToUpdate.documentUrl = response?.data?.url;
              }
              this.checkFileInProcess(topic);
              this.videoValidation(topic);
            }
          },
          error: (error: any) => {
            const topic = this.uploadedDocuments.get(file.name);
            const objectToUpdate = topic.video.documentData.documents.find(
              (document: any) => document.documentKey === file.name
            );
            if (objectToUpdate) {
              // this.stopDocumentProgressSimulation(file.name); // Stop specific progress
              objectToUpdate.documentProgress = 0;
              objectToUpdate.documentFileType = '';
              objectToUpdate.documentFileName = '';
              objectToUpdate.date = '';
              objectToUpdate.documentKey = '';
            }
          },
        });
      } else {
        return null;
      }
    } else {
      // this._messageService.error('File name contains special characters.');
      return null;
    }
  };

  handleDocumentChange(info: NzUploadChangeParam, topic?: any): void {
    const validPattern = /^[a-zA-Z0-9._\-()@+\[\],\s]+$/;
    if (validPattern.test(info.file.name)) {
      if (info.file.type.split('/')[1] == 'pdf') {
        topic.video.fileProcessing = false;
        topic.validate = false;
        const fileType = info.file.type.split('/')[1];
        const documentKey = info.file.name;
        topic.video.documentData.documentFileName = info.file.name;
        topic.video.documentData.documents.push({
          id: '',
          delete: false,
          documentUrl: '',
          documentProgress: 1,
          documentFileName: info.file.name,
          documentFileType: fileType,
          date: this.getCurrentDate(),
          documentSummary: '',
          file: info.file as unknown as File,
          documentKey,
        });

        topic.video.showTable = true;
        const lastIndex = topic.video.documentData.documents.length - 1;
        // this.startDocumentProgressSimulation(topic.video.documentData.documents[lastIndex]);

        this.uploadedDocuments.set(info.file.name, topic);
      } else {
        this._messageService.error('Please upload a pdf file.');
      }
    } else {
      this._messageService.error('File name contains special characters.');
    }
  }

  customRequestArticleDocument = (item: NzUploadXHRArgs): Subscription => {
    const file = item.file as unknown as File;

    const validPattern = /^[a-zA-Z0-9._\-()@+\[\],\s]+$/;
    if (validPattern.test(file.name)) {
      if (file.type.split('/')[1] == 'pdf') {
        return this._fileManagerService.uploadFile(file, 'DOCS')?.subscribe({
          next: (response: any) => {
            if (
              response?.status ==
              this._httpConstants.REQUEST_STATUS.CREATED_201.CODE
            ) {
              const topic = this.uploadedArticleDocuments.get(file.name);
              topic.article.articleProgressBar = false;
              this.articleValidation(topic);
              topic.article.articleSummary = response?.data?.summary;
              topic.article.articleDocumnetUrl = response?.data.url;
              (topic.article.articleFileType = file.type.split('/')[1]),
                (topic.article.articleDate = this.getCurrentDate()),
                (topic.article.articleBtnName = 'Replace');
            }
          },
          error: (error: any) => {
            const topic = this.uploadedArticleDocuments.get(file.name);
            topic.article.articleFileName = '';
            topic.article.articleProgressBar = false;
            this.articleValidation(topic);
          },
        });
      } else {
        return null;
      }
    } else {
      // this._messageService.error('File name contains special characters.');
      return null;
    }
  };

  handleArticleDocumentChange(info: NzUploadChangeParam, topic?: any): void {
    const validPattern = /^[a-zA-Z0-9._\-()@+\[\],\s]+$/;
    if (validPattern.test(info.file.name)) {
      if (info.file.type.split('/')[1] == 'pdf') {
        topic.article.articleProgressBar = true;
        this.articleValidation(topic);
        if (topic.article.articleDocumnetUrl != '') {
          this.deleteArticleDocument(topic);
        }
        topic.article.articleFileName = info.file.name;
        topic.article.file = info.file as unknown as File;
        this.uploadedArticleDocuments.set(info.file.name, topic);
      } else {
        this._messageService.error('Please upload a pdf file.');
      }
    } else {
      this._messageService.error('File name contains special characters.');
    }
  }

  markCorrectAnswer(topic?: any, question?: any, answer?: any) {
    // question.correctAnswer.ans = answer?.ans;
    let valid = false;
    if (question?.questionType?.key == this.questionType.MULTIPLE_CHOICE) {
      valid = true;
    } else {
      question?.answers?.forEach((answer?: any) => {
        answer.isCorrectAnswer = false;
      });
      valid = true;
    }

    if (valid) {
      answer.isCorrectAnswer = !answer.isCorrectAnswer;
      this.onQuizInputChange(topic);
    }
  }

  changeQuestionType(topic?: any, question?: any) {
    question?.answers?.forEach((el: any, index: number) => {
      el.isCorrectAnswer = false;
    });
    if (question?.questionType?.key == this.questionType.TRUE_FALSE || question?.questionType?.key == this.questionType.TEXT_FIELD) {
      this.addOption(topic, question);
    }
    this.quizValidation(topic);
  }

  removeQuizQuestion(topic?: any, question?: any) {
    const nonDeletedQuestions = topic.quiz.questions.filter((q: any) => !q.delete);
    if(nonDeletedQuestions.length <= 1) {
      this._messageService.error("At least one question is required in the quiz.");
      return;
    }
    question.delete = true;
    question.answers = [];
    // question.correctAnswer.ans = null;
    if (
      topic.quiz.randomQuestion &&
      topic.quiz.randomQuestion >=
      topic?.quiz?.questions?.filter((question?: any) => !question?.delete)
        ?.length
    ) {
      topic.quiz.randomQuestion = topic?.quiz?.questions?.filter(
        (question?: any) => !question?.delete
      )?.length;
    }
    this.quizValidation(topic);
  }

  removeQuizQuestionAnswers(topic?: any, question?: any, answer?: any) {
    answer.delete = true;
    // if (question.correctAnswer.ans == answer.ans) {
    //   question.correctAnswer.ans = null;
    // }
    this.maintainQuizQuestionAnswersOrder(question?.answers);
    this.quizValidation(topic);
  }

  editorArticleContentChanged(event: any, topic?: any): void {
    if (!event) {
      topic.article.content = '';
    }
    this.articleValidation(topic);
  }

  get getInitialOfLoggedInUser() {
    return this._authService.getLoggedInName();
  }

  sectionActiveStatus(event?: any, section?: any) {
    event.preventDefault();
    event.stopPropagation();
    section.active = !section.active;
  }

  singleSectionCheck(section?: any, event?: any) {
    if (this.sections.some((section: any) => section.checkSection === false)) {
      this.checkAllSection = false;
    }
    this.deleteSectionIcon();
  }

  deleteSectionIcon() {
    if (this.sections.some((section: any) => section.checkSection === true)) {
      this.showDltSectionBtn = true;
    } else {
      this.showDltSectionBtn = false;
    }
  }

  allSectionCheck(event?: any) {
    this.sections.forEach((section: any) => {
      section.checkSection = event.target.checked ? true : false;
    });

    this.deleteSectionIcon();
  }

  deleteSections() {
    const selectedSections = this.sections.filter(s => s.checkSection);

    // Prevent deleting all sections
    if (this.sections.length - selectedSections.length < 1) {
      this._messageService.error('You cannot delete all sections. At least one section must remain.');
      return;
    }

    const modal = this._modal.create({
      nzContent: DeletionModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: '40%',
      nzComponentParams: {
        msg: 'Are you sure you want to delete the selected sections?',
      },
    });
    modal.componentInstance.deleteClick?.subscribe(() => {
      this.sections.forEach((section: any) => {
        if (section.checkSection) {
          section.delete = true;
        }
      });
      this.hideDeleteSectionContainer();
      this.updateSectionLevels();
      this.deleteSectionIcon();
      this.showDltSectionBtn = false;
    });
  }

  hideDeleteSectionContainer() {
    const sections = this.sections.filter(
      (section: any) => section.checkSection === false
    );
    if (sections?.length == 0) {
      // this.showDltSectionBtn = false;
      this.checkAllSection = false;
      // this.showSectionDltContainer = false;
    }
    this.deleteSectionIcon();
  }

  updateSectionLevels() {
    let counter = 0;
    this.sections.forEach((section: any) => {
      if (!section.delete) {
        section.level = counter + 1;
        counter++;
      }
    });
  }

  updateTopicLevels(section?: any) {
    let counter = 0;
    section.topics.forEach((topic: any) => {
      if (!topic.delete) {
        topic.level = counter + 1;
        counter++;
      }
    });
  }

  openDeletionModal(msg?: any) {
    const modal = this._modal.create({
      nzContent: DeletionModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: '40%',
      nzComponentParams: {
        msg: msg,
      },
    });
    modal.componentInstance.cancelClick?.subscribe(() => { });

    modal.componentInstance.deleteClick?.subscribe(() => { });
  }

  checkPreviousTopic(topic?: any) {
    if (topic.selectedContentType == this.typeArticle) {
      this.deletePreviousTopicVideo(topic.video);
      this.deletePreviousTopicQuiz(topic.quiz);
    } else if (topic.selectedContentType == this.typeQuiz) {
      this.deletePreviousTopicVideo(topic.video);
      this.deletePreviousTopicArticle(topic.article);
    } else if (topic.selectedContentType == this.typeVideo) {
      this.deletePreviousTopicQuiz(topic.quiz);
      this.deletePreviousTopicArticle(topic.article);
    }
  }

  deletePreviousTopicVideo(video?: any) {
    if (video.videoData.videoId != '') {
      video.videoData.delete = true;
      if (video.documentData.documents.length > 0) {
        video.documentData.documents.forEach((el: any) => {
          el.delete = true;
        });
      }
    }
  }

  deletePreviousTopicQuiz(quiz?: any) {
    if (quiz.quizId != '') {
      quiz.delete = true;
    }
  }

  deletePreviousTopicArticle(article?: any) {
    if (article.articleId != '') {
      article.delete = true;
    }
  }

  saveAsDraftCourse() {
    if (
      !this.courseSaved &&
      this.sections &&
      this.courseInformationData?.get('courseProgress').value != 100
    ) {
      this.courseSaved = true;
      this._courseService
        .createCourseDto(
          this.courseInformationData,
          this.sections,
          this.courseId,
          false,
          null,
          this.selectedContentType
        )
        ?.subscribe({
          next: (response: any) => {
            if (
              response?.status ==
              this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
            ) {
              if (this.courseId == null) {
                this.courseId = response?.data?.courseId;
              }
              this.communicationService.updateInstructorCourse();
            } else {
              this.courseSaved = false;
            }
          },
          error: (error: any) => { },
        });
    }
  }

  publishCourse(step?: any) {
    if (
      !this.courseSaved &&
      this.sections &&
      this.courseInformationData?.get('courseProgress').value != 100
    ) {
      this.courseSaved = true;
      this._courseService
        .createCourseDto(
          this.courseInformationData,
          this.sections,
          this.courseId,
          false,
          null,
          this.selectedContentType
        )
        ?.subscribe({
          next: (response: any) => {
            if (
              response?.status ==
              this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
            ) {
              if (this.courseId == null) {
                this.courseId = response?.data?.courseId;
              }
              this.sections = [];
              this.communicationService.updateInstructorCourse();
              this.patchSectionData();
              this.assignSurveyAnswersToAnswers();
              this.currentStep.emit(step);
              this.sectionsDataOutPut.emit(this.sections);
            } else {
              this.courseSaved = false;
            }
          },
          error: (error: any) => { },
        });
    } else if (this.courseInformationData.get('courseProgress').value == 100) {
      // this runs when course is published (100% progress)
      this.currentStep.emit(step);
      this.assignSurveyAnswersToAnswers();
      this.sectionsDataOutPut.emit(this.sections);
    }
  }

  assignSurveyAnswersToAnswers() {
    this.sections.forEach((section: any) => {
      section.topics.forEach((topic: any) => {
        if (topic.quiz.type === this.quizType.SURVEY) {
          topic.quiz.questions.forEach((question: any) => {
            question.answers = question.surveyAnswers.map(a => ({
              ...a,
              ans: a.answer,
            }));
          });
        }
      });
    });
  }

  checkYoutubeLink(value: string): void {
    const youtubeRegex =
      /^(https?\:\/\/)?((www|m)\.youtube\.com|youtu\.?be)\/.+$/;
    this.isYoutubeLinkPresent = youtubeRegex.test(value);
    if (this.isYoutubeLinkPresent) {
    }
  }

  youtubeVideoUrlUpload(topic?: any) {
    const videoUrl = topic.video.videoData.youtubeVideoUrl;
    if (videoUrl) {
      if (topic.video.videoData.videoFileType != '') {
        this.deleteVideoData(topic);
      }
      topic.validate = false;
      (topic.video.videoData.delete = false),
        (topic.video.fileProcessing = false);
      topic.video.videoData.videoProgress = 0;
      topic.video.videoData.videoFileType = 'Video';
      topic.video.videoData.videoFileName = 'YOUTUBE';
      topic.video.videoData.date = this.getCurrentDate();
      topic.video.showTable = true;
      topic.video.videoData.videoProgress = 1;
      const videoId = this.extractYoutubeVideoId(videoUrl);
      this._courseService.youtubeVideoUrlUpload(videoId)?.subscribe({
        next: (response) => {
          topic.video.videoData.videoTranscript = '';
          topic.video.videoData.videoTranscript = '';
          topic.video.videoData.videoSubtitles = '';
          // topic.video.videoData.videoBtnName = 'Replace';
          topic.video.videoData.videoProgress = 100;
          topic.topicDuration = response?.data;
          topic.video.videoData.videoUrl = videoUrl;
          topic.video.videoData.youtubeVideoUrl = videoUrl;
          this.checkFileInProcess(topic);
          this.videoValidation(topic);
        },
        error: (error) => {
          this._messageService.error(error?.error?.message);
          topic.video.videoData.videoFileName = '';
          topic.video.videoData.videoProgress = 0;
          topic.video.videoData.videoBtnName = 'Replace';
          topic.video.videoData.videoFileType = '';
          topic.video.videoData.date = '';
          topic.video.videoData.videoFileName = 'Add Video';
          topic.video.videoData.videoFileType = '';
        },
      });
    } else {
      console.warn('YouTube URL is empty');
    }
  }

  extractYoutubeVideoId(url: string): string | null {
    const regex = /(?<=\?v=)[^&]+/;
    const match = url.match(regex);
    return match ? match[0] : null;
  }

  questionShowAnswers(answers?: any) {
    return answers?.filter((ans: any) => !ans.delete);
  }

  validateNumberInput(
    event: KeyboardEvent,
    durationInMinutes?: boolean,
    passingCriteria?: boolean,
    randomNumber?: boolean
  ) {
    const input = event.target as HTMLInputElement;
    const charCode = event.which ? event.which : event.keyCode;

    // Allow only numeric values (0-9)
    if (charCode < 48 || charCode > 57) {
      event.preventDefault();
      return;
    }

    // If passingCriteria is true, prevent values above 100
    if (passingCriteria) {
      const newValue = input.value + event.key;
      if (Number(newValue) > 100) {
        event.preventDefault();
      }
    }
  }

  validateRandomNumberInput(event: KeyboardEvent, topic?: any) {
    const input = event.target as HTMLInputElement;
    const charCode = event.which ? event.which : event.keyCode;

    // Allow only numeric values (0-9)
    if (charCode < 48 || charCode > 57) {
      event.preventDefault();
      return;
    }

    const newValue = input.value + event.key;
    if (Number(newValue) <= 0) {
      event.preventDefault();
    }

    if (
      Number(newValue) >
      topic?.quiz?.questions?.filter((question?: any) => !question?.delete)
        ?.length
    ) {
      event.preventDefault();
    }
  }

  enforceLimit(topic?: any) {
    if (topic.quiz.passingCriteria < 0) {
      topic.quiz.passingCriteria = 0;
    } else if (topic.quiz.passingCriteria > 100) {
      topic.quiz.passingCriteria = 100;
    }
  }

  onSelectRandomQuestionType(topic?: any) {
    topic.quiz.randomQuestion =
      topic?.quiz?.randomQuestionType == this.randomQuestionAll
        ? topic?.quiz?.questions?.length
        : topic?.quiz?.randomQuestion;
  }

  onSelectTopicType(topic: any) {
    console.log("topic: ", topic);
    // this.selectedQuizType = topic?.quiz?.type;
    if (topic?.quiz?.questions.length > 0) {
      topic.quiz.questions.forEach((q: any) => {
        q.ques = '';
        q.surveyAnswers = [];
        if (topic?.quiz?.type === this.quizType.SURVEY) {
          q.surveyAnswers = this.generateSurveyOptions(q.surveyQuestionCount);
        } else {
          q.answers = [
            { answerId: null, ans: '', isCorrectAnswer: false, delete: false },
          ];
        }
      })
    }
    topic.quiz.questions.forEach(q => {
      if (topic?.quiz?.type === this.quizType.SURVEY) {
        q.questionType = this.questionTypes[1];
      }
    })
  }

  onSelectSurveyQuestionCount(question: any) {
    console.log("before question: ", question);
    // this.selectedQuizType = question?.quiz?.type;
    question.surveyAnswers = this.generateSurveyOptions(question.surveyQuestionCount, question.answers);
    /**
     * @description
     * these are the answers that were there before changing the survey question count
     * we are storing them in onHoldAnswers property to avoid data loss
     */
    // question.onHoldAnswers = JSON.parse(JSON.stringify(question.answers));
    question.answers = question.surveyAnswers;
    console.log("after question: ", question);
  }

  generateSurveyOptions(count: number, answers?: any[]) {
    const surveyAnswers = [];
    // const answersMap = new Map([
    //     [5, ["Strongly Disagree", "Disagree", "Neutral", "Agree", "Strongly Agree"]],
    //     [4, ["Strongly Disagree", "Disagree", "Neutral", "Agree", "Strongly Agree"]],
    //     [3, ["Strongly Disagree", "Disagree", "Neutral", "Agree", "Strongly Agree"]],
    //   ])

    for (let i = 0; i < count; i++) {
      surveyAnswers.push({
        answerId: answers?.[i]?.answerId || null,
        count: i + 1,
        answer: this.surveyDefaults[i] || "",
        ans: this.surveyDefaults[i] || "",
        delete: i >= count,
        isCorrectAnswer: false,
      });
    }


    return surveyAnswers;
  }

  updateDefaultSurveyOption(index: number, newValue: string) {
    this.surveyDefaults[index] = newValue;
  }

  preventEmoji(event: KeyboardEvent) {
    const key = event.key;
    const emojiRegex = /\p{Extended_Pictographic}/u;

    if (emojiRegex.test(key)) {
      event.preventDefault();
    }
  }

  preventEmojiOnPaste(event: ClipboardEvent) {
    const pastedText = event.clipboardData?.getData('text') || '';
    const emojiRegex = /\p{Extended_Pictographic}/u;

    if (emojiRegex.test(pastedText)) {
      event.preventDefault();
    }
  }

  onTextFieldAnswerChange(value?: any, topic?: any, answer?: any) {
    answer.isCorrectAnswer = true;
    this.quizValidation(topic);
  }

  validateAndContinue(): void {
    this.markAllFieldsAsTouched();

    if (this.sectionValidation()) {
      this.steps(2);
    } else {
      // First, try to find and scroll to invalid questions
      const firstInvalidQuestion = this.findFirstInvalidQuestion();
      if (firstInvalidQuestion) {
        this.scrollToInvalidQuestion(firstInvalidQuestion.topic, firstInvalidQuestion.questionIndex);
      } else {
        // Fallback to general validation
        this.scrollToFirstInvalidField();
      }
      this.showValidationErrors();
    }
  }

  private findFirstInvalidQuestion(): { topic: any, questionIndex: number } | null {
    for (const section of this.sections) {
      if (!section.delete) {
        for (const topic of section.topics) {
          if (!topic.delete && topic.quizSection && topic.quiz?.invalidQuestions?.length > 0) {
            return {
              topic: topic,
              questionIndex: topic.quiz.invalidQuestions[0]
            };
          }
        }
      }
    }
    return null;
  }

  private markAllFieldsAsTouched(): void {
    // Mark all sections and topics for validation
    this.sections.forEach((section: any) => {
      if (!section.delete) {
        section._touched = true; // Mark section as touched

        section.topics.forEach((topic: any) => {
          if (!topic.delete) {
            topic._touched = true; // Mark topic as touched
          }
        });
      }
    });

    // Trigger change detection
    this._cdr.detectChanges();
  }

  private scrollToFirstInvalidField(): void {
    setTimeout(() => {
      const firstInvalidControl = this.findFirstInvalidControl();
      if (firstInvalidControl) {
        firstInvalidControl.scrollIntoView({
          behavior: 'smooth',
          block: 'center'
        });

        // Add focus for better accessibility
        if (firstInvalidControl.tagName === 'INPUT' ||
          firstInvalidControl.tagName === 'SELECT' ||
          firstInvalidControl.tagName === 'TEXTAREA') {
          (firstInvalidControl as HTMLElement).focus();
        }
      } else {
        // Fallback: scroll to top
        this.formElement?.nativeElement?.scrollIntoView({
          behavior: 'smooth',
          block: 'start'
        });
      }
    }, 100);
  }

  private findFirstInvalidControl(): HTMLElement | null {
    const rootElement = this.formElement?.nativeElement || document;

    // 1. First check for empty section names
    const emptySections = this.sections.filter((section: any) =>
      !section.delete && (!section.name || section.name.trim() === '')
    );

    if (emptySections.length > 0) {
      const firstEmptySection = emptySections[0];
      // Try to find section name input
      const sectionInput = rootElement.querySelector(`input[ng-reflect-model="${firstEmptySection.name}"]`) ||
        rootElement.querySelector('.section-name-input') ||
        rootElement.querySelector('.test-section-name-input');
      if (sectionInput) return sectionInput as HTMLElement;
    }

    // 2. Check for sections without topics
    const sectionsWithoutTopics = this.sections.filter((section: any) =>
      !section.delete && section.topics.length === 0
    );

    if (sectionsWithoutTopics.length > 0) {
      const firstSection = sectionsWithoutTopics[0];
      // Find the add topic button for this section
      const addTopicBtn = rootElement.querySelector('.own-topic-btn') ||
        rootElement.querySelector('.test-add-section-btn');
      if (addTopicBtn) return addTopicBtn as HTMLElement;
    }

    // 3. Check for empty topic names
    for (const section of this.sections) {
      if (!section.delete) {
        const emptyTopics = section.topics.filter((topic: any) =>
          !topic.delete && (!topic.name || topic.name.trim() === '')
        );

        if (emptyTopics.length > 0) {
          const firstEmptyTopic = emptyTopics[0];
          // Try to find topic name input
          const topicInput = rootElement.querySelector(`input[ng-reflect-model="${firstEmptyTopic.name}"]`) ||
            rootElement.querySelector('.topic-input') ||
            rootElement.querySelector('.test-section-name-input');
          if (topicInput) return topicInput as HTMLElement;
        }
      }
    }

    // 4. Check for incomplete topics (not saved/validated)
    for (const section of this.sections) {
      if (!section.delete) {
        const incompleteTopics = section.topics.filter((topic: any) =>
          !topic.delete && !topic.validate
        );

        if (incompleteTopics.length > 0) {
          const firstIncompleteTopic = incompleteTopics[0];
          // Find the save button for this topic
          const saveBtn = rootElement.querySelector('.quiz-save-btn:not(.quiz-save-btn-disable)') ||
            rootElement.querySelector('.quiz-save-btn');
          if (saveBtn) return saveBtn as HTMLElement;

          // Or find the topic container
          const topicContainer = rootElement.querySelector('.topic-collapse-panel');
          if (topicContainer) return topicContainer as HTMLElement;
        }
      }
    }

    return null;
  }

  private showValidationErrors(): void {
    const errors = [];

    // Check for empty section names
    const emptySections = this.sections.filter((section: any) =>
      !section.delete && (!section.name || section.name.trim() === '')
    );
    if (emptySections.length > 0) {
      errors.push(`${emptySections.length} section(s) need names`);
    }

    // Check for sections without topics
    const sectionsWithoutTopics = this.sections.filter((section: any) =>
      !section.delete && section.topics.length === 0
    );
    if (sectionsWithoutTopics.length > 0) {
      errors.push(`${sectionsWithoutTopics.length} section(s) need at least one topic`);
    }

    // Check for empty topic names
    let emptyTopicCount = 0;
    this.sections.forEach((section: any) => {
      if (!section.delete) {
        emptyTopicCount += section.topics.filter((topic: any) =>
          !topic.delete && (!topic.name || topic.name.trim() === '')
        ).length;
      }
    });
    if (emptyTopicCount > 0) {
      errors.push(`${emptyTopicCount} topic(s) need names`);
    }

    // Check for incomplete topics
    let incompleteTopicCount = 0;
    this.sections.forEach((section: any) => {
      if (!section.delete) {
        incompleteTopicCount += section.topics.filter((topic: any) =>
          !topic.delete && !topic.validate
        ).length;
      }
    });
    if (incompleteTopicCount > 0) {
      errors.push(`${incompleteTopicCount} topic(s) need to be completed and saved`);
    }

    if (errors.length > 0) {
      this._messageService.error('Please complete: ' + errors.join(', '));
    }
  }

  // Update your existing save methods to mark topics as touched
  saveQuiz(topic?: any) {
  topic.completed = false;
  topic.validate = false;
  topic._touched = true;
  this.quizValidation(topic);
  
  if (topic.quiz) {
    topic.quiz.generateAIReport = topic.quiz.generateAIReport || false;
    topic.quiz.reportPrompt = topic.quiz.reportPrompt || '';
  }

  if (topic.validate) {
    this.checkPreviousTopic(topic);
    topic.completed = true;
    topic.validate = true;
    topic.topicStatusImg = this.topicStatusCompleteImg;
    topic.active = !topic.active;
    this._messageService.success('Quiz saved successfully!');
  } else {
    // Show specific validation error message
    if (topic.quiz.validationError) {
      this._messageService.error(topic.quiz.validationError);
      this.scrollToTopic(topic);
      return;
    } 
    // Show first question validation error
    else if (topic.quiz.invalidQuestions && topic.quiz.invalidQuestions.length > 0) {
      const firstInvalidIndex = topic.quiz.invalidQuestions[0];
      const firstInvalidQuestion = topic.quiz.questions[firstInvalidIndex];
      const questionNum = firstInvalidIndex + 1;
      
      // Get question type for better error message
      const questionType = firstInvalidQuestion?.questionType?.value || 'question';
      
      // Check specific validation conditions
      let errorMessage = '';
      
      if (!firstInvalidQuestion.ques || firstInvalidQuestion.ques.trim() === '') {
        errorMessage = `Question ${questionNum}: Question text cannot be empty`;
      } 
      else if (firstInvalidQuestion.questionType?.key === this.questionType.SINGLE_CHOICE) {
        const answers = firstInvalidQuestion.answers.filter((a: any) => !a.delete);
        if (answers.length < 2) {
          errorMessage = `Question ${questionNum}: Single choice questions require at least two options`;
        } 
        else if (answers.filter((a: any) => a.isCorrectAnswer).length !== 1) {
          errorMessage = `Question ${questionNum}: Single choice questions require exactly one correct answer`;
        }
        else if (answers.some((a: any) => !a.ans || a.ans.trim() === '')) {
          errorMessage = `Question ${questionNum}: All options must have text`;
        }
      }
      else if (firstInvalidQuestion.questionType?.key === this.questionType.MULTIPLE_CHOICE) {
        const answers = firstInvalidQuestion.answers.filter((a: any) => !a.delete);
        if (answers.length < 2) {
          errorMessage = `Question ${questionNum}: Multiple choice questions require at least two options`;
        } 
        else if (answers.filter((a: any) => a.isCorrectAnswer).length < 1) {
          errorMessage = `Question ${questionNum}: Multiple choice questions require at least one correct answer`;
        }
        else if (answers.some((a: any) => !a.ans || a.ans.trim() === '')) {
          errorMessage = `Question ${questionNum}: All options must have text`;
        }
      }
      else if (firstInvalidQuestion.questionType?.key === this.questionType.TRUE_FALSE) {
        const answers = firstInvalidQuestion.answers.filter((a: any) => !a.delete);
        if (answers.length !== 2) {
          errorMessage = `Question ${questionNum}: True/False questions must have exactly two options (True and False)`;
        }
      }
      else if (firstInvalidQuestion.questionType?.key === this.questionType.TEXT_FIELD) {
        const answers = firstInvalidQuestion.answers.filter((a: any) => !a.delete);
        if (answers.length !== 1) {
          errorMessage = `Question ${questionNum}: Text field questions should have exactly one answer`;
        }
        else if (!answers[0].ans || answers[0].ans.trim() === '') {
          errorMessage = `Question ${questionNum}: Text field answer cannot be empty`;
        }
      }
      
      if (errorMessage) {
        this._messageService.error(errorMessage);
      } else {
        // Fallback generic message
        this._messageService.error(`Question ${questionNum}: Please complete all required fields`);
      }
    } 
    else {
      // Check for other validation errors
      if (!topic.quiz.durationInMinutes || topic.quiz.durationInMinutes <= 0) {
        this._messageService.error('Quiz duration is required');
      }
      
      else if (!topic.quiz.title?.trim() && this.selectedContentType != this.courseContentType.TEST) {
        this._messageService.error('Quiz title is required');
      }     
      // else if (topic.quiz.generateAIReport && !topic.quiz.reportPrompt?.trim()) {
      //   this._messageService.error('Report prompt is required when AI report is enabled');
      // }
      // else {
      //   this._messageService.error('Please complete all required fields in the quiz');
      // }
    }
    
    // Scroll to the first invalid question
    if (topic.quiz.invalidQuestions && topic.quiz.invalidQuestions.length > 0) {
      this.scrollToInvalidQuestion(topic, topic.quiz.invalidQuestions[0]);
    } else {
      topic.validate = true;
      topic.active = !topic.active;
      this._messageService.success('Quiz saved successfully!');
    }
  }
}
  saveVideo(topic?: any) {
    topic.completed = false;
    topic.validate = false;
    topic._touched = true; // Mark as touched when user tries to save
    this.videoValidation(topic);
    if (topic.validate) {
      this.checkPreviousTopic(topic);
      topic.completed = true;
      topic.topicStatusImg = this.topicStatusCompleteImg;
      topic.active = !topic.active;
      this._messageService.success('Video content saved successfully!');
    } else {
      this._messageService.error('Please complete all required fields for video content');
      this.scrollToTopic(topic);
    }
  }

  saveArticle(topic?: any) {
    topic.completed = false;
    topic.validate = false;
    topic._touched = true; // Mark as touched when user tries to save
    this.articleValidation(topic);
    if (topic.validate) {
      this.checkPreviousTopic(topic);
      topic.completed = true;
      topic.topicStatusImg = this.topicStatusCompleteImg;
      topic.active = !topic.active;
      this._messageService.success('Article saved successfully!');
    } else {
      this._messageService.error('Please complete all required fields for the article');
      this.scrollToTopic(topic);
    }
  }

  private scrollToTopic(topic: any): void {
    this.timeout = setTimeout(() => {
      // Try to find the topic element
      const topicElement = document.querySelector(`[ng-reflect-model="${topic.name}"]`)?.closest('.topic-collapse-panel') ||
        document.querySelector('.topic-collapse-panel');

      if (topicElement) {
        topicElement.scrollIntoView({
          behavior: 'smooth',
          block: 'center'
        });

        // Expand the topic if it's collapsed
        topic.active = true;
      }
    }, 100);
  }

  private scrollToTopicTop(topic: any) {
    this.timeout = setTimeout(() => {
      // Try to find the topic element
      const topicElement = document.querySelector(`[ng-reflect-model="${topic.name}"]`);

      if (topicElement) {
        topicElement.scrollIntoView({
          behavior: 'smooth',
          block: 'center'
        });

        // Expand the topic if it's collapsed
        topic.active = true;
      }
    }, 100);
  }

  // Helper method to check if a section is valid
  isSectionValid(section: any): boolean {
    if (!section.name || section.name.trim() === '') {
      return false;
    }

    if (section.topics.length === 0) {
      return false;
    }

    return section.topics.every((topic: any) =>
      !topic.delete && topic.name && topic.name.trim() !== '' && topic.validate
    );
  }

  // Helper method to check if a topic is valid
  isTopicValid(topic: any): boolean {
    return topic.name && topic.name.trim() !== '' && topic.validate;
  }

  private scrollToInvalidQuestion(topic: any, questionIndex: number): void {
    setTimeout(() => {
      // Ensure the topic is expanded
      topic.active = true;

      // Wait for DOM update then scroll
      setTimeout(() => {
        // Find the question element by its index
        const questionElement = document.querySelector(
          `[data-topic-id="${topic.topicId}"][data-question-index="${questionIndex}"]`
        ) ||
          document.querySelector(
            `.question-outer-container:nth-child(${questionIndex + 1})`
          ) ||
          document.querySelector('.question-outer-container');

        if (questionElement) {
          questionElement.scrollIntoView({
            behavior: 'smooth',
            block: 'center'
          });

          // Add highlight effect
          questionElement.classList.add('highlight-invalid');
          setTimeout(() => {
            questionElement.classList.remove('highlight-invalid');
          }, 3000);

          // Focus on the question input if it's empty
          const questionInput = questionElement.querySelector('input[ng-reflect-model]') as HTMLInputElement;
          if (questionInput) {
            questionInput.focus();
          }
        } else {
          // Fallback: scroll to topic
          this.scrollToTopic(topic);
        }
      }, 300);
    }, 100);
  }

  previewReport(topic: any): void {
    if(!topic.quiz.durationInMinutes) {
      this._messageService.error("Please enter quiz duration");
      this.scrollToTopicTop(topic);
      return;
    }
    if (topic.quiz?.generateAIReport) {
      const activeQuestions = topic.quiz.questions.filter((q: any) => !q.delete);

      this._courseService.previewAIReport({
        quizQuestions: activeQuestions,
        reportPrompt: topic.quiz.reportPrompt,
        durationInMinutes: topic.quiz.durationInMinutes,
        timeZone: this.timeZone,
        topicTitle: topic.name,
      })
        .subscribe({
          next: (response: any) => {
            this.showReportPreview(response, topic);
          },
          error: (error: any) => {
            console.error('Error generating report:', error);
            this._messageService.error('Failed to generate report preview');

          }
        });
    } else {
      this._messageService.error('Please enter a report prompt');
    }
  }

  showReportPreview(reportContent: string, topic: any): void {
    const modal = this._modal.create({
      nzContent: ReportPreviewModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        reportContent: reportContent,
        quizTitle: topic.quiz.title
      },
      nzFooter: null,
      nzWidth: '80%',
      nzStyle: { top: '20px' }
    });
  }

  toggleAIReport(topic: any): void {
    if (topic.quiz) {

      if (!topic.quiz.generateAIReport) {
        topic.quiz.reportPrompt = '';
      }
      this.quizValidation(topic);
    }
  }

  onReportPromptChange(topic: any, event: any): void {
    if (topic.quiz) {
      topic.quiz.reportPrompt = event.target.value;
      this.quizValidation(topic);
    }
  }


  isAllowedToGenerateAIReport(topic: any): boolean {
    const isValid = topic.quiz.questions
      .filter((q: any) => !q.delete)
      .some((q: any) => q.ques && q.ques.trim() !== '');
    return isValid;
  }
}