import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PdfViewerComponent } from './pdf-viewer.component';
import { NzCardModule } from 'ng-zorro-antd/card';
import { PdfViewerModule } from 'ng2-pdf-viewer';

describe('PdfViewerComponent', () => {
  let component: PdfViewerComponent;
  let fixture: ComponentFixture<PdfViewerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PdfViewerComponent ],
      imports: [
        NzCardModule,
        PdfViewerModule,
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PdfViewerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
