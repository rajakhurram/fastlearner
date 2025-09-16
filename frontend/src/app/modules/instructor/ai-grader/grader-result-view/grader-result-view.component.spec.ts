import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraderResultViewComponent } from './grader-result-view.component';

describe('GraderResultViewComponent', () => {
  let component: GraderResultViewComponent;
  let fixture: ComponentFixture<GraderResultViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GraderResultViewComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GraderResultViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
