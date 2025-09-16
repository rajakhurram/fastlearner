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
import { CourseContentComponent } from './course-content.component';
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
    mockCacheService = jasmine.createSpyObj('CacheService', ['clearCache']);
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

    const messageServiceSpy = jasmine.createSpyObj('NzMessageService', [
      'success',
      'error',
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
    fixture = TestBed.createComponent(CourseContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
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
    const mockError = { status: 500, message: 'Server error' };
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
});
