import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FastlearnerSigninComponent } from './fastlearner-signin.component';

describe('FastlearnerSigninComponent', () => {
  let component: FastlearnerSigninComponent;
  let fixture: ComponentFixture<FastlearnerSigninComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FastlearnerSigninComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FastlearnerSigninComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
