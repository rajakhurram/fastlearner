import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AiGraderStudentComponent } from './ai-grader-student.component';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { SharedModule } from '../../shared/shared.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { CacheService } from 'src/app/core/services/cache.service';
import { DatePipe } from '@angular/common';
import { NzEmptyModule } from 'ng-zorro-antd/empty';

describe('AiGraderStudentComponent', () => {
  let component: AiGraderStudentComponent;
  let fixture: ComponentFixture<AiGraderStudentComponent>;
  let mockAiGraderService: jasmine.SpyObj<AiGraderService>;

  beforeEach(async () => {
    mockAiGraderService = jasmine.createSpyObj<AiGraderService>('AiGraderService', [
      'getClassesStudent',
      'getAssessmentsByClassIdAndAssessmentId',
    ]);

    await TestBed.configureTestingModule({
      declarations: [ AiGraderStudentComponent ],
      imports: [
        SharedModule,
        BrowserAnimationsModule,
        NzEmptyModule,
      ],
      providers: [
        { provide: Router, useValue: {} },
        { provide: DatePipe, useValue: {} },
        { provide: ActivatedRoute, useValue: {} },
        { provide: CacheService, useValue: {} },
        { provide: AiGraderService, useValue: mockAiGraderService }
      ],
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
