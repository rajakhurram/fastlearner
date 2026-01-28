import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssignCourseAffliateComponent } from './assign-course-affliate.component';
import { AffiliateService } from 'src/app/core/services/affiliate.service';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzInputModule } from 'ng-zorro-antd/input';
import { SharedModule } from '../shared.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('AssignCourseAffliateComponent', () => {
  let component: AssignCourseAffliateComponent;
  let fixture: ComponentFixture<AssignCourseAffliateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AssignCourseAffliateComponent ],
      imports: [
        NzSelectModule,
        NzInputModule,
        SharedModule,
        BrowserAnimationsModule
      ],
      providers: [
        {provide: AffiliateService, useValue: {} },
        {provide: NzMessageService, useValue: {} },
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AssignCourseAffliateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
