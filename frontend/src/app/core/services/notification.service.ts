import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, Injector, OnDestroy } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { environment } from 'src/environments/environment.development';
import { CacheService } from './cache.service';
import { AuthService } from './auth.service';
import { DataHolderConstants } from '../constants/dataHolder.constants';
import { CommunicationService } from './communication.service';

@Injectable({
  providedIn: 'root'
})
export class NotificationService implements OnDestroy {
  
  _dataHolderConstants: DataHolderConstants = new DataHolderConstants();
  private notificationSubject = new Subject<any>();
  private eventSource: EventSource | undefined;
  timestamp?: any;
  notifications: Array<any> = [];
  notificationCount?: any = 0;
  private _cacheService: CacheService;

  constructor(
    private _http: HttpClient,
    private _authService: AuthService,
    private _communicationService: CommunicationService,
    private injector: Injector,
  ) { 
    this._cacheService = this.injector.get(CacheService);
          this.connectSSE();
        }

  notifications$ = this.notificationSubject.asObservable();

  connectSSE(): void {
    if(this._cacheService.getDataFromCache('isLoggedIn')){

      if (this.eventSource) {
        this.eventSource.close();
      }

      const uniqueId = this._cacheService.getDataFromCache('unique-id');

      this.timestamp = uniqueId || this.generateTimeStamp();

     this._cacheService.saveInCache('unique-id', this.timestamp);

      this.eventSource = new EventSource(
        `${environment.baseUrl}notification/register/` +
          this.timestamp +
          `?token=` +
          this._authService.getAccessToken()
      );
      this.eventSource.addEventListener('notification', (message) => {
        const response = JSON.parse(message.data);
        this.notifications = this._cacheService.getNotifications(
          this._dataHolderConstants.CACHE_KEYS.NOTIFICATION
        );
        if (
          !this.notifications == null ||
          (this.notifications.length != 0 && !Array.isArray(response))
        ) {
          this.notifications.unshift(response);
          if (this.notifications.length > 4) {
            this.notifications.pop();
          }
          this.notificationCount = Number(this._cacheService.getDataFromCache('unclicked-noti-count'));
          this.notificationCount += 1;
          this._cacheService.saveInCache('unclicked-noti-count', this.notificationCount);
          this._cacheService.saveNotifications(
            this._dataHolderConstants.CACHE_KEYS.NOTIFICATION,
            this.notifications
          );
        } else {
          this._cacheService.removeFromCache(
            this._dataHolderConstants.CACHE_KEYS.NOTIFICATION
          );
          this._cacheService.saveNotifications(
            this._dataHolderConstants.CACHE_KEYS.NOTIFICATION,
            response
          );
          this._cacheService.saveInCache('unclicked-noti-count', response.filter((noti: any) => !noti.read).length);
        }
        this._communicationService.showNotificationCountData();
        this.notifications = [];
        this._communicationService.showNotificationData();
      });
    }
  }

  ngOnDestroy(): void {
    if (this.eventSource) {
      this.eventSource.close();
    }
  }

  closeConnection(): void {
    if (this.eventSource) {
      this.eventSource.close();
    }
  }

  generateTimeStamp(){
    return new Date().getTime();
  }

  public getNotifications(payLoad?: any): Observable<any> {
    const url = `${environment.baseUrl}notification/fetch-all?pageNo=${payLoad.pageNo}&pageSize=${payLoad.pageSize}`;
    return this._http.get(url);
  }

  public removeNotification(notificationId: any): Observable<any> {
    const url = `${environment.baseUrl}notification/`;
    return this._http.delete(url, { body: { notificationId } });
  }
}
