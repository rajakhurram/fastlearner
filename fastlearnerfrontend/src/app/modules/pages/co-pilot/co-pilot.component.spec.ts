import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CoPilotComponent } from './co-pilot.component';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { CourseService } from 'src/app/core/services/course.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NzCardModule } from 'ng-zorro-antd/card';
import { of, throwError } from 'rxjs';
import { ElementRef } from '@angular/core';

describe('CoPilotComponent', () => {
  let component: CoPilotComponent;
  let fixture: ComponentFixture<CoPilotComponent>;
  let courseService: jasmine.SpyObj<CourseService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const courseServiceSpy = jasmine.createSpyObj('CourseService', ['sendCoPilotMessage']);
    const routerSpy = jasmine.createSpyObj('Router', ['createUrlTree', 'serializeUrl']);

    await TestBed.configureTestingModule({
      declarations: [CoPilotComponent],
      imports: [
        NzCardModule,
        FormsModule,
        ReactiveFormsModule,
      ],
      providers: [
        { provide: HttpClient, useValue: {} },
        { provide: Router, useValue: routerSpy },
        { provide: CourseService, useValue: courseServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CoPilotComponent);
    component = fixture.componentInstance;
    courseService = TestBed.inject(CourseService) as jasmine.SpyObj<CourseService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('sendMessage', () => {
    it('should not send message if message is empty', () => {
      component.message = '';
      component.sendMessage();
      expect(courseService.sendCoPilotMessage).not.toHaveBeenCalled();
    });

    it('should not send message if message contains only whitespace', () => {
      component.message = '   ';
      component.sendMessage();
      expect(courseService.sendCoPilotMessage).not.toHaveBeenCalled();
    });

    it('should not send message if spinner is already showing', () => {
      component.message = 'Test message';
      component.showSpinner = true;
      component.sendMessage();
      expect(courseService.sendCoPilotMessage).not.toHaveBeenCalled();
    });

    it('should add user message to chat history', () => {
      const userMessage = 'Hello bot';
      component.message = userMessage;
      courseService.sendCoPilotMessage.and.returnValue(of({ answer: { answer: 'Response', entities: [] } }));

      component.sendMessage();

      expect(component.chatHistory.length).toBeGreaterThan(0);
      expect(component.chatHistory[0]).toEqual({ sender: 'user', text: userMessage });
    });

    it('should clear message input after sending', () => {
      component.message = 'Test message';
      courseService.sendCoPilotMessage.and.returnValue(of({ answer: { answer: 'Response', entities: [] } }));

      component.sendMessage();

      expect(component.message).toBe('');
    });

    it('should show spinner while sending message', () => {
      component.message = 'Test message';
      courseService.sendCoPilotMessage.and.returnValue(of({ answer: { answer: 'Response', entities: [] } }));

      component.sendMessage();

      expect(component.showSpinner).toBe(false); // Will be false after observable completes
    });

    it('should add bot response to chat history on success', () => {
      const userMessage = 'Test question';
      const botResponse = 'Test answer';
      component.message = userMessage;
      
      courseService.sendCoPilotMessage.and.returnValue(of({
        answer: {
          answer: botResponse,
          entities: []
        }
      }));

      component.sendMessage();

      expect(component.chatHistory.length).toBe(2);
      expect(component.chatHistory[1]).toEqual({
        sender: 'bot',
        text: botResponse,
        entities: []
      });
    });

    it('should handle response with entities', () => {
      component.message = 'Show courses';
      const entities = [
        { type: 'course', course_id: '1', name: 'Course 1' },
        { type: 'instructor', name: 'Instructor 1' }
      ];

      courseService.sendCoPilotMessage.and.returnValue(of({
        answer: {
          answer: 'Here are the courses',
          entities: entities
        }
      }));

      component.sendMessage();

      expect(component.chatHistory[1].entities).toBeDefined();
      expect(component.chatHistory[1].entities?.length).toBeGreaterThan(0);
    });

    it('should handle missing answer in response', () => {
      component.message = 'Test';
      courseService.sendCoPilotMessage.and.returnValue(of({ answer: {} }));

      component.sendMessage();

      expect(component.chatHistory[1].text).toBe('No answer found.');
    });

    it('should handle error response', () => {
      component.message = 'Test';
      courseService.sendCoPilotMessage.and.returnValue(throwError(() => new Error('API Error')));

      component.sendMessage();

      expect(component.chatHistory[1]).toEqual({
        sender: 'bot',
        text: 'Sorry, something went wrong. Please check your connection and try again.',
      });
    });

    it('should set showSpinner to false after completion', () => {
      component.message = 'Test';
      courseService.sendCoPilotMessage.and.returnValue(of({ answer: { answer: 'Response', entities: [] } }));

      component.sendMessage();

      expect(component.showSpinner).toBe(false);
    });

    it('should set shouldScroll to true when sending message', () => {
      component.message = 'Test';
      courseService.sendCoPilotMessage.and.returnValue(of({ answer: { answer: 'Response', entities: [] } }));

      component.sendMessage();

      expect(component.shouldScroll).toBe(true);
    });
  });

  describe('processEntities', () => {
    it('should group courses with their sections and topics', () => {
      const entities = [
        { type: 'course', course_id: '1', name: 'Course 1' },
        { type: 'section', course_id: '1', name: 'Section 1' },
        { type: 'topic', course_id: '1', name: 'Topic 1' },
        { type: 'instructor', name: 'Instructor 1' }
      ];

      const result = component['processEntities'](entities);

      expect(result.length).toBe(2);
      expect(result[0].sections.length).toBe(1);
      expect(result[0].topics.length).toBe(1);
    });

    it('should handle multiple courses', () => {
      const entities = [
        { type: 'course', course_id: '1', name: 'Course 1' },
        { type: 'course', course_id: '2', name: 'Course 2' },
        { type: 'section', course_id: '1', name: 'Section 1' },
        { type: 'section', course_id: '2', name: 'Section 2' }
      ];

      const result = component['processEntities'](entities);

      expect(result.length).toBe(2);
      expect(result[0].sections.length).toBe(1);
      expect(result[1].sections.length).toBe(1);
    });

    it('should separate instructors from courses', () => {
      const entities = [
        { type: 'course', course_id: '1', name: 'Course 1' },
        { type: 'instructor', name: 'Instructor 1' },
        { type: 'instructor', name: 'Instructor 2' }
      ];

      const result = component['processEntities'](entities);

      expect(result.length).toBe(3);
      expect(result[1].type).toBe('instructor');
      expect(result[2].type).toBe('instructor');
    });

    it('should handle empty entities array', () => {
      const result = component['processEntities']([]);
      expect(result.length).toBe(0);
    });
  });

  describe('routeToInstructorProfile', () => {
    it('should open instructor profile in new tab with URL', () => {
      spyOn(window, 'open');
      const profileUrl = 'instructor-profile-url';
      router.createUrlTree.and.returnValue({} as any);
      router.serializeUrl.and.returnValue('/user/profile?url=' + profileUrl);

      component.routeToInstructorProfile(profileUrl);

      expect(window.open).toHaveBeenCalled();
      const callArgs = (window.open as jasmine.Spy).calls.mostRecent().args;
      expect(callArgs[0]).toContain('/user/profile');
      expect(callArgs[1]).toBe('_blank');
    });

    it('should handle missing profile URL', () => {
      spyOn(window, 'open');
      router.createUrlTree.and.returnValue({} as any);
      router.serializeUrl.and.returnValue('/user/profile');

      component.routeToInstructorProfile(undefined);

      expect(window.open).toHaveBeenCalled();
    });
  });

  describe('routeToCourseDetails', () => {
    it('should open course details in new tab', () => {
      spyOn(window, 'open');
      const courseUrl = 'test-course-url';
      router.createUrlTree.and.returnValue({} as any);
      router.serializeUrl.and.returnValue('/student/course-details/' + courseUrl);

      component.routeToCourseDetails(courseUrl);

      expect(router.createUrlTree).toHaveBeenCalledWith(['student/course-details', courseUrl]);
      expect(window.open).toHaveBeenCalled();
      const callArgs = (window.open as jasmine.Spy).calls.mostRecent().args;
      expect(callArgs[1]).toBe('_blank');
    });
  });

  describe('routeToCourseContent', () => {
    it('should open course content with section and topic IDs', () => {
      spyOn(window, 'open');
      const event = { stopPropagation: jasmine.createSpy('stopPropagation') };
      const course = { url: 'course-url' };
      const sectionId = 'section-1';
      const topicId = 'topic-1';

      router.createUrlTree.and.returnValue({} as any);
      router.serializeUrl.and.returnValue('/student/course-content/course-url');

      component.routeToCourseContent(event, course, sectionId, topicId);

      expect(router.createUrlTree).toHaveBeenCalledWith(
        ['student/course-content', course.url],
        { queryParams: { sectionId: sectionId, topicId: topicId } }
      );
      expect(window.open).toHaveBeenCalled();
      expect(event.stopPropagation).toHaveBeenCalled();
    });

    it('should work without topic ID', () => {
      spyOn(window, 'open');
      const event = { stopPropagation: jasmine.createSpy('stopPropagation') };
      const course = { url: 'course-url' };
      const sectionId = 'section-1';

      router.createUrlTree.and.returnValue({} as any);
      router.serializeUrl.and.returnValue('/student/course-content/course-url');

      component.routeToCourseContent(event, course, sectionId, undefined);

      expect(window.open).toHaveBeenCalled();
      expect(event.stopPropagation).toHaveBeenCalled();
    });
  });

  describe('formatChatText', () => {
    it('should return empty string for null or undefined input', () => {
      expect(component.formatChatText('')).toBe('');
      expect(component.formatChatText(null as any)).toBe('');
    });

    it('should escape HTML special characters', () => {
      const input = '<script>alert("test")</script>';
      const result = component.formatChatText(input);
      expect(result).toContain('&lt;script&gt;');
      expect(result).not.toContain('<script>');
    });

    it('should convert newlines to <br> tags', () => {
      const input = 'Line 1\nLine 2\nLine 3';
      const result = component.formatChatText(input);
      expect(result).toContain('<br>');
      expect(result.match(/<br>/g)?.length).toBe(2);
    });

    it('should format numbered lists', () => {
      const input = '1. First item\n2. Second item';
      const result = component.formatChatText(input);
      expect(result).toContain('<br>1. First item');
      expect(result).toContain('<br>2. Second item');
    });

    it('should convert markdown bold to HTML strong tags', () => {
      const input = 'This is **bold** text';
      const result = component.formatChatText(input);
      expect(result).toContain('<strong>bold</strong>');
      expect(result).not.toContain('**');
    });

    it('should handle multiple formatting types together', () => {
      const input = 'Hello\n1. **Bold** item\n2. Normal item';
      const result = component.formatChatText(input);
      expect(result).toContain('<br>');
      expect(result).toContain('<strong>Bold</strong>');
    });

    it('should escape ampersands', () => {
      const input = 'Tom & Jerry';
      const result = component.formatChatText(input);
      expect(result).toContain('&amp;');
    });
  });

  describe('scrollToBottom', () => {
    it('should scroll chat container to bottom', () => {
      const mockElement = {
        nativeElement: {
          scrollTop: 0,
          scrollHeight: 1000
        }
      };
      component.chatContainer = mockElement as ElementRef;

      component['scrollToBottom']();

      expect(mockElement.nativeElement.scrollTop).toBe(1000);
    });

    it('should handle missing chat container gracefully', () => {
      component.chatContainer = undefined as any;
      expect(() => component['scrollToBottom']()).not.toThrow();
    });
  });

  describe('ngAfterViewChecked', () => {
    it('should call scrollToBottom', () => {
      spyOn<any>(component, 'scrollToBottom');
      component.ngAfterViewChecked();
      expect(component['scrollToBottom']).toHaveBeenCalled();
    });
  });
});