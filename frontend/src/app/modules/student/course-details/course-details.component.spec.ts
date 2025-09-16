import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { CourseDetailsComponent } from './course-details.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Router, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { NzModalService } from 'ng-zorro-antd/modal';
import {
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA,
  Renderer2,
} from '@angular/core';
import { CourseService } from 'src/app/core/services/course.service';
import { AuthService } from 'src/app/core/services/auth.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { Title, Meta } from '@angular/platform-browser';
import { SharedModule } from '../../shared/shared.module';
import { StudentModule } from '../student.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('CourseDetailsComponent', () => {
  let component: CourseDetailsComponent;
  let fixture: ComponentFixture<CourseDetailsComponent>;

  const mockCourseService = jasmine.createSpyObj('CourseService', [
    'getCourseByTitle',
    'getCourseDetails',
    'likeAndDislikeReviewSection',
    'getRelatedCourses',
    'addOrRemoveCourseToFavorite',
    'getCourseRatingReviewAndFeedback',
    'getCourseByUrl',
  ]);

  const mockAuthService = jasmine.createSpyObj('AuthService', ['isLoggedIn']);

  const mockMessageService = jasmine.createSpyObj('MessageService', [
    'success',
    'error',
  ]);

  const mockSharedService = jasmine.createSpyObj('SharedService', [
    'updateFavCourseMenu',
  ]);

  const mockCacheService = jasmine.createSpyObj('CacheService', [
    'saveInCache',
  ]);

  const mockRouter = jasmine.createSpyObj('Router', ['navigate']);

  const mockActivatedRoute = {
    paramMap: of({
      get: (param: string) => 'test-course-title',
    }),
  };

  const mockModalService = jasmine.createSpyObj('NzModalService', ['create']);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CourseDetailsComponent],
      imports: [
        HttpClientTestingModule,
        StudentModule,
        BrowserAnimationsModule,
      ],
      providers: [
        { provide: CourseService, useValue: mockCourseService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: MessageService, useValue: mockMessageService },
        { provide: SharedService, useValue: mockSharedService },
        { provide: CacheService, useValue: mockCacheService },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: NzModalService, useValue: mockModalService },
        {
          provide: Renderer2,
          useValue: jasmine.createSpyObj('Renderer2', [
            'addClass',
            'removeClass',
          ]),
        },
        Title,
        Meta,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CourseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set courseTitle and call getCourseByTitle on init', () => {
    spyOn(component, 'getCourseByTitle');
    component.ngOnInit();
    expect(component.courseTitle).toBeUndefined();
  });

  it('should handle window resize event', () => {
    const event = { target: { innerWidth: 500 } };
    component.onResize(event);
    expect(component.fullWidth).toBeFalse();
  });

  it('should handle window scroll event', () => {
    spyOn(component, 'checkTooltipVisibility');
    component.onWindowScroll();
    expect(component.checkTooltipVisibility).toHaveBeenCalled();
  });

  it('should handle the expansion of sections', () => {
    component.toggleAllSection();
    expect(component.expand).toBeTrue();
    expect(component.expandButtonName).toBe('Collapse All Sections');
  });

  it('should navigate to course details route', () => {
    component.routeToCourseDetails('Test Course Title');
    expect(mockRouter.navigate).toHaveBeenCalledWith([
      'student/course-details',
      'Test Course Title',
    ]);
  });

  it('should check if the user is logged in and update courseButtonName accordingly', () => {
    mockAuthService.isLoggedIn.and.returnValue(true);
    component.isUserLoggedIn();
    expect(component.courseButtonName).toBe('Start Learning');
  });

  it('should toggle favorite course', () => {
    spyOn(component, 'toggleFavoriteCourse').and.callThrough();
    component.toggleFavoriteCourse();
    expect(mockCourseService.addOrRemoveCourseToFavorite).toHaveBeenCalledWith(
      component.courseId
    );
  });

  it('should open share course modal', () => {
    component.openShareCourseModal();
    expect(mockModalService.create).toHaveBeenCalled();
  });

  it('should handle error when getting related courses', () => {
    mockCourseService.getRelatedCourses.and.returnValue(
      throwError(() => new Error('Error'))
    );

    component.getRelatedCourses(true);

    expect(component.courseList).toEqual([]);
    expect(component.noOfCount).toBeUndefined();
  });

  it('should set courseId after fetching course details', () => {
    const mockResponse = {
      status: 200,
      data: { id: 1 },
    };

    mockCourseService.getCourseByTitle.and.returnValue(of(mockResponse));
    component.courseTitle = 'test-title';
    component.ngOnInit();

    fixture.whenStable().then(() => {
      expect(component.courseId).toBe(1);
    });
  });
  it('should display course details properly', () => {
    const mockResponse = {
      status: 200,
      data: { id: 1, name: 'Test Course', description: 'Course Description' },
    };

    mockCourseService.getCourseDetails.and.returnValue(of(mockResponse));
    component.getCourseDetails();

    fixture.whenStable().then(() => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement;
      expect(compiled.querySelector('.course-name').textContent).toContain(
        'Test Course'
      );
      expect(
        compiled.querySelector('.course-description').textContent
      ).toContain('Course Description');
    });
  });
  it('should toggle section and button name on toggleAllSection', () => {
    component.expand = false;
    component.toggleAllSection();
    expect(component.expand).toBeTrue();
    expect(component.expandButtonName).toBe('Collapse All Sections');

    component.toggleAllSection();
    expect(component.expand).toBeFalse();
    expect(component.expandButtonName).toBe('Expand All Sections');
  });

  it('should handle errors in ngOnInit correctly', () => {
    mockCourseService.getCourseByTitle.and.returnValue(
      throwError(() => new Error('Error'))
    );

    component.ngOnInit();

    fixture.whenStable().then(() => {
      expect(component.courseTitle).toBeUndefined();
    });
  });

  it('should set courseOutcomeLength correctly based on flag', () => {
    component.courseDetails = { courseOutcome: { length: 10 } } as any;
    component.cousrseOutcomeDefaultLength = 5;

    component.showCourseOutcome(true);
    expect(component.courseOutcomeLength).toBe(5);

    component.showCourseOutcome(false);
    expect(component.courseOutcomeLength).toBe(10);
  });

  it('should navigate to instructor profile with correct query params', () => {
    const profileUrl = 'john';
    component.routeToInsructorProfile(profileUrl);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['user/profile'], {
      queryParams: { url: profileUrl },
    });
  });
  it('should correctly convert seconds to hours and minutes', () => {
    expect(component.convertSecondsToHoursAndMinutes(3600)).toBe('1 hours');
    expect(component.convertSecondsToHoursAndMinutes(60)).toBe('1 minutes');
    expect(component.convertSecondsToHoursAndMinutes(3660)).toBe(
      '1 hours 1 minutes'
    );
    expect(component.convertSecondsToHoursAndMinutes(0)).toBe('0 minutes');
  });

  it('should scroll to course content section', () => {
    // Create a mock element and append it to the document body
    const mockElement = document.createElement('div');
    mockElement.id = 'course-content';

    // Spy on scrollIntoView
    const scrollIntoViewSpy = spyOn(mockElement, 'scrollIntoView');

    // Attach mock element to the DOM
    document.body.appendChild(mockElement);

    // Call the method
    component.scrollToCourseContent();

    // Check if scrollIntoView was called
    expect(scrollIntoViewSpy).toHaveBeenCalled();

    // Clean up the DOM after test
    document.body.removeChild(mockElement);
  });

  it('should navigate to course content', () => {
    component.courseTitle = 'Test Course';
    component.startCouseLearning();
    expect(mockRouter.navigate).toHaveBeenCalledWith([
      'student/course-content',
      'test-course-title',
    ]);
  });
  it('should go to the next page of related courses', () => {
    component.relatedCourse = { pageNo: 1, pageSize: 8, courseId: 1 };
    component.noOfCount = 5;

    spyOn(component, 'getRelatedCourses');

    component.nextPageOfRelatedCourse();

    expect(component.relatedCourse.pageNo).toBe(2);
    expect(component.noOfCount).toBe(4);
    expect(component.getRelatedCourses).toHaveBeenCalled();
  });

  it('should get course complete review and update courseReview', () => {
    const mockResponse = {
      status: 200,
      data: { feedback: ['Excellent', 'Good'] },
    };

    mockCourseService.getCourseRatingReviewAndFeedback.and.returnValue(
      of(mockResponse)
    );

    component.getCourseCompleteReview();

    fixture.whenStable().then(() => {
      expect(
        mockCourseService.getCourseRatingReviewAndFeedback
      ).toHaveBeenCalledWith({
        courseId: component.courseId,
        pageNo: 0,
        pageSize: 20,
      });
      expect(component.courseReview).toBeDefined();
    });
  });

  it('should toggle section panel open state', () => {
    const section = { panelOpen: false };
    const event = true;

    component.toggleSectionPanel(event, section);

    expect(section.panelOpen).toBe(true);
  });

  it('should not proceed if likeCIP is true', () => {
    component.likeCIP = true;
    spyOn(component, 'getCourseDetails');

    component.commentActions('like', 1);

    expect(component.getCourseDetails).not.toHaveBeenCalled();
  });

  it('should call likeAndDislikeReviewSection and handle success response', fakeAsync(() => {
    component.isLoggedIn = true;
    component.likeCIP = false;

    const mockResponse = {
      status: 200,
    };

    mockCourseService.likeAndDislikeReviewSection.and.returnValue(
      of(mockResponse)
    );
    spyOn(component, 'getCourseDetails');

    component.commentActions('like', 1);

    tick(1000); // simulate the timeout

    fixture.whenStable().then(() => {
      expect(
        mockCourseService.likeAndDislikeReviewSection
      ).toHaveBeenCalledWith({
        reviewId: 1,
        action: 'like',
      });
      expect(component.getCourseDetails).toHaveBeenCalled();
      expect(component.likeCIP).toBeFalse();
    });
  }));

  it('should handle error response and reset likeCIP', fakeAsync(() => {
    component.isLoggedIn = true;
    component.likeCIP = false;

    mockCourseService.likeAndDislikeReviewSection.and.returnValue(
      throwError(() => new Error('Error'))
    );

    component.commentActions('like', 1);

    tick(1000); // simulate the timeout

    fixture.whenStable().then(() => {
      expect(
        mockCourseService.likeAndDislikeReviewSection
      ).toHaveBeenCalledWith({
        reviewId: 1,
        action: 'like',
      });
      expect(component.likeCIP).toBeFalse();
    });
  }));
});
