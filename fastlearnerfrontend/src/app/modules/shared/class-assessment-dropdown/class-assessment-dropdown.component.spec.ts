import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClassAssessmentDropdownComponent } from './class-assessment-dropdown.component';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { SharedModule } from '../shared.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('ClassAssessmentDropdownComponent', () => {
  let component: ClassAssessmentDropdownComponent;
  let fixture: ComponentFixture<ClassAssessmentDropdownComponent>;
  let aiGraderServiceSpy: jasmine.SpyObj<AiGraderService>;

  beforeEach(async () => {
    aiGraderServiceSpy = jasmine.createSpyObj<AiGraderService>('AiGraderService', [
      'getClasses',
    ])
    await TestBed.configureTestingModule({
      declarations: [ ClassAssessmentDropdownComponent ],
      imports: [
        SharedModule,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: AiGraderService, useValue: aiGraderServiceSpy }
      ]
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
