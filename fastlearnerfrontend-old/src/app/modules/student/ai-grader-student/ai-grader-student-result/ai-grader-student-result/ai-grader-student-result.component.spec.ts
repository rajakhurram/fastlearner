import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AiGraderStudentResultComponent } from './ai-grader-student-result.component';
import { SharedModule } from 'src/app/modules/shared/shared.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TestRoutingModule } from 'src/app/modules/instructor/test/test-routing.module';
import { ActivatedRoute, Router } from '@angular/router';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { Location } from '@angular/common';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzEmptyModule } from 'ng-zorro-antd/empty';

describe('AiGraderStudentResultComponent', () => {
  let component: AiGraderStudentResultComponent;
  let fixture: ComponentFixture<AiGraderStudentResultComponent>;
  let mockCacheService: jasmine.SpyObj<CacheService>;
  let mockAiGraderService: jasmine.SpyObj<AiGraderService>;

  beforeEach(async () => {
    mockCacheService = jasmine.createSpyObj<CacheService>('CacheService', ['getJsonData', 'getDataFromCache']);
    mockAiGraderService = jasmine.createSpyObj<AiGraderService>('AiGraderService', ['getResultByClassAndAssessmentId']);

    mockCacheService.getJsonData.and.returnValue({});
    mockCacheService.getDataFromCache.and.returnValue("{}");

    await TestBed.configureTestingModule({
      declarations: [AiGraderStudentResultComponent],
      imports: [
        TestRoutingModule,
        SharedModule,
        BrowserAnimationsModule,
        NzCardModule,
        NzEmptyModule
      ],
      providers: [
        { provide: ActivatedRoute, useValue: jasmine.createSpyObj<ActivatedRoute>('ActivatedRoute', [], ['snapshot']) },
        { provide: MessageService, useValue: jasmine.createSpyObj<MessageService>('MessageService', ['error', 'info', 'success']) },
        { provide: CacheService, useValue: mockCacheService },
        { provide: AiGraderService, useValue: mockAiGraderService },
        { provide: Router, useValue: {} },
        { provide: Location, useValue: {} },
      ]
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
