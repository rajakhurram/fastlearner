import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportPreviewModalComponent } from './report-preview-modal-component.component';
import { NzModalModule, NzModalRef } from 'ng-zorro-antd/modal';

describe('ReportPreviewModalComponentComponent', () => {
  let component: ReportPreviewModalComponent;
  let fixture: ComponentFixture<ReportPreviewModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReportPreviewModalComponent ],
      providers: [
        {provide: NzModalRef, useValue: {}}
      ],
      imports: [
        NzModalModule,
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReportPreviewModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
