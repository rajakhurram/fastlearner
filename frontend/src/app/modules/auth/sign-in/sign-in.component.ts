import {
  FacebookLoginProvider,
  GoogleLoginProvider,
  SocialAuthService,
} from '@abacritt/angularx-social-login';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DataHolderConstants } from 'src/app/core/constants/dataHolder.constants';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { Role } from 'src/app/core/enums/Role';
import { Providers } from 'src/app/core/enums/providers';
import { User } from 'src/app/core/models/user.model';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { MessageService } from 'src/app/core/services/message.service';
import { UserService } from 'src/app/core/services/user.service';
import { environment } from 'src/environments/environment.development';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NotificationService } from 'src/app/core/services/notification.service';
import { SubscriptionService } from 'src/app/core/services/subscription.service';
import { CookiesService } from 'src/app/core/services/cookie.service';

@Component({
  selector: 'app-sign-in',
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.scss'],
})
export class SignInComponent implements OnInit {
  _httpConstants: HttpConstants = new HttpConstants();
  _dataHolderConstants: DataHolderConstants = new DataHolderConstants();
  eyeType = 'eye-invisible';
  passwordTextType: boolean = false;
  callInProgress: boolean = false;
  linkedInToken: string = '';
  currentLoggedInUserDetails: any;
  user: User = new User();
  emailValid?: any = false;
  notificationCount?: any = 0;

  payLoad = {
    token: '',
    provider: '',
  };
  saveRole?: any;

  validateForm: FormGroup<{
    email: FormControl<string>;
    password: FormControl<string>;
  }>;

  private eventSource: EventSource | undefined;
  timestamp?: any;
  notifications: Array<any> = [];
  isSubscribed: boolean;
  excludedUrls?: any = ['/auth/forget-password'];

  constructor(
    private _fb: FormBuilder,
    private _router: Router,
    private _cacheService: CacheService,
    private _activatedRoute: ActivatedRoute,
    private _authService: AuthService,
    private _messageService: MessageService,
    private _socialAuthService: SocialAuthService,
    private _userService: UserService,
    private _communicationService: CommunicationService,
    private _notificationService: NotificationService,
    private cdr: ChangeDetectorRef,
    private _subscriptionService: SubscriptionService,
    private _cookiesService: CookiesService
  ) {
    this.validateForm = this._fb.group({
      email: ['', [Validators.required]],
      password: ['', [Validators.required]],
    });
  }

  ngAfterViewInit() {
    this.cdr.detectChanges();
  }

  ngOnInit(): void {
    this.userIsSubscribed();
    // this.getAuthenticUserState();
    // this.getLinkedInToken();
    this._communicationService.removeEmitterData$.subscribe(() => {});
  }

  userIsSubscribed() {
    if (this._authService.isSubscribed()) {
      this.isSubscribed = true;
    } else {
      this.isSubscribed = false;
    }
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

  // getLinkedInToken() {
  //   if (this._activatedRoute.snapshot.queryParams['code']) {
  //     this.linkedInToken = this._activatedRoute.snapshot.queryParams['code'];
  //     this.payLoad.provider = Providers.LINKEDIN;
  //     this.payLoad.token = this.linkedInToken;
  //     this.signUp(this.payLoad);
  //   }
  // }

  onSignUpWithSocialAccount(body: any) {
    this._authService.signUpWithSocialAccount(body).subscribe({
      next: (response: any) => {
        if (response) {
          this.saveResponseInCache(response);
          this._subscriptionService.loadSubscriptionPermissions();
          this._authService.changeNavState(true);
          const redirectUrl =
            this._cacheService.getDataFromCache('redirectUrl');

          if (redirectUrl && response?.subscribed) {
            this._cacheService.removeFromCache('redirectUrl');
            this._router.navigateByUrl(redirectUrl);
          } else if (response?.role == null) {
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

  saveUserRole() {
    this.saveRole = 'STUDENT';

    this._cacheService.saveInCache('role', this.saveRole);

    this._authService.saveRole(this.saveRole).subscribe({
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

  submitForm(): void {
    if (this.validateForm.valid) {
      this.onSignIn(this.validateForm.value);
    } else {
      Object.values(this.validateForm.controls).forEach((control) => {
        if (control.invalid) {
          control.markAsDirty();
          control.updateValueAndValidity({ onlySelf: true });
        }
      });
    }
  }

  onSignIn(body: any) {
    this.callInProgress = true;
    this._authService.signIn(body).subscribe({
      next: (response: any) => {
        if (response) {
          this.saveResponseInCache(response);
          this._subscriptionService.loadSubscriptionPermissions();
          this._notificationService.connectSSE();
          this._authService.changeNavState(true);
          this.getUserCompleteProfile();

          // Retrieve the redirect URL from the cache
          const redirectUrl =
            this._cacheService.getDataFromCache('redirectUrl');

          // Delay for the progress indicator
          setTimeout(() => {
            this.callInProgress = false;
          }, 2000);

          if (response?.role == null) {
            // If the user has no role, save the user role
            this.saveUserRole();
          } else {
            const splittedUrl = redirectUrl ? redirectUrl.split('?')[0] : '';

            // Redirect based on the retrieved URL if it's not the reset password page
            if (redirectUrl && splittedUrl !== '/auth/reset-password') {
              this._cacheService.removeFromCache('redirectUrl'); // Clear the redirect URL from cache
              this._router.navigateByUrl(redirectUrl); // Navigate to the intended URL
            } else {
              // Handle navigation based on user role
              if (response?.role === Role.Student) {
                if (response?.subscribed) {
                  this._router.navigate(['student']);
                } else {
                  this._router.navigate(['subscription-plan']);
                }
              } else if (response?.role === Role.Instructor) {
                this._router.navigate(['instructor']);
              }
            }
          }
        }
      },
      error: (error: any) => {
        setTimeout(() => {
          this.callInProgress = false; // Stop the progress indicator
        }, 2000);

        this._messageService.error(error?.error?.message);


        // if (errorMessage === 'User does not Exist.') {
        //   this._messageService.error('User does not exist. Please sign up.');
        // } else if (errorMessage === 'Email or password is incorrect.') {
        //   this._messageService.error('Email or password is incorrect.');
        // } else if (
        //   error?.status === this._httpConstants.REQUEST_STATUS.UNAUTHORIZED_401.CODE
        // ) {
        //   this._messageService.error(error?.error?.msg);
        // } else {
        //   this._messageService.error(error?.error?.error_description || errorMessage);
        // }
      },
    });
  }

  getUserCompleteProfile() {
    this._userService.getUserProfile().subscribe({
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

  signInWithGoogle() {
    this._socialAuthService.signIn(GoogleLoginProvider.PROVIDER_ID);
  }

  togglePassword() {
    if (this.passwordTextType == true) {
      this.passwordTextType = false;
      this.eyeType = 'eye-invisible';
    } else {
      this.passwordTextType = true;
      this.eyeType = 'eye';
    }
  }

  routeToSignUp() {
    this._router.navigate(['auth/sign-up']);
  }

  routeToForgetPassword() {
    this._router.navigate(['auth/forget-password']);
  }

  generateTimeStamp() {
    return new Date().getTime();
  }

  validateEmail(event?: any) {
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    const valid = emailPattern.test(event.target.value);
    valid == false ? (this.emailValid = false) : (this.emailValid = true);
  }
}
