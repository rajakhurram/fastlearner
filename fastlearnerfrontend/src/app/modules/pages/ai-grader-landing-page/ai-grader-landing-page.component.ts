import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  AfterViewInit,
  OnDestroy,
  OnInit,
  NgZone,
  ViewChild,
} from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { aiGraderCards } from 'src/app/core/constants/staticData.constants';
import { AccordionItems } from 'src/app/core/interfaces/accordian.interafce';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SubscriptionPlanType } from 'src/app/core/enums/subscription-plan.enum';
import JSZip from 'jszip';

@Component({
  selector: 'app-ai-grader-landing-page',
  templateUrl: './ai-grader-landing-page.component.html',
  styleUrls: ['./ai-grader-landing-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AiGraderLandingPageComponent
  implements OnInit, AfterViewInit, OnDestroy
{
  private readonly freePlanMaxGradableFiles = 50;
  @ViewChild('textAreaRef') textAreaRef!: ElementRef;

  scheduleDemo() {
    throw new Error('Method not implemented.');
  }
  isMobile = false;
  /** Desktop video is mounted after first paint to reduce FCP/TBT. */
  showHeroVideo = false;
  isDragging = false;
  uploadedFiles: any[] = [];
  evaluationCriteria?: any;
  assessmentName = '';
  className = '';
  assessmentId?: any;
  classId?: any;
  selectedFileName: string = '';
  selectedAnswerFile: any | null = null;
  isLoggedIn: any;
  logos: string[] = [
    'assets/icons/1one.webp',
    'assets/icons/2two.webp',
    'assets/icons/3three.webp',
    'assets/icons/4four.webp',
    'assets/icons/5five.webp',
    'assets/icons/6six.webp',
    'assets/icons/7seven.webp',
    'assets/icons/8eight.png',
    'assets/icons/9nine.webp',
  ];
  @ViewChild('faq_container') faq_container!: ElementRef;
  @ViewChild('carousel') carousel!: ElementRef;
  @ViewChild('power_of_grader') power_of_grader!: ElementRef;
  @ViewChild('premium_courses') premium_courses!: ElementRef;
  @ViewChild('logo_slider') logo_slider!: ElementRef;
  @ViewChild('about_us_bg_light') about_us_bg_light!: ElementRef;
  @ViewChild('teacher_review') teacher_review!: ElementRef;
  @ViewChild('effortless') effortless!: ElementRef;
  @ViewChild('grading') grading!: ElementRef;
  @ViewChild('student_pick') student_pick!: ElementRef;
  filePreviewUrl: string | null = null;
  totalFileCount = 0;
  isProcessing = false;
  private destroyed = false;
  private sectionObserver?: IntersectionObserver;
  userLoggedIn?: boolean = false;
  private pendingAutoGradeFromCache = false;
  private loggedInUserPlanType?: string = '';

  graderForm = new FormGroup({
    className: new FormControl<string>(null, [Validators.required]),
    assessmentName: new FormControl<string>(null, [Validators.required]),
    evaluationCriteria: new FormControl<string>(null, [Validators.required]),
    userSubmittedAnswerAsText: new FormControl<string>(null, [
      Validators.maxLength(12000),
    ]),
  });

  constructor(
    private _router: Router,
    private _messageService: MessageService,
    private aiGraderService: AiGraderService,
    private router: Router,
    private _authService: AuthService,
    private _cacheService: CacheService,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone,
  ) {}

  loggedInStatic = [];
  staticCards = aiGraderCards;
  isAcrdionExpanded: boolean = false;
  faqContainerSectionsLoaded = true;

  isActive: boolean = false;

  items: AccordionItems[] = [
    {
      title: 'What is an AI Grader?',
      description: `An AI Grader is a tool that uses artificial intelligence to score student work, such as exams and assignments, automatically. Fast Learner’s AI grading tool helps teachers save time and ensures consistent results.
      `,
      isExpanded: false,
    },
    {
      title: 'Which AI is best for grading?',
      description: `The best AI grading tools combine accuracy, speed, and ease of use. Fast Learner’s AI Grader offers a seamless experience for teachers: simply upload your students’ work, and the AI grading system will analyze and score each paper using advanced algorithms.
      `,
      isExpanded: false,
    },
    {
      title: 'Can an AI grade student work?',
      description: `Yes, AI can grade student work! Our AI grading tool is built to evaluate various assignments, from multiple-choice quizzes to written essays. Teachers can receive instant feedback and detailed scoring by leveraging AI grading technology.
      `,
      isExpanded: false,
    },
    {
      title: 'Is AI Grading accurate?',
      description: `Thanks to improvements in machine learning, AI grading has become highly accurate. Fast Learner’s AI Grader is trained on diverse educational data to ensure fair and consistent results. While no system is perfect, our AI grading tool is continually updated to improve accuracy.
      `,
      isExpanded: false,
    },
    {
      title: 'How can AI grade essays?',
      description: `AI can grade essays by analyzing the content, structure, grammar, and relevance of each response. Our AI essay grader uses sophisticated algorithms to evaluate essays based on criteria set by educators. The essay checker tool provides detailed feedback and scores, making AI grading efficient and insightful for teachers.
      `,
      isExpanded: false,
    },
    {
      title: 'Can AI grading be customized for different subjects or rubrics?',
      description: `Absolutely. Fast Learner’s AI grader can be tailored to match your grading criteria, rubrics, and subject requirements, making it flexible for any classroom. This customization ensures that AI grading tools deliver relevant, subject-specific feedback for teachers and students.
      `,
      isExpanded: false,
    },
    {
      title: 'Is my data safe with Fast Learner’s AI grader?',
      description: `Yes, privacy is a top priority. Fast Learner’s AI grading for teachers follows strict data protection standards to keep student information secure.
      `,
      isExpanded: false,
    },
    {
      title: 'How fast is AI grading compared to manual grading?',
      description: `AI grading tools like Fast Learner can grade assignments in seconds, much faster than manual grading. This means teachers get results and feedback almost instantly.
      `,
      isExpanded: false,
    },
  ];

  readonly repeatedLogos = [...this.logos, ...this.logos];

  sectionsLoaded: { [key: string]: boolean } = {
    power_of_grader: false,
    premium_courses: false,
    logo_slider: false,
    about_us_bg_light: false,
    teacher_review: false,
    effortless: false,
    grading: false,
    student_pick: false,
    faq_container: false,
  };

  trackByIndex = (index: number) => index;
  trackByLogo = (_: number, logo: string) => logo;
  trackByFaqTitle = (_: number, item: AccordionItems) => item.title;

  ngOnInit(): void {
    this.isMobile = this.computeIsMobile();
    this.scheduleDeferredInit();
    const savedCount = Number(localStorage.getItem('totalFileCount') || 0);
    this.totalFileCount = Number.isFinite(savedCount) ? savedCount : 0;
  }

  ngAfterViewInit(): void {
    this.setupLazySections();
    this.afterFirstPaint(() => {
      if (this.destroyed) return;
      this.cdr.detach();
    });
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    this.sectionObserver?.disconnect();
  }

  private setupLazySections(): void {
    this.sectionObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const sectionId = entry.target.id;
            if (sectionId && !this.sectionsLoaded[sectionId]) {
              this.loadLazySection(sectionId);
            }
          }
        });
      },
      { rootMargin: '250px 0px', threshold: 0.01 },
    );

    Object.keys(this.sectionsLoaded).forEach((sectionId) => {
      const sectionRef = this[sectionId as keyof this] as ElementRef;
      if (sectionRef?.nativeElement) {
        this.sectionObserver?.observe(sectionRef.nativeElement);
      }
    });
  }

  private loadLazySection(sectionId: string): void {
    if (this.sectionsLoaded[sectionId]) {
      return;
    }
    this.sectionsLoaded[sectionId] = true;
    this.withChangeDetection(() => {});
  }

  onPdfLoaded(pdf: any, file: any) {
    this.withChangeDetection(() => {
      file.numPages = pdf.numPages;
      file.pages = Array.from({ length: pdf.numPages }, (_, i) => i + 1);
    });
  }

  isZipFile(file: any, zipOnly = false): boolean {
    if (!file) return false;

    const type = (file.type || '').toLowerCase();
    const name = (file.name || '').toLowerCase();

    const isZip =
      type === 'application/zip' ||
      type === 'application/x-zip-compressed' ||
      name.endsWith('.zip');

    if (zipOnly) return isZip;

    return (
      isZip ||
      type === 'application/vnd.rar' ||
      type === 'application/x-rar-compressed' ||
      name.endsWith('.rar')
    );
  }

  resetUpload(fileInput?: HTMLInputElement) {
    this.withChangeDetection(() => {
      this.uploadedFiles = [];
      this.graderForm.get('userSubmittedAnswerAsText')?.enable();
      if (fileInput) {
        fileInput.value = '';
      }
    });
  }

  async onFileUpload(event: any) {
    const files: FileList = event.target.files;
    const newFiles = Array.from(files || []);
    const totalFiles = (this.uploadedFiles?.length || 0) + newFiles.length;

    if (totalFiles > 100) {
      this._messageService?.error('You can upload a maximum of 100 files.');
      return;
    }

    let didAdd = false;
    // let queuedUnits = this.getCurrentQueuedUnits();

    for (let i = 0; i < newFiles.length; i++) {
      const file = newFiles[i];
      // const incomingUnits = await this.getIncomingUnits(file);
      // if (!this.canUploadInFreePlan(incomingUnits, queuedUnits)) continue;

      // if (file.type === 'application/pdf') {
      const objectUrl = URL.createObjectURL(file);

      const alreadyUploaded = this.uploadedFiles.some(
        (f) => f.name === file.name,
      );
      if (alreadyUploaded) continue;

      this.uploadedFiles.push({
        type: file.type,
        src: objectUrl,
        pages: [1],
        file,
        name: file.name,
        // gradingUnits: incomingUnits,
      });

      didAdd = true;
      // queuedUnits += incomingUnits;
      // }
      // else {
      //   this._messageService?.error?.(`${file.name} is not a PDF file.`);
      // }
    }

    if (didAdd) {
      this.withChangeDetection(() => {
        if (this.uploadedFiles.length) {
          this.graderForm.get('userSubmittedAnswerAsText')?.disable();
        }
      });
    }

    if (event?.target) {
      event.target.value = '';
    }
  }

  private isFreePlan(): boolean {
    return (
      (this.loggedInUserPlanType || '').toUpperCase() ===
      SubscriptionPlanType.FREE
    );
  }

  private getCurrentQueuedUnits(): number {
    return this.uploadedFiles.reduce(
      (sum, fileWrapper: any) => sum + (Number(fileWrapper?.gradingUnits) || 1),
      0,
    );
  }

  private canUploadInFreePlan(
    incomingUnits: number,
    queuedUnits: number,
  ): boolean {
    if (!this.isFreePlan()) return true;

    const remainingUnits =
      this.freePlanMaxGradableFiles - this.totalFileCount - queuedUnits;
    if (incomingUnits <= remainingUnits) return true;

    this._messageService.error('free plan limit exceed , upgrade your plan');
    return false;
  }

  private async getIncomingUnits(file: File): Promise<number> {
    if (!this.isZipFile(file, true)) return 1;

    try {
      const zip = await JSZip.loadAsync(file);
      const totalFilesInZip = Object.values(zip.files).filter(
        (entry) => !entry.dir,
      ).length;
      return totalFilesInZip || 1;
    } catch {
      this._messageService.error(
        'Invalid ZIP file. Please upload a valid ZIP archive.',
      );
      return Number.MAX_SAFE_INTEGER;
    }
  }

  loadFAQContainer() {
    this.faqContainerSectionsLoaded = true;
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

    this.onFileUpload({ target: { files: event.dataTransfer?.files } });
    this.withChangeDetection(() => {});
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.isDragging = true;
    this.withChangeDetection(() => {});
  }

  onDragLeave() {
    this.isDragging = false;
    this.withChangeDetection(() => {});
  }

  onAnswerFileSelected(event: any): void {
    const file: File = event.target.files[0];

    if (file && file.type === 'application/pdf') {
      this.withChangeDetection(() => {
        this.selectedFileName = file.name;
        // this.selectedAnswerFile = file;
        const objectUrl = URL.createObjectURL(file);
        this.selectedAnswerFile = {
          type: file.type,
          src: objectUrl,
          pages: [1],
          file,
          base64: null,
          name: file.name,
        };
        this.graderForm.get('evaluationCriteria')?.clearValidators();
        this.graderForm.get('evaluationCriteria')?.updateValueAndValidity();
      });
    } else {
      this.withChangeDetection(() => {
        this.selectedFileName = 'No file chosen';
        this.selectedAnswerFile = null;
        this.graderForm
          .get('evaluationCriteria')
          ?.setValidators([Validators.required]);
        this.graderForm.get('evaluationCriteria')?.updateValueAndValidity();
      });
      alert('Only PDF files are allowed.');
    }
  }

  expand(faq, event: Event) {
    const container = (event.currentTarget as HTMLElement).parentElement;
    const descriptionEl = container?.querySelector('.acc-description');

    if (!descriptionEl) return;

    if (faq.isExpanded) {
      descriptionEl.classList.remove('expanded');
      faq.isExpanded = false;
    } else {
      descriptionEl.classList.add('expanded');
      faq.isExpanded = true;
    }
  }

  onButtonOnClick() {
    if (this.userLoggedIn) {
      this._router.navigate(['instructor/ai-grader/uploader']);
    } else {
      this._cacheService.saveInCache(
        'redirectUrl',
        'instructor/ai-grader/uploader',
      );
      this.router.navigate(['/auth/sign-in']);
    }
  }
  // formData = new FormData();
  async gradeNow(): Promise<void> {
    if (this.isProcessing) return;

    if (this.userLoggedIn) {
      const currentUploadUnits = this.getCurrentQueuedUnits();
      if (!this.canUploadInFreePlan(currentUploadUnits, 0)) {
        return;
      }
      console.log(this.graderForm.value);
      const formData = new FormData();

      // appending quiz files

      this.uploadedFiles.forEach((fileWrapper: any) => {
        formData.append('quiz_files', fileWrapper.file);
      });
      // appending answer key (rubric) file if provided
      if (this.selectedAnswerFile) {
        formData.append('answer_key_file', this.selectedAnswerFile.file);
      }
      // converting the reactive form fields to form data
      const fields = Object.entries(this.graderForm.value);
      for (let [key, value] of fields) {
        if (value) {
          formData.append(key, value);
        }
      }

      // flagging the processing state (ensure loader shows immediately)
      this.withChangeDetection(() => {
        this.isProcessing = true;
      });

      this.aiGraderService.startGradingLandingPage(formData).subscribe({
        next: (res) => {
          if (this.isFreePlan()) {
            this.totalFileCount += currentUploadUnits;
            localStorage.setItem(
              'totalFileCount',
              this.totalFileCount.toString(),
            );
          }
          this.withChangeDetection(() => {
            this.isProcessing = false;
          });
          this.router.navigate(['instructor/ai-grader/results'], {
            queryParams: {
              id: res.data.assessmentId,
              classId: res.data.classId,
              numberOfFiles: res.data.numberOfFiles,
              // uploading: true,
            },
          });
        },
        error: (err) => {
          this.withChangeDetection(() => {
            this.isProcessing = false;
          });
          if (err?.error?.status != 500) {
            this._messageService.error(err?.error?.message);
          } else {
            this._messageService.error('Upload failed. Please try again.');
          }
        },
      });
    } else {
      const graderLandingFormData = await this.prepareGraderLandingFormData();
      this._cacheService.saveJsonData(
        'graderLandingFormData',
        graderLandingFormData,
      );
      this._cacheService.saveInCache('redirectUrl', '/ai-grader');
      this.router.navigate(['/auth/sign-in']);
    }
  }

  async prepareGraderLandingFormData(): Promise<any> {
    const quizFilesWithBase64 = await Promise.all(
      this.uploadedFiles.map(async (fileWrapper: any) => ({
        ...fileWrapper,
        base64: await this.convertToBase64(fileWrapper.file), // ✅ FIXED
      })),
    );

    const answerFileWithBase64 = this.selectedAnswerFile
      ? {
          ...this.selectedAnswerFile,
          base64: await this.convertToBase64(this.selectedAnswerFile.file), // ✅ FIXED
        }
      : null;

    return {
      quiz_files: quizFilesWithBase64,
      answer_key_file: answerFileWithBase64,
      // assessmentName: this.assessmentName,
      // className: this.className,
      // evaluationCriteria: this.evaluationCriteria,
      ...this.graderForm.value,
    };
  }

  autoPlay() {
    if (window?.innerWidth < 800) return;

    const carousel = this.carousel?.nativeElement;
    const maxScrollLeft = carousel?.scrollWidth - carousel?.offsetWidth;

    if (carousel?.scrollLeft >= maxScrollLeft) return;

    setTimeout(() => {
      carousel.scrollLeft += carousel?.offsetWidth;
      this.autoPlay();
    }, 2500);
  }

  autoPlayLogos() {
    // Intentionally no-op: logo marquee is handled via CSS animation.
  }

  convertToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  base64ToFile(base64: string, filename: string, mimeType: string): File {
    const arr = base64.split(',');
    const mime = mimeType || arr[0].match(/:(.*?);/)?.[1];
    const bstr = atob(arr[1]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);

    while (n--) {
      u8arr[n] = bstr.charCodeAt(n);
    }

    return new File([u8arr], filename, { type: mime });
  }

  handleSubmit() {
    if (this.isProcessing) return;
    this.markAllFieldsAsTouched();
    if (this.graderForm.invalid) {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } else {
      this.gradeNow();
    }
  }

  markAllFieldsAsTouched() {
    this.graderForm.markAllAsTouched();
  }

  toggleIsActive(elementName: 'text' | 'file') {
    if (!this.isActive && elementName === 'file') return;

    if (!this.graderForm.get('userSubmittedAnswerAsText')?.value) {
      this.isActive = !this.isActive;
    } else {
      this.isActive = true;
    }

    this.withChangeDetection(() => {
      if (this.isActive) {
        this.textAreaRef.nativeElement.focus();
      } else {
        this.textAreaRef.nativeElement.blur();
      }
    });
  }

  clearTextArea() {
    this.withChangeDetection(() => {
      this.graderForm.get('userSubmittedAnswerAsText')?.setValue('');
      this.textAreaRef.nativeElement.focus();
    });
  }

  private scheduleDeferredInit(): void {
    // Ensure first screen renders, then run all init during idle time.
    this.afterFirstPaint(() => {
      this.runWhenIdle(() => {
        if (this.destroyed) return;

        // Defer mounting the desktop background video (non-LCP) until idle.
        if (!this.isMobile) {
          this.showHeroVideo = true;
          this.withChangeDetection(() => {});
        }

        // Auth + cache reads deferred.
        this.userLoggedIn = this._authService.isLoggedIn();
        this.isLoggedIn = this.userLoggedIn;
        this.loggedInUserPlanType =
          this._cacheService.getJsonData('loggedInUserDetails')
            ?.subscriptionPlanType || '';

        const graderLandingFormData = this._cacheService.getJsonData(
          'graderLandingFormData',
        );

        // Form patching must not block initial render; schedule microtask.
        if (this.userLoggedIn && graderLandingFormData) {
          this.pendingAutoGradeFromCache = true;
          queueMicrotask(() => {
            if (this.destroyed) return;
            this.withChangeDetection(() => {
              this.assessmentName = graderLandingFormData.assessmentName;
              this.className = graderLandingFormData.className;
              this.evaluationCriteria =
                graderLandingFormData.evaluationCriteria;
              this.graderForm.patchValue({
                className: this.className,
                assessmentName: this.assessmentName,
                evaluationCriteria: this.evaluationCriteria,
                userSubmittedAnswerAsText:
                  graderLandingFormData.userSubmittedAnswerAsText,
              });
            });
          });

          // Heavy restore should run only after full paint + idle + >=500ms.
          this.scheduleCacheRestore(graderLandingFormData);
        }

        // Any final UI updates.
        this.withChangeDetection(() => {});
      }, 1500);
    });
  }

  private scheduleCacheRestore(graderLandingFormData: any): void {
    this.runWhenIdle(() => {
      setTimeout(async () => {
        if (this.destroyed) return;
        await this.restoreCachedUploadsInChunks(graderLandingFormData);
        this.triggerAutoGradeAfterRestore();
      }, 500);
    }, 2000);
  }

  private triggerAutoGradeAfterRestore(): void {
    if (
      !this.pendingAutoGradeFromCache ||
      this.destroyed ||
      this.isProcessing ||
      !this.userLoggedIn
    ) {
      return;
    }

    // One-time trigger only after cache restore has completed.
    this.pendingAutoGradeFromCache = false;

    // Yield one microtask so restored form/file state is fully settled.
    queueMicrotask(() => {
      if (this.destroyed || this.isProcessing) {
        return;
      }

      if (!this.uploadedFiles?.length) {
        return;
      }

      void this.gradeNow();
    });
  }

  private computeIsMobile(): boolean {
    // This runs after first paint; safe to read window here.
    if (typeof window === 'undefined') return false;
    return window.innerWidth < 768;
  }

  private runWhenIdle(cb: () => void, timeout = 1000): void {
    this.ngZone.runOutsideAngular(() => {
      const w = window as any;
      if (typeof w.requestIdleCallback === 'function') {
        w.requestIdleCallback(() => this.ngZone.run(() => cb()), { timeout });
      } else {
        setTimeout(() => this.ngZone.run(() => cb()), Math.min(timeout, 1000));
      }
    });
  }

  private afterFirstPaint(cb: () => void): void {
    // rAF -> microtask: reliably after browser has a chance to paint.
    this.ngZone.runOutsideAngular(() => {
      requestAnimationFrame(() => {
        queueMicrotask(() => this.ngZone.run(cb));
      });
    });
  }

  private withChangeDetection(cb: () => void): void {
    // Temporarily reattach for UI updates, then detach again.
    try {
      this.cdr.reattach();
      cb();
      this.cdr.detectChanges();
    } finally {
      this.cdr.detach();
    }
  }

  private async restoreCachedUploadsInChunks(
    graderLandingFormData: any,
  ): Promise<void> {
    try {
      const quizFiles = Array.isArray(graderLandingFormData.quiz_files)
        ? graderLandingFormData.quiz_files
        : [];

      const restored: any[] = [];
      // Chunk base64 decoding to yield back to the main thread.
      for (let i = 0; i < quizFiles.length; i++) {
        if (this.destroyed) return;
        const fw = quizFiles[i];
        const restoredFile = this.base64ToFile(fw.base64, fw.name, fw.type);
        restored.push({
          ...fw,
          file: restoredFile,
          // Cached blob URLs are not valid after navigation/refresh.
          src: URL.createObjectURL(restoredFile),
          // Ensure required template shape exists.
          pages: Array.isArray(fw.pages) && fw.pages.length ? fw.pages : [1],
          name: fw.name,
          type: fw.type,
          gradingUnits: Number(fw.gradingUnits) || 1,
        });
        if (i % 2 === 1) {
          await new Promise((r) => setTimeout(r, 0));
        }
      }

      this.uploadedFiles = restored;

      const answerFile = graderLandingFormData.answer_key_file;
      if (answerFile?.base64) {
        const restoredAnswerFile = this.base64ToFile(
          answerFile.base64,
          answerFile.name,
          answerFile.type,
        );
        this.selectedAnswerFile = {
          ...answerFile,
          file: restoredAnswerFile,
          src: URL.createObjectURL(restoredAnswerFile),
          pages:
            Array.isArray(answerFile.pages) && answerFile.pages.length
              ? answerFile.pages
              : [1],
          name: answerFile.name,
          type: answerFile.type,
        };
        this.selectedFileName = this.selectedAnswerFile?.name;
      }

      this._cacheService.removeFromCache('graderLandingFormData');

      if (this.uploadedFiles.length) {
        this.graderForm.get('userSubmittedAnswerAsText')?.disable();
      }

      this.withChangeDetection(() => {});
    } catch {
      // If restore fails, just continue with empty state (no business logic change).
    }
  }
}
