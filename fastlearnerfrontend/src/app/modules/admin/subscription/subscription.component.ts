import { Component } from '@angular/core';
import { Router } from '@angular/router';
import {
  Subject,
  Subscription,
  debounceTime,
  distinctUntilChanged,
  from,
  lastValueFrom,
  switchMap,
} from 'rxjs';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { MessageService } from 'src/app/core/services/message.service';
import { AdminService } from '../admin.service';

interface SubscriptionMetric {
  label: string;
  value: number;
}

@Component({
  selector: 'app-subscription',
  templateUrl: './subscription.component.html',
  styleUrls: ['./subscription.component.scss'],
})
export class SubscriptionComponent {
  startFromDate: Date | null = null;
  startToDate: Date | null = null;
  day: string = 'all-time';
  totalPages: number = 0;
  totalElements: number = 0;
  queryParams: any = {
    search: '',
    planType: '',
    status: '',
    billingCycle: '',
    dateFrom: '',
    dateTo: '',
    page: 0,
    size: 10,
  };

  private searchSubject = new Subject<string>();
  private searchSubscription?: Subscription;

  metrics: SubscriptionMetric[] = [
    // { label: 'Active Subscriptions', value: 5 },
    { label: 'Standard Plan', value: 5 },
    { label: 'Premium Plan', value: 3 },
    { label: 'Enterprise Plan', value: 3 },
  ];

  subscriptionList: any[] = [];
  isTableLoading = false;

  private _httpConstants: HttpConstants = new HttpConstants();

  constructor(
    private readonly router: Router,
    private readonly adminService: AdminService,
    private readonly message: MessageService,
  ) {}

  ngOnInit(): void {
    this.queryParams.page = this.adminService.currentPage - 1;
    this.getSubscriptionStats();
    this.getSubscriptionsList();
  }

  async getSubscriptionStats(): Promise<void> {
    const res: any = await lastValueFrom(
      this.adminService.getSubscriptionStats(),
    );
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
        // case 'Active Subscriptions':
        //   return { ...metric, value: stats.activeSubscriptions ?? 0 };
        case 'Standard Plan':
          return { ...metric, value: stats.standardPlanCount ?? 0 };
        case 'Premium Plan':
          return { ...metric, value: stats.premiumPlanCount ?? 0 };
        case 'Enterprise Plan':
          return { ...metric, value: stats.enterprisePlanCount ?? 0 };
        default:
          return metric;
      }
    });
  }

  async getSubscriptionsList(): Promise<void> {
    this.normalizeDateRange();
    this.isTableLoading = true;
    try {
      const res: any = await lastValueFrom(
        this.adminService.getSubscriptionsList(this.queryParams),
      );
      if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this.subscriptionList = res.data.content;
        this.totalPages = res.data?.totalPages;
        this.totalElements = res.data?.totalElements;
      } else {
        this.message.error(res.message);
      }
    } finally {
      this.isTableLoading = false;
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
            return from(this.getSubscriptionsList());
          }),
        )
        .subscribe();
    }

    this.searchSubject.next(this.queryParams.search);
  }

  onSelectChange(
    field: 'planType' | 'status' | 'billingCycle' | 'dateFrom' | 'dateTo',
    value: string | Date | null,
  ): void {
    if (field === 'dateFrom') {
      this.startFromDate = value as Date | null;
      this.queryParams.dateFrom = this.startFromDate
        ? this.formatDate(this.startFromDate)
        : '';

      if (this.startFromDate && !this.startToDate) {
        this.startToDate = this.startFromDate;
        this.queryParams.dateTo = this.queryParams.dateFrom;
      } else if (!this.startFromDate) {
        this.queryParams.dateTo = this.startToDate
          ? this.formatDate(this.startToDate)
          : '';
      }
    } else if (field === 'dateTo') {
      this.startToDate = value as Date | null;
      this.queryParams.dateTo = this.startToDate
        ? this.formatDate(this.startToDate)
        : '';
    } else {
      this.queryParams[field] = (value as string) ?? '';
    }

    this.queryParams.page = 0;
    this.getSubscriptionsList();
  }

  clearFilters(): void {
    this.startFromDate = null;
    this.startToDate = null;
    this.queryParams = {
      search: '',
      planType: '',
      status: '',
      billingCycle: '',
      dateFrom: '',
      dateTo: '',
      page: 0,
      size: 10,
    };
    this.adminService.currentPage = 1;
    this.getSubscriptionsList();
  }

  onPageChange(page: number): void {
    this.adminService.currentPage = page;
    this.queryParams.page = page - 1;
    this.getSubscriptionsList();
  }

  private normalizeDateRange(): void {
    if (this.queryParams.dateFrom && !this.queryParams.dateTo) {
      this.queryParams.dateTo = this.queryParams.dateFrom;
      if (this.startFromDate && !this.startToDate) {
        this.startToDate = this.startFromDate;
      }
    }
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  routeToInvoices(subscriptionId: string): void {
    this.router.navigate(['/admin/invoices', subscriptionId]);
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
    if (!this.router.url.includes('/admin/invoices')) {
      this.adminService.currentPage = 1;
    }
  }
}
