import {
  ChangeDetectorRef,
  Component,
  OnInit,
  ViewContainerRef,
} from '@angular/core';
import { Router } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CourseStatus, CourseType } from 'src/app/core/enums/course-status';
import { Dashboard } from 'src/app/core/models/dashboard.model';
import { InstructorCourse } from 'src/app/core/models/instructor-courses.model';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { DashboardService } from 'src/app/core/services/dashboard.service';
import { MessageService } from 'src/app/core/services/message.service';
import { DeletionModalComponent } from '../../dynamic-modals/deletion-modal/deletion-modal.component';
import { CacheService } from 'src/app/core/services/cache.service';
import { Permission } from 'src/app/core/enums/permission.enum';
import { CourseContentType } from 'src/app/core/enums/course-content-type.enum';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {
  myCourseList: Array<InstructorCourse> = [];
  _httpConstants: HttpConstants = new HttpConstants();
  dashboard: Dashboard;
  payLoad = {
    pageNo: 0,
    pageSize: 10,
    searchInput: '',
    sort: '1',
  };

  totalElements: any = 0;

  dashboardStatsParam?: any = 'monthly';
  canEditPremiumCourse?: true;
  allCourse?: boolean = false;
  CourseStatus = CourseStatus;
  currentCourseStatus?: any;
  permissions?: any = [];
  courseContentType = CourseContentType

  constructor(
    private _dashboardService: DashboardService,
    private _router: Router,
    private _communicationService: CommunicationService,
    private _messageService: MessageService,
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef,
    private _cacheService: CacheService
  ) {
    this._communicationService.instructorCourseUpdate$.subscribe(() => {
      this.getCourseListOfInstructor();
    });
  }

  ngOnInit(): void {
    this.getDashboardStatistics();
    this.getCourseListOfInstructor();
    this.checkPermissions();
  }

  checkPermissions() {
      const data = this._cacheService.getDataFromCache('permissions');
      this.permissions = data
        ? JSON.parse(this._cacheService.getDataFromCache('permissions'))
        : null;
      if (
        this.permissions &&
        this.permissions.length > 0 &&
        this.permissions.includes(Permission.EDIT_PREMIUM_COURSE)
      ) {
        this.canEditPremiumCourse = true;
      }
    }

  getDashboardStatistics() {
    this._dashboardService
      .getDashboardStats(this.dashboardStatsParam)
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.dashboard = response?.data;
          }
        },
        error: (error: any) => {
          console.log(error);
        },
      });
  }

  getCourseListOfInstructorBySearch() {
      this.getCourseListOfInstructor();
  }

  getCourseListOfInstructor() {
    this._dashboardService.getMyCourses(this.payLoad).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.myCourseList = [];
          this.myCourseList = response?.data?.courses;
          this.totalElements = response?.data?.totalElements;
          this.myCourseList.forEach((el) => {
            el.coursePublish =
              el.courseStatus == CourseStatus.PUBLISHED ? true : false;
          });
        }
      },
      error: (error: any) => {
        this.myCourseList = [];
      },
    });
  }

  routeToCoursePage(data?: any) {
    if(data?.courseType != 'PREMIUM' || this.canEditPremiumCourse){
      if(data?.contentType.toUpperCase() == this.courseContentType.COURSE.toUpperCase()){
        this._router.navigate(['instructor/course'], { queryParams: { id: data?.id } });
      }else if(data?.contentType.toUpperCase() == this.courseContentType.TEST.toUpperCase()){
        this._router.navigate(['instructor/test'], { queryParams: { id: data?.id } });
      }
    }
  }

  routeToCreateCourse() {
    // this._router.navigate(['instructor/course']);
    this._router.navigate(['/content-type']);
  }

  orderMyCourses() {
    this.getCourseListOfInstructor();
  }

  onPageChange(page?: any) {
    this.payLoad.pageNo = page - 1;
    this.getCourseListOfInstructor();
  }

  changeCourseStatus(courseId?: number, courseStatus?: CourseStatus) {
    this._dashboardService
      .changeCourseStatus(courseId, courseStatus)
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.getCourseListOfInstructor();
          }
        },
        error: (error: any) => {
          let result = this.myCourseList.find((c: any) => c?.id == courseId);
          result.coursePublish =
            courseStatus == CourseStatus.PUBLISHED ? false : true;
          this._messageService.error(error?.error?.message);
        },
      });
  }

  coursePublish(courseId?: number, event?: any) {
    let courseStatus = event
      ? CourseStatus.PUBLISHED
      : CourseStatus.UNPUBLISHED;
    this.openConfirmationModal(courseId, courseStatus);
  }

  deleteCourse(courseId?: number, courseStatus?: CourseStatus) {
    this.openConfirmationModal(courseId, courseStatus);
  }

  openConfirmationModal(courseId?: number, courseStatus?: CourseStatus) {
    const modal = this._modal.create({
      nzContent: DeletionModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: '40%',
      nzComponentParams: {
        msg:
          'Are you sure you want to ' +
          this.capitalizeFirstLetter(courseStatus.toLowerCase()) +
          ' the course?',
        secondBtnText: courseStatus == CourseStatus.DELETE ? 'Delete' : 'Yes',
      },
      nzOnCancel: () => {
        this.switchPreviousState(courseId, courseStatus);
      },
    });

    modal.componentInstance?.deleteClick?.subscribe(() => {
      this.changeCourseStatus(courseId, courseStatus);
    });

    modal.componentInstance.cancelClick?.subscribe(() => {
      this.switchPreviousState(courseId, courseStatus);
    });
  }

  switchPreviousState(courseId: number, courseStatus: CourseStatus) {
    let result = this.myCourseList.find((c: any) => c?.id == courseId);
    result.coursePublish =
      courseStatus == CourseStatus.PUBLISHED ? false : true;
  }

  capitalizeFirstLetter(status: string): string {
    return status.charAt(0).toUpperCase() + status.slice(1).toLowerCase();
  }
}
