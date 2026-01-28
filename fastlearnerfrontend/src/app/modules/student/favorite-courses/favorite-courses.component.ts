import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { FavoriteCourse } from 'src/app/core/models/favorite-course.model';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { environment } from 'src/environments/environment.development';

@Component({
  selector: 'app-favorite-courses',
  templateUrl: './favorite-courses.component.html',
  styleUrls: ['./favorite-courses.component.scss'],
})
export class FavoriteCoursesComponent implements OnInit {
  imageUrl = environment.imageUrl;
  _httpConstants: HttpConstants = new HttpConstants();
  favoriteCourses: FavoriteCourse = new FavoriteCourse();
  favoriteCourseList: Array<any> = [];
  isHeartClick: boolean = false;
  noOfCount: any;
  page?: any = 0;
  size?: any = 8;
  isTogglingFavorite: boolean = false;
  constructor(
    private _router: Router,
    private _courseService: CourseService,
    private _messageService: MessageService,
    private _sharedService: SharedService
  ) {}

  ngOnInit(): void {
    this.getFavoriteCourseList(this.page, this.size);
  }

  getFavoriteCourseList(page?: any, size?: any) {
    this.favoriteCourses.pageNo = page;
    this.favoriteCourses.pageSize = size;
    this._courseService?.getFavoriteCourses(this.favoriteCourses)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.favoriteCourseList = [];
          response?.data?.favouriteCourses?.forEach((x: any) => {
            x.courseDuration = this.convertSecondsToHoursAndMinutes(
              x.courseDuration
            );
            this.favoriteCourseList.push(x);
          });
          this.noOfCount = response?.data?.totalPages;
        } else if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
        ) {
          this.favoriteCourseList = [];
        }
      },
      error: (error: any) => {
        if (
          error?.error?.status ==
          this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
        ) {
          this.favoriteCourseList = [];
        }
      },
    });
  }

  changeOfFavoriteCourse(event: any) {
    this.isHeartClick = event;
  }

  showMore() {
    this.page += 1;
    this.getFavoriteCourseList(this.page, this.size);
  }

  search() {
    if (this.favoriteCourses.title?.trim()) {  
      this.page = 0; 
      this.favoriteCourseList = []; 
      this.getFavoriteCourseList(this.page, this.size); 
    } 
  }

  clearSearch() {
    this.favoriteCourses.title = ''; 
    this.page = 0; 
    this.favoriteCourseList = []; 
    this.getFavoriteCourseList(this.page, this.size); 
  }

  toggleFavoriteCourse(courseId?: any) {
    this.isTogglingFavorite = true;
    this._courseService.addOrRemoveCourseToFavorite(courseId).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.favoriteCourseList = this.favoriteCourseList.filter(
            (course) => course.courseId !== courseId
          );
          this._sharedService.updateFavCourseMenu();
        }
      },
      error: (error: any) => {},
      complete: () => {
        this.isTogglingFavorite = false;
      },
    });
  }

  routeToCourseDetails(courseUrl: any) {
    if (!this.isTogglingFavorite) {
      this._router.navigate(['student/course-details', courseUrl]);
    }
  }

  routeToCourseDetailsContent(courseUrl: any) {
    this._router.navigate(['student/course-details', courseUrl], {
      fragment: 'course-content',
    });
  }

  routeToInsructorProfile(event) {
    this._router.navigate(['user/profile'], {
      queryParams: { url: event?.profileUrl },
    });
    event?.event.stopPropagation();
  }

  convertSecondsToHoursAndMinutes(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);

    if (hours === 0) {
      return `${minutes} minutes`;
    } else if (minutes === 0) {
      return `${hours} hours`;
    } else {
      return `${hours} hours ${minutes} minutes`;
    }
  }
}
