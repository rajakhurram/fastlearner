import { HttpClient, HttpBackend, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { AuthService } from "./auth.service";
import { Observable } from "rxjs";
import { environment } from "src/environments/environment.development";

@Injectable({
    providedIn: 'root'
  })
  export class FileManager {
    private _httpBackend: HttpClient;

  constructor(
    private _http: HttpClient,
    private _authService : AuthService,
    handler: HttpBackend
  ) 
  {
    this._httpBackend = new HttpClient(handler)
  }


  public uploadFile(file: any, fileType?: string): Observable<any>{
    const formData = new FormData();
    formData.append('file', file);
    formData.append('fileType', fileType);
    let url = environment.baseUrl+'uploader/';
    return this._http.post(url,formData);
  }

  public deleteFile(id?: any, url?: any, topicId?: any, fileType?: string): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    const body = {
      id: id,
      url: url,
      topicId: topicId,
      fileType: fileType
    };
  
    return this._http.delete(`${environment.baseUrl}uploader/`, { headers: headers, body: body });
  }

  public regenerateSummary(url: any,  fileType?: string): Observable<any>{
    const formData = new FormData();
    formData.append('fileType', fileType);
    formData.append('url', url);
    return this._http.post(`${environment.baseUrl}uploader/regenerate`,formData);
  }

}