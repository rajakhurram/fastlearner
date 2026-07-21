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

  it('should render processing overlay with progress and spinner', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.processing-overlay')).toBeTruthy();
    expect(el.querySelector('.ai-grader-loader h3')?.textContent).toContain(
      'Processing Submissions',
    );
    expect(el.querySelector('.progress-bar')).toBeTruthy();
    expect(el.querySelector('.spinner')).toBeTruthy();
    expect(el.textContent).toContain('Please wait');
  });
});
