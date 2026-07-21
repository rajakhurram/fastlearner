import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ChatModalComponent } from './chat-modal.component';
import { CourseService } from 'src/app/core/services/course.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { Title } from '@angular/platform-browser';

describe('ChatModalComponent', () => {
  let component: ChatModalComponent;
  let fixture: ComponentFixture<ChatModalComponent>;
  let courseService: jasmine.SpyObj<CourseService>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    const courseServiceSpy = jasmine.createSpyObj('CourseService', [
      'getSectionAndTopicsChatQuestion',
      'getCourseChatHistory',
      'sendMessageInChat'
    ]);

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [ChatModalComponent],
      providers: [
        { provide: CourseService, useValue: courseServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ChatModalComponent);
    component = fixture.componentInstance;
    courseService = TestBed.inject(CourseService) as jasmine.SpyObj<CourseService>;
    httpMock = TestBed.inject(HttpTestingController);

    // Initialize HttpConstants
    component._httpConstants = new HttpConstants();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should call getSectionTopicsAndChatQuestion on ngOnInit', () => {
    const mockResponse = {
      status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      data: []
    };

    courseService.getSectionAndTopicsChatQuestion.and.returnValue(of(mockResponse));
    component.ngOnInit();
    expect(courseService.getSectionAndTopicsChatQuestion).toHaveBeenCalledWith(component.data);
  });


  it('should call getCourseChatHistory and update courseChatHistory', () => {
    const chatId = '1';
    const chatTime = '02:56';
    const topicId = '1';
    const mockResponse = {
      status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      data: []
    };

    courseService.getCourseChatHistory.and.returnValue(of(mockResponse));
    component.getCourseChatHistory(chatId, chatTime, topicId);

    expect(courseService.getCourseChatHistory).toHaveBeenCalledWith(chatId);
    expect(component.courseChatHistory).toEqual(mockResponse.data);
  });

  it('should send a message and update courseChatHistory and call getSectionTopicsAndChatQuestion', () => {
    const chatPayload = {
      courseId: component.data,
      topicId: component.topicId,
      question: component.askQuestion,
      time: component.currentVideoTime ?? '02:56'
    };
    const mockResponse = {
      status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      data: {}
    };

    component.currentVideoTime = '02:56';
    courseService.sendMessageInChat.and.returnValue(of(mockResponse));
    spyOn(component, 'getSectionTopicsAndChatQuestion');

    component.sendMessage();

    expect(courseService.sendMessageInChat).toHaveBeenCalledWith(chatPayload);
    expect(component.askQuestion).toBe('');
    expect(component.showSpinner).toBe(false);
    expect(component.courseChatHistory).toContain(mockResponse.data);
    expect(component.getSectionTopicsAndChatQuestion).toHaveBeenCalled();
  });


});
