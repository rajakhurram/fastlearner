import { of } from 'rxjs';
import { CoursesComponent } from './courses.component';
import { configureAdminComponentTest } from '../testing/admin-component.testing';



describe('CoursesComponent', () => {

  const course = {

    rawId: 'course-1',

    id: 1,

    title: 'Angular Basics',

    instructor: 'Jane',

    students: 10,

    rating: '4.5',

    status: 'PUBLISHED',

    category: 'Tech',

    price: '$10',

    description: 'Learn Angular',

    created: '2024-01-01',

  };



  it('should create', async () => {

    const { component } = await configureAdminComponentTest(CoursesComponent);

    expect(component).toBeTruthy();

  });



  it('should load instructors, categories, and courses on init', async () => {

    const { component, adminService } = await configureAdminComponentTest(

      CoursesComponent,

    );



    expect(adminService.getInstructorsList).toHaveBeenCalled();

    expect(adminService.getCourseCategory).toHaveBeenCalled();

    expect(adminService.getCoursesList).toHaveBeenCalled();

    expect(component.courses).toEqual([]);

  });



  it('should resolve instructor value and label', async () => {

    const { component } = await configureAdminComponentTest(CoursesComponent);



    expect(

      component.getInstructorValue({ rawId: 'inst-1', email: 'a@b.com' }),

    ).toBe('inst-1');

    expect(

      component.getInstructorLabel({ fullName: 'Jane Doe', email: 'a@b.com' }),

    ).toBe('Jane Doe');

    expect(component.getInstructorLabel({})).toBe('Instructor');

  });



  it('should open and close course drawer', async () => {

    const { component, adminService } = await configureAdminComponentTest(

      CoursesComponent,

    );



    component.openCourseDrawer(course);

    expect(component.isCourseDrawerVisible).toBeTrue();

    expect(adminService.getCourseDetails).toHaveBeenCalledWith('course-1');



    component.closeCourseDrawer();

    expect(component.isCourseDrawerVisible).toBeFalse();

  });



  it('should return status class and label', async () => {

    const { component } = await configureAdminComponentTest(CoursesComponent);



    expect(component.getStatusClass('PUBLISHED')).toBe('published');

    expect(component.getStatusClass('UNPUBLISHED')).toBe('pending');

    expect(component.getStatusClass('DRAFT')).toBe('draft');

    expect(component.getStatusLabel('PUBLISHED')).toBe('Published');

  });



  it('should apply filters and pagination', async () => {

    const { component, adminService } = await configureAdminComponentTest(

      CoursesComponent,

    );

    adminService.getCoursesList.calls.reset();



    component.onSelectFilterChange('status', 'PUBLISHED');

    expect(component.queryParams.status).toBe('PUBLISHED');



    component.onPageChange(2);

    expect(component.queryParams.page).toBe(1);



    component.onPageSizeChange(20);

    expect(component.queryParams.size).toBe(20);

    expect(component.queryParams.page).toBe(0);



    component.clearFilters();

    expect(component.queryParams.search).toBe('');

    expect(adminService.getCoursesList).toHaveBeenCalled();

  });



  it('should publish unpublished course from drawer', async () => {

    const { component, adminService, messageService } =

      await configureAdminComponentTest(CoursesComponent);

    component.courseDetails = { status: 'UNPUBLISHED' };

    adminService.coursePublish.and.returnValue(

      of({ status: 200, message: 'Published' }),

    );



    await component.togglePublishUnpublish('course-1');



    expect(adminService.coursePublish).toHaveBeenCalledWith('course-1');

    expect(messageService.success).toHaveBeenCalledWith('Published');

    expect(component.isCourseDrawerVisible).toBeFalse();

  });

});


