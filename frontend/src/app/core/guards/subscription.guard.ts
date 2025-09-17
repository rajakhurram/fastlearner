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
export class SubscriptionGuard
  implements
    CanActivate,
    CanActivateChild,
    CanDeactivate<unknown>,
    CanLoad,
    CanMatch
{
  constructor(
    private _authService: AuthService,
    private _router: Router,
    private _cacheService: CacheService
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {
    const isLoggedIn = this._authService.isLoggedIn();
    // console.log('Current URL:', state.url);
    const queryParamMap = route.queryParamMap;

    if(queryParamMap.has('sessionId') && queryParamMap.has('isMobile')){
      const sessionId = queryParamMap.get('sessionId');
      const isMobile = queryParamMap.get('isMobile');
      if(sessionId && isMobile === 'true'){
        return true;
      }else{
        this._router.navigate(['/auth/sign-in']);
      }
    }

    if (isLoggedIn) {
      return true; // Allow access if logged in
    } else {
      this._cacheService.saveInCache('redirectUrl', state.url);
      // Optionally, you can save the intended URL before redirecting
      this._router.navigate(['/auth/sign-in']); // Redirect to sign-in
      return false; // Deny access
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
    return true;
  }
}
