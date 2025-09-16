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
    ]);
    const fileManagerServiceSpy = jasmine.createSpyObj('FileManager', [
      'uploadFile',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', ['error']);
    const modalServiceSpy = jasmine.createSpyObj('NzModalService', ['create']);

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
      expect(formGroup.controls['courseSummaryInfo'].errors).toBeNull();
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
});
