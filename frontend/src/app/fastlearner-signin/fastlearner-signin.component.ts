import { SocialAuthService, GoogleLoginProvider, GoogleSigninButtonModule } from '@abacritt/angularx-social-login';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { NzCardComponent } from 'ng-zorro-antd/card';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { MessageService } from 'src/app/core/services/message.service';
import { NotificationService } from 'src/app/core/services/notification.service';
import { UserService } from 'src/app/core/services/user.service';
import { AntDesignModule } from '../ui-library/ant-design/ant-design.module';

@Component({
  // standalone: true,
  // imports: [AntDesignModule, GoogleSigninButtonModule],
  selector: 'app-fastlearner-signin',
  templateUrl: './fastlearner-signin.component.html',
  styleUrls: ['./fastlearner-signin.component.scss']
})
export class FastlearnerSigninComponent {

  isSubscribed: boolean;
  emailValid?: any = false;
  passwordTextType: boolean = false;
  eyeType = 'eye-invisible';
  callInProgress: boolean = false;

  validateForm: FormGroup<{
    email: FormControl<string>;
    password: FormControl<string>;
  }>;

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
    private cdr: ChangeDetectorRef
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

  submitForm(): void {
    if (this.validateForm.valid) {
      const token = 'validate-token'
      // we have to return token here
      // this.onSignIn(this.validateForm.value);
      window.opener.postMessage(
        { token },
        'http://localhost:4200/'
      );
      window.close();
    } else {
      Object.values(this.validateForm.controls).forEach((control) => {
        if (control.invalid) {
          control.markAsDirty();
          control.updateValueAndValidity({ onlySelf: true });
        }
      });
    }
  }

  routeToForgetPassword() {
    this._router.navigate(['auth/forget-password']);
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

  validateEmail(event?: any) {
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    const valid = emailPattern.test(event.target.value);
    valid == false ? (this.emailValid = false) : (this.emailValid = true);
  }

  routeToSignUp() {
    this._communicationService.flLoginStateChange(false);
  }

}
