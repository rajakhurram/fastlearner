import { Injectable } from "@angular/core";
import { CookieService } from "ngx-cookie-service";

@Injectable({
  providedIn: 'root',
})
export class CookiesService {

  constructor(
    private _cookieService: CookieService
  ) {}

  setToken(token: string) {
    this._cookieService.set('token', token);
  }

  setRefreshToken(refreshToken: string) {
    this._cookieService.set('refreshToken', refreshToken);
  }

  getToken(): string {
    return this._cookieService.get('token');
  }

  getRefresToken(): string {
    return this._cookieService.get('refreshToken');
  }

  removeToken() {
    this._cookieService.delete('token', '/');
  }

  removeRefresToken() {
    this._cookieService.delete('refreshToken', '/');
  }

  removeAllData(){
    this._cookieService.deleteAll();
  }

}