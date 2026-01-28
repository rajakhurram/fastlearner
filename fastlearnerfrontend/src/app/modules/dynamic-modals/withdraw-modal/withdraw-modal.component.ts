import { Component, Input, OnInit } from '@angular/core';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { PaymentService } from 'src/app/core/services/payment.service';

@Component({
  selector: 'app-withdraw-modal',
  templateUrl: './withdraw-modal.component.html',
  styleUrls: ['./withdraw-modal.component.scss']
})
export class WithdrawModalComponent implements OnInit {

  _httpConstants: HttpConstants = new HttpConstants();
  @Input() balance?: any;
  @Input() bankName?: any;
  @Input() canWithdraw?: boolean;
  withDrawAmount?: any;
  withdrawBtnDisable?: boolean = true;
  availableBalance?: any;

  constructor(
    private _paymentService: PaymentService,
    private _modalRef: NzModalRef
  ) { }

  ngOnInit(): void {
    this.availableBalance = this.balance;
  }

  withdrawBalance() {
    this._paymentService?.withdrawBalance(this.withDrawAmount, this.bankName)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this._modalRef.destroy('withdrawn'); 
        }
      },
      error: (error: any) => { },
    });
  }

  onlyInputNumber(event: KeyboardEvent) {

    const charCode = event.key ? event.key.charCodeAt(0) : null;

    if (charCode !== null && (charCode < 48 || charCode > 57) && charCode !== 8 && event.key !== 'Backspace') {
      event.preventDefault();
    }
  }

  validateAmount(event: KeyboardEvent) {

    const charCode = event.key ? event.key.charCodeAt(0) : null;

    if (charCode !== null && (charCode < 48 || charCode > 57) && charCode !== 8 && event.key !== 'Backspace') {
      event.preventDefault();
    }else {
      const withDrawlAmount = Number(this.withDrawAmount);

      if (withDrawlAmount <= 0) {
        this.withDrawAmount = null;
      }

      if (withDrawlAmount === null) {
        this.withdrawBtnDisable = true;
        this.availableBalance = this.balance;
        return;
      }
  
      if (withDrawlAmount > this.balance) {
        this.withdrawBtnDisable = true;
      } else {
        this.withdrawBtnDisable = false;
        this.availableBalance = this.balance - withDrawlAmount;
      }
    }
  }

  close() {
    this._modalRef.destroy();
  }

}
