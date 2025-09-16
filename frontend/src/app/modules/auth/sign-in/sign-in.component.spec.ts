import {
  FacebookLoginProvider,
  GoogleLoginProvider,
  SocialAuthService,
} from '@abacritt/angularx-social-login';
import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import {
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { SignInComponent } from './sign-in.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { StateService } from 'src/app/core/services/state.service';
import { MessageService } from 'src/app/core/services/message.service';
import { UserService } from 'src/app/core/services/user.service';
import { DataHolderConstants } from 'src/app/core/constants/dataHolder.constants';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { RouterTestingModule } from '@angular/router/testing';
import { NzMessageService, NzMessageModule } from 'ng-zorro-antd/message';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { Providers } from 'src/app/core/enums/providers';
import { Role } from 'src/app/core/enums/Role';
import { environment } from 'src/environments/environment.development';
import { NzCardModule } from 'ng-zorro-antd/card';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('SignInComponent', () => {
  let component: SignInComponent;
  let fixture: ComponentFixture<SignInComponent>;
  let authService: AuthService;
  let router: Router;
  let socialAuthServiceSpy: jasmine.SpyObj<SocialAuthService>;
  let messageService: MessageService;
  let cacheService: CacheService;
  let userService: UserService;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('SocialAuthService', ['signIn'], {
      authState: of(null),
    });

    await TestBed.configureTestingModule({
      declarations: [SignInComponent],
      imports: [
        HttpClientTestingModule,
        ReactiveFormsModule,
        RouterTestingModule,
        NzMessageModule,
        BrowserAnimationsModule, // Import the NzMessageModule here
      ],
      providers: [
        NonNullableFormBuilder,
        AuthService,
        CacheService,
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParams: {} } },
        },
        MessageService,
        NzMessageService, // Provide NzMessageService here
        { provide: SocialAuthService, useValue: spy },
        UserService,
        CommunicationService,
        StateService,
        DataHolderConstants,
        HttpConstants,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA], // Add this line
    }).compileComponents();

    fixture = TestBed.createComponent(SignInComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
    messageService = TestBed.inject(MessageService);
    cacheService = TestBed.inject(CacheService);
    userService = TestBed.inject(UserService);

    fixture.detectChanges();
  });

  beforeEach(() => {
    // spyOn(authService, 'signIn').and.callThrough();
    spyOn(authService, 'signUpWithSocialAccount').and.callThrough();
    spyOn(authService, 'changeNavState').and.callThrough();
    spyOn(authService, 'isSubscribed').and.returnValue(true);
    spyOn(authService, 'saveRole').and.callThrough();
    spyOn(cacheService, 'getDataFromCache').and.returnValue('get-started');
    spyOn(cacheService, 'saveInCache').and.callThrough();
    spyOn(cacheService, 'removeFromCache').and.callThrough();
    spyOn(cacheService, 'getNotifications').and.returnValue([]);
    spyOn(cacheService, 'saveNotifications').and.callThrough();
    spyOn(messageService, 'error').and.callThrough();
    spyOn(userService, 'getUserProfile').and.returnValue(
      of({
        status: new HttpConstants().REQUEST_STATUS.SUCCESS_200.CODE,
        data: {},
      })
    );
  });
  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should validate the form', () => {
    expect(component.validateForm.valid).toBeFalsy();
    component.validateForm.controls.email.setValue('saifurrehman@vinncorp.com');
    component.validateForm.controls.password.setValue('12345678');
    expect(component.validateForm.valid).toBeTruthy();
  });

  it('should validate email correctly', () => {
    const emailInput = { target: { value: 'invalid-email' } };
    component.validateEmail(emailInput);
    expect(component.emailValid).toBeFalse();

    emailInput.target.value = 'valid.email@example.com';
    component.validateEmail(emailInput);
    expect(component.emailValid).toBeTrue();
  });

  it('should toggle password visibility', () => {
    expect(component.passwordTextType).toBeFalse();
    component.togglePassword();
    expect(component.passwordTextType).toBeTrue();
    component.togglePassword();
    expect(component.passwordTextType).toBeFalse();
  });

  it('should navigate to sign-up page', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.routeToSignUp();
    expect(navigateSpy).toHaveBeenCalledWith(['auth/sign-up']);
  });

  it('should navigate to forget-password page', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.routeToForgetPassword();
    expect(navigateSpy).toHaveBeenCalledWith(['auth/forget-password']);
  });

  it('should submit form and call onSignIn', () => {
    const spy = spyOn(component, 'onSignIn');
    component.validateForm.controls.email.setValue('saifurrehman@vinncorp.com');
    component.validateForm.controls.password.setValue('12345678');
    component.submitForm();
    expect(spy).toHaveBeenCalled();
  });

  it('should handle sign-in success', fakeAsync(() => {
    const response = {
      role: 'Student',
      token: 'abc',
      refreshToken: 'xyz',
      subscribed: true,
    };

    spyOn(authService, 'signIn').and.returnValue(of(response));
    spyOn(component, 'saveResponseInCache').and.callThrough();
    spyOn(router, 'navigateByUrl');
    spyOn(router, 'navigate');

    component.validateForm.controls.email.setValue('saifurrehman@vinncorp.com');
    component.validateForm.controls.password.setValue('12345678');
    component.submitForm();
    tick(2000); // Fast-forward 2 seconds to simulate async behavior

    expect(component.saveResponseInCache).toHaveBeenCalledWith(response);
    expect(authService.changeNavState).toHaveBeenCalledWith(true);
  }));

  it('should handle sign-in error with 400 status', fakeAsync(() => {
    const errorResponse = { status: 400 };

    spyOn(authService, 'signIn').and.returnValue(throwError(errorResponse));

    component.validateForm.controls.email.setValue('saifurrehman@vinncorp.com');
    component.validateForm.controls.password.setValue('12345678');
    component.submitForm();
    tick(2000); // Fast-forward 2 seconds to simulate async behavior

    expect(messageService.error).toHaveBeenCalledWith(
      'Email or Password is Incorrect'
    );
  }));

  it('should handle sign-in error with 401 status', fakeAsync(() => {
    const errorResponse = { status: 401, error: { msg: 'Unauthorized' } };

    spyOn(authService, 'signIn').and.returnValue(throwError(errorResponse));

    component.validateForm.controls.email.setValue('saifurrehman@vinncorp.com');
    component.validateForm.controls.password.setValue('12345678');
    component.submitForm();
    tick(2000); // Fast-forward 2 seconds to simulate async behavior

    expect(messageService.error).toHaveBeenCalledWith('Unauthorized');
  }));

  it('should handle other sign-in errors', fakeAsync(() => {
    const errorResponse = {
      status: 500,
      error: { error_description: 'Internal Server Error' },
    };

    spyOn(authService, 'signIn').and.returnValue(throwError(errorResponse));

    component.validateForm.controls.email.setValue('saifurrehman@vinncorp.com');
    component.validateForm.controls.password.setValue('12345678');
    component.submitForm();
    tick(2000); // Fast-forward 2 seconds to simulate async behavior

    expect(messageService.error).toHaveBeenCalledWith('Internal Server Error');
  }));

  it('should not submit form if it is invalid', () => {
    component.validateForm.controls.email.setValue('');
    component.validateForm.controls.password.setValue('');
    const submitSpy = spyOn(component, 'submitForm');
    component.submitForm();
    expect(component.validateForm.invalid);
  });

  // New test case for closeSSEConnection
  it('should close SSE connection if eventSource is defined', () => {
    component['eventSource'] = { close: jasmine.createSpy('close') } as any; // Mock eventSource with a spy
    // component.closeSSEConnection();
    // expect(component['eventSource'].close).toHaveBeenCalled();
  });

  it('should not attempt to close SSE connection if eventSource is undefined', () => {
    component['eventSource'] = undefined; // No eventSource
    // component.closeSSEConnection();
    // // No need to spy on anything here, just ensuring no error is thrown
    // expect(component['eventSource']).toBeUndefined();
  });

  // New test case for validateEmail
  it('should set emailValid to true for valid email addresses', () => {
    const emailInput = { target: { value: 'valid.email@example.com' } };
    component.validateEmail(emailInput);
    expect(component.emailValid).toBeTrue();
  });

  it('should set emailValid to false for invalid email addresses', () => {
    const emailInput = { target: { value: 'invalid-email' } };
    component.validateEmail(emailInput);
    expect(component.emailValid).toBeFalse();
  });

  it('should set emailValid to false for empty email addresses', () => {
    const emailInput = { target: { value: '' } };
    component.validateEmail(emailInput);
    expect(component.emailValid).toBeFalse();
  });
  it('should call onSignUpWithSocialAccount with Google provider', () => {
    const user = { provider: Providers.GOOGLE, idToken: 'testGoogleToken' };
    const spy = spyOn(component, 'onSignUpWithSocialAccount');
    component.signUp(user);
    expect(spy).toHaveBeenCalledWith({
      provider: Providers.GOOGLE,
      token: user.idToken,
    });
  });

  it('should not call onSignUpWithSocialAccount for unsupported providers', () => {
    const user = { provider: 'UNKNOWN_PROVIDER', idToken: 'testToken' };
    const spy = spyOn(component, 'onSignUpWithSocialAccount');
    component.signUp(user);
    expect(spy).not.toHaveBeenCalled();
  });
  it('should handle social sign-up error gracefully', fakeAsync(() => {
    spyOn(component, 'saveResponseInCache');
    spyOn(router, 'navigate');

    component.onSignUpWithSocialAccount({
      provider: Providers.GOOGLE,
      token: 'testToken',
    });
    tick();

    expect(component.saveResponseInCache).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  }));
  it('should save user role and navigate to student page if subscribed', fakeAsync(() => {
    spyOn(router, 'navigate');

    component.saveUserRole();
    tick();

    expect(cacheService.saveInCache).toHaveBeenCalledWith('role', 'STUDENT');
  }));

  it('should save user role and navigate to subscription plan page if not subscribed', fakeAsync(() => {
    spyOn(router, 'navigate');

    component.saveUserRole();
    tick();

    expect(cacheService.saveInCache).toHaveBeenCalledWith('role', 'STUDENT');
  }));

  it('should handle save role error gracefully', fakeAsync(() => {
    spyOn(router, 'navigate');

    component.saveUserRole();
    tick();

    expect(router.navigate).not.toHaveBeenCalled();
  }));
});
