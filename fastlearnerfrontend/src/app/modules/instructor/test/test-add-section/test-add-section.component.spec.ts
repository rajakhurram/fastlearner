import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { QuestionType } from 'src/app/core/enums/question-type';
import { AuthService } from 'src/app/core/services/auth.service';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { CourseService } from 'src/app/core/services/course.service';
import { FileManager } from 'src/app/core/services/file-manager.service';
import { InstructorService } from 'src/app/core/services/instructor.service';
import { MessageService } from 'src/app/core/services/message.service';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import { TestAddSectionComponent } from './test-add-section.component';

describe('TestAddSectionComponent', () => {
  let component: TestAddSectionComponent;
  let fixture: ComponentFixture<TestAddSectionComponent>;
  let fb: FormBuilder;
  let courseServiceSpy: jasmine.SpyObj<CourseService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let communicationServiceSpy: jasmine.SpyObj<CommunicationService>;

  const successCode = 200;

  const createCourseInformationForm = (progress = 0): FormGroup =>
    fb.group({ courseProgress: [progress] });

  const createValidQuizTopic = () => ({
    delete: false,
    validate: false,
    completed: false,
    active: false,
    topicStatusImg: '',
    quiz: {
      title: 'Chapter Quiz',
      durationInMinutes: 10,
      passingCriteria: 50,
      questions: [
        {
          delete: false,
          ques: 'What is 2+2?',
          questionType: { key: QuestionType.MULTIPLE_CHOICE },
          answers: [
            { delete: false, ans: '4', exist: false, isCorrectAnswer: true },
            { delete: false, ans: '5', exist: false, isCorrectAnswer: false },
          ],
        },
      ],
    },
  });

  const createValidSection = () => ({
    delete: false,
    deleteAll: false,
    name: 'Section 1',
    topics: [{ ...createValidQuizTopic(), validate: true }],
  });

  beforeEach(async () => {
    courseServiceSpy = jasmine.createSpyObj<CourseService>('CourseService', [
      'createCourseDto',
      'getSectionByCourseId',
      'getTopicsBySectionId',
      'youtubeVideoUrlUpload',
    ]);
    messageServiceSpy = jasmine.createSpyObj<MessageService>('MessageService', [
      'error',
      'success',
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

    await TestBed.configureTestingModule({
      declarations: [TestAddSectionComponent],
      imports: [ReactiveFormsModule],
      providers: [
        FormBuilder,
        {
          provide: InstructorService,
          useValue: jasmine.createSpyObj('InstructorService', ['generator']),
        },
        { provide: MessageService, useValue: messageServiceSpy },
        {
          provide: FileManager,
          useValue: jasmine.createSpyObj('FileManager', ['uploadFile', 'deleteFile']),
        },
        {
          provide: NzModalService,
          useValue: jasmine.createSpyObj('NzModalService', ['create']),
        },
        {
          provide: NzMessageService,
          useValue: jasmine.createSpyObj('NzMessageService', ['error', 'success']),
        },
        { provide: CommunicationService, useValue: communicationServiceSpy },
        {
          provide: AuthService,
          useValue: jasmine.createSpyObj('AuthService', ['getLoggedInName']),
        },
        { provide: CourseService, useValue: courseServiceSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    })
      .overrideComponent(TestAddSectionComponent, { set: { template: '' } })
      .compileComponents();

    fb = TestBed.inject(FormBuilder);
    fixture = TestBed.createComponent(TestAddSectionComponent);
    component = fixture.componentInstance;
    component.sections = [];
    component.sectionsData = [];
    component.courseId = null;
    component.courseInformationData = createCourseInformationForm(0);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should add section on init when no courseId and no sectionsData', () => {
    const addSpy = spyOn(component, 'addSection');
    component.ngOnInit();
    expect(addSpy).toHaveBeenCalled();
  });

  it('should patch section data on init when courseId exists', () => {
    const patchSpy = spyOn(component, 'patchSectionData');
    component.courseId = 'course-1';
    component.ngOnInit();
    expect(patchSpy).toHaveBeenCalled();
  });

  it('should use provided sectionsData on init', () => {
    const sections = [createValidSection()];
    component.sectionsData = sections;
    component.ngOnInit();
    expect(component.sections).toEqual(sections);
  });

  it('should add section with incremental level', () => {
    component.sections = [{ level: 1 }, { level: 2 }] as any;
    component.addSection();
    expect(component.sections.length).toBe(3);
    expect(component.sections[2].level).toBe(3);
  });

  it('should trim topic input', () => {
    const section = { topicInput: '  topic name  ' };
    component.topicInputChange(section);
    expect(section.topicInput).toBe('topic name');
  });

  it('should validate quiz topic with complete data', () => {
    const topic = createValidQuizTopic();
    component.quizValidation(topic);
    expect(topic.validate).toBeTrue();
  });

  it('should invalidate quiz topic without title', () => {
    const topic = createValidQuizTopic();
    topic.quiz.title = '';
    component.quizValidation(topic);
    expect(topic.validate).toBeFalse();
  });

  it('should validate video topic when upload is complete', () => {
    const topic = {
      validate: false,
      topicStatusImg: '',
      video: {
        videoData: { videoProgress: 100 },
        documentData: { documents: [] },
      },
    };
    component.videoValidation(topic);
    expect(topic.validate).toBeTrue();
  });

  it('should invalidate video topic when upload is incomplete', () => {
    const topic = {
      validate: true,
      topicStatusImg: component.topicStatusCompleteImg,
      video: {
        videoData: { videoProgress: 50 },
        documentData: { documents: [{ documentProgress: 50 }] },
      },
    };
    component.videoValidation(topic);
    expect(topic.validate).toBeFalse();
  });

  it('should validate article topic with content', () => {
    const topic = {
      validate: false,
      topicStatusImg: '',
      article: { content: 'Article body', articleProgressBar: false },
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

  it('should return true for valid sections', () => {
    component.sections = [createValidSection()];
    expect(component.sectionValidation()).toBeTrue();
  });

  it('should return false when section has no topics', () => {
    component.sections = [
      { delete: false, deleteAll: false, name: 'S1', topics: [] },
    ];
    expect(component.sectionValidation()).toBeFalse();
  });

  it('should call publishCourse when steps is valid', () => {
    spyOn(component, 'sectionValidation').and.returnValue(true);
    const publishSpy = spyOn(component, 'publishCourse');
    component.steps('next');
    expect(publishSpy).toHaveBeenCalledWith('next');
  });

  it('should show error when steps is invalid', () => {
    spyOn(component, 'sectionValidation').and.returnValue(false);
    component.steps('next');
    expect(messageServiceSpy.error).toHaveBeenCalledWith(
      'Please complete the sections',
    );
  });

  it('should emit outputs on backToPreviousStep', () => {
    const stepSpy = spyOn(component.currentStep, 'emit');
    const sectionSpy = spyOn(component.sectionsDataOutPut, 'emit');
    component.sections = [createValidSection()];
    component.backToPreviousStep('prev');
    expect(stepSpy).toHaveBeenCalledWith('prev');
    expect(sectionSpy).toHaveBeenCalledWith(component.sections);
  });

  it('should add true/false options when question type is TRUE_FALSE', () => {
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

  it('should detect duplicate quiz options', () => {
    const question = {
      answers: [
        { delete: false, ans: 'Same', exist: false },
        { delete: false, ans: 'Same', exist: false },
      ],
    };
    component.quizOptions(question, 1);
    expect(question.answers[1].exist).toBeTrue();
    expect(messageServiceSpy.error).toHaveBeenCalledWith(
      'Option already exist in this question',
    );
  });

  it('should save quiz and mark topic complete when valid', () => {
    const topic = createValidQuizTopic();
    component.saveQuiz(topic);
    expect(topic.completed).toBeTrue();
    expect(topic.topicStatusImg).toBe(component.topicStatusCompleteImg);
  });

  it('should call createCourseDto on publish success', () => {
    component.sections = [createValidSection()];
    component.courseSaved = false;
    courseServiceSpy.createCourseDto.and.returnValue(
      of({ status: successCode, data: { courseId: 'new-id' } }),
    );
    spyOn(component, 'patchSectionData');
    const stepSpy = spyOn(component.currentStep, 'emit');

    component.publishCourse('step-2');

    expect(courseServiceSpy.createCourseDto).toHaveBeenCalled();
    expect(component.courseId).toBe('new-id');
    expect(communicationServiceSpy.updateInstructorCourse).toHaveBeenCalled();
    expect(stepSpy).toHaveBeenCalledWith('step-2');
  });

  it('should save draft when course is not published', () => {
    component.sections = [createValidSection()];
    component.courseSaved = false;
    courseServiceSpy.createCourseDto.and.returnValue(
      of({ status: successCode, data: { courseId: 'draft-id' } }),
    );

    component.saveAsDraftCourse();

    expect(courseServiceSpy.createCourseDto).toHaveBeenCalled();
    expect(component.courseId).toBe('draft-id');
  });

  it('should not save draft when course progress is 100', () => {
    component.courseInformationData = createCourseInformationForm(100);
    component.sections = [createValidSection()];
    component.saveAsDraftCourse();
    expect(courseServiceSpy.createCourseDto).not.toHaveBeenCalled();
  });

  it('should call patchSectionData on API success', () => {
    component.courseId = 'course-1';
    const uploadSpy = spyOn(component, 'uploadSectionData');
    courseServiceSpy.getSectionByCourseId.and.returnValue(
      of({
        status: successCode,
        data: [{ sectionId: 's1', sectionName: 'Section 1' }],
      }),
    );
    component.patchSectionData();
    expect(courseServiceSpy.getSectionByCourseId).toHaveBeenCalledWith(
      'course-1',
    );
    expect(uploadSpy).toHaveBeenCalled();
  });

  it('should add section when patchSectionData API fails', () => {
    component.courseId = 'course-1';
    const addSpy = spyOn(component, 'addSection');
    courseServiceSpy.getSectionByCourseId.and.returnValue(
      throwError(() => new Error('fail')),
    );
    component.patchSectionData();
    expect(addSpy).toHaveBeenCalled();
  });

  it('should prevent default on beforeunload', () => {
    const event = new Event('beforeunload');
    const preventSpy = spyOn(event, 'preventDefault');
    component.handleBeforeUnload(event);
    expect(preventSpy).toHaveBeenCalled();
  });

  it('should update screen width on resize', () => {
    component.onResize({ target: { innerWidth: 1024 } });
    expect(component.screenWidth).toBe(1024);
  });

  it('should stop event propagation on stopCollapse', () => {
    const event = new Event('click');
    const stopSpy = spyOn(event, 'stopPropagation');
    component.stopCollapse(event);
    expect(stopSpy).toHaveBeenCalled();
  });
});
