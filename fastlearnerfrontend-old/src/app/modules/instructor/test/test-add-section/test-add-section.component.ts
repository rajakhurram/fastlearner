import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  Output,
  ViewContainerRef,
} from '@angular/core';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { InstructorService } from 'src/app/core/services/instructor.service';

import {
  CdkDragDrop,
  CdkDragEnter,
  moveItemInArray,
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

@Component({
  selector: 'app-test-add-section',
  templateUrl: './test-add-section.component.html',
  styleUrls: ['./test-add-section.component.scss'],
})
export class TestAddSectionComponent {
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
    // { key: this.questionType.TEXT_FIELD, value: 'Text field' },
  ];

  alphabet: string[] = Array.from({ length: 26 }, (_, i) =>
    String.fromCharCode(65 + i)
  );

  constructor(
    private _instructorService: InstructorService,
    private _messageService: MessageService,
    private _fileManagerService: FileManager,
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef,
    private communicationService: CommunicationService,
    private msg: NzMessageService,
    private _courseService: CourseService,
    private _authService: AuthService
  ) {}

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.screenWidth = event.target.innerWidth;
  }

  ngOnDestroy() {
    this.saveAsDraftCourse();
  }

  @HostListener('window:beforeunload', ['$event'])
  handleBeforeUnload(event: Event): void {
    event.preventDefault();
  }

  ngOnInit(): void {
    if (this.sectionsData?.length > 0) {
      this.sections = this.sectionsData;
      this.hideDeleteSectionContainer();
    } else if (this.courseId) {
      this.patchSectionData();
    } else {
      this.addSection();
      // this.createTopics(this.sections[0]);
    }

    this.communicationService.documentSummary$?.subscribe(
      (document: any) => {}
    );

    this.communicationService.videoSummary$?.subscribe((videoData: any) => {});

    this.communicationService.articleSummary$?.subscribe((article: any) => {});

    this.communicationService.videoTranscript$?.subscribe(
      (videoData: any) => {}
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
              questions: [
                {
                  questionId: '',
                  delete: false,
                  label: 'Question ' + 1 + ' ',
                  questionType: this.questionTypes[0],
                  ques: '',
                  explanation: '',
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
              quiz.passingCriteria = topic.durationInMinutes;
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
                      isCorrectAnswer: answer?.isCorrect,
                    });
                  });

                  let question = {
                    questionId: value.questionId,
                    delete: false,
                    label: 'Question ' + (index + 1),
                    ques: value.questionText,
                    explanation: value?.explanation,
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
    this.showSectionDltContainer = true;
  }

  createTopics(section?: any) {
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
      active: false, // default false
      name: '', // default empty
      disabled: false,
      checkTopic: false,
      topicContainer: true, //default false
      contentScreen: false, //default false
      videoSection: false, //default false
      articleSection: false, //default false
      quizSection: false, //default false
      selectedContentType: '', //default empty
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
        questions: [
          {
            questionId: '',
            delete: false,
            label: 'Question ' + 1 + ' ',
            ques: '',
            explanation: '',
            questionType: this.questionTypes[0],
            answerOrder: this.alphabet[0],
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
      questionType: this.questionTypes[0],
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
    this.maintainQuizQuestionAnswersOrder(
      questions[questions?.length - 1]?.answers
    );
    this.quizValidation(topic);
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

  onQuizInputChange(topic?: any) {
    this.quizValidation(topic);
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

  saveQuiz(topic?: any) {
    topic.completed = false;
    topic.validate = false;
    this.quizValidation(topic);
    if (topic.validate) {
      this.checkPreviousTopic(topic);
      topic.completed = true;
      topic.validate = true;
      topic.topicStatusImg = this.topicStatusCompleteImg;
      topic.active = !topic.active;
    }
  }

  saveVideo(topic?: any) {
    topic.completed = false;
    topic.validate = false;
    this.videoValidation(topic);
    if (topic.validate) {
      this.checkPreviousTopic(topic);
      topic.completed = true;
      topic.topicStatusImg = this.topicStatusCompleteImg;
      topic.active = !topic.active;
    }
  }

  saveArticle(topic?: any) {
    topic.completed = false;
    topic.validate = false;
    this.articleValidation(topic);
    if (topic.validate) {
      this.checkPreviousTopic(topic);
      topic.completed = true;
      topic.topicStatusImg = this.topicStatusCompleteImg;
      topic.active = !topic.active;
    }
  }

  quizValidation(topic?: any) {
    let questionIteration = 0;
    topic.quiz?.questions.forEach((question: any) => {
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

    let outerLopop = true;
    const questions = topic.quiz?.questions.filter(
      (question: any) => question.delete == false
    );
    if (questions?.length > 0 && topic.quiz.title != '') {
      for (let i = 0; i < questions.length && outerLopop == true; i++) {
        const answers = questions[i].answers.filter(
          (answer: any) => answer.delete == false
        );
        if (
          questions[i].ques != '' &&
          questions[i]?.answers.filter((answer: any) => answer.isCorrectAnswer)
            ?.length > 0 &&
          answers.length > 1
        ) {
          for (let j = 0; j < answers.length; j++) {
            if (answers[j].ans != '' && !answers[j].exist) {
              topic.validate = true;
            } else {
              topic.validate = false;
              topic.topicStatusImg = this.topicStatusIncompleteImg;
              outerLopop = false;
              break;
            }
          }
        } else {
          topic.validate = false;
          topic.topicStatusImg = this.topicStatusIncompleteImg;
          outerLopop = false;
          break;
        }
      }
    } else {
      topic.validate = false;
      topic.topicStatusImg = this.topicStatusIncompleteImg;
    }
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
    if (this.sectionValidation()) {
      this.publishCourse(step);
    } else {
      this._messageService.error('Please complete the sections');
    }
  }

  backToPreviousStep(step?: any) {
    this.currentStep.emit(step);
    this.sectionsDataOutPut.emit(this.sections);
  }

  sectionValidation() {
    let sectionValid = true;
    if (
      this.sections?.length != 0 &&
      this.sections?.filter((section: any) => !section.delete)?.length > 0
    ) {
      this.sections.forEach((section: any) => {
        if (!section.delete) {
          if (!section.deleteAll) {
            if (section.topics.length != 0) {
              section.topics.forEach((topic: any) => {
                if (!topic.delete && !topic.validate) {
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
    const interval$ = interval(4000)?.subscribe(() => {});
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
      error: (error: any) => {},
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
            .catch((error) => {});
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
    if (question?.questionType?.key == this.questionType.TRUE_FALSE) {
      this.addOption(topic, question);
    }
    this.quizValidation(topic);
  }

  removeQuizQuestion(topic?: any, question?: any) {
    question.delete = true;
    question.answers = [];
    // question.correctAnswer.ans = null;
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
    });
  }

  hideDeleteSectionContainer() {
    const sections = this.sections.filter(
      (section: any) => section.checkSection === false
    );
    if (sections == 0) {
      this.showDltSectionBtn = false;
      this.checkAllSection = false;
      this.showSectionDltContainer = false;
    }
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
    modal.componentInstance.cancelClick?.subscribe(() => {});

    modal.componentInstance.deleteClick?.subscribe(() => {});
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
          false
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
          error: (error: any) => {},
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
          false
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
              this.currentStep.emit(step);
              this.sectionsDataOutPut.emit(this.sections);
            } else {
              this.courseSaved = false;
            }
          },
          error: (error: any) => {},
        });
    } else if (this.courseInformationData.get('courseProgress').value == 100) {
      this.currentStep.emit(step);
      this.sectionsDataOutPut.emit(this.sections);
    }
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
}
