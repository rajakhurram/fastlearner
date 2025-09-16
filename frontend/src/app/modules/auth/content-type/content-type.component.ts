import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth.service';

@Component({
  selector: 'app-content-type',
  templateUrl: './content-type.component.html',
  styleUrls: ['./content-type.component.scss'],
})
export class ContentTypeComponent {
  constructor(private _router: Router, private _authService: AuthService) {}

  contents = [
    {
      img: '../../../../assets/icons/create_course_icon_red.svg',
      title: 'Course',
      des: `Enhance learning with engaging video lectures, interactive quizzes,
      coding exercises, and more.`,
      to: '',
    },
    {
      img: '../../../../assets/icons/test.svg',
      title: 'Test',
      des: `Support students in certification exam prep with targeted practice
      questions.`,
      to: '',
    },
  ];

  routeTo(title) {
    if (title?.toLowerCase().includes('course')) {
      this._router.navigate(['/instructor/course']);
    } else {
      this._router.navigate(['/instructor/test']);
    }
  }

  skip() {
    this._router.navigate(['/instructor/instructor-dashboard']);
  }
}
