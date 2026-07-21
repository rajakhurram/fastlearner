import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { from, lastValueFrom, Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { MessageService } from 'src/app/core/services/message.service';
import { AdminService } from '../admin.service';

interface PayoutRecord {
  instructor: string;
  email: string;
  totalEarnings: string;
  pending: string;
  paid: string;
  lastPayout: string;
  status: 'Partially Paid' | 'Pending' | 'Paid';
  canMarkPaid: boolean;
}

@Component({
  selector: 'app-payouts',
  templateUrl: './payouts.component.html',
  styleUrls: ['./payouts.component.scss'],
})
export class PayoutsComponent implements OnInit, OnDestroy {
  private static readonly DRAWER_MOBILE_BREAKPOINT_PX = 510;

  drawerWidth: number | string = 500;
  metrics = [
    { label: 'Total Earnings', value: '0', icon: 'total-earning' },
    { label: 'Pending Payouts', value: '0', icon: 'pending-payout' },
    { label: 'Total Paid Out', value: '0', icon: 'total-paidout' },
    { label: 'This Month Payouts', value: '0', icon: 'this-month-payout' },
  ];

  payouts: any[] = [];
  isTableLoading = false;
  // courses: any[] = [];
  queryParams: any = {
    search: '',
    status: '',
    dateFrom: '',
    dateTo: '',
    page: 0,
    size: 10,
  };
  startFromDate: Date | null = null;
  startToDate: Date | null = null;
  totalElements = 0;
  day: string = 'all-time';
  isPayoutDrawerVisible = false;
  isMarkPaidModalVisible = false;
  selectedPayout: any = null;
  markPaidAmount: number | null = null;
  markPaidDate: Date | null = null;
  markPaidNote = '';
  private _httpConstants: HttpConstants = new HttpConstants();
  private searchSubject = new Subject<string>();
  private searchSubscription?: Subscription;

  // readonly earningsBreakdownByEmail: Record<string, BreakdownItem[]> = {
  //   'sarah@fastlearner.com': [
  //     {
  //       courseTitle: 'Java Fundamentals',
  //       watchTime: '80h 0m',
  //       amount: '$5,200',
  //     },
  //     { courseTitle: 'Advanced Java', watchTime: '60h 0m', amount: '$4,100' },
  //     {
  //       courseTitle: 'Spring Boot Masterclass',
  //       watchTime: '46h 40m',
  //       amount: '$3,200',
  //     },
  //   ],
  // };

  // readonly payoutHistoryByEmail: Record<string, PayoutHistoryItem[]> = {
  //   'sarah@fastlearner.com': [],
  // };

  totalPages: any;

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
      screenWidth <= PayoutsComponent.DRAWER_MOBILE_BREAKPOINT_PX
        ? '100%'
        : 500;
  }

  ngOnInit(): void {
    this.updateDrawerWidth(window.innerWidth);
    this.getPayoutStats();
    this.getPayoutsList();
    // this.getCoursesList();
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }

  // async getCoursesList(): Promise<void> {
  //   const res: any = await lastValueFrom(
  //     this.adminService.getCoursesList({
  //       search: '',
  //       status: '',
  //       category: '',
  //       instructor: '',
  //       page: 0,
  //       size: 1000,
  //     }),
  //   );
  //   if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
  //     this.courses = res.data?.content ?? [];
  //   }
  // }

  async getPayoutStats(): Promise<void> {
    const res: any = await lastValueFrom(this.adminService.getPayoutStats());
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.mapPayoutStatsToMetrics(res.data);
    } else {
      this.message.error(res.message);
    }
  }

  private mapPayoutStatsToMetrics(stats: any): void {
    if (!stats) {
      return;
    }

    this.metrics = this.metrics.map((metric) => {
      if (metric.label === 'Total Earnings') {
        return { ...metric, value: this.formatCurrency(stats.totalEarnings) };
      }
      if (metric.label === 'Pending Payouts') {
        return { ...metric, value: this.formatCurrency(stats.pendingPayouts) };
      }
      if (metric.label === 'Total Paid Out') {
        return { ...metric, value: this.formatCurrency(stats.totalPaidOut) };
      }
      if (metric.label === 'This Month Payouts') {
        return {
          ...metric,
          value: this.formatCurrency(stats.thisMonthPayouts),
        };
      }
      return metric;
    });
  }

  async getPayoutsList(): Promise<void> {
    this.isTableLoading = true;
    try {
      const res: any = await lastValueFrom(
        this.adminService.getPayoutsList(this.queryParams),
      );

      if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this.payouts = res.data.content;
        this.totalElements = res.data.totalElements;
        this.totalPages = res.data.totalPages;
      } else {
        this.message.error(res.message);
      }
    } finally {
      this.isTableLoading = false;
    }
  }

  async getPayoutDetails(instructorId: any): Promise<void> {
    const res: any = await lastValueFrom(
      this.adminService.getPayoutDetails(instructorId),
    );
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.selectedPayout = res.data;
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
            return from(this.getPayoutsList());
          }),
        )
        .subscribe();
    }

    this.searchSubject.next(this.queryParams.search);
  }

  onStatusChange(value: string): void {
    this.queryParams.status = value;
    this.queryParams.page = 0;
    this.getPayoutsList();
  }

  clearFilters(): void {
    this.day = 'all-time';
    this.startFromDate = null;
    this.startToDate = null;
    this.queryParams = {
      search: '',
      status: '',
      dateFrom: '',
      dateTo: '',
      page: 0,
      size: 10,
    };
    this.getPayoutsList();
  }

  // onStartFromDateChange(date: Date | null): void {
  //   this.startFromDate = date;
  //   this.queryParams.dateFrom = date ? this.formatDate(date) : '';
  //   this.queryParams.page = 0;
  //   this.getPayoutsList();
  // }

  // onStartToDateChange(date: Date | null): void {
  //   this.startToDate = date;
  //   this.queryParams.dateTo = date ? this.formatDate(date) : '';
  //   this.queryParams.page = 0;
  //   this.getPayoutsList();
  // }

  onPageChange(page: number): void {
    this.queryParams.page = page - 1;
    this.getPayoutsList();
  }

  openPayoutDrawer(payout: any): void {
    this.selectedPayout = payout;
    this.isPayoutDrawerVisible = true;
    this.getPayoutDetails(payout.instructorId);
  }

  closePayoutDrawer(): void {
    this.isPayoutDrawerVisible = false;
    this.selectedPayout = null;
  }

  // openMarkPaidModal(payout: any): void {
  //   this.selectedPayout = payout;
  //   this.markPaidAmount = this.pendingAmountForModal;
  //   this.markPaidDate = new Date();
  //   this.markPaidNote = '';
  //   this.isMarkPaidModalVisible = true;
  // }

  closeMarkPaidModal(): void {
    this.isMarkPaidModalVisible = false;
  }

  // confirmMarkPaid(): void {
  //   if (!this.selectedPayout || !this.canConfirmPayment) {
  //     return;
  //   }

  //   this.message.success('Payout marked as paid successfully.');
  //   this.closeMarkPaidModal();
  // }

  // get remainingAmountText(): string {
  //   if (!this.selectedPayout) return '';
  //   return this.selectedPayout.pending;
  // }

  // get selectedEarningsBreakdown(): BreakdownItem[] {
  //   if (!this.selectedPayout) return [];
  //   return this.earningsBreakdownByEmail[this.selectedPayout.email] || [];
  // }

  // get selectedPayoutHistory(): PayoutHistoryItem[] {
  //   if (!this.selectedPayout) return [];
  //   return this.payoutHistoryByEmail[this.selectedPayout.email] || [];
  // }

  getStatusClass(status: PayoutRecord['status']): string {
    return status.toLowerCase().replace(/\s+/g, '-');
  }

  // get pendingAmountForModal(): number {
  //   return this.parseAmount(this.selectedPayout?.pending);
  // }

  // get canConfirmPayment(): boolean {
  //   return (
  //     !!this.markPaidDate &&
  //     !!this.markPaidAmount &&
  //     this.markPaidAmount > 0 &&
  //     this.markPaidAmount <= this.pendingAmountForModal
  //   );
  // }

  // private normalizeStatus(status: string): PayoutRecord['status'] {
  //   if (status === 'Paid') {
  //     return 'Paid';
  //   }
  //   if (status === 'Partially Paid') {
  //     return 'Partially Paid';
  //   }
  //   return 'Pending';
  // }

  private formatCurrency(value: any): string {
    const numericValue = Number(value ?? 0);
    return `$${numericValue.toLocaleString(undefined, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  // private parseAmount(value: unknown): number {
  //   if (typeof value === 'number') {
  //     return Number.isFinite(value) ? value : 0;
  //   }

  //   if (typeof value === 'string') {
  //     const normalized = value.replace(/[^0-9.-]/g, '');
  //     const parsed = Number(normalized);
  //     return Number.isFinite(parsed) ? parsed : 0;
  //   }

  //   return 0;
  // }

  onDateFilterChange(filter: string): void {
    this.day = filter;
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
    this.getPayoutsList();
  }

  // private formatDate(date: Date): string {
  //   const year = date.getFullYear();
  //   const month = String(date.getMonth() + 1).padStart(2, '0');
  //   const day = String(date.getDate()).padStart(2, '0');
  //   return `${year}-${month}-${day}`;
  // }
}
