// import {
//   ComponentFixture,
//   TestBed,
//   fakeAsync,
//   tick,
// } from '@angular/core/testing';
// import { Router } from '@angular/router';
// import { ActivatedRoute } from '@angular/router';
// import { of, throwError } from 'rxjs';
// import { WithdrawalSubscriptionComponent } from './withdrawal-subscription.component';
// import { AuthService } from 'src/app/core/services/auth.service';
// import { CacheService } from 'src/app/core/services/cache.service';
// import { MessageService } from 'src/app/core/services/message.service';
// import { HttpConstants } from 'src/app/core/constants/http.constants';
// import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
// import { RouterTestingModule } from '@angular/router/testing';
// import { SocialAuthService } from '@abacritt/angularx-social-login';

// describe('WithdrawalSubscriptionComponent', () => {
//   let component: WithdrawalSubscriptionComponent;
//   let fixture: ComponentFixture<WithdrawalSubscriptionComponent>;
//   let mockAuthService: jasmine.SpyObj<AuthService>;
//   let mockMessageService: jasmine.SpyObj<MessageService>;
//   let mockActivatedRoute: jasmine.SpyObj<ActivatedRoute>;
//   let mockRouter: jasmine.SpyObj<Router>;

//   beforeEach(async () => {
//     const spy = jasmine.createSpyObj('SocialAuthService', ['signIn'], {
//       authState: of(null),
//     });

//     const authServiceSpy = jasmine.createSpyObj('AuthService', [
//       'cancelPaypalSubscription',
//     ]);
//     const messageServiceSpy = jasmine.createSpyObj('MessageService', ['error']);
//     const activatedRouteSpy = jasmine.createSpyObj('ActivatedRoute', [
//       'snapshot',
//     ]);
//     const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

//     await TestBed.configureTestingModule({
//       imports: [RouterTestingModule],
//       declarations: [WithdrawalSubscriptionComponent],
//       providers: [
//         { provide: AuthService, useValue: authServiceSpy },
//         { provide: MessageService, useValue: messageServiceSpy },
//         { provide: ActivatedRoute, useValue: activatedRouteSpy },
//         { provide: Router, useValue: routerSpy },
//         { provide: SocialAuthService, useValue: spy },
//       ],
//       schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
//     }).compileComponents();

//     fixture = TestBed.createComponent(WithdrawalSubscriptionComponent);
//     component = fixture.componentInstance;
//     mockAuthService = TestBed.inject(
//       AuthService
//     ) as jasmine.SpyObj<AuthService>;
//     mockMessageService = TestBed.inject(
//       MessageService
//     ) as jasmine.SpyObj<MessageService>;
//     mockActivatedRoute = TestBed.inject(
//       ActivatedRoute
//     ) as jasmine.SpyObj<ActivatedRoute>;
//     mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;
//   });

//   it('should initialize and cancel subscription if subscription_id is present', () => {
//     const subscriptionId = 'test-subscription-id';
//     mockActivatedRoute.snapshot.queryParams = {
//       subscription_id: subscriptionId,
//     };

//     const cancelSpy = mockAuthService.cancelPaypalSubscription.and.returnValue(
//       of({ status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE })
//     );
//     component.ngOnInit();

//     expect(cancelSpy).toHaveBeenCalledWith(subscriptionId);
//   });

//   it('should show error message on successful subscription cancellation', () => {
//     const subscriptionId = 'test-subscription-id';
//     mockActivatedRoute.snapshot.queryParams = {
//       subscription_id: subscriptionId,
//     };
//     mockAuthService.cancelPaypalSubscription.and.returnValue(
//       of({ status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE })
//     );

//     component.getPaypalSubscritionIdFromRoute();

//     expect(mockMessageService.error).toHaveBeenCalledWith(
//       'Subscription Withdrawal.'
//     );
//   });

//   it('should log error on subscription cancellation failure', () => {
//     const subscriptionId = 'test-subscription-id';
//     mockActivatedRoute.snapshot.queryParams = {
//       subscription_id: subscriptionId,
//     };
//     const errorResponse = new Error('Cancellation failed');
//     mockAuthService.cancelPaypalSubscription.and.returnValue(
//       throwError(() => errorResponse)
//     );

//     spyOn(console, 'log'); // Spy on console.log to check if it's called
//     component.getPaypalSubscritionIdFromRoute();

//     expect(console.log).toHaveBeenCalledWith(errorResponse);
//   });

//   it('should navigate to sign-in route on routeBack call', () => {
//     component.routeBack();
//     expect(mockRouter.navigate).toHaveBeenCalledWith(['auth/sign-in']);
//   });

//   it('should navigate to sign-in route correctly', () => {
//     component.routeBack();
//     expect(mockRouter.navigate).toHaveBeenCalledWith(['auth/sign-in']);
//   });
//   it('should call cancelPaypalSubscription with correct parameters', () => {
//     const subscriptionId = 'test-subscription-id';
//     mockActivatedRoute.snapshot.queryParams = {
//       subscription_id: subscriptionId,
//     };
//     const cancelSpy = mockAuthService.cancelPaypalSubscription.and.returnValue(
//       of({ status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE })
//     );

//     component.ngOnInit();

//     expect(cancelSpy).toHaveBeenCalledWith(subscriptionId);
//   });
// });

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { WithdrawalSubscriptionComponent } from './withdrawal-subscription.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { SocialAuthService } from '@abacritt/angularx-social-login';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';

describe('WithdrawalSubscriptionComponent', () => {
  let component: WithdrawalSubscriptionComponent;
  let fixture: ComponentFixture<WithdrawalSubscriptionComponent>;
  let authService: AuthService;
  let messageService: MessageService;
  let activatedRoute: ActivatedRoute;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('SocialAuthService', ['signIn'], {
            authState: of(null),
          });
    await TestBed.configureTestingModule({
      declarations: [WithdrawalSubscriptionComponent],
      imports: [RouterTestingModule, HttpClientTestingModule, AntDesignModule ],
      providers: [
        CacheService,
        {
          provide: AuthService,
          useValue: jasmine.createSpyObj('AuthService', [
            'cancelPaypalSubscription',
          ]),
        },
        {
          provide: MessageService,
          useValue: jasmine.createSpyObj('MessageService', ['error']),
        },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParams: { subscription_id: '123' } } },
        },
        { provide: SocialAuthService, useValue: spy },
        NzMessageService,
      ],
      schemas : [NO_ERRORS_SCHEMA , CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WithdrawalSubscriptionComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    messageService = TestBed.inject(MessageService);
    activatedRoute = TestBed.inject(ActivatedRoute);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should retrieve PayPal subscription ID from route on init', () => {
    component.ngOnInit();
    expect(component.paypalSubscriptionId).toBe('123');
  });

  it('should call cancelOrWithDrawSubscription with subscription ID on init if ID exists', () => {
    spyOn(component, 'cancelOrWithDrawSubscription');
    component.ngOnInit();
    expect(component.cancelOrWithDrawSubscription).toHaveBeenCalledWith('123');
  });

  it('should handle successful subscription cancellation', () => {
    const response = { status: '200' };
    (authService.cancelPaypalSubscription as jasmine.Spy).and.returnValue(
      of(response)
    );
    component.cancelOrWithDrawSubscription('123');
    expect(messageService.error).toHaveBeenCalledWith(
      'Subscription Withdrawal.'
    );
  });

  it('should handle failed subscription cancellation', () => {
    const error = new Error('Error');
    (authService.cancelPaypalSubscription as jasmine.Spy).and.returnValue(
      throwError(() => error)
    );
    spyOn(console, 'log');
    component.cancelOrWithDrawSubscription('123');
    expect(console.log).toHaveBeenCalledWith(error);
  });

  it('should navigate to the sign-in route on routeBack', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    component.routeBack();
    expect(router.navigate).toHaveBeenCalledWith(['auth/sign-in']);
  });
  
});
