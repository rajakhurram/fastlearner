import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ForgetPasswordComponent } from './forget-password.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { MessageService } from 'src/app/core/services/message.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { NzModalService } from 'ng-zorro-antd/modal';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('ForgetPasswordComponent', () => {
  let component: ForgetPasswordComponent;
  let fixture: ComponentFixture<ForgetPasswordComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let modalService: jasmine.SpyObj<NzModalService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['forgetPassword']);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', ['success', 'error']);
    const modalServiceSpy = jasmine.createSpyObj('NzModalService', ['create']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [ForgetPasswordComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: NzModalService, useValue: modalServiceSpy },
        { provide: Router, useValue: routerSpy },
        HttpConstants,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ForgetPasswordComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    messageService = TestBed.inject(MessageService) as jasmine.SpyObj<MessageService>;
    modalService = TestBed.inject(NzModalService) as jasmine.SpyObj<NzModalService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not proceed if email is missing in forgetPassword', () => {
    component.forgetPassword({ email: '' });

    expect(authService.forgetPassword).not.toHaveBeenCalled();
    expect(messageService.error).toHaveBeenCalledWith('Email is required to reset the password.');
  });

  it('should call forgetPassword on AuthService with email', () => {
    const email = 'test@example.com';
    authService.forgetPassword.and.returnValue(of({ status: 200 }));

    component.forgetPassword({ email });

    expect(authService.forgetPassword).toHaveBeenCalledWith(email);
  });

  it('should show success message and open OTP modal on successful password reset', () => {
    const email = 'test@example.com';
    authService.forgetPassword.and.returnValue(of({ status: 200 }));

    modalService.create.and.returnValue({
      afterClose: of(true),
    } as any); // Mock modal close

    component.forgetPassword({ email });

    expect(authService.forgetPassword).toHaveBeenCalledWith(email);
    expect(modalService.create).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['auth/reset-password']);
  });

  it('should handle non-success status response', () => {
    const email = 'test@example.com';
    authService.forgetPassword.and.returnValue(of({ status: 400 }));

    component.forgetPassword({ email });

    expect(messageService.error).toHaveBeenCalledWith('Failed to send password reset link. Please try again.');
  });

  it('should handle error response from forgetPassword', () => {
    const errorResponse = { error: { message: 'An error occurred' } };
    authService.forgetPassword.and.returnValue(throwError(() => errorResponse));

    component.forgetPassword({ email: 'test@example.com' });

    expect(messageService.error).toHaveBeenCalledWith('An error occurred');
  });

  it('should show default error message if no error message is provided', () => {
    authService.forgetPassword.and.returnValue(throwError(() => ({})));

    component.forgetPassword({ email: 'test@example.com' });

    expect(messageService.error).toHaveBeenCalledWith('An error occurred. Please try again.');
  });
});
