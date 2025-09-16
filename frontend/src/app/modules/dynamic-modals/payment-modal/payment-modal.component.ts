import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NzModalRef, NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { PaymentProfile } from 'src/app/core/models/payment-profile.model';
import { SubscriptionService } from 'src/app/core/services/subscription.service';
import { NzMessageService } from 'ng-zorro-antd/message';

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



  ngOnInit(): void {
    if (this.paymentProfileData != null && this.paymentProfileData != undefined) {
      this.paymentProfile = this.paymentProfileData;
      this.paymentProfile.date = this.paymentProfile.expiryMonth + '/' + this.paymentProfile.expiryYear;
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
      this.paymentProfile.isSave = this.paymentProfile.isSave == null ? this.paymentProfile.isSave = false : this.paymentProfile.isSave;
      if (typeof this.paymentProfile.cardNumber == 'string') {
        const cardNumberAsInteger = parseInt(
          this.paymentProfile.cardNumber.replace(/\D/g, ''),
          10
        );
        this.paymentProfile.cardNumber = cardNumberAsInteger;
      } else {
        this.paymentProfile.cardNumber = this.paymentProfile.cardNumber;
      }
      this._subscriptionService.savePaymentProfile(this.paymentProfile).subscribe({
        next: (response: any) => {
          if (response?.status == this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
            this.modalRef.close( { success: true } );
          }
        },
        error: (error: any) => {
           this._message.error(error?.error?.message);
        }
      })
    }else {
      this._message.error('Please fill the fields');
    }
  }

  closeModal() {
    this._modal.closeAll();
  }

  validateData() {
    if (this.paymentProfile?.firstName != '' && this.paymentProfile?.lastName != '' && this.paymentProfile.firstName?.length <= 50 &&
      this.paymentProfile.lastName?.length <= 50 && this.paymentProfile.cardNumber != '' && this.paymentProfile.expiryMonth != '' && this.paymentProfile.expiryYear != '' && this.paymentProfile.cvv != '') {
      return true
    }
    return false;
  }

  checkInput(event: Event): void {
    let cardNumber = this.paymentProfile.cardNumber.replace(/\s+/g, '');
    cardNumber = cardNumber.replace(/(.{4})/g, '$1 ');
    this.paymentProfile.cardNumber = cardNumber.trim();
  }

}
