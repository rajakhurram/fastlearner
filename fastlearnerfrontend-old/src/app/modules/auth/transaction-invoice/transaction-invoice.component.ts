import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { TransactionHistory } from 'src/app/core/models/transaction-history.model';
import { TransactionInvoice } from 'src/app/core/models/transaction-invoice.model';
import { AuthService } from 'src/app/core/services/auth.service';
import { SubscriptionService } from 'src/app/core/services/subscription.service';

@Component({
  selector: 'app-transaction-invoice',
  templateUrl: './transaction-invoice.component.html',
  styleUrls: ['./transaction-invoice.component.scss'],
})
export class TransactionInvoiceComponent implements OnInit {
  billingDetails(billingDetails: any) {
    throw new Error('Method not implemented.');
  }
  getBillingHistDetail() {
    throw new Error('Method not implemented.');
  }
  _httpConstants: HttpConstants = new HttpConstants();
  transId?: any;
  transactionInvoice?: TransactionInvoice;
  transactionHistory?:TransactionHistory;
  billingHistoryId: string;

  constructor(
    private _subscriptionService: SubscriptionService,
    private _message: NzMessageService,
    private _router: Router,
    private _activatedRoute: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this._activatedRoute.queryParams.subscribe((params) => {
      this.transId = params['transId'];
      this.getInvoiceData();
    });
  }

  getInvoiceData() {
    this._subscriptionService?.getTransactionHistoryByTransId(this.transId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.transactionHistory = response?.data;
        }
      },
      error: (error: any) => {
        
      },
    });
  }

  formatDate(date?: any) {
    const formattedDate = new Date(date);
    const day = String(formattedDate.getDate()).padStart(2, '0');
    const month = String(formattedDate.getMonth() + 1).padStart(2, '0');
    const year = formattedDate.getFullYear();
    const d = `${month}/${day}/${year}`;
    return d;
  }
}
