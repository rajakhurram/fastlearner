import { of } from 'rxjs';
import { PayoutsComponent } from './payouts.component';
import {
  configureAdminComponentTest,
  createAdminServiceSpy,
} from '../testing/admin-component.testing';

describe('PayoutsComponent', () => {
  async function setup() {
    const adminService = createAdminServiceSpy();
    adminService.getPayoutStats.and.returnValue(
      of({
        status: 200,
        data: {
          totalEarnings: 1000,
          pendingPayouts: 200,
          totalPaidOut: 800,
          thisMonthPayouts: 150,
        },
      }),
    );
    return configureAdminComponentTest(PayoutsComponent, [], adminService);
  }

  it('should create', async () => {
    const { component } = await setup();
    expect(component).toBeTruthy();
  });

  it('should load payout stats and list on init', async () => {
    const { component, adminService } = await setup();

    expect(adminService.getPayoutStats).toHaveBeenCalled();
    expect(adminService.getPayoutsList).toHaveBeenCalled();
    expect(component.metrics[0].value).toContain('$1,000.00');
    expect(component.payouts).toEqual([]);
  });

  it('should open and close payout drawer', async () => {
    const { component, adminService } = await setup();
    const payout = { instructorId: 'inst-1', email: 'inst@example.com' };

    component.openPayoutDrawer(payout);
    expect(component.isPayoutDrawerVisible).toBeTrue();
    expect(adminService.getPayoutDetails).toHaveBeenCalledWith('inst-1');

    component.closePayoutDrawer();
    expect(component.isPayoutDrawerVisible).toBeFalse();
    expect(component.selectedPayout).toBeNull();
  });

  it('should return status class', async () => {
    const { component } = await setup();
    expect(component.getStatusClass('Partially Paid')).toBe('partially-paid');
    expect(component.getStatusClass('Paid')).toBe('paid');
  });

  it('should apply filters, search, and pagination', async () => {
    const { component, adminService } = await setup();
    adminService.getPayoutsList.calls.reset();

    component.onStatusChange('Pending');
    expect(component.queryParams.status).toBe('Pending');

    component.onSearchChange('john');
    expect(component.queryParams.page).toBe(0);
    expect(component.queryParams.search).toBe('john');

    component.onPageChange(2);
    expect(component.queryParams.page).toBe(1);

    component.clearFilters();
    expect(component.day).toBe('all-time');
    expect(component.queryParams.search).toBe('');
    expect(adminService.getPayoutsList).toHaveBeenCalled();
  });

  it('should close mark paid modal', async () => {
    const { component } = await setup();
    component.isMarkPaidModalVisible = true;
    component.closeMarkPaidModal();
    expect(component.isMarkPaidModalVisible).toBeFalse();
  });
});
