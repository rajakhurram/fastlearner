import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PreviewUploadComponent } from './preview-upload.component';

describe('PreviewUploadComponent', () => {
  let component: PreviewUploadComponent;
  let fixture: ComponentFixture<PreviewUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PreviewUploadComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PreviewUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
