import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SubscriptionPlanComponent } from './subscription-plan.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { MessageService } from 'src/app/core/services/message.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { SubscriptionService } from 'src/app/core/services/subscription.service';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('SubscriptionPlanComponent', () => {
  let component: SubscriptionPlanComponent;
  let fixture: ComponentFixture<SubscriptionPlanComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let cacheService: jasmine.SpyObj<CacheService>;
  let subscriptionService: jasmine.SpyObj<SubscriptionService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthService', [
      'getSubscriptionPlans',
      'createSubscription',
    ]);
    const messageSpy = jasmine.createSpyObj('MessageService', [
      'success',
      'error',
    ]);
    const cacheSpy = jasmine.createSpyObj('CacheService', [
      'getDataFromCache',
      'removeFromCache',
      'saveInCache',
    ]);
    const subscriptionSpy = jasmine.createSpyObj('SubscriptionService', [
      'updateUserSubscriptionCheck',
    ]);
    const routerSpy = jasmine.createSpyObj('Router', [
      'navigate',
      'navigateByUrl',
    ]);

    await TestBed.configureTestingModule({
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
      declarations: [SubscriptionPlanComponent],
      providers: [
        { provide: AuthService, useValue: authSpy },
        { provide: MessageService, useValue: messageSpy },
        { provide: CacheService, useValue: cacheSpy },
        { provide: SubscriptionService, useValue: subscriptionSpy },
        { provide: Router, useValue: routerSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SubscriptionPlanComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
    cacheService = TestBed.inject(CacheService) as jasmine.SpyObj<CacheService>;
    subscriptionService = TestBed.inject(
      SubscriptionService
    ) as jasmine.SpyObj<SubscriptionService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should call getSubscriptionPlanList', () => {
      spyOn(component, 'getSubscriptionPlanList');
      component.ngOnInit();
      expect(component.getSubscriptionPlanList).toHaveBeenCalled();
    });
  });

  describe('ngOnDestroy', () => {
    it('should subscribe to Free Plan if no plan is selected', () => {
      spyOn(component, 'subscribeToPlan');
      component.isPlanSelected = false;
      component.fromSubscriptionPlan = false;

      component.ngOnDestroy();

      expect(component.subscribeToPlan).toHaveBeenCalledWith(
        { paypalPlanId: null, subscriptionId: 1 },
        'Free Plan'
      );
    });

    it('should not subscribe to Free Plan if a plan is selected', () => {
      spyOn(component, 'subscribeToPlan');
      component.isPlanSelected = true;

      component.ngOnDestroy();

      expect(component.subscribeToPlan).not.toHaveBeenCalled();
    });
  });

  describe('getSubscriptionPlanList', () => {
    it('should set subscriptionList on success', () => {
      const mockResponse = { status: 200, data: ['plan1', 'plan2'] };
      authService.getSubscriptionPlans.and.returnValue(of(mockResponse));

      component.getSubscriptionPlanList();

      expect(component.subscriptionList).toEqual(mockResponse.data);
      expect(component.noSubscriptionPresent).toBeFalse();
    });

    it('should handle error', () => {
      authService.getSubscriptionPlans.and.returnValue(throwError('error'));

      component.getSubscriptionPlanList();

      expect(component.noSubscriptionPresent).toBeTrue();
    });
  });

  describe('onSelectPlan', () => {
    it('should select the Free Plan and subscribe', () => {
      spyOn(component, 'subscribeToPlan');
      component.onSelectPlan('Free Plan', '1', 'paypalPlanId');

      expect(component.isPlanSelected).toBeTrue();
      expect(component.isFreePlanSelected).toBeTrue();
      expect(component.subscribeToPlan).toHaveBeenCalledWith(
        { paypalPlanId: 'paypalPlanId', subscriptionId: '1' },
        'Free Plan'
      );
    });

    it('should navigate to payment-method for paid plans', () => {
      component.onSelectPlan('Paid Plan', '1', 'paypalPlanId');

      expect(component.isPlanSelected).toBeTrue();
      expect(router.navigate).toHaveBeenCalledWith(['payment-method'], {
        queryParams: { subscriptionId: '1' },
      });
    });
  });

  describe('subscribeToPlan', () => {
    it('should subscribe to Free Plan and navigate on success', () => {
      const mockResponse = { status: 200, data: 'mock-url' };
      authService.createSubscription.and.returnValue(of(mockResponse));
      cacheService.getDataFromCache.and.returnValue(null);

      component.subscribeToPlan(
        { paypalPlanId: 'paypalPlanId', subscriptionId: '1' },
        'Free Plan'
      );

      expect(messageService.success).toHaveBeenCalledWith(
        'You are Successfully Subscribed To Free Plan'
      );
      expect(
        subscriptionService.updateUserSubscriptionCheck
      ).toHaveBeenCalledWith(true);
    });

    it('should handle error', () => {
      const mockError = { error: { status: 400, message: 'Bad Request' } };
      authService.createSubscription.and.returnValue(throwError(mockError));

      component.subscribeToPlan(
        { paypalPlanId: 'paypalPlanId', subscriptionId: '1' },
        'Free Plan'
      );

      expect(messageService.error).toHaveBeenCalledWith('Bad Request');
    });
  });
});
