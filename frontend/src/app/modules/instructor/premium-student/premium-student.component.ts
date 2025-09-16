import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router, UrlTree } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CourseService } from 'src/app/core/services/course.service';

import { PremiumStudentsService } from 'src/app/core/services/premium-student.service';
import { debounceTime, distinctUntilChanged, Subject, switchMap } from 'rxjs';
import { MessageService } from 'src/app/core/services/message.service';

import dayjs from 'dayjs';
import utc from 'dayjs/plugin/utc';
import { SearchFilterConfig } from 'src/app/core/models/search-filter-config.model';

interface PremiumStudent {
  purchasedDate: Date;
  name: string;
  email: string;
  courseTitle: string;
}

@Component({
  selector: 'app-premium-student',
  templateUrl: './premium-student.component.html',
  styleUrls: ['./premium-student.component.scss'],
})
export class PremiumStudentComponent implements OnInit {
  errorMessage: string = ''; // This is for storing error messages

  students: any[] = [];
  private searchSubject: Subject<string> = new Subject<string>();
  searchText: string = '';
  dateRange: Date[] = [];
  searchInputText?: any;
  searchSuggestions: Array<any> = [];
  searchKeyword: string = '';
  debounceTimeout: any = null;
  showNoCourseText: boolean = false;
  payLoad = {
    searchValue: '',
    startDate: null,
    endDate: null,
    pageNo: 0,
    pageSize: 10,
  };
  _httpConstants: HttpConstants = new HttpConstants();

  totalElements: number = 0;
  dropsDown = 'down';
  dropsUp = 'up';
  opensRight = 'right';
  opensCenter = 'center';
  opensLeft = 'left';
  selectedRangeCalendarTimeRight: any = null;
  maxDate?: dayjs.Dayjs;
  minDate?: dayjs.Dayjs;
  invalidDates: dayjs.Dayjs[] = [];
  ranges: any = {
    Today: [dayjs(), dayjs()],
    Yesterday: [dayjs().subtract(1, 'days'), dayjs().subtract(1, 'days')],
    'Last 7 Days': [dayjs().subtract(6, 'days'), dayjs()],
    'Last 30 Days': [dayjs().subtract(29, 'days'), dayjs()],
    'This Month': [dayjs().startOf('month'), dayjs().endOf('month')],
    'Last Month': [
      dayjs().subtract(1, 'month').startOf('month'),
      dayjs().subtract(1, 'month').endOf('month'),
    ],
  };
  localeTime = {
    firstDay: 1,
    startDate: dayjs().startOf('day'),
    endDate: dayjs().endOf('day'),
    format: 'DD.MM.YYYY HH:mm:ss',
    applyLabel: 'Apply',
    cancelLabel: 'Cancel',
    fromLabel: 'From',
    toLabel: 'To',
  };
  locale = {
    firstDay: 1,
    startDate: dayjs().startOf('day'),
    endDate: dayjs().endOf('day'),
    format: 'DD.MM.YYYY',
    applyLabel: 'Apply',
    cancelLabel: 'Cancel',
    fromLabel: 'From',
    toLabel: 'To',
  };
  tooltips = [
    { date: dayjs(), text: 'Today is just unselectable' },
    { date: dayjs().add(2, 'days'), text: 'Yeeeees!!!' },
  ];

    searchFilter: SearchFilterConfig = {
      placeHolder: 'Search by student or course title',
      height: '45px',
    };
  

  constructor(
    private _premiumStudentsService: PremiumStudentsService,
    private _messageService: MessageService,
    private _cdr: ChangeDetectorRef
  ) {
    // this.setupSearchListener();
  }

  ngOnInit(): void {
    // this.searchSubject.pipe(
    //   debounceTime(300),
    //   distinctUntilChanged(),
    //   switchMap((searchTerm: string) =>
    //     this._premiumStudentsService.getPremiumStudents(searchTerm)
    //   )
    // ).subscribe(results => this.searchResults = results?.data?.content || []);
    // this.payLoad.startDate = dayjs(this.selectedRangeCalendarTimeRight.startDate).startOf('day').format('YYYY-MM-DDTHH:mm:ss');
    this.fetchPremiumStudents();
  }

  exportData(): void {
    if (this.students && this.students.length > 0) {
      this._premiumStudentsService.downloadExcel(this.payLoad).subscribe(
        (response: Blob) => {
          const blob = new Blob([response], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          });
          const link = document.createElement('a');
          link.href = window.URL.createObjectURL(blob);
          link.download = 'students_export.xlsx';
          document.body.appendChild(link);
          link.click();
          setTimeout(() => {
            document.body.removeChild(link);
            window.URL.revokeObjectURL(link.href);
          }, 100);
        },
        (error: any) => {
          console.error('Export failed:', error);
        }
      );
    } else {
      console.warn('No students available to export.');
    }
  }

  // searchByKeyword(): void {
  //   this.searchKeyword = this.searchKeyword.trim();
  //   if (this.searchKeyword) {
  //     this.payLoad.pageNo = 0;
  //     this.payLoad.searchValue = this.searchKeyword;
  //     this.fetchPremiumStudents();
  //   }
  // }

   searchCallBack(value?: any) {
      this.searchKeyword = value.trim();
      if (value && value.length >= 1) {
        this.payLoad.pageNo = 0;
        this.payLoad.searchValue = this.searchKeyword;
        this.fetchPremiumStudents();
      }
  }

  clearSearch() {
    if(this.searchKeyword){
      this.searchKeyword = '';
      this.showNoCourseText = false;
      this.payLoad.pageNo = 0;
      this.payLoad.searchValue = this.searchKeyword;
      this.fetchPremiumStudents();
    }
  }

  isInvalidDate = (m: dayjs.Dayjs) => {
    return this.invalidDates.some((d) => d.isSame(m, 'day'));
  };

  isCustomDate = (date: dayjs.Dayjs) => {
    return date.month() === 0 || date.month() === 6 ? 'mycustomdate' : false;
  };

  isTooltipDate = (m: dayjs.Dayjs) => {
    const tooltip = this.tooltips.find((tt) => tt.date.isSame(m, 'day'));
    return tooltip ? tooltip.text : false;
  };

  datesUpdatedRange(event: any) {
    if (event?.startDate && event?.endDate) {
      const startDate = dayjs(event.startDate)
        .startOf('day')
        .format('YYYY-MM-DDTHH:mm:ss');
      const endDate = dayjs(event.endDate)
        .endOf('day')
        .format('YYYY-MM-DDTHH:mm:ss');
      this.payLoad.startDate = startDate;
      this.payLoad.endDate = endDate;
      this.payLoad.pageNo = 0;
      this.fetchPremiumStudents();
    }
  }

  resetDateRange() {
    this.payLoad.startDate = null;
    this.payLoad.endDate = null;
    this.selectedRangeCalendarTimeRight = null;
    this.fetchPremiumStudents();
  }

  datesUpdatedInline($event: Object) {}

  onChanges(event: any) {
    // if(!event?.startDate && !event?.endDate){
    //   this.payLoad.startDate = null;
    //   this.payLoad.endDate = null;
    //   this.fetchPremiumStudents();
    // }
    // if (event && event.hasOwnProperty('action') && event.action === 'cancel') {
    //   this.selectedRangeCalendarTimeRight = {
    //     startDate: null,
    //     endDate: null,
    //   };
    //   this.fetchPremiumStudents();
    // } else {
    //   console.log('Updated date range:', event);
    // }
  }

  onRangeChanges(event: any) {
    const { Label, dates } = event;
    this.selectedRangeCalendarTimeRight = {
      startDate: dates[0],
      endDate: dates[1],
    };
    this.payLoad.startDate = dayjs(
      this.selectedRangeCalendarTimeRight.startDate
    )
      .startOf('day')
      .format('YYYY-MM-DDTHH:mm:ss');
    this.payLoad.endDate = dayjs(this.selectedRangeCalendarTimeRight.endDate)
      .startOf('day')
      .format('YYYY-MM-DDTHH:mm:ss');
    // this.datesUpdatedRange({
    //   startDate: this.selectedRangeCalendarTimeRight?.startDate,
    //   endDate: dayjs(this.selectedRangeCalendarTimeRight?.endDate),
    // });
  }

  fetchPremiumStudents(): void {
    this._premiumStudentsService.getPremiumStudents(this.payLoad)?.subscribe(
      (response: any) => {
        this.students = response?.data?.content;
        this.totalElements = response?.data?.totalElements;
      },
      (error: any) => {
        this.students = [];
        this.totalElements = 0;
      }
    );
  }

  onPageChange(pageIndex: number) {
    this.payLoad.pageNo = pageIndex - 1;
    this.scrollToTop('top');
    this.fetchPremiumStudents();
  }

  // applyDateRange() {
  //   const startDate =
  //     this.selectedRangeCalendarTimeRight.startDate.toISOString();
  //   const endDate = this.selectedRangeCalendarTimeRight.endDate.toISOString();

  //   if (startDate && endDate) {
  //     this.payLoad.startDate = startDate;
  //     this.payLoad.endDate = endDate;
  //     this.fetchPremiumStudents();
  //   }
  // }

  scrollToTop(id?: any) {
    const targetDiv = document.getElementById(id);
    if (targetDiv) {
      targetDiv.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }
}
