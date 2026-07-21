import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { of } from 'rxjs';

import { SubscriptionComponent } from './subscription.component';

import {

  configureAdminComponentTest,

  createAdminServiceSpy,

} from '../testing/admin-component.testing';



describe('SubscriptionComponent', () => {

  async function setup() {

    const adminService = createAdminServiceSpy();

    const router = jasmine.createSpyObj('Router', ['navigate'], {

      url: '/admin/subscription',

    });

    adminService.getSubscriptionStats.and.returnValue(

      of({

        status: 200,

        data: {

          standardPlanCount: 5,

          premiumPlanCount: 3,

          enterprisePlanCount: 1,

        },

      }),

    );



    return configureAdminComponentTest(

      SubscriptionComponent,

      [{ provide: Router, useValue: router }],

      adminService,

    );

  }



  it('should create', async () => {

    const { component } = await setup();

    expect(component).toBeTruthy();

  });



  it('should load stats and subscriptions on init', async () => {

    const { component, adminService } = await setup();



    expect(adminService.getSubscriptionStats).toHaveBeenCalled();

    expect(adminService.getSubscriptionsList).toHaveBeenCalled();

    expect(component.metrics[0].value).toBe(5);

    expect(component.subscriptionList).toEqual([]);

  });



  it('should apply select filters', async () => {

    const { component, adminService } = await setup();

    adminService.getSubscriptionsList.calls.reset();



    component.onSelectChange('planType', 'PREMIUM');

    expect(component.queryParams.planType).toBe('PREMIUM');

    expect(adminService.getSubscriptionsList).toHaveBeenCalled();

  });



  it('should set date range when from date is selected', async () => {

    const { component, adminService } = await setup();

    const fromDate = new Date(2024, 0, 15);

    adminService.getSubscriptionsList.calls.reset();



    component.onSelectChange('dateFrom', fromDate);



    expect(component.queryParams.dateFrom).toBe('2024-01-15');

    expect(component.queryParams.dateTo).toBe('2024-01-15');

    expect(adminService.getSubscriptionsList).toHaveBeenCalled();

  });



  it('should clear filters and reset current page', async () => {

    const { component, adminService } = await setup();

    component.queryParams.search = 'test';

    adminService.currentPage = 3;

    adminService.getSubscriptionsList.calls.reset();



    component.clearFilters();



    expect(component.queryParams.search).toBe('');

    expect(adminService.currentPage).toBe(1);

    expect(adminService.getSubscriptionsList).toHaveBeenCalled();

  });



  it('should update page and navigate to invoices', async () => {

    const { component, adminService } = await setup();

    const router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    adminService.getSubscriptionsList.calls.reset();



    component.onPageChange(2);

    expect(component.queryParams.page).toBe(1);

    expect(adminService.currentPage).toBe(2);

    expect(adminService.getSubscriptionsList).toHaveBeenCalled();



    component.routeToInvoices('sub-99');

    expect(router.navigate).toHaveBeenCalledWith(['/admin/invoices', 'sub-99']);

  });



  it('should reset current page on destroy when not on invoices route', async () => {

    const { component, adminService } = await setup();

    adminService.currentPage = 4;



    component.ngOnDestroy();



    expect(adminService.currentPage).toBe(1);

  });

});


