import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { from, lastValueFrom, Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { MessageService } from 'src/app/core/services/message.service';
import { AdminService } from '../admin.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-invoices',
  templateUrl: './invoices.component.html',
  styleUrls: ['./invoices.component.scss'],
})
export class InvoicesComponent implements OnInit, OnDestroy {
  private static readonly DRAWER_MOBILE_BREAKPOINT_PX = 510;

  drawerWidth: number | string = 500;
  invoiceFromDate: Date | null = null;
  invoiceToDate: Date | null = null;
  queryParams: any = {
    search: '',
    planType: '',
    type: '',
    status: '',
    dateFrom: '',
    dateTo: '',
    page: 0,
    size: 10,
  };
  totalElements = 0;
  totalPages = 0;
  invoiceId: string | number = '';
  private _httpConstants: HttpConstants = new HttpConstants();

  invoices: any[] = [];
  isTableLoading = false;
  isInvoiceDrawerVisible = false;
  selectedInvoice: any = null;
  private searchSubject = new Subject<string>();
  private searchSubscription?: Subscription;

  subscriptionId: string = '';

  constructor(
    private adminService: AdminService,
    private message: MessageService,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  goBackToSubscriptions(): void {
    this.router.navigate(['/admin/subscription']);
  }

  @HostListener('window:resize', ['$event'])
  onWindowResize(event: UIEvent): void {
    this.updateDrawerWidth((event.target as Window).innerWidth);
  }

  private updateDrawerWidth(screenWidth: number): void {
    this.drawerWidth =
      screenWidth <= InvoicesComponent.DRAWER_MOBILE_BREAKPOINT_PX
        ? '100%'
        : 500;
  }

  ngOnInit(): void {
    this.updateDrawerWidth(window.innerWidth);
    this.subscriptionId = this.route.snapshot.params['subscriptionId'];
    this.getInvoicesList();
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }

  async getInvoicesList(): Promise<void> {
    this.isTableLoading = true;
    try {
      const res: any = await lastValueFrom(
        this.adminService.getInvoicesList(this.subscriptionId, this.queryParams),
      );

      if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this.invoices = res.data.content;
        this.totalElements = res.data.totalElements;
        this.totalPages = res.data.totalPages;
      } else {
        this.message.error(res.message);
      }
    } finally {
      this.isTableLoading = false;
    }
  }

  async getInvoiceDetailById(invoiceId: string): Promise<void> {
    const res: any = await lastValueFrom(
      this.adminService.getInvoiceDetailById(invoiceId),
    );
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.selectedInvoice = res.data;
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
            return from(this.getInvoicesList());
          }),
        )
        .subscribe();
    }

    this.searchSubject.next(this.queryParams.search);
  }

  onSelectFilterChange(
    field: 'planType' | 'status' | 'dateFrom' | 'dateTo',
    value: string | Date | null,
  ): void {
    if (field === 'dateFrom') {
      this.invoiceFromDate = value as Date | null;
      this.queryParams.dateFrom = this.invoiceFromDate
        ? this.formatDate(this.invoiceFromDate)
        : '';
    } else if (field === 'dateTo') {
      this.invoiceToDate = value as Date | null;
      this.queryParams.dateTo = this.invoiceToDate
        ? this.formatDate(this.invoiceToDate)
        : '';
    } else {
      this.queryParams[field] = (value as string) ?? '';
    }

    this.queryParams.page = 0;
    this.getInvoicesList();
  }

  clearFilters(): void {
    this.invoiceFromDate = null;
    this.invoiceToDate = null;
    this.queryParams = {
      search: '',
      planType: '',
      type: '',
      status: '',
      dateFrom: '',
      dateTo: '',
      page: 0,
      size: 10,
    };
    this.getInvoicesList();
  }

  onPreviousPage(): void {
    if (this.queryParams.page <= 0) {
      return;
    }
    this.queryParams.page -= 1;
    this.getInvoicesList();
  }

  onNextPage(): void {
    if (this.queryParams.page + 1 >= this.totalPages) {
      return;
    }
    this.queryParams.page += 1;
    this.getInvoicesList();
  }

  get currentPage(): number {
    return this.queryParams.page + 1;
  }

  get startItemIndex(): number {
    if (this.totalElements === 0) {
      return 0;
    }
    return this.queryParams.page * this.queryParams.size + 1;
  }

  get endItemIndex(): number {
    return Math.min(
      (this.queryParams.page + 1) * this.queryParams.size,
      this.totalElements,
    );
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  async downloadinvoice(invoiceId: any) {
    const res: any = await lastValueFrom(
      this.adminService.downloadInvoice(invoiceId),
    );
    if (res) {
      this.downloadPdf(res, `invoice-${invoiceId}.pdf`);
      this.message.success(res.message);
    } else {
      this.message.error(res.message);
    }
  }

  async sendinvoice(invoiceId: any) {
    const res: any = await lastValueFrom(
      this.adminService.sendInvoice(invoiceId),
    );

    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.message.success(res.message);
    } else {
      this.message.error(res.message);
    }
  }

  openInvoiceDrawer(invoice: any): void {
    // this.selectedInvoice = invoice;
    this.invoiceId = invoice.rawId;
    this.getInvoiceDetailById(invoice.rawId);
    this.isInvoiceDrawerVisible = true;
  }

  closeInvoiceDrawer(): void {
    this.isInvoiceDrawerVisible = false;
    this.selectedInvoice = null;
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
    if (key === 'failed' || key === 'unsuccessful') {
      return 'failed';
    }
    if (key === 'trialed') {
      return 'trialed';
    }
    return 'pending';
  }

  downloadPdf(blob: Blob, fileName: string = 'file.pdf') {
    const url = window.URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.click();

    window.URL.revokeObjectURL(url);
  }
}
