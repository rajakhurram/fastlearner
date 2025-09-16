import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { Router } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import {
  debounceTime,
  distinctUntilChanged,
  Observable,
  Subject,
  switchMap,
} from 'rxjs';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { assignpremium } from 'src/app/core/constants/staticData.constants';
import {
  AssignedPremiumCourses,
  InstructorAffiliate,
  InstructorPremiumCourses,
} from 'src/app/core/models/affiliate.model';
import { buttonConfig } from 'src/app/core/models/button.model-config';
import { SearchFilterConfig } from 'src/app/core/models/search-filter-config.model';
import { TableConfig } from 'src/app/core/models/table.model-config';
import { AffiliateService } from 'src/app/core/services/affiliate.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { AffiliateModalComponent } from 'src/app/modules/dynamic-modals/affiliate-modal/affiliate-modal.component';
import { AssignCourseModalComponent } from 'src/app/modules/dynamic-modals/assign-course-modal/assign-course-modal.component';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-premium-courses',
  templateUrl: './premium-courses.component.html',
  styleUrls: ['./premium-courses.component.scss'],
})
export class PremiumCoursesComponent implements OnInit {
  _httpConstants: HttpConstants = new HttpConstants();
  premiumCourses = [];
  expandAll: boolean;
  searchFilter: SearchFilterConfig = {
    placeHolder: 'Search by Course Title',
    height: '45px',
    width: '100%',
  };
  affliates = [];
  buttonConfig: buttonConfig = {
    size: 'large',
    backgroundColor: '#fe4a55',
    color: '#fff',
    text: 'Assign Affliate',
    borderColor: '#fe4a55',
    height: '40px',
    paddingTop: '4px',
    paddingRight: '27px',
    paddingBottom: '4px',
    paddingLeft: '27px',
    fontSize: '14px',
    borderRadius: '5px',
  };

  tableConfig: TableConfig = {
    columns: [
      {
        header: 'Name',
        field: 'affiliateName',
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
    paginated: true,
    pageNo: 0,
    pageSize: 10,
    totalElements: 10,
    itemsPerPage: false,
  };

  payload = {
    pageSize: 10,
    pageNo: 0,
    search: '',
  };
  instructorPremiumCoursesPayload = {
    pageSize: 10,
    pageNo: 0,
    search: '',
    totalElements: 0
  };
  private searchSubject = new Subject<string>();
  searchTerm: string;
  selectedCourse?: any;

  constructor(
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef,
    private _router: Router,
    private _affiliateService: AffiliateService,
    private _messageService: MessageService,
    private _cacheService: CacheService
  ) {}

  onPanelClick(course: any) {
    this.selectedCourse = course;
    this.scrollIntoView('top');
    this.tableConfig.rowData = [];
    this.premiumCourses.forEach((element) => {
      if (course.courseId === element.courseId) {
        element.panelOpen = !element.panelOpen; // Toggle the clicked course
      } else {
        element.panelOpen = false; // Close all other panels
      }
    });

    if (course.panelOpen) {
      this.payload.pageNo = 0;
      this.getPremiumCoursesAffiliates(course);
    }
  }
  ngOnInit(): void {
    this.initializeSearch();
    this.getPremiumCoursesByInstructor(this.instructorPremiumCoursesPayload);
    this.getAffiliateData(this.payload);
  }

  getAffiliateData(payload: any): void {
    this._affiliateService.getAffiliates(payload)?.subscribe({
      next: (response: {
        status: number;
        data: {
          affiliates: InstructorAffiliate[];
          pageNo: number;
          pageSize: number;
          totalElements: number;
        };
      }) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          const affiliates: InstructorAffiliate[] = response?.data?.affiliates;
          this.affliates = affiliates;
        }
      },
      error: (error: any) => {},
    });
  }

  getPremiumCoursesByInstructor(payload) {
    this._affiliateService.getPremiumCoursesByInstructor(payload)?.subscribe({
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
          const premiumCourses: InstructorPremiumCourses[] =
            response?.data?.content;
          premiumCourses?.forEach((element) => {
            element['showCopyIcon'] = true;
          });

          this.premiumCourses = premiumCourses;
          this.instructorPremiumCoursesPayload.totalElements = response?.data?.totalElements;
        }
      },
      error: (error: any) => {
        this.premiumCourses = [];
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

  initializeSearch() {
    this.searchSubject
      .pipe(
        debounceTime(300), // Wait 300ms after each keystroke
        distinctUntilChanged(), // Ignore if the next search term is the same as the previous
        switchMap((searchTerm) => {
          const updatedPayload = {
            ...this.instructorPremiumCoursesPayload,
            search: searchTerm, // Add search term to the payload
          };
          return new Observable((observer) => {
            this.getPremiumCoursesByInstructor(updatedPayload);
            observer.complete(); // Complete the observable after the function call
          });
        })
      )
      .subscribe();
  }

  searchCallBack(value?: any) {
    this.searchTerm = value;
    if (value && value.length >= 1) {
      this.instructorPremiumCoursesPayload.pageNo = 0;
      this.instructorPremiumCoursesPayload.search = this.searchTerm;
      this.getPremiumCoursesByInstructor(this.instructorPremiumCoursesPayload);
    }
    // else if (value.length === 0) {
    //   const updatedPayload = {
    //     ...this.payload,
    //     search: '', 
    //   };
    //   this.getPremiumCoursesByInstructor(updatedPayload);
    // }
  }
  

  handleTableAction(event: { event; action: string; row: any }) {
    if (event.action === 'edit') {
      return;
    } else if (event.action === 'copy') {
      console.log('Copy action triggered for:', event.row);
      this.copyURL(event?.row);
      event.event.stopPropagation();
      return;
    }
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
  getPremiumCoursesAffiliates(course) {
    this._affiliateService.getPremiumCoursesAffiliates(course, this.payload)?.subscribe({
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

          this.scrollIntoView('top');
          
          const assignedCourses: AssignedPremiumCourses[] =
            response?.data?.content;
          assignedCourses?.forEach((element) => {
            element['showCopyIcon'] = true;
          });

          this.tableConfig = {
            ...this.tableConfig,
            rowData: assignedCourses,
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

  assignAffliate(event, courseId, courseUrl) {
    const modal = this._modal.create({
      nzContent: AssignCourseModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      nzWidth: '60%',
      nzComponentParams: {
        affiliates: this.affliates,
        courseId: courseId,
        courseUrl: courseUrl,
      },
    });
    modal?.afterClose?.subscribe((result) => {
      this.collapsePanel();
    });
    event.stopPropagation();
  }

  routeToDetails(event) {
    this._cacheService.saveInCache('affiliatePreviousRoute', 'instructor/affiliate/premium-courses')
    this._router.navigate([
      'instructor/affiliate/affiliate-details',
      event.row.instructorAffiliateId,
    ]);
  }

  expandCoursesPanel() {
    this.premiumCourses.map((element) => {
      element.panelOpen = true;
    });
    this.expandAll = false;
  }
  collapsePanel() {
    this.premiumCourses.map((element) => {
      element.panelOpen = false;
    });
    this.expandAll = true;
  }

  handleCourseAffiliatesPageChange(page?: any){
    this.payload.pageNo = page-1;
    this.getPremiumCoursesAffiliates(this.selectedCourse);
  }

  scrollIntoView(id?: any){
    const targetDiv = document.getElementById(id);

    if (targetDiv) {
      targetDiv.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  onSearchClear(){
    if(this.instructorPremiumCoursesPayload.search){
      this.instructorPremiumCoursesPayload.pageNo = 0;
      this.instructorPremiumCoursesPayload.search = '';
      this.getPremiumCoursesByInstructor(this.instructorPremiumCoursesPayload);
    }
  }

  onInstructorPremiumCoursePageChange(page?: any){
    this.instructorPremiumCoursesPayload.pageNo = page - 1;
    this.getPremiumCoursesByInstructor(this.instructorPremiumCoursesPayload);
  }

}
