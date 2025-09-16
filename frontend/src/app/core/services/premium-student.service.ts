// premium-students.service.ts
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';

@Injectable({
  providedIn: 'root'
})
export class PremiumStudentsService {

  constructor(private _http: HttpClient) { }

  public getPremiumStudents(payLoad?: any): Observable<any> {
    let params = new HttpParams()
        .set('pageNo', payLoad?.pageNo)
        .set('pageSize', payLoad?.pageSize);
    
    if (payLoad?.searchValue) {
        params = params.set('search', payLoad?.searchValue);
    }
    if (payLoad?.startDate) {
        params = params.set('startDate', payLoad?.startDate)
    }
    if (payLoad?.endDate) {
      params = params.set('endDate', payLoad?.endDate)
    }

    return this._http.get(`${environment.baseUrl}premium-students/`, { params });
}

public getPremiumStudentsByDate(startDate: string, endDate: string, pageNo: number = 0, pageSize: number = 10): Observable<any> {
    let params = new HttpParams()
        .set('startDate', startDate) 
        .set('endDate', endDate) 
        .set('pageNo', pageNo.toString())
        .set('pageSize', pageSize.toString());
    
    return this._http.get(`${environment.baseUrl}premium-students/by-date`, { params });
}

  downloadExcel(payLoad?: any): Observable<Blob> {
    let params = new HttpParams()
    .set('pageNo', payLoad?.pageNo)
    .set('pageSize', payLoad?.pageSize);

    if (payLoad?.searchValue) {
      params = params.set('search', payLoad?.searchValue);
    }
    if (payLoad?.startDate) {
      params = params.set('startDate', payLoad?.startDate);
    }
    if (payLoad?.endDate) {
      params = params.set('endDate', payLoad?.endDate);
    }
    return this._http.get(`${environment.baseUrl}premium-students/export/premium-students`, {
      responseType: 'blob',
      params: params
    });
  }
}
