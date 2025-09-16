import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AiGraderStudentResultComponent } from './ai-grader-student-result.component';

describe('AiGraderStudentResultComponent', () => {
  let component: AiGraderStudentResultComponent;
  let fixture: ComponentFixture<AiGraderStudentResultComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AiGraderStudentResultComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AiGraderStudentResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
