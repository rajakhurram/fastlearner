import { CommonModule } from '@angular/common';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
  flush,
} from '@angular/core/testing';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { of, Subject, throwError } from 'rxjs';
import { CourseContentType } from 'src/app/core/enums/course-content-type.enum';
import { CourseType } from 'src/app/core/enums/course-status';
import { QuestionType } from 'src/app/core/enums/question-type';
import { QuizType } from 'src/app/core/enums/quiz-type';
import { AddSectionComponent } from './add-section.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { CourseService } from 'src/app/core/services/course.service';
import { FileManager } from 'src/app/core/services/file-manager.service';
import { InstructorService } from 'src/app/core/services/instructor.service';
import { MessageService } from 'src/app/core/services/message.service';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';

describe('AddSectionComponent', () => {
  let component: AddSectionComponent;
  let fixture: ComponentFixture<AddSectionComponent>;
  let fb: FormBuilder;
  let courseServiceSpy: jasmine.SpyObj<CourseService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let communicationServiceSpy: jasmine.SpyObj<CommunicationService>;
  let fileManagerSpy: jasmine.SpyObj<FileManager>;
  let modalSpy: jasmine.SpyObj<NzModalService>;
  let instructorServiceSpy: jasmine.SpyObj<InstructorService>;

  const successCode = 200;

  const createCourseInformationForm = (
    progress: number = 0,
    courseType: string = 'FREE',
  ): FormGroup =>
    fb.group({
      courseProgress: [progress],
      courseType: [courseType],
    });

  const createValidSections = () => [
    {
      sectionId: '',
      delete: false,
      deleteAll: false,
      name: 'Section 1',
      topics: [
        {
          topicId: '',
          name: 'Topic 1',
          delete: false,
          validate: true,
          quiz: {
            type: 'TEST',
            questions: [],
          },
        },
      ],
    },
  ];

  function createValidQuizTopic(): any {
    return {
      validate: false,
      topicStatusImg: '',
      quiz: {
        title: 'Chapter Quiz',
        durationInMinutes: 10,
        passingCriteria: 50,
        type: QuizType.BASIC_QUIZ,
        invalidQuestions: [],
        validationMessages: [],
        questions: [
          {
            delete: false,
            ques: 'What is 2+2?',
            questionType: { key: QuestionType.MULTIPLE_CHOICE },
            answers: [
              {
                delete: false,
                ans: '4',
                isCorrectAnswer: true,
                exist: false,
              },
              {
                delete: false,
                ans: '5',
                isCorrectAnswer: false,
                exist: false,
              },
            ],
          },
        ],
      },
    };
  }

  beforeEach(async () => {
    courseServiceSpy = jasmine.createSpyObj<CourseService>('CourseService', [
      'createCourseDto',
      'getSectionByCourseId',
      'getTopicsBySectionId',
      'previewAIReport',
      'youtubeVideoUrlUpload',
    ]);
    messageServiceSpy = jasmine.createSpyObj<MessageService>('MessageService', [
      'error',
      'success',
      'loading',
    ]);
    communicationServiceSpy = jasmine.createSpyObj<CommunicationService>(
      'CommunicationService',
      ['updateInstructorCourse'],
      {
        documentSummary$: of(null),
        videoSummary$: of(null),
        articleSummary$: of(null),
        videoTranscript$: of(null),
      },
    );

    instructorServiceSpy = jasmine.createSpyObj<InstructorService>(
      'InstructorService',
      ['generator', 'getTopicTypes'],
    );
    fileManagerSpy = jasmine.createSpyObj<FileManager>('FileManager', [
      'uploadFile',
      'deleteFile',
    ]);
    modalSpy = jasmine.createSpyObj<NzModalService>('NzModalService', [
      'create',
    ]);
    const nzMessageSpy = jasmine.createSpyObj<NzMessageService>(
      'NzMessageService',
      ['success', 'error'],
    );
    const authSpy = jasmine.createSpyObj<AuthService>('AuthService', [
      'getLoggedInName',
    ]);

    await TestBed.configureTestingModule({
      declarations: [AddSectionComponent],
      imports: [ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: InstructorService, useValue: instructorServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: FileManager, useValue: fileManagerSpy },
        { provide: NzModalService, useValue: modalSpy },
        { provide: CommunicationService, useValue: communicationServiceSpy },
        { provide: NzMessageService, useValue: nzMessageSpy },
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: AuthService, useValue: authSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    })
      .overrideComponent(AddSectionComponent, {
        set: { template: '' },
      })
      .compileComponents();

    fb = TestBed.inject(FormBuilder);
    fixture = TestBed.createComponent(AddSectionComponent);
    component = fixture.componentInstance;
    component.sections = [];
    component.sectionsData = [];
    component.courseId = null;
    component.selectedContentType = null;
    component.courseInformationData = createCourseInformationForm(0);
    fileManagerSpy.uploadFile.and.returnValue(of({ data: 'https://cdn/img.png' }));
    fileManagerSpy.deleteFile.and.returnValue(of(null));
    instructorServiceSpy.generator.and.returnValue(
      of({ status: successCode, data: 'Topic A\nTopic B' }),
    );
    modalSpy.create.and.returnValue({
      afterClose: of(null),
      componentInstance: { deleteClick: new Subject<void>() },
    } as any);
  });

  it('should create and initialize with form input', () => {
    component.ngOnInit();
    expect(component).toBeTruthy();
    expect(component.courseInformationData.get('courseProgress')?.value).toBe(0);
    expect(component.isPublished).toBeFalse();
  });

  it('should mark isPublished true when progress is 100', () => {
    component.courseInformationData = createCourseInformationForm(100);
    component.ngOnInit();
    expect(component.isPublished).toBeTrue();
  });

  it('should call addSection on init when no courseId and no sectionsData', () => {
    const addSectionSpy = spyOn(component, 'addSection');
    component.sectionsData = [];
    component.courseId = null;

    component.ngOnInit();

    expect(addSectionSpy).toHaveBeenCalled();
  });

  it('should call patchSectionData on init when courseId exists', () => {
    const patchSpy = spyOn(component, 'patchSectionData');
    component.courseId = 'course-1';
    component.sectionsData = [];

    component.ngOnInit();

    expect(patchSpy).toHaveBeenCalled();
  });

  it('should set sections from provided sectionsData on init', () => {
    const existingSections = createValidSections();
    component.sectionsData = existingSections;

    component.ngOnInit();

    expect(component.sections).toEqual(existingSections);
  });

  it('should add section with incremental level', () => {
    component.sections = [{ level: 1 }, { level: 2 }] as any;

    component.addSection();

    expect(component.sections.length).toBe(3);
    expect(component.sections[2].level).toBe(3);
  });

  it('should return true for valid section data', () => {
    component.sections = createValidSections();
    expect(component.sectionValidation()).toBeTrue();
  });

  it('should return false for invalid section data (empty values)', () => {
    component.sections = [
      {
        delete: false,
        deleteAll: false,
        name: '',
        topics: [{ name: '', delete: false, validate: false, quiz: { type: 'TEST' } }],
      },
    ];
    expect(component.sectionValidation()).toBeFalse();
  });

  it('should return false for invalid section data (null values)', () => {
    component.sections = [
      {
        delete: false,
        deleteAll: false,
        name: null,
        topics: [{ name: null, delete: false, validate: false, quiz: { type: 'TEST' } }],
      },
    ];
    expect(component.sectionValidation()).toBeFalse();
  });

  it('should call publishCourse when steps called with valid data', () => {
    spyOn(component, 'sectionValidation').and.returnValue(true);
    const publishSpy = spyOn(component, 'publishCourse');

    component.steps('next-step');

    expect(publishSpy).toHaveBeenCalledWith('next-step');
    expect(messageServiceSpy.error).not.toHaveBeenCalled();
  });

  it('should not call publishCourse when steps called with invalid data', () => {
    spyOn(component, 'sectionValidation').and.returnValue(false);
    const publishSpy = spyOn(component, 'publishCourse');

    component.steps('next-step');

    expect(publishSpy).not.toHaveBeenCalled();
    expect(messageServiceSpy.error).toHaveBeenCalledWith(
      'Please complete the sections',
    );
  });

  it('should call API and handle successful publish', () => {
    component.sections = createValidSections();
    component.courseSaved = false;
    component.courseId = null;
    component.courseInformationData = createCourseInformationForm(0);

    courseServiceSpy.createCourseDto.and.returnValue(
      of({
        status: successCode,
        data: { courseId: 'new-course-id' },
      }),
    );

    const patchSpy = spyOn(component, 'patchSectionData').and.callFake(
      (afterLoaded?: () => void) => afterLoaded?.(),
    );
    const assignSpy = spyOn(component, 'assignSurveyAnswersToAnswers');
    const stepEmitSpy = spyOn(component.currentStep, 'emit');
    const sectionEmitSpy = spyOn(component.sectionsDataOutPut, 'emit');

    component.publishCourse('next-step');

    expect(courseServiceSpy.createCourseDto).toHaveBeenCalled();
    expect(component.courseId).toBe('new-course-id');
    expect(communicationServiceSpy.updateInstructorCourse).toHaveBeenCalled();
    expect(patchSpy).toHaveBeenCalled();
    expect(assignSpy).toHaveBeenCalled();
    expect(stepEmitSpy).toHaveBeenCalledWith('next-step');
    expect(sectionEmitSpy).toHaveBeenCalled();
  });

  it('should call API publish even when courseSaved is already true', () => {
    component.courseSaved = true;
    component.sections = createValidSections();
    component.courseInformationData = createCourseInformationForm(0);
    courseServiceSpy.createCourseDto.and.returnValue(
      of({
        status: successCode,
        data: { courseId: 'existing-course-id' },
      }),
    );
    spyOn(component, 'patchSectionData');

    component.publishCourse('next-step');

    expect(courseServiceSpy.createCourseDto).toHaveBeenCalled();
  });

  it('should call API for saveAsDraftCourse and handle success', () => {
    component.courseSaved = false;
    component.sections = createValidSections();
    component.courseId = null;
    component.courseInformationData = createCourseInformationForm(0);

    courseServiceSpy.createCourseDto.and.returnValue(
      of({
        status: successCode,
        data: { courseId: 'draft-course-id' },
      }),
    );

    component.saveAsDraftCourse();

    expect(courseServiceSpy.createCourseDto).toHaveBeenCalled();
    expect(component.courseId).toBe('draft-course-id');
    expect(communicationServiceSpy.updateInstructorCourse).toHaveBeenCalled();
  });

  it('should not call draft API when course is already published', () => {
    component.courseInformationData = createCourseInformationForm(100);
    component.sections = createValidSections();
    component.courseSaved = false;

    component.saveAsDraftCourse();

    expect(courseServiceSpy.createCourseDto).not.toHaveBeenCalled();
  });

  it('should handle draft API error without throwing', () => {
    component.courseSaved = false;
    component.sections = createValidSections();
    component.courseInformationData = createCourseInformationForm(0);
    courseServiceSpy.createCourseDto.and.returnValue(
      throwError(() => new Error('Draft save failed')),
    );

    expect(() => component.saveAsDraftCourse()).not.toThrow();
    expect(courseServiceSpy.createCourseDto).toHaveBeenCalled();
  });

  it('should call uploadSectionData when patchSectionData API succeeds', () => {
    component.courseId = 'course-xyz';
    const uploadSpy = spyOn(component, 'uploadSectionData');
    courseServiceSpy.getSectionByCourseId.and.returnValue(
      of({
        status: successCode,
        data: [{ sectionId: 'sec-1', sectionName: 'Section 1', free: true }],
      }),
    );

    component.patchSectionData();

    expect(courseServiceSpy.getSectionByCourseId).toHaveBeenCalledWith(
      'course-xyz',
    );
    expect(uploadSpy).toHaveBeenCalled();
  });

  it('should call addSection when patchSectionData API fails', () => {
    component.courseId = 'course-xyz';
    const addSpy = spyOn(component, 'addSection');
    courseServiceSpy.getSectionByCourseId.and.returnValue(
      throwError(() => new Error('Fetch failed')),
    );

    component.patchSectionData();

    expect(addSpy).toHaveBeenCalled();
  });

  it('should format time as mm:ss', () => {
    expect(component.formatTime(125)).toBe('2:05');
    expect(component.formatTime(0)).toBe('0:00');
  });

  it('should extract filename from url', () => {
    expect(component.getFilenameFromUrl('https://cdn.test.com/path/file.png')).toBe(
      'file.png',
    );
  });

  it('should detect image and audio file types', () => {
    expect(component.isImage('https://cdn.test.com/a.jpg')).toBeTrue();
    expect(component.isImage('https://cdn.test.com/a.pdf')).toBeFalse();
    expect(component.isAudio('https://cdn.test.com/a.mp3')).toBeTrue();
    expect(component.isAudio('https://cdn.test.com/a.jpg')).toBeFalse();
  });

  it('should detect video source from url or youtube link', () => {
    expect(
      component.hasVideoSource({
        video: { videoData: { videoUrl: 'https://video.mp4', youtubeVideoUrl: '' } },
      }),
    ).toBeTrue();
    expect(
      component.hasVideoSource({
        video: { videoData: { videoUrl: '', youtubeVideoUrl: 'https://youtu.be/x' } },
      }),
    ).toBeTrue();
    expect(
      component.hasVideoSource({
        video: { videoData: { videoUrl: '', youtubeVideoUrl: '' } },
      }),
    ).toBeFalse();
  });

  it('should validate article topic with content', () => {
    const topic = {
      validate: false,
      topicStatusImg: component.topicStatusIncompleteImg,
      article: { content: 'Body', articleProgressBar: false },
    };
    component.articleValidation(topic);
    expect(topic.validate).toBeTrue();
  });

  it('should invalidate article topic without content', () => {
    const topic = {
      validate: true,
      topicStatusImg: component.topicStatusCompleteImg,
      article: { content: '', articleProgressBar: false },
    };
    component.articleValidation(topic);
    expect(topic.validate).toBeFalse();
  });

  it('should validate video topic when source and uploads are complete', () => {
    const topic = {
      validate: false,
      topicStatusImg: component.topicStatusIncompleteImg,
      video: {
        videoData: { videoUrl: 'https://v.mp4', videoProgress: 100 },
        documentData: { documents: [] },
      },
    };
    component.videoValidation(topic);
    expect(topic.validate).toBeTrue();
  });

  it('should toggle bulk quiz instructions modal', () => {
    component.openBulkQuizInstructionsModal();
    expect(component.bulkQuizInstructionsVisible).toBeTrue();
    component.closeBulkQuizInstructionsModal();
    expect(component.bulkQuizInstructionsVisible).toBeFalse();
  });

  it('should emit outputs on backToPreviousStep', () => {
    const stepSpy = spyOn(component.currentStep, 'emit');
    const sectionSpy = spyOn(component.sectionsDataOutPut, 'emit');
    component.sections = createValidSections();
    component.backToPreviousStep('prev');
    expect(stepSpy).toHaveBeenCalledWith('prev');
    expect(sectionSpy).toHaveBeenCalledWith(component.sections);
  });

  it('should show error when removing last quiz question', () => {
    const topic = {
      quiz: {
        randomQuestion: 1,
        questions: [{ delete: false, answers: [] }],
      },
    };
    const question = topic.quiz.questions[0];
    component.removeAudioQuestion(topic, question);
    expect(messageServiceSpy.error).toHaveBeenCalledWith(
      'At least one question is required in the quiz.',
    );
  });

  it('should bind imported answerImageUrl to attachedImageUrl for image options', () => {
    const mapped = (component as any).mapApiQuestionToUiQuestion(
      {
        questionType: 'SINGLE_CHOICE',
        questionText: 'Identify image option',
        questionImageUrl: '',
        answers: [
          {
            answerText: '',
            answerImageUrl: 'https://cdn.example.com/img2.jpg',
            isCorrectAnswer: true,
            answerOrder: 'A',
          },
        ],
      },
      0,
    );

    expect(mapped.answers[0].ans).toBe('');
    expect(mapped.answers[0].attachedImageUrl).toBe(
      'https://cdn.example.com/img2.jpg',
    );
    expect(mapped.answers[0].isCorrectAnswer).toBeTrue();
    expect(mapped._clientKey).toBeTruthy();
    expect(mapped.answers[0]._clientKey).toBeTruthy();
  });

  it('should track quiz questions and answers by stable ids', () => {
    expect(component.trackByQuizQuestion(0, { questionId: 5 })).toBe('qid-5');
    expect(component.trackByQuizQuestion(1, { _clientKey: 'ck-1' })).toBe('ck-1');
    expect(component.trackByQuizAnswer(0, { answerId: 9 })).toBe('aid-9');
    expect(component.trackByQuizAnswer(2, { _clientKey: 'ak-1' })).toBe('ak-1');
  });

  it('should paginate quiz questions via load more', () => {
    const topic = {
      quiz: { questions: Array.from({ length: 8 }, () => ({ delete: false })) },
    };

    expect(component.shouldRenderQuizQuestion(topic, 4)).toBeTrue();
    expect(component.shouldRenderQuizQuestion(topic, 5)).toBeFalse();
    expect(component.hasMoreQuizQuestionsToLoad(topic)).toBeTrue();
    expect(component.getRemainingQuizQuestionsCount(topic)).toBe(3);

    component.onLoadMoreQuizQuestions(topic);

    expect(component.shouldRenderQuizQuestion(topic, 5)).toBeTrue();
    expect(component.hasMoreQuizQuestionsToLoad(topic)).toBeFalse();
  });

  it('should reorder sections and update levels on drop', () => {
    component.sections = [
      { name: 'A', delete: false },
      { name: 'B', delete: false },
    ] as any;

    component.dropSection({ previousIndex: 0, currentIndex: 1 } as any);

    expect(component.sections[0].name).toBe('B');
    expect(component.sections[0].level).toBe(1);
    expect(component.sections[1].level).toBe(2);
  });

  it('should reorder topics on drop', () => {
    const topics = [{ name: 'T1' }, { name: 'T2' }];

    component.dropTopic({ previousIndex: 0, currentIndex: 1 } as any, topics);

    expect(topics[0].name).toBe('T2');
    expect(topics[1].name).toBe('T1');
  });

  it('should stop collapse event propagation', () => {
    const event = jasmine.createSpyObj<Event>('Event', ['stopPropagation']);

    component.stopCollapse(event);

    expect(event.stopPropagation).toHaveBeenCalled();
  });

  it('should validate survey topic with complete questions and answers', () => {
    const topic = {
      validate: false,
      topicStatusImg: component.topicStatusIncompleteImg,
      quiz: {
        type: component.quizType.SURVEY,
        questions: [
          {
            delete: false,
            ques: 'How was the course?',
            surveyAnswers: [{ answer: 'Great' }, { answer: 'Good' }],
          },
        ],
      },
    };

    component.validateSurvey(topic);

    expect(topic.validate).toBeTrue();
    expect(topic.topicStatusImg).toBe(component.topicStatusCompleteImg);
  });

  it('should reject empty survey answers', () => {
    const topic = {
      validate: true,
      topicStatusImg: component.topicStatusCompleteImg,
      quiz: {
        type: component.quizType.SURVEY,
        questions: [
          {
            delete: false,
            ques: 'Rate us',
            surveyAnswers: [{ answer: '' }],
          },
        ],
      },
    };

    expect(() => component.validateSurvey(topic)).toThrow();
    expect(topic.validate).toBeFalse();
    expect(messageServiceSpy.error).toHaveBeenCalled();
  });

  it('should add a new quiz question to topic', () => {
    const topic = {
      quiz: {
        type: 'TEST',
        randomQuestionType: null,
        questions: [],
        title: 'Quiz',
        durationInMinutes: 10,
        passingCriteria: 50,
      },
    };

    component.addQuestion(topic, topic.quiz.questions);

    expect(topic.quiz.questions.length).toBe(1);
    expect(topic.quiz.questions[0]._clientKey).toBeTruthy();
  });

  it('should update audio progress and duration on question', () => {
    const question: any = {};
    const audio = {
      currentTime: 65,
      duration: 125,
    } as HTMLAudioElement;

    component.updateProgress(audio, question);
    component.setDuration(audio, question);

    expect(question.currentTime).toBe('1:05');
    expect(question.duration).toBe('2:05');
    expect(question.progress).toBeCloseTo(52, 0);
  });

  it('should reset question state when audio ends', () => {
    const question = { isPlaying: true, progress: 80 };

    component.audioEnded(question);

    expect(question.isPlaying).toBeFalse();
    expect(question.progress).toBe(0);
  });

  it('should map survey answers for API payload', () => {
    const mapped = component.mapSurveyAnswers([
      { answerId: 1, answerText: 'Yes', delete: false },
      { answerId: 2, answerText: 'No', delete: true },
    ]);

    expect(mapped.length).toBe(2);
    expect(mapped[0].answer).toBe('Yes');
    expect(mapped[0].count).toBe(1);
    expect(mapped[1].count).toBe(2);
  });

  it('should detect in-process file uploads on topic', () => {
    const completeTopic: any = {
      video: {
        videoData: { videoFileType: '', videoProgress: 100 },
        documentData: { documents: [{ documentProgress: 100 }] },
      },
    };
    const processingTopic: any = {
      video: {
        videoData: { videoFileType: 'mp4', videoProgress: 50 },
        documentData: { documents: [{ documentProgress: 100 }] },
      },
    };

    component.checkFileInProcess(completeTopic);
    component.checkFileInProcess(processingTopic);

    expect(completeTopic.video.fileProcessing).toBeTrue();
    expect(processingTopic.video.fileProcessing).toBeFalse();
  });

  it('should update screen width on resize', () => {
    component.onResize({ target: { innerWidth: 1024 } });

    expect(component.screenWidth).toBe(1024);
  });

  describe('quiz validation and options', () => {
    it('should validate a complete multiple-choice quiz', () => {
      const topic = createValidQuizTopic();

      component.quizValidation(topic);

      expect(topic.validate).toBeTrue();
      expect(topic.quiz.invalidQuestions.length).toBe(0);
    });

    it('should flag missing quiz duration', () => {
      const topic = createValidQuizTopic();
      topic.quiz.durationInMinutes = null;

      component.quizValidation(topic);

      expect(topic.validate).toBeFalse();
      expect(topic.quiz.validationError).toBe('Quiz duration is required');
    });

    it('should flag missing passing criteria', () => {
      const topic = createValidQuizTopic();
      topic.quiz.passingCriteria = null;

      component.quizValidation(topic);

      expect(topic.validate).toBeFalse();
      expect(topic.quiz.validationError).toBe('Passing criteria is required');
    });

    it('should flag empty question text', () => {
      const topic = createValidQuizTopic();
      topic.quiz.questions[0].ques = '   ';

      component.quizValidation(topic);

      expect(topic.validate).toBeFalse();
      expect(topic.quiz.invalidQuestions).toContain(0);
    });

    it('should flag single-choice without exactly one correct answer', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.questionType = { key: QuestionType.SINGLE_CHOICE };
      question.answers[0].isCorrectAnswer = false;
      question.answers[1].isCorrectAnswer = false;

      component.quizValidation(topic);

      expect(topic.validate).toBeFalse();
      expect(topic.quiz.invalidQuestions).toContain(0);
    });

    it('should detect duplicate quiz options', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.answers[1].ans = '4';

      component.quizOptions(question, 1);

      expect(question.answers[1].exist).toBeTrue();
      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Option already exist in this question',
      );
    });

    it('should add true/false options when adding option', () => {
      const topic = createValidQuizTopic();
      const question = {
        questionType: { key: QuestionType.TRUE_FALSE },
        answers: [],
      };

      component.addOption(topic, question);

      expect(question.answers.length).toBe(2);
      expect(question.answers[0].ans).toBe('True');
      expect(question.answers[1].ans).toBe('False');
    });

    it('should add a single text-field answer option', () => {
      const topic = createValidQuizTopic();
      const question = {
        questionType: { key: QuestionType.TEXT_FIELD },
        answers: [],
      };

      component.addOption(topic, question);

      expect(question.answers.length).toBe(1);
    });

    it('should maintain answer order labels', () => {
      const answers: any[] = [
        { delete: false },
        { delete: true },
        { delete: false },
      ];

      component.maintainQuizQuestionAnswersOrder(answers);

      expect(answers[0].answerOrder).toBe('A');
      expect(answers[2].answerOrder).toBe('B');
    });

    it('should clear invalid question index on quiz input change', () => {
      const topic = createValidQuizTopic();
      topic.quiz.invalidQuestions = [0];
      const question = topic.quiz.questions[0];
      question.ques = 'Updated question';

      component.onQuizInputChange(topic, question);

      expect(topic.quiz.invalidQuestions).not.toContain(0);
    });

    it('should remove quiz question when more than one exists', () => {
      const topic: any = createValidQuizTopic();
      topic.quiz.randomQuestion = 2;
      topic.quiz.questions.push({
        delete: false,
        ques: 'Second',
        questionType: { key: QuestionType.MULTIPLE_CHOICE },
        answers: [],
      });

      component.removeAudioQuestion(topic, topic.quiz.questions[0]);

      expect(topic.quiz.questions[0].delete).toBeTrue();
      expect(messageServiceSpy.error).not.toHaveBeenCalled();
    });
  });

  describe('content type and topic management', () => {
    it('should open topic container flags', () => {
      const topic: any = { topicContainer: false, contentScreen: false };

      component.openTopicContainer(topic);

      expect(topic.topicContainer).toBeTrue();
      expect(topic.contentScreen).toBeTrue();
    });

    it('should open video content and validate', () => {
      const topic: any = {
        contentScreen: true,
        selectedContentType: null,
        topicStatusImg: '',
        validate: false,
        video: {
          videoData: { videoUrl: 'https://v.mp4', videoProgress: 100 },
          documentData: { documents: [] },
        },
        article: { content: '' },
        quiz: createValidQuizTopic().quiz,
      };

      component.openContent(topic, component.typeVideo);

      expect(topic.videoSection).toBeTrue();
      expect(topic.articleSection).toBeFalse();
      expect(topic.quizSection).toBeFalse();
      expect(topic.validate).toBeTrue();
    });

    it('should open quiz content and run validation', () => {
      const topic: any = {
        contentScreen: true,
        selectedContentType: null,
        topicStatusImg: '',
        videoSection: false,
        articleSection: false,
        quizSection: false,
        quiz: createValidQuizTopic().quiz,
      };

      component.openContent(topic, 'Quiz');

      expect(topic.quizSection).toBeTrue();
    });

    it('should set randomQuestion to 1 before save for single-question course quiz', () => {
      component.selectedContentType = CourseContentType.COURSE;
      component.sections = [
        {
          topics: [
            {
              selectedContentType: component.typeQuiz,
              quiz: {
                randomQuestion: null,
                questions: [{ delete: false, ques: 'Only question?' }],
              },
            },
          ],
        },
      ];

      (component as any).syncSingleQuestionQuizRandomCountsBeforeSave();

      expect(component.sections[0].topics[0].quiz.randomQuestion).toBe(1);
    });

    it('should not change randomQuestion for multi-question course quizzes before save', () => {
      component.selectedContentType = CourseContentType.COURSE;
      component.sections = [
        {
          topics: [
            {
              selectedContentType: component.typeQuiz,
              quiz: {
                randomQuestion: null,
                questions: [
                  { delete: false, ques: 'Q1' },
                  { delete: false, ques: 'Q2' },
                ],
              },
            },
          ],
        },
      ];

      (component as any).syncSingleQuestionQuizRandomCountsBeforeSave();

      expect(component.sections[0].topics[0].quiz.randomQuestion).toBeNull();
    });

    it('should skip single-question randomQuestion sync for test content type', () => {
      component.selectedContentType = CourseContentType.TEST;
      component.sections = [
        {
          topics: [
            {
              selectedContentType: component.typeQuiz,
              quiz: {
                randomQuestion: null,
                questions: [{ delete: false, ques: 'Only question?' }],
              },
            },
          ],
        },
      ];

      (component as any).syncSingleQuestionQuizRandomCountsBeforeSave();

      expect(component.sections[0].topics[0].quiz.randomQuestion).toBeNull();
    });

    it('should delegate onSelectContentType to openContent', () => {
      const topic: any = {
        selectedContentType: component.typeArticle,
        topicStatusImg: 'old',
        article: { content: 'text', articleProgressBar: false },
        validate: false,
      };
      spyOn(component, 'openContent');

      component.onSelectContentType(topic);

      expect(topic.topicStatusImg).toBe('');
      expect(component.openContent).toHaveBeenCalledWith(
        topic,
        component.typeArticle,
      );
    });

    it('should create topics when adding section for TEST course', () => {
      component.selectedContentType = CourseContentType.TEST;
      component.sections = [];

      component.addSection();

      expect(component.sections.length).toBe(1);
      expect(component.sections[0].topics.length).toBeGreaterThan(0);
    });

    it('should trim topic input on change', () => {
      const section = { topicInput: '  hello  ' };

      component.topicInputChange(section);

      expect(section.topicInput).toBe('hello');
    });

    it('should show and clear topic generation prompt', () => {
      const section: any = {
        generateTopicBtn1: true,
        generateTopicsPrompt: false,
        generateTopicBtn2: true,
        showChatBox: true,
        questionAnswers: { question: 'old', answers: ['a'] },
      };

      component.showGenerateTopicsPrompt(section);
      expect(section.generateTopicsPrompt).toBeTrue();

      component.clearTopicChat(section);
      expect(section.showChatBox).toBeFalse();
      expect(section.questionAnswers.question).toBe('');
      expect(section.questionAnswers.answers).toEqual([]);
    });

    it('should generate topics from instructor service', () => {
      const section: any = {
        topicInput: 'Algebra basics',
        questionAnswers: { question: '', answers: [] },
        showChatBox: false,
        showSpinner: false,
        generateTopicsPrompt: true,
      };

      component.generateTopics(section);

      expect(instructorServiceSpy.generator).toHaveBeenCalledWith('Algebra basics');
      expect(section.showSpinner).toBeFalse();
      expect(section.questionAnswers.answers.length).toBeGreaterThan(0);
    });
  });

  describe('topic selection and deletion', () => {
    it('should check all topics and show delete icon', () => {
      const section: any = {
        topics: [{ checkTopic: false }, { checkTopic: false }],
        deleteTopicIcon: false,
      };
      const event = { target: { checked: true } };

      component.allTopicCheck(section, event);

      expect(section.topics.every((t: any) => t.checkTopic)).toBeTrue();
      expect(section.deleteTopicIcon).toBeTrue();
    });

    it('should uncheck section select-all when a topic is unchecked', () => {
      const section: any = {
        checkAll: true,
        topics: [{ checkTopic: true }, { checkTopic: false }],
        deleteTopicIcon: false,
      };

      component.singleTopicCheck(section, section.topics[1], {});

      expect(section.checkAll).toBeFalse();
    });

    it('should block deleting all topics', () => {
      const section: any = {
        topics: [{ checkTopic: true, delete: false }],
      };

      component.deleteTopics(section);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'You cannot delete all topics. At least one topic must remain in the section.',
      );
      expect(modalSpy.create).not.toHaveBeenCalled();
    });

    it('should reorder quiz questions on drop', () => {
      const topic = createValidQuizTopic();
      const questions = [
        { id: 1, delete: false, ques: 'Q1', questionType: { key: QuestionType.MULTIPLE_CHOICE }, answers: [] },
        { id: 2, delete: false, ques: 'Q2', questionType: { key: QuestionType.MULTIPLE_CHOICE }, answers: [] },
      ];
      topic.quiz.questions = questions;

      component.dropQuizQuestion(
        { previousIndex: 0, currentIndex: 1 } as any,
        topic,
        questions,
      );

      expect(questions[0].id).toBe(2);
      expect(questions[1].id).toBe(1);
    });

    it('should deactivate section and topic when drag starts', () => {
      const section = { active: true };
      const topic = { active: true };

      component.onDragSectionStarted(section);
      component.onDragTopicStarted(topic);

      expect(section.active).toBeFalse();
      expect(topic.active).toBeFalse();
    });
  });

  describe('media and uploads', () => {
    it('should upload question image successfully', () => {
      const topic = createValidQuizTopic();
      const question: any = { attachedImageUrl: null };
      const file = new File(['img'], 'q.png', { type: 'image/png' });
      const input = document.createElement('input');
      Object.defineProperty(input, 'files', { value: [file] });

      component.onQuestionImageSelected(topic, question, { target: input } as any);

      expect(fileManagerSpy.uploadFile).toHaveBeenCalled();
      expect(question.attachedImageUrl).toBe('https://cdn/img.png');
      expect(component.quizImageUploading).toBeFalse();
    });

    it('should reject non-image question upload', () => {
      const topic = createValidQuizTopic();
      const question: any = {};
      const file = new File(['txt'], 'q.txt', { type: 'text/plain' });
      const input = document.createElement('input');
      Object.defineProperty(input, 'files', { value: [file] });

      component.onQuestionImageSelected(topic, question, { target: input } as any);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Please upload an image file.',
      );
      expect(fileManagerSpy.uploadFile).not.toHaveBeenCalled();
    });

    it('should remove question image when options have no images', () => {
      const topic = createValidQuizTopic();
      const question: any = {
        attachedImageUrl: 'https://cdn/q.png',
        answers: [{ attachedImageUrl: null }],
      };

      component.removeQuestionImage(topic, question);

      expect(question.attachedImageUrl).toBeNull();
    });

    it('should block removing question image when option image exists', () => {
      const topic = createValidQuizTopic();
      const question: any = {
        attachedImageUrl: 'https://cdn/q.png',
        answers: [{ attachedImageUrl: 'https://cdn/a.png' }],
      };

      component.removeQuestionImage(topic, question);

      expect(messageServiceSpy.error).toHaveBeenCalled();
      expect(question.attachedImageUrl).toBe('https://cdn/q.png');
    });

    it('should remove answer image and revalidate', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      const answer: any = { attachedImageUrl: 'https://cdn/a.png' };
      spyOn(component, 'onQuizInputChange');

      component.removeAnswerImage(topic, question, answer);

      expect(answer.attachedImageUrl).toBeNull();
      expect(component.onQuizInputChange).toHaveBeenCalledWith(topic, question);
    });

    it('should invalidate video topic while upload is in progress', () => {
      const topic: any = {
        validate: true,
        topicStatusImg: component.topicStatusCompleteImg,
        video: {
          videoData: { videoUrl: 'https://v.mp4', videoProgress: 50 },
          documentData: { documents: [] },
        },
      };

      component.videoValidation(topic);

      expect(topic.validate).toBeFalse();
    });
  });

  describe('data loading and lifecycle', () => {
    it('should call afterLoaded when uploadSectionData has no sections', () => {
      const callback = jasmine.createSpy('afterLoaded');

      component.uploadSectionData([], callback);

      expect(callback).toHaveBeenCalled();
    });

    it('should load topics for each uploaded section', () => {
      component.sections = [];
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({ status: successCode, data: [] }),
      );
      const afterLoaded = jasmine.createSpy('afterLoaded');

      component.uploadSectionData(
        [{ sectionId: 's1', sectionName: 'Intro', free: true, delete: false }],
        afterLoaded,
      );

      expect(component.sections.length).toBe(1);
      expect(courseServiceSpy.getTopicsBySectionId).toHaveBeenCalledWith('s1');
      expect(afterLoaded).toHaveBeenCalled();
    });

    it('should open bulk quiz uploader modal', () => {
      const topic = createValidQuizTopic();

      component.openBulkQuizUploaderModal(topic);

      expect(modalSpy.create).toHaveBeenCalled();
    });

    it('should format current and custom dates', () => {
      const today = component.getCurrentDate();
      expect(today).toMatch(/^\d{2}\/\d{2}\/\d{4}$/);
      expect(component.formatDate('2026-06-17')).toBe('06/17/2026');
    });

    it('should save draft on destroy by default', () => {
      spyOn(component, 'saveAsDraftCourse');

      component.ngOnDestroy();

      expect(component.saveAsDraftCourse).toHaveBeenCalled();
    });

    it('should skip draft save on destroy when flagged', () => {
      spyOn(component, 'saveAsDraftCourse');
      (component as any).skipDraftOnDestroy = true;

      component.ngOnDestroy();

      expect(component.saveAsDraftCourse).not.toHaveBeenCalled();
    });

    it('should prevent default on beforeunload', () => {
      const event = jasmine.createSpyObj<Event>('Event', ['preventDefault']);

      component.handleBeforeUnload(event);

      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('should handle publish API error without throwing', () => {
      component.sections = createValidSections();
      component.courseInformationData = createCourseInformationForm(0);
      courseServiceSpy.createCourseDto.and.returnValue(
        throwError(() => ({ error: { message: 'Failed' } })),
      );

      expect(() => component.publishCourse('next')).not.toThrow();
    });

    it('should trim article prompt input', () => {
      const article = { articlePromptInput: '  prompt  ' };

      component.articleInputChange(article);

      expect(article.articlePromptInput).toBe('prompt');
    });

    it('should open article prompt UI', () => {
      const article: any = {
        generateArticleBtn: true,
        articlePrompt: false,
        showChatBox: true,
      };

      component.openArticlePrompt(article);

      expect(article.generateArticleBtn).toBeFalse();
      expect(article.articlePrompt).toBeTrue();
      expect(article.showChatBox).toBeFalse();
    });
  });

  describe('uploadTopicsData API mapping', () => {
    const videoApiTopic = {
      topicId: 't-v1',
      topicName: 'Intro Video',
      topicType: 'Video',
      videoId: 'vid-1',
      filename: 'lecture.mp4',
      videoUrl: 'https://cdn/video.mp4',
      summary: 'video summary',
      transcript: 'transcript text',
      vttContent: 'vtt content',
      creationDate: '2026-01-15',
      docs: [
        {
          id: 'd1',
          url: 'https://cdn/slides.pdf',
          name: 'slides.pdf',
          summary: 'doc summary',
        },
      ],
    };

    const quizApiTopic = {
      topicId: 't-q1',
      topicName: 'Chapter Quiz',
      topicType: 'Quiz',
      quizId: 'qz-1',
      quizTitle: 'Quiz One',
      durationInMinutes: 20,
      passingCriteria: 70,
      testType: 'TEST',
      randomQuestion: 1,
      quizQuestionAnswer: {
        quizQuestions: [
          {
            questionId: 'q1',
            questionText: 'What is 2+2?',
            questionType: 'MULTIPLE_CHOICE',
            questionImageUrl: '',
            explanation: 'math',
            quizAnswers: [
              {
                answerId: 'a1',
                answerText: '4',
                isCorrect: true,
                answerImageUrl: '',
              },
              {
                answerId: 'a2',
                answerText: '5',
                isCorrect: false,
                answerImageUrl: '',
              },
            ],
          },
        ],
      },
    };

    const articleApiTopic = {
      topicId: 't-a1',
      topicName: 'Reading',
      topicType: 'Article',
      articleId: 'art-1',
      article: '<p>Article body</p>',
      docs: [
        {
          id: 'ad1',
          name: 'reading.pdf',
          url: 'https://cdn/reading.pdf',
          summary: 'article doc summary',
        },
      ],
    };

    it('should map video topics from API response', () => {
      const section: any = { sectionId: 's1', topics: [{ name: 'stale' }] };
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({ status: successCode, data: [videoApiTopic] }),
      );

      component.uploadTopicsData(section);

      expect(section.topics.length).toBe(1);
      expect(section.topics[0].videoSection).toBeTrue();
      expect(section.topics[0].video.videoData.videoUrl).toBe(
        'https://cdn/video.mp4',
      );
      expect(section.topics[0].video.documentData.documents.length).toBe(1);
    });

    it('should map quiz topics with questions from API response', () => {
      const section: any = { sectionId: 's1', topics: [] };
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({ status: successCode, data: [quizApiTopic] }),
      );

      component.uploadTopicsData(section);

      expect(section.topics.length).toBe(1);
      expect(section.topics[0].quizSection).toBeTrue();
      expect(section.topics[0].quiz.title).toBe('Quiz One');
      expect(section.topics[0].quiz.questions[0].ques).toBe('What is 2+2?');
      expect(section.topics[0].quiz.questions[0].answers[0].isCorrectAnswer).toBeTrue();
    });

    it('should map article topics from API response', () => {
      const section: any = { sectionId: 's1', topics: [] };
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({ status: successCode, data: [articleApiTopic] }),
      );

      component.uploadTopicsData(section);

      expect(section.topics.length).toBe(1);
      expect(section.topics[0].articleSection).toBeTrue();
      expect(section.topics[0].article.content).toBe('<p>Article body</p>');
      expect(section.topics[0].article.articleDocumnetUrl).toBe(
        'https://cdn/reading.pdf',
      );
    });

    it('should create default topics for TEST course when API returns empty', () => {
      component.selectedContentType = CourseContentType.TEST;
      const section: any = { sectionId: 's1', topics: [], delete: false };
      const createTopicsSpy = spyOn(component, 'createTopics');
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({ status: successCode, data: [] }),
      );
      const done = jasmine.createSpy('done');

      component.uploadTopicsData(section, done);

      expect(createTopicsSpy).toHaveBeenCalledWith(section);
      expect(done).toHaveBeenCalled();
    });

    it('should invoke done callback when topics API fails', () => {
      const section: any = { sectionId: 's1', topics: [] };
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        throwError(() => new Error('Topics failed')),
      );
      const done = jasmine.createSpy('done');

      component.uploadTopicsData(section, done);

      expect(done).toHaveBeenCalled();
    });
  });

  describe('quiz validation edge cases', () => {
    it('should validate true/false quiz with one correct answer', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.questionType = { key: QuestionType.TRUE_FALSE };
      question.answers = [
        { delete: false, ans: 'True', isCorrectAnswer: true, exist: false },
        { delete: false, ans: 'False', isCorrectAnswer: false, exist: false },
      ];

      component.quizValidation(topic);

      expect(topic.validate).toBeTrue();
    });

    it('should validate text-field quiz with a single answer', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.questionType = { key: QuestionType.TEXT_FIELD };
      question.answers = [{ delete: false, ans: 'Expected answer', exist: false }];

      component.quizValidation(topic);

      expect(topic.validate).toBeTrue();
    });

    it('should accept image-only multiple-choice options', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.answers = [
        {
          delete: false,
          ans: '',
          attachedImageUrl: 'https://cdn/a.png',
          isCorrectAnswer: true,
          exist: false,
        },
        {
          delete: false,
          ans: '',
          attachedImageUrl: 'https://cdn/b.png',
          isCorrectAnswer: false,
          exist: false,
        },
      ];

      component.quizValidation(topic);

      expect(topic.validate).toBeTrue();
    });

    it('should require quiz title for non-TEST courses', () => {
      component.selectedContentType = CourseContentType.COURSE;
      const topic = createValidQuizTopic();
      topic.quiz.title = '';

      component.quizValidation(topic);

      expect(topic.validate).toBeFalse();
      expect(topic.quiz.validationError).toBe('Quiz title is required');
    });
  });

  describe('video and document uploads', () => {
    const createVideoTopic = () => ({
      validate: true,
      topicStatusImg: component.topicStatusCompleteImg,
      video: {
        fileProcessing: false,
        showTable: false,
        videoData: {
          videoFileType: '',
          videoProgress: 0,
          videoFileName: '',
          videoBtnName: 'Upload File',
          delete: false,
        },
        documentData: { documents: [], documentFileName: 'Add Resource' },
      },
    });

    it('should reject video upload with special characters in filename', () => {
      const topic: any = createVideoTopic();

      component.handleVideoChange(
        {
          file: {
            name: 'bad#name.mp4',
            type: 'video/mp4',
            size: 1024,
          },
        } as any,
        topic,
      );

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'File name contains special characters.',
      );
    });

    it('should reject non-mp4 video upload', () => {
      const topic: any = createVideoTopic();

      component.handleVideoChange(
        {
          file: {
            name: 'clip.avi',
            type: 'video/avi',
            size: 1024,
          },
        } as any,
        topic,
      );

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Please upload a video file in MP4 format.',
      );
    });

    it('should reject video upload larger than 4 GB', () => {
      const topic: any = createVideoTopic();
      const fiveGb = 5 * 1024 * 1024 * 1024;

      component.handleVideoChange(
        {
          file: {
            name: 'huge.mp4',
            type: 'video/mp4',
            size: fiveGb,
          },
        } as any,
        topic,
      );

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Size should not exceed 4 GB.',
      );
    });

    it('should stage valid mp4 video for upload', () => {
      const topic: any = createVideoTopic();

      component.handleVideoChange(
        {
          file: {
            name: 'lecture.mp4',
            type: 'video/mp4',
            size: 1024,
            originFileObj: new File(['v'], 'lecture.mp4', { type: 'video/mp4' }),
          },
        } as any,
        topic,
      );

      expect(topic.video.videoData.videoFileName).toBe('lecture.mp4');
      expect(topic.video.showTable).toBeTrue();
      expect((component as any).uploadedVideos.get('lecture.mp4')).toBe(topic);
    });

    it('should accept pdf document upload', () => {
      const topic: any = createVideoTopic();

      component.handleDocumentChange(
        {
          file: {
            name: 'slides.pdf',
            type: 'application/pdf',
          },
        } as any,
        topic,
      );

      expect(topic.video.documentData.documents.length).toBe(1);
      expect(topic.video.showTable).toBeTrue();
      expect((component as any).uploadedDocuments.get('slides.pdf')).toBe(topic);
    });

    it('should reject non-pdf document upload', () => {
      const topic: any = createVideoTopic();

      component.handleDocumentChange(
        {
          file: {
            name: 'notes.docx',
            type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
          },
        } as any,
        topic,
      );

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Please upload a pdf file.',
      );
    });

    it('should simulate and stop video upload progress', fakeAsync(() => {
      const topic: any = { video: { videoData: { videoProgress: 0 } } };

      component.startVideoProgressSimulation(topic);
      tick(4000);

      expect(topic.video.videoData.videoProgress).toBe(5);
      component.stopVideoProgressSimulation();
    }));

    it('should stop document progress simulation by key', () => {
      const document = { documentKey: 'slides.pdf', documentProgress: 1 };

      component.startDocumentProgressSimulation(document);
      component.stopDocumentProgressSimulation('slides.pdf');

      expect((component as any).progressIntervals.has('slides.pdf')).toBeFalse();
    });

    it('should complete custom video upload and validate topic', () => {
      const topic: any = createVideoTopic();
      topic.video.videoData.videoProgress = 1;
      (component as any).uploadedVideos.set('lecture.mp4', topic);
      fileManagerSpy.uploadFile.and.returnValue(
        of({
          data: {
            url: 'https://cdn/lecture.mp4',
            transcriptData: {
              transcript: 'transcript',
              vttContent: 'vtt',
              summary: 'summary',
            },
          },
        }),
      );

      component.customRequestVideo({
        file: new File(['v'], 'lecture.mp4', { type: 'video/mp4' }),
      } as any);

      expect(topic.video.videoData.videoProgress).toBe(100);
      expect(topic.video.videoData.videoUrl).toBe('https://cdn/lecture.mp4');
    });

    it('should complete custom document upload on 201 response', () => {
      const topic: any = createVideoTopic();
      topic.video.documentData.documents = [
        { documentKey: 'slides.pdf', documentProgress: 1 },
      ];
      (component as any).uploadedDocuments.set('slides.pdf', topic);
      fileManagerSpy.uploadFile.and.returnValue(
        of({
          status: 201,
          data: { url: 'https://cdn/slides.pdf', summary: 'doc summary' },
        }),
      );

      component.customRequestDocument({
        file: new File(['d'], 'slides.pdf', { type: 'application/pdf' }),
      } as any);

      expect(topic.video.documentData.documents[0].documentProgress).toBe(100);
      expect(topic.video.documentData.documents[0].documentUrl).toBe(
        'https://cdn/slides.pdf',
      );
    });

    it('should reset video data on deleteVideoData', () => {
      spyOn(component, 'deleteVideoOrDocument');
      const topic: any = {
        topicId: 't1',
        validate: true,
        topicStatusImg: component.topicStatusCompleteImg,
        video: {
          videoData: {
            videoId: 'v1',
            videoUrl: 'https://cdn/v.mp4',
            videoFileName: 'v.mp4',
          },
          documentData: {
            documents: [],
            documentFileName: 'Add Resource',
            documentBtnName: 'Upload File',
          },
          showTable: true,
        },
      };

      component.deleteVideoData(topic);

      expect(component.deleteVideoOrDocument).toHaveBeenCalledWith(
        'v1',
        'https://cdn/v.mp4',
        't1',
        'VIDEO',
      );
      expect(topic.video.videoData.videoFileName).toBe('Add Video');
    });

    it('should clear article document on deleteArticleDocument', () => {
      const topic: any = {
        topicId: 't1',
        article: {
          articleDocumnetId: 'a1',
          articleDocumnetUrl: 'https://cdn/a.pdf',
          articleFileName: 'a.pdf',
          articleSummary: 'summary',
          articleBtnName: 'Replace',
        },
      };

      component.deleteArticleDocument(topic);

      expect(fileManagerSpy.deleteFile).toHaveBeenCalledWith(
        'a1',
        'https://cdn/a.pdf',
        't1',
        'DOCS',
      );
      expect(topic.article.articleFileName).toBe('Upload File');
    });
  });

  describe('summary and report modals', () => {
    it('should open document summary modal', () => {
      const document = { file: new File(['d'], 'doc.pdf') };

      component.documentSummaryModal(document);

      expect(modalSpy.create).toHaveBeenCalled();
      const config = modalSpy.create.calls.mostRecent().args[0] as any;
      expect(config.nzComponentParams.documentSummary).toBeTrue();
    });

    it('should open video summary modal', () => {
      const videoData = { file: new File(['v'], 'v.mp4') };

      component.videoSummaryModal(videoData);

      expect(modalSpy.create).toHaveBeenCalled();
      const config = modalSpy.create.calls.mostRecent().args[0] as any;
      expect(config.nzComponentParams.videoSummary).toBeTrue();
    });

    it('should open article summary modal', () => {
      const article = { file: new File(['a'], 'a.pdf') };

      component.articleSummaryModal(article);

      expect(modalSpy.create).toHaveBeenCalled();
      const config = modalSpy.create.calls.mostRecent().args[0] as any;
      expect(config.nzComponentParams.articleSummary).toBeTrue();
    });

    it('should open video transcript modal', () => {
      const videoData = { videoTranscript: 'text' };

      component.videoTranscriptModal(videoData);

      expect(modalSpy.create).toHaveBeenCalled();
      const config = modalSpy.create.calls.mostRecent().args[0] as any;
      expect(config.nzComponentParams.videoData).toBe(videoData);
    });

    it('should open report preview modal', () => {
      const topic = createValidQuizTopic();

      component.showReportPreview('<p>Report</p>', topic);

      expect(modalSpy.create).toHaveBeenCalled();
      const config = modalSpy.create.calls.mostRecent().args[0] as any;
      expect(config.nzComponentParams.reportContent).toBe('<p>Report</p>');
      expect(config.nzComponentParams.quizTitle).toBe('Chapter Quiz');
    });
  });

  describe('publish and draft error paths', () => {
    it('should short-circuit publish when course progress is 100', () => {
      component.courseInformationData = createCourseInformationForm(100);
      component.sections = createValidSections();
      const assignSpy = spyOn(component, 'assignSurveyAnswersToAnswers');
      const stepSpy = spyOn(component.currentStep, 'emit');
      const sectionSpy = spyOn(component.sectionsDataOutPut, 'emit');

      component.publishCourse('pricing');

      expect(courseServiceSpy.createCourseDto).not.toHaveBeenCalled();
      expect(assignSpy).toHaveBeenCalled();
      expect(stepSpy).toHaveBeenCalledWith('pricing');
      expect(sectionSpy).toHaveBeenCalledWith(component.sections);
    });

    it('should sync single-question randomQuestion before emit when course progress is 100', () => {
      component.selectedContentType = CourseContentType.COURSE;
      component.courseInformationData = createCourseInformationForm(100);
      component.sections = [
        {
          topics: [
            {
              selectedContentType: component.typeQuiz,
              quiz: {
                randomQuestion: null,
                questions: [{ delete: false, ques: 'Only question?' }],
              },
            },
          ],
        },
      ];
      spyOn(component, 'assignSurveyAnswersToAnswers');

      component.publishCourse('pricing');

      expect(courseServiceSpy.createCourseDto).not.toHaveBeenCalled();
      expect(component.sections[0].topics[0].quiz.randomQuestion).toBe(1);
    });

    it('should show error when publish API returns non-success status', () => {
      component.sections = createValidSections();
      component.courseInformationData = createCourseInformationForm(0);
      courseServiceSpy.createCourseDto.and.returnValue(
        of({ status: 400, message: 'Validation failed' }),
      );

      component.publishCourse('next');

      expect(messageServiceSpy.error).toHaveBeenCalledWith('Validation failed');
    });

    it('should reload sections when publish API errors and courseId exists', () => {
      component.courseId = 'course-1';
      component.sections = createValidSections();
      component.courseInformationData = createCourseInformationForm(0);
      const patchSpy = spyOn(component, 'patchSectionData');
      courseServiceSpy.createCourseDto.and.returnValue(
        throwError(() => ({ error: { message: 'Save failed' } })),
      );

      component.publishCourse('next');

      expect(patchSpy).toHaveBeenCalled();
      expect((component as any).isPublishing).toBeFalse();
    });

    it('should reset courseSaved flag when draft save fails', () => {
      component.courseSaved = false;
      component.sections = createValidSections();
      component.courseInformationData = createCourseInformationForm(0);
      courseServiceSpy.createCourseDto.and.returnValue(
        throwError(() => new Error('Draft failed')),
      );

      component.saveAsDraftCourse();

      expect(component.courseSaved).toBeFalse();
    });
  });

  describe('bulk quiz import', () => {
    it('should import questions from modal afterClose result', fakeAsync(() => {
      const afterClose = new Subject<any>();
      modalSpy.create.and.returnValue({
        afterClose: afterClose.asObservable(),
      } as any);

      const topic = createValidQuizTopic();
      topic.quiz.questions = [
        { delete: false, ques: '', answers: [], questionId: '' },
      ];

      component.openBulkQuizUploaderModal(topic);
      afterClose.next({
        fileName: 'bulk.csv',
        questions: [
          {
            questionType: 'MULTIPLE_CHOICE',
            questionText: 'Imported question?',
            answers: [
              { answerText: 'Yes', isCorrectAnswer: true },
              { answerText: 'No', isCorrectAnswer: false },
            ],
          },
        ],
      });

      tick();
      tick();

      expect(
        topic.quiz.questions.some((q: any) => q.ques === 'Imported question?'),
      ).toBeTrue();
      expect(topic.quiz.randomQuestion).toBeGreaterThan(0);
      expect(messageServiceSpy.loading).toHaveBeenCalled();
      expect(messageServiceSpy.success).toHaveBeenCalledWith(
        'Imported 1 question(s).',
      );
    }));

    it('should identify removable empty quiz questions', () => {
      const isEmpty = (component as any).isRemovableEmptyQuizQuestion.bind(
        component,
      );

      expect(
        isEmpty({ delete: false, ques: '', answers: [] }),
      ).toBeTrue();
      expect(
        isEmpty({ delete: false, ques: 'Filled', answers: [] }),
      ).toBeFalse();
      expect(
        isEmpty({ delete: false, ques: '', questionId: 'q1', answers: [] }),
      ).toBeFalse();
    });

    it('should ignore modal result without questions', () => {
      const afterClose = new Subject<any>();
      modalSpy.create.and.returnValue({
        afterClose: afterClose.asObservable(),
      } as any);
      const topic = createValidQuizTopic();
      const initialCount = topic.quiz.questions.length;

      component.openBulkQuizUploaderModal(topic);
      afterClose.next({ fileName: 'empty.csv', questions: [] });

      expect(topic.quiz.questions.length).toBe(initialCount);
      expect(messageServiceSpy.success).not.toHaveBeenCalled();
    });
  });

  describe('AI report and answer images', () => {
    it('should clear report prompt when AI report is disabled', () => {
      const topic = createValidQuizTopic();
      topic.quiz.generateAIReport = false;
      topic.quiz.reportPrompt = 'old prompt';
      spyOn(component, 'quizValidation');

      component.toggleAIReport(topic);

      expect(topic.quiz.reportPrompt).toBe('');
      expect(component.quizValidation).toHaveBeenCalledWith(topic);
    });

    it('should allow AI report when at least one question has text', () => {
      const topic = createValidQuizTopic();

      expect(component.isAllowedToGenerateAIReport(topic)).toBeTrue();
    });

    it('should block AI report when all questions are empty', () => {
      const topic = createValidQuizTopic();
      topic.quiz.questions[0].ques = '   ';

      expect(component.isAllowedToGenerateAIReport(topic)).toBeFalse();
    });

    it('should error when previewing report without duration', () => {
      const topic = createValidQuizTopic();
      topic.quiz.durationInMinutes = null;
      topic.quiz.generateAIReport = true;

      component.previewReport(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Please enter quiz duration',
      );
    });

    it('should error when previewing report without AI report enabled', () => {
      const topic = createValidQuizTopic();
      topic.quiz.generateAIReport = false;

      component.previewReport(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Please enter a report prompt',
      );
    });

    it('should preview AI report and open modal on success', () => {
      const topic = createValidQuizTopic();
      topic.quiz.generateAIReport = true;
      topic.quiz.reportPrompt = 'Summarize performance';
      const previewSpy = spyOn(component, 'showReportPreview');
      courseServiceSpy.previewAIReport.and.returnValue(of('<p>Preview</p>'));

      component.previewReport(topic);

      expect(courseServiceSpy.previewAIReport).toHaveBeenCalled();
      expect(previewSpy).toHaveBeenCalledWith('<p>Preview</p>', topic);
    });

    it('should upload answer image and revalidate', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      const answer: any = { attachedImageUrl: null };
      const file = new File(['img'], 'opt.png', { type: 'image/png' });
      const input = document.createElement('input');
      Object.defineProperty(input, 'files', { value: [file] });
      spyOn(component, 'onQuizInputChange');
      fileManagerSpy.uploadFile.and.returnValue(
        of({ data: 'https://cdn/opt.png' }),
      );

      component.onAnswerImageSelected(
        topic,
        question,
        answer,
        { target: input } as any,
      );

      expect(answer.attachedImageUrl).toBe('https://cdn/opt.png');
      expect(component.onQuizInputChange).toHaveBeenCalledWith(topic, question);
    });
  });

  describe('section and topic interaction', () => {
    it('should prevent default on sectionActive', () => {
      const event = jasmine.createSpyObj<Event>('Event', [
        'preventDefault',
        'stopPropagation',
      ]);

      component.sectionActive(event);

      expect(event.preventDefault).toHaveBeenCalled();
      expect(event.stopPropagation).toHaveBeenCalled();
    });

    it('should toggle topic active state', () => {
      const event = jasmine.createSpyObj<Event>('Event', [
        'preventDefault',
        'stopPropagation',
      ]);
      const topic: any = { active: false };

      component.topicActive(event, topic);

      expect(topic.active).toBeTrue();
    });
  });

  describe('article generation and uploads', () => {
    it('should skip article generation when prompt is empty', () => {
      const article: any = {
        articlePromptInput: '   ',
        articlePrompt: true,
        questionAnswers: { question: '', answers: [] },
      };

      component.generateArticles(article);

      expect(instructorServiceSpy.generator).not.toHaveBeenCalled();
    });

    it('should generate article content from instructor service', () => {
      instructorServiceSpy.generator.and.returnValue(
        of({
          status: successCode,
          data: '- Bullet one\n- Bullet two\nIntro paragraph',
        }),
      );
      const article: any = {
        articlePromptInput: 'Write about photosynthesis',
        articlePrompt: true,
        showChatBox: true,
        showSpinner: false,
        questionAnswers: { question: '', answers: [] },
        content: '',
        uploadArticleDocument: false,
      };
      const topic: any = {
        validate: false,
        topicStatusImg: '',
        article,
      };
      spyOn(component, 'articleValidation');

      component.generateArticles(article, topic);

      expect(instructorServiceSpy.generator).toHaveBeenCalledWith(
        'Write about photosynthesis',
      );
      expect(article.content).toContain('<ul>');
      expect(article.content).toContain('<li>Bullet one</li>');
      expect(article.content).toContain('<p>Intro paragraph</p>');
      expect(article.uploadArticleDocument).toBeTrue();
      expect(component.articleValidation).toHaveBeenCalledWith(topic);
    });

    it('should handle article generation API error', () => {
      instructorServiceSpy.generator.and.returnValue(
        throwError(() => new Error('Generation failed')),
      );
      const article: any = {
        articlePromptInput: 'prompt',
        articlePrompt: false,
        showSpinner: true,
        questionAnswers: { question: '', answers: [] },
      };

      component.generateArticles(article);

      expect(article.showSpinner).toBeFalse();
      expect(article.articlePrompt).toBeTrue();
      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Failed to generate article. Please try again.',
      );
    });

    it('should clear article chat state', () => {
      const article: any = {
        showChatBox: true,
        articlePrompt: false,
        questionAnswers: { question: 'old', answers: ['a'] },
      };

      component.clearArticleChat(article);

      expect(article.showChatBox).toBeFalse();
      expect(article.articlePrompt).toBeTrue();
      expect(article.questionAnswers.question).toBe('');
      expect(article.questionAnswers.answers).toEqual([]);
    });

    it('should open upload article screen', fakeAsync(() => {
      const article: any = { uploadArticleDocument: false };
      const section: any = { level: 2 };
      const editor = document.createElement('div');
      editor.id = 'article-editor-2-1';
      document.body.appendChild(editor);
      spyOn(editor, 'scrollIntoView');

      component.openUploadArticleScreen(article, section, 1);
      tick(150);

      expect(article.uploadArticleDocument).toBeTrue();
      expect(editor.scrollIntoView).toHaveBeenCalled();
      document.body.removeChild(editor);
    }));

    it('should stage article pdf upload', () => {
      const topic: any = {
        validate: true,
        article: {
          articleDocumnetUrl: '',
          articleProgressBar: false,
          articleFileName: 'Add Resource',
        },
      };

      component.handleArticleDocumentChange(
        {
          file: {
            name: 'reading.pdf',
            type: 'application/pdf',
          },
        } as any,
        topic,
      );

      expect(topic.article.articleFileName).toBe('reading.pdf');
      expect(topic.article.articleProgressBar).toBeTrue();
      expect((component as any).uploadedArticleDocuments.get('reading.pdf')).toBe(
        topic,
      );
    });

    it('should complete custom article document upload', () => {
      const topic: any = {
        validate: false,
        topicStatusImg: '',
        article: {
          articleProgressBar: true,
          content: 'body',
          articleFileName: 'reading.pdf',
        },
      };
      (component as any).uploadedArticleDocuments.set('reading.pdf', topic);
      fileManagerSpy.uploadFile.and.returnValue(
        of({
          status: 201,
          data: { url: 'https://cdn/reading.pdf', summary: 'summary' },
        }),
      );
      spyOn(component, 'articleValidation');

      component.customRequestArticleDocument({
        file: new File(['d'], 'reading.pdf', { type: 'application/pdf' }),
      } as any);

      expect(topic.article.articleDocumnetUrl).toBe('https://cdn/reading.pdf');
      expect(topic.article.articleProgressBar).toBeFalse();
      expect(component.articleValidation).toHaveBeenCalledWith(topic);
    });

    it('should revalidate article when editor content changes', () => {
      const topic: any = {
        validate: true,
        topicStatusImg: component.topicStatusCompleteImg,
        article: { content: 'old', articleProgressBar: false },
      };

      component.editorArticleContentChanged(null, topic);

      expect(topic.article.content).toBe('');
      expect(topic.validate).toBeFalse();
    });
  });

  describe('YouTube video handling', () => {
    it('should detect valid YouTube links', () => {
      component.checkYoutubeLink('https://www.youtube.com/watch?v=abc123');

      expect(component.isYoutubeLinkPresent).toBeTrue();
    });

    it('should reject invalid YouTube links', () => {
      component.checkYoutubeLink('https://example.com/video');

      expect(component.isYoutubeLinkPresent).toBeFalse();
    });

    it('should extract YouTube video id from url', () => {
      expect(
        component.extractYoutubeVideoId('https://www.youtube.com/watch?v=xyz789'),
      ).toBe('xyz789');
      expect(component.extractYoutubeVideoId('invalid')).toBeNull();
    });

    it('should upload YouTube video url successfully', () => {
      courseServiceSpy.youtubeVideoUrlUpload.and.returnValue(
        of({ data: 180 }),
      );
      const topic: any = {
        validate: false,
        topicDuration: 0,
        video: {
          fileProcessing: false,
          showTable: false,
          videoData: {
            youtubeVideoUrl: 'https://www.youtube.com/watch?v=abc123',
            videoFileType: '',
            videoProgress: 0,
            videoFileName: '',
            videoBtnName: 'Upload File',
            delete: false,
          },
        },
      };

      component.youtubeVideoUrlUpload(topic);

      expect(courseServiceSpy.youtubeVideoUrlUpload).toHaveBeenCalledWith(
        'abc123',
      );
      expect(topic.video.videoData.videoProgress).toBe(100);
      expect(topic.topicDuration).toBe(180);
      expect(topic.validate).toBeTrue();
    });

    it('should reset video state when YouTube upload fails', () => {
      courseServiceSpy.youtubeVideoUrlUpload.and.returnValue(
        throwError(() => ({ error: { message: 'Invalid video' } })),
      );
      const topic: any = {
        validate: false,
        video: {
          fileProcessing: false,
          showTable: true,
          videoData: {
            youtubeVideoUrl: 'https://www.youtube.com/watch?v=abc123',
            videoFileType: 'Video',
            videoProgress: 50,
            videoFileName: 'YOUTUBE',
            videoBtnName: 'Replace',
          },
          documentData: {
            documents: [],
            documentFileName: 'Add Resource',
            documentBtnName: 'Upload File',
          },
        },
      };

      component.youtubeVideoUrlUpload(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith('Invalid video');
      expect(topic.video.videoData.videoFileName).toBe('Add Video');
      expect(topic.video.videoData.videoProgress).toBe(0);
    });
  });

  describe('section management', () => {
    it('should check all sections and show delete button', () => {
      component.sections = [
        { checkSection: false },
        { checkSection: false },
      ] as any;
      const event = { target: { checked: true } };

      component.allSectionCheck(event);

      expect(component.sections.every((s: any) => s.checkSection)).toBeTrue();
      expect(component.showDltSectionBtn).toBeTrue();
    });

    it('should uncheck select-all when a section is unchecked', () => {
      component.sections = [
        { checkSection: true },
        { checkSection: false },
      ] as any;
      component.checkAllSection = true;

      component.singleSectionCheck(component.sections[1], {});

      expect(component.checkAllSection).toBeFalse();
    });

    it('should block deleting all sections', () => {
      component.sections = [{ checkSection: true }] as any;

      component.deleteSections();

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'You cannot delete all sections. At least one section must remain.',
      );
      expect(modalSpy.create).not.toHaveBeenCalled();
    });

    it('should mark selected sections deleted after modal confirm', () => {
      const deleteClick = new Subject<void>();
      modalSpy.create.and.returnValue({
        componentInstance: { deleteClick: deleteClick.asObservable() },
      } as any);
      component.sections = [
        { checkSection: true, delete: false, level: 1 },
        { checkSection: false, delete: false, level: 2 },
      ] as any;

      component.deleteSections();
      deleteClick.next();

      expect(component.sections[0].delete).toBeTrue();
      expect(component.sections[1].delete).toBeFalse();
      expect(component.showDltSectionBtn).toBeFalse();
    });

    it('should update section levels after deletion', () => {
      component.sections = [
        { delete: true, level: 1 },
        { delete: false, level: 2 },
        { delete: false, level: 3 },
      ] as any;

      component.updateSectionLevels();

      expect(component.sections[1].level).toBe(1);
      expect(component.sections[2].level).toBe(2);
    });

    it('should toggle section active state', () => {
      const event = jasmine.createSpyObj<Event>('Event', [
        'preventDefault',
        'stopPropagation',
      ]);
      const section: any = { active: false };

      component.sectionActiveStatus(event, section);

      expect(section.active).toBeTrue();
    });

    it('should mark topic incomplete on prompt keyup', () => {
      const topic: any = { topicStatusImg: component.topicStatusCompleteImg };

      component.topicPromptKeyUp(topic);

      expect(topic.topicStatusImg).toBe(component.topicStatusIncompleteImg);
    });
  });

  describe('topic deletion and documents', () => {
    it('should delete selected topics after modal confirm', () => {
      const deleteClick = new Subject<void>();
      modalSpy.create.and.returnValue({
        componentInstance: { deleteClick: deleteClick.asObservable() },
      } as any);
      const section: any = {
        topics: [
          { checkTopic: true, delete: false, level: 1 },
          { checkTopic: false, delete: false, level: 2 },
        ],
        deleteTopicIcon: true,
        checkAll: true,
      };

      component.deleteTopics(section);
      deleteClick.next();

      expect(section.topics[0].delete).toBeTrue();
      expect(section.topics[1].level).toBe(1);
      expect(section.deleteTopicIcon).toBeFalse();
    });

    it('should delete video document at index', () => {
      spyOn(component, 'deleteVideoOrDocument');
      const topic: any = {
        topicId: 't1',
        validate: true,
        topicStatusImg: component.topicStatusCompleteImg,
        video: {
          videoData: { videoFileName: 'Add Video' },
          documentData: {
            documents: [
              {
                id: 'd1',
                documentUrl: 'https://cdn/doc.pdf',
                delete: false,
              },
            ],
            documentFileName: 'doc.pdf',
          },
          showTable: true,
        },
      };

      component.deleteDocument(topic, 0);

      expect(component.deleteVideoOrDocument).toHaveBeenCalledWith(
        'd1',
        'https://cdn/doc.pdf',
        't1',
        'DOCS',
      );
      expect(topic.video.documentData.documents[0].delete).toBeTrue();
    });
  });

  describe('quiz question helpers', () => {
    it('should toggle correct answer for multiple choice', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.questionType = { key: QuestionType.MULTIPLE_CHOICE };
      question.answers[0].isCorrectAnswer = true;
      spyOn(component, 'onQuizInputChange');

      component.markCorrectAnswer(topic, question, question.answers[0]);

      expect(question.answers[0].isCorrectAnswer).toBeFalse();
      expect(component.onQuizInputChange).toHaveBeenCalledWith(topic);
    });

    it('should set single correct answer for single choice', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.questionType = { key: QuestionType.SINGLE_CHOICE };
      question.answers[0].isCorrectAnswer = true;
      spyOn(component, 'onQuizInputChange');

      component.markCorrectAnswer(topic, question, question.answers[1]);

      expect(question.answers[0].isCorrectAnswer).toBeFalse();
      expect(question.answers[1].isCorrectAnswer).toBeTrue();
    });

    it('should reset answers when changing to true/false type', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.questionType = { key: QuestionType.TRUE_FALSE };
      question.answers = [];

      component.changeQuestionType(topic, question);

      expect(question.answers.length).toBe(2);
      expect(question.answers[0].ans).toBe('True');
    });

    it('should remove quiz question when more than one exists', () => {
      const topic: any = createValidQuizTopic();
      topic.quiz.randomQuestion = 2;
      topic.quiz.questions.push({
        delete: false,
        ques: 'Q2',
        questionType: { key: QuestionType.MULTIPLE_CHOICE },
        answers: [
          { delete: false, ans: 'a', isCorrectAnswer: true, exist: false },
          { delete: false, ans: 'b', isCorrectAnswer: false, exist: false },
        ],
      });

      component.removeQuizQuestion(topic, topic.quiz.questions[0]);

      expect(topic.quiz.questions[0].delete).toBeTrue();
      expect(topic.quiz.randomQuestion).toBe(1);
    });

    it('should remove quiz answer option', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      const answer = question.answers[1];

      component.removeQuizQuestionAnswers(topic, question, answer);

      expect(answer.delete).toBeTrue();
    });

    it('should mark text-field answer as correct on change', () => {
      const topic = createValidQuizTopic();
      const answer: any = { isCorrectAnswer: false };

      component.onTextFieldAnswerChange('typed answer', topic, answer);

      expect(answer.isCorrectAnswer).toBeTrue();
    });

    it('should clamp passing criteria between 0 and 100', () => {
      const topic: any = { quiz: { passingCriteria: 150 } };

      component.enforceLimit(topic);

      expect(topic.quiz.passingCriteria).toBe(100);

      topic.quiz.passingCriteria = -5;
      component.enforceLimit(topic);

      expect(topic.quiz.passingCriteria).toBe(0);
    });

    it('should block non-numeric quiz number input', () => {
      const event = {
        key: 'a',
        which: 65,
        keyCode: 65,
        preventDefault: jasmine.createSpy('preventDefault'),
        target: { value: '' },
      } as any;

      component.validateNumberInput(event);

      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('should block passing criteria above 100', () => {
      const event = {
        key: '5',
        which: 53,
        keyCode: 53,
        preventDefault: jasmine.createSpy('preventDefault'),
        target: { value: '95' },
      } as any;

      component.validateNumberInput(event, false, true);

      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('should set random question count to all active questions', () => {
      const topic = createValidQuizTopic();
      topic.quiz.randomQuestionType = component.randomQuestionAll;
      topic.quiz.questions.push({
        delete: false,
        ques: 'Q2',
        questionType: { key: QuestionType.MULTIPLE_CHOICE },
        answers: [],
      });

      component.onSelectRandomQuestionType(topic);

      expect(topic.quiz.randomQuestion).toBe(2);
    });

    it('should regenerate survey options when topic type changes', () => {
      const topic: any = createValidQuizTopic();
      topic.quiz.type = QuizType.SURVEY;
      topic.quiz.questions = [
        {
          ques: 'old',
          surveyAnswers: [],
          answers: [{ ans: 'x', delete: false }],
          surveyQuestionCount: 5,
        },
      ];

      component.onSelectTopicType(topic);

      expect(topic.quiz.questions[0].ques).toBe('');
      expect(topic.quiz.questions[0].surveyAnswers.length).toBe(5);
    });

    it('should update survey answers when question count changes', () => {
      const question: any = {
        surveyQuestionCount: 3,
        surveyAnswers: [],
        answers: [],
      };

      component.onSelectSurveyQuestionCount(question);

      expect(question.surveyAnswers.length).toBe(3);
      expect(question.answers).toBe(question.surveyAnswers);
    });

    it('should filter deleted answers for display', () => {
      const visible = component.questionShowAnswers([
        { delete: false, ans: 'A' },
        { delete: true, ans: 'B' },
      ]);

      expect(visible.length).toBe(1);
      expect(visible[0].ans).toBe('A');
    });

    it('should assign survey answers to answers for API payload', () => {
      component.sections = [
        {
          topics: [
            {
              quiz: {
                type: QuizType.SURVEY,
                questions: [
                  {
                    surveyAnswers: [{ answer: 'Strongly Agree', count: 1 }],
                    answers: [],
                  },
                ],
              },
            },
          ],
        },
      ] as any;

      component.assignSurveyAnswersToAnswers();

      expect(
        component.sections[0].topics[0].quiz.questions[0].answers[0].ans,
      ).toBe('Strongly Agree');
    });
  });

  describe('media and input guards', () => {
    it('should upload question audio media', () => {
      const topic = createValidQuizTopic();
      const question: any = {};
      const file = new File(['audio'], 'clip.mp3', { type: 'audio/mpeg' });
      const input = document.createElement('input');
      Object.defineProperty(input, 'files', { value: [file] });
      spyOn(component, 'onQuizInputChange');
      fileManagerSpy.uploadFile.and.returnValue(
        of({ data: 'https://cdn/clip.mp3' }),
      );

      component.onQuestionMediaSelected(topic, question, { target: input } as any);

      expect(question.attachedImageUrl).toBe('https://cdn/clip.mp3');
      expect(question.mediaType).toBe('audio');
      expect(component.onQuizInputChange).toHaveBeenCalledWith(topic, question);
    });

    it('should reject unsupported question media types', () => {
      const topic = createValidQuizTopic();
      const question: any = {};
      const file = new File(['txt'], 'notes.txt', { type: 'text/plain' });
      const input = document.createElement('input');
      Object.defineProperty(input, 'files', { value: [file] });

      component.onQuestionMediaSelected(topic, question, { target: input } as any);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Please upload an image or audio file.',
      );
    });

    it('should remove question audio attachment', () => {
      const topic = createValidQuizTopic();
      const question: any = { attachedImageUrl: 'https://cdn/a.mp3' };
      component.mediaAttached = true;
      component.mediaType = 'audio';
      spyOn(component, 'onQuizInputChange');

      component.removeQuestionaAudio(topic, question);

      expect(question.attachedImageUrl).toBeNull();
      expect(component.mediaAttached).toBeFalse();
      expect(component.mediaType).toBeNull();
    });

    it('should toggle audio playback state', () => {
      const question: any = {
        attachedImageUrl: 'https://cdn/sample.mp3',
        isPlaying: false,
      };
      const audio = document.createElement('audio');
      audio.src = 'https://cdn/sample.mp3';
      spyOn(audio, 'play').and.returnValue(Promise.resolve());
      document.body.appendChild(audio);

      component.toggleAudio(question);

      expect(question.isPlaying).toBeTrue();
      document.body.removeChild(audio);
    });

    it('should prevent emoji key input', () => {
      const event = {
        key: '😀',
        preventDefault: jasmine.createSpy('preventDefault'),
      } as any;

      component.preventEmoji(event);

      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('should prevent emoji paste', () => {
      const event = {
        clipboardData: {
          getData: () => 'hello 😀',
        },
        preventDefault: jasmine.createSpy('preventDefault'),
      } as any;

      component.preventEmojiOnPaste(event);

      expect(event.preventDefault).toHaveBeenCalled();
    });
  });

  describe('validation flow and misc helpers', () => {
    it('should continue when validateAndContinue has valid sections', () => {
      spyOn(component, 'sectionValidation').and.returnValue(true);
      const stepsSpy = spyOn(component, 'steps');
      const emitSpy = spyOn(component.sectionsDataOutPut, 'emit');
      component.sections = createValidSections();

      component.validateAndContinue();

      expect(emitSpy).toHaveBeenCalledWith(component.sections);
      expect(stepsSpy).toHaveBeenCalledWith(2);
    });

    it('should show validation errors when validateAndContinue is invalid', () => {
      spyOn(component, 'sectionValidation').and.returnValue(false);
      const showErrorsSpy = spyOn(component as any, 'showValidationErrors');
      component.sections = createValidSections();

      component.validateAndContinue();

      expect(showErrorsSpy).toHaveBeenCalled();
    });

    it('should set content section flags', () => {
      const topic: any = {};

      component.contentSection(topic, true, false, false);

      expect(topic.videoSection).toBeTrue();
      expect(topic.articleSection).toBeFalse();
      expect(topic.quizSection).toBeFalse();
    });

    it('should update report prompt and revalidate', () => {
      const topic = createValidQuizTopic();
      topic.quiz.reportPrompt = '';
      spyOn(component, 'quizValidation');

      component.onReportPromptChange(topic, {
        target: { value: 'New prompt' },
      });

      expect(topic.quiz.reportPrompt).toBe('New prompt');
      expect(component.quizValidation).toHaveBeenCalledWith(topic);
    });

    it('should return logged-in user initials', () => {
      const authSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
      authSpy.getLoggedInName.and.returnValue('RA');

      expect(component.getInitialOfLoggedInUser).toBe('RA');
    });

    it('should not publish when sections are empty', () => {
      component.sections = [];
      component.courseInformationData = createCourseInformationForm(0);

      component.publishCourse('next');

      expect(courseServiceSpy.createCourseDto).not.toHaveBeenCalled();
    });

    it('should reset video upload state on custom upload error', () => {
      const topic: any = {
        video: {
          videoData: {
            videoProgress: 50,
            videoFileName: 'lecture.mp4',
            videoBtnName: 'Replace',
            videoFileType: 'video',
            date: '01/01/2026',
          },
        },
      };
      (component as any).uploadedVideos.set('lecture.mp4', topic);
      fileManagerSpy.uploadFile.and.returnValue(
        throwError(() => new Error('Upload failed')),
      );

      component.customRequestVideo({
        file: new File(['v'], 'lecture.mp4', { type: 'video/mp4' }),
      } as any);

      expect(topic.video.videoData.videoFileName).toBe('Add Video');
      expect(topic.video.videoData.videoProgress).toBe(0);
    });

    it('should invalidate video topic without any source', () => {
      const topic: any = {
        validate: true,
        topicStatusImg: component.topicStatusCompleteImg,
        video: {
          videoData: { videoUrl: '', youtubeVideoUrl: '', videoProgress: 100 },
          documentData: { documents: [] },
        },
      };

      component.videoValidation(topic);

      expect(topic.validate).toBeFalse();
    });
  });

  describe('save quiz video and article', () => {
    it('should save valid quiz and toggle topic active state', () => {
      const topic: any = createValidQuizTopic();
      topic.active = true;
      topic.name = 'Chapter Quiz';
      topic.selectedContentType = 'Quiz';
      spyOn(component, 'checkPreviousTopic');

      component.saveQuiz(topic);

      expect(topic.completed).toBeTrue();
      expect(topic.validate).toBeTrue();
      expect(topic.active).toBeFalse();
      expect(component.checkPreviousTopic).toHaveBeenCalledWith(topic);
      expect(messageServiceSpy.success).toHaveBeenCalledWith(
        'Quiz saved successfully!',
      );
    });

    it('should show quiz validation error and scroll to topic', fakeAsync(() => {
      const topic: any = createValidQuizTopic();
      topic.name = 'Quiz Topic';
      topic.quiz.durationInMinutes = null;
      const panel = document.createElement('div');
      panel.className = 'topic-collapse-panel';
      document.body.appendChild(panel);
      spyOn(panel, 'scrollIntoView');

      component.saveQuiz(topic);
      tick(100);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Quiz duration is required',
      );
      expect(topic.active).toBeTrue();
      document.body.removeChild(panel);
    }));

    it('should show specific error for empty question text on save', () => {
      const topic: any = createValidQuizTopic();
      topic.name = 'Quiz Topic';
      topic.quiz.questions[0].ques = '';

      component.saveQuiz(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Question 1: Question text cannot be empty',
      );
    });

    it('should save valid video content', () => {
      const topic: any = {
        active: true,
        selectedContentType: 'Video',
        validate: false,
        topicStatusImg: '',
        video: {
          videoData: { videoUrl: 'https://cdn/v.mp4', videoProgress: 100 },
          documentData: { documents: [] },
        },
      };
      spyOn(component, 'checkPreviousTopic');

      component.saveVideo(topic);

      expect(topic.completed).toBeTrue();
      expect(topic.active).toBeFalse();
      expect(messageServiceSpy.success).toHaveBeenCalledWith(
        'Video content saved successfully!',
      );
    });

    it('should error when saving video without a source', fakeAsync(() => {
      const topic: any = {
        active: false,
        name: 'Video Topic',
        validate: false,
        video: {
          videoData: { videoUrl: '', youtubeVideoUrl: '', videoProgress: 100 },
          documentData: { documents: [] },
        },
      };
      const panel = document.createElement('div');
      panel.className = 'topic-collapse-panel';
      document.body.appendChild(panel);

      component.saveVideo(topic);
      tick(100);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Please add a video file or YouTube URL before saving (document alone is not enough).',
      );
      document.body.removeChild(panel);
    }));

    it('should save valid article content', () => {
      const topic: any = {
        active: true,
        validate: false,
        topicStatusImg: '',
        article: { content: 'Article body', articleProgressBar: false },
      };
      spyOn(component, 'checkPreviousTopic');

      component.saveArticle(topic);

      expect(topic.completed).toBeTrue();
      expect(topic.active).toBeFalse();
      expect(messageServiceSpy.success).toHaveBeenCalledWith(
        'Article saved successfully!',
      );
    });

    it('should error when saving incomplete article', fakeAsync(() => {
      const topic: any = {
        active: false,
        name: 'Article Topic',
        validate: false,
        article: { content: '', articleProgressBar: false },
      };
      const panel = document.createElement('div');
      panel.className = 'topic-collapse-panel';
      document.body.appendChild(panel);

      component.saveArticle(topic);
      tick(100);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Please complete all required fields for the article',
      );
      document.body.removeChild(panel);
    }));
  });

  describe('content type cleanup and topic creation', () => {
    it('should mark previous video and quiz deleted when saving article topic', () => {
      const topic: any = {
        selectedContentType: 'Article',
        video: {
          videoData: { videoId: 'v1', delete: false },
          documentData: {
            documents: [{ id: 'd1', delete: false }],
          },
        },
        quiz: { quizId: 'q1', delete: false },
        article: { articleId: 'a1', delete: false },
      };

      component.checkPreviousTopic(topic);

      expect(topic.video.videoData.delete).toBeTrue();
      expect(topic.video.documentData.documents[0].delete).toBeTrue();
      expect(topic.quiz.delete).toBeTrue();
      expect(topic.article.delete).toBeFalse();
    });

    it('should mark previous video and article deleted when saving quiz topic', () => {
      const topic: any = {
        selectedContentType: 'Quiz',
        video: {
          videoData: { videoId: 'v1', delete: false },
          documentData: { documents: [] },
        },
        quiz: { quizId: 'q1', delete: false },
        article: { articleId: 'a1', delete: false },
      };

      component.checkPreviousTopic(topic);

      expect(topic.video.videoData.delete).toBeTrue();
      expect(topic.article.delete).toBeTrue();
    });

    it('should skip cleanup when previous content has no persisted ids', () => {
      const topic: any = {
        selectedContentType: 'Video',
        video: {
          videoData: { videoId: '', delete: false },
          documentData: { documents: [] },
        },
        quiz: { quizId: '', delete: false },
        article: { articleId: '', delete: false },
      };

      component.checkPreviousTopic(topic);

      expect(topic.quiz.delete).toBeFalse();
      expect(topic.article.delete).toBeFalse();
    });

    it('should create a default topic for non-TEST sections', () => {
      component.selectedContentType = CourseContentType.COURSE;
      const section: any = {
        topics: [],
        generateTopicsPrompt: false,
        deleteAll: false,
        deleteTopicIcon: true,
        generateTopicBtn1: true,
        generateTopicBtn2: false,
      };

      component.createTopics(section);

      expect(section.topics.length).toBe(1);
      expect(section.topics[0].selectedContentType).toBe('');
      expect(section.deleteTopicIcon).toBeFalse();
    });

    it('should open article content and validate', () => {
      const topic: any = {
        contentScreen: true,
        topicStatusImg: component.topicStatusCompleteImg,
        validate: false,
        article: { content: 'Body text', articleProgressBar: false },
      };

      component.openContent(topic, component.typeArticle);

      expect(topic.articleSection).toBeTrue();
      expect(topic.validate).toBeTrue();
    });

    it('should add a multiple-choice option to existing answers', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      const initialCount = question.answers.length;

      component.addOption(topic, question);

      expect(question.answers.length).toBe(initialCount + 1);
    });
  });

  describe('section and topic validity helpers', () => {
    it('should validate a complete section', () => {
      const section = {
        name: 'Section 1',
        topics: [{ delete: false, name: 'Topic 1', validate: true }],
      };

      expect(component.isSectionValid(section)).toBeTrue();
    });

    it('should invalidate section with empty name', () => {
      expect(
        component.isSectionValid({ name: '  ', topics: [{ name: 'T', validate: true }] }),
      ).toBeFalse();
    });

    it('should invalidate section without topics', () => {
      expect(component.isSectionValid({ name: 'S1', topics: [] })).toBeFalse();
    });

    it('should validate topic with name and completion flag', () => {
      expect(
        component.isTopicValid({ name: 'Topic', validate: true }),
      ).toBeTrue();
      expect(
        component.isTopicValid({ name: 'Topic', validate: false }),
      ).toBeFalse();
    });

    it('should detect non-deleted topics in section', () => {
      expect(
        component.hasNonDeletedTopics({
          topics: [{ delete: true }, { delete: false }],
        }),
      ).toBeTrue();
      expect(
        component.hasNonDeletedTopics({ topics: [{ delete: true }] }),
      ).toBeFalse();
    });

    it('should show aggregated validation errors', () => {
      component.sections = [
        {
          delete: false,
          name: '',
          topics: [
            {
              delete: false,
              name: '',
              validate: false,
              selectedContentType: 'Video',
              video: {
                videoData: { videoUrl: '', youtubeVideoUrl: '' },
              },
            },
          ],
        },
      ] as any;

      (component as any).showValidationErrors();

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        jasmine.stringMatching(/Please complete:/),
      );
    });

    it('should toggle delete topic icon based on selection', () => {
      const section: any = {
        topics: [{ checkTopic: false }, { checkTopic: true }],
        deleteTopicIcon: false,
      };

      component.deleteTopicIcon(section);

      expect(section.deleteTopicIcon).toBeTrue();
    });

    it('should reset check-all when hiding delete section container', () => {
      component.sections = [
        { checkSection: true },
        { checkSection: true },
      ] as any;
      component.checkAllSection = true;

      component.hideDeleteSectionContainer();

      expect(component.checkAllSection).toBeFalse();
    });
  });

  describe('scroll and invalid field targeting', () => {
    it('should find first invalid quiz question in sections', () => {
      const topic: any = {
        delete: false,
        quizSection: true,
        quiz: { invalidQuestions: [1] },
      };
      component.sections = [{ delete: false, topics: [topic] }] as any;

      const found = (component as any).findFirstInvalidQuestion();

      expect(found?.topic).toBe(topic);
      expect(found?.questionIndex).toBe(1);
      expect(found?.sectionIndex).toBe(0);
    });

    it('should scroll to invalid question when validateAndContinue fails', () => {
      const topic: any = createValidQuizTopic();
      topic.quiz.invalidQuestions = [0];
      topic.quizSection = true;
      topic.name = 'Quiz';
      component.sections = [
        { delete: false, name: 'Section', topics: [topic] },
      ] as any;
      spyOn(component, 'sectionValidation').and.returnValue(false);
      const scrollSpy = spyOn(component as any, 'scrollToInvalidQuestion');

      component.validateAndContinue();

      expect(scrollSpy).toHaveBeenCalledWith(topic, 0, 0);
    });

    it('should scroll to first invalid control in the DOM', fakeAsync(() => {
      component.sections = [{ delete: false, name: '', topics: [] }] as any;
      const input = document.createElement('input');
      input.className = 'section-name-input';
      spyOn(input, 'scrollIntoView');
      document.body.appendChild(input);

      (component as any).scrollToFirstInvalidField();
      tick(100);

      expect(input.scrollIntoView).toHaveBeenCalled();
      document.body.removeChild(input);
    }));

    it('should expand section and scroll to invalid question element', fakeAsync(() => {
      component.sections = [{ active: false, topics: [] }] as any;
      const topic: any = { active: false, level: 1, topicId: 't1', name: 'Q' };
      const questionEl = document.createElement('div');
      questionEl.className = 'question-outer-container';
      questionEl.setAttribute('data-topic-id', 't1');
      questionEl.setAttribute('data-question-index', '0');
      const input = document.createElement('input');
      questionEl.appendChild(input);
      spyOn(questionEl, 'scrollIntoView');
      document.body.appendChild(questionEl);

      (component as any).scrollToInvalidQuestion(topic, 0, 0);
      tick(100);
      tick(300);
      flush();

      expect(component.sections[0].active).toBeTrue();
      expect(topic.active).toBeTrue();
      expect(questionEl.scrollIntoView).toHaveBeenCalled();
      document.body.removeChild(questionEl);
    }));

    it('should scroll to topic top for report preview duration error', fakeAsync(() => {
      const topic: any = { name: 'Quiz Topic', active: false };
      const input = document.createElement('input');
      input.setAttribute('ng-reflect-model', 'Quiz Topic');
      spyOn(input, 'scrollIntoView');
      document.body.appendChild(input);
      topic.quiz = { durationInMinutes: null, generateAIReport: true };

      component.previewReport(topic);
      tick(100);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Please enter quiz duration',
      );
      expect(topic.active).toBeTrue();
      document.body.removeChild(input);
    }));
  });

  describe('data loading and publish guards', () => {
    it('should rebuild sections from patchSectionData response', () => {
      component.courseId = 'course-1';
      component.sections = [{ name: 'stale' }] as any;
      const uploadSpy = spyOn(component, 'uploadSectionData');
      courseServiceSpy.getSectionByCourseId.and.returnValue(
        of({
          status: successCode,
          data: [{ sectionId: 's1', sectionName: 'Fresh', free: true }],
        }),
      );

      component.patchSectionData();

      expect(component.sections).toEqual([]);
      expect(uploadSpy).toHaveBeenCalled();
    });

    it('should load multiple sections via uploadSectionData', () => {
      component.sections = [];
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({ status: successCode, data: [] }),
      );
      const done = jasmine.createSpy('done');

      component.uploadSectionData(
        [
          { sectionId: 's1', sectionName: 'A', free: true, delete: false },
          { sectionId: 's2', sectionName: 'B', free: false, delete: false },
        ],
        done,
      );

      expect(component.sections.length).toBe(2);
      expect(courseServiceSpy.getTopicsBySectionId).toHaveBeenCalledTimes(2);
      expect(done).toHaveBeenCalled();
    });

    it('should skip publish when already publishing', () => {
      (component as any).isPublishing = true;
      component.sections = createValidSections();
      component.courseInformationData = createCourseInformationForm(0);

      component.publishCourse('next');

      expect(courseServiceSpy.createCourseDto).not.toHaveBeenCalled();
    });

    it('should not update courseId when draft save returns non-success', () => {
      component.courseSaved = false;
      component.courseId = null;
      component.sections = createValidSections();
      component.courseInformationData = createCourseInformationForm(0);
      courseServiceSpy.createCourseDto.and.returnValue(
        of({ status: 400, data: null }),
      );

      component.saveAsDraftCourse();

      expect(component.courseId).toBeNull();
      expect(component.courseSaved).toBeFalse();
    });

    it('should handle preview report API error', () => {
      const topic = createValidQuizTopic();
      topic.quiz.generateAIReport = true;
      topic.quiz.reportPrompt = 'Summarize';
      courseServiceSpy.previewAIReport.and.returnValue(
        throwError(() => new Error('API error')),
      );

      component.previewReport(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Failed to generate report preview',
      );
    });

    it('should handle topic generation API error', () => {
      const section: any = {
        topicInput: 'Algebra',
        questionAnswers: { question: '', answers: [] },
        showSpinner: true,
        generateTopicsPrompt: false,
      };
      instructorServiceSpy.generator.and.returnValue(
        throwError(() => new Error('fail')),
      );

      component.generateTopics(section);

      expect(section.showSpinner).toBeFalse();
    });
  });

  describe('survey input guards and drag handlers', () => {
    it('should block random question count of zero', () => {
      const topic = createValidQuizTopic();
      const event = {
        key: '0',
        which: 48,
        keyCode: 48,
        preventDefault: jasmine.createSpy('preventDefault'),
        target: { value: '' },
      } as any;

      component.validateRandomNumberInput(event, topic);

      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('should block random question count above active questions', () => {
      const topic = createValidQuizTopic();
      const event = {
        key: '3',
        which: 51,
        keyCode: 51,
        preventDefault: jasmine.createSpy('preventDefault'),
        target: { value: '' },
      } as any;

      component.validateRandomNumberInput(event, topic);

      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('should update default survey option label', () => {
      component.updateDefaultSurveyOption(1, 'Somewhat Agree');

      expect(component.surveyDefaults[1]).toBe('Somewhat Agree');
    });

    it('should disable section drop target on enter', () => {
      const dropRef = { disabled: false };
      const event = {
        item: { dropContainer: { _dropListRef: dropRef } },
      } as any;

      component.sectionEnter(event);

      expect(dropRef.disabled).toBeTrue();
    });

    it('should disable topic drop target on enter', () => {
      const dropRef = { disabled: false };
      const event = {
        item: { dropContainer: { _dropListRef: dropRef } },
      } as any;

      component.topicEnter(event);

      expect(dropRef.disabled).toBeTrue();
    });

    it('should disable quiz question drop target on enter', () => {
      const dropRef = { disabled: false };
      const event = {
        item: { dropContainer: { _dropListRef: dropRef } },
      } as any;

      component.quizQuestionEnter(event);

      expect(dropRef.disabled).toBeTrue();
    });

    it('should open generic deletion modal', () => {
      component.openDeletionModal('Delete this item?');

      expect(modalSpy.create).toHaveBeenCalled();
      const config = modalSpy.create.calls.mostRecent().args[0] as any;
      expect(config.nzComponentParams.msg).toBe('Delete this item?');
    });

    it('should reject document upload with special characters in filename', () => {
      const topic: any = {
        validate: true,
        video: {
          documentData: { documents: [] },
          videoData: { videoFileName: '' },
        },
      };

      component.handleDocumentChange(
        {
          file: { name: 'bad#file.pdf', type: 'application/pdf' },
        } as any,
        topic,
      );

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'File name contains special characters.',
      );
    });

    it('should seek audio position on progress bar click', () => {
      const question: any = { attachedAudioUrl: 'https://cdn/track.mp3' };
      const audio = document.createElement('audio');
      audio.src = 'https://cdn/track.mp3';
      Object.defineProperty(audio, 'duration', { value: 200, writable: true });
      document.body.appendChild(audio);
      const bar = document.createElement('div');
      spyOn(bar, 'getBoundingClientRect').and.returnValue({
        left: 0,
        width: 100,
      } as DOMRect);
      const event = {
        currentTarget: bar,
        clientX: 50,
      } as unknown as MouseEvent;

      component.seekAudio(event, question);

      expect(audio.currentTime).toBe(100);
      document.body.removeChild(audio);
    });

    it('should pause audio when toggling while playing', () => {
      const question: any = {
        attachedImageUrl: 'https://cdn/sample.mp3',
        isPlaying: true,
      };
      const audio = document.createElement('audio');
      audio.src = 'https://cdn/sample.mp3';
      spyOn(audio, 'pause');
      document.body.appendChild(audio);

      component.toggleAudio(question);

      expect(audio.pause).toHaveBeenCalled();
      expect(question.isPlaying).toBeFalse();
      document.body.removeChild(audio);
    });
  });

  describe('saveQuiz type-specific errors', () => {
    function quizTopicForSave(): any {
      const topic = createValidQuizTopic();
      topic.name = 'Quiz Topic';
      topic.active = false;
      return topic;
    }

    it('should error when single-choice has fewer than two options', () => {
      const topic = quizTopicForSave();
      const question = topic.quiz.questions[0];
      question.questionType = {
        key: QuestionType.SINGLE_CHOICE,
        value: 'Single choice',
      };
      question.answers = [
        { delete: false, ans: 'Only', isCorrectAnswer: true, exist: false },
      ];

      component.saveQuiz(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Question 1: Single choice questions require at least two options',
      );
    });

    it('should error when single-choice has no correct answer', () => {
      const topic = quizTopicForSave();
      const question = topic.quiz.questions[0];
      question.questionType = {
        key: QuestionType.SINGLE_CHOICE,
        value: 'Single choice',
      };
      question.answers[0].isCorrectAnswer = false;
      question.answers[1].isCorrectAnswer = false;

      component.saveQuiz(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Question 1: Single choice questions require exactly one correct answer',
      );
    });

    it('should error when multiple-choice has no correct answer', () => {
      const topic = quizTopicForSave();
      const question = topic.quiz.questions[0];
      question.answers[0].isCorrectAnswer = false;
      question.answers[1].isCorrectAnswer = false;

      component.saveQuiz(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Question 1: Multiple choice questions require at least one correct answer',
      );
    });

    it('should error when multiple-choice option text is empty', () => {
      const topic = quizTopicForSave();
      const question = topic.quiz.questions[0];
      question.answers[0].ans = '   ';

      component.saveQuiz(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Question 1: All options must have text',
      );
    });

    it('should error when true/false has wrong option count', () => {
      const topic = quizTopicForSave();
      const question = topic.quiz.questions[0];
      question.questionType = {
        key: QuestionType.TRUE_FALSE,
        value: 'True/False',
      };
      question.answers = [
        { delete: false, ans: 'True', isCorrectAnswer: true, exist: false },
      ];

      component.saveQuiz(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Question 1: True/False questions must have exactly two options (True and False)',
      );
    });

    it('should error when true/false has no correct answer selected', () => {
      const topic = quizTopicForSave();
      const question = topic.quiz.questions[0];
      question.questionType = {
        key: QuestionType.TRUE_FALSE,
        value: 'True/False',
      };
      question.answers = [
        { delete: false, ans: 'True', isCorrectAnswer: false, exist: false },
        { delete: false, ans: 'False', isCorrectAnswer: false, exist: false },
      ];

      component.saveQuiz(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Question 1: True/False questions require selecting exactly one correct answer',
      );
    });

    it('should error when text-field answer is empty', () => {
      const topic = quizTopicForSave();
      const question = topic.quiz.questions[0];
      question.questionType = {
        key: QuestionType.TEXT_FIELD,
        value: 'Text field',
      };
      question.answers = [
        { delete: false, ans: '', isCorrectAnswer: false, exist: false },
      ];

      component.saveQuiz(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Question 1: Text field answer cannot be empty',
      );
    });
  });

  describe('section validation and survey paths', () => {
    it('should pass section validation for a valid survey topic', () => {
      component.sections = [
        {
          delete: false,
          deleteAll: false,
          name: 'Section 1',
          topics: [
            {
              delete: false,
              name: 'Survey Topic',
              validate: false,
              quiz: {
                type: QuizType.SURVEY,
                questions: [
                  {
                    delete: false,
                    ques: 'How was it?',
                    surveyAnswers: [{ answer: 'Great' }, { answer: 'Good' }],
                  },
                ],
              },
            },
          ],
        },
      ] as any;

      expect(component.sectionValidation()).toBeTrue();
    });

    it('should fail section validation when all sections are deleted', () => {
      component.sections = [{ delete: true, name: 'S', topics: [] }] as any;

      expect(component.sectionValidation()).toBeFalse();
    });

    it('should fail section validation when section has no topics', () => {
      component.sections = [
        { delete: false, deleteAll: false, name: 'Empty', topics: [] },
      ] as any;

      expect(component.sectionValidation()).toBeFalse();
    });

    it('should check all topics via allTopicCheck', () => {
      const section: any = {
        topics: [{ checkTopic: false }, { checkTopic: false }],
        deleteTopicIcon: false,
      };

      component.allTopicCheck(section, { target: { checked: true } });

      expect(section.topics.every((t: any) => t.checkTopic)).toBeTrue();
      expect(section.deleteTopicIcon).toBeTrue();
    });

    it('should update topic levels skipping deleted topics', () => {
      const section: any = {
        topics: [
          { delete: true, level: 9 },
          { delete: false, level: 9 },
          { delete: false, level: 9 },
        ],
      };

      component.updateTopicLevels(section);

      expect(section.topics[1].level).toBe(1);
      expect(section.topics[2].level).toBe(2);
    });

    it('should collapse other topics when one is activated', () => {
      component.sections = [
        {
          topics: [
            { active: true, delete: false },
            { active: false, delete: false },
          ],
        },
      ] as any;
      const activeTopic = component.sections[0].topics[1];

      (component as any).collapseOtherTopics(activeTopic);

      expect(component.sections[0].topics[0].active).toBeFalse();
      expect(activeTopic.active).toBeTrue();
    });
  });

  describe('findFirstInvalidControl fallbacks', () => {
    afterEach(() => {
      document
        .querySelectorAll(
          '.own-topic-btn, .topic-input, .quiz-save-btn, .topic-collapse-panel, .test-section-name-input',
        )
        .forEach((el) => el.remove());
    });

    it('should find add-topic button for section without topics', () => {
      component.sections = [
        { delete: false, name: 'Section', topics: [] },
      ] as any;
      const btn = document.createElement('button');
      btn.className = 'own-topic-btn';
      document.body.appendChild(btn);

      const found = (component as any).findFirstInvalidControl();

      expect(found).toBe(btn);
    });

    it('should find topic name input for empty topic name', () => {
      component.sections = [
        {
          delete: false,
          name: 'Section',
          topics: [{ delete: false, name: '', validate: false }],
        },
      ] as any;
      const input = document.createElement('input');
      input.className = 'topic-input';
      document.body.appendChild(input);

      const found = (component as any).findFirstInvalidControl();

      expect(found).toBe(input);
    });

    it('should find quiz save button for incomplete topic', () => {
      component.sections = [
        {
          delete: false,
          name: 'Section',
          topics: [{ delete: false, name: 'Topic', validate: false }],
        },
      ] as any;
      const saveBtn = document.createElement('button');
      saveBtn.className = 'quiz-save-btn';
      document.body.appendChild(saveBtn);

      const found = (component as any).findFirstInvalidControl();

      expect(found).toBe(saveBtn);
    });

    it('should find topic collapse panel as last resort', () => {
      component.sections = [
        {
          delete: false,
          name: 'Section',
          topics: [{ delete: false, name: 'Topic', validate: false }],
        },
      ] as any;
      const panel = document.createElement('div');
      panel.className = 'topic-collapse-panel';
      document.body.appendChild(panel);

      const found = (component as any).findFirstInvalidControl();

      expect(found).toBe(panel);
    });

    it('should fall back to form element scroll when no control is found', fakeAsync(() => {
      component.sections = [
        {
          delete: false,
          name: 'Named',
          topics: [{ delete: false, name: 'Topic', validate: true }],
        },
      ] as any;
      const formEl = document.createElement('form');
      const scrollSpy = spyOn(formEl, 'scrollIntoView');
      component.formElement = { nativeElement: formEl } as any;

      (component as any).scrollToFirstInvalidField();
      tick(100);

      expect(scrollSpy).toHaveBeenCalled();
    }));

    it('should use scrollToFirstInvalidField when no invalid quiz question exists', () => {
      spyOn(component, 'sectionValidation').and.returnValue(false);
      spyOn(component as any, 'findFirstInvalidQuestion').and.returnValue(null);
      const scrollSpy = spyOn(component as any, 'scrollToFirstInvalidField');

      component.validateAndContinue();

      expect(scrollSpy).toHaveBeenCalled();
    });
  });

  describe('upload mapping and article document errors', () => {
    it('should map survey quiz topic from API response', () => {
      const section: any = { sectionId: 's1', topics: [] };
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({
          status: successCode,
          data: [
            {
              topicId: 't-survey',
              topicName: 'Feedback',
              topicType: 'Quiz',
              quizId: 'qz-s',
              quizTitle: 'Survey',
              durationInMinutes: 10,
              passingCriteria: 0,
              testType: 'SURVEY',
              randomQuestion: 1,
              quizQuestionAnswer: {
                quizQuestions: [
                  {
                    questionId: 'q1',
                    questionText: 'Rate us',
                    questionType: 'SINGLE_CHOICE',
                    quizAnswers: [
                      {
                        answerId: 'a1',
                        answerText: 'Good',
                        isCorrect: false,
                        answerImageUrl: '',
                      },
                    ],
                  },
                ],
              },
            },
          ],
        }),
      );

      component.uploadTopicsData(section);

      expect(section.topics[0].quiz.type).toBe('SURVEY');
      expect(section.topics[0].quiz.questions[0].surveyAnswers.length).toBeGreaterThan(
        0,
      );
    });

    it('should set switchValue from section free flag for non-premium courses', () => {
      component.sections = [];
      component.courseInformationData = fb.group({
        courseProgress: [0],
        courseType: [CourseType.FREE],
      });
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({ status: successCode, data: [] }),
      );

      component.uploadSectionData(
        [{ sectionId: 's1', sectionName: 'Free Sec', free: true, delete: false }],
      );

      expect(component.sections[0].switchValue).toBeTrue();
    });

    it('should force switchValue false for premium courses', () => {
      component.sections = [];
      component.courseInformationData = fb.group({
        courseProgress: [0],
        courseType: [CourseType.PREMIUM],
      });
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({ status: successCode, data: [] }),
      );

      component.uploadSectionData(
        [{ sectionId: 's1', sectionName: 'Premium Sec', free: true, delete: false }],
      );

      expect(component.sections[0].switchValue).toBeFalse();
    });

    it('should invoke afterLoaded when patchSectionData API fails', () => {
      component.courseId = 'course-1';
      const afterLoaded = jasmine.createSpy('afterLoaded');
      spyOn(component, 'addSection');
      courseServiceSpy.getSectionByCourseId.and.returnValue(
        throwError(() => new Error('Fetch failed')),
      );

      component.patchSectionData(afterLoaded);

      expect(component.addSection).toHaveBeenCalled();
      expect(afterLoaded).toHaveBeenCalled();
    });

    it('should reset article upload on custom request error', () => {
      const topic: any = {
        validate: false,
        article: {
          articleProgressBar: true,
          articleFileName: 'reading.pdf',
          content: '',
        },
      };
      (component as any).uploadedArticleDocuments.set('reading.pdf', topic);
      fileManagerSpy.uploadFile.and.returnValue(
        throwError(() => new Error('Upload failed')),
      );
      spyOn(component, 'articleValidation');

      component.customRequestArticleDocument({
        file: new File(['d'], 'reading.pdf', { type: 'application/pdf' }),
      } as any);

      expect(topic.article.articleFileName).toBe('');
      expect(topic.article.articleProgressBar).toBeFalse();
      expect(component.articleValidation).toHaveBeenCalledWith(topic);
    });

    it('should reject non-pdf article document upload', () => {
      const topic: any = {
        article: {
          articleDocumnetUrl: '',
          articleProgressBar: false,
          articleFileName: 'Add Resource',
        },
      };

      component.handleArticleDocumentChange(
        {
          file: { name: 'notes.docx', type: 'application/msword' },
        } as any,
        topic,
      );

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Please upload a pdf file.',
      );
    });

    it('should revalidate quiz after reordering questions', () => {
      const topic = createValidQuizTopic();
      const questions = topic.quiz.questions;
      spyOn(component, 'quizValidation');

      component.dropQuizQuestion(
        { previousIndex: 0, currentIndex: 0 } as any,
        topic,
        questions,
      );

      expect(component.quizValidation).toHaveBeenCalledWith(topic);
    });
  });

  describe('media upload errors and survey options', () => {
    it('should handle question image upload failure', () => {
      const topic = createValidQuizTopic();
      const question: any = {};
      const file = new File(['img'], 'q.png', { type: 'image/png' });
      const input = document.createElement('input');
      Object.defineProperty(input, 'files', { value: [file] });
      fileManagerSpy.uploadFile.and.returnValue(
        throwError(() => new Error('Upload failed')),
      );

      component.onQuestionImageSelected(topic, question, { target: input } as any);

      expect(messageServiceSpy.error).toHaveBeenCalledWith('Failed to upload image.');
      expect(component.quizImageUploading).toBeFalse();
    });

    it('should handle question media upload failure', () => {
      const topic = createValidQuizTopic();
      const question: any = {};
      const file = new File(['img'], 'q.png', { type: 'image/png' });
      const input = document.createElement('input');
      Object.defineProperty(input, 'files', { value: [file] });
      fileManagerSpy.uploadFile.and.returnValue(
        throwError(() => new Error('Upload failed')),
      );

      component.onQuestionMediaSelected(topic, question, { target: input } as any);

      expect(messageServiceSpy.error).toHaveBeenCalledWith('Failed to upload file.');
      expect(component.quizImageUploading).toBeFalse();
    });

    it('should handle answer image upload failure', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      const answer: any = {};
      const file = new File(['img'], 'a.png', { type: 'image/png' });
      const input = document.createElement('input');
      Object.defineProperty(input, 'files', { value: [file] });
      fileManagerSpy.uploadFile.and.returnValue(
        throwError(() => new Error('Upload failed')),
      );

      component.onAnswerImageSelected(
        topic,
        question,
        answer,
        { target: input } as any,
      );

      expect(messageServiceSpy.error).toHaveBeenCalledWith('Failed to upload image.');
      expect(component.quizImageUploading).toBeFalse();
    });

    it('should preserve answerId when regenerating survey options', () => {
      const options = component.generateSurveyOptions(3, [
        { answerId: 42, answerText: 'Custom', delete: false },
      ]);

      expect(options[0].answerId).toBe(42);
      expect(options.length).toBe(3);
    });

    it('should map single-choice API question to UI question', () => {
      const mapped = (component as any).mapApiQuestionToUiQuestion(
        {
          questionType: 'SINGLE_CHOICE',
          questionText: 'Pick one',
          questionImageUrl: '',
          answers: [
            { answerText: 'A', isCorrectAnswer: true, answerImageUrl: '' },
            { answerText: 'B', isCorrectAnswer: false, answerImageUrl: '' },
          ],
        },
        0,
      );

      expect(mapped.questionType.key).toBe(QuestionType.SINGLE_CHOICE);
      expect(mapped.answers[0].isCorrectAnswer).toBeTrue();
      expect(mapped.answers[1].isCorrectAnswer).toBeFalse();
    });

    it('should skip delete flags when previous content has no ids', () => {
      const video: any = {
        videoData: { videoId: '', delete: false },
        documentData: { documents: [] },
      };
      const quiz: any = { quizId: '', delete: false };
      const article: any = { articleId: '', delete: false };

      component.deletePreviousTopicVideo(video);
      component.deletePreviousTopicQuiz(quiz);
      component.deletePreviousTopicArticle(article);

      expect(video.videoData.delete).toBeFalse();
      expect(quiz.delete).toBeFalse();
      expect(article.delete).toBeFalse();
    });
  });

  describe('Phase 1 batch 6: ngOnInit sectionsData hydration', () => {
    it('should remap quiz question types from sectionsData on init', () => {
      component.sectionsData = [
        {
          topics: [
            {
              quiz: {
                type: QuizType.BASIC_QUIZ,
                questions: [
                  {
                    questionType: { key: QuestionType.SINGLE_CHOICE },
                  },
                ],
              },
            },
          ],
        },
      ] as any;
      const hideSpy = spyOn(component, 'hideDeleteSectionContainer');

      component.ngOnInit();

      expect(
        component.sections[0].topics[0].quiz.questions[0].questionType.key,
      ).toBe(QuestionType.SINGLE_CHOICE);
      expect(
        component.sections[0].topics[0].quiz.questions[0].questionType.value,
      ).toBe('Single choice');
      expect(hideSpy).toHaveBeenCalled();
    });
  });

  describe('Phase 1 batch 6: bulk quiz import depth', () => {
    it('should initialize quiz questions array when missing on bulk import', fakeAsync(() => {
      const afterClose = new Subject<any>();
      modalSpy.create.and.returnValue({
        afterClose: afterClose.asObservable(),
      } as any);
      const topic: any = { quiz: {} };

      component.openBulkQuizUploaderModal(topic);
      afterClose.next({
        fileName: 'bulk.csv',
        questions: [
          {
            questionType: 'MULTIPLE_CHOICE',
            questionText: 'First import?',
            answers: [
              { answerText: 'Yes', isCorrectAnswer: true },
              { answerText: 'No', isCorrectAnswer: false },
            ],
          },
        ],
      });
      tick();
      tick();

      expect(topic.quiz.questions.length).toBeGreaterThan(0);
    }));

    it('should filter removable empty rows and preserve edited questions', fakeAsync(() => {
      const afterClose = new Subject<any>();
      modalSpy.create.and.returnValue({
        afterClose: afterClose.asObservable(),
      } as any);
      const topic = createValidQuizTopic();
      topic.quiz.questions = [
        {
          delete: false,
          ques: 'Keep me',
          questionType: { key: QuestionType.MULTIPLE_CHOICE },
          answers: [],
          questionId: '',
        },
        {
          delete: false,
          ques: '',
          questionType: { key: QuestionType.MULTIPLE_CHOICE },
          answers: [],
          questionId: '',
        },
      ];

      component.openBulkQuizUploaderModal(topic);
      afterClose.next({
        fileName: 'bulk.csv',
        questions: [
          {
            questionType: 'MULTIPLE_CHOICE',
            questionText: 'Imported only?',
            answers: [
              { answerText: 'A', isCorrectAnswer: true },
              { answerText: 'B', isCorrectAnswer: false },
            ],
          },
        ],
      });
      tick();
      tick();

      expect(
        topic.quiz.questions.some((q: any) => q.ques === 'Keep me'),
      ).toBeTrue();
      expect(
        topic.quiz.questions.some((q: any) => q.ques === 'Imported only?'),
      ).toBeTrue();
    }));

    it('should import large question sets in multiple chunks', fakeAsync(() => {
      const afterClose = new Subject<any>();
      modalSpy.create.and.returnValue({
        afterClose: afterClose.asObservable(),
      } as any);
      const topic = createValidQuizTopic();
      topic.quiz.questions = [];
      const imported = Array.from({ length: 26 }, (_, i) => ({
        questionType: 'MULTIPLE_CHOICE',
        questionText: `Imported Q${i + 1}`,
        answers: [
          { answerText: 'A', isCorrectAnswer: true },
          { answerText: 'B', isCorrectAnswer: false },
        ],
      }));

      component.openBulkQuizUploaderModal(topic);
      afterClose.next({ fileName: 'big.csv', questions: imported });
      tick();
      tick();
      flush();

      expect(
        topic.quiz.questions.filter((q: any) => !q.delete).length,
      ).toBe(26);
      expect(messageServiceSpy.success).toHaveBeenCalledWith(
        'Imported 26 question(s).',
      );
    }));

    it('should no-op applyBulkImportedQuestions for empty question list', () => {
      const topic = createValidQuizTopic();
      const initialLength = topic.quiz.questions.length;

      (component as any).applyBulkImportedQuestions(topic, [], 'empty.csv');

      expect(topic.quiz.questions.length).toBe(initialLength);
      expect(messageServiceSpy.loading).not.toHaveBeenCalled();
    });

    it('should reject removable empty questions with attachments or answers', () => {
      const isEmpty = (component as any).isRemovableEmptyQuizQuestion.bind(
        component,
      );

      expect(
        isEmpty({
          delete: false,
          ques: '',
          attachedImageUrl: 'https://cdn/q.png',
          answers: [],
        }),
      ).toBeFalse();
      expect(
        isEmpty({
          delete: false,
          ques: '',
          explanation: 'hint',
          answers: [],
        }),
      ).toBeFalse();
      expect(
        isEmpty({
          delete: false,
          ques: '',
          answers: [{ delete: false, isCorrectAnswer: true, ans: '' }],
        }),
      ).toBeFalse();
      expect(
        isEmpty({
          delete: false,
          ques: '',
          answers: [{ delete: false, isCorrectAnswer: false, ans: 'typed' }],
        }),
      ).toBeFalse();
    });

    it('should no-op syncRandomQuestionAfterImport without quiz object', () => {
      const topic: any = {};

      (component as any).syncRandomQuestionAfterImport(topic);

      expect(topic.quiz).toBeUndefined();
    });
  });

  describe('Phase 1 batch 6: quiz media and option guards', () => {
    it('should return early when question image input has no files', () => {
      const topic = createValidQuizTopic();
      const question: any = {};
      const input = document.createElement('input');
      Object.defineProperty(input, 'files', { value: [] });

      component.onQuestionImageSelected(topic, question, { target: input } as any);

      expect(fileManagerSpy.uploadFile).not.toHaveBeenCalled();
    });

    it('should return early when question media input has no files', () => {
      const topic = createValidQuizTopic();
      const question: any = {};
      const input = document.createElement('input');
      Object.defineProperty(input, 'files', { value: [] });

      component.onQuestionMediaSelected(topic, question, { target: input } as any);

      expect(fileManagerSpy.uploadFile).not.toHaveBeenCalled();
    });

    it('should upload image media and set attachedImageUrl', () => {
      const topic = createValidQuizTopic();
      const question: any = { attachedImageUrl: null };
      const file = new File(['img'], 'q.png', { type: 'image/png' });
      const input = document.createElement('input');
      Object.defineProperty(input, 'files', { value: [file] });
      spyOn(component, 'onQuizInputChange');

      component.onQuestionMediaSelected(topic, question, { target: input } as any);

      expect(question.attachedImageUrl).toBe('https://cdn/img.png');
      expect(component.mediaType).toBe('image');
      expect(component.onQuizInputChange).toHaveBeenCalledWith(topic, question);
    });

    it('should return early when answer image input has no files', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      const answer: any = {};
      const input = document.createElement('input');
      Object.defineProperty(input, 'files', { value: [] });

      component.onAnswerImageSelected(
        topic,
        question,
        answer,
        { target: input } as any,
      );

      expect(fileManagerSpy.uploadFile).not.toHaveBeenCalled();
    });

    it('should reject non-image answer uploads', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      const answer: any = {};
      const file = new File(['txt'], 'a.txt', { type: 'text/plain' });
      const input = document.createElement('input');
      Object.defineProperty(input, 'files', { value: [file] });

      component.onAnswerImageSelected(
        topic,
        question,
        answer,
        { target: input } as any,
      );

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Please upload an image file.',
      );
      expect(fileManagerSpy.uploadFile).not.toHaveBeenCalled();
    });

    it('should block removing question image when options still have images', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.attachedImageUrl = 'https://cdn/q.png';
      question.answers[0].attachedImageUrl = 'https://cdn/opt.png';
      spyOn(component, 'onQuizInputChange');

      component.removeQuestionImage(topic, question);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Please remove the attached image from options first.',
      );
      expect(component.onQuizInputChange).not.toHaveBeenCalled();
    });

    it('should clear duplicate flag when quiz option text is unique', () => {
      const question: any = {
        answers: [
          { delete: false, ans: 'Alpha', exist: true },
          { delete: false, ans: 'Beta', exist: false },
        ],
      };

      component.quizOptions(question, 1);

      expect(question.answers[1].exist).toBeFalse();
      expect(messageServiceSpy.error).not.toHaveBeenCalled();
    });
  });

  describe('Phase 1 batch 6: quiz validation and input branches', () => {
    it('should flag text-field questions with more than one answer', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.questionType = { key: QuestionType.TEXT_FIELD };
      question.answers = [
        { delete: false, ans: 'one', exist: false },
        { delete: false, ans: 'two', exist: false },
      ];

      component.quizValidation(topic);

      expect(topic.validate).toBeFalse();
      expect(topic.quiz.invalidQuestions).toContain(0);
    });

    it('should set validation error when quiz has no active questions', () => {
      const topic = createValidQuizTopic();
      topic.quiz.questions = [{ delete: true, ques: '', answers: [] }];

      component.quizValidation(topic);

      expect(topic.quiz.validationError).toBe('At least one question is required');
    });

    it('should remove fixed question from invalidQuestions on input change', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.ques = '';
      component.quizValidation(topic);
      expect(topic.quiz.invalidQuestions).toContain(0);

      question.ques = 'Now filled';
      component.onQuizInputChange(topic, question);

      expect(topic.quiz.invalidQuestions).not.toContain(0);
    });

    it('should add question and sync random count for all-questions mode', () => {
      const topic = createValidQuizTopic();
      topic.quiz.randomQuestionType = component.randomQuestionAll;
      topic.quiz.questions = [topic.quiz.questions[0]];
      spyOn(component, 'onSelectRandomQuestionType');

      component.addQuestion(topic, topic.quiz.questions);

      expect(topic.quiz.questions.length).toBe(2);
      expect(component.onSelectRandomQuestionType).toHaveBeenCalledWith(topic);
    });

    it('should create topics with survey question type when section has survey', () => {
      const section: any = {
        generateTopicsPrompt: false,
        topics: [
          {
            quiz: { type: QuizType.SURVEY },
          },
        ],
      };

      component.createTopics(section);

      const newTopic = section.topics[section.topics.length - 1];
      expect(newTopic.quiz.questions[0].questionType.key).toBe(
        QuestionType.SINGLE_CHOICE,
      );
    });
  });

  describe('Phase 1 batch 6: section validation and error aggregation', () => {
    it('should fail section validation for incomplete non-survey topics', () => {
      component.sections = [
        {
          delete: false,
          deleteAll: false,
          name: 'Section 1',
          topics: [
            {
              delete: false,
              name: 'Incomplete quiz',
              validate: false,
              quiz: { type: QuizType.BASIC_QUIZ, questions: [] },
            },
          ],
        },
      ] as any;

      expect(component.sectionValidation()).toBeFalse();
    });

    it('should include empty-section topic count in validation errors', () => {
      component.sections = [
        { delete: false, name: 'Named', topics: [] },
      ] as any;

      (component as any).showValidationErrors();

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        jasmine.stringMatching(/section\(s\) need at least one topic/),
      );
    });

    it('should include video topics missing URLs in validation errors', () => {
      component.sections = [
        {
          delete: false,
          name: 'Section',
          topics: [
            {
              delete: false,
              name: 'Video topic',
              validate: false,
              selectedContentType: component.typeVideo,
              video: {
                videoData: { videoUrl: '', youtubeVideoUrl: '' },
              },
            },
          ],
        },
      ] as any;

      (component as any).showValidationErrors();

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        jasmine.stringMatching(/video topic\(s\) need a video file or YouTube URL/),
      );
    });
  });

  describe('Phase 1 batch 6: topic deletion and question guards', () => {
    it('should hide delete topic icon when no topics are checked', () => {
      const section: any = {
        topics: [{ checkTopic: false }, { checkTopic: false }],
        deleteTopicIcon: true,
      };

      component.deleteTopicIcon(section);

      expect(section.deleteTopicIcon).toBeFalse();
    });

    it('should delete checked topics after modal confirmation', () => {
      const deleteClick = new Subject<void>();
      modalSpy.create.and.returnValue({
        componentInstance: { deleteClick },
      } as any);
      const section: any = {
        topics: [
          { checkTopic: true, delete: false, level: 1 },
          { checkTopic: false, delete: false, level: 2 },
        ],
        deleteTopicIcon: true,
        checkAll: false,
        deleteAll: false,
      };

      component.deleteTopics(section);
      deleteClick.next();

      expect(section.topics[0].delete).toBeTrue();
      expect(section.deleteTopicIcon).toBeFalse();
      expect(section.topics[1].level).toBe(1);
    });

    it('should block removing the last quiz question', () => {
      const topic = createValidQuizTopic();

      component.removeQuizQuestion(topic, topic.quiz.questions[0]);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'At least one question is required in the quiz.',
      );
      expect(topic.quiz.questions[0].delete).toBeFalse();
    });

    it('should clear correct-answer flags when changing question type', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.questionType = { key: QuestionType.MULTIPLE_CHOICE };
      question.answers[0].isCorrectAnswer = true;
      spyOn(component, 'addOption');

      component.changeQuestionType(topic, question);

      expect(question.answers[0].isCorrectAnswer).toBeFalse();
    });
  });

  describe('Phase 1 batch 6: video document and article uploads', () => {
    it('should hide video table after deleteVideoData clears content', () => {
      spyOn(component, 'deleteVideoOrDocument');
      const topic: any = {
        topicId: 't1',
        video: {
          showTable: true,
          videoData: {
            videoId: 'v1',
            videoUrl: 'https://cdn/v.mp4',
            videoFileName: 'v.mp4',
          },
          documentData: {
            documents: [],
            documentFileName: 'Add Resource',
            documentBtnName: 'Upload File',
          },
        },
      };

      component.deleteVideoData(topic);

      expect(topic.video.showTable).toBeFalse();
    });

    it('should return null from customRequestVideo for invalid filenames', () => {
      const result = component.customRequestVideo({
        file: new File(['v'], 'bad#name.mp4', { type: 'video/mp4' }),
      } as any);

      expect(result).toBeNull();
      expect(fileManagerSpy.uploadFile).not.toHaveBeenCalled();
    });

    it('should delete existing video before staging a replacement upload', () => {
      const topic: any = {
        validate: true,
        video: {
          showTable: true,
          videoData: {
            videoFileType: 'video',
            videoProgress: 100,
            videoFileName: 'old.mp4',
            delete: false,
          },
          documentData: { documents: [] },
        },
      };
      const deleteSpy = spyOn(component, 'deleteVideoData');

      component.handleVideoChange(
        {
          file: {
            name: 'new-lecture.mp4',
            type: 'video/mp4',
            size: 1024,
            originFileObj: new File(['v'], 'new-lecture.mp4', {
              type: 'video/mp4',
            }),
          },
        } as any,
        topic,
      );

      expect(deleteSpy).toHaveBeenCalledWith(topic);
      expect(topic.video.videoData.videoFileName).toBe('new-lecture.mp4');
    });

    it('should resolve video duration from loaded metadata', async () => {
      const file = new File(['v'], 'clip.mp4', { type: 'video/mp4' });
      const listeners: Record<string, () => void> = {};
      const videoStub = {
        duration: 95,
        src: '',
        addEventListener: (event: string, cb: () => void) => {
          listeners[event] = cb;
        },
      };
      spyOn(document, 'createElement').and.returnValue(videoStub as any);
      spyOn(URL, 'createObjectURL').and.returnValue('blob:clip');

      const durationPromise = component.getVideoDuration(file);
      listeners['loadedmetadata']();
      const duration = await durationPromise;

      expect(duration).toBe(95);
    });

    it('should reset document fields on custom document upload error', () => {
      const topic: any = {
        validate: false,
        video: {
          documentData: {
            documents: [
              {
                documentKey: 'slides.pdf',
                documentProgress: 50,
                documentFileName: 'slides.pdf',
                documentFileType: 'pdf',
                date: '01/01/2026',
              },
            ],
          },
        },
      };
      (component as any).uploadedDocuments.set('slides.pdf', topic);
      fileManagerSpy.uploadFile.and.returnValue(
        throwError(() => new Error('Upload failed')),
      );

      component.customRequestDocument({
        file: new File(['d'], 'slides.pdf', { type: 'application/pdf' }),
      } as any);

      const doc = topic.video.documentData.documents[0];
      expect(doc.documentProgress).toBe(0);
      expect(doc.documentFileName).toBe('');
      expect(doc.documentKey).toBe('');
    });

    it('should delete existing article document before replacing upload', () => {
      const topic: any = {
        article: {
          articleDocumnetUrl: 'https://cdn/old.pdf',
          articleProgressBar: false,
          articleFileName: 'old.pdf',
        },
      };
      const deleteSpy = spyOn(component, 'deleteArticleDocument');

      component.handleArticleDocumentChange(
        {
          file: { name: 'new-reading.pdf', type: 'application/pdf' },
        } as any,
        topic,
      );

      expect(deleteSpy).toHaveBeenCalledWith(topic);
      expect(topic.article.articleFileName).toBe('new-reading.pdf');
      expect(topic.article.articleProgressBar).toBeTrue();
    });
  });

  describe('Phase 1 batch 6: saveQuiz and scroll fallbacks', () => {
    function quizTopicForSave(): any {
      const topic = createValidQuizTopic();
      topic.name = 'Quiz Topic';
      topic.active = false;
      return topic;
    }

    it('should error when text-field has more than one answer on save', () => {
      const topic = quizTopicForSave();
      const question = topic.quiz.questions[0];
      question.questionType = {
        key: QuestionType.TEXT_FIELD,
        value: 'Text field',
      };
      question.answers = [
        { delete: false, ans: 'one', isCorrectAnswer: false, exist: false },
        { delete: false, ans: 'two', isCorrectAnswer: false, exist: false },
      ];

      component.saveQuiz(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Question 1: Text field questions should have exactly one answer',
      );
    });

    it('should show generic save error when validation details are unavailable', () => {
      const topic = quizTopicForSave();
      spyOn(component, 'quizValidation').and.callFake((quizTopic: any) => {
        quizTopic.validate = false;
        quizTopic.quiz.validationError = undefined;
        quizTopic.quiz.invalidQuestions = [0];
      });

      component.saveQuiz(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Question 1: Please complete all required fields',
      );
    });

    it('should require quiz title when saving non-test course quiz', () => {
      const topic = quizTopicForSave();
      topic.quiz.title = '   ';
      component.selectedContentType = CourseContentType.COURSE;
      spyOn(component, 'quizValidation').and.callFake((quizTopic: any) => {
        quizTopic.validate = false;
        quizTopic.quiz.validationError = undefined;
        quizTopic.quiz.invalidQuestions = [];
      });

      component.saveQuiz(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Quiz title is required',
      );
    });

    it('should save survey quiz via fallback branch when invalidQuestions empty', () => {
      const topic = quizTopicForSave();
      topic.quiz.type = QuizType.SURVEY;
      topic.quiz.passingCriteria = 0;
      topic.quiz.title = 'Survey';
      topic.active = true;

      component.saveQuiz(topic);

      expect(messageServiceSpy.success).toHaveBeenCalledWith(
        'Quiz saved successfully!',
      );
      expect(topic.active).toBeFalse();
    });

    it('should fall back to scrollToTopic when question element is missing', fakeAsync(() => {
      const topic: any = { active: false, name: 'Missing Question' };
      component.sections = [{ active: false, topics: [] }] as any;
      const scrollSpy = spyOn(component as any, 'scrollToTopic');

      (component as any).scrollToInvalidQuestion(topic, 0, 0);
      tick(100);
      tick(300);

      expect(scrollSpy).toHaveBeenCalledWith(topic);
    }));
  });

  describe('Phase 1 batch 6: misc validators and article formatting', () => {
    it('should block non-numeric keys in validateRandomNumberInput', () => {
      const topic = createValidQuizTopic();
      const event = {
        key: 'a',
        which: 65,
        keyCode: 65,
        preventDefault: jasmine.createSpy('preventDefault'),
        target: { value: '' },
      } as any;

      component.validateRandomNumberInput(event, topic);

      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('should reset non-survey answers when topic type changes', () => {
      const topic = createValidQuizTopic();
      topic.quiz.type = QuizType.BASIC_QUIZ;
      topic.quiz.questions[0].ques = 'Old text';
      topic.quiz.questions[0].answers = [
        { ans: 'A', isCorrectAnswer: true, delete: false },
        { ans: 'B', isCorrectAnswer: false, delete: false },
      ];

      component.onSelectTopicType(topic);

      expect(topic.quiz.questions[0].ques).toBe('');
      expect(topic.quiz.questions[0].answers.length).toBe(1);
    });

    it('should close bullet list when article ends with list items', () => {
      instructorServiceSpy.generator.and.returnValue(
        of({
          status: successCode,
          data: '- Only bullet one\n- Only bullet two',
        }),
      );
      const article: any = {
        articlePromptInput: 'List only',
        articlePrompt: true,
        showChatBox: true,
        showSpinner: false,
        questionAnswers: { question: '', answers: [] },
        content: '',
      };

      component.generateArticles(article);

      expect(article.content).toBe(
        '<ul><li>Only bullet one</li><li>Only bullet two</li></ul>',
      );
    });

    it('should warn when uploading empty YouTube URL', () => {
      const warnSpy = spyOn(console, 'warn');
      const topic: any = {
        video: { videoData: { youtubeVideoUrl: '' } },
      };

      component.youtubeVideoUrlUpload(topic);

      expect(warnSpy).toHaveBeenCalledWith('YouTube URL is empty');
      expect(courseServiceSpy.youtubeVideoUrlUpload).not.toHaveBeenCalled();
    });
  });

  describe('Phase 1 batch 7: quiz input and save error branches', () => {
    it('should remove question index from invalidQuestions after text is filled', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.ques = '';
      component.quizValidation(topic);
      expect(topic.quiz.invalidQuestions).toContain(0);

      question.ques = 'Now valid question text';
      component.onQuizInputChange(topic, question);

      expect(topic.quiz.invalidQuestions).not.toContain(0);
    });

    it('should error on save when single-choice option text is empty', () => {
      const topic = createValidQuizTopic();
      topic.name = 'Quiz Topic';
      const question = topic.quiz.questions[0];
      question.questionType = {
        key: QuestionType.SINGLE_CHOICE,
        value: 'Single choice',
      };
      question.answers = [
        { delete: false, ans: 'A', isCorrectAnswer: true, exist: false },
        { delete: false, ans: '   ', isCorrectAnswer: false, exist: false },
      ];
      spyOn(component, 'quizValidation').and.callFake((quizTopic: any) => {
        quizTopic.validate = false;
        quizTopic.quiz.invalidQuestions = [0];
      });

      component.saveQuiz(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Question 1: All options must have text',
      );
    });

    it('should error on save when multiple-choice has only one option', () => {
      const topic = createValidQuizTopic();
      topic.name = 'Quiz Topic';
      const question = topic.quiz.questions[0];
      question.answers = [
        { delete: false, ans: 'Only', isCorrectAnswer: true, exist: false },
      ];
      spyOn(component, 'quizValidation').and.callFake((quizTopic: any) => {
        quizTopic.validate = false;
        quizTopic.quiz.invalidQuestions = [0];
      });

      component.saveQuiz(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Question 1: Multiple choice questions require at least two options',
      );
    });

    it('should error on save for missing duration when invalidQuestions is empty', () => {
      const topic = createValidQuizTopic();
      topic.name = 'Quiz Topic';
      topic.quiz.durationInMinutes = null;
      spyOn(component, 'quizValidation').and.callFake((quizTopic: any) => {
        quizTopic.validate = false;
        quizTopic.quiz.invalidQuestions = [];
        quizTopic.quiz.validationError = undefined;
      });

      component.saveQuiz(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Quiz duration is required',
      );
    });

    it('should flag quiz answers marked as duplicate via exist flag', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.answers[1].exist = true;

      component.quizValidation(topic);

      expect(topic.validate).toBeFalse();
      expect(topic.quiz.invalidQuestions).toContain(0);
    });

    it('should clear exist flag when option text is unique', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.answers[1].ans = 'Unique option';
      question.answers[1].exist = true;

      component.quizOptions(question, 1);

      expect(question.answers[1].exist).toBeFalse();
      expect(messageServiceSpy.error).not.toHaveBeenCalledWith(
        'Option already exist in this question',
      );
    });

    it('should reduce randomQuestion count when removing a question at the cap', () => {
      const topic = createValidQuizTopic();
      topic.quiz.randomQuestion = 2;
      topic.quiz.questions.push({
        delete: false,
        ques: 'Second question',
        questionType: { key: QuestionType.MULTIPLE_CHOICE },
        answers: [
          { delete: false, ans: 'A', isCorrectAnswer: true, exist: false },
          { delete: false, ans: 'B', isCorrectAnswer: false, exist: false },
        ],
      });

      component.removeQuizQuestion(topic, topic.quiz.questions[1]);

      expect(topic.quiz.questions[1].delete).toBeTrue();
      expect(topic.quiz.randomQuestion).toBe(1);
    });

    it('should add survey question type when adding to survey quiz', () => {
      const topic = createValidQuizTopic();
      topic.quiz.type = QuizType.SURVEY;
      const questions = topic.quiz.questions;

      component.addQuestion(topic, questions);

      const added = questions[questions.length - 1];
      expect(added.questionType.key).toBe(QuestionType.SINGLE_CHOICE);
      expect(added.surveyAnswers.length).toBe(5);
    });

    it('should keep custom random count when not using all-questions mode', () => {
      const topic = createValidQuizTopic();
      topic.quiz.randomQuestionType = component.randomQuestionCustom;
      topic.quiz.randomQuestion = 1;

      component.onSelectRandomQuestionType(topic);

      expect(topic.quiz.randomQuestion).toBe(1);
    });
  });

  describe('Phase 1 batch 7: upload request guards', () => {
    it('should return null from customRequestVideo for non-mp4 mime type', () => {
      const result = component.customRequestVideo({
        file: new File(['x'], 'notes.txt', { type: 'text/plain' }),
      } as any);

      expect(result).toBeNull();
      expect(fileManagerSpy.uploadFile).not.toHaveBeenCalled();
    });

    it('should return null from customRequestDocument for non-pdf files', () => {
      const result = component.customRequestDocument({
        file: new File(['d'], 'slides.docx', {
          type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        }),
      } as any);

      expect(result).toBeNull();
    });

    it('should return null from customRequestDocument for invalid filenames', () => {
      const result = component.customRequestDocument({
        file: new File(['d'], 'bad#name.pdf', { type: 'application/pdf' }),
      } as any);

      expect(result).toBeNull();
    });

    it('should return null from customRequestArticleDocument for non-pdf files', () => {
      const result = component.customRequestArticleDocument({
        file: new File(['d'], 'notes.docx', {
          type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        }),
      } as any);

      expect(result).toBeNull();
    });

    it('should return null from customRequestArticleDocument for invalid filenames', () => {
      const result = component.customRequestArticleDocument({
        file: new File(['d'], 'bad#name.pdf', { type: 'application/pdf' }),
      } as any);

      expect(result).toBeNull();
    });

    it('should reject article document upload with special characters in filename', () => {
      const topic: any = {
        article: {
          articleDocumnetUrl: '',
          articleProgressBar: false,
          articleFileName: 'Add Resource',
        },
      };

      component.handleArticleDocumentChange(
        {
          file: { name: 'bad#reading.pdf', type: 'application/pdf' },
        } as any,
        topic,
      );

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'File name contains special characters.',
      );
    });

    it('should populate transcript fields on successful custom video upload', () => {
      const topic: any = {
        validate: false,
        video: {
          videoData: {
            videoProgress: 1,
            videoFileName: 'lecture.mp4',
            videoBtnName: 'Upload File',
            videoFileType: 'video',
          },
        },
      };
      (component as any).uploadedVideos.set('lecture.mp4', topic);
      fileManagerSpy.uploadFile.and.returnValue(
        of({
          data: {
            url: 'https://cdn/lecture.mp4',
            transcriptData: {
              transcript: 'Hello world',
              vttContent: 'WEBVTT',
              summary: 'Short summary',
            },
          },
        }),
      );
      spyOn(component, 'checkFileInProcess');
      spyOn(component, 'videoValidation');

      component.customRequestVideo({
        file: new File(['v'], 'lecture.mp4', { type: 'video/mp4' }),
      } as any);

      expect(topic.video.videoData.videoTranscript).toBe('Hello world');
      expect(topic.video.videoData.videoSubtitles).toBe('WEBVTT');
      expect(topic.video.videoData.videoSummary).toBe('Short summary');
      expect(component.checkFileInProcess).toHaveBeenCalledWith(topic);
    });

    it('should reject getVideoDuration when metadata load fails', async () => {
      const file = new File(['v'], 'broken.mp4', { type: 'video/mp4' });
      const listeners: Record<string, (event?: any) => void> = {};
      const videoStub = {
        duration: 0,
        src: '',
        addEventListener: (event: string, cb: (event?: any) => void) => {
          listeners[event] = cb;
        },
      };
      spyOn(document, 'createElement').and.returnValue(videoStub as any);
      spyOn(URL, 'createObjectURL').and.returnValue('blob:broken');

      const durationPromise = component.getVideoDuration(file);
      listeners['error']?.(new Event('error'));

      await expectAsync(durationPromise).toBeRejected();
    });

    it('should ignore getVideoDuration rejection during handleVideoChange', async () => {
      const topic: any = {
        validate: true,
        video: {
          fileProcessing: false,
          showTable: false,
          videoData: {
            videoFileType: '',
            videoProgress: 0,
            videoFileName: '',
            videoBtnName: 'Upload File',
            delete: false,
          },
          documentData: { documents: [], documentFileName: 'Add Resource' },
        },
      };
      spyOn(component, 'getVideoDuration').and.returnValue(
        Promise.reject(new Error('duration failed')),
      );

      component.handleVideoChange(
        {
          file: {
            name: 'clip.mp4',
            type: 'video/mp4',
            size: 1024,
            originFileObj: new File(['v'], 'clip.mp4', { type: 'video/mp4' }),
          },
        } as any,
        topic,
      );

      await fixture.whenStable();
      expect(topic.video.videoData.videoFileName).toBe('clip.mp4');
    });

    it('should clear existing uploaded video before YouTube URL upload', () => {
      const deleteSpy = spyOn(component, 'deleteVideoData');
      courseServiceSpy.youtubeVideoUrlUpload.and.returnValue(of({ data: 120 }));
      const topic: any = {
        validate: false,
        topicDuration: 0,
        video: {
          fileProcessing: false,
          showTable: true,
          videoData: {
            youtubeVideoUrl: 'https://www.youtube.com/watch?v=abc123',
            videoFileType: 'video',
            videoProgress: 100,
            videoFileName: 'old.mp4',
            videoBtnName: 'Replace',
            delete: false,
          },
          documentData: { documents: [] },
        },
      };

      component.youtubeVideoUrlUpload(topic);

      expect(deleteSpy).toHaveBeenCalledWith(topic);
      expect(topic.video.videoData.videoFileName).toBe('YOUTUBE');
    });
  });

  describe('Phase 1 batch 7: topic deletion and track-by fallbacks', () => {
    it('should no-op deleteTopics when section is undefined', () => {
      expect(() => component.deleteTopics(undefined)).not.toThrow();
      expect(modalSpy.create).not.toHaveBeenCalled();
    });

    it('should uncheck all topics via allTopicCheck', () => {
      const section: any = {
        topics: [{ checkTopic: true }, { checkTopic: true }],
        deleteTopicIcon: true,
      };

      component.allTopicCheck(section, { target: { checked: false } });

      expect(section.topics.every((t: any) => !t.checkTopic)).toBeTrue();
      expect(section.deleteTopicIcon).toBeFalse();
    });

    it('should return index fallback from trackBy helpers without ids', () => {
      expect(component.trackByQuizQuestion(4, {})).toBe(4);
      expect(component.trackByQuizAnswer(7, {})).toBe(7);
    });

    it('should return early from toggleAudio when audio element is missing', () => {
      const question: any = {
        attachedImageUrl: 'https://cdn/missing.mp3',
        isPlaying: false,
      };

      component.toggleAudio(question);

      expect(question.isPlaying).toBeFalse();
    });

    it('should sync random question settings after bulk import on quiz topic', () => {
      const topic = createValidQuizTopic();
      topic.quiz.questions = [
        {
          delete: false,
          ques: 'Q1',
          questionType: { key: QuestionType.MULTIPLE_CHOICE },
          answers: [],
        },
      ];

      (component as any).syncRandomQuestionAfterImport(topic);

      expect(topic.quiz.randomQuestion).toBe(1);
      expect(topic.quiz.randomQuestionType).toBe(component.randomQuestionAll);
    });

    it('should hide video table after deleteVideoData resets upload state', () => {
      spyOn(component, 'deleteVideoOrDocument');
      const topic: any = {
        topicId: 't1',
        validate: true,
        video: {
          showTable: true,
          videoData: {
            videoId: 'v1',
            videoUrl: 'https://cdn/v.mp4',
            videoFileName: 'v.mp4',
          },
          documentData: {
            documents: [],
            documentFileName: 'Add Resource',
            documentBtnName: 'Upload File',
          },
        },
      };

      component.deleteVideoData(topic);

      expect(topic.video.showTable).toBeFalse();
      expect(topic.video.videoData.videoFileName).toBe('Add Video');
    });
  });

  describe('Phase 1 batch 7: API hydration and publish guards', () => {
    it('should map youtube video topics with YOUTUBE filename from API', () => {
      const section: any = { sectionId: 's1', topics: [] };
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({
          status: successCode,
          data: [
            {
              topicId: 't-yt',
              topicName: 'YouTube Lecture',
              topicType: 'Video',
              videoId: 'vid-yt',
              filename: 'YOUTUBE',
              videoUrl: 'https://www.youtube.com/watch?v=abc123',
              creationDate: '2026-03-01',
              docs: [],
            },
          ],
        }),
      );

      component.uploadTopicsData(section);

      expect(section.topics[0].video.videoData.videoFileName).toBe('YOUTUBE');
      expect(section.topics[0].video.videoData.youtubeVideoUrl).toBe(
        'https://www.youtube.com/watch?v=abc123',
      );
    });

    it('should map custom randomQuestionType for partial random quizzes', () => {
      const section: any = { sectionId: 's1', topics: [] };
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({
          status: successCode,
          data: [
            {
              topicId: 't-q2',
              topicName: 'Partial Quiz',
              topicType: 'Quiz',
              quizId: 'qz-2',
              quizTitle: 'Partial',
              durationInMinutes: 15,
              passingCriteria: 60,
              testType: 'TEST',
              randomQuestion: 1,
              quizQuestionAnswer: {
                quizQuestions: [
                  {
                    questionId: 'q1',
                    questionText: 'Q1',
                    questionType: 'MULTIPLE_CHOICE',
                    quizAnswers: [
                      { answerId: 'a1', answerText: 'A', isCorrect: true },
                      { answerId: 'a2', answerText: 'B', isCorrect: false },
                    ],
                  },
                  {
                    questionId: 'q2',
                    questionText: 'Q2',
                    questionType: 'MULTIPLE_CHOICE',
                    quizAnswers: [
                      { answerId: 'a3', answerText: 'C', isCorrect: true },
                      { answerId: 'a4', answerText: 'D', isCorrect: false },
                    ],
                  },
                ],
              },
            },
          ],
        }),
      );

      component.uploadTopicsData(section);

      expect(section.topics[0].quiz.randomQuestionType).toBe(
        component.randomQuestionCustom,
      );
    });

    it('should map article topics without attached documents', () => {
      const section: any = { sectionId: 's1', topics: [] };
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({
          status: successCode,
          data: [
            {
              topicId: 't-a2',
              topicName: 'Reading Only',
              topicType: 'Article',
              articleId: 'art-2',
              article: '<p>Text only</p>',
              docs: null,
            },
          ],
        }),
      );

      component.uploadTopicsData(section);

      expect(section.topics[0].article.articleFileName).toBe('Add Resource');
      expect(section.topics[0].article.articleDocumnetUrl).toBe('');
    });

    it('should invoke afterLoaded when uploadSectionData receives only deleted sections', () => {
      const afterLoaded = jasmine.createSpy('afterLoaded');

      component.uploadSectionData(
        [{ sectionId: 's1', sectionName: 'Deleted', delete: true }],
        afterLoaded,
      );

      expect(afterLoaded).toHaveBeenCalled();
      expect(courseServiceSpy.getTopicsBySectionId).not.toHaveBeenCalled();
    });

    it('should not upload sections when patchSectionData gets non-success status', () => {
      component.courseId = 'course-1';
      const uploadSpy = spyOn(component, 'uploadSectionData');
      courseServiceSpy.getSectionByCourseId.and.returnValue(
        of({ status: 400, data: [] }),
      );

      component.patchSectionData();

      expect(uploadSpy).not.toHaveBeenCalled();
    });

    it('should skip draft save when courseSaved flag is already true', () => {
      component.courseSaved = true;
      component.sections = createValidSections();
      component.courseInformationData = createCourseInformationForm(0);

      component.saveAsDraftCourse();

      expect(courseServiceSpy.createCourseDto).not.toHaveBeenCalled();
    });

    it('should use default publish error when API message is missing', () => {
      component.sections = createValidSections();
      component.courseInformationData = createCourseInformationForm(0);
      courseServiceSpy.createCourseDto.and.returnValue(
        of({ status: 500, message: '' }),
      );

      component.publishCourse('next');

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Failed to save course. Please try again.',
      );
    });

    it('should ignore generateTopics response when status is not success', () => {
      const section: any = {
        topicInput: 'Geometry',
        questionAnswers: { question: '', answers: [] },
        showSpinner: true,
        generateTopicsPrompt: false,
      };
      instructorServiceSpy.generator.and.returnValue(
        of({ status: 400, data: 'Topic A\nTopic B' }),
      );

      component.generateTopics(section);

      expect(section.showSpinner).toBeTrue();
      expect(section.questionAnswers.answers.length).toBe(0);
    });
  });

  describe('Phase 1 batch 7: validation UI and scroll targeting', () => {
    it('should skip deleted sections when counting video topics missing URLs', () => {
      component.sections = [
        {
          delete: true,
          name: 'Deleted Section',
          topics: [
            {
              delete: false,
              name: 'Ghost Video',
              selectedContentType: component.typeVideo,
              video: {
                videoData: { videoUrl: '', youtubeVideoUrl: '' },
              },
            },
          ],
        },
        {
          delete: false,
          name: 'Active Section',
          topics: [
            {
              delete: false,
              name: 'Real Video',
              selectedContentType: component.typeVideo,
              video: {
                videoData: { videoUrl: '', youtubeVideoUrl: '' },
              },
            },
          ],
        },
      ] as any;

      (component as any).showValidationErrors();

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        jasmine.stringMatching(/1 video topic\(s\) need a video file or YouTube URL/),
      );
    });

    it('should include empty section names in aggregated validation errors', () => {
      component.sections = [
        { delete: false, name: '   ', topics: [{ name: 'T', validate: true }] },
      ] as any;

      (component as any).showValidationErrors();

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        jasmine.stringMatching(/1 section\(s\) need names/),
      );
    });

    it('should fail section validation when topic name is null', () => {
      component.sections = [
        {
          delete: false,
          name: 'Section',
          topics: [
            {
              delete: false,
              name: null,
              validate: true,
              quiz: { type: QuizType.BASIC_QUIZ },
            },
          ],
        },
      ] as any;

      expect(component.sectionValidation()).toBeFalse();
    });

    it('should find section input via ng-reflect-model in findFirstInvalidControl', () => {
      component.sections = [
        {
          delete: false,
          name: '',
          topics: [{ delete: false, name: 'Topic', validate: true }],
        },
      ] as any;
      const input = document.createElement('input');
      input.setAttribute('ng-reflect-model', '');
      document.body.appendChild(input);

      const found = (component as any).findFirstInvalidControl();

      expect(found).toBe(input);
      document.body.removeChild(input);
    });

    it('should focus textarea in scrollToFirstInvalidField fallback', fakeAsync(() => {
      component.sections = [{ delete: false, name: '', topics: [] }] as any;
      const textarea = document.createElement('textarea');
      textarea.className = 'section-name-input';
      spyOn(textarea, 'scrollIntoView');
      spyOn(textarea, 'focus');
      document.body.appendChild(textarea);

      (component as any).scrollToFirstInvalidField();
      tick(100);

      expect(textarea.focus).toHaveBeenCalled();
      document.body.removeChild(textarea);
    }));

    it('should highlight invalid question input while scrolling', fakeAsync(() => {
      component.sections = [{ active: false, topics: [] }] as any;
      const topic: any = { active: false, level: 2, topicId: 't2', name: 'Quiz' };
      const questionEl = document.createElement('div');
      questionEl.className = 'question-outer-container';
      questionEl.setAttribute('data-topic-id', 't2');
      questionEl.setAttribute('data-question-index', '0');
      const input = document.createElement('input');
      input.setAttribute('nz-input', '');
      questionEl.appendChild(input);
      document.body.appendChild(questionEl);

      (component as any).scrollToInvalidQuestion(topic, 0, 0);
      tick(100);
      tick(300);

      expect(input.classList.contains('highlight-invalid')).toBeTrue();
      tick(3000);
      expect(input.classList.contains('highlight-invalid')).toBeFalse();
      document.body.removeChild(questionEl);
      flush();
    }));

    it('should mark section and topic fields touched during validateAndContinue', () => {
      component.sections = createValidSections();
      spyOn(component, 'sectionValidation').and.returnValue(true);
      const stepsSpy = spyOn(component, 'steps');

      component.validateAndContinue();

      expect(component.sections[0]._touched).toBeTrue();
      expect(component.sections[0].topics[0]._touched).toBeTrue();
      expect(stepsSpy).toHaveBeenCalledWith(2);
    });

    it('should reset quiz render limit when activating collapsed topic', () => {
      const topic: any = {
        active: false,
        _quizRenderLimit: 15,
        quiz: { questions: Array.from({ length: 12 }, () => ({ delete: false })) },
      };
      const event = {
        preventDefault: jasmine.createSpy('preventDefault'),
        stopPropagation: jasmine.createSpy('stopPropagation'),
      };

      component.topicActive(event, topic);

      expect(topic.active).toBeTrue();
      expect(topic._quizRenderLimit).toBe(5);
      expect(component.shouldRenderQuizQuestion(topic, 4)).toBeTrue();
      expect(component.shouldRenderQuizQuestion(topic, 5)).toBeFalse();
    });

    it('should scroll to article editor when opening upload screen with section context', fakeAsync(() => {
      const article: any = { uploadArticleDocument: false };
      const section: any = { level: 3 };
      const editor = document.createElement('div');
      editor.id = 'article-editor-3-1';
      spyOn(editor, 'scrollIntoView');
      document.body.appendChild(editor);

      component.openUploadArticleScreen(article, section, 1);
      tick(150);

      expect(article.uploadArticleDocument).toBeTrue();
      expect(editor.scrollIntoView).toHaveBeenCalled();
      document.body.removeChild(editor);
    }));
  });

  describe('Phase 1 batch 7: bulk import and question type helpers', () => {
    it('should import bulk questions with null fileName', fakeAsync(() => {
      const afterClose = new Subject<any>();
      modalSpy.create.and.returnValue({
        afterClose: afterClose.asObservable(),
      } as any);
      const topic = createValidQuizTopic();
      topic.quiz.questions = [];

      component.openBulkQuizUploaderModal(topic);
      afterClose.next({
        fileName: null,
        questions: [
          {
            questionType: 'MULTIPLE_CHOICE',
            questionText: 'No filename import?',
            answers: [
              { answerText: 'Yes', isCorrectAnswer: true },
              { answerText: 'No', isCorrectAnswer: false },
            ],
          },
        ],
      });
      tick();
      tick();

      expect(topic.bulkQuizFileName).toBeNull();
      expect(
        topic.quiz.questions.some((q: any) => q.ques === 'No filename import?'),
      ).toBeTrue();
    }));

    it('should map unknown API question type to default question type', () => {
      const mapped = (component as any).mapApiQuestionToUiQuestion(
        {
          questionType: 'UNKNOWN_TYPE',
          questionText: 'Fallback type',
          answers: [{ answerText: 'A', isCorrectAnswer: true }],
        },
        0,
      );

      expect(mapped.questionType).toBe(component.questionTypes[0]);
      expect(mapped.ques).toBe('Fallback type');
    });

    it('should add text-field option when changing question type', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.questionType = { key: QuestionType.TEXT_FIELD };
      question.answers = [];
      spyOn(component, 'addOption').and.callThrough();

      component.changeQuestionType(topic, question);

      expect(component.addOption).toHaveBeenCalledWith(topic, question);
      expect(question.answers.length).toBe(1);
    });

    it('should collapse other topics even when sections array is empty', () => {
      component.sections = [];
      const activeTopic: any = { active: false, delete: false };

      expect(() =>
        (component as any).collapseOtherTopics(activeTopic),
      ).not.toThrow();
      expect(activeTopic.active).toBeTrue();
    });

    it('should treat deleted placeholder quiz rows as non-removable', () => {
      const isEmpty = (component as any).isRemovableEmptyQuizQuestion.bind(
        component,
      );

      expect(
        isEmpty({ delete: true, ques: '', answers: [], questionId: '' }),
      ).toBeFalse();
    });
  });

  describe('Phase 1 batch 8: nullish coalescing and guard branches', () => {
    it('should collapse other topics when sections is null', () => {
      component.sections = null as any;
      const activeTopic: any = { active: false, delete: false };

      (component as any).collapseOtherTopics(activeTopic);

      expect(activeTopic.active).toBeTrue();
    });

    it('should collapse other topics when section topics is undefined', () => {
      component.sections = [{ topics: undefined }] as any;
      const activeTopic: any = { active: false, delete: false };

      (component as any).collapseOtherTopics(activeTopic);

      expect(activeTopic.active).toBeTrue();
    });

    it('should treat quiz question with undefined answers as removable when empty', () => {
      const isEmpty = (component as any).isRemovableEmptyQuizQuestion.bind(
        component,
      );

      expect(
        isEmpty({ delete: false, ques: '', answers: undefined }),
      ).toBeTrue();
    });

    it('should reject removable empty question when answer has image url only', () => {
      const isEmpty = (component as any).isRemovableEmptyQuizQuestion.bind(
        component,
      );

      expect(
        isEmpty({
          delete: false,
          ques: '',
          answers: [
            {
              delete: false,
              isCorrectAnswer: false,
              ans: '',
              answerImageUrl: 'https://cdn/ans.png',
            },
          ],
        }),
      ).toBeFalse();
    });

    it('should map API question with null answers and image-only options', () => {
      const mapped = (component as any).mapApiQuestionToUiQuestion(
        {
          questionType: 'MULTIPLE_CHOICE',
          questionText: null,
          answers: [
            {
              answerText: 'ignored',
              answerImageUrl: 'https://cdn/opt.png',
              isCorrectAnswer: true,
            },
          ],
        },
        2,
      );

      expect(mapped.ques).toBe('');
      expect(mapped.answers[0].ans).toBe('');
      expect(mapped.answers[0].attachedImageUrl).toBe('https://cdn/opt.png');
      expect(mapped.label).toBe('Question 3');
    });

    it('should map API question with undefined answers array', () => {
      const mapped = (component as any).mapApiQuestionToUiQuestion(
        {
          questionType: 'TEXT_FIELD',
          questionText: 'Open ended',
        },
        0,
      );

      expect(mapped.answers).toEqual([]);
      expect(mapped.ques).toBe('Open ended');
    });

    it('should fall back to empty answer order when alphabet index is exhausted', () => {
      const mapped = (component as any).mapApiQuestionToUiQuestion(
        {
          questionType: 'MULTIPLE_CHOICE',
          questionText: 'Many options',
          answers: Array.from({ length: 27 }, (_, i) => ({
            answerText: `Opt ${i}`,
            isCorrectAnswer: i === 0,
          })),
        },
        0,
      );

      expect(mapped.answers[26].answerOrder).toBe('');
    });

    it('should invoke afterLoaded when uploadSectionData receives undefined sections', () => {
      const afterLoaded = jasmine.createSpy('afterLoaded');

      component.uploadSectionData(undefined, afterLoaded);

      expect(afterLoaded).toHaveBeenCalled();
      expect(courseServiceSpy.getTopicsBySectionId).not.toHaveBeenCalled();
    });

    it('should return false from hasNonDeletedTopics for null section', () => {
      expect(component.hasNonDeletedTopics(null)).toBeFalse();
    });

    it('should return false from hasNonDeletedTopics when topics is undefined', () => {
      expect(component.hasNonDeletedTopics({ topics: undefined })).toBeFalse();
    });

    it('should splice invalidQuestions in onQuizInputChange after quizValidation', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      topic.quiz.invalidQuestions = [0];
      question.ques = 'Fixed question text';
      spyOn(component, 'quizValidation');

      component.onQuizInputChange(topic, question);

      expect(topic.quiz.invalidQuestions).not.toContain(0);
    });

    it('should set deleteAll when all topics appear checked on modal confirm', () => {
      const deleteClick = new Subject<void>();
      modalSpy.create.and.returnValue({
        componentInstance: { deleteClick },
      } as any);
      const section: any = {
        topics: [
          { checkTopic: true, delete: false },
          { checkTopic: false, delete: false },
        ],
        deleteTopicIcon: true,
        checkAll: true,
        deleteAll: false,
      };

      component.deleteTopics(section);
      section.topics[1].checkTopic = true;
      deleteClick.next();

      expect(section.deleteAll).toBeTrue();
      expect(section.checkAll).toBeFalse();
      expect(section.deleteTopicIcon).toBeFalse();
    });
  });

  describe('Phase 1 batch 8: upload hydration edge cases', () => {
    it('should hydrate video topic without filename as YOUTUBE placeholder', () => {
      const section: any = { sectionId: 's1', topics: [] };
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({
          status: successCode,
          data: [
            {
              topicId: 't-v-no-name',
              topicName: 'Untitled Video',
              topicType: 'Video',
              videoId: 'vid-1',
              videoUrl: 'https://cdn/video.mp4',
              creationDate: '2026-03-01',
              docs: [],
            },
          ],
        }),
      );

      component.uploadTopicsData(section);

      expect(section.topics[0].video.videoData.videoFileName).toBe('YOUTUBE');
    });

    it('should default randomQuestion to quiz question count when API value is null', () => {
      const section: any = { sectionId: 's1', topics: [] };
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({
          status: successCode,
          data: [
            {
              topicId: 't-q-null-random',
              topicName: 'Quiz',
              topicType: 'Quiz',
              quizId: 'qz-1',
              quizTitle: 'Quiz',
              durationInMinutes: 10,
              passingCriteria: 50,
              testType: 'TEST',
              randomQuestion: null,
              quizQuestionAnswer: {
                quizQuestions: [
                  {
                    questionId: 'q1',
                    questionText: 'Q1',
                    questionType: 'MULTIPLE_CHOICE',
                    quizAnswers: [
                      { answerId: 'a1', answerText: 'A', isCorrect: true },
                      { answerId: 'a2', answerText: 'B', isCorrect: false },
                    ],
                  },
                  {
                    questionId: 'q2',
                    questionText: 'Q2',
                    questionType: 'MULTIPLE_CHOICE',
                    quizAnswers: [
                      { answerId: 'a3', answerText: 'C', isCorrect: true },
                      { answerId: 'a4', answerText: 'D', isCorrect: false },
                    ],
                  },
                ],
              },
            },
          ],
        }),
      );

      component.uploadTopicsData(section);

      expect(section.topics[0].quiz.randomQuestion).toBe(2);
    });

    it('should hydrate quiz question without quizAnswers array', () => {
      const section: any = { sectionId: 's1', topics: [] };
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({
          status: successCode,
          data: [
            {
              topicId: 't-q-no-answers',
              topicName: 'Empty Answers Quiz',
              topicType: 'Quiz',
              quizId: 'qz-2',
              quizTitle: 'Quiz',
              durationInMinutes: 5,
              passingCriteria: 50,
              testType: 'TEST',
              randomQuestion: 1,
              quizQuestionAnswer: {
                quizQuestions: [
                  {
                    questionId: 'q1',
                    questionText: 'Solo question',
                    questionType: null,
                    quizAnswers: null,
                  },
                ],
              },
            },
          ],
        }),
      );

      component.uploadTopicsData(section);

      expect(section.topics[0].quiz.questions[0].answers).toEqual([]);
      expect(section.topics[0].quiz.questions[0].surveyAnswers).toEqual([]);
      expect(section.topics[0].quiz.questions[0].questionType).toBe(
        component.questionTypes[0],
      );
    });

    it('should create topics for TEST content when API returns no topics', () => {
      component.selectedContentType = CourseContentType.TEST;
      const section: any = { sectionId: 's-test', topics: [] };
      const createTopicsSpy = spyOn(component, 'createTopics');
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({ status: successCode, data: [] }),
      );

      component.uploadTopicsData(section);

      expect(createTopicsSpy).toHaveBeenCalledWith(section);
    });

    it('should tolerate null API data array in uploadTopicsData', () => {
      const section: any = { sectionId: 's1', topics: [] };
      const done = jasmine.createSpy('done');
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({ status: successCode, data: null }),
      );

      component.uploadTopicsData(section, done);

      expect(section.topics).toEqual([]);
      expect(done).toHaveBeenCalled();
    });

    it('should use survey placeholder question type when API topic carries quiz.type SURVEY', () => {
      const section: any = { sectionId: 's1', topics: [] };
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({
          status: successCode,
          data: [
            {
              topicId: 't-art-survey-default',
              topicName: 'Article with survey default',
              topicType: 'Article',
              articleId: 'art-1',
              article: '<p>Body</p>',
              quiz: { type: QuizType.SURVEY },
              docs: null,
            },
          ],
        }),
      );

      component.uploadTopicsData(section);

      expect(section.topics[0].article.content).toBe('<p>Body</p>');
    });
  });

  describe('Phase 1 batch 8: UI prompt and input branches', () => {
    it('should hide generateTopicBtn2 when generateTopicsPrompt is already true', () => {
      const section: any = {
        generateTopicBtn1: true,
        generateTopicBtn2: true,
        generateTopicsPrompt: true,
      };

      component.showGenerateTopicsPrompt(section);

      expect(section.generateTopicBtn2).toBeFalse();
    });

    it('should keep generateTopicBtn2 false when createTopics starts with prompt open', () => {
      const section: any = {
        generateTopicsPrompt: true,
        topics: [{ quiz: { type: QuizType.BASIC_QUIZ }, level: 1 }],
      };

      component.createTopics(section);

      expect(section.generateTopicBtn2).toBeFalse();
    });

    it('should uncheck all sections via allSectionCheck', () => {
      component.sections = [
        { checkSection: true },
        { checkSection: true },
      ] as any;

      component.allSectionCheck({ target: { checked: false } });

      expect(component.sections.every((s: any) => !s.checkSection)).toBeTrue();
      expect(component.showDltSectionBtn).toBeFalse();
    });

    it('should block non-numeric keys in validateNumberInput via event.which', () => {
      const event = {
        which: 65,
        keyCode: undefined,
        key: 'A/B',
        preventDefault: jasmine.createSpy('preventDefault'),
        target: { value: '' },
      } as any;

      component.validateNumberInput(event);

      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('should block non-numeric keys in validateRandomNumberInput via event.which', () => {
      const topic = createValidQuizTopic();
      const event = {
        which: 46,
        keyCode: undefined,
        key: '.',
        preventDefault: jasmine.createSpy('preventDefault'),
        target: { value: '1' },
      } as any;

      component.validateRandomNumberInput(event, topic);

      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('should use empty default survey label when count exceeds defaults length', () => {
      const options = component.generateSurveyOptions(6);

      expect(options[5].answer).toBe('');
      expect(options[5].ans).toBe('');
    });

    it('should allow paste when clipboardData is missing', () => {
      const event = {
        preventDefault: jasmine.createSpy('preventDefault'),
        clipboardData: null,
      } as any;

      component.preventEmojiOnPaste(event);

      expect(event.preventDefault).not.toHaveBeenCalled();
    });

    it('should set topicDuration when handleVideoChange resolves duration', fakeAsync(() => {
      const topic: any = {
        validate: true,
        topicDuration: 0,
        video: {
          fileProcessing: false,
          showTable: false,
          videoData: {
            videoFileType: '',
            videoProgress: 0,
            videoFileName: '',
            videoBtnName: 'Upload File',
            delete: false,
          },
          documentData: { documents: [], documentFileName: 'Add Resource' },
        },
      };
      spyOn(component, 'getVideoDuration').and.returnValue(
        Promise.resolve(185),
      );

      component.handleVideoChange(
        {
          file: {
            name: 'lesson.mp4',
            type: 'video/mp4',
            size: 2048,
            originFileObj: new File(['v'], 'lesson.mp4', { type: 'video/mp4' }),
          },
        } as any,
        topic,
      );

      tick();
      expect(topic.topicDuration).toBe(185);
    }));

    it('should hide video table after deleting last document with Add Video filename', () => {
      spyOn(component, 'deleteVideoOrDocument');
      const topic: any = {
        topicId: 't1',
        validate: true,
        video: {
          showTable: true,
          videoData: { videoFileName: 'Add Video' },
          documentData: {
            documents: [
              {
                id: 'd1',
                documentUrl: 'https://cdn/doc.pdf',
                delete: false,
              },
            ],
            documentFileName: 'doc.pdf',
          },
        },
      };

      component.deleteDocument(topic, 0);

      expect(topic.video.showTable).toBeFalse();
      expect(topic.video.documentData.documentFileName).toBe('Add Resource');
    });
  });

  describe('Phase 1 batch 8: save validation and publish errors', () => {
    it('should fail section validation for invalid non-survey quiz topic', () => {
      component.sections = [
        {
          delete: false,
          deleteAll: false,
          name: 'Section',
          topics: [
            {
              delete: false,
              name: 'Quiz Topic',
              validate: false,
              quiz: { type: QuizType.BASIC_QUIZ, questions: [] },
            },
          ],
        },
      ] as any;

      expect(component.sectionValidation()).toBeFalse();
    });

    it('should error when saving video with source but incomplete fields', fakeAsync(() => {
      const topic: any = {
        active: false,
        name: 'Video Topic',
        validate: false,
        video: {
          videoData: {
            videoUrl: 'https://cdn/v.mp4',
            youtubeVideoUrl: '',
            videoProgress: 50,
          },
          documentData: { documents: [] },
        },
      };
      spyOn(component, 'videoValidation').and.callFake((t: any) => {
        t.validate = false;
      });
      document.body.appendChild(document.createElement('div'));

      component.saveVideo(topic);
      tick(100);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Please complete all required fields for video content',
      );
    }));

    it('should surface API error message on publish failure', () => {
      component.sections = createValidSections();
      component.courseInformationData = createCourseInformationForm(0);
      courseServiceSpy.createCourseDto.and.returnValue(
        throwError(() => ({ error: { message: 'Course slug already exists' } })),
      );

      component.publishCourse('next');

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Course slug already exists',
      );
    });

    it('should no-op bulk uploader modal when import result has no questions', () => {
      const afterClose = new Subject<any>();
      modalSpy.create.and.returnValue({
        afterClose: afterClose.asObservable(),
      } as any);
      const topic = createValidQuizTopic();
      messageServiceSpy.loading.calls.reset();

      component.openBulkQuizUploaderModal(topic);
      afterClose.next(null);

      expect(messageServiceSpy.loading).not.toHaveBeenCalled();
    });

    it('should initialize quiz questions array before bulk import when missing', fakeAsync(() => {
      const afterClose = new Subject<any>();
      modalSpy.create.and.returnValue({
        afterClose: afterClose.asObservable(),
      } as any);
      const topic: any = {
        quiz: {},
      };

      component.openBulkQuizUploaderModal(topic);
      afterClose.next({
        fileName: 'quiz.csv',
        questions: [
          {
            questionType: 'MULTIPLE_CHOICE',
            questionText: 'Imported?',
            answers: [
              { answerText: 'Yes', isCorrectAnswer: true },
              { answerText: 'No', isCorrectAnswer: false },
            ],
          },
        ],
      });
      tick();
      tick();

      expect(topic.quiz.questions.length).toBeGreaterThan(0);
    }));

    it('should import bulk questions in multiple chunks', fakeAsync(() => {
      const topic = createValidQuizTopic();
      topic.quiz.questions = [];
      const uiQuestions = Array.from({ length: 30 }, (_, i) => ({
        delete: false,
        ques: `Imported Q${i + 1}`,
        questionType: { key: QuestionType.MULTIPLE_CHOICE },
        answers: [
          { delete: false, ans: 'A', isCorrectAnswer: true, exist: false },
          { delete: false, ans: 'B', isCorrectAnswer: false, exist: false },
        ],
      }));

      (component as any).applyBulkImportedQuestions(topic, uiQuestions, 'big.csv');
      tick();
      tick();
      tick();

      expect(topic.quiz.questions.length).toBe(30);
      expect(messageServiceSpy.success).toHaveBeenCalledWith(
        'Imported 30 question(s).',
      );
    }));
  });

  describe('Phase 1 batch 8: findFirstInvalidControl fallbacks', () => {
    afterEach(() => {
      document
        .querySelectorAll(
          '.test-section-name-input, .test-add-section-btn, .topic-input',
        )
        .forEach((el) => el.remove());
    });

    it('should find test section name input for empty section name', () => {
      component.sections = [{ delete: false, name: '', topics: [] }] as any;
      const input = document.createElement('input');
      input.className = 'test-section-name-input';
      document.body.appendChild(input);

      const found = (component as any).findFirstInvalidControl();

      expect(found).toBe(input);
    });

    it('should find test add-section button when section has no topics', () => {
      component.sections = [
        { delete: false, name: 'Section', topics: [] },
      ] as any;
      const btn = document.createElement('button');
      btn.className = 'test-add-section-btn';
      document.body.appendChild(btn);

      const found = (component as any).findFirstInvalidControl();

      expect(found).toBe(btn);
    });

    it('should find test section name input for empty topic name', () => {
      component.sections = [
        {
          delete: false,
          name: 'Section',
          topics: [{ delete: false, name: '', validate: false }],
        },
      ] as any;
      const input = document.createElement('input');
      input.className = 'test-section-name-input';
      document.body.appendChild(input);

      const found = (component as any).findFirstInvalidControl();

      expect(found).toBe(input);
    });
  });

  describe('Phase 1 batch 8: template detectChanges branches', () => {
    const templateBatch8 = `
      <div id="bulk-instructions" *ngIf="bulkQuizInstructionsVisible">instructions</div>
      <div id="delete-sections" *ngIf="showDltSectionBtn && sections.length > 1">delete</div>
      <div id="generate-topics" *ngIf="sections[0]?.generateTopicBtn2">generate</div>
      <div id="generate-prompt" *ngIf="sections[0]?.generateTopicsPrompt">prompt</div>
    `;

    let tplFixture: ComponentFixture<AddSectionComponent>;
    let tplComponent: AddSectionComponent;

    beforeEach(async () => {
      TestBed.resetTestingModule();
      await TestBed.configureTestingModule({
        declarations: [AddSectionComponent],
        imports: [CommonModule, ReactiveFormsModule],
        providers: [
          FormBuilder,
          { provide: InstructorService, useValue: instructorServiceSpy },
          { provide: MessageService, useValue: messageServiceSpy },
          { provide: FileManager, useValue: fileManagerSpy },
          { provide: NzModalService, useValue: modalSpy },
          {
            provide: CommunicationService,
            useValue: communicationServiceSpy,
          },
          {
            provide: NzMessageService,
            useValue: jasmine.createSpyObj('NzMessageService', [
              'success',
              'error',
            ]),
          },
          { provide: CourseService, useValue: courseServiceSpy },
          {
            provide: AuthService,
            useValue: jasmine.createSpyObj('AuthService', ['getLoggedInName']),
          },
        ],
        schemas: [NO_ERRORS_SCHEMA],
      })
        .overrideComponent(AddSectionComponent, {
          set: { template: templateBatch8 },
        })
        .compileComponents();

      tplFixture = TestBed.createComponent(AddSectionComponent);
      tplComponent = tplFixture.componentInstance;
      tplComponent.sections = [];
      tplComponent.courseInformationData = createCourseInformationForm(0);
    });

    it('should render bulk quiz instructions after detectChanges', () => {
      tplComponent.bulkQuizInstructionsVisible = true;
      tplFixture.detectChanges();

      expect(
        tplFixture.nativeElement.querySelector('#bulk-instructions'),
      ).not.toBeNull();
    });

    it('should render delete sections control when multiple sections selected', () => {
      tplComponent.sections = [
        { checkSection: true },
        { checkSection: true },
      ] as any;
      tplComponent.showDltSectionBtn = true;
      tplFixture.detectChanges();

      expect(
        tplFixture.nativeElement.querySelector('#delete-sections'),
      ).not.toBeNull();
    });

    it('should render generate topics button when generateTopicBtn2 is true', () => {
      tplComponent.sections = [
        { generateTopicBtn2: true, generateTopicsPrompt: false },
      ] as any;
      tplFixture.detectChanges();

      expect(
        tplFixture.nativeElement.querySelector('#generate-topics'),
      ).not.toBeNull();
    });

    it('should render generate topics prompt panel when prompt flag is true', () => {
      tplComponent.sections = [
        { generateTopicBtn2: false, generateTopicsPrompt: true },
      ] as any;
      tplFixture.detectChanges();

      expect(
        tplFixture.nativeElement.querySelector('#generate-prompt'),
      ).not.toBeNull();
    });
  });

  describe('Phase 1 batch 9: saveQuiz validationError and defaults', () => {
    it('should show validationError and scroll when quiz.validationError is set', () => {
      const topic = createValidQuizTopic();
      topic.quiz.validationError = 'Custom quiz error';
      spyOn(component, 'quizValidation').and.callFake((quizTopic: any) => {
        quizTopic.validate = false;
        quizTopic.quiz.validationError = 'Custom quiz error';
      });
      const scrollSpy = spyOn(component as any, 'scrollToTopic');

      component.saveQuiz(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith('Custom quiz error');
      expect(scrollSpy).toHaveBeenCalledWith(topic);
    });

    it('should initialize generateAIReport and reportPrompt defaults on save', () => {
      const topic = createValidQuizTopic();
      topic.name = 'Quiz Topic';
      topic.active = true;
      topic.quiz.generateAIReport = undefined;
      topic.quiz.reportPrompt = undefined;

      component.saveQuiz(topic);

      expect(topic.quiz.generateAIReport).toBeFalse();
      expect(topic.quiz.reportPrompt).toBe('');
      expect(messageServiceSpy.success).toHaveBeenCalledWith(
        'Quiz saved successfully!',
      );
    });

    it('should show generic question error when specific branch does not match', () => {
      const topic = createValidQuizTopic();
      topic.name = 'Quiz Topic';
      const question = topic.quiz.questions[0];
      question.questionType = {
        key: QuestionType.SINGLE_CHOICE,
        value: 'Single choice',
      };
      question.answers = [
        { delete: false, ans: 'A', isCorrectAnswer: true, exist: false },
        { delete: false, ans: 'B', isCorrectAnswer: false, exist: false },
      ];
      spyOn(component, 'quizValidation').and.callFake((quizTopic: any) => {
        quizTopic.validate = false;
        quizTopic.quiz.invalidQuestions = [0];
      });

      component.saveQuiz(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        'Question 1: Please complete all required fields',
      );
    });
  });

  describe('Phase 1 batch 9: findFirstInvalidControl extended', () => {
    afterEach(() => {
      document
        .querySelectorAll('.quiz-save-btn, .topic-collapse-panel')
        .forEach((el) => el.remove());
    });

    it('should find quiz save button for incomplete topic', () => {
      component.sections = [
        {
          delete: false,
          name: 'Section',
          topics: [{ delete: false, name: 'Topic', validate: false }],
        },
      ] as any;
      const btn = document.createElement('button');
      btn.className = 'quiz-save-btn';
      document.body.appendChild(btn);

      const found = (component as any).findFirstInvalidControl();

      expect(found).toBe(btn);
    });

    it('should find topic collapse panel when save button is missing', () => {
      component.sections = [
        {
          delete: false,
          name: 'Section',
          topics: [{ delete: false, name: 'Topic', validate: false }],
        },
      ] as any;
      const panel = document.createElement('div');
      panel.className = 'topic-collapse-panel';
      document.body.appendChild(panel);

      const found = (component as any).findFirstInvalidControl();

      expect(found).toBe(panel);
    });
  });

  describe('Phase 1 batch 9: showValidationErrors aggregation', () => {
    it('should aggregate multiple validation issues in one message', () => {
      component.sections = [
        {
          delete: false,
          name: '',
          topics: [
            {
              delete: false,
              name: '',
              validate: false,
              selectedContentType: component.typeVideo,
              video: {
                videoData: { videoUrl: '', youtubeVideoUrl: '' },
              },
            },
          ],
        },
        { delete: false, name: 'No topics section', topics: [] },
      ] as any;

      (component as any).showValidationErrors();

      const errorMessage = messageServiceSpy.error.calls.mostRecent().args[0];
      expect(errorMessage).toContain('section(s) need names');
      expect(errorMessage).toContain('section(s) need at least one topic');
      expect(errorMessage).toContain('topic(s) need names');
      expect(errorMessage).toContain(
        'video topic(s) need a video file or YouTube URL',
      );
    });
  });

  describe('Phase 1 batch 9: article document and youtube uploads', () => {
    it('should complete customRequestArticleDocument on 201 response', () => {
      const topic: any = {
        validate: false,
        article: {
          articleProgressBar: true,
          articleFileName: 'guide.pdf',
          articleBtnName: 'Upload File',
        },
      };
      (component as any).uploadedArticleDocuments.set('guide.pdf', topic);
      fileManagerSpy.uploadFile.and.returnValue(
        of({
          status: 201,
          data: { url: 'https://cdn/guide.pdf', summary: 'Article summary' },
        }),
      );
      spyOn(component, 'articleValidation');

      component.customRequestArticleDocument({
        file: new File(['a'], 'guide.pdf', { type: 'application/pdf' }),
      } as any);

      expect(topic.article.articleProgressBar).toBeFalse();
      expect(topic.article.articleDocumnetUrl).toBe('https://cdn/guide.pdf');
      expect(topic.article.articleSummary).toBe('Article summary');
      expect(topic.article.articleBtnName).toBe('Replace');
      expect(component.articleValidation).toHaveBeenCalledWith(topic);
    });

    it('should complete youtubeVideoUrlUpload on API success', () => {
      courseServiceSpy.youtubeVideoUrlUpload.and.returnValue(of({ data: 240 }));
      const topic: any = {
        validate: false,
        topicDuration: 0,
        video: {
          fileProcessing: false,
          showTable: false,
          videoData: {
            youtubeVideoUrl: 'https://www.youtube.com/watch?v=abc123',
            videoFileType: '',
            videoProgress: 0,
            videoFileName: '',
            delete: false,
          },
          documentData: { documents: [] },
        },
      };
      spyOn(component, 'videoValidation');

      component.youtubeVideoUrlUpload(topic);

      expect(topic.topicDuration).toBe(240);
      expect(topic.video.videoData.videoProgress).toBe(100);
      expect(topic.video.videoData.videoUrl).toBe(
        'https://www.youtube.com/watch?v=abc123',
      );
      expect(component.videoValidation).toHaveBeenCalledWith(topic);
    });

    it('should reset video state when youtubeVideoUrlUpload API fails', () => {
      courseServiceSpy.youtubeVideoUrlUpload.and.returnValue(
        throwError(() => ({ error: { message: 'Invalid video ID' } })),
      );
      const topic: any = {
        validate: false,
        video: {
          fileProcessing: false,
          showTable: true,
          videoData: {
            youtubeVideoUrl: 'https://www.youtube.com/watch?v=bad',
            videoProgress: 50,
            videoFileName: 'YOUTUBE',
            videoBtnName: 'Replace',
            videoFileType: 'Video',
            date: '01/01/2026',
          },
          documentData: {
            documents: [],
            documentFileName: 'Add Resource',
            documentBtnName: 'Upload File',
          },
        },
      };

      component.youtubeVideoUrlUpload(topic);

      expect(messageServiceSpy.error).toHaveBeenCalledWith('Invalid video ID');
      expect(topic.video.videoData.videoFileName).toBe('Add Video');
      expect(topic.video.videoData.videoProgress).toBe(0);
    });
  });

  describe('Phase 1 batch 9: video replace and validation input', () => {
    it('should replace existing staged video when uploading new mp4', () => {
      const deleteSpy = spyOn(component, 'deleteVideoData');
      const topic: any = {
        validate: true,
        video: {
          fileProcessing: false,
          showTable: true,
          videoData: {
            videoFileType: 'video',
            videoProgress: 100,
            videoFileName: 'old.mp4',
            delete: false,
          },
          documentData: { documents: [], documentFileName: 'Add Resource' },
        },
      };
      spyOn(component, 'getVideoDuration').and.returnValue(
        Promise.resolve(60),
      );

      component.handleVideoChange(
        {
          file: {
            name: 'new.mp4',
            type: 'video/mp4',
            size: 1024,
            originFileObj: new File(['v'], 'new.mp4', { type: 'video/mp4' }),
          },
        } as any,
        topic,
      );

      expect(deleteSpy).toHaveBeenCalledWith(topic);
    });

    it('should block passing criteria above 100 in validateNumberInput', () => {
      const event = {
        which: 51,
        keyCode: 51,
        key: '3',
        preventDefault: jasmine.createSpy('preventDefault'),
        target: { value: '10' },
      } as any;

      component.validateNumberInput(event, false, true);

      expect(event.preventDefault).toHaveBeenCalled();
    });
  });

  describe('Phase 1 batch 9: publish flags and article formatting', () => {
    it('should set skipDraftOnDestroy after successful publish flow', () => {
      component.sections = createValidSections();
      component.courseInformationData = createCourseInformationForm(0);
      courseServiceSpy.createCourseDto.and.returnValue(
        of({ status: successCode, data: { courseId: 'c1' } }),
      );
      courseServiceSpy.getSectionByCourseId.and.returnValue(
        of({ status: successCode, data: [] }),
      );

      component.publishCourse('next');

      expect((component as any).skipDraftOnDestroy).toBeTrue();
    });

    it('should format article bullets using asterisk and bullet characters', () => {
      instructorServiceSpy.generator.and.returnValue(
        of({
          status: successCode,
          data: '* Star bullet\n• Dot bullet\nPlain line',
        }),
      );
      const article: any = {
        articlePromptInput: 'prompt',
        articlePrompt: true,
        showChatBox: true,
        showSpinner: false,
        questionAnswers: { question: '', answers: [] },
        content: '',
      };

      component.generateArticles(article);

      expect(article.content).toContain('<li>Star bullet</li>');
      expect(article.content).toContain('<li>Dot bullet</li>');
      expect(article.content).toContain('<p>Plain line</p>');
    });
  });

  describe('Phase 1 batch 9: quiz helpers and hydration', () => {
    it('should toggle multiple-choice answer without clearing other selections', () => {
      const topic = createValidQuizTopic();
      const question = topic.quiz.questions[0];
      question.questionType = { key: QuestionType.MULTIPLE_CHOICE };
      question.answers[1].isCorrectAnswer = false;
      spyOn(component, 'onQuizInputChange');

      component.markCorrectAnswer(topic, question, question.answers[1]);

      expect(question.answers[1].isCorrectAnswer).toBeTrue();
      expect(question.answers[0].isCorrectAnswer).toBeTrue();
      expect(component.onQuizInputChange).toHaveBeenCalledWith(topic);
    });

    it('should map TRUE_FALSE API questions to true/false options', () => {
      const mapped = (component as any).mapApiQuestionToUiQuestion(
        {
          questionType: 'TRUE_FALSE',
          questionText: 'Sky is blue?',
          answers: [
            { answerText: 'True', isCorrectAnswer: true },
            { answerText: 'False', isCorrectAnswer: false },
          ],
        },
        0,
      );

      expect(mapped.questionType.key).toBe(QuestionType.TRUE_FALSE);
      expect(mapped.answers.length).toBe(2);
    });

    it('should hydrate video topic documents from API docs array', () => {
      const section: any = { sectionId: 's1', topics: [] };
      courseServiceSpy.getTopicsBySectionId.and.returnValue(
        of({
          status: successCode,
          data: [
            {
              topicId: 't1',
              topicName: 'Video with docs',
              topicType: 'Video',
              videoId: 'v1',
              videoUrl: 'https://cdn/v.mp4',
              filename: 'v.mp4',
              creationDate: '2026-03-01',
              docs: [
                {
                  id: 'd1',
                  url: 'https://cdn/d.pdf',
                  name: 'slides.pdf',
                  summary: 'Doc summary',
                },
              ],
            },
          ],
        }),
      );

      component.uploadTopicsData(section);

      expect(section.topics[0].video.documentData.documents.length).toBe(1);
      expect(section.topics[0].video.documentData.documents[0].documentUrl).toBe(
        'https://cdn/d.pdf',
      );
    });

    it('should set quiz section flags when opening quiz content', () => {
      const topic: any = {
        selectedContentType: component.typeQuiz,
        quiz: {
          type: QuizType.BASIC_QUIZ,
          questions: [],
          invalidQuestions: [],
          validationMessages: [],
        },
      };

      component.openContent(topic, component.typeQuiz);

      expect(topic.quizSection).toBeTrue();
      expect(topic.videoSection).toBeFalse();
      expect(topic.articleSection).toBeFalse();
    });

    it('should increment section level when adding another section', () => {
      component.sections = [{ level: 1 }] as any;

      component.addSection();

      expect(component.sections[1].level).toBe(2);
    });
  });

  describe('Phase 1 batch 9: editor and scroll behaviors', () => {
    it('should clear article content when editor emits falsy event', () => {
      const topic: any = { article: { content: '<p>Old</p>' }, validate: true };
      spyOn(component, 'articleValidation');

      component.editorArticleContentChanged(null, topic);

      expect(topic.article.content).toBe('');
      expect(component.articleValidation).toHaveBeenCalledWith(topic);
    });

    it('should expand topic when scrollToTopic runs', fakeAsync(() => {
      const topic: any = { name: 'Topic A', active: false };
      const panel = document.createElement('div');
      panel.className = 'topic-collapse-panel';
      document.body.appendChild(panel);

      (component as any).scrollToTopic(topic);
      tick(100);

      expect(topic.active).toBeTrue();
      panel.remove();
    }));
  });
});
