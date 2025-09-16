import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SubscriptionService } from 'src/app/core/services/subscription.service';
import { NzMessageService } from 'ng-zorro-antd/message';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AppConstants } from 'src/app/core/constants/app.constants';
import { ActivatedRoute } from '@angular/router';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { PaymentMethodComponent } from './payment-method.component';

describe('PaymentMethodComponent', () => {
  let component: PaymentMethodComponent;
  let fixture: ComponentFixture<PaymentMethodComponent>;
  let subscriptionService: jasmine.SpyObj<SubscriptionService>;
  let messageService: jasmine.SpyObj<NzMessageService>;
  let router: jasmine.SpyObj<Router>;
  let authService: jasmine.SpyObj<AuthService>;
  let cacheService: jasmine.SpyObj<CacheService>;
  let activatedRoute: jasmine.SpyObj<ActivatedRoute>;

  beforeEach(async () => {
    const subscriptionServiceSpy = jasmine.createSpyObj('SubscriptionService', [
      'getSavedPaymentProfile',
      'addSubscription',
      'getSubscriptionById',
      'updateUserSubscriptionCheck',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('NzMessageService', [
      'success',
      'error',
    ]);
    const routerSpy = jasmine.createSpyObj('Router', [
      'navigate',
      'navigateByUrl',
    ]);
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'createSubscription',
      'isSubscribed',
    ]);
    const cacheServiceSpy = jasmine.createSpyObj('CacheService', [
      'getDataFromCache',
      'removeFromCache',
    ]);
    const activatedRouteSpy = jasmine.createSpyObj('ActivatedRoute', [], {
      snapshot: { queryParams: { subscriptionId: '123' } },
    });

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, FormsModule, AntDesignModule],
      declarations: [PaymentMethodComponent],
      providers: [
        { provide: SubscriptionService, useValue: subscriptionServiceSpy },
        { provide: NzMessageService, useValue: messageServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: CacheService, useValue: cacheServiceSpy },
        { provide: ActivatedRoute, useValue: activatedRouteSpy },
        HttpConstants,
        AppConstants,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(PaymentMethodComponent);
    component = fixture.componentInstance;
    subscriptionService = TestBed.inject(
      SubscriptionService
    ) as jasmine.SpyObj<SubscriptionService>;
    messageService = TestBed.inject(
      NzMessageService
    ) as jasmine.SpyObj<NzMessageService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    cacheService = TestBed.inject(CacheService) as jasmine.SpyObj<CacheService>;
    activatedRoute = TestBed.inject(
      ActivatedRoute
    ) as jasmine.SpyObj<ActivatedRoute>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should retrieve saved payment profile successfully', () => {
    const mockResponse = {
      status: 200,
      data: {
        expiryMonth: '12',
        expiryYear: '25',
        cardNumber: '4111111111111111',
        cvv: '123',
        firstName: 'John',
        lastName: 'Doe',
      },
    };
    subscriptionService.getSavedPaymentProfile.and.returnValue(
      of(mockResponse)
    );

    component.getSavedPaymentProfile();

    expect(component.paymentProfile).toEqual(mockResponse.data);
    expect(component.paymentProfile.date).toBe('12/25');
  });

  it('should handle error while retrieving saved payment profile', () => {
    subscriptionService.getSavedPaymentProfile.and.returnValue(
      throwError(() => new Error('Failed to retrieve payment profile'))
    );

    component.getSavedPaymentProfile();

    expect(component.paymentProfile).toEqual({});
  });

  it('should create subscription successfully', () => {
    component.validateData = jasmine.createSpy().and.returnValue(true);
    const mockResponse = {
      status: 200,
      data: {
        /* mock subscription data */
      },
    };
    subscriptionService.addSubscription.and.returnValue(of(mockResponse));

    cacheService.getDataFromCache.and.returnValue(null);

    component.addSubscription();

    expect(subscriptionService.addSubscription).toHaveBeenCalled();
    expect(component.isPlanSelected).toBeTrue();
  });

  it('should handle error while creating subscription', () => {
    // Spy on validateData method to ensure it returns true
    spyOn(component, 'validateData').and.returnValue(true);

    // Spy on addSubscription method to simulate an error response
    subscriptionService.addSubscription.and.returnValue(
      throwError(() => new Error('Failed to create subscription'))
    );

    // Call the method to test
    component.addSubscription();

    // Check if isPlanSelected is false after handling error
    expect(component.isPlanSelected).toBeFalse();

    // Verify validateData was called
    expect(component.validateData).toHaveBeenCalled();
  });

  it('should handle invalid data and not call addSubscription on invalid data', () => {
    // Spy on validateData method to return false
    spyOn(component, 'validateData').and.returnValue(false);

    // Call the method to test
    component.addSubscription();

    expect(component.isPlanSelected).toBeFalse();
    expect(subscriptionService.addSubscription).not.toHaveBeenCalled();
  });

  it('should retrieve subscription by ID successfully', () => {
    const mockResponse = {
      data: {
        /* mock subscription data */
      },
    };
    subscriptionService.getSubscriptionById.and.returnValue(of(mockResponse));

    component.getSubscriptionById();

    expect(component.subscriptionData).toEqual(mockResponse.data);
  });

  it('should handle error while retrieving subscription by ID', () => {
    subscriptionService.getSubscriptionById.and.returnValue(
      throwError(() => new Error('Failed to retrieve subscription'))
    );

    component.getSubscriptionById();

    expect(component.subscriptionData).toEqual({});
  });

  it('should validate form data correctly', () => {
    component.paymentProfile = {
      firstName: 'John',
      lastName: 'Doe',
      cardNumber: '4111111111111111',
      date: '12/25',
      cvv: '123',
    };

    expect(component.validateData()).toBeTrue();
  });

  it('should validate form data incorrectly for invalid input', () => {
    component.paymentProfile = {
      firstName: '',
      lastName: 'Doe',
      cardNumber: '4111111111111111',
      date: '12/25',
      cvv: '',
    };

    expect(component.validateData()).toBeFalse();
  });

  it('should format card number correctly', () => {
    component.paymentProfile.cardNumber = '4111 1111 1111 1111';
    component.checkInput(new Event('input'));

    expect(component.paymentProfile.cardNumber).toBe('4111 1111 1111 1111');
  });

  it('should not allow non-numeric characters in expiry date input', fakeAsync(() => {
    const event = {
      target: { value: '12/34' },
      preventDefault: jasmine.createSpy(),
    };
    component.onExpiryDateKeyDown(event as any);
    tick();

    expect(component.paymentProfile.date).toBeUndefined();
  }));

  it('should allow valid expiry date input', fakeAsync(() => {
    const event = {
      target: { value: '12/25' },
      preventDefault: jasmine.createSpy(),
    };
    component.onExpiryDateKeyDown(event as any);
    tick();

    expect(component.paymentProfile.date).toBe;
  }));

  it('should not allow special characters in card number input', fakeAsync(() => {
    component.paymentProfile.cardNumber = '4111 1111 1111 1111';
    component.checkInput(component.paymentProfile.cardNumber);
    tick();

    expect(component.paymentProfile.cardNumber).toBe('4111 1111 1111 1111');
  }));

  it('should handle empty payment profile on form submission', () => {
    component.paymentProfile = {
      firstName: '',
      lastName: '',
      cardNumber: '',
      date: '',
      cvv: '',
    };

    spyOn(component, 'validateData').and.returnValue(false);

    component.addSubscription();

    expect(component.isPlanSelected).toBeFalse();
    expect(subscriptionService.addSubscription).not.toHaveBeenCalled();
  });

  it('should format card number correctly with extra spaces', () => {
    component.paymentProfile.cardNumber = '4111    1111    1111    1111';
    component.checkInput(new Event('input'));

    expect(component.paymentProfile.cardNumber).toBe('4111 1111 1111 1111');
  });

  it('should handle card number with incorrect length', () => {
    component.paymentProfile.cardNumber = '4111 1111 1111';
    component.checkInput(new Event('input'));

    expect(component.paymentProfile.cardNumber).toBe('4111 1111 1111');
  });

  it('should retrieve payment profile on component initialization', () => {
    const mockResponse = {
      status: 200,
      data: {
        expiryMonth: '12',
        expiryYear: '25',
        cardNumber: '4111111111111111',
        cvv: '123',
        firstName: 'John',
        lastName: 'Doe',
      },
    };
    subscriptionService.getSavedPaymentProfile.and.returnValue(
      of(mockResponse)
    );

    component.ngOnInit();

    expect(component.paymentProfile).toEqual(mockResponse.data);
    expect(component.paymentProfile.date).toBe('12/25');
  });

  it('should correctly retrieve subscriptionId from ActivatedRoute', () => {
    expect(component.subscriptionId).toBe('123');
  });
});
