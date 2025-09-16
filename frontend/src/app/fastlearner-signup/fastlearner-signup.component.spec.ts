import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FastlearnerSignupComponent } from './fastlearner-signup.component';

describe('FastlearnerSignupComponent', () => {
  let component: FastlearnerSignupComponent;
  let fixture: ComponentFixture<FastlearnerSignupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FastlearnerSignupComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FastlearnerSignupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
