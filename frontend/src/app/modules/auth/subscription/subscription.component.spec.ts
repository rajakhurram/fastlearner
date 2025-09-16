import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { SubscriptionComponent } from './subscription.component';
import { NzModalService } from 'ng-zorro-antd/modal';
import { AuthService } from 'src/app/core/services/auth.service';
import { SubscriptionService } from 'src/app/core/services/subscription.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { NzMessageService } from 'ng-zorro-antd/message';
import { Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { CUSTOM_ELEMENTS_SCHEMA, ViewContainerRef } from '@angular/core';
import { DealComponent } from '../../dynamic-modals/deal-modal/deal.component';
import { PaymentModalComponent } from '../../dynamic-modals/payment-modal/payment-modal.component';
import { SubscriptionPlanComponent } from '../subscription-plan/subscription-plan.component';

describe('SubscriptionComponent', () => {
  let component: SubscriptionComponent;
  let fixture: ComponentFixture<SubscriptionComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let subscriptionService: jasmine.SpyObj<SubscriptionService>;
  let cacheService: jasmine.SpyObj<CacheService>;
  let messageService: jasmine.SpyObj<NzMessageService>;
  let modalService: jasmine.SpyObj<NzModalService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'isSubscribed',
      'getSubscribedPlanAndDetails',
    ]);
    const subscriptionServiceSpy = jasmine.createSpyObj('SubscriptionService', [
      'getAllPaymentProfiles',
      'paymentDefault',
      'removePaymentProfile',
      'getBillingHistory',
      'getBillingHistoryByUser'
    ]);
    const cacheServiceSpy = jasmine.createSpyObj('CacheService', [
      'getDataFromCache',
      'saveInCache',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('NzMessageService', [
      'error',
      'success',
    ]);
    const modalServiceSpy = jasmine.createSpyObj('NzModalService', ['create']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [SubscriptionComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: SubscriptionService, useValue: subscriptionServiceSpy },
        { provide: CacheService, useValue: cacheServiceSpy },
        { provide: NzMessageService, useValue: messageServiceSpy },
        { provide: NzModalService, useValue: modalServiceSpy },
        { provide: Router, useValue: routerSpy },
        ViewContainerRef,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(SubscriptionComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    subscriptionService = TestBed.inject(
      SubscriptionService
    ) as jasmine.SpyObj<SubscriptionService>;
    cacheService = TestBed.inject(CacheService) as jasmine.SpyObj<CacheService>;
    messageService = TestBed.inject(
      NzMessageService
    ) as jasmine.SpyObj<NzMessageService>;
    modalService = TestBed.inject(
      NzModalService
    ) as jasmine.SpyObj<NzModalService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize and call necessary methods', () => {
    spyOn(component, 'getCurrentSubscriptionAndDetails');
    spyOn(component, 'getAllPaymentProfiles');
    spyOn(component, 'getBillingHistory');

    component.ngOnInit();

    expect(component.getCurrentSubscriptionAndDetails).toHaveBeenCalled();
    expect(component.getAllPaymentProfiles).toHaveBeenCalled();
    expect(component.getBillingHistory).toHaveBeenCalled();
  });

  it('should set fullWidth to true when screen width is greater than 768', () => {
    component.onResize({ target: { innerWidth: 800 } });
    expect(component.fullWidth).toBe(true);
  });

  it('should set fullWidth to false when screen width is less than or equal to 768', () => {
    component.onResize({ target: { innerWidth: 768 } });
    expect(component.fullWidth).toBe(false);
  });

  it('should update isSubscribed based on authService response', () => {
    authService.isSubscribed.and.returnValue(true);
    component.userIsSubscribed();
    expect(component.isSubscribed).toBe(true);

    authService.isSubscribed.and.returnValue(false);
    component.userIsSubscribed();
    expect(component.isSubscribed).toBe(false);
  });

  it('should open DealModal when cancelSubscription is called', () => {
    const modal = { afterClose: of(null) };
    modalService.create.and.returnValue(modal as any);

    component.cancelSubscription();

    expect(modalService.create).toHaveBeenCalledWith(
      jasmine.objectContaining({
        nzContent: DealComponent,
        nzViewContainerRef: jasmine.any(ViewContainerRef),
        nzComponentParams: { data: '', title: 'Deal' },
        nzFooter: null,
        nzKeyboard: true,
        nzWidth: '70%',
      })
    );
  });

  it('should open SubscriptionPlan when openSubscriptionPlan is called', () => {
    const modal = { afterClose: of(null) };
    modalService.create.and.returnValue(modal as any);

    component.openSubscriptionPlan();

    expect(modalService.create).toHaveBeenCalledWith(
      jasmine.objectContaining({
        nzContent: SubscriptionPlanComponent,
        nzComponentParams: {
          fromSubscriptionPlan: true,
          showFreePlan: true,
        },
        nzViewContainerRef: jasmine.any(ViewContainerRef),
        nzFooter: null,
        nzKeyboard: true,
        nzWidth: '80%',
      })
    );
  });

  it('should open PaymentMethod when openPaymentMethod is called', () => {
    const modal = { afterClose: of(null) };
    modalService.create.and.returnValue(modal as any);

    const paymentProfile = { id: 1 };
    component.openPaymentMethod(null);

    expect(modalService.create).toHaveBeenCalledWith(
      jasmine.objectContaining({
        nzContent: PaymentModalComponent,
        nzViewContainerRef: jasmine.any(ViewContainerRef),
        nzFooter: null,
        nzKeyboard: true,
        nzWidth: '70%',
        // nzComponentParams: {
        //   paymentProfileData: {}
        // },
      })
    );
  });

  it('should get all payment profiles', () => {
    const mockResponse = {
      status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      data: [{ id: 1 }, { id: 2 }],
    };
    subscriptionService.getAllPaymentProfiles.and.returnValue(of(mockResponse));

    component.getAllPaymentProfiles();

    expect(subscriptionService.getAllPaymentProfiles).toHaveBeenCalled();
    expect(component.paymentProfiles).toEqual(mockResponse.data);
  });

  it('should handle error when getting payment profiles fails', () => {
    subscriptionService.getAllPaymentProfiles.and.returnValue(
      throwError('error')
    );

    component.getAllPaymentProfiles();

    expect(subscriptionService.getAllPaymentProfiles).toHaveBeenCalled();
    expect(component.paymentProfiles).toEqual([]);
  });

  it('should get billing history', () => {
    const mockResponse = {
      status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      data: [{ date: '2021-01-01' }, { date: '2021-02-01' }],
    };
    subscriptionService.getBillingHistory.and.returnValue(of(mockResponse));

    component.getBillingHistory();

    expect(subscriptionService.getBillingHistory).toHaveBeenCalledWith(
      component.payLoad
    );
    expect(component.billingHistory).toEqual(mockResponse.data);
  });

  it('should format date correctly', () => {
    const date = '2021-01-01';
    const formattedDate = component.formatDate(date);
    expect(formattedDate).toBe('01/01/2021');
  });

  it('should navigate to transaction invoice on openInvoice call', () => {
    const transId = '123';
    component.openInvoice(transId);
    expect(router.navigate).toHaveBeenCalledWith(['transaction-invoice'], {
      queryParams: { transId },
    });
  });

  it('should open PaymentMethod and initialize cvv property correctly', () => {
    const modal = { afterClose: of(null) };
    modalService.create.and.returnValue(modal as any);

    const paymentProfile = { id: 1 };
    component.openPaymentMethod(paymentProfile);

    expect(modalService.create).toHaveBeenCalledWith(
      jasmine.objectContaining({
        nzContent: PaymentModalComponent,
        nzViewContainerRef: jasmine.any(ViewContainerRef),
        nzFooter: null,
        nzKeyboard: true,
        nzWidth: '70%',
        nzComponentParams: {
          paymentProfileData: { id: 1, cvv: '' },
        },
      })
    );
  });

  it('should handle error when fetching payment profiles', () => {
    subscriptionService.getAllPaymentProfiles.and.returnValue(
      throwError('error')
    );
    component.getAllPaymentProfiles();

    expect(subscriptionService.getAllPaymentProfiles).toHaveBeenCalled();
    expect(component.paymentProfiles).toEqual([]);
  });

  it('should get billing history', () => {
    const mockResponse = {
      status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      data: [{ date: '2021-01-01' }, { date: '2021-02-01' }],
    };
  
    subscriptionService.getBillingHistory.and.returnValue(of(mockResponse)); // Mock observable return
  
    component.getBillingHistory();
  
    expect(subscriptionService.getBillingHistory).toHaveBeenCalledWith(component.payLoad);
    expect(component.billingHistory).toEqual(mockResponse.data);
  });
});
