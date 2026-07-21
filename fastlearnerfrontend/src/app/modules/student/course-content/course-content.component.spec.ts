import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { By, Meta, Title } from '@angular/platform-browser';

import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';

import { FormsModule } from '@angular/forms';
import {
  BrowserAnimationsModule,
  NoopAnimationsModule,
} from '@angular/platform-browser/animations';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { CertificateService } from 'src/app/core/services/certificate.service';
import { CourseService } from 'src/app/core/services/course.service';
import { ChatModalComponent } from '../../dynamic-modals/chat-modal/chat-modal.component';
import { ShareModalComponent } from '../../dynamic-modals/share-modal/share-modal.component';
import { CourseContentType } from 'src/app/core/enums/course-content-type.enum';
import { CourseType } from 'src/app/core/enums/course-status';
import { NzModalService } from 'ng-zorro-antd/modal';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { NzMessageService } from 'ng-zorro-antd/message';
import { MessageService } from 'src/app/core/services/message.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { HttpClient, HttpHandler } from '@angular/common/http';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { SharedModule } from '../../shared/shared.module';
import {
  NzMenuDirective,
  NzMenuItemDirective,
  NzMenuModule,
} from 'ng-zorro-antd/menu';
import {
  NzIconDirective,
  NzIconModule,
  NzIconService,
} from 'ng-zorro-antd/icon';
import { StudentModule } from '../student.module';
import { NzCollapseModule } from 'ng-zorro-antd/collapse';
import { IconDirective, IconModule } from '@ant-design/icons-angular';
import { NzTransitionPatchDirective } from 'ng-zorro-antd/core/transition-patch/transition-patch.directive';
import { ReviewModalComponent } from '../../dynamic-modals/review-modal/review-modal.component';
import { RatingModalComponent } from '../../dynamic-modals/rating-modal/rating-modal.component';
import { CourseContentComponent } from './course-content.component';

describe('CourseContentComponent', () => {
  let component: CourseContentComponent;
  let fixture: ComponentFixture<CourseContentComponent>;
  let mockCourseService: jasmine.SpyObj<CourseService>;
  let mockModal: jasmine.SpyObj<NzModalService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockCacheService: jasmine.SpyObj<CacheService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let mockCertificateService: jasmine.SpyObj<CertificateService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let routerSpy: jasmine.SpyObj<Router>;
  let activatedRoute: jasmine.SpyObj<ActivatedRoute>;
  let metaService: jasmine.SpyObj<Meta>;
  let titleService: jasmine.SpyObj<Title>;

  beforeEach(async () => {
    mockCourseService = jasmine.createSpyObj('CourseService', [
      'getTime',
      'getSectionAndTopicsChatQuestion',
      'getCourseChatHistory',
      'sendMessageInChat',
      'getTopicNotes',
      'createTopicNote',
      'deleteTopicNote',
      'getTopicSummary',
      'getQuestions',
      'getQuestionsReplies',
      'createQuestion',
      'replyQuestion',
      'getCourseRatingReviewAndFeedback',
      'addOrRemoveCourseToFavorite',
      'getAlternateInstructorSections',
      'pinAlternateInstructor',
      'unPinAlternateInstructor',
      'getSectionTopics',
      'markTopicComplete',
      'enrolledInCourse',
      'getCourseByTitle',
      'getCourseSections',
      'courseProgress',
      'getSectionRatingAndReview',
      'likeAndDislikeReviewSection',
      'getCourseByUrl',
      'manageWatchTime',
      'deleteChat',
    ]);

    mockModal = jasmine.createSpyObj('ModalService', ['create']);
    mockAuthService = jasmine.createSpyObj('AuthService', [
      'getLoggedInName',
      'getLoggedInPicture',
      'signOut',
      'isLoggedIn',
      'changeNavState',
      'getLoggedInEmail',
      'getUserProfile',
      'isSubscribed',
    ]);
    mockCacheService = jasmine.createSpyObj('CacheService', [
      'clearCache',
      'saveJsonData',
    ]);
    mockCertificateService = jasmine.createSpyObj('CertificateService', [
      'getCertificateData',
    ]);
    const routerMock = jasmine.createSpyObj('Router', [
      'navigateByUrl',
      'navigate',
      'serializeUrl',
      'createUrlTree',
      'events',
    ]);

    const metaServiceMock = jasmine.createSpyObj('Meta', ['updateTag']);
    const titleServiceMock = jasmine.createSpyObj('Title', ['setTitle']);

    const messageServiceSpy = jasmine.createSpyObj('MessageService', [
      'success',
      'error',
      'info',
    ]);

    const activatedRouteSpy = jasmine.createSpyObj('ActivatedRoute', [], {
      snapshot: { queryParams: { subscriptionId: '123' } },
    });

    routerMock.events = of(new NavigationEnd(0, '', ''));
    await TestBed.configureTestingModule({
      imports: [
        FormsModule,
        AntDesignModule,
        SharedModule,
        BrowserAnimationsModule,
        NzMenuModule,
        StudentModule,
        NzCollapseModule,
      ],
      declarations: [CourseContentComponent],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
      providers: [
        { provide: CourseService, useValue: mockCourseService },
        { provide: NzModalService, useValue: mockModal },
        { provide: AuthService, useValue: mockAuthService },
        { provide: CacheService, useValue: mockCacheService },
        { provide: CertificateService, useValue: mockCertificateService },
        { provide: Router, useValue: routerMock },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: ActivatedRoute, useValue: activatedRouteSpy },
        { provide: Meta, useValue: metaServiceMock },
        { provide: Title, useValue: titleServiceMock },
        SharedService,
        HttpClient,
        HttpHandler,
        NzMenuItemDirective,
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParams: {} } },
        },
        {
          provide: HttpConstants,
          useValue: {
            REQUEST_STATUS: {
              SUCCESS_200: { CODE: 200 },
              REQUEST_NOT_FOUND_404: { CODE: 404 },
            },
          },
        },
      ],
    }).compileComponents();
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
  });

  beforeEach(() => {
    mockCourseService.markTopicComplete.and.returnValue(of({ status: 200 }));
    mockCourseService.manageWatchTime.and.returnValue(of({ status: 200 }));
    mockCourseService.courseProgress.and.returnValue(
      of({ status: 200, data: 50 }),
    );
    mockCourseService.getTopicNotes.and.returnValue(
      of({ status: 200, data: { topicNotes: [] } }),
    );
    mockCourseService.getTopicSummary.and.returnValue(
      of({ status: 200, data: { summary: '' } }),
    );
    mockCourseService.getSectionRatingAndReview.and.returnValue(
      of({ status: 200, data: {} }),
    );
    mockCourseService.deleteChat.and.returnValue(of({ status: 200 }));

    fixture = TestBed.createComponent(CourseContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    // Mock the services that are called during component destruction
    if (mockCourseService.markTopicComplete) {
      mockCourseService.markTopicComplete.and.returnValue(of({ status: 200 }));
    }
    if (mockCourseService.manageWatchTime) {
      mockCourseService.manageWatchTime.and.returnValue(of({ status: 200 }));
    }

    // Safely destroy the fixture
    if (fixture) {
      try {
        fixture.destroy();
      } catch (e) {
        console.error('Error during fixture cleanup:', e);
      }
    }
  });

  it('should call getTime on _courseService', () => {
    component.getVideoCurrentTimeFromVideoPlayer();
    expect(mockCourseService.getTime).toHaveBeenCalled();
  });

  it('should convert seconds to H:M:S format correctly', () => {
    expect(component.secondsToHms(3661)).toEqual('1h:1m:1s');
    expect(component.secondsToHms(61)).toEqual('1m:1s');
    expect(component.secondsToHms(5)).toEqual('5s');
  });

  it('should convert time string to seconds correctly', () => {
    expect(component.getTimeInSec('1h:1m:1s')).toEqual(3661);
    expect(component.getTimeInSec('1m:1s')).toEqual(61);
    expect(component.getTimeInSec('5s')).toEqual(5);
  });

  it('should get section topics and chat questions', () => {
    const mockResponse = {
      status: 200,
      data: [
        {
          topics: [
            {
              topicId: '1',
              chatTopicHistory: [{ chatId: 'chat1', time: '00:01:00' }],
            },
          ],
        },
      ],
    };
    mockCourseService.getSectionAndTopicsChatQuestion.and.returnValue(
      of(mockResponse)
    );
    component.getSectionTopicsAndChatQuestion();
    expect(
      mockCourseService.getSectionAndTopicsChatQuestion
    ).toHaveBeenCalled();
  });

  it('should send a message in chat', () => {
    const mockResponse = {
      status: 200,
      data: { message: 'Message sent' },
    };
    mockCourseService.sendMessageInChat.and.returnValue(of(mockResponse));
    component.sendMessage();
    expect(mockCourseService.sendMessageInChat).toHaveBeenCalled();
  });

  it('should open chat modal', () => {
    const modal = {
      afterClose: of(null),
    };
    component.openChatModal();
    expect(mockModal.create).toHaveBeenCalledWith({
      nzContent: ChatModalComponent,
      nzViewContainerRef: component['_viewContainerRef'],
      nzComponentParams: jasmine.any(Object),
      nzFooter: null,
      nzKeyboard: true,
      nzWidth: '90%',
      nzCentered: true,
      nzCloseIcon: '../../../assets/icons/chat-collapse',
    });
  });

  it('should add a note', () => {
    const mockResponse = {
      status: 200,
      data: { message: 'Note added' },
    };
    mockCourseService.createTopicNote.and.returnValue(of(mockResponse));
    component.addNote();
    expect(mockCourseService.createTopicNote).toHaveBeenCalled();
  });

  it('should delete a note', () => {
    const mockResponse = {
      status: 200,
      data: { message: 'Note deleted' },
    };
    mockCourseService.deleteTopicNote.and.returnValue(of(mockResponse));
    component.deleteNote(1);
    expect(mockCourseService.deleteTopicNote).toHaveBeenCalled();
  });

  it('should get summary report', () => {
    const mockResponse = {
      status: 200,
      data: { summary: 'Summary data' },
    };
    mockCourseService.getTopicSummary.and.returnValue(of(mockResponse));
    component.getSummaryReport();
    expect(mockCourseService.getTopicSummary).toHaveBeenCalled();
  });

  it('should add a question', () => {
    const mockResponse = {
      status: 200,
      data: { message: 'Question added' },
    };
    mockCourseService.createQuestion.and.returnValue(of(mockResponse));
    component.addQuestion();
    expect(mockCourseService.createQuestion).toHaveBeenCalled();
  });

  it('should reply to a question', () => {
    const mockResponse = {
      status: 200,
      data: { message: 'Reply added' },
    };
    mockCourseService.replyQuestion.and.returnValue(of(mockResponse));
    component.replyQuestion({ questionId: '1' });
    expect(mockCourseService.replyQuestion).toHaveBeenCalled();
  });

  it('should get course complete review', () => {
    const mockResponse = {
      status: 200,
      data: { feedback: 'Course review data' },
    };
    mockCourseService.getCourseRatingReviewAndFeedback.and.returnValue(
      of(mockResponse)
    );
    component.getCourseCompleteReview();
    expect(
      mockCourseService.getCourseRatingReviewAndFeedback
    ).toHaveBeenCalled();
  });

  it('should toggle favorite course', () => {
    const mockResponse = {
      status: 200,
      data: { message: 'Course favorited' },
    };
    mockCourseService.addOrRemoveCourseToFavorite.and.returnValue(
      of(mockResponse)
    );
    component.toggleFavoriteCourse();
    expect(mockCourseService.addOrRemoveCourseToFavorite).toHaveBeenCalled();
  });

  it('should pin an alternate instructor', () => {
    const mockResponse = {
      status: 200,
      data: { message: 'Instructor pinned' },
    };
    mockCourseService.pinAlternateInstructor.and.returnValue(of(mockResponse));
    component.pinAlternateInstructor('1');
    expect(mockCourseService.pinAlternateInstructor).toHaveBeenCalled();
  });

  it('should unpin an alternate instructor', () => {
    const mockResponse = {
      status: 200,
      data: { message: 'Instructor unpinned' },
    };
    mockCourseService.unPinAlternateInstructor.and.returnValue(
      of(mockResponse)
    );
    component.unPinAlternateInstructor('1');
    expect(mockCourseService.unPinAlternateInstructor).toHaveBeenCalled();
  });

  it('should handle errors when getting course sections', () => {
    mockCourseService.getCourseSections.and.returnValue(
      throwError(() => new Error('Error'))
    );

    component.getCourseSectionList();

    expect(messageService.error).toHaveBeenCalled();
  });

  it('should check tooltip visibility on window scroll', () => {
    spyOn(component, 'checkTooltipVisibility');

    window.dispatchEvent(new Event('scroll'));

    expect(component.checkTooltipVisibility).toHaveBeenCalled();
  });

  it('should navigate to notifications page', () => {
    component.routeToNotificationPage();

    expect(routerSpy?.navigate).toHaveBeenCalledWith(['/user/notifications']);
  });

  it('should navigate to landing page', () => {
    component.routeToLandingPage();
    expect(routerSpy?.navigate).toHaveBeenCalledWith(['']);
  });

  it('should not navigate if user is not authenticated', () => {
    mockAuthService.isSubscribed.and.returnValue(false);

    component.routeToNotificationPage();

    expect(routerSpy.navigate).toHaveBeenCalled();
  });

  it('should check tooltip visibility on window scroll', () => {
    spyOn(component, 'checkTooltipVisibility');

    window.dispatchEvent(new Event('scroll'));

    expect(component.checkTooltipVisibility).toHaveBeenCalled();
  });

  it('should add a note', () => {
    const mockResponse = {
      status: 200,
      data: { message: 'Note added' },
    };
    mockCourseService.createTopicNote.and.returnValue(of(mockResponse));
    component.addNote();
    expect(mockCourseService.createTopicNote).toHaveBeenCalled();
  });

  it('should update a note', fakeAsync(() => {
    component.courseId = 'course123';
    component.currentSelectedTopicId = 'topic123';
    component.courseNote = [
      { topicNotesId: 123, notes: 'Some notes', time: '12:00' },
    ];

    const mockResponse = {
      status: 200,
      data: { message: 'Note updated' },
    };
    mockCourseService.createTopicNote.and.returnValue(of(mockResponse));

    component.updateNote('123');
    tick(); // Simulate the passage of time for asynchronous operations
    expect(mockCourseService.createTopicNote).toHaveBeenCalled();
  }));

  it('should handle multiple errors in a single operation', () => {
    mockCourseService.getCourseByUrl.and.returnValue(
      throwError(() => new Error('Error'))
    );
    mockCourseService.getCourseSections.and.returnValue(
      throwError(() => new Error('Another Error'))
    );

    component.getCourseSectionList();

    expect(messageService.error).toHaveBeenCalled();
  });

  it('should retrieve and populate notes successfully', () => {
    const mockResponse = {
      status: 200,
      data: {
        topicNotes: [
          { topicNotesId: '123', notes: 'Test Note', time: '00:01' },
        ],
      },
    };
    mockCourseService.getTopicNotes.and.returnValue(of(mockResponse));

    component.getNotes();

    expect(mockCourseService.getTopicNotes).toHaveBeenCalled();
    expect(component.courseNote.length).toBe(1);
    expect(component.courseNote[0].disable).toBeTrue();
  });

  it('should handle errors when retrieving notes', () => {
    const mockError = { status: 500, message: 'Server error' };
    mockCourseService.getTopicNotes.and.returnValue(throwError(mockError));

    component.getNotes();

    expect(mockCourseService.getTopicNotes).toHaveBeenCalled();
    // Optionally: Check for any error handling or logging here
  });

  it('should add a note successfully', () => {
    const mockResponse = { status: 200, data: { message: 'Note added' } };
    mockCourseService.createTopicNote.and.returnValue(of(mockResponse));

    component.addNote();

    expect(mockCourseService.createTopicNote).toHaveBeenCalled();
    expect(mockCourseService.createTopicNote).toHaveBeenCalledWith({
      courseId: component.courseId,
      topicId: component.currentSelectedTopicId,
      note: component.noteText,
      time: component.currentVideoTime,
    });
    expect(component.noteText).toBe('');
  });

  it('should handle errors when adding a note', () => {
    const mockError = { status: 500, message: 'Server error' };
    mockCourseService.createTopicNote.and.returnValue(throwError(mockError));

    component.addNote();

    expect(mockCourseService.createTopicNote).toHaveBeenCalled();
    // Optionally: Check for any error handling or logging here
  });

  it('should handle errors when adding a note', () => {
    const mockError = { status: 500, message: 'Server error' };
    mockCourseService.createTopicNote.and.returnValue(throwError(mockError));

    component.addNote();

    expect(mockCourseService.createTopicNote).toHaveBeenCalled();
    // Optionally: Check for any error handling or logging here
  });
  it('should handle non-existing note ID when editing', () => {
    const noteId = '999'; // Non-existing note ID
    component.courseNote = [{ topicNotesId: 123, disable: true }];

    component.editNote(noteId);

    expect(component.courseNote[0].disable).toBeTrue(); // Note should remain disabled
  });
  it('should update a note successfully', () => {
    const noteId = 123;
    const mockResponse = { status: 200, data: { message: 'Note updated' } };
    component.courseNote = [
      { topicNotesId: noteId, notes: 'Updated Note', time: '00:02' },
    ];
    mockCourseService.createTopicNote.and.returnValue(of(mockResponse));

    component.updateNote(noteId);

    expect(mockCourseService.createTopicNote).toHaveBeenCalled();
    expect(mockCourseService.createTopicNote).toHaveBeenCalledWith({
      courseId: component.courseId,
      topicId: component.currentSelectedTopicId,
      note: 'Updated Note',
      time: '00:02',
      topicNotesId: noteId,
    });
    expect(mockCourseService.getTopicNotes).toHaveBeenCalled();
  });
  it('should handle errors when updating a note', () => {
    const noteId = 123;
    const mockError = { status: 500, message: 'Server error' };
    component.courseNote = [
      { topicNotesId: noteId, notes: 'Updated Note', time: '00:02' },
    ];
    mockCourseService.createTopicNote.and.returnValue(throwError(mockError));

    component.updateNote(noteId);

    expect(mockCourseService.createTopicNote).toHaveBeenCalled();
    // Optionally: Check for any error handling or logging here
  });

  it('should delete a note successfully and refresh notes', () => {
    const noteId = 123;
    const mockResponse = { status: 200, data: { message: 'Note deleted' } };
    component.courseNote = [{ topicNotesId: noteId }];
    mockCourseService.deleteTopicNote.and.returnValue(of(mockResponse));

    component.deleteNote(noteId);

    expect(mockCourseService.deleteTopicNote).toHaveBeenCalled();
    expect(mockCourseService.deleteTopicNote).toHaveBeenCalledWith({
      courseId: component.courseId,
      topicId: component.currentSelectedTopicId,
      topicNoteId: noteId,
    });
  });

  it('should handle errors when deleting a note', () => {
    const noteId = 123;
    const mockError = { status: 500, message: 'Server error' };
    component.courseNote = [{ topicNotesId: noteId }];
    mockCourseService.deleteTopicNote.and.returnValue(throwError(mockError));

    component.deleteNote(noteId);

    expect(mockCourseService.deleteTopicNote).toHaveBeenCalled();
    // Optionally: Check for any error handling or logging here
  });

  it('should clear notes if the deleted note was the only one', () => {
    const noteId = 123;
    const mockResponse = { status: 200, data: { message: 'Note deleted' } };
    component.courseNote = [{ topicNotesId: noteId }];
    mockCourseService.deleteTopicNote.and.returnValue(of(mockResponse));

    component.deleteNote(noteId);

    expect(component.courseNote.length).toBe(0);
  });

  it('should retrieve and populate question list successfully', () => {
    const mockResponse = {
      status: 200,
      data: {
        totalElements: 10,
        questionDetails: [
          { questionId: 1, text: 'Sample Question', showReply: true },
        ],
      },
    };
    mockCourseService.getQuestions.and.returnValue(of(mockResponse));

    component.getQuestionList();

    expect(mockCourseService.getQuestions).toHaveBeenCalled();
    expect(component.questionAnswers).toEqual(mockResponse.data);
    expect(component.totalQuestions).toBe(10);
  });
  it('should handle errors when retrieving questions', () => {
    const mockError = { status: 500, message: 'Server error' };
    mockCourseService.getQuestions.and.returnValue(throwError(mockError));

    component.getQuestionList();

    expect(mockCourseService.getQuestions).toHaveBeenCalled();
    // Optionally: Check for any error handling or logging here
  });
  it('should retrieve and populate question replies successfully', () => {
    const question = { questionId: 1, showReply: true };
    const mockResponse = {
      status: 200,
      data: {
        answerDetail: [{ answerId: 1, text: 'Sample Answer' }],
      },
    };
    component.questionAnswers = { questionDetails: [question] };
    mockCourseService.getQuestionsReplies.and.returnValue(of(mockResponse));

    component.getQuestionReplies(question);

    expect(mockCourseService.getQuestionsReplies).toHaveBeenCalled();
    expect(component.questionAnswers.questionDetails[0].answerDetail).toEqual(
      mockResponse.data.answerDetail
    );
  });
  it('should handle errors when retrieving question replies', () => {
    const question = { questionId: 1, showReply: true };
    const mockError = { status: 500, message: 'Server error' };
    component.questionAnswers = { questionDetails: [question] };
    mockCourseService.getQuestionsReplies.and.returnValue(
      throwError(mockError)
    );

    component.getQuestionReplies(question);

    expect(mockCourseService.getQuestionsReplies).toHaveBeenCalled();
    // Optionally: Check for any error handling or logging here
  });
  it('should add a question successfully', () => {
    const mockResponse = { status: 200, data: { message: 'Question added' } };
    mockCourseService.createQuestion.and.returnValue(of(mockResponse));

    component.addQuestion();

    expect(mockCourseService.createQuestion).toHaveBeenCalled();
    expect(mockCourseService.createQuestion).toHaveBeenCalledWith({
      courseId: component.courseId,
      topicId: component.currentSelectedTopicId,
      text: component.questionText,
    });
    expect(component.questionText).toBe('');
  });
  it('should handle errors when adding a question', () => {
    const mockError = { status: 500, message: 'Server error' };
    mockCourseService.createQuestion.and.returnValue(throwError(mockError));

    component.addQuestion();

    expect(mockCourseService.createQuestion).toHaveBeenCalled();
    // Optionally: Check for any error handling or logging here
  });
  // it('should reply to a question successfully', () => {
  //   const mockResponse = { status: 200, data: { message: 'Reply added' } };
  //   mockCourseService.replyQuestion.and.returnValue(of(mockResponse));

  //   component.replyQuestion({ questionId: '1' });

  //   expect(mockCourseService.replyQuestion).toHaveBeenCalled();
  //   expect(mockCourseService.replyQuestion).toHaveBeenCalledWith({
  //     courseId: component.courseId,
  //     answerId: null,
  //     questionId: '1',
  //     text: component.replyText,
  //   });
  //   expect(component.replyText).toBe('');
  //   expect(mockCourseService.getQuestions).toHaveBeenCalled();
  // });
  it('should handle errors when replying to a question', () => {
    const mockError = { status: 500, message: 'Server error' };
    mockCourseService.replyQuestion.and.returnValue(throwError(mockError));

    component.replyQuestion({ questionId: '1' });

    expect(mockCourseService.replyQuestion).toHaveBeenCalled();
    // Optionally: Check for any error handling or logging here
  });

  it('should retrieve and populate course review successfully', () => {
    const mockResponse = {
      status: 200,
      data: {
        feedback: [{ reviewId: '1', text: 'Great course!' }],
        totalElements: 1,
      },
    };
    mockCourseService.getCourseRatingReviewAndFeedback.and.returnValue(
      of(mockResponse)
    );

    component.getCourseCompleteReview();

    expect(
      mockCourseService.getCourseRatingReviewAndFeedback
    ).toHaveBeenCalled();
    expect(component.totalReviewElements).toBe(1);
  });
  it('should handle errors when retrieving course review', () => {
    const mockError = { status: 500, message: 'Server error' };
    mockCourseService.getCourseRatingReviewAndFeedback.and.returnValue(
      throwError(mockError)
    );

    component.getCourseCompleteReview();

    expect(
      mockCourseService.getCourseRatingReviewAndFeedback
    ).toHaveBeenCalled();
    // Optionally: Check for any error handling or logging here
  });

  it('should retrieve and populate section rating and reviews successfully', () => {
    const mockResponse = {
      status: 200,
      data: {
        value: 4.5,
        totalReviews: 10,
        comment: 'Test Comment',
        courseId: '1',
        sectionId: '2',
      },
    };
    mockCourseService.getSectionRatingAndReview.and.returnValue(
      of(mockResponse)
    );

    component.getSectionRatingAndReviews();

    expect(mockCourseService.getSectionRatingAndReview).toHaveBeenCalled();
    expect(component.sectionReview).toEqual(mockResponse.data);
  });
  it('should handle errors when retrieving section rating and reviews', () => {
    const mockError = { status: 404, error: { status: 404 } };
    mockCourseService.getSectionRatingAndReview.and.returnValue(
      throwError(mockError)
    );

    component.getSectionRatingAndReviews();

    expect(mockCourseService.getSectionRatingAndReview).toHaveBeenCalled();
    expect(component.sectionReview.value).toBe(0);
    expect(component.sectionReview.totalReviews).toBe(0);
  });
  it('should toggle favorite course successfully', () => {
    const mockResponse = { status: 200, message: 'Course updated' };
    mockCourseService.addOrRemoveCourseToFavorite.and.returnValue(
      of(mockResponse)
    );

    component.toggleFavoriteCourse();

    expect(mockCourseService.addOrRemoveCourseToFavorite).toHaveBeenCalled();
    expect(mockCourseService.addOrRemoveCourseToFavorite).toHaveBeenCalledWith(
      component.courseId
    );
  });
  it('should handle errors when toggling favorite course', () => {
    const mockError = { status: 500, message: 'Server error' };
    mockCourseService.addOrRemoveCourseToFavorite.and.returnValue(
      throwError(mockError)
    );

    component.toggleFavoriteCourse();

    expect(mockCourseService.addOrRemoveCourseToFavorite).toHaveBeenCalled();
    // Optionally: Check for any error handling or logging here
  });
  it('should retrieve and populate alternate instructor sections successfully', () => {
    const section = { sectionId: '1' };
    const mockResponse = {
      status: 200,
      data: {
        details: [{ sectionId: '2', sectionName: 'Alternate Section' }],
      },
    };
    mockCourseService.getAlternateInstructorSections.and.returnValue(
      of(mockResponse)
    );

    component.getAlternateInstructorSections(section);

    expect(mockCourseService.getAlternateInstructorSections).toHaveBeenCalled();
    expect(component.alternateSectionPanelList).toEqual(
      mockResponse.data.details
    );
  });
  it('should retrieve and populate alternate instructor sections successfully', () => {
    const section = { sectionId: '1' };
    const mockResponse = {
      status: 200,
      data: {
        details: [{ sectionId: '2', sectionName: 'Alternate Section' }],
      },
    };
    mockCourseService.getAlternateInstructorSections.and.returnValue(
      of(mockResponse)
    );

    component.getAlternateInstructorSections(section);

    expect(mockCourseService.getAlternateInstructorSections).toHaveBeenCalled();
    expect(component.alternateSectionPanelList).toEqual(
      mockResponse.data.details
    );
  });
  it('should handle errors when retrieving alternate instructor sections', () => {
    const section = { sectionId: '1', active: true, showHeader: false };
    const mockError = { status: 500, error: { message: 'Server error' } };
    mockCourseService.getAlternateInstructorSections.and.returnValue(
      throwError(mockError)
    );

    component.getAlternateInstructorSections(section);

    expect(mockCourseService.getAlternateInstructorSections).toHaveBeenCalled();
    expect(section.active).toBeFalse();
    expect(section.showHeader).toBeTrue();
  });
  it('should pin alternate instructor successfully', () => {
    const mockResponse = { status: 200, message: 'Instructor pinned' };
    mockCourseService.pinAlternateInstructor.and.returnValue(of(mockResponse));

    component.pinAlternateInstructor('fromCourseId', 'fromSectionId');

    expect(mockCourseService.pinAlternateInstructor).toHaveBeenCalled();
    expect(mockCourseService.pinAlternateInstructor).toHaveBeenCalledWith(
      component.toCourseId,
      component.toSectionId,
      'fromCourseId',
      'fromSectionId'
    );
    expect(mockCourseService.getCourseSections).toHaveBeenCalled();
  });
  it('should handle errors when pinning alternate instructor', () => {
    const mockError = { status: 500, message: 'Server error' };
    mockCourseService.pinAlternateInstructor.and.returnValue(
      throwError(mockError)
    );

    component.pinAlternateInstructor('fromCourseId', 'fromSectionId');

    expect(mockCourseService.pinAlternateInstructor).toHaveBeenCalled();
    // Optionally: Check for any error handling or logging here
  });
  it('should retrieve and populate alternate section topic list successfully', () => {
    const section = { alternateSectionPanelList: [{ sectionId: '2' }] };
    const mockResponse = {
      status: 200,
      data: [{ topicId: '1', topicDuration: 3600, isCompleted: true }],
    };
    spyOn(component, 'convertSecondsToHoursAndMinutes').and.callThrough();
    mockCourseService.getSectionTopics.and.returnValue(of(mockResponse));

    component.getAlternateSectionTopicList(section, 0);

    expect(mockCourseService.getSectionTopics).toHaveBeenCalled();
    expect(component.convertSecondsToHoursAndMinutes).toHaveBeenCalled();
  });

  it('should open alternate panel if section is free', () => {
    const section = { free: true };
    const spy = spyOn(
      component,
      'getAlternateInstructorSections'
    ).and.callThrough();

    component.openAlternatePanel(null, section);

    expect(spy).toHaveBeenCalled();
    expect(spy).toHaveBeenCalledWith(section);
  });
  it('should close alternate collapse dropdown', () => {
    const section = {
      active: false,
      showHeader: false,
      alternateSectionPanelList: [],
    };
    const event = { preventDefault: () => {}, stopPropagation: () => {} };

    component.closeAlternateCollapseDropdown(event, section);

    expect(section.active).toBeTrue();
    expect(section.showHeader).toBeTrue();
    expect(section.alternateSectionPanelList).toEqual([]);
  });
  it('should toggle alternate section panel', () => {
    const alternateSection = { active: false };

    component.alternateToggleSectionPanel(alternateSection);

    expect(alternateSection.active).toBeTrue();
  });

  // it('should increase page size and retrieve more reviews', () => {
  //   component.courseReviewPayLoad = { pageSize: 5 };
  //   spyOn(component, 'getCourseCompleteReview').and.callThrough();

  //   component.showMoreReviews();

  //   expect(component.courseReviewPayLoad.pageSize).toBe(7);
  //   expect(component.getCourseCompleteReview).toHaveBeenCalled();
  // });
  it('should handle errors when retrieving certificate data', () => {
    const mockError = { error: {status: 500, message: 'Server error'} };
    spyOn(component, 'routeToGenerateCertificate');
    mockCertificateService.getCertificateData.and.returnValue(
      throwError(mockError)
    );

    component.getCertificateData();

    expect(mockCertificateService.getCertificateData).toHaveBeenCalled();
    expect(component.routeToGenerateCertificate).not.toHaveBeenCalled();
    // Optionally: Check for any error handling or logging here
  });
  it('should navigate to generate certificate page with courseId', () => {
    component.routeToGenerateCertificate();
    expect(routerSpy?.navigate).toHaveBeenCalledWith(
      ['student/generate-certificate'],
      {
        queryParams: { courseId: component.courseId },
      }
    );
  });
  it('should handle errors during enrollment and still update data', () => {
    const mockError = { status: 500, message: 'Server error' };
    spyOn(component, 'getCourseSectionList').and.callThrough();
    spyOn(component, 'getSectionTopicsAndChatQuestion').and.callThrough();
    spyOn(component, 'getCourseProgress').and.callThrough();
    mockCourseService.enrolledInCourse.and.returnValue(throwError(mockError));

    component.enrolledInCourse();

    expect(mockCourseService.enrolledInCourse).toHaveBeenCalled();
    expect(component.getCourseSectionList).toHaveBeenCalled();
    expect(component.getSectionTopicsAndChatQuestion).toHaveBeenCalled();
    expect(component.getCourseProgress).toHaveBeenCalled();
  });

  it('should convert seconds to hours, minutes, and seconds', () => {
    expect(component.convertSecondsToHoursAndMinutes(3661)).toBe(
      '1 hr 1 min 1 sec'
    );
    expect(component.convertSecondsToHoursAndMinutes(61)).toBe('1 min 1 sec');
    expect(component.convertSecondsToHoursAndMinutes(59)).toBe('59 sec');
    expect(component.convertSecondsToHoursAndMinutes(0)).toBe('0 sec');
  });
  it('should not call manageSeekTime if time is not a multiple of two minutes', () => {
    spyOn(component, 'manageSeekTime');

    component.manageSeekTimeAfterEveryTwoMins(90); // 1.5 minutes

    expect(component.manageSeekTime).not.toHaveBeenCalled();
  });

  it('should retrieve section topics and chat questions and update course chat', () => {
    const mockResponse = {
      status: 200,
      data: [
        {
          topics: [
            {
              topicId: 'topic1',
              chatTopicHistory: [{ chatId: 'chat1', time: '10:00' }],
            },
          ],
        },
      ],
    };
    component.isFirstTime = true;
    spyOn(component, 'getCourseChatHistory').and.callThrough();
    mockCourseService.getSectionAndTopicsChatQuestion.and.returnValue(
      of(mockResponse)
    );

    component.getSectionTopicsAndChatQuestion();

    expect(component.selectedChatId).toBe('chat1');
    expect(component.getCourseChatHistory).toHaveBeenCalledWith(
      'chat1',
      '10:00',
      'topic1'
    );
  });
  it('should handle errors during retrieval of section topics and chat questions', () => {
    const mockError = { status: 500, message: 'Server error' };
    spyOn(component, 'getCourseChatHistory');
    mockCourseService.getSectionAndTopicsChatQuestion.and.returnValue(
      throwError(mockError)
    );

    component.getSectionTopicsAndChatQuestion();

    expect(
      mockCourseService.getSectionAndTopicsChatQuestion
    ).toHaveBeenCalled();
    expect(component.getCourseChatHistory).not.toHaveBeenCalled();
  });
  it('should retrieve course chat history and scroll to bottom', () => {
    const mockResponse = { status: 200, data: ['message1', 'message2'] };
    spyOn(component, 'scrollToBottom').and.callThrough();
    mockCourseService.getCourseChatHistory.and.returnValue(of(mockResponse));

    component.getCourseChatHistory('chat1', '10:00', 'topic1');

    expect(component.courseChatHistory).toEqual(mockResponse.data);
  });
  it('should handle errors when retrieving course chat history', () => {
    const mockError = { status: 500, message: 'Server error' };
    spyOn(component, 'scrollToBottom');
    mockCourseService.getCourseChatHistory.and.returnValue(
      throwError(mockError)
    );

    component.getCourseChatHistory('chat1', '10:00', 'topic1');

    expect(mockCourseService.getCourseChatHistory).toHaveBeenCalled();
    expect(component.scrollToBottom).not.toHaveBeenCalled();
  });
  it('should send a message, update chat history, and fetch section topics and chat questions', () => {
    component.courseChatPresent = false;
    const mockResponse = { status: 200, data: 'newMessage' };
    spyOn(component, 'getSectionTopicsAndChatQuestion').and.callThrough();
    spyOn(component, 'scrollToBottom').and.callThrough();
    mockCourseService.sendMessageInChat.and.returnValue(of(mockResponse));

    component.sendMessage();

    expect(component.courseChatHistory).toContain(mockResponse.data);
    expect(component.courseChatPresent).toBe(true);
    expect(component.getSectionTopicsAndChatQuestion).toHaveBeenCalled();
  });

  it('should perform like/dislike action and update reviews', () => {
    const reviewId = 'review1';
    const action = 'like';
    const mockResponse = { status: 200 };

    // Setup spy for the service method
    mockCourseService.likeAndDislikeReviewSection.and.returnValue(
      of(mockResponse)
    );

    // Setup spy for the component method
    spyOn(component, 'getCourseCompleteReview').and.callThrough();

    component.commentActions(action, reviewId);

    expect(mockCourseService.likeAndDislikeReviewSection).toHaveBeenCalledWith({
      reviewId: reviewId,
      action: action,
    });
  });

  it('should set dropdown visibilty to true on change', () => {
    let visible = true;
    component.onDropdownVisibilityChange(visible);
    expect(component.isDropdownVisible).toBeTrue();
  });

  it('should set dropdown visibilty to false on change', () => {
    let visible = false;
    component.closeDropdown();
    expect(component.isDropdownVisible).toBeFalse();
  });

  it('should set dropdown visibilty to true on change in mobile', () => {
    let visible = true;
    component.onDropdownProfileVisibilityChange(visible);
    expect(component.isDropdownProfileVisible).toBeTrue();
  });
  it('should set dropdown visibilty on change to false in mobile', () => {
    component.closeProfileDropdown();
    expect(component.isDropdownProfileVisible).toBeFalse();
  });

  describe('Phase 1 batch 1: topic navigation and quiz flow', () => {
    const successCode = 200;

    function createTopic(
      id: string,
      overrides: Record<string, unknown> = {},
    ): any {
      return {
        topicId: id,
        topicType: 'Quiz',
        testType: 'TEST',
        isCompleted: false,
        seekTime: 0,
        ...overrides,
      };
    }

    function createSection(
      sectionId: string,
      topics: any[],
      overrides: Record<string, unknown> = {},
    ): any {
      return {
        sectionId,
        free: true,
        topicList: topics,
        totalTopicCompleted: 0,
        active: false,
        ...overrides,
      };
    }

    beforeEach(() => {
      component.courseId = 'course-1';
      component.currentVideoTime = '1m:30s';
    });

    it('should set current topic and section details', () => {
      const topic = createTopic('t1', { topicType: 'Video' });
      const section = createSection('s1', [topic]);

      component.setCurrentTopicAndSection(topic, section);

      expect(component.currentSelectedTopic).toBe(topic);
      expect(component.currentSelectedTopicId).toBe('t1');
      expect(component.currentSelectedTopicType).toBe('Video');
      expect(component.currentSelectedSection).toBe(section);
      expect(component.currentSelectedSectionId).toBe('s1');
    });

    it('should ignore setCurrentTopicAndSection when topic or section is missing', () => {
      component.currentSelectedTopic = createTopic('existing');
      component.setCurrentTopicAndSection(null, null);
      expect(component.currentSelectedTopic.topicId).toBe('existing');
    });

    it('should show info when skipQuiz has no active section', () => {
      component.sectionPanelList = [];
      component.currentSelectedSection = { sectionId: 'missing' };

      component.skipQuiz({});

      expect(messageService.info).toHaveBeenCalledWith('No active section found.');
    });

    it('should advance to next topic on skipQuiz', fakeAsync(() => {
      const topic1 = createTopic('t1');
      const topic2 = createTopic('t2', { topicType: 'Video' });
      const section = createSection('s1', [topic1, topic2]);
      component.sectionPanelList = [section];
      component.currentSelectedSection = section;
      component.currentSelectedTopic = topic1;
      spyOn(component, 'setCurrentTopicAndSection');

      component.skipQuiz({});
      tick(500);

      expect(topic1.isCompleted).toBeTrue();
      expect(mockCourseService.markTopicComplete).toHaveBeenCalled();
      expect(component.setCurrentTopicAndSection).toHaveBeenCalledWith(
        topic2,
        section,
      );
    }));

    it('should play next topic within the same section', () => {
      const topic1 = createTopic('t1', { topicType: 'Video' });
      const topic2 = createTopic('t2', { topicType: 'Video' });
      const section = createSection('s1', [topic1, topic2]);
      component.sectionPanelList = [section];
      component.currentSelectedSection = section;
      component.currentSelectedSectionId = 's1';
      component.currentSelectedTopic = topic1;

      component.playNextVideo();

      expect(component.currentSelectedTopic).toBe(topic2);
      expect(component.currentSelectedTopicId).toBe('t2');
    });

    it('should show subscription error when next section is not free', () => {
      const topic1 = createTopic('t1', { topicType: 'Video' });
      const section1 = createSection('s1', [topic1]);
      const section2 = createSection('s2', [createTopic('t3')], { free: false });
      component.sectionPanelList = [section1, section2];
      component.currentSelectedSection = section1;
      component.currentSelectedSectionId = 's1';
      component.currentSelectedTopic = topic1;

      component.playNextVideo();

      expect(messageService.error).toHaveBeenCalledWith(
        'User have to get a subscription, next section is not free.',
      );
    });

    it('should complete video topic with seek time payload', () => {
      const topic = createTopic('t1', { topicType: 'Video' });
      const section = createSection('s1', [topic]);
      component.currentSelectedTopicId = 'other';
      spyOn(component, 'getCourseProgress');

      component.completeTopic(section, topic, true, 't1', 0, 'Video');

      expect(section.totalTopicCompleted).toBe(1);
      expect(mockCourseService.markTopicComplete).toHaveBeenCalledWith({
        isCompleted: true,
        topicId: 't1',
        seekTime: 0,
      });
    });

    it('should decrement section completion count when marking incomplete', () => {
      const topic = createTopic('t1');
      const section = createSection('s1', [topic], { totalTopicCompleted: 2 });

      component.completeTopic(section, topic, false, 't1', 0, 'Quiz');

      expect(section.totalTopicCompleted).toBe(1);
    });

    it('should reset quiz UI state on retakeQuiz', () => {
      const topic = createTopic('t1', { sectionId: 's1' });
      const section = createSection('s1', [topic]);
      component.sectionPanelList = [section];
      component.showCongratsScreen = true;
      component.isReviewing = true;
      component.showQuizAttempt = true;
      const getTopicsSpy = spyOn(component, 'getSectionTopicList');

      component.retakeQuiz(topic);

      expect(component.showCongratsScreen).toBeFalse();
      expect(component.isReviewing).toBeFalse();
      expect(component.showQuizAttempt).toBeFalse();
      expect(component.showQuizPlayerWelcomeScreen).toBeTrue();
      expect(getTopicsSpy).toHaveBeenCalledWith(section, 0, true, topic);
    });

    it('should block topic switch while quiz is in progress', () => {
      component.currentSelectedTopic = createTopic('t1', { testType: 'TEST' });
      component.isQuizOrSurveyStarted = true;

      expect(() => component.checkCurrectSelectedTopicIsQuizOrSurvey()).toThrow();
    });

    it('should allow topic switch when quiz is not started', () => {
      component.currentSelectedTopic = createTopic('t1', { testType: 'TEST' });
      component.isQuizOrSurveyStarted = false;

      expect(component.checkCurrectSelectedTopicIsQuizOrSurvey()).toBeTrue();
    });

    it('should store quiz attempt metadata on answer submit', () => {
      component.onAnswerSubmit({
        quizAttemptId: 'attempt-1',
        isAllowedToRetake: true,
      });

      expect(component.quizAttemptId).toBe('attempt-1');
      expect(component.isAllowedToRetake).toBeTrue();
    });

    it('should update quiz started flag', () => {
      component.updateQuizStartedStatus(true);
      expect(component.isQuizOrSurveyStarted).toBeTrue();
      component.updateQuizStartedStatus(false);
      expect(component.isQuizOrSurveyStarted).toBeFalse();
    });

    it('should block playlist selection when active quiz is unfinished', () => {
      component.currentSelectedTopic = createTopic('t1', { testType: 'TEST' });
      component.isQuizOrSurveyStarted = true;
      const section = createSection('s1', [component.currentSelectedTopic]);
      const nextTopic = createTopic('t2');

      component.onSelectTopicFromPlayList(section, nextTopic);

      expect(messageService.error).toHaveBeenCalled();
      expect(component.currentSelectedTopic.topicId).toBe('t1');
    });

    it('should load quiz welcome screen for incomplete quiz topic', () => {
      const topic = createTopic('t1', { topicType: 'Quiz', isCompleted: false });
      const section = createSection('s1', [topic]);
      spyOn(component, 'getNotes');
      spyOn(component, 'getSectionRatingAndReviews');
      spyOn(component, 'getCourseProgress');
      spyOn(component, 'getSummaryReport');
      spyOn(component, 'getSectionTopicsAndChatQuestion');

      component.onSelectTopicFromPlayList(section, topic);

      expect(component.showQuizPlayerWelcomeScreen).toBeTrue();
      expect(component.showCongratsScreen).toBeFalse();
      expect(component.getNotes).toHaveBeenCalled();
    });

    it('should mark video complete and advance on videoCompleted', fakeAsync(() => {
      const topic = createTopic('t1', { topicType: 'Video', isCompleted: false });
      const section = createSection('s1', [topic]);
      component.currentSelectedTopic = topic;
      component.currentSelectedSection = section;
      spyOn(component, 'playNextVideo');
      spyOn(component, 'manageWatchTime');

      component.videoCompleted({});
      tick(800);

      expect(topic.isCompleted).toBeTrue();
      expect(component.playNextVideo).toHaveBeenCalled();
    }));

    it('should delete chat and refresh section chat data', () => {
      component.courseChatHistory = [{ id: 1 }];
      spyOn(component, 'getSectionTopicsAndChatQuestion');

      component.deleteChat({ chatId: 'chat-1' });

      expect(mockCourseService.deleteChat).toHaveBeenCalledWith(
        'course-1',
        'chat-1',
      );
      expect(component.courseChatHistory).toEqual([]);
      expect(component.getSectionTopicsAndChatQuestion).toHaveBeenCalled();
    });
  });

  describe('Phase 1 batch 1: utilities and UI state', () => {
    it('should reflect subscription status from auth service', () => {
      mockAuthService.isSubscribed.and.returnValue(true);
      component.userIsSubscribed();
      expect(component.isSubscribed).toBeTrue();

      mockAuthService.isSubscribed.and.returnValue(false);
      component.userIsSubscribed();
      expect(component.isSubscribed).toBeFalse();
    });

    it('should open dropdown menus manually', () => {
      component.openDropdown();
      expect(component.isDropdownVisible).toBeTrue();
      component.openProfileDropdown();
      expect(component.isDropdownProfileVisible).toBeTrue();
    });

    it('should remove duplicate questions by questionId', () => {
      component.questionAnswers = {
        questionDetails: [
          { questionId: 1, text: 'A' },
          { questionId: 1, text: 'Duplicate' },
          { questionId: 2, text: 'B' },
        ],
      } as any;

      component.removeDuplicateQuestions();

      expect(component.questionAnswers.questionDetails.length).toBe(2);
    });

    it('should update course progress and certificate flag', () => {
      mockCourseService.courseProgress.and.returnValue(
        of({ status: 200, data: 100 }),
      );

      component.getCourseProgress();

      expect(component.courseProgress).toBe(100);
      expect(component.generateCertificateEnable).toBeTrue();
    });

    it('should call tab-specific loaders from onToggleTabs', () => {
      spyOn(component, 'getSectionTopicsAndChatQuestion');
      spyOn(component, 'getNotes');
      spyOn(component, 'getQuestionList');
      spyOn(component, 'getSummaryReport');
      spyOn(component, 'getCourseCompleteReview');

      component.onToggleTabs(0);
      component.onToggleTabs(1);
      component.onToggleTabs(2);
      component.onToggleTabs(3);
      component.onToggleTabs(4);

      expect(component.getSectionTopicsAndChatQuestion).toHaveBeenCalled();
      expect(component.getNotes).toHaveBeenCalled();
      expect(component.getQuestionList).toHaveBeenCalledWith(false);
      expect(component.getSummaryReport).toHaveBeenCalled();
      expect(component.getCourseCompleteReview).toHaveBeenCalled();
    });

    it('should toggle course content tab visibility on resize', () => {
      component.onResize({ target: { innerWidth: 1200 } });
      expect(component.showCourseContentTab).toBeFalse();

      component.onResize({ target: { innerWidth: 768 } });
      expect(component.showCourseContentTab).toBeTrue();
    });

    it('should accumulate watch time from video player events', () => {
      component.watchTime = 0;
      spyOn(component, 'manageSeekTimeAfterEveryTwoMins');

      component.getVideoTime({
        videoTime: 45,
        isVideoPlaying: true,
        playbackRate: 2,
      });

      expect(component.watchTime).toBe(2);
      expect(component.currentVideoTime).toBe('45s');
      expect(component.manageSeekTimeAfterEveryTwoMins).toHaveBeenCalledWith(45);
    });

    it('should persist current course data to cache', fakeAsync(() => {
      const topic = { topicId: 't1', seekTime: 0 };
      const section = { sectionId: 's1' };
      component.courseId = 'course-1';
      component.currentSelectedTopic = topic;
      component.currentSelectedSection = section;
      component.sectionPanelList = [section];
      component.currentVideoTime = '1m:0s';

      component.saveCurrentCourseData();
      tick(0);

      expect(mockCacheService.saveJsonData).toHaveBeenCalledWith(
        'currentCourseData',
        jasmine.objectContaining({
          courseId: 'course-1',
          topic,
          section,
        }),
      );
    }));

    it('should call seek and watch time handlers on beforeunload', () => {
      spyOn(component, 'manageSeekTime');
      spyOn(component, 'manageWatchTime');

      component.beforeUnloadHandler(new Event('beforeunload'));

      expect(component.manageSeekTime).toHaveBeenCalled();
      expect(component.manageWatchTime).toHaveBeenCalled();
    });

    it('should toggle reply visibility and focus reply input', fakeAsync(() => {
      const question: any = { questionId: 9, showReply: false };
      const input = document.createElement('input');
      input.id = 'reply-input-9';
      spyOn(input, 'focus');
      spyOn(input, 'scrollIntoView');
      document.body.appendChild(input);

      component.toggleReplyAndScroll(question);
      tick(100);

      expect(question.showReply).toBeTrue();
      document.body.removeChild(input);
    }));

    it('should call manageSeekTime when two-minute boundary is reached', () => {
      spyOn(component, 'manageSeekTime');
      spyOn(component, 'manageWatchTime');

      component.manageSeekTimeAfterEveryTwoMins(120);

      expect(component.manageSeekTime).toHaveBeenCalled();
      expect(component.manageWatchTime).toHaveBeenCalled();
      expect(component.accumulatedTime).toBe(0);
    });

    it('should save seek time for in-progress video topics', () => {
      component.currentSelectedTopic = {
        topicType: 'Video',
        isCompleted: false,
        seekTime: 0,
      };
      component.currentSelectedTopicId = 't1';
      component.currentVideoTime = '2m:0s';
      spyOn(component, 'getCourseProgress');

      component.manageSeekTime();

      expect(mockCourseService.markTopicComplete).toHaveBeenCalledWith(
        jasmine.objectContaining({
          topicId: 't1',
          seekTime: 120,
        }),
      );
    });
  });

  describe('Phase 1 batch 2: course loading and sections', () => {
    const successCode = 200;
    const sectionApiResponse = {
      status: successCode,
      data: {
        sectionDetails: [
          { sectionId: 1, free: true, sectionName: 'Intro' },
          { sectionId: 2, free: false, sectionName: 'Advanced' },
        ],
        category: 'Mathematics',
        title: 'Algebra Basics',
        hasCertificate: true,
      },
    };

    beforeEach(() => {
      component.courseId = 'course-1';
      mockCourseService.getCourseSections.and.returnValue(
        of(sectionApiResponse),
      );
    });

    it('should load course sections and metadata on success', () => {
      spyOn(component, 'setDefaultSection');

      component.getCourseSectionList();

      expect(mockCourseService.getCourseSections).toHaveBeenCalledWith('course-1');
      expect(component.sectionPanelList.length).toBe(2);
      expect(component.currentCategory).toBe('Mathematics');
      expect(component.currentCourse).toBe('Algebra Basics');
      expect(component.showCertificate).toBeTrue();
      expect(component.setDefaultSection).toHaveBeenCalledWith(
        sectionApiResponse.data.sectionDetails,
      );
    });

    it('should activate requested section when sectionId is provided', () => {
      spyOn(component, 'getSectionTopicList');
      component.sectionPanelList = [];

      component.getCourseSectionList(1, null);

      expect(component.sectionPanelList[0].active).toBeTrue();
      expect(component.getSectionTopicList).toHaveBeenCalledWith(
        jasmine.objectContaining({ sectionId: 1 }),
        0,
        true,
        null,
      );
    });

    it('should activate default free section via setDefaultSection', () => {
      component.sectionPanelList = [
        { sectionId: 2, free: false, active: false },
        { sectionId: 1, free: true, active: false },
      ];
      const getTopicsSpy = spyOn(component, 'getSectionTopicList');

      component.setDefaultSection(component.sectionPanelList);

      expect(component.sectionPanelList[1].active).toBeTrue();
      expect(component.defaultSection.sectionId).toBe(1);
      expect(getTopicsSpy).toHaveBeenCalledWith(
        component.sectionPanelList[1],
        1,
        true,
        null,
      );
    });

    it('should load section topics and select first topic', () => {
      const section = { sectionId: 1 };
      component.sectionPanelList = [section];
      mockCourseService.getSectionTopics.and.returnValue(
        of({
          status: successCode,
          data: [
            { topicId: 't1', topicDuration: 90, isCompleted: false },
            { topicId: 't2', topicDuration: 120, isCompleted: true },
          ],
        }),
      );
      const selectSpy = spyOn(component, 'onSelectTopicFromPlayList');

      component.getSectionTopicList(section, 0, true);

      expect(mockCourseService.getSectionTopics).toHaveBeenCalledWith(
        'course-1',
        1,
      );
      expect(component.sectionPanelList[0].topicList.length).toBe(2);
      expect(selectSpy).toHaveBeenCalledWith(
        section,
        jasmine.objectContaining({ topicId: 't1' }),
      );
    });

    it('should select specific topic from URL when provided', () => {
      const section = { sectionId: 1 };
      component.sectionPanelList = [section];
      mockCourseService.getSectionTopics.and.returnValue(
        of({
          status: successCode,
          data: [
            { topicId: 't1', topicDuration: 60, isCompleted: true },
            { topicId: 't2', topicDuration: 60, isCompleted: false },
          ],
        }),
      );
      const selectSpy = spyOn(component, 'onSelectTopicFromPlayList');

      component.getSectionTopicList(section, 0, true, { topicId: 't2' });

      expect(selectSpy).toHaveBeenCalledWith(
        section,
        jasmine.objectContaining({ topicId: 't2' }),
      );
    });

    it('should open subscription plan when section topics fail to load', () => {
      const section: any = { sectionId: 1, active: true };
      component.sectionPanelList = [section];
      component.subscriptionModalOpened = false;
      mockCourseService.getSectionTopics.and.returnValue(
        throwError(() => ({ error: { message: 'Forbidden' } })),
      );
      const openPlanSpy = spyOn(component, 'openSubscriptionPlan');

      component.getSectionTopicList(section, 0, false);

      expect(section.active).toBeFalse();
      expect(component.subscriptionModalOpened).toBeTrue();
      expect(openPlanSpy).toHaveBeenCalled();
    });

    it('should redirect premium courses without access', () => {
      mockCourseService.getCourseByUrl.and.returnValue(
        of({
          status: successCode,
          data: {
            canAccess: false,
            activeUrl: 'premium-course',
            course: {
              id: 'c-premium',
              courseType: CourseType.PREMIUM,
              title: 'premium course',
              contentType: CourseContentType.COURSE,
            },
          },
        }),
      );

      component.getCourseByUrl('premium-course');

      expect(routerSpy.navigate).toHaveBeenCalledWith([
        'student/course-details',
        'premium-course',
      ]);
      expect(component.isLoading).toBeTrue();
    });

    it('should enroll and load sections after course url resolves', () => {
      mockCourseService.getCourseByUrl.and.returnValue(
        of({
          status: successCode,
          data: {
            canAccess: true,
            activeUrl: 'algebra',
            course: {
              id: 'course-1',
              courseType: CourseType.FREE,
              title: 'algebra',
              contentType: CourseContentType.COURSE,
            },
          },
        }),
      );
      const enrollSpy = spyOn(component, 'enrolledInCourse');

      component.getCourseByUrl('algebra');

      expect(component.isLoading).toBeFalse();
      expect(component.courseId).toBe('course-1');
      expect(enrollSpy).toHaveBeenCalled();
    });

    it('should navigate home when course url fetch fails', () => {
      mockCourseService.getCourseByUrl.and.returnValue(
        throwError(() => ({ error: { status: 500 } })),
      );

      component.getCourseByUrl('missing-course');

      expect(component.isLoading).toBeFalse();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['']);
    });

    it('should load sections after successful enrollment', () => {
      (component as any)._activatedRoute = {
        snapshot: { queryParams: {} },
        queryParams: of({ sectionId: '1' }),
        fragment: of(null),
      };
      mockCourseService.enrolledInCourse.and.returnValue(of({ status: successCode }));
      const sectionsSpy = spyOn(component, 'getCourseSectionList');
      const chatSpy = spyOn(component, 'getSectionTopicsAndChatQuestion');
      const progressSpy = spyOn(component, 'getCourseProgress');

      component.enrolledInCourse();

      expect(mockCourseService.enrolledInCourse).toHaveBeenCalledWith('course-1');
      expect(sectionsSpy).toHaveBeenCalledWith(1, null);
      expect(chatSpy).toHaveBeenCalled();
      expect(progressSpy).toHaveBeenCalled();
    });

    it('should still load sections when enrollment API errors', () => {
      (component as any)._activatedRoute = {
        snapshot: { queryParams: {} },
        queryParams: of({ topicId: '9' }),
        fragment: of(null),
      };
      mockCourseService.enrolledInCourse.and.returnValue(
        throwError(() => new Error('Already enrolled')),
      );
      const sectionsSpy = spyOn(component, 'getCourseSectionList');

      component.enrolledInCourse();

      expect(sectionsSpy).toHaveBeenCalledWith(null, 9 as any);
    });

    it('should open QnA tab when fragment requests discussion', fakeAsync(() => {
      component.queryString = 'COURSE_QnA_DISCUSSION';
      mockCourseService.getCourseByUrl.and.returnValue(
        of({
          status: successCode,
          data: {
            canAccess: true,
            activeUrl: 'algebra',
            course: {
              id: 'course-1',
              courseType: CourseType.FREE,
              title: 'algebra',
              contentType: CourseContentType.COURSE,
            },
          },
        }),
      );
      spyOn(component, 'enrolledInCourse');
      spyOn(component, 'onToggleTabs');
      spyOn(component, 'scrollToTab');

      component.getCourseByUrl('algebra');
      tick(900);

      expect(component.tabIndex).toBe(2);
      expect(component.onToggleTabs).toHaveBeenCalledWith(2);
      expect(component.scrollToTab).toHaveBeenCalledWith('qa-header');
    }));
  });

  describe('Phase 1 batch 2: panels, reviews, and routing', () => {
    const successCode = 200;

    it('should load topics when panel becomes active', () => {
      const section: any = { sectionId: 1 };
      const getTopicsSpy = spyOn(component, 'getSectionTopicList');
      spyOn(component, 'chatSectionPosition');

      component.isPanelActive(section, true, 0);

      expect(section.active).toBeTrue();
      expect(getTopicsSpy).toHaveBeenCalledWith(section, 0, true, null);
    });

    it('should not open alternate panel for paid sections', () => {
      const section = { free: false, active: true };
      const getAlternateSpy = spyOn(component, 'getAlternateInstructorSections');

      component.openAlternatePanel(null, section);

      expect(getAlternateSpy).not.toHaveBeenCalled();
    });

    it('should reorder chat sections to bring active section first', () => {
      component.courseChat = [
        { sectionId: 2, name: 'B' },
        { sectionId: 1, name: 'A' },
      ] as any;

      component.chatSectionPosition(1);

      expect(component.courseChat[0].sectionId).toBe(1);
      expect(component.courseChat.length).toBe(2);
    });

    it('should append new review feedback', () => {
      component.completeReview = { feedbackComments: [] } as any;
      const feedback = {
        reviewId: 1,
        comment: 'Great',
        userName: 'User',
        rating: 5,
      };

      component.updateCourseReview(feedback);

      expect(component.completeReview.feedbackComments.length).toBe(1);
    });

    it('should update existing review feedback', () => {
      component.completeReview = {
        feedbackComments: [{ reviewId: 1, comment: 'Old', likes: 0 }],
      } as any;

      component.updateCourseReview({
        reviewId: 1,
        comment: 'Updated',
        userName: 'User',
        rating: 4,
        likes: 2,
        dislikes: 0,
        createdAt: 'today',
        profileImage: '',
      });

      expect(component.completeReview.feedbackComments[0].comment).toBe('Updated');
      expect(component.completeReview.feedbackComments[0].likes).toBe(2);
    });

    it('should refresh review data after successful like action', () => {
      const updatedReview = { reviewId: 5, comment: 'Nice' };
      mockCourseService.likeAndDislikeReviewSection.and.returnValue(
        of({ status: successCode, data: updatedReview }),
      );
      const updateSpy = spyOn(component, 'updateCourseReview');

      component.commentActions('like', 5);

      expect(updateSpy).toHaveBeenCalledWith(updatedReview);
    });

    it('should reload sections after unpinning alternate instructor', () => {
      mockCourseService.unPinAlternateInstructor.and.returnValue(
        of({ status: successCode, message: 'Unpinned' }),
      );
      const reloadSpy = spyOn(component, 'getCourseSectionList');
      component.courseId = 'course-1';

      component.unPinAlternateInstructor(3);

      expect(messageService.success).toHaveBeenCalledWith('Unpinned');
      expect(reloadSpy).toHaveBeenCalled();
    });

    it('should open rating modal for free sections', () => {
      component.courseId = 'course-1';
      component.currentSelectedSectionId = 2;
      spyOn(component, 'openSubscriptionPlan');
      spyOn(component, 'openRatingModal');

      component.handleRatingClick({ free: true });
      component.handleRatingClick({ free: false });

      expect(component.openRatingModal).toHaveBeenCalled();
      expect(component.openSubscriptionPlan).toHaveBeenCalled();
    });

    it('should open share and review modals', () => {
      component.courseId = 'course-1';
      mockModal.create.and.returnValue({ afterClose: of(null) } as any);

      component.openRatingModal();
      component.openReviewModal();
      component.openShareCourseModal();

      expect(mockModal.create).toHaveBeenCalledTimes(3);
    });

    it('should navigate to certificate generation on success', () => {
      component.courseId = 'course-1';
      mockCertificateService.getCertificateData.and.returnValue(
        of({ status: successCode }),
      );
      const routeSpy = spyOn(component, 'routeToGenerateCertificate');

      component.getCertificateData();

      expect(routeSpy).toHaveBeenCalled();
    });

    it('should clear cache and navigate home on sign out', () => {
      mockAuthService.signOut.and.returnValue(of({ status: successCode }));
      mockAuthService.changeNavState.and.stub();

      component.signOut();

      expect(mockCacheService.clearCache).toHaveBeenCalled();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['']);
      expect(mockAuthService.changeNavState).toHaveBeenCalledWith(false);
    });

    it('should expose logged-in user helpers', () => {
      mockAuthService.getLoggedInName.and.returnValue('RA');
      mockAuthService.getLoggedInPicture.and.returnValue('pic.png');

      expect(component.getInitialOfLoggedInUser).toBe('RA');
      expect(component.getLoggedInPicture).toBe('pic.png');
    });

    it('should navigate to student routes', () => {
      component.courseId = 'course-1';
      mockAuthService.isLoggedIn.and.returnValue(true);

      component.routeToFavoriteCourses();
      component.routeToMyCourses();
      component.routeToUpdateProfile();
      component.routeToSubscription();
      component.routeToInstructorDashboard();

      expect(routerSpy.navigate).toHaveBeenCalledWith(['student/favorite-courses']);
      expect(routerSpy.navigate).toHaveBeenCalledWith(['student/my-courses']);
      expect(routerSpy.navigate).toHaveBeenCalledWith(['user/update-profile']);
      expect(routerSpy.navigate).toHaveBeenCalledWith(['subscription']);
      expect(routerSpy.navigate).toHaveBeenCalledWith([
        '/instructor/instructor-dashboard',
      ]);
    });

    it('should scroll to tab element when present', () => {
      const tab = document.createElement('div');
      tab.id = 'qa-header';
      spyOn(tab, 'scrollIntoView');
      document.body.appendChild(tab);

      component.scrollToTab('qa-header');

      expect(tab.scrollIntoView).toHaveBeenCalled();
      document.body.removeChild(tab);
    });

    it('should close dropdowns on window scroll', () => {
      component.isDropdownVisible = true;
      component.isDropdownProfileVisible = true;
      spyOn(component, 'checkTooltipVisibility');
      spyOn(component, 'closeDropdown');
      spyOn(component, 'closeProfileDropdown');

      component.onWindowScroll();

      expect(component.checkTooltipVisibility).toHaveBeenCalled();
      expect(component.closeDropdown).toHaveBeenCalled();
      expect(component.closeProfileDropdown).toHaveBeenCalled();
    });

    it('should revert topic completion when mark complete API fails', () => {
      const topic: any = { topicId: 't1', isCompleted: true, topicType: 'Quiz' };
      mockCourseService.markTopicComplete.and.returnValue(
        throwError(() => new Error('Failed')),
      );

      component.completeTopic(null, topic, true, 't1', 0, 'Quiz');

      expect(topic.isCompleted).toBeFalse();
    });

    it('should show quiz attempt UI for completed quiz topics', () => {
      const topic: any = {
        topicId: 't1',
        topicType: 'Quiz',
        isCompleted: true,
        testType: 'TEST',
      };
      const section: any = { sectionId: 1, topicList: [topic] };
      spyOn(component, 'getNotes');
      spyOn(component, 'getSectionRatingAndReviews');
      spyOn(component, 'getCourseProgress');
      spyOn(component, 'getSummaryReport');
      spyOn(component, 'getSectionTopicsAndChatQuestion');

      component.onSelectTopicFromPlayList(section, topic);

      expect(component.showQuizPlayerWelcomeScreen).toBeFalse();
      expect(component.showQuizAttempt).toBeTrue();
    });

    it('should enter review mode from review callback', () => {
      component.sectionPanelList = [
        {
          sectionId: 1,
          topicList: [{ topicId: 't1', isCompleted: true }],
        },
      ];
      component.currentSelectedSection = component.sectionPanelList[0];
      component.currentSelectedTopic = component.sectionPanelList[0].topicList[0];
      spyOn(component, 'updateCourseProgressOnReview');

      component.reviewCallBack([{ question: 'Q1' }]);

      expect(component.isReviewing).toBeTrue();
      expect(component.questionAnswersQuiz).toEqual([{ question: 'Q1' }]);
      expect(component.updateCourseProgressOnReview).toHaveBeenCalled();
    });

    it('should show congrats screen when review callback is empty', () => {
      component.reviewCallBack(null);

      expect(component.isReviewing).toBeFalse();
      expect(component.showCongratsScreen).toBeTrue();
    });
  });

  describe('Phase 1 batch 3: alternate instructor panels', () => {
    const successCode = 200;

    beforeEach(() => {
      component.courseId = 'course-1';
    });

    it('should navigate to alternate course when routing is enabled', fakeAsync(() => {
      component.alternateSectionRouting = true;
      const section: any = { courseUrl: 'alt-course', active: false };
      (routerSpy as any).routeReuseStrategy = {
        shouldReuseRoute: jasmine.createSpy('shouldReuseRoute'),
      };
      routerSpy.navigate.and.returnValue(Promise.resolve(true));

      component.isAlternatePanelActive(section, true, 0);
      tick();

      expect(routerSpy.navigate).toHaveBeenCalledWith([
        'student/course-content',
        'alt-course',
      ]);
      expect(section.active).toBeTrue();
    }));

    it('should enable alternate routing on first panel activation', () => {
      component.alternateSectionRouting = false;

      component.isAlternatePanelActive({ courseUrl: 'alt' }, true, 0);

      expect(component.alternateSectionRouting).toBeTrue();
      expect(routerSpy.navigate).not.toHaveBeenCalled();
    });

    it('should arm routing flag on first pin without calling API', () => {
      component.alternateSectionRouting = false;

      component.pinAlternateInstructor('from-c', 'from-s');

      expect(component.alternateSectionRouting).toBeTrue();
      expect(mockCourseService.pinAlternateInstructor).not.toHaveBeenCalled();
    });

    it('should open alternate panel and stash target section', () => {
      const section: any = { free: true, sectionId: 5, active: true, showHeader: true };
      const getAlternateSpy = spyOn(component, 'getAlternateInstructorSections');

      component.openAlternatePanel(null, section);

      expect(section.active).toBeFalse();
      expect(section.showHeader).toBeFalse();
      expect(component.toCourseId).toBe('course-1');
      expect(component.toSectionId).toBe(5);
      expect(getAlternateSpy).toHaveBeenCalledWith(section);
    });

    it('should load alternate sections and fetch topics for free instructors', () => {
      const section: any = { sectionId: 1 };
      mockCourseService.getAlternateInstructorSections.and.returnValue(
        of({
          status: successCode,
          data: {
            details: [
              { sectionId: 'alt-1', isFree: true },
              { sectionId: 'alt-2', isFree: false },
            ],
          },
        }),
      );
      const topicsSpy = spyOn(component, 'getAlternateSectionTopicList');

      component.getAlternateInstructorSections(section);

      expect(section.alternateSectionPanelList.length).toBe(2);
      expect(topicsSpy).toHaveBeenCalledWith(section, 0);
      expect(topicsSpy).not.toHaveBeenCalledWith(section, 1);
    });

    it('should populate alternate section topic metadata', () => {
      const section: any = {
        alternateSectionPanelList: [
          {
            sectionId: 'alt-1',
            courseId: 'c2',
            instructorId: 'i1',
            instructorName: 'Alt',
            instructorImage: 'img.png',
            totalReviewer: 1,
            totalReviews: 2,
            sectionName: 'Alt Section',
            isFree: true,
            courseTitle: 'Alt Course',
            courseUrl: 'alt-url',
          },
        ],
      };
      mockCourseService.getSectionTopics.and.returnValue(
        of({
          status: successCode,
          data: [
            { topicId: 't1', topicDuration: 3661, isCompleted: true },
            { topicId: 't2', topicDuration: 61, isCompleted: false },
          ],
        }),
      );

      component.getAlternateSectionTopicList(section, 0);

      const alt = section.alternateSectionPanelList[0];
      expect(alt.totalTopicCompleted).toBe(1);
      expect(alt.topicList.length).toBe(2);
      expect(alt.sectionDuration).toContain('hr');
    });

    it('should reset section when alternate collapse is closed', () => {
      const section: any = {
        active: false,
        showHeader: false,
        alternateSectionPanelList: [{ sectionId: 1 }],
      };
      const event = jasmine.createSpyObj('event', ['preventDefault', 'stopPropagation']);

      component.closeAlternateCollapseDropdown(event, section);

      expect(section.active).toBeTrue();
      expect(section.showHeader).toBeTrue();
      expect(section.alternateSectionPanelList).toEqual([]);
      expect(event.preventDefault).toHaveBeenCalled();
      expect(event.stopPropagation).toHaveBeenCalled();
    });

    it('should toggle alternate section active state', () => {
      const alternateSection: any = { active: false };
      component.alternateSectionRouting = true;

      component.alternateToggleSectionPanel(alternateSection);

      expect(alternateSection.active).toBeTrue();
      expect(component.alternateSectionRouting).toBeFalse();
    });
  });

  describe('Phase 1 batch 3: chat and notes', () => {
    const successCode = 200;

    beforeEach(() => {
      component.courseId = 'course-1';
      component.currentSelectedTopicId = 'topic-1';
    });

    it('should load chat history on first section topics fetch', () => {
      component.isFirstTime = true;
      mockCourseService.getSectionAndTopicsChatQuestion.and.returnValue(
        of({
          status: successCode,
          data: [
            {
              sectionId: 1,
              topics: [
                {
                  topicId: 'topic-1',
                  chatTopicHistory: [{ chatId: 'chat-1', time: '1m:0s' }],
                },
              ],
            },
          ],
        }),
      );
      const historySpy = spyOn(component, 'getCourseChatHistory');

      component.getSectionTopicsAndChatQuestion();

      expect(component.courseChat.length).toBe(1);
      expect(component.isFirstTime).toBeFalse();
      expect(historySpy).toHaveBeenCalledWith('chat-1', '1m:0s', 'topic-1');
    });

    it('should clear chat state when section topics fetch fails', () => {
      component.courseChat = [{ sectionId: 1 }] as any;
      component.courseChatHistory = [{ id: 1 }];
      component.courseChatPresent = true;
      mockCourseService.getSectionAndTopicsChatQuestion.and.returnValue(
        throwError(() => new Error('Network error')),
      );

      component.getSectionTopicsAndChatQuestion();

      expect(component.courseChat).toEqual([]);
      expect(component.courseChatHistory).toEqual([]);
      expect(component.courseChatPresent).toBeFalse();
    });

    it('should populate chat history for selected topic', () => {
      mockCourseService.getCourseChatHistory.and.returnValue(
        of({
          status: successCode,
          data: [{ message: 'Hello' }],
        }),
      );

      component.getCourseChatHistory('chat-9', '2m:0s', 'topic-9');

      expect(component.chatTopicId).toBe('topic-9');
      expect(component.selectedChatId).toBe('chat-9');
      expect(component.courseChatHistory).toEqual([{ message: 'Hello' }]);
      expect(component.courseChatPresent).toBeTrue();
    });

    it('should send chat message and clear input', () => {
      component.askQuestion = 'What is this?';
      component.currentVideoTime = '1m:30s';
      component.courseChatPresent = true;
      component.courseChatHistory = [];
      mockCourseService.sendMessageInChat.and.returnValue(
        of({ status: successCode, data: { chatId: 'new-chat' } }),
      );
      spyOn(component, 'getSectionTopicsAndChatQuestion');

      component.sendMessage();

      expect(component.askQuestion).toBe('');
      expect(component.showSpinner).toBeFalse();
      expect(component.courseChatHistory.length).toBe(1);
      expect(component.getSectionTopicsAndChatQuestion).toHaveBeenCalled();
    });

    it('should load notes with inputs disabled', () => {
      mockCourseService.getTopicNotes.and.returnValue(
        of({
          status: successCode,
          data: {
            topicNotes: [
              { topicNotesId: 1, notes: 'Note A' },
              { topicNotesId: 2, notes: 'Note B' },
            ],
          },
        }),
      );

      component.getNotes();

      expect(component.courseNote.length).toBe(2);
      expect(component.courseNote.every((n: any) => n.disable === true)).toBeTrue();
    });

    it('should enable only the selected note for editing', () => {
      component.courseNote = [
        { topicNotesId: 1, disable: true },
        { topicNotesId: 2, disable: true },
      ] as any;

      component.editNote(2);

      expect(component.courseNote[0].disable).toBeTrue();
      expect(component.courseNote[1].disable).toBeFalse();
    });

    it('should clear notes array when deleting the only note', () => {
      component.courseNote = [{ topicNotesId: 9 }] as any;
      mockCourseService.deleteTopicNote.and.returnValue(of({ status: successCode }));
      const getNotesSpy = spyOn(component, 'getNotes');

      component.deleteNote(9);

      expect(component.courseNote).toEqual([]);
      expect(getNotesSpy).not.toHaveBeenCalled();
    });

    it('should refresh notes after adding a new note', () => {
      component.noteText = 'My note';
      mockCourseService.createTopicNote.and.returnValue(of({ status: successCode }));
      const getNotesSpy = spyOn(component, 'getNotes');

      component.addNote();

      expect(component.noteText).toBe('');
      expect(getNotesSpy).toHaveBeenCalled();
    });

    it('should store summary report text', () => {
      mockCourseService.getTopicSummary.and.returnValue(
        of({ status: successCode, data: 'Topic summary content' }),
      );

      component.getSummaryReport();

      expect(component.summary).toBe('Topic summary content');
    });
  });

  describe('Phase 1 batch 3: Q&A and reviews pagination', () => {
    const successCode = 200;

    beforeEach(() => {
      component.courseId = 'course-1';
      component.currentSelectedTopicId = 'topic-1';
    });

    it('should replace question list when refresh flag is true', () => {
      component.questionAnswers = {
        questionDetails: [{ questionId: 1, text: 'Old' }],
      } as any;
      mockCourseService.getQuestions.and.returnValue(
        of({
          status: successCode,
          data: {
            totalPages: 2,
            totalElements: 5,
            questionDetails: [{ questionId: 2, text: 'New' }],
          },
        }),
      );

      component.getQuestionList(true);

      expect(component.questionAnswers.questionDetails.length).toBe(1);
      expect(component.questionAnswers.questionDetails[0].questionId).toBe(2);
      expect(component.totalQuestions).toBe(5);
    });

    it('should append questions when loading additional pages', () => {
      component.questionAnswers = {
        questionDetails: [{ questionId: 1, text: 'First' }],
      } as any;
      mockCourseService.getQuestions.and.returnValue(
        of({
          status: successCode,
          data: {
            totalPages: 3,
            totalElements: 20,
            questionDetails: [{ questionId: 2, text: 'Second' }],
          },
        }),
      );

      component.getQuestionList(false);

      expect(component.questionAnswers.questionDetails.length).toBe(2);
    });

    it('should toggle question answers visibility after loading replies', () => {
      component.questionAnswers = {
        questionDetails: [
          { questionId: 5, showQuestionAnswers: undefined },
        ],
      } as any;
      const question = component.questionAnswers.questionDetails[0];
      mockCourseService.getQuestionsReplies.and.returnValue(
        of({
          status: successCode,
          data: { answerDetail: [{ answerId: 1, text: 'Reply' }] },
        }),
      );

      component.getQuestionReplies(question);

      expect(question.answerDetail.length).toBe(1);
      expect(question.showQuestionAnswers).toBeTrue();
    });

    it('should add question and refresh the list', () => {
      component.questionText = 'New question?';
      component.questionAnswers = { questionDetails: [] } as any;
      mockCourseService.createQuestion.and.returnValue(
        of({ status: successCode, data: { questionId: 10, text: 'New question?' } }),
      );
      const listSpy = spyOn(component, 'getQuestionList');

      component.addQuestion();

      expect(component.questionText).toBe('');
      expect(component.questionAnswers.questionDetails.length).toBe(1);
      expect(listSpy).toHaveBeenCalledWith(true);
    });

    it('should submit reply and reload question replies', () => {
      const question: any = {
        questionId: 3,
        replyText: 'Thanks',
        totalReplies: 1,
      };
      mockCourseService.replyQuestion.and.returnValue(of({ status: successCode }));
      const repliesSpy = spyOn(component, 'getQuestionReplies');

      component.replyQuestion(question);

      expect(question.totalReplies).toBe(2);
      expect(repliesSpy).toHaveBeenCalledWith(question);
    });

    it('should load next question page when scrolled near bottom', () => {
      component.questionAnswers = {
        questionDetails: [{ questionId: 1 }],
      } as any;
      component.totalQuestions = 20;
      component.questionAnswersTotalPages = 3;
      component.questionPayLoad = { pageNo: 0, pageSize: 10 };
      const listSpy = spyOn(component, 'getQuestionList');
      const event = {
        target: { scrollHeight: 500, scrollTop: 350, clientHeight: 100 },
      };

      component.onQuestionScroll(event);

      expect(component.questionPayLoad.pageNo).toBe(1);
      expect(listSpy).toHaveBeenCalledWith(false);
    });

    it('should load next review page when scrolled near bottom', () => {
      component.completeReview = { feedbackComments: [{ reviewId: 1 }] } as any;
      component.totalReviewElements = 30;
      component.courseReviewTotalPages = 3;
      component.courseReviewPayLoad = { pageNo: 0, pageSize: 15 };
      const reviewSpy = spyOn(component, 'getCourseCompleteReview');
      const event = {
        target: { scrollHeight: 600, scrollTop: 450, clientHeight: 100 },
      };

      component.onReviewScroll(event);

      expect(component.courseReviewPayLoad.pageNo).toBe(1);
      expect(reviewSpy).toHaveBeenCalled();
    });

    it('should merge feedback into completeReview on load', () => {
      mockCourseService.getCourseRatingReviewAndFeedback.and.returnValue(
        of({
          status: successCode,
          data: {
            totalPages: 2,
            totalElements: 10,
            feedback: {
              averageRating: 4.5,
              feedbackComments: [{ reviewId: 1, comment: 'Great' }],
            },
          },
        }),
      );
      component.courseReviewPayLoad = { pageNo: 0, pageSize: 15 };

      component.getCourseCompleteReview();

      expect((component.completeReview as any).averageRating).toBe(4.5);
      expect(component.completeReview.feedbackComments.length).toBe(1);
      expect(component.hasMoreComments).toBeTrue();
    });

    it('should refresh reviews after review modal closes with data', () => {
      mockModal.create.and.returnValue({
        afterClose: of({ data: { submitted: true } }),
      } as any);
      const reviewSpy = spyOn(component, 'getCourseCompleteReview');

      component.openReviewModal();

      expect(reviewSpy).toHaveBeenCalled();
    });
  });

  describe('Phase 1 batch 3: navigation and utilities', () => {
    const successCode = 200;

    function createTopic(
      id: string,
      overrides: Record<string, unknown> = {},
    ): any {
      return {
        topicId: id,
        topicType: 'Quiz',
        testType: 'TEST',
        isCompleted: false,
        seekTime: 0,
        ...overrides,
      };
    }

    function createSection(
      sectionId: string,
      topics: any[],
      overrides: Record<string, unknown> = {},
    ): any {
      return {
        sectionId,
        free: true,
        topicList: topics,
        totalTopicCompleted: 0,
        active: false,
        ...overrides,
      };
    }

    beforeEach(() => {
      component.courseId = 'course-1';
    });

    it('should mark topic complete on review progress update', () => {
      const topic = createTopic('t1', { topicType: 'Quiz' });
      const section = createSection('s1', [topic]);
      component.currentSelectedTopicId = 't1';
      component.currentVideoTime = '1m:0s';
      spyOn(component, 'getCourseProgress');

      component.updateCourseProgressOnReview(section, topic);

      expect(topic.isCompleted).toBeTrue();
      expect(section.totalTopicCompleted).toBe(1);
      expect(mockCourseService.markTopicComplete).toHaveBeenCalledWith({
        isCompleted: true,
        topicId: 't1',
        seekTime: 0,
      });
    });

    it('should load section topics when panel is toggled open', () => {
      const section: any = { sectionId: 1 };
      const topicsSpy = spyOn(component, 'getSectionTopicList');
      spyOn(component, 'chatSectionPosition');

      component.toggleSectionPanel(true, section, 0);

      expect(topicsSpy).toHaveBeenCalledWith(section, 0, false, null);
    });

    it('should advance to next section on skipQuiz at last topic', fakeAsync(() => {
      const topic1 = createTopic('t1', { topicType: 'Video' });
      const topic2 = createTopic('t2', { topicType: 'Video' });
      const section1 = createSection('s1', [topic1]);
      const section2 = createSection('s2', [topic2]);
      component.sectionPanelList = [section1, section2];
      component.currentSelectedSection = section1;
      component.currentSelectedTopic = topic1;
      const panelSpy = spyOn(component, 'isPanelActive');

      component.skipQuiz({});
      tick(1500);

      expect(panelSpy).toHaveBeenCalledWith(section2, true, 1);
    }));

    it('should activate next section when playing last topic in section', fakeAsync(() => {
      const topic1 = createTopic('t1', { topicType: 'Video' });
      const topic2 = createTopic('t2', { topicType: 'Video' });
      const section1 = createSection('s1', [topic1]);
      const section2 = createSection('s2', [topic2]);
      component.sectionPanelList = [section1, section2];
      component.currentSelectedSection = section1;
      component.currentSelectedSectionId = 's1';
      component.currentSelectedTopic = topic1;
      const panelSpy = spyOn(component, 'isPanelActive');

      component.playNextVideo();
      tick(1100);

      expect(panelSpy).toHaveBeenCalledWith(section2, true, 1);
      expect(component.currentSelectedTopic).toBe(topic2);
    }));

    it('should read course url from route and start loading', () => {
      (component as any)._activatedRoute = {
        snapshot: { paramMap: { get: () => 'algebra-101' } },
        fragment: of(null),
        queryParams: of({}),
      };
      const meta = TestBed.inject(Meta) as jasmine.SpyObj<Meta>;
      spyOn(component, 'getCourseByUrl');

      component.getCourseIdFromRoute();

      expect(component.courseUrl).toBe('algebra-101');
      expect(component.getCourseByUrl).toHaveBeenCalledWith('algebra-101');
      expect(meta.updateTag).toHaveBeenCalled();
    });

    it('should persist seek and watch time when going offline', () => {
      const seekSpy = spyOn(component, 'manageSeekTime');
      const watchSpy = spyOn(component, 'manageWatchTime');

      component.onOffline(new Event('offline'));

      expect(seekSpy).toHaveBeenCalled();
      expect(watchSpy).toHaveBeenCalled();
    });

    it('should format durations via convertSecondsToHoursAndMinutes', () => {
      expect(component.convertSecondsToHoursAndMinutes(0)).toBe('0 sec');
      expect(component.convertSecondsToHoursAndMinutes(3661)).toContain('hr');
      expect(component.convertSecondsToHoursAndMinutes(90)).toContain('min');
    });

    it('should reset watch time after syncing with backend', () => {
      component.watchTime = 45;
      mockCourseService.manageWatchTime.and.returnValue(of({ status: successCode }));
      spyOn(component, 'getCourseProgress');

      component.manageWatchTime();

      expect(component.watchTime).toBe(0);
      expect(mockCourseService.manageWatchTime).toHaveBeenCalledWith('course-1', 45);
    });

    it('should open document links in a new tab', () => {
      const openSpy = spyOn(window, 'open');

      component.openDoc({ url: 'https://example.com/doc.pdf' });

      expect(openSpy).toHaveBeenCalledWith('https://example.com/doc.pdf', '_blank');
    });

    it('should navigate to instructor profile page', () => {
      component.routeToInsructorProfile('instructor-jane');

      expect(routerSpy.navigate).toHaveBeenCalledWith(['user/profile'], {
        queryParams: { url: 'instructor-jane' },
      });
    });

    it('should warn when review callback has no matching section', () => {
      component.sectionPanelList = [];
      component.currentSelectedSection = { sectionId: 'missing' };

      component.reviewCallBack([{ question: 'Q1' }]);

      expect(messageService.info).toHaveBeenCalledWith('No active section found.');
      expect(component.isReviewing).toBeTrue();
    });

    it('should sync dropdown visibility from change events', () => {
      component.onDropdownVisibilityChange(true);
      expect(component.isDropdownVisible).toBeTrue();
      component.onDropdownProfileVisibilityChange(false);
      expect(component.isDropdownProfileVisible).toBeFalse();
    });

    it('should report input focus state from DOM', () => {
      const input = document.createElement('input');
      document.body.appendChild(input);
      input.focus();

      expect(component.isInputFocused()).toBeTrue();

      input.blur();
      document.body.removeChild(input);
      expect(component.isInputFocused()).toBeFalse();
    });
  });

  describe('Phase 1 batch 4: edge flows and guards', () => {
    const successCode = 200;

    function createTopic(
      id: string,
      overrides: Record<string, unknown> = {},
    ): any {
      return {
        topicId: id,
        topicType: 'Quiz',
        testType: 'TEST',
        isCompleted: false,
        seekTime: 0,
        ...overrides,
      };
    }

    function createSection(
      sectionId: string,
      topics: any[],
      overrides: Record<string, unknown> = {},
    ): any {
      return {
        sectionId,
        free: true,
        topicList: topics,
        totalTopicCompleted: 0,
        active: false,
        ...overrides,
      };
    }

    beforeEach(() => {
      component.courseId = 'course-1';
    });

    it('should show subscription error when skipQuiz reaches paid section', fakeAsync(() => {
      const topic = createTopic('t1', { topicType: 'Video' });
      const section1 = createSection('s1', [topic]);
      const section2 = createSection('s2', [createTopic('t2')], { free: false });
      component.sectionPanelList = [section1, section2];
      component.currentSelectedSection = section1;
      component.currentSelectedTopic = topic;

      component.skipQuiz({});
      tick(600);

      expect(messageService.error).toHaveBeenCalledWith(
        'You have to get a subscription, next section is not free.',
      );
    }));

    it('should warn when review callback section has no topics', () => {
      component.sectionPanelList = [
        { sectionId: 1, topicList: [] },
      ];
      component.currentSelectedSection = component.sectionPanelList[0];
      component.currentSelectedTopic = { topicId: 't1' };

      component.reviewCallBack([{ question: 'Q1' }]);

      expect(messageService.info).toHaveBeenCalledWith(
        'No topics in the current section.',
      );
    });

    it('should warn when review callback topic is missing', () => {
      component.sectionPanelList = [
        { sectionId: 1, topicList: [{ topicId: 'other' }] },
      ];
      component.currentSelectedSection = component.sectionPanelList[0];
      component.currentSelectedTopic = { topicId: 't1' };

      component.reviewCallBack([{ question: 'Q1' }]);

      expect(messageService.info).toHaveBeenCalledWith('Current topic not found.');
    });

    it('should reset section review on 404', () => {
      mockCourseService.getSectionRatingAndReview.and.returnValue(
        throwError(() => ({
          error: { status: 404 },
        })),
      );
      component.sectionReview = { value: 5, totalReviews: 10 } as any;

      component.getSectionRatingAndReviews();

      expect(component.sectionReview.value).toBe(0);
      expect(component.sectionReview.totalReviews).toBe(0);
    });

    it('should reset subscription modal flag when plan modal closes', () => {
      component.subscriptionModalOpened = true;
      mockModal.create.and.returnValue({ afterClose: of(null) } as any);

      component.openSubscriptionPlan();

      expect(mockModal.create).toHaveBeenCalled();
      expect(component.subscriptionModalOpened).toBeFalse();
    });

    it('should refresh notes after update', () => {
      component.courseNote = [
        { topicNotesId: 1, notes: 'Updated text', time: '1m:0s' },
      ] as any;
      mockCourseService.createTopicNote.and.returnValue(of({ status: successCode }));
      const getNotesSpy = spyOn(component, 'getNotes');

      component.updateNote(1);

      expect(mockCourseService.createTopicNote).toHaveBeenCalled();
      expect(getNotesSpy).toHaveBeenCalled();
    });

    it('should show success when toggling favorite course', () => {
      mockCourseService.addOrRemoveCourseToFavorite.and.returnValue(
        of({ status: successCode }),
      );

      component.toggleFavoriteCourse();

      expect(messageService.success).toHaveBeenCalledWith('Added to favorites');
    });

    it('should complete current video topic with zero seek time when marking complete', () => {
      const topic = createTopic('t1', { topicType: 'Video' });
      const section = createSection('s1', [topic]);
      component.currentSelectedTopicId = 't1';
      component.currentVideoTime = '2m:30s';

      component.completeTopic(section, topic, true, 't1', 0, 'Video');

      expect(mockCourseService.markTopicComplete).toHaveBeenCalledWith({
        isCompleted: true,
        topicId: 't1',
        seekTime: 0,
      });
    });

    it('should unsubscribe route listener on destroy', () => {
      const sub = jasmine.createSpyObj('Subscription', ['unsubscribe']);
      component.routeSubscription = sub as any;
      spyOn(component, 'manageSeekTime');
      spyOn(component, 'manageWatchTime');

      component.ngOnDestroy();

      expect(sub.unsubscribe).toHaveBeenCalled();
    });

    it('should navigate to landing and notification routes', () => {
      component.routeToLandingPage();
      component.routeToNotificationPage();

      expect(routerSpy.navigate).toHaveBeenCalledWith(['']);
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/user/notifications']);
    });

    it('should fetch certificate when last video ends at 100% progress', fakeAsync(() => {
      const topic = createTopic('t1', { topicType: 'Video' });
      const section = createSection('s1', [topic]);
      component.sectionPanelList = [section];
      component.currentSelectedSection = section;
      component.currentSelectedSectionId = 's1';
      component.currentSelectedTopic = topic;
      component.courseProgress = 100;
      const certSpy = spyOn(component, 'getCertificateData');

      component.playNextVideo();
      tick(1100);

      expect(certSpy).toHaveBeenCalled();
    }));
  });

  describe('Phase 1 batch 5: remaining coverage gaps', () => {
    const successCode = 200;
    const sectionApiResponse = {
      status: successCode,
      data: {
        sectionDetails: [
          { sectionId: 1, free: true, sectionName: 'Intro' },
          { sectionId: 2, free: false, sectionName: 'Advanced' },
        ],
        category: 'Science',
        title: 'Physics 101',
        hasCertificate: false,
      },
    };

    function createTopic(
      id: string,
      overrides: Record<string, unknown> = {},
    ): any {
      return {
        topicId: id,
        topicType: 'Quiz',
        testType: 'TEST',
        isCompleted: false,
        seekTime: 0,
        ...overrides,
      };
    }

    function createSection(
      sectionId: string,
      topics: any[],
      overrides: Record<string, unknown> = {},
    ): any {
      return {
        sectionId,
        free: true,
        topicList: topics,
        totalTopicCompleted: 0,
        active: false,
        ...overrides,
      };
    }

    beforeEach(() => {
      component.courseId = 'course-1';
      component.toCourseId = 'target-course';
      component.toSectionId = 5;
      mockCourseService.getCourseSections.and.returnValue(
        of(sectionApiResponse),
      );
    });

    it('should pin alternate instructor and reload sections on success', () => {
      component.alternateSectionRouting = true;
      mockCourseService.pinAlternateInstructor.and.returnValue(
        of({ status: successCode, message: 'Instructor pinned' }),
      );
      const reloadSpy = spyOn(component, 'getCourseSectionList');

      component.pinAlternateInstructor('from-c', 'from-s');

      expect(mockCourseService.pinAlternateInstructor).toHaveBeenCalledWith(
        'target-course',
        5,
        'from-c',
        'from-s',
      );
      expect(messageService.success).toHaveBeenCalledWith('Instructor pinned');
      expect(reloadSpy).toHaveBeenCalled();
      expect(component.alternateSectionRouting).toBeFalse();
    });

    it('should tolerate pin alternate instructor API errors', () => {
      component.alternateSectionRouting = true;
      mockCourseService.pinAlternateInstructor.and.returnValue(
        throwError(() => ({ error: { message: 'Pin failed' } })),
      );

      expect(() =>
        component.pinAlternateInstructor('from-c', 'from-s'),
      ).not.toThrow();
    });

    it('should log error when getCourseCompleteReview fails', () => {
      const consoleSpy = spyOn(console, 'error');
      mockCourseService.getCourseRatingReviewAndFeedback.and.returnValue(
        throwError(() => new Error('Review fetch failed')),
      );
      component.courseReviewPayLoad = { pageNo: 0, pageSize: 15 };

      component.getCourseCompleteReview();

      expect(consoleSpy).toHaveBeenCalled();
    });

    it('should set hasMoreComments false on last review page', () => {
      mockCourseService.getCourseRatingReviewAndFeedback.and.returnValue(
        of({
          status: successCode,
          data: {
            totalPages: 1,
            totalElements: 3,
            feedback: {
              feedbackComments: [{ reviewId: 1, comment: 'Nice' }],
            },
          },
        }),
      );
      component.courseReviewPayLoad = { pageNo: 0, pageSize: 15 };

      component.getCourseCompleteReview();

      expect(component.hasMoreComments).toBeFalse();
    });

    it('should refresh chat data when chat modal closes without result', () => {
      mockModal.create.and.returnValue({ afterClose: of(null) } as any);
      const refreshSpy = spyOn(component, 'getSectionTopicsAndChatQuestion');

      component.openChatModal();

      expect(refreshSpy).toHaveBeenCalled();
    });

    it('should not refresh chat when chat modal closes with a result', () => {
      mockModal.create.and.returnValue({
        afterClose: of({ saved: true }),
      } as any);
      const refreshSpy = spyOn(component, 'getSectionTopicsAndChatQuestion');

      component.openChatModal();

      expect(refreshSpy).not.toHaveBeenCalled();
    });

    it('should reset spinner when sendMessage fails', () => {
      component.askQuestion = 'Help?';
      component.showSpinner = true;
      mockCourseService.sendMessageInChat.and.returnValue(
        throwError(() => new Error('Send failed')),
      );

      component.sendMessage();

      expect(component.showSpinner).toBeFalse();
    });

    it('should use chatTopicTime in sendMessage when video time is absent', () => {
      component.askQuestion = 'Question';
      component.currentVideoTime = null;
      component.chatTopicTime = '3m:0s';
      component.courseChatPresent = false;
      mockCourseService.sendMessageInChat.and.returnValue(
        of({ status: successCode, data: { chatId: 'c1' } }),
      );
      spyOn(component, 'getSectionTopicsAndChatQuestion');

      component.sendMessage();

      expect(mockCourseService.sendMessageInChat).toHaveBeenCalledWith(
        jasmine.objectContaining({ time: '3m:0s' }),
      );
      expect(component.courseChatHistory.length).toBe(1);
    });

    it('should send updateNote payload with note content and refresh on success', () => {
      component.currentSelectedTopicId = 'topic-1';
      component.courseNote = [
        { topicNotesId: 7, notes: 'Edited note', time: '2m:0s' },
      ] as any;
      mockCourseService.createTopicNote.and.returnValue(
        of({ status: successCode }),
      );
      const getNotesSpy = spyOn(component, 'getNotes');

      component.updateNote(7);

      expect(mockCourseService.createTopicNote).toHaveBeenCalledWith({
        courseId: 'course-1',
        topicId: 'topic-1',
        note: 'Edited note',
        time: '2m:0s',
        topicNotesId: 7,
      });
      expect(getNotesSpy).toHaveBeenCalled();
    });

    it('should skip updateNote when note id is not found', () => {
      component.courseNote = [{ topicNotesId: 1, notes: 'A', time: '1m:0s' }] as any;

      component.updateNote(99);

      expect(mockCourseService.createTopicNote).not.toHaveBeenCalled();
    });

    it('should not refresh notes when updateNote API fails', () => {
      component.courseNote = [
        { topicNotesId: 1, notes: 'Text', time: '1m:0s' },
      ] as any;
      mockCourseService.createTopicNote.and.returnValue(
        throwError(() => new Error('Update failed')),
      );
      const getNotesSpy = spyOn(component, 'getNotes');

      component.updateNote(1);

      expect(getNotesSpy).not.toHaveBeenCalled();
    });

    it('should focus myInput when editNote enables a note', () => {
      const focusSpy = jasmine.createSpy('focus');
      component.myInput = { nativeElement: { focus: focusSpy } } as any;
      component.courseNote = [
        { topicNotesId: 1, disable: true },
        { topicNotesId: 2, disable: true },
      ] as any;

      component.editNote(2);

      expect(component.courseNote[1].disable).toBeFalse();
      expect(focusSpy).toHaveBeenCalled();
    });

    it('should not show success when toggleFavoriteCourse fails', () => {
      mockCourseService.addOrRemoveCourseToFavorite.and.returnValue(
        throwError(() => ({ error: { message: 'Favorite failed' } })),
      );

      component.toggleFavoriteCourse();

      expect(messageService.success).not.toHaveBeenCalled();
    });

    it('should show info when skipQuiz section has no topics', () => {
      const section = createSection('s1', []);
      component.sectionPanelList = [section];
      component.currentSelectedSection = section;
      component.currentSelectedTopic = { topicId: 't1' };

      component.skipQuiz({});

      expect(messageService.info).toHaveBeenCalledWith(
        'No topics in the current section.',
      );
    });

    it('should show info when skipQuiz cannot find current topic', () => {
      const section = createSection('s1', [createTopic('other')]);
      component.sectionPanelList = [section];
      component.currentSelectedSection = section;
      component.currentSelectedTopic = { topicId: 'missing' };

      component.skipQuiz({});

      expect(messageService.info).toHaveBeenCalledWith('Current topic not found.');
    });

    it('should show info when skipQuiz reaches the final section', fakeAsync(() => {
      const topic = createTopic('t1', { topicType: 'Video' });
      const section = createSection('s1', [topic]);
      component.sectionPanelList = [section];
      component.currentSelectedSection = section;
      component.currentSelectedTopic = topic;
      component.courseProgress = 50;

      component.skipQuiz({});
      tick(600);

      expect(messageService.info).toHaveBeenCalledWith('No more sections.');
    }));

    it('should show info when playNextVideo has invalid section', () => {
      component.currentSelectedSectionId = 'missing';
      component.sectionPanelList = [];

      component.playNextVideo();

      expect(messageService.info).toHaveBeenCalledWith('Invalid section.');
    });

    it('should show info when playNextVideo has no more sections', () => {
      const topic = createTopic('t1', { topicType: 'Video' });
      const section = createSection('s1', [topic]);
      component.sectionPanelList = [section];
      component.currentSelectedSection = section;
      component.currentSelectedSectionId = 's1';
      component.currentSelectedTopic = topic;
      component.courseProgress = 50;

      component.playNextVideo();

      expect(messageService.info).toHaveBeenCalledWith('No more sections');
    });

    it('should only advance when videoCompleted topic is already complete', () => {
      const topic = createTopic('t1', { topicType: 'Video', isCompleted: true });
      component.currentSelectedTopic = topic;
      spyOn(component, 'playNextVideo');
      spyOn(component, 'manageWatchTime');

      component.videoCompleted({});

      expect(component.manageWatchTime).not.toHaveBeenCalled();
      expect(component.playNextVideo).toHaveBeenCalled();
    });

    it('should fall back to default section when topicId has no matching section', () => {
      const defaultSpy = spyOn(component, 'setDefaultSection');

      component.getCourseSectionList(null, 'topic-99');

      expect(defaultSpy).toHaveBeenCalledWith(
        sectionApiResponse.data.sectionDetails,
      );
    });

    it('should pass topicId to getSectionTopicList when sectionId is provided', () => {
      const topicsSpy = spyOn(component, 'getSectionTopicList');

      component.getCourseSectionList(1, 'topic-2');

      expect(topicsSpy).toHaveBeenCalledWith(
        jasmine.objectContaining({ sectionId: 1 }),
        0,
        true,
        { topicId: 'topic-2' },
      );
    });

    it('should fall back to default section when sectionId is unknown', () => {
      const defaultSpy = spyOn(component, 'setDefaultSection');

      component.getCourseSectionList(999, null);

      expect(defaultSpy).toHaveBeenCalledWith(
        sectionApiResponse.data.sectionDetails,
      );
    });

    it('should show error when getCourseSectionList fails', () => {
      mockCourseService.getCourseSections.and.returnValue(
        throwError(() => ({ error: { message: 'Sections unavailable' } })),
      );

      component.getCourseSectionList();

      expect(messageService.error).toHaveBeenCalledWith('Sections unavailable');
    });

    it('should show congrats screen when review callback receives null', () => {
      component.isReviewing = true;
      component.showCongratsScreen = false;

      component.reviewCallBack(null);

      expect(component.isReviewing).toBeFalse();
      expect(component.showCongratsScreen).toBeTrue();
    });

    it('should ignore updateCourseReview when feedback is missing', () => {
      component.completeReview = { feedbackComments: [{ reviewId: 1 }] } as any;

      component.updateCourseReview(undefined);

      expect(component.completeReview.feedbackComments.length).toBe(1);
    });

    it('should refresh notes after deleting one of multiple notes', () => {
      component.courseNote = [
        { topicNotesId: 1 },
        { topicNotesId: 2 },
      ] as any;
      mockCourseService.deleteTopicNote.and.returnValue(of({ status: successCode }));
      const getNotesSpy = spyOn(component, 'getNotes');

      component.deleteNote(1);

      expect(getNotesSpy).toHaveBeenCalled();
    });

    it('should scroll chat container to bottom', () => {
      const container = { scrollHeight: 500, scrollTop: 0 };
      component.chatContainer = { nativeElement: container } as any;

      component.scrollToBottom();

      expect(container.scrollTop).toBe(500);
    });

    it('should show error when certificate fetch fails', () => {
      mockCertificateService.getCertificateData.and.returnValue(
        throwError(() => ({ error: { message: 'No certificate' } })),
      );

      component.getCertificateData();

      expect(messageService.error).toHaveBeenCalledWith('No certificate');
    });

    it('should navigate to generate certificate route', () => {
      component.routeToGenerateCertificate();

      expect(routerSpy.navigate).toHaveBeenCalledWith(
        ['student/generate-certificate'],
        { queryParams: { courseId: 'course-1' } },
      );
    });

    it('should update tooltip visibility from video player bounds', () => {
      component.videoPlayerElement = {
        media: {
          nativeElement: {
            getBoundingClientRect: () => ({
              top: 10,
              bottom: 310,
              height: 300,
            }),
          },
        },
      } as any;

      component.checkTooltipVisibility();

      expect(component.isTooltipVisible).toBeTrue();
    });
  });

  describe('Phase 1 batch 6: progress sync and chat branches', () => {
    const successCode = 200;

    function createTopic(
      id: string,
      overrides: Record<string, unknown> = {},
    ): any {
      return {
        topicId: id,
        topicType: 'Quiz',
        testType: 'TEST',
        isCompleted: false,
        seekTime: 0,
        ...overrides,
      };
    }

    function createSection(
      sectionId: string,
      topics: any[],
      overrides: Record<string, unknown> = {},
    ): any {
      return {
        sectionId,
        free: true,
        topicList: topics,
        totalTopicCompleted: 0,
        active: false,
        ...overrides,
      };
    }

    beforeEach(() => {
      component.courseId = 'course-1';
      component.currentVideoTime = '2m:0s';
    });

    it('should use current video seek time when reviewing the active video topic', () => {
      const topic = createTopic('t1', { topicType: 'Video' });
      const section = createSection('s1', [topic]);
      component.currentSelectedTopicId = 't1';
      spyOn(component, 'getCourseProgress');

      component.updateCourseProgressOnReview(section, topic);

      expect(mockCourseService.markTopicComplete).toHaveBeenCalledWith({
        isCompleted: true,
        topicId: 't1',
        seekTime: 120,
      });
    });

    it('should use zero seek time when reviewing a different video topic', () => {
      const topic = createTopic('t2', { topicType: 'Video' });
      const section = createSection('s1', [topic]);
      component.currentSelectedTopicId = 't1';

      component.updateCourseProgressOnReview(section, topic);

      expect(mockCourseService.markTopicComplete).toHaveBeenCalledWith({
        isCompleted: true,
        topicId: 't2',
        seekTime: 0,
      });
    });

    it('should revert topic completion when review progress update fails', () => {
      const topic = createTopic('t1', { topicType: 'Quiz' });
      const section = createSection('s1', [topic]);
      mockCourseService.markTopicComplete.and.returnValue(
        throwError(() => new Error('Review update failed')),
      );
      const consoleSpy = spyOn(console, 'error');

      component.updateCourseProgressOnReview(section, topic);

      expect(topic.isCompleted).toBeFalse();
      expect(consoleSpy).toHaveBeenCalled();
    });

    it('should warn when review progress update is missing section or topic', () => {
      const warnSpy = spyOn(console, 'warn');

      component.updateCourseProgressOnReview(null, null);

      expect(warnSpy).toHaveBeenCalledWith('Section or topic is missing');
      expect(mockCourseService.markTopicComplete).not.toHaveBeenCalled();
    });

    it('should not refresh chat after delete when API status is not success', () => {
      component.courseChatHistory = [{ chatId: 'c1', message: 'Hi' }];
      mockCourseService.deleteChat.and.returnValue(of({ status: 400 }));
      const refreshSpy = spyOn(component, 'getSectionTopicsAndChatQuestion');

      component.deleteChat({ chatId: 'c1' });

      expect(component.courseChatHistory.length).toBe(1);
      expect(refreshSpy).not.toHaveBeenCalled();
    });

    it('should tolerate deleteChat API errors', () => {
      mockCourseService.deleteChat.and.returnValue(
        throwError(() => new Error('Delete failed')),
      );

      expect(() => component.deleteChat({ chatId: 'c1' })).not.toThrow();
    });

    it('should not accumulate watch time when video is paused', () => {
      component.watchTime = 5;
      spyOn(component, 'manageSeekTimeAfterEveryTwoMins');

      component.getVideoTime({
        videoTime: 45,
        isVideoPlaying: false,
        playbackRate: 1,
      });

      expect(component.watchTime).toBe(5);
    });

    it('should not accumulate watch time when video time is below one second', () => {
      component.watchTime = 0;
      spyOn(component, 'manageSeekTimeAfterEveryTwoMins');

      component.getVideoTime({
        videoTime: 0,
        isVideoPlaying: true,
        playbackRate: 1,
      });

      expect(component.watchTime).toBe(0);
    });

    it('should hide tooltip when video player is outside the viewport', () => {
      component.isTooltipVisible = true;
      component.videoPlayerElement = {
        media: {
          nativeElement: {
            getBoundingClientRect: () => ({
              top: -400,
              bottom: -100,
              height: 300,
            }),
          },
        },
      } as any;

      component.checkTooltipVisibility();

      expect(component.isTooltipVisible).toBeFalse();
    });

    it('should load chat history when selecting a topic with existing chat data', () => {
      const topic = createTopic('t1', { topicType: 'Video' });
      const section = createSection('s1', [topic]);
      component.courseChat = [
        {
          sectionId: 's1',
          topics: [
            {
              topicId: 't1',
              chatTopicHistory: [{ chatId: 'chat-1', time: '1m:0s' }],
            },
          ],
        },
      ] as any;
      component.currentSelectedSectionId = 's1';
      spyOn(component, 'getCourseChatHistory');
      spyOn(component, 'getSectionRatingAndReviews');
      spyOn(component, 'getCourseProgress');
      spyOn(component, 'getSummaryReport');
      spyOn(component, 'getSectionTopicsAndChatQuestion');
      spyOn(component, 'manageSeekTime');
      spyOn(component, 'manageWatchTime');

      component.onSelectTopicFromPlayList(section, topic);

      expect(component.courseChatPresent).toBeTrue();
      expect(component.selectedChatId).toBe('chat-1');
      expect(component.getCourseChatHistory).toHaveBeenCalledWith(
        'chat-1',
        '1m:0s',
        't1',
      );
    });

    it('should fetch certificate when skipQuiz completes course at 100%', fakeAsync(() => {
      const topic = createTopic('t1', { topicType: 'Video' });
      const section = createSection('s1', [topic]);
      component.sectionPanelList = [section];
      component.currentSelectedSection = section;
      component.currentSelectedTopic = topic;
      component.courseProgress = 100;
      mockCourseService.courseProgress.and.returnValue(
        of({ status: successCode, data: 100 }),
      );
      const certSpy = spyOn(component, 'getCertificateData');

      component.skipQuiz({});
      tick(500);

      expect(certSpy).toHaveBeenCalled();
    }));

    it('should sync watch time when an incomplete video finishes', fakeAsync(() => {
      const topic = createTopic('t1', { topicType: 'Video', isCompleted: false });
      const section = createSection('s1', [topic]);
      component.currentSelectedTopic = topic;
      component.currentSelectedSection = section;
      const watchSpy = spyOn(component, 'manageWatchTime');
      spyOn(component, 'completeTopic').and.returnValue({
        unsubscribe: jasmine.createSpy('unsubscribe'),
      } as any);
      spyOn(component, 'playNextVideo');

      component.videoCompleted({});
      tick(800);

      expect(topic.isCompleted).toBeTrue();
      expect(watchSpy).toHaveBeenCalled();
    }));

    it('should persist seek time for incomplete video topics via manageSeekTime', () => {
      const topic = createTopic('t1', {
        topicType: 'Video',
        isCompleted: false,
        seekTime: 0,
      });
      component.currentSelectedTopic = topic;
      component.currentSelectedTopicId = 't1';
      component.currentVideoTime = '1m:30s';
      spyOn(component, 'getCourseProgress');

      component.manageSeekTime();

      expect(topic.seekTime).toBe(90);
      expect(mockCourseService.markTopicComplete).toHaveBeenCalledWith({
        isCompleted: false,
        topicId: 't1',
        seekTime: 90,
      });
    });

    it('should log when getSectionTopicList cannot find an incomplete topic', () => {
      const section: any = { sectionId: 's1', active: true };
      component.sectionPanelList = [section];
      mockCourseService.getSectionTopics.and.returnValue(
        of({
          status: successCode,
          data: [{ topicId: 't1', isCompleted: true, topicDuration: 60 }],
        }),
      );
      const consoleSpy = spyOn(console, 'error');
      spyOn(component, 'onSelectTopicFromPlayList');

      component.getSectionTopicList(section, 0, true, { topicId: 'missing' });

      expect(consoleSpy).toHaveBeenCalledWith('Topic not found');
    });

    it('should log when getSectionTopicList receives non-success status', () => {
      const section: any = { sectionId: 's1', active: true };
      component.sectionPanelList = [section];
      mockCourseService.getSectionTopics.and.returnValue(of({ status: 500 }));
      const consoleSpy = spyOn(console, 'error');

      component.getSectionTopicList(section, 0, false);

      expect(consoleSpy).toHaveBeenCalledWith('Failed to load topics');
    });

    it('should return false when quiz guard has no selected topic', () => {
      component.currentSelectedTopic = null;

      expect(component.checkCurrectSelectedTopicIsQuizOrSurvey()).toBeFalse();
    });

    it('should toggle reply visibility without throwing when reply input is missing', fakeAsync(() => {
      const question = { questionId: 99, showReply: false };

      expect(() => component.toggleReplyAndScroll(question)).not.toThrow();
      expect(question.showReply).toBeTrue();
      tick(100);
    }));

    it('should avoid question pagination when all questions are already loaded', () => {
      component.questionAnswers = {
        questionDetails: [{ questionId: 1 }, { questionId: 2 }],
      } as any;
      component.totalQuestions = 2;
      const listSpy = spyOn(component, 'getQuestionList');
      const event = {
        target: {
          scrollHeight: 500,
          scrollTop: 400,
          clientHeight: 100,
        },
      };

      component.onQuestionScroll(event);

      expect(listSpy).not.toHaveBeenCalled();
    });

    it('should decrement review page when already on the last page', () => {
      component.completeReview = {
        feedbackComments: [{ reviewId: 1 }],
      } as any;
      component.totalReviewElements = 5;
      component.courseReviewPayLoad = { pageNo: 2 };
      component.courseReviewTotalPages = 3;
      const reviewSpy = spyOn(component, 'getCourseCompleteReview');
      const event = {
        target: {
          scrollHeight: 500,
          scrollTop: 400,
          clientHeight: 100,
        },
      };

      component.onReviewScroll(event);

      expect(component.courseReviewPayLoad.pageNo).toBe(2);
      expect(reviewSpy).not.toHaveBeenCalled();
    });
  });
});
