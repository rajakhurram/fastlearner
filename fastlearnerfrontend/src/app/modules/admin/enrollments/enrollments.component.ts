import { Component } from '@angular/core';
import { from, lastValueFrom, Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { AdminService } from '../admin.service';
import { MessageService } from 'src/app/core/services/message.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';

interface EnrollmentRecord {
  id: string;
  student: string;
  course: string;
  enrolled: string;
  progress: number;
  status: 'Active' | 'Completed' | 'Inactive';
}

@Component({
  selector: 'app-enrollments',
  templateUrl: './enrollments.component.html',
  styleUrls: ['./enrollments.component.scss'],
})
export class EnrollmentsComponent {
  enrollments: any[] = [];
  isTableLoading = false;
  courses: any[] = [];
  totalElements: number = 0;
  totalPages: number = 0;
  page: number = 1;
  size: number = 10;
  queryParams: any = {
    search: '',
    courseId: '',
    status: '',
    progress: '',
    page: 0,
    size: 10,
  };
  courseQueryParams: any = {
    search: '',
    status: '',
    category: '',
    instructor: '',
    page: 0,
    size: 1000,
  };
  private _httpConstants: HttpConstants = new HttpConstants();
  private searchSubject = new Subject<string>();
  private searchSubscription?: Subscription;

  constructor(
    private adminService: AdminService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.getCoursesList();
    this.getEnrollmentsList();
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }

  getStatusClass(status: EnrollmentRecord['status']): string {
    return status.toLowerCase();
  }

  async getEnrollmentsList(): Promise<void> {
    this.isTableLoading = true;
    try {
      const res: any = await lastValueFrom(
        this.adminService.getEnrollmentsList(this.queryParams),
      );
      if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this.enrollments = res.data.content;
        this.totalElements = res.data.totalElements;
      } else {
        this.message.error(res.message);
      }
    } finally {
      this.isTableLoading = false;
    }
  }

  async getCoursesList(): Promise<void> {
    const res: any = await lastValueFrom(
      this.adminService.getCoursesList(this.courseQueryParams),
    );
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.courses = res?.data?.content ?? [];
    } else {
      this.message.error(res.message);
    }
  }

  onSearchChange(searchTerm: string): void {
    this.queryParams.page = 0;
    this.queryParams.search = searchTerm ?? '';

    if (!this.searchSubscription) {
      this.searchSubscription = this.searchSubject
        .pipe(
          debounceTime(350),
          distinctUntilChanged(),
          switchMap((term: string) => {
            this.queryParams.search = term;
            return from(this.getEnrollmentsList());
          }),
        )
        .subscribe();
    }

    this.searchSubject.next(this.queryParams.search);
  }

  onSelectFilterChange(
    field: 'status' | 'courseId' | 'progress',
    value: string,
  ): void {
    this.queryParams[field] = value;
    this.queryParams.page = 0;
    this.getEnrollmentsList();
  }

  clearFilters(): void {
    this.queryParams = {
      search: '',
      courseId: '',
      status: '',
      progress: '',
      page: 0,
      size: 10,
    };
    this.getEnrollmentsList();
  }

  onPageChange(page: number): void {
    this.queryParams.page = page - 1;
    this.getEnrollmentsList();
  }
}
