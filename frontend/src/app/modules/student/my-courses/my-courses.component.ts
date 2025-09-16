import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { MyCourses } from 'src/app/core/models/my-courses.model';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { environment } from 'src/environments/environment.development';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
  selector: 'app-my-courses',
  templateUrl: './my-courses.component.html',
  styleUrls: ['./my-courses.component.scss'],
})
export class MyCoursesComponent implements OnInit {
  imageUrl = environment.imageUrl;
  _httpConstants: HttpConstants = new HttpConstants();
  myCourses: MyCourses = new MyCourses();
  selectedValue = '4';
  showLoader?: boolean = true;

  myCourseList: Array<any> = [];
  noOfCount: any;
  page?: any = 0;
  size?: any = 8;

  constructor(
    private _courseService: CourseService,
    private _messageService: MessageService,
    private _router: Router,
    private loader: NgxUiLoaderService
  ) {}

  ngOnInit(): void {
    this.getMyCourseList(this.page, this.size, this.myCourses.sortBy);
  }

  onChangeOfFilter(event) {
    this.getMyCourseList(this.page, this.size, event);
  }

  getMyCourseList(page?: any, size?: any, sortBy?: any, flag?: boolean) {
    this.showLoader ? this.loader.start() : null;
    this.showLoader = true;
    this.myCourses.pageNo = page;
    this.myCourses.pageSize = size;
    this.myCourses.sortBy = sortBy == null || undefined ? 0 : sortBy;
    this._courseService.getMyCourses(this.myCourses).subscribe({
      next: (response: any) => {
        this.loader.stop();
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          flag ? (this.myCourseList = []) : null;
          this.myCourseList = []
          response?.data?.myCourses.forEach((x: any) => {
            this.myCourseList.push(x);
          });
          if (!flag) {
            this.myCourseList.forEach((res: any) => {
              res.courseDuration = this.convertSecondsToHoursAndMinutes(
                res.courseDuration
              );
            });
          } else {
            this.myCourseList.forEach((res: any) => {
              res.courseDuration = this.convertSecondsToHoursAndMinutes(
                res.courseDuration
              );
            });
          }
          this.noOfCount = response?.data?.totalPages;
        } else if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
        ) {
          this.myCourseList = [];
        }
      },
      error: (error: any) => {
        this.loader.stop();
        if (
          error?.error?.status ==
          this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
        ) {
          this.myCourseList = [];
        }
      },
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

  showMore() {
    this.page += 1;
    this.getMyCourseList(this.page, this.size);
  }

  search(flag?: boolean) {
    if (this.myCourses?.title?.trim() || flag) {
      this.page = 0;
      this.myCourseList = [];
      this.showLoader = false;
      this.getMyCourseList(this.page, this.size, this.myCourses.sortBy, true);
    }
  }

  routeToCourseDetails(courseUrl: any) {
    this._router.navigate(['student/course-details', courseUrl]);
  }

  routeToInsructorProfile(event: any) {
    this._router.navigate(['user/profile'], {
      queryParams: { url: event?.profileUrl },
    });
    event?.event?.stopPropagation();
  }
}
