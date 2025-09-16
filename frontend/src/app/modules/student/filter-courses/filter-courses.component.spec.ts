import { ComponentFixture, fakeAsync, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { FilterCoursesComponent } from './filter-courses.component';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { environment } from 'src/environments/environment.development';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('FilterCoursesComponent', () => {
  let component: FilterCoursesComponent;
  let fixture: ComponentFixture<FilterCoursesComponent>;
  let courseService: jasmine.SpyObj<CourseService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const courseServiceSpy = jasmine.createSpyObj(
      'CourseService',
      ['searchCourse', 'setSearchResults'],
      { searchResults$: of(null), $searchSuggestionsIds: of(null) }
    );
    const messageServiceSpy = jasmine.createSpyObj('MessageService', ['']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [
        FormsModule,
        RouterModule.forRoot([]),
        BrowserAnimationsModule,
        ReactiveFormsModule,
        AntDesignModule,
      ],
      declarations: [FilterCoursesComponent],
      providers: [
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: { queryParams: of({ search: 'test' }) },
        },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    courseService = TestBed.inject(
      CourseService
    ) as jasmine.SpyObj<CourseService>;
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FilterCoursesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have default values', () => {
    expect(component.courseList).toEqual([]);
    expect(component.selectedRating).toBeUndefined();
    expect(component.showSelect).toBeFalse();
    expect(component.payLoad).toEqual({
      reviewFrom: 0,
      reviewTo: 5,
      searchValue: 'test',
      pageNo: 0,
      pageSize: 8,
      isNlpSearch: true,
    });
  });

  it('should navigate to course details page', () => {
    component.routeToCourseDetails('Course Title');
    expect(router.navigate).toHaveBeenCalledWith([
      'student/course-details',
      'Course Title',
    ]);
  });

  it('should update payLoad based on rating change', () => {
    component.onChangeOfRating('5');
    expect(component.payLoad.reviewFrom).toBe(4.1);
    expect(component.payLoad.reviewTo).toBe(5);

    component.onChangeOfRating('0');
    expect(component.payLoad.reviewFrom).toBeNull();
    expect(component.payLoad.reviewTo).toBeNull();
  });

  it('should convert seconds to hours and minutes', () => {
    expect(component.convertSecondsToHoursAndMinutes(3600)).toBe('1 hours');
    expect(component.convertSecondsToHoursAndMinutes(1800)).toBe('30 minutes');
    expect(component.convertSecondsToHoursAndMinutes(3900)).toBe(
      '1 hours 5 minutes'
    );
  });

  it('should contain elements in the template', () => {
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('.filter-course')).toBeTruthy();
  });

  it('should display "No Course Found" when courseList is empty', () => {
    component.courseList = [];
    fixture.detectChanges();

    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('.center')).toBeTruthy();
    expect(compiled.querySelector('nz-empty')).toBeTruthy();
  });
  it('should handle error from searchCourse', () => {
    const errorResponse = { status: 500, message: 'Server Error' };
    courseService.searchCourse.and.returnValue(throwError(() => errorResponse));

    component.searchByKeyword();
    expect(component.courseList).toEqual([]);
  });
  it('should handle error from searchResults$', () => {
    const errorResponse = { status: 500, message: 'Server Error' };
    courseService.searchResults$ = throwError(() => errorResponse);

    fixture.detectChanges();
    expect(component.courseList).toEqual([]);
  });

  it('should handle error from $searchSuggestionsIds', () => {
    // Mock observable to return an error
    const errorResponse = { status: 500, message: 'Server Error' };
    courseService.$searchSuggestionsIds = throwError(() => errorResponse);

    fixture.detectChanges();
    expect(component.searchKeyword).toEqual('test');
  });
  it('should handle invalid route parameters in routeToCourseDetailsContent', () => {
    const invalidTitle = '';
    component.routeToCourseDetailsContent(invalidTitle);
    expect(router.navigate).toHaveBeenCalledWith(
      ['student/course-details', ''],
      { fragment: 'course-content' }
    );
  });
  it('should display "No Course Found" when courseList is empty', () => {
    component.courseList = [];
    fixture.detectChanges();

    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('.center')).toBeTruthy();
    expect(compiled.querySelector('nz-empty')).toBeTruthy();
  });

  it('should have default values', () => {
    expect(component.courseList).toEqual([]);
    expect(component.selectedRating).toBeUndefined();
    expect(component.showSelect).toBeFalse();
    expect(component.payLoad).toEqual({
      reviewFrom: 0,
      reviewTo: 5,
      searchValue: 'test',
      pageNo: 0,
      pageSize: 8,
      isNlpSearch: true,
    });
  });

  it('should navigate to course details page', () => {
    component.routeToCourseDetails('Course Title');
    expect(router.navigate).toHaveBeenCalledWith([
      'student/course-details',
      'Course Title',
    ]);
  });

  it('should navigate to instructor profile', () => {
    component.routeToInsructorProfile("john");
    expect(router.navigate).toHaveBeenCalledWith(
      ['user/profile'],
      {
        queryParams: { url: "john" },
      }
    );
  });

  it('should update payLoad based on rating change', () => {
    component.onChangeOfRating('5');
    expect(component.payLoad.reviewFrom).toBe(4.1);
    expect(component.payLoad.reviewTo).toBe(5);

    component.onChangeOfRating('0');
    expect(component.payLoad.reviewFrom).toBeNull();
    expect(component.payLoad.reviewTo).toBeNull();
  });

  it('should convert seconds to hours and minutes', () => {
    expect(component.convertSecondsToHoursAndMinutes(3600)).toBe('1 hours');
    expect(component.convertSecondsToHoursAndMinutes(1800)).toBe('30 minutes');
    expect(component.convertSecondsToHoursAndMinutes(3900)).toBe(
      '1 hours 5 minutes'
    );
  });

  it('should contain elements in the template', () => {
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('.filter-course')).toBeTruthy();
  });

  it('should display "No Course Found" when courseList is empty', () => {
    component.courseList = [];
    fixture.detectChanges();

    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('.center')).toBeTruthy();
    expect(compiled.querySelector('nz-empty')).toBeTruthy();
  });

  it('should handle error from searchCourse', () => {
    const errorResponse = { status: 500, message: 'Server Error' };
    courseService.searchCourse.and.returnValue(throwError(() => errorResponse));

    component.searchByKeyword();
    expect(component.courseList).toEqual([]);
  });

  it('should handle error from searchResults$', () => {
    const errorResponse = { status: 500, message: 'Server Error' };
    courseService.searchResults$ = throwError(() => errorResponse);

    fixture.detectChanges();
    expect(component.courseList).toEqual([]);
  });

  it('should handle error from $searchSuggestionsIds', () => {
    const errorResponse = { status: 500, message: 'Server Error' };
    courseService.$searchSuggestionsIds = throwError(() => errorResponse);

    fixture.detectChanges();

    expect(component.searchKeyword).toEqual('test');
  });

  it('should handle invalid route parameters in routeToCourseDetailsContent', () => {
    const invalidTitle = '';
    component.routeToCourseDetailsContent(invalidTitle);
    expect(router.navigate).toHaveBeenCalledWith(
      ['student/course-details', ''],
      { fragment: 'course-content' }
    );
  });

  describe('showMoreCourse', () => {
    it('should fetch courses on show more', () => {
      component.disableShowMore = false;
      component.showMoreCourse();
      expect(component.payLoad.pageNo).toBeDefined();
    });
  });
});
