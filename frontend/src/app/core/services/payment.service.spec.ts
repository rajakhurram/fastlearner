import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { PaymentService } from './payment.service';
import { environment } from 'src/environments/environment.development';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('PaymentService', () => {
  let service: PaymentService;
  let httpMock: HttpTestingController;

  const mockResponse = { data: 'sample data' };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PaymentService],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    });
    service = TestBed.inject(PaymentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('createStripeAccount', () => {
    it('should send a GET request to create a Stripe account', () => {
      service.createStripeAccount().subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${environment.baseUrl}stripe-account/`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('fetchStripeAccountDetails', () => {
    it('should send a GET request to fetch Stripe account details', () => {
      service.fetchStripeAccountDetails().subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(
        `${environment.baseUrl}stripe-account/detail`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('deleteStripeAccount', () => {
    it('should send a DELETE request to delete a Stripe account', () => {
      service.deleteStripeAccount().subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${environment.baseUrl}stripe-account/`);
      expect(req.request.method).toBe('DELETE');
      req.flush(mockResponse);
    });
  });

  describe('withdrawBalance', () => {
    it('should send a POST request to withdraw balance with bankName and amount as query params', () => {
      const withdrawAmount = 100;
      const bankName = 'Test Bank';

      service
        .withdrawBalance(withdrawAmount, bankName)
        .subscribe((response) => {
          expect(response).toEqual(mockResponse);
        });

      const req = httpMock.expectOne(
        `${environment.baseUrl}stripe-account/?bankName=${bankName}&amount=${withdrawAmount}`
      );
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });

    it('should send a POST request without query params if no arguments are provided', () => {
      service.withdrawBalance().subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(
        `${environment.baseUrl}stripe-account/?bankName=undefined&amount=undefined`
      );
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });
  });

  describe('fetchTransactionHistory', () => {
    it('should send a GET request with pagination parameters', () => {
      const payLoad = { pageNo: 1, pageSize: 10 };

      service.fetchTransactionHistory(payLoad).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(
        `${environment.baseUrl}stripe-account/history?pageNo=${payLoad.pageNo}&pageSize=${payLoad.pageSize}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle undefined payload gracefully', () => {
      service.fetchTransactionHistory().subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(
        `${environment.baseUrl}stripe-account/history?pageNo=undefined&pageSize=undefined`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });
});
