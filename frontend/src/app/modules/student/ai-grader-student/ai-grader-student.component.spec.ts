import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AiGraderStudentComponent } from './ai-grader-student.component';

describe('AiGraderStudentComponent', () => {
  let component: AiGraderStudentComponent;
  let fixture: ComponentFixture<AiGraderStudentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AiGraderStudentComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AiGraderStudentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
