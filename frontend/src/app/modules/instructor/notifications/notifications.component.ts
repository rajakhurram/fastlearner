import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Router, UrlTree } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { Notification } from 'src/app/core/models/notification.model';
import { AuthService } from 'src/app/core/services/auth.service';
import { NotificationService } from 'src/app/core/services/notification.service';

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss'],
})
export class NotificationsComponent implements OnInit {
  _httpConstants: HttpConstants = new HttpConstants();
  notifications?: Notification[];
  notificationIds?: Array<any> = [];
  isOverflow = false;

  payLoad = {
    pageNo: 0,
    pageSize: 10,
  };
  totalPages?: any;

  constructor(
    private _authService: AuthService,
    private _notificationService: NotificationService,
    private _router: Router,
    private titleService: Title
  ) {}

  ngOnInit(): void {
    // this.titleService.setTitle('Notifications');
    this.getNotifications();
  }

  getNotifications() {
    this._notificationService?.getNotifications(this.payLoad)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.notifications = response?.data?.notificationList;
          this.totalPages = response?.data?.noOfPages;
          this.notifications.forEach((notification: any) => {
            notification.timeInterval = this.timeAgo(
              new Date(notification.creationDate)
            );
          });
        }
      },
      error: (error: any) => {},
    });
  }

  get getInitialOfLoggedInUser() {
    return this._authService.getLoggedInName();
  }

  showMoreNotifications() {
    this.payLoad.pageNo += 1;
    this.getNotifications();
  }

  clearAllNotifications() {
    this.notifications?.forEach((notification) => {
      this.notificationIds?.push(notification.id);
    });
    this.removeNotifications();
    this.notifications = [];
  }

  redirect(notification?: any) {
    this.notificationIds.push(notification.id);
    this.removeNotifications();
    this.routeToSelectedUrl(notification);
    this.notifications = [];
    this.getNotifications();
  }

  removeNotifications() {
    this._notificationService
      ?.removeNotification(this.notificationIds)
      ?.subscribe(() => {
        this.getNotifications();
        this.notificationIds = [];
      });
  }

  routeToSelectedUrl(notification?: any) {
    const url = notification?.url;
    const fragment = notification?.type;
    const tree: UrlTree = this._router.parseUrl(url);
    const queryParams = tree.queryParams;

    Object.keys(queryParams).forEach((key) => {
      console.log(`${key}: ${queryParams[key]}`);
    });
    this._router.navigate([url?.split('?')[0]], { queryParams, fragment });
  }
  

  timeAgo(date: Date): string {
    const currentDate = new Date();
    const seconds = Math.floor((currentDate.getTime() - date.getTime()) / 1000);
  
    const intervals = [
      { label: 'year', seconds: 31536000 },
      { label: 'month', seconds: 2592000 },
      { label: 'week', seconds: 604800 },
      { label: 'day', seconds: 86400 },
      { label: 'hour', seconds: 3600 },
      { label: 'minute', seconds: 60 },
    ];
  
    for (const interval of intervals) {
      const intervalCount = Math.floor(seconds / interval.seconds);
      if (intervalCount > 1) {
        return `${intervalCount} ${interval.label}s ago`;
      } else if (intervalCount === 1) {
        return `1 ${interval.label} ago`;
      }
    }
  
    if (seconds < 10) {
      return 'just now';
    }
  
    return `${seconds} seconds ago`;
  }
  
  checkOverflow(event: MouseEvent): void {
    const element = event.target as HTMLElement;
    this.isOverflow = element.scrollHeight > element.clientHeight;
  }


}
