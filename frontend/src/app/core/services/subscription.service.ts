import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';
import { PaymentProfile } from '../models/payment-profile.model';
import { CheckoutData, Subscription } from '../models/subscription.model';
import { CacheService } from './cache.service';

@Injectable({
  providedIn: 'root',
})
export class SubscriptionService {
  constructor(private _http: HttpClient, private _cacheService: CacheService) {}

  private permissionsSubject = new BehaviorSubject<any[]>([]);
  permissions$ = this.permissionsSubject.asObservable();

  public getSavedPaymentProfile(): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}authorizenet/payment-profile/saved`
    );
  }

  public savePaymentProfile(paymentProfile?: PaymentProfile): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}authorizenet/payment-profile/create`,
      paymentProfile
    );
  }

  public addSubscription(subscription?: Subscription): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}authorizenet/create-subscription`,
      subscription
    );
  }

  getDisscount(coupon: string, couponType: string, id?: number) {
    let params = new HttpParams()
      .set('coupon', coupon)
      .set('couponType', couponType);
  
    if (id) {
      if (couponType === 'SUBSCRIPTION') {
        params = params.set('subscriptionId', id.toString());
      } else if (couponType === 'PREMIUM') {
        params = params.set('courseId', id.toString());
      }
    }
  
    return this._http.get(`${environment.baseUrl}coupon/validate`, { params });
  }
  
  

  public courseCheckout(checkoutData: CheckoutData): Observable<any> {
    return this._http.post(`${environment.baseUrl}checkout/`, checkoutData);
  }

  public getAllPaymentProfiles(): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}authorizenet/payment-profile/`
    );
  }

  public paymentDefault(id?: any): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}authorizenet/update-subscription?paymentProfileId=${id}`,
      null
    );
  }

  public removePaymentProfile(id?: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}authorizenet/payment-profile/${id}`
    );
  }

  public getBillingHistory(payLoad?: any): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}authorizenet/history/`,
      payLoad
    );
  }

  public getPurchasedHistory(payLoad?: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}purhcased-course/?pageNo=${payLoad?.pageNo}&pageSize=${payLoad?.pageSize}`
    );
  }

  public getPurchasedCourseInvoice(courseId?: any) {
    return this._http.get(
      `${environment.baseUrl}purhcased-course/download?courseId=${courseId}`,
      {
        responseType: 'blob',
      }
    );
  }

  public getBillingHistoryByUser(payLoad?: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}transaction-history/get?pageNo=${payLoad?.pageNo}&pageSize=${payLoad?.pageSize}`
    );
  }

  public getSubscriptionById(subscribedId?: any): Observable<any> {
    return this._http.get(`${environment.baseUrl}subscription/${subscribedId}`);
  }

  public getInvoiceByTransId(transId?: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}authorizenet/history/detail?transactionId=${transId}`
    );
  }

  public getTransactionHistoryByTransId(transId?: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}transaction-history/get-by-id?transactionId=${transId}`,
    {
      responseType: 'blob',
    }
  );
  }
  public updateUserSubscriptionCheck(isSubscribe?: boolean) {
    let loggedInUserDetails = JSON.parse(
      this._cacheService.getDataFromCache('loggedInUserDetails') ?? ''
    );
    loggedInUserDetails.subscribed = isSubscribe;
    this._cacheService.saveInCache(
      'loggedInUserDetails',
      JSON.stringify(loggedInUserDetails)
    );
  }

  public getTokenAgainstSessionId(sessionId?: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}user-session/generate-token?sessionId=${sessionId}`
    );
  }

  // public getCurrentSubscriptionPermission(): Observable<any> {
  //   return this._http.get(
  //     `${environment.baseUrl}subscription/current-subscription`
  //   );
  // }

  public loadSubscriptionPermissions(): void {
    this._http
      .get(`${environment.baseUrl}subscription/current-subscription`)
      .subscribe({
        next: (response: any) => {
          if (response?.status === 200) {
            if (
              response?.data?.permissions &&
              response?.data?.permissions?.length > 0
            ) {
              this._cacheService.saveInCache(
                'permissions',
                JSON.stringify(response?.data?.permissions)
              );
            } else {
              this._cacheService.removeFromCache('permissions');
            }
          }
        },
        error: (error) => {},
      });
  }
}
