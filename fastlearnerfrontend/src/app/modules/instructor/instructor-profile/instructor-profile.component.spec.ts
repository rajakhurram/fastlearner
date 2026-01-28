import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { InstructorProfileComponent } from './instructor-profile.component';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { By } from '@angular/platform-browser';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { NzButtonModule } from 'ng-zorro-antd/button';

describe('InstructorProfileComponent', () => {
  let component: InstructorProfileComponent;
  let fixture: ComponentFixture<InstructorProfileComponent>;
  let courseService: jasmine.SpyObj<CourseService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let router: jasmine.SpyObj<Router>;
  let activatedRoute: ActivatedRoute;
  const httpConstants = new HttpConstants();

  beforeEach(async () => {
    const courseServiceSpy = jasmine.createSpyObj('CourseService', [
      'getInstructorProfile',
      'getInstructorCourses',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', ['info']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [
        AntDesignModule,
        BrowserAnimationsModule,
        NzAvatarModule,
        NzButtonModule,
      ],
      declarations: [InstructorProfileComponent],
      providers: [
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: { queryParams: of({ instructorId: '123' }) },
        },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(InstructorProfileComponent);
    component = fixture.componentInstance;
    courseService = TestBed.inject(
      CourseService
    ) as jasmine.SpyObj<CourseService>;
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    activatedRoute = TestBed.inject(ActivatedRoute);
    activatedRoute.queryParams = of({ instructorId: '123' });
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize and fetch instructor profile and courses', () => {
    const mockProfile = { name: 'John Doe', bio: 'Instructor Bio' };
    const mockCourses = {
      data: [{ id: 1, title: 'Course 1', courseDuration: 3600 }],
      pages: 1,
    };
    const mockProfileResponse = {
      status: httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      data: mockProfile,
    };
    const mockCoursesResponse = {
      status: httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      data: mockCourses,
    };

    courseService.getInstructorProfile.and.returnValue(of(mockProfileResponse));
    courseService.getInstructorCourses.and.returnValue(of(mockCoursesResponse));

    component.ngOnInit();

    expect(courseService.getInstructorProfile).toHaveBeenCalled();
    expect(courseService.getInstructorCourses).toHaveBeenCalledWith(
      component.coursePayLoad
    );
    expect(component.instructorPublicProfile).toEqual(mockProfile);
    expect(component.courseList).toEqual(mockCourses.data);
    expect(component.noOfCount).toBe(mockCourses.pages);
  });

  it('should handle error when fetching instructor profile', () => {
    courseService.getInstructorProfile.and.returnValue(
      throwError({ status: 500 })
    );

    component.getInstructorPublicProfile();

    expect(courseService.getInstructorProfile).toHaveBeenCalled();
    expect(component.instructorPublicProfile).toBeUndefined();
  });

  it('should handle error when fetching courses', () => {
    courseService.getInstructorCourses.and.returnValue(
      throwError({ status: 404 })
    );

    component.getInstructorCourseList(true);

    expect(courseService.getInstructorCourses).toHaveBeenCalledWith(
      component.coursePayLoad
    );
    expect(component.courseList).toEqual([]);
  });

  it('should show more courses', () => {
    spyOn(component, 'getInstructorCourseList').and.callThrough();

    component.showMore();

    expect(component.coursePayLoad.pageNo).toBe(1);
    expect(component.getInstructorCourseList).toHaveBeenCalledWith(false);
  });

  it('should navigate to course details', () => {
    const courseTitle = 'Course Title';
    const expectedRoute = ['student/course-details', 'Course Title'];

    component.routeToCourseDetails(courseTitle);

    expect(router.navigate).toHaveBeenCalledWith(expectedRoute);
  });

  it('should navigate to course details with fragment', () => {
    const courseTitle = 'Course Title';
    const expectedRoute = ['student/course-details', 'Course Title'];
    const expectedOptions = { fragment: 'course-content' };

    component.routeToCourseDetailsContent(courseTitle);

    expect(router.navigate).toHaveBeenCalledWith(
      expectedRoute,
      expectedOptions
    );
  });

  it('should convert seconds to hours and minutes', () => {
    expect(component.convertSecondsToHoursAndMinutes(3600)).toBe('1 hours');
    expect(component.convertSecondsToHoursAndMinutes(1800)).toBe('30 minutes');
    expect(component.convertSecondsToHoursAndMinutes(3660)).toBe(
      '1 hours 1 minutes'
    );
  });

  it('should handle routeTo with invalid route', () => {
    spyOn(window, 'open');
    component.routeTo();

    expect(messageService.info).toHaveBeenCalledWith('No link found');
    expect(window.open).not.toHaveBeenCalled();
  });

  it('should display instructor profile information correctly', () => {
    const mockProfile = {
      fullName: 'John Doe',
      headline: 'Expert in Angular',
      profilePicture: 'path/to/profile.jpg',
      twitterUrl: 'https://twitter.com/johndoe',
      facebookUrl: 'https://facebook.com/johndoe',
      linkedInUrl: 'https://linkedin.com/in/johndoe',
      youtubeUrl: 'https://youtube.com/johndoe',
      websiteUrl: 'https://johndoe.com',
      totalStudents: 1000,
      totalReviews: 200,
      specialization: 'Software Engineering',
      qualification: 'PhD in Computer Science',
      experience: '10 years',
      aboutMe: 'Passionate about teaching and coding.',
    };

    component.instructorPublicProfile = mockProfile;
    fixture.detectChanges();

    const profileName = fixture.debugElement.query(
      By.css('.profile-details h2')
    ).nativeElement.textContent;
    const profileHeadline = fixture.debugElement.query(
      By.css('.profile-details p')
    ).nativeElement.textContent;

    expect(profileName).toContain(mockProfile.fullName);
    expect(profileHeadline).toContain(mockProfile.headline);
  });

  it('should show empty message when no courses are available', () => {
    component.courseList = [];
    fixture.detectChanges();

    const emptyMessage = fixture.debugElement.query(
      By.css('.center nz-empty')
    ).nativeElement;
    expect(emptyMessage).toBeTruthy();
  });
});
