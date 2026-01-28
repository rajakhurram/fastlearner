import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudentNotificationsComponent } from './student-notifications.component';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('StudentNotificationsComponent', () => {
  let component: StudentNotificationsComponent;
  let fixture: ComponentFixture<StudentNotificationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StudentNotificationsComponent ],
      schemas : [CUSTOM_ELEMENTS_SCHEMA , NO_ERRORS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StudentNotificationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
