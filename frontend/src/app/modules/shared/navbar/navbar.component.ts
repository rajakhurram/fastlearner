import {
  Component,
  HostListener,
  OnDestroy,
  OnInit,
  Renderer2,
} from '@angular/core';
import {
  ActivatedRoute,
  NavigationEnd,
  Router,
  UrlTree,
} from '@angular/router';
import { CacheService } from 'src/app/core/services/cache.service';
import { Subscription } from 'rxjs';
import { AuthService } from 'src/app/core/services/auth.service';
import { CourseService } from 'src/app/core/services/course.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { FavoriteCourse } from 'src/app/core/models/favorite-course.model';
import { MyCourses } from 'src/app/core/models/my-courses.model';
import { environment } from 'src/environments/environment.development';
import { SharedService } from 'src/app/core/services/shared.service';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { NotificationService } from 'src/app/core/services/notification.service';
import { DataHolderConstants } from 'src/app/core/constants/dataHolder.constants';
import { CourseTypeMap, ViewAllMap } from 'src/app/core/enums/course-status';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
})
export class NavbarComponent implements OnInit {
  imageUrl = environment.imageUrl;
  _httpConstants: HttpConstants = new HttpConstants();
  _dataHolderConstants: DataHolderConstants = new DataHolderConstants();
  favoriteCourses: FavoriteCourse = new FavoriteCourse();
  myCourses: MyCourses = new MyCourses();
  debounceTimeout: any = null;
  debounceDelay: number = 150; // Adjust the debounce delay as needed
hover = false;
  isSticky: boolean = false;
  isLoggedIn: any;
  categoryList: Array<any> = [];
  favoriteCourseList: Array<any> = [];
  myCourseList: Array<any> = [];
  searchResults: Array<any> = [];
  searchInstructorResults: Array<any> = [];
  isHeartClick: boolean = false;
  showUserToggle: boolean = false;
  isMenuOpen: boolean = false;
  isMenuOpenMobile: boolean = false;
  showSearch: boolean = false;
  isFavMenuOpen: boolean = false;
  showNoCourseText: boolean = false;
  isFavMenuOpenMobile: boolean = false;
  navbarVisible: boolean = false;
  isNotificationDropdownVisible: boolean = false;
  isNotificationDropdownVisibleMobile: boolean = false;
  searchKeyword: string = '';
  searchSuggestions: Array<any> = [];
  nzFilterOption = (): boolean => true;
  private _subscription: Subscription;
  routeSubscription: Subscription;
  searchInputText?: any;
  isLoading: boolean = false;
logoWidth: number;

  loggedInUser: any = {
    fullName: '',
    email: '',
    profilePicture: null,
  };
  selectedIndex: number = -1;

  payLoad = {
    reviewFrom: 0,
    reviewTo: 5,
    searchValue: '',
    pageNo: 0,
    pageSize: 10,
  };
  notificationCount?: any = 0;

  notifications: Array<any> = [];
  notificationIds?: Array<any> = [];
  component: { pageNo: number };

  @HostListener('window:scroll', ['$event'])
  onScroll(event: Event): void {
    this.isSticky = window.scrollY > 0;
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    const screenWidth = event.target.innerWidth;
    if (screenWidth > 1178) {
      this.navbarVisible = false;
      this.showSearch = false;
      document.body.classList.remove('hide-scrollbar');
    } else {
      if (this.navbarVisible) {
        document.body.classList.add('hide-scrollbar');
      }
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    this.showNoCourseText = false;
    this.searchResults = [];
    this.searchInstructorResults = [];
    this.selectedIndex = -1;
  }

  constructor(
    private _router: Router,
    private _cacheService: CacheService,
    private _authService: AuthService,
    private _courseService: CourseService,
    private _sharedService: SharedService,
    private _communicationService: CommunicationService,
    private _notificationService: NotificationService,
    private router: Router
  ) {
    this.isLoggedIn = this._authService.isLoggedIn();
    this._subscription = this._sharedService.getNavDetail()?.subscribe(() => {
      this.getLoggedInUserDetails();
    });
    this._subscription = this._sharedService
      .getFavCourseMenu()
      ?.subscribe(() => {
        this.getFavoriteCourseList();
      });
  }
  ngOnInit(): void {
    this.onResize({ target: window });
    document.body.classList.remove('hide-scrollbar');

    this.routeSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        // Code to handle route change
        document.body.classList.remove('hide-scrollbar');
        this.showNoCourseText = false;

        if (!event.url.includes('/student/filter-courses')) {
          this.searchKeyword = '';
          this.searchResults = [];
          this.searchInstructorResults = [];
        }
      }
    });
    if (this._authService.isLoggedIn()) {
      this.getLoggedInUserDetails();
      this.getFavoriteCourseList();
      this.getMyCourseList();
      this.updateNotificationCount();
    }
    this.listenNavbarState();
    this.getCourseCategoryList();
    this._communicationService.notificationData$.subscribe(() => {
      this.viewNotifications();
    });

    this._communicationService.notificationCountData$.subscribe(() => {
      this.updateNotificationCount();
    });

    this.viewNotifications();

    this.setLogoWidth();

  window.addEventListener('resize', () => {
    this.setLogoWidth();
  });
  }

  toggleSearch() {
    this.showSearch = !this.showSearch;
    this.navbarVisible = false;
    if (this.showSearch) {
      document.body.classList.add('hide-scrollbar');
    } else {
      document.body.classList.remove('hide-scrollbar');
    }
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
          // Optionally log the error or handle it as needed
        }
      }, 1000);
    } catch (error) {
      this.loggedInUser = { fullName: '', email: '', profilePicture: null };
      // Optionally log the error or handle it as needed
    }
  }

  // getLoggedInUserDetails(): void {
  //   this._authService.getLoggedInName()?.subscribe(name => {
  //     this.loggedInUser.fullName = name || '';
  //   });

  //   this._authService.getLoggedInEmail()?.subscribe(email => {
  //     this.loggedInUser.email = email || '';
  //   });

  //   this._authService.getLoggedInPicture()?.subscribe(picture => {
  //     this.loggedInUser.profilePicture = picture || null;
  //   });
  // }

  listenNavbarState() {
    this._authService.$changeNavbarSate.subscribe((state: any) => {
      this.isLoggedIn = this._authService.isLoggedIn();
      if (state) {
        this.getFavoriteCourseList();
        this.getMyCourseList();
        this.getLoggedInUserDetails();
      }
    });
  }

  getSearchSuggestions(keyword: any) {
    if (keyword?.length >= 3) {
      this.searchInputText = keyword;
      this._courseService.getSuggestions(keyword).subscribe({
        next: (response: any) => {
          const searchSuggestions: Array<any> = [];
          response?.data?.forEach((item: any) => {
            searchSuggestions.push(item);
          });
          this.searchSuggestions = searchSuggestions;
        },
        error: (error: any) => {
          this.searchSuggestions = [];
        },
      });
    } else if (keyword.length == 0) {
      this.searchSuggestions = [];
    }
  }

  isMyCourseMenuVisible(event) {
    this.isMenuOpen = event;
  }
  isMyCourseMenuMobileVisible(event) {
    this.isMenuOpenMobile = event;
  }

  searchByIconClick(searchValue: any, type?: any) {
    this.searchKeyword = searchValue;
    this.searchResults = [];
    this.searchInstructorResults = [];
    if (!searchValue || !searchValue.trim() || this.searchKeyword?.length < 3) {
      this.showNoCourseText = false;
      // If searchValue is empty or contains only whitespace, return without executing the search
      return;
    }
    document.body.classList.remove('hide-scrollbar');
    this.payLoad.searchValue = searchValue;

    this.navbarVisible = false;
    this.showSearch = false;
    this.selectedIndex = -1;
    this.showNoCourseText = false;
    this._router.navigate(['student/filter-courses'], {
      queryParams: {
        search: this.searchKeyword,
      },
    });
  }

  clearSearch() {
    this.searchKeyword = '';
    this.searchResults = [];
    this.searchInstructorResults = [];
    this.selectedIndex = -1;
    this.showNoCourseText = false;
  }
  searchByKeyword(searchValue: any, type?: any) {
    this.searchKeyword = searchValue;

    // Clear the previous debounce timeout
    if (this.debounceTimeout) {
      clearTimeout(this.debounceTimeout);
    }

    // Set a new debounce timeout
    this.debounceTimeout = setTimeout(() => {
      this.searchResults = [];
      this.searchInstructorResults = [];
      if (
        !searchValue ||
        !searchValue.trim() ||
        this.searchKeyword?.length < 3
      ) {
        this.showNoCourseText = false;
        // If searchValue is empty or contains only whitespace, return without executing the search
        return;
      }
      document.body.classList.remove('hide-scrollbar');
      this.payLoad.searchValue = searchValue;
      this._courseService.getSuggestions(searchValue).subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.showNoCourseText = false;
            this.searchResults = response?.data?.searchCourses;
            this.searchInstructorResults = response?.data?.instructorProfiles;
            this.selectedIndex = -1;
          }
        },
        error: (error: any) => {
          if (
            error?.error?.status ==
            this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
          ) {
            this.showNoCourseText = true;
            // this._messageService.info('No Course found');
          }
        },
      });
    }, this.debounceDelay);
  }

  event(event) {}

  navigateSearch(result) {
    if (this.selectedIndex < this.searchResults?.length) {
      this.searchKeyword =
        this.selectedIndex == -1
          ? result.target.value
          : this.searchResults[this.selectedIndex]?.title;
      if (
        !this.searchKeyword ||
        !this.searchKeyword.trim() ||
        this.searchKeyword?.length < 3
      ) {
        this.showNoCourseText = false;
        // If searchValue is empty or contains only whitespace, return without executing the search
        return;
      }
      this.navbarVisible = false;
      this.showSearch = false;
      this.showNoCourseText = false;
      this.searchResults = [];
      this.searchInstructorResults = [];
      this.selectedIndex = -1;

      this._router.navigate(['student/filter-courses'], {
        queryParams: {
          search: this.searchKeyword,
        },
      });
    } else {
      this.routeToInstructorProfile(
        this.searchInstructorResults[
          this.selectedIndex - this.searchResults?.length
        ]?.profileUrl
      );
    }
  }
  onListItemClicked(result) {
    this.searchKeyword = result.title;
    this.navbarVisible = false;
    this.showSearch = false;
    this.showNoCourseText = false;
    this.searchResults = [];
    this.searchInstructorResults = [];
    this.selectedIndex = -1;

    this._router.navigate(['student/course-details', result?.courseUrl]);
  }

  // handleKeydown(event: KeyboardEvent) {
  //   const maxIndex =
  //     this.searchResults?.length + this.searchInstructorResults?.length - 1;

  //   if (event.key === 'ArrowDown') {
  //     // Move down in the list
  //     this.selectedIndex =
  //       this.selectedIndex != maxIndex ? this.selectedIndex + 1 : 0;
  //   } else if (event.key === 'ArrowUp') {
  //     // Move up in the list
  //     this.selectedIndex =
  //       this.selectedIndex > 0 ? this.selectedIndex - 1 : maxIndex;
  //   } else if (event.key === 'Enter') {
  //     // Select the highlighted result
  //     if (this.selectedIndex >= 0 && this.selectedIndex <= maxIndex) {
  //     }
  //   }
  // }

  handleKeydown(event: KeyboardEvent) {
    const maxIndex =
      this.searchResults?.length + this.searchInstructorResults?.length - 1;

    if (event.key === 'ArrowDown') {
      // Move down in the list
      this.selectedIndex =
        this.selectedIndex !== maxIndex ? this.selectedIndex + 1 : 0;
    } else if (event.key === 'ArrowUp') {
      // Move up in the list
      this.selectedIndex =
        this.selectedIndex > 0 ? this.selectedIndex - 1 : maxIndex;
    } else if (event.key === 'Enter') {
      event.preventDefault();
      // Check if an item is selected in the dropdown
      if (this.selectedIndex >= 0 && this.selectedIndex <= maxIndex) {
        if (this.selectedIndex < this.searchResults?.length) {
          // If the selected item is a search result, navigate to the course details page
          this.onListItemClicked(this.searchResults[this.selectedIndex]);
        } else {
          // If the selected item is an instructor result, navigate to the instructor profile
          const instructorIndex =
            this.selectedIndex - this.searchResults.length;
          this.routeToInstructorProfile(
            this.searchInstructorResults[instructorIndex]?.profileUrl
          );
        }
      } else {
        // If no item is selected, perform the search
        this.navigateSearch({ target: { value: this.searchKeyword } });
      }
    }
  }

  handleEnterKey(result?) {
    this.searchByKeyword(result);
  }

  getFavoriteCourseList() {
    this._courseService.getFavoriteCourses(this.favoriteCourses).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.favoriteCourseList = response?.data?.favouriteCourses;
        }
      },
      error: (error: any) => {
        if (
          error?.error?.status ==
          this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
        ) {
           this.favoriteCourseList = [];
        }
      },
    });
    }

  onScroller(event: any): void {
    const dropdown = event.target;

    if (this.myCourseList.length % this.myCourses.pageSize === 0) {
      const threshold = 100; // distance from the bottom
      const nearBottom =
        dropdown.scrollHeight - dropdown.scrollTop <=
        dropdown.clientHeight + threshold;

      if (nearBottom && !this.isLoading) {
        this.getMyCourseList();
      }
    }
  }

  getMyCourseList() {
    if (this.isLoading) return;
    this.isLoading = true;

    this._courseService.getMyCourses(this.myCourses).subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          if (response?.data?.myCourses?.length > 0) {
            this.myCourseList.push(...response.data.myCourses);
            this.myCourses.pageNo += 1;
          } else {
            this.isLoading = false;
            return;
          }
        }
        this.isLoading = false;
      },
      error: (error: any) => {
        if (
          error?.error?.status ===
          this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
        ) {
          // this.myCourseList = [];
        }
        this.isLoading = false;
      },
    });
  }

  changeOfFavoriteCourse(event: any) {
    this.isHeartClick = event;
    this.isFavMenuOpen = event;
  }
  changeOfFavoriteCourseMobile(event: any) {
    this.isHeartClick = event;
    this.isFavMenuOpenMobile = event;
  }
  notificationToggle(event: any) {
    this.isNotificationDropdownVisible = event;
  }
  notificationToggleMobile(event: any) {
    this.isNotificationDropdownVisibleMobile = event;
  }

  get getInitialOfLoggedInUser() {
    return this._authService.getLoggedInName();
  }

  getCourseCategoryList() {
    this._courseService.getCourseCategory().subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.categoryList = response?.data;
        }
      },
      error: (error: any) => {},
    });
  }

  viewNotifications() {
    if (this.isLoggedIn) {
      this.notifications = this._cacheService.getNotifications(
        this._dataHolderConstants.CACHE_KEYS.NOTIFICATION
      );
      this.notifications?.forEach((notification: any) => {
        notification.creationDate = this.timeAgo(
          new Date(notification.creationDate)
        );
      });
    }
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

  removeNotifications() {
    this._notificationService
      .removeNotification(this.notificationIds)
      .subscribe();
    this.notificationIds = [];
  }

  routeToSelectedUrl(notification?: any) {
    document.body.classList.remove('hide-scrollbar');
    this.navbarVisible = false;
    const url = notification?.url;
    const fragment = notification?.type;
    const tree: UrlTree = this._router.parseUrl(url);
    const queryParams = tree.queryParams;

    Object.keys(queryParams).forEach((key) => {
      console.log(`${key}: ${queryParams[key]}`);
    });
    this._router.navigate([url.split('?')[0]], { queryParams, fragment });
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

  routeToSignInScreen() {
    document.body.classList.remove('hide-scrollbar');
    this.navbarVisible = false;
    this._router.navigate(['auth/sign-in']);
  }

  toggleBurgerMenu() {
    const mainContent = document.getElementById('main-content');
    this.navbarVisible = !this.navbarVisible;
    if (this.navbarVisible) {
      document.body.classList.add('hide-scrollbar');
    } else if (this.showSearch) {
      this.toggleSearch();
      return;
    } else {
      document.body.classList.remove('hide-scrollbar');
    }
    this.isMenuOpenMobile = false;
    this.isNotificationDropdownVisibleMobile = false;
    this.isFavMenuOpenMobile = false;
  }

  routeToLandingPage() {
    document.body.classList.remove('hide-scrollbar');
    this.navbarVisible = false;
    this._router.navigate(['']);
  }

  routeToSignUpScreen() {
    document.body.classList.remove('hide-scrollbar');
    this.navbarVisible = false;
    this._router.navigate(['auth/sign-up']);
  }

  routeToInstructorWelcomePage() {
    document.body.classList.remove('hide-scrollbar');
    this.navbarVisible = false;
    this._router.navigate(['welcome-instructor']);
  }

  routeToFavoriteCourses() {
    document.body.classList.remove('hide-scrollbar');
    this.navbarVisible = false;
    this.isFavMenuOpen = false;
    this.isFavMenuOpenMobile = false;
    this.isHeartClick = false;
    this._router.navigate(['student/favorite-courses']);
  }

  routeToMyCourses() {
    document.body.classList.remove('hide-scrollbar');
    this.navbarVisible = false;
    this.isMenuOpen = false;
    this.isMenuOpenMobile = false;
    this._router.navigate(['student/my-courses']);
  }

  routeToUpdateProfile() {
    document.body.classList.remove('hide-scrollbar');
    this.navbarVisible = false;
    this._router.navigate(['user/update-profile']);
    document.body.click();
  }

  routeToUserProfile() {
    const userProfileUrl = this._authService.getUserProfileUrl();
  
    if (!userProfileUrl) {
      console.error('User profile URL is missing. Cannot redirect to the profile page.');
      return;
    }
  
    const sanitizedFullName = userProfileUrl
      .toLowerCase()
      .replace(/\s+/g, '-') 
      .replace(/[^a-z0-9\-]/g, ''); 
    
    this._router.navigate(['user/profile'], { queryParams: { url: sanitizedFullName } });
  }
  
  
  

  routeToSubscription() {
    document.body.classList.remove('hide-scrollbar');
    this.navbarVisible = false;
    this._router.navigate(['subscription']);
  }

  routeToCourseList(category?: any) {
    document.body.classList.remove('hide-scrollbar');
    this.navbarVisible = false;
    this._router.navigate(['student/courses'], {
      queryParams: { selection: category},
    });
  }
  routeToCoursePage() {
    document.body.classList.remove('hide-scrollbar');
    this.navbarVisible = false;
    this._router.navigate(['student/courses']);
  }

  routeToInstructorDashboard() {
    document.body.classList.remove('hide-scrollbar');
    this.navbarVisible = false;
    this._authService.isLoggedIn()
      ? this._router.navigate(['/instructor/instructor-dashboard'])
      : this._router.navigate(['/auth/sign-in']);
  }

  routeToCourseDetails(courseUrl: any) {
    document.body.classList.remove('hide-scrollbar');
    this.navbarVisible = false;
    this._router.navigate(['student/course-details', courseUrl]);
  }
  routeToNotificationPage() {
    this.removeNotificationCount();
    document.body.classList.remove('hide-scrollbar');
    this.navbarVisible = false;
    this.isNotificationDropdownVisible = false;
    this.isNotificationDropdownVisibleMobile = false;
    this._router.navigate(['/user/notifications']);
  }

  signOut() {
    document.body.classList.remove('hide-scrollbar');
    const uniqueId = this._cacheService.getDataFromCache('unique-id');
    this._authService.signOut(uniqueId).subscribe({
      next: (response: any) => {
        this.notifications = [];
        this._cacheService.clearCache();
        this._router.navigate(['']);
        this._authService.changeNavState(false);
        this.myCourseList = [];
        this.favoriteCourseList = [];
        this.myCourses.pageNo = 0;
      },
      error: (error: any) => {},
    });
    this.navbarVisible = false;
    this.showUserToggle = false;
    this.loggedInUser.profilePicture = null;
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

  routeToInstructorProfile(url?: any) {
    this.selectedIndex = -1;
    this.navbarVisible = false;
    this.showSearch = false;
    this.showNoCourseText = false;
    this.searchResults = [];
    this.searchInstructorResults = [];
    const tree: UrlTree = this._router.parseUrl(url);
    const queryParams = tree.queryParams;

    Object.keys(queryParams).forEach((key) => {
      console.log(`${key}: ${queryParams[key]}`);
    });
    this._router.navigate([url?.split('?')[0]], { queryParams });
  }

setLogoWidth() {
  const screenWidth = window.innerWidth;

  if (screenWidth < 360) {
    this.logoWidth = 125;
  } else if (screenWidth < 420) {
    this.logoWidth = 140;
  } else if (screenWidth < 480) {
    this.logoWidth = 170;
  } else {
    this.logoWidth = 190;
  }
}

}

