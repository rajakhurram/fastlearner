import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from 'src/environments/environment.development';
import { CourseStatus } from '../enums/course-status';
import { DashboardService } from './dashboard.service';

describe('DashboardService', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get dashboard stats', () => {
    service.getDashboardStats('monthly').subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}dashboard/stats?filterBy=monthly`,
    );
    expect(req.request.method).toBe('GET');
    req.flush({ status: 200, data: {} });
  });

  it('should get instructor courses with pagination params', () => {
    const body = {
      pageNo: 1,
      pageSize: 10,
      searchInput: 'angular',
      sort: '1',
    };

    service.getMyCourses(body).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}course/course-by-teacher?pageNo=1&pageSize=10&searchInput=angular&sort=1`,
    );
    expect(req.request.method).toBe('GET');
    req.flush({ status: 200, data: { courses: [], totalElements: 0 } });
  });

  it('should change course status', () => {
    service.changeCourseStatus(42, CourseStatus.PUBLISHED).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}course/course-status?courseId=42&courseStatus=${CourseStatus.PUBLISHED}`,
    );
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBeNull();
    req.flush({ status: 200 });
  });
});
