import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraderAssessmentComponent } from './grader-assessment.component';

describe('GraderAssessmentComponent', () => {
  let component: GraderAssessmentComponent;
  let fixture: ComponentFixture<GraderAssessmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GraderAssessmentComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GraderAssessmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
