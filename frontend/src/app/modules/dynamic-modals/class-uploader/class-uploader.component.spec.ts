import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClassUploaderComponent } from './class-uploader.component';

describe('ClassUploaderComponent', () => {
  let component: ClassUploaderComponent;
  let fixture: ComponentFixture<ClassUploaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClassUploaderComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClassUploaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
