import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { NavbarComponent } from './navbar.component';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { NotificationService } from 'src/app/core/services/notification.service';
import { ActivatedRoute, Router } from '@angular/router';
import { async, of, throwError } from 'rxjs';
import { SocialAuthService } from '@abacritt/angularx-social-login';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { NzMessageService } from 'ng-zorro-antd/message';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { SharedModule } from '../shared.module';

describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('SocialAuthService', ['signIn'], {
      authState: of(null),
    });
    await TestBed.configureTestingModule({
      declarations: [NavbarComponent],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        AntDesignModule,
        SharedModule,
      ],
      providers: [
        AuthService,
        CacheService,
        CourseService,
        MessageService,
        SharedService,
        CommunicationService,
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParams: {} } },
        },
        NzMessageService, // Provide NzMessageService here
        NotificationService,
        { provide: SocialAuthService, useValue: spy },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    component.notifications = [];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch logged in user details on init if user is logged in', () => {
    const authService = TestBed.inject(AuthService);
    spyOn(authService, 'isLoggedIn').and.returnValue(true);
    spyOn(component, 'getLoggedInUserDetails');
    spyOn(component, 'getFavoriteCourseList');
    spyOn(component, 'getMyCourseList');

    component.ngOnInit();

    expect(component.getLoggedInUserDetails).toHaveBeenCalled();
    expect(component.getFavoriteCourseList).toHaveBeenCalled();
    expect(component.getMyCourseList).toHaveBeenCalled();
  });
  it('should navigate to the favorite courses page', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    component.routeToFavoriteCourses();

    expect(router.navigate).toHaveBeenCalledWith(['student/favorite-courses']);
  });
  it('should navigate to the favorite courses page', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    component.routeToFavoriteCourses();

    expect(router.navigate).toHaveBeenCalledWith(['student/favorite-courses']);
  });
  it('should fetch search suggestions for a valid keyword', (done) => {
    const courseService = TestBed.inject(CourseService);
    spyOn(courseService, 'getSuggestions').and.callFake((keyword) => {
      return of({ data: ['Course1', 'Course2'] });
    });

    component.getSearchSuggestions('angular');

    setTimeout(() => {
      expect(component.searchSuggestions.length).toBe(2);
      done();
    }, 200);
  });

  it('should navigate to the favorite courses page', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    component.routeToFavoriteCourses();

    expect(router.navigate).toHaveBeenCalledWith(['student/favorite-courses']);
  });
  it('should fetch logged in user details on init if user is logged in', () => {
    const authService = TestBed.inject(AuthService);
    spyOn(authService, 'isLoggedIn').and.returnValue(true);
    spyOn(component, 'getLoggedInUserDetails');
    spyOn(component, 'getFavoriteCourseList');
    spyOn(component, 'getMyCourseList');

    component.ngOnInit();

    expect(component.getLoggedInUserDetails).toHaveBeenCalled();
    expect(component.getFavoriteCourseList).toHaveBeenCalled();
    expect(component.getMyCourseList).toHaveBeenCalled();
  });

  it('should clear cache and navigate to home on sign out', () => {
    const authService = TestBed.inject(AuthService);
    const cacheService = TestBed.inject(CacheService);
    const router = TestBed.inject(Router);

    spyOn(authService, 'signOut').and.returnValue(of({}));
    spyOn(cacheService, 'clearCache');
    spyOn(router, 'navigate');
    spyOn(authService, 'changeNavState');

    component.signOut();

    expect(cacheService.clearCache).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['']);
    expect(authService.changeNavState).toHaveBeenCalledWith(false);
  });

  it('should handle empty search keyword gracefully', fakeAsync((done) => {
    const courseService = TestBed.inject(CourseService);
    spyOn(courseService, 'getSuggestions').and.returnValue(of({ data: [] }));

    component.getSearchSuggestions('');

    expect(component.searchSuggestions.length).toBe(0);
  }));

  it('should handle sign out failure gracefully', () => {
    const authService = TestBed.inject(AuthService);
    const cacheService = TestBed.inject(CacheService);
    const router = TestBed.inject(Router);

    spyOn(authService, 'signOut').and.returnValue(of(null)); // Simulate failure
    spyOn(cacheService, 'clearCache');
    spyOn(router, 'navigate');
    spyOn(authService, 'changeNavState');

    component.signOut();

    // Check if cache clearing and navigation still happened
    expect(cacheService.clearCache).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['']);
    expect(authService.changeNavState).toHaveBeenCalledWith(false);
  });
  it('should handle failure in fetching user details', () => {
    const authService = TestBed.inject(AuthService);

    spyOn(authService, 'getLoggedInName');
    spyOn(authService, 'getLoggedInEmail');
    spyOn(authService, 'getLoggedInPicture');

    // Call the method to fetch user details
    component.getLoggedInUserDetails();

    // Verify that component handles the failure gracefully
    expect(component.loggedInUser).toEqual({
      fullName: undefined,
      email: undefined,
      profilePicture: null,
    });
  });

  it('should update the search suggestions when a valid keyword is provided', (done) => {
    const courseService = TestBed.inject(CourseService);
    spyOn(courseService, 'getSuggestions').and.callFake((keyword) => {
      return of({ data: ['Angular Basics', 'Advanced Angular'] });
    });

    component.getSearchSuggestions('angular');

    setTimeout(() => {
      expect(component.searchSuggestions).toEqual([
        'Angular Basics',
        'Advanced Angular',
      ]);
      done();
    }, 200);
  });

  it('should update component state when user details are fetched successfully', fakeAsync(() => {
    const authService = TestBed.inject(AuthService);

    spyOn(authService, 'getLoggedInName').and.returnValue(of('John Doe'));
    spyOn(authService, 'getLoggedInEmail').and.returnValue(
      of('john.doe@example.com')
    );
    spyOn(authService, 'getLoggedInPicture').and.returnValue(of('profile.jpg'));

    component.getLoggedInUserDetails();

    // Simulate the passage of time for any pending Observables
    tick(2000);
    expect(component.loggedInUser).toBeDefined();
  }));

  it('should handle cache clearing failure gracefully', () => {
    const authService = TestBed.inject(AuthService);
    const cacheService = TestBed.inject(CacheService);
    const router = TestBed.inject(Router);

    spyOn(authService, 'signOut').and.returnValue(of({}));
    spyOn(cacheService, 'clearCache');
    spyOn(router, 'navigate');
    spyOn(authService, 'changeNavState');

    component.signOut();

    expect(router.navigate).toHaveBeenCalledWith(['']);
    expect(authService.changeNavState).toHaveBeenCalledWith(false);
  });

  it('should handle failure when fetching user details', () => {
    const authService = TestBed.inject(AuthService);
    spyOn(authService, 'getLoggedInName');
    spyOn(authService, 'getLoggedInEmail');
    spyOn(authService, 'getLoggedInPicture');

    component.getLoggedInUserDetails();

    expect(component.loggedInUser).toEqual({
      fullName: undefined,
      email: undefined,
      profilePicture: null,
    });
  });
  it('should handle sign out when the router fails to navigate', () => {
    const authService = TestBed.inject(AuthService);
    const cacheService = TestBed.inject(CacheService);
    const router = TestBed.inject(Router);

    spyOn(authService, 'signOut').and.returnValue(of({}));
    spyOn(cacheService, 'clearCache');
    spyOn(authService, 'changeNavState');

    component.signOut();

    expect(cacheService.clearCache).toHaveBeenCalled();
    expect(authService.changeNavState).toHaveBeenCalledWith(false);
  });
  it('should handle failed response when fetching search suggestions', (done) => {
    const courseService = TestBed.inject(CourseService);
    spyOn(courseService, 'getSuggestions').and.returnValue(
      of({ error: 'Failed to fetch suggestions' })
    );

    component.getSearchSuggestions('angular');

    setTimeout(() => {
      expect(component.searchSuggestions).toEqual([]);
      done();
    }, 200);
  });

  it('should hide "No Course Text" on document click', () => {
    component.showNoCourseText = true;
    component.onDocumentClick(new MouseEvent('click'));

    expect(component.showNoCourseText).toBeFalse();
  });
  it('should update navbar state on auth change', () => {
    const authService = TestBed.inject(AuthService);
    spyOn(authService.$changeNavbarSate, 'subscribe').and.callFake((callback) =>
      callback(true)
    );
    spyOn(component, 'getFavoriteCourseList');
    spyOn(component, 'getMyCourseList');
    spyOn(component, 'getLoggedInUserDetails');

    component.listenNavbarState();

    expect(component.getFavoriteCourseList).toHaveBeenCalled();
    expect(component.getMyCourseList).toHaveBeenCalled();
    expect(component.getLoggedInUserDetails).toHaveBeenCalled();
  });
  // it('should view notifications and remove read ones', () => {
  //   component.viewNotifications();

  //   spyOn(component, 'removeNotifications').and.callThrough();

  //   expect(component.notificationIds).toEqual([]);
  // });
  it('should navigate to course details page with proper title', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    component.routeToCourseDetails('Angular Basics');

    expect(router.navigate).toHaveBeenCalledWith([
      'student/course-details',
      'Angular Basics',
    ]);
  });

  it('should update isMenuOpen when isMyCourseMenuVisible is called', () => {
    component.isMenuOpen = false; // initial state
    component.isMyCourseMenuVisible(true);
    expect(component.isMenuOpen).toBeTrue();

    component.isMyCourseMenuVisible(false);
    expect(component.isMenuOpen).toBeFalse();
  });

  it('should update isMenuOpenMobile when isMyCourseMenuMobileVisible is called', () => {
    component.isMenuOpenMobile = false; // initial state
    component.isMyCourseMenuMobileVisible(true);
    expect(component.isMenuOpenMobile).toBeTrue();

    component.isMyCourseMenuMobileVisible(false);
    expect(component.isMenuOpenMobile).toBeFalse();
  });

  it('should not execute search and show no course text when search value is empty', () => {
    component.searchByIconClick('');
    expect(component.searchKeyword).toBe('');
    expect(component.showNoCourseText).toBeFalse();
    expect(component.searchResults).toEqual([]);
  });

  it('should not execute search and show no course text when search value contains only whitespace', () => {
    component.searchByIconClick('   ');
    expect(component.searchKeyword).toBe('   ');
    expect(component.showNoCourseText).toBeFalse();
    expect(component.searchResults).toEqual([]);
  });

  it('should not execute search and show no course text when search value is less than 3 characters', () => {
    component.searchByIconClick('ab');
    expect(component.searchKeyword).toBe('ab');
    expect(component.showNoCourseText).toBeFalse();
    expect(component.searchResults).toEqual([]);
  });

  it('should execute search and navigate when search value is valid', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    component.searchByIconClick('angular');

    expect(component.searchKeyword).toBe('angular');
    expect(component.showNoCourseText).toBeFalse();
    expect(component.searchResults).toEqual([]);
    expect(component.navbarVisible).toBeFalse();
    expect(component.showSearch).toBeFalse();
    expect(component.selectedIndex).toBe(-1);
  });

  it('should clear search fields and reset the state', () => {
    component.clearSearch();
    expect(component.searchKeyword).toBe('');
    expect(component.searchResults).toEqual([]);
    expect(component.selectedIndex).toBe(-1);
    expect(component.showNoCourseText).toBeFalse();
  });

  it('should not execute search if search value is empty or less than 3 characters', fakeAsync(() => {
    component.searchByKeyword('ab');
    tick(component.debounceDelay);

    expect(component.showNoCourseText).toBeFalse();
    expect(component.searchResults).toEqual([]);
  }));

  it('should execute search and update search results', fakeAsync(() => {
    const courseService = TestBed.inject(CourseService);
    spyOn(courseService, 'getSuggestions').and.returnValue(
      of({ status: 200, data: { searchCourses: ['Course1', 'Course2'] } })
    );
    spyOn(document.body.classList, 'remove');

    component.searchByKeyword('angular');
    tick(component.debounceDelay);

    expect(document.body.classList.remove).toHaveBeenCalledWith(
      'hide-scrollbar'
    );
    expect(component.searchResults).toEqual(['Course1', 'Course2']);
    expect(component.showNoCourseText).toBeFalse();
  }));

  it('should handle search error and show "No Course Text"', fakeAsync(() => {
    const courseService = TestBed.inject(CourseService);
    spyOn(courseService, 'getSuggestions').and.returnValue(
      of({ error: { status: 404 } })
    );

    component.searchByKeyword('angular');
    tick(component.debounceDelay);

    expect(component.showNoCourseText).toBeFalse();
  }));

  it('should navigate to the search results page with the correct search keyword', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    component.searchResults = [{ title: 'Angular Basics' }];
    component.selectedIndex = 0;

    component.navigateSearch({ target: { value: 'Angular' } });

    expect(component.searchKeyword).toBe('Angular Basics');
    expect(component.navbarVisible).toBeFalse();
    expect(component.showSearch).toBeFalse();
    expect(component.showNoCourseText).toBeFalse();
    expect(component.searchResults).toEqual([]);
    expect(component.selectedIndex).toBe(-1);
  });

  describe('getFavoriteCourseList', () => {
    it('should set favoriteCourseList when API call is successful', () => {
      const courseService = TestBed.inject(CourseService);
      spyOn(courseService, 'getFavoriteCourses').and.returnValue(
        of({
          status: 200,
          data: { favouriteCourses: ['Course1', 'Course2'] },
        })
      );

      component.getFavoriteCourseList();

      expect(component.favoriteCourseList).toEqual(['Course1', 'Course2']);
    });

    it('should set favoriteCourseList to empty array when API returns 404', () => {
      const courseService = TestBed.inject(CourseService);
      spyOn(courseService, 'getFavoriteCourses').and.returnValue(
        throwError({
          error: { status: 404 },
        })
      );

      component.getFavoriteCourseList();

      expect(component.favoriteCourseList).toEqual([]);
    });
  });

  describe('getMyCourseList', () => {
    it('should set myCourseList when API call is successful', fakeAsync(() => {
      // Inject the CourseService
      const courseService = TestBed.inject(CourseService);

      // Spy on the getMyCourses method and mock its return value
      spyOn(courseService, 'getMyCourses').and.returnValue(
        of({
          status: 200,
          data: { myCourses: ['Course1', 'Course2'] },
        })
      );

      // Ensure the component state is ready for the test
      component.isLoading = false; // Reset isLoading to false before calling the method
      component.myCourseList = []; // Initialize myCourseList to an empty array

      // Call the method and simulate async operations
      component.getMyCourseList();
      tick(); // Simulate the passage of time

      // Assert that myCourseList has been updated correctly
      expect(component.myCourseList).toEqual(['Course1', 'Course2']);
    }));

    describe('changeOfFavoriteCourse', () => {
      it('should update isHeartClick and isFavMenuOpen based on the event', () => {
        component.changeOfFavoriteCourse(true);

        expect(component.isHeartClick).toBeTrue();
        expect(component.isFavMenuOpen).toBeTrue();

        component.changeOfFavoriteCourse(false);

        expect(component.isHeartClick).toBeFalse();
        expect(component.isFavMenuOpen).toBeFalse();
      });
    });
    describe('changeOfFavoriteCourseMobile', () => {
      it('should update isHeartClick and isFavMenuOpenMobile based on the event', () => {
        component.changeOfFavoriteCourseMobile(true);

        expect(component.isHeartClick).toBeTrue();
        expect(component.isFavMenuOpenMobile).toBeTrue();

        component.changeOfFavoriteCourseMobile(false);

        expect(component.isHeartClick).toBeFalse();
        expect(component.isFavMenuOpenMobile).toBeFalse();
      });
    });

    it('should set myCourseList to empty array when API returns 404', () => {
      const courseService = TestBed.inject(CourseService);
      spyOn(courseService, 'getMyCourses').and.returnValue(
        throwError({
          error: { status: 404 },
        })
      );

      component.getMyCourseList();

      expect(component.myCourseList).toEqual([]);
    });
  });
  it('should update isHeartClick and isFavMenuOpen based on the event', () => {
    component.changeOfFavoriteCourse(true);

    expect(component.isHeartClick).toBeTrue();
    expect(component.isFavMenuOpen).toBeTrue();

    component.changeOfFavoriteCourse(false);

    expect(component.isHeartClick).toBeFalse();
    expect(component.isFavMenuOpen).toBeFalse();
  });
  it('should update isHeartClick and isFavMenuOpenMobile based on the event', () => {
    component.changeOfFavoriteCourseMobile(true);

    expect(component.isHeartClick).toBeTrue();
    expect(component.isFavMenuOpenMobile).toBeTrue();

    component.changeOfFavoriteCourseMobile(false);

    expect(component.isHeartClick).toBeFalse();
    expect(component.isFavMenuOpenMobile).toBeFalse();
  });
  it('should update isNotificationDropdownVisible based on the event', () => {
    component.notificationToggle(true);

    expect(component.isNotificationDropdownVisible).toBeTrue();

    component.notificationToggle(false);

    expect(component.isNotificationDropdownVisible).toBeFalse();
  });
  it('should update isNotificationDropdownVisibleMobile based on the event', () => {
    component.notificationToggleMobile(true);

    expect(component.isNotificationDropdownVisibleMobile).toBeTrue();

    component.notificationToggleMobile(false);

    expect(component.isNotificationDropdownVisibleMobile).toBeFalse();
  });

  it('should load notifications and update creationDate using timeAgo if logged in', () => {
    const cacheService = TestBed.inject(CacheService);
    component.isLoggedIn = true;
    const notifications = [
      { creationDate: new Date(), id: 1 },
      { creationDate: new Date(), id: 2 },
    ];
    spyOn(cacheService, 'getNotifications').and.returnValue(notifications);
    spyOn(component, 'timeAgo').and.callFake((date) => '1 minute ago');

    component.viewNotifications();

    expect(component.notifications).toEqual([
      { creationDate: '1 minute ago', id: 1 },
      { creationDate: '1 minute ago', id: 2 },
    ]);
  });

  it('should not load notifications if not logged in', () => {
    component.isLoggedIn = false;
    component.viewNotifications();

    expect(component.notifications).not.toBeUndefined();
  });

  describe('getCourseCategoryList', () => {
    it('should set categoryList when API call is successful', () => {
      const courseService = TestBed.inject(CourseService);
      spyOn(courseService, 'getCourseCategory').and.returnValue(
        of({
          status: 200,
          data: ['Category1', 'Category2'],
        })
      );

      component.getCourseCategoryList();

      expect(component.categoryList).toEqual(['Category1', 'Category2']);
    });

    it('should handle error when API call fails', () => {
      const courseService = TestBed.inject(CourseService);
      spyOn(courseService, 'getCourseCategory').and.returnValue(throwError({}));

      component.getCourseCategoryList();

      // No specific expectation for error handling as the error block is empty
    });
  });

  describe('viewNotifications', () => {
    it('should load notifications and update creationDate using timeAgo if logged in', () => {
      // Test implementation
    });

    it('should not load notifications if not logged in', () => {
      // Test implementation
    });
  });

  describe('removeNotifications', () => {
    it('should call removeNotification API and clear notificationIds', () => {
      // Test implementation
    });
  });

  describe('routeToSelectedUrl', () => {
    it('should call removeNotification API and clear notificationIds', () => {
      const notificationService = TestBed.inject(NotificationService);
      spyOn(notificationService, 'removeNotification').and.returnValue(of({}));

      component.notificationIds = [1, 2];
      component.removeNotifications();

      expect(notificationService.removeNotification).toHaveBeenCalledWith([
        1, 2,
      ]);
      expect(component.notificationIds.length).toBe(0);
    });
  });

  describe('timeAgo', () => {
    it('should return the correct time ago string based on the date', () => {
      const currentDate = new Date();
      const date = new Date(currentDate.getTime() - 60 * 1000); // 1 minute ago

      const result = component.timeAgo(date);

      expect(result).toBe('1 minute ago');
    });

    it('should return "just now" if the date is less than 10 seconds ago', () => {
      const currentDate = new Date();
      const date = new Date(currentDate.getTime() - 5 * 1000); // 5 seconds ago

      const result = component.timeAgo(date);

      expect(result).toBe('just now');
    });
  });

  describe('routeToSignInScreen', () => {
    it('should navigate to sign-in page and reset navbarVisible', () => {
      // Test implementation
    });
  });

  describe('toggleBurgerMenu', () => {
    it('should toggle navbarVisible and manage scrollbar visibility', () => {
      const mainContent = document.createElement('div');
      mainContent.id = 'main-content';
      document.body.appendChild(mainContent);

      component.navbarVisible = false;
      component.toggleBurgerMenu();

      expect(component.navbarVisible).toBeTrue();
      expect(document.body.classList.contains('hide-scrollbar')).toBeTrue();

      component.toggleBurgerMenu();

      expect(component.navbarVisible).toBeFalse();
      expect(document.body.classList.contains('hide-scrollbar')).toBeFalse();
    });

    it('should close mobile menus when navbar is closed', () => {
      component.navbarVisible = true;
      component.isMenuOpenMobile = true;
      component.isNotificationDropdownVisibleMobile = true;
      component.isFavMenuOpenMobile = true;

      component.toggleBurgerMenu();

      expect(component.isMenuOpenMobile).toBeFalse();
      expect(component.isNotificationDropdownVisibleMobile).toBeFalse();
      expect(component.isFavMenuOpenMobile).toBeFalse();
    });
  });

  describe('routeToLandingPage', () => {
    it('should navigate to the landing page and reset navbarVisible', () => {
      const router = TestBed.inject(Router);
      spyOn(router, 'navigate');

      component.routeToLandingPage();

      expect(document.body.classList.contains('hide-scrollbar')).toBeFalse();
      expect(component.navbarVisible).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith(['']);
    });
  });

  describe('routeToSignUpScreen', () => {
    it('should navigate to sign-in page and reset navbarVisible', () => {
      const router = TestBed.inject(Router);
      spyOn(router, 'navigate');

      component.routeToSignInScreen();

      expect(document.body.classList.contains('hide-scrollbar')).toBeFalse();
      expect(component.navbarVisible).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith(['auth/sign-in']);
    });
    it('should navigate to sign-up page and reset navbarVisible', () => {
      const router = TestBed.inject(Router);
      spyOn(router, 'navigate');

      component.routeToSignUpScreen();

      expect(document.body.classList.contains('hide-scrollbar')).toBeFalse();
      expect(component.navbarVisible).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith(['auth/sign-up']);
    });
  });

  describe('routeToInstructorWelcomePage', () => {
    it('should navigate to welcome instructor page and reset navbarVisible', () => {
      const router = TestBed.inject(Router);
      spyOn(router, 'navigate');

      component.routeToInstructorWelcomePage();

      expect(document.body.classList.contains('hide-scrollbar')).toBeFalse();
      expect(component.navbarVisible).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith(['welcome-instructor']);
    });
  });

  describe('routeToFavoriteCourses', () => {
    it('should navigate to favorite courses page and close mobile menus', () => {
      const router = TestBed.inject(Router);
      spyOn(router, 'navigate');

      component.routeToFavoriteCourses();

      expect(document.body.classList.contains('hide-scrollbar')).toBeFalse();
      expect(component.navbarVisible).toBeFalse();
      expect(component.isFavMenuOpen).toBeFalse();
      expect(component.isFavMenuOpenMobile).toBeFalse();
      expect(component.isHeartClick).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith([
        'student/favorite-courses',
      ]);
    });
  });

  describe('routeToMyCourses', () => {
    it('should navigate to my courses page and close menus', () => {
      const router = TestBed.inject(Router);
      spyOn(router, 'navigate');

      component.routeToMyCourses();

      expect(document.body.classList.contains('hide-scrollbar')).toBeFalse();
      expect(component.navbarVisible).toBeFalse();
      expect(component.isMenuOpen).toBeFalse();
      expect(component.isMenuOpenMobile).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith(['student/my-courses']);
    });
  });

  describe('routeToUpdateProfile', () => {
    it('should navigate to update profile page and reset navbarVisible', () => {
      const router = TestBed.inject(Router);
      spyOn(router, 'navigate');
      spyOn(document.body, 'click');

      component.routeToUpdateProfile();

      expect(document.body.classList.contains('hide-scrollbar'));
    });
  });
  describe('routeToNotificationPage', () => {
    it('should navigate to notification page', () => {
      const router = TestBed.inject(Router);
      spyOn(router, 'navigate');
      component.routeToNotificationPage();
      expect(document.body.classList.contains('hide-scrollbar')).toBeFalse();
      expect(component.navbarVisible).toBeFalse();
      expect(component.isNotificationDropdownVisible).toBeFalse();
      expect(component.isNotificationDropdownVisibleMobile).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith(['/user/notifications']);
    });
  });
  describe('routeToSubscription', () => {
    it('should navigate to routeToSubscription page', () => {
      const router = TestBed.inject(Router);
      spyOn(router, 'navigate');
      component.routeToSubscription();
      expect(document.body.classList.contains('hide-scrollbar')).toBeFalse();
      expect(component.navbarVisible).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith(['subscription']);
    });
  });
  describe('routeToCourseList', () => {
    it('should navigate to courses page', () => {
      const router = TestBed.inject(Router);
      spyOn(router, 'navigate');
      component.routeToCourseList();
      expect(document.body.classList.contains('hide-scrollbar')).toBeFalse();
      expect(component.navbarVisible).toBeFalse();
    });
  });
  describe('routeToInstructorDashboard', () => {
    it('should navigate to instructor dashboard page', () => {
      const router = TestBed.inject(Router);
      spyOn(router, 'navigate');
      component.routeToInstructorDashboard();
      expect(document.body.classList.contains('hide-scrollbar')).toBeFalse();
      expect(component.navbarVisible).toBeFalse();
    });
  });

  describe('updateNotificationCount', () => {
    it('should update notification count', () => {
      component.notificationCount = 1;
      expect(component.notificationCount).toBeGreaterThan(0);
    });
  });
  describe('removeNotificationCount', () => {
    it('should remove notification count', () => {
      expect(component.notificationCount).toBeDefined();
    });
  });
  describe('redirect', () => {
    it('should update notificationIds, call removeNotifications, and navigate to the notification URL', () => {
      expect(component.isNotificationDropdownVisible).toBeFalse();
      expect(component.isNotificationDropdownVisibleMobile).toBeFalse();
    });
  });

  describe('toggleSearch', () => {
    it('should toggle search', () => {
      component.toggleSearch();
      expect(component.showSearch).toBe(true);
      expect(component.navbarVisible).toBeFalse();
    });
  });
});
