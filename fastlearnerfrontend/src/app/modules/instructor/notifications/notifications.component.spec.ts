import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { of, throwError } from 'rxjs';
import { NotificationsComponent } from './notifications.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { NotificationService } from 'src/app/core/services/notification.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { Notification } from 'src/app/core/models/notification.model';
import { Title } from '@angular/platform-browser';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('NotificationsComponent', () => {
  let component: NotificationsComponent;
  let fixture: ComponentFixture<NotificationsComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let notificationService: jasmine.SpyObj<NotificationService>;
  let router: jasmine.SpyObj<Router>;
  let titleService: jasmine.SpyObj<Title>;
  const httpConstants = new HttpConstants();

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['getLoggedInName']);
    const notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['getNotifications', 'removeNotification']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate', 'parseUrl']);
    const titleServiceSpy = jasmine.createSpyObj('Title', ['setTitle']);
    
    await TestBed.configureTestingModule({
      declarations: [NotificationsComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: Title, useValue: titleServiceSpy }
      ],
      schemas : [CUSTOM_ELEMENTS_SCHEMA , NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(NotificationsComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    notificationService = TestBed.inject(NotificationService) as jasmine.SpyObj<NotificationService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    titleService = TestBed.inject(Title) as jasmine.SpyObj<Title>;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch notifications on init', () => {
    const mockNotifications: Notification[] = [
      { id: 1, creationDate: new Date().toString(), content: 'Test notification', url: 'test-url' }
    ];
    const mockResponse = {
      status: httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      data: {
        notificationList: mockNotifications,
        noOfPages: 1
      }
    };
    notificationService.getNotifications.and.returnValue(of(mockResponse));

    component.getNotifications();

    expect(notificationService.getNotifications).toHaveBeenCalledWith(component.payLoad);
    expect(component.notifications).toEqual(mockNotifications);
    expect(component.totalPages).toBe(1);
  });

  it('should handle error when fetching notifications', () => {
    notificationService.getNotifications.and.returnValue(throwError({ status: 500 }));

    component.getNotifications();

    expect(component.notifications).toBeUndefined();
    expect(component.totalPages).toBeUndefined();
  });

  it('should get initial of logged-in user', () => {
    authService.getLoggedInName.and.returnValue('John Doe');

    expect(component.getInitialOfLoggedInUser).toBe('John Doe');
  });

  it('should load more notifications', () => {
    spyOn(component, 'getNotifications').and.callThrough();

    component.showMoreNotifications();

    expect(component.payLoad.pageNo).toBe(1);
    expect(component.getNotifications).toHaveBeenCalled();
  });

  it('should clear all notifications', () => {
    component.notifications = [
      { id: 1, creationDate: new Date().toString(), content: 'Test notification', url: 'test-url' }
    ];
    notificationService.removeNotification.and.returnValue(of({}));

    component.clearAllNotifications();
    expect(notificationService.removeNotification).toHaveBeenCalledWith([1]);
    expect(component.notifications).toEqual([]);
  });

 it('should redirect to selected URL', () => {
    const notification = { url: 'test-url?page=1', type: 'fragment' };
    const mockTree: UrlTree = {
      queryParams: { page: '1' }
    } as any;
    router.parseUrl.and.returnValue(mockTree);
    router.navigate.and.returnValue(Promise.resolve(true));

    component.routeToSelectedUrl(notification);

    expect(router.parseUrl).toHaveBeenCalledWith('test-url?page=1');
    expect(router.navigate).toHaveBeenCalledWith(['test-url'], { queryParams: { page: '1' }, fragment: 'fragment' });
  });

  it('should remove notifications', () => {
    component.notificationIds = [1, 2];
    notificationService.removeNotification.and.returnValue(of({}));

    component.removeNotifications();

    expect(notificationService.removeNotification).toHaveBeenCalledWith([1, 2]);
    expect(component.notificationIds).toEqual([]);
  });

  it('should convert time to "time ago" format', () => {
    const now = new Date();
    expect(component.timeAgo(new Date(now.getTime() - 3600 * 1000))).toEqual('1 hour ago');
    expect(component.timeAgo(new Date(now.getTime() - 86400 * 1000))).toEqual('1 day ago');
    expect(component.timeAgo(new Date(now.getTime() - 604800 * 1000))).toEqual('1 week ago');
    expect(component.timeAgo(new Date(now.getTime() - 2592000 * 1000))).toEqual('1 month ago');
    expect(component.timeAgo(new Date(now.getTime() - 31536000 * 1000))).toEqual('1 year ago');
    expect(component.timeAgo(new Date(now.getTime() - 10000))).toEqual('10 seconds ago');
    expect(component.timeAgo(now)).toEqual('just now');
  });

  
});
