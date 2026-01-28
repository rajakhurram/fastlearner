import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, NavigationEnd } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AppComponent } from './app.component';
import { AuthService } from './core/services/auth.service';
import { UserService } from './core/services/user.service';
import { CacheService } from './core/services/cache.service';
import { HttpConstants } from './core/constants/http.constants';
import { Gtag, GtagModule } from 'angular-gtag';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { HttpBackend, HttpClient, HttpHandler } from '@angular/common/http';
import { SocialAuthService } from '@abacritt/angularx-social-login';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { LottieModule } from 'ngx-lottie';
import { NgxUiLoaderHttpModule, NgxUiLoaderModule } from 'ngx-ui-loader';
import { SharedModule } from './modules/shared/shared.module';
import { AntDesignModule } from './ui-library/ant-design/ant-design.module';
import { GoogleTagManagerModule } from 'angular-google-tag-manager';
import { StateService } from './core/services/state.service';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let router: Router;
  let authService: AuthService;
  let userService: UserService;
  let cacheService: CacheService;
  let gtag: Gtag;
  let googleStateService: StateService;
  let socialAuthServiceSpy: jasmine.SpyObj<SocialAuthService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('SocialAuthService', ['signIn'], {
      authState: of(null),
    });
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([]),
        GtagModule.forRoot({
          trackingId: 'G-SGQM3551LR',
          trackPageviews: true,
        }),
        GoogleTagManagerModule.forRoot({
          id: 'GTM-W48M5MLM',
        }),
        LottieModule,
        AntDesignModule,
        BrowserAnimationsModule,
        NgxUiLoaderHttpModule,
        NgxUiLoaderModule,
        SharedModule,
      ],
      declarations: [AppComponent],
      providers: [
        AuthService,
        UserService,
        CacheService,
        StateService,

        HttpClient,
        HttpHandler,
        HttpBackend,

        { provide: SocialAuthService, useValue: spy },
        HttpConstants,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    authService = TestBed.inject(AuthService);
    userService = TestBed.inject(UserService);
    cacheService = TestBed.inject(CacheService);
    gtag = TestBed.inject(Gtag);
    googleStateService = TestBed.inject(StateService);

    fixture.detectChanges();
  });

  describe('isUserLoggedIn', () => {
    it('should set isLoggedIn and call getUserCompleteProfile if logged in', () => {
      spyOn(authService, 'isLoggedIn').and.returnValue(true);
      const getUserProfileSpy = spyOn(
        component,
        'getUserCompleteProfile'
      ).and.callThrough();

      component.isUserLoggedIn();

      expect(component.isLoggedIn).toBeTrue();
      expect(getUserProfileSpy).toHaveBeenCalled();
    });

    it('should not call getUserCompleteProfile if not logged in', () => {
      spyOn(authService, 'isLoggedIn').and.returnValue(false);
      const getUserProfileSpy = spyOn(
        component,
        'getUserCompleteProfile'
      ).and.callThrough();

      component.isUserLoggedIn();

      expect(component.isLoggedIn).toBeFalse();
      expect(getUserProfileSpy).not.toHaveBeenCalled();
    });
  });

  describe('getUserCompleteProfile', () => {
    it('should save user profile in cache on success', () => {
      const response = { status: 200, data: {} };
      spyOn(userService, 'getUserProfile').and.returnValue(of(response));
      const saveInCacheSpy = spyOn(
        cacheService,
        'saveInCache'
      ).and.callThrough();

      component.getUserCompleteProfile();

      expect(saveInCacheSpy).toHaveBeenCalledWith(
        'userProfile',
        JSON.stringify(response.data)
      );
    });

    it('should handle error without throwing', () => {
      spyOn(userService, 'getUserProfile').and.returnValue(throwError({}));
      const saveInCacheSpy = spyOn(
        cacheService,
        'saveInCache'
      ).and.callThrough();

      component.getUserCompleteProfile();

      expect(saveInCacheSpy).not.toHaveBeenCalled();
    });
  });

  describe('showOrHideNavbar', () => {
    it('should hide navbar on specific routes', () => {
      // Create a mock router event observable
      const mockEvents = of(
        new NavigationEnd(1, '/instructor/course', '/instructor/course')
      );
      spyOn(router.events, 'subscribe').and.callFake(
        (callback: (value: NavigationEnd) => void) => {
          mockEvents.subscribe(callback);
          return { unsubscribe: () => {} } as any; // Return a dummy subscription object
        }
      );

      fixture.detectChanges(); // Trigger change detection

      expect(component.showNavbar).toBeTrue();
    });

    it('should show navbar on other routes', () => {
      // Create a mock router event observable
      const mockEvents = of(new NavigationEnd(1, '/', '/'));
      spyOn(router.events, 'subscribe').and.callFake(
        (callback: (value: NavigationEnd) => void) => {
          mockEvents.subscribe(callback);
          return { unsubscribe: () => {} } as any; // Return a dummy subscription object
        }
      );

      fixture.detectChanges(); // Trigger change detection

      expect(component.showNavbar).toBeTrue();
    });
  });
  
  describe('isUserLoggedIn', () => {
    it('should set isLoggedIn to true and call getUserCompleteProfile if user is logged in', () => {
      spyOn(authService, 'isLoggedIn').and.returnValue(true);
      const getUserCompleteProfileSpy = spyOn(
        component,
        'getUserCompleteProfile'
      ).and.callThrough();

      component.isUserLoggedIn();

      expect(component.isLoggedIn).toBeTrue();
      expect(getUserCompleteProfileSpy).toHaveBeenCalled();
    });

    it('should set isLoggedIn to false and not call getUserCompleteProfile if user is not logged in', () => {
      spyOn(authService, 'isLoggedIn').and.returnValue(false);
      const getUserCompleteProfileSpy = spyOn(
        component,
        'getUserCompleteProfile'
      ).and.callThrough();

      component.isUserLoggedIn();

      expect(component.isLoggedIn).toBeFalse();
      expect(getUserCompleteProfileSpy).not.toHaveBeenCalled();
    });
  });

  describe('getUserCompleteProfile', () => {
    it('should save user profile in cache on success', () => {
      const response = { status: 200, data: { name: 'John Doe' } };
      spyOn(userService, 'getUserProfile').and.returnValue(of(response));
      const saveInCacheSpy = spyOn(
        cacheService,
        'saveInCache'
      ).and.callThrough();

      component.getUserCompleteProfile();

      expect(saveInCacheSpy).toHaveBeenCalledWith(
        'userProfile',
        JSON.stringify(response.data)
      );
    });

    it('should not call saveInCache on error', () => {
      spyOn(userService, 'getUserProfile').and.returnValue(throwError({}));
      const saveInCacheSpy = spyOn(
        cacheService,
        'saveInCache'
      ).and.callThrough();

      component.getUserCompleteProfile();

      expect(saveInCacheSpy).not.toHaveBeenCalled();
    });

    it('should not save user profile if response status is not 200', () => {
      const response = { status: 500, data: {} };
      spyOn(userService, 'getUserProfile').and.returnValue(of(response));
      const saveInCacheSpy = spyOn(
        cacheService,
        'saveInCache'
      ).and.callThrough();

      component.getUserCompleteProfile();

      expect(saveInCacheSpy).not.toHaveBeenCalled();
    });
  });

  
});
