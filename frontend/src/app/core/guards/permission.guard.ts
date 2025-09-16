import { Injectable } from '@angular/core';
import {
  CanLoad,
  Route,
  UrlSegment,
  Router,
  CanActivate,
  CanActivateChild,
  CanDeactivate,
  CanMatch,
} from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service'; // Replace with your actual AuthService
import { InstructorTabs } from '../enums/instructor_tabs';
import { CacheService } from '../services/cache.service';

@Injectable({
  providedIn: 'root',
})
export class PermissionGuard implements CanLoad {
  permissions?: any;

  constructor(
    private authService: AuthService,
    private router: Router,
    private _cacheService: CacheService
  ) {}

  canLoad(route: Route, segments: UrlSegment[]): boolean {
    const requiredPermission = route.data?.['requiredPermission'];
    return this.checkPermissions(requiredPermission);
  }

  private checkPermissions(requiredPermission: string): boolean {
    const data = this._cacheService.getDataFromCache('permissions');
    const permissions = data ? JSON.parse(data) : [];

    if (permissions?.includes(requiredPermission)) {
      return true;
    }

    this.router.navigate(['/auth/sign-in']);
    return false;
  }
}
