import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PerformanceService } from './performance.service';
import { environment } from 'src/environments/environment.development';

describe('PerformanceService', () => {
  let service: PerformanceService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PerformanceService]
    });

    service = TestBed.inject(PerformanceService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get course visits', () => {
    const courseId = 123;
    const mockResponse = { visits: 50 };

    service.getCourseVisits(courseId).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.baseUrl}course-visitor/?courseId=${courseId}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should get ratings and reviews with courseId', () => {
    const payLoad = { courseId: 123, pageNo: 1, pageSize: 10 };
    const mockResponse = { reviews: [] };

    service.getRatingsAndReviews(payLoad).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.baseUrl}course-review/instructor?pageNo=${payLoad.pageNo}&pageSize=${payLoad.pageSize}&courseId=${payLoad.courseId}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should get ratings and reviews without courseId', () => {
    const payLoad = { pageNo: 1, pageSize: 10 };
    const mockResponse = { reviews: [] };

    service.getRatingsAndReviews(payLoad).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.baseUrl}course-review/instructor?pageNo=${payLoad.pageNo}&pageSize=${payLoad.pageSize}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should get course names', () => {
    const mockResponse = { courses: [] };

    service.getCourseNames().subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.baseUrl}course/dropdown-for-performance`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should get active students', () => {
    const courseId = 123;
    const mockResponse = { students: [] };

    service.getActiveStudents(courseId).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.baseUrl}user-course-progress/active-students?courseId=${courseId}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should get active students without courseId', () => {
    const mockResponse = { students: [] };

    service.getActiveStudents().subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.baseUrl}user-course-progress/active-students`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });
});
