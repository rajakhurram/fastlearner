import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraderResultViewComponent } from './grader-result-view.component';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { Router } from '@angular/router';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { NzModalService } from 'ng-zorro-antd/modal';
import { ViewContainerRef } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { SharedModule } from 'src/app/modules/shared/shared.module';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { of } from 'rxjs';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { NZ_ICONS, NzIconModule } from 'ng-zorro-antd/icon';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { ArrowLeftOutline } from '@ant-design/icons-angular/icons';

describe('GraderResultViewComponent', () => {
  let component: GraderResultViewComponent;
  let fixture: ComponentFixture<GraderResultViewComponent>;
  let mockCacheService: jasmine.SpyObj<CacheService>;
  let mockAiGraderService: jasmine.SpyObj<AiGraderService>;

  beforeEach(async () => {
    mockCacheService = jasmine.createSpyObj<CacheService>('CacheService', [
      'getJsonData'
    ]);
    
    mockCacheService.getJsonData.and.returnValue({students: []});
    
    mockAiGraderService = jasmine.createSpyObj<AiGraderService>('AiGraderService', [
      'getNoOfPagesUsed'
    ]);

    mockAiGraderService.getNoOfPagesUsed.and.returnValue(of({data: {noOfPagesUsed: 10}}));

    await TestBed.configureTestingModule({
      declarations: [ GraderResultViewComponent ],
      providers: [
        {provide: AiGraderService, useValue: mockAiGraderService},
        {provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate'])},
        {provide: CacheService, useValue: mockCacheService},
        {provide: MessageService, useValue: jasmine.createSpyObj('MessageService', ['success', 'error'])},
        {provide: NzModalService, useValue: jasmine.createSpyObj('NzModalService', ['create', 'confirm'])},
        {provide: ViewContainerRef, useValue: {}},
        { provide: NZ_ICONS, useValue: [ArrowLeftOutline] }
      ],
      imports: [
        RouterTestingModule,
        SharedModule,
        NzGridModule,
        NzCardModule,
        NzProgressModule,
        NzIconModule,
        NzToolTipModule,
        NzEmptyModule
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GraderResultViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
