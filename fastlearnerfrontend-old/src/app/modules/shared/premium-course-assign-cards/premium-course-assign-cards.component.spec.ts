import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PremiumCourseAssignCardsComponent } from './premium-course-assign-cards.component';

describe('PremiumCourseAssignCardsComponent', () => {
  let component: PremiumCourseAssignCardsComponent;
  let fixture: ComponentFixture<PremiumCourseAssignCardsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PremiumCourseAssignCardsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PremiumCourseAssignCardsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
