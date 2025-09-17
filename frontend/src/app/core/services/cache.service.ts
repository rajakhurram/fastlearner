import { Injectable } from '@angular/core';
import { DataHolderConstants } from './../constants/dataHolder.constants';
import  *  as CryptoJS from  'crypto-js';
import { CommunicationService } from './communication.service';
import { SocialAuthService } from '@abacritt/angularx-social-login';
import { NotificationService } from './notification.service';
import { CookiesService } from './cookie.service';
@Injectable({
  providedIn: 'root'
})
export class CacheService {

  private secretKey = 'Hello Fast Learner World';

  public constantDataHolder: DataHolderConstants = new DataHolderConstants();

  constructor(
    private _communicateionService?: CommunicationService, 
    private _socialAuthService?: SocialAuthService,
    private _cookiesService?: CookiesService
    // private _notificationService?: NotificationService
  ) { }

  removeFromCache(key: string) {
    window.localStorage.removeItem(key);
  }

  getDataFromCache(key: string): string {
    let val = window.localStorage.getItem(key);
    return val != null ? val : "";
  }

  saveInCache(key: string, value: string) {
    window.localStorage.setItem(key, value);
  }

  clearCache() {
    this._socialAuthService.signOut();
    // this._notificationService.closeConnection();
    window.localStorage.clear();
    this._cookiesService.removeAllData();
  }
  
  cacheLoginData(data: any) {
    this.saveInCache(this.constantDataHolder.CACHE_KEYS.USER, JSON.stringify(data.user));
    this.saveInCache(this.constantDataHolder.CACHE_KEYS.ROLE, JSON.stringify(data?.user.role));
    this.saveInCache(this.constantDataHolder.CACHE_KEYS.TOKEN, data.token);
    this.saveInCache(this.constantDataHolder.CACHE_KEYS.EXPIRY_TIME, data.expiryTime);
    this.saveInCache(this.constantDataHolder.CACHE_KEYS.USER_ID, data.user.id);
    this.saveInCache(this.constantDataHolder.CACHE_KEYS.USER_TYPE, data.user.userType);
  }

  cacheUserDetails(data: any){
    this.saveInCache(this.constantDataHolder.CACHE_KEYS.USER, JSON.stringify(data.user));
    this.saveInCache('Authorized Apps', data?.apps);
    this.saveInCache('Authorized Widgets', data.widgets);
    this.saveInCache('GroupName', data.groupName);
  }

  // Save array of objects to local storage
  public saveNotifications(key: string, data: any[]) {
    localStorage.setItem(key, JSON.stringify(data));
  }

  // Retrieve array of objects from local storage
  public getNotifications(key: string): any[] {
    return JSON.parse(localStorage.getItem(key)) || [];
  }

  public saveCourseData(key: string, data: any) {
    localStorage.setItem(key, JSON.stringify(data));
  }

  public getCourseData(key: string): any {
    return JSON.parse(localStorage.getItem(key)) || "";
  }

}