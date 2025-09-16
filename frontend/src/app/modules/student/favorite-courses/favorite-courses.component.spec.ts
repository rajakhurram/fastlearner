import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FavoriteCoursesComponent } from './favorite-courses.component';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('FavoriteCoursesComponent', () => {
  let component: FavoriteCoursesComponent;
  let fixture: ComponentFixture<FavoriteCoursesComponent>;
  let mockCourseService: jasmine.SpyObj<CourseService>;
  let mockMessageService: jasmine.SpyObj<MessageService>;
  let mockSharedService: jasmine.SpyObj<SharedService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const courseServiceSpy = jasmine.createSpyObj('CourseService', [
      'getFavoriteCourses',
      'addOrRemoveCourseToFavorite',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', ['info']);
    const sharedServiceSpy = jasmine.createSpyObj('SharedService', [
      'updateFavCourseMenu',
    ]);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [FavoriteCoursesComponent],
      imports: [BrowserAnimationsModule],
      providers: [
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: SharedService, useValue: sharedServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA], // Ignore unknown elements
    }).compileComponents();

    fixture = TestBed.createComponent(FavoriteCoursesComponent);
    component = fixture.componentInstance;
    mockCourseService = TestBed.inject(
      CourseService
    ) as jasmine.SpyObj<CourseService>;
    mockMessageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
    mockSharedService = TestBed.inject(
      SharedService
    ) as jasmine.SpyObj<SharedService>;
    mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should call getFavoriteCourseList on initialization', () => {
    spyOn(component, 'getFavoriteCourseList');
    component.ngOnInit();
    expect(component.getFavoriteCourseList).toHaveBeenCalledWith(
      component.page,
      component.size
    );
  });

  it('should populate favoriteCourseList with courses on successful API response', () => {
    const mockResponse = {
      status: 200,
      data: {
        favouriteCourses: [
          {
            courseId: 1,
            courseTitle: 'Course 1',
            courseDuration: 3600,
            creatorId: 1,
          },
        ],
        totalPages: 1,
      },
    };
    mockCourseService.getFavoriteCourses.and.returnValue(of(mockResponse));
    component.getFavoriteCourseList(0, 8);
    fixture.detectChanges();
    expect(component.favoriteCourseList.length).toBe(1);
  });

  it('should handle API errors correctly', () => {
    const mockErrorResponse = {
      error: { status: 404 },
    };
    mockCourseService.getFavoriteCourses.and.returnValue(
      throwError(mockErrorResponse)
    );
    component.getFavoriteCourseList(0, 8);
    fixture.detectChanges();
    expect(component.favoriteCourseList.length).toBe(0);
  });

  it('should update favoriteCourseList and call updateFavCourseMenu on successful toggleFavoriteCourse', () => {
    const mockResponse = {
      status: 200,
    };
    mockCourseService.addOrRemoveCourseToFavorite.and.returnValue(
      of(mockResponse)
    );
    const initialListLength = component.favoriteCourseList.length;
    component.favoriteCourseList = [{ courseId: 1 }];
    component.toggleFavoriteCourse(1);
    fixture.detectChanges();
    expect(mockSharedService.updateFavCourseMenu).toHaveBeenCalled();
  });

  it('should not navigate to course details if isTogglingFavorite is true', () => {
    component.isTogglingFavorite = true;
    component.routeToCourseDetails('Test Course');
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should navigate to course details when routeToCourseDetails is called', () => {
    component.isTogglingFavorite = false;
    component.routeToCourseDetails('Test Course');
    expect(mockRouter.navigate).toHaveBeenCalledWith([
      'student/course-details',
      'Test Course',
    ]);
  });

  it('should handle click on show more button and fetch more courses', () => {
    spyOn(component, 'showMore');
    const button = fixture.nativeElement.querySelector('button');
    button?.click();
    expect(component.showMore).toHaveBeenCalled();
  });

  it('should call search method and reset favoriteCourseList', () => {
    spyOn(component, 'getFavoriteCourseList');
    component.favoriteCourseList = [{ courseId: 1 }];
    component.search();
    expect(component.favoriteCourseList.length).toBe(0);
    expect(component.getFavoriteCourseList).toHaveBeenCalledWith(
      0,
      component.size
    );
  });

  it('should call convertSecondsToHoursAndMinutes and return formatted duration', () => {
    const result = component.convertSecondsToHoursAndMinutes(3660);
    expect(result).toBe('1 hours 1 minutes');
  });
});
