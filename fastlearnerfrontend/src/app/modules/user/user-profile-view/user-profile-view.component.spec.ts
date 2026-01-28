import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UserProfileViewComponent } from './user-profile-view.component';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';

describe('UserProfileViewComponent', () => {
  let component: UserProfileViewComponent;
  let fixture: ComponentFixture<UserProfileViewComponent>;
  let courseService: jasmine.SpyObj<CourseService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let router: jasmine.SpyObj<Router>;
  let activatedRoute: ActivatedRoute;
  const httpConstants = new HttpConstants();

  beforeEach(async () => {
    const courseServiceSpy = jasmine.createSpyObj('CourseService', ['getInstructorPublicProfile', 'getInstructorCourses']);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', ['info']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    
    await TestBed.configureTestingModule({
      declarations: [UserProfileViewComponent],
      providers: [
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: { queryParams: of({ url: 'john' }) } }
      ],
      schemas : [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(UserProfileViewComponent);
    component = fixture.componentInstance;
    courseService = TestBed.inject(CourseService) as jasmine.SpyObj<CourseService>;
    messageService = TestBed.inject(MessageService) as jasmine.SpyObj<MessageService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    activatedRoute = TestBed.inject(ActivatedRoute);
    activatedRoute.queryParams = of({ url: 'john' });

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch instructor public profile and course list on profile fetch success', () => {
    const mockProfileResponse = { 
      status: httpConstants.REQUEST_STATUS.SUCCESS_200.CODE, 
      data: { name: 'John Doe', userId: 1 }
    };
    const mockCourseResponse = { 
      status: httpConstants.REQUEST_STATUS.SUCCESS_200.CODE, 
      data: { data: [] } 
    };

    courseService.getInstructorPublicProfile.and.returnValue(of(mockProfileResponse));
    courseService.getInstructorCourses.and.returnValue(of(mockCourseResponse));

    component.ngOnInit();

    expect(courseService.getInstructorPublicProfile).toHaveBeenCalledWith('john');
    expect(component.instructorPublicProfile).toEqual(mockProfileResponse.data);
    expect(courseService.getInstructorCourses).toHaveBeenCalledWith(component.coursePayLoad);
    expect(component.courseList).toEqual([]);
  });

  it('should handle error when fetching instructor public profile', () => {
    courseService.getInstructorPublicProfile.and.returnValue(throwError({ status: 404 }));

    component.ngOnInit();

    expect(component.instructorPublicProfile).toBeUndefined();
  });

  it('should handle error when fetching course list', () => {
    const mockProfileResponse = { 
      status: httpConstants.REQUEST_STATUS.SUCCESS_200.CODE, 
      data: { name: 'John Doe', userId: 1 }
    };

    courseService.getInstructorPublicProfile.and.returnValue(of(mockProfileResponse));
    courseService.getInstructorCourses.and.returnValue(throwError({ error: { status: 404 } }));

    component.ngOnInit();

    expect(courseService.getInstructorCourses).toHaveBeenCalledWith(component.coursePayLoad);
    expect(component.courseList).toEqual([]);
  });

  it('should route to course details', () => {
    const title = 'Test Course';
    component.routeToCourseDetails(title);

    expect(router.navigate).toHaveBeenCalledWith(['student/course-details', 'Test Course']);
  });

  it('should convert seconds to hours and minutes correctly', () => {
    expect(component.convertSecondsToHoursAndMinutes(3661)).toEqual('1 hours 1 minutes');
    expect(component.convertSecondsToHoursAndMinutes(3600)).toEqual('1 hours');
    expect(component.convertSecondsToHoursAndMinutes(61)).toEqual('1 minutes');
  });

  it('should show more courses', () => {
    component.coursePayLoad.pageNo = 1;
    component.noOfCount = 5;
    const mockCourseResponse = { 
      status: httpConstants.REQUEST_STATUS.SUCCESS_200.CODE, 
      data: { data: [] } 
    };
    courseService.getInstructorCourses.and.returnValue(of(mockCourseResponse));

    component.showMore();

    expect(component.coursePayLoad.pageNo).toBe(2);
    expect(component.noOfCount).toBe(4);
    expect(courseService.getInstructorCourses).toHaveBeenCalledWith(component.coursePayLoad);
  });

  it('should handle show more course error', () => {
    component.coursePayLoad.pageNo = 1;
    component.noOfCount = 5;
    courseService.getInstructorCourses.and.returnValue(throwError({ error: { status: 404 } }));

    component.showMore();

    expect(component.noOfCount).toBe(4);
  });
});
