import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { PaymentConfirmationComponent } from './payment-confirmation.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { MessageService } from 'src/app/core/services/message.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AnimationOptions } from 'ngx-lottie';
import { AnimationItem } from 'lottie-web';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('PaymentConfirmationComponent', () => {
  let component: PaymentConfirmationComponent;
  let fixture: ComponentFixture<PaymentConfirmationComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let router: jasmine.SpyObj<Router>;
  let activatedRoute: jasmine.SpyObj<ActivatedRoute>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'completeSubscription',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', [
      'success',
    ]);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const activatedRouteSpy = jasmine.createSpyObj('ActivatedRoute', [], {
      snapshot: { queryParams: { subscription_id: 'test-subscription-id' } },
    });

    await TestBed.configureTestingModule({
      declarations: [PaymentConfirmationComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: activatedRouteSpy },
        HttpConstants,
      ],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(PaymentConfirmationComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    activatedRoute = TestBed.inject(
      ActivatedRoute
    ) as jasmine.SpyObj<ActivatedRoute>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get PayPal subscription ID from route and call paymentConfirmCompleteSubscription', () => {
    spyOn(component, 'paymentConfirmCompleteSubscription');

    component.ngOnInit();

    expect(component.paypalSubscriptionId).toBe('test-subscription-id');
    expect(component.paymentConfirmCompleteSubscription).toHaveBeenCalledWith(
      'test-subscription-id'
    );
  });

  it('should handle successful subscription completion', () => {
    authService.completeSubscription.and.returnValue(of({ status: 200 }));

    component.paymentConfirmCompleteSubscription('test-subscription-id');

    expect(messageService.success).toHaveBeenCalledWith(
      'Subscription Successfully Subscribed. Welcome!'
    );
  });

  it('should handle failed subscription completion', () => {
    authService.completeSubscription.and.returnValue(
      throwError(() => new Error('Failed to complete subscription'))
    );

    component.paymentConfirmCompleteSubscription('test-subscription-id');

    // No messageService call expected on error; you can add error handling logic if needed
    expect(messageService.success).not.toHaveBeenCalled();
  });

  it('should navigate to student landing page on continue', () => {
    component.continue();

    expect(router.navigate).toHaveBeenCalledWith(['student']);
  });

  it('should call routeToStudentLandingPage on continue', () => {
    spyOn(component, 'routeToStudentLandingPage');

    component.continue();

    expect(component.routeToStudentLandingPage).toHaveBeenCalled();
  });

  it('should initialize component properties', () => {
    expect(component.paypalSubscriptionId).toBe('test-subscription-id');
    expect(component.options).toEqual({
      path: '../../../../assets/animations/check Mark.json',
    });
    expect(component.styling).toEqual({ height: '100px' });
  });
});
