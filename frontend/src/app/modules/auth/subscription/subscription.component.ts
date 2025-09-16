import {
  Component,
  HostListener,
  OnInit,
  ViewContainerRef,
} from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { SubscriptionDetails } from 'src/app/core/models/subscription-details.model';
import { AuthService } from 'src/app/core/services/auth.service';
import { DealComponent } from '../../dynamic-modals/deal-modal/deal.component';
import { SubscriptionPlanComponent } from '../subscription-plan/subscription-plan.component';
import { SubscriptionService } from 'src/app/core/services/subscription.service';
import { PaymentProfile } from 'src/app/core/models/payment-profile.model';
import { NzMessageService } from 'ng-zorro-antd/message';
import { PaymentModalComponent } from '../../dynamic-modals/payment-modal/payment-modal.component';
import { Router } from '@angular/router';
import { BillingHistory } from 'src/app/core/models/billing-history.model';
import { CacheService } from 'src/app/core/services/cache.service';
import { TransactionHistory } from 'src/app/core/models/transaction-history.model';

@Component({
  selector: 'app-subscription',
  templateUrl: './subscription.component.html',
  styleUrls: ['./subscription.component.scss'],
})
export class SubscriptionComponent implements OnInit {
  _httpConstants: HttpConstants = new HttpConstants();
  subscriptionDetails: SubscriptionDetails = new SubscriptionDetails();

  billingHistoryList: Array<any> = [];
  saveCard: boolean = true;
  isSubscribed: boolean = true;
  fullWidth: boolean = false;
  disabledChangePlanButton: boolean = false;
  paymentProfiles?: PaymentProfile[] = [];
  billingHistory?: BillingHistory[] = [];
  purchasedHistory?: any;
  transactionHistoryDetail: any;
  purchasedHistoryTotalElements?: any;
  transactionHistoryTotalElements?: any;

  transactionHistoryPayLoad?: any = {
    pageNo: 0,
    pageSize: 3,
  };

  purchasedHistoryPayLoad?: any = {
    pageNo: 0,
    pageSize: 3,
  };

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    const screenWidth = event.target.innerWidth;
    if (screenWidth > 768) {
      this.fullWidth = true;
    } else {
      this.fullWidth = false;
    }
  }

  constructor(
    private _authService: AuthService,
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef,
    private _subscriptionService: SubscriptionService,
    private _message: NzMessageService,
    private _router: Router,
    private _cacheService: CacheService
  ) {
    this.userIsSubscribed();
  }

  ngOnInit(): void {
    this.getCurrentSubscriptionAndDetails();
    this.getAllPaymentProfiles();
    this.getBillingHistDetail();
  }

  userIsSubscribed() {
    if (this._authService.isSubscribed()) {
      this.isSubscribed = true;
    } else {
      this.isSubscribed = false;
    }
  }

  getCurrentSubscriptionAndDetails() {
    this._authService.getSubscribedPlanAndDetails().subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.subscriptionDetails = response?.data;
          let loggedInUserDetails = JSON.parse(
            this._cacheService.getDataFromCache('loggedInUserDetails')
          );
          loggedInUserDetails.subscribed = this.subscriptionDetails.subscribed;
          this._cacheService.saveInCache(
            'loggedInUserDetails',
            JSON.stringify(loggedInUserDetails)
          );

          this.userIsSubscribed();
        }
      },
      error: (error: any) => {},
    });
  }

  cancelSubscription() {
    this.openDealModal();
  }

  openDealModal(): void {
    const modal = this._modal.create({
      nzContent: DealComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        data: '',
        title: 'Deal',
      },
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: this.fullWidth ? '40%' : '100%',
      nzWidth: '70%',
    });
  }

  openSubscriptionPlan(): void {

    const currentPlanId = this.subscriptionDetails?.subscriptionId; 

    const modal = this._modal.create({
      nzContent: SubscriptionPlanComponent,
      nzComponentParams: {
        fromSubscriptionPlan: true,
        showFreePlan: true,
        currentPlanId: currentPlanId, 
      },
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: this.fullWidth ? '80%' : '100%',
      nzWidth: '80%',
      nzClassName: 'subscription-modal',
    });
  }

  openPaymentMethod(paymentProfile?: any): void {
    paymentProfile != null && paymentProfile != undefined
      ? (paymentProfile.cvv = '')
      : paymentProfile;
    const modal = this._modal.create({
      nzContent: PaymentModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: this.fullWidth ? '40%' : '100%',
      nzWidth: '70%',
      nzComponentParams: {
        paymentProfileData: paymentProfile,
      },
    });
    modal.afterClose.subscribe((result) => {
      if (result?.success) {
        this.getAllPaymentProfiles();
      }
    });
    
    
  }

  getAllPaymentProfiles() {
    this._subscriptionService?.getAllPaymentProfiles()?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.paymentProfiles = response?.data;
        }
      },
      error: (error: any) => {
        this.paymentProfiles = [];
      },
    });
  }

  paymentDefault(paymentProfile?: any) {
    this._subscriptionService.paymentDefault(paymentProfile.id).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.getAllPaymentProfiles();
        }
      },
      error: (error: any) => {
        console.log(error);
      },
    });
  }

  removePaymentProfile(paymentProfile?: any) {
    this._subscriptionService
      .removePaymentProfile(paymentProfile.id)
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
              this.getAllPaymentProfiles();
          }
        },
        error: (error: any) => {
          this._message.error(error?.error.message);
        },
      });
  }

  updatePaymentProfile(paymentProfile?: any) {
    this.openPaymentMethod(paymentProfile);
  }

  getPurchasedHistory() {
    this._subscriptionService
      .getPurchasedHistory(this.purchasedHistoryPayLoad)
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.purchasedHistoryTotalElements = response?.data?.totalElements;
            this.purchasedHistory = response?.data?.purchasedCourses;
            this.purchasedHistory.forEach((el) => {
              el.date = this.formatDate(el.date);
            });
          }
        },
        error: (error: any) => {
          console.log(error);
        },
      });
  }

  getBillingHistDetail() {
    this._subscriptionService
      ?.getBillingHistoryByUser(this.transactionHistoryPayLoad)
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.transactionHistoryDetail = response?.data?.content;
            this.transactionHistoryTotalElements =
              response?.data?.totalElements;
          }
        },
        error: (error: any) => {
          console.log(error);
        },
      });
  }

  openInvoice(transId?: any) {
    this._router.navigate(['transaction-invoice'], {
      queryParams: { transId: transId },
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

  onPurchasedHistoryPageChange(page?: any) {
    this.purchasedHistoryPayLoad.pageNo = page - 1;
    this.getPurchasedHistory();
  }

  onTransactionHistoryPageChange(page?: any) {
    this.transactionHistoryPayLoad.pageNo = page - 1;
    this.getBillingHistDetail();
  }

  openPurchasedInvoice(courseId?: any) {
    this._subscriptionService
      ?.getPurchasedCourseInvoice(courseId)
      ?.subscribe((response: Blob) => {
        const fileURL = URL.createObjectURL(response);
        window.open(fileURL, '_blank');
        URL.revokeObjectURL(fileURL);
      });
  }
  
  openPurchasedInvoiceFromSubscription(courseId?: any) {
    this._subscriptionService
      ?.getTransactionHistoryByTransId(courseId)
      ?.subscribe((response: Blob) => {
        const fileURL = URL.createObjectURL(response);
        window.open(fileURL, '_blank');
        URL.revokeObjectURL(fileURL);
      });
  }

  tabCheck(event) {
    console.log(event);
    if (event.index == 0) {
      this.billingHistory = [];
      this.getBillingHistDetail();
    } else if (event.index == 1) {
      this.purchasedHistory = [];
      this.getPurchasedHistory();
    }
  }
}
