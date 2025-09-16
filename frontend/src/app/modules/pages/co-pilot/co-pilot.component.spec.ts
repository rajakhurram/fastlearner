import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CoPilotComponent } from './co-pilot.component';

describe('CoPilotComponent', () => {
  let component: CoPilotComponent;
  let fixture: ComponentFixture<CoPilotComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CoPilotComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CoPilotComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
