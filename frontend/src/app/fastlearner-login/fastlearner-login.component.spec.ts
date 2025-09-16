import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FastlearnerLoginComponent } from './fastlearner-login.component';

describe('FastlearnerLoginComponent', () => {
  let component: FastlearnerLoginComponent;
  let fixture: ComponentFixture<FastlearnerLoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FastlearnerLoginComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FastlearnerLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
