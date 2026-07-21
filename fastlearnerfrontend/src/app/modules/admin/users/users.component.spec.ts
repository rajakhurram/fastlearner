import { NgForm } from '@angular/forms';

import { of } from 'rxjs';

import { UsersComponent } from './users.component';

import {

  configureAdminComponentTest,

  createAdminServiceSpy,

} from '../testing/admin-component.testing';



describe('UsersComponent', () => {

  const user = {

    rawId: 'raw-1',

    id: '1',

    name: 'John Doe',

    email: 'john@example.com',

    registered: '2024-01-01',

    planType: 'Premium',

    subscription: 'Active',

    status: 'Active',

  };



  async function setup() {

    const adminService = createAdminServiceSpy();

    adminService.getUserStats.and.returnValue(

      of({

        status: 200,

        data: {

          totalUsers: 10,

          freeUsers: 2,

          standardUsers: 3,

          premiumUsers: 4,

          enterpriseUsers: 1,

        },

      }),

    );

    return configureAdminComponentTest(UsersComponent, [], adminService);

  }



  it('should create', async () => {

    const { component } = await setup();

    expect(component).toBeTruthy();

  });



  it('should load stats and users on init', async () => {

    const { component, adminService } = await setup();



    expect(adminService.getUserStats).toHaveBeenCalled();

    expect(adminService.getUsersList).toHaveBeenCalled();

    expect(component.metrics[0].value).toBe(10);

    expect(component.users).toEqual([]);

  });



  it('should update drawer width on resize', async () => {

    const { component } = await setup();



    component.onWindowResize({ target: { innerWidth: 400 } } as unknown as UIEvent);

    expect(component.drawerWidth).toBe('100%');



    component.onWindowResize({ target: { innerWidth: 1200 } } as unknown as UIEvent);

    expect(component.drawerWidth).toBe(500);

  });



  it('should open and close user drawer', async () => {

    const { component, adminService } = await setup();



    component.openUserDrawer(user);



    expect(component.isUserDrawerVisible).toBeTrue();

    expect(component.selectedUser).toEqual(user);

    expect(adminService.getUserOverview).toHaveBeenCalledWith('raw-1');



    component.closeUserDrawer();

    expect(component.isUserDrawerVisible).toBeFalse();

  });



  it('should load overview data when drawer tab changes', async () => {

    const { component, adminService } = await setup();

    component.selectedUser = user;



    component.onDrawerTabChange(1);

    expect(adminService.getSubscriptionOverview).toHaveBeenCalledWith('raw-1');



    component.onDrawerTabChange(2);

    expect(adminService.getCourseOverview).toHaveBeenCalledWith('raw-1');



    component.onDrawerTabChange(3);

    expect(adminService.getTransactionOverview).toHaveBeenCalledWith('raw-1');

  });



  it('should return color helpers', async () => {

    const { component } = await setup();



    expect(component.getPlanColor('Premium')).toBe('blue');

    expect(component.getPlanColor('Standard')).toBe('gold');

    expect(component.getSubscriptionColor('Active')).toBe('green');

    expect(component.getStatusColor('Inactive')).toBe('red');

    expect(component.getColor('Total Users')?.bg).toBe('#262261');

  });



  it('should apply and clear filters', async () => {

    const { component, adminService } = await setup();

    adminService.getUsersList.calls.reset();



    component.onSelectFilterChange('planType', 'PREMIUM');

    expect(component.queryParams.planType).toBe('PREMIUM');

    expect(adminService.getUsersList).toHaveBeenCalled();



    component.clearFilters();

    expect(component.queryParams.search).toBe('');

    expect(component.day).toBe('all-time');

  });



  it('should update page on pagination', async () => {

    const { component, adminService } = await setup();

    adminService.getUsersList.calls.reset();



    component.onPageChange(2);

    expect(component.queryParams.page).toBe(1);

    expect(adminService.getUsersList).toHaveBeenCalled();

  });



  it('should open and close add admin modal', async () => {

    const { component } = await setup();



    component.openAddAdminModal();

    expect(component.isAddAdminModalVisible).toBeTrue();



    component.closeAddAdminModal();

    expect(component.isAddAdminModalVisible).toBeFalse();

  });



  it('should invite admin when form is valid', async () => {

    const { component, adminService, messageService } = await setup();

    adminService.inviteAdmin.and.returnValue(

      of({ status: 200, message: 'Invite sent' }),

    );

    component.adminEmail = 'admin@example.com';



    await component.inviteAdmin({ invalid: false } as NgForm);



    expect(adminService.inviteAdmin).toHaveBeenCalledWith({

      email: 'admin@example.com',

      link: component.link,

    });

    expect(messageService.success).toHaveBeenCalledWith('Invite sent');

    expect(component.isAddAdminModalVisible).toBeFalse();

  });



  it('should not invite admin when form is invalid', async () => {

    const { component, adminService } = await setup();



    await component.inviteAdmin({ invalid: true } as NgForm);



    expect(adminService.inviteAdmin).not.toHaveBeenCalled();

  });

});


