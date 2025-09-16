import { Component, Input } from '@angular/core';
import { AngularEditorConfig } from '@kolkov/angular-editor';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CourseType } from 'src/app/core/enums/course-status';
import { CourseService } from 'src/app/core/services/course.service';
import { NzIconService } from 'ng-zorro-antd/icon';
import { ChangeDetectorRef } from '@angular/core';
import { TestInformationDropdownsService } from 'src/app/core/services/test-information-dropdowns.service';
import { TestCenterConst } from 'src/app/core/constants/testCenter.constants';

@Component({
  selector: 'app-test-information',
  templateUrl: './test-information.component.html',
  styleUrls: ['./test-information.component.scss'],
})
export class TestInformationComponent {
  _httpConstants: HttpConstants = new HttpConstants();
  isTypeValid: boolean = true;
  isTypeTouched: boolean = false;
  isCategoryValid: boolean = true;
  isCategoryTouched: boolean = false;
  isTitleTouched = false;
  isTitleDuplicate = false;
  titleMinLength = false;
  isLevelTouched = false;
  isHeadlineTouched = false;
  isDescriptionTouched = false;
  isHashtagsTouched = false;
  isPrerequisiteTouched = false;
  isLearningsTouched = false;
  isThumbnailTouched = false;
  isUrlTouched = false;
  urlError = '';
  editorConfig: AngularEditorConfig;
  imageSrc: string;
  videoSrc: string;
  readonly baseURL = 'https://staging.fastlearner.ai/student/course-details/';
  testCenterConsts = new TestCenterConst();
  constructor(
    private iconService: NzIconService,
    private cdr: ChangeDetectorRef,
    private _dropdownDataService: TestInformationDropdownsService,
    private _courseService: CourseService
  ) {
    this.iconService.fetchFromIconfont({
      scriptUrl: 'https://example.com/iconfont.js',
    });
    this.editorConfig = this.testCenterConsts.editorConfig;
    ({
      previewSrcs: { imageSrc: this.imageSrc, videoSrc: this.videoSrc },
    } = this.testCenterConsts);
  }

  @Input() videoUrl: string | null = null;

  // videoSrc = '../../../../../assets/images/add_video.svg';
  // imageSrc = '../../../../assets/images/add_image.svg';

  inputFields: any = {
    types: [
      {
        name: 'STANDARD_COURSE',
        value: 'Standard',
        locked: false,
      },
      {
        name: 'PREMIUM_COURSE',
        value: 'Premium',
        locked: true,
      },
    ],
    selectedType: null,
    testSubscriptionType: CourseType,
    categories: [],
    selectedCategory: null,
    title: null,
    levels: [],
    selectedLevel: null,
    url: null,
    headline: null,
    description: null,
    hashTags: [],
    hashTagValue: null,
    prerequisite: null,
    learnings: [
      {
        id: 0,
        text: null,
      },
    ],
    increment: 1,
    videoFileName: null,
    videoFile: null,
    videoPreview: null,
    uploadedVideoUrl: null,
    videoUploaded: false,
    thumbnailFileName: null,
    thumbnailFile: null,
    thumbnailPath: null,
    showThumbnail: false,
  };

  validationErrors: { [key: string]: string | null | boolean } = {
    title: false,
    url: null,
  };

  ngOnInit() {
    this._dropdownDataService.getDropdownData().subscribe({
      next: (data) => {
        if (
          data?.categories?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE &&
          data?.levels?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        )
          this.inputFields.categories = data?.categories?.data;
        this.inputFields.levels = data?.levels?.data;
        console.log(data?.levels?.data);
      },
      error: (error) => {
        console.error('Error:', error.message);
      },
    });
  }

  validateType() {
    this.isTypeValid = !!this.inputFields.selectedType;
  }

  markTypeAsTouched() {
    this.isTypeTouched = true;
  }

  validateCategory() {
    this.isCategoryValid = !!this.inputFields.selectedCategory;
  }

  markCategoryAsTouched() {
    this.isCategoryTouched = true;
  }

  // HANDLE VALIDATION
  handleValidation(field: string, error: any | null) {
    // console.log(this.inputFields.title);
    // this.validationErrors[field] = error;
  }

  inputHandler(
    field: string,
    value: any | null = null,
    isUnique: boolean = false,
    minLength: any = 10,
    method: string = ''
  ) {
    this.inputFields[field] = value;

    if (isUnique) {
      this.uniqueValueHandler(field, minLength, value, 0, method);
    }
  }

  uniqueValueHandler(
    field: string = '',
    minLength: any = 10,
    value: any | null = '',
    id: number = 0,
    method: string
  ) {
    if (value.length >= minLength) {
      this._courseService[method](value, id).subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.validationErrors[field] = false;
            if (field.trim() === 'title') {
              const { data = '' } = response || {};
              this.inputFields.url = data;
            }
          }
        },
        error: (error: any) => {
          this.validationErrors[field] = true;
        },
      });
    }
  }

  fileUploadHandler(event: any, type: string) {
    const { name = '', url = '' } = event || {};

    if (type === 'video' && name) {
      this.inputFields.videoFileName = name;
      this.inputFields.uploadedVideoUrl = url;
      this.inputFields.videoUploaded = true;
    } else {
      this.inputFields.thumbnailPath = url;
      this.inputFields.showThumbnail = true;
    }
  }

  markLevelAsTouched() {
    this.isLevelTouched = this.inputFields.selectedLevel == null;
  }

  validateLevel() {
    this.isLevelTouched = false;
  }

  markDescriptionAsTouched() {
    this.isDescriptionTouched = this.inputFields.description == null;
  }

  validateDescription() {
    this.isDescriptionTouched = false;
  }

  handleInputEnter(): void {
    const newTag = this.inputFields.hashTagValue?.trim();
    this.isHashtagsTouched = false;
    if (newTag && !this.inputFields.hashTags.includes(newTag)) {
      this.inputFields.hashTags = [...this.inputFields.hashTags, newTag];
      this.inputFields.hashTagValue = '';
    }
    this.cdr.detectChanges();
  }

  removeTag(tag: string): void {
    this.inputFields.hashTags = this.inputFields.hashTags.filter(
      (t) => t !== tag
    );
  }

  markHashtagsAsTouched() {
    this.isHashtagsTouched = this.inputFields.hashTagValue == null;
  }

  markLearningsAsTouched() {
    this.isLearningsTouched = this.inputFields.learnings[0].text == null;
  }

  validateLearnings() {
    this.isLearningsTouched = false;
  }

  addLearning() {
    const id = this.inputFields.increment++;

    this.inputFields.learnings.push({
      id,
      text: null,
    });
  }

  deleteLearning(index) {
    const filteredList = this.inputFields.learnings.filter(
      ({ id = 0 }) => id !== index
    );
    // Modify id in sequence..
    const inSequenceList = filteredList.map((learning, i) => ({
      ...learning,
      id: i,
    }));
    this.inputFields.learnings = [...inSequenceList];
  }

  get currentSelectedTopic() {
    return {
      videoUrl: this.inputFields.uploadedVideoUrl,
      vttContent: this.inputFields.videoFileName,
    };
  }
}
