import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth.service';

@Component({
  selector: 'app-welcome-page-instructor',
  templateUrl: './welcome-page-instructor.component.html',
  styleUrls: ['./welcome-page-instructor.component.scss'],
})
export class WelcomePageInstructorComponent {
  constructor(private _router: Router, private _authService: AuthService) {}

  routeToCreateCourse() {
    this._router.navigate(['/content-type']);
  }

  skip() {
    this._router.navigate(['/instructor/instructor-dashboard']);
  }
}
