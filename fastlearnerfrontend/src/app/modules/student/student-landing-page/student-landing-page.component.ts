import { Component, OnInit } from '@angular/core';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { FavoriteCourse } from 'src/app/core/models/favorite-course.model';
import { CourseService } from 'src/app/core/services/course.service';

@Component({
  selector: 'app-student-landing-page',
  templateUrl: './student-landing-page.component.html',
  styleUrls: ['./student-landing-page.component.scss']
})
export class StudentLandingPageComponent implements OnInit {

  constructor() {}

  ngOnInit(): void {
  }

}
