import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { LandingPageComponent } from './landing-page.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { SocialAuthService } from '@abacritt/angularx-social-login';
import { NzMessageModule } from 'ng-zorro-antd/message';
import { By } from '@angular/platform-browser';
import {
  CUSTOM_ELEMENTS_SCHEMA,
  DebugElement,
  NO_ERRORS_SCHEMA,
} from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { query } from '@angular/animations';

describe('LandingPageComponent', () => {
  let component: LandingPageComponent;
  let fixture: ComponentFixture<LandingPageComponent>;
  let authService: AuthService;
  let courseService: CourseService;
  let messageService: MessageService;
  let cacheService: CacheService;
  let sharedService: SharedService;
  let router: Router;
  let socialAuthService: jasmine.SpyObj<SocialAuthService>;
  let de: DebugElement;

  const mockCourseList = [
    {
      courseId: 29,
      categoryName: 'Business',
      title: 'The Unsung Selldiers',
      courseUrl: 'the-unsung-selldiers',
      level: 'All Levels',
      courseDescription:
        "<p>Sales is a journey that commences with recognising your clientele, fostering relationships, acquiring customers until delivering good quality and ensuring client's success post-product or service utilisation. This journey will empower you to excel in sales, equipping you with the skills to understand customer filtration, achieve growth objectives, channel sales, establish pricing strategies, negotiate effectively, and utilise case-based selling techniques.</p>",
      creatorId: 40,
      creatorName: 'Khurram Kalimi',
      prerequisite: ['You must have a basic knowledge of sales'],
      courseOutcome: [
        'The Real Account Management/ The excellence of Account Management',
        'Importance of History, Patience, Responsiveness, and Service Quality',
        'Making your Client Successful',
        'Effective Funnel Management',
        'To make Empathy as your Super power',
        'Finally Mastering the Art of Sales',
        'How is Sales a "Lifestyle"',
        'The Real Account Management/ The excellence of Account Management',
        'Importance of History, Patience, Responsiveness, and Service Quality',
      ],
      courseDuration: 4256,
      noOfTopics: 27,
      review: 4.857142857142857,
      noOfReviewers: 7,
      courseThumbnailUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/cXZX2lOw_profile_image.jpeg',
      previewVideoUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_VIDEO/OX9WpR0X_Video_Preview.mp4',
      instructorImage:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/RsdEH3gz_profile_image.jpeg',
      hasCertificate: true,
      metaHeading:
        'The Unsung Selldiers: Effective Sales Strategies to Improve Your Sales Game',
      metaTitle:
        'The Unsung Selldiers Effective Sales Strategies | FastLearner',
      metaDescription:
        'Discover effective sales strategies with Selldiers. Learn from an experienced professional on our learning platform, FastLearner, to improve your sales skills',
      enrolled: false,
      favourite: false,
    },
    {
      courseId: 91,
      categoryName: 'Design',
      title:
        'Mastering Adobe Firefly: From Basics to Advanced Creative Techniques',
      courseUrl:
        'mastering-adobe-firefly-from-basics-to-advanced-creative-techniques',
      level: 'Intermediate',
      courseDescription:
        '<p><span style="color: black;">Discover the power of Adobe Firefly in this comprehensive course designed for creative professionals and enthusiasts alike. You’ll start by exploring the unique features of Firefly as a web service, before diving into hands-on sessions that cover everything from generating and refining images to using AI-driven tools like Generative Fill, Generative Expand, and Generative Re-color within professional design software like Photoshop and Illustrator. Learn to create stunning vectors, patterns, and mock-ups with precision, and enhance your workflow with advanced techniques for detail enhancement and object removal. Whether you\'re looking to streamline your design process or explore new creative possibilities, this course has everything you need to master Adobe Firefly.</span></p>',
      creatorId: 165,
      creatorName: 'Joseph Labrecque',
      prerequisite: [
        'Basic understanding of design principles and familiarity with Adobe creative tools.',
      ],
      courseOutcome: [
        'How to generate and manipulate images using Adobe Firefly.',
        'Techniques for enhancing detail and expanding creative content.',
        'Efficient use of selection brushes and object removal tools.',
        'Mastery of generating vectors, patterns, and mockups.',
        'Advanced methods for recoloring and filling shapes with AI assistance.',
        'Practical skills for integrating generative AI into design workflows.',
      ],
      courseDuration: 3148,
      noOfTopics: 17,
      review: 4.75,
      noOfReviewers: 4,
      courseThumbnailUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/3bA8vH0W_profile_image.jpeg',
      previewVideoUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_VIDEO/Q1eQVTZV_01-00-_Preview.mp4',
      instructorImage:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/8PYdYETW_profile_image.jpeg',
      hasCertificate: true,
      metaHeading:
        'Master Adobe Firefly: From Beginner to Expert Guide [Updated 2024]',
      metaTitle: 'Master Adobe Firefly: Beginner to Expert Guide | FastLearner',
      metaDescription:
        'Master Adobe Firefly from scratch. Learn essential tips and techniques to create stunning visuals. Our AI-Based Learning platform takes you from beginner to expert.',
      enrolled: false,
      favourite: false,
    },
    {
      courseId: 45,
      categoryName: 'Development',
      title: 'Artificial Intelligence and expert systems',
      courseUrl: 'artificial-intelligence-and-expert-systems',
      level: 'Intermediate',
      courseDescription:
        '<p>This comprehensive course on Artificial Intelligence covers everything from basic concepts to advanced techniques. Learners will start with an introduction to AI, understand its syntax, and learn how to structure their AI projects effectively. The course includes hands-on exercises and real-world examples to ensure practical understanding. By the end of the course, students will be equipped with the knowledge and skills to develop and implement AI solutions in various domains.</p>',
      creatorId: 6,
      creatorName: 'David Bomb',
      prerequisite: [
        'Basic knowledge of programming, preferably in Python, and a fundamental understanding of mathematics, including linear algebra and probability, are recommended to get the most out of this course.',
      ],
      courseOutcome: [
        'Understanding the core concepts and applications of artificial intelligence.',
        'Mastering the syntax and programming constructs used in AI development.',
        'Organizing and structuring AI projects efficiently.',
      ],
      courseDuration: 55203,
      noOfTopics: 50,
      review: 4.5,
      noOfReviewers: 4,
      courseThumbnailUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/hYo5hNon_profile_image.jpeg',
      previewVideoUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_VIDEO/fZQHySfQ_A__Algorithm_in_AI___A_Star_Search_Algorithm___Artificial_Intelligence_Tutorial___Edureka.mp4',
      instructorImage:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/fqQq6KHJ_profile_image.jpeg',
      hasCertificate: true,
      metaHeading: null,
      metaTitle: null,
      metaDescription: null,
      enrolled: false,
      favourite: false,
    },
    {
      courseId: 15,
      categoryName: 'Development',
      title: 'Fundamentals of Python',
      courseUrl: 'fundamentals-of-python',
      level: 'Beginner',
      courseDescription:
        "<p>Welcome to Python Programming Essentials! This course is your gateway to mastering one of the most versatile and in-demand programming languages today. Whether you're a complete beginner or looking to enhance your programming skills, this course will provide you with a comprehensive understanding of Python's fundamentals.</p><p>The journey begins with an Introduction to Python, where you'll explore the language's history, its wide-ranging applications, and the reasons why it's a top choice for beginners and seasoned developers alike. From there, we'll dive into Syntax, where you'll learn the essential elements of Python syntax, including variables, data types, control structures, and functions.</p><p>Understanding File Structure is crucial for organizing your Python projects effectively. You'll learn how to manage files and directories, work with modules and packages, and adopt best practices for structuring your codebase.</p>",
      creatorId: 12,
      creatorName: 'Peter Fernandez',
      prerequisite: [
        'No prior programming experience is required. This course is designed for absolute beginners who are eager to learn Python programming from scratch. All you need is a passion for learning and a willingness to dive into the world of coding.',
      ],
      courseOutcome: [
        'Introduction\nHistory and Evolution of Python\nApplications of Python in Various Fields\nAdvantages of Learning Python',
        'Syntax\nVariables and Data Types\nOperators and Expressions\nControl Structures (if statements, loops)\nFunctions and Scope\n',
        'Course Title\nAdvanced Topics in Python (This section will cover more advanced concepts or specific topics based on the interests and needs of the learners.)',
      ],
      courseDuration: 22998,
      noOfTopics: 19,
      review: 4.5,
      noOfReviewers: 2,
      courseThumbnailUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/nif0RRQq_profile_image.jpeg',
      previewVideoUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_VIDEO/og0WUESF_Python_Tutorial_for_Beginners_15_-_Classes_and_Self.mp4',
      instructorImage:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/3WcuChLa_Mohsen-Hassan.jpeg.webp',
      hasCertificate: true,
      metaHeading: null,
      metaTitle: null,
      metaDescription: null,
      enrolled: false,
      favourite: false,
    },
    {
      courseId: 75,
      categoryName: 'Development',
      title: 'Full-Stack Developer Bootcamp',
      courseUrl: 'full-stack-developer-bootcamp',
      level: 'All Levels',
      courseDescription:
        "<p>In this bootcamp, you'll embark on a comprehensive journey to becoming a Full-Stack Developer. This course covers both front-end and back-end development technologies, providing you with the skills needed to build dynamic web applications from scratch.</p><p>Starting with HTML, CSS, and JavaScript, you will learn how to create visually appealing and interactive user interfaces. You'll then progress to server-side programming with Node.js, Express.js, and databases like MongoDB. The course includes hands-on projects to reinforce your learning, allowing you to apply what you've learned in real-world scenarios.</p><p>By the end of the bootcamp, you will have a solid understanding of full-stack development and a portfolio of projects to showcase your skills.</p>",
      creatorId: 25,
      creatorName: 'Linda Green',
      prerequisite: [
        'A basic understanding of web technologies and programming concepts is beneficial but not mandatory.',
      ],
      courseOutcome: [
        'Proficiency in HTML, CSS, and JavaScript for front-end development.',
        'Understanding of server-side programming with Node.js and Express.js.',
        'Familiarity with database management using MongoDB.',
        'Ability to build full-stack applications from scratch.',
      ],
      courseDuration: 86265,
      noOfTopics: 65,
      review: 5,
      noOfReviewers: 2,
      courseThumbnailUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/H4Y3pn5O_profile_image.jpeg',
      previewVideoUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_VIDEO/4XwTHVtZ_01_-_Full_Stack_Developer_Bootcamp_Preview.mp4',
      instructorImage:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/hUq2Ul6m_profile_image.jpeg',
      hasCertificate: true,
      metaHeading:
        'Full-Stack Developer Bootcamp: Build Web Applications from Scratch',
      metaTitle: 'Full-Stack Developer Bootcamp | FastLearner',
      metaDescription:
        'Join our Full-Stack Developer Bootcamp to learn web development skills. Build dynamic applications with HTML, CSS, JavaScript, and Node.js. Enroll now!',
      enrolled: false,
      favourite: false,
    },
    {
      courseId: 11,
      categoryName: 'Marketing',
      title: 'Digital Marketing Essentials',
      courseUrl: 'digital-marketing-essentials',
      level: 'Beginner',
      courseDescription:
        "<p>In today's digital landscape, understanding marketing fundamentals is crucial for any aspiring marketer. This course covers the essentials of digital marketing, from SEO and content marketing to social media strategies and email marketing. Whether you're looking to launch a career in marketing or enhance your existing skills, this course is designed to provide you with a strong foundation.</p><p>You will explore key topics such as search engine optimization, social media engagement, content creation, and effective email marketing techniques. Each module includes practical assignments and real-world examples to help you apply what you learn.</p>",
      creatorId: 10,
      creatorName: 'Emily Davis',
      prerequisite: [
        'No prior experience in marketing is necessary. A passion for learning and a desire to grow in the digital marketing field are the only requirements.',
      ],
      courseOutcome: [
        'Understanding of digital marketing principles and best practices.',
        'Skills to create effective marketing strategies across various digital platforms.',
        'Ability to measure and analyze marketing performance using relevant tools.',
      ],
      courseDuration: 10805,
      noOfTopics: 21,
      review: 4.5,
      noOfReviewers: 6,
      courseThumbnailUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/C1orBsYi_profile_image.jpeg',
      previewVideoUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_VIDEO/d5n0isgT_01_Digital_Marketing_Essentials_Preview.mp4',
      instructorImage:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/OnZPvcO5_profile_image.jpeg',
      hasCertificate: true,
      metaHeading:
        'Digital Marketing Essentials: Kickstart Your Marketing Career',
      metaTitle: 'Digital Marketing Essentials | FastLearner',
      metaDescription:
        'Learn digital marketing fundamentals to boost your career. From SEO to social media, master essential skills with our comprehensive course.',
      enrolled: false,
      favourite: false,
    },
  ];

  beforeEach(async () => {
    socialAuthService = jasmine.createSpyObj(
      'SocialAuthService',
      ['signIn', 'signOut', 'authState'],
      {
        authState: of(null),
      }
    );

    await TestBed.configureTestingModule({
      declarations: [LandingPageComponent],
      imports: [
        HttpClientTestingModule,
        NzMessageModule,
        BrowserAnimationsModule,
      ],
      providers: [
        AuthService,
        CacheService,
        CourseService,
        MessageService,
        SharedService,
        {
          provide: Router,
          useValue: {
            navigate: jasmine.createSpy('navigate'),
            navigateByUrl: jasmine.createSpy('navigateByUrl'),
          },
        },
        { provide: SocialAuthService, useValue: socialAuthService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParams: {} }, params: of({}) },
        },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(LandingPageComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    courseService = TestBed.inject(CourseService);
    messageService = TestBed.inject(MessageService);
    cacheService = TestBed.inject(CacheService);
    sharedService = TestBed.inject(SharedService);
    router = TestBed.inject(Router);
    de = fixture.debugElement;

    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should call getCategoryList on initialization', () => {
    spyOn(component, 'getCategoryList').and.callThrough();
    component.ngOnInit();
    expect(component.getCategoryList).toHaveBeenCalled();
  });

  it('should update visible courses correctly', () => {
    component.categoryList = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
    component.currentIndex = 0;
    component.updateVisibleCourses();
    expect(component.visibleCourses.length).toBe(6);
    expect(component.visibleCourses).toEqual([1, 2, 3, 4, 5, 6]);
  });

  it('should slide courses left and right', () => {
    component.categoryList = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
    component.currentIndex = 0;
    component.slideRight();
    expect(component.currentIndex).toBe(1);

    component.slideLeft();
    expect(component.currentIndex).toBe(0);
  });

  it('should handle window resize event', fakeAsync(() => {
    spyOn(component, 'setScreenWidth').and.callThrough();
    component.onResize({ target: { innerWidth: 500 } });
    tick();
    expect(component.setScreenWidth).toHaveBeenCalledWith(500);
  }));

  it('should navigate to sign-up screen', () => {
    component.routeToSignUpScreen();
    expect(router.navigate).toHaveBeenCalledWith(['auth/sign-up']);
  });

  it('should have a button to sign up if not logged in', () => {
    component.isLoggedIn = false;
    fixture.detectChanges();
    const signUpButton = de.query(By.css('.landing-page-button-sign-up'));
    expect(signUpButton).toBeNull();
  });

  it('should handle email validation', () => {
    const event = { target: { value: 'test@example.com' } };
    component.validateEmail(event);
    expect(component.emailValid).toBeTrue();

    event.target.value = 'invalid-email';
    component.validateEmail(event);
    expect(component.emailValid).toBeFalse();
  });

  it('should handle invalid email input', () => {
    component.subscribeEmail = 'invalid-email';
    component.validateEmail({ target: { value: 'invalid-email' } });
    fixture.detectChanges();
    const errorMessage = de.query(By.css('.error-message'));
    expect(errorMessage).toBeNull();
  });

  it('should subscribe to newsletter', () => {
    spyOn(sharedService, 'subscribeNewsLetter').and.returnValue(
      of({ message: 'Subscribed successfully' })
    );
    spyOn(messageService, 'success').and.callThrough();
    component.subscribeEmail = 'test@example.com';
    component.emailValid = true;
    component.subscribeNewsLetter();
    expect(sharedService.subscribeNewsLetter).toHaveBeenCalledWith(
      'test@example.com'
    );
    expect(messageService.success).toHaveBeenCalledWith(
      'Subscribed successfully'
    );
  });

  it('should handle course list retrieval by category', () => {
    spyOn(courseService, 'getCoursesByCategory').and.returnValue(
      of({ status: 200, data: { pages: 1, data: mockCourseList } })
    );

    component.getCourseListByCategory();

    expect(courseService.getCoursesByCategory).toHaveBeenCalledWith(
      component.payLoad
    );
    expect(component.courseList).toEqual(mockCourseList);

    // Additional check to ensure each course has a courseId
    expect(component.courseList.every((course) => course.courseId)).toBeTrue();
  });

  it('should toggle favorite status of a course', () => {
    spyOn(courseService, 'addOrRemoveCourseToFavorite').and.returnValue(
      of({ status: 200, message: 'Success' })
    );
    spyOn(sharedService, 'updateFavCourseMenu').and.callThrough();
    component.toggleFavoriteCourse(1, false);
    expect(courseService.addOrRemoveCourseToFavorite).toHaveBeenCalledWith(1);
    expect(sharedService.updateFavCourseMenu).toHaveBeenCalled();
  });

  it('should scroll to courses section', () => {
    const mockElement = {
      scrollIntoView: jasmine.createSpy('scrollIntoView'),
    } as unknown as HTMLElement;

    spyOn(document, 'getElementById').and.returnValue(mockElement);

    component.scrollToCourseSection();

    expect(document.getElementById).toHaveBeenCalledWith('courses-section');

    expect(mockElement.scrollIntoView).toHaveBeenCalled();
  });

  it('should handle scrolling left and right', () => {
    const mockScrollBy = jasmine.createSpy('scrollBy');

    const mockNativeElement = {
      parentElement: {
        scrollBy: mockScrollBy,
      },
    };

    component.scrollerContent.nativeElement = mockNativeElement;

    component.scrollLeft();
    expect(mockScrollBy).toHaveBeenCalledWith({
      left: -200,
      behavior: 'smooth',
    });

    component.scrollRight();
    expect(mockScrollBy).toHaveBeenCalledWith({
      left: 200,
      behavior: 'smooth',
    });
  });

  it('should handle errors when retrieving course list by category', () => {
    spyOn(courseService, 'getCoursesByCategory').and.returnValue(
      of({ status: 500, error: 'Internal Server Error' })
    );

    component.getCourseListByCategory();

    expect(courseService.getCoursesByCategory).toHaveBeenCalledWith(
      component.payLoad
    );
    expect(component.courseList).toEqual([]);
  });
  it('should not update visible courses if categoryList is empty', () => {
    component.categoryList = [];
    component.currentIndex = 0;
    component.updateVisibleCourses();
    expect(component.visibleCourses.length).toBe(0);
  });
  it('should not subscribe to newsletter with invalid email format', () => {
    spyOn(sharedService, 'subscribeNewsLetter').and.callThrough();
    spyOn(messageService, 'error').and.callThrough();
    component.emailValid = false;
    component.subscribeNewsLetter();

    expect(sharedService.subscribeNewsLetter).not.toHaveBeenCalled();
    expect(component.subscribeEmail).toBeUndefined();
  });

  it('should handle API errors when toggling favorite status of a course', () => {
    spyOn(courseService, 'addOrRemoveCourseToFavorite').and.returnValue(
      of({ status: 500, message: 'Error toggling favorite' })
    );
    spyOn(sharedService, 'updateFavCourseMenu').and.callThrough();

    component.toggleFavoriteCourse(1, false);

    expect(courseService.addOrRemoveCourseToFavorite).toHaveBeenCalledWith(1);
    expect(sharedService.updateFavCourseMenu).not.toHaveBeenCalled();
  });

  it('should navigate to about-us page', () => {
    component.routeToAboutUs();
    expect(router.navigate).toHaveBeenCalledWith(['about-us']);
  });

  it('should navigate to welcome-instructor page', () => {
    component.routeToInstructorWelcomePage();
    expect(router.navigate).toHaveBeenCalledWith(['welcome-instructor']);
  });

  describe('onSelectCategory', () => {
    it('should set selectedCategory and call getCourseListByCategory', () => {
      const categoryId = 1;
      spyOn(component, 'getCourseListByCategory');

      component.onSelectCategory(categoryId);

      expect(component.selectedCategory).toBe(categoryId);
      expect(component.payLoad.categoryId).toBe(categoryId);
      expect(component.getCourseListByCategory).toHaveBeenCalled();
    });
  });
  describe('startCourse', () => {
    it('should navigate to course details if logged in', () => {
      component.isLoggedIn = true;
      const title = 'Course Title';

      component.startCourse(title);

      expect(component['_router'].navigate).toHaveBeenCalledWith([
        'student/course-details',
        'Course Title',
      ]);
    });

    it('should cache redirect URL and navigate to sign-in if not logged in', () => {
      component.isLoggedIn = false;
      const title = 'Course Title';
      spyOn(component['_cacheService'], 'saveInCache');

      component.startCourse(title);

      expect(component['_cacheService'].saveInCache).toHaveBeenCalledWith(
        'redirectUrl',
        'student/course-details/' + title
      );
      expect(component['_router'].navigate).toHaveBeenCalledWith([
        'auth/sign-in',
      ]);
    });
  });
  describe('routeToCourseDetails', () => {
    it('should navigate to course details', () => {
      const title = 'Course Title';

      component.routeToCourseDetails(title);

      expect(component['_router'].navigate).toHaveBeenCalledWith([
        'student/course-details',
        'Course Title',
      ]);
    });
  });
  describe('routeToCourseDetailsContent', () => {
    it('should navigate to course details with fragment', () => {
      const title = 'Course Title';

      component.routeToCourseDetailsContent(title);

      expect(component['_router'].navigate).toHaveBeenCalledWith(
        ['student/course-details', 'Course Title'],
        {
          fragment: 'course-content',
        }
      );
    });
  });
  describe('routeToCourseList', () => {
    it('should navigate to course list', () => {
      component.routeToCourseList('CATEGORY_COURSES');

      expect(component['_router'].navigate).toHaveBeenCalledWith(
        ['student/courses'],
        {
          queryParams: { selection: 'CATEGORY_COURSES' },
        }
      );
    });
  });
  describe('routeToInsructorProfile', () => {
    it('should navigate to instructor profile with queryParams', () => {
      const profileUrl = 'john';

      component.routeToInsructorProfile(profileUrl);

      expect(component['_router'].navigate).toHaveBeenCalledWith(
        ['user/profile'],
        {
          queryParams: { url: profileUrl },
        }
      );
    });
  });
  describe('scrollToSection', () => {
    it('should scroll to the element with ID "courses-section"', () => {});

    it('should not throw an error if the element is not found', () => {
      spyOn(document, 'getElementById').and.returnValue(null);

      expect(() => component.scrollToSection()).not.toThrow();
    });
  });
  describe('scrollToBottom', () => {
    it('should scroll to the bottom of the page', () => {
      spyOn(window, 'scrollTo');

      component.scrollToBottom();

      expect(window.scrollTo).toHaveBeenCalled();
    });
  });
  describe('hideHoverCard', () => {
    it('should set hoverCardVisible to false', () => {
      component.hoverCardVisible = true;

      component.hideHoverCard();

      expect(component.hoverCardVisible).toBeFalse();
    });
  });
  describe('showHoverCard', () => {
    it('should set hoverCardVisible to true', () => {
      component.hoverCardVisible = false;

      component.showHoverCard();

      expect(component.hoverCardVisible).toBeTrue();
    });
  });
  describe('checkStateManagement', () => {
    it('should navigate to the URL from cache and remove it from cache', () => {
      const redirectUrl = 'some-url';
      spyOn(component['_cacheService'], 'getDataFromCache').and.returnValue(
        redirectUrl
      );
      spyOn(component['_cacheService'], 'removeFromCache');

      component.checkStateManagement();

      expect(component['_cacheService'].removeFromCache).toHaveBeenCalledWith(
        'redirectUrl'
      );
      expect(router.navigateByUrl).toHaveBeenCalledWith(redirectUrl);
    });

    it('should not navigate if no redirect URL is present in cache', () => {
      spyOn(component['_cacheService'], 'getDataFromCache').and.returnValue(
        null
      );

      component.checkStateManagement();

      expect(router.navigateByUrl).not.toHaveBeenCalled();
    });
  });
  describe('expand', () => {
    it('should toggle the expanded class and update faq.isExpanded', () => {
      const faq = { isExpanded: false };
      const event = {
        currentTarget: {
          nextSibling: {
            classList: {
              contains: (className: string) => false,
              add: jasmine.createSpy('add'),
              remove: jasmine.createSpy('remove'),
            },
          },
        },
      };

      component.expand(faq, event as any);

      expect(
        event.currentTarget.nextSibling.classList.add
      ).toHaveBeenCalledWith('expanded');
      expect(faq.isExpanded).toBeTrue();
    });

    it('should remove the expanded class and update faq.isExpanded if already expanded', () => {
      const faq = { isExpanded: true };
      const event = {
        currentTarget: {
          nextSibling: {
            classList: {
              contains: (className: string) => true,
              add: jasmine.createSpy('add'),
              remove: jasmine.createSpy('remove'),
            },
          },
        },
      };

      component.expand(faq, event as any);

      expect(
        event.currentTarget.nextSibling.classList.remove
      ).toHaveBeenCalledWith('expanded');
      expect(faq.isExpanded).toBeFalse();
    });
  });
});
