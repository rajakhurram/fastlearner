import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { AffiliateService } from './affiliate.service';
import { environment } from 'src/environments/environment.development';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('AffiliateService', () => {
  let service: AffiliateService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AffiliateService],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    });
    service = TestBed.inject(AffiliateService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch affiliates with correct parameters', () => {
    const body = { pageNo: 1, pageSize: 10, search: 'test' };
    service.getAffiliates(body).subscribe();
    const req = httpMock.expectOne(
      `${environment.baseUrl}affiliate/fetch?pageNo=1&pageSize=10&search=test`
    );
    expect(req.request.method).toBe('GET');
  });

  it('should fetch an affiliate detail by ID', () => {
    const body = { instructorAffliateId: '123' };
    service.getAffiliate(body).subscribe();
    const req = httpMock.expectOne(
      `${environment.baseUrl}affiliate/detail?instructorAffiliateId=123`
    );
    expect(req.request.method).toBe('GET');
  });

  it('should fetch courses by affiliate', () => {
    const body = { pageNo: 1, pageSize: 10, instructorAffiliateId: '456' };
    service.getCourseByAffiliate(body).subscribe();
    const req = httpMock.expectOne(
      `${environment.baseUrl}affiliate-course/?pageNo=1&pageSize=10&affiliateId=456`
    );
    expect(req.request.method).toBe('GET');
  });

  it('should fetch courses with rewards', () => {
    const body = { affiliateId: '789' };
    service.getCoursesWithReward(body).subscribe();
    const req = httpMock.expectOne(
      `${environment.baseUrl}instructor-affiliate/premium-courses-with-reward?affiliateId=789`
    );
    expect(req.request.method).toBe('GET');
  });

  it('should fetch premium courses by instructor', () => {
    const body = { pageNo: 1, pageSize: 10, search: 'test' };
    service.getPremiumCoursesByInstructor(body).subscribe();
    const req = httpMock.expectOne(
      `${environment.baseUrl}course/premium-courses?pageNo=1&pageSize=10&search=test`
    );
    expect(req.request.method).toBe('GET');
  });

  it('should create an affiliate', () => {
    const body = { name: 'New Affiliate' };
    service.createAffiliate(body).subscribe();
    const req = httpMock.expectOne(`${environment.baseUrl}affiliate/create`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
  });

  it('should assign a course to an affiliate', () => {
    const body = { courseId: '123', affiliateId: '456' };
    service.assignAffiliateCourse(body).subscribe();
    const req = httpMock.expectOne(`${environment.baseUrl}affiliate-course/`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
  });

  it('should resend stripe link', () => {
    const email = 'test@example.com';
    service.resendLink(email).subscribe();
    const req = httpMock.expectOne(
      `${environment.baseUrl}affiliate/stripe-resend-link?email=test@example.com`
    );
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
  });

  it('should edit an affiliate', () => {
    const body = { id: '123', name: 'Updated Affiliate' };
    service.editAffiliate(body).subscribe();
    const req = httpMock.expectOne(`${environment.baseUrl}affiliate/update`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(body);
  });

  it('should delete an affiliate', () => {
    const body = { instructorAffiliateId: '123' };
    service.deleteAffiliate(body).subscribe();
    const req = httpMock.expectOne(
      `${environment.baseUrl}affiliate/delete?instructorAffiliateId=123`
    );
    expect(req.request.method).toBe('DELETE');
  });

  it('should delete an affiliate course', () => {
    const body = { affiliateId: '456', id: '789' };
    service.deleteAffiliateCourse(body).subscribe();
    const req = httpMock.expectOne(
      `${environment.baseUrl}affiliate-course/?affiliateId=456&affiliateCourseId=789`
    );
    expect(req.request.method).toBe('DELETE');
  });
});
