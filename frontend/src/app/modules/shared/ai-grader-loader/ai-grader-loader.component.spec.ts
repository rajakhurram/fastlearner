import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AiGraderLoaderComponent } from './ai-grader-loader.component';

describe('AiGraderLoaderComponent', () => {
  let component: AiGraderLoaderComponent;
  let fixture: ComponentFixture<AiGraderLoaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AiGraderLoaderComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AiGraderLoaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
