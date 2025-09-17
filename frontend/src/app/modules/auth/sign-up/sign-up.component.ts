import {
  FacebookLoginProvider,
  GoogleLoginProvider,
  SocialAuthService,
} from '@abacritt/angularx-social-login';
import {
  ChangeDetectorRef,
  Component,
  OnInit,
  ViewContainerRef,
} from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { Role } from 'src/app/core/enums/Role';
import { Providers } from 'src/app/core/enums/providers';
import { RegisterUser } from 'src/app/core/models/register-user.model';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { environment } from 'src/environments/environment.development';
import { OtpModalComponent } from '../../dynamic-modals/otp-modal/otp-modal.component';
import { UserService } from 'src/app/core/services/user.service';
import { StateService } from 'src/app/core/services/state.service';
import { SubscriptionService } from 'src/app/core/services/subscription.service';
import { CookiesService } from 'src/app/core/services/cookie.service';

@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.component.html',
  styleUrls: ['./sign-up.component.scss'],
})
export class SignUpComponent implements OnInit {
  _httpConstants: HttpConstants = new HttpConstants();
  user: RegisterUser = new RegisterUser();
  eyeType = 'eye-invisible';
  eyeTypeConfirm = 'eye-invisible';
  passwordTextType: boolean = false;
  disableSubmitButton: boolean = false;
  confirmPasswordTextType: boolean = false;
  currentLoggedInUserDetails: any;
  linkedInToken: string = '';
  emailValid?: any = true;
  isSubscribed: boolean;
  saveRole?: any;
  excludedUrls?: any = ['/auth/forget-password'];

  payLoad = {
    token: '',
    provider: '',
  };

  validateForm: FormGroup<{
    name: FormControl<string>;
    email: FormControl<string>;
    password: FormControl<string>;
    confirmPassword: FormControl<string>;
    subscribeNewsletter: FormControl<boolean>;
    acceptTerms: FormControl<boolean>;
  }>;

  constructor(
    private _fb: NonNullableFormBuilder,
    private _router: Router,
    private _activatedRoute: ActivatedRoute,
    private _cacheService: CacheService,
    private _socialAuthService: SocialAuthService,
    private _authService: AuthService,
    private _messageService: MessageService,
    private _viewContainerRef: ViewContainerRef,
    private _modal: NzModalService,
    private _userService: UserService,
    private _authStateHandlerService: StateService,
    private cdr: ChangeDetectorRef,
    private _subscriptionService: SubscriptionService,
    private _cookiesService: CookiesService
  ) {
    this.validateForm = this._fb.group({
      name: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(/^[A-Za-z\s]*$/)]],
      email: ['', [Validators.required, Validators.email]],
      password: [
        '',
        [
          Validators.required,
          Validators.minLength(6),
          this.alphanumericValidator(),
        ],
      ],
      confirmPassword: ['', [Validators.required, this.confirmationValidator]],
      subscribeNewsletter: [false],
      acceptTerms: [false],
    });
  }

  ngAfterViewInit() {
    this.cdr.detectChanges();
  }

  ngOnInit(): void {
    this.userIsSubscribed();
    // this.getAuthenticUserState();
    // this.getLinkedInToken();
  }

  userIsSubscribed() {
    if (this._authService.isSubscribed()) {
      this.isSubscribed = true;
    } else {
      this.isSubscribed = false;
    }
  }

  updateConfirmValidator(): void {
    Promise.resolve().then(() =>
      this.validateForm.controls.confirmPassword.updateValueAndValidity()
    );
  }

  confirmationValidator: ValidatorFn = (
    control: AbstractControl
  ): { [s: string]: boolean } => {
    if (!control.value) {
      return { required: true };
    } else if (control.value !== this.validateForm.controls.password.value) {
      return { confirm: true, error: true };
    }
    return {};
  };

  alphanumericValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null; // Don't validate empty values
      }
      const hasAlphabet = /[a-zA-Z]/.test(control.value);
      const hasNumber = /\d/.test(control.value);
      const valid = hasAlphabet && hasNumber;
      return valid ? null : { alphanumeric: true };
    };
  }

  signUp(user: any) {
    switch (user?.provider) {
      case Providers.GOOGLE:
        this.payLoad.provider = Providers.GOOGLE;
        this.payLoad.token = user?.idToken;
        this.onSignUpWithSocialAccount(this.payLoad);
        break;
      //TODO: case Providers.FACEBOOK:
      //   this.payLoad.provider = Providers.FACEBOOK;
      //   this.payLoad.token = user?.authToken;
      //   this.onSignUpWithSocialAccount(this.payLoad);
      //   break;
      // case Providers.LINKEDIN:
      //   this.onSignUpWithSocialAccount(this.payLoad);
      //   break;
    }
  }

  //TODO: getLinkedInToken() {
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
          const splittedUrl = redirectUrl?.split('?')[0];
          if (
            redirectUrl &&
            response?.subscribed &&
            splittedUrl != '/auth/reset-password'
          ) {
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
      error: (error: any) => {
        console.log(error);
      },
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
      error: (error: any) => {
        console.log(error?.error?.message);
      },
    });
  }

  onSignUp(body: any) {
    this.disableSubmitButton = true;
    body.otpLength = 6;
    setTimeout(() => {
      this.disableSubmitButton = false;
    }, 2000);
    this._authService.signUp(body).subscribe({
      next: (response: any) => {
        if (response) {
          this._messageService.info(
            'Check your email for the OTP code to complete sign up.'
          );
          const modal = this._modal.create({
            nzContent: OtpModalComponent,
            nzViewContainerRef: this._viewContainerRef,
            nzComponentParams: {
              data: {
                ...body,       
                isSignup: true 
              },
            },
            nzFooter: null,
            nzKeyboard: true,
            nzMaskClosable: false,
            // nzCloseIcon: null,
            // nzOnCancel: () => false,
          });
          modal?.afterClose.subscribe((event) => {
            if (!event) {
              this.disableSubmitButton = false;
              return;
            }
            this.disableSubmitButton = false;
            this.saveResponseInCache(event);
            this._subscriptionService.loadSubscriptionPermissions();
            this._authService.changeNavState(true);
            this.getUserCompleteProfile();
            // const redirectUrl = this._cacheService.getDataFromCache('redirectUrl');
            // if(redirectUrl){
            //   this._cacheService.removeFromCache('redirectUrl')
            //   this._router.navigateByUrl(redirectUrl);
            // }
            if (response?.role == null) {
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
          });
        }
      },
      error: (error: any) => {
        if (
          error?.error?.status ==
            this._httpConstants.REQUEST_STATUS.BAD_REQUEST_400.CODE ||
          error?.error?.status ==
            this._httpConstants.REQUEST_STATUS.SERVER_ERROR_500.CODE
        ) {
          this._messageService.error(error?.error?.message);
        }
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

  submitForm(): void {
    this.disableSubmitButton = true;
    setTimeout(() => {
      this.disableSubmitButton = false;
    }, 2000);
    if (this.validateForm.valid) {
      if (!this.validateForm.value?.acceptTerms) {
        this._messageService.info('Accept the terms and conditions to proceed');
        return;
      }
      this.user.email = this.validateForm.value?.email;
      this.user.name = this.validateForm.value?.name;
      this.user.password = this.validateForm.value?.password;
      this.user.subscribeNewsletter =
        this.validateForm.value?.subscribeNewsletter;
      this.onSignUp(this.user);
    } else {
      Object.values(this.validateForm.controls).forEach((control) => {
        if (control.invalid) {
          control.markAsDirty();
          control.updateValueAndValidity({ onlySelf: true });
        }
      });
    }
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

  toggleConfirmPassword() {
    if (this.confirmPasswordTextType == true) {
      this.confirmPasswordTextType = false;
      this.eyeTypeConfirm = 'eye-invisible';
    } else {
      this.confirmPasswordTextType = true;
      this.eyeTypeConfirm = 'eye';
    }
  }

  routeToSignIn() {
    this._router.navigate(['auth/sign-in']);
  }
  routeToPrivacyPolicy() {
    const url = this._router.serializeUrl(
      this._router.createUrlTree(['privacy-policy'])
    );
    window.open(url, '_blank');
  }

  validateEmail(event?: any) {
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    const invalidPatterns = [
      /@.*@/,
      /^\s/,
      /[^\w\d.@+-]/,
      /\.\./,
      /@-/,
      /-\./,
      /\.$/,
      /\.\d+$/,
      /\.[a-zA-Z]{1}$/,
    ];

    const email = event.target.value;

    // If the email is empty, leave validation to the 'required' validator
    if (!email) {
      this.validateForm.get('email').setErrors({ required: true });
      this.emailValid = false;
      return;
    }

    let isValid = emailPattern.test(email);

    // Validate against custom invalid patterns
    for (const pattern of invalidPatterns) {
      if (pattern.test(email)) {
        isValid = false;
        break;
      }
    }

    // Update the form control's validity based on custom logic
    this.validateForm
      .get('email')
      .setErrors(isValid ? null : { invalidEmail: true });
    this.emailValid = isValid;
  }

  restrictInput(event: KeyboardEvent): boolean {
    const inputChar = String.fromCharCode(event.keyCode);
    if (!/^[A-Za-z\s]*$/.test(inputChar)) {
      event.preventDefault();
      return false;
    }
    return true;
  }

}
