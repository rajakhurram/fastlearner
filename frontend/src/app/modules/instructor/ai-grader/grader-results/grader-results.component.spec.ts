import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraderResultsComponent } from './grader-results.component';

describe('GraderResultsComponent', () => {
  let component: GraderResultsComponent;
  let fixture: ComponentFixture<GraderResultsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GraderResultsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GraderResultsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
