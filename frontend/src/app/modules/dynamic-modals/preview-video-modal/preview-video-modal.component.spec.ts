import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PreviewVideoModalComponent } from './preview-video-modal.component';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('PreviewVideoModalComponent', () => {
  let component: PreviewVideoModalComponent;
  let fixture: ComponentFixture<PreviewVideoModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PreviewVideoModalComponent ],
      schemas : [NO_ERRORS_SCHEMA , CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PreviewVideoModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
