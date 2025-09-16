import { Component, OnInit } from '@angular/core';
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
import { ResetPassword } from 'src/app/core/models/reset-password.model';
import { AuthService } from 'src/app/core/services/auth.service';
import { MessageService } from 'src/app/core/services/message.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss'],
})
export class ResetPasswordComponent implements OnInit {
  _httpConstants: HttpConstants = new HttpConstants();
  resetPassword: ResetPassword = new ResetPassword();
  passwordEyeType = 'eye-invisible'; // Changed variable name
  confirmPasswordEyeType = 'eye-invisible'; // Added variable for confirm password
  passwordTextType: boolean = false;
  confirmPasswordTextType: boolean = false;
  isLoading:boolean=false;
  email:String;

  validateForm: FormGroup<{
    password: FormControl<string>;
    confirmPassword: FormControl<string>;
  }>;

  constructor(
    private _fb: NonNullableFormBuilder,
    private _router: Router,
    private _activatedRoute: ActivatedRoute,
    private _authService: AuthService,
    private _messageService: MessageService,
    private _modal: NzModalService,
  ) {
    this.validateForm = this._fb.group({
      password: [
        '',
        [
          Validators.required,
          Validators.minLength(6),
          this.alphanumericValidator(),
        ],
      ],
      confirmPassword: ['', [Validators.required, this.confirmationValidator]],
    });
  }

  ngOnInit(): void {
    console.log(this._activatedRoute.snapshot.queryParams['email']);
    this.getOtpAndEmailFromRoute();

  }
  






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

  getOtpAndEmailFromRoute() {

    this.resetPassword.email=this._activatedRoute.snapshot.queryParams['email'];

    this.resetPassword.value = this._activatedRoute.snapshot.queryParams['otp'];
    // this.resetPassword.email =
    //   this._activatedRoute.snapshot.queryParams['email'];


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

  submitForm(): void {
    if (this.validateForm.valid) {
      this.resetPassword.password = this.validateForm.value.password;
      this.changePassword(this.resetPassword);
    } else {
      Object.values(this.validateForm.controls).forEach((control) => {
        if (control.invalid) {
          control.markAsDirty();
          control.updateValueAndValidity({ onlySelf: true });
        }
      });
    }
  }

  changePassword(body: any) {
    this._authService.resetPassword(body).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this._messageService.success('Password Reset Successfully');
          this._router.navigate(['auth/sign-in']);
        }
      },
      error: (error: any) => {
        this._messageService.error(error?.error?.message);
      },
    });
  }

  togglePassword() {
    if (this.passwordTextType == true) {
      this.passwordTextType = false;
      this.passwordEyeType = this.passwordTextType ? 'eye' : 'eye-invisible';
    } else {
      this.passwordTextType = true;
      this.passwordEyeType = 'eye';
    }
  }

  toggleConfirmPassword() {
    if (this.confirmPasswordTextType == true) {
      this.confirmPasswordTextType = false;
      this.confirmPasswordEyeType = 'eye-invisible';
    } else {
      this.confirmPasswordTextType = true;
      this.confirmPasswordEyeType = 'eye';
    }
  }

  routeToSignUp() {
    this._router.navigate(['auth/sign-up']);
  }
}
