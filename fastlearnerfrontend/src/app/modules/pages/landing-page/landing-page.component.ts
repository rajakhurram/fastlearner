import { HttpClient } from '@angular/common/http';
import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  HostListener,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import {
  cards,
  instructorCards,
} from 'src/app/core/constants/staticData.constants';
import { CourseTypeMap, ViewAllMap } from 'src/app/core/enums/course-status';
import { Direction } from 'src/app/core/enums/direction.enum';
import { AccordionItems } from 'src/app/core/interfaces/accordian.interafce';
import { buttonConfig } from 'src/app/core/models/button.model-config';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { environment } from 'src/environments/environment.development';
import { StateService } from 'src/app/core/services/state.service';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-landing-page',
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.scss'],
})
export class LandingPageComponent implements OnInit, OnDestroy, AfterViewInit {
  paymentData = {
    cardNumber: '',
    expDate: '',
    cvv: '',
    amount: 0,
  };
  _httpConstants: HttpConstants = new HttpConstants();
  imageUrl = environment.imageUrl;
  categoryList: Array<any> = [];
  hoverCardVisible: boolean = false;
  emptyEmail: boolean = false;
  isLoggedIn: any;
  courseButtonName: string = 'Start Now';
  instructorCards = instructorCards;
  staticCards = cards;
  selectedCategory: any = null;
  heartFilled = '../../../../assets/icons/heart_filled.svg';
  heartUnFilled = '../../../../assets/icons/heart_unfilled.svg';
  fullWidth: boolean;
  mobileView: boolean = false;
  subscribeEmail?: any;
  emailValid?: any = false;
  courseList: Array<any> = [];
  newCourses: Array<any> = [];
  instructorList: Array<any> = [];
  trendingCourses: Array<any> = [];
  tests: Array<any> = [];
  freeCourses: Array<any> = [];
  freeCoursesNextPage = 0;
  premiumCoursesNextPage = 0;
  newCoursesNextPage = 0;
  trendingCoursesNextPage = 0;
  instructorNextPage = 0;
  cardsToShow = 4;
  courseTypeMap = CourseTypeMap;
  viewAllMap = ViewAllMap;
  premiumCourses: Array<any> = [];
  isAcrdionExpanded: boolean = false;
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
    {
      title: 'What is a quick learning ability?',
      description: `A quick learner is a person who can understand new information in a short amount of time. When someone learns quickly, they typically have excellent communication and listening abilities. FastLearner.ai will help you learn quickly by providing innovative AI-based learning tools.
      `,
      isExpanded: false,
    },
    {
      title: 'What are the benefits of fast learning?',
      description: `Quick learning benefits professional development, and professionals who learn quickly tend to be more productive. This is because they can adapt to new situations faster and successfully. The key to learning quickly is having a high level of curiosity and enthusiasm to ask questions when required.
      `,
      isExpanded: false,
    },
  ];
  totalPages?: any;
  currentNewPage: number = 0;
  newCoursePageSize: number = 3;
  totalNewCoursesPages: number = 0;
  totalTestPages: number = 0;
  currentTrendingPage: number = 0;
  trendingPageSize: number = 4;
  totalTrendingCoursesPages: number = 0;
  currentFreePage: number = 0;
  freePageSize: number = 4;
  totalFreeCoursesPages: number = 0;
  currentPremiumPage: number = 0;
  premiumPageSize: number = 4;
  totalPremiumCoursesPages: number = 0;
  totalPremiumCoursesElements: number = 0;
  totalNewCoursesElements: number = 0;
  totalTestElements: number = 0;
  totalTrendingCoursesElements: number = 0;
  totalFreeCoursesElements: number = 0;
  currentInstructorPage: number = 0;
  instructorPageSize: number = 4;
  totalInstructorsPage: number = 0;
  directionEnum = Direction;
  loggedInStatic = [];
  payLoad = {
    categoryId: null,
    pageNo: 0,
    pageSize: 9,
  };
  private sectionObserver?: IntersectionObserver;
  bannerLoaded = true;
  premiumCoursesLoaded = true;
  aboutUsSectionsLoaded = true;
  freeCourseSectionsLoaded = true;
  aboutUsLightSectionsLoaded = true;
  courseSectionsLoaded = true;
  aboutUsEmpwoeringSectionsLoaded = true;
  newCourseSectionsLoaded = true;
  studentPickSectionsLoaded = true;
  faqContainerSectionsLoaded = true;
  baseUrl = environment.basePath;
  sectionId: string = '';
  sliderLoading: Record<string, boolean> = {
    test_center: false,
    about_us: false,
    courses_section: false,
    premium_courses: false,
    free_courses: false,
    new_courses: false,
  };
  @ViewChild('scrollerContent', { static: true }) scrollerContent: ElementRef;

  @ViewChild('banner_container') banner_container!: ElementRef;
  @ViewChild('premium_courses') premium_courses!: ElementRef;
  @ViewChild('about_us') about_us!: ElementRef;
  @ViewChild('free_courses') free_courses!: ElementRef;
  @ViewChild('about_us_bg_light') about_us_bg_light!: ElementRef;
  @ViewChild('courses_section') courses_section!: ElementRef;
  @ViewChild('about_us_empowering') about_us_empowering!: ElementRef;
  @ViewChild('new_courses') new_courses!: ElementRef;
  @ViewChild('student_pick') student_pick!: ElementRef;
  @ViewChild('faq_container') faq_container!: ElementRef;
  @ViewChild('test_center') test_center!: ElementRef;

  sectionsLoaded: { [key: string]: boolean } = {
    banner_container: false,
    premium_courses: false,
    about_us: false,
    free_courses: false,
    about_us_bg_light: false,
    courses_section: false,
    about_us_empowering: false,
    new_courses: false,
    student_pick: false,
    faq_container: false,
    test_center: false,
  };

  private readonly sectionDomOrder = [
    'banner_container',
    'about_us_empowering',
    'test_center',
    'about_us',
    'courses_section',
    'premium_courses',
    'free_courses',
    'about_us_bg_light',
    'new_courses',
    'student_pick',
    'faq_container',
  ];

  private readonly apiSections = new Set([
    'test_center',
    'about_us',
    'courses_section',
    'premium_courses',
    'free_courses',
    'new_courses',
  ]);

  private sectionsPendingForScroll = new Set<string>();
  private scrollRetryTimers: ReturnType<typeof setTimeout>[] = [];

  sectionData: { [key: string]: any } = {};

  scrollLeft() {
    const scroller = this.scrollerContent.nativeElement.parentElement;
    scroller.scrollBy({ left: -200, behavior: 'smooth' });
  }

  scrollRight() {
    const scroller = this.scrollerContent.nativeElement.parentElement;
    scroller.scrollBy({ left: 200, behavior: 'smooth' });
  }

  visibleCourses: any[] = [];
  currentIndex: number = 0;
  visibleCount: number = 6; // Number of items to show in the slider

  updateVisibleCourses() {
    this.visibleCourses = this.categoryList.slice(
      this.currentIndex,
      this.currentIndex + this.visibleCount,
    );
  }

  slideLeft() {
    if (this.currentIndex > 0) {
      this.currentIndex--;
      this.updateVisibleCourses();
    }
  }

  routeToLink(link) {
    window.open(link, '_blank');
  }

  routeToLinkInstructor(path: string) {
    window.location.href = `${this.baseUrl}${path}`;
  }

  slideRight() {
    if (this.currentIndex < this.categoryList.length - this.visibleCount) {
      this.currentIndex++;
      this.updateVisibleCourses();
    }
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.setScreenWidth(event.target.innerWidth);
  }

  constructor(
    private _router: Router,
    private _courseService: CourseService,
    private _authService: AuthService,
    private _messageService: MessageService,
    private _sharedService: SharedService,
    private _cacheService: CacheService,
    private stateService: StateService,
    private metaService: Meta,
    private titleService: Title,
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
  ) {
    this.isUserLoggedIn();
    this.listenNavbarState();
    this.listenRefreshToken();
  }

  ngAfterViewInit(): void {
    this.setupSectionObserver();
  }

  private setupSectionObserver(): void {
    this.sectionObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const sectionId = entry.target.id;
            if (sectionId && !this.sectionsLoaded[sectionId]) {
              this.loadSection(sectionId);
              this.cdr.markForCheck();
            }
          }
        });
      },
      { rootMargin: '300px 0px', threshold: 0.01 },
    );

    Object.keys(this.sectionsLoaded).forEach((sectionId) => {
      const sectionRef = this[sectionId as keyof this] as ElementRef;
      if (sectionRef?.nativeElement) {
        this.sectionObserver?.observe(sectionRef.nativeElement);
      }
    });
  }

  private scrollDone = false;
  private pendingScrollSectionId: string | null = null;

  private tryScrollToPendingSection(): void {
    const targetSectionId = this.pendingScrollSectionId;
    if (!targetSectionId || this.scrollDone) {
      return;
    }

    if (this.sectionsPendingForScroll.size > 0) {
      return;
    }

    this.scrollDone = true;
    this.pendingScrollSectionId = null;
    sessionStorage.removeItem('sectionId');
    this.scheduleScrollToSection(targetSectionId);
  }

  private setSliderLoading(sectionId: string, loading: boolean): void {
    this.sliderLoading[sectionId] = loading;
  }

  private completeSliderLoad(sectionId: string): void {
    this.setSliderLoading(sectionId, false);
    this.markApiSectionReady(sectionId);
  }

  private markApiSectionReady(sectionId: string): void {
    this.sectionsPendingForScroll.delete(sectionId);
    this.tryScrollToPendingSection();
  }

  private scheduleScrollToSection(sectionId: string): void {
    this.clearScrollRetryTimers();

    const scroll = () => this.scrollToSpecificSection(sectionId);
    if (typeof requestAnimationFrame === 'function') {
      requestAnimationFrame(scroll);
    } else {
      scroll();
    }

    [300, 800].forEach((delay) => {
      this.scrollRetryTimers.push(setTimeout(scroll, delay));
    });
  }

  private clearScrollRetryTimers(): void {
    this.scrollRetryTimers.forEach((timer) => clearTimeout(timer));
    this.scrollRetryTimers = [];
  }

  private getScrollOffset(): number {
    const navbar = document.querySelector('.navbar') as HTMLElement | null;
    return navbar?.offsetHeight ?? 80;
  }

  scrollToSpecificSection(sectionId: string) {
    const element = document.getElementById(sectionId);
    if (!element) {
      return;
    }

    const top =
      element.getBoundingClientRect().top +
      window.scrollY -
      this.getScrollOffset();
    window.scrollTo({ top, behavior: 'auto' });
  }

  private initPendingScroll(targetSectionId: string): void {
    const targetIndex = this.sectionDomOrder.indexOf(targetSectionId);
    if (targetIndex < 0) {
      return;
    }

    for (let i = 0; i <= targetIndex; i++) {
      const sectionId = this.sectionDomOrder[i];
      if (this.apiSections.has(sectionId)) {
        this.sectionsPendingForScroll.add(sectionId);
      }
      this.loadSection(sectionId);
    }
  }

  loadSection(sectionId: string) {
    if (this.sectionsLoaded[sectionId]) {
      return;
    }
    this.sectionsLoaded[sectionId] = true;
    this.fetchSectionData(sectionId);
  }

  private fetchSectionData(sectionId: string) {
    switch (sectionId) {
      case 'premium_courses':
        this.getPremiumCourses(this.directionEnum.INITIAL);
        break;
      case 'about_us':
        this.getTrendingCourses(this.directionEnum.INITIAL);
        break;
      case 'free_courses':
        this.getFreeCourses(this.directionEnum.INITIAL);
        break;
      case 'courses_section':
        if (!this.categoryList.length) {
          this.getCategoryList();
        }
        this.getCourseListByCategory();
        break;
      case 'new_courses':
        this.getNewCourses(this.directionEnum.INITIAL);
        break;
      case 'test_center':
        this.getTest(this.directionEnum.INITIAL);
        break;
      default:
        break;
    }
  }

  private refreshLoadedSections() {
    Object.entries(this.sectionsLoaded).forEach(([sectionId, loaded]) => {
      if (loaded) {
        this.fetchSectionData(sectionId);
      }
    });
  }

  // loadBannerContent() {
  //   this.bannerLoaded = true;
  // }

  // loadPremiumCourses() {
  //   this.premiumCoursesLoaded = true;
  // }

  // loadAboutUs() {
  //   this.aboutUsSectionsLoaded = true;
  // }

  // loadFreeCourses() {
  //   this.freeCourseSectionsLoaded = true;
  // }

  // loadAboutUsLight() {
  //   this.aboutUsLightSectionsLoaded = true;
  // }

  // loadCourseSection() {
  //   this.courseSectionsLoaded = true;
  // }

  // loadAboutUsEmpowering() {
  //   this.aboutUsEmpwoeringSectionsLoaded = true;
  // }

  // loadNewCourses() {
  //   this.newCourseSectionsLoaded = true;
  // }

  // loadStudentPick() {
  //   this.studentPickSectionsLoaded = true;
  // }

  // loadFAQContainer() {
  //   this.faqContainerSectionsLoaded = true;
  // }

  checkStateManagement() {
    const redirectUrl = this._cacheService.getDataFromCache('redirectUrl');
    if (redirectUrl) {
      this._cacheService.removeFromCache('redirectUrl');
      this._router.navigateByUrl(redirectUrl);
    }
  }

  expand(faq, event) {
    const isExpand =
      event.currentTarget.nextSibling.classList.contains('expanded');
    if (isExpand) {
      event.currentTarget.nextSibling.classList.remove('expanded');
      faq.isExpanded = false;
    } else {
      event.currentTarget.nextSibling.classList.add('expanded');
      faq.isExpanded = true;
    }
  }

  isUserLoggedIn() {
    this.isLoggedIn = this._authService.isLoggedIn();
    this.courseButtonName = this._authService.isLoggedIn()
      ? 'Start Learning'
      : 'Start Now';
  }
  routeToSignUpScreen() {
    this._router.navigate(['auth/sign-up']);
  }

  showHoverCard() {
    this.hoverCardVisible = true;
  }

  listenNavbarState() {
    this._authService.$changeNavbarSate.subscribe(() => {
      this.isLoggedIn = this._authService.isLoggedIn();
      this.courseButtonName = this.isLoggedIn ? 'Start Learning' : 'Start Now';
    });
  }

  listenRefreshToken() {
    this._authService.$getCategoriesAndCourse.subscribe((state: any) => {
      if (state !== null && state !== undefined) {
        this.refreshLoadedSections();
      }
    });
  }

  scrollToBottom() {
    window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
  }

  hideHoverCard() {
    this.hoverCardVisible = false;
  }

  ngOnInit(): void {
    this.metaService.updateTag({
      name: 'description',
      content: `Join Fast Learner’s AI based learning platform for quick learning. Access top courses from experts, boost your skills with AI assistance & become a fast learner`,
    });
    this.titleService.setTitle(
      'AI based Learning Platform Transforming Education | Fast Learner',
    );
    this.setScreenWidth(window.innerWidth);
    this.loggedInStatic = this.staticCards.slice(0, -1);

    this.pendingScrollSectionId = sessionStorage.getItem('sectionId');
    if (
      this.pendingScrollSectionId &&
      this.pendingScrollSectionId in this.sectionsLoaded
    ) {
      this.initPendingScroll(this.pendingScrollSectionId);
    }
  }

  ngOnDestroy(): void {
    this.clearScrollRetryTimers();
    this.sectionObserver?.disconnect();
    this.metaService.removeTag("name='Home'");
  }

  onSelectCategory(categoryId: any) {
    this.selectedCategory = categoryId;
    this.payLoad.categoryId = categoryId;
    this.getCourseListByCategory();
  }

  startCourse(courseUrl: any) {
    if (this.isLoggedIn) {
      this._router.navigate(['student/course-details', courseUrl]);
    } else {
      this._cacheService.saveInCache(
        'redirectUrl',
        'student/course-details/' + courseUrl,
      );
      this._router.navigate(['auth/sign-in']);
    }
  }

  routeToCourseDetails(courseUrl: any) {
    this._router.navigate(['student/course-details', courseUrl]);
  }

  routeToCourseDetailsContent(courseUrl: any) {
    this._router.navigate(['student/course-details', courseUrl], {
      fragment: 'course-content',
    });
  }

  routeToCourseList(selection?: string, sectionId?: string) {
    if (sectionId) {
      sessionStorage.setItem('sectionId', sectionId);
    }
    if (selection === 'TEST') {
      this._router.navigate(['student/courses'], {
        queryParams: { contentType: 'TEST' },
      });
      return;
    }

    this._router.navigate(['student/courses'], {
      queryParams: {
        selection: selection,
      },
    });
  }

  routeToInsructorProfile(profileUrl?: any) {
    this._router.navigate(['user/profile'], {
      queryParams: { url: profileUrl },
    });
  }

  handleStartNowClick() {
    if (this.isLoggedIn) {
      this.routeToCourseList(this.viewAllMap.CATEGORY);
    } else {
      this._cacheService.saveInCache('redirectUrl', '/student/courses');
      this.routeToSignUpScreen();
    }
  }

  getCategoryList() {
    this._courseService.getCourseCategory().subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.categoryList = response?.data;
          this.updateVisibleCourses();
        }
      },
      error: (error: any) => {},
    });
  }

  validateEmail(event?: any) {
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    const email = event.target.value;

    if (email.length > 255) {
      this.emailValid = false;
    } else {
      const valid = emailPattern.test(email);
      this.emailValid = valid;
    }
  }

  subscribeNewsLetter() {
    if (!this.subscribeEmail) {
      this.emptyEmail = true;
      return;
    }
    if (this.emailValid) {
      this._sharedService.subscribeNewsLetter(this.subscribeEmail).subscribe({
        next: (response: any) => {
          this.subscribeEmail = '';
          this.emptyEmail = false;
          this._messageService.success(response?.message);
        },
        error: (error: any) => {
          this.subscribeEmail = '';
          // this._messageService.error(error?.error?.message);
        },
      });
    }
  }

  getCourseListByCategory() {
    const prioritizedCreatorIds = [40, 271, 165, 137];
    this.setSliderLoading('courses_section', true);
    this._courseService
      .getCoursesByCategory(this.payLoad)
      .pipe(finalize(() => this.completeSliderLoad('courses_section')))
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.totalPages = response?.data?.pages;
            this.courseList = response?.data?.data;

            const prioritizedCourses = response.data.data.filter((e) =>
              prioritizedCreatorIds.includes(e.creatorId),
            );

            prioritizedCourses.sort((a, b) => {
              if (a.creatorId === 40) return -1;
              if (b.creatorId === 40) return 1;
              return 0;
            });

            const otherCourses = response.data.data.filter(
              (e) => !prioritizedCreatorIds.includes(e.creatorId),
            );

            this.courseList = [...prioritizedCourses, ...otherCourses];

            this.courseList?.forEach((element) => {
              element.courseDuration = this.convertSecondsToHoursAndMinutes(
                element.courseDuration,
              );
            });
          } else if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
          ) {
            this.courseList = [];
          }
        },
        error: (error: any) => {
          if (
            error?.error?.status ==
            this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
          ) {
            this.courseList = [];
          }
        },
      });
  }

  getNewCourses(direction?: string) {
    // If total pages haven't been loaded yet, return early
    if (
      this.totalNewCoursesPages === 0 &&
      direction !== this.directionEnum.INITIAL
    ) {
      return; // Prevents moving left or right until data isthis.directionEnum.INITIALzed
    }

    // Update pageNo based on the direction (left or right)
    if (
      direction === this.directionEnum.RIGHT &&
      this.currentNewPage < this.totalNewCoursesPages
    ) {
      this.currentNewPage++;
    } else if (
      direction === this.directionEnum.LEFT &&
      this.currentNewPage > 0
    ) {
      this.currentNewPage--;
    } else if (direction === this.directionEnum.INITIAL) {
      this.currentNewPage = 0; // Set to page 1 onthis.directionEnum.INITIALload
    }

    const payload = {
      pageNo: this.currentNewPage,
      pageSize: 16,
    };

    this.setSliderLoading('new_courses', true);
    this._courseService
      .getNewCourses(payload)
      .pipe(finalize(() => this.completeSliderLoad('new_courses')))
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            // if (response?.data?.nextPage == null) {
            //   return;
            // }
            const newCourses = response?.data?.data || [];
            this.newCourses = [...newCourses];
            this.newCoursesNextPage = response?.data?.nextPage;
            this.totalNewCoursesPages = response?.data?.pages;
            this.totalNewCoursesElements = response?.data?.totalElements; // Set the total number of pages based on the response

            // Convert course durations to hours and minutes
            this.newCourses?.forEach((element) => {
              element.courseDuration = this.convertSecondsToHoursAndMinutes(
                element.courseDuration,
              );
            });
          } else if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
          ) {
            this.newCourses = [];
          }
        },
        error: (error: any) => {
          if (
            error?.error?.status ===
            this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
          ) {
            this.newCourses = [];
          }
        },
      });
  }

  getTest(direction?: string) {
    // If total pages haven't been loaded yet, return early
    if (this.totalTestPages === 0 && direction !== this.directionEnum.INITIAL) {
      return; // Prevents moving left or right until data isthis.directionEnum.INITIALzed
    }

    // Update pageNo based on the direction (left or right)
    if (
      direction === this.directionEnum.RIGHT &&
      this.currentNewPage < this.totalTestPages
    ) {
      this.currentNewPage++;
    } else if (
      direction === this.directionEnum.LEFT &&
      this.currentNewPage > 0
    ) {
      this.currentNewPage--;
    } else if (direction === this.directionEnum.INITIAL) {
      this.currentNewPage = 0; // Set to page 1 onthis.directionEnum.INITIALload
    }

    const payload = {
      pageNo: this.currentNewPage,
      pageSize: 16,
    };

    this.setSliderLoading('test_center', true);
    this._courseService
      .getTest(payload)
      .pipe(finalize(() => this.completeSliderLoad('test_center')))
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            // if (response?.data?.nextPage == null) {
            //   return;
            // }
            const tests = response?.data?.data || [];
            this.tests = [...tests];
            this.newCoursesNextPage = response?.data?.nextPage;
            this.totalTestPages = response?.data?.pages;
            this.totalTestElements = response?.data?.totalElements; // Set the total number of pages based on the response

            // Convert course durations to hours and minutes
            this.tests?.forEach((element) => {
              element.courseDuration = this.convertSecondsToHoursAndMinutes(
                element.courseDuration,
              );
            });
          } else if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
          ) {
            this.tests = [];
          }
        },
        error: (error: any) => {
          if (
            error?.error?.status ===
            this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
          ) {
            this.newCourses = [];
          }
        },
      });
  }
  getInstructors(direction?: string) {
    // If total pages haven't been loaded yet, return early
    if (
      this.totalInstructorsPage === 0 &&
      direction !== this.directionEnum.INITIAL
    ) {
      return; // Prevents moving left or right until data isthis.directionEnum.INITIALzed
    }

    // Update pageNo based on the direction (left or right)
    if (
      direction === this.directionEnum.RIGHT &&
      this.currentInstructorPage < this.totalInstructorsPage
    ) {
      this.currentInstructorPage++;
    } else if (
      direction === this.directionEnum.LEFT &&
      this.currentInstructorPage > 0
    ) {
      this.currentInstructorPage--;
    } else if (direction === this.directionEnum.INITIAL) {
      this.currentInstructorPage = 0; // Set to page 1 onthis.directionEnum.INITIALload
    }

    const payload = {
      pageNo: this.currentInstructorPage,
      pageSize: this.instructorPageSize,
    };

    this._courseService.getInstructors(payload).subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          if (response?.data?.nextPage == null) {
            return;
          }
          this.instructorList = response?.data?.data;
          this.instructorNextPage = response?.data?.nextPage;
          this.totalInstructorsPage = response?.data?.pages; // Set the total number of pages based on the response

          // Convert course durations to hours and minutes
          this.instructorList?.forEach((element) => {
            element.courseDuration = this.convertSecondsToHoursAndMinutes(
              element.courseDuration,
            );
          });
        } else if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
        ) {
          this.instructorList = [];
        }
      },
      error: (error: any) => {
        if (
          error?.error?.status ===
          this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
        ) {
          this.instructorList = [];
        }
      },
    });
  }
  getTrendingCourses(direction?: string) {
    // If total pages haven't been loaded yet, return early
    if (
      this.totalTrendingCoursesPages === 0 &&
      direction !== this.directionEnum.INITIAL
    ) {
      return; // Prevents moving left or right until data isthis.directionEnum.INITIALzed
    }

    // Update pageNo based on the direction (left or right)
    if (
      direction === this.directionEnum.RIGHT &&
      this.currentTrendingPage < this.totalTrendingCoursesPages
    ) {
      this.currentTrendingPage++;
    } else if (
      direction === this.directionEnum.LEFT &&
      this.currentTrendingPage > 0
    ) {
      this.currentTrendingPage--;
    } else if (direction === this.directionEnum.INITIAL) {
      this.currentTrendingPage = 0; // Set to page 1 onthis.directionEnum.INITIALload
    }

    const payload = {
      pageNo: this.currentTrendingPage,
      pageSize: 16,
    };

    this.setSliderLoading('about_us', true);
    this._courseService
      .getTrendingCourses(payload)
      .pipe(finalize(() => this.completeSliderLoad('about_us')))
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            // if (response?.data?.nextPage == null) {
            //   return;
            // }
            const newTrendingCourses = response?.data?.data || [];
            this.trendingCourses = [...newTrendingCourses];
            this.trendingCoursesNextPage = response?.data?.nextPage;
            this.totalTrendingCoursesPages = response?.data?.pages;
            this.totalTrendingCoursesElements = response?.data?.totalElements;
            this.trendingCourses?.forEach((element) => {
              element.courseDuration = this.convertSecondsToHoursAndMinutes(
                element.courseDuration,
              );
            });
          } else if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
          ) {
            this.trendingCourses = [];
          }
        },
        error: (error: any) => {
          if (
            error?.error?.status ===
            this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
          ) {
            this.trendingCourses = [];
          }
        },
      });
  }
  getFreeCourses(direction?: string) {
    // If total pages haven't been loaded yet, return early
    if (
      this.totalFreeCoursesPages === 0 &&
      direction !== this.directionEnum.INITIAL
    ) {
      return; // Prevents moving left or right until data isthis.directionEnum.INITIALzed
    }

    // Update pageNo based on the direction (left or right)
    if (
      direction === this.directionEnum.RIGHT &&
      this.currentFreePage < this.totalFreeCoursesPages
    ) {
      this.currentFreePage++;
    } else if (
      direction === this.directionEnum.LEFT &&
      this.currentFreePage > 0
    ) {
      this.currentFreePage--;
    } else if (direction === this.directionEnum.INITIAL) {
      this.currentFreePage = 0; // Set to page 1 onthis.directionEnum.INITIALload
    }

    const payload = {
      pageNo: this.currentFreePage,
      pageSize: 16,
    };

    this.setSliderLoading('free_courses', true);
    this._courseService
      .getFreeCourses(payload)
      .pipe(finalize(() => this.completeSliderLoad('free_courses')))
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            // if (response?.data?.nextPage == null) {
            //   return;
            // }
            const newFreeCourses = response?.data?.data || [];

            this.freeCourses = [...newFreeCourses];
            this.freeCoursesNextPage = response?.data?.nextPage;
            this.totalFreeCoursesPages = response?.data?.pages;
            this.totalFreeCoursesElements = response?.data?.totalElements;
            this.freeCourses?.forEach((element) => {
              element.courseDuration = this.convertSecondsToHoursAndMinutes(
                element.courseDuration,
              );
            });
          } else if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
          ) {
            this.freeCourses = [];
          }
        },
        error: (error: any) => {
          if (
            error?.error?.status ===
            this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
          ) {
            this.freeCourses = [];
          }
        },
      });
  }
  // getPremiumCourses(direction?: string) {
  //   // If total pages haven't been loaded yet, return early
  //   if (
  //     this.totalPremiumCoursesPages === 0 &&
  //     direction !== this.directionEnum.INITIAL
  //   ) {
  //     return; // Prevents moving left or right until data isthis.directionEnum.INITIALzed
  //   }

  //   // Update pageNo based on the direction (left or right)
  //   if (
  //     direction === this.directionEnum.RIGHT &&
  //     this.currentPremiumPage < this.totalPremiumCoursesPages
  //   ) {
  //     this.currentPremiumPage++;
  //   } else if (
  //     direction === this.directionEnum.LEFT &&
  //     this.currentPremiumPage > 0
  //   ) {
  //     this.currentPremiumPage--;
  //   } else if (direction === this.directionEnum.INITIAL) {
  //     this.currentPremiumPage = 0; // Set to page 1 onthis.directionEnum.INITIALload
  //   }

  //   const payload = {
  //     pageNo: this.currentPremiumPage,
  //     pageSize: this.premiumCourses.length ? 1 : this.premiumPageSize,
  //   };

  //   this._courseService.getPremiumCourses(payload).subscribe({
  //     next: (response: any) => {
  //       if (
  //         response?.status ===
  //         this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
  //       ) {
  //         if (
  //           response?.data?.nextPage == null &&
  //           response.data?.totalElements > 4
  //         ) {
  //           return;
  //         }
  //         this.premiumCourses = response?.data?.data;
  //         this.premiumCoursesNextPage = response?.data?.nextPage;
  //         this.totalPremiumCoursesPages = response?.data?.pages; // Set the total number of pages based on the response
  //         this.totalPremiumCoursesElements = response?.data?.totalElements;

  //         // Convert course durations to hours and minutes
  //         this.premiumCourses?.forEach((element) => {
  //           element.courseDuration = this.convertSecondsToHoursAndMinutes(
  //             element.courseDuration
  //           );
  //         });
  //       } else if (
  //         response?.status ===
  //         this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
  //       ) {
  //         this.premiumCourses = [];
  //       }
  //     },
  //     error: (error: any) => {
  //       if (
  //         error?.error?.status ===
  //         this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
  //       ) {
  //         this.premiumCourses = [];
  //       }
  //     },
  //   });
  // }
  getPremiumCourses(direction?: string) {
    // Prevent loading more data if there are no more pages and not the initial load
    if (
      this.totalPremiumCoursesPages === 0 &&
      direction !== this.directionEnum.INITIAL
    ) {
      return;
    }

    // Update pageNo based on the direction (left or right)
    if (
      direction === this.directionEnum.RIGHT &&
      this.currentPremiumPage < this.totalPremiumCoursesPages
    ) {
      this.currentPremiumPage++;
    } else if (
      direction === this.directionEnum.LEFT &&
      this.currentPremiumPage > 0
    ) {
      this.currentPremiumPage--;
    } else if (direction === this.directionEnum.INITIAL) {
      this.currentPremiumPage = 0;
    }

    const payload = {
      pageNo: 0,
      pageSize: 16,
    };

    this.setSliderLoading('premium_courses', true);
    this._courseService
      .getPremiumCourses(payload)
      .pipe(finalize(() => this.completeSliderLoad('premium_courses')))
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            const newCourses = response?.data?.data || [];

            // if (
            //   response?.data?.nextPage == null &&
            //   response.data?.totalElements > 4
            // ) {
            //   return;
            // }

            // Append new courses if they exist, otherwise keep current courses
            this.premiumCourses = [...newCourses];
            this.premiumCoursesNextPage = response?.data?.nextPage;
            this.totalPremiumCoursesPages = response?.data?.pages;
            this.totalPremiumCoursesElements = response?.data?.totalElements;

            // Convert course durations to hours and minutes
            this.premiumCourses?.forEach((element) => {
              element.courseDuration = this.convertSecondsToHoursAndMinutes(
                element.courseDuration,
              );
            });
          } else if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
          ) {
            this.premiumCourses = [];
          }
        },
        error: (error: any) => {
          if (
            error?.error?.status ===
            this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
          ) {
            this.premiumCourses = [];
          }
        },
      });
  }

  toggleFavoriteCourse(courseId: any, isFavorite: boolean) {
    this._courseService.addOrRemoveCourseToFavorite(courseId).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          let course = this.courseList.find(
            (x: any) => x?.courseId == courseId,
          );
          if (course) {
            course.favourite = !isFavorite;
          }
          this._sharedService.updateFavCourseMenu();
          // this._messageService.success(response?.message);
        }
      },
      error: (error: any) => {},
    });
  }

  routeToAboutUs() {
    this._router.navigate(['about-us']);
  }

  routeToInstructorWelcomePage() {
    this._router.navigate(['welcome-instructor']);
  }

  convertSecondsToHoursAndMinutes(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);

    if (hours === 0) {
      return `${minutes} minutes`;
    } else if (minutes === 0) {
      return `${hours} ${hours === 1 ? 'hour' : 'hours'}`;
    } else {
      return `${hours} ${hours === 1 ? 'hour' : 'hours'} ${minutes} minutes`;
    }
  }

  setCardsToShow(visbileSize) {
    this.cardsToShow = visbileSize;
    this.freePageSize = visbileSize;
    this.newCoursePageSize = visbileSize;
    this.trendingPageSize = visbileSize;
    this.premiumPageSize = visbileSize;
  }

  setScreenWidth(screenWidth: number) {
    if (screenWidth > 768) {
      this.fullWidth = true;
      this.mobileView = false;
    } else {
      this.fullWidth = false;
      this.mobileView = true;
    }

    if (screenWidth > 1750) {
      this.setCardsToShow(4);
    } else if (screenWidth <= 1745 && screenWidth >= 1100) {
      this.setCardsToShow(3);
    } else if (screenWidth <= 1099 && screenWidth >= 650) {
      this.setCardsToShow(2);
    } else if (screenWidth <= 650 && screenWidth >= 100) {
      this.setCardsToShow(1);
    }
  }

  // scrollToCourseSection() {
  //   document.getElementById('courses-section').scrollIntoView();
  // }
}
