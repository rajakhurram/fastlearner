import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { Router, NavigationEnd, UrlTree } from '@angular/router';
import { of, Subject } from 'rxjs';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { NotificationService } from 'src/app/core/services/notification.service';
import { InstructorComponent } from './instructor.component';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { SharedModule } from '../shared/shared.module';

class MockRouter {
  events = new Subject();
  url = 'instructor/instructor-dashboard';
  navigate = jasmine.createSpy('navigate');
  parseUrl = jasmine.createSpy('parseUrl').and.returnValue({ queryParams: {} });
}

describe('InstructorComponent', () => {
  let component: InstructorComponent;
  let fixture: ComponentFixture<InstructorComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let cacheService: jasmine.SpyObj<CacheService>;
  let communicationService: jasmine.SpyObj<CommunicationService>;
  let notificationService: jasmine.SpyObj<NotificationService>;
  let router: MockRouter;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'getLoggedInName',
      'getLoggedInEmail',
      'getLoggedInPicture',
      'signOut',
      'changeNavState',
    ]);
    const cacheServiceSpy = jasmine.createSpyObj('CacheService', [
      'getNotifications',
      'saveNotifications',
      'clearCache',
      'getDataFromCache',
      'removeFromCache',
    ]);
    const communicationServiceSpy = jasmine.createSpyObj(
      'CommunicationService',
      ['notificationData$', 'notificationCountData$']
    );
    const notificationServiceSpy = jasmine.createSpyObj('NotificationService', [
      'removeNotification',
    ]);

    router = new MockRouter();

    await TestBed.configureTestingModule({
      declarations: [InstructorComponent],
      imports: [AntDesignModule, BrowserAnimationsModule, SharedModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: CacheService, useValue: cacheServiceSpy },
        { provide: CommunicationService, useValue: communicationServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: router },
      ],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(InstructorComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    cacheService = TestBed.inject(CacheService) as jasmine.SpyObj<CacheService>;
    communicationService = TestBed.inject(
      CommunicationService
    ) as jasmine.SpyObj<CommunicationService>;
    notificationService = TestBed.inject(
      NotificationService
    ) as jasmine.SpyObj<NotificationService>;

    authService.getLoggedInName.and.returnValue('John Doe');
    authService.getLoggedInEmail.and.returnValue('johndoe@example.com');
    authService.getLoggedInPicture.and.returnValue('profile-pic-url');

    cacheService.getNotifications.and.returnValue([
      { id: 1, creationDate: new Date(), read: false },
    ]);

    communicationService.notificationData$ = new Subject();
    communicationService.notificationCountData$ = new Subject();

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize logged-in user details on init', fakeAsync(() => {
    component.ngOnInit();
    expect(authService.getLoggedInName).toHaveBeenCalled();
    expect(authService.getLoggedInEmail).toHaveBeenCalled();
    tick(1000);
    expect(authService.getLoggedInPicture).toHaveBeenCalled();
    expect(component.loggedInUser.fullName).toBe('John Doe');
    expect(component.loggedInUser.email).toBe('johndoe@example.com');
  }));

  it('should handle window resize and set visibility flags', () => {
    component.onResize({ target: { innerWidth: 1200 } });
    expect(component.navbarVisible).toBeFalse();

    component.onResize({ target: { innerWidth: 1000 } });
    expect(component.navbarVisible).toBeTrue();
  });

  it('should toggle menu visibility and scrollbar state', () => {
    component.menuVisible = false;
    component.menuToggle();
    expect(component.menuVisible).toBeTrue();

    component.menuToggle();
    expect(component.menuVisible).toBeFalse();
  });

  it('should toggle sidebar visibility and scrollbar state', () => {
    component.sideBarVisible = false;
    component.sideBarToggle();
    expect(component.sideBarVisible).toBeTrue();

    component.sideBarToggle();
    expect(component.sideBarVisible).toBeFalse();
  });

  it('should view notifications and format their creation date', () => {
    component.viewNotifications();
    expect(cacheService.getNotifications).toHaveBeenCalled();
    expect(component.notifications[0].creationDate).toContain('ago');
  });

  it('should redirect to selected URL', () => {
    const notification = { url: 'test-url?page=1', type: 'fragment' };
    const mockTree: UrlTree = {
      queryParams: { page: '1' },
    } as any;
    router.parseUrl.and.returnValue(mockTree);
    router.navigate.and.returnValue(Promise.resolve(true));

    component.routeToSelectedUrl(notification);

    expect(router.parseUrl).toHaveBeenCalledWith('test-url?page=1');
    expect(router.navigate).toHaveBeenCalledWith(['test-url'], {
      queryParams: { page: '1' },
      fragment: 'fragment',
    });
  });

  it('should remove notifications', () => {
    component.notificationIds = [1, 2, 3];
    notificationService.removeNotification.and.returnValue(of({}));

    component.removeNotifications();
    expect(notificationService.removeNotification).toHaveBeenCalledWith([
      1, 2, 3,
    ]);
    expect(component.notificationIds.length).toBe(0);
  });

  it('should sign out and clear data', () => {
    authService.signOut.and.returnValue(of({}));

    component.signOut();
    expect(authService.signOut).toHaveBeenCalled();
    expect(cacheService.clearCache).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['']);
    expect(component.notifications.length).toBe(0);
  });

  it('should handle route changes and hide menus', () => {
    router.events.next(new NavigationEnd(1, 'url', 'url'));
    expect(component.menuVisible).toBeFalse();
    expect(component.sideBarVisible).toBeFalse();
  });

  it('should update the notification count', () => {
    const initialCount = component.notificationCount;
    component.updateNotificationCount();
    expect(component.notificationCount).toBeUndefined();
  });

  it('should reset notification count', () => {
    component.notificationCount = 5;
    component.removeNotificationCount();
    expect(component.notificationCount).toBe(0);
  });

  it('should route to update profile', () => {
    component.routeToUpdateProfile();
    expect(router.navigate).toHaveBeenCalledWith(['user/update-profile']);
  });

  it('should route to student dashboard', () => {
    component.routeToStudentDashboard();
    expect(router.navigate).toHaveBeenCalledWith(['student/dashboard']);
  });

  it('should route to instructor profile', () => {
    component.routeToInstructorProfile();
    expect(router.navigate).toHaveBeenCalledWith(['instructor/profile']);
  });

  it('should route to notification page', () => {
    component.routeToNotificationPage();
    expect(router.navigate).toHaveBeenCalledWith(['user/notifications']);
  });

  it('should route to create course page', () => {
    component.routeToCreateCourse();
    expect(router.navigate).toHaveBeenCalledWith(['instructor/course']);
  });

  it('should format time ago correctly', () => {
    const date = new Date();
    date.setHours(date.getHours() - 2);
    expect(component.timeAgo(date)).toBe('2 hours ago');

    date.setMinutes(date.getMinutes() - 30);
    expect(component.timeAgo(date)).toBe('2 hours ago');
  });
});
