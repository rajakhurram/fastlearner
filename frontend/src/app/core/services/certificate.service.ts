import { HttpClient } from '@angular/common/http';
import { Token } from '@angular/compiler';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { environment } from 'src/environments/environment.development';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class CertificateService {
  constructor(private _http: HttpClient, private _authService: AuthService) {}

  public getCertificateData(courseId: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}certificate/generate/${courseId}`
    );
  }

  public getCertificateUrl(courseId: any, isDownloadable: any): string {
    const token = this._authService.getAccessToken();
    return `${environment.baseUrl}certificate/download?courseId=${courseId}&isDownloadable=${isDownloadable}&token=${token}`;
  }

  public verifyCertificate(uuid: string): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}certificate/verify/to/${uuid}`
    );
  }
}
