import { of } from 'rxjs';

import { PaymentsComponent } from './payments.component';

import { configureAdminComponentTest } from '../testing/admin-component.testing';



describe('PaymentsComponent', () => {

  it('should create', async () => {

    const { component } = await configureAdminComponentTest(PaymentsComponent);

    expect(component).toBeTruthy();

  });



  it('should load payments on init', async () => {

    const { component, adminService } = await configureAdminComponentTest(

      PaymentsComponent,

    );



    expect(adminService.getPaymentList).toHaveBeenCalled();

    expect(component.payments).toEqual([]);

  });



  it('should return css helper classes', async () => {

    const { component } = await configureAdminComponentTest(PaymentsComponent);



    expect(component.getPlanClass('Premium')).toBe('premium');

    expect(component.getTypeClass('Subscription')).toBe('subscription');

    expect(component.getTransactionStatusClass('Successful')).toBe('successful');

    expect(component.getTransactionStatusClass('Failed')).toBe('failed');

    expect(component.getTransactionStatusClass('Unknown')).toBe('pending');

  });



  it('should apply filters and pagination', async () => {

    const { component, adminService } = await configureAdminComponentTest(

      PaymentsComponent,

    );

    adminService.getPaymentList.calls.reset();



    component.onSelectFilterChange('planType', 'Premium');

    expect(component.queryParams.planType).toBe('Premium');



    component.onPageChange(2);

    expect(component.queryParams.page).toBe(1);



    component.clearFilters();

    expect(component.queryParams.search).toBe('');

    expect(adminService.getPaymentList).toHaveBeenCalled();

  });



  it('should download payment csv', async () => {

    const { component, adminService, messageService } =

      await configureAdminComponentTest(PaymentsComponent);

    adminService.downloadPaymentCSV.and.returnValue(

      of({ message: 'CSV ready', data: 'id,amount' }),

    );

    spyOn(component, 'downloadCsv');



    await component.getPaymentCSV();



    expect(component.downloadCsv).toHaveBeenCalledWith(
      jasmine.objectContaining({ message: 'CSV ready' }),
      'payments.csv',
    );

    expect(messageService.success).toHaveBeenCalledWith('CSV ready');

  });

});


