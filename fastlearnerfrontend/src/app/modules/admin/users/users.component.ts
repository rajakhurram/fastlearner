import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { AdminService } from '../admin.service';
import { from, lastValueFrom, Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { MessageService } from 'src/app/core/services/message.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { NgForm } from '@angular/forms';

interface AdminUser {
  rawId?: string;
  id: string;
  name: string;
  email: string;
  registered: string;
  planType: string;
  subscription: string;
  status: string;
}

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss'],
})
export class UsersComponent implements OnInit, OnDestroy {
  private static readonly DRAWER_MOBILE_BREAKPOINT_PX = 510;

  drawerWidth: number | string = 500;
  isUserDrawerVisible = false;
  selectedUser: AdminUser | null = null;
  activeDrawerTabIndex = 0;
  day: string = 'all-time';
  totalElements: number = 0;
  queryParams: any = {
    search: '',
    planType: '',
    subscriptionStatus: '',
    accountStatus: '',
    dateFrom: '',
    dateTo: '',
    page: 0,
    size: 10,
  };
  metrics = [
    { label: 'Total Users', value: 0, icon: 'total-users' },
    { label: 'Free Users', value: 0, icon: 'free-users' },
    { label: 'Standard Users', value: 0, icon: 'standard-users' },
    { label: 'Premium Users', value: 0, icon: 'premium-users' },
    { label: 'Enterprise Users', value: 0, icon: 'subscription-new' },
  ];

  users: any[] = [];
  isUsersTableLoading = false;
  UserOverview: any;
  subscriptionOverview: any = null;
  courseOverview: any[] = [];
  transactionOverview: any[] = [];

  colors = [
    { label: 'Total Users', bg: '#262261', icon: '#fff' },
    { label: 'Standard Users', bg: '#EBF2FE', icon: '#fff' },
    { label: 'Premium Users', bg: '#FEF5E6', icon: '#fff' },
    { label: 'Free Users', bg: '#EEEFF2', icon: '#EEEFF2' },
  ];

  isAddAdminModalVisible = false;
  adminEmail = '';
  link: string = 'https://fastlearner.ai/auth/sign-up';

  private _httpConstants: HttpConstants = new HttpConstants();
  totalPages: number = 0;
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
      screenWidth <= UsersComponent.DRAWER_MOBILE_BREAKPOINT_PX ? '100%' : 500;
  }

  ngOnInit(): void {
    this.updateDrawerWidth(window.innerWidth);
    this.getUserStats();
    this.getUsersList();
    // this.onDateFilterChange(this.day);
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
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
            return from(this.getUsersList());
          }),
        )
        .subscribe();
    }

    this.searchSubject.next(this.queryParams.search);
  }

  onSelectFilterChange(
    field: 'planType' | 'subscriptionStatus' | 'accountStatus',
    value: string,
  ): void {
    this.queryParams[field] = value;
    this.queryParams.page = 0;
    this.getUsersList();
  }

  clearFilters(): void {
    this.day = 'all-time';
    this.queryParams = {
      search: '',
      planType: '',
      subscriptionStatus: '',
      accountStatus: '',
      dateFrom: '',
      dateTo: '',
      page: 0,
      size: 10,
    };
    this.getUsersList();
  }

  async getUserStats() {
    const res: any = await lastValueFrom(this.adminService.getUserStats());
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.mapStatsToMetrics(res.data);
    } else {
      this.message.error(res.message);
    }
  }

  private mapStatsToMetrics(stats: any): void {
    if (!stats) {
      return;
    }

    this.metrics = this.metrics.map((metric) => {
      switch (metric.label) {
        case 'Total Users':
          return { ...metric, value: stats.totalUsers ?? 0 };
        case 'Free Users':
          return { ...metric, value: stats.freeUsers ?? 0 };
        case 'Standard Users':
          return { ...metric, value: stats.standardUsers ?? 0 };
        case 'Premium Users':
          return { ...metric, value: stats.premiumUsers ?? 0 };
        case 'Enterprise Users':
          return {
            ...metric,
            value: stats.enterprise_users ?? stats.enterpriseUsers ?? 0,
          };
        default:
          return metric;
      }
    });
  }

  async getUsersList() {
    this.isUsersTableLoading = true;
    try {
      const res: any = await lastValueFrom(
        this.adminService.getUsersList(this.queryParams),
      );
      if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this.users = res.data.content;
        this.totalPages = res.data.totalPages;
        this.totalElements = res.data.totalElements;
        console.log(this.totalElements);
      } else {
        this.message.error(res.message);
      }
    } finally {
      this.isUsersTableLoading = false;
    }
  }

  async getUserOverview(rawId: string): Promise<void> {
    const res: any = await lastValueFrom(
      this.adminService.getUserOverview(rawId),
    );
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      console.log(res);
      this.UserOverview = res.data;
    } else {
      this.message.error(res.message);
    }
  }

  async getSubscriptionOverview(rawId: string): Promise<void> {
    const res: any = await lastValueFrom(
      this.adminService.getSubscriptionOverview(rawId),
    );
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.subscriptionOverview = res.data;
    } else {
      this.message.error(res.message);
    }
  }

  async getCourseOverview(rawId: string): Promise<void> {
    const res: any = await lastValueFrom(
      this.adminService.getCourseOverview(rawId),
    );
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      console.log(res);
      this.courseOverview = res.data;
    } else {
      this.message.error(res.message);
    }
  }

  async getTransactionOverview(rawId: string): Promise<void> {
    const res: any = await lastValueFrom(
      this.adminService.getTransactionOverview(rawId),
    );
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.transactionOverview = res.data;
    } else {
      this.message.error(res.message);
    }
  }

  getColor(label: string) {
    const color = this.colors.find((color) => color.label === label);
    return color;
  }

  openUserDrawer(user: any): void {
    this.selectedUser = user;
    this.activeDrawerTabIndex = 0;
    this.isUserDrawerVisible = true;
    this.UserOverview = null;
    this.subscriptionOverview = null;
    this.courseOverview = [];
    this.transactionOverview = [];
    this.onDrawerTabChange(0);
  }

  closeUserDrawer(): void {
    this.isUserDrawerVisible = false;
  }

  onDrawerTabChange(tabIndex: number): void {
    this.activeDrawerTabIndex = tabIndex;

    if (!this.selectedUser?.rawId) {
      return;
    }

    const currentRawId = this.selectedUser.rawId;
    if (tabIndex === 0) {
      this.getUserOverview(currentRawId);
      return;
    }

    if (tabIndex === 1) {
      this.getSubscriptionOverview(currentRawId);
      return;
    }

    if (tabIndex === 2) {
      this.getCourseOverview(currentRawId);
      return;
    }

    if (tabIndex === 3) {
      this.getTransactionOverview(currentRawId);
    }
  }

  getPlanColor(planType: string): string {
    if (planType === 'Premium') {
      return 'blue';
    }
    if (planType === 'Standard') {
      return 'gold';
    }
    return 'default';
  }

  getSubscriptionColor(subscription: string): string {
    if (subscription === 'Active') {
      return 'green';
    }
    if (subscription === 'Cancelled') {
      return 'red';
    }
    return 'default';
  }

  getStatusColor(status: string): string {
    return status === 'Active' ? 'green' : 'red';
  }

  onDateFilterChange(filter: string): void {
    this.day = filter;
    this.queryParams.page = 0;
    const today = new Date();

    switch (filter) {
      case 'all-time':
        this.queryParams.dateFrom = '';
        this.queryParams.dateTo = '';
        break;

      case 'today':
        this.queryParams.dateFrom = this.formatDate(today);
        this.queryParams.dateTo = this.formatDate(today);
        break;

      case 'this-week': {
        // Week starts on Monday.
        const startOfWeek = new Date(today);
        const dayOfWeek = startOfWeek.getDay(); // 0 = Sunday, 1 = Monday
        const diffToMonday = dayOfWeek === 0 ? 6 : dayOfWeek - 1;
        startOfWeek.setDate(startOfWeek.getDate() - diffToMonday);
        this.queryParams.dateFrom = this.formatDate(startOfWeek);
        this.queryParams.dateTo = this.formatDate(today);
        break;
      }

      case 'this-month': {
        const startOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
        this.queryParams.dateFrom = this.formatDate(startOfMonth);
        this.queryParams.dateTo = this.formatDate(today);
        break;
      }

      case 'last-month': {
        const startOfLastMonth = new Date(
          today.getFullYear(),
          today.getMonth() - 1,
          1,
        );
        const endOfLastMonth = new Date(
          today.getFullYear(),
          today.getMonth(),
          0,
        );
        this.queryParams.dateFrom = this.formatDate(startOfLastMonth);
        this.queryParams.dateTo = this.formatDate(endOfLastMonth);
        break;
      }

      case 'last-3-month': {
        // Current month + previous 2 full months (3 months total).
        const startOfRange = new Date(
          today.getFullYear(),
          today.getMonth() - 2,
          1,
        );
        this.queryParams.dateFrom = this.formatDate(startOfRange);
        this.queryParams.dateTo = this.formatDate(today);
        break;
      }

      default:
        this.queryParams.dateFrom = '';
        this.queryParams.dateTo = '';
        break;
    }

    this.getUsersList();
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  onPageChange(event: any): void {
    this.queryParams.page = event - 1;
    this.getUsersList();
  }

  openAddAdminModal(): void {
    this.isAddAdminModalVisible = true;
    this.adminEmail = '';
  }

  closeAddAdminModal(): void {
    this.isAddAdminModalVisible = false;
    this.adminEmail = '';
  }

  async inviteAdmin(form: NgForm) {
    if (form.invalid) {
      return;
    }

    const payload = {
      email: this.adminEmail,
      link: this.link,
    };
    try {
      const res: any = await lastValueFrom(
        this.adminService.inviteAdmin(payload),
      );
      if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this.message.success(res.message);
        this.closeAddAdminModal();
      } else {
        this.message.error(res.message || 'Unable to invite admin.');
      }
    } catch (error: any) {
      this.message.error(
        error?.error?.message || error?.message || 'Unable to invite admin.',
      );
    }
  }

  async exportUserCsv() {
    try {
      const blob: Blob = await lastValueFrom(
        this.adminService.exportUserCsv(this.queryParams),
      );

      // ✅ File download karo
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'users.csv';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);

      this.message.success('CSV exported successfully');
    } catch (err) {
      this.message.error('Export failed');
      console.error(err);
    }
  }
}
