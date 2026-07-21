import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { StateService } from 'src/app/core/services/state.service';

@Component({
  selector: 'app-welcome-page-instructor',
  templateUrl: './welcome-page-instructor.component.html',
  styleUrls: ['./welcome-page-instructor.component.scss'],
})
export class WelcomePageInstructorComponent {
  constructor(
    private _router: Router,
    private _authService: AuthService,
    private stateService: StateService,
    private cacheService: CacheService,
  ) {}

  routeToCreateCourse() {
    const loggedIn = this.cacheService.getDataFromCache('isLoggedIn');
    if (!loggedIn) {
      this.cacheService.saveInCache('redirectUrl', '/instructor/content-type');
      this._router.navigate(['/auth/sign-in']);
      return;
    }
    this._router.navigate(['/instructor/content-type']);
  }

  skip() {
    // sessionStorage.removeItem('redirectInstructorUrl');
    this._router.navigate(['/instructor/instructor-dashboard']);
  }
}
