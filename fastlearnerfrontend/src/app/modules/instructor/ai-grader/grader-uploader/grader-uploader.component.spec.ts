import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraderUploaderComponent } from './grader-uploader.component';
import { TestRoutingModule } from '../../test/test-routing.module';
import { SharedModule } from 'src/app/modules/shared/shared.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NzModalService } from 'ng-zorro-antd/modal';
import { ViewContainerRef } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { NzCardModule } from 'ng-zorro-antd/card';
import { of } from 'rxjs';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { FormsModule } from '@angular/forms';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

describe('GraderUploaderComponent', () => {
  let component: GraderUploaderComponent;
  let fixture: ComponentFixture<GraderUploaderComponent>;
  let mockAiGraderService: jasmine.SpyObj<AiGraderService>;
  let mockCacheService: jasmine.SpyObj<CacheService>;

  beforeEach(async () => {
    mockAiGraderService = jasmine.createSpyObj<AiGraderService>('AiGraderService', [
      'getClasses',
      'getNoOfPagesUsed'
    ]);

    mockCacheService = jasmine.createSpyObj<CacheService>('CacheService', [
      'getJsonData',
    ]);

    mockAiGraderService.getClasses.and.returnValue(of({
      data: {aiClasses: []}
    }))
    
    mockAiGraderService.getNoOfPagesUsed.and.returnValue(of({
      data: {noOfPagesUsed: 0}
    }))

    await TestBed.configureTestingModule({
      declarations: [ GraderUploaderComponent ],
      imports: [
        TestRoutingModule,
        SharedModule,
        BrowserAnimationsModule,
        NzCardModule,
        NzProgressModule,
        NzSelectModule,
        NzGridModule,
        NzToolTipModule,
        FormsModule
      ],
      providers: [
        { provide: NzModalService, useValue: {} },
        { provide: AiGraderService, useValue: mockAiGraderService },
        { provide: Router, useValue: {} },
        { provide: MessageService, useValue: {} },
        { provide: DomSanitizer, useValue: {} },
        { provide: NgxUiLoaderService, useValue: {} },
        { provide: ViewContainerRef, useValue: {} },
        { provide: CacheService, useValue: mockCacheService },
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GraderUploaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
