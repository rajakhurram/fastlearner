import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { CourseComponent } from './course.component';
import { RouterTestingModule } from '@angular/router/testing';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { of } from 'rxjs';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';

describe('CourseComponent', () => {
  let component: CourseComponent;
  let fixture: ComponentFixture<CourseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CourseComponent],
      imports: [
        RouterTestingModule,
        ReactiveFormsModule,
        BrowserAnimationsModule,
        AntDesignModule,
      ],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
      providers: [
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({ id: '123' }), // Mocking queryParams
          },
        },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CourseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set courseId from queryParams on ngOnInit', () => {
    component.ngOnInit();
    expect(component.courseId).toBe('123');
  });
  it('should update currentStep when step method is called', () => {
    component.step(2);
    expect(component.currentStep).toBe(2);
  });
  it('should update sectionsData when sectionData method is called', () => {
    const mockSectionData = [{ sectionName: 'Introduction' }];
    component.sectionData(mockSectionData);
    expect(component.sectionsData).toBe(mockSectionData);
  });
  it('should update courseInformationData when courseInformation method is called', () => {
    const mockFormGroup = new FormBuilder().group({
      title: ['Test Course', Validators.required],
    });
    component.courseInformation(mockFormGroup);
    expect(component.courseInformationData).toBe(mockFormGroup);
  });
  it('should update courseId when getDraftCourseId method is called', () => {
    component.getDraftCourseId('456');
    expect(component.courseId).toBe('456');
  });
  it('should have stepOne as true, stepTwo and stepThree as false on initialization', () => {
    expect(component.stepOne).toBeTrue();
    expect(component.stepTwo).toBeFalse();
    expect(component.stepThree).toBeFalse();
    expect(component.currentStep).toBe(0);
  });
  it('should subscribe to queryParams and set courseId on ngOnInit', () => {
    spyOn(component['_route'].queryParams, 'subscribe').and.callThrough();
    component.ngOnInit();
    expect(component['_route'].queryParams.subscribe).toHaveBeenCalled();
    expect(component.courseId).toBe('123');
  });

});
