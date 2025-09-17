import {
  HttpBackend,
  HttpClient,
  HttpHeaders,
  HttpParams,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Subject, tap, throwError } from 'rxjs';
import { Observable } from 'rxjs/internal/Observable';
import { environment } from 'src/environments/environment.development';
import { CacheService } from './cache.service';
import { AppConstants } from '../constants/app.constants';
import { SocialAuthService } from '@abacritt/angularx-social-login';
import { CookiesService } from './cookie.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private _httpBackend: HttpClient;
  private refreshTokenTimer: any;

  constructor(
    private _http: HttpClient,
    private _cacheService: CacheService,
    handler: HttpBackend,
    private _socialAuthService: SocialAuthService,
    private _cookiesService: CookiesService
  ) {
    this._httpBackend = new HttpClient(handler);
    this.startTokenTimer();
  }

  private changeNavbarSate = new Subject<any>();
  private getCategoriesAndCourse = new Subject<any>();
  $changeNavbarSate = this.changeNavbarSate?.asObservable();
  $getCategoriesAndCourse = this.getCategoriesAndCourse.asObservable();

  changeNavState(state: boolean) {
    this.changeNavbarSate.next(state);
  }
  getCategories(state: boolean) {
    this.getCategoriesAndCourse.next(state);
  }

  getAccessToken() {
    return this._cacheService.getDataFromCache('token');
  }

  isLoggedIn() {
    return this._cacheService.getDataFromCache('token') ? true : false;
  }

  getLoggedInRole() {
    return this._cacheService.getDataFromCache('role');
  }

  getLoggedInName() {
    return this._cacheService.getDataFromCache('loggedInUserDetails')
      ? JSON.parse(this._cacheService.getDataFromCache('loggedInUserDetails'))[
          'name'
        ]
      : '';
  }
  getUserProfile() {
    return this._cacheService.getDataFromCache('userProfile')
      ? JSON.parse(this._cacheService.getDataFromCache('userProfile'))
      : '';
  }

  getLoggedInEmail() {
    return this._cacheService.getDataFromCache('loggedInUserDetails')
      ? JSON.parse(this._cacheService.getDataFromCache('loggedInUserDetails'))[
          'email'
        ]
      : '';
  }
  getLoggedInPicture() {
    return this._cacheService.getDataFromCache('userProfile')
      ? JSON.parse(this._cacheService.getDataFromCache('userProfile'))[
          'profilePicture'
        ]
      : '';
  }

  getUserProfileUrl(){
    return this._cacheService.getDataFromCache('userProfile')
    ? JSON.parse(this._cacheService.getDataFromCache('userProfile'))[
      'userProfileUrl'
    ]
    : '';
  }

  isSubscribed() {
    return this._cacheService.getDataFromCache('loggedInUserDetails')
      ? JSON.parse(this._cacheService.getDataFromCache('loggedInUserDetails'))[
          'subscribed'
        ]
      : '';
  }

  startTokenTimer() {
    const expiryTimeInSeconds = parseInt(localStorage.getItem('expiresIn'), 10);

    if (expiryTimeInSeconds) {
      const currentTimeInSeconds = Math.floor(Date.now() / 1000);

      const timeLeftInSeconds = expiryTimeInSeconds - currentTimeInSeconds;

      if (timeLeftInSeconds > 0) {
        if (timeLeftInSeconds <= 300) {
          this.refreshToken().subscribe(
            (response) => {
              console.log('Token refreshed successfully');
            },
            (error) => {
              console.error('Error refreshing token:', error);
            }
          );
        } else {
          const refreshTimeInMs = (timeLeftInSeconds - 60) * 1000;

          this.refreshTokenTimer = setTimeout(() => {
            this.refreshToken().subscribe(
              (response) => {
                console.log('Token refreshed successfully');
              },
              (error) => {
                console.error('Error refreshing token:', error);
              }
            );
          }, refreshTimeInMs);
        }
      }
    }
  }

  clearTokenTimer() {
    if (this.refreshTokenTimer) {
      clearTimeout(this.refreshTokenTimer);
      this.refreshTokenTimer = null;
    }
  }

  public refreshToken(): Observable<any> {
    const refresh_Token = this._cacheService.getDataFromCache('refresh_token');
    const headers = new HttpHeaders().set(
      'Authorization',
      `Bearer ${refresh_Token}`
    );

    // console.log('calling the refresh token api');
    return this._httpBackend
      .get(`${environment.loginUrl}/refreshtoken`, { headers })
      .pipe(
        tap(() => {
          // console.log('API call is being made');
        }),
        tap((response: any) => {
          const expiryTimeInSeconds = Math.floor(Date.now() / 1000) + 9000;
          this._cacheService.saveInCache('token', response?.token);
          this._cacheService.saveInCache(
            'refresh_token',
            response?.refresh_token
          );
          this._cookiesService.setToken(response?.token);
          this._cookiesService.setRefreshToken(response?.refresh_token);
          this._cacheService.saveInCache(
            'expiresIn',
            expiryTimeInSeconds.toString()
          );
          this.clearTokenTimer();
          this.startTokenTimer();
        }),
        catchError((error) => {
          // console.error('Error in API call:', error);
          this._cacheService.removeFromCache('token');
          this._cacheService.removeFromCache('refresh_token');
          this.clearTokenTimer();
          return throwError(error);
        })
      );
  }

  public signUpWithSocialAccount(body: any): Observable<any> {
    return this._httpBackend.post(
      `${environment.loginUrl}/social-login?token=${body?.token}&provider=${body?.provider}`,
      null
    );
  }

  public signUp(body: any): Observable<any> {
    return this._httpBackend.post(
      `${environment.loginUrl}/authentication-otp`,
      body
    );
  }

  public resendOtp(email: string): Observable<any> {
    // Add email as a query parameter
    const params = new HttpParams().set('email', email);

    // Send POST request with empty body and query parameter
    return this._http.post<any>(
      `${environment.loginUrl}/send-link`,
      {},
      { params }
    );
  }

  public verifyOTP(email?: string, otp?: any): Observable<any> {
    return this._httpBackend.post(
      `${environment.loginUrl}/verify-authentication-otp?email=${email}&otp=${otp}`,
      null
    );
  }

  public resetOtpVerify(email?: string, otp?: any): Observable<any> {
    return this._httpBackend.post(
      `${environment.loginUrl}/verify-otp?email=${email}&otp=${otp}`,
      null
    );
  }

  public verifyOtpResetPassword(email?: string, otp?: any): Observable<any> {
    return this._httpBackend.post(
      `${environment.loginUrl}/verify-otp?email=${email}&otp=${otp}`,
      null
    );
  }

  public signIn(body: any): Observable<any> {
    return this._httpBackend.post(`${environment.loginUrl}/local-login`, body);
  }
  public contactUs(body: any): Observable<any> {
    if (this.isLoggedIn()) {
      return this._http.post(`${environment.baseUrl}contact-us/`, body);
    } else {
      return this._httpBackend.post(`${environment.baseUrl}contact-us/`, body);
    }
  }

  public forgetPassword(email: string): Observable<any> {
    return this._httpBackend.post(
      `${environment.loginUrl}/send-link?email=${email}`,
      null
    );
  }

  public resetPassword(body: any): Observable<any> {
    return this._httpBackend.post(
      `${environment.loginUrl}/reset-password`,
      body
    );
  }

  public saveRole(role: any): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}user/add-role?role=${role}`,
      null
    );
  }

  public getSubscriptionPlans(): Observable<any> {
    return this._http.get(`${environment.baseUrl}subscription/`);
  }

  public createSubscription(body: any): Observable<any> {
    return this._http.post(`${environment.baseUrl}subscription/`, body);
  }

  public validatePromoCode(body: any): Observable<any> {
    return this._http.post(`${environment.baseUrl}payload/` , body)
  }

  public completeSubscription(paypalSubscriptionId: any): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}subscription/complete-subscription?paypalSubscriptionId=${paypalSubscriptionId}`,
      null
    );
  }

  public cancelPaypalSubscription(paypalSubscriptionId: any): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}subscription/cancel-subscription?paypalSubscriptionId=${paypalSubscriptionId}`,
      null
    );
  }

  public verifyUserSubscription(): Observable<any> {
    return this._http.get(`${environment.baseUrl}subscription/verify`);
  }

  public getSubscribedPlanAndDetails(): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}subscription/current-subscription`
    );
  }

  public cancelSubscription(): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}subscription/cancel-subscription`,
      null
    );
  }
  public signOut(uniqueId?: any): Observable<any> {
    const url = uniqueId
      ? `${environment.loginUrl}/logout?uniqueId=${uniqueId}`
      : `${environment.loginUrl}/logout`;

    return this._http.post(url, '');
  }

  public newUserSubscription(): Observable<any>{
    return this._http.post(
      `${environment.baseUrl}authorizenet/free-subscription-for-signup?subscriptionId=1`,
      null
    );            

  }
}
