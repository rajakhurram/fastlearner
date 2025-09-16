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
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { ResetPassword } from 'src/app/core/models/reset-password.model';
import { AuthService } from 'src/app/core/services/auth.service';
import { MessageService } from 'src/app/core/services/message.service';
import { UserService } from 'src/app/core/services/user.service';
import {
  FacebookLoginProvider,
  GoogleLoginProvider,
  SocialAuthService,
} from '@abacritt/angularx-social-login';
@Component({
  selector: 'app-change-user-password',
  templateUrl: './change-user-password.component.html',
  styleUrls: ['./change-user-password.component.scss'],
})
export class ChangeUserPasswordComponent implements OnInit {
  _httpConstants: HttpConstants = new HttpConstants();
  oldEyeType = 'eye-invisible';
  newEyeType = 'eye-invisible';
  confirmEyeType = 'eye-invisible';
  passwordTextType: boolean = false;
  confirmPasswordTextType: boolean = false;
  oldPasswordTextType: boolean = false;

  validateForm: FormGroup<{
    oldPassword: FormControl<string>;
    newPassword: FormControl<string>;
    confirmPassword: FormControl<string>;
  }>;

  payLoad = {
    oldPassword: '',
    newPassword: '',
  };

  constructor(
    private _fb: NonNullableFormBuilder,
    private _router: Router,
    private _activatedRoute: ActivatedRoute,
    private _userService: UserService,
    private _messageService: MessageService,
    private _socialAuthService: SocialAuthService
  ) {
    this.validateForm = this._fb.group({
      oldPassword: ['', [Validators.required]],
      newPassword: [
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
    this.checkSocialUser();
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

  checkSocialUser(): void {
    this._socialAuthService.authState.subscribe((user) => {
      if (user) {
        this._router.navigate(['']);
      }
    });
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
    } else if (control.value !== this.validateForm.controls.newPassword.value) {
      return { confirm: true, error: true };
    }
    return {};
  };

  submitForm(): void {
    if (this.validateForm.valid) {
      this.payLoad.oldPassword = this.validateForm.value.oldPassword;
      this.payLoad.newPassword = this.validateForm.value.newPassword;
      this.changePassword(this.payLoad);
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
    this._userService.changeUserPassword(body).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this._messageService.success('Password Changed Successfully');
          this._router.navigate(['student']);
        }
      },
      error: (error: any) => {
        if (
          error?.error?.status ==
          this._httpConstants.REQUEST_STATUS.BAD_REQUEST_400.CODE
        ) {
          this._messageService.error(error?.error?.message);
        }
      },
    });
  }

  toggleOldPassword() {
    if (this.oldPasswordTextType == true) {
      this.oldPasswordTextType = false;
      this.oldEyeType = 'eye-invisible';
    } else {
      this.oldPasswordTextType = true;
      this.oldEyeType = 'eye';
    }
  }

  togglePassword() {
    if (this.passwordTextType == true) {
      this.passwordTextType = false;
      this.newEyeType = 'eye-invisible';
    } else {
      this.passwordTextType = true;
      this.newEyeType = 'eye';
    }
  }

  toggleConfirmPassword() {
    if (this.confirmPasswordTextType == true) {
      this.confirmPasswordTextType = false;
      this.confirmEyeType = 'eye-invisible';
    } else {
      this.confirmPasswordTextType = true;
      this.confirmEyeType = 'eye';
    }
  }
}
