import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CourseContentType } from 'src/app/core/enums/course-content-type.enum';
import { CourseType } from 'src/app/core/enums/course-status';
import { AuthService } from 'src/app/core/services/auth.service';

@Component({
  selector: 'app-course-card',
  templateUrl: './course-card.component.html',
  styleUrls: ['./course-card.component.scss'],
})
export class CourseCardComponent implements OnInit {
  @Input() isPremium: boolean = false;
  @Input() fromMyCourse: boolean = false;
  @Input() fromFavouriteCourse: boolean = false;
  @Input() course;
  @Input() buttonTheme: string;
  @Output() routeToInstructorprofileEmitter: EventEmitter<any> =
    new EventEmitter();
  @Output() routeToCourseEmitter: EventEmitter<any> = new EventEmitter();
  @Output() favoriteCourseEmitter: EventEmitter<any> = new EventEmitter();
  isLoggedIn: boolean = false;
  courseType = CourseType;
  courseContentType = CourseContentType;

  constructor(private _authService: AuthService) {
    this.isLoggedIn = this._authService.isLoggedIn();
  }

  ngOnInit() {
    this._authService.$changeNavbarSate.subscribe((state: any) => {
      this.isLoggedIn = this._authService.isLoggedIn();
    });
  }

  routeToCourseDetails(courseUrl) {
    this.routeToCourseEmitter.emit(courseUrl);
  }

  routeToInsructorProfile(event, profileUrl) {
    this.routeToInstructorprofileEmitter.emit({ event, profileUrl });
  }
  favouriteCourse(courseId) {
    this.favoriteCourseEmitter.emit(courseId);
  }
}
