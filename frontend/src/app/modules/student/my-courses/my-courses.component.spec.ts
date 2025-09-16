import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MyCoursesComponent } from './my-courses.component';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('MyCoursesComponent', () => {
  let component: MyCoursesComponent;
  let fixture: ComponentFixture<MyCoursesComponent>;
  let courseService: jasmine.SpyObj<CourseService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let router: jasmine.SpyObj<Router>;

  const mockCoursesData = [
    {
      courseId: 44,
      categoryName: 'Development',
      title: 'Advanced Spring Boot Mastery',
      level: 'Beginner',
      courseDescription:
        '<p>Java is a multi-platform, object-oriented, and network-centric language that can be used as a platform in itself. It is a fast, secure, reliable programming language for coding everything from mobile apps and enterprise software to big data applications and server-side technologies.</p>',
      creatorId: 6,
      creatorName: 'David Bomb',
      prerequisite: [
        'Basic understanding of Java programming.\nFamiliarity with basic web concepts (HTTP, REST).\nNo prior experience with Spring Boot required.',
      ],
      courseOutcome: [
        'Understand the core concepts of Spring Boot and its architecture.\nDevelop dynamic and responsive web applications using Spring Boot.\nMaster Spring Boot modules, including Spring MVC, Spring Data, and Spring Security.\nWork with Spring Boot configurations and application properties.\nImplement RESTful services and handle HTTP requests.',
      ],
      courseDuration: 1271,
      noOfTopics: 4,
      review: 5,
      noOfReviewers: 1,
      courseThumbnailUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/JwEskvoc_profile_image.jpeg',
      previewVideoUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_VIDEO/7x9ufuqQ_Spring_Boot_Tutorial_1___Start_Learning_Spring_Boot_Today.mp4',
      instructorImage:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/VEcbv7BY_profile_image.jpg',
      hasCertificate: false,
      enrolled: true,
      favourite: false,
    },
    {
      courseId: 43,
      categoryName: 'Development',
      title: 'Spring Boot Mastery From Basics to Advanced',
      level: 'Beginner',
      courseDescription:
        "<p>This comprehensive Spring Boot course is designed to take you from beginner to expert. Whether you're just starting out or looking to deepen your knowledge, this course will guide you through the fundamentals to advanced concepts of Spring Boot, one of the most powerful and widely-used frameworks for building Java-based web applications. Through hands-on projects and real-world examples, you'll gain the skills needed to build and maintain robust, efficient, and scalable applications using Spring Boot.</p>",
      creatorId: 6,
      creatorName: 'David Bomb',
      prerequisite: [
        'Basic understanding of Java programming.\nFamiliarity with basic web concepts (HTTP, REST).\nNo prior experience with Spring Boot required.',
      ],
      courseOutcome: [
        'Understand the core concepts of Spring Boot and its architecture.\nDevelop dynamic and responsive web applications using Spring Boot.\nMaster Spring Boot modules, including Spring MVC, Spring Data, and Spring Security.\nWork with Spring Boot configurations and application properties.\nImplement RESTful services and handle HTTP requests.',
      ],
      courseDuration: 515,
      noOfTopics: 4,
      review: 5,
      noOfReviewers: 1,
      courseThumbnailUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/nyvj2u7d_profile_image.jpeg',
      previewVideoUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_VIDEO/lKk57TiA_Introduction_of_RESTful_Web_Services_with_Spring_Boot.mp4',
      instructorImage:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/VEcbv7BY_profile_image.jpg',
      hasCertificate: false,
      enrolled: false,
      favourite: false,
    },
    {
      courseId: 29,
      categoryName: 'Business',
      title: 'The Unsung Selldiers',
      level: 'All Levels',
      courseDescription:
        "<p>Sales is a journey that commences with recognising your clientele, fostering relationships, acquiring customers until delivering good quality and ensuring client's success post-product or service utilisation. This journey will empower you to excel in sales, equipping you with the skills to understand customer filtration, achieve growth objectives, channel sales, establish pricing strategies, negotiate effectively, and utilise case-based selling techniques.</p>",
      creatorId: 40,
      creatorName: 'Khurram Kalimi',
      prerequisite: ['You must have a basic knowledge of sales'],
      courseOutcome: [
        'The Real Account Management/ The excellence of Account Management',
        'Importance of History, Patience, Responsiveness, and Service Quality',
        'Making your client Successful',
        'Effective Funnel Management',
        'To make Empathy as your Super power',
        'Finally Mastering the Art of Sales',
        'How is Sales a "lifestyle"',
        'The Real Account Management/ The excellence of Account Management',
        'Importance of History, Patience, Responsiveness, and Service Quality',
        'Making your client Successful',
        'Effective Funnel Management',
        'To make Empathy as your Super power',
        'Finally Mastering the Art of Sales',
      ],
      courseDuration: 4256,
      noOfTopics: 27,
      review: 4.75,
      noOfReviewers: 4,
      courseThumbnailUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_THUMBNAIL/YaCd4JMa_Thumbnail.jpg',
      previewVideoUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_VIDEO/OX9WpR0X_Video_Preview.mp4',
      instructorImage:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/JHfZ8EW1_IMG_5782_(2).jpg',
      hasCertificate: false,
      enrolled: true,
      favourite: false,
    },
    {
      courseId: 45,
      categoryName: 'Development',
      title: 'Artificial Intelligence and expert systems',
      level: 'Intermediate',
      courseDescription:
        '<p>This comprehensive course on Artificial Intelligence covers everything from basic concepts to advanced techniques. Learners will start with an introduction to AI, understand its syntax, and learn how to structure their AI projects effectively. The course includes hands-on exercises and real-world examples to ensure practical understanding. By the end of the course, students will be equipped with the knowledge and skills to develop and implement AI solutions in various domains</p><p><br></p>',
      creatorId: 6,
      creatorName: 'David Bomb',
      prerequisite: [
        'Basic knowledge of programming, preferably in Python, and a fundamental understanding of mathematics, including linear algebra and probability, are recommended to get the most out of this course.',
      ],
      courseOutcome: [
        'Understanding the core concepts and applications of artificial intelligence.\nMastering the syntax and programming constructs used in AI development.\nOrganizing and structuring AI projects efficiently.',
      ],
      courseDuration: 55203,
      noOfTopics: 50,
      review: 4.5,
      noOfReviewers: 2,
      courseThumbnailUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/7WA9Q3Ts_profile_image.jpeg',
      previewVideoUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_VIDEO/fZQHySfQ_A__Algorithm_in_AI___A_Star_Search_Algorithm___Artificial_Intelligence_Tutorial___Edureka.mp4',
      instructorImage:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/VEcbv7BY_profile_image.jpg',
      hasCertificate: true,
      enrolled: true,
      favourite: false,
    },
    {
      courseId: 28,
      categoryName: 'Development',
      title:
        'Mastering Calculus: A Comprehensive Guide to Mathematical Analysis',
      level: 'Intermediate',
      courseDescription:
        "<p>Embark on a journey to master Calculus, the cornerstone of mathematical analysis, with this comprehensive course. Whether you're a student, professional, or enthusiast, this course equips you with the essential knowledge and skills to tackle calculus with confidence. From understanding its foundational concepts to applying advanced techniques, you'll gain hands-on experience through practical examples and real-world applications. Dive deep into differential and integral calculus, explore limits, derivatives, and integrals, and learn how to solve complex problems with precision. By the end of this course, you'll have a solid understanding of calculus principles and their applications, empowering you to excel in various fields such as physics, engineering, economics, and beyond.</p><p><br></p><p><br></p><p><br></p>",
      creatorId: 22,
      creatorName: 'Craig Bogart',
      prerequisite: [
        'Basic understanding of algebra and trigonometry\nFamiliarity with mathematical concepts such as functions and equations\nEagerness to explore and master advanced mathematical techniques',
      ],
      courseOutcome: [
        'Introduction\nOverview of Calculus\nImportance and Applications\nGetting Started with Calculus Tools',
        'Limits and Continuity\nDifferentiation Techniques\nApplications of Derivatives\nIntegration Techniques\nApplications of Integrals',
        'File Structure\nUnderstanding Calculus Notations\nOrganizing Calculus Problems\nUtilizing Calculus Software and Tool',
      ],
      courseDuration: 904,
      noOfTopics: 8,
      review: 4.5,
      noOfReviewers: 2,
      courseThumbnailUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_THUMBNAIL/ma676JRQ_pngtree-differential-calculus-math-doodle-idea-png-image_4005848.png',
      previewVideoUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_VIDEO/mMFv0UIF_The_Velocity_Problem___Part_I__Numerically.mp4',
      instructorImage:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/eYcML3NI_Todd-Birzer.jpeg.webp',
      hasCertificate: true,
      enrolled: true,
      favourite: false,
    },
    {
      courseId: 40,
      categoryName: 'Development',
      title: 'Spring Boot Mastery',
      level: 'Expert',
      courseDescription:
        '<p><span style="color: rgb(50, 58, 69);">Microservices architecture is a software development approach that structures an application as a collection of small, loosely coupled services or modules. In this&nbsp;approach, each service is responsible for performing a specific business function and can be developed, deployed, and managed independently.</span></p>',
      creatorId: 3,
      creatorName: 'Darain ',
      prerequisite: [
        'Basic Understanding of Java, RESTFUL Web Services, Spring Framework Fundamentals',
      ],
      courseOutcome: [
        'The fundamentals of microservices using Spring Boot,',
        'How to Design and develop microservices using Spring Boot.',
        'Configuring distributed configuration management with Spring Cloud Config Server.',
      ],
      courseDuration: 257,
      noOfTopics: 2,
      review: 4,
      noOfReviewers: 1,
      courseThumbnailUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/caTnYlXc_profile_image.jpeg',
      previewVideoUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_VIDEO/l9Y8MhFy_Introduction_of_RESTful_Web_Services_with_Spring_Boot.mp4',
      instructorImage:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/Ii9dsNbI_artturi-jalli-gYrYa37fAKI-unsplash.jpg',
      hasCertificate: false,
      enrolled: true,
      favourite: false,
    },
    {
      courseId: 30,
      categoryName: 'Development',
      title: 'Calculus A Comprehensive Guide to Mathematics',
      level: 'Intermediate',
      courseDescription:
        "<p>Embark on a journey to master Calculus, the cornerstone of mathematical analysis, with this comprehensive course. Whether you're a student, professional, or enthusiast, this course equips you with the essential knowledge and skills to tackle calculus with confidence. From understanding its foundational concepts to applying advanced techniques, you'll gain hands-on experience through practical examples and real-world applications. Dive deep into differential and integral calculus, explore limits, derivatives, and integrals, and learn how to solve complex problems with precision. By the end of this course, you'll have a solid understanding of calculus principles and their applications, empowering you to excel in various fields such as physics, engineering, economics, and beyond.</p><p><br></p>",
      creatorId: 22,
      creatorName: 'Craig Bogart',
      prerequisite: [
        'Basic understanding of algebra and trigonometry\nFamiliarity with mathematical concepts such as functions and equations\nEagerness to explore and master advanced mathematical techniques',
      ],
      courseOutcome: [
        'Introduction\nOverview of Calculus\nImportance and Applications\nGetting Started with Calculus Tools',
        'Limits and Continuity\nDifferentiation Techniques\nApplications of Derivatives\nIntegration Techniques\nApplications of Integrals',
        'File Structure\nUnderstanding Calculus Notations\nOrganizing Calculus Problems\nUtilizing Calculus Software and Tool',
      ],
      courseDuration: 866,
      noOfTopics: 8,
      review: 4,
      noOfReviewers: 2,
      courseThumbnailUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_THUMBNAIL/f6YrAkhC_realistic-math-chalkboard-background_23-2148156773.jpg',
      previewVideoUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_VIDEO/IfqsRtaa_The_Velocity_Problem___Part_II__Graphically.mp4',
      instructorImage:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/eYcML3NI_Todd-Birzer.jpeg.webp',
      hasCertificate: true,
      enrolled: true,
      favourite: false,
    },
    {
      courseId: 15,
      categoryName: 'Development',
      title: 'Fundamentals of Python',
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
      courseDuration: 541,
      noOfTopics: 2,
      review: 4,
      noOfReviewers: 1,
      courseThumbnailUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_THUMBNAIL/VjgWlAyz_python.jpg',
      previewVideoUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_VIDEO/og0WUESF_Python_Tutorial_for_Beginners_15_-_Classes_and_Self.mp4',
      instructorImage:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/3WcuChLa_Mohsen-Hassan.jpeg.webp',
      hasCertificate: true,
      enrolled: true,
      favourite: false,
    },
    {
      courseId: 12,
      categoryName: 'Development',
      title: 'Computing in Python',
      level: 'Expert',
      courseDescription:
        "<p>Welcome to Python Programming Essentials! This course is your gateway to mastering one of the most versatile and in-demand programming languages today. Whether you're a complete beginner or looking to enhance your programming skills, this course will provide you with a comprehensive understanding of Python's fundamentals.</p><p>The journey begins with an Introduction to Python, where you'll explore the language's history, its wide-ranging applications, and the reasons why it's a top choice for beginners and seasoned developers alike. From there, we'll dive into Syntax, where you'll learn the essential elements of Python syntax, including variables, data types, control structures, and functions.</p><p>Understanding File Structure is crucial for organizing your Python projects effectively. You'll learn how to manage files and directories, work with modules and packages, and adopt best practices for structuring your codebase.</p>",
      creatorId: 20,
      creatorName: 'Frawley Barton',
      prerequisite: [
        'No prior programming experience is required. This course is designed for absolute beginners who are eager to learn Python programming from scratch. All you need is a passion for learning and a willingness to dive into the world of coding.',
      ],
      courseOutcome: [
        'Introduction\nHistory and Evolution of Python\nApplications of Python in Various Fields\nAdvantages of Learning Python',
        'Syntax\nVariables and Data Types\nOperators and Expressions\nControl Structures (if statements, loops)\nFunctions and Scope\n',
        'Course Title\nAdvanced Topics in Python (This section will cover more advanced concepts or specific topics based on the interests and needs of the learners.)',
      ],
      courseDuration: 610,
      noOfTopics: 1,
      review: 4,
      noOfReviewers: 1,
      courseThumbnailUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_THUMBNAIL/VlU06Uc0_pexels-photo-276452.jpeg',
      previewVideoUrl:
        'https://storage.googleapis.com/fastlearner-bucket/PREVIEW_VIDEO/HwsczKFW_Python_Tutorial_for_Beginners_12_-_While_Loop__and_For_Loops_in_Python.mp4',
      instructorImage:
        'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/HXhgQHoi_Crystal-Richards.jpeg.webp',
      hasCertificate: false,
      enrolled: false,
      favourite: false,
    },
  ];

  beforeEach(async () => {
    const courseServiceSpy = jasmine.createSpyObj('CourseService', [
      'getMyCourses',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', [
      'success',
      'error',
    ]);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [MyCoursesComponent],
      providers: [
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(MyCoursesComponent);
    component = fixture.componentInstance;
    courseService = TestBed.inject(
      CourseService
    ) as jasmine.SpyObj<CourseService>;
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with course list', () => {
    const mockResponse = {
      status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      data: {
        myCourses: mockCoursesData,
        totalPages: 1,
      },
    };
    courseService.getMyCourses.and.returnValue(of(mockResponse));

    component.ngOnInit();

    expect(courseService.getMyCourses).toHaveBeenCalledWith(
      component.myCourses
    );
    expect(component.myCourseList.length).toBe(component.myCourseList.length);
  });

  it('should handle error during course list fetch', () => {
    const mockError = {
      error: {
        status:
          component._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE,
      },
    };
    courseService.getMyCourses.and.returnValue(throwError(mockError));

    component.ngOnInit();

    expect(courseService.getMyCourses).toHaveBeenCalledWith(
      component.myCourses
    );
    expect(component.myCourseList.length).toBe(0);
  });

  it('should fetch course list on filter change', () => {
    const mockResponse = {
      status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      data: {
        myCourses: mockCoursesData,
        totalPages: 1,
      },
    };
    courseService.getMyCourses.and.returnValue(of(mockResponse));

    component.onChangeOfFilter('newFilter');

    expect(courseService.getMyCourses).toHaveBeenCalledWith(
      component.myCourses
    );
    expect(component.myCourseList.length).toBe(component.myCourseList.length);
  });

  it('should fetch more courses on showMore', () => {
    const mockResponse = {
      status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      data: {
        myCourses: [{ id: 2, title: 'Course 2' }],
        totalPages: 2,
      },
    };
    courseService.getMyCourses.and.returnValue(of(mockResponse));

    component.showMore();

    expect(courseService.getMyCourses).toHaveBeenCalled();
    expect(component.myCourseList.length).toBe(component.myCourseList.length); // Assuming initial list was empty
  });

  it('should search and reset page to 0', () => {
    const mockResponse = {
      status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      data: {
        myCourses: mockCoursesData,
        totalPages: 1,
      },
    };
    courseService.getMyCourses.and.returnValue(of(mockResponse));

    component.search();

    expect(component.page).toBe(0);
  });

  it('should navigate to course details', () => {
    const courseTitle = 'Course Title';
    component.routeToCourseDetails(courseTitle);

    expect(router.navigate).toHaveBeenCalledWith([
      'student/course-details',
      'Course Title',
    ]);
  });
});
