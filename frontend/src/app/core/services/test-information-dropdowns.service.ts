import { HttpBackend, HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import { environment } from 'src/environments/environment.development';
import { Observable, forkJoin } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class TestInformationDropdownsService {
  private _httpBackend: HttpClient;

  constructor(
    private _http: HttpClient,
    private _authService: AuthService,
    handler: HttpBackend
  ) {
    this._httpBackend = new HttpClient(handler);
  }

  private getCategory(): Observable<any> {
    if (this._authService.isLoggedIn()) {
      return this._http.get(`${environment.baseUrl}course-category/`);
    } else {
      return this._httpBackend.get(`${environment.baseUrl}course-category/`);
    }
  }

  private getLevels(): Observable<any> {
    return this._http.get(`${environment.baseUrl}course-level/`);
  }

  getDropdownData(): Observable<{ categories: any; levels: any }> {
    return forkJoin({
      categories: this.getCategory(),
      levels: this.getLevels(),
    });
  }
}
