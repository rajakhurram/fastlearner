import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { CacheService } from './cache.service';
import { environment } from 'src/environments/environment.development';
import { SocialAuthService } from '@abacritt/angularx-social-login';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let cacheServiceSpy: jasmine.SpyObj<CacheService>;
  let socialAuthServiceSpy: jasmine.SpyObj<SocialAuthService>;

  beforeEach(() => {
    const cacheSpy = jasmine.createSpyObj('CacheService', [
      'getDataFromCache',
      'saveInCache',
    ]);
    const socialAuthSpy = jasmine.createSpyObj('SocialAuthService', [
      'signOut',
    ]);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        { provide: CacheService, useValue: cacheSpy },
        { provide: SocialAuthService, useValue: socialAuthSpy },
      ],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    cacheServiceSpy = TestBed.inject(
      CacheService
    ) as jasmine.SpyObj<CacheService>;
    socialAuthServiceSpy = TestBed.inject(
      SocialAuthService
    ) as jasmine.SpyObj<SocialAuthService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return access token', () => {
    const token = 'test-token';
    cacheServiceSpy.getDataFromCache.and.returnValue(token);
    expect(service.getAccessToken()).toBe(token);
  });

  it('should check if user is logged in', () => {
    cacheServiceSpy.getDataFromCache.and.returnValue('test-token');
    expect(service.isLoggedIn()).toBeTrue();

    cacheServiceSpy.getDataFromCache.and.returnValue(null);
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('should return logged in user role', () => {
    const role = 'admin';
    cacheServiceSpy.getDataFromCache.and.returnValue(role);
    expect(service.getLoggedInRole()).toBe(role);
  });

  it('should return logged in user name', () => {
    const userDetails = JSON.stringify({ name: 'John Doe' });
    cacheServiceSpy.getDataFromCache.and.returnValue(userDetails);
    expect(service.getLoggedInName()).toBe('John Doe');

    cacheServiceSpy.getDataFromCache.and.returnValue(null);
    expect(service.getLoggedInName()).toBe('');
  });

  it('should refresh token', () => {
    const refreshToken = 'refresh-token';
    const newToken = 'new-token';
    const response = { token: newToken, refresh_token: refreshToken };
    cacheServiceSpy.getDataFromCache.and.returnValue(refreshToken);

    service.refreshToken().subscribe((res) => {
      expect(res).toEqual(response);
    });

    const req = httpMock.expectOne(`${environment.loginUrl}/refreshtoken`);
    expect(req.request.method).toBe('GET');
    req.flush(response);

    expect(cacheServiceSpy.saveInCache).toHaveBeenCalledWith('token', newToken);
    expect(cacheServiceSpy.saveInCache).toHaveBeenCalledWith(
      'refresh_token',
      refreshToken
    );
  });

  it('should handle refresh token error', () => {
    const refreshToken = 'refresh-token';
    cacheServiceSpy.getDataFromCache.and.returnValue(refreshToken);

    service.refreshToken().subscribe({
      error: (error) => {
        expect(error.status).toBe(401);
      },
    });

    const req = httpMock.expectOne(`${environment.loginUrl}/refreshtoken`);
    expect(req.request.method).toBe('GET');
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(cacheServiceSpy.saveInCache).toHaveBeenCalledWith('token', null);
    expect(cacheServiceSpy.saveInCache).toHaveBeenCalledWith(
      'refresh_token',
      null
    );
  });

  it('should sign up with social account', () => {
    const body = { token: 'social-token', provider: 'google' };
    service.signUpWithSocialAccount(body).subscribe();

    const req = httpMock.expectOne(
      `${environment.loginUrl}/social-login?token=${body.token}&provider=${body.provider}`
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should sign up', () => {
    const body = { email: 'test@example.com', password: 'password' };
    service.signUp(body).subscribe();

    const req = httpMock.expectOne(
      `${environment.loginUrl}/authentication-otp`
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should verify OTP', () => {
    const email = 'test@example.com';
    const otp = '123456';
    service.verifyOTP(email, otp).subscribe();

    const req = httpMock.expectOne(
      `${environment.loginUrl}/verify-authentication-otp?email=${email}&otp=${otp}`
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should sign in', () => {
    const body = { email: 'test@example.com', password: 'password' };
    service.signIn(body).subscribe();

    const req = httpMock.expectOne(`${environment.loginUrl}/local-login`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should handle contact us when user is logged in', () => {
    cacheServiceSpy.getDataFromCache.and.returnValue('test-token');
    const body = { message: 'Test message' };
    service.contactUs(body).subscribe();

    const req = httpMock.expectOne(`${environment.baseUrl}contact-us/`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should handle contact us when user is not logged in', () => {
    cacheServiceSpy.getDataFromCache.and.returnValue(null);
    const body = { message: 'Test message' };
    service.contactUs(body).subscribe();

    const req = httpMock.expectOne(`${environment.baseUrl}contact-us/`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should forget password', () => {
    const email = 'test@example.com';
    service.forgetPassword(email).subscribe();

    const req = httpMock.expectOne(
      `${environment.loginUrl}/send-link?email=${email}`
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should reset password', () => {
    const body = { password: 'newpassword' };
    service.resetPassword(body).subscribe();

    const req = httpMock.expectOne(`${environment.loginUrl}/reset-password`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });
  it('should get subscription plans', () => {
    service.getSubscriptionPlans().subscribe();

    const req = httpMock.expectOne(`${environment.baseUrl}subscription/`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should create a subscription', () => {
    const body = { planId: 'plan_12345' };
    service.createSubscription(body).subscribe();

    const req = httpMock.expectOne(`${environment.baseUrl}subscription/`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('should complete a subscription', () => {
    const paypalSubscriptionId = 'sub_12345';
    service.completeSubscription(paypalSubscriptionId).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}subscription/complete-subscription?paypalSubscriptionId=${paypalSubscriptionId}`
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should cancel PayPal subscription', () => {
    const paypalSubscriptionId = 'sub_12345';
    service.cancelPaypalSubscription(paypalSubscriptionId).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}subscription/cancel-subscription?paypalSubscriptionId=${paypalSubscriptionId}`
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should verify user subscription', () => {
    service.verifyUserSubscription().subscribe();

    const req = httpMock.expectOne(`${environment.baseUrl}subscription/verify`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should get subscribed plan and details', () => {
    service.getSubscribedPlanAndDetails().subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}subscription/current-subscription`
    );
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should cancel subscription', () => {
    service.cancelSubscription().subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}subscription/cancel-subscription`
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should sign out and call logout API', () => {
    service.signOut().subscribe();
  
    const req = httpMock.expectOne(`${environment.loginUrl}/logout`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBe(''); // Ensure the request body is an empty string
    req.flush({});
  });
  it('should verify OTP for resetting password', () => {
    const email = 'test@example.com';
    const otp = '123456';
    const response = { success: true, message: 'OTP verified successfully' };
  
    service.resetOtpVerify(email, otp).subscribe(res => {
      expect(res).toEqual(response);
    });
  
    const req = httpMock.expectOne(
      `${environment.loginUrl}/verify-otp?email=${email}&otp=${otp}`
    );
    expect(req.request.method).toBe('POST');
    req.flush(response); // Mock the response
  });
  
  
});
