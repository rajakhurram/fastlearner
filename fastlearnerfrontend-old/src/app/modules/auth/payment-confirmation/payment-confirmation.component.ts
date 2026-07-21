import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { ConfirmationModalComponent } from '../../dynamic-modals/confirmation-modal/confirmation-modal.component';
import { AnimationOptions } from 'ngx-lottie';
import { AnimationItem } from 'lottie-web';

@Component({
  selector: 'app-payment-confirmation',
  templateUrl: './payment-confirmation.component.html',
  styleUrls: ['./payment-confirmation.component.scss'],
})
export class PaymentConfirmationComponent implements OnInit {
  _httpConstants: HttpConstants = new HttpConstants();

  paypalSubscriptionId: any;

  options: AnimationOptions = {
    path: '../../../../assets/animations/check Mark.json',
  };

  styling: Partial<CSSStyleDeclaration> = {
    height: '100px',
  };

  animationCreated(animationItem: AnimationItem): void {}

  constructor(
    private _router: Router,
    private _activatedRoute: ActivatedRoute,
    private _authService: AuthService,
    private _messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.getPaypalSubscritionIdFromRoute();
  }

  getPaypalSubscritionIdFromRoute() {
    this.paypalSubscriptionId =
      this._activatedRoute.snapshot.queryParams['subscription_id'];
    if (this.paypalSubscriptionId) {
      this.paymentConfirmCompleteSubscription(this.paypalSubscriptionId);
    }
  }

  paymentConfirmCompleteSubscription(paypalSubscriptionId: any) {
    this._authService?.completeSubscription(paypalSubscriptionId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this._messageService.success(
            'Subscription Successfully Subscribed. Welcome!'
          );
        }
      },
      error: (error: any) => {},
    });
  }

  continue() {
    this.routeToStudentLandingPage();
  }

  routeToStudentLandingPage() {
    this._router.navigate(['student']);
  }
}
