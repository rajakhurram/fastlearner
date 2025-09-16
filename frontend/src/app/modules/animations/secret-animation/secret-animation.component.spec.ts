import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecretAnimationComponent } from './secret-animation.component';

describe('SecretAnimationComponent', () => {
  let component: SecretAnimationComponent;
  let fixture: ComponentFixture<SecretAnimationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SecretAnimationComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecretAnimationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
