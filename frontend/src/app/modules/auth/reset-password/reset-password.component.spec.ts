import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ResetPasswordComponent } from './reset-password.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { MessageService } from 'src/app/core/services/message.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { ActivatedRoute } from '@angular/router';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';

describe('ResetPasswordComponent', () => {
  let component: ResetPasswordComponent;
  let fixture: ComponentFixture<ResetPasswordComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let router: jasmine.SpyObj<Router>;
  let modalService: jasmine.SpyObj<NzModalService>;
  let activatedRoute: jasmine.SpyObj<ActivatedRoute>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'resetPassword',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', [
      'success',
      'error',
    ]);
    const modalServiceSpy = jasmine.createSpyObj('NzModalService', ['create']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const activatedRouteSpy = jasmine.createSpyObj(
      'ActivatedRoute',
      ['snapshot'],
      {
        snapshot: {
          queryParams: { otp: 'dummyOtp', email: 'test@example.com' },
        },
      }
    );

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [ResetPasswordComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: NzModalService, useValue: modalServiceSpy },
        { provide: ActivatedRoute, useValue: activatedRouteSpy },
        HttpConstants,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ResetPasswordComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    activatedRoute = TestBed.inject(
      ActivatedRoute
    ) as jasmine.SpyObj<ActivatedRoute>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with otp and email from route', () => {
    expect(component.resetPassword.value).toBe('dummyOtp');
    expect(component.resetPassword.email).toBe('test@example.com');
  });

  it('should call changePassword on valid form submission', () => {
    component.validateForm.setValue({
      password: 'newPassword123',
      confirmPassword: 'newPassword123',
    });
    authService.resetPassword.and.returnValue(of({ status: 200 }));

    component.submitForm();

    expect(authService.resetPassword).toHaveBeenCalledWith(
      component.resetPassword
    );
    expect(messageService.success).toHaveBeenCalledWith(
      'Password Reset Successfully'
    );
    expect(router.navigate).toHaveBeenCalledWith(['auth/sign-in']);
  });

  it('should handle error response from AuthService on form submission', fakeAsync(() => {
    component.validateForm.setValue({
      password: 'newPassword123',
      confirmPassword: 'newPassword123',
    });
    const errorResponse = { error: { message: null } };
    authService.resetPassword.and.returnValue(throwError(() => errorResponse));

    component.submitForm();
    tick(1000);
    expect(messageService.error).toHaveBeenCalled();
  }));

  it('should toggle password visibility', () => {
    component.togglePassword();
    expect(component.passwordTextType).toBeTrue();
    expect(component.passwordEyeType).toBe('eye');

    component.togglePassword();
    expect(component.passwordTextType).toBeFalse();
    expect(component.passwordEyeType).toBe('eye-invisible');
  });

  it('should toggle confirm password visibility', () => {
    component.toggleConfirmPassword();
    expect(component.confirmPasswordTextType).toBeTrue();
    expect(component.confirmPasswordEyeType).toBe('eye');

    component.toggleConfirmPassword();
    expect(component.confirmPasswordTextType).toBeFalse();
    expect(component.confirmPasswordEyeType).toBe('eye-invisible');
  });

  it('should update confirm password validation when password changes', fakeAsync(() => {
    component.validateForm.controls.password.setValue('newPassword123');
    component.validateForm.controls.confirmPassword.setValue('wrongPassword');
    fixture.detectChanges();

    component.updateConfirmValidator();
    tick();

    const confirmPasswordControl =
      component.validateForm.get('confirmPassword');
    expect(confirmPasswordControl.errors).toEqual({
      confirm: true,
      error: true,
    });
  }));

  it('should not show password errors when password is valid', () => {
    component.validateForm.controls.password.setValue('newPassword123');
    component.validateForm.controls.confirmPassword.setValue('newPassword123');
    fixture.detectChanges();

    expect(component.validateForm.get('password').valid).toBeTrue();
    expect(component.validateForm.get('confirmPassword').valid).toBeTrue();
  });

  it('should handle missing passwords on form submission', () => {
    component.validateForm.setValue({
      password: '',
      confirmPassword: '',
    });

    component.submitForm();

    expect(authService.resetPassword).not.toHaveBeenCalled();
    expect(component.validateForm.invalid);
  });
  it('should handle unexpected error response from AuthService on form submission', fakeAsync(() => {
    component.validateForm.setValue({
      password: 'newPassword123',
      confirmPassword: 'newPassword123',
    });

    const unexpectedErrorResponse = { error: { message: 'Unexpected error' } };
    authService.resetPassword.and.returnValue(
      throwError(() => unexpectedErrorResponse)
    );
    expect(component.validateForm.invalid);
  }));
  it('should handle toggle visibility when passwordTextType is undefined', () => {
    component.passwordTextType = undefined; // simulate unexpected state
    component.togglePassword();

    expect(component.passwordTextType).toBeTrue();
    expect(component.passwordEyeType).toBe('eye');

    component.togglePassword();
    expect(component.passwordTextType).toBeFalse();
    expect(component.passwordEyeType).toBe('eye-invisible');
  });

  it('should handle toggle visibility when confirmPasswordTextType is undefined', () => {
    component.confirmPasswordTextType = undefined; // simulate unexpected state
    component.toggleConfirmPassword();

    expect(component.confirmPasswordTextType).toBeTrue();
    expect(component.confirmPasswordEyeType).toBe('eye');

    component.toggleConfirmPassword();
    expect(component.confirmPasswordTextType).toBeFalse();
    expect(component.confirmPasswordEyeType).toBe('eye-invisible');
  });
});
