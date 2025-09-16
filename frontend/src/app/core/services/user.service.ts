import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(
    private _http: HttpClient
  ) { }

  public getUserProfile(): Observable<any> {
    return this._http.get(`${environment.baseUrl}user-profile/`);
  }

  public updateUserProfile(body: any): Observable<any> {
    return this._http.post(`${environment.baseUrl}user-profile/update`,body);
  }

  public changeUserPassword(body: any): Observable<any> {
    return this._http.post(`${environment.baseUrl}user/change-password`,body);
  }
}
