import { ChangeDetectorRef, Component, HostListener, Input, OnInit } from '@angular/core';
import {
  NonNullableFormBuilder,
  FormGroup,
  FormControl,
  Validators,
} from '@angular/forms';
import { NzModalRef, NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AffiliateMode } from 'src/app/core/enums/affiliate.enum';
import { MessageService } from 'src/app/core/services/message.service';
import { buttonConfig } from 'src/app/core/models/button.model-config';
import { AffiliateService } from 'src/app/core/services/affiliate.service';

@Component({
  selector: 'app-affiliate-modal',
  templateUrl: './affiliate-modal.component.html',
  styleUrls: ['./affiliate-modal.component.scss'],
})
export class AffiliateModalComponent implements OnInit {
  affiliateMode = AffiliateMode;
  @Input() mode = AffiliateMode.ADD;
  @Input() data;
  _httpConstants: HttpConstants = new HttpConstants();
  @Input() addedAffiliate = false;
  @Input() fromResendLink = false;
  emailValid?: any = true;
  rewardInvalid: boolean = false;         // Track invalid input
  rewardValidationMsg: string | null = null;  // Store validation message
  errorMessageKey: string = 'rewardError';  // Key for the error message

  constructor(
    private _modal: NzModalService,
    private modalRef: NzModalRef,
    private _affiliateService: AffiliateService,
    private _fb: NonNullableFormBuilder,
    private cdr: ChangeDetectorRef,
    private _messageService: MessageService
  ) {}
  buttonCancelConfig: buttonConfig = {
    backgroundColor: '#fff',
    color: '#fe4a55',
    borderColor: '#FE4A55',
    paddingRight: '60px',
    paddingLeft: '60px',
    border: '1px solid',
  };
  buttonAddConfig: buttonConfig = {
    paddingRight: '60px',
    paddingLeft: '60px',
  };

  affiliateForm: FormGroup<{
    email: FormControl<string>;
    name: FormControl<string>;
    nickName: FormControl<string>;
    defaultReward: FormControl<number>;
  }>;

  ngOnInit(): void {
    this.initializeForm();
    if (this.data) {
      this.patchForm(this.data);
    }
  }

  @HostListener('document:keydown.enter', ['$event'])
  handleEnterKey(event: KeyboardEvent) {
    event.preventDefault(); // Prevent the default action
    event.stopPropagation(); // Stop the event from propagating
  }

  patchForm(data) {
    this.affiliateForm.patchValue(data);
    this.affiliateForm.get('email').disable();
  }

  initializeForm() {
    this.affiliateForm = this._fb.group({
      name: [
        '',
        [
          Validators.required,
          Validators.maxLength(50),
          Validators.pattern(/^[A-Za-z\s]*$/),
        ],
      ],
      nickName: [
        '',
        [
          Validators.required,
          Validators.maxLength(50),
          Validators.pattern(/^(?!^[^a-zA-Z0-9]*$)(?!.*[!*\(\)\}\{\]\[><:;"?])[a-zA-Z0-9@_\$\%\^\&\+=#]+$/)

        ],
      ],
      email: ['', [Validators.required, Validators.email]],
      defaultReward: [
        10,
        [Validators.required, Validators.min(1), Validators.max(90)],
      ],
    });
  }

  cancel(event) {
    this.closeModal(false);
    event?.preventDefault();
  }

  // rewardValidation(event: Event): void {
  //   const inputElement = event.target as HTMLInputElement;
  //   let value = inputElement.value;
  
  //   value = value.replace(/[^0-9.]/g, ''); 
  //   value = value.replace(/(\..*)\./g, '$1'); 
  //   inputElement.value = value;
  //     const numericValue = Number(value);
  //   if (!isNaN(numericValue)) {
  //     this.rewardMinMaxValidation(numericValue);
  //   }
  // }
  
  // rewardMinMaxValidation(value?: number){
  //   if (value > 90) {
  //     this.rewardInvalid = true;
  //     this.rewardValidationMsg = 'Reward cannot be greater than 90%.'
  //     this.showErrorMessage(this.rewardValidationMsg);
  //   } else if (value < 1) {
  //     this.rewardInvalid = true;
  //     this.rewardValidationMsg = 'Reward must be at least 1%.'
  //     this.showErrorMessage(this.rewardValidationMsg);
  //   }else {
  //     this.rewardInvalid = false;
  //     this.rewardValidationMsg = null;
  //     this._messageService.remove();
  //   }
  //   this.cdr.detectChanges();
  // }
  rewardValidation(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    let value = inputElement.value;
  
    // Allow only numeric input with up to 2 decimal places
    const validDecimalRegex = /^\d*\.?\d{0,2}$/;
  
    if (!validDecimalRegex.test(value)) {
      const match = value.match(/^\d*\.?\d{0,2}/);
      value = match ? match[0] : '';
    }
    if (value === '.') {
      value = '';
    }
  
    inputElement.value = value;
  
    const numericValue = value === '' ? null : parseFloat(value);
    if (numericValue !== null) {
      this.rewardMinMaxValidation(numericValue);
    } else {
      this.rewardInvalid = false;
      this.rewardValidationMsg = null;
      this._messageService.remove();
    }
  
    this.cdr.detectChanges();
  }
  
  rewardMinMaxValidation(value: number): void {
    if (value > 90) {
      this.rewardInvalid = true;
      this.rewardValidationMsg = 'Reward cannot be greater than 90%.';
      this.showErrorMessage(this.rewardValidationMsg);
    } else if (value < 1) {
      this.rewardInvalid = true;
      this.rewardValidationMsg = 'Reward must be at least 1%.';
      this.showErrorMessage(this.rewardValidationMsg);
    } else {
      this.rewardInvalid = false;
      this.rewardValidationMsg = null;
      this._messageService.remove();
    }
  
    this.cdr.detectChanges();
  }

  showErrorMessage(message: string): void {
  if (this.rewardInvalid && message) {
    this._messageService.error(message);  
  }
}


  addAffiliate() {
    this.addedAffiliate = true;
    // this.closeModal('added');
  }

  closeModal(params?) {
    this.modalRef.close(params);
  }

  submitForm() {
    if (this.affiliateForm.invalid) {
      Object.keys(this.affiliateForm.controls).forEach((key) => {
        const control = this.affiliateForm.get(key);
        control?.markAsTouched();
        control?.markAsDirty();
      });
      return;
    }
    if (this.mode == this.affiliateMode.ADD) {
      this._affiliateService
        .createAffiliate(this.affiliateForm.value)
        ?.subscribe({
          next: (response: any) => {
            if (
              response?.status ==
              this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
            ) {
              this.addAffiliate();
            }
          },
          error: (error: any) => {
            this._messageService.error(error?.error?.message);
          },
        });
    } else {
      let updatedPayload;
      updatedPayload = {
        ...this.affiliateForm.getRawValue(),
        instructorAffiliateId: this.data.instructorAffiliateId,
      };
      this._affiliateService.editAffiliate(updatedPayload)?.subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this._messageService.success('Affiliate updated successfully');
            this.closeModal(true);
          }
        },
        error: (error: any) => {
          this._messageService.error(error?.error?.message);
        },
      });
    }
  }

  validateEmail(event?: any) {
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    const invalidPatterns = [
      /@.*@/,
      /^\s/,
      /[^\w\d.@+-]/,
      /\.\./,
      /@-/,
      /-\./,
      /\.$/,
      /\.\d+$/,
      /\.[a-zA-Z]{1}$/,
    ];

    const email = event.target.value;

    // If the email is empty, leave validation to the 'required' validator
    if (!email) {
      this.affiliateForm.get('email').setErrors({ required: true });
      this.emailValid = false;
      return;
    }

    let isValid = emailPattern.test(email);

    // Validate against custom invalid patterns
    for (const pattern of invalidPatterns) {
      if (pattern.test(email)) {
        isValid = false;
        break;
      }
    }

    // Update the form control's validity based on custom logic
    this.affiliateForm
      .get('email')
      .setErrors(isValid ? null : { invalidEmail: true });
    this.emailValid = isValid;
  }
}
