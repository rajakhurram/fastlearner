import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CourseCardComponent } from './course-card.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { of } from 'rxjs';
import { CourseType } from 'src/app/core/enums/course-status';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('CourseCardComponent', () => {
  let component: CourseCardComponent;
  let fixture: ComponentFixture<CourseCardComponent>;
  let authServiceMock: any;

  beforeEach(async () => {
    authServiceMock = {
      isLoggedIn: jasmine.createSpy().and.returnValue(true),
      $changeNavbarSate: of(true),
    };

    await TestBed.configureTestingModule({
      declarations: [CourseCardComponent],
      providers: [{ provide: AuthService, useValue: authServiceMock }],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CourseCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default @Input properties', () => {
    expect(component.isPremium).toBeFalse();
    expect(component.fromMyCourse).toBeFalse();
    expect(component.fromFavouriteCourse).toBeFalse();
    expect(component.buttonTheme).toBeUndefined();
  });

  it('should initialize isLoggedIn from AuthService', () => {
    expect(component.isLoggedIn).toBeTrue();
    expect(authServiceMock.isLoggedIn).toHaveBeenCalled();
  });

  it('should subscribe to $changeNavbarSate on ngOnInit and update isLoggedIn', () => {
    authServiceMock.isLoggedIn.and.returnValue(false);
    component.ngOnInit();
    expect(component.isLoggedIn).toBeFalse();
  });

  it('should emit routeToCourseEmitter with the course URL when routeToCourseDetails is called', () => {
    spyOn(component.routeToCourseEmitter, 'emit');
    const courseUrl = 'test-course-url';
    component.routeToCourseDetails(courseUrl);
    expect(component.routeToCourseEmitter.emit).toHaveBeenCalledWith(courseUrl);
  });

  it('should emit routeToInstructorprofileEmitter with the event and profile URL when routeToInsructorProfile is called', () => {
    spyOn(component.routeToInstructorprofileEmitter, 'emit');
    const mockEvent = new Event('click');
    const profileUrl = 'test-profile-url';
    component.routeToInsructorProfile(mockEvent, profileUrl);
    expect(component.routeToInstructorprofileEmitter.emit).toHaveBeenCalledWith(
      {
        event: mockEvent,
        profileUrl: profileUrl,
      }
    );
  });

  it('should emit favoriteCourseEmitter with course ID when favouriteCourse is called', () => {
    spyOn(component.favoriteCourseEmitter, 'emit');
    const courseId = '123';
    component.favouriteCourse(courseId);
    expect(component.favoriteCourseEmitter.emit).toHaveBeenCalledWith(courseId);
  });

  it('should have courseType set to CourseType enum', () => {
    expect(component.courseType).toEqual(CourseType);
  });
});
