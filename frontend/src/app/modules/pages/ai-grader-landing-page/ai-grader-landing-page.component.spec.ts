import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AiGraderLandingPageComponent } from './ai-grader-landing-page.component';

describe('AiGraderLandingPageComponent', () => {
  let component: AiGraderLandingPageComponent;
  let fixture: ComponentFixture<AiGraderLandingPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AiGraderLandingPageComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AiGraderLandingPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
