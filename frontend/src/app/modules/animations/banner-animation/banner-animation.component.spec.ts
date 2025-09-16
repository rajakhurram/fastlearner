import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BannerAnimationComponent } from './banner-animation.component';

describe('BannerAnimationComponent', () => {
  let component: BannerAnimationComponent;
  let fixture: ComponentFixture<BannerAnimationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BannerAnimationComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BannerAnimationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
