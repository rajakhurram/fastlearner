import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  constructor(private http: HttpClient) {}

  currentPage: number = 1;

  private buildParams(queryParams: Record<string, any>): HttpParams {
    let params = new HttpParams();
    Object.entries(queryParams || {}).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        params = params.set(key, String(value));
      }
    });
    return params;
  }

  getUserStats(): Observable<any> {
    return this.http.get(`${environment.baseUrl}super-admin/users/stats`);
  }

  getUsersList(queryParams: any): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}super-admin/users?search=${queryParams.search}&planType=${queryParams.planType}&subscriptionStatus=${queryParams.subscriptionStatus}&accountStatus=${queryParams.accountStatus}&dateFrom=${queryParams.dateFrom}&dateTo=${queryParams.dateTo}&page=${queryParams.page}&size=${queryParams.size}`,
    );
  }

  getUserOverview(rawId: any): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}super-admin/users/${rawId}/overview`,
    );
  }

  getCourseCategory(): Observable<any> {
    return this.http.get(`${environment.baseUrl}course-category/`);
  }

  getSubscriptionOverview(rawId: any): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}super-admin/users/${rawId}/subscriptions`,
    );
  }

  getCourseOverview(rawId: any): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}super-admin/users/${rawId}/courses`,
    );
  }

  getTransactionOverview(rawId: any): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}super-admin/users/${rawId}/transactions`,
    );
  }

  getSubscriptionStats(): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}super-admin/subscriptions/stats`,
    );
  }

  // getSubscriptionsList(queryParams: any): Observable<any> {
  //   return this.http.get(
  //     `${environment.baseUrl}super-admin/subscriptions?search=${queryParams.search}&planType=${queryParams.planType}&status=${queryParams.status}&billingCycle=${queryParams.cycle}&dateFrom=${queryParams.dateFrom}&dateTo=${queryParams.dateTo}&page=${queryParams.page}&size=${queryParams.size}`,
  //   );
  // }

  getSubscriptionsList(queryParams: any): Observable<any> {
    const params = new HttpParams()
      .set('search', queryParams.search ?? '')
      .set('planType', queryParams.planType ?? '')
      .set('status', queryParams.status ?? '')
      .set('billingCycle', queryParams.billingCycle ?? '')
      .set('dateFrom', queryParams.dateFrom ?? '')
      .set('dateTo', queryParams.dateTo ?? '')
      .set('page', queryParams.page ?? 0)
      .set('size', queryParams.size ?? 10);

    return this.http.get(`${environment.baseUrl}super-admin/subscriptions`, {
      params,
    });
  }

  getCoursesList(queryParams: any): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}super-admin/courses?search=${queryParams.search}&status=${queryParams.status}&category=${queryParams.category}&instructor=${queryParams.instructor}&page=${queryParams.page}&size=${queryParams.size}`,
    );
  }

  getInstructorsList(): Observable<any> {
    return this.http.get(`${environment.baseUrl}super-admin/instructors`);
  }

  getCourseDetails(rawId: any): Observable<any> {
    return this.http.get(`${environment.baseUrl}super-admin/courses/${rawId}`);
  }

  getEnrollmentsList(queryParams: any): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}super-admin/enrollments?search=${queryParams.search}&courseId=${queryParams.courseId}&status=${queryParams.status}&progress=${queryParams.progress}&page=${queryParams.page}&size=${queryParams.size}`,
    );
  }

  getPaymentList(queryParams: any): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}super-admin/payments?search=${queryParams.search}&planType=${queryParams.planType}&status=${queryParams.status}&type=${queryParams.type}&dateFrom=${queryParams.dateFrom}&dateTo=${queryParams.dateTo}&page=${queryParams.page}&size=${queryParams.size}`,
    );
  }

  getInvoicesList(subscriptionId: any, queryParams: any): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}super-admin/subscriptions/${subscriptionId}/invoices?page=${queryParams.page}&size=${queryParams.size}&search=${queryParams.search}&type=${queryParams.type}&planType=${queryParams.planType}&status=${queryParams.status}&dateFrom=${queryParams.dateFrom}&dateTo=${queryParams.dateTo}`,
    );
  }

  downloadInvoice(invoiceId: any): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}super-admin/invoices/${invoiceId}/pdf`,
      { responseType: 'blob' },
    );
  }

  sendInvoice(invoiceId: any): Observable<any> {
    return this.http.post(
      `${environment.baseUrl}super-admin/invoices/${invoiceId}/sendViaEmail`,
      {},
    );
  }

  getInvoiceDetailById(invoiceId: any): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}super-admin/invoices/${invoiceId}`,
    );
  }

  getPayoutStats(): Observable<any> {
    return this.http.get(`${environment.baseUrl}super-admin/payouts/stats`);
  }

  getPayoutsList(queryParams: any): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}super-admin/payouts?search=${queryParams.search}&status=${queryParams.status}&dateFrom=${queryParams.dateFrom}&dateTo=${queryParams.dateTo}&page=${queryParams.page}&size=${queryParams.size}`,
    );
  }

  getPayoutDetails(instructorId: any): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}super-admin/payouts/${instructorId}`,
    );
  }

  getSettingsAdmins(): Observable<any> {
    return this.http.get(`${environment.baseUrl}super-admin/settings/admins`);
  }

  getCouponsList(queryParams: any): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}coupon/?page=${queryParams.page}&size=${queryParams.size}&search=${queryParams.search || ''}&isActive=${queryParams.isActive}`,
    );
  }

  createCoupon(data: any): Observable<any> {
    return this.http.post(`${environment.baseUrl}coupon/`, data);
  }

  getCoupenSubscription(cycle: string): Observable<any> {
    return this.http.get(`${environment.baseUrl}subscription/cycle/${cycle}`);
  }

  getCoupenById(id: string | number): Observable<any> {
    return this.http.get(`${environment.baseUrl}coupon/${id}`);
  }

  updateCoupon(data: any): Observable<any> {
    return this.http.put(`${environment.baseUrl}coupon/`, data);
  }

  deleteCoupon(id: string | number): Observable<any> {
    return this.http.delete(`${environment.baseUrl}coupon/?id=${id}`);
  }

  toggleCouponStatus(id: string | number): Observable<any> {
    return this.http.patch(
      `${environment.baseUrl}coupon/${id}/toggle-status`,
      {},
    );
  }

  downloadPaymentCSV(queryParams: any): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}super-admin/payments/export?search=${queryParams.search}&planType=${queryParams.planType}&status=${queryParams.status}&type=${queryParams.type}&dateFrom=${queryParams.dateFrom}&dateTo=${queryParams.dateTo}`,
      { responseType: 'text' },
    );
  }

  coursePublish(courseId: any): Observable<any> {
    return this.http.patch(
      `${environment.baseUrl}super-admin/courses/${courseId}/publish`,
      {},
    );
  }

  courseUnpublish(courseId: any): Observable<any> {
    return this.http.patch(
      `${environment.baseUrl}super-admin/courses/${courseId}/unpublish`,
      {},
    );
  }

  addAdmin(data: any): Observable<any> {
    return this.http.post(
      `${environment.baseUrl}super-admin/settings/admins`,
      data,
    );
  }

  inviteAdmin(data: any): Observable<any> {
    return this.http.post(
      `${environment.baseUrl}super-admin/users/invite?email=${data.email}&link=${data.link}`,
      {},
    );
  }

  deActivateAdmin(adminId: any): Observable<any> {
    return this.http.patch(
      `${environment.baseUrl}super-admin/settings/admins/${adminId}/deactivate`,
      {},
    );
  }

  exportUserCsv(queryParams: any): Observable<any> {
    return this.http.get(
      `${environment.baseUrl}super-admin/users/exportCsv?search=${queryParams.search}&planType=${queryParams.planType}&subscriptionStatus=${queryParams.subscriptionStatus}&accountStatus=${queryParams.accountStatus}&dateFrom=${queryParams.dateFrom}&dateTo=${queryParams.dateTo}`,
      { responseType: 'blob' },
    );
  }
}
