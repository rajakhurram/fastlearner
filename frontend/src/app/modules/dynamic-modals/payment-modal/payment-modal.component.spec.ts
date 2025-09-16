import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { PaymentModalComponent } from './payment-modal.component';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { SubscriptionService } from 'src/app/core/services/subscription.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { PaymentProfile } from 'src/app/core/models/payment-profile.model';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

describe('PaymentModalComponent', () => {
  let component: PaymentModalComponent;
  let fixture: ComponentFixture<PaymentModalComponent>;
  let modalService: jasmine.SpyObj<NzModalService>;
  let messageService: jasmine.SpyObj<NzMessageService>;
  let subscriptionService: jasmine.SpyObj<SubscriptionService>;

  beforeEach(async () => {
    const modalServiceSpy = jasmine.createSpyObj('NzModalService', [
      'closeAll',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('NzMessageService', [
      'error',
    ]);
    const subscriptionServiceSpy = jasmine.createSpyObj('SubscriptionService', [
      'savePaymentProfile',
    ]);

    await TestBed.configureTestingModule({
      declarations: [PaymentModalComponent],
      providers: [
        { provide: NzModalService, useValue: modalServiceSpy },
        { provide: NzMessageService, useValue: messageServiceSpy },
        { provide: SubscriptionService, useValue: subscriptionServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParams: {} } },
        },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(PaymentModalComponent);
    component = fixture.componentInstance;
    modalService = TestBed.inject(
      NzModalService
    ) as jasmine.SpyObj<NzModalService>;
    messageService = TestBed.inject(
      NzMessageService
    ) as jasmine.SpyObj<NzMessageService>;
    subscriptionService = TestBed.inject(
      SubscriptionService
    ) as jasmine.SpyObj<SubscriptionService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize payment profile with data', () => {
    const mockPaymentProfile = {
      firstName: 'John',
      lastName: 'Doe',
      cardNumber: '4111111111111111',
      expiryMonth: '12',
      expiryYear: '2025',
      cvv: '123',
    };
    component.paymentProfileData = mockPaymentProfile;
    component.ngOnInit();

    expect(component.paymentProfile).toEqual(mockPaymentProfile);
    expect(component.paymentProfile.date).toBe('12/2025');
  });
  
  it('should return current year in two digits', () => {
    const currentYearTwoDigits = component.getCurrentYearTwoDigits();
    const expectedYear = Number(new Date().getFullYear().toString().slice(-2));
    expect(currentYearTwoDigits).toBe(expectedYear);
  });

  it('should allow only numeric digits in key up event', () => {
    const event: any = { which: 50 };
    expect(component.allowNumericDigitsOnlyOnKeyUp(event)).toBeTrue();

    event.which = 65;
    expect(component.allowNumericDigitsOnlyOnKeyUp(event)).toBeFalse();
  });

  it('should handle expiry date key down event', () => {
    const event: any = { target: { value: '123025' }, preventDefault: jasmine.createSpy('preventDefault') };
    spyOn(component, 'formatExpiryDate').and.returnValue('12/2025');
    spyOn(component, 'getCurrentYearTwoDigits').and.returnValue(25);
  
    component.onExpiryDateKeyDown(event);
    
    // Check if formatExpiryDate was called and date was formatted correctly
    expect(component.formatExpiryDate).toHaveBeenCalledWith('123025');
    expect(component.paymentProfile.date).toBe('12/2025');
  });
  

  it('should not allow paste event', () => {
    const event: any = { preventDefault: jasmine.createSpy('preventDefault') };

    component.onPaste(event);

    expect(event.preventDefault).toHaveBeenCalled();
  });

  it('should validate data correctly', () => {
    component.paymentProfile = {
      firstName: 'John',
      lastName: 'Doe',
      cardNumber: '4111111111111111',
      expiryMonth: '12',
      expiryYear: '2025',
      cvv: '123',
    };

    expect(component.validateData()).toBeTrue();

    component.paymentProfile.firstName = '';
    expect(component.validateData()).toBeFalse();
  });

  it('should check input format for card number', () => {
    component.paymentProfile = { cardNumber: '4111111111111111' };
    component.checkInput(new Event('input'));

    expect(component.paymentProfile.cardNumber).toBe('4111 1111 1111 1111');
  });

  it('should save payment profile successfully', fakeAsync(() => {
    const mockResponse = { status: '200' };
    const paymentProfile = {
      firstName: 'John',
      lastName: 'Doe',
      cardNumber: '4111111111111111',
      expiryMonth: '12',
      expiryYear: '2025',
      cvv: '123'
    };
    
    // Set the component's paymentProfile
    component.paymentProfile = paymentProfile;
    
    // Mock the service response
    subscriptionService.savePaymentProfile.and.returnValue(of(mockResponse));
  
    // Call the method
    component.savePaymentProfile();
    tick(); // Simulate the passage of time for async operations
  
    // Assert the service was called
    expect(subscriptionService.savePaymentProfile).toHaveBeenCalledWith(paymentProfile);
    expect(modalService.closeAll).toHaveBeenCalled();
  }));
  

  it('should handle save payment profile error', fakeAsync(() => {
    const mockError = new Error('Error');
    const paymentProfile = {
      firstName: 'John',
      lastName: 'Doe',
      cardNumber: '4111111111111111',
      expiryMonth: '12',
      expiryYear: '2025',
      cvv: '123'
    };
  
    // Set the component's paymentProfile
    component.paymentProfile = paymentProfile;
  
    // Mock the service to throw an error
    subscriptionService.savePaymentProfile.and.returnValue(throwError(mockError));
  
    // Call the method
    component.savePaymentProfile();
    tick(); // Simulate the passage of time for async operations
  
    // Assert the service was called
    expect(subscriptionService.savePaymentProfile).toHaveBeenCalledWith(paymentProfile);
  }));
  

  it('should close modal', () => {
    component.closeModal();

    expect(modalService.closeAll).toHaveBeenCalled();
  });
});
