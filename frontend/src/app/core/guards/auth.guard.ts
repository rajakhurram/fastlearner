import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  CanActivateChild,
  CanDeactivate,
  CanLoad,
  CanMatch,
  Route,
  Router,
  RouterStateSnapshot,
  UrlSegment,
  UrlTree,
} from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { CacheService } from '../services/cache.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard
  implements
    CanActivate,
    CanActivateChild,
    CanDeactivate<unknown>,
    CanLoad,
    CanMatch
{
  constructor(
    private _router: Router,
    private _authService: AuthService,
    private _cacheService: CacheService
  ) {}

  // canActivate(
  //   route: ActivatedRouteSnapshot,
  //   state: RouterStateSnapshot
  // ):
  //   | Observable<boolean | UrlTree>
  //   | Promise<boolean | UrlTree>
  //   | boolean
  //   | UrlTree {
  //   const isLoggedIn = this._authService.isLoggedIn();

  //   if (isLoggedIn) {
  //     // Prevent redirect if already on an auth page
  //     if (state.url.startsWith('/auth')) {
  //       return this._router.createUrlTree(['/']); // Redirect to home
  //     }
  //     return true; // Allow access
  //   } else {
  //     // Check if already on the sign-in page
  //     if (state.url === '/auth/sign-in') {
  //       return true; // Allow access to sign-in
  //     }
  //     this._cacheService.saveInCache('redirectUrl', state.url);
  //     return this._router.createUrlTree(['/auth/sign-in']); // Redirect to sign-in
  //   }
  // }
  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ):
    | Observable<boolean | UrlTree>
    | Promise<boolean | UrlTree>
    | boolean
    | UrlTree {
    const isLoggedIn = this._authService.isLoggedIn();

    if (isLoggedIn) {
      // Prevent redirect if already on an auth page
      if (state.url.startsWith('/auth')) {
        return this._router.createUrlTree(['/']); // Redirect to home
      }
      return true; // Allow access to other routes
    } else {
      // If not logged in, check if on the sign-in page
      if (
        state.url === '/auth/sign-in' ||
        state.url === '/auth/sign-up' ||
        state.url.includes('/auth/reset-password')
      ) {
        return true; // Allow access
      }

      // Save the intended URL before redirecting to sign-in
      this._cacheService.saveInCache('redirectUrl', state.url);

      return this._router.createUrlTree(['/auth/sign-in']); // Redirect to sign-in
    }
  }

  canActivateChild(
    childRoute: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ):
    | Observable<boolean | UrlTree>
    | Promise<boolean | UrlTree>
    | boolean
    | UrlTree {
    return true;
  }
  canDeactivate(
    component: unknown,
    currentRoute: ActivatedRouteSnapshot,
    currentState: RouterStateSnapshot,
    nextState?: RouterStateSnapshot
  ):
    | Observable<boolean | UrlTree>
    | Promise<boolean | UrlTree>
    | boolean
    | UrlTree {
    return true;
  }
  canMatch(
    route: Route,
    segments: UrlSegment[]
  ):
    | Observable<boolean | UrlTree>
    | Promise<boolean | UrlTree>
    | boolean
    | UrlTree {
    return true;
  }
  canLoad(
    route: Route,
    segments: UrlSegment[]
  ):
    | Observable<boolean | UrlTree>
    | Promise<boolean | UrlTree>
    | boolean
    | UrlTree {
    // Check if the user is logged in
    const isLoggedIn = this._authService.isLoggedIn();

    // If the user is logged in, prevent access to the auth module
    if (isLoggedIn) {
      // If trying to access auth routes, redirect to home/dashboard
      if (segments[0]?.path === 'auth') {
        return this._router.createUrlTree(['/']); // Redirect to home or dashboard
      }
      return true; // Allow access to other routes
    } else {
      // Allow access to specific routes even if not logged in
      if (
        this._authService.isLoggedIn() ||
        segments[1]?.path == 'course-details' ||
        segments[1]?.path == 'courses' ||
        segments[1]?.path == 'verify-certificate' ||
        segments[1]?.path == 'filter-courses' ||
        segments[1]?.path == 'profile'
      ) {
        return true;
      }

      // If the user is not logged in and trying to access auth routes, do nothing
      if (segments[0]?.path === 'auth') {
        return true; // Allow access to auth routes
      }
      // Save the current path before navigating to sign-in
      const path = segments.map((segment) => segment?.path).join('/');
      this._cacheService.saveInCache('redirectUrl', path);

      // Navigate to sign-in
      return this._router.createUrlTree(['/auth/sign-in']);
    }
  }
}
