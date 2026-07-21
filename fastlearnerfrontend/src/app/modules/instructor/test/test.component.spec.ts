import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';
import { of } from 'rxjs';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { InstructorTabs } from 'src/app/core/enums/instructor_tabs';
import { TestComponent } from './test.component';

describe('TestComponent', () => {
  let component: TestComponent;
  let fixture: ComponentFixture<TestComponent>;
  let communicationService: jasmine.SpyObj<CommunicationService>;

  beforeEach(async () => {
    communicationService = jasmine.createSpyObj<CommunicationService>(
      'CommunicationService',
      ['instructorTabChange'],
    );

    await TestBed.configureTestingModule({
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      declarations: [TestComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({ id: '123' }),
          },
        },
        { provide: CommunicationService, useValue: communicationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should notify instructor tab change on construction', () => {
    expect(communicationService.instructorTabChange).toHaveBeenCalledWith(
      InstructorTabs.COURSE,
    );
  });

  it('should set courseId from query params on init', () => {
    component.ngOnInit();
    expect(component.courseId).toBe('123');
  });

  it('should update current step', () => {
    component.step(2);
    expect(component.currentStep).toBe(2);
  });

  it('should update sections data', () => {
    const sections = [{ name: 'Section 1' }];
    component.sectionData(sections);
    expect(component.sectionsData).toBe(sections);
  });

  it('should update course information form', () => {
    const form = new FormBuilder().group({
      title: ['Quiz', Validators.required],
    });
    component.courseInformation(form);
    expect(component.courseInformationData).toBe(form);
  });

  it('should update draft course id', () => {
    component.getDraftCourseId(456);
    expect(component.courseId).toBe('456');
  });

  it('should initialize wizard steps', () => {
    expect(component.stepOne).toBeTrue();
    expect(component.stepTwo).toBeFalse();
    expect(component.stepThree).toBeFalse();
    expect(component.currentStep).toBe(0);
  });
});
