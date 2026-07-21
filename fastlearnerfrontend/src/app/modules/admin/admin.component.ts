import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  HostListener,
  NgZone,
  OnDestroy,
  OnInit,
  TemplateRef,
} from '@angular/core';
import { NavigationEnd, Router, UrlTree } from '@angular/router';
import { Subscription } from 'rxjs';
import { DataHolderConstants } from 'src/app/core/constants/dataHolder.constants';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { NotificationService } from 'src/app/core/services/notification.service';

interface AdminNavItem {
  label: string;
  icon: string;
  route?: string;
}

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss'],
})
export class AdminComponent implements OnInit, AfterViewInit, OnDestroy {
  private static readonly MOBILE_BREAKPOINT_PX = 700;

  isCollapsed = false;
  isLayoutReady = false;
  isMobileView = false;
  sideBarVisible = false;
  notificationCount: number = 0;
  notifications: Array<any> = [];
  isNotificationDropdownVisible = false;
  notificationIds: Array<any> = [];
  private readonly legacyIcons = new Set([
    'promo-codes',
    'payouts',
    'settings',
  ]);
  private readonly _dataHolderConstants = new DataHolderConstants();
  private notificationDataSub?: Subscription;
  private notificationCountSub?: Subscription;
  private routerEventsSub?: Subscription;

  readonly menuItems: AdminNavItem[] = [
    { label: 'Users', icon: 'user-new', route: '/admin/users' },
    {
      label: 'Subscriptions',
      icon: 'subscription-new',
      route: '/admin/subscription',
    },
    { label: 'Courses', icon: 'course-new', route: '/admin/courses' },
    {
      label: 'Enrollments',
      icon: 'enrollment-new',
      route: '/admin/enrollments',
    },
    { label: 'Payments', icon: 'payment-new', route: '/admin/payments' },
    { label: 'Promo Codes', icon: 'promo-codes', route: '/admin/promo-codes' },
    { label: 'Payouts', icon: 'payouts', route: '/admin/payouts' },
    { label: 'Settings', icon: 'settings', route: '/admin/settings' },
  ];

  loggedInUser: any = {
    fullName: '',
    email: '',
    profilePicture: null,
  };
  suffixIconSearch: string | TemplateRef<void>;
  searchKeyword: string = '';

  constructor(
    private readonly router: Router,
    private readonly _authService: AuthService,
    private readonly _cacheService: CacheService,
    private readonly _communicationService: CommunicationService,
    private readonly _notificationService: NotificationService,
    private readonly cdr: ChangeDetectorRef,
    private readonly ngZone: NgZone,
  ) {}

  @HostListener('window:resize', ['$event'])
  onResize(event: UIEvent): void {
    this.updateMobileLayout((event.target as Window).innerWidth);
  }

  private updateMobileLayout(screenWidth: number): void {
    const wasMobile = this.isMobileView;
    this.isMobileView = screenWidth <= AdminComponent.MOBILE_BREAKPOINT_PX;

    if (!this.isMobileView) {
      this.closeSidebar();
    } else if (wasMobile !== this.isMobileView) {
      this.closeSidebar();
    }
  }

  ngOnInit(): void {
    this.updateMobileLayout(window.innerWidth);
    this.getLoggedInUserDetails();
    this.updateNotificationCount();
    this.viewNotifications();

    this.routerEventsSub = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.closeSidebar();
      }
    });

    this.notificationDataSub =
      this._communicationService.notificationData$.subscribe(() => {
        this.viewNotifications();
      });

    this.notificationCountSub =
      this._communicationService.notificationCountData$.subscribe(() => {
        this.updateNotificationCount();
      });
  }

  ngOnDestroy(): void {
    this.notificationDataSub?.unsubscribe();
    this.notificationCountSub?.unsubscribe();
    this.routerEventsSub?.unsubscribe();
    document.body.classList.remove('hide-scrollbar');
  }

  ngAfterViewInit(): void {
    this.ngZone.runOutsideAngular(() => {
      requestAnimationFrame(() => {
        requestAnimationFrame(() => {
          this.ngZone.run(() => {
            this.isLayoutReady = true;
            this.cdr.detectChanges();
          });
        });
      });
    });
  }

  toggleSidebar(): void {
    this.sideBarVisible = !this.sideBarVisible;
    if (this.isMobileView && this.sideBarVisible) {
      document.body.classList.add('hide-scrollbar');
    } else {
      document.body.classList.remove('hide-scrollbar');
    }
  }

  closeSidebar(): void {
    this.sideBarVisible = false;
    document.body.classList.remove('hide-scrollbar');
  }

  navigate(route?: string): void {
    if (!route) {
      return;
    }

    this.router.navigateByUrl(route);
    this.closeSidebar();
  }

  get getInitialOfLoggedInUser() {
    return this._authService.getLoggedInName();
  }

  routeToStudentPortal(): void {
    this.router.navigate(['/student/my-courses']);
  }

  routeToInstructorPortal(): void {
    this.router.navigate(['/instructor/instructor-dashboard']);
  }

  routeToMyCourses(): void {
    this.router.navigate(['/student/my-courses']);
  }

  routeToFavoriteCourses(): void {
    this.router.navigate(['/student/favorite-courses']);
  }

  routeToSubscription(): void {
    this.router.navigate(['/subscription']);
  }

  routeToUpdateProfile(): void {
    this.router.navigate(['/user/update-profile']);
  }

  routeToNotificationPage(): void {
    this.removeNotificationCount();
    this.isNotificationDropdownVisible = false;
    this.router.navigate(['/user/notifications']);
  }

  notificationToggle(visible: boolean): void {
    this.isNotificationDropdownVisible = visible;
  }

  viewNotifications(): void {
    this.notifications = this._cacheService.getNotifications(
      this._dataHolderConstants.CACHE_KEYS.NOTIFICATION,
    );
    if (this.notifications) {
      this.notifications.forEach((notification: any) => {
        notification.creationDate = this.timeAgo(
          new Date(notification.creationDate),
        );
      });
    }
  }

  redirect(notification?: any): void {
    this.isNotificationDropdownVisible = false;
    this.notificationIds.push(notification.id);
    this.removeNotifications();
    this.routeToSelectedUrl(notification);
    this.notifications = this._cacheService.getNotifications(
      this._dataHolderConstants.CACHE_KEYS.NOTIFICATION,
    );
    this.notifications.forEach((el) => {
      if (notification.id === el.id) {
        el.read = true;
      }
    });
    this._cacheService.saveNotifications(
      this._dataHolderConstants.CACHE_KEYS.NOTIFICATION,
      this.notifications,
    );
    this.viewNotifications();
  }

  removeNotifications(): void {
    this._notificationService
      .removeNotification(this.notificationIds)
      .subscribe();
    this.notificationIds = [];
  }

  routeToSelectedUrl(notification?: any): void {
    const url = notification?.url;
    const fragment = notification?.type;
    const tree: UrlTree = this.router.parseUrl(url);
    const queryParams = tree.queryParams;
    this.router.navigate([url?.split('?')[0]], { queryParams, fragment });
  }

  updateNotificationCount(): void {
    const count = this._cacheService.getDataFromCache('unclicked-noti-count');
    this.notificationCount = count ? Number(count) : 0;
  }

  removeNotificationCount(): void {
    this.notificationCount = 0;
    this._cacheService.removeFromCache('unclicked-noti-count');
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

  signOut(): void {
    const uniqueId = this._cacheService.getDataFromCache('unique-id');
    this._authService.signOut(uniqueId).subscribe({
      next: () => {
        this.notifications = [];
        this._cacheService.clearCache();
        localStorage.setItem('redirectUrl', '/');
        this._authService.changeNavState(false);
        this.router.navigate(['']);
      },
      error: () => {
        this.notifications = [];
        this._cacheService.clearCache();
        localStorage.setItem('redirectUrl', '/');
        this._authService.changeNavState(false);
        this.router.navigate(['']);
      },
    });
  }

  isRouteActive(route?: string): boolean {
    if (!route) {
      return false;
    }

    return this.router.url.startsWith(route);
  }

  isLegacyIcon(iconName: string): boolean {
    return this.legacyIcons.has(iconName);
  }

  getLoggedInUserDetails() {
    try {
      this.loggedInUser.fullName = this._authService.getLoggedInName();
      this.loggedInUser.email = this._authService.getLoggedInEmail();
      setTimeout(() => {
        try {
          this.loggedInUser.profilePicture =
            this._authService.getLoggedInPicture();
        } catch (error) {
          this.loggedInUser.profilePicture = null;
        }
      }, 1000);
    } catch (error) {
      this.loggedInUser = { fullName: '', email: '', profilePicture: null };
    }
  }
}
