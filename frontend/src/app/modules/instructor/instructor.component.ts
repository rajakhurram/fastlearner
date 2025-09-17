import { Component, HostListener, OnInit, ViewContainerRef } from '@angular/core';
import { NavigationEnd, Router, UrlTree } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { Subscription } from 'rxjs';
import { DataHolderConstants } from 'src/app/core/constants/dataHolder.constants';
import { InstructorTabs } from 'src/app/core/enums/instructor_tabs';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { NotificationService } from 'src/app/core/services/notification.service';
import { SubscriptionPlanComponent } from '../auth/subscription-plan/subscription-plan.component';

@Component({
  selector: 'app-instructor',
  templateUrl: './instructor.component.html',
  styleUrls: ['./instructor.component.scss'],
})
export class InstructorComponent implements OnInit {
  notificationCount?: any = 0;

  notifications: Array<any> = [];
  isNotificationDropdownVisible: boolean = false;
  isNotificationDropdownVisibleMobile: boolean = false;
  navbarVisible: boolean = false;
  menuVisible: boolean = false;
  sideBarVisible: boolean = false;
  _dataHolderConstants: DataHolderConstants = new DataHolderConstants();
  notificationIds?: Array<any> = [];
  selectedRoute: string;
  routeSubscription: Subscription;
  InstructorTabs = InstructorTabs;
  activeDashboard?: boolean;
  activeCourse?: boolean;
  activePerformance?: boolean;
  activeNotifications?: boolean;
  activePayment?: boolean;
  activeAffiliateProfiles?: boolean;
  activePremiumCourses?: boolean;
  affiliateTabOpen?: boolean = false;
  activePremiumStudents?: boolean;
  permissions?: any;
  hideAffiliate?: boolean = true;

  constructor(
    private _authService: AuthService,
    private _router: Router,
    private _cacheService: CacheService,
    private _communicationService: CommunicationService,
    private _notificationService: NotificationService,
    private router: Router,
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef
  ) {
    this._communicationService.instructorTabChange$?.subscribe((tab: any) => {
      this.changeTabColor(tab);
    });
    this.router.events.subscribe((val) => {
      if (this.router.url?.split('?')?.[1]) {
        this.selectedRoute = InstructorTabs.COURSE;
      } else {
        this.selectedRoute = this.router.url;
      }
    });
  }

  loggedInUser: any = {
    fullName: '',
    email: '',
    profilePicture: null,
  };

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    const screenWidth = event.target.innerWidth;
    if (screenWidth > 1178) {
      this.navbarVisible = false;
      this.menuVisible = false;
      this.sideBarVisible = false;
      document.body.classList.remove('hide-scrollbar');
    } else {
      this.navbarVisible = true;
      if (this.menuVisible || this.sideBarVisible) {
        document.body.classList.add('hide-scrollbar');
      }
    }
  }

  ngOnInit(): void {
    this.onResize({ target: window });
    document.body.classList.remove('hide-scrollbar');
    this.routeSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        // Code to handle route change
        this.sideBarVisible = false;
        this.menuVisible = false;
        document.body.classList.remove('hide-scrollbar');
      }
    });

    this.updateNotificationCount();

    this.getLoggedInUserDetails();
    this._communicationService.notificationData$.subscribe(() => {
      this.viewNotifications();
    });

    this._communicationService.notificationCountData$.subscribe(() => {
      this.updateNotificationCount();
    });

    this.viewNotifications();
    this.checkPermissions();
  }

  changeTabColor(value?: any) {
    if (value == InstructorTabs.DASHBOARD) {
      this.activeTab(true, false, false, false, false, false, false, false);
      this.selectedRoute = InstructorTabs.DASHBOARD;
    } else if (value == InstructorTabs.COURSE) {
      this.activeTab(false, true, false, false, false, false, false, false);
      this.selectedRoute = InstructorTabs.COURSE;
    } else if (value == InstructorTabs.PERFORMANCE) {
      this.activeTab(false, false, true, false, false, false, false, false);
      this.selectedRoute = InstructorTabs.PERFORMANCE;
    } else if (value == InstructorTabs.NOTIFICATIONS) {
      this.activeTab(false, false, false, true, false, false, false, false);
      this.selectedRoute = InstructorTabs.NOTIFICATIONS;
    } else if (value == InstructorTabs.PAYMENT) {
      this.activeTab(false, false, false, false, true, false, false, false);
    } else if (value == InstructorTabs.AFFILIATE_PROFILES) {
      this.activeTab(false, false, false, false, false, true, false, false);
    } else if (value == InstructorTabs.PREMIUM_COURSES) {
      this.activeTab(false, false, false, false, false, false, false, true);
    }
  }

  changeTab(value?: any) {
    if (value == InstructorTabs.DASHBOARD) {
      this.activeTab(true, false, false, false, false, false, false, false);
      this._router.navigate(['instructor/instructor-dashboard']);
    } else if (value == InstructorTabs.COURSE) {
      if (value != this.selectedRoute.split('?')[0]) {
        this.activeTab(false, true, false, false, false, false, false, false);
        // this._router.navigate(['instructor/course']);
        this._router.navigate(['/content-type']);
      }
    } else if (value == InstructorTabs.PERFORMANCE) {
      this.activeTab(false, false, true, false, false, false, false, false);
      this._router.navigate(['instructor/performance']);
    } else if (value == InstructorTabs.NOTIFICATIONS) {
      this.removeNotificationCount();
      this.activeTab(false, false, false, true, false, false, false, false);
      this._router.navigate(['instructor/notifications']);
    } else if (value == InstructorTabs.PAYMENT) {
      this.activeTab(false, false, false, false, true, false, false, false);
      this._router.navigate(['instructor/payment']);
    } else if (value == InstructorTabs.AFFILIATE_PROFILES) {
      this.activeTab(false, false, false, false, false, true, false, false);
      this._router.navigate(['instructor/affiliate/profiles']);
    } else if (value == InstructorTabs.PREMIUM_COURSES) {
      this.activeTab(false, false, false, false, false, false, true, false);
      this._router.navigate(['instructor/affiliate/premium-courses']);
    } else if (value == InstructorTabs.PREMIUM_STUDENTS) {
      this.activeTab(false, false, false, false, false, false, false, true);
      this._router.navigate(['instructor/premium-student']);
    }
  }

  activeTab(
    activeDashboard?: boolean,
    activeCourse?: boolean,
    activePerformance?: boolean,
    activeNotifications?: boolean,
    activePayment?: boolean,
    activeAffiliateProfiles?: boolean,
    activePremiumCourses?: boolean,
    activePremiumStudents?: boolean
  ) {
    this.activeDashboard = activeDashboard;
    this.activeCourse = activeCourse;
    this.activePerformance = activePerformance;
    this.activeNotifications = activeNotifications;
    this.activePayment = activePayment;
    this.activeAffiliateProfiles = activeAffiliateProfiles;
    this.activePremiumCourses = activePremiumCourses;
    this.activePremiumStudents = activePremiumStudents;
  }

  openAffiliateTab() {
    this.affiliateTabOpen = !this.affiliateTabOpen;
  }

  notificationToggle(event: any) {
    this.isNotificationDropdownVisible = event;
  }

  notificationToggleMobile(event: any) {
    this.isNotificationDropdownVisibleMobile = event;
  }

  viewNotifications() {
    this.notifications = this._cacheService.getNotifications(
      this._dataHolderConstants.CACHE_KEYS.NOTIFICATION
    );
    if (this.notifications) {
      this.notifications?.forEach((notification: any) => {
        notification.creationDate = this.timeAgo(
          new Date(notification.creationDate)
        );
      });
    }
  }

  menuToggle() {
    if (this.menuVisible) {
      document.body.classList.remove('hide-scrollbar');
    } else {
      document.body.classList.add('hide-scrollbar');
    }
    this.menuVisible = !this.menuVisible;
    this.sideBarVisible = false;
  }

  sideBarToggle() {
    if (this.sideBarVisible) {
      document.body.classList.remove('hide-scrollbar');
    } else {
      document.body.classList.add('hide-scrollbar');
    }
    this.sideBarVisible = !this.sideBarVisible;
    this.menuVisible = false;
  }

  redirect(notification?: any) {
    this.isNotificationDropdownVisible = false;
    this.isNotificationDropdownVisibleMobile = false;
    this.notificationIds.push(notification.id);
    this.removeNotifications();
    this.routeToSelectedUrl(notification);
    this.notifications = this._cacheService.getNotifications(
      this._dataHolderConstants.CACHE_KEYS.NOTIFICATION
    );
    this.notifications.forEach((el) => {
      if (notification.id == el.id) {
        el.read = true;
      }
    });
    this._cacheService.saveNotifications(
      this._dataHolderConstants.CACHE_KEYS.NOTIFICATION,
      this.notifications
    );
    this.viewNotifications();
  }
  routeToLandingPage() {
    document.body.classList.remove('hide-scrollbar');
    this._communicationService.instructorTabChange(InstructorTabs.DASHBOARD);
    this._router.navigate(['instructor/instructor-dashboard']);
  }

  removeNotifications() {
    this._notificationService
      .removeNotification(this.notificationIds)
      .subscribe();
    this.notificationIds = [];
  }

  routeToSelectedUrl(notification?: any) {
    const url = notification?.url;
    const fragment = notification?.type;
    document.body.classList.remove('hide-scrollbar');
    const tree: UrlTree = this._router.parseUrl(url);
    const queryParams = tree.queryParams;
    this.menuVisible = false;
    this.sideBarVisible = false;

    Object.keys(queryParams).forEach((key) => {
      console.log(`${key}: ${queryParams[key]}`);
    });
    this._router.navigate([url?.split('?')[0]], { queryParams, fragment });
  }

  showNotificationCount() {
    this.notificationCount = 0;
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

  getLoggedInUserDetails() {
    this.loggedInUser.fullName = this._authService.getLoggedInName();
    this.loggedInUser.email = this._authService.getLoggedInEmail();
    setTimeout(() => {
      this.loggedInUser.profilePicture = this._authService.getLoggedInPicture();
    }, 1000);
  }

  routeToUpdateProfile() {
    this.menuVisible = false;
    this.sideBarVisible = false;
    document.body.classList.remove('hide-scrollbar');
    this._router.navigate(['user/update-profile']);
  }

  routeToStudentDashboard() {
    this.menuVisible = false;
    this.sideBarVisible = false;
    document.body.classList.remove('hide-scrollbar');
    this._router.navigate(['student/dashboard']);
  }

  get getInitialOfLoggedInUser() {
    return this._authService.getLoggedInName();
  }

  routeToInstructorProfile() {
    this.menuVisible = false;
    this.sideBarVisible = false;
    document.body.classList.remove('hide-scrollbar');
    this._router.navigate(['instructor/profile']);
  }

  routeToNotificationPage() {
    this.removeNotificationCount();
    this.menuVisible = false;
    this.sideBarVisible = false;
    this.isNotificationDropdownVisible = false;
    this.isNotificationDropdownVisibleMobile = false;
    document.body.classList.remove('hide-scrollbar');
    this._router.navigate(['user/notifications']);
  }

  signOut() {
    document.body.classList.remove('hide-scrollbar');
    this._authService.signOut().subscribe({
      next: (response: any) => {
        this.notifications = [];
        this.menuVisible = false;
        this.sideBarVisible = false;
        this.loggedInUser.profilePicture = null;
        this._cacheService.clearCache();
        this._router.navigate(['']);
        this._authService.changeNavState(false);
      },
      error: (error: any) => {},
    });
  }

  checkPermissions() {
    const data = this._cacheService.getDataFromCache('permissions');
    this.permissions = data
      ? JSON.parse(this._cacheService.getDataFromCache('permissions'))
      : null;
    if (
      this.permissions &&
      this.permissions.length > 0 &&
      this.permissions.includes(InstructorTabs.AFFILIATE)
    ) {
      this.hideAffiliate = false;
    }
  }

  updateNotificationCount() {
    this.notificationCount = this._cacheService.getDataFromCache(
      'unclicked-noti-count'
    );
  }

  removeNotificationCount() {
    this.notificationCount = 0;
    this._cacheService.removeFromCache('unclicked-noti-count');
  }

  routeToCreateCourse() {
    document.body.classList.remove('hide-scrollbar');
    this.menuVisible = false;
    this.sideBarVisible = false;
    this._router.navigate(['/content-type']);
    // this._router.navigate(['instructor/course']);
  }

  routeToPremiumStudent() {
    document.body.classList.remove('hide-scrollbar');
    this.menuVisible = false;
    this.sideBarVisible = false;
    this._router.navigate(['instructor/premium-student']);
  }

  openSubscriptionPlan(): void {
    const modal = this._modal.create({
      nzContent: SubscriptionPlanComponent,
      nzComponentParams: {
        fromSubscriptionPlan: true,
        showFreePlan: false,
        showStandardPlan: false
      },
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: this.fullWidth ? '80%' : '100%',
      nzWidth: '80%',
    });
    modal.afterClose?.subscribe((result) => {
      // this.subscriptionModalOpened = false;
    });
  }

  routeToSubscription(){
    // document.body.classList.remove('hide-scrollbar');
    // this.navbarVisible = false;
    this._router.navigate(['subscription']);
  }

}
