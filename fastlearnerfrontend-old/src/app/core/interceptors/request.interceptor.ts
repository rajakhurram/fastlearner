import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
  HttpHeaders,
} from '@angular/common/http';
import {
  BehaviorSubject,
  catchError,
  filter,
  Observable,
  switchMap,
  take,
  tap,
  throwError,
} from 'rxjs';
import { CacheService } from '../services/cache.service';
import { AuthService } from '../services/auth.service';
import { MessageService } from '../services/message.service';
import { NavigationEnd, Router } from '@angular/router';
import { environment } from 'src/environments/environment.development';
import { CourseService } from '../services/course.service';

@Injectable()
export class RequestInterceptor implements HttpInterceptor {
  environment = environment.loginUrl;
  basePath?: string;

  constructor(
    private _cacheService: CacheService,
    private _authService: AuthService,
    private _messageService: MessageService,
    private _courseService: CourseService,
    private _router: Router
  ) {
    this.basePath = environment.basePath;
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<any> {
    // Clone the request to add the authorization header
    let authRequest = this.addAuthorizationHeader(request);

    // Pass the modified request to the next interceptor or to the HTTP handler
    return next.handle(authRequest).pipe(
      catchError((error) => {
        // Check if the error is due to an expired token
        if (error.status === 401 && !request.url.includes('/refreshtoken')) {
          // Refresh the token and retry the original request
          return this._authService.refreshToken().pipe(
            switchMap(() => {
              const newAuthRequest = this.addAuthorizationHeader(request);
              return next.handle(newAuthRequest);
            }),
            catchError((refreshError) => {
              // Handle any errors during token refresh
              if (refreshError?.status == 401) {
                const redirectUrl = this._cacheService.getDataFromCache('redirectUrl');
                this._cacheService.clearCache();
                this._cacheService.saveInCache('redirectUrl', redirectUrl);
                this._router.navigate(['auth/sign-in']);
                this._authService.changeNavState(false);
                this._authService.getCategories(true);
              }else if(refreshError?.status == 403){
                this._cacheService.clearCache();
                this._router.navigate(['auth/sign-in']);
                this._authService.changeNavState(false);
                this._authService.getCategories(true);
              }
              return throwError(refreshError);
            })
          );
        } else if (error.status === 403) {
          const redirectUrl = this._cacheService.getDataFromCache('redirectUrl');
          this._cacheService.clearCache();
          this._router.navigate(['auth/sign-in']);
          this._authService.changeNavState(false);
          this._authService.getCategories(true);
        } else if (error?.status === 301) {
          let redirectUrl = this._cacheService.getDataFromCache('redirectUrl');
          if (
            redirectUrl.split('/')[1] === 'student' &&
            (redirectUrl.split('/')[2] === 'course-details' ||
              redirectUrl.split('/')[2] === 'course-content')
          ) {
            const affiliateUUID = this._cacheService.getDataFromCache('affiliate');
            let finalUrl = this.basePath;  // this need to be changed
            finalUrl +=
              redirectUrl.split('/')[1] +
              '/' +
              redirectUrl.split('/')[2] +
              '/' +
              error?.error?.url;

              if(affiliateUUID && redirectUrl.split('/')[2] === 'course-details'){
                finalUrl += '?affiliate='+affiliateUUID;
                this._cacheService.removeFromCache('affiliate');
              }
              window.location.href = finalUrl;
          }
        }
        // Pass the error through if it's not related to token expiration
        return throwError(error);
      })
    );
  }

  private addAuthorizationHeader(request: HttpRequest<any>): HttpRequest<any> {
    const accessToken = this._authService.getAccessToken();
    if (accessToken) {
      return request.clone({
        setHeaders: {
          Authorization: `Bearer ${accessToken}`,
          'Cache-Control': 'no-store, must-revalidate',
          Pragma: 'no-cache',
          Expires: '0',
        },
      });
    }
    return request;
  }

  private addAuthorizationHeaderWithCSRF(
    request: HttpRequest<any>
  ): HttpRequest<any> {
    const accessToken = this._authService.getAccessToken();
    // const crsfToken = this.getCSRFToken()
    if (accessToken) {
      return request.clone({
        setHeaders: {
          Authorization: `Bearer ${accessToken}`,
          'X-CSRF-TOKEN': `db2ec44a-27a3-4788-aaa5-48c92fa1b6a4`,
          'Cache-Control': 'no-store, must-revalidate',
          Pragma: 'no-cache',
          Expires: '0',
        },
      });
    }
    return request;
  }
}
