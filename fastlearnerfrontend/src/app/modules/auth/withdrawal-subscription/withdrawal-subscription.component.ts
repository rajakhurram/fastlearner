import { Component, ViewContainerRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';

@Component({
  selector: 'app-withdrawal-subscription',
  templateUrl: './withdrawal-subscription.component.html',
  styleUrls: ['./withdrawal-subscription.component.scss']
})
export class WithdrawalSubscriptionComponent {

  _httpConstants : HttpConstants = new HttpConstants();

  paypalSubscriptionId : any;

  constructor(
    private _router : Router,
    private _activatedRoute : ActivatedRoute,
    private _cacheService : CacheService,
    private _authService : AuthService,
    private _messageService : MessageService,
  ) { }

  ngOnInit(): void {
    this.getPaypalSubscritionIdFromRoute();
  }

  getPaypalSubscritionIdFromRoute(){
    this.paypalSubscriptionId = this._activatedRoute.snapshot.queryParams['subscription_id'];
    if(this.paypalSubscriptionId){
      this.cancelOrWithDrawSubscription(this.paypalSubscriptionId);
    }
  }

  cancelOrWithDrawSubscription(paypalSubscriptionId: any){
    this._authService?.cancelPaypalSubscription(paypalSubscriptionId)?.subscribe({
      next: (response: any) => {
        if(response?.status == this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE){
          this._messageService.error('Subscription Withdrawal.');
        }
      },
      error: (error: any) => {
        console.log(error)
      }
    })
  }

  routeBack(){
    this._router.navigate(['auth/sign-in'])
  }
}
