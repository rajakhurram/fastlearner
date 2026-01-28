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
import { ClassAssessmentModalComponent } from 'src/app/modules/dynamic-modals/class-assessment-modal/class-assessment-modal.component';

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
            source: 'uploader'
          },
        });
      },
     error: (err) => {
    this.isProcessing = false;

    if (err.status != 500 && err.error?.message) {
      this._messageService.error(err.error.message);
    }else {
      this._messageService.error('Upload failed. Please try again.');
    }

    this.router.navigate(['instructor/ai-grader/uploader']);
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
  const newFiles = Array.from(files || []);
  const totalFiles = (this.uploadedFiles?.length || 0) + newFiles.length;

  if (totalFiles > 40) {
    this._messageService?.error('You can upload a maximum of 40 files.');
    return;
  }

  for (let i = 0; i < newFiles.length; i++) {
    const file = newFiles[i];

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

  const files = Array.from(event.dataTransfer?.files || []);
  const totalFiles = (this.uploadedFiles?.length || 0) + files.length;

  if (totalFiles > 40) {
    this._messageService?.error('You can upload a maximum of 40 files.');
    return;
  }

  // Only proceed if limit is fine
  this.onFileUpload({ target: { files: event.dataTransfer?.files } });
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

onClassDropdownChange(value: string | number): void {
  // Use setTimeout to ensure the dropdown updates properly
  setTimeout(() => {
    if (value === '__create__') {
      this.selectedClass = null;
      this.assessments = [];            
      this.filteredAssessmentList = []; 
      this.selectedAssessment = null;
      this.openModal('class');
    } else {
      this.selectedClass = Number(value);
      this.assessments = [];            
      this.filteredAssessmentList = [];
      this.selectedAssessment = null;
      this.fetchAssessments();
    }
  });
}

onAssessmentDropdownChange(value: string | number): void {
  // Use setTimeout to ensure the dropdown updates properly
  setTimeout(() => {
    if (value === '__create__') {
      this.selectedAssessment = null;   
      this.openModal('assessment');
    } else {
      this.selectedAssessment = Number(value);
    }
  });
}


  cancelNewAssessmentInput() {
  if (!this.newAssessmentName?.trim()) {
    this.showNewAssessmentInput = false;
    this.selectedAssessment = null; // ✅ clear wrong selection
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

  undoRubricFile(){
    this.selectedAnswerFile = null;
    this.selectedFileName = null;
  }

  openModal(type: 'class' | 'assessment'): void {
  const modalRef = this.modal.create({
    nzContent: ClassAssessmentModalComponent,
    nzComponentParams: {
      type,
      classId: this.selectedClass,
    },
    nzFooter: null,
    nzWidth: 600,
  });

  modalRef.afterClose.subscribe((created) => {
    if (created) {
      if (created.type === 'class') {
        this.classes.push({ id: created.id, name: created.name });
        this.selectedClass = created.id;
        this.fetchAssessments(); // ✅ Fetch assessments tied to new class
      } else if (created.type === 'assessment') {
        this.assessments.push({ id: created.id, name: created.name });
        this.selectedAssessment = created.id;
      }
    } else {
      // ✅ Reset the dropdowns when modal is closed without creation
      if (type === 'class') {
        this.selectedClass = null;
      } else if (type === 'assessment') {
        this.selectedAssessment = null;
      }
    }
  });
}

}
