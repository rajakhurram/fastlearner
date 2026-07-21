import { EnrollmentsComponent } from './enrollments.component';
import { configureAdminComponentTest } from '../testing/admin-component.testing';

describe('EnrollmentsComponent', () => {
  it('should create', async () => {
    const { component } = await configureAdminComponentTest(EnrollmentsComponent);
    expect(component).toBeTruthy();
  });

  it('should load courses and enrollments on init', async () => {
    const { component, adminService } = await configureAdminComponentTest(
      EnrollmentsComponent,
    );

    expect(adminService.getCoursesList).toHaveBeenCalled();
    expect(adminService.getEnrollmentsList).toHaveBeenCalled();
    expect(component.enrollments).toEqual([]);
  });

  it('should return status class', async () => {
    const { component } = await configureAdminComponentTest(EnrollmentsComponent);
    expect(component.getStatusClass('Active')).toBe('active');
    expect(component.getStatusClass('Completed')).toBe('completed');
  });

  it('should apply filters and pagination', async () => {
    const { component, adminService } = await configureAdminComponentTest(
      EnrollmentsComponent,
    );
    adminService.getEnrollmentsList.calls.reset();

    component.onSelectFilterChange('status', 'Active');
    expect(component.queryParams.status).toBe('Active');

    component.onPageChange(3);
    expect(component.queryParams.page).toBe(2);

    component.clearFilters();
    expect(component.queryParams.search).toBe('');
    expect(adminService.getEnrollmentsList).toHaveBeenCalled();
  });
});
