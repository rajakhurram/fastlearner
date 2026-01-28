import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CourseContentType } from 'src/app/core/enums/course-content-type.enum';
import { CourseType } from 'src/app/core/enums/course-status';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { environment } from 'src/environments/environment.development';

@Component({
  selector: 'app-filter-courses',
  templateUrl: './filter-courses.component.html',
  styleUrls: ['./filter-courses.component.scss'],
})
export class FilterCoursesComponent implements OnInit , OnDestroy {
  imageUrl = environment.imageUrl;
  _httpConstants: HttpConstants = new HttpConstants();
  courseList: Array<any> = [];
  nlpCourseList: Array<any> = [];
  searchKeyword: any;
  selectedRating: any;
  totalElements: any;
  showSelect?: boolean = false;
  disableShowMore?: boolean = false;
  courseType = CourseType;
  courseContentType = CourseContentType;

  payLoad = {
    reviewFrom: 0,
    reviewTo: 5,
    searchValue: '',
    pageNo: 0,
    pageSize: 8,
    isNlpSearch: true,
  };

  constructor(
    private _courseService: CourseService,
    private _messageService: MessageService,
    private _router: Router,
    private _activatedRoute: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // this.getSearchResults();
    this.getSearchKeyword();

    this._activatedRoute.queryParams.subscribe((params) => {
      this.searchKeyword = params['search'];
      this.searchByKeyword();
    });
  }

  ngOnDestroy(): void {
    this.courseList = [];
    this.nlpCourseList = [];
    this._courseService.setSearchResults([] , [])
  }

  // getSearchResults() {
  //   this._courseService?.searchResults$?.subscribe((result: any) => {
  //     if (result.length) {
  //       this.courseList = result[0]?.courses;
  //       this.nlpCourseList = result[1]?.nlpCourses;
  //       this.courseList?.length <= 1
  //         ? (this.showSelect = false)
  //         : (this.showSelect = true);
  //       this.nlpCourseList?.length <= 1
  //         ? (this.showSelect = false)
  //         : (this.showSelect = true);
  //       this.courseList?.forEach((res: any) => {
  //         if (typeof res.courseDuration === 'number') {
  //           res.courseDuration = this.convertSecondsToHoursAndMinutes(
  //             res.courseDuration
  //           );
  //         }
  //       });
  //       this.nlpCourseList?.forEach((res: any) => {
  //         if (typeof res.duration === 'number') {
  //           res.duration = this.convertSecondsToHoursAndMinutes(res.duration);
  //         }
  //       });
  //     } else {
  //       this._activatedRoute.queryParams.subscribe((params) => {
  //         this.searchKeyword = params['search'];
  //       });

  //       this.searchByKeyword();
  //     }
  //   });
  // }

  getSearchKeyword() {
    this._courseService.$searchSuggestionsIds.subscribe((key: any) => {
      this.searchKeyword = key;
    });
  }

  searchByKeyword(fromShowMore?) {
    this.payLoad.searchValue = this.searchKeyword.trim();
    this._courseService?.searchCourse(this.payLoad)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.courseList = response?.data?.content
          this.courseList?.forEach((element) => {
            element.duration = this.convertSecondsToHoursAndMinutes(
              element.duration
            );
          });
          this.totalElements = response?.data?.totalElements;
          this.disableShowMore =
            this.totalElements == this.courseList?.length
              ? true
              : false;
        }
      },
      error: (error: any) => {
        this.courseList = [];
      },
    });
  }

  showMoreCourse() {
    if (!this.disableShowMore) {
      this.payLoad.pageNo += 1;
      this.searchByKeyword(true);
    }
  }

  routeToCourseDetails(courseUrl: any) {
    this._router.navigate(['student/course-details', courseUrl]);
  }

  onChangeOfRating(event: any) {
    switch (event) {
      case '5':
        this.payLoad.reviewFrom = 4.1;
        this.payLoad.reviewTo = 5;
        break;
      case '4':
        this.payLoad.reviewFrom = 3.1;
        this.payLoad.reviewTo = 4;
        break;
      case '3':
        this.payLoad.reviewFrom = 2.1;
        this.payLoad.reviewTo = 3;
        break;
      case '2':
        this.payLoad.reviewFrom = 1.1;
        this.payLoad.reviewTo = 2;
        break;
      case '1':
        this.payLoad.reviewFrom = 0.1;
        this.payLoad.reviewTo = 1;
        break;
      case '0':
        this.payLoad.reviewFrom = null;
        this.payLoad.reviewTo = null;
        break;
    }
    this.searchByKeyword();
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

  routeToInsructorProfile(profileUrl?: any) {
    this._router.navigate(['user/profile'], {
      queryParams: { url: profileUrl },
    });
  }

  routeToCourseDetailsContent(courseUrl: any) {
    this._router.navigate(['student/course-details', courseUrl], {
      fragment: 'course-content',
    });
  }
  routeToCourseContent(event, course, sectionId, topicId?) {
    this._router.navigate(['student/course-content', course?.courseUrl], {
      queryParams: {
        sectionId: sectionId,
        topicId: topicId,
      },
    });
    event.stopPropagation();
  }

}
