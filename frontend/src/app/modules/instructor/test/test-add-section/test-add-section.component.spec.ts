import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TestAddSectionComponent } from './test-add-section.component';

describe('TestAddSectionComponent', () => {
  let component: TestAddSectionComponent;
  let fixture: ComponentFixture<TestAddSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TestAddSectionComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TestAddSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
