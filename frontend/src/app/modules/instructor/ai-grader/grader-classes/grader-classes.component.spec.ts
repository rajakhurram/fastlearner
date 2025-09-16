import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraderClassesComponent } from './grader-classes.component';

describe('GraderClassesComponent', () => {
  let component: GraderClassesComponent;
  let fixture: ComponentFixture<GraderClassesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GraderClassesComponent ]
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
