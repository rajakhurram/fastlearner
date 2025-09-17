import { SocialAuthService } from '@abacritt/angularx-social-login';
import { Injectable } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';
import { Role } from '../enums/Role';
import { Providers } from '../enums/providers';
import { AuthService } from './auth.service';
import { CacheService } from './cache.service';
import { MessageService } from './message.service';
import { DataHolderConstants } from '../constants/dataHolder.constants';
import { HttpConstants } from '../constants/http.constants';
import { CommunicationService } from './communication.service';
import { UserService } from './user.service';
import { User } from '../models/user.model';
import { NotificationService } from './notification.service';
import { CookiesService } from './cookie.service';

@Injectable({
  providedIn: 'root',
})
export class StateService {
  _httpConstants: HttpConstants = new HttpConstants();
  _dataHolderConstants: DataHolderConstants = new DataHolderConstants();
  currentLoggedInUserDetails: any;
  payLoad = {
    token: '',
    provider: '',
  };
  saveRole?: any;
  isSubscribed: boolean;
  user: User = new User();

  constructor(
    private _socialAuthService: SocialAuthService,
    private _authService: AuthService,
    private _cacheService: CacheService,
    private _router: Router,
    private _userService: UserService,
    private _notificationService: NotificationService,
    private _cookiesService: CookiesService
  ) {
    this.userIsSubscribed();
    this._router.events
      ?.pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => {
        this.getAuthenticUserState();
        const redirectUrl = this._cacheService.getDataFromCache('redirectUrl');

        this.saveCurrentRoute();
      });

    this._socialAuthService.authState?.subscribe((user) => {
      if (!user) {
        this.currentLoggedInUserDetails = null;
        return;
      }
    });
  }

  userIsSubscribed() {
    if (this._authService.isSubscribed()) {
      this.isSubscribed = true;
    } else {
      this.isSubscribed = false;
    }
  }

  getAuthenticUserState() {
    this._socialAuthService.authState.subscribe((user) => {
      if (user) {
        if (
          this.currentLoggedInUserDetails == undefined ||
          this.currentLoggedInUserDetails == null
        ) {
          this.currentLoggedInUserDetails = user;
          this.signUp(this.currentLoggedInUserDetails);
        }
      }
    });
  }

  signUp(user: any) {
    switch (user?.provider) {
      case Providers.GOOGLE:
        this.payLoad.provider = Providers.GOOGLE;
        this.payLoad.token = user?.idToken;
        this.onSignUpWithSocialAccount(this.payLoad);
        break;
      // case Providers.FACEBOOK:
      //   this.payLoad.provider = Providers.FACEBOOK;
      //   this.payLoad.token = user?.authToken;
      //   this.onSignUpWithSocialAccount(this.payLoad);
      //   break;
      // case Providers.LINKEDIN:
      //   this.onSignUpWithSocialAccount(this.payLoad);
      //   break;
    }
  }

  onSignUpWithSocialAccount(body: any) {
    this._authService.signUpWithSocialAccount(body)?.subscribe({
      next: (response: any) => {
        if (response) {
          this.saveResponseInCache(response);
          this.getUserCompleteProfile();
          this._authService.changeNavState(true);
          const redirectUrl =
            this._cacheService.getDataFromCache('redirectUrl');
          this._notificationService.connectSSE();
          if (redirectUrl && response?.subscribed) {
            this._cacheService.removeFromCache('redirectUrl');
            this._router.navigateByUrl(redirectUrl);
          } else if (response?.role == null) {
            // this._router.navigate(['auth/get-started']);
            this.saveUserRole();
          } else {
            if (response?.role == Role.Student && response?.subscribed) {
              this._router.navigate(['student']);
            } else if (
              response?.role == Role.Student &&
              !response?.subscribed
            ) {
              this._router.navigate(['subscription-plan']);
            } else if (response?.role == Role.Instructor) {
              this._router.navigate(['instructor']);
            }
          }
        }
      },
      error: (error: any) => {},
    });
  }

  getUserCompleteProfile() {
    this._userService?.getUserProfile()?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.user = response?.data;
          this._cacheService.saveInCache(
            'userProfile',
            JSON.stringify(response?.data)
          );
        }
      },
      error: (error: any) => {},
    });
  }

  saveUserRole() {
    this.saveRole = 'STUDENT';

    this._cacheService.saveInCache('role', this.saveRole);

    this._authService?.saveRole(this.saveRole)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          if (this.saveRole == Role.Student && this.isSubscribed) {
            this._router.navigate(['student']);
          } else if (this.saveRole == Role.Student && !this.isSubscribed) {
            this._router.navigate(['subscription-plan']);
          } else if (this.saveRole == Role.Instructor) {
            this._router.navigate(['instructor']);
          }
        }
      },
      error: (error: any) => {},
    });
  }

  saveResponseInCache(response: any) {
    this._cacheService.saveInCache('token', response?.token);
    this._cacheService.saveInCache('refresh_token', response?.refreshToken);
    this._cookiesService.setToken(response?.token);
    this._cookiesService.setRefreshToken(response?.refreshToken);
    const expiryTimeInSeconds =
      Math.floor(Date.now() / 1000) + response?.expiredInSec;
    this._cacheService.saveInCache('expiresIn', expiryTimeInSeconds);
    this._cacheService.saveInCache(
      'loggedInUserDetails',
      JSON.stringify(response)
    );
    this._cacheService.saveInCache('isLoggedIn', 'true');
    this._authService.startTokenTimer();
  }

  saveCurrentRoute() {
    const currentRoute = this._router.url;
    const subscriptionPathRegex =
      /^\/auth\/payment-method\?subscriptionId=\d+$/;

    if (
      currentRoute !== '/' &&
      currentRoute !== '/auth/sign-in' &&
      currentRoute !== '/auth/sign-up' &&
      currentRoute !== 'subscription-plan' &&
      currentRoute !== '/auth/forget-password' &&
      !subscriptionPathRegex.test(currentRoute)
    ) {
      this._cacheService.saveInCache('redirectUrl', currentRoute);
    }
  }
}
