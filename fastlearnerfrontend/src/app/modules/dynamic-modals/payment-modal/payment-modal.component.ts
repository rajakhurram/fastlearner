import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NzModalRef, NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { PaymentProfile } from 'src/app/core/models/payment-profile.model';
import { SubscriptionService } from 'src/app/core/services/subscription.service';
import { NzMessageService } from 'ng-zorro-antd/message';
import { contries } from 'src/app/constants/contries';

@Component({
  selector: 'app-payment-modal',
  templateUrl: './payment-modal.component.html',
  styleUrls: ['./payment-modal.component.scss']
})
export class PaymentModalComponent implements OnInit {
  paymentProfile?: PaymentProfile = {};
  _httpConstants: HttpConstants = new HttpConstants();
  @Input() paymentProfileData: any;

  constructor(
    private _activatedRoute: ActivatedRoute,
    private _subscriptionService: SubscriptionService,
    private _modal: NzModalService,
    private modalRef: NzModalRef,
    private _message: NzMessageService
  ) { }

  countries = contries;

  ngOnInit(): void {
    if (this.paymentProfileData != null && this.paymentProfileData != undefined) {
      this.paymentProfile = { ...this.paymentProfileData };
      this.paymentProfile.date =
        this.paymentProfile.expiryMonth + '/' + this.paymentProfile.expiryYear;
      // Masked card numbers from Authorize.Net cannot be re-submitted; require a fresh entry.
      if (String(this.paymentProfile.cardNumber ?? '').toUpperCase().includes('X')) {
        this.paymentProfile.cardNumber = '';
      }
      if (this.paymentProfile.zipCode != null) {
        this.paymentProfile.zipCode = String(this.paymentProfile.zipCode);
      }
    }
  }

  allowNumericDigitsOnlyOnKeyUp(e: any) {
    const charCode = e.which ? e.which : e.keyCode;

    if (charCode > 31 && (charCode < 48 || charCode > 57)) {
      return false;
    }
    return true;
  }

  onExpiryDateKeyDown(event: any): void {
    const allowedKeys = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'];
    let inputValue = event.target.value;

    if (Number(inputValue.slice(0, 2)) > 12) {
      event.preventDefault();
      this.paymentProfile.date = inputValue.slice(0, 1);
    } else if (
      inputValue.length == 5 &&
      Number(inputValue.replace(/\D/g, '').slice(2, 4)) <
        this.getCurrentYearTwoDigits()
    ) {
      this.paymentProfile.date = inputValue.slice(0, 2);
    } else {
      const numericValue = inputValue.replace(/\D/g, '');
      const formattedValue = this.formatExpiryDate(numericValue);

      if (inputValue !== formattedValue) {
        this.paymentProfile.date = formattedValue;
      }
    }
  }

  getCurrentYearTwoDigits(): any {
    const fullYear = new Date().getFullYear();
    return Number(fullYear.toString().slice(-2));
  }


  formatExpiryDate(value: string): string {
    if (value.length <= 2) {
      return value;
    } else {
      const formattedMonth = value.slice(0, 2);
      const formattedYear = value.slice(2, 4);
      return `${formattedMonth}/${formattedYear}`;
    }
  }

  onPaste(event: any): void {
    event.preventDefault();
  }

  savePaymentProfile() {
    if(this.validateData()){
      
      this.paymentProfile.expiryMonth = this.paymentProfile.date?.split('/')[0];
      this.paymentProfile.expiryYear = this.paymentProfile.date?.split('/')[1];
      this.paymentProfile.isSave = this.paymentProfile.isSave == 
      null ? this.paymentProfile.isSave = false : this.paymentProfile.isSave;
      this.paymentProfile.zipCode = String(this.paymentProfile.zipCode).trim();
      this.paymentProfile.countryCode = String(this.paymentProfile.countryCode).trim().toUpperCase();
      if (typeof this.paymentProfile.cardNumber == 'string') {
        const cardDigits = this.paymentProfile.cardNumber.replace(/\D/g, '');
        if (cardDigits.length < 13) {
          this._message.error('Please enter the full card number');
          return;
        }
        this.paymentProfile.cardNumber = cardDigits;
      } else {
        this.paymentProfile.cardNumber = String(this.paymentProfile.cardNumber);
      }
      this._subscriptionService.savePaymentProfile(this.paymentProfile).subscribe({
  next: (response: any) => {
    if (response?.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.modalRef.close({ success: true });
    }
  },
  error: (error: any) => {
    const backendMessage = error?.error;

    if (backendMessage?.data) {
      // If backend returned field-specific validation errors
      const fieldErrors = backendMessage.data;
      let userFriendlyMessage = '';
      for (const key in fieldErrors) {
        if (fieldErrors.hasOwnProperty(key)) {
          userFriendlyMessage += `${fieldErrors[key]}\n`;
        }
      }
      this._message.error(userFriendlyMessage.trim());
    } else if (backendMessage?.message) {
      // fallback to generic backend message
      this._message.error(backendMessage.message);
    } else {
      this._message.error('Something went wrong. Please try again.');
    }
  }
});
    }else {
      this._message.remove();
      this._message.error('Please fill all required fields. ZIP code must be 4-10 digits.');
    }
  }

  closeModal() {
    this._modal.closeAll();
  }

  validateData() {
    const zipCode = this.paymentProfile?.zipCode?.toString().trim() ?? '';
    const isZipValid = /^[0-9]{4,10}$/.test(zipCode);

    const countryCode = this.paymentProfile?.countryCode?.toString().trim() ?? '';
    const countryCodeValid = /^[A-Z]{2}$/i.test(countryCode);

    if (
      this.paymentProfile.firstName != '' &&
      this.paymentProfile.lastName != '' &&
      this.paymentProfile.firstName?.length <= 50 &&
      this.paymentProfile.lastName?.length <= 50 &&
      this.paymentProfile.cardNumber != '' &&
      this.paymentProfile.date.split('/')[0] != '' &&
      this.paymentProfile.date.split('/')[0].length == 2 &&
      this.paymentProfile.date.split('/')[1].length == 2 &&
      this.paymentProfile.date.split('/')[1] != '' &&
      this.paymentProfile.cvv != '' &&
      isZipValid &&
      countryCodeValid &&
      this.paymentProfile.city != '' &&
      this.paymentProfile.address != '' 
    ) {
      return true;
    }
    return false;
  }
  

  checkInput(event: Event): void {
    let cardNumber = this.paymentProfile.cardNumber.replace(/\s+/g, '');
    cardNumber = cardNumber.replace(/(.{4})/g, '$1 ');
    this.paymentProfile.cardNumber = cardNumber.trim();
  }

  onCountryChange(iso2: string) {
    this.paymentProfile.countryCode = iso2;
  }

  allowOnlyLetters(event: KeyboardEvent): void {
    const char = String.fromCharCode(event.keyCode || event.which);
    const pattern = /^[a-zA-Z\s\-']$/;

    if (!pattern.test(char)) {
      event.preventDefault();
    }
  }

  preventEmojiOnPaste(event: ClipboardEvent) {
    const pastedText = event.clipboardData?.getData('text') || '';
    const emojiRegex = /\p{Extended_Pictographic}/u;

    if (emojiRegex.test(pastedText)) {
      event.preventDefault();
    }
  }
}
