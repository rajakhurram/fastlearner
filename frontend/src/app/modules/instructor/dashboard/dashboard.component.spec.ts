import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { DashboardService } from 'src/app/core/services/dashboard.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { RouterTestingModule } from '@angular/router/testing';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let dashboardService: DashboardService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DashboardComponent],
      imports: [HttpClientTestingModule, RouterTestingModule,AntDesignModule , BrowserAnimationsModule],
      providers: [DashboardService, HttpConstants],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    dashboardService = TestBed.inject(DashboardService);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call getDashboardStatistics and getCourseListOfInstructor on init', () => {
    spyOn(component, 'getDashboardStatistics');
    spyOn(component, 'getCourseListOfInstructor');
    component.ngOnInit();
    expect(component.getDashboardStatistics).toHaveBeenCalled();
    expect(component.getCourseListOfInstructor).toHaveBeenCalled();
  });
  it('should set dashboard property on successful getDashboardStatistics call', () => {
    const mockDashboardData = {
      completionRate: 2,
      totalParticipants: 2,
      revenue: 2,
      totalStudents: {
        totalValue: 1,
        values: {
          monthDate: 2,
          value: 2,
        },
      },
      totalProfileVisits: {
        totalValue: 1,
        values: {
          monthDate: 2,
          value: 2,
        },
      },
    };
    spyOn(dashboardService, 'getDashboardStats').and.returnValue(
      of({
        status: 200,
        data: mockDashboardData,
      })
    );
    component.getDashboardStatistics();
    expect(component.dashboard).toEqual(mockDashboardData);
  });

  it('should log error on getDashboardStatistics failure', () => {
    const consoleSpy = spyOn(console, 'log');
    spyOn(dashboardService, 'getDashboardStats').and.returnValue(
      throwError(() => new Error('Error'))
    );
    component.getDashboardStatistics();
    expect(consoleSpy).toHaveBeenCalledWith(new Error('Error'));
  });
  it('should set myCourseList and totalElements on successful getCourseListOfInstructor call', () => {
    const mockCourses = [
      /* array of mock InstructorCourse objects */
    ];
    const mockResponse = {
      status: 200,
      data: {
        courses: mockCourses,
        totalElements: 5,
      },
    };
    spyOn(dashboardService, 'getMyCourses').and.returnValue(of(mockResponse));
    component.getCourseListOfInstructor();
    expect(component.myCourseList).toEqual(mockCourses);
    expect(component.totalElements).toBe(5);
  });

  it('should handle error on getCourseListOfInstructor call', () => {
    spyOn(dashboardService, 'getMyCourses').and.returnValue(
      throwError(() => new Error('Error'))
    );
    component.getCourseListOfInstructor();
    expect(component.myCourseList).toEqual([]);
  });
  it('should navigate to the course page with the correct ID', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.routeToCoursePage(1);
    expect(navigateSpy).toHaveBeenCalledWith(['instructor/course'], {
      queryParams: { id: 1 },
    });
  });
  it('should navigate to the course creation page', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.routeToCreateCourse();
    expect(navigateSpy).toHaveBeenCalledWith(['instructor/course']);
  });
  it('should re-fetch the course list when orderMyCourses is called', () => {
    spyOn(component, 'getCourseListOfInstructor');
    component.orderMyCourses();
    expect(component.getCourseListOfInstructor).toHaveBeenCalled();
  });
  it('should update pageNo and fetch the course list when onPageChange is called', () => {
    spyOn(component, 'getCourseListOfInstructor');
    component.onPageChange(2);
    expect(component.payLoad.pageNo).toBe(1); // pageNo is 0-indexed
    expect(component.getCourseListOfInstructor).toHaveBeenCalled();
  });
  it('should handle no data in getDashboardStatistics', () => {
    spyOn(dashboardService, 'getDashboardStats').and.returnValue(
      of({
        status: 200,
        data: null,
      })
    );
    component.getDashboardStatistics();
    expect(component.dashboard).toBeNull();
  });

  it('should handle no data in getCourseListOfInstructor', () => {
    spyOn(dashboardService, 'getMyCourses').and.returnValue(
      of({
        status: 200,
        data: { courses: [], totalElements: 0 },
      })
    );
    component.getCourseListOfInstructor();
    expect(component.myCourseList.length).toBe(0);
    expect(component.totalElements).toBe(0);
  });
});
