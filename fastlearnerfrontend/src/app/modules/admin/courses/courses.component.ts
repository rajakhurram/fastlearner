import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { from, lastValueFrom, Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { MessageService } from 'src/app/core/services/message.service';
import { AdminService } from '../admin.service';

interface AdminCourse {
  rawId?: string;
  id: string | number;
  title: string;
  instructor: string;
  students: number;
  rating: string;
  status: 'DRAFT' | 'PUBLISHED' | 'UNPUBLISHED' | string;
  category: string;
  price: string;
  description: string;
  created: string;
}

@Component({
  selector: 'app-courses',
  templateUrl: './courses.component.html',
  styleUrls: ['./courses.component.scss'],
})
export class CoursesComponent implements OnInit, OnDestroy {
  private static readonly DRAWER_MOBILE_BREAKPOINT_PX = 510;

  drawerWidth: number | string = 500;
  isCourseDrawerVisible = false;
  selectedCourse: AdminCourse | null = null;
  totalElements: number = 0;
  instructors: any[] = [];
  queryParams: any = {
    search: '',
    status: '',
    category: '',
    instructor: '',
    page: 0,
    size: 10,
  };

  courses: any[] = [];
  isTableLoading = false;
  courseDetails: any = null;
  courseCategoryList: any[] = [];
  private _httpConstants: HttpConstants = new HttpConstants();
  private searchSubject = new Subject<string>();
  private searchSubscription?: Subscription;

  constructor(
    private adminService: AdminService,
    private message: MessageService,
  ) {}

  @HostListener('window:resize', ['$event'])
  onWindowResize(event: UIEvent): void {
    this.updateDrawerWidth((event.target as Window).innerWidth);
  }

  private updateDrawerWidth(screenWidth: number): void {
    this.drawerWidth =
      screenWidth <= CoursesComponent.DRAWER_MOBILE_BREAKPOINT_PX
        ? '100%'
        : 500;
  }

  ngOnInit(): void {
    this.updateDrawerWidth(window.innerWidth);
    this.getInstructorsList();
    this.getCoursesList();
    this.getCourseCategoryList();
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }

  async getCourseCategoryList(): Promise<void> {
    const res: any = await lastValueFrom(this.adminService.getCourseCategory());
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.courseCategoryList = res.data;
    } else {
      this.message.error(res.message);
    }
  }

  async getCoursesList() {
    this.isTableLoading = true;
    try {
      const res: any = await lastValueFrom(
        this.adminService.getCoursesList(this.queryParams),
      );

      if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this.courses = res.data.content;
        this.totalElements = res.data.totalElements;
      } else {
        this.message.error(res.message);
      }
    } finally {
      this.isTableLoading = false;
    }
  }

  async getCourseDetails(rawId: string): Promise<void> {
    const res: any = await lastValueFrom(
      this.adminService.getCourseDetails(rawId),
    );
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.courseDetails = res.data;
    } else {
      this.message.error(res.message);
    }
  }

  async getInstructorsList(): Promise<void> {
    const res: any = await lastValueFrom(
      this.adminService.getInstructorsList(),
    );
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.instructors = res?.data ?? [];
    } else {
      this.message.error(res.message);
    }
  }

  getInstructorValue(instructor: any): string | number {
    return (
      instructor?.rawId ??
      instructor?.id ??
      instructor?.instructorId ??
      instructor?.email ??
      ''
    );
  }

  getInstructorLabel(instructor: any): string {
    return (
      instructor?.name ||
      instructor?.fullName ||
      instructor?.instructorName ||
      instructor?.email ||
      'Instructor'
    );
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
            return from(this.getCoursesList());
          }),
        )
        .subscribe();
    }

    this.searchSubject.next(this.queryParams.search);
  }

  onSelectFilterChange(
    field: 'status' | 'category' | 'instructor',
    value: string,
  ): void {
    this.queryParams[field] = value;
    this.queryParams.page = 0;
    this.getCoursesList();
  }

  clearFilters(): void {
    this.queryParams = {
      search: '',
      status: '',
      category: '',
      instructor: '',
      page: 0,
      size: 10,
    };
    this.getCoursesList();
  }

  onPageChange(page: number): void {
    this.queryParams.page = page - 1;
    this.getCoursesList();
  }

  onPageSizeChange(size: number): void {
    this.queryParams.size = size;
    this.queryParams.page = 0;
    this.getCoursesList();
  }

  openCourseDrawer(course: AdminCourse): void {
    this.selectedCourse = course;
    this.isCourseDrawerVisible = true;
    this.getCourseDetails(course.rawId);
  }

  closeCourseDrawer(): void {
    this.isCourseDrawerVisible = false;
  }

  getStatusClass(status: AdminCourse['status']): string {
    const normalized = String(status).toUpperCase();
    if (normalized === 'PUBLISHED') {
      return 'published';
    }
    if (normalized === 'UNPUBLISHED') {
      return 'pending';
    }
    return 'draft';
  }

  getStatusLabel(status: AdminCourse['status']): string {
    const normalized = String(status).toUpperCase();
    if (normalized === 'PUBLISHED') {
      return 'Published';
    }
    if (normalized === 'UNPUBLISHED') {
      return 'Unpublished';
    }
    return 'Draft';
  }

  async togglePublishUnpublish(courseId: any) {
    const api =
      this.courseDetails.status === 'UNPUBLISHED'
        ? this.adminService.coursePublish(courseId)
        : this.adminService.courseUnpublish(courseId);
    const res: any = await lastValueFrom(api);
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.message.success(res.message);
      this.closeCourseDrawer();
      this.getCoursesList();
    } else {
      this.message.error(res.message);
    }
  }
}
