import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ChangeUserPasswordComponent } from './change-user-password.component';
import { UserService } from 'src/app/core/services/user.service';
import { MessageService } from 'src/app/core/services/message.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { SocialAuthService } from '@abacritt/angularx-social-login';
import { ActivatedRoute } from '@angular/router';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('ChangeUserPasswordComponent', () => {
  let component: ChangeUserPasswordComponent;
  let fixture: ComponentFixture<ChangeUserPasswordComponent>;
  let userService: jasmine.SpyObj<UserService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let router: jasmine.SpyObj<Router>;
  let socialAuthService: jasmine.SpyObj<SocialAuthService>;

  beforeEach(async () => {
    const userServiceSpy = jasmine.createSpyObj('UserService', [
      'changeUserPassword',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', [
      'success',
      'error',
    ]);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const socialAuthServiceSpy = jasmine.createSpyObj('SocialAuthService', [], {
      authState: of(null),
    });
    const activatedRouteSpy = jasmine.createSpyObj('ActivatedRoute', [], {
      snapshot: { queryParams: {} },
    });

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [ChangeUserPasswordComponent],
      providers: [
        { provide: UserService, useValue: userServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: SocialAuthService, useValue: socialAuthServiceSpy },
        { provide: ActivatedRoute, useValue: activatedRouteSpy },
        HttpConstants,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ChangeUserPasswordComponent);
    component = fixture.componentInstance;
    userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    socialAuthService = TestBed.inject(
      SocialAuthService
    ) as jasmine.SpyObj<SocialAuthService>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call changePassword on valid form submission', () => {
    component.validateForm.setValue({
      oldPassword: 'oldPassword123',
      newPassword: 'newPassword123',
      confirmPassword: 'newPassword123',
    });
    userService.changeUserPassword.and.returnValue(of({ status: 200 }));

    component.submitForm();

    expect(userService.changeUserPassword).toHaveBeenCalledWith({
      oldPassword: 'oldPassword123',
      newPassword: 'newPassword123',
    });
    expect(messageService.success).toHaveBeenCalledWith(
      'Password Changed Successfully'
    );
    expect(router.navigate).toHaveBeenCalledWith(['student']);
  });

  it('should handle error response from UserService on form submission', () => {
    component.validateForm.setValue({
      oldPassword: 'oldPassword123',
      newPassword: 'newPassword123',
      confirmPassword: 'newPassword123',
    });
    const errorResponse = {
      error: { status: 400, message: 'Invalid request' },
    };
    userService.changeUserPassword.and.returnValue(
      throwError(() => errorResponse)
    );

    component.submitForm();

    expect(messageService.error).toHaveBeenCalledWith('Invalid request');
  });

  it('should not submit the form if it is invalid', () => {
    component.validateForm.setValue({
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
    });

    component.submitForm();

    expect(userService.changeUserPassword).not.toHaveBeenCalled();
  });

  it('should toggle old password visibility', () => {
    component.toggleOldPassword();
    expect(component.oldPasswordTextType).toBeTrue();
    expect(component.oldEyeType).toBe('eye');

    component.toggleOldPassword();
    expect(component.oldPasswordTextType).toBeFalse();
    expect(component.oldEyeType).toBe('eye-invisible');
  });

  it('should toggle new password visibility', () => {
    component.togglePassword();
    expect(component.passwordTextType).toBeTrue();
    expect(component.newEyeType).toBe('eye');

    component.togglePassword();
    expect(component.passwordTextType).toBeFalse();
    expect(component.newEyeType).toBe('eye-invisible');
  });

  it('should toggle confirm password visibility', () => {
    component.toggleConfirmPassword();
    expect(component.confirmPasswordTextType).toBeTrue();
    expect(component.confirmEyeType).toBe('eye');

    component.toggleConfirmPassword();
    expect(component.confirmPasswordTextType).toBeFalse();
    expect(component.confirmEyeType).toBe('eye-invisible');
  });

  it('should update confirm password validation when new password changes', fakeAsync(() => {
    component.validateForm.controls.newPassword.setValue('newPassword123');
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

  it('should validate form fields properly', () => {
    component.validateForm.controls.oldPassword.setValue('oldPassword123');
    component.validateForm.controls.newPassword.setValue('newPassword123');
    component.validateForm.controls.confirmPassword.setValue('newPassword123');
    fixture.detectChanges();

    expect(component.validateForm.get('oldPassword').valid).toBeTrue();
    expect(component.validateForm.get('newPassword').valid).toBeTrue();
    expect(component.validateForm.get('confirmPassword').valid).toBeTrue();
  });

  it('should show error messages for invalid form fields', () => {
    component.validateForm.controls.oldPassword.setValue('');
    component.validateForm.controls.newPassword.setValue('short');
    component.validateForm.controls.confirmPassword.setValue('different');
    fixture.detectChanges();

    expect(component.validateForm.get('oldPassword').invalid).toBeTrue();
    expect(component.validateForm.get('newPassword').invalid).toBeTrue();
    expect(component.validateForm.get('confirmPassword').errors).toEqual({
      confirm: true,
      error: true,
    });
  });

  it('should not call changePassword if form is empty', () => {
    component.validateForm.setValue({
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
    });

    component.submitForm();

    expect(userService.changeUserPassword).not.toHaveBeenCalled();
    expect(messageService.success).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should not call changePassword if new passwords do not match', () => {
    component.validateForm.setValue({
      oldPassword: 'oldPassword123',
      newPassword: 'newPassword123',
      confirmPassword: 'mismatchPassword',
    });

    component.submitForm();

    expect(userService.changeUserPassword).not.toHaveBeenCalled();
    expect(component.validateForm.invalid);
  });

  it('should handle network error during form submission', () => {
    component.validateForm.setValue({
      oldPassword: 'oldPassword123',
      newPassword: 'newPassword123',
      confirmPassword: 'newPassword123',
    });
    const errorResponse = {
      error: { status: 500, message: 'Server error' },
    };
    userService.changeUserPassword.and.returnValue(
      throwError(() => errorResponse)
    );

    component.submitForm();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should not validate form if required fields are missing', () => {
    component.validateForm.controls.oldPassword.setValue('');
    component.validateForm.controls.newPassword.setValue('');
    component.validateForm.controls.confirmPassword.setValue(''); // Missing fields

    expect(component.validateForm.invalid).toBeTrue();
    expect(component.validateForm.get('oldPassword').errors).toEqual({
      required: true,
    });
    expect(component.validateForm.get('newPassword').errors).toEqual({
      required: true,
    });
    expect(component.validateForm.get('confirmPassword').errors).toEqual({
      required: true,
    });
  });

  it('should show error for confirm password mismatch', () => {
    component.validateForm.controls.newPassword.setValue('newPassword123');
    component.validateForm.controls.confirmPassword.setValue(
      'differentPassword'
    );
    fixture.detectChanges();

    expect(component.validateForm.get('confirmPassword').errors).toEqual({
      confirm: true,
      error: true,
    });
  });
});
