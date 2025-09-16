import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraderUploaderComponent } from './grader-uploader.component';

describe('GraderUploaderComponent', () => {
  let component: GraderUploaderComponent;
  let fixture: ComponentFixture<GraderUploaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GraderUploaderComponent ]
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
