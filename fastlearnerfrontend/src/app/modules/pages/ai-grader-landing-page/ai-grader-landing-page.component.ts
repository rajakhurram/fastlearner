import { Component, ElementRef, OnChanges, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import {
  aiGraderCards,
  cards,
} from 'src/app/core/constants/staticData.constants';
import { AccordionItems } from 'src/app/core/interfaces/accordian.interafce';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';

@Component({
  selector: 'app-ai-grader-landing-page',
  templateUrl: './ai-grader-landing-page.component.html',
  styleUrls: ['./ai-grader-landing-page.component.scss'],
})
export class AiGraderLandingPageComponent implements OnInit, OnDestroy {
  @ViewChild('textAreaRef') textAreaRef!: ElementRef;

  scheduleDemo() {
    throw new Error('Method not implemented.');
  }

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
    'assets/icons/01.svg',
    'assets/icons/02.svg',
    'assets/icons/03.svg',
    'assets/icons/04.svg',
    'assets/icons/05.svg',
    'assets/icons/06.svg',
    'assets/icons/07.svg',
    'assets/icons/08.svg',
    'assets/icons/09.svg',
  ];
  @ViewChild('logoCarousel') logoCarousel!: ElementRef;
  @ViewChild('faq_container') faq_container!: ElementRef;
  @ViewChild('carousel') carousel!: ElementRef;
  filePreviewUrl: string | null = null;

  isProcessing = false;
  autoPlayTimeout: any;
  userLoggedIn?: boolean = false;

  graderForm = new FormGroup({
    className: new FormControl<string>(null, [Validators.required]),
    assessmentName: new FormControl<string>(null, [Validators.required]),
    evaluationCriteria: new FormControl<string>(null, [Validators.required]),
    userSubmittedAnswerAsText: new FormControl<string>(null, [Validators.maxLength(12000)]),
  })

  constructor(
    private _router: Router,
    private _messageService: MessageService,
    private aiGraderService: AiGraderService,
    private router: Router,
    private _authService: AuthService,
    private _cacheService: CacheService,
  ) { }
  groupedLogos: string[][] = [];

  loggedInStatic = [];
  staticCards = aiGraderCards;
  isAcrdionExpanded: boolean = false;
  faqContainerSectionsLoaded = true;

  isActive: boolean = false;

  items: AccordionItems[] = [
    {
      title: 'What is an AI Grader?',
      description: `An AI Grader is a tool that uses artificial intelligence to score student work, such as exams and assignments, automatically. Quality Center’s AI grading tool helps teachers save time and ensures consistent results.
      `,
      isExpanded: false,
    },
    {
      title: 'Which AI is best for grading?',
      description: `The best AI grading tools combine accuracy, speed, and ease of use. Quality Center’s AI Grader offers a seamless experience for teachers: simply upload your students’ work, and the AI grading system will analyze and score each paper using advanced algorithms.
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
      description: `AI grading tools like Quality Center can grade assignments in seconds, much faster than manual grading. This means teachers get results and feedback almost instantly.
      `,
      isExpanded: false,
    },
  ];

  ngOnInit() {
    this.userLoggedIn = this._authService.isLoggedIn();
    const graderLandingFormData = this._cacheService.getJsonData(
      'graderLandingFormData'
    );

    if (this.userLoggedIn && graderLandingFormData) {
      this.uploadedFiles = graderLandingFormData.quiz_files.map(
        (fileWrapper: any) => ({
          ...fileWrapper,
          file: this.base64ToFile(
            fileWrapper.base64,
            fileWrapper.name,
            fileWrapper.type
          ),
        })
      );

      const answerFile = graderLandingFormData.answer_key_file;
      if (answerFile?.base64) {
        this.selectedAnswerFile = {
          ...answerFile,
          file: this.base64ToFile(
            answerFile.base64,
            answerFile.name,
            answerFile.type
          ),
        };
      }

      this.selectedFileName = this.selectedAnswerFile?.name;
      this.assessmentName = graderLandingFormData.assessmentName;
      this.className = graderLandingFormData.className;
      this.evaluationCriteria = graderLandingFormData.evaluationCriteria;
      this.graderForm.patchValue({
        className: this.className,
        assessmentName: this.assessmentName,
        evaluationCriteria: this.evaluationCriteria,
        userSubmittedAnswerAsText: graderLandingFormData.userSubmittedAnswerAsText
      });
      this._cacheService.removeFromCache('graderLandingFormData');
    }

    this.autoPlayLogos();

    if (this.uploadedFiles.length) {
      this.graderForm.get('userSubmittedAnswerAsText').disable();
    }
  }

  ngOnDestroy() {
    clearTimeout(this.autoPlayTimeout);
  }

  onPdfLoaded(pdf: any, file: any) {
    file.numPages = pdf.numPages;
    file.pages = Array.from({ length: pdf.numPages }, (_, i) => i + 1);
  }

  resetUpload() {
    this.uploadedFiles = [];
    this.graderForm.get('userSubmittedAnswerAsText').enable();
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

        if (this.uploadedFiles.length) {
          this.graderForm.get('userSubmittedAnswerAsText')?.disable();
        }
      } else {
        this._messageService?.error?.(`${file.name} is not a PDF file.`);
      }
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
  }


  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.isDragging = true;
  }

  onDragLeave() {
    this.isDragging = false;
  }

  onAnswerFileSelected(event: any): void {
    const file: File = event.target.files[0];

    if (file && file.type === 'application/pdf') {
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
    } else {
      this.selectedFileName = 'No file chosen';
      this.selectedAnswerFile = null;
      this.graderForm.get('evaluationCriteria')?.setValidators([Validators.required]);
      this.graderForm.get('evaluationCriteria')?.updateValueAndValidity();
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
        'instructor/ai-grader/uploader'
      );
      this.router.navigate(['/auth/sign-in']);
    }
  }

  async gradeNow(): Promise<void> {
    if (this.userLoggedIn) {
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
      // flaging the processing state
      this.isProcessing = true;

      this.aiGraderService.startGradingLandingPage(formData).subscribe({
        next: (res) => {
          this.isProcessing = false;
          this.router.navigate(['instructor/ai-grader/results'], {
            queryParams: {
              id: res.data.assessmentId,
              classId: res.data.classId,
            },
          });
        },
        error: (err) => {
          this.isProcessing = false;
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
        graderLandingFormData
      );
      this.router.navigate(['/auth/sign-in']);
    }
  }

  async prepareGraderLandingFormData(): Promise<any> {
    const quizFilesWithBase64 = await Promise.all(
      this.uploadedFiles.map(async (fileWrapper: any) => ({
        ...fileWrapper,
        base64: await this.convertToBase64(fileWrapper.file), // ✅ FIXED
      }))
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

    this.autoPlayTimeout = setTimeout(() => {
      carousel.scrollLeft += carousel?.offsetWidth;
      this.autoPlay();
    }, 2500);
  }

  autoPlayLogos() {
    const carousel = this.logoCarousel?.nativeElement;
    if (!carousel) return;

    const maxScrollLeft = carousel.scrollWidth - carousel.offsetWidth;

    if (carousel.scrollLeft >= maxScrollLeft) {
      carousel.scrollLeft = 0; // 🔁 loop to start
    } else {
      carousel.scrollBy({
        left: 200, // adjust scroll step
        behavior: 'smooth',
      });
    }

    this.autoPlayTimeout = setTimeout(() => {
      this.autoPlayLogos();
    }, 2500);
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

    if (this.isActive) {
      this.textAreaRef.nativeElement.focus();
    } else {
      this.textAreaRef.nativeElement.blur();
    }
  }

  clearTextArea() {
    this.graderForm.get('userSubmittedAnswerAsText')?.setValue('');
    this.textAreaRef.nativeElement.focus();
  }
}
