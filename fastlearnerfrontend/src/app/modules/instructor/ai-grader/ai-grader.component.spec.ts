import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AiGraderComponent } from './ai-grader.component';

describe('AiGraderComponent', () => {
  let component: AiGraderComponent;
  let fixture: ComponentFixture<AiGraderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AiGraderComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AiGraderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
