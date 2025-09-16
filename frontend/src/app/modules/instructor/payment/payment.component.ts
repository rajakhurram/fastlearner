import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { Payment } from 'src/app/core/models/payment.model';
import { PaymentService } from 'src/app/core/services/payment.service';
import { CompletionModalComponent } from '../../dynamic-modals/completion-modal/completion-modal.component';
import { WithdrawModalComponent } from '../../dynamic-modals/withdraw-modal/withdraw-modal.component';
import { DeletionModalComponent } from '../../dynamic-modals/deletion-modal/deletion-modal.component';

@Component({
  selector: 'app-payment',
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.scss']
})
export class PaymentComponent implements OnInit {

  _httpConstants: HttpConstants = new HttpConstants();
  payment?: Payment;
  accountConnected?: boolean = false;
  balance?: any;
  totalElements?: any;

  transactionHistoryPayload?: any = {
    pageNo: 0,
    pageSize: 10
  }

  transactionHistory?: any = [];

  constructor(
    private _paymentService: PaymentService,
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef,
  ) { }

  ngOnInit(): void {
    this.fetchStripeAccountDetails();
    this.fetchTransactionHistory();
  }

  createStripeAccount() {
    this._paymentService?.createStripeAccount()?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          window.open(response?.data, '_blank');
        }
      },
      error: (error: any) => { },
    });
  }

  fetchStripeAccountDetails() {
    this._paymentService?.fetchStripeAccountDetails()?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.payment = response?.data;
          this.balance = this.payment?.balance;
          this.accountConnected = true;
        }
      },
      error: (error: any) => { },
    });
  }

  deleteStripeAccount() {
    if (!this.balance && Number(this.balance) == 0) {
      this.openDeletionModal();
    } else {
      this.openWithdrawWaringModal();
    }
  }

  fetchTransactionHistory() {
    this._paymentService?.fetchTransactionHistory(this.transactionHistoryPayload)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.transactionHistory = response?.data?.histories;
          this.totalElements = response?.data?.totalElements;
          this.transactionHistory?.forEach((el: any) => {
            el.time = this.formatTime(el.withdrawalAt);
            el.withdrawalAt = this.formatDate(el.withdrawalAt);
          });
        }
      },
      error: (error: any) => { },
    });
  }

  openDeletionModal() {

    const modal = this._modal?.create({
      nzContent: DeletionModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: '40%',
      nzComponentParams: {
        msg: "Are you sure you want to delete?",
        secondBtnText: "Delete"
      },
      nzOnCancel: () => {

      },
    });

    modal?.componentInstance?.deleteClick?.subscribe(() => {
      this._paymentService?.deleteStripeAccount()?.subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.payment = null;
            this.balance = null;
            this.accountConnected = false;
          }
        },
        error: (error: any) => { },
      });
    });

    modal?.componentInstance.cancelClick?.subscribe(() => {
    });
  }

  openWithdrawWaringModal() {
    const modal = this._modal.create({
      nzContent: WithdrawModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        canWithdraw: false
      },
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: '40%',
    });
    modal?.afterClose?.subscribe((event) => {
    });
  }

  openWithDrawModal() {
    const modal = this._modal.create({
      nzContent: WithdrawModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        balance: this.balance,
        bankName: this.payment?.bankName,
        canWithdraw: true
      },
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: '40%',
    });
    modal?.afterClose?.subscribe((event) => {
      if (event === 'withdrawn') {
        this.fetchStripeAccountDetails();
        this.fetchTransactionHistory();
      }
    });
    
  }

  onPageChange(page?: any) {
    this.transactionHistoryPayload.pageNo = page - 1;
    this.fetchTransactionHistory();
  }

  formatDate(date?: any) {
    const formattedDate = new Date(date);
    const day = String(formattedDate.getDate()).padStart(2, '0');
    const monthShort = formattedDate.toLocaleString('default', { month: 'short' }).toUpperCase();
    const year = formattedDate.getFullYear();
    const formattedString = `${monthShort} ${day}, ${year}`;
    return formattedString;
  }

  formatTime(date?: any): any {
    const datetimeStr = date;
    const dateObj = new Date(datetimeStr);
    let hours = dateObj.getUTCHours();
    const minutes = dateObj.getUTCMinutes().toString().padStart(2, '0');

    const period = hours >= 12 ? 'PM' : 'AM';

    hours = hours % 12;
    hours = hours ? hours : 12;
    const timeStr = `${hours}:${minutes} ${period}`;
    return timeStr;

  }

}
