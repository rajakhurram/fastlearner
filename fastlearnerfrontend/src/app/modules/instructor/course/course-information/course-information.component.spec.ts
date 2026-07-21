import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import {
  ReactiveFormsModule,
  FormsModule,
  FormArray,
  FormControl,
  FormGroup,
} from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NzModalService } from 'ng-zorro-antd/modal';
import { of, throwError } from 'rxjs';
import { CourseInformationComponent } from './course-information.component';
import { CourseService } from 'src/app/core/services/course.service';
import { FileManager } from 'src/app/core/services/file-manager.service';
import { MessageService } from 'src/app/core/services/message.service';
import { CreateCourse } from 'src/app/core/models/create-course.model';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { NzUploadXHRArgs } from 'ng-zorro-antd/upload';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { AngularEditorModule } from '@kolkov/angular-editor';
import { CourseContentType } from 'src/app/core/enums/course-content-type.enum';
import { CourseStatus, CourseType } from 'src/app/core/enums/course-status';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { Router } from '@angular/router';

describe('CourseInformationComponent', () => {
  let component: CourseInformationComponent;
  let fixture: ComponentFixture<CourseInformationComponent>;
  let courseService: jasmine.SpyObj<CourseService>;
  let fileManagerService: jasmine.SpyObj<FileManager>;
  let messageService: jasmine.SpyObj<MessageService>;
  let modalService: jasmine.SpyObj<NzModalService>;

  beforeEach(async () => {
    const courseServiceSpy = jasmine.createSpyObj('CourseService', [
      'getCourseCategory',
      'getCourseLevels',
      'createCourseDto',
      'courseTitleExist',
      'courseUrlExist',
      'premiumCourseAvailable',
      'getCourseFirstStepDetail',
    ]);
    courseServiceSpy.premiumCourseAvailable.and.returnValue(of({
      status: 200,
      data: {isAvailablePremium: false}
    }))
    const fileManagerServiceSpy = jasmine.createSpyObj('FileManager', [
      'uploadFile',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', ['error']);
    const modalServiceSpy = jasmine.createSpyObj('NzModalService', ['create']);
    const communicationServiceSpy = jasmine.createSpyObj(
      'CommunicationService',
      ['updateInstructorCourse'],
    );
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [CourseInformationComponent],
      imports: [
        ReactiveFormsModule,
        FormsModule,
        HttpClientTestingModule,
        BrowserAnimationsModule,
        AntDesignModule,
        AngularEditorModule,
      ],
      providers: [
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: FileManager, useValue: fileManagerServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: NzModalService, useValue: modalServiceSpy },
        { provide: CommunicationService, useValue: communicationServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    courseService = TestBed.inject(
      CourseService
    ) as jasmine.SpyObj<CourseService>;
    fileManagerService = TestBed.inject(
      FileManager
    ) as jasmine.SpyObj<FileManager>;
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
    modalService = TestBed.inject(
      NzModalService
    ) as jasmine.SpyObj<NzModalService>;

    fixture = TestBed.createComponent(CourseInformationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form group', () => {
    expect(component.formGroup).toBeDefined();
    expect(component.formGroup.get('courseTitle')).toBeTruthy();
    expect(component.formGroup.get('courseCategory')).toBeTruthy();
  });

  it('should call getCategoryList on ngOnInit', () => {
    const getCategoryListSpy = spyOn(
      component,
      'getCategoryList'
    ).and.callThrough();
    component.ngOnInit();
    expect(getCategoryListSpy).toHaveBeenCalled();
  });

  describe('getCategoryList', () => {
    it('should call getCourseLevels on getCategoryList success', () => {
      courseService.getCourseCategory.and.returnValue(
        of({ status: 200, data: [] })
      );
      const getCourseLevelsSpy = spyOn(
        component,
        'getCourseLevels'
      ).and.callThrough();
      component.getCategoryList();
      expect(getCourseLevelsSpy).toHaveBeenCalled();
    });
  });

  describe('getCourseLevels', () => {
    it('should handle getCourseLevels error', () => {
      courseService.getCourseCategory.and.returnValue(
        of({ status: 200, data: [] })
      );
      courseService.getCourseLevels.and.returnValue(
        throwError(() => new Error('Error'))
      );
      component.getCourseLevels();
      expect(component.listOfLevel).toEqual([]);
    });
  });

  it('should add course tag', () => {
    component.formGroup.get('tags')?.setValue('NewTag');
    component.handleInputConfirm();
    expect(component.courseTagArray.length).toBeGreaterThan(0);
    expect(component.tags).toContain('NewTag');
  });

  it('should remove course tag', () => {
    component.addCourseTag({ id: 1, name: 'TagToRemove', active: true });
    component.removeCourseTag(0);
    expect(component.courseTagArray.length).toBe(0);
  });

  it('should add course summary', () => {
    component.addCourseSummary('New Summary');
    expect(component.courseSummaryArray.length).toBeGreaterThan(0);
  });

  it('should remove course summary', () => {
    component.addCourseSummary('Summary to Remove');
    component.removeSummary(0);
    expect(component.courseSummaryArray.length).toBe(1);
  });

  it('should handle showVideo file upload', () => {
    const file = new File([''], 'video.mp4', { type: 'video/mp4' });
    const event = { target: { files: [file] } };
    fileManagerService.uploadFile.and.returnValue(
      of({ status: 201, data: { fileUrl: 'video-url' } })
    );
    component.showVideo(event);
    expect(component.formGroup.get('previewPath')?.value).toBe('video-url');
  });

  it('should handle showImage file upload', () => {
    const file = new File([''], 'image.jpg', { type: 'image/jpeg' });
    const event = { target: { files: [file] } };
    fileManagerService.uploadFile.and.returnValue(
      of({ status: 201, data: 'image-url' })
    );
    component.showImage(event);
    expect(component.formGroup.get('thumbnailPath')?.value).toBe('image-url');
  });

  it('should validate form fields', () => {
    component.formGroup.get('courseTitle')?.setValue('Course Title');
    expect(component.anyFieldValid()).toBeTrue();
  });

  it('should handle courseTitleExist check', () => {
    const title = 'Existing Title';
    courseService.courseTitleExist.and.returnValue(of({ status: 200 }));
    component.formGroup.get('courseTitle')?.setValue(title);
    component.courseTitleExist();
    expect(component.formGroup.get('titleExist')?.value).toBeFalse();
  });

  it('should publish course', () => {
    component.publishCourse();
    expect(component.courseSaved).toBeFalse();
  });

  it('should handle YouTube URL check', () => {
    component.formGroup
      .get('previewName')
      ?.setValue('https://www.youtube.com/watch?v=dQw4w9WgXcQ');
    expect(component.checkYoutubeLink()).toBeTrue();
  });

  it('should patch form group with new values', () => {
    const mockData = {
      courseTitle: 'Updated Course Title',
      courseCategory: 'Updated Category',
      courseSummary: 'Updated Summary',
      courseTags: ['tag1', 'tag2'],
    };
    component.formGroup.patchValue(mockData);
    fixture.detectChanges();

    expect(component.formGroup.get('courseTitle').value).toBe(
      mockData.courseTitle
    );
    expect(component.formGroup.get('courseCategory').value).toBe(
      mockData.courseCategory
    );
  });

  describe('ngOnDestroy', () => {
    it('should call saveAsDraftCourse on component destroy', () => {
      const saveAsDraftCourseSpy = spyOn(component, 'saveAsDraftCourse');
      component.ngOnDestroy();
      expect(saveAsDraftCourseSpy).toHaveBeenCalled();
    });
  });

  describe('handleBeforeUnload', () => {
    it('should prevent default action on beforeunload event', () => {
      const event = new Event('beforeunload');
      const preventDefaultSpy = spyOn(event, 'preventDefault');
      component.handleBeforeUnload(event);
      expect(preventDefaultSpy).toHaveBeenCalled();
    });
  });

  describe('checkTooltipVisibility', () => {
    it('should calculate tooltip visibility based on video player element position', () => {
      component.videoPlayerElement = {
        media: {
          nativeElement: {
            getBoundingClientRect: () => ({
              top: 100,
              bottom: 400,
              height: 300,
            }),
          },
        },
      };
      window.innerHeight = 600;
      component.checkTooltipVisibility();
      expect(component.isTooltipVisible).toBeTrue();
    });
  });

  it('should mark form as invalid if required fields are missing', () => {
    component.formGroup.get('courseTitle')?.setValue('');
    component.formGroup.get('courseCategory')?.setValue(null);
    expect(component.formGroup.invalid).toBeTrue();
  });

  describe('checkYoutubeLink', () => {
    it('should correctly detect a valid YouTube URL', () => {
      component.formGroup
        .get('previewName')
        .setValue('https://www.youtube.com/watch?v=example');
      expect(component.checkYoutubeLink()).toBeTrue();
    });

    it('should correctly detect an invalid YouTube URL', () => {
      component.formGroup.get('previewName').setValue('https://example.com');
      expect(component.checkYoutubeLink()).toBeFalse();
    });

    it('should correctly detect an empty previewName', () => {
      component.formGroup.get('previewName').setValue('');
      expect(component.checkYoutubeLink()).toBeFalse();
    });
  });

  describe('showVideo', () => {
    it('should not show video preview if no file is selected', () => {
      const event = { target: { files: [] } };
      component.showVideo(event);
      expect(component.showPreview).toBeFalse();
    });
  });

  describe('publishCourse', () => {
    it('should not publish course if form is invalid', () => {
      // Ensure form is invalid by not setting required fields
      component.formGroup.get('courseTitle')?.setValue('');
      component.formGroup.get('courseCategory')?.setValue(null);

      component.publishCourse();

      expect(component.courseSaved).toBeFalse();
      expect(component.formGroup.invalid).toBeTrue();
    });

    it('should handle error during course publishing', () => {
      courseService.createCourseDto.and.returnValue(
        throwError({ status: 500, message: 'Server error' })
      );

      component.publishCourse();
      fixture.detectChanges();

      expect(component.courseSaved).toBeFalse();
    });
  });

  describe('handleInputConfirm', () => {
    it('should hide input and clear inputValue when tag input is canceled', () => {
      component.handleInputConfirm();
      expect(component.inputVisible).toBeFalse();
      expect(component.inputValue).toBe('');
    });
  });

  describe('processInput', () => {
    it('should replace multiple spaces with a single space', () => {
      const input = 'This   is  a   test';
      const result = component.processInput(input);
      expect(result).toBe('This is a test');
    });

    it('should remove hyphens from the input string', () => {
      const input = 'This-is-a-test';
      const result = component.processInput(input);
      expect(result).toBeDefined();
    });

    it('should handle empty input', () => {
      const input = '';
      const result = component.processInput(input);
      expect(result).toBe('');
    });

    it('should handle input with no spaces or hyphens', () => {
      const input = 'TestString';
      const result = component.processInput(input);
      expect(result).toBe('TestString');
    });
  });

  describe('preventSpecialChar', () => {
    it('should allow alphanumeric and specific special characters', () => {});

    it('should prevent characters not allowed by the regex', () => {});

    it('should allow spaces', () => {});
  });

  describe('addYoutubeUrl', () => {
    it('should set previewPath to the value of previewName', () => {
      component.formGroup
        .get('previewName')
        .setValue('https://www.youtube.com/watch?v=example');
      component.addYoutubeUrl();
      expect(component.formGroup.get('previewPath').value).toBe(
        'https://www.youtube.com/watch?v=example'
      );
    });

    it('should update currentSelectedTopic with the videoUrl', () => {
      const mockTopic = { id: 1, name: 'Topic 1', videoUrl: '' };
      component.currentSelectedTopic = mockTopic;
      component.formGroup
        .get('previewName')
        .setValue('https://www.youtube.com/watch?v=example');
      component.addYoutubeUrl();
      expect(component.currentSelectedTopic.videoUrl).toBe(
        'https://www.youtube.com/watch?v=example'
      );
    });

    it('should set showPreview to true', () => {
      component.addYoutubeUrl();
      expect(component.showPreview).toBeTrue();
    });
  });

  describe('youtubeInputChange', () => {
    it('should set videoFileBtn to "Upload File" and isYoutubeLinkPresent to false if previewName is empty', () => {
      component.formGroup.get('previewName').setValue('');
      component.youtubeInputChange();
      expect(component.videoFileBtn).toBe('Upload File');
      expect(component.isYoutubeLinkPresent).toBeFalse();
    });

    it('should set videoFileBtn to "Add" and isYoutubeLinkPresent to true if previewName contains a valid YouTube URL', () => {
      component.formGroup
        .get('previewName')
        .setValue('https://www.youtube.com/watch?v=example');
      spyOn(component, 'checkYoutubeLink').and.returnValue(true);
      component.youtubeInputChange();
      expect(component.videoFileBtn).toBe('Add');
      expect(component.isYoutubeLinkPresent).toBeTrue();
    });

    it('should call checkYoutubeLink when previewName is not empty', () => {
      spyOn(component, 'checkYoutubeLink');
      component.formGroup
        .get('previewName')
        .setValue('https://www.youtube.com/watch?v=example');
      component.youtubeInputChange();
      expect(component.checkYoutubeLink).toHaveBeenCalled();
    });
  });

  describe('createTag', () => {
    it('should create a FormGroup with tag values', () => {
      // Arrange
      const mockTag = { id: 1, name: 'TagName', active: true };

      // Act
      const formGroup = component.createTag(mockTag);

      // Assert
      expect(formGroup.value).toEqual(mockTag);
      expect(formGroup.controls['name'].errors).toBeNull();
    });

    it('should create a FormGroup with default values when no tag is provided', () => {
      // Act
      const formGroup = component.createTag();

      // Assert
      expect(formGroup.value).toEqual({ id: null, name: null, active: null });
    });
  });

  describe('addCourseTag', () => {
    it('should add a new tag to the tagsArray FormArray', () => {
      // Arrange
      const initialLength = component.courseTagArray.length;
      const mockTag = { id: 1, name: 'NewTag', active: true };

      // Act
      component.addCourseTag(mockTag);

      // Assert
      expect(component.courseTagArray.length).toBe(initialLength + 1);
      expect(component.courseTagArray.at(initialLength).value).toEqual(mockTag);
    });
  });
  describe('removeCourseTag', () => {
    it('should remove a tag from the tagsArray FormArray at the specified index', () => {
      // Arrange
      component.addCourseTag({ id: 1, name: 'Tag1', active: true });
      component.addCourseTag({ id: 2, name: 'Tag2', active: true });
      const initialLength = component.courseTagArray.length;

      // Act
      component.removeCourseTag(0);

      // Assert
      expect(component.courseTagArray.length).toBe(initialLength - 1);
      expect(component.courseTagArray.at(0).value).toEqual({
        id: 2,
        name: 'Tag2',
        active: true,
      });
    });
  });
  describe('createCourseSummary', () => {
    it('should create a FormGroup with summary values', () => {
      // Arrange
      const mockSummary = 'Course Summary';

      // Act
      const formGroup = component.createCourseSummary(mockSummary);

      // Assert
      expect(formGroup.value).toEqual({ courseSummaryInfo: mockSummary });
      expect(formGroup.controls['courseSummaryInfo'].errors).toBeNull();
    });

    it('should create a FormGroup with default values when no summary is provided', () => {
      // Act
      const formGroup = component.createCourseSummary();
      // Assert
      expect(formGroup.value).toEqual({ courseSummaryInfo: '' });
      expect(formGroup.controls['courseSummaryInfo'].errors).toBeTruthy();
    });
  });
  describe('showInput', () => {
    it('should set inputVisible to true and focus the input element', fakeAsync(() => {}));
  });
  describe('sliceTagName', () => {
    it('should return a truncated tag with "..." if the tag is longer than 20 characters', () => {
      // Arrange
      const longTag = 'ThisIsAVeryLongTagNameThatNeedsTruncation';

      // Act
      const result = component.sliceTagName(longTag);

      // Assert
      expect(result).toBe('ThisIsAVeryLongTagNa...');
    });

    it('should return the tag as is if it is 20 characters or less', () => {
      // Arrange
      const shortTag = 'ShortTagName';

      // Act
      const result = component.sliceTagName(shortTag);

      // Assert
      expect(result).toBe(shortTag);
    });
  });
  describe('removeSummary', () => {
    it('should remove a summary from the courseSummaries FormArray at the specified index', () => {
      // Arrange
      component.addCourseSummary('Summary 1');
      component.addCourseSummary('Summary 2');
      const initialLength = component.courseSummaryArray.length;

      // Act
      component.removeSummary(0);

      // Assert
      expect(component.courseSummaryArray.length).toBe(initialLength - 1);
      expect(component.courseSummaryArray.at(0).value).toEqual({
        courseSummaryInfo: 'Summary 1',
      });
    });

    it('should not remove summary if there is only one item in the array', () => {
      // Arrange
      component.addCourseSummary('Summary 1');

      // Act
      component.removeSummary(0);

      // Assert
      expect(component.courseSummaryArray.length).toBe(1); // Ensure length is not reduced below 1
    });
  });

  describe('addCourseSummary', () => {
    it('should add a new summary to the courseSummaries FormArray', () => {
      // Arrange
      const initialLength = component.courseSummaryArray.length;
      const mockSummary = 'New Summary';

      // Act
      component.addCourseSummary(mockSummary);

      // Assert
      expect(component.courseSummaryArray.length).toBe(initialLength + 1);
      expect(component.courseSummaryArray.at(initialLength).value).toEqual({
        courseSummaryInfo: mockSummary,
      });
    });
  });
  describe('manageCourseSummaryArrayLength', () => {
    it('should set courseSummaryArrayLength to 0 if all summaries are empty', () => {
      // Arrange
      component.courseSummaryArray.clear();
      component.addCourseSummary(''); // Add an empty summary
      component.addCourseSummary(''); // Add another empty summary

      // Act
      component.manageCourseSummaryArrayLength();

      // Assert
      expect(component.courseSummaryArrayLength).toBe(0);
    });

    it('should set courseSummaryArrayLength to the number of non-empty summaries', () => {
      // Arrange
      component.courseSummaryArray.clear();
      component.addCourseSummary('Summary 1'); // Non-empty summary
      component.addCourseSummary(''); // Empty summary
      component.addCourseSummary('Summary 2'); // Non-empty summary

      // Act
      component.manageCourseSummaryArrayLength();

      // Assert
      expect(component.courseSummaryArrayLength).toBe(2);
    });

    it('should correctly count non-empty summaries when there are mixed empty and non-empty summaries', () => {
      // Arrange
      component.courseSummaryArray.clear();
      component.addCourseSummary('Summary A'); // Non-empty summary
      component.addCourseSummary(''); // Empty summary
      component.addCourseSummary('Summary B'); // Non-empty summary
      component.addCourseSummary(''); // Empty summary
      component.addCourseSummary('Summary C'); // Non-empty summary

      // Act
      component.manageCourseSummaryArrayLength();

      // Assert
      expect(component.courseSummaryArrayLength).toBe(3);
    });

    it('should handle empty courseSummaryArray without errors', () => {
      // Arrange
      component.courseSummaryArray.clear();

      // Act
      component.manageCourseSummaryArrayLength();

      // Assert
      expect(component.courseSummaryArrayLength).toBe(0);
    });

    it('should correctly update courseSummaryArrayLength when summaries are added and removed', () => {
      // Arrange
      component.courseSummaryArray.clear();
      component.addCourseSummary('Summary 1'); // Non-empty summary
      component.addCourseSummary('Summary 2'); // Non-empty summary
      component.manageCourseSummaryArrayLength();

      // Act
      component.removeSummary(0); // Remove one summary
      component.manageCourseSummaryArrayLength();

      // Assert
      expect(component.courseSummaryArrayLength).toBe(1);
    });
  });
  describe('anyFieldValid', () => {
    it('should return true if courseTitle is not empty and titleExist is false', () => {
      component.formGroup.get('courseTitle').setValue('Valid Title');
      component.formGroup.get('titleExist').setValue(false);
      const result = component.anyFieldValid();
      expect(result).toBeTrue();
    });

    it('should return true if description is not empty', () => {
      component.formGroup.get('description').setValue('Valid Description');
      const result = component.anyFieldValid();
      expect(result).toBeTrue();
    });

    it('should return true if courseCategory has an id', () => {
      component.formGroup.get('courseCategory').setValue({ id: 1 });
      const result = component.anyFieldValid();
      expect(result).toBeTrue();
    });

    it('should return true if courseLevel has an id', () => {
      component.formGroup.get('courseLevel').setValue({ id: 1 });
      const result = component.anyFieldValid();
      expect(result).toBeTrue();
    });

    it('should return true if courseHeadline is not empty', () => {
      component.formGroup.get('courseHeadline').setValue('Valid Headline');
      const result = component.anyFieldValid();
      expect(result).toBeTrue();
    });

    it('should return true if thumbnailPath is not empty', () => {
      component.formGroup.get('thumbnailPath').setValue('thumbnail-url');
      const result = component.anyFieldValid();
      expect(result).toBeTrue();
    });

    it('should return true if previewPath is not empty', () => {
      component.formGroup.get('previewPath').setValue('preview-url');
      const result = component.anyFieldValid();
      expect(result).toBeTrue();
    });

    it('should return true if courseSummaries has at least one non-empty summary', () => {
      component.formGroup
        .get('courseSummaries')
        .setValue([{ courseSummaryInfo: 'Summary' }]);
      const result = component.anyFieldValid();
      expect(result).toBeTrue();
    });

    it('should return true if prerequisite is not empty', () => {
      component.formGroup.get('prerequisite').setValue('Prerequisite');
      const result = component.anyFieldValid();
      expect(result).toBeTrue();
    });
  });

  describe('courseTitleExist', () => {
    let courseServiceSpy: jasmine.SpyObj<CourseService>;

    beforeEach(() => {
      courseServiceSpy = TestBed.inject(
        CourseService
      ) as jasmine.SpyObj<CourseService>;
    });

    it('should process and trim course title before checking existence', () => {
      const courseTitle = '  Title with spaces  ';
      const processedTitle = 'Title with spaces';
      spyOn(component, 'processInput').and.returnValue(processedTitle);
      courseServiceSpy.courseTitleExist.and.returnValue(of({ status: 200 }));

      component.formGroup.get('courseTitle').setValue(courseTitle);
      component.courseTitleExist();

      expect(component.formGroup.get('courseTitle').value).toBe(processedTitle);
      expect(component.processInput).toHaveBeenCalledWith(courseTitle);
    });

    it('should set titleExist to false if the title exists in the courseService response', () => {
      const title = 'Existing Title';
      courseServiceSpy.courseTitleExist.and.returnValue(of({ status: 200 }));

      component.formGroup.get('courseTitle').setValue(title);
      component.courseTitleExist();

      expect(component.formGroup.get('titleExist').value).toBeFalse();
    });

    it('should set titleExist to true if the title does not exist in the courseService response', () => {
      const title = 'Non-existing Title';
      courseServiceSpy.courseTitleExist.and.returnValue(
        throwError(() => new Error('Not Found'))
      );

      component.formGroup.get('courseTitle').setValue(title);
      component.courseTitleExist();

      expect(component.formGroup.get('titleExist').value).toBeTrue();
    });

    it('should handle errors during course title existence check', () => {
      const title = 'Error Title';
      courseServiceSpy.courseTitleExist.and.returnValue(
        throwError(() => new Error('Error'))
      );

      component.formGroup.get('courseTitle').setValue(title);
      component.courseTitleExist();

      expect(component.formGroup.get('titleExist').value).toBeTrue();
    });
  });

  describe('Phase 1 batch 1: publish flow and validation helpers', () => {
    const successCode = 200;

    beforeEach(() => {
      courseService.createCourseDto.and.returnValue(
        of({ status: successCode, data: { courseId: 'new-course' } }),
      );
    });

    it('should require preview video for course content type', () => {
      component.selectedContentType = CourseContentType.COURSE;
      component.handleConditionalValidation();

      expect(
        component.formGroup.get('previewPath')?.hasError('required'),
      ).toBeTrue();
    });

    it('should clear preview validators for non-course content type', () => {
      component.selectedContentType = CourseContentType.TEST;
      component.handleConditionalValidation();

      expect(
        component.formGroup.get('previewPath')?.hasError('required'),
      ).toBeFalse();
    });

    it('should reject non-positive premium prices', () => {
      const validator = component.priceGreaterThanZeroValidator();
      expect(validator({ value: 0 } as any)).toEqual({
        priceGreaterThanZero: true,
      });
      expect(validator({ value: 10 } as any)).toBeNull();
    });

    it('should sanitize course title input', () => {
      expect(component.sanitizeCourseTitle('Hello@World!')).toBe('HelloWorld');
      expect(component.processInput('  Trim  Me  ')).toBe(' Trim Me ');
    });

    it('should block invalid characters in course title keydown', () => {
      const event = {
        key: '@',
        ctrlKey: false,
        metaKey: false,
        altKey: false,
        preventDefault: jasmine.createSpy('preventDefault'),
      } as any;

      component.preventCourseTitleKeydown(event);

      expect(event.preventDefault).toHaveBeenCalled();
      expect(component.courseTitleFormatError).toBeTrue();
    });

    it('should call publishCourse from steps when form is ready', () => {
      const publishSpy = spyOn(component, 'publishCourse');
      spyOnProperty(component.formGroup, 'valid', 'get').and.returnValue(true);
      component.courseSummaryArrayLength = 1;
      component.addCourseTag({ id: 1, name: 'tag', active: true });

      component.steps('section-step');

      expect(publishSpy).toHaveBeenCalledWith('section-step');
    });

    it('should publish course and emit outputs on success', () => {
      component.courseSaved = false;
      component.sectionsData = [];
      component.formGroup.get('courseProgress')?.setValue(0);
      spyOn(component.currentStep, 'emit');
      spyOn(component.courseInformationData, 'emit');
      spyOn(component.draftCourseId, 'emit');
      component.formGroup.get('description')?.setValue('Has content');

      component.publishCourse('next');

      expect(courseService.createCourseDto).toHaveBeenCalled();
      expect(component.courseId).toBe('new-course');
      expect(component.currentStep.emit).toHaveBeenCalledWith('next');
      expect(component.courseInformationData.emit).toHaveBeenCalled();
      expect(component.draftCourseId.emit).toHaveBeenCalled();
    });

    it('should short-circuit publish when course progress is 100', () => {
      component.formGroup.get('courseProgress')?.setValue(100);
      spyOn(component.currentStep, 'emit');

      component.publishCourse('pricing');

      expect(courseService.createCourseDto).not.toHaveBeenCalled();
      expect(component.currentStep.emit).toHaveBeenCalledWith('pricing');
    });

    it('should save draft course when fields are valid', () => {
      component.courseSaved = false;
      component.sectionsData = [{ name: 'Section' }];
      component.formGroup.get('courseProgress')?.setValue(0);
      component.formGroup.get('description')?.setValue('Draft body');

      component.saveAsDraftCourse();

      expect(courseService.createCourseDto).toHaveBeenCalled();
      expect(component.courseId).toBe('new-course');
      expect(component.courseSaved).toBeTrue();
    });

    it('should count non-empty course summaries', () => {
      component.courseSummaryArray.at(0).get('courseSummaryInfo')?.setValue('');
      component.addCourseSummary('Summary one');
      component.addCourseSummary('');

      component.manageCourseSummaryArrayLength();

      expect(component.courseSummaryArrayLength).toBe(1);
    });
  });

  describe('Phase 1 batch 1: youtube, uploads, and UI helpers', () => {
    it('should add youtube url to preview path', () => {
      component.formGroup.get('previewName')?.setValue('https://youtu.be/abc');
      component.addYoutubeUrl();

      expect(component.formGroup.get('previewPath')?.value).toBe(
        'https://youtu.be/abc',
      );
      expect(component.showPreview).toBeTrue();
    });

    it('should reset youtube input state when cleared', () => {
      component.formGroup.get('previewName')?.setValue('');
      component.videoFileBtn = 'Replace';
      component.isYoutubeLinkPresent = true;

      component.youtubeInputChange();

      expect(component.videoFileBtn).toBe('Upload File');
      expect(component.isYoutubeLinkPresent).toBeFalse();
    });

    it('should enable add button for valid youtube links', () => {
      component.formGroup
        .get('previewName')
        ?.setValue('https://www.youtube.com/watch?v=abc');

      component.youtubeInputChange();

      expect(component.videoFileBtn).toBe('Add');
      expect(component.isYoutubeLinkPresent).toBeTrue();
    });

    it('should copy course url to clipboard', fakeAsync(() => {
      component.applicationCourseDetailsUrl = 'https://learn.test/course/';
      const input = document.createElement('input');
      input.id = 'url-input';
      input.value = 'my-course';
      document.body.appendChild(input);
      spyOn(document, 'execCommand').and.returnValue(true);
      component.formGroup.get('courseUrl')?.setValue('my-course');

      component.copyUrl();
      tick(1000);

      expect(document.execCommand).toHaveBeenCalledWith('copy');
      expect(component.copyTooltipText).toBe('Click to copy URL');
      document.body.removeChild(input);
    }));

    it('should filter free and all course types from dropdown', () => {
      component.courseTypes = [
        { name: 'Free', value: 'FREE' },
        { name: 'Premium', value: 'PREMIUM' },
        { name: 'All', value: 'ALL' },
      ] as any;

      expect(component.filteredCourseTypes.length).toBe(1);
      expect(component.filteredCourseTypes[0].name).toBe('Premium');
    });

    it('should lock course type for published premium courses', () => {
      component.pricingLocked = true;
      component.courseStatus = CourseStatus.PUBLISHED;

      expect(component.isCourseTypeLocked).toBeTrue();

      component.courseStatus = CourseStatus.DRAFT;
      expect(component.isCourseTypeLocked).toBeFalse();
    });

    it('should open subscription modal when premium is unavailable', () => {
      component.isAvailablePremium = false;
      modalService.create.and.returnValue({ afterClose: of(null) } as any);

      component.openSubscriptionPlan({
        name: 'Premium',
        value: CourseType.PREMIUM,
        disabled: false,
      });

      expect(modalService.create).toHaveBeenCalled();
    });

    it('should reject invalid preview video uploads', () => {
      component.customRequestVideo({
        file: new File(['v'], 'bad#name.mp4', { type: 'video/mp4' }),
      } as any);

      expect(messageService.error).toHaveBeenCalledWith(
        'File name contains special characters.',
      );
    });

    it('should reject non-mp4 preview video uploads', () => {
      component.customRequestVideo({
        file: new File(['v'], 'clip.avi', { type: 'video/avi' }),
      } as any);

      expect(messageService.error).toHaveBeenCalledWith(
        'Please upload a video file in MP4 format.',
      );
    });

    it('should reject invalid thumbnail uploads', () => {
      component.customRequestImage({
        file: new File(['x'], 'file.txt', { type: 'text/plain' }),
      } as any);

      expect(messageService.error).toHaveBeenCalledWith(
        'Please select a valid image file (jpg, jpeg, gif, png).',
      );
    });

    it('should show validation errors when validateAndContinue is invalid', () => {
      spyOn(component as any, 'showValidationErrors');
      spyOn(component as any, 'scrollToFirstInvalidField');
      spyOn(component, 'steps');
      component.formGroup.get('courseTitle')?.setValue('');

      component.validateAndContinue();

      expect((component as any).showValidationErrors).toHaveBeenCalled();
      expect((component as any).scrollToFirstInvalidField).toHaveBeenCalled();
      expect(component.steps).not.toHaveBeenCalled();
    });

    it('should prevent emoji input', () => {
      const event = {
        key: '😀',
        preventDefault: jasmine.createSpy('preventDefault'),
      } as any;

      component.preventEmoji(event);

      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('should slice long tag names for display', () => {
      expect(component.sliceTagName('Short')).toBe('Short');
      expect(component.sliceTagName('VeryLongTagNameExample')).toContain('...');
    });
  });

  describe('Phase 1 batch 2: form patching and media helpers', () => {
    const successCode = 200;

    beforeEach(() => {
      courseService.getCourseCategory.and.returnValue(
        of({
          status: successCode,
          data: [{ id: 1, name: 'Science' }],
        }),
      );
      courseService.getCourseLevels.and.returnValue(
        of({
          status: successCode,
          data: [{ id: 2, name: 'Beginner' }],
        }),
      );
      courseService.courseTitleExist.and.returnValue(of({ status: successCode }));
      courseService.courseUrlExist.and.returnValue(of({ status: successCode }));
      component.categoryList = [{ id: 1, name: 'Science' }] as any;
      component.listOfLevel = [{ id: 2, name: 'Beginner' }] as any;
      component.currentSelectedTopic = { videoUrl: '', vttContent: '' };
    });

    it('should patch form from course detail payload', () => {
      component.patchForm({
        title: 'My Course',
        courseUrl: 'my-course',
        categoryId: 1,
        courseType: CourseType.FREE,
        about: 'About',
        price: 0,
        levelId: 2,
        courseDescription: 'Description',
        courseThumbnailUrl: 'thumb.png',
        previewVideoUrl: 'video.mp4',
        previewVideoVttContent: 'vtt',
        prerequisite: ['None'],
        courseProgress: 10,
        certificateEnabled: true,
        tags: [{ id: 1, name: 'tag1', active: true }],
        courseOutcome: ['Outcome 1'],
      });

      expect(component.formGroup.get('courseTitle')?.value).toBe('My Course');
      expect(component.showPreview).toBeTrue();
      expect(component.showThumbnail).toBeTrue();
    });

    it('should detect youtube links', () => {
      component.formGroup.get('previewName')?.setValue(
        'https://www.youtube.com/watch?v=abc123',
      );

      expect(component.checkYoutubeLink()).toBeTrue();
      expect(component.youtubeInputChange()).toBeUndefined();
      expect(component.isYoutubeLinkPresent).toBeTrue();
    });

    it('should apply youtube url to preview path', () => {
      component.formGroup.get('previewName')?.setValue(
        'https://www.youtube.com/watch?v=abc123',
      );

      component.addYoutubeUrl();

      expect(component.formGroup.get('previewPath')?.value).toContain('youtube');
      expect(component.showPreview).toBeTrue();
    });

    it('should copy course url to clipboard', () => {
      const input = document.createElement('input');
      input.id = 'url-input';
      input.value = 'my-course';
      document.body.appendChild(input);
      component.applicationCourseDetailsUrl = 'https://fastlearner.ai/course/';
      const execSpy = spyOn(document, 'execCommand').and.returnValue(true);

      component.copyUrl();

      expect(execSpy).toHaveBeenCalledWith('copy');
      document.body.removeChild(input);
    });

    it('should filter course types for instructor selection', () => {
      expect(
        component.filteredCourseTypes.every(
          (ct) => ct.name !== 'Free' && ct.name !== 'All',
        ),
      ).toBeTrue();
    });

    it('should lock course type for published premium courses', () => {
      component.formGroup.get('courseType')?.setValue(CourseType.PREMIUM);
      component.courseStatus = CourseStatus.PUBLISHED;
      component.pricingLocked = true;

      expect(component.isCourseTypeLocked).toBeTrue();
    });

    it('should save draft and keep courseSaved flag on success', () => {
      courseService.createCourseDto.and.returnValue(
        of({ status: successCode, data: { courseId: 'draft-1' } }),
      );
      component.formGroup.get('description')?.setValue('Draft content');

      component.saveAsDraftCourse();

      expect(courseService.createCourseDto).toHaveBeenCalled();
      expect(component.courseSaved).toBeTrue();
    });

    it('should block special characters in course url keydown', () => {
      const event = {
        key: '@',
        preventDefault: jasmine.createSpy('preventDefault'),
      } as any;

      component.preventSpecialCharUrl(event);

      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('should load course levels into list', () => {
      component.getCourseLevels();

      expect(component.listOfLevel.length).toBe(1);
      expect(component.listOfLevel[0].name).toBe('Beginner');
    });

    it('should remove course summary row', () => {
      component.addCourseSummary('Summary line');
      expect(component.courseSummaryArray.length).toBeGreaterThan(1);

      component.removeSummary(1);

      expect(component.courseSummaryArray.length).toBe(1);
    });
  });

  describe('Phase 1 batch 3: course detail, url, tags, and validation', () => {
    const successCode = 200;
    let router: jasmine.SpyObj<Router>;

    const baseCourseDetail = () => ({
      title: 'Loaded Course',
      courseUrl: 'loaded-course',
      categoryId: 1,
      courseType: CourseType.FREE,
      about: 'About loaded',
      price: 0,
      levelId: 2,
      courseDescription: 'Description loaded',
      courseThumbnailUrl: 'thumb.png',
      previewVideoUrl: 'video.mp4',
      previewVideoVttContent: 'vtt',
      prerequisite: ['None'],
      courseProgress: 25,
      certificateEnabled: false,
      courseStatus: CourseStatus.DRAFT,
      contentType: 'COURSE',
      tags: [{ id: 1, name: 'tag1', active: true }],
      courseOutcome: ['Outcome 1'],
    });

    beforeEach(() => {
      router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
      courseService.getCourseCategory.and.returnValue(
        of({ status: successCode, data: [{ id: 1, name: 'Science' }] }),
      );
      courseService.getCourseLevels.and.returnValue(
        of({ status: successCode, data: [{ id: 2, name: 'Beginner' }] }),
      );
      courseService.courseTitleExist.and.returnValue(of({ status: successCode }));
      courseService.courseUrlExist.and.returnValue(of({ status: successCode }));
      courseService.getCourseFirstStepDetail.and.returnValue(
        of({ status: successCode, data: baseCourseDetail() }),
      );
      component.categoryList = [{ id: 1, name: 'Science' }] as any;
      component.listOfLevel = [{ id: 2, name: 'Beginner' }] as any;
      component.currentSelectedTopic = { videoUrl: '', vttContent: '' };
      component.courseId = 'course-1';
    });

    it('should block editing published premium courses', () => {
      courseService.getCourseFirstStepDetail.and.returnValue(
        of({
          status: successCode,
          data: {
            ...baseCourseDetail(),
            courseType: CourseType.PREMIUM,
            courseStatus: CourseStatus.PUBLISHED,
          },
        }),
      );

      component.getCourseFirstStepDetail();

      expect(messageService.error).toHaveBeenCalledWith(
        'Premium courses cannot be edited.',
      );
      expect(router.navigate).toHaveBeenCalledWith(['instructor/dashboard']);
    });

    it('should load course detail and patch form on success', () => {
      const patchSpy = spyOn(component, 'patchForm');
      const validationSpy = spyOn(component, 'handleConditionalValidation');

      component.getCourseFirstStepDetail();

      expect(courseService.getCourseFirstStepDetail).toHaveBeenCalledWith(
        'course-1',
      );
      expect(component.selectedContentType).toBe('course');
      expect(validationSpy).toHaveBeenCalled();
      expect(patchSpy).toHaveBeenCalled();
    });

    it('should allow draft premium courses to load', () => {
      courseService.getCourseFirstStepDetail.and.returnValue(
        of({
          status: successCode,
          data: {
            ...baseCourseDetail(),
            courseType: CourseType.PREMIUM,
            courseStatus: CourseStatus.DRAFT,
          },
        }),
      );
      const patchSpy = spyOn(component, 'patchForm');

      component.getCourseFirstStepDetail();

      expect(messageService.error).not.toHaveBeenCalled();
      expect(router.navigate).not.toHaveBeenCalled();
      expect(patchSpy).toHaveBeenCalled();
    });

    it('should fetch first step detail when patchFormGroup has courseId', () => {
      const detailSpy = spyOn(component, 'getCourseFirstStepDetail');

      component.patchFormGroup();

      expect(detailSpy).toHaveBeenCalled();
    });

    it('should map premium and non-premium types in patchForm', () => {
      component.patchForm({
        ...baseCourseDetail(),
        courseType: CourseType.PREMIUM,
      });
      expect(component.formGroup.get('courseType')?.value).toBe(
        CourseType.PREMIUM,
      );

      component.courseTagArray.clear();
      component.courseSummaryArray.clear();
      component.addCourseSummary('');
      component.patchForm({
        ...baseCourseDetail(),
        courseType: CourseType.FREE,
      });
      expect(component.formGroup.get('courseType')?.value).toBe(
        CourseType.STANDARD,
      );
    });

    it('should mark course url as available when service succeeds', () => {
      component.formGroup.get('courseUrl')?.setValue('my-course-url');

      component.courseUrlExist();

      expect(courseService.courseUrlExist).toHaveBeenCalledWith(
        'my-course-url',
        'course-1',
      );
      expect(component.formGroup.get('urlExist')?.value).toBeFalse();
    });

    it('should mark course url as taken when service errors', () => {
      courseService.courseUrlExist.and.returnValue(
        throwError(() => new Error('Exists')),
      );
      component.formGroup.get('courseUrl')?.setValue('taken-url');

      component.courseUrlExist();

      expect(component.formGroup.get('urlExist')?.value).toBeTrue();
    });

    it('should skip course url check when url is empty', () => {
      component.formGroup.get('courseUrl')?.setValue('');

      component.courseUrlExist();

      expect(courseService.courseUrlExist).not.toHaveBeenCalled();
    });

    it('should block invalid characters in course url keydown', () => {
      const blocked = {
        key: '@',
        preventDefault: jasmine.createSpy('preventDefault'),
      } as any;
      const allowed = {
        key: 'a',
        preventDefault: jasmine.createSpy('preventDefault'),
      } as any;

      component.preventSpecialCharUrl(blocked);
      component.preventSpecialCharUrl(allowed);

      expect(blocked.preventDefault).toHaveBeenCalled();
      expect(allowed.preventDefault).not.toHaveBeenCalled();
    });

    it('should open subscription modal when premium is unavailable', () => {
      component.isAvailablePremium = false;
      modalService.create.and.returnValue({ afterClose: of(null) } as any);

      component.openSubscriptionPlan({
        name: 'Premium',
        value: CourseType.PREMIUM,
        disabled: false,
      });

      expect(modalService.create).toHaveBeenCalled();
    });

    it('should not open subscription modal when premium is available', () => {
      component.isAvailablePremium = true;

      component.openSubscriptionPlan({
        name: 'Premium',
        value: CourseType.PREMIUM,
        disabled: false,
      });

      expect(modalService.create).not.toHaveBeenCalled();
    });

    it('should set premium availability from service response', () => {
      courseService.premiumCourseAvailable.and.returnValue(
        of({ status: successCode, data: { isAvailablePremium: true } }),
      );

      component.premiumCourseAvailable();

      expect(component.isAvailablePremium).toBeTrue();
    });

    it('should log content change events', () => {
      const logSpy = spyOn(console, 'log');
      const event = { html: '<p>updated</p>' };

      component.contentChanged(event);

      expect(logSpy).toHaveBeenCalledWith(event);
    });

    it('should recheck tooltip visibility on window scroll', () => {
      const checkSpy = spyOn(component, 'checkTooltipVisibility');

      component.onWindowScroll();

      expect(checkSpy).toHaveBeenCalled();
    });

    it('should show tag input and focus the element', fakeAsync(() => {
      component.inputElement = {
        nativeElement: { focus: jasmine.createSpy('focus') },
      } as any;

      component.showInput();
      tick(10);

      expect(component.inputVisible).toBeTrue();
      expect(component.inputElement?.nativeElement.focus).toHaveBeenCalled();
    }));

    it('should add a unique tag on input confirm', () => {
      component.formGroup.get('tags')?.setValue('UniqueTag');

      component.handleInputConfirm();

      expect(component.tags).toContain('UniqueTag');
      expect(component.courseTagArray.length).toBe(1);
      expect(component.inputVisible).toBeFalse();
    });

    it('should not duplicate tags on input confirm', () => {
      component.tags = ['ExistingTag'];
      component.formGroup.get('tags')?.setValue('ExistingTag');
      const initialLength = component.courseTagArray.length;

      component.handleInputConfirm();

      expect(component.courseTagArray.length).toBe(initialLength);
      expect(component.tags).toEqual(['ExistingTag']);
    });

    it('should keep at least one summary when removing', () => {
      component.courseSummaryArray.clear();
      component.addCourseSummary('Only summary');

      component.removeSummary(0);

      expect(component.courseSummaryArray.length).toBe(1);
    });

    it('should add summaries and update summary count', () => {
      component.courseSummaryArray.clear();
      component.addCourseSummary('Summary A');
      component.addCourseSummary('Summary B');

      expect(component.courseSummaryArray.length).toBe(2);
      expect(component.courseSummaryArrayLength).toBe(2);
    });

    it('should validate tag form group required name', () => {
      const invalidTag = component.createTag();
      const validTag = component.createTag({
        id: 1,
        name: 'Valid',
        active: true,
      });

      expect(invalidTag.get('name')?.valid).toBeFalse();
      expect(validTag.get('name')?.valid).toBeTrue();
    });

    it('should return false from anyFieldValid when form is empty', () => {
      component.formGroup.reset({
        courseTitle: '',
        titleExist: false,
        courseUrl: '',
        urlExist: false,
        description: '',
        courseCategory: null,
        courseType: null,
        courseLevel: null,
        courseHeadline: '',
        thumbnailPath: '',
        previewPath: '',
        prerequisite: '',
      });
      component.courseTagArray.clear();
      component.courseSummaryArray.at(0).get('courseSummaryInfo')?.setValue('');

      expect(component.anyFieldValid()).toBeFalse();
    });

    it('should return true from anyFieldValid for title and url', () => {
      component.formGroup.get('courseTitle')?.setValue('Valid Course Title');
      component.formGroup.get('titleExist')?.setValue(false);
      component.formGroup.get('courseUrl')?.setValue('valid-course-url');
      component.formGroup.get('urlExist')?.setValue(false);

      expect(component.anyFieldValid()).toBeTrue();
    });

    it('should reject title path when url already exists', () => {
      component.formGroup.get('courseTitle')?.setValue('Valid Course Title');
      component.formGroup.get('titleExist')?.setValue(false);
      component.formGroup.get('courseUrl')?.setValue('valid-course-url');
      component.formGroup.get('urlExist')?.setValue(true);

      expect(component.anyFieldValid()).toBeFalse();
    });

    it('should return true from anyFieldValid when tags are present', () => {
      component.addCourseTag({ id: 1, name: 'DraftTag', active: true });

      expect(component.anyFieldValid()).toBeTrue();
    });
  });
});
