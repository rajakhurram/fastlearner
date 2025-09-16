import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClassAssessmentDropdownComponent } from './class-assessment-dropdown.component';

describe('ClassAssessmentDropdownComponent', () => {
  let component: ClassAssessmentDropdownComponent;
  let fixture: ComponentFixture<ClassAssessmentDropdownComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClassAssessmentDropdownComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClassAssessmentDropdownComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
