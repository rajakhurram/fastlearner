import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { CourseListComponent } from './course-list.component';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { ActivatedRoute, Router } from '@angular/router';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { Meta, Title } from '@angular/platform-browser';
import { of, throwError } from 'rxjs';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { environment } from 'src/environments/environment.development';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import {
  BrowserAnimationsModule,
  NoopAnimationsModule,
} from '@angular/platform-browser/animations';
import { NzIconModule } from 'ng-zorro-antd/icon';

describe('CourseListComponent', () => {
  let component: CourseListComponent;
  let fixture: ComponentFixture<CourseListComponent>;
  let mockCourseService: jasmine.SpyObj<CourseService>;
  let mockMessageService: jasmine.SpyObj<MessageService>;
  let mockNgxUiLoaderService: jasmine.SpyObj<NgxUiLoaderService>;
  let mockMetaService: jasmine.SpyObj<Meta>;
  let mockTitleService: jasmine.SpyObj<Title>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockActivatedRoute: jasmine.SpyObj<ActivatedRoute>;

  beforeEach(async () => {
    const courseServiceSpy = jasmine.createSpyObj('CourseService', [
      'getCourseCategory',
      'getCoursesByCategory',
      'getAllCourses',
      'applyFilter'
    ]);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', ['info']);
    const ngxUiLoaderServiceSpy = jasmine.createSpyObj('NgxUiLoaderService', [
      'start',
      'stop',
    ]);
    const metaServiceSpy = jasmine.createSpyObj('Meta', ['updateTag']);
    const titleServiceSpy = jasmine.createSpyObj('Title', ['setTitle']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const activatedRouteSpy = jasmine.createSpyObj(
      'ActivatedRoute',
      ['queryParams'],
      { queryParams: of({}) }
    );

    await TestBed.configureTestingModule({
      imports: [BrowserAnimationsModule, NoopAnimationsModule, NzIconModule],
      declarations: [CourseListComponent],
      providers: [
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: NgxUiLoaderService, useValue: ngxUiLoaderServiceSpy },
        { provide: Meta, useValue: metaServiceSpy },
        { provide: Title, useValue: titleServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: activatedRouteSpy },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(CourseListComponent);
    component = fixture.componentInstance;
    mockCourseService = TestBed.inject(
      CourseService
    ) as jasmine.SpyObj<CourseService>;
    mockMessageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
    mockNgxUiLoaderService = TestBed.inject(
      NgxUiLoaderService
    ) as jasmine.SpyObj<NgxUiLoaderService>;
    mockMetaService = TestBed.inject(Meta) as jasmine.SpyObj<Meta>;
    mockTitleService = TestBed.inject(Title) as jasmine.SpyObj<Title>;
    mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    mockActivatedRoute = TestBed.inject(
      ActivatedRoute
    ) as jasmine.SpyObj<ActivatedRoute>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set the page title and meta tags on initialization', () => {
    component.ngOnInit();
    expect(mockTitleService.setTitle).toHaveBeenCalledWith(
      'Courses | Fastlearner.ai'
    );
    expect(mockMetaService.updateTag).toHaveBeenCalledWith({
      name: 'Courses',
      content: 'Courses page of Fastlearner.ai',
    });
  });

  it('should handle error while getting category list', () => {
    mockCourseService.getCourseCategory.and.returnValue(
      throwError(() => new Error('Error'))
    );
    component.ngOnInit();
    expect(component.categoryList).toEqual([]);
  });

  it('should call getCourseListByCategory with the correct payload', () => {
    spyOn(component, 'getCourseListByCategory');
    component.onChangeOfCategory(1);
    expect(component.payLoad.categoryId).toBe(1);
    expect(component.getCourseListByCategory).toHaveBeenCalled();
  });

  it('should handle course list response', () => {
    const mockCourses = {
      data: { data: [{ courseDuration: 3600 }], pages: 1 },
      status: 200,
    };
    mockCourseService.getCoursesByCategory.and.returnValue(of(mockCourses));
    component.getCourseListByCategory();
    expect(component.courseList[0].courseDuration).toBe('1 hours');
  });

  it('should handle course list error', () => {
    mockCourseService.getCoursesByCategory.and.returnValue(
      throwError(() => new Error('Error'))
    );
    component.getCourseListByCategory();
    expect(component.showErrorMsg).toBe(true);
  });

  it('should navigate to course details', () => {
    component.routeToCourseDetails('Test Course');
    expect(mockRouter.navigate).toHaveBeenCalledWith([
      'student/course-details',
      'Test Course',
    ]);
  });

  it('should navigate to instructor profile', () => {
    const mockEvent = {
      profileUrl: 'john',
      stopPropagation: jasmine.createSpy('stopPropagation'),
    };

    component.routeToInstructorProfile(mockEvent);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['user/profile'], {
      queryParams: { url: 'john' },
    });
    expect(mockEvent.stopPropagation).toHaveBeenCalled();
  });

  it('should render the course cards when courseList is populated', () => {
    const mockCourses = [
      {
        title: 'Course 1',
        categoryName: 'Category 1',
        review: 4.5,
        noOfReviewers: 10,
        courseDescription: 'Description 1',
        creatorName: 'Instructor 1',
        courseDuration: '2 hours',
        courseThumbnailUrl: 'url-to-thumbnail',
        instructorImage: 'url-to-image',
        creatorId: 1,
        courseId: 1,
        noOfTopics: 5,
      },
    ];
    component.courseList = mockCourses;
    fixture.detectChanges();
    expect(component.courseList).toEqual(mockCourses);
  });

  it('should display "No Course Found" message when courseList is empty and showErrorMsg is true', () => {
    component.courseList = [];
    component.showErrorMsg = true;
    fixture.detectChanges();
    const emptyMessageElement = fixture.nativeElement.querySelector('nz-empty');
    expect(emptyMessageElement).toBeTruthy();
  });

  it('should call showMoreCourse when "Show More" button is clicked', () => {
    spyOn(component, 'showMoreCourse');
    const button = fixture.nativeElement.querySelector('.show-more-button');
    button?.click();
    expect(component.showMoreCourse).toHaveBeenCalled();
  });

  it('should disable "Show More" button if there are no more pages', () => {
    component.filterPayload.pageNo = 0;
    component.totalCoursePages = 1;
    fixture.detectChanges();
    const button = fixture.nativeElement.querySelector('.show-more-button');
    expect(button.disabled).toBeTrue();
  });
  it('should handle level change and clear level parameter', () => {
    spyOn(component, 'clearLevelParam').and.callThrough();
    spyOn(component, 'getCourseListByCategory');

    // Case when level is 0
    component.onChangeOfLevel(0);
    expect(component.clearLevelParam).toHaveBeenCalled();
    expect(component.payLoad.courseLevelId).toBeNull();
    expect(component.getCourseListByCategory).toHaveBeenCalled();

    // Case when level is a valid number
    component.onChangeOfLevel(2);
    expect(component.clearLevelParam).toHaveBeenCalled();
    expect(component.payLoad.courseLevelId).toBe(2);
    expect(component.getCourseListByCategory).toHaveBeenCalled();

    // Case when event is null
    component.onChangeOfLevel(null);
    expect(component.clearLevelParam).toHaveBeenCalled();
    expect(component.payLoad.courseLevelId).toBeNull();
    expect(component.getCourseListByCategory).toHaveBeenCalled();
  });

  it('should increase page number and call applyFilter on showMoreCourse', () => {
    spyOn(component, 'getCourseListByCategory');
    component.showMoreCourse();
    expect(component.filterPayload.pageNo).toBe(1);
    expect(component.applyFilter);
  });

  it('should clear level parameter', () => {
    component.clearLevelParam();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['./'], {
      relativeTo: mockActivatedRoute,
      queryParams: { level: null },
      queryParamsHandling: 'merge',
    });
  });

  it('should navigate to course details content', () => {
    component.routeToCourseDetailsContent('Test Course');
    expect(mockRouter.navigate).toHaveBeenCalledWith(
      ['student/course-details', 'Test Course'],
      { fragment: 'course-content' }
    );
  });
});
