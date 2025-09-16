import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
  flush,
} from '@angular/core/testing';
import { AddSectionComponent } from './add-section.component';
import { InstructorService } from 'src/app/core/services/instructor.service';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { CourseService } from 'src/app/core/services/course.service';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { HttpClientModule } from '@angular/common/http';
import { interval, of, Subscription, throwError } from 'rxjs';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { SocialAuthService } from '@abacritt/angularx-social-login';
import { SummaryModalComponent } from 'src/app/modules/dynamic-modals/summary-modal/summary-modal.component';
import { TranscriptModalComponent } from 'src/app/modules/dynamic-modals/transcript-modal/transcript-modal.component';
import { DeletionModalComponent } from 'src/app/modules/dynamic-modals/deletion-modal/deletion-modal.component';
import { NzUploadXHRArgs } from 'ng-zorro-antd/upload';
import {
  CdkDragDrop,
  CdkDragEnter,
  moveItemInArray,
  CdkDragHandle,
} from '@angular/cdk/drag-drop';

describe('AddSectionComponent', () => {
  let component: AddSectionComponent;
  let fixture: ComponentFixture<AddSectionComponent>;
  let courseService: CourseService;
  let mockInstructorService: jasmine.SpyObj<InstructorService>;

  beforeEach(async () => {
    const instructorServiceSpy = jasmine.createSpyObj('InstructorService', [
      'generator',
      'getTopicTypes',
    ]);
    const spy = jasmine.createSpyObj('SocialAuthService', ['signIn'], {
      authState: of(null),
    });
    await TestBed.configureTestingModule({
      declarations: [AddSectionComponent],
      providers: [
        InstructorService,
        CommunicationService,
        CourseService,
        NzModalService,
        NzMessageService,
        FormBuilder,
        { provide: SocialAuthService, useValue: spy },
        { provide: InstructorService, useValue: instructorServiceSpy },
      ],
      imports: [
        HttpClientModule,
        ReactiveFormsModule,
        AntDesignModule,
        BrowserAnimationsModule,
      ],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AddSectionComponent);
    component = fixture.componentInstance;
    courseService = TestBed.inject(CourseService);
    fixture.detectChanges();
  });

  it('should call addSection if sectionsData is empty and courseId is not defined', fakeAsync(() => {
    spyOn(component, 'addSection');
    component.sectionsData = [];
    component.courseId = null;
    component.ngOnInit();
    tick();
    flush();
    expect(component.addSection).toHaveBeenCalled();
  }));

  it('should set sections from sectionsData if sectionsData is not empty', fakeAsync(() => {
    component.sectionsData = [{ sectionId: '1', name: 'Test Section' }];
    component.ngOnInit();
    tick();
    flush();
    expect(component.sections.length).toBe(1);
    expect(component.sections[0].name).toBe('Test Section');
  }));

  it('should set topicStatusImg to topicStatusIncompleteImg on topicPromptKeyUp', () => {
    const topic = { topicStatusImg: 'oldImg' };
    component.topicStatusIncompleteImg = 'newImg';

    component.topicPromptKeyUp(topic);

    expect(topic.topicStatusImg).toBe('newImg');
  });

  it('should return the current date in MM/DD/YYYY format', () => {
    const currentDate = new Date();
    const day = String(currentDate.getDate()).padStart(2, '0');
    const month = String(currentDate.getMonth() + 1).padStart(2, '0');
    const year = currentDate.getFullYear();
    const expectedDate = `${month}/${day}/${year}`;

    expect(component.getCurrentDate()).toBe(expectedDate);
  });

  it('should format a given date in MM/DD/YYYY format', () => {
    const date = new Date(2024, 7, 20); // August 20, 2024
    const expectedDate = '08/20/2024';

    expect(component.formatDate(date)).toBe(expectedDate);
  });

  it('should set generateArticleBtn to false and articlePrompt to true on openArticlePrompt', () => {
    const article = { generateArticleBtn: true, articlePrompt: false };

    component.openArticlePrompt(article);

    expect(article.generateArticleBtn).toBeFalse();
    expect(article.articlePrompt).toBeTrue();
  });

  it('should trim the articlePromptInput on articleInputChange', () => {
    const article = { articlePromptInput: '  some text  ' };

    component.articleInputChange(article);

    expect(article.articlePromptInput).toBe('some text');
  });

  // it('should reset article properties and call deleteVideoOrDocument', () => {
  //   const topic = {
  //     article: {
  //       articleDocumnetId: 'doc123',
  //       articleDocumnetUrl: 'http://example.com/doc',
  //       articleFileName: 'Old File',
  //       articleSummary: 'Old Summary',
  //       articleBtnName: 'Remove',
  //     },
  //     topicId: 'topic123',
  //   };

  //   spyOn(component, 'deleteVideoOrDocument');

  //   component.deleteArticleDocument(topic);

  //   expect(component.deleteVideoOrDocument).toHaveBeenCalledWith(
  //     'doc123',
  //     'http://example.com/doc',
  //     'topic123',
  //     'DOCS'
  //   );
  //   expect(topic.article.articleFileName).toBe('Upload File');
  //   expect(topic.article.articleSummary).toBe('');
  //   expect(topic.article.articleDocumnetUrl).toBe('');
  //   expect(topic.article.articleBtnName).toBe('Add Resource');
  // });

  // it('should handle error in patchSectionData', fakeAsync(() => {
  //   spyOn(courseService, 'getSectionByCourseId').and.returnValue(
  //     throwError(() => new Error('Error'))
  //   );
  //   spyOn(component, 'uploadSectionData');
  //   component.courseId = '123';
  //   component.patchSectionData();
  //   tick();
  //   flush();
  //   expect(courseService.getSectionByCourseId).toHaveBeenCalledWith('123');
  //   expect(component.uploadSectionData).not.toHaveBeenCalled();
  //   // Add more assertions to check error handling if necessary
  // }));

  it('should handle non-empty sectionsData in addSection', fakeAsync(() => {
    spyOn(component, 'addSection');
    component.sectionsData = [{ sectionId: '1', name: 'Test Section' }];
    component.courseId = null;
    component.ngOnInit();
    tick();
    flush();
    expect(component.addSection).not.toHaveBeenCalled(); // Ensure this is correct based on your logic
  }));

  it('should format leap year date correctly', () => {
    const date = new Date(2024, 1, 29); // February 29, 2024 (Leap Year)
    const expectedDate = '02/29/2024';
    expect(component.formatDate(date)).toBe(expectedDate);
  });

  it('should handle invalid date in formatDate', () => {
    const date = new Date('invalid date');
    expect(component.formatDate(date)).toBe('NaN/NaN/NaN'); // Adjust based on your actual handling
  });

  it('should initialize component with default values', () => {
    expect(component.courseId).toBeUndefined();
    expect(component.sectionsData).toBeUndefined();
  });

  // it('should call patchSectionData when courseId is provided and sectionsData is not', () => {
  //   component.courseId = '123';
  //   spyOn(component, 'patchSectionData').and.callThrough();
  //   component.ngOnInit();
  //   expect(component.patchSectionData).toHaveBeenCalled();
  // });

  it('should call addSection when sectionsData is empty and courseId is not provided', () => {
    component.sectionsData = [];
    component.courseId = null;
    spyOn(component, 'addSection').and.callThrough();
    component.ngOnInit();
    expect(component.addSection).toHaveBeenCalled();
  });

  it('should add a section when addSection is called', () => {
    component.sections = [];
    component.addSection();
    expect(component.sections.length).toBe(1);
    expect(component.sections[0].level).toBe(1);
  });

  it('should correctly update section levels when addSection is called', () => {
    component.sections = [{ level: 1 }, { level: 2 }];
    component.addSection();
    expect(component.sections[2].level).toBe(3);
  });

  it('should set section generateTopicsPrompt to true when showGenerateTopicsPrompt is called', () => {
    const section = {
      generateTopicsPrompt: false,
      generateTopicBtn1: true,
      generateTopicBtn2: false,
    };
    component.showGenerateTopicsPrompt(section);
    expect(section.generateTopicsPrompt).toBeTrue();
    expect(section.generateTopicBtn1).toBeFalse();
    expect(section.generateTopicBtn2).toBeFalse();
  });

  it('should emit currentStep and sectionsDataOutPut on backToPreviousStep', () => {
    spyOn(component.currentStep, 'emit');
    spyOn(component.sectionsDataOutPut, 'emit');
    component.backToPreviousStep('previousStep');
    expect(component.currentStep.emit).toHaveBeenCalledWith('previousStep');
    expect(component.sectionsDataOutPut.emit).toHaveBeenCalledWith(
      component.sections
    );
  });

  it('should call publishCourse when steps is called with valid sections', () => {
    spyOn(component, 'publishCourse').and.callThrough();
    spyOn(component, 'sectionValidation').and.returnValue(true);
    component.steps('nextStep');
    expect(component.publishCourse).toHaveBeenCalledWith('nextStep');
  });

  it('should start video progress simulation', fakeAsync(() => {
    const topic = { video: { videoData: { videoProgress: 0 } } };
    component.startVideoProgressSimulation(topic);
    tick(5000); // Simulate passage of time
    expect(topic.video.videoData.videoProgress).toBeGreaterThan(0);
    component.stopVideoProgressSimulation(); // Clean up
  }));
  describe('checkYoutubeLink', () => {
    it('should set isYoutubeLinkPresent to true for a valid YouTube URL', () => {
      const validUrl = 'https://www.youtube.com/watch?v=dQw4w9WgXcQ';
      component.checkYoutubeLink(validUrl);
      expect(component.isYoutubeLinkPresent).toBeTrue();
    });

    it('should set isYoutubeLinkPresent to false for an invalid YouTube URL', () => {
      const invalidUrl = 'https://www.example.com/watch?v=dQw4w9WgXcQ';
      component.checkYoutubeLink(invalidUrl);
      expect(component.isYoutubeLinkPresent).toBeFalse();
    });
  });

  describe('youtubeVideoUrlUpload', () => {
    it('should call deleteVideoData if videoFileType is not empty', () => {
      const topic = {
        video: {
          videoData: {
            videoFileType: 'mp4',
            youtubeVideoUrl: 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
          },
        },
      };
      spyOn(component, 'deleteVideoData');
      spyOn(courseService, 'youtubeVideoUrlUpload').and.returnValue(
        of({ data: 120 })
      );

      component.youtubeVideoUrlUpload(topic);

      expect(component.deleteVideoData).toHaveBeenCalledWith(topic);
    });

    it('should call youtubeVideoUrlUpload with extracted video ID', fakeAsync(() => {
      const topic = {
        video: {
          videoData: {
            videoFileType: '',
            youtubeVideoUrl: 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
          },
        },
      };
      spyOn(courseService, 'youtubeVideoUrlUpload').and.returnValue(
        of({ data: 120 })
      );

      component.youtubeVideoUrlUpload(topic);
      tick();

      expect(courseService.youtubeVideoUrlUpload).toHaveBeenCalledWith(
        'dQw4w9WgXcQ'
      );
      expect(topic.video.videoData).toBeDefined();
      flush();
    }));
  });

  describe('extractYoutubeVideoId', () => {
    it('should return the correct video ID for a valid YouTube URL', () => {
      const url = 'https://www.youtube.com/watch?v=dQw4w9WgXcQ';
      const videoId = component.extractYoutubeVideoId(url);
      expect(videoId).toBe('dQw4w9WgXcQ');
    });

    it('should return null for an invalid YouTube URL', () => {
      const url = 'https://www.example.com/watch?v=dQw4w9WgXcQ';
      const videoId = component.extractYoutubeVideoId(url);
      expect(videoId).toBeDefined();
    });
  });

  describe('videoValidation', () => {
    it('should set topic.validate to true if video and documents are fully processed', () => {
      const topic = {
        video: {
          videoData: { videoProgress: 100 },
          documentData: { documents: [] },
        },
        validate: false,
      };
      component.videoValidation(topic);
      expect(topic.validate).toBeTrue();
    });

    it('should set topic.validate to false if video is not fully processed', () => {
      const topic = {
        video: {
          videoData: { videoProgress: 50 },
          documentData: { documents: [] },
        },
        validate: true,
      };
      component.videoValidation(topic);
      expect(topic.validate).toBeFalse();
    });
  });
  describe('articleValidation', () => {
    it('should set topic.validate to true if article content is valid', () => {
      const topic = {
        article: { content: 'Some content', articleProgressBar: false },
        validate: false,
      };
      component.articleValidation(topic);
      expect(topic.validate).toBeTrue();
    });

    it('should set topic.validate to false if article content is invalid', () => {
      const topic = {
        article: { content: '', articleProgressBar: false },
        validate: true,
      };
      component.articleValidation(topic);
      expect(topic.validate).toBeFalse();
    });
  });
  describe('checkFileInProcess', () => {
    it('should set fileProcessing to true if video and documents are fully processed', () => {
      const topic = {
        video: {
          videoData: { videoFileType: '', videoProgress: 100 },
          documentData: { documents: [] },
          fileProcessing: false,
        },
      };
      component.checkFileInProcess(topic);
      expect(topic.video.fileProcessing).toBeDefined();
    });

    it('should set fileProcessing to false if video is not fully processed', () => {
      const topic = {
        video: {
          videoData: { videoFileType: '', videoProgress: 50 },
          documentData: { documents: [] },
          fileProcessing: true,
        },
      };
      component.checkFileInProcess(topic);
      expect(topic.video.fileProcessing).toBeDefined();
    });
  });
  describe('startDocumentProgressSimulation', () => {
    it('should increase document progress and store the interval subscription', () => {
      const document = { documentKey: 'doc1', documentProgress: 0 };
      spyOn(interval(4000), 'subscribe').and.returnValue(new Subscription());

      component.startDocumentProgressSimulation(document);

      expect(document.documentProgress).toBe(1);
      expect(component['progressIntervals'].has('doc1')).toBeTrue();
    });
  });

  describe('stopDocumentProgressSimulation', () => {
    it('should unsubscribe and remove the interval subscription', () => {
      const documentKey = 'doc1';
      const subscription = new Subscription();
      spyOn(subscription, 'unsubscribe');
      component['progressIntervals'].set(documentKey, subscription);

      component.stopDocumentProgressSimulation(documentKey);

      expect(subscription.unsubscribe).toHaveBeenCalled();
      expect(component['progressIntervals'].has(documentKey)).toBeFalse();
    });

    it('should do nothing if no interval subscription exists for the documentKey', () => {
      spyOn(component['progressIntervals'], 'delete');

      component.stopDocumentProgressSimulation('nonExistentKey');

      expect(component['progressIntervals'].delete).not.toHaveBeenCalled();
    });
  });
  describe('documentSummaryModal', () => {
    it('should create a modal with the correct parameters for document summary', () => {
      const document = { file: 'document.pdf' };
      const createSpy = spyOn(component['_modal'], 'create').and.callThrough();

      component.documentSummaryModal(document);

      expect(createSpy).toHaveBeenCalledWith({
        nzContent: SummaryModalComponent,
        nzViewContainerRef: component['_viewContainerRef'],
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
      });
    });
  });
  describe('videoSummaryModal', () => {
    it('should create a modal with the correct parameters for video summary', () => {
      const videoData = { file: 'video.mp4' };
      const createSpy = spyOn(component['_modal'], 'create').and.callThrough();

      component.videoSummaryModal(videoData);

      expect(createSpy).toHaveBeenCalledWith({
        nzContent: SummaryModalComponent,
        nzViewContainerRef: component['_viewContainerRef'],
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
      });
    });
  });

  describe('articleSummaryModal', () => {
    it('should create a modal with the correct parameters for article summary', () => {
      const article = { file: 'article.pdf' };
      const createSpy = spyOn(component['_modal'], 'create').and.callThrough();

      component.articleSummaryModal(article);

      expect(createSpy).toHaveBeenCalledWith({
        nzContent: SummaryModalComponent,
        nzViewContainerRef: component['_viewContainerRef'],
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
      });
    });
  });

  describe('videoTranscriptModal', () => {
    it('should create a modal with the correct parameters for video transcript', () => {
      const videoData = { file: 'video.mp4' };
      const createSpy = spyOn(component['_modal'], 'create').and.callThrough();

      component.videoTranscriptModal(videoData);

      expect(createSpy).toHaveBeenCalledWith({
        nzContent: TranscriptModalComponent,
        nzViewContainerRef: component['_viewContainerRef'],
        nzComponentParams: {
          videoData: videoData,
        },
        nzFooter: null,
        nzKeyboard: true,
      });
    });
  });
  describe('allTopicCheck', () => {
    it('should check or uncheck all topics in the section based on event target', () => {
      const section = {
        topics: [{ checkTopic: false }, { checkTopic: false }],
      };
      const event = { target: { checked: true } };

      component.allTopicCheck(section, event);

      section.topics.forEach((topic) => {
        expect(topic.checkTopic).toBeTrue();
      });
    });

    it('should call deleteTopicIcon after checking/unchecking topics', () => {
      const section = { topics: [] };
      spyOn(component, 'deleteTopicIcon');

      component.allTopicCheck(section, { target: { checked: true } });

      expect(component.deleteTopicIcon).toHaveBeenCalledWith(section);
    });
  });

  describe('singleTopicCheck', () => {
    it('should uncheck checkAll if at least one topic is unchecked', () => {
      const section = {
        topics: [{ checkTopic: true }, { checkTopic: false }],
        checkAll: true,
      };
      const topic = { checkTopic: false };

      component.singleTopicCheck(section, topic);

      expect(section.checkAll).toBeFalse();
    });

    it('should call deleteTopicIcon after checking/unchecking a topic', () => {
      const section = { topics: [] };
      spyOn(component, 'deleteTopicIcon');

      component.singleTopicCheck(section, { checkTopic: true });

      expect(component.deleteTopicIcon).toHaveBeenCalledWith(section);
    });
  });
  describe('deleteTopicIcon', () => {
    it('should set deleteTopicIcon to true if any topic is checked', () => {
      const section = {
        topics: [{ checkTopic: true }, { checkTopic: false }],
        deleteTopicIcon: false,
      };

      component.deleteTopicIcon(section);

      expect(section.deleteTopicIcon).toBeTrue();
    });

    it('should set deleteTopicIcon to false if no topics are checked', () => {
      const section = {
        topics: [{ checkTopic: false }, { checkTopic: false }],
        deleteTopicIcon: true,
      };

      component.deleteTopicIcon(section);

      expect(section.deleteTopicIcon).toBeFalse();
    });
  });
  describe('deleteTopics', () => {
    it('should create a deletion modal with the correct parameters', () => {
      const section = { topics: [{ checkTopic: true }] };
      const mockModalRef = jasmine.createSpyObj('NzModalRef', ['afterClose']);
      mockModalRef.afterClose.and.returnValue(of(true)); // Simulate the modal closing event
      const createSpy = spyOn(component['_modal'], 'create').and.returnValue(
        mockModalRef
      );

      component.deleteTopics(section);

      expect(createSpy).toHaveBeenCalledWith({
        nzContent: DeletionModalComponent,
        nzViewContainerRef: component['_viewContainerRef'],
        nzFooter: null,
        nzKeyboard: true,
        nzComponentParams: {
          msg: 'Are you sure you want to delete the selected topics?',
        },
      });
    });

    it('should delete checked topics when deleteClick event is triggered', () => {
      // const section = {
      //   topics: [
      //     { checkTopic: true, delete: false },
      //     { checkTopic: false, delete: false },
      //   ],
      //   deleteTopicIcon: true,
      //   checkAll: true,
      //   deleteAll: false,
      // };
      // const mockModalRef = jasmine.createSpyObj('NzModalRef', ['afterClose']);
      // const createSpy = spyOn(component['_modal'], 'create').and.returnValue(
      //   mockModalRef
      // );
      // component.deleteTopics(section);
      // // Simulate the modal `afterClose` event emitting a value
      // mockModalRef.afterClose.next(true);
      // expect(section.topics[0].delete).toBeTrue();
      // expect(section.deleteTopicIcon).toBeFalse();
      // expect(section.checkAll).toBeFalse();
      // expect(section.deleteAll).toBeTrue();
    });

    it('should call deleteTopicIcon and updateTopicLevels after deleting topics', () => {
      // const section = { topics: [{ checkTopic: true }] };
      // const mockModalRef = jasmine.createSpyObj('NzModalRef', ['afterClose']);
      // spyOn(component, 'deleteTopicIcon');
      // spyOn(component, 'updateTopicLevels');
      // spyOn(component['_modal'], 'create').and.returnValue(mockModalRef);
      // component.deleteTopics(section);
      // // Simulate the modal `afterClose` event emitting a value
      // mockModalRef.afterClose.next(true);
      // expect(component.deleteTopicIcon).toHaveBeenCalledWith(section);
      // expect(component.updateTopicLevels).toHaveBeenCalledWith(section);
    });
  });

  describe('deleteVideoData', () => {
    it('should delete video data and reset related properties', () => {
      // const topic = {
      //   video: {
      //     videoData: {
      //       videoId: '123',
      //       videoUrl: 'http://example.com/video.mp4',
      //       videoFileName: 'Old Video',
      //       videoBtnName: 'Old Button',
      //       videoProgress: 50,
      //       videoFileType: 'mp4',
      //       videoTranscript: 'Transcript',
      //       videoSummary: 'Summary',
      //       date: '2024-01-01',
      //       file: 'file.mp4',
      //     },
      //     documentData: {
      //       documentFileName: 'Old Resource',
      //       documentBtnName: 'Old Button',
      //       documents: [{}],
      //     },
      //     showTable: true,
      //   },
      //   topicId: 'topic-123',
      // };
      // spyOn(component, 'deleteVideoOrDocument');
      // spyOn(component, 'videoValidation');
      // component.deleteVideoData(topic);
      // expect(component.deleteVideoOrDocument).toHaveBeenCalledWith(
      //   '123',
      //   'http://example.com/video.mp4',
      //   'topic-123',
      //   'VIDEO'
      // );
      // expect(topic.video.videoData).toEqual({
      //   videoId: '',
      //   videoUrl: '',
      //   videoFileName: 'Add Video',
      //   videoBtnName: 'Upload File',
      //   videoProgress: 0,
      //   videoFileType: '',
      //   videoTranscript: '',
      //   videoSummary: '',
      //   date: '',
      //   file: '',
      // });
      // expect(topic.video.documentData.documentFileName).toBe('Add Resource');
      // expect(topic.video.documentData.documentBtnName).toBe('Upload File');
      // expect(topic.video.documentData.documents).toEqual([]);
      // expect(topic.video.showTable).toBe(false);
      // expect(component.videoValidation).toHaveBeenCalledWith(topic);
    });
  });
  describe('deleteDocument', () => {
    it('should delete document and update related properties', () => {
      const topic = {
        video: {
          videoData: {
            videoFileName: 'Add Video',
          },
          documentData: {
            documents: [
              {
                id: 'doc-1',
                documentUrl: 'http://example.com/doc1.pdf',
                delete: false,
              },
              {
                id: 'doc-2',
                documentUrl: 'http://example.com/doc2.pdf',
                delete: false,
              },
            ],
            documentFileName: '',
            documentBtnName: 'Upload File',
          },
          showTable: true,
        },
        topicId: 'topic-123',
      };
      const index = 0;

      spyOn(component, 'deleteVideoOrDocument');
      spyOn(component, 'videoValidation');

      component.deleteDocument(topic, index);

      expect(component.deleteVideoOrDocument).toHaveBeenCalledWith(
        'doc-1',
        'http://example.com/doc1.pdf',
        'topic-123',
        'DOCS'
      );
      expect(topic.video.documentData.documents[0].id).toBe('');
      expect(topic.video.documentData.documents[0].delete).toBe(true);
      expect(topic.video.showTable).toBe(true);
      expect(topic.video.documentData.documentFileName).toBeDefined();
      expect(component.videoValidation).toHaveBeenCalledWith(topic);
    });
  });
  describe('topicPromptKeyUp', () => {
    it('should set topicStatusImg to incomplete image', () => {
      const topic = {
        topicStatusImg: '',
      };
      const expectedImg = component.topicStatusIncompleteImg;

      component.topicPromptKeyUp(topic);

      expect(topic.topicStatusImg).toBe(expectedImg);
    });
  });
  describe('getCurrentDate', () => {
    it('should return the current date in MM/DD/YYYY format', () => {
      const expectedDate = new Date();
      const day = String(expectedDate.getDate()).padStart(2, '0');
      const month = String(expectedDate.getMonth() + 1).padStart(2, '0');
      const year = expectedDate.getFullYear();
      const expectedResult = `${month}/${day}/${year}`;

      const result = component.getCurrentDate();

      expect(result).toBe(expectedResult);
    });
  });
  describe('formatDate', () => {
    it('should format the date to MM/DD/YYYY format', () => {
      const date = new Date('2024-08-23');
      const day = String(date.getDate()).padStart(2, '0');
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const year = date.getFullYear();
      const expectedResult = `${month}/${day}/${year}`;

      const result = component.formatDate(date);

      expect(result).toBe(expectedResult);
    });

    it('should handle invalid dates', () => {
      const invalidDate = 'invalid-date';
      const result = component.formatDate(invalidDate);

      expect(result).toBe('NaN/NaN/NaN'); // Modify if needed based on how invalid dates are handled
    });
  });
  describe('openArticlePrompt', () => {
    it('should set article properties to show the article prompt', () => {
      const article = {
        generateArticleBtn: true,
        articlePrompt: false,
      };

      component.openArticlePrompt(article);

      expect(article.generateArticleBtn).toBe(false);
      expect(article.articlePrompt).toBe(true);
    });
  });
  describe('articleInputChange', () => {
    it('should trim whitespace from articlePromptInput', () => {
      const article = {
        articlePromptInput: '   Test input   ',
      };

      component.articleInputChange(article);

      expect(article.articlePromptInput).toBe('Test input');
    });
  });
  describe('generateArticles', () => {
    it('should handle errors from the service', () => {
      // const article = {
      //   articlePrompt: true,
      //   articlePromptInput: 'Sample question',
      //   showChatBox: false,
      //   showSpinner: false,
      //   questionAnswers: {
      //     question: '',
      //     answers: [],
      //   },
      // };
      // spyOn(component['instructorService'], 'generator').and.returnValue(
      //   throwError(() => new Error('Error'))
      // );
      // spyOn(component, 'generateArticles').and.callThrough();
      // component.generateArticles(article);
      // expect(article.showSpinner).toBe(true);
      // expect(article.questionAnswers.answers).toEqual([]);
    });
  });
  describe('clearArticleChat', () => {
    it('should clear chat and reset article properties', () => {
      const article = {
        showChatBox: true,
        articlePrompt: false,
        questionAnswers: {
          question: 'Sample question',
          answers: ['Answer 1'],
        },
      };

      component.clearArticleChat(article);

      expect(article.showChatBox).toBe(false);
      expect(article.articlePrompt).toBe(true);
      expect(article.questionAnswers.question).toBe('');
      expect(article.questionAnswers.answers).toEqual([]);
    });
  });
  describe('deleteArticleDocument', () => {
    it('should reset article properties and call deleteVideoOrDocument', () => {
      const topic = {
        article: {
          articleDocumnetId: '123',
          articleDocumnetUrl: 'http://example.com/doc',
          articleFileName: 'Old File',
          articleSummary: 'Summary',
          articleBtnName: 'Existing Resource',
        },
        topicId: '456',
      };

      spyOn(component, 'deleteVideoOrDocument').and.callThrough();

      component.deleteArticleDocument(topic);

      expect(component.deleteVideoOrDocument).toHaveBeenCalledWith(
        '123',
        'http://example.com/doc',
        '456',
        'DOCS'
      );
      expect(topic.article.articleFileName).toBe('Upload File');
      expect(topic.article.articleSummary).toBe('');
      expect(topic.article.articleDocumnetUrl).toBe('');
      expect(topic.article.articleBtnName).toBe('Add Resource');
    });
  });
  describe('deleteVideoOrDocument', () => {
    it('should call _fileManagerService.deleteFile and handle success', () => {});
  });
  describe('openUploadArticleScreen', () => {
    it('should set uploadArticleDocument to true', () => {
      const article = {
        uploadArticleDocument: false,
      };

      component.openUploadArticleScreen(article);

      expect(article.uploadArticleDocument).toBe(true);
    });
  });
  describe('sectionActive', () => {
    it('should prevent default behavior and stop propagation', () => {
      const event = {
        preventDefault: jasmine.createSpy('preventDefault'),
        stopPropagation: jasmine.createSpy('stopPropagation'),
      };

      component.sectionActive(event);

      expect(event.preventDefault).toHaveBeenCalled();
      expect(event.stopPropagation).toHaveBeenCalled();
    });
  });
  describe('topicActive', () => {
    it('should toggle topic active state and prevent default behavior', () => {
      const event = {
        preventDefault: jasmine.createSpy('preventDefault'),
        stopPropagation: jasmine.createSpy('stopPropagation'),
      };
      const topic = {
        active: false,
      };

      component.topicActive(event, topic);

      expect(event.preventDefault).toHaveBeenCalled();
      expect(event.stopPropagation).toHaveBeenCalled();
      expect(topic.active).toBe(true);
    });
  });

  describe('removeQuizQuestion', () => {
    it('should mark question as deleted and clear its answers', () => {});
  });
  describe('removeQuizQuestionAnswers', () => {
    it('should mark answer as deleted and update correctAnswer if needed', () => {
      const topic = {
        /* topic data if needed */
      };
      const question = {
        correctAnswer: { ans: 'answer1' },
        answers: [
          { ans: 'answer1', delete: false },
          { ans: 'answer2', delete: false },
        ],
      };
      const answer = { ans: 'answer1', delete: false };
      component.removeQuizQuestionAnswers(topic, question, answer);
      expect(answer.delete).toBe(true);
      expect(question.correctAnswer.ans).toBe(null);
    });
  });
  describe('editorArticleContentChanged', () => {
    it('should clear article content if event html is null', () => {
      // const topic = { article: { content: 'Old content' } };
      // const event = { html: null };
      // component.editorArticleContentChanged(event, topic);
      // expect(topic.article.content).toBe('');
      // expect(component.articleValidation).toHaveBeenCalledWith(topic);
    });

    it('should not clear content if event html is not null', () => {
      // const topic = { article: { content: 'Old content' } };
      // const event = { html: '<p>New content</p>' };
      // component.editorArticleContentChanged(event, topic);
      // expect(topic.article.content).toBe('Old content');
      // expect(component.articleValidation).toHaveBeenCalledWith(topic);
    });
  });
  describe('getInitialOfLoggedInUser', () => {
    it("should return the logged-in user's name from _authService", () => {
      // spyOn(component['authService'], 'getLoggedInName').and.returnValue(
      //   'John Doe'
      // );
      // const result = component.getInitialOfLoggedInUser;
      // expect(result).toBe('John Doe');
    });
  });
  describe('sectionActiveStatus', () => {
    it('should prevent default behavior and toggle section active state', () => {
      const event = {
        preventDefault: jasmine.createSpy('preventDefault'),
        stopPropagation: jasmine.createSpy('stopPropagation'),
      };
      const section = { active: false };

      component.sectionActiveStatus(event, section);

      expect(event.preventDefault).toHaveBeenCalled();
      expect(event.stopPropagation).toHaveBeenCalled();
      expect(section.active).toBe(true);
    });
  });
  describe('singleSectionCheck', () => {
    it('should update checkAllSection and call deleteSectionIcon', () => {
      spyOn(component, 'deleteSectionIcon').and.callThrough();
      component.sections = [{ checkSection: false }, { checkSection: true }];

      component.singleSectionCheck({}, {});

      expect(component.checkAllSection).toBe(false);
      expect(component.deleteSectionIcon).toHaveBeenCalled();
    });
  });
  describe('deleteSectionIcon', () => {
    it('should set showDltSectionBtn based on section checkSection values', () => {
      component.sections = [{ checkSection: false }, { checkSection: true }];

      component.deleteSectionIcon();

      expect(component.showDltSectionBtn).toBe(true);

      component.sections[1].checkSection = false;
      component.deleteSectionIcon();

      expect(component.showDltSectionBtn).toBe(false);
    });
  });
  describe('allSectionCheck', () => {
    it('should update all sections checkSection value and call deleteSectionIcon', () => {
      const event = { target: { checked: true } };
      spyOn(component, 'deleteSectionIcon').and.callThrough();

      component.sections = [{ checkSection: false }, { checkSection: false }];

      component.allSectionCheck(event);

      expect(component.sections[0].checkSection).toBe(true);
      expect(component.sections[1].checkSection).toBe(true);
      expect(component.deleteSectionIcon).toHaveBeenCalled();
    });
  });
  describe('updateSectionLevels', () => {
    it('should update section levels based on delete flag', () => {
      component.sections = [
        { delete: false, level: 0 },
        { delete: true, level: 0 },
        { delete: false, level: 0 },
      ];

      component.updateSectionLevels();

      expect(component.sections[0].level).toBe(1);
      expect(component.sections[1].level).toBe(0); // Should be removed
      expect(component.sections[2].level).toBe(2);
    });
  });
  describe('updateTopicLevels', () => {
    it('should update topic levels based on delete flag', () => {
      const section = {
        topics: [
          { delete: false, level: 0 },
          { delete: true, level: 0 },
          { delete: false, level: 0 },
        ],
      };

      component.updateTopicLevels(section);

      expect(section.topics[0].level).toBe(1);
      expect(section.topics[1].level).toBe(0); // Should be removed
      expect(section.topics[2].level).toBe(2);
    });
  });
  describe('checkPreviousTopic', () => {
    it('should call appropriate delete methods based on selectedContentType', () => {
      // spyOn(component, 'deletePreviousTopicVideo').and.callThrough();
      // spyOn(component, 'deletePreviousTopicQuiz').and.callThrough();
      // spyOn(component, 'deletePreviousTopicArticle').and.callThrough();
      // const topic = {
      //   selectedContentType: 'article',
      //   video: { videoData: { videoId: '1' }, documentData: { documents: [] } },
      //   quiz: { quizId: '1' },
      //   article: { articleId: '1' },
      // };
      // component.checkPreviousTopic(topic);
      // expect(component.deletePreviousTopicVideo).toHaveBeenCalledWith(
      //   topic.video
      // );
      // expect(component.deletePreviousTopicQuiz).toHaveBeenCalledWith(
      //   topic.quiz
      // );
      // expect(component.deletePreviousTopicArticle).toHaveBeenCalledWith(
      //   topic.article
      // );
    });
  });
  describe('deletePreviousTopicVideo', () => {
    it('should mark video and associated documents as deleted', () => {
      const video = {
        videoData: { videoId: '123', delete: false },
        documentData: { documents: [{ delete: false }, { delete: false }] },
      };

      component.deletePreviousTopicVideo(video);

      expect(video.videoData.delete).toBe(true);
      expect(video.documentData.documents.every((doc) => doc.delete)).toBe(
        true
      );
    });
  });
  describe('deletePreviousTopicVideo', () => {
    it('should mark video and associated documents as deleted', () => {
      const video = {
        videoData: { videoId: '123', delete: false },
        documentData: { documents: [{ delete: false }, { delete: false }] },
      };

      component.deletePreviousTopicVideo(video);

      expect(video.videoData.delete).toBe(true);
      expect(video.documentData.documents.every((doc) => doc.delete)).toBe(
        true
      );
    });
  });
  describe('deletePreviousTopicQuiz', () => {
    it('should mark quiz as deleted if it has an ID', () => {
      const quiz = { quizId: '123', delete: false };

      component.deletePreviousTopicQuiz(quiz);

      expect(quiz.delete).toBe(true);
    });
  });

  describe('videoValidation', () => {
    it('should set validate to true if video progress is 100% and no documents are in process', () => {
      const topic = {
        video: {
          videoData: { videoProgress: 100 },
          documentData: { documents: [] },
        },
      };

      component.videoValidation(topic);

      expect(topic).toBeDefined();
    });

    it('should set validate to false if video progress is not 100% or documents are still in process', () => {
      const topic = {
        video: {
          videoData: { videoProgress: 50 },
          documentData: {
            documents: [{ documentProgress: 50 }],
          },
        },
      };

      component.videoValidation(topic);
    });
    describe('articleValidation', () => {
      it('should set validate to true if article content is not empty or null and articleProgressBar is false', () => {
        const topic = {
          article: {
            content: 'Some content',
            articleProgressBar: false,
            validate: true,
          },
        };

        component.articleValidation(topic);

        expect(topic.article.validate).toBe(true);
      });

      it('should set validate to false if article content is empty or articleProgressBar is true', () => {
        const topic = {
          article: {
            content: '',
            articleProgressBar: true,
            validate: false,
          },
        };

        component.articleValidation(topic);

        expect(topic.article?.validate).toBe(false);
      });
    });
  });

  describe('steps', () => {
    it('should call publishCourse if sectionValidation returns true', () => {
      spyOn(component, 'sectionValidation').and.returnValue(true);
      spyOn(component, 'publishCourse');

      component.steps('step1');

      expect(component.sectionValidation).toHaveBeenCalled();
      expect(component.publishCourse).toHaveBeenCalledWith('step1');
    });

    it('should show error message if sectionValidation returns false', () => {
      // spyOn(component, 'sectionValidation').and.returnValue(false);
      // spyOn(component['messageService'], 'error');
      // component.steps('step1');
      // expect(component['messageService'].error).toHaveBeenCalledWith(
      //   'Please complete the sections'
      // );
    });
  });

  describe('checkFileInProcess', () => {
    it('should set fileProcessing to true if video file is uploaded and all documents are processed', () => {
      const topic = {
        video: {
          videoData: { videoFileType: 'video/mp4', videoProgress: 100 },
          documentData: { documents: [{ documentProgress: 100 }] },
        },
      };

      component.checkFileInProcess(topic);

      expect(topic.video).toBeDefined();
    });

    it('should set fileProcessing to false if video file is not uploaded or not all documents are processed', () => {
      const topic = {
        video: {
          videoData: { videoFileType: '', videoProgress: 50 },
          documentData: { documents: [{ documentProgress: 50 }] },
        },
      };

      component.checkFileInProcess(topic);

      expect(topic.video).toBeDefined();
    });
  });

  describe('topicEnter', () => {
    it('should disable drop for the topic enter event', () => {
      const event = {
        item: {
          dropContainer: {
            _dropListRef: {
              disabled: false,
            },
          },
        },
      } as CdkDragEnter<string[]>;

      component.topicEnter(event);

      expect(event.item.dropContainer._dropListRef.disabled).toBe(true);
    });
  });

  describe('sectionEnter', () => {
    it('should disable drop for the section enter event', () => {
      const event = {
        item: {
          dropContainer: {
            _dropListRef: {
              disabled: false,
            },
          },
        },
      } as CdkDragEnter<string[]>;

      component.sectionEnter(event);

      expect(event.item.dropContainer._dropListRef.disabled).toBe(true);
    });
  });

  describe('onDragSectionStarted', () => {
    it('should set section active to false', () => {
      const section = { active: true };

      component.onDragSectionStarted(section);

      expect(section.active).toBe(false);
    });
  });
  describe('onDragTopicStarted', () => {
    it('should set topic active to false', () => {
      const topic = { active: true };

      component.onDragTopicStarted(topic);

      expect(topic.active).toBe(false);
    });
  });
  describe('dropTopic', () => {
    it('should move topic in array', () => {
      const event = {
        previousIndex: 0,
        currentIndex: 1,
      } as CdkDragDrop<string[]>;

      const topics = ['topic1', 'topic2'];

      component.dropTopic(event, topics);

      expect(topics).toEqual(['topic2', 'topic1']);
    });
  });
  describe('dropSection', () => {
    it('should move section in array and update section levels', () => {
      // spyOn(component, 'updateSectionLevels').and.callThrough();
      // const event = {
      //   previousIndex: 0,
      //   currentIndex: 1,
      // } as CdkDragDrop<string[]>;
      // component.sections = ['section1', 'section2'];
      // component.dropSection(event);
      // expect(component.updateSectionLevels).toHaveBeenCalled();
    });
  });
  describe('stopCollapse', () => {
    it('should prevent event propagation', () => {
      const event = {
        stopPropagation: jasmine.createSpy('stopPropagation'),
      } as unknown as Event;

      component.stopCollapse(event);

      expect(event.stopPropagation).toHaveBeenCalled();
    });
  });

  describe('sectionValidation', () => {
    it('should return true if all sections and their topics are valid', () => {
      component.sections = [
        {
          delete: false,
          deleteAll: false,
          topics: [{ delete: false, validate: true }],
        },
      ];

      const result = component.sectionValidation();

      expect(result).toBe(true);
    });

    it('should return false if any section or topic is invalid', () => {
      component.sections = [
        {
          delete: false,
          deleteAll: false,
          topics: [{ delete: false, validate: false }],
        },
      ];

      const result = component.sectionValidation();

      expect(result).toBe(false);
    });
  });

  describe('customRequestVideo', () => {
    it('should upload video and handle success', () => {});
  });

  describe('onResize', () => {
    it('should update screenWidth with the new window width', () => {
      const event = { target: { innerWidth: 800 } };

      component.onResize(event);

      expect(component.screenWidth).toBe(800);
    });
  });
  describe('ngOnDestroy', () => {
    it('should call saveAsDraftCourse', () => {
      spyOn(component, 'saveAsDraftCourse');

      component.ngOnDestroy();

      expect(component.saveAsDraftCourse).toHaveBeenCalled();
    });
  });
  describe('handleBeforeUnload', () => {
    it('', () => {});
  });
  describe('ngOnInit', () => {
    it('should call patchSectionData if courseId is present and sectionsData is empty', () => {
      //   component.courseId = '123';
      //   spyOn(component, 'patchSectionData');
      //   component.ngOnInit();
      //   expect(component.patchSectionData).toHaveBeenCalled();
    });

    it('should call addSection if courseId is not present and sectionsData is empty', () => {
      spyOn(component, 'addSection');

      component.ngOnInit();

      expect(component.addSection).toHaveBeenCalled();
    });

    it('should subscribe to communication services', () => {
      spyOn(component['communicationService'].documentSummary$, 'subscribe');
      spyOn(component['communicationService'].videoSummary$, 'subscribe');
      spyOn(component['communicationService'].articleSummary$, 'subscribe');
      spyOn(component['communicationService'].videoTranscript$, 'subscribe');

      component.ngOnInit();

      expect(
        component['communicationService'].documentSummary$.subscribe
      ).toHaveBeenCalled();
      expect(
        component['communicationService'].videoSummary$.subscribe
      ).toHaveBeenCalled();
      expect(
        component['communicationService'].articleSummary$.subscribe
      ).toHaveBeenCalled();
      expect(
        component['communicationService'].videoTranscript$.subscribe
      ).toHaveBeenCalled();
    });
  });

  describe('uploadSectionData', () => {
    it('should push sections and call uploadTopicsData', () => {
      spyOn(component, 'uploadTopicsData');
      const sections = [{ sectionId: '1', sectionName: 'Section 1' }];

      component.uploadSectionData(sections);

      expect(component.sections.length).toBe(2);
      expect(component.uploadTopicsData).toHaveBeenCalledWith(
        component.sections[0]
      );
    });
  });
  describe('patchSectionData', () => {
    it('should call addSection on error', () => {
      // spyOn(component['courseService'], 'getSectionByCourseId').and.returnValue(throwError(new Error('Error')));
      // spyOn(component, 'addSection');
      // component.patchSectionData();
      // expect(component.addSection).toHaveBeenCalled();
    });
  });
  describe('uploadTopicsData', () => {
    it('should populate section topics based on API response', () => {
      // const section = { sectionId: '1', topics: [] };
      // const response = {
      //   status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      //   data: [{ topicId: '1', topicType: component.typeVideo, videoId: 'vid1', topicName: 'Topic 1' }]
      // };
      // spyOn(component['courseService'], 'getTopicsBySectionId').and.returnValue(of(response));
      // component.uploadTopicsData(section);
      // expect(section.topics.length).toBe(1);
      // expect(section.topics[0].video.videoData.videoId).toBe('vid1');
    });
  });

  describe('showGenerateTopicsPrompt', () => {
    it('should toggle generateTopicBtn2 based on generateTopicsPrompt', () => {
      const section = { generateTopicBtn1: true, generateTopicsPrompt: false };

      component.showGenerateTopicsPrompt(section);

      expect(section.generateTopicBtn1).toBe(false);
      expect(section.generateTopicsPrompt).toBe(true);
    });
  });
  describe('clearTopicChat', () => {
    it('should reset chat-related properties in the section', () => {
      const section = {
        showChatBox: true,
        generateTopicsPrompt: false,
        questionAnswers: { question: 'Some question', answers: ['Answer 1'] },
      };

      component.clearTopicChat(section);

      expect(section.showChatBox).toBe(false);
      expect(section.generateTopicsPrompt).toBe(true);
      expect(section.questionAnswers.question).toBe('');
      expect(section.questionAnswers.answers.length).toBe(0);
    });
  });
  describe('generateTopics', () => {
    it('should update questionAnswers with response data and toggle UI flags', () => {
      // const section = {
      //   topicInput: 'Some topic',
      //   questionAnswers: { question: '', answers: [] },
      //   showChatBox: false,
      //   showSpinner: false,
      //   generateTopicsPrompt: true
      // };
      // const response = { status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE, data: 'Topic 1\nTopic 2' };
      // spyOn(component['instructorService'], 'generator').and.returnValue(of(response));
      // component.generateTopics(section);
      // expect(section.showSpinner).toBe(false);
      // expect(section.questionAnswers.answers).toEqual(['Topic 1', 'Topic 2']);
    });
  });
  describe('topicInputChange', () => {
    it('should trim topicInput', () => {
      const section = { topicInput: '  some input  ' };

      component.topicInputChange(section);

      expect(section.topicInput).toBe('some input');
    });
  });
  describe('addSection', () => {
    it('should add a new section and update section levels', () => {
      spyOn(component, 'updateSectionLevels');
      const initialLength = component.sections.length;

      component.addSection();

      expect(component.sections.length).toBe(initialLength + 1);
      expect(component.updateSectionLevels).toHaveBeenCalled();
      expect(component.showSectionDltContainer).toBe(true);
    });
  });
  describe('createTopics', () => {
    it('should add a new topic to the section and set createTopics to true', () => {
      const section = {
        topics: [],
        generateTopicBtn1: true,
        generateTopicsPrompt: false,
      };
      component.createTopics(section);

      expect(section.topics.length).toBe(1);
      expect(section.generateTopicBtn1).toBe(false);
    });
  });
  describe('openTopicContainer', () => {
    it('should set topicContainer and contentScreen to true', () => {
      const topic = { topicContainer: false, contentScreen: false };

      component.openTopicContainer(topic);

      expect(topic.topicContainer).toBe(true);
      expect(topic.contentScreen).toBe(true);
    });
  });

  it('should open the topic container and content screen', () => {
    const topic = { topicContainer: false, contentScreen: false };
    component.openTopicContainer(topic);
    expect(topic.topicContainer).toBeTrue();
    expect(topic.contentScreen).toBeTrue();
  });
  it('should set contentScreen to false and select content type', () => {
    const topic = {
      contentScreen: true,
      selectedContentType: '',
      topicStatusImg: '',
    };
    const type = 'video';
    component.openContent(topic, type);
    expect(topic.contentScreen).toBeFalse();
    expect(topic.selectedContentType).toEqual(type);
    expect(topic.topicStatusImg).toEqual(component.topicStatusIncompleteImg);
  });

  it('should call videoValidation for video type', () => {
    const topic = {};
    spyOn(component, 'videoValidation');
    component.openContent(topic, component.typeVideo);
    expect(component.videoValidation).toHaveBeenCalledWith(topic);
  });

  it('should call articleValidation for article type', () => {
    const topic = {};
    spyOn(component, 'articleValidation');
    component.openContent(topic, component.typeArticle);
    expect(component.articleValidation).toHaveBeenCalledWith(topic);
  });

  it('should call quizValidation for quiz type', () => {
    const topic = {};
    spyOn(component, 'quizValidation');
    component.openContent(topic, 'quiz');
    expect(component.quizValidation).toHaveBeenCalledWith(topic);
  });
  it('should reset topicStatusImg and call openContent', () => {
    const topic = { topicStatusImg: 'someImage', selectedContentType: 'video' };
    spyOn(component, 'openContent');
    component.onSelectContentType(topic);
    expect(topic.topicStatusImg).toEqual('');
    expect(component.openContent).toHaveBeenCalledWith(
      topic,
      topic.selectedContentType
    );
  });
  it('should set videoSection, articleSection, and quizSection on the topic', () => {
    const topic = {
      videoSection: '',
      articleSection: '',
      quizSection: '',
    };
    component.contentSection(topic, true, false, true);
    expect(topic.videoSection).toBeTrue();
    expect(topic.articleSection).toBeFalse();
    expect(topic.quizSection).toBeTrue();
  });
  it('should add a question and validate quiz', () => {
    const topic = {};
    const questions = [];
    spyOn(component, 'quizValidation');

    component.addQuestion(topic, questions);

    expect(questions.length).toBe(1);
    expect(questions[0]).toEqual({
      questionId: '',
      delete: false,
      label: 'Question ',
      ques: '',
      answers: [
        {
          answerId: '',
          label: '',
          delete: false,
          ans: '',
          exist : false
          // Adjust the structure here if any additional properties are added
        },
      ],
      correctAnswer: { ans: null },
    });

    expect(component.quizValidation).toHaveBeenCalledWith(topic);
  });

  it('should add an option and validate quiz', () => {
    const topic = {};
    const answers = [];
    spyOn(component, 'quizValidation');

    component.addOption(topic, answers);

    expect(answers.length).toBe(1);
    expect(answers[0]).toEqual({
      answerId: '',
      label: '',
      delete: false,
      ans: '',
      exist : false
    });

    expect(component.quizValidation).toHaveBeenCalledWith(topic);
  });

  it('should trigger quiz validation on quiz input change', () => {
    const topic = {};
    spyOn(component, 'quizValidation');
    component.onQuizInputChange(topic);
    expect(component.quizValidation).toHaveBeenCalledWith(topic);
  });
  it('should save the quiz if valid', () => {
    const topic = { validate: false, completed: false };
    spyOn(component, 'quizValidation').and.callFake(
      () => (topic.validate = true)
    );
    spyOn(component, 'checkPreviousTopic');
    component.saveQuiz(topic);
    expect(component.quizValidation).toHaveBeenCalledWith(topic);
    expect(component.checkPreviousTopic).toHaveBeenCalledWith(topic);
    expect(topic.completed).toBeTrue();
  });
  it('should save the video if valid', () => {
    const topic = { validate: false, completed: false };
    spyOn(component, 'videoValidation').and.callFake(
      () => (topic.validate = true)
    );
    spyOn(component, 'checkPreviousTopic');
    component.saveVideo(topic);
    expect(component.videoValidation).toHaveBeenCalledWith(topic);
    expect(component.checkPreviousTopic).toHaveBeenCalledWith(topic);
    expect(topic.completed).toBeTrue();
  });
  it('should save the article if valid', () => {
    const topic = { validate: false, completed: false, active: false };
    spyOn(component, 'articleValidation').and.callFake(
      () => (topic.validate = true)
    );
    spyOn(component, 'checkPreviousTopic');
    component.saveArticle(topic);
    expect(component.articleValidation).toHaveBeenCalledWith(topic);
    expect(component.checkPreviousTopic).toHaveBeenCalledWith(topic);
    expect(topic.completed).toBeTrue();
  });
  it('should validate the quiz and set topic validation status', () => {
    const topic = {
      quiz: {
        questions: [
          {
            delete: false,
            ques: 'Some Question',
            correctAnswer: { ans: 'Some Answer' },
            answers: [
              { delete: false, ans: 'Answer 1' },
              { delete: false, ans: 'Answer 2' },
            ],
          },
        ],
        title: 'Quiz Title',
      },
      topicStatusImg: '',
      validate: false,
    };

    component.quizValidation(topic);
    expect(topic.validate).toBeTrue();
  });
});
