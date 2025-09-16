import { Component, ViewContainerRef } from '@angular/core';
import {
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AuthService } from 'src/app/core/services/auth.service';
import { MessageService } from 'src/app/core/services/message.service';
import { OtpModalComponent } from '../../dynamic-modals/otp-modal/otp-modal.component';
import { NzModalService } from 'ng-zorro-antd/modal';
@Component({
  selector: 'app-forget-password',
  templateUrl: './forget-password.component.html',
  styleUrls: ['./forget-password.component.scss'],
})
export class ForgetPasswordComponent {
  _httpConstants: HttpConstants = new HttpConstants();
  showTimer: boolean = false;
  timeLeft: any = 119;
  timerId: any;
  showResendLinkButton: boolean = false;
  resetButton: boolean = false;
  isPasswordResetSent: boolean = false;  
  disableSubmitButton: boolean = false;
  isModalOpen = false; // Flag to track modal state

  validateForm: FormGroup<{
    email: FormControl<string>;
  }>;

  constructor(
    private _fb: NonNullableFormBuilder,
    private _router: Router,
    private _authService: AuthService,
    private _messageService: MessageService,
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef,
  ) {
    this.validateForm = this._fb.group({
      email: [
        '',
        [
          Validators.required,
          Validators.pattern(
            /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
          ),
        ],
      ],
    });
  }

  get emailControl() {
    return this.validateForm.get('email');
  }

  submitForm(): void {
    if (this.isModalOpen) {
      return; // Prevent multiple clicks
    }
  
    if (this.validateForm.valid) {
      this.forgetPassword(this.validateForm.value);
    } else {
      Object.values(this.validateForm.controls).forEach((control) => {
        if (control.invalid) {
          control.markAsDirty();
          control.updateValueAndValidity({ onlySelf: true });
        }
      });
    }
  }
  
  forgetPassword(body: any) {
    // Ensure email is provided before making a request
    if (!body?.email) {
      this._messageService.error('Email is required to reset the password.');
      return;
    }
  
    if (this.isModalOpen) {
      return; // Prevent multiple modals
    }
    
    this.isModalOpen = true; // Set flag to prevent multiple modals
    body.otpLength = 6;
  
    this._authService?.forgetPassword(body.email)?.subscribe({
      next: (response: any) => {
        if (response?.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
          // Open the OTP modal upon successful response
          const modal = this._modal.create({
            nzContent: OtpModalComponent,
            nzViewContainerRef: this._viewContainerRef,
            nzComponentParams: { data: body },
            nzFooter: null,
            nzKeyboard: true,
            nzMaskClosable: false
          });
  
          // Handle modal close event
          modal?.afterClose.subscribe((event) => {
            this.isModalOpen = false; // Reset flag after modal closes
            if (event && event.isValidOtp) {
              this._router.navigate(['auth/reset-password']);
            }
          });
        } else {
          this.isModalOpen = false; // Reset flag in case of failure
          this._messageService.error('Failed to send password reset link. Please try again.');
        }
      },
      error: (error: any) => {
        this.isModalOpen = false; // Reset flag in case of error
        this.showResendLinkButton = false;
        this.showTimer = false;
        this._messageService.error(error?.error?.message || 'An error occurred. Please try again.');
      }
    });
  }

  countDown() {
    this.timerId = setInterval(() => {
      if (this.timeLeft === 0) {
        this.showResendLinkButton = true;
        this.resetButton = false;
        clearInterval(this.timerId);
      } else {
        this.timeLeft--;
      }
    }, 1000);
  }


  routeToSignUp() {
    this._router.navigate(['auth/sign-up']);
  }

  formatTimeLeft(): string {
    const minutes = Math.floor(this.timeLeft / 60);
    const seconds = this.timeLeft % 60;
    return `${minutes < 10 ? '0' : ''}${minutes}:${seconds < 10 ? '0' : ''}${seconds}`;
  }
  

}

