import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraderAssessmentComponent } from './grader-assessment.component';
import { NzModalService } from 'ng-zorro-antd/modal';
import { ViewContainerRef } from '@angular/core';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { Router } from '@angular/router';
import { CacheService } from 'src/app/core/services/cache.service';
import { SharedModule } from 'src/app/modules/shared/shared.module';
import { of } from 'rxjs';
import { NzCardModule } from 'ng-zorro-antd/card';
import { FormsModule } from '@angular/forms';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { NzInputModule } from 'ng-zorro-antd/input';
import { CommonModule } from '@angular/common';
import { AiGraderRoutingModule } from '../ai-grader-routing.module';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { PdfViewerModule } from 'ng2-pdf-viewer';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NZ_ICONS, NzIconModule } from 'ng-zorro-antd/icon';

import { IconService } from '@ant-design/icons-angular'; // The service used to register icons
import { DeleteFill } from '@ant-design/icons-angular/icons'; // The specific icon class

const icons = [DeleteFill];

describe('GraderAssessmentComponent', () => {
  let component: GraderAssessmentComponent;
  let fixture: ComponentFixture<GraderAssessmentComponent>;
  let mockAiGraderService: jasmine.SpyObj<AiGraderService>;
  let mockCacheService: jasmine.SpyObj<CacheService>;

  beforeEach(async () => {
    mockAiGraderService = jasmine.createSpyObj<AiGraderService>('AiGraderService', [
      'getAssessmentsDetails',
      'getNoOfPagesUsed',
      'getClasses'
    ]);
    
    mockCacheService = jasmine.createSpyObj<CacheService>('CacheService', [
      'getJsonData',
    ]);

    mockAiGraderService.getNoOfPagesUsed.and.returnValue(of({data: {noOfPagesUsed: 10}}));

    await TestBed.configureTestingModule({
      declarations: [ GraderAssessmentComponent ],
      imports: [
        SharedModule,
        NzCardModule,
        NzToolTipModule,
        CommonModule,
        AiGraderRoutingModule,
        AntDesignModule,
        SharedModule,
        FormsModule,
        PdfViewerModule,
        NzInputModule,
        NzProgressModule,
        NoopAnimationsModule,
        NzIconModule
      ],
      providers: [
        { provide: NzModalService, useValue: {} },
        { provide: ViewContainerRef, useValue: {} },
        { provide: AiGraderService, useValue: mockAiGraderService },
        { provide: Router, useValue: {} },
        { provide: CacheService, useValue: mockCacheService },
        { provide: IconService, useClass: IconService },
        { provide: NZ_ICONS, useValue: icons },
      ]
    })
    .compileComponents();

    // 💡 THE FIX: Register the icon after compilation 💡
    const iconService = TestBed.inject(IconService); 
    iconService.addIcon(DeleteFill); // Register the DeleteFill icon

    fixture = TestBed.createComponent(GraderAssessmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call getNoOfPagesUsed on initialization', () => {
    // Assert that the spy was called after fixture.detectChanges() ran (which triggers ngOnInit)
    expect(mockAiGraderService.getNoOfPagesUsed).toHaveBeenCalled();
  });
});
