import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { Router } from '@angular/router';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { of, throwError } from 'rxjs';
import { OtpModalComponent } from './otp-modal.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { MessageService } from 'src/app/core/services/message.service';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('OtpModalComponent', () => {
  let component: OtpModalComponent;
  let fixture: ComponentFixture<OtpModalComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let modalRef: jasmine.SpyObj<NzModalRef>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'resetOtpVerify',
      'verifyOTP',
      'resendOtp',
      'signUp',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', [
      'error',
      'success',
    ]);
    const modalRefSpy = jasmine.createSpyObj('NzModalRef', ['close']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [OtpModalComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: NzModalRef, useValue: modalRefSpy },
        { provide: Router, useValue: routerSpy },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(OtpModalComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
    modalRef = TestBed.inject(NzModalRef) as jasmine.SpyObj<NzModalRef>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    component.data = { email: 'test@example.com', otpLength: 4 }; // Mock input data
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should initialize the countdown timer', () => {
      spyOn(component, 'countDown');
      component.ngOnInit();
      expect(component.countDown).toHaveBeenCalled();
    });
  });

  describe('onOtpChange', () => {
    it('should emit OTP code when 4 digits are entered', () => {
      spyOn(component.otpCodeEmitter, 'emit');
      component.onOtpChange('1234');
      expect(component.otpCodeEmitter.emit).toHaveBeenCalledWith('1234');
    });
  });

  describe('onClose', () => {
    it('should close modal and navigate if OTP is valid and 6 digits', () => {
      component.data.otpLength = 6;
      component.otpcode = '123456';
      authService.resetOtpVerify.and.returnValue(of({}));

      component.onClose();

      expect(authService.resetOtpVerify).toHaveBeenCalledWith(
        component.data.email,
        Number(component.otpcode)
      );
      expect(modalRef.close).toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['auth/reset-password'], {
        queryParams: { email: component.data.email },
      });
    });

    it('should display error message if OTP is invalid', () => {
      component.otpcode = '123';
      component.onClose();
      expect(messageService.error).toHaveBeenCalledWith(
        'Enter all 4-digits to proceed'
      );
    });
  });

  describe('resendOTP', () => {
    it('should call resendOtp and reset timer if otpLength is 6', () => {
      component.data.otpLength = 6;
      authService.resendOtp.and.returnValue(of({}));
      spyOn(component, 'countDown');

      component.resendOTP();

      expect(authService.resendOtp).toHaveBeenCalledWith(component.data.email);
      expect(component.countDown).toHaveBeenCalled();
    });

    it('should call signUp if otpLength is not 6', () => {
      component.data.otpLength = 4;
      authService.signUp.and.returnValue(of({}));

      component.resendOTP();

      expect(authService.signUp).toHaveBeenCalledWith(component.data);
    });
  });
});
