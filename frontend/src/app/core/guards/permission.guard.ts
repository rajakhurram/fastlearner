import { Injectable } from '@angular/core';
import { CanLoad, Route, UrlSegment, Router, CanActivate, CanActivateChild, CanDeactivate, CanMatch } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service'; // Replace with your actual AuthService
import { InstructorTabs } from '../enums/instructor_tabs';
import { CacheService } from '../services/cache.service';

@Injectable({
  providedIn: 'root',
})
export class PermissionGuard implements CanLoad{
  permissions?: any;

  constructor(
    private authService: AuthService, 
    private router: Router,
    private _cacheService: CacheService,
  ) {}

  canLoad(
    route: Route,
    segments: UrlSegment[]
  ): Observable<boolean> | Promise<boolean> | boolean {
    if (this.checkPermissions()) {
      return true;
    } else {
      this.router.navigate(['']);
      return false;
    }
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
        return true;
      }
      return false;
    }
}
