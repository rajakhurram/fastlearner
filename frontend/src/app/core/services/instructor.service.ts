import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';

@Injectable({
  providedIn: 'root'
})
export class InstructorService {

  constructor(
    private _http: HttpClient
  ) { }

  public generator(input: any): Observable<any> {
    const url = `${environment.baseUrl}ai-generator/?input=${input}`;
    return this._http.post(url, null);
  }

  public getTopicTypes(): Observable<any> {
    const url = `${environment.baseUrl}topic-type/`;
    return this._http.get(url);
  }
}
