import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TestPreviewComponent } from './test-preview.component';
import { CourseService } from 'src/app/core/services/course.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { NzModalService } from 'ng-zorro-antd/modal';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { SharedModule } from 'src/app/modules/shared/shared.module';
import { FormArray, FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';

describe('TestPreviewComponent', () => {
  let component: TestPreviewComponent;
  let fixture: ComponentFixture<TestPreviewComponent>;

  beforeEach(async () => {
    const courseServiceSpy = jasmine.createSpyObj<CourseService>('CourseService', [
      'createCourseDto'
    ]);

    const cacheServiceSpy = jasmine.createSpyObj<CacheService>('CacheService', [
      'getJsonData',
      'saveJsonData'
    ]);

    const messageServiceSpy = jasmine.createSpyObj<MessageService>('MessageService', [
      'error',
      'info',
      'success'
    ]);

    const routerSpy = jasmine.createSpyObj<Router>('Router', [
      'navigate',
    ]);
    
    const activatedRouteSpy = {
      paramMap: of(convertToParamMap({ id: '123' })) // provide your test params here
    };
    
    const communicationServiceSpy = jasmine.createSpyObj<CommunicationService>('CommunicationService', 
      ['updateInstructorCourse'],
      { closeCompletionData$: of(null) }
    );
    
    await TestBed.configureTestingModule({
      declarations: [TestPreviewComponent],
      imports: [
        HttpClientTestingModule,
        SharedModule,
        FormsModule,
        ReactiveFormsModule,
        NzCheckboxModule
      ],
      providers: [
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: CacheService, useValue: cacheServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: CommunicationService, useValue: communicationServiceSpy },
        { provide: NzModalService, useValue: {} },
        { provide: ActivatedRoute, useValue: activatedRouteSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA] // Add this to ignore ng-zorro components
    })
      .compileComponents();

    fixture = TestBed.createComponent(TestPreviewComponent);
    component = fixture.componentInstance;
    component.course = { certificateEnabled: false };
    component.courseInformationData = new FormGroup({
      courseTitle: new FormControl('Test Course'),
      courseCategory: new FormGroup({ name: new FormControl('Category 1') }),
      courseHeadline: new FormControl('This is a headline'),
      courseLevel: new FormGroup({ name: new FormControl('Beginner') }),
      thumbnailPath: new FormControl('path/to/thumb.jpg'),
      prerequisite: new FormControl('Some prerequisite'),
      courseSummaries: new FormControl([{ courseSummaryInfo: 'Summary 1' }]),
      description: new FormControl('Course description'),
      previewPath: new FormControl('path/to/video.mp4'),
      previewVideoVttContent: new FormControl(null),
      certificateEnabled: new FormControl(false),
      tagsArray: new FormArray([
        new FormGroup({ name: new FormControl('Angular') }),
        new FormGroup({ name: new FormControl('Reactive Forms') }),
      ])
    });
    
    component.sectionsData = [];
    
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});