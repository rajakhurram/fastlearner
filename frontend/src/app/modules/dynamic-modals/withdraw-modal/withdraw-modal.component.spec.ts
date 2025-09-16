import { ComponentFixture, TestBed } from '@angular/core/testing';
import { WithdrawModalComponent } from './withdraw-modal.component';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { PaymentService } from 'src/app/core/services/payment.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('WithdrawModalComponent', () => {
  let component: WithdrawModalComponent;
  let fixture: ComponentFixture<WithdrawModalComponent>;
  let mockPaymentService: jasmine.SpyObj<PaymentService>;
  let mockModalRef: jasmine.SpyObj<NzModalRef>;
  
  beforeEach(async () => {
    const paymentServiceSpy = jasmine.createSpyObj('PaymentService', ['withdrawBalance']);
    const modalRefSpy = jasmine.createSpyObj('NzModalRef', ['close']);

    await TestBed.configureTestingModule({
      declarations: [ WithdrawModalComponent ],
      providers: [
        { provide: PaymentService, useValue: paymentServiceSpy },
        { provide: NzModalRef, useValue: modalRefSpy },
        HttpConstants
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(WithdrawModalComponent);
    component = fixture.componentInstance;
    mockPaymentService = TestBed.inject(PaymentService) as jasmine.SpyObj<PaymentService>;
    mockModalRef = TestBed.inject(NzModalRef) as jasmine.SpyObj<NzModalRef>;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should initialize availableBalance with the balance input', () => {
      component.balance = 100;
      component.ngOnInit();
      expect(component.availableBalance).toBe(100);
    });
  });

  describe('withdrawBalance', () => {
    it('should call withdrawBalance service and close modal on success', () => {
      component.withDrawAmount = 50;
      component.bankName = 'Test Bank';
      const mockResponse = { status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE };
      mockPaymentService.withdrawBalance.and.returnValue(of(mockResponse));
      
      component.withdrawBalance();

      expect(mockPaymentService.withdrawBalance).toHaveBeenCalledWith(50, 'Test Bank');
      expect(mockModalRef.close).toHaveBeenCalled();
    });

    it('should not close modal if withdrawBalance service fails', () => {
      component.withDrawAmount = 50;
      component.bankName = 'Test Bank';
      mockPaymentService.withdrawBalance.and.returnValue(throwError('Error'));

      component.withdrawBalance();

      expect(mockPaymentService.withdrawBalance).toHaveBeenCalledWith(50, 'Test Bank');
      expect(mockModalRef.close).not.toHaveBeenCalled();
    });
  });

  describe('onlyInputNumber', () => {
    it('should prevent non-numeric characters except backspace', () => {
      const event = new KeyboardEvent('keydown', { key: 'a' });
      spyOn(event, 'preventDefault');
      component.onlyInputNumber(event);
      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('should allow numeric characters and backspace', () => {
      const event1 = new KeyboardEvent('keydown', { key: '5' });
      const event2 = new KeyboardEvent('keydown', { key: 'Backspace' });
      spyOn(event1, 'preventDefault');
      spyOn(event2, 'preventDefault');
      
      component.onlyInputNumber(event1);
      component.onlyInputNumber(event2);
      
      expect(event1.preventDefault).not.toHaveBeenCalled();
      expect(event2.preventDefault).not.toHaveBeenCalled();
    });
  });

  describe('validateAmount', () => {
    it('should disable the withdraw button for invalid amounts', () => {
      component.balance = 100;
      component.withDrawAmount = 150;

      const event = new KeyboardEvent('keydown', { key: '5' });
      component.validateAmount(event);

      expect(component.withdrawBtnDisable).toBeTrue();
    });

    it('should enable the withdraw button for valid amounts and update available balance', () => {
      component.balance = 100;
      component.withDrawAmount = 50;

      const event = new KeyboardEvent('keydown', { key: '5' });
      component.validateAmount(event);

      expect(component.withdrawBtnDisable).toBeFalse();
      expect(component.availableBalance).toBe(50);
    });

    it('should reset withdraw amount if less than zero', () => {
      component.withDrawAmount = -10;

      const event = new KeyboardEvent('keydown', { key: '5' });
      component.validateAmount(event);

      expect(component.withDrawAmount).toBeNull();
    });
  });

  describe('close', () => {
    it('should close the modal', () => {
      component.close();
      expect(mockModalRef.close).toHaveBeenCalled();
    });
  });
});
