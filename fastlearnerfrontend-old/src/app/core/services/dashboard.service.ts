import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';
import { CourseStatus } from '../enums/course-status';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  constructor( private _http: HttpClient) { }

  public getDashboardStats(dashboardStatsParam?: any):Observable<any>{
    return this._http.get(`${environment.baseUrl}dashboard/stats?filterBy=${dashboardStatsParam}`)
  }

  public getMyCourses(body: any): Observable<any> {
    return this._http.get(`${environment.baseUrl}course/course-by-teacher?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}&searchInput=${body?.searchInput}&sort=${body?.sort}`)
  }

  public changeCourseStatus(courseId?: number, courseStatus?: CourseStatus): Observable<any> {
    return this._http.post(`${environment.baseUrl}course/course-status?courseId=${courseId}&courseStatus=${courseStatus.toString()}`, null);
  }
}
