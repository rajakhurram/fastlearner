import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { createActivatedRouteMock } from 'src/app/testing/router.testing';
import { InvoicesComponent } from './invoices.component';
import { configureAdminComponentTest } from '../testing/admin-component.testing';

describe('InvoicesComponent', () => {
  async function setup() {
    const router = jasmine.createSpyObj('Router', ['navigate']);
    return configureAdminComponentTest(InvoicesComponent, [
      { provide: Router, useValue: router },
      {
        provide: ActivatedRoute,
        useValue: createActivatedRouteMock({
          params: { subscriptionId: 'sub-123' },
        } as any),
      },
    ]);
  }

  it('should create', async () => {
    const { component } = await setup();
    expect(component).toBeTruthy();
  });

  it('should load invoices for route subscription id', async () => {
    const { component, adminService } = await setup();

    expect(component.subscriptionId).toBe('sub-123');
    expect(adminService.getInvoicesList).toHaveBeenCalledWith(
      'sub-123',
      component.queryParams,
    );
    expect(component.invoices).toEqual([]);
  });

  it('should navigate back to subscriptions', async () => {
    const { component } = await setup();
    const router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    component.goBackToSubscriptions();

    expect(router.navigate).toHaveBeenCalledWith(['/admin/subscription']);
  });

  it('should return transaction status class', async () => {
    const { component } = await setup();

    expect(component.getTransactionStatusClass('Successful')).toBe('successful');
    expect(component.getTransactionStatusClass('Pending')).toBe('pending');
    expect(component.getTransactionStatusClass('Failed')).toBe('failed');
  });

  it('should calculate pagination indexes', async () => {
    const { component } = await setup();
    component.totalElements = 25;
    component.queryParams.page = 1;
    component.queryParams.size = 10;

    expect(component.currentPage).toBe(2);
    expect(component.startItemIndex).toBe(11);
    expect(component.endItemIndex).toBe(20);
  });

  it('should open and close invoice drawer', async () => {
    const { component, adminService } = await setup();
    const invoice = { rawId: 'inv-1' };

    component.openInvoiceDrawer(invoice);
    expect(component.isInvoiceDrawerVisible).toBeTrue();
    expect(adminService.getInvoiceDetailById).toHaveBeenCalledWith('inv-1');

    component.closeInvoiceDrawer();
    expect(component.isInvoiceDrawerVisible).toBeFalse();
    expect(component.selectedInvoice).toBeNull();
  });

  it('should apply filters and page navigation', async () => {
    const { component, adminService } = await setup();
    component.totalPages = 3;
    adminService.getInvoicesList.calls.reset();

    component.onSelectFilterChange('status', 'Paid');
    expect(component.queryParams.status).toBe('Paid');

    component.onNextPage();
    expect(component.queryParams.page).toBe(1);

    component.onPreviousPage();
    expect(component.queryParams.page).toBe(0);

    component.clearFilters();
    expect(component.queryParams.search).toBe('');
    expect(adminService.getInvoicesList).toHaveBeenCalled();
  });

  it('should send invoice on success', async () => {
    const { component, adminService, messageService } = await setup();
    adminService.sendInvoice.and.returnValue(
      of({ status: 200, message: 'Invoice sent' }),
    );

    await component.sendinvoice('inv-9');

    expect(adminService.sendInvoice).toHaveBeenCalledWith('inv-9');
    expect(messageService.success).toHaveBeenCalledWith('Invoice sent');
  });
});
