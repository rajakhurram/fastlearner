import { HttpClient } from '@angular/common/http';
import {
  AfterViewInit,
  Component,
  ElementRef,
  HostListener,
  OnDestroy,
  OnInit,
  ViewChild,
  SimpleChanges,
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
import { LazyLoadDirective } from '../../directives/lazy-load.directive';

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
  };

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
      this.currentIndex + this.visibleCount
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
    private metaService: Meta,
    private titleService: Title,
    private http: HttpClient,
    private el: ElementRef
  ) {
    this.isUserLoggedIn();
    this.listenNavbarState();
    this.listenRefreshToken();
  }

  ngAfterViewInit(): void {
    // const directive = new LazyLoadDirective(this.el);
    // directive.ngAfterViewInit();

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const sectionId = entry.target.id;
            if (!this.sectionsLoaded[sectionId]) {
              this.loadSection(sectionId);
            }
          }
        });
      },
      { threshold: 0.5 }
    );

    // Observe all sections dynamically
    Object.keys(this.sectionsLoaded).forEach((sectionId) => {
      const sectionRef = this[sectionId as keyof this] as ElementRef;
      if (sectionRef) {
        observer.observe(sectionRef.nativeElement);
      }
    });
  }

  loadSection(sectionId: string) {
    this.sectionsLoaded[sectionId] = true;
    if (sectionId === 'premium_courses') {
      this.getPremiumCourses(this.directionEnum.INITIAL);
    } else if (sectionId === 'about_us') {
      this.getTrendingCourses(this.directionEnum.INITIAL);
    } else if (sectionId === 'free_courses') {
      this.getFreeCourses(this.directionEnum.INITIAL);
    } else if (sectionId === 'courses_section') {
      this.getCourseListByCategory();
    } else if (sectionId === 'new_courses') {
      this.getNewCourses(this.directionEnum.INITIAL);
    }
  }

  loadBannerContent() {
    console.log('🔥 loadBannerContent() called!');
    this.bannerLoaded = true;
  }
  
  loadPremiumCourses() {
    console.log('🔥 loadPremiumCourses() called!');
    this.premiumCoursesLoaded = true;
  }
  
  loadAboutUs() {
    console.log('🔥 loadAboutUs() called!');
    this.aboutUsSectionsLoaded = true;
  }
  
  loadFreeCourses() {
    console.log('🔥 loadFreeCourses() called!');
    this.freeCourseSectionsLoaded = true;
  }
  
  loadAboutUsLight() {
    console.log('🔥 loadAboutUsLight() called!');
    this.aboutUsLightSectionsLoaded = true;
  }
  
  loadCourseSection() {
    console.log('🔥 loadCourseSection() called!');
    this.courseSectionsLoaded = true;
  }
  
  loadAboutUsEmpowering() {
    console.log('🔥 loadAboutUsEmpowering() called!');
    this.aboutUsEmpwoeringSectionsLoaded = true;
  }
  
  loadNewCourses() {
    console.log('🔥 loadNewCourses() called!');
    this.newCourseSectionsLoaded = true;
  }
  
  loadStudentPick() {
    console.log('🔥 loadStudentPick() called!');
    this.studentPickSectionsLoaded = true;
  }
  

  loadFAQContainer() {
    console.log('🔥 loadFAQContainer() called!');
    this.faqContainerSectionsLoaded = true;
  }

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
    this._authService.$changeNavbarSate.subscribe((state: any) => {
      this.isLoggedIn = this._authService.isLoggedIn();
    });

    // this.staticCards = this.isLoggedIn ? this.loggedInStatic : this.staticCards;
    setTimeout(() => {
      this._authService.$getCategoriesAndCourse.subscribe((state: any) => {
        this.fetchAllCourses();
      });
    }, 2000);
  }
  listenRefreshToken() {
    this._authService.$getCategoriesAndCourse.subscribe((state: any) => {
      if (state) {
        this.fetchAllCourses();
      } else {
        this.fetchAllCourses();
      }
    });
  }

  fetchAllCourses() {
    this.getPremiumCourses(this.directionEnum.INITIAL);
    this.getTrendingCourses(this.directionEnum.INITIAL);
    this.getFreeCourses(this.directionEnum.INITIAL);
    this.getCategoryList();
    this.getCourseListByCategory();
    this.getInstructors(this.directionEnum.INITIAL);
    this.getNewCourses(this.directionEnum.INITIAL);
  }

  scrollToSection() {
    const element = document.getElementById('courses-section');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
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
      'AI based Learning Platform Transforming Education | Fast Learner'
    );
    this.setScreenWidth(window.innerWidth);
    this.loggedInStatic = this.staticCards.slice(0, -1);
    this.getCategoryList();

    this.fetchAllCourses();

    // this.fetchAllCourses();
  }

  ngOnDestroy(): void {
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
        'student/course-details/' + courseUrl
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

  routeToCourseList(selection?) {
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
    this._courseService.getCoursesByCategory(this.payLoad).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.totalPages = response?.data?.pages;
          this.courseList = response?.data?.data;

          const prioritizedCourses = response.data.data.filter((e) =>
            prioritizedCreatorIds.includes(e.creatorId)
          );

          prioritizedCourses.sort((a, b) => {
            if (a.creatorId === 40) return -1;
            if (b.creatorId === 40) return 1;
            return 0;
          });

          const otherCourses = response.data.data.filter(
            (e) => !prioritizedCreatorIds.includes(e.creatorId)
          );

          this.courseList = [...prioritizedCourses, ...otherCourses];
          
          this.courseList?.forEach((element) => {
            element.courseDuration = this.convertSecondsToHoursAndMinutes(
              element.courseDuration
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

    this._courseService.getNewCourses(payload).subscribe({
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
              element.courseDuration
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
              element.courseDuration
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

    this._courseService.getTrendingCourses(payload).subscribe({
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
              element.courseDuration
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

    this._courseService.getFreeCourses(payload).subscribe({
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
              element.courseDuration
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

    this._courseService.getPremiumCourses(payload).subscribe({
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
              element.courseDuration
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
            (x: any) => x?.courseId == courseId
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

  scrollToCourseSection() {
    document.getElementById('courses-section').scrollIntoView();
  }
}
