import { Component, ViewContainerRef } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { Router } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { error } from '@ant-design/icons-angular';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { AIClass } from 'src/app/core/models/ai-class.model';
import { AIAssessment } from 'src/app/core/models/assessment.model';
import { MessageService } from 'src/app/core/services/message.service';
import { SubscriptionPlanComponent } from 'src/app/modules/auth/subscription-plan/subscription-plan.component';
import { CacheService } from 'src/app/core/services/cache.service';
import { SubscriptionPlanType } from 'src/app/core/enums/subscription-plan.enum';

@Component({
  selector: 'app-grader-uploader',
  templateUrl: './grader-uploader.component.html',
  styleUrls: ['./grader-uploader.component.scss'],
})
export class GraderUploaderComponent {
  className: any;
  constructor(
    private modal: NzModalService,
    private aiGraderService: AiGraderService,
    private router: Router,
    private _messageService: MessageService,
    public sanitizer: DomSanitizer,
    private loader: NgxUiLoaderService,
    private _viewContainerRef: ViewContainerRef,
    private _cacheService?: CacheService
  ) {}

  userCreatedClasses: Array<{ label: string; value: string }> = [];
  isProcessing = false;
  selectedClass?: any | null = 0;
  newClassName: string = '';
  selectedAssessment: any = '';
  newAssessmentName: string = '';
  classes?: AIClass[] = [];
  assessments?: AIAssessment[] = [];
  showClassDropdown = false;
  showAssessmentDropdown = false;
  isDropdownOpen = false;
  isDragging = false;
  uploadedFiles: any[] = [];
  filePreviewUrl: string | null = null;
  selectedFileName: string = '';
  pdfSrc: string | ArrayBuffer | null = null;
  totalPages = 0;
  pages: number[] = [];
  showNewClassInput = false;
  showNewAssessmentInput = false;
  filteredClassList: AIClass[] = [];
  filteredAssessmentList?: AIAssessment[] = [];
  selectedAnswerFile: File | null = null;
  evaluationCriteria?: any;
  classesTotalPages?: any;
  assessmentsTotalPages?: any;
  classPayload?: any = {
    classId: null,
    pageNo: 0,
    pageSize: 10,
  };
  assessmentPayload?: any = {
    classId: null,
    search: null,
    pageNo: 0,
    pageSize: 10,
  };
  gradedPapers?: number = 0;
  allowedPapers?: number = 0;
  maxPapers = 500;
  showUpgradePlan?: boolean = true;
  subscriptionPlanType = SubscriptionPlanType;

  ngOnInit(): void {
    this.fetchClasses();
    this.getNoOfPages();
    this.showUpgradePLanButton();
  }

  fetchClasses(append?: boolean): void {
    this.aiGraderService.getClasses(this.classPayload).subscribe({
      next: (res) => {
        this.classes = append
          ? [...this.classes, ...res?.data?.aiClasses]
          : res?.data?.aiClasses ?? [];
        this.classesTotalPages = res?.data?.pages;
        this.filteredClassList = [...this.classes];
      },
      error: (err) => {
        console.error('Failed to load classes:', err);
      },
    });
  }

  showUpgradePLanButton() {
    const planType: string = this._cacheService.getJsonData(
      'loggedInUserDetails'
    )?.subscriptionPlanType;

    if (
      planType &&
      (planType.toLowerCase() ===
        this.subscriptionPlanType.PREMIUM.toLowerCase() ||
        planType.toLowerCase() ===
          this.subscriptionPlanType.ULTIMATE.toLowerCase())
    ) {
      this.showUpgradePlan = false;
    } else {
      this.showUpgradePlan = true;
    }
  }

  // openClassModal(): void {
  //   const modalRef = this.modal.create({
  //     nzTitle: 'New Class',
  //     nzContent: ClassUploaderComponent,
  //     nzClosable: false,
  //     nzMaskClosable: false,
  //     nzFooter: null,
  //     nzWidth: 600,
  //   });

  //   modalRef.afterClose.subscribe((createdClass) => {
  //     if (createdClass?.name) {
  //       const newOption = {
  //         label: createdClass.name,
  //         value: createdClass.name,
  //       };

  //       const exists = this.userCreatedClasses.find(
  //         (c) => c.value === createdClass.name
  //       );

  //       if (!exists) {
  //         this.userCreatedClasses = [...this.userCreatedClasses, newOption];
  //       }

  //       this.selectedClass = createdClass.name;
  //       this.isDropdownOpen = false;
  //     }
  //   });
  // }

  gradeNow(): void {
    if (!this.selectedClass || !this.selectedAssessment) {
      this._messageService.error('Please Select A Class');
      return;
    }

    const formData = new FormData();

    this.uploadedFiles.forEach((fileWrapper: any) => {
      formData.append('quiz_files', fileWrapper.file);
    });

    if (this.selectedAnswerFile) {
      formData.append('answer_key_file', this.selectedAnswerFile);
    }

    formData.append('evaluationCriteria', this.evaluationCriteria);
    formData.append('assessmentId', this.selectedAssessment);

    this.isProcessing = true;

    this.aiGraderService.startGrading(formData).subscribe({
      next: (res) => {
        this.isProcessing = false;
        this.router.navigate(['instructor/ai-grader/results'], {
          queryParams: {
            id: this.selectedAssessment,
            classId: this.selectedClass,
          },
        });
      },
      error: (err) => {
        this._messageService.error(err?.error?.message);
        this.isProcessing = false;
        this.router.navigate(['instructor/ai-grader/uploader'], {});
      },
    });
  }

  onAnswerFileSelected(event: any): void {
    const file: File = event.target.files[0];

    if (file) {
      if (file.type === 'application/pdf') {
        this.selectedFileName = file.name;
        this.selectedAnswerFile = file;
      } else {
        this.selectedFileName = 'No file chosen';
        this.selectedAnswerFile = null;

        // Show error message
        this._messageService?.error?.('Please upload a pdf file.');
        // OR use alert if message service is not available
        // alert(`${file.name} is not a valid PDF file.`);
      }
    } else {
      this.selectedFileName = 'No file chosen';
      this.selectedAnswerFile = null;
    }

    // Optional: reset file input if needed
    event.target.value = '';
  }

  onFileUpload(event: any) {
    const files: FileList = event.target.files;

    for (let i = 0; i < files.length; i++) {
      const file = files[i];

      if (file.type === 'application/pdf') {
        const objectUrl = URL.createObjectURL(file);

        const alreadyUploaded = this.uploadedFiles.some(
          (f) => f.name === file.name
        );
        if (alreadyUploaded) continue;

        this.uploadedFiles.push({
          type: file.type,
          src: objectUrl,
          pages: [1],
          file,
          name: file.name,
        });
      } else {
        this._messageService?.error?.(`${file.name} is not a PDF file.`);
      }
    }
  }

  onPdfLoaded(pdf: any, file: any) {
    console.log('PDF loaded:', pdf);
    file.numPages = pdf.numPages;
    file.pages = Array.from({ length: pdf.numPages }, (_, i) => i + 1);
  }

  resetUpload() {
    this.uploadedFiles = [];
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.isDragging = false;
    if (event.dataTransfer?.files) {
      this.onFileUpload({ target: { files: event.dataTransfer.files } });
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.isDragging = true;
  }

  onDragLeave() {
    this.isDragging = false;
  }

  fetchAssessments(append?: boolean): void {
    if (!this.selectedClass) {
      console.warn('No class selected. Cannot fetch assessments.');
      return;
    }
    this.assessmentPayload.classId = this.selectedClass;

    this.aiGraderService.getAssessments(this.assessmentPayload).subscribe({
      next: (res) => {
        this.assessments = append
          ? [...this.assessments, ...res?.data?.aiAssessments]
          : res?.data?.aiAssessments ?? [];
        // this.assessments = res?.data?.aiAssessments;
        this.assessmentsTotalPages = res?.data?.pages;
        this.filteredAssessmentList = [...this.assessments];
      },
      error: (err) => {
        console.error('Failed to load assessments:', err);
      },
    });
  }

  selectClass(value: string) {
    this.showClassDropdown = false;
    if (value === 'create-new') {
      this.newClassName = '';
    } else {
      this.newClassName = value;
    }
  }

  selectAssessment(value: string) {
    this.showAssessmentDropdown = false;
    if (value === 'create-new') {
      this.selectedAssessment = '';
    } else {
      this.selectedAssessment = value;
    }
  }

  handleClassCreate() {
    const trimmed = this.newClassName.trim();
    if (!trimmed) return;

    const newClass = { name: trimmed };

    this.aiGraderService.createClass(newClass).subscribe({
      next: (res) => {
        const createdId = res?.data?.id;
        if (createdId) {
          this.showNewClassInput = false;
          this.selectedClass = createdId;
          this.fetchClasses();
          setTimeout(() => {
            // this.showNewAssessmentInput = true;
          }, 0);
        }
      },
      error: (err) => console.error('Error creating class:', err),
    });
  }

  handleAssessmentCreate() {
    const trimmed = this.newAssessmentName.trim();
    const classId = Number(this.selectedClass);

    if (!trimmed || !classId) return;

    const newAssessment = {
      name: trimmed, // ✅ Add this!
      classId: classId,
    };

    this.aiGraderService.createAssessment(newAssessment).subscribe({
      next: (res) => {
        const created = res?.data; // ✅ changed here
        if (created?.id) {
          if (!this.assessments.some((a) => a.id === created.id)) {
            this.assessments.unshift(created);
          }
          this.selectedAssessment = created.id;
          this.newAssessmentName = '';
          this.showNewAssessmentInput = false;
        }
      },
      error: (err) => console.error('Error creating assessment:', err),
    });
  }

  onClassDropdownChange(value: string | number) {
    if (value === '__create__') {
      this.showNewClassInput = true;
      this.newClassName = '';
      this.assessments = [];
      this.filteredAssessmentList = [];
    } else {
      this.showNewClassInput = false;
      this.showNewAssessmentInput = false;
      this.selectedClass = Number(value);
      const selected = this.classes?.find((c) => c.id === this.selectedClass);
      this.newClassName = selected?.name || '';
      this.assessments = [];
      this.fetchAssessments();
    }

    this.selectedAssessment = '';
    this.newAssessmentName = '';
  }

  onAssessmentDropdownChange(value: string | number) {
    if (value === '__create__') {
      this.showNewAssessmentInput = true;
      this.selectedAssessment = '';
    } else {
      this.showNewAssessmentInput = false;
      this.selectedAssessment = value;
      const selected = this.assessments.find(
        (a) => Number(a.id) === Number(value)
      );
      this.newAssessmentName = selected?.name || '';
    }
  }

  cancelNewAssessmentInput() {
    if (!this.selectedAssessment.trim()) {
      this.showNewAssessmentInput = false;
    }
  }

  onClassScrollToBottom() {
    if (this.classPayload?.pageNo + 1 < this.classesTotalPages) {
      this.classPayload.pageNo++;
      this.fetchClasses(true);
    }
  }

  onAssessmentScrollToBottom() {
    if (this.assessmentPayload?.pageNo + 1 < this.assessmentsTotalPages) {
      this.assessmentPayload.pageNo++;
      this.fetchAssessments(true);
    }
  }

  openSubscriptionPlan(): void {
    const modal = this.modal.create({
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

  getNoOfPages() {
    this.aiGraderService.getNoOfPagesUsed().subscribe({
      next: (res) => {
        this.gradedPapers = res?.data.noOfPagesUsed
          ? res?.data.noOfPagesUsed
          : 0;
        this.allowedPapers = res?.data.allowedPages
          ? res?.data.allowedPages
          : 0;
      },
      error: (err) => console.error('Error creating assessment:', err),
    });
  }
}
