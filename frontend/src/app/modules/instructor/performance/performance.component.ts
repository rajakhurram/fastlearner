import { DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import * as Highcharts from 'highcharts';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AuthService } from 'src/app/core/services/auth.service';
import { PerformanceService } from 'src/app/core/services/performance.service';

@Component({
  selector: 'app-performance',
  templateUrl: './performance.component.html',
  styleUrls: ['./performance.component.scss'],
})
export class PerformanceComponent implements OnInit {
  _httpConstants: HttpConstants = new HttpConstants();
  courseVisits?: Array<any> = [];
  activeStudents?: Array<any> = [];
  ratingsAndReviews?: any = null;
  showChartOptionsBar?: any = false;
  chartOptionsLineFlag?: any = false;
  selectedCourseId?: any;
  feedBackComments?: any = [];

  payLoad = {
    courseId: 0,
    pageNo: 0,
    pageSize: 3,
  };
  totalPages?: any;
  courseNames?: Array<any> = [
    {
      id: 0,
      title: 'All courses',
    },
  ];

  Highcharts: typeof Highcharts = Highcharts;
  chartOptionsLine: Highcharts.Options = {
    chart: {
      type: 'spline',
    },
    credits: {
      enabled: false,
    },
    colorAxis: {
      lineColor: '#ffffff',
    },
    title: {
      text: '',
    },
    xAxis: {
      categories: [],
    },
    yAxis: {
      title: {
        text: '',
      },
    },
    plotOptions: {
      line: {
        dataLabels: {
          enabled: true,
        },
        enableMouseTracking: false,
      },
    },
    series: [
      {
        type: 'spline',
        name: 'No of students',
        color: '#FE4A55',
        data: [],
      },
    ],
  };

  chartOptionsBar: Highcharts.Options = {
    chart: {
      type: 'column',
    },
    credits: {
      enabled: false,
    },
    title: {
      text: '',
    },
    xAxis: {
      categories: [],
    },
    yAxis: {
      min: 0,
      title: {
        text: '',
      },
      tickInterval: 500,
      minorTickInterval: 5,
    },
    series: [
      {
        type: 'column',
        name: 'Visits',
        color: '#212189',
        data: [],
      },
    ],
  };

  constructor(
    private _authService: AuthService,
    private _performanceService: PerformanceService,
    private datePipe: DatePipe
  ) {}

  ngOnInit(): void {
    if (this.courseNames.length > 0) {
      this.selectedCourseId = this.courseNames[0].id;
      this.payLoad.courseId = this.selectedCourseId;
    }
    this.getCourseVisits();
    this.getRatingsAndReviews();
    this.getCourseNames();
    this.getActiveStudents();
  }

  getCourseVisits() {
    this.showChartOptionsBar = false;
    this.courseVisits = [];
    (this.chartOptionsBar.xAxis as Highcharts.XAxisOptions).categories = [];
    (this.chartOptionsBar.series as Highcharts.SeriesSplineOptions[])[0].data =
      [];
    (this.chartOptionsBar.yAxis as Highcharts.YAxisOptions).tickInterval = 0;
    this._performanceService.getCourseVisits(this.selectedCourseId).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.courseVisits = response.data;
          this.courseVisits.forEach((value) => {
            (this.chartOptionsBar.xAxis as any).categories.push(
              value.monthName.trim().substring(0, 3)
            );
            (this.chartOptionsBar.series as any)[0].data.push(
              value.totalVisitors
            );
          });
          let highestValueObject = this.courseVisits.reduce(
            (max: any, obj: any) => {
              return obj.totalVisitors > max.totalVisitors ? obj : max;
            },
            this.courseVisits[0]
          );
          (this.chartOptionsBar.yAxis as Highcharts.YAxisOptions).tickInterval =
            highestValueObject?.totalVisitors;
          this.showChartOptionsBar = true;
        }
      },
      error: (error: any) => {},
    });
  }

  getRatingsAndReviews() {
    this.ratingsAndReviews = null;
    this.payLoad.courseId = this.selectedCourseId;
    this._performanceService.getRatingsAndReviews(this.payLoad).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.totalPages = response?.data?.totalPages;
          if (
            this.ratingsAndReviews == null ||
            this.ratingsAndReviews == undefined
          ) {
            this.ratingsAndReviews = response.data;
            this.ratingsAndReviews.feedback.feedbackComments.forEach(
              (feedback) => {
                feedback.createdAt = this.transFormDate1(feedback.createdAt);
                this.feedBackComments.push(feedback);
              }
            );
            this.ratingsAndReviews.feedback.feedbackComments =
              this.feedBackComments;
          }
          // else {
          //   response?.data?.feedback.feedbackComments.forEach((feedback) => {
          //     feedback.createdAt = this.transFormDate1(feedback.createdAt);
          //     this.ratingsAndReviews.feedback.feedbackComments.push(feedback);
          //   });
          // }
        }
      },
    });
  }

  getActiveStudents() {
    this.chartOptionsLineFlag = false;
    this.activeStudents = [];
    (this.chartOptionsLine.xAxis as Highcharts.XAxisOptions).categories = [];
    (this.chartOptionsLine.series as Highcharts.SeriesSplineOptions[])[0].data =
      [];
    (this.chartOptionsLine.yAxis as Highcharts.YAxisOptions).tickInterval = 0;
    this._performanceService
      .getActiveStudents(this.selectedCourseId)
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.activeStudents = response.data;
            this.activeStudents.forEach((value) => {
              (this.chartOptionsLine.xAxis as any).categories.push(
                value.monthName.trim().substring(0, 3)
              );
              (this.chartOptionsLine.series as any)[0].data.push(
                value.totalStudents
              );
            });
            let highestValueObject = this.activeStudents.reduce(
              (max: any, obj: any) => {
                return obj.totalStudents > max.totalStudents ? obj : max;
              },
              this.activeStudents[0]
            );
            (
              this.chartOptionsLine.yAxis as Highcharts.YAxisOptions
            ).tickInterval = highestValueObject?.totalStudents;
            this.chartOptionsLineFlag = true;
          }
        },
        error: (error: any) => {},
      });
  }

  getCourseNames() {
    this._performanceService.getCourseNames().subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          response.data.forEach((el: any) => {
            if (el.title) {
              this.courseNames.push(el);
            }
          });
        }
      },
      error: (error: any) => {
        this.courseNames = [];
      },
    });
  }

  selectCourse() {
    this.getCourseVisits();
    this.getRatingsAndReviews();
    this.getActiveStudents();
  }

  transFormDate(date: any) {
    const transformDate = new Date(date);
    return this.datePipe.transform(transformDate, 'MMM').slice(0, 3) || '';
  }

  transFormDate1(date: any) {
    const transformDate = new Date(date);
    return this.datePipe.transform(transformDate, 'MMMM d, y');
  }

  showMoreReviews() {
    this.payLoad.pageNo += 1;
    this.getRatingsAndReviews();
  }

  get getInitialOfLoggedInUser() {
    return this._authService.getLoggedInName();
  }
}
