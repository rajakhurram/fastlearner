import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraderResultsComponent } from './grader-results.component';
import { NzModalService } from 'ng-zorro-antd/modal';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { ViewContainerRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { NzCardModule } from 'ng-zorro-antd/card';
import { SharedModule } from 'src/app/modules/shared/shared.module';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { of } from 'rxjs';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { FormsModule } from '@angular/forms';

describe('GraderResultsComponent', () => {
  let component: GraderResultsComponent;
  let fixture: ComponentFixture<GraderResultsComponent>;
  let mockActivatedRoute: jasmine.SpyObj<ActivatedRoute>;
  let mockAiGraderService: jasmine.SpyObj<AiGraderService>;
  let mockCacheService: jasmine.SpyObj<CacheService>;

  beforeEach(async () => {
    mockActivatedRoute = jasmine.createSpyObj<ActivatedRoute>('Router', [], {
      queryParams: of({
        params: '12',
      })
    });

    mockAiGraderService = jasmine.createSpyObj<AiGraderService>('AiGraderService', [
      'getAssessmentsDetails',
      'getNoOfPagesUsed',
      'getClasses'
    ]);

    mockAiGraderService.getNoOfPagesUsed.and.returnValue(of({data: {noOfPagesUsed: 10}}));

    mockCacheService = jasmine.createSpyObj<CacheService>('CacheService', [
      'getJsonData',
    ]);

    await TestBed.configureTestingModule({
      declarations: [ GraderResultsComponent ],
      providers: [
        { provide: NzModalService, useValue: {} },
        { provide: AiGraderService, useValue: mockAiGraderService },
        { provide: ViewContainerRef, useValue: {} },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: Router, useValue: {} },
        { provide: CacheService, useValue: mockCacheService },
        { provide: MessageService, useValue: {} },
      ],
      imports: [
        NzCardModule,
        SharedModule,
        NzTableModule,
        NzPaginationModule,
        NzProgressModule,
        NzToolTipModule,
        FormsModule,
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GraderResultsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
