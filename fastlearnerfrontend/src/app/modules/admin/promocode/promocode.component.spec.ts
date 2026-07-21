import { of } from 'rxjs';
import { PromocodeComponent } from './promocode.component';
import { Coupon } from './coupon.model';
import {
  configureAdminComponentTest,
  createAdminServiceSpy,
} from '../testing/admin-component.testing';

describe('PromocodeComponent', () => {
  async function setup() {
    return configureAdminComponentTest(
      PromocodeComponent,
      [],
      createAdminServiceSpy(),
    );
  }

  it('should create', async () => {
    const { component } = await setup();
    expect(component).toBeTruthy();
  });

  it('should load coupons on init', async () => {
    const { component, adminService } = await setup();

    expect(adminService.getCouponsList).toHaveBeenCalled();
    expect(component.list).toEqual([]);
  });

  it('should validate percentage discounts', async () => {
    const { component } = await setup();

    component.coupon.discountUnit = 'PERCENTAGE';
    component.coupon.discount = 150;
    expect(component.isFormInvalid()).toBeTrue();

    component.coupon.discount = 20;
    expect(component.isFormInvalid()).toBeFalse();
  });

  it('should manage create promo modal', async () => {
    const { component } = await setup();

    component.openCreatePromoModal();
    expect(component.isCreatePromoModalVisible).toBeTrue();
    expect(component.coupon).toEqual(jasmine.any(Coupon));

    component.closeCreatePromoModal();
    expect(component.isCreatePromoModalVisible).toBeFalse();
  });

  it('should handle applies-to changes', async () => {
    const { component, adminService } = await setup();
    component.coupon.subscriptionId = 'sub-1';
    component.coupon.specifiedCourses = ['course-1'];

    component.onAppliesToChange('COURSE_BASED');
    expect(component.coupon.subscriptionId).toBe('');
    expect(adminService.getCoursesList).toHaveBeenCalled();

    component.onAppliesToChange('SUBSCRIPTION_BASED');
    expect(component.coupon.specifiedCourses).toEqual([]);
  });

  it('should update filters and pagination', async () => {
    const { component, adminService } = await setup();
    adminService.getCouponsList.calls.reset();

    component.onSelectFilterChange('status', 'ACTIVE');
    expect(component.coupenQuery.isActive).toBe('true');

    component.onPageChange(3);
    expect(component.coupenQuery.page).toBe(2);

    component.clearFilters();
    expect(component.coupenQuery.search).toBe('');
    expect(adminService.getCouponsList).toHaveBeenCalled();
  });

  it('should return status class and plan value', async () => {
    const { component } = await setup();

    expect(component.getStatusClass('ACTIVE')).toBe('active');
    expect(component.getStatusClass('EXPIRED')).toBe('expired');
    expect(component.getPlanValue({ id: 'plan-1' })).toBe('plan-1');
  });

  it('should toggle coupon status', async () => {
    const { component, adminService, messageService } = await setup();
    adminService.toggleCouponStatus.and.returnValue(
      of({ status: 200, message: 'Toggled' }),
    );
    adminService.getCouponsList.calls.reset();

    await component.toggleCouponStatus(10);

    expect(adminService.toggleCouponStatus).toHaveBeenCalledWith(10);
    expect(messageService.success).toHaveBeenCalledWith('Toggled');
    expect(adminService.getCouponsList).toHaveBeenCalled();
  });

  it('should delete coupon', async () => {
    const { component, adminService, messageService } = await setup();
    adminService.deleteCoupon.and.returnValue(
      of({ status: 200, message: 'Deleted' }),
    );
    adminService.getCouponsList.calls.reset();

    await component.onDeleteCoupon({ id: 5 });

    expect(adminService.deleteCoupon).toHaveBeenCalledWith(5);
    expect(messageService.success).toHaveBeenCalledWith('Deleted');
    expect(adminService.getCouponsList).toHaveBeenCalled();
  });

  it('should create promo code', async () => {
    const { component, adminService, messageService } = await setup();
    adminService.createCoupon.and.returnValue(
      of({ status: 200, message: 'Created' }),
    );
    adminService.getCouponsList.calls.reset();
    component.coupon.coupon = 'SAVE10';

    await component.createPromoCode();

    expect(adminService.createCoupon).toHaveBeenCalled();
    expect(messageService.success).toHaveBeenCalledWith('Created');
    expect(component.isCreatePromoModalVisible).toBeFalse();
    expect(adminService.getCouponsList).toHaveBeenCalled();
  });

  it('should set durationInMonth to 1 when billing cycle is MONTHLY', async () => {
    const { component } = await setup();
    component.coupon.billingCycle = 'MONTHLY';

    await component.getcoupenSubscription();

    expect(component.coupon.durationInMonth).toBe(1);
  });

  it('should set durationInMonth to 12 when billing cycle is YEARLY', async () => {
    const { component } = await setup();
    component.coupon.billingCycle = 'YEARLY';

    await component.getcoupenSubscription();

    expect(component.coupon.durationInMonth).toBe(12);
  });
});
