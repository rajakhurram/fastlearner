import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { TransactionInvoiceComponent } from './transaction-invoice.component';
import { SubscriptionService } from 'src/app/core/services/subscription.service';
import { NzMessageService } from 'ng-zorro-antd/message';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('TransactionInvoiceComponent', () => {
  let component: TransactionInvoiceComponent;
  let fixture: ComponentFixture<TransactionInvoiceComponent>;
  let subscriptionService: jasmine.SpyObj<SubscriptionService>;
  let messageService: jasmine.SpyObj<NzMessageService>;
  let activatedRoute: ActivatedRoute;

  const mockTransactionHistory = {
    id: 1,
    userId: '456',
    planId: '789',
    transactionId: '12345',
    amount: 100,
    status: 'SUCCESS',
    createdAt: '2024-08-08T00:00:00Z',
  };

  beforeEach(async () => {
    const subscriptionServiceSpy = jasmine.createSpyObj('SubscriptionService', [
      'getTransactionHistoryByTransId',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('NzMessageService', [
      'success',
      'error',
    ]);
    const activatedRouteStub = { queryParams: of({ transId: '12345' }) };

    await TestBed.configureTestingModule({
      imports: [RouterTestingModule, HttpClientTestingModule],
      declarations: [TransactionInvoiceComponent],
      providers: [
        { provide: SubscriptionService, useValue: subscriptionServiceSpy },
        { provide: NzMessageService, useValue: messageServiceSpy },
        { provide: ActivatedRoute, useValue: activatedRouteStub },
      ],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    subscriptionService = TestBed.inject(
      SubscriptionService
    ) as jasmine.SpyObj<SubscriptionService>;
    messageService = TestBed.inject(
      NzMessageService
    ) as jasmine.SpyObj<NzMessageService>;
    activatedRoute = TestBed.inject(ActivatedRoute);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TransactionInvoiceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    subscriptionService.getTransactionHistoryByTransId.and.returnValue(
      of({
        status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
        data: mockTransactionHistory,
      })
    );
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize component with transId from queryParams', () => {
    expect(component.transId).toBe('12345');
  });

  it('should call getInvoiceData on initialization', () => {
    spyOn(component, 'getInvoiceData').and.callThrough(); // Potential duplicate spy
    component.ngOnInit();
    expect(component.getInvoiceData).toHaveBeenCalled();
  });

  it('should set transactionHistory correctly on successful data fetch', fakeAsync(() => {
    subscriptionService.getTransactionHistoryByTransId.and.returnValue(
      of({
        status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
        data: mockTransactionHistory,
      })
    );

    component.getInvoiceData();
    tick();

    expect(component.transactionHistory).toBeDefined();
    expect(component.transactionHistory?.id).toBe(1);
  }));

  it('should format date correctly', () => {
    const date = new Date('2024-08-08T00:00:00Z');
    const formattedDate = component.formatDate(date);
    expect(formattedDate).toBe('08/08/2024');
  });
});
