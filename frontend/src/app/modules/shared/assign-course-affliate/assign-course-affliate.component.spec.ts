import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssignCourseAffliateComponent } from './assign-course-affliate.component';

describe('AssignCourseAffliateComponent', () => {
  let component: AssignCourseAffliateComponent;
  let fixture: ComponentFixture<AssignCourseAffliateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AssignCourseAffliateComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AssignCourseAffliateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
