import { query } from '@angular/animations';
import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { Router } from '@angular/router';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AuthService } from 'src/app/core/services/auth.service';
import { MessageService } from 'src/app/core/services/message.service';

@Component({
  selector: 'app-otp-modal',
  templateUrl: './otp-modal.component.html',
  styleUrls: ['./otp-modal.component.scss'],
})
export class OtpModalComponent implements OnInit, OnDestroy {
  @Input() data?: any;
  _httpConstants: HttpConstants = new HttpConstants();
  @Output() otpCodeEmitter = new EventEmitter<string>();
  otpcode: string = '';
  timeLeft = 59;
  isSignup?: boolean; 
  continueBtn: boolean = true;
  resentButton: boolean = false;
  showTimer: boolean = true;
  timerId: any;
  otp: string = '';
  otpSend?: boolean = false;
  constructor(
    private modalRef: NzModalRef,
    private _messageService: MessageService,
    private _authService: AuthService,
    private _router: Router
  ) {}

  ngOnInit(): void {
    this.countDown();
    this.modalRef.afterClose?.subscribe((event) => {
      this.timeLeft = 59;
      this.continueBtn = true;
      clearTimeout(this.timerId);
    });
  }

  ngOnDestroy(): void {
    clearTimeout(this.timerId);
  }

  onOtpChange(event: string) {
    this.otpcode = event;
    if (event.length == 6) {
      this.otpCodeEmitter.emit(event);
    }
  }

  onEnterKeyPress() {
    if (this.otpcode.length === this.data?.otpLength) {
      this.onClose();
    }
  }
  onClose() {
    if (
      this.otpcode &&
      this.otpcode.toString().length === this.data?.otpLength
    ) {
      this.data.otp = Number(this.otpcode);
  
      if (this.data?.isSignup) {
        // Handle OTP verification for signup
        this._authService
          ?.verifyOTP(this.data.email, this.data.otp)
          ?.subscribe({
            next: (data) => {
              if (data) {
                this.modalRef.close(data);
              }
            },
            error: (error: any) => {
              this._messageService.error(error?.error?.message);
            },
          });
      } else {
        // Handle OTP verification for forgot password
        this._authService
          ?.resetOtpVerify(this.data.email, this.data.otp)
          ?.subscribe({
            next: (data) => {
              if (data) {
                this._router.navigate(['auth/reset-password'], {
                  queryParams: { email: this.data?.email },
                });
                this.modalRef.close(data);
              }
            },
            error: (error: any) => {
              this._messageService.error(error?.error?.message);
            },
          });
      }
    } else {
      this._messageService.error('Enter all 6-digits to proceed');
    }
  }
  
  resendOTP() {
    this.otpSend = true;
    const successCallback = () => {
      this._messageService.success('OTP code has been resent to your email');
      this.continueBtn = true;
      this.showTimer = true;
      this.timeLeft = 59;
      this.countDown();
      this.resentButton = false;
      this.otpSend = false;
    };

    if (this.data?.isSignup) {
      // Resend OTP for signup
      this._authService.signUp(this.data)?.subscribe({
        next: (response: any) => {
          successCallback();
        },
        error: (error: any) => {
          console.error('Error signing up user for OTP:', error);
          this.otpSend = false;
        },
      });
    } else {
      // Resend OTP for forgot password
      this._authService.resendOtp(this.data.email)?.subscribe({
        next: (response: any) => {
          successCallback();
        },
        error: (error: any) => {
          console.error('Error resending OTP:', error);
          this.otpSend = false;
        },
      });
    }
  }

  countDown() {
    this.timerId = setInterval(() => {
      if (this.timeLeft == 0) {
        this.continueBtn = false;
        this.showTimer = false;
        this.resentButton = true;
        clearTimeout(this.timerId);
      } else {
        this.timeLeft = this.timeLeft;
        this.timeLeft--;
      }
    }, 1000);
  }
}
