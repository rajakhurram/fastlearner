import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { DomSanitizer } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { AngularEditorConfig } from '@kolkov/angular-editor';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzUploadXHRArgs } from 'ng-zorro-antd/upload';
import { Subscription } from 'rxjs';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CourseContentType } from 'src/app/core/enums/course-content-type.enum';
import { CourseType, courseTypeArray } from 'src/app/core/enums/course-status';
import { CreateCourse } from 'src/app/core/models/create-course.model';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { CourseService } from 'src/app/core/services/course.service';
import { FileManager } from 'src/app/core/services/file-manager.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SubscriptionPlanComponent } from 'src/app/modules/auth/subscription-plan/subscription-plan.component';
import { ImageUploaderModalComponent } from 'src/app/modules/dynamic-modals/image-uploader-modal/image-uploader-modal.component';
import { environment } from 'src/environments/environment.development';
@Component({
  selector: 'app-course-information',
  templateUrl: './course-information.component.html',
  styleUrls: ['./course-information.component.scss'],
})
export class CourseInformationComponent {
  _httpConstants: HttpConstants = new HttpConstants();
  formGroup: FormGroup;
  videoFileName?: string = '';
  videoFileBtn?: string = 'Upload File';

  imageFileName?: string = 'No choose file';
  imageFileBtn?: string = 'Upload File';
  imageSrc = '../../../../assets/images/add_image.svg';
  videoSrc = '../../../../../assets/images/add_video.svg';
  categoryList: Array<any> = [];
  listOfLevel: Array<any> = [];
  showPreview?: boolean = false; // default false;
  showThumbnail?: boolean = false; // default false;
  dataModel?: any;
  isYoutubeLinkPresent = false;
  currentSelectedTopic?: any = {};
  generatedUrl: string = '';
  minTitleLength = 10;
  tags = [];
  inputVisible = false;
  inputValue = '';
  @ViewChild('inputElement', { static: false }) inputElement?: ElementRef;
  @ViewChild('videoPlayerElement', { static: false }) videoPlayerElement: any;
  isTooltipVisible: boolean = true;
  @Output() currentStep = new EventEmitter<string>();
  @Output() courseInformationData = new EventEmitter<FormGroup>();
  @Output() draftCourseId = new EventEmitter<FormGroup>();
  @Input() formGroupData: FormGroup;
  @Input() courseId: any;
  @Input() selectedContentType: any;
  @Input() sectionsData: any;
  courseSummaryArrayLength?: any = 0;
  videoProgressBar?: any = false;
  imageProgressBar?: any = false;
  course?: CreateCourse = {};
  courseTypes = courseTypeArray;
  courseType = CourseType;
  urlTooltip = false;
  copyTooltipText = 'Click to copy URL';
  isAvailablePremium?: boolean;

  editorConfig: AngularEditorConfig = {
    editable: true,
    spellcheck: true,
    height: 'auto',
    minHeight: '150px',
    maxHeight: 'auto',
    width: 'auto',
    minWidth: '0',
    translate: 'yes',
    enableToolbar: true,
    showToolbar: true,
    placeholder: 'Enter text here...',
    defaultParagraphSeparator: '',
    defaultFontName: '',
    defaultFontSize: '',
    toolbarHiddenButtons: [
      [
        'strikeThrough',
        'subscript',
        'superscript',
        'justifyLeft',
        'justifyCenter',
        'justifyRight',
        'justifyFull',
        'indent',
        'outdent',
        'insertOrderedList',
        'insertUnorderedList',
        'heading',
        'fontSize',
        'textColor',
        'backgroundColor',
        'link',
        'unlink',
        'insertVideo',
        'insertHorizontalRule',
        'removeFormat',
        'toggleEditorMode',
        'undo',
        'redo',
        'fontName',
        'insertImage',
      ],
    ],
  };

  afterCloseSubscription: Subscription | undefined;
  subscription: Subscription;
  browserRefresh?: any;
  courseSaved?: any = false;
  applicationCourseDetailsUrl?: string;
  courseTypeSubscription: Subscription;
  courseContentType = CourseContentType

  constructor(
    public domSanitizer: DomSanitizer,
    private fb: FormBuilder,
    private _modal: NzModalService,
    private _messageService: MessageService,
    private _courseService: CourseService,
    private _fileManagerService: FileManager,
    private _viewContainerRef: ViewContainerRef,
    private _router: Router,
    private _communicationService: CommunicationService
  ) {
    this.applicationCourseDetailsUrl = environment.applicationCourseDetailsUrl;
  }

  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.checkTooltipVisibility();
  }

  ngOnDestroy() {
    this.saveAsDraftCourse();
  }

  @HostListener('window:beforeunload', ['$event'])
  handleBeforeUnload(event: Event): void {
    event.preventDefault();
  }
  ngAfterViewInit() {}

  ngOnInit(): void {
    this.formGroup = this.fb.group({
      courseTitle: [
        '',
        [
          Validators.required,
          this.trimmedTitleLengthValidator(10, 60)
        ],
      ],
      titleExist: [false],
      courseCategory: [null, Validators.required],
      courseHeadline: ['', [Validators.required, Validators.maxLength(300)]],
      price: [null],
      courseLevel: [null, Validators.required],
      courseType: [null, Validators.required],
      description: ['', [Validators.required, Validators.maxLength(3000)]],
      tags: [null],
      tagsArray: this.fb.array([]), // Use FormArray for tags
      prerequisite: ['', [Validators.required, Validators.maxLength(500)]],
      courseSummaries: this.fb.array([this.createCourseSummary()]),
      previewName: [''],
      thumbnailName: ['No choose file'],
      previewVideoVttContent: [null],
      previewPath: ['', Validators.required],
      thumbnailPath: ['', Validators.required],
      courseProgress: [null],
      certificateEnabled: [null],
      youtubeUrl: [''],
      courseUrl: [null, [Validators.required, Validators.minLength(10), Validators.maxLength(60)]],
      urlExist: [false],
    });

    this.handleConditionalValidation();  
    this.getCategoryList();
    this.premiumCourseAvailable();
  }

  handleConditionalValidation(): void {
      const previewPathControl = this.formGroup.get('previewPath');
  
      if (this.selectedContentType === CourseContentType.COURSE) {
        previewPathControl?.setValidators([Validators.required]);
      } else {
        previewPathControl?.clearValidators();
      }
  
      previewPathControl?.updateValueAndValidity();

    this.courseTypeSubscription = this.formGroup
    .get('courseType')
    .valueChanges.subscribe((newCourseType) => {
      const priceControl = this.formGroup.get('price');

      if (newCourseType === this.courseType?.PREMIUM) {
        // Apply the validator for Premium course type
        priceControl.setValidators([
          Validators.required,
          this.priceGreaterThanZeroValidator(),
        ]);
      } else {
        priceControl.clearValidators();
      }
      priceControl.updateValueAndValidity();
    });
  }

  onInput(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    const value = inputElement.value;
  
    // Allow only numbers with up to two decimal places
    if (!/^\d+(\.\d{0,2})?$/.test(value)) {
      // Revert to the valid portion of the value
      inputElement.value = value.substring(0, value.length - 1);
    }
  }

  priceGreaterThanZeroValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      return value > 0 ? null : { priceGreaterThanZero: true };
    };
  }

  checkTooltipVisibility() {
    const rect =
      this.videoPlayerElement?.media?.nativeElement.getBoundingClientRect();
    const tooltipHeight = rect?.height;

    // Calculate the visible height of the tooltip
    const visibleTop = Math.max(rect?.top, 0);
    const visibleBottom = Math.min(rect?.bottom, window.innerHeight);
    const visibleHeight = visibleBottom - visibleTop;

    // Check if at least half of the tooltip is visible
    this.isTooltipVisible = visibleHeight >= tooltipHeight / 2.3;
  }

  getCategoryList() {
    this._courseService.getCourseCategory()?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.categoryList = response?.data;
          if (this.formGroupData != null && this.formGroupData != undefined) {
          }
          this.getCourseLevels();
        }
      },
      error: (error: any) => {},
    });
  }

  contentChanged(event?: any) {
    console.log(event);
  }

  getCourseLevels() {
    this._courseService.getCourseLevels()?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.listOfLevel = response?.data;
          if (this.formGroupData != null && this.formGroupData != undefined) {
          }
          this.patchFormGroup();
        }
      },
      error: (error: any) => {},
    });
  }

  patchFormGroup() {
    if (this.formGroupData != null && this.formGroupData != undefined) {
      this.imageFileBtn = 'Replace';
      this.videoFileBtn = 'Replace';
      this.showPreview = true;
      this.showThumbnail = true;
      this.formGroup = this.formGroupData;
      this.currentSelectedTopic.videoUrl =
        this.formGroup.get('previewPath').value;
    }

    if (this.courseId) {
      this.getCourseFirstStepDetail();
    }
  }

  createTag(tag?: any): FormGroup {
    return this.fb.group({
      id: [tag?.id],
      name: [tag?.name, [Validators.required, Validators.maxLength(50)]],
      active: [tag?.active],
    });
  }

  get courseTagArray(): FormArray {
    return this.formGroup.get('tagsArray') as FormArray;
  }

  addCourseTag(tag?: any): void {
    this.courseTagArray.push(this.createTag(tag));
  }

  removeCourseTag(index?: any) {
    this.courseTagArray.removeAt(index);
  }

  createCourseSummary(summary?: any): FormGroup {
    return this.fb.group({
      courseSummaryInfo: [summary ? summary : '', [Validators.maxLength(500)]],
    });
  }

  get courseSummaryArray(): FormArray {
    return this.formGroup.get('courseSummaries') as FormArray;
  }

  addCourseSummary(summary?: any): void {
    this.courseSummaryArray.push(this.createCourseSummary(summary));
    this.manageCourseSummaryArrayLength();
  }

  removeSummary(index: number): void {
    if (this.courseSummaryArray.length > 1) {
      this.courseSummaryArray.removeAt(index);
    }
  }

  sliceTagName(tag: string): string {
    const isLongTag = tag.length > 20;
    return isLongTag ? `${tag.slice(0, 20)}...` : tag;
  }

  showInput(): void {
    this.inputVisible = true;
    setTimeout(() => {
      this.inputElement?.nativeElement.focus();
    }, 10);
  }

  handleInputConfirm(): void {
    const tag = this.formGroup.get('tags')?.value;
    if (tag && this.tags.indexOf(tag) === -1) {
      this.addCourseTag({
        id: null,
        name: tag,
        active: true,
      });
      this.tags = [...this.tags, tag];
    }
    this.formGroup.get('tags')?.patchValue('');
    this.inputVisible = false;
  }

  showVideo(event?: any, fileDisplayId?: string) {
    this.formGroup.get('previewPath')?.patchValue('');
    const input = event.target;
    this.showPreview = false;

    if (input.files.length > 0) {
      const file = input.files[0];
      const fileType = file.type.split('/')[1];

      if (!['avi', 'mov', 'mp4'].includes(fileType.toLowerCase())) {
        this._messageService.error(
          'Please select a valid video file (AVI, MOV, MP4).'
        );
        return;
      } else {
        this.formGroup.get('previewName')?.patchValue(file.name);
        this._fileManagerService.uploadFile(file, 'PREVIEW_VIDEO')?.subscribe({
          next: (response: any) => {
            if (
              response?.status ==
              this._httpConstants.REQUEST_STATUS.CREATED_201.CODE
            ) {
              this.showPreview = true;
              this.videoFileBtn = 'Replace';
              this.formGroup
                .get('previewPath')
                ?.patchValue(response.data?.fileUrl);
              this.formGroup
                .get('previewVideoVttContent')
                ?.patchValue(response.data?.previewVideoVttContent);
              this.showPreview = true;
              this.currentSelectedTopic = {
                ...this.currentSelectedTopic,
                videoUrl: response.data,
                vttContent: response?.data?.previewVideoVttContent,
              };
            }
          },
          error: (error: any) => {
            this.formGroup.get('previewName')?.patchValue('');
            this.videoFileBtn = 'Replace';
            // this._messageService.error(error?.error?.message);
          },
        });
      }
    }
  }

  showImage(event?: any) {
    this.formGroup.get('thumbnailPath')?.patchValue('');
    this.showThumbnail = false;
    const input = event.target;
    if (input.files.length > 0) {
      const file = input.files[0];
      const fileType = file.type.split('/')[1];
      if (!['jpg', 'jpeg', 'gif', 'png'].includes(fileType)) {
        this._messageService.error(
          'Please select a valid image file (jpg, jpeg, gif, png).'
        );
        return;
      } else {
        this.formGroup.get('thumbnailName')?.patchValue(file.name);
        this._fileManagerService
          .uploadFile(file, 'PREVIEW_THUMBNAIL')
          .subscribe({
            next: (response: any) => {
              if (
                response?.status ==
                this._httpConstants.REQUEST_STATUS.CREATED_201.CODE
              ) {
                this.showThumbnail = true;
                this.imageFileBtn = 'Replace';
                this.formGroup.get('thumbnailPath')?.patchValue(response.data);
              }
            },
            error: (error: any) => {
              this.formGroup.get('thumbnailName')?.patchValue('No choose file');
              this.videoFileName = '';
            },
          });
      }
    }
  }

  getCourseFirstStepDetail() {
    this._courseService.getCourseFirstStepDetail(this.courseId).subscribe({
      next: (response) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.selectedContentType = response?.data?.contentType?.toLowerCase();
          this.handleConditionalValidation();
          this.patchForm(response?.data);
        }
      },
    });
  }

  patchForm(data?: any) {
    this.imageFileBtn = 'Replace';
    this.videoFileBtn = 'Replace';
    this.currentSelectedTopic.videoUrl = data?.previewVideoUrl;
    this.currentSelectedTopic.vttContent = data?.previewVideoVttContent;
    let tagsArray: any = [];
    let courseSummaries: any = [];
    if (this.formGroup.get('tagsArray').value.length == 0) {
      data?.tags?.forEach((tag: any) => {
        this.addCourseTag(tag);
      });
    }
    if (
      this.formGroup.get('courseSummaries').value.length == 0 ||
      (this.formGroup.get('courseSummaries').value.length == 1 &&
        this.formGroup.get('courseSummaries').value[0].courseSummaryInfo == '')
    ) {
      if (data?.courseOutcome?.length > 0) {
        data?.courseOutcome?.forEach((outcome: any) => {
          this.addCourseSummary(outcome);
        });
        this.courseSummaryArray.removeAt(0);
      }
    }
    this.manageCourseSummaryArrayLength();
    let courseType;
    courseType = this.courseTypes.find(
      (courseType) => courseType.value == data?.courseType
    );

    this.formGroup.patchValue({
      courseTitle: data?.title,
      courseUrl: data?.courseUrl,
      courseCategory: this.categoryList.find(
        (category) => category.id == data?.categoryId
      ),
      courseType:
        courseType?.value != this.courseType.PREMIUM
          ? this.courseType.STANDARD
          : courseType?.value,
      courseHeadline: data?.about,
      price: data?.price,
      courseLevel: this.listOfLevel.find((level) => level.id == data?.levelId),
      description: data?.courseDescription,
      thumbnailPath: data?.courseThumbnailUrl,
      previewPath: data?.previewVideoUrl,
      previewVideoVttContent: data?.previewVideoVttContent,
      prerequisite: data?.prerequisite[0],
      courseProgress: data?.courseProgress,
      certificateEnabled: data?.certificateEnabled,
    });

    Object.keys(this.formGroup.controls).forEach((controlName) => {
      const control = this.formGroup.get(controlName);
      if (control) {
        control.markAsDirty();
        control.updateValueAndValidity();
      }
    });

    this.courseTitleExist();
    this.courseUrlExist();

    this.showPreview =
      this.formGroup.get('previewPath').value != '' ? true : false;
    this.showThumbnail =
      this.formGroup.get('thumbnailPath').value != '' ? true : false;
  }

  manageCourseSummaryArrayLength() {
    this.courseSummaryArrayLength = 0;
    this.courseSummaryArray.controls.forEach((el) => {
      if (el.value.courseSummaryInfo) {
        this.courseSummaryArrayLength += 1;
      }
    });
  }

  steps(step?: any) {
    if (
      this.formGroup.valid &&
      this.courseSummaryArrayLength > 0 &&
      this.courseTagArray.controls.length != 0
    ) {
      this.publishCourse(step);
    }
  }

  customRequestVideo = (item: NzUploadXHRArgs): Subscription => {
    const file = item.file as unknown as File;

    const validPattern = /^[a-zA-Z0-9._\-()@+\[\],\s]+$/;
    if (validPattern.test(file.name)) {
      if (
        file.type.split('/')[0] == 'video' &&
        file.type.split('/')[1] == 'mp4'
      ) {
        const maxSizeGB = 4;
        if (file.size < maxSizeGB * 1024 * 1024 * 1024) {
          this.formGroup.get('previewPath')?.patchValue('');
          this.showPreview = false;
          this.videoProgressBar = true;

          this.formGroup.get('previewName')?.patchValue(file.name);
          this._fileManagerService.uploadFile(file, 'PREVIEW_VIDEO').subscribe({
            next: (response: any) => {
              if (
                response?.status ==
                this._httpConstants.REQUEST_STATUS.CREATED_201.CODE
              ) {
                this.showPreview = true;
                this.videoFileBtn = 'Replace';
                this.formGroup
                  .get('previewPath')
                  ?.patchValue(response.data?.fileUrl);
                this.formGroup
                  .get('previewVideoVttContent')
                  ?.patchValue(response.data?.previewVideoVttContent);
                this.videoProgressBar = false;
                this.showPreview = true;
                this.currentSelectedTopic = {
                  ...this.currentSelectedTopic,
                  videoUrl: response.data?.fileUrl,
                  vttContent: response.data?.previewVideoVttContent,
                };
              }
            },
            error: (error: any) => {
              this.formGroup.get('previewName')?.patchValue('');
              this.videoProgressBar = false;
              this.videoFileBtn = 'Replace';
              this._messageService.error(error?.error?.message);
            },
          });
        } else {
          this._messageService.error('Size should not exceed 4 GB.');
        }
      } else {
        this._messageService.error('Please upload a video file in MP4 format.');
      }
    } else {
      this._messageService.error('File name contains special characters.');
    }

    return null;
  };

  openImageUploaderModal(): void {
    const modalRef = this._modal.create({
      nzTitle: '',
      nzContent: ImageUploaderModalComponent,
      nzFooter: null,
      nzComponentParams: {
        imageAspectRatio: 1, // Pass the imageAspectRatio here
      },
    });

    this.afterCloseSubscription = modalRef.afterClose.subscribe((data: any) => {
      if (data && data.profilePicture) {
        this.formGroup.get('thumbnailPath').setValue(data.profilePicture);
        this.formGroup.get('thumbnailName').setValue(data.fileName);
        this.showThumbnail = true;
        this.imageFileBtn = 'Replace';
      }
    });
  }

  customRequestImage = (item: NzUploadXHRArgs): Subscription => {
    const file = item.file as unknown as File;
    if (['jpg', 'jpeg', 'gif', 'png'].includes(file.type.split('/')[1])) {
      this.formGroup.get('thumbnailPath')?.patchValue('');
      this.imageProgressBar = true;
      this.showThumbnail = false;
      this.formGroup.get('thumbnailName')?.patchValue(file.name);
      this._fileManagerService.uploadFile(file, 'PREVIEW_THUMBNAIL').subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.CREATED_201.CODE
          ) {
            this.showThumbnail = true;
            this.imageProgressBar = false;
            this.imageFileBtn = 'Replace';
            this.formGroup.get('thumbnailPath')?.patchValue(response.data);
          }
        },
        error: (error: any) => {
          this.formGroup.get('thumbnailName')?.patchValue('No choose file');
          this.imageProgressBar = false;
          this.videoFileName = '';
          // this._messageService.error(error?.error?.message);
        },
      });
    } else {
      this._messageService.error(
        'Please select a valid image file (jpg, jpeg, gif, png).'
      );
    }
    return null;
  };

  publishCourse(step?: any) {
    if (
      !this.courseSaved &&
      (this.anyFieldValid() || this.sectionsData) &&
      this.formGroup.get('courseProgress').value != 100
    ) {
      this.courseSaved = true;
      this._courseService
        .createCourseDto(
          this.formGroup,
          this.sectionsData,
          this.courseId,
          false,
          null,
          this.selectedContentType
        )
        .subscribe({
          next: (response: any) => {
            if (
              response?.status ==
              this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
            ) {
              if (this.courseId == null) {
                this.courseId = response?.data?.courseId;
              }
              // this._courseService.course.tags = [];
              // this._courseService.tags = [];
              this._communicationService.updateInstructorCourse();
              this.currentStep.emit(step);
              this.courseInformationData.emit(this.formGroup);
              this.draftCourseId.emit(this.courseId);
            } else {
              this.courseSaved = false;
              // this._courseService.course.tags = [];
              // this._courseService.tags = [];
            }
          },
          error: (error: any) => {
            this.courseSaved = false;
            console.log(error);
          },
        });
    } else if (this.formGroup.get('courseProgress').value == 100) {
      this.currentStep.emit(step);
      this.courseInformationData.emit(this.formGroup);
      this.draftCourseId.emit(this.courseId);
    }
  }

  saveAsDraftCourse() {
    if (
      !this.courseSaved &&
      (this.anyFieldValid() || this.sectionsData?.length > 0) &&
      this.formGroup.get('courseProgress').value != 100
    ) {
      this.courseSaved = true;
      this._courseService
        .createCourseDto(
          this.formGroup,
          this.sectionsData,
          this.courseId,
          false,
          null,
          this.selectedContentType
        )
        ?.subscribe({
          next: (response: any) => {
            if (
              response?.status ==
              this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
            ) {
              if (this.courseId == null) {
                this.courseId = response?.data?.courseId;
              }
              this._communicationService.updateInstructorCourse();
            } else {
              this.courseSaved = false;
            }
          },

          error: (error: any) => {
            console.log(error);
            this.courseSaved = false;
          },
        });
    }
  }

  anyFieldValid() {
    let valid = false;
    if (
      this.formGroup.get('courseTitle').value != '' &&
      !this.formGroup.get('titleExist').value &&
      this.formGroup.get('courseUrl').value != '' &&
      !this.formGroup.get('urlExist').value
    ) {
      valid = true;
      return valid;
    }
    if (
      this.formGroup.get('description').value != null &&
      this.formGroup.get('description').value != ''
    ) {
      valid = true;
      return valid;
    }
    if (this.formGroup.get('courseCategory').value?.id) {
      valid = true;
      return valid;
    }
    if (this.formGroup.get('courseType').value?.value) {
      valid = true;
      return valid;
    }
    if (this.formGroup.get('courseLevel').value?.id) {
      valid = true;
      return valid;
    }
    if (this.formGroup.get('courseHeadline').value != '') {
      valid = true;
      return valid;
    }
    if (this.formGroup.get('thumbnailPath').value != '') {
      valid = true;
      return valid;
    }
    if (this.formGroup.get('previewPath').value != '') {
      valid = true;
      return valid;
    }
    if (this.formGroup.get('tagsArray').value.length > 0) {
      valid = true;
      return valid;
    }
    if (
      this.formGroup.get('courseSummaries').value.length > 0 &&
      this.formGroup.get('courseSummaries').value[0].courseSummaryInfo != ''
    ) {
      valid = true;
      return valid;
    }
    if (this.formGroup.get('prerequisite').value != '') {
      valid = true;
      return valid;
    }

    return valid;
  }

  courseTitleExist() {
    const courseTitleControl = this.formGroup.get('courseTitle').value;
    if (courseTitleControl) {
      const processedTitle = this.processInput(courseTitleControl);
      this.formGroup.get('courseTitle').setValue(processedTitle);
    }
    const title = this.formGroup.get('courseTitle').value.trim();
    if (title.length >= this.minTitleLength) {
      this._courseService
        .courseTitleExist(title, this.courseId ? this.courseId : 0)
        .subscribe({
          next: (response: any) => {
            if (
              response?.status ==
              this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
            ) {
              this.formGroup.get('titleExist').setValue(false);
              this.formGroup.get('urlExist').setValue(false);
              if (this.formGroup.get('courseProgress').value != 100) {
                this.generatedUrl = response.data;
                this.formGroup.get('courseUrl').setValue(this.generatedUrl);
                this.courseUrlExist();
              }
            }
          },
          error: (error: any) => {
            this.formGroup.get('titleExist').setValue(true);
            // this.formGroup.get('urlExist').setValue(true);
            this.generatedUrl = '';
          },
        });
    } else {
      this.formGroup.get('courseUrl').setValue(null);
      this.formGroup.get('titleExist').setValue(false);
      this.formGroup.get('urlExist').setValue(false);
      this.generatedUrl = '';
    }
  }

  courseUrlExist() {
    if (this.formGroup.get('courseUrl')?.value) {
      const url = this.formGroup.get('courseUrl')?.value?.trim();
      this._courseService
        .courseUrlExist(url, this.courseId ? this.courseId : 0)
        .subscribe({
          next: (response: any) => {
            if (
              response?.status ==
              this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
            ) {
              this.formGroup.get('urlExist').setValue(false);
              // this.generatedUrl = response.data;
            }
          },
          error: (error: any) => {
            this.formGroup.get('urlExist').setValue(true);
            // this.generatedUrl = '';
          },
        });
    }
  }

  trimmedTitleLengthValidator(min: number, max: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const trimmedValue = control.value?.trim();
      const length = trimmedValue ? trimmedValue.length : 0;
  
      if (length < min) {
        return { minLength: { requiredLength: min, actualLength: length } };
      }
      if (length > max) {
        return { maxLength: { requiredLength: max, actualLength: length } };
      }
      return null;
    };
  }

  processInput(input: string): string {
    // return input.replace(/\s+/g, ' ').replace(/-/g, '');
    return input.replace(/\s+/g, ' ');
  }

  preventSpecialChar(event: KeyboardEvent) {
    const regex = /^[a-zA-Z0-9+#&_:, ]+$/;

    if (!regex.test(event.key)) {
      event.preventDefault();
    }
  }

  preventSpecialCharOnPaste(event: ClipboardEvent) {
    const clipboardData = event.clipboardData || (window as any).clipboardData;
    const pastedText = clipboardData.getData('text');
    const regex = /^[a-zA-Z0-9+#&_:, ]+$/;

    if (!regex.test(pastedText)) {
      event.preventDefault();
    }
  }

  preventSpecialCharUrl(event: KeyboardEvent) {
    const regex = /^[a-zA-Z0-9-]*$/;

    if (!regex.test(event.key)) {
      event.preventDefault();
    }
  }

  preventSpecialCharUrlOnPaste(event: ClipboardEvent) {
    const clipboardData = event.clipboardData || (window as any).clipboardData;
    const pastedText = clipboardData.getData('text');
    const regex = /^[a-zA-Z0-9-]*$/;

    if (!regex.test(pastedText)) {
      event.preventDefault();
    }
  }

  addYoutubeUrl() {
    this.formGroup
      .get('previewPath')
      .setValue(this.formGroup.get('previewName').value);
    this.currentSelectedTopic = {
      ...this.currentSelectedTopic,
      videoUrl: this.formGroup.get('previewPath').value,
    };
    this.showPreview = true;
  }

  youtubeInputChange() {
    if (this.formGroup.get('previewName').value == '') {
      this.videoFileBtn = 'Upload File';
      this.isYoutubeLinkPresent = false;
    } else if (this.checkYoutubeLink()) {
      this.videoFileBtn = 'Add';
      this.isYoutubeLinkPresent = true;
      // this.formGroup.get('previewPath').setValue(this.formGroup.get('previewName').value);
    }
  }

  checkYoutubeLink(): boolean {
    const youtubeRegex =
      /^(https?\:\/\/)?((www|m)\.youtube\.com|youtu\.?be)\/.+$/;
    if (youtubeRegex.test(this.formGroup.get('previewName').value)) {
      return true;
    }
    return false;
  }

  copyUrl(): void {
    // Get the input element and the static URL
    const inputElement = document.getElementById('url-input') as HTMLInputElement;
    const textToCopy = this.applicationCourseDetailsUrl + inputElement.value ;
    
    // Create a temporary textarea element to copy text
    const tempTextArea = document.createElement('textarea');
    tempTextArea.value = textToCopy;
    
    // Append the textarea to the document, select its content, and copy it
    document.body.appendChild(tempTextArea);
    tempTextArea.select();
    tempTextArea.setSelectionRange(0, 99999); // For mobile devices
    
    // Execute the copy command
    try {
      document.execCommand('copy');
      this.copyTooltipText = 'Copied!';
      this.urlTooltip = true;

      // Hide the tooltip after 1 second
      setTimeout(() => {
        this.urlTooltip = false;
        this.copyTooltipText = 'Click to copy URL';
      }, 1000);
    } catch (err) {
      console.error('Unable to copy', err);
    }

    // Remove the temporary textarea element
    document.body.removeChild(tempTextArea);
  }

  get filteredCourseTypes() {
    return this.courseTypes.filter(ct => ct.name !== 'Free' && ct.name !== 'All');
  }

  premiumCourseAvailable(){
    this._courseService.premiumCourseAvailable().subscribe({
      next: (response: any) => {
        if (response?.status === 200) {
            this.isAvailablePremium = response?.data?.isAvailablePremium;
        }
      },
      error: (error) => {
      },
    });
  }

   openSubscriptionPlan(): void {
      const modal = this._modal.create({
        nzContent: SubscriptionPlanComponent,
        nzComponentParams: {
          fromSubscriptionPlan: true,
          showFreePlan: false,
          showStandardPlan: false,
        },
        nzViewContainerRef: this._viewContainerRef,
        nzFooter: null,
        nzKeyboard: true,
        // nzWidth: this.fullWidth ? '80%' : '100%',
        nzWidth: '80%',
      });
      modal.afterClose?.subscribe((result) => {
        // this.subscriptionModalOpened = false;
      });
    }
  

    preventEmoji(event: KeyboardEvent) {
      const key = event.key;
      const emojiRegex = /\p{Extended_Pictographic}/u;
    
      if (emojiRegex.test(key)) {
        event.preventDefault();
      }
    }
    
    preventEmojiOnPaste(event: ClipboardEvent) {
      const pastedText = event.clipboardData?.getData('text') || '';
      const emojiRegex = /\p{Extended_Pictographic}/u;
    
      if (emojiRegex.test(pastedText)) {
        event.preventDefault();
      }
    }
    
}
