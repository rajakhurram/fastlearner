import { TestBed } from '@angular/core/testing';
import { AuthGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';
import { CacheService } from '../services/cache.service';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import {
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  UrlSegment,
  Route,
} from '@angular/router';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let authService: jasmine.SpyObj<AuthService>;
  let cacheService: jasmine.SpyObj<CacheService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', ['isLoggedIn']);
    cacheService = jasmine.createSpyObj('CacheService', ['saveInCache']);
    router = jasmine.createSpyObj('Router', ['createUrlTree']);

    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: AuthService, useValue: authService },
        { provide: CacheService, useValue: cacheService },
        { provide: Router, useValue: router },
      ],
    });

    guard = TestBed.inject(AuthGuard);
  });

  describe('canActivate', () => {
    let route: ActivatedRouteSnapshot;
    let state: RouterStateSnapshot;

    beforeEach(() => {
      route = new ActivatedRouteSnapshot();
      state = { url: '' } as RouterStateSnapshot;
    });

    it('should allow access if user is logged in and not on auth page', () => {
      authService.isLoggedIn.and.returnValue(true);
      state.url = '/home';

      expect(guard.canActivate(route, state)).toBeTrue();
    });

    it('should redirect to home if user is logged in and on auth page', () => {
      authService.isLoggedIn.and.returnValue(true);
      state.url = '/auth/sign-in';

      expect(guard.canActivate(route, state)).toEqual(
        router.createUrlTree(['/'])
      );
    });

    it('should allow access to sign-in if user is not logged in', () => {
      authService.isLoggedIn.and.returnValue(false);
      state.url = '/auth/sign-in';

      expect(guard.canActivate(route, state)).toBeTrue();
    });

    it('should redirect to sign-in if user is not logged in and trying to access other routes', () => {
      authService.isLoggedIn.and.returnValue(false);
      state.url = '/restricted';

      expect(guard.canActivate(route, state)).toEqual(
        router.createUrlTree(['/auth/sign-in'])
      );
      expect(cacheService.saveInCache).toHaveBeenCalledWith(
        'redirectUrl',
        state.url
      );
    });
  });

  describe('canActivateChild', () => {
    let childRoute: ActivatedRouteSnapshot;
    let state: RouterStateSnapshot;

    beforeEach(() => {
      childRoute = new ActivatedRouteSnapshot();
      state = { url: '/parent' } as RouterStateSnapshot;
    });

    it('should allow access to child routes', () => {
      expect(guard.canActivateChild(childRoute, state)).toBeTrue();
    });
  });

  describe('canDeactivate', () => {
    it('should allow deactivation of any component', () => {
      expect(
        guard.canDeactivate(
          {},
          {} as ActivatedRouteSnapshot,
          {} as RouterStateSnapshot
        )
      ).toBeTrue();
    });
  });

  describe('canMatch', () => {
    it('should allow matching of any route', () => {
      const route: Route = { path: 'some-path' };
      const segments: UrlSegment[] = [new UrlSegment('some-path', {})];

      expect(guard.canMatch(route, segments)).toBeTrue();
    });
  });

  describe('canLoad', () => {
    let route: Route;
    let segments: UrlSegment[];

    beforeEach(() => {
      route = { path: 'auth' };
      segments = [new UrlSegment('auth', {})];
    });

    it('should allow loading auth routes if not logged in', () => {
      authService.isLoggedIn.and.returnValue(false);
      expect(guard.canLoad(route, segments)).toBeTrue();
    });

    it('should redirect to sign-in if not logged in and trying to access non-allowed routes', () => {
      authService.isLoggedIn.and.returnValue(false);
      segments = [new UrlSegment('some-path', {})];

      expect(guard.canLoad(route, segments)).toEqual(
        router.createUrlTree(['/auth/sign-in'])
      );
      expect(cacheService.saveInCache).toHaveBeenCalledWith(
        'redirectUrl',
        'some-path'
      );
    });

    it('should redirect to home if logged in and trying to access auth routes', () => {
      authService.isLoggedIn.and.returnValue(true);
      segments = [new UrlSegment('auth', {})];

      expect(guard.canLoad(route, segments)).toEqual(
        router.createUrlTree(['/'])
      );
    });

    it('should allow access to specific routes even if not logged in', () => {
      authService.isLoggedIn.and.returnValue(false);
      segments = [new UrlSegment('courses', {})];

      expect(guard.canLoad(route, segments)).toBeUndefined();
    });
  });
});
