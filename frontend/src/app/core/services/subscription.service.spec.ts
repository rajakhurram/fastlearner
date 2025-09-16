import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpHandler, HttpBackend } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { SubscriptionService } from './subscription.service';
import { PaymentProfile } from '../models/payment-profile.model';
import { Subscription } from '../models/subscription.model';
import { environment } from 'src/environments/environment.development';
import { SocialAuthService } from '@abacritt/angularx-social-login';

describe('SubscriptionService', () => {
  let service: SubscriptionService;
  let httpClientSpy: { get: jasmine.Spy; post: jasmine.Spy };

  beforeEach(() => {
    const spy = jasmine.createSpyObj('SocialAuthService', ['signIn'], {
      authState: of(null),
    });
    const spyHttpClient = jasmine.createSpyObj('HttpClient', ['get', 'post']);

    TestBed.configureTestingModule({
      providers: [
        SubscriptionService,
        { provide: HttpClient, useValue: spyHttpClient },
        HttpHandler,
        HttpBackend,
        { provide: SocialAuthService, useValue: spy },
      ],
    });

    service = TestBed.inject(SubscriptionService);
    httpClientSpy = TestBed.inject(HttpClient) as any;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return saved payment profile', () => {
    const expectedProfile = { id: 1, name: 'John Doe' };
    httpClientSpy.get.and.returnValue(of(expectedProfile));

    service.getSavedPaymentProfile().subscribe((profile) => {
      expect(profile).toEqual(expectedProfile);
    });
  });

  it('should handle error when getting saved payment profile', () => {
    const error = 'Error occurred';
    httpClientSpy.get.and.returnValue(throwError(() => new Error(error)));

    service.getSavedPaymentProfile().subscribe({
      next: () => fail('expected an error, not payment profile'),
      error: (err) => expect(err.message).toContain(error),
    });
  });

  it('should save payment profile', () => {
    const paymentProfile: PaymentProfile = { id: 1, firstName: 'John Doe' };
    const expectedResponse = { success: true };
    httpClientSpy.post.and.returnValue(of(expectedResponse));

    service.savePaymentProfile(paymentProfile).subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });
  });

  it('should handle error when saving payment profile', () => {
    const error = 'Error occurred';
    const paymentProfile: PaymentProfile = { id: 1, firstName: 'John Doe' };
    httpClientSpy.post.and.returnValue(throwError(() => new Error(error)));

    service.savePaymentProfile(paymentProfile).subscribe({
      next: () => fail('expected an error, not response'),
      error: (err) => expect(err.message).toContain(error),
    });
  });

  it('should add a subscription', () => {
    const subscription: Subscription = {
      subscriptionId: 1,
      paymentDetail: { id: 2 },
    };
    const expectedResponse = { success: true };
    httpClientSpy.post.and.returnValue(of(expectedResponse));

    service.addSubscription(subscription).subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });
  });

  it('should handle error when adding a subscription', () => {
    const error = 'Error occurred';
    const subscription: Subscription = {
      subscriptionId: 1,
      paymentDetail: { id: 2 },
    };
    httpClientSpy.post.and.returnValue(throwError(() => new Error(error)));

    service.addSubscription(subscription).subscribe({
      next: () => fail('expected an error, not response'),
      error: (err) => expect(err.message).toContain(error),
    });
  });

  it('should return all payment profiles', () => {
    const expectedProfiles = [{ id: 1, name: 'John Doe' }];
    httpClientSpy.get.and.returnValue(of(expectedProfiles));

    service.getAllPaymentProfiles().subscribe((profiles) => {
      expect(profiles).toEqual(expectedProfiles);
    });
  });

  it('should handle error when getting all payment profiles', () => {
    const error = 'Error occurred';
    httpClientSpy.get.and.returnValue(throwError(() => new Error(error)));

    service.getAllPaymentProfiles().subscribe({
      next: () => fail('expected an error, not payment profiles'),
      error: (err) => expect(err.message).toContain(error),
    });
  });

  it('should update payment profile as default', () => {
    const id = 1;
    const expectedResponse = { success: true };
    httpClientSpy.post.and.returnValue(of(expectedResponse));

    service.paymentDefault(id).subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });
  });

  it('should handle error when updating payment profile as default', () => {
    const error = 'Error occurred';
    const id = 1;
    httpClientSpy.post.and.returnValue(throwError(() => new Error(error)));

    service.paymentDefault(id).subscribe({
      next: () => fail('expected an error, not response'),
      error: (err) => expect(err.message).toContain(error),
    });
  });

  it('should remove a payment profile', () => {
    const id = 1;
    const expectedResponse = { success: true };
    httpClientSpy.get.and.returnValue(of(expectedResponse));

    service.removePaymentProfile(id).subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });
  });

  it('should handle error when removing a payment profile', () => {
    const error = 'Error occurred';
    const id = 1;
    httpClientSpy.get.and.returnValue(throwError(() => new Error(error)));

    service.removePaymentProfile(id).subscribe({
      next: () => fail('expected an error, not response'),
      error: (err) => expect(err.message).toContain(error),
    });
  });

  it('should return billing history', () => {
    const payload = { userId: 1 };
    const expectedHistory = [{ transactionId: 1, amount: 100 }];
    httpClientSpy.post.and.returnValue(of(expectedHistory));

    service.getBillingHistory(payload).subscribe((history) => {
      expect(history).toEqual(expectedHistory);
    });
  });

  it('should handle error when getting billing history', () => {
    const error = 'Error occurred';
    const payload = { userId: 1 };
    httpClientSpy.post.and.returnValue(throwError(() => new Error(error)));

    service.getBillingHistory(payload).subscribe({
      next: () => fail('expected an error, not billing history'),
      error: (err) => expect(err.message).toContain(error),
    });
  });

  it('should return subscription by id', () => {
    const subscribedId = 1;
    const expectedSubscription = { id: 1, plan: 'Basic' };
    httpClientSpy.get.and.returnValue(of(expectedSubscription));

    service.getSubscriptionById(subscribedId).subscribe((subscription) => {
      expect(subscription).toEqual(expectedSubscription);
    });
  });

  it('should handle error when getting subscription by id', () => {
    const error = 'Error occurred';
    const subscribedId = 1;
    httpClientSpy.get.and.returnValue(throwError(() => new Error(error)));

    service.getSubscriptionById(subscribedId).subscribe({
      next: () => fail('expected an error, not subscription'),
      error: (err) => expect(err.message).toContain(error),
    });
  });

  it('should return invoice by transaction id', () => {
    const transId = 1;
    const expectedInvoice = { transactionId: 1, amount: 100 };
    httpClientSpy.get.and.returnValue(of(expectedInvoice));

    service.getInvoiceByTransId(transId).subscribe((invoice) => {
      expect(invoice).toEqual(expectedInvoice);
    });
  });

  it('should handle error when getting invoice by transaction id', () => {
    const error = 'Error occurred';
    const transId = 1;
    httpClientSpy.get.and.returnValue(throwError(() => new Error(error)));

    service.getInvoiceByTransId(transId).subscribe({
      next: () => fail('expected an error, not invoice'),
      error: (err) => expect(err.message).toContain(error),
    });
  });
});
