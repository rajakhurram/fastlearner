import { Component, ElementRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import {
  aiGraderCards,
  cards,
} from 'src/app/core/constants/staticData.constants';
import { AccordionItems } from 'src/app/core/interfaces/accordian.interafce';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';

@Component({
  selector: 'app-ai-grader-landing-page',
  templateUrl: './ai-grader-landing-page.component.html',
  styleUrls: ['./ai-grader-landing-page.component.scss'],
})
export class AiGraderLandingPageComponent {
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

  constructor(
    private _router: Router,
    private aiGraderService: AiGraderService,
    private router: Router,
    private _authService: AuthService,
    private _cacheService: CacheService
  ) {}
  groupedLogos: string[][] = [];

  loggedInStatic = [];
  staticCards = aiGraderCards;
  isAcrdionExpanded: boolean = false;
  faqContainerSectionsLoaded = true;

  items: AccordionItems[] = [
    {
      title:
        'What makes Fast Learner different from the other online learning platforms?',
      description: `FastLearner differs from other online learning platforms due to its innovative features, such as AI-powered Q&A for swift answers, customizable learning paths for a tailored experience, and video summaries for quick review.
      `,
      isExpanded: false,
    },
    {
      title: 'Is Fast Learner boring like traditional textbook learning?',
      description: `Not at all! Fast Learner provides bite-sized lessons, remote education prospects, interactive content (think videos and quizzes!), and customizing courses by mixing and matching instructors to make the learning experience engaging. So you can improve your skills without feeling stuck at any point.
      `,
      isExpanded: false,
    },
    {
      title: 'How much does Fast Learner cost?',
      description: `Fast Learner's digital AI learning platform operates on a subscription model. For a single monthly fee, you can enjoy personalized learning and unlimited access to our complete course library.
      `,
      isExpanded: false,
    },
    {
      title: 'Can I learn at my own pace with Fast Learner?',
      description: `Yes! Fast Learner prioritizes personal and professional development with flexible learning. You can take courses anytime, anywhere, and alter the speed to suit your schedule.
      `,
      isExpanded: false,
    },
    {
      title: 'Do I get any certification upon finishing a course?',
      description: `Absolutely! Fast Learner awards certificates upon successful course completion. These certificates display your accomplishments of skill development and devotion to professional growth.
      `,
      isExpanded: false,
    },
    {
      title: 'How do I start with Fast Learner?',
      description: `Simply create an account on our digital learning platform and discover a vast course library! Many courses provide free previews, and you can subscribe whenever you are ready to unlock the complete learning experience.
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
      this._cacheService.removeFromCache('graderLandingFormData');
    }

    this.autoPlayLogos();
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
          base64: null,
        });
      }
    }
  }
  loadFAQContainer() {
    console.log('🔥 loadFAQContainer() called!');
    this.faqContainerSectionsLoaded = true;
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
    } else {
      this.selectedFileName = 'No file chosen';
      this.selectedAnswerFile = null;
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
    if(this.userLoggedIn){
      this._router.navigate(['instructor/ai-grader/uploader']);
    }else {
      this._cacheService.saveInCache('redirectUrl', 'instructor/ai-grader/uploader');
      this.router.navigate(['/auth/sign-in'])
    }
  }

  async gradeNow(): Promise<void> {
    if (!this.evaluationCriteria || this.uploadedFiles.length === 0) return;

    if (this.userLoggedIn) {
      const formData = new FormData();

      this.uploadedFiles.forEach((fileWrapper: any) => {
        formData.append('quiz_files', fileWrapper.file);
      });

      if (this.selectedAnswerFile) {
        formData.append('answer_key_file', this.selectedAnswerFile.file);
      }
      formData.append('assessmentName', this.assessmentName);
      formData.append('evaluationCriteria', this.evaluationCriteria);
      formData.append('className', this.className);

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
          // console.error('Grading failed:', err);
          // alert('Failed to start grading. Check logs for details.');
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
      assessmentName: this.assessmentName,
      className: this.className,
      evaluationCriteria: this.evaluationCriteria,
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
}
