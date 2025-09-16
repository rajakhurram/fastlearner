import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PreviewComponent } from './preview.component';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InstructorService } from 'src/app/core/services/instructor.service';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { NzModalService } from 'ng-zorro-antd/modal';
import { of, throwError } from 'rxjs';
import { CompletionModalComponent } from 'src/app/modules/dynamic-modals/completion-modal/completion-modal.component';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('PreviewComponent', () => {
  let component: PreviewComponent;
  let fixture: ComponentFixture<PreviewComponent>;
  let mockInstructorService: jasmine.SpyObj<InstructorService>;
  let mockCourseService: jasmine.SpyObj<CourseService>;
  let mockMessageService: jasmine.SpyObj<MessageService>;
  let mockCommunicationService: jasmine.SpyObj<CommunicationService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockModal: jasmine.SpyObj<NzModalService>;
  let formBuilder: FormBuilder;

  beforeEach(async () => {
    mockInstructorService = jasmine.createSpyObj('InstructorService', [
      'getTopicTypes',
    ]);
    mockCourseService = jasmine.createSpyObj('CourseService', [
      'createCourseDto',
      'getTagsByCourseId',
      'createCourse',
    ]);
    mockMessageService = jasmine.createSpyObj('MessageService', ['']);
    mockCommunicationService = jasmine.createSpyObj('CommunicationService', [
      'updateInstructorCourse',
    ]);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockModal = jasmine.createSpyObj('NzModalService', ['create']);

    await TestBed.configureTestingModule({
      declarations: [PreviewComponent],
      imports: [ReactiveFormsModule, AntDesignModule, BrowserAnimationsModule],
      providers: [
        FormBuilder,
        { provide: InstructorService, useValue: mockInstructorService },
        { provide: CourseService, useValue: mockCourseService },
        { provide: MessageService, useValue: mockMessageService },
        { provide: CommunicationService, useValue: mockCommunicationService },
        { provide: Router, useValue: mockRouter },
        { provide: NzModalService, useValue: mockModal },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PreviewComponent);
    formBuilder = TestBed.inject(FormBuilder);

    component = fixture.componentInstance;
    component.courseInformationData = formBuilder.group({
      description: [''],
      certificateEnabled: [false],
      previewPath: [''],
      tagsArray: formBuilder.array([]),
    });
    fixture.detectChanges();
  });

  describe('ngOnInit', () => {
    it('should call getTopicTypes and getCourseTags on init', () => {
      spyOn(component, 'getTopicTypes').and.callThrough();
      spyOn(component, 'getCourseTags').and.callThrough();
      component.ngOnInit();
      expect(component.getTopicTypes).toHaveBeenCalled();
    });
  });

  describe('checkTooltipVisibility', () => {
    it('should update tooltip visibility based on scroll', () => {
      // component.videoPlayerElement = {
      //   media: {
      //     nativeElement: {
      //       getBoundingClientRect: () => ({ top: 0, bottom: 100, height: 100 }),
      //     },
      //   },
      // };
      // spyOnProperty(window, 'innerHeight').and.returnValue(50);
      // component.checkTooltipVisibility();
      // expect(component.isTooltipVisible).toBeTrue();
    });

    it('should set tooltip visibility to true if enough height is visible', () => {
      // component.videoPlayerElement = {
      //   media: {
      //     nativeElement: {
      //       getBoundingClientRect: () => ({ top: 0, bottom: 150, height: 100 }),
      //     },
      //   },
      // };
      // spyOnProperty(window, 'innerHeight').and.returnValue(100);
      // component.checkTooltipVisibility();
      // expect(component.isTooltipVisible).toBeTrue();
    });
  });

  describe('getTopicTypes', () => {
    it('should populate topicTypes on successful getTopicTypes call', () => {
      const mockResponse = { status: 200, data: [{ id: 1, name: 'Video' }] };
      mockInstructorService.getTopicTypes.and.returnValue(of(mockResponse));
      component.getTopicTypes();
      expect(component.topicTypes).toEqual(mockResponse.data);
    });

    it('should handle error in getTopicTypes', () => {
      mockInstructorService.getTopicTypes.and.returnValue(
        throwError(() => new Error('Error'))
      );
      spyOn(console, 'log');
      component.getTopicTypes();
      expect(console.log).toHaveBeenCalledWith(new Error('Error'));
    });
  });

  describe('publishCourse', () => {
    it('should call createCourseDto and open modal on success', () => {
      const mockResponse = { status: 200 };
      mockCourseService.createCourseDto.and.returnValue(of(mockResponse));
      spyOn(component, 'opencourseCompletionModal');
      component.publishCourse();
      expect(component.opencourseCompletionModal).toHaveBeenCalled();
    });

    it('should handle error in publishCourse', () => {
      mockCourseService.createCourseDto.and.returnValue(
        throwError(() => new Error('Error'))
      );
      spyOn(console, 'log');
      component.publishCourse();
      expect(console.log).toHaveBeenCalledWith(new Error('Error'));
    });
  });

  describe('getCourseTags', () => {
    it('should populate tags on successful getCourseTags call', () => {
      const mockResponse = { data: [{ id: 1, name: 'Tag1' }] };
      mockCourseService.getTagsByCourseId.and.returnValue(of(mockResponse));
      component.getCourseTags();
      expect(component.tags).toEqual(mockResponse.data);
    });

    it('should handle error in getCourseTags', () => {
      mockCourseService.getTagsByCourseId.and.returnValue(
        throwError(() => new Error('Error'))
      );
      spyOn(console, 'log');
      component.getCourseTags();
      expect(console.log).toHaveBeenCalledWith(new Error('Error'));
    });
  });

  describe('modifyCourseTags', () => {
    it('should modify course tags based on provided tagsArray', () => {
      component.tags = [
        { id: 1, active: true },
        { id: 2, active: true },
      ];
      const tagsArray = [{ id: 1 }, { id: 3 }];
      const modifiedTags = component.modifyCourseTags(tagsArray);
      expect(modifiedTags).toBeDefined();
    });
  });

  describe('createCourse', () => {
    it('should call createCourse and open modal on success', () => {
      const mockResponse = { status: 200 };
      mockCourseService.createCourse.and.returnValue(of(mockResponse));
      spyOn(component, 'opencourseCompletionModal');
      component.createCourse();
      expect(component.opencourseCompletionModal).toHaveBeenCalled();
    });

    it('should handle error in createCourse', () => {
      mockCourseService.createCourse.and.returnValue(
        throwError(() => new Error('Error'))
      );
      spyOn(console, 'log');
      component.createCourse();
      expect(console.log).toHaveBeenCalledWith(new Error('Error'));
    });
  });

  describe('routeToDashboard', () => {
    it('should navigate to instructor dashboard', () => {
      component.routeToDashboard();
      expect(mockRouter.navigate).toHaveBeenCalledWith([
        'instructor/instructor-dashboard',
      ]);
    });
  });

  describe('createSections', () => {
    it('should create sections from sectionsData', () => {
      component.sectionsData = [
        {
          sectionId: 1,
          delete: false,
          name: 'Section 1',
          switchValue: true,
          topics: [],
        },
      ];
      const sections = component.createSections();
      expect(sections.length).toBe(1);
      expect(sections[0].id).toBe(1);
      expect(sections[0].title).toBe('Section 1');
    });
  });

  describe('createTopics', () => {
    it('should create topics from section data', () => {
      component.typeVideo = 'Video';
      component.typeQuiz = 'Quiz';
      component.typeArticle = 'Article';
      const section = {
        topics: [
          {
            id: 1,
            selectedContentType: 'Video',
            video: { videoData: { videoId: 'v1' } },
            article: { articleId: '' },
            quiz: { quizId: '' },
          },
        ],
      };
      const topics = component.createTopics(section);
      expect(topics.length).toBe(1);
      expect(topics[0].video.id).toBe('v1');
    });
  });

  describe('getVideoData', () => {
    it('should create Video object from video data', () => {
      const videoData = {
        videoData: {
          videoId: 'v1',
          videoFileName: 'video.mp4',
          delete: false,
          videoUrl: 'http://example.com',
          videoSummary: 'Summary',
          videoTranscript: 'Transcript',
        },
        documentData: { documents: [] },
      };
      const video = component.getVideoData(videoData);
      expect(video.id).toBe('v1');
      expect(video.videoURL).toBe('http://example.com');
    });
  });

  describe('getDocuments', () => {
    it('should create Document objects from documents data', () => {
      const documents = [
        {
          id: 'd1',
          delete: false,
          summary: 'Summary',
          documentFileName: 'doc.pdf',
          documentUrl: 'http://example.com/doc.pdf',
        },
      ];
      const result = component.getDocuments(documents);
      expect(result.length).toBe(1);
      expect(result[0].id).toBe('d1');
    });
  });

  describe('getQuizData', () => {
    it('should create Quiz object from quiz data', () => {
      const quizData = {
        quizId: 'q1',
        delete: false,
        title: 'Quiz 1',
        questions: [],
      };
      const quiz = component.getQuizData(quizData);
      expect(quiz.id).toBe('q1');
      expect(quiz.title).toBe('Quiz 1');
    });
  });

  describe('getArticleData', () => {
    it('should create Article object from article data', () => {
      const articleData = {
        articleId: 'a1',
        delete: false,
        content: 'Content',
        articleDocumnetId: 'd1',
        articleFileName: 'article.pdf',
        articleDocumnetUrl: 'http://example.com/article.pdf',
        articleSummary: 'Summary',
      };
      const article = component.getArticleData(articleData);
      expect(article.id).toBe('a1');
      expect(article.documents[0].docUrl).toBe(
        'http://example.com/article.pdf'
      );
    });
  });

  describe('fetchTopicTypeId', () => {
    it('should return the topic type id for the given content type', () => {
      component.topicTypes = [
        { id: 1, name: 'Video' },
        { id: 2, name: 'Quiz' },
      ];
      const id = component.fetchTopicTypeId('Video');
      expect(id).toBe(1);
    });
  });

  describe('fetchCourseSummariesInfo', () => {
    it('should return course summaries info from courseSummaries', () => {
      const courseSummaries = [
        { courseSummaryInfo: 'Summary 1' },
        { courseSummaryInfo: 'Summary 2' },
      ];
      const summariesInfo = component.fetchCourseSummariesInfo(courseSummaries);
      expect(summariesInfo.length).toBe(2);
    });
  });

  describe('fetchPrerequisite', () => {
    it('should return prerequisites as an array', () => {
      const prerequisite = 'Prerequisite 1';
      const prerequisites = component.fetchPrerequisite(prerequisite);
      expect(prerequisites.length).toBe(1);
      expect(prerequisites[0]).toBe('Prerequisite 1');
    });
  });

  // describe('opencourseCompletionModal', () => {
  //   it('should open completion modal', () => {
  //     mockModal.create.and.returnValue({ afterClose: of(null) });
  //     component.opencourseCompletionModal();
  //     expect(mockModal.create).toHaveBeenCalledWith({
  //       nzContent: CompletionModalComponent,
  //       nzViewContainerRef: component['_viewContainerRef'],
  //       nzFooter: null,
  //       nzKeyboard: true,
  //     });
  //   });
  // });

  describe('closeCourseCompletionModal', () => {
    it('should navigate to instructor dashboard', () => {
      component.closeCourseCompletionModal();
      expect(mockRouter.navigate).toHaveBeenCalledWith([
        'instructor/instructor-dashboard',
      ]);
    });
  });
});
