import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';

@Component({
  selector: 'app-user-profile-view',
  templateUrl: './user-profile-view.component.html',
  styleUrls: ['./user-profile-view.component.scss']
})
export class UserProfileViewComponent  implements OnInit {
  _httpConstants: HttpConstants = new HttpConstants();
  showDescription: boolean = false;
  courseList: Array<any> = [];
  profileUrl: any;
  instructorId?: any;
  instructorPublicProfile: any;
  numberOfCourses: number = 0;
  noOfCount: any;
  notFound: boolean = false; // Not Found variable
  coursePayLoad = {
    instructorId: null,
    pageNo: 0,
    pageSize: 10,
  };
  constructor(
    private _courseService: CourseService,
    private _messageService: MessageService,
    private _router: Router,
    private _activatedRoute: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.getRouteQueryParam();
  }

  getRouteQueryParam() {
    this._activatedRoute.queryParams.subscribe((params) => {
      this.profileUrl = params['url'];
      this.getInstructorPublicProfile(this.profileUrl);
    });
  }

  getInstructorPublicProfile(profileUrl: any) {
    this.notFound = false; // Reset before request
    this._courseService?.getInstructorPublicProfile(profileUrl)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.instructorPublicProfile = response?.data;
          this.instructorId = this.instructorPublicProfile?.userId;
          this.getInstructorCourseList(true);
        }
      },
      error: (error: any) => {
        if (error?.status === 404) {
          this.notFound = true; // 404 error pe Empty state show karein
          this.courseList = []; // Courses ko empty karna zaroori hai
        }
      },
    });
  }

  routeTo(route?: string) {
    if (route) window.open(route, '_blank');
    else {
      this._messageService.info('No link found');
    }
  }

  getInstructorCourseList(isFirst: boolean) {
    this.coursePayLoad.instructorId = this.instructorId;
    this._courseService?.getInstructorCourses(this.coursePayLoad)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          if (!isFirst) {
            response?.data?.data?.forEach((x: any) => {
              x.courseDuration = this.convertSecondsToHoursAndMinutes(
                x.courseDuration
              );
              this.courseList.push(x);
            });
          }
          if (isFirst) {
            this.courseList = response?.data?.data;
            this.noOfCount = response?.data?.pages;
            this.courseList?.forEach((res: any) => {
              res.courseDuration = this.convertSecondsToHoursAndMinutes(
                res.courseDuration
              );
            });
          }
        } else if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
        ) {
          this.courseList = [];
        }
      },
      error: (error: any) => {
        if (
          error?.error?.status ==
          this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
        ) {
          this.courseList = [];
        }
      },
    });
  }

  showMore() {
    this.coursePayLoad.pageNo = this.coursePayLoad.pageNo + 1;
    this.noOfCount = this.noOfCount - 1;
    this.getInstructorCourseList(false);
  }

  routeToCourseDetails(courseUrl: any) {
    this._router.navigate(['student/course-details', courseUrl]);
  }

  routeToCourseDetailsContent(courseUrl: any) {
    this._router.navigate(['student/course-details', courseUrl], {
      fragment: 'course-content',
    });
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
