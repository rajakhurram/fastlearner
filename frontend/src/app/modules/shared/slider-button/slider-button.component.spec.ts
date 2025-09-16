import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SliderButtonComponent } from './slider-button.component';

describe('SliderButtonComponent', () => {
  let component: SliderButtonComponent;
  let fixture: ComponentFixture<SliderButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SliderButtonComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SliderButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
