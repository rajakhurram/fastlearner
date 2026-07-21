import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, NavigationEnd } from '@angular/router';
import { Subject } from 'rxjs';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { NotificationService } from 'src/app/core/services/notification.service';
import { InitialCharactorPipe } from 'src/app/core/pipes/initial-charactor.pipe';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { AdminComponent } from './admin.component';

describe('AdminComponent', () => {
  let component: AdminComponent;
  let fixture: ComponentFixture<AdminComponent>;
  let routerEvents: Subject<unknown>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    routerEvents = new Subject();
    router = jasmine.createSpyObj('Router', ['navigate', 'navigateByUrl', 'parseUrl'], {
      url: '/admin/users',
      events: routerEvents.asObservable(),
    });
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'getLoggedInName',
      'getLoggedInEmail',
      'getLoggedInPicture',
      'signOut',
    ]);
    const cacheServiceSpy = jasmine.createSpyObj('CacheService', [
      'getNotifications',
      'saveNotifications',
      'clearCache',
      'getDataFromCache',
      'removeFromCache',
    ]);

    authServiceSpy.getLoggedInName.and.returnValue('Admin User');
    authServiceSpy.getLoggedInEmail.and.returnValue('admin@example.com');
    authServiceSpy.getLoggedInPicture.and.returnValue('');
    cacheServiceSpy.getDataFromCache.and.returnValue('2');
    cacheServiceSpy.getNotifications.and.returnValue([
      { id: 1, creationDate: new Date(), read: false },
    ]);

    await TestBed.configureTestingModule({
      declarations: [AdminComponent, InitialCharactorPipe],
      imports: [AntDesignModule, BrowserAnimationsModule],
      providers: [
        { provide: Router, useValue: router },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: CacheService, useValue: cacheServiceSpy },
        {
          provide: CommunicationService,
          useValue: {
            notificationData$: new Subject(),
            notificationCountData$: new Subject(),
          },
        },
        {
          provide: NotificationService,
          useValue: jasmine.createSpyObj('NotificationService', [
            'removeNotification',
          ]),
        },
      ],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load logged-in user details on init', () => {
    expect(component.loggedInUser.fullName).toBe('Admin User');
    expect(component.loggedInUser.email).toBe('admin@example.com');
  });

  it('should update notification count from cache', () => {
    component.updateNotificationCount();
    expect(component.notificationCount).toBe(2);
  });

  it('should reset notification count', () => {
    component.notificationCount = 5;
    component.removeNotificationCount();
    expect(component.notificationCount).toBe(0);
  });

  it('should format time ago', () => {
    const twoHoursAgo = new Date();
    twoHoursAgo.setHours(twoHoursAgo.getHours() - 2);

    expect(component.timeAgo(twoHoursAgo)).toContain('hour');
  });

  it('should close sidebar on navigation end', () => {
    component.sideBarVisible = true;
    routerEvents.next(new NavigationEnd(1, '/admin/courses', '/admin/courses'));
    expect(component.sideBarVisible).toBeFalse();
  });

  it('should navigate to admin routes', () => {
    component.navigate('/admin/users');
    expect(router.navigateByUrl).toHaveBeenCalledWith('/admin/users');
  });
});
