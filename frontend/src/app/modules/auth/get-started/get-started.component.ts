import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { Role } from 'src/app/core/enums/Role';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';

@Component({
  selector: 'app-get-started',
  templateUrl: './get-started.component.html',
  styleUrls: ['./get-started.component.scss'],
})
export class GetStartedComponent implements OnInit {
  selectedRoleStudent: any = false;
  selectedRoleInstructor: any = false;
  saveRole?: any;
  isSubscribed: any;
  _httpConstants: HttpConstants = new HttpConstants();

  constructor(
    private _authService: AuthService,
    private _router: Router,
    private _cacheService: CacheService
  ) {}

  ngOnInit(): void {
    this.verifyUserIsSubscribed();
  }

  saveUserRole() {
    if (
      (this.selectedRoleStudent && this.selectedRoleInstructor) ||
      (this.selectedRoleStudent && !this.selectedRoleInstructor)
    ) {
      this.saveRole = 'STUDENT';
    } else if (!this.selectedRoleStudent && this.selectedRoleInstructor) {
      this.saveRole = 'INSTRUCTOR';
    }

    this._cacheService.saveInCache('role', this.saveRole);

    this._authService.saveRole(this.saveRole).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          if (this.saveRole == Role.Student && this.isSubscribed) {
            this._router.navigate(['student']);
          } else if (this.saveRole == Role.Student && !this.isSubscribed) {
            this._router.navigate(['subscription-plan']);
          } else if (this.saveRole == Role.Instructor) {
            this._router.navigate(['instructor']);
          }
        }
      },
      error: (error: any) => {},
    });
  }

  verifyUserIsSubscribed() {
    this._authService.verifyUserSubscription().subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.isSubscribed = response?.data;
        }
      },
      error: (error: any) => {},
    });
  }

  toggleRole(role: any) {
    if (role == 'STUDENT') {
      this.selectedRoleStudent = !this.selectedRoleStudent;
    } else if (role == 'INSTRUCTOR') {
      this.selectedRoleInstructor = !this.selectedRoleInstructor;
    }
  }
}
