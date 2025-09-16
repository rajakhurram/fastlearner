import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PerformanceComponent } from './performance.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { PerformanceService } from 'src/app/core/services/performance.service';
import { of } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { DatePipe } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';

describe('PerformanceComponent', () => {
  let component: PerformanceComponent;
  let fixture: ComponentFixture<PerformanceComponent>;
  let performanceService: jasmine.SpyObj<PerformanceService>;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    const performanceServiceSpy = jasmine.createSpyObj('PerformanceService', [
      'getCourseVisits',
      'getRatingsAndReviews',
      'getActiveStudents',
      'getCourseNames',
    ]);
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'getLoggedInName',
    ]);

    await TestBed.configureTestingModule({
      declarations: [PerformanceComponent],
      imports: [HttpClientTestingModule, SharedModule],
      providers: [
        DatePipe,
        { provide: PerformanceService, useValue: performanceServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(PerformanceComponent);
    component = fixture.componentInstance;
    performanceService = TestBed.inject(
      PerformanceService
    ) as jasmine.SpyObj<PerformanceService>;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;

    // Mock responses
    performanceService.getCourseVisits.and.returnValue(
      of({
        status: 200,
        data: [
          { monthName: 'January', totalVisitors: 100 },
          { monthName: 'February', totalVisitors: 150 },
        ],
      })
    );

    performanceService.getRatingsAndReviews.and.returnValue(
      of({
        status: 200,
        data: {
          totalPages: 1,
          feedback: {
            feedbackComments: [
              { createdAt: '2023-01-01T00:00:00Z', comment: 'Good' },
            ],
          },
        },
      })
    );

    performanceService.getActiveStudents.and.returnValue(
      of({
        status: 200,
        data: [
          { monthName: 'January', totalStudents: 10 },
          { monthName: 'February', totalStudents: 20 },
        ],
      })
    );

    performanceService.getCourseNames.and.returnValue(
      of({
        status: 200,
        data: [
          { id: 1, title: 'Course 1' },
          { id: 2, title: 'Course 2' },
        ],
      })
    );

    authService.getLoggedInName.and.returnValue('John Doe');

    fixture.detectChanges(); // Run ngOnInit
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load course names on init', () => {
    expect(component.courseNames.length).toBe(3); // Including 'All courses'
  });

  it('should load course visits on init', () => {
    expect(component.courseVisits.length).toBe(2);
    expect(component.chartOptionsBar.series[0]['data']).toEqual([100, 150]);
  });

  it('should load ratings and reviews on init', () => {
    expect(component.ratingsAndReviews).toBeTruthy();
    expect(component.ratingsAndReviews.feedback.feedbackComments.length).toBe(
      1
    );
  });

  it('should load active students on init', () => {
    expect(component.activeStudents.length).toBe(2);
    expect(component.chartOptionsLine.series[0]['data']).toEqual([10, 20]);
  });

  it('should transform date correctly', () => {
    const date = '2023-01-01T00:00:00Z';
    expect(component.transFormDate1(date)).toBe('January 1, 2023');
  });

  it('should show more reviews when showMoreReviews is called', () => {
    component.showMoreReviews();
    expect(component.payLoad.pageNo).toBe(1);
    expect(performanceService.getRatingsAndReviews).toHaveBeenCalled();
  });

  it('should select a course and load relevant data', () => {
    component.selectedCourseId = 1;
    component.selectCourse();
    expect(performanceService.getCourseVisits).toHaveBeenCalledWith(1);
    expect(performanceService.getRatingsAndReviews).toHaveBeenCalled();
    expect(performanceService.getActiveStudents).toHaveBeenCalled();
  });

  it('should return logged in user name', () => {
    expect(component.getInitialOfLoggedInUser).toBe('John Doe');
  });

  it('should handle no course visits data', () => {
    performanceService.getCourseVisits.and.returnValue(
      of({
        status: 200,
        data: [],
      })
    );
    component.getCourseVisits();
    expect(component.courseVisits.length).toBe(0);
    expect(component.chartOptionsBar.series[0]['data']).toEqual([]);
    expect(component.showChartOptionsBar).toBe(true);
  });

  it('should handle no ratings and reviews data', () => {
    performanceService.getRatingsAndReviews.and.returnValue(
      of({
        status: 200,
        data: {
          totalPages: 0,
          feedback: {
            feedbackComments: [],
          },
        },
      })
    );
    component.getRatingsAndReviews();
    expect(component.ratingsAndReviews.feedback.feedbackComments.length).toBe(
      1
    );
    expect(component.totalPages).toBe(0);
  });

  it('should handle no active students data', () => {
    performanceService.getActiveStudents.and.returnValue(
      of({
        status: 200,
        data: [],
      })
    );
    component.getActiveStudents();
    expect(component.activeStudents.length).toBe(0);
    expect(component.chartOptionsLine.series[0]['data']).toEqual([]);
    expect(component.chartOptionsLineFlag).toBe(true);
  });
  it('should handle error in getCourseVisits', () => {
    performanceService.getCourseVisits.and.returnValue(
      of({
        status: 500,
        data: null,
      })
    );
    component.getCourseVisits();
    expect(component.courseVisits).toEqual([]);
    expect(component.showChartOptionsBar).toBe(false);
  });

  it('should handle error in getRatingsAndReviews', () => {
    performanceService.getRatingsAndReviews.and.returnValue(
      of({
        status: 500,
        data: null,
      })
    );
    component.getRatingsAndReviews();
    expect(component.ratingsAndReviews).toBeNull();
  });

  it('should handle error in getActiveStudents', () => {
    performanceService.getActiveStudents.and.returnValue(
      of({
        status: 500,
        data: null,
      })
    );
    component.getActiveStudents();
    expect(component.activeStudents).toEqual([]);
    expect(component.chartOptionsLineFlag).toBe(false);
  });

  it('should handle error in getCourseNames', () => {
    component.courseNames = []
    performanceService.getCourseNames.and.returnValue(
      of({
        status: 500,
        data: null,
      })
    );
    component.getCourseNames();
    expect(component.courseNames.length).toBe(0); 
  });
  it('should handle edge case where selectedCourseId is undefined', () => {
    component.selectedCourseId = undefined;
    component.selectCourse();
    expect(performanceService.getCourseVisits).toHaveBeenCalledWith(undefined);
    expect(performanceService.getRatingsAndReviews).toHaveBeenCalled();
    expect(performanceService.getActiveStudents).toHaveBeenCalled();
  });
  it('should correctly set ratingsAndReviews on first fetch', () => {
    component.ratingsAndReviews = null;
    component.getRatingsAndReviews();
    expect(component.ratingsAndReviews).not.toBeNull();
  });

  it('should append feedback comments on subsequent fetch', () => {
    const initialFeedback = {
      feedbackComments: [
        { createdAt: '2023-01-01T00:00:00Z', comment: 'Initial Comment' },
      ],
    };
    component.ratingsAndReviews = { feedback: initialFeedback };
    performanceService.getRatingsAndReviews.and.returnValue(
      of({
        status: 200,
        data: {
          feedback: {
            feedbackComments: [
              { createdAt: '2023-02-01T00:00:00Z', comment: 'New Comment' },
            ],
          },
        },
      })
    );
    component.getRatingsAndReviews();
    expect(component.ratingsAndReviews.feedback.feedbackComments.length).toBe(
      2
    );
  });
  it('should correctly update pageNo and fetch new reviews when showMoreReviews is called', () => {
    const initialPageNo = component.payLoad.pageNo;
    component.showMoreReviews();
    expect(component.payLoad.pageNo).toBe(initialPageNo + 1);
    expect(performanceService.getRatingsAndReviews).toHaveBeenCalled();
  });
});
