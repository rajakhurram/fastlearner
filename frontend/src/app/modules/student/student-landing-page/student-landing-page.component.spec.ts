import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudentLandingPageComponent } from './student-landing-page.component';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('StudentLandingPageComponent', () => {
  let component: StudentLandingPageComponent;
  let fixture: ComponentFixture<StudentLandingPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [StudentLandingPageComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(StudentLandingPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
