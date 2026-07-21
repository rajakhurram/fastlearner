import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { environment } from "src/environments/environment.development";

@Injectable({
    providedIn: 'root'
})
export class PerformanceService {

    constructor(
        private _http: HttpClient
    ) { }

    public getCourseVisits(courseId?: any): Observable<any> {
        const url = courseId && courseId !== 0 ? `${environment.baseUrl}course-visitor/?courseId=${courseId}` : `${environment.baseUrl}course-visitor/`;
        return this._http.get(url);
    }
    
    public getRatingsAndReviews(payLoad?: any): Observable<any> {
        const url = payLoad.courseId && payLoad.courseId !== 0 ? `${environment.baseUrl}course-review/instructor?pageNo=${payLoad.pageNo}&pageSize=${payLoad.pageSize}&courseId=${payLoad.courseId}` : `${environment.baseUrl}course-review/instructor?pageNo=${payLoad.pageNo}&pageSize=${payLoad.pageSize}`;
        return this._http.get(url);
    }

    public getCourseNames(): Observable<any> {
        const url = `${environment.baseUrl}course/dropdown-for-performance`;
        return this._http.get(url);
    }

    public getActiveStudents(courseId?: any): Observable<any> {
        const url = courseId && courseId !== 0 ? `${environment.baseUrl}user-course-progress/active-students?courseId=${courseId}` : `${environment.baseUrl}user-course-progress/active-students`;
        return this._http.get(url);
    }

}