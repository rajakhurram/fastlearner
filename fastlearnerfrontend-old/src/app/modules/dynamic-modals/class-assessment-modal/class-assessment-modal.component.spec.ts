import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClassAssessmentModalComponent } from './class-assessment-modal.component';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { FormsModule } from '@angular/forms';

describe('ClassAssessmentModalComponent', () => {
  let component: ClassAssessmentModalComponent;
  let fixture: ComponentFixture<ClassAssessmentModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClassAssessmentModalComponent ],
      imports: [
        FormsModule
      ],
      providers: [
        {provide: NzModalRef, useValue: {}},
        {provide: AiGraderService, useValue: {}},
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClassAssessmentModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
