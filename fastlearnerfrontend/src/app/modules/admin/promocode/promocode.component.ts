import { Component } from '@angular/core';
import { from, lastValueFrom, Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { AdminService } from '../admin.service';
import { MessageService } from 'src/app/core/services/message.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { Coupon } from './coupon.model';

@Component({
  selector: 'app-promocode',
  templateUrl: './promocode.component.html',
  styleUrls: ['./promocode.component.scss'],
})
export class PromocodeComponent {
  isCreatePromoModalVisible = false;

  promoCode = '';
  discountType = 'Percentage (%)';
  discountValue: number | null = null;
  promoAppliesTo = 'Subscription Plans';
  targetUserEmail = '';
  targetDomain = '';
  expiryDate: Date | null = null;

  readonly discountTypes = ['PERCENTAGE', 'FIXED_AMOUNT'];
  readonly applyToOptions = [
    { label: 'Subscription Plans', value: 'SUBSCRIPTION_BASED' },
    { label: 'Specific Course', value: 'COURSE_BASED' },
  ];
  readonly couponTypes = [
    { label: 'Subscription', value: 'SUBSCRIPTION' },
    { label: 'Premium', value: 'PREMIUM' },
    // { label: 'Both', value: 'BOTH' },
  ];
  queryParams: any = {
    search: '',
    status: '',
    category: '',
    instructor: '',
    page: 0,
    size: 1000,
  };

  coupenQuery = {
    search: '',
    status: '',
    page: 0,
    size: 10,
    isActive: '',
  };
  private searchSubject = new Subject<string>();
  private searchSubscription?: Subscription;
  courseList: any[] = [];
  private _httpConstants: HttpConstants = new HttpConstants();
  list: any[] = [];
  isTableLoading = false;
  coupon: Coupon = new Coupon();
  coupenSubscriptionList: any[] = [];
  totalElements: any;
  totalPages: any;
  constructor(
    private adminService: AdminService,
    private message: MessageService,
  ) {}

  ngOnInit(): void {
    this.getCouponsList();
    // this.getcoupenSubscription();
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }

  isFormInvalid(): boolean {
    if (
      this.coupon.discountUnit === 'PERCENTAGE' &&
      this.coupon.discount > 100
    ) {
      return true;
    }
    return false;
  }

  onAppliesToChange(value: string) {
    if (value === 'SUBSCRIPTION_BASED') {
      this.coupon.specifiedCourses = [];
      // this.getcoupenSubscription(this.coupon.billingCycle);
    } else {
      this.coupon.subscriptionId = '';
      this.getCoursesList();
    }
  }

  // onBillingCycleChange(value: string) {
  //   this.coupon.billingCycle = value;
  //   this.getcoupenSubscription(value);
  // }

  async getcoupenSubscription() {
    const res: any = await lastValueFrom(
      this.adminService.getCoupenSubscription(this.coupon.billingCycle),
    );
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.coupenSubscriptionList = res.data;
      this.coupon.durationInMonth =
        this.coupon.billingCycle === 'MONTHLY' ? 1 : 12;
      if (
        !this.coupon.subscriptionId &&
        this.coupenSubscriptionList.length > 0
      ) {
        this.coupon.subscriptionId = this.getPlanValue(
          this.coupenSubscriptionList[0],
        );
      }
    } else {
      this.message.error(res.message);
    }
  }

  async getCoursesList() {
    const res: any = await lastValueFrom(
      this.adminService.getCoursesList(this.queryParams),
    );

    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.courseList = res.data.content;
      this.courseList = this.courseList.filter(
        (c) => c.courseType == 'PREMIUM_COURSE',
      );
      // if (!this.coupon.specifiedCourses.length) {
      //   this.coupon.specifiedCourses.push(this.courseList[0].rawId);
      // }
    } else {
      this.message.error(res.message);
    }
  }

  async getCouponsList(): Promise<void> {
    this.isTableLoading = true;
    try {
      const res: any = await lastValueFrom(
        this.adminService.getCouponsList(this.coupenQuery),
      );
      if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this.list = res.data.content;
        this.totalElements = res.data.totalElements;
        this.totalPages = res.data.totalPages;
      } else {
        this.message.error(res.message);
      }
    } finally {
      this.isTableLoading = false;
    }
  }

  openCreatePromoModal(): void {
    this.targetUserEmail = '';
    this.targetDomain = '';
    this.coupon = new Coupon();
    this.isCreatePromoModalVisible = true;
  }

  closeCreatePromoModal(): void {
    this.isCreatePromoModalVisible = false;
  }

  // generatePromoCode(): void {
  //   const randomChunk = Math.random().toString(36).slice(2, 7).toUpperCase();
  //   this.promoCode = `SAVE${randomChunk}`;
  // }

  onPageChange(event: any): void {
    this.coupenQuery.page = event - 1;
    this.getCouponsList();
  }

  onSearchChange(searchTerm: string): void {
    this.coupenQuery.page = 0;
    this.coupenQuery.search = searchTerm ?? '';

    if (!this.searchSubscription) {
      this.searchSubscription = this.searchSubject
        .pipe(
          debounceTime(350),
          distinctUntilChanged(),
          switchMap((term: string) => {
            this.coupenQuery.search = term;
            return from(this.getCouponsList());
          }),
        )
        .subscribe();
    }

    this.searchSubject.next(this.coupenQuery.search);
  }

  onSelectFilterChange(field: 'status', value: string): void {
    this.coupenQuery[field] = value;
    this.coupenQuery.isActive =
      value === 'ACTIVE' ? 'true' : value === 'INACTIVE' ? 'false' : '';
    this.coupenQuery.page = 0;
    this.getCouponsList();
  }

  clearFilters(): void {
    this.coupenQuery = {
      search: '',
      status: '',
      page: 0,
      size: 10,
      isActive: '',
    };
    this.getCouponsList();
  }

  // async onUpdate(promo: any) {
  //   this.coupon = new Coupon(
  //     promo.id,
  //     promo.coupon,
  //     promo.discountUnit,
  //     promo.discount,
  //     promo.subscriptionId,
  //     promo.startDate,
  //     promo.endDate,
  //     promo.appliesTo,
  //     promo.specifiedCourses,
  //     promo.specifiedUsers,
  //     promo.isActive ?? true,
  //     promo.specifiedDomains ?? [],
  //     promo.couponType ?? '',
  //     promo.billingCycle ?? '',
  //   );
  //   this.targetUserEmail = (promo?.specifiedUsers || []).join(', ');
  //   this.targetDomain = (promo?.specifiedDomains || []).join(', ');

  //   if (
  //     this.coupon.appliesTo === 'SUBSCRIPTION_BASED' &&
  //     this.coupon.billingCycle
  //   ) {
  //     await this.getcoupenSubscription();
  //   } else if (this.coupon.appliesTo === 'COURSE_BASED') {
  //     await this.getCoursesList();
  //   }

  //   this.isCreatePromoModalVisible = true;
  // }

  async getcoupenById(id: string | number) {
    const res: any = await lastValueFrom(this.adminService.getCoupenById(id));
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.coupon = new Coupon(
        res.data.id,
        res.data.coupon,
        res.data.discountUnit,
        res.data.discount,
        res.data.subscriptionId,
        res.data.startDate,
        res.data.endDate,
        res.data.appliesTo,
        res.data.specifiedCourses,
        res.data.specifiedUsers,
        res.data.isActive,
        res.data.specifiedEmailDomains,
        res.data.couponType ?? '',
        res.data.billingCycle ?? '',
      );
      this.targetUserEmail = (res.data.specifiedUsers || []).join(', ');
      this.targetDomain = (res.data.specifiedEmailDomains || []).join(', ');

      if (
        this.coupon.appliesTo === 'SUBSCRIPTION_BASED' &&
        this.coupon.billingCycle
      ) {
        await this.getcoupenSubscription();
      } else if (this.coupon.appliesTo === 'COURSE_BASED') {
        await this.getCoursesList();
      }

      this.isCreatePromoModalVisible = true;
    } else {
      this.message.error(res.message);
    }
  }

  async onDeactivateCoupon(promo: any): Promise<void> {
    const id = this.getCouponId(promo);
    if (!id) {
      this.message.error('Invalid coupon id.');
      return;
    }

    const payload = this.buildUpdatePayload(promo, false);
    const res: any = await lastValueFrom(
      this.adminService.updateCoupon(payload),
    );
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.message.success(res.message || 'Coupon deactivated successfully.');
      this.getCouponsList();
    } else {
      this.message.error(res.message || 'Unable to deactivate coupon.');
    }
  }

  async onDeleteCoupon(promo: any): Promise<void> {
    this.adminService.deleteCoupon(promo.id).subscribe((res: any) => {
      if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this.message.success(res.message);
        this.getCouponsList();
      } else {
        this.message.error(res.message);
      }
    });
  }

  async createPromoCode() {
    this.coupon.specifiedUsers = this.parseSpecifiedUsers(this.targetUserEmail);
    this.coupon.specifiedDomains = this.parseSpecifiedDomains(
      this.targetDomain,
    );
    this.coupon.startDate = this.formatDate(this.coupon.startDate);
    this.coupon.endDate = this.formatDate(this.coupon.endDate);
    try {
      const res: any = await lastValueFrom(
        this.adminService.createCoupon(this.coupon),
      );
      if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this.message.success(res.message);
        this.closeCreatePromoModal();
        this.getCouponsList();
      } else {
        this.message.error(res.message || 'Failed to create promo code.');
      }
    } catch (error: any) {
      this.message.error(
        error?.error?.message ||
          error?.message ||
          'Failed to create promo code.',
      );
    }
  }

  async updatePromoCode() {
    const id = this.getCouponId(this.coupon);
    if (!id) {
      this.message.error('Invalid coupon id.');
      return;
    }

    this.coupon.specifiedUsers = this.parseSpecifiedUsers(this.targetUserEmail);
    this.coupon.specifiedDomains = this.parseSpecifiedDomains(
      this.targetDomain,
    );
    this.coupon.startDate = this.formatDate(this.coupon.startDate);
    this.coupon.endDate = this.formatDate(this.coupon.endDate);
    try {
      const res: any = await lastValueFrom(
        this.adminService.updateCoupon(this.coupon),
      );
      if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this.message.success(res.message);
        this.closeCreatePromoModal();
        this.getCouponsList();
      } else {
        this.message.error(res.message);
      }
    } catch (error: any) {
      this.message.error(
        error?.error?.message ||
          error?.message ||
          'Failed to update promo code.',
      );
    }
  }

  async toggleCouponStatus(id: string | number) {
    const res: any = await lastValueFrom(
      this.adminService.toggleCouponStatus(id),
    );
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.message.success(res.message);
      this.getCouponsList();
    } else {
      this.message.error(res.message);
    }
  }

  getStatusClass(status: string): string {
    const normalized = String(status || '').toLowerCase();

    if (normalized === 'active') {
      return 'active';
    }

    return 'expired';
  }

  getPlanValue(plan: any): string | number {
    return plan?.id ?? plan?.subscriptionId ?? plan?.planId ?? '';
  }

  getPlanLabel(plan: any): string {
    return (
      plan?.name ||
      plan?.subscriptionName ||
      plan?.planName ||
      `Plan ${this.getPlanValue(plan)}`
    );
  }

  private parseSpecifiedUsers(value: string): string[] {
    return String(value || '')
      .split(',')
      .map((email) => email.trim())
      .filter((email) => !!email);
  }

  private parseSpecifiedDomains(value: string): string[] {
    return String(value || '')
      .split(',')
      .map((domain) => domain.trim().toLowerCase().replace(/^@/, ''))
      .filter((domain) => !!domain);
  }

  formatDate(date: Date | string | null | undefined): string {
    if (!date) {
      return '';
    }

    if (typeof date === 'string') {
      return date.includes('T') ? date.split('T')[0] : date;
    }

    if (!(date instanceof Date) || Number.isNaN(date.getTime())) {
      return '';
    }

    return date.toISOString().split('T')[0];
  }

  private getCouponId(promo: any): string | number | null {
    return promo?.id ?? promo?.couponId ?? promo?.rawId ?? null;
  }

  private buildUpdatePayload(promo: any, isActive: boolean): Coupon {
    return new Coupon(
      this.getCouponId(promo),
      promo?.coupon ?? promo?.code ?? '',
      promo?.discountUnit ?? 'PERCENTAGE',
      Number(promo?.discount ?? 0),
      promo?.subscriptionId ?? '',
      promo?.startDate ?? '',
      promo?.endDate ?? '',
      promo?.appliesTo ?? 'SUBSCRIPTION_BASED',
      promo?.specifiedCourses ?? [],
      promo?.specifiedUsers ?? [],
      isActive,
      promo?.specifiedDomains ?? [],
      promo?.couponType ?? '',
      promo?.billingCycle ?? '',
    );
  }
}
