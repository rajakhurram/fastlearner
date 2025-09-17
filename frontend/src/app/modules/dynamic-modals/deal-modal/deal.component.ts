import { Component, Input, OnInit, ViewContainerRef } from '@angular/core';
import { Router } from '@angular/router';
import { NzModalRef, NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AuthService } from 'src/app/core/services/auth.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SubscriptionPlanComponent } from '../../auth/subscription-plan/subscription-plan.component';
import { ConfirmationModalComponent } from '../confirmation-modal/confirmation-modal.component';
import { SubscriptionService } from 'src/app/core/services/subscription.service';

@Component({
  selector: 'app-deal',
  templateUrl: './deal.component.html',
  styleUrls: ['./deal.component.scss'],
})
export class DealComponent implements OnInit {
  _httpConstants: HttpConstants = new HttpConstants();

  @Input() title?: string;
  @Input() subtitle?: string;
  @Input() data?: any;

  constructor(
    private _authService: AuthService,
    private _router: Router,
    private _messageService: MessageService,
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef,
    private modalRef: NzModalRef,
    private _subscriptionService: SubscriptionService
  ) {}

  ngOnInit(): void {}

  cancelSubscription() {
    this._authService.cancelSubscription().subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this._subscriptionService.loadSubscriptionPermissions();
          this.openConfirmationModal();
        }
      },
      error: (error: any) => {
        this._messageService.error(error.error.message);
      },
    });
  }

  openConfirmationModal(): void {
    const modal = this._modal.create({
      nzContent: ConfirmationModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        data: '',
        title: 'Cancel',
      },
      nzFooter: null,
      nzKeyboard: true,
    });
  }

  openSubscriptionPlan(): void {
    const modal = this._modal.create({
      nzContent: SubscriptionPlanComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      nzWidth: '80%',
    });
  }
}
