import {
  ChangeDetectorRef,
  Component,
  Input,
  OnDestroy,
  OnInit,
  Optional,
} from '@angular/core';
import { Router } from '@angular/router';
import { AppConstants } from 'src/app/core/constants/app.constants';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SubscriptionService } from 'src/app/core/services/subscription.service';
import { environment } from 'src/environments/environment.development';
import { NzModalRef, NzModalService } from 'ng-zorro-antd/modal';

@Component({
  selector: 'app-subscription-plan',
  templateUrl: './subscription-plan.component.html',
  styleUrls: ['./subscription-plan.component.scss'],
})
export class SubscriptionPlanComponent implements OnInit, OnDestroy {
  @Input() fromSubscriptionPlan = false;
  @Input() showFreePlan?: boolean = true;
  @Input() showStandardPlan?: boolean= true;
  @Input() currentPlanId?: string | null;
  _httpConstants: HttpConstants = new HttpConstants();
  _appConstants: AppConstants = new AppConstants();

  subscriptionList: Array<any> = [];
  noSubscriptionPresent: boolean = false;
  isFreePlanSelected: boolean = false;
  isPlanSelected?: boolean = false;
  displayAnnual: boolean = false; // Track the plan type (Annual or Monthly)
  // switchValue?: boolean = false;
  constructor(
    private _changeDetectorRef: ChangeDetectorRef,
    private _router: Router,
    @Optional() private modalRef: NzModalRef,
    private _authService: AuthService,
    private _messageService: MessageService,
    private _cacheService: CacheService,
    private _subscriptionService: SubscriptionService,
    private _modal: NzModalService
  ) {}

  ngOnInit(): void {
    if (!this.subscriptionList?.length) {
      this.getSubscriptionPlanList();
    }
  }

  ngOnDestroy(): void {
    if (!this.isPlanSelected && !this.fromSubscriptionPlan && !this.isFreePlanSelected) {
      this._authService.verifyUserSubscription().subscribe({
        next: (response: any) => {
          if (!response?.data?.currentPlan || response?.data?.currentPlan !== 'Free Plan') {            
            this.handleFreePlan();
          }
        },
        error: (error: any) => {
          console.log('Failed to verify subscription:', error);
        },
      });
    }
  }
  

  togglePlanType(isAnnual: boolean): void {
    this.displayAnnual = isAnnual;
  }

  getSubscriptionPlanList() {
    this._authService.getSubscriptionPlans()?.subscribe({
      next: (response: any) => {
        if (response?.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
          this.subscriptionList = response?.data?.filter((plan: any) =>
            (!this.currentPlanId || plan.id !== this.currentPlanId) &&
            (this.showStandardPlan || plan.planType !== 'STANDARD')
          );
  
          this.noSubscriptionPresent = false;
          this._changeDetectorRef.detectChanges();
        } else {
          this.noSubscriptionPresent = true;
          this.isPlanSelected = false;
        }
      },
      error: (error: any) => {
        this.noSubscriptionPresent = true;
        console.error(error);
      },
    });
  }
  
  

  onSelectPlan(
    subscriptionPlanType: any,
    subscriptionId: any,
    paypalPlanId: any
  ) {
    this.isPlanSelected = true;
    if (subscriptionPlanType == 'Free Plan') {
      this.isFreePlanSelected = true;
      let payLoad = {
        paypalPlanId: paypalPlanId,
        subscriptionId: subscriptionId,
      };
      this.subscribeToPlan(payLoad, subscriptionPlanType);
    } else {
      this._router.navigate(['payment-method'], {
        queryParams: { subscriptionId: subscriptionId },
      });
    }
  }

 handleFreePlan(): void {
  this._authService.verifyUserSubscription().subscribe({
    next: (response: any) => {
      const isSubscribed = response?.data === true;
      const currentPlan = response?.data?.currentPlan;

      if (!isSubscribed) {
        this.isFreePlanSelected = true; 
        this._authService.newUserSubscription().subscribe({
          next: () => this.navigateToLandingPage(),
          error: () => this._messageService.error('Error processing free subscription.'),
        });
      } else if (currentPlan && currentPlan !== 'Free Plan') {
        this.isFreePlanSelected = true; 
        this._authService.cancelSubscription().subscribe({
          next: (res: any) => {
            if (res?.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
              this.navigateToLandingPage();
            }
          },
        });
      } else {
        this.isFreePlanSelected = true; 
        this.modalRef?.close(); 
      }
    },
    error: () => this._messageService.error('Failed to fetch subscription details.'),
  });
}

  
  
  
  

  // Helper method to handle navigation
  private navigateToLandingPage(): void {
    const redirectUrl = this._cacheService.getDataFromCache('redirectUrl');
    if (redirectUrl) {
      this._router.navigateByUrl(environment.basePath); // Redirect to base path (landing page)
    } else {
      this._router.navigate(['/']); // Fallback to root if no redirect URL is found
    }
  }

  subscribeToPlan(payLoad: any, subscriptionPlanType: any) {
    if (
      subscriptionPlanType ===
      this._appConstants.SUBSCRIPTION_PLAN_TYPE.FREE_PLAN
    ) {
      this.handleFreePlan();
    }
    this._authService.createSubscription(payLoad)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this._subscriptionService.loadSubscriptionPermissions();
          switch (subscriptionPlanType) {
            case this._appConstants.SUBSCRIPTION_PLAN_TYPE.FREE_PLAN:
              this._messageService.success(
                'You are Successfully Subscribed To Free Plan'
              );

              this._subscriptionService.updateUserSubscriptionCheck(true);
              const redirectUrl =
                this._cacheService.getDataFromCache('redirectUrl');
              if (redirectUrl) {
                this._router.navigateByUrl(environment.basePath);
              }
              return;
            case this._appConstants.SUBSCRIPTION_PLAN_TYPE.STANDARD_PLAN:
              this._messageService.success(
                'You are Successfully Subscribed To Standard Plan'
              );
              window.open(response?.data, '_self');
              break;
            case this._appConstants.SUBSCRIPTION_PLAN_TYPE.ANNUAL_PLAN:
              this._messageService.success(
                'You are Successfully Subscribed To Annual Plan'
              );
              window.open(response?.data, '_self');
              break;
          }
        }

        this.isPlanSelected = true;
      },
      error: (error: any) => {
        console.log(error);
        if (
          error?.error?.status ==
          this._httpConstants.REQUEST_STATUS.BAD_REQUEST_400.CODE
        ) {
          this._messageService.error(error?.error?.message);
        }
      },
    });
  }

  getSvgForCard(planType: string): string {
    switch (planType) {
      case 'PREMIUM':
        return '../../../../assets/icons/premium-sparkle.svg';
      case 'ULTIMATE':
        return '../../../../assets/icons/ultimate-sparkle.svg';
      default:
        return '../../../../assets/icons/sparkle.svg';
    }
  }

  toggleSwitch(value: boolean) {
    this.displayAnnual = value;
  }

}
