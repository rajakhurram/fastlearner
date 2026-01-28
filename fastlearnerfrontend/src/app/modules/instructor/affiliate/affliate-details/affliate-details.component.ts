import {
  Component,
  ElementRef,
  OnInit,
  Renderer2,
  SecurityContext,
  SimpleChanges,
  ViewContainerRef,
} from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';

import { NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import {
  AffiliateMode,
  AffiliateStripeStatus,
} from 'src/app/core/enums/affiliate.enum';
import {
  AssignedPremiumCourses,
  InstructorAffiliate,
  InstructorPremiumCourses,
} from 'src/app/core/models/affiliate.model';
import { TableConfig } from 'src/app/core/models/table.model-config';
import { AffiliateService } from 'src/app/core/services/affiliate.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SubscriptionPlanComponent } from 'src/app/modules/auth/subscription-plan/subscription-plan.component';
import { AffiliateModalComponent } from 'src/app/modules/dynamic-modals/affiliate-modal/affiliate-modal.component';
import { DeletionModalComponent } from 'src/app/modules/dynamic-modals/deletion-modal/deletion-modal.component';
import { environment } from 'src/environments/environment.development';

@Component({
  selector: 'app-affliate-details',
  templateUrl: './affliate-details.component.html',
  styleUrls: ['./affliate-details.component.scss'],
})
export class AffliateDetailsComponent implements OnInit {
  _httpConstants: HttpConstants = new HttpConstants();
  affiliateDetail: InstructorAffiliate;
  affliateStripeStatus = AffiliateStripeStatus;
  affiliateId: number;
  affiliateLimitExceed: boolean = false;
  affiliateLimitExceedMessage: SafeHtml;
  private isUpgradeClickEventAttached = false;
  payload = {
    pageSize: 10,
    pageNo: 0,
    instructorAffiliateId: 0,
  };
  constructor(
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef,
    private _router: Router,
    private _affiliateService: AffiliateService,
    private _messageService: MessageService,
    private _activatedRoute: ActivatedRoute,
    private _cacheService: CacheService,
    private sanitizer: DomSanitizer,
    private renderer: Renderer2,
    private el: ElementRef
  ) {}
    tableConfig: TableConfig = {
    columns: [
      {
        header: 'Courses',
        field: 'courseTitle',
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Students',
        field: 'students',
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Rewards(%)',
        field: 'reward',
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Revenue',
        field: 'revenue',
        tooltip: true,
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Generated Link',
        field: 'url',
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Actions',
        field: 'actions',
        actions: true,
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
    ],
    rowData: [],
    headerColor: '#f5f5f5',
    rowColor: '#ffffff',
    rowTextColor: '#212189',
    paginated: true,
    pageNo: 0,
    pageSize: 10,
    totalElements: 10,
    itemsPerPage: false,
  };

  coursesSelection: [];

  ngOnInit(): void {
    this.affiliateId = parseInt(
      this._activatedRoute?.snapshot?.paramMap?.get('affiliateId')
    );
    this.getAffiliate(this.affiliateId);
  }

  // ngOnChanges(changes: SimpleChanges): void {
  //   if (changes['affiliateLimitExceedMessage']) {
  //     this.isUpgradeClickEventAttached = false;
  //   }
  // }

  ngAfterViewChecked(): void {
    if (!this.isUpgradeClickEventAttached) {
      this.attachUpgradeClickEvent();
    }
  }

  attachUpgradeClickEvent(): void {
    const upgradeLink = this.el.nativeElement.querySelector('.upgrade-link');
    if (upgradeLink) {
      this.renderer.listen(upgradeLink, 'click', () => {
        this.openSubscriptionPlan();
      });
      this.isUpgradeClickEventAttached = true; // Avoid duplicate listeners
    }
  }
  handleTableAction(event: { event; action: string; row: any }) {
    if (event.action === 'edit') {
      event.event.stopPropagation();
      return;
    } else if (event.action === 'delete') {
      this.deleteAssignCourse(event?.row);
      event.event.stopPropagation();
      return;
    } else if (event.action === 'copy') {
      this.copyURL(event?.row);
      event.event.stopPropagation();
      return;
    }else if (event.action ==='switch'){
      this.activeInActiveAssignCourses(event?.row);
      // event.event.stopPropagation();
      return;
    }
  }

  activeInActiveAssignCourses(event:any){
    console.log(event);
    let status="";
    if(event?.switchValue){
      status="ACTIVE"
    }else{
      status="INACTIVE"
    }
    
    this._affiliateService.acive_InActiveAssignCourse(event?.id, event?.instructorAffiliateId, status).subscribe(
      {
        next:(response:any)=>{
          if(response?.status==this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE){
            
          }
        },
        error: (error: any) => {
          this._messageService.error(error?.error?.message);
          this.getCourseByAffiliate();
        },
      }
    )

  }

  getCoursesForSelection(affiliateId) {
    this._affiliateService
      .getCoursesWithReward({ affiliateId: affiliateId })
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.coursesSelection = response?.data;
          }
        },
        error: (error: any) => {},
      });
  }

  onAddCourse(addedCourse) {
    if (!addedCourse?.courseId) {
      return;
    }
    addedCourse = {
      ...addedCourse,
      affiliateId: this.affiliateDetail?.affiliateId,
    };
    this._affiliateService.assignAffiliateCourse([addedCourse]).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.affiliateLimitExceed = false;
          this.affiliateLimitExceedMessage = '';
          this.getCourseByAffiliate();
          this._messageService.success('Course assigned successfully');
        }
      },
      error: (error) => {
        if (
          error?.error?.status ==
          this._httpConstants.REQUEST_STATUS.CONFLICT_409.CODE
        ) {
          this.affiliateLimitExceed = true;
          this.affiliateLimitExceedMessage =
            this.sanitizer.bypassSecurityTrustHtml(error?.error?.message);
        } else {
          this._messageService.error(error?.error?.message);
        }
      },
    });
  }

  getAffiliate(affiliateId) {
    this._affiliateService
      .getAffiliate({ instructorAffliateId: affiliateId })
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.affiliateDetail = response?.data;

            this.payload = {
              ...this.payload,
              instructorAffiliateId: response?.data?.affiliateId,
            };
            this.getCourseByAffiliate();
            this.getCoursesForSelection(response?.data?.affiliateId);
          }
        },
        error: (error: any) => {},
      });
  }
  getCourseByAffiliate() {
    this._affiliateService.getCourseByAffiliate(this.payload).subscribe({
      next: (response: {
        status: number;
        data: {
          content: [];
          pageable: any;
          totalElements: number;
        };
      }) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          const assignedCourses: AssignedPremiumCourses[] =
            response?.data?.content;
          assignedCourses?.forEach((element) => {
            
            
            element['showCopyIcon'] = true;
            element['showDeleteIcon'] = false;
            element['showSwitchIcon'] = true;
            if(element.status=='ACTIVE'){
                element.switchValue=true;
            }else{
              element.switchValue=false;
            }

            // element.switchValue=='Active'?true:false;
          });

          this.tableConfig = {
            ...this.tableConfig,
            rowData: assignedCourses, // Assigning typed data
            pageNo: response?.data?.pageable?.pageNumber,
            pageSize: 10,
            totalElements: response?.data?.totalElements,
          };
        }
      },
      error: (error: any) => {
        this.tableConfig = {
          ...this.tableConfig,
          rowData: [],
          pageNo: 0,
          pageSize: 10,
          totalElements: 0,
        };
      },
    });
  }

  editAffliateDetails() {
    this.editAffiliate();
  }
  deleteAffliate() {
    this.openDeleteModal(this.affiliateDetail);
  }
  deleteAssignCourse(data) {
    this.openDeleteModal(data, true);
  }

  resendLink(email) {
    this._affiliateService.resendLink(email).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.openSuccesModal();
        }
      },
      error: (error) => {},
    });
  }

  openSuccesModal() {
    const modal = this._modal.create({
      nzContent: AffiliateModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: false, // Disables closing with Enter or Escape
      nzWidth: '40%',
      nzComponentParams: {
        addedAffiliate: true,
        fromResendLink: true,
        mode: AffiliateMode.RESEND,
      },
      nzMask: true, // Ensures the modal has a backdrop
    });

    modal?.afterClose?.subscribe((result) => {});
  }

  deleteAffiliate(affiliate) {
    this._affiliateService.deleteAffiliate(affiliate)?.subscribe({
      next: (response) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this._messageService.success('Affiliate deleted successfully');
          this._router.navigate(['instructor/affiliate/profiles']);
        }
      },
      error: (error: any) => {},
    });
  }

  deleteAffiliateCourse(affiliateCourse) {
    affiliateCourse = {
      ...affiliateCourse,
      affiliateId: this.affiliateDetail?.affiliateId,
    };
    this._affiliateService.deleteAffiliateCourse(affiliateCourse)?.subscribe({
      next: (response) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this._messageService.success('Affiliate Unassigned successfully');
          this.payload.pageNo = 0;
          this.getCourseByAffiliate();
        }
      },
      error: (error: any) => {},
    });
  }

  openDeleteModal(data?, fromAssignCourse?) {
    const modal = this._modal.create({
      nzContent: DeletionModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: false,
      nzWidth: '40%',
      nzComponentParams: {
        msg: fromAssignCourse
          ? 'Are you sure you want to unassign course?'
          : 'Are you sure you want to delete the selected Affiliate',
        secondBtnText: 'Delete',
      },
    });

    modal?.afterClose?.subscribe((result) => {
      if (result && fromAssignCourse) {
        this.deleteAffiliateCourse(data);
      } else if (result && !fromAssignCourse) {
        this.deleteAffiliate(data);
      }
    });
  }

  editAffiliate() {
    const modal = this._modal.create({
      nzContent: AffiliateModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      nzComponentParams: {
        data: this.affiliateDetail,
        mode: AffiliateMode.EDIT,
      },
      nzWidth: '40%',
      // nzComponentParams: {
      //   msg: "Are you sure you want to " + this.capitalizeFirstLetter(courseStatus.toLowerCase()) + " the course?",
      //   secondBtnText: courseStatus == CourseStatus.DELETE ? "Delete" : "Yes"
      // },
      // nzOnCancel: () => {
      //   this.switchPreviousState(courseId, courseStatus);
      // },
    });

    modal.afterClose.subscribe((result) => {
      if (result) this.getAffiliate(this.affiliateId);
    });
  }

  copyURL(data) {
    if (data?.url) {
      const temporaryInput = document.createElement('textarea');
      temporaryInput.value = `${environment.basePath}student/course-details/${data.url}`;
      document.body.appendChild(temporaryInput);
      temporaryInput.select();
      try {
        const successful = document.execCommand('copy');
        if (successful) {
          this._messageService.success('URL copied to clipboard');
        } else {
          this._messageService.error('Failed to copy URL to clipboard.');
        }
      } catch (err) {
        console.error('Error copying to clipboard:', err);
      } finally {
        document.body.removeChild(temporaryInput);
      }
    } else {
      console.error('No URL provided to copy.');
    }
  }

  affiliatePreviusRoute() {
    const affiliatePreviousRoute = this._cacheService.getDataFromCache(
      'affiliatePreviousRoute'
    );
    if (affiliatePreviousRoute) {
      this._cacheService.removeFromCache('affiliatePreviousRoute');
      this._router.navigate([affiliatePreviousRoute]);
    } else {
      this._router.navigate(['instructor/affiliate/profiles']);
    }
  }

  onPageChange(page?: any) {
    this.payload.pageNo = page - 1;
    this.getCourseByAffiliate();
  }

  openSubscriptionPlan(): void {
    const modal = this._modal.create({
      nzContent: SubscriptionPlanComponent,
      nzComponentParams: {
        fromSubscriptionPlan: true,
        showFreePlan: false,
      },
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: this.fullWidth ? '80%' : '100%',
      nzWidth: '80%',
    });
    modal.afterClose?.subscribe((result) => {
      // this.subscriptionModalOpened = false;
    });
  }
}
