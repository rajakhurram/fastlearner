import { NgForm } from '@angular/forms';
import { of } from 'rxjs';
import { SettingsComponent } from './settings.component';
import { configureAdminComponentTest } from '../testing/admin-component.testing';

describe('SettingsComponent', () => {
  it('should create', async () => {
    const { component } = await configureAdminComponentTest(SettingsComponent);
    expect(component).toBeTruthy();
  });

  it('should load admins on init', async () => {
    const { component, adminService } = await configureAdminComponentTest(
      SettingsComponent,
    );

    expect(adminService.getSettingsAdmins).toHaveBeenCalled();
    expect(component.list).toEqual([]);
  });

  it('should open and close add admin modal', async () => {
    const { component } = await configureAdminComponentTest(SettingsComponent);

    component.openAddAdminModal();
    expect(component.isAddAdminModalVisible).toBeTrue();
    expect(component.adminEmail).toBe('');

    component.closeAddAdminModal();
    expect(component.isAddAdminModalVisible).toBeFalse();
  });

  it('should add admin when form is valid', async () => {
    const { component, adminService, messageService } =
      await configureAdminComponentTest(SettingsComponent);
    adminService.addAdmin.and.returnValue(
      of({ status: 200, message: 'Admin added' }),
    );
    component.adminEmail = 'new-admin@example.com';

    await component.addAdmin({ invalid: false } as NgForm);

    expect(adminService.addAdmin).toHaveBeenCalledWith({
      email: 'new-admin@example.com',
    });
    expect(messageService.success).toHaveBeenCalledWith('Admin added');
    expect(component.isAddAdminModalVisible).toBeFalse();
    expect(adminService.getSettingsAdmins).toHaveBeenCalledTimes(2);
  });

  it('should not add admin when form is invalid', async () => {
    const { component, adminService } = await configureAdminComponentTest(
      SettingsComponent,
    );

    await component.addAdmin({ invalid: true } as NgForm);

    expect(adminService.addAdmin).not.toHaveBeenCalled();
  });

  it('should deactivate admin', async () => {
    const { component, adminService, messageService } =
      await configureAdminComponentTest(SettingsComponent);
    adminService.deActivateAdmin.and.returnValue(
      of({ status: 200, message: 'Admin deactivated' }),
    );

    await component.deActivateAdmin('admin-1');

    expect(adminService.deActivateAdmin).toHaveBeenCalledWith('admin-1');
    expect(messageService.success).toHaveBeenCalledWith('Admin deactivated');
    expect(adminService.getSettingsAdmins).toHaveBeenCalledTimes(2);
  });
});
