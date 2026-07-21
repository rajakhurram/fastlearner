import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraderClassesComponent } from './grader-classes.component';
import { NzModalService } from 'ng-zorro-antd/modal';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { SharedModule } from 'src/app/modules/shared/shared.module';
import { of } from 'rxjs';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('GraderClassesComponent', () => {
  let component: GraderClassesComponent;
  let fixture: ComponentFixture<GraderClassesComponent>;

  beforeEach(async () => {
    const aiGraderService = jasmine.createSpyObj<AiGraderService>('AiGraderService', [
      'getClasses',
      'getNoOfPagesUsed',
    ])
    const cacheService = jasmine.createSpyObj<CacheService>('CacheService', [
      'getJsonData'
    ])

    aiGraderService.getNoOfPagesUsed.and.returnValue(of())

    await TestBed.configureTestingModule({
      declarations: [ GraderClassesComponent ],
      imports: [
        SharedModule,
        NzCardModule,
        NzProgressModule,
        NzToolTipModule,
        NzEmptyModule,
        BrowserAnimationsModule,
      ],
      providers: [
        { provide: NzModalService, useValue: {} },
        { provide: AiGraderService, useValue: aiGraderService },
        { provide: CacheService, useValue: cacheService },
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GraderClassesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
