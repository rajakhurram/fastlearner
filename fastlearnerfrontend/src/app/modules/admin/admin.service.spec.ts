import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from 'src/environments/environment.development';
import { AdminService } from './admin.service';

describe('AdminService', () => {
  let service: AdminService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(AdminService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch user stats', () => {
    service.getUserStats().subscribe();
    const req = httpMock.expectOne(
      `${environment.baseUrl}super-admin/users/stats`,
    );
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should fetch users list', () => {
    const queryParams = {
      search: 'john',
      planType: 'STANDARD',
      subscriptionStatus: 'ACTIVE',
      accountStatus: 'ACTIVE',
      dateFrom: '',
      dateTo: '',
      page: 0,
      size: 10,
    };

    service.getUsersList(queryParams).subscribe();
    const req = httpMock.expectOne(
      `${environment.baseUrl}super-admin/users?search=john&planType=STANDARD&subscriptionStatus=ACTIVE&accountStatus=ACTIVE&dateFrom=&dateTo=&page=0&size=10`,
    );
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should fetch coupons list', () => {
    service.getCouponsList({ page: 0, size: 10 }).subscribe();
    const req = httpMock.expectOne(
      `${environment.baseUrl}coupon/?page=0&size=10&search=&isActive=undefined`,
    );
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should create coupon', () => {
    const payload = { coupon: 'SAVE10' };

    service.createCoupon(payload).subscribe();
    const req = httpMock.expectOne(`${environment.baseUrl}coupon/`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({});
  });

  it('should toggle coupon status', () => {
    service.toggleCouponStatus(12).subscribe();
    const req = httpMock.expectOne(
      `${environment.baseUrl}coupon/12/toggle-status`,
    );
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });

  it('should publish course', () => {
    service.coursePublish(7).subscribe();
    const req = httpMock.expectOne(
      `${environment.baseUrl}super-admin/courses/7/publish`,
    );
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });

  it('should invite admin', () => {
    const payload = { email: 'admin@example.com', link: 'invite-link' };

    service.inviteAdmin(payload).subscribe();
    const req = httpMock.expectOne(
      `${environment.baseUrl}super-admin/users/invite?email=admin@example.com&link=invite-link`,
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });
});
