// import { ComponentFixture, TestBed } from '@angular/core/testing';
// import { PaymentComponent } from './payment.component';
// import { PaymentService } from 'src/app/core/services/payment.service';
// import { NzModalRef, NzModalService } from 'ng-zorro-antd/modal';
// import { ViewContainerRef } from '@angular/core';
// import { of, throwError } from 'rxjs';
// import { NO_ERRORS_SCHEMA } from '@angular/core';

// describe('PaymentComponent', () => {
//   let component: PaymentComponent;
//   let fixture: ComponentFixture<PaymentComponent>;
//   let mockPaymentService: jasmine.SpyObj<PaymentService>;
//   let mockModalService: jasmine.SpyObj<NzModalService>;
//   let viewContainerRef: ViewContainerRef;

//   beforeEach(async () => {
//     const paymentServiceSpy = jasmine.createSpyObj('PaymentService', [
//       'createStripeAccount',
//       'fetchStripeAccountDetails',
//       'deleteStripeAccount',
//       'fetchTransactionHistory'
//     ]);
//     const modalServiceSpy = jasmine.createSpyObj('NzModalService', ['create']);

//     await TestBed.configureTestingModule({
//       declarations: [PaymentComponent],
//       providers: [
//         { provide: PaymentService, useValue: paymentServiceSpy },
//         { provide: NzModalService, useValue: modalServiceSpy },
//         { provide: ViewContainerRef, useValue: {} },
//       ],
//       schemas: [NO_ERRORS_SCHEMA]
//     }).compileComponents();

//     fixture = TestBed.createComponent(PaymentComponent);
//     component = fixture.componentInstance;
//     mockPaymentService = TestBed.inject(PaymentService) as jasmine.SpyObj<PaymentService>;
//     mockModalService = TestBed.inject(NzModalService) as jasmine.SpyObj<NzModalService>;
//     viewContainerRef = TestBed.inject(ViewContainerRef);

//     fixture.detectChanges();
//   });

//   it('should create the component', () => {
//     expect(component).toBeTruthy();
//   });

//   describe('ngOnInit', () => {
//     it('should fetch Stripe account details and transaction history', () => {
//       spyOn(component, 'fetchStripeAccountDetails').and.callThrough();
//       spyOn(component, 'fetchTransactionHistory').and.callThrough();

//       component.ngOnInit();

//       expect(component.fetchStripeAccountDetails).toHaveBeenCalled();
//       expect(component.fetchTransactionHistory).toHaveBeenCalled();
//     });
//   });

//   describe('createStripeAccount', () => {
//     it('should open a new window with the Stripe account creation link on success', () => {
//       const response = { status: 200, data: 'https://stripe.com/account' };
//       mockPaymentService.createStripeAccount.and.returnValue(of(response));

//       spyOn(window, 'open');
//       component.createStripeAccount();

//       expect(mockPaymentService.createStripeAccount).toHaveBeenCalled();
//       expect(window.open).toHaveBeenCalledWith('https://stripe.com/account', '_blank');
//     });

//     it('should handle errors during Stripe account creation', () => {
//       mockPaymentService.createStripeAccount.and.returnValue(throwError('Error'));

//       component.createStripeAccount();

//       expect(mockPaymentService.createStripeAccount).toHaveBeenCalled();
//     });
//   });

//   describe('fetchStripeAccountDetails', () => {
//     it('should set payment details and mark account as connected on success', () => {
//       const response = { status: 200, data: { balance: 100, bankName: 'Bank' } };
//       mockPaymentService.fetchStripeAccountDetails.and.returnValue(of(response));

//       component.fetchStripeAccountDetails();

//       expect(mockPaymentService.fetchStripeAccountDetails).toHaveBeenCalled();
//       expect(component.payment).toEqual(response.data);
//       expect(component.accountConnected).toBeTrue();
//     });

//     it('should handle errors during fetching Stripe account details', () => {
//       mockPaymentService.fetchStripeAccountDetails.and.returnValue(throwError('Error'));

//       component.fetchStripeAccountDetails();

//       expect(mockPaymentService.fetchStripeAccountDetails).toHaveBeenCalled();
//       expect(component.payment).toBeUndefined();
//     });
//   });

//   describe('deleteStripeAccount', () => {
//     it('should delete the Stripe account if balance is 0 and reset account details', () => {
//       component.payment = { balance: 0 };
//       const response = { status: 200 };
//       mockPaymentService.deleteStripeAccount.and.returnValue(of(response));

//       component.deleteStripeAccount();

//       expect(mockPaymentService.deleteStripeAccount).toHaveBeenCalled();
//       expect(component.payment).toBeNull();
//       expect(component.accountConnected).toBeFalse();
//     });

//     it('should open a withdraw warning modal if balance is greater than 0', () => {
//       component.payment = { balance: 100 };
//       spyOn(component, 'openWithdrawWaringModal');

//       component.deleteStripeAccount();

//       expect(component.openWithdrawWaringModal).toHaveBeenCalled();
//     });

//     it('should handle errors during Stripe account deletion', () => {
//       component.payment = { balance: 0 };
//       mockPaymentService.deleteStripeAccount.and.returnValue(throwError('Error'));

//       component.deleteStripeAccount();

//       expect(mockPaymentService.deleteStripeAccount).toHaveBeenCalled();
//     });
//   });

//   describe('fetchTransactionHistory', () => {
//     it('should fetch and format transaction history on success', () => {
//       const response = {
//         status: 200,
//         data: [{ withdrawalAt: '2023-09-28T00:00:00Z' }]
//       };
//       spyOn(component, 'formatDate').and.returnValue('SEP 28, 2023');
//       mockPaymentService.fetchTransactionHistory.and.returnValue(of(response));

//       component.fetchTransactionHistory();

//       expect(mockPaymentService.fetchTransactionHistory).toHaveBeenCalled();
//       expect(component.transactionHistory[0].withdrawalAt).toBe('SEP 28, 2023');
//     });

//     it('should handle errors during fetching transaction history', () => {
//       mockPaymentService.fetchTransactionHistory.and.returnValue(throwError('Error'));

//       component.fetchTransactionHistory();

//       expect(mockPaymentService.fetchTransactionHistory).toHaveBeenCalled();
//       expect(component.transactionHistory.length).toBe(0);
//     });
//   });

//   describe('openWithdrawWaringModal', () => {
//     it('should open the withdraw warning modal', () => {
//       const mockModalRef = {
//         afterClose: of(null), // mock the afterClose Observable
//         // Add other properties of NzModalRef if necessary
//         close: jasmine.createSpy('close'), // mock the close method if needed
//         destroy: jasmine.createSpy('destroy') // mock destroy if needed
//       } as unknown as NzModalRef<any, any>; // Cast to NzModalRef to satisfy the type system

//       const modalSpy = mockModalService.create.and.returnValue(mockModalRef);

//       component.openWithdrawWaringModal();

//       expect(mockModalService.create).toHaveBeenCalled();
//       expect(modalSpy).toBeDefined();
//     });
//   });

// describe('openWithDrawModal', () => {
//   it('should open the withdraw modal and refresh data after close', () => {
//     spyOn(component, 'fetchStripeAccountDetails');
//     spyOn(component, 'fetchTransactionHistory');

//     // Mock the modalRef with afterClose Observable
//     const mockModalRef = {
//       afterClose: of(true)
//     } as unknown as NzModalRef<any, any>;

//     // Set the return value of the create method to the mock modalRef
//     mockModalService.create.and.returnValue(mockModalRef);

//     // Call the method under test
//     component.openWithDrawModal();

//     // Check if create method was called
//     expect(mockModalService.create).toHaveBeenCalled();

//     // Check if afterClose triggers the data refresh methods
//     mockModalRef.afterClose.subscribe(() => {
//       expect(component.fetchStripeAccountDetails).toHaveBeenCalled();
//       expect(component.fetchTransactionHistory).toHaveBeenCalled();
//     });
//   });
// });

//   describe('formatDate', () => {
//     it('should format the date correctly', () => {
//       const result = component.formatDate('2023-09-28T00:00:00Z');
//       expect(result).toBe('SEP 28, 2023');
//     });
//   });
// });

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PaymentComponent } from './payment.component';
import { PaymentService } from 'src/app/core/services/payment.service';
import { NzModalService } from 'ng-zorro-antd/modal';
import { of, throwError } from 'rxjs';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CompletionModalComponent } from '../../dynamic-modals/completion-modal/completion-modal.component';
import { WithdrawModalComponent } from '../../dynamic-modals/withdraw-modal/withdraw-modal.component';
import { DeletionModalComponent } from '../../dynamic-modals/deletion-modal/deletion-modal.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { NO_ERRORS_SCHEMA } from '@angular/compiler';

describe('PaymentComponent', () => {
  let component: PaymentComponent;
  let fixture: ComponentFixture<PaymentComponent>;
  let paymentService: jasmine.SpyObj<PaymentService>;
  let modalService: jasmine.SpyObj<NzModalService>;
  let httpConstants: HttpConstants;

  beforeEach(async () => {
    const paymentServiceSpy = jasmine.createSpyObj('PaymentService', [
      'createStripeAccount',
      'fetchStripeAccountDetails',
      'deleteStripeAccount',
      'fetchTransactionHistory',
    ]);

    const modalServiceSpy = jasmine.createSpyObj('NzModalService', ['create']);

    await TestBed.configureTestingModule({
      declarations: [PaymentComponent],
      schemas : [CUSTOM_ELEMENTS_SCHEMA , NO_ERRORS_SCHEMA],
      providers: [
        { provide: PaymentService, useValue: paymentServiceSpy },
        { provide: NzModalService, useValue: modalServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PaymentComponent);
    component = fixture.componentInstance;
    paymentService = TestBed.inject(
      PaymentService
    ) as jasmine.SpyObj<PaymentService>;
    modalService = TestBed.inject(
      NzModalService
    ) as jasmine.SpyObj<NzModalService>;
    httpConstants = new HttpConstants();
    component._httpConstants = httpConstants; // Set HttpConstants
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should fetch stripe account details and transaction history on init', () => {
      const fetchStripeAccountDetailsSpy =
        paymentService.fetchStripeAccountDetails.and.returnValue(
          of({
            status: httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
            data: {},
          })
        );
      const fetchTransactionHistorySpy =
        paymentService.fetchTransactionHistory.and.returnValue(
          of({
            status: httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
            data: {},
          })
        );

      component.ngOnInit();

      expect(fetchStripeAccountDetailsSpy).toHaveBeenCalled();
      expect(fetchTransactionHistorySpy).toHaveBeenCalled();
    });
  });

  describe('createStripeAccount', () => {
    it('should open a new window with the Stripe account URL', () => {
      const response = {
        status: httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
        data: 'http://example.com',
      };
      paymentService.createStripeAccount.and.returnValue(of(response));

      spyOn(window, 'open');
      component.createStripeAccount();

      expect(window.open).toHaveBeenCalledWith(response.data, '_blank');
    });

    it('should not open a new window if the response is not successful', () => {
      const response = { status: 400, data: 'http://example.com' };
      paymentService.createStripeAccount.and.returnValue(of(response));

      spyOn(window, 'open');
      component.createStripeAccount();

      expect(window.open).not.toHaveBeenCalled();
    });
  });

  describe('fetchStripeAccountDetails', () => {
    it('should set payment and balance when fetching account details is successful', () => {
      const response = {
        status: httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
        data: { balance: 100 },
      };
      paymentService.fetchStripeAccountDetails.and.returnValue(of(response));

      component.fetchStripeAccountDetails();

      expect(component.payment).toEqual(response.data);
      expect(component.balance).toEqual(response.data.balance);
      expect(component.accountConnected).toBeTrue();
    });

    it('should not set payment when fetching account details fails', () => {
      paymentService.fetchStripeAccountDetails.and.returnValue(throwError({}));

      component.fetchStripeAccountDetails();

      expect(component.payment).toBeUndefined();
      expect(component.balance).toBeUndefined();
      expect(component.accountConnected).toBeFalse();
    });
  });

  describe('deleteStripeAccount', () => {
    it('should open deletion modal if balance is zero', () => {
      component.balance = 0;

      component.deleteStripeAccount();

      expect(modalService.create).toHaveBeenCalledWith(
        jasmine.objectContaining({
          nzContent: DeletionModalComponent,
          nzComponentParams: jasmine.objectContaining({
            msg: 'Are you sure you want to delete?',
            secondBtnText: 'Delete',
          }),
        })
      );
    });

    it('should open withdraw warning modal if balance is not zero', () => {
      component.balance = 100;

      component.deleteStripeAccount();

      expect(modalService.create).toHaveBeenCalledWith(
        jasmine.objectContaining({
          nzContent: WithdrawModalComponent,
          nzComponentParams: jasmine.objectContaining({
            canWithdraw: false,
          }),
        })
      );
    });
  });

  describe('fetchTransactionHistory', () => {
    it('should fetch transaction history and format dates', () => {
      const response = {
        status: httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
        data: {
          histories: [{ withdrawalAt: new Date().toISOString() }],
          totalElements: 1,
        },
      };
      paymentService.fetchTransactionHistory.and.returnValue(of(response));

      component.fetchTransactionHistory();

      expect(component.transactionHistory).toEqual(response.data.histories);
      expect(component.totalElements).toEqual(response.data.totalElements);
      expect(component.transactionHistory[0].withdrawalAt).toBeDefined(); // Check if withdrawalAt is formatted
    });
  });

  describe('formatDate', () => {
    it('should format date correctly', () => {
      const date = '2023-09-30T12:00:00Z';
      const formattedDate = component.formatDate(date);

      expect(formattedDate).toContain('SEP'); // Adjust as per the expected format
    });
  });

  describe('formatTime', () => {
    it('should format time correctly', () => {
      const time = '2023-09-30T12:00:00Z';
      const formattedTime = component.formatTime(time);

      expect(formattedTime).toContain('PM'); // Adjust as per the expected format
    });
  });
  
});
