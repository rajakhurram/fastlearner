import { HttpClient, HttpBackend } from '@angular/common/http';
import { Injectable, Injector } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';

@Injectable({
  providedIn: 'root',
})
export class PaymentService {
  constructor(private _http: HttpClient, handler: HttpBackend) {}

  public createStripeAccount(): Observable<any> {
    const url = `${environment.baseUrl}stripe-account/`;
    return this._http.get(url);
  }

  public fetchStripeAccountDetails(): Observable<any> {
    const url = `${environment.baseUrl}stripe-account/detail`;
    return this._http.get(url);
  }

  public deleteStripeAccount(): Observable<any> {
    const url = `${environment.baseUrl}stripe-account/`;
    return this._http.delete(url);
  }

  public withdrawBalance(
    withDrawAmount?: any,
    bankName?: any
  ): Observable<any> {
    const url = `${environment.baseUrl}stripe-account/?bankName=${bankName}&amount=${withDrawAmount}`;
    return this._http.post(url, null);
  }

  public fetchTransactionHistory(payLoad?: any): Observable<any> {
    const url = `${environment.baseUrl}stripe-account/history?pageNo=${payLoad?.pageNo}&pageSize=${payLoad?.pageSize}`;
    return this._http.get(url);
  }
}
