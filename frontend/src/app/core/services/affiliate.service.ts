import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';

@Injectable({
  providedIn: 'root',
})
export class AffiliateService {
 
  constructor(private _http: HttpClient) {}

 public acive_InActiveAssignCourse(affiliateId?:any, instructorAffiliateId?: any, status?:any): Observable<any>  {
  return this._http.get(`${environment.baseUrl}affiliate-course/course-active-inactive?affiliateCourseId=${affiliateId}&instructorAffiliateId=${instructorAffiliateId}&status=${status}`);
  }
  public getAffiliates(body): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}affiliate/fetch?pageNo=${body?.pageNo}&pageSize=${
        body?.pageSize
      }&search=${body?.search ?? ''}`
    );
  }
  public getAffiliate(body): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}affiliate/detail?instructorAffiliateId=${body?.instructorAffliateId}`
    );
  }
  public getCourseByAffiliate(body): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}affiliate-course/?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}&affiliateId=${body?.instructorAffiliateId}`
    );
  }
  public getCoursesWithReward(body): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}instructor-affiliate/premium-courses-with-reward?affiliateId=${body?.affiliateId}`
    );
  }
  public getPremiumCoursesByInstructor(body): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}course/premium-courses?pageNo=${
        body?.pageNo
      }&pageSize=${body?.pageSize}&search=${body?.search ?? ''}`
    );
  }
  public getPremiumCoursesAffiliates(body, payload?: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}affiliate-course/by-course?courseId=${body?.courseId}&pageNo=${payload?.pageNo}&pageSize=${payload?.pageSize}`
    );
  }
  public createAffiliate(body): Observable<any> {
    return this._http.post(`${environment.baseUrl}affiliate/create`, body);
  }
  public assignAffiliateCourse(body): Observable<any> {
    return this._http.post(`${environment.baseUrl}affiliate-course/`, body);
  }
  public resendLink(body): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}affiliate/stripe-resend-link?email=${body}`,
      {}
    );
  }
  public editAffiliate(body): Observable<any> {
    return this._http.put(`${environment.baseUrl}affiliate/update`, body);
  }
  public deleteAffiliate(body): Observable<any> {
    return this._http.delete(
      `${environment.baseUrl}affiliate/delete?instructorAffiliateId=${body?.instructorAffiliateId}`
    );
  }
  public deleteAffiliateCourse(body): Observable<any> {
    return this._http.delete(
      `${environment.baseUrl}affiliate-course/?affiliateId=${body?.affiliateId}&affiliateCourseId=${body?.id}`
    );
  }
}
