import { Component } from '@angular/core';
import { AdminService } from '../admin.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { MessageService } from 'src/app/core/services/message.service';
import { from, lastValueFrom, Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
interface PaymentRecord {
  transactionId: string;
  user: string;
  planType: 'Premium' | 'Standard' | 'Free';
  amount: string;
  date: string;
  type: 'Subscription' | 'Course';
  status: 'Successful' | 'Pending' | 'Failed' | 'Trialed' | 'Unsuccessful';
}

@Component({
  selector: 'app-payments',
  templateUrl: './payments.component.html',
  styleUrls: ['./payments.component.scss'],
})
export class PaymentsComponent {
  payments: any[] = [];
  isTableLoading = false;
  totalElements: number = 0;
  totalPages: number = 0;
  queryParams: any = {
    search: '',
    planType: '',
    status: '',
    type: '',
    dateFrom: '',
    dateTo: '',
    page: 0,
    size: 10,
  };
  private _httpConstants: HttpConstants = new HttpConstants();
  private searchSubject = new Subject<string>();
  private searchSubscription?: Subscription;

  constructor(
    private adminService: AdminService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.getPaymentsList();
  }

  async getPaymentsList(): Promise<void> {
    this.isTableLoading = true;
    try {
      const res: any = await lastValueFrom(
        this.adminService.getPaymentList(this.queryParams),
      );
      if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this.payments = res.data.content;
        this.totalElements = res.data.totalElements;
        this.totalPages = res.data.totalPages;
      } else {
        this.message.error(res.message);
      }
    } finally {
      this.isTableLoading = false;
    }
  }
  getPlanClass(plan: PaymentRecord['planType']): string {
    return plan.toLowerCase();
  }

  getTypeClass(type: PaymentRecord['type']): string {
    return type.toLowerCase();
  }

  /** Matches API labels from subscription_status (see SuperAdminService.resolvePaymentStatus). */
  getTransactionStatusClass(status: string): string {
    const key = (status ?? '').toLowerCase();
    if (key === 'successful' || key === 'completed') {
      return 'successful';
    }
    if (key === 'pending') {
      return 'pending';
    }
    if (key === 'failed') {
      return 'failed';
    }
    if (key === 'trialed') {
      return 'trialed';
    }
    if (key === 'unsuccessful') {
      return 'unsuccessful';
    }
    return 'pending';
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
            return from(this.getPaymentsList());
          }),
        )
        .subscribe();
    }

    this.searchSubject.next(this.queryParams.search);
  }

  onSelectFilterChange(
    field: 'planType' | 'status' | 'type',
    value: string,
  ): void {
    this.queryParams[field] = value;
    this.queryParams.page = 0;
    this.getPaymentsList();
  }

  clearFilters(): void {
    this.queryParams = {
      search: '',
      planType: '',
      status: '',
      type: '',
      dateFrom: '',
      dateTo: '',
      page: 0,
      size: 10,
    };
    this.getPaymentsList();
  }

  onPageChange(page: number): void {
    this.queryParams.page = page - 1;
    this.getPaymentsList();
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }

  async getPaymentCSV() {
    const res: any = await lastValueFrom(
      this.adminService.downloadPaymentCSV(this.queryParams),
    );
    if (res) {
      this.downloadCsv(res, 'payments.csv');
      this.message.success(res.message);
    } else {
      this.message.error(res.message);
    }
  }

  downloadCsv(csvData: string, fileName: string = 'file.csv') {
    const blob = new Blob([csvData], { type: 'text/csv;charset=utf-8;' });

    const url = window.URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.click();

    window.URL.revokeObjectURL(url);
  }
}
