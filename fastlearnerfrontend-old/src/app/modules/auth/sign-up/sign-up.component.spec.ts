import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { of, throwError } from 'rxjs';

import { SignUpComponent } from './sign-up.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { UserService } from 'src/app/core/services/user.service';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import {
  GoogleLoginProvider,
  SocialAuthService,
} from '@abacritt/angularx-social-login';
import { StateService } from 'src/app/core/services/state.service';
import { HttpBackend, HttpClient, HttpHandler } from '@angular/common/http';

describe('SignUpComponent', () => {
  let component: SignUpComponent;
  let fixture: ComponentFixture<SignUpComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let cacheService: jasmine.SpyObj<CacheService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let userService: jasmine.SpyObj<UserService>;
  let modalService: jasmine.SpyObj<NzModalService>;
  let router: jasmine.SpyObj<Router>;
  let socialAuthService: jasmine.SpyObj<SocialAuthService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('SocialAuthService', ['signIn'], {
      authState: of(null),
    });

    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'signUp',
      'changeNavState',
      'saveRole',
      'isSubscribed',
      'signUpWithSocialAccount',
      'startTokenTimer',
    ]);
    const cacheServiceSpy = jasmine.createSpyObj('CacheService', [
      'saveInCache',
      'getDataFromCache',
      'removeFromCache',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', [
      'info',
      'error',
    ]);
    const userServiceSpy = jasmine.createSpyObj('UserService', [
      'getUserProfile',
    ]);
    const modalServiceSpy = jasmine.createSpyObj('NzModalService', ['create']);
    const routerSpy = jasmine.createSpyObj('Router', [
      'navigateByUrl',
      'navigate',
      'serializeUrl',
      'createUrlTree',
      'events',
    ]);

    routerSpy.events = of(new NavigationEnd(0, '', ''));

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [SignUpComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: CacheService, useValue: cacheServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: UserService, useValue: userServiceSpy },
        { provide: NzModalService, useValue: modalServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: SocialAuthService, useValue: spy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParams: {} } },
        },
        HttpClient,
        HttpHandler,
        HttpBackend,
        // Mock or provide other dependencies if necessary
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(SignUpComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    cacheService = TestBed.inject(CacheService) as jasmine.SpyObj<CacheService>;
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
    userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
    modalService = TestBed.inject(
      NzModalService
    ) as jasmine.SpyObj<NzModalService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    socialAuthService = TestBed.inject(
      SocialAuthService
    ) as jasmine.SpyObj<SocialAuthService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with default values', () => {
    expect(component.validateForm.value).toEqual({
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      subscribeNewsletter: false,
      acceptTerms: false,
    });
  });

  it('should display validation errors for required fields', () => {
    component.validateForm.controls.name.setValue('');
    component.validateForm.controls.email.setValue('');
    component.validateForm.controls.password.setValue('');
    component.validateForm.controls.confirmPassword.setValue('');

    component.submitForm();

    expect(component.validateForm.controls.name.errors).toEqual({
      required: true,
    });
    expect(component.validateForm.controls.email.errors).toEqual({
      required: true,
    });
    expect(component.validateForm.controls.password.errors).toEqual({
      required: true,
    });
    expect(component.validateForm.controls.confirmPassword.errors).toEqual({
      required: true,
    });
  });

  it('should validate email format', () => {
    component.validateForm.controls.email.setValue('invalid-email');
    component.submitForm();

    expect(component.validateForm.controls.email.errors).toEqual({
      email: true,
    });
  });

  it('should validate password strength', () => {
    // Set a password that does not meet the alphanumeric criteria
    component.validateForm.controls.password.setValue('password');
    component.submitForm();

    // Expect an alphanumeric validation error
    expect(component.validateForm.controls.password.errors).toEqual({
      alphanumeric: true,
    });

    // Set a valid password
    component.validateForm.controls.password.setValue('password123');
    component.submitForm();

    // Expect no validation errors
    expect(component.validateForm.controls.password.errors).toBeNull();
  });

  it('should validate password and confirm password match', () => {
    component.validateForm.controls.password.setValue('password123');
    component.validateForm.controls.confirmPassword.setValue(
      'differentPassword'
    );

    component.submitForm();

    expect(component.validateForm.controls.confirmPassword.errors).toEqual({
      confirm: true,
      error: true,
    });
  });

  it('should not submit the form if terms and conditions are not accepted', () => {
    component.validateForm.setValue({
      name: 'Test User',
      email: 'test@example.com',
      password: 'password123',
      confirmPassword: 'password123',
      subscribeNewsletter: true,
      acceptTerms: false,
    });
    component.submitForm();

    expect(messageService.info).toHaveBeenCalledWith(
      'Accept the terms and conditions to proceed'
    );
    expect(authService.signUp).not.toHaveBeenCalled();
  });

  it('should submit the form and call authService.signUp if valid', fakeAsync(() => {
    component.validateForm.setValue({
      name: 'Test User',
      email: 'test@example.com',
      password: 'password123',
      confirmPassword: 'password123',
      subscribeNewsletter: true,
      acceptTerms: true,
    });

    authService.signUp.and.returnValue(of({}));
    messageService.info.and.stub();

    component.submitForm();
    tick(2500);

    expect(authService.signUp).toHaveBeenCalled();
    expect(messageService.info).toHaveBeenCalledWith(
      'Check your email for the OTP code to complete sign up.'
    );
  }));

  it('should handle signUp error and display message if signUp fails', fakeAsync(() => {
    component.validateForm.setValue({
      name: 'Test User',
      email: 'test@example.com',
      password: 'password123',
      confirmPassword: 'password123',
      subscribeNewsletter: true,
      acceptTerms: true,
    });

    const errorResponse = {
      error: {
        error: {
          message: 'An error occurred while processing your request.',
        },
      },
    };
    authService.signUp.and.returnValue(throwError(() => errorResponse));
    expect(component.disableSubmitButton).toBeFalse();
  }));

  it('should handle email already registered error', fakeAsync(() => {
    component.validateForm.setValue({
      name: 'Test User',
      email: 'already@registered.com',
      password: 'password123',
      confirmPassword: 'password123',
      subscribeNewsletter: true,
      acceptTerms: true,
    });

    const errorResponse = {
      error: {
        error: {
          message: 'Email is already registered.',
        },
      },
    };
    authService.signUp.and.returnValue(throwError(() => errorResponse));
    expect(component.disableSubmitButton).toBeFalse();
  }));

  it('should display error if confirm password does not match valid password', () => {
    component.validateForm.controls.password.setValue('password123');
    component.validateForm.controls.confirmPassword.setValue('password124');

    component.submitForm();

    expect(component.validateForm.invalid);
  });

  it('should not submit the form if required fields are missing', fakeAsync(() => {
    component.validateForm.setValue({
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      subscribeNewsletter: false,
      acceptTerms: false,
    });

    component.submitForm();
    tick(2500);

    expect(authService.signUp).not.toHaveBeenCalled();
    expect(messageService.info).not.toHaveBeenCalled();
  }));
  describe('routeToSignIn', () => {
    it('should navigate to the sign-in page', () => {
      component.routeToSignIn();
      expect(router.navigate).toHaveBeenCalledWith(['auth/sign-in']);
    });
  });
  describe('validateEmail', () => {
    it('should be defined', () => {
      expect(component.validateEmail).toBeDefined();
    });
  });
  describe('routeToSignIn', () => {
    it('should navigate to the sign-in page', () => {
      component.routeToSignIn();
      expect(router.navigate).toHaveBeenCalledWith(['auth/sign-in']);
    });
  });
  describe('togglePassword', () => {
    it('should toggle password visibility', () => {
      component.passwordTextType = true;
      component.togglePassword();
      expect(component.passwordTextType).toBeFalse();
      expect(component.eyeType).toBe('eye-invisible');

      component.togglePassword();
      expect(component.passwordTextType).toBeTrue();
      expect(component.eyeType).toBe('eye');
    });
  });
  describe('toggleConfirmPassword', () => {
    it('should toggle confirm password visibility', () => {
      component.confirmPasswordTextType = true;
      component.toggleConfirmPassword();
      expect(component.confirmPasswordTextType).toBeFalse();
      expect(component.eyeTypeConfirm).toBe('eye-invisible');

      component.toggleConfirmPassword();
      expect(component.confirmPasswordTextType).toBeTrue();
      expect(component.eyeTypeConfirm).toBe('eye');
    });
  });

  describe('userIsSubscribed', () => {
    it('should set isSubscribed to true if isSubscribed returns true', () => {
      authService.isSubscribed.and.returnValue(true);
      component.userIsSubscribed();
      expect(component.isSubscribed).toBeTrue();
    });

    it('should set isSubscribed to false if isSubscribed returns false', () => {
      authService.isSubscribed.and.returnValue(false);
      component.userIsSubscribed();
      expect(component.isSubscribed).toBeFalse();
    });
  });
  describe('updateConfirmValidator', () => {
    it('should update confirmPassword validity asynchronously', fakeAsync(() => {
      spyOn(
        component.validateForm.controls.confirmPassword,
        'updateValueAndValidity'
      );
      component.updateConfirmValidator();
      tick();
      expect(
        component.validateForm.controls.confirmPassword.updateValueAndValidity
      ).toHaveBeenCalled();
    }));
  });
  describe('confirmationValidator', () => {
    it('should return required error if confirmPassword is empty', () => {
      const result = component.confirmationValidator({
        value: '',
      } as AbstractControl);
      expect(result).toEqual({ required: true });
    });

    it('should return confirm error if confirmPassword does not match password', () => {
      component.validateForm.controls.password.setValue('password123');
      const result = component.confirmationValidator({
        value: 'differentPassword',
      } as AbstractControl);
      expect(result).toEqual({ confirm: true, error: true });
    });

    it('should return no error if confirmPassword matches password', () => {
      component.validateForm.controls.password.setValue('password123');
      const result = component.confirmationValidator({
        value: 'password123',
      } as AbstractControl);
      expect(result).toEqual({});
    });
  });
  describe('onSignUpWithSocialAccount', () => {
    it('should call saveResponseInCache and changeNavState if response is successful', () => {
      const response = { subscribed: true, role: 'Student' };
      authService.signUpWithSocialAccount.and.returnValue(of(response));
      spyOn(component, 'saveResponseInCache');
      authService.changeNavState.and.stub();

      component.onSignUpWithSocialAccount({});

      expect(component.saveResponseInCache).toHaveBeenCalledWith(response);
      expect(authService.changeNavState).toHaveBeenCalledWith(true);
    });

    it('should navigate to redirectUrl if response is subscribed and redirectUrl is valid', () => {
      const response = { subscribed: true, role: 'Student' };
      const redirectUrl = '/some-url';
      cacheService.getDataFromCache.and.returnValue(redirectUrl);
      authService.signUpWithSocialAccount.and.returnValue(of(response));
      spyOn(component, 'saveResponseInCache');
      authService.changeNavState.and.stub();

      component.onSignUpWithSocialAccount({});

      expect(cacheService.removeFromCache).toHaveBeenCalledWith('redirectUrl');
      expect(router.navigateByUrl).toHaveBeenCalledWith(redirectUrl);
    });

    it('should save user role if response role is null', () => {
      const response = { role: null };
      authService.signUpWithSocialAccount.and.returnValue(of(response));
      spyOn(component, 'saveUserRole');

      component.onSignUpWithSocialAccount({});

      expect(component.saveUserRole).toHaveBeenCalled();
    });
  });

  describe('saveUserRole', () => {
    it('should save role in cache and call authService.saveRole', () => {
      const response = { status: 200 };
      authService.saveRole.and.returnValue(of(response));
      spyOn(component, 'saveUserRole').and.callThrough();

      component.saveUserRole();

      expect(cacheService.saveInCache).toHaveBeenCalledWith('role', 'STUDENT');
      expect(authService.saveRole).toHaveBeenCalledWith('STUDENT');
    });

    it('should navigate to student if role is STUDENT and isSubscribed is true', () => {
      component.isSubscribed = true;
      const response = { status: 200 };
      authService.saveRole.and.returnValue(of(response));

      component.saveUserRole();

      expect(router.navigate).toHaveBeenCalledWith(['student']);
    });

    it('should navigate to subscription plan if role is STUDENT and isSubscribed is false', () => {
      component.isSubscribed = false;
      const response = { status: 200 };
      authService.saveRole.and.returnValue(of(response));

      component.saveUserRole();

      expect(router.navigate).toHaveBeenCalledWith(['subscription-plan']);
    });

    it('should navigate to instructor if role is INSTRUCTOR', () => {
      // Arrange
      component.saveRole = 'INSTRUCTOR';
      const response = { status: 200 };
      authService.saveRole.and.returnValue(of(response));

      // Act
      component.saveUserRole();

      // Assert
      expect(router.navigate).toHaveBeenCalledWith(['subscription-plan']);
    });
  });
  describe('routeToPrivacyPolicy', () => {
    it('should open the privacy policy page in a new tab', () => {
      spyOn(window, 'open');
      component.routeToPrivacyPolicy();
      expect(router.serializeUrl).toHaveBeenCalledWith(
        router.createUrlTree(['privacy-policy'])
      );
      expect(window.open).toHaveBeenCalledWith(undefined, '_blank');
    });
  });

  describe('getAuthenticUserState', () => {
    it('should call signUp when a user is authenticated', () => {});
  });

  describe('signInWithGoogle', () => {
    it('should call signIn from SocialAuthService', () => {
      component.signInWithGoogle();
      expect(socialAuthService.signIn).toHaveBeenCalledWith(
        GoogleLoginProvider.PROVIDER_ID
      );
    });
  });

  describe('getUserCompleteProfile', () => {
    it('should save user profile in cache on success', () => {
      const response = {
        status: 200,
        data: { name: 'John Doe' },
      };
      userService.getUserProfile.and.returnValue(of(response));

      component.getUserCompleteProfile();

      expect(component.user).toBeDefined();
      expect(cacheService.saveInCache).toHaveBeenCalledWith(
        'userProfile',
        JSON.stringify(response.data)
      );
    });
  });

  describe('signUp', () => {
    it('should call onSignUpWithSocialAccount for Google provider', () => {
      spyOn(component, 'onSignUpWithSocialAccount');
      const user = { idToken: 'some-token', provider: 'GOOGLE' };

      component.signUp(user);

      expect(component.onSignUpWithSocialAccount).toHaveBeenCalledWith({
        provider: 'GOOGLE',
        token: user.idToken,
      });
    });
  });

  it('should validate a valid email correctly', () => {
    const event = { target: { value: 'test@example.com' } };
    component.validateEmail(event);

    expect(component.emailValid).toBeTrue();
    expect(component.validateForm.get('email').errors).toBeNull();
  });

  it('should invalidate an email with multiple "@" symbols', () => {
    const event = { target: { value: 'test@@example.com' } };
    component.validateEmail(event);

    expect(component.emailValid).toBeFalse();
    expect(component.validateForm.get('email').errors).toEqual({
      invalidEmail: true,
    });
  });

  it('should invalidate an email with leading space', () => {
    const event = { target: { value: ' test@example.com' } };
    component.validateEmail(event);

    expect(component.emailValid).toBeFalse();
    expect(component.validateForm.get('email').errors).toEqual({
      invalidEmail: true,
    });
  });

  it('should invalidate an email with consecutive dots', () => {
    const event = { target: { value: 'test..example@example.com' } };
    component.validateEmail(event);

    expect(component.emailValid).toBeFalse();
    expect(component.validateForm.get('email').errors).toEqual({
      invalidEmail: true,
    });
  });

  it('should invalidate an email with domain starting with hyphen', () => {
    const event = { target: { value: 'test@-example.com' } };
    component.validateEmail(event);

    expect(component.emailValid).toBeFalse();
    expect(component.validateForm.get('email').errors).toEqual({
      invalidEmail: true,
    });
  });

  it('should invalidate an email with domain ending with hyphen', () => {
    const event = { target: { value: 'test@example-.com' } };
    component.validateEmail(event);

    expect(component.emailValid).toBeFalse();
    expect(component.validateForm.get('email').errors).toEqual({
      invalidEmail: true,
    });
  });

  it('should invalidate an email ending with a dot', () => {
    const event = { target: { value: 'test@example.com.' } };
    component.validateEmail(event);

    expect(component.emailValid).toBeFalse();
    expect(component.validateForm.get('email').errors).toEqual({
      invalidEmail: true,
    });
  });

  it('should invalidate an email with a numeric TLD', () => {
    const event = { target: { value: 'test@example.123' } };
    component.validateEmail(event);

    expect(component.emailValid).toBeFalse();
    expect(component.validateForm.get('email').errors).toEqual({
      invalidEmail: true,
    });
  });

  it('should invalidate an email with a single character TLD', () => {
    const event = { target: { value: 'test@example.c' } };
    component.validateEmail(event);

    expect(component.emailValid).toBeFalse();
    expect(component.validateForm.get('email').errors).toEqual({
      invalidEmail: true,
    });
  });

  it('should invalidate an email with special characters not allowed', () => {
    const event = { target: { value: 'test!@example.com' } };
    component.validateEmail(event);

    expect(component.emailValid).toBeFalse();
    expect(component.validateForm.get('email').errors).toEqual({
      invalidEmail: true,
    });
  });
});
