import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { CourseService } from './course.service';
import { AuthService } from './auth.service';
import { InstructorService } from './instructor.service';
import { environment } from 'src/environments/environment.development';
import { of, throwError } from 'rxjs';

describe('CourseService', () => {
  let service: CourseService;
  let httpTestingController: HttpTestingController;
  let authService: jasmine.SpyObj<AuthService>;
  let instructorService: jasmine.SpyObj<InstructorService>;

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn']);
    const instructorServiceSpy = jasmine.createSpyObj('InstructorService', [
      'getTopicTypes',
    ]);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        CourseService,
        { provide: AuthService, useValue: authServiceSpy },
        { provide: InstructorService, useValue: instructorServiceSpy },
      ],
    });

    service = TestBed.inject(CourseService);
    httpTestingController = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    instructorService = TestBed.inject(
      InstructorService
    ) as jasmine.SpyObj<InstructorService>;
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('HTTP Methods', () => {
    const mockResponse = { data: [] };

    it('should get course categories', () => {
      authService.isLoggedIn.and.returnValue(true);

      service.getCourseCategory().subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course-category/`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should get courses by category', () => {
      const body = { category: 'Science' };
      authService.isLoggedIn.and.returnValue(true);

      service.getCoursesByCategory(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/course-by-category`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });

    it('should get course details', () => {
      const courseId = 1;
      authService.isLoggedIn.and.returnValue(true);

      service.getCourseDetails(courseId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/get/${courseId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should get related courses', () => {
      const body = { courseId: 1 };
      authService.isLoggedIn.and.returnValue(true);

      service.getRelatedCourses(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/get-related-courses`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });

    it('should get favorite courses', () => {
      const body = { title: 'Angular', pageNo: 1, pageSize: 10 };

      service.getFavoriteCourses(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}favourite-course/?pageNo=${body.pageNo}&pageSize=${body.pageSize}&title=${body.title}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should get my courses', () => {
      const body = {
        title: 'Angular',
        pageNo: 1,
        pageSize: 10,
        sortBy: 'title',
      };

      service.getMyCourses(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}enrollment/?pageNo=${body.pageNo}&pageSize=${body.pageSize}&title=${body.title}&sortBy=${body.sortBy}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should enroll in a course', () => {
      const courseId = 1;

      service.enrolledInCourse(courseId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}enrollment/?courseId=${courseId}`
      );
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });

    it('should add or remove course to favorites', () => {
      const courseId = 1;

      service.addOrRemoveCourseToFavorite(courseId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}favourite-course/?courseId=${courseId}`
      );
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });

    it('should upload YouTube video URL', () => {
      const videoId = '123';

      service.youtubeVideoUrlUpload(videoId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}youtube-video/duration?videoId=${videoId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should get course sections', () => {
      const courseId = 1;

      service.getCourseSections(courseId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}section/${courseId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should get section topics', () => {
      const courseId = 1;
      const sectionId = 2;

      service.getSectionTopics(courseId, sectionId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}topic/course/${courseId}/section/${sectionId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should rate and review course', () => {
      const body = { courseId: 1, rating: 5, review: 'Excellent' };

      service.rateAndReviewCourse(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course-review/`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });

    it('should get course rating and review', () => {
      const courseId = 1;

      service.getCourseRatingAndReview(courseId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course-review/${courseId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should rate and review section', () => {
      const body = { sectionId: 1, rating: 5, review: 'Good section' };

      service.rateAndReviewSection(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}section-review/`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });

    it('should like and dislike review section', () => {
      const body = { reviewId: 1, action: 'like' };

      service.likeAndDislikeReviewSection(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course-review/like/${body.reviewId}/${body.action}`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({});
      req.flush(mockResponse);
    });

    it('should get section rating and review', () => {
      const sectionId = 1;

      service.getSectionRatingAndReview(sectionId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}section-review/${sectionId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('Utility Methods', () => {
    it('should calculate course progress', () => {
      service.course = {
        title: 'Course Title',
        description: 'Course Description',
        categoryId: 1,
        courseLevelId: 1,
        about: 'About Course',
        thumbnailUrl: 'thumbnail.jpg',
        previewVideoURL: 'video.mp4',
        tags: [
          { id: 1, name: 'Tag1' },
          { id: 2, name: 'Tag2' },
        ],
        prerequisite: ['Prerequisite1'],
        courseOutcomes: ['Outcome1'],
        sections: [{}, {}],
      };

      const progress = service.calculateCourseProgress();
      expect(progress).toBe('29.70'); // Example progress
    });

    it('should fetch topic type id', () => {
      service.topicTypes = [
        { name: 'Video', id: 1 },
        { name: 'Quiz', id: 2 },
      ];
      const typeId = service.fetchTopicTypeId('Video');
      expect(typeId).toBe(1);
    });

    // it('should get course tags', () => {
    //   service.tags = [
    //     { id: 1, name: 'Tag1' },
    //     { id: 2, name: 'Tag2' },
    //   ];
    //   const tags = service.getCourseTags();
    //   expect(tags).toEqual(service.tags);
    // });

    it('should get topic types', () => {
      const response = { status: 200, data: [{ name: 'Video', id: 1 }] };
      instructorService.getTopicTypes.and.returnValue(of(response));

      service.getTopicTypes();
      expect(service.topicTypes).toBeDefined();
    });
  });

  // it('should get topic types', () => {
  //   const response = { status: 200, data: [{ name: 'Video', id: 1 }] };
  //   instructorService.getTopicTypes.and.returnValue(of(response));

  //   service.getTopicTypes().subscribe((res) => {
  //     expect(res).toEqual(response.data);
  //   });

  //   const req = httpTestingController.expectOne(
  //     `${environment.baseUrl}topic-types`
  //   );
  //   expect(req.request.method).toBe('GET');
  //   req.flush(response);
  // });

  it('should calculate course progress', () => {
    service.course = {
      title: 'Course Title',
      description: 'Course Description',
      categoryId: 1,
      courseLevelId: 1,
      about: 'About Course',
      thumbnailUrl: 'thumbnail.jpg',
      previewVideoURL: 'video.mp4',
      tags: [
        { id: 1, name: 'Tag1' },
        { id: 2, name: 'Tag2' },
      ],
      prerequisite: ['Prerequisite1'],
      courseOutcomes: ['Outcome1'],
      sections: [{}, {}],
    };

    const progress = service.calculateCourseProgress();
    expect(progress).toBe('29.70'); // Example progress value
  });
  it('should fetch topic type id', () => {
    service.topicTypes = [
      { name: 'Video', id: 1 },
      { name: 'Quiz', id: 2 },
    ];
    const typeId = service.fetchTopicTypeId('Video');
    expect(typeId).toBe(1);
  });

  describe('modifyCourseTags', () => {
    it('should deactivate tags not present in the provided array', () => {
      service.tags = [
        { id: 1, name: 'Tag1', active: true },
        { id: 2, name: 'Tag2', active: true },
      ];
      const tagsArray = [{ id: 1, name: 'Tag1' }];

      const result = service.modifyCourseTags(tagsArray);

      expect(result).toEqual([
        { id: 1, name: 'Tag1', active: true },
        { id: 2, name: 'Tag2', active: false },
      ]);
    });

    it('should add new tags with null id to the tags array', () => {
      service.tags = [{ id: 1, name: 'Tag1', active: true }];
      const tagsArray = [{ id: null, name: 'NewTag' }];

      const result = service.modifyCourseTags(tagsArray);

      expect(result).toBeDefined();
    });

    it('should handle empty tagsArray', () => {
      service.tags = [{ id: 1, name: 'Tag1', active: true }];
      const tagsArray = [];

      const result = service.modifyCourseTags(tagsArray);

      expect(result).toEqual([{ id: 1, name: 'Tag1', active: false }]);
    });
  });

  describe('fetchPrerequisite', () => {
    it('should return an array containing the provided prerequisite', () => {
      const prerequisite = 'Introduction to Programming';
      const result = service.fetchPrerequisite(prerequisite);

      expect(result).toEqual([prerequisite]);
    });

    it('should handle undefined prerequisite', () => {
      const result = service.fetchPrerequisite();

      expect(result).toEqual([undefined]);
    });
  });

  describe('fetchCourseSummariesInfo', () => {
    it('should return an array of courseSummaryInfo from courseSummaries', () => {
      const courseSummaries = [
        { courseSummaryInfo: 'Info1' },
        { courseSummaryInfo: 'Info2' },
        { otherInfo: 'OtherInfo' },
      ];

      const result = service.fetchCourseSummariesInfo(courseSummaries);

      expect(result).toEqual(['Info1', 'Info2']);
    });

    it('should return an empty array if no courseSummaries are provided', () => {
      const result = service.fetchCourseSummariesInfo();

      expect(result).toEqual([]);
    });

    it('should return an empty array if courseSummaries has no courseSummaryInfo', () => {
      const courseSummaries = [{ otherInfo: 'OtherInfo' }];

      const result = service.fetchCourseSummariesInfo(courseSummaries);

      expect(result).toEqual([]);
    });
  });

  describe('courseTitleExist', () => {
    it('should make a POST request to check if the course title exists', () => {
      const title = 'New Course Title';
      const courseId = 123;
      const mockResponse = { exists: true };

      service.courseTitleExist(title, courseId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/unique-course-title`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ courseId, courseTitle: title });
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const title = 'New Course Title';
      const courseId = 123;

      service.courseTitleExist(title, courseId).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/unique-course-title`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  // describe('getCourseByTitle', () => {
  //   it('should make a POST request to get course by title', () => {
  //     const title = 'Course Title';
  //     const mockResponse = { course: 'Course details' };

  //     service.getCourseByUrl(title).subscribe((response) => {
  //       expect(response).toEqual(mockResponse);
  //     });

  //     const req = httpTestingController.expectOne(
  //       `${environment.baseUrl}course/course-title`
  //     );
  //     expect(req.request.method).toBe('POST');
  //     expect(req.request.body).toEqual({ courseTitle: title });
  //     req.flush(mockResponse);
  //   });

  //   it('should handle errors gracefully', () => {
  //     const title = 'Course Title';

  //     service.getCourseByUrl(title).subscribe(
  //       () => fail('expected an error, not a response'),
  //       (error) => expect(error.status).toBe(500)
  //     );

  //     const req = httpTestingController.expectOne(
  //       `${environment.baseUrl}course/course-title`
  //     );
  //     req.flush('Server error', { status: 500, statusText: 'Server Error' });
  //   });
  // });

  describe('calculateCourseProgress', () => {
    it('should return the total course progress as a string', () => {
      spyOn(service, 'calculateCourseInformationProgress').and.returnValue(25);
      spyOn(service, 'calculateSectionProgress').and.returnValue(75);

      const result = service.calculateCourseProgress();

      expect(result).toBe('100.00');
    });

    it('should handle zero progress values', () => {
      spyOn(service, 'calculateCourseInformationProgress').and.returnValue(0);
      spyOn(service, 'calculateSectionProgress').and.returnValue(0);

      const result = service.calculateCourseProgress();

      expect(result).toBe('0.00');
    });
  });
  describe('getSuggestions', () => {
    it('should make a POST request to get suggestions', () => {
      const input = 'test';
      const mockResponse = ['suggestion1', 'suggestion2'];

      service.getSuggestions(input).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/autocomplete`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ input: input.trim() });
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const input = 'test';

      service.getSuggestions(input).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/autocomplete`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('courseProgress', () => {
    it('should make a GET request to get course progress', () => {
      const courseId = 123;
      const mockResponse = { progress: 75 };

      service.courseProgress(courseId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}user-course-progress/${courseId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const courseId = 123;

      service.courseProgress(courseId).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}user-course-progress/${courseId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getInstructorPublicProfile', () => {
    it('should make a GET request to get the instructor public profile', () => {
      const profileUrl = "john";
      const mockResponse = { profile: 'Instructor profile' };

      service.getInstructorPublicProfile(profileUrl).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}user-profile/?profileUrl=${profileUrl}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const profileUrl = "john";

      service.getInstructorPublicProfile(profileUrl).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}user-profile/?profileUrl=${profileUrl}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getInstructorProfile', () => {
    it('should make a GET request to get the instructor profile', () => {
      const mockResponse = { profile: 'Instructor profile' };

      service.getInstructorProfile().subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}user-profile/`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      service.getInstructorProfile().subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}user-profile/`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getInstructorCourses', () => {
    it('should make a GET request with instructorId and pagination params', () => {
      const body = { instructorId: 789, pageNo: 1, pageSize: 10 };
      const mockResponse = ['course1', 'course2'];

      service.getInstructorCourses(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/course-by-teacher-for-profile?instructorId=${body.instructorId}&pageNo=${body.pageNo}&pageSize=${body.pageSize}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should make a GET request without instructorId and with pagination params', () => {
      const body = { pageNo: 1, pageSize: 10 };
      const mockResponse = ['course1', 'course2'];

      service.getInstructorCourses(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/course-by-teacher-for-profile?pageNo=${body.pageNo}&pageSize=${body.pageSize}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const body = { instructorId: 789, pageNo: 1, pageSize: 10 };

      service.getInstructorCourses(body).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/course-by-teacher-for-profile?instructorId=${body.instructorId}&pageNo=${body.pageNo}&pageSize=${body.pageSize}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getCourseLevels', () => {
    it('should make a GET request to get course levels', () => {
      const mockResponse = ['level1', 'level2'];

      service.getCourseLevels().subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course-level/`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      service.getCourseLevels().subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course-level/`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getCourseFirstStepDetail', () => {
    it('should make a GET request to get course first step details', () => {
      const courseId = 123;
      const mockResponse = { step: 'First step details' };

      service.getCourseFirstStepDetail(courseId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/course-detail-for-update-first-step/${courseId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const courseId = 123;

      service.getCourseFirstStepDetail(courseId).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/course-detail-for-update-first-step/${courseId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getSectionByCourseId', () => {
    it('should make a GET request to get sections by course ID', () => {
      const courseId = 123;
      const mockResponse = ['section1', 'section2'];

      service.getSectionByCourseId(courseId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}section/section-for-update/${courseId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const courseId = 123;

      service.getSectionByCourseId(courseId).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}section/section-for-update/${courseId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getTopicsBySectionId', () => {
    it('should make a GET request to get topics by section ID', () => {
      const sectionId = 456;
      const mockResponse = ['topic1', 'topic2'];

      service.getTopicsBySectionId(sectionId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}topic/section/${sectionId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const sectionId = 456;

      service.getTopicsBySectionId(sectionId).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}topic/section/${sectionId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getTagsByCourseId', () => {
    it('should make a GET request to get tags by course ID', () => {
      const courseId = 789;
      const mockResponse = ['tag1', 'tag2'];

      service.getTagsByCourseId(courseId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}tag/${courseId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const courseId = 789;

      service.getTagsByCourseId(courseId).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}tag/${courseId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('createCourse', () => {
    it('should make a POST request to create a course', () => {
      const body = { course: 'new course' };
      const mockResponse = { success: true };

      service.createCourse(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/create`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const body = { course: 'new course' };

      service.createCourse(body).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/create`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });
  describe('enrolledInCourse', () => {
    it('should make a POST request to enroll in a course', () => {
      const courseId = 123;
      const mockResponse = { success: true };

      service.enrolledInCourse(courseId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}enrollment/?courseId=${courseId}`
      );
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const courseId = 123;

      service.enrolledInCourse(courseId).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}enrollment/?courseId=${courseId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('addOrRemoveCourseToFavorite', () => {
    it('should make a POST request to add or remove a course from favorites', () => {
      const courseId = 123;
      const mockResponse = { success: true };

      service.addOrRemoveCourseToFavorite(courseId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}favourite-course/?courseId=${courseId}`
      );
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const courseId = 123;

      service.addOrRemoveCourseToFavorite(courseId).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}favourite-course/?courseId=${courseId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('youtubeVideoUrlUpload', () => {
    it('should make a GET request to get YouTube video duration', () => {
      const videoId = 'abc123';
      const mockResponse = { duration: 120 };

      service.youtubeVideoUrlUpload(videoId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}youtube-video/duration?videoId=${videoId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const videoId = 'abc123';

      service.youtubeVideoUrlUpload(videoId).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}youtube-video/duration?videoId=${videoId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getCourseSections', () => {
    it('should make a GET request to get course sections', () => {
      const courseId = 123;
      const mockResponse = ['section1', 'section2'];

      service.getCourseSections(courseId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}section/${courseId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const courseId = 123;

      service.getCourseSections(courseId).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}section/${courseId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getSectionTopics', () => {
    it('should make a GET request to get section topics by course and section ID', () => {
      const courseId = 123;
      const sectionId = 456;
      const mockResponse = ['topic1', 'topic2'];

      service.getSectionTopics(courseId, sectionId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}topic/course/${courseId}/section/${sectionId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const courseId = 123;
      const sectionId = 456;

      service.getSectionTopics(courseId, sectionId).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}topic/course/${courseId}/section/${sectionId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('rateAndReviewCourse', () => {
    it('should make a POST request to rate and review a course', () => {
      const body = { rating: 5, review: 'Excellent course!' };
      const mockResponse = { success: true };

      service.rateAndReviewCourse(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course-review/`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const body = { rating: 5, review: 'Excellent course!' };

      service.rateAndReviewCourse(body).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course-review/`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getCourseRatingAndReview', () => {
    it('should make a GET request to get course rating and review', () => {
      const courseId = 123;
      const mockResponse = { rating: 4.5, reviews: ['review1', 'review2'] };

      service.getCourseRatingAndReview(courseId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course-review/${courseId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const courseId = 123;

      service.getCourseRatingAndReview(courseId).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course-review/${courseId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('rateAndReviewSection', () => {
    it('should make a POST request to rate and review a section', () => {
      const body = { sectionId: 456, rating: 5, review: 'Great section!' };
      const mockResponse = { success: true };

      service.rateAndReviewSection(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}section-review/`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const body = { sectionId: 456, rating: 5, review: 'Great section!' };

      service.rateAndReviewSection(body).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}section-review/`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('likeAndDislikeReviewSection', () => {
    it('should make a POST request to like or dislike a review section', () => {
      const body = { reviewId: 789, action: 'like' };
      const mockResponse = { success: true };

      service.likeAndDislikeReviewSection(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course-review/like/${body.reviewId}/${body.action}`
      );
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const body = { reviewId: 789, action: 'like' };

      service.likeAndDislikeReviewSection(body).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course-review/like/${body.reviewId}/${body.action}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getSectionRatingAndReview', () => {
    it('should make a GET request to get section rating and review', () => {
      const sectionId = 456;
      const mockResponse = { rating: 4.0, reviews: ['review1', 'review2'] };

      service.getSectionRatingAndReview(sectionId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}section-review/${sectionId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const sectionId = 456;

      service.getSectionRatingAndReview(sectionId).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}section-review/${sectionId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getTopicSummary', () => {
    it('should make a GET request to get topic summary', () => {
      const topicId = 789;
      const mockResponse = { summary: 'This is a topic summary.' };

      service.getTopicSummary(topicId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}topic/summary/${topicId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const topicId = 789;

      service.getTopicSummary(topicId).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}topic/summary/${topicId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getQuestions', () => {
    it('should make a GET request to get questions by course ID', () => {
      const body = { courseId: 123, pageNo: 1, pageSize: 10 };
      const mockResponse = [
        { questionId: 1, questionText: 'What is Angular?' },
      ];

      service.getQuestions(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}question/?courseId=${body.courseId}&pageNo=${body.pageNo}&pageSize=${body.pageSize}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const body = { courseId: 123, pageNo: 1, pageSize: 10 };

      service.getQuestions(body).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}question/?courseId=${body.courseId}&pageNo=${body.pageNo}&pageSize=${body.pageSize}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getQuestionsReplies', () => {
    it('should make a GET request to get replies to a question', () => {
      const body = { courseId: 123, questionId: 456, pageNo: 1, pageSize: 10 };
      const mockResponse = [
        { answerId: 1, answerText: 'Angular is a framework.' },
      ];

      service.getQuestionsReplies(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}answer/?courseId=${body.courseId}&questionId=${body.questionId}&pageNo=${body.pageNo}&pageSize=${body.pageSize}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const body = { courseId: 123, questionId: 456, pageNo: 1, pageSize: 10 };

      service.getQuestionsReplies(body).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}answer/?courseId=${body.courseId}&questionId=${body.questionId}&pageNo=${body.pageNo}&pageSize=${body.pageSize}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('createQuestion', () => {
    it('should make a POST request to create a question', () => {
      const body = { courseId: 123, questionText: 'What is Angular?' };
      const mockResponse = { success: true };

      service.createQuestion(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}question/`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const body = { courseId: 123, questionText: 'What is Angular?' };

      service.createQuestion(body).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}question/`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('replyQuestion', () => {
    it('should make a POST request to reply to a question', () => {
      const body = { questionId: 456, answerText: 'Angular is a framework.' };
      const mockResponse = { success: true };

      service.replyQuestion(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}answer/`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const body = { questionId: 456, answerText: 'Angular is a framework.' };

      service.replyQuestion(body).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}answer/`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getCourseRatingReviewAndFeedback', () => {
    it('should make a GET request to get course rating, review, and feedback', () => {
      const body = { courseId: 123, pageNo: 1, pageSize: 10 };
      const mockResponse = [{ rating: 4.5, review: 'Excellent course!' }];

      service.getCourseRatingReviewAndFeedback(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course-review/?courseId=${body.courseId}&pageNo=${body.pageNo}&pageSize=${body.pageSize}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const body = { courseId: 123, pageNo: 1, pageSize: 10 };

      service.getCourseRatingReviewAndFeedback(body).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course-review/?courseId=${body.courseId}&pageNo=${body.pageNo}&pageSize=${body.pageSize}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getSectionAndTopicsChatQuestion', () => {
    it('should make a GET request to get chat questions for a section and topics', () => {
      const courseId = 123;
      const mockResponse = [{ chatId: 1, question: 'How do I start?' }];

      service
        .getSectionAndTopicsChatQuestion(courseId)
        .subscribe((response) => {
          expect(response).toEqual(mockResponse);
        });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}chat/?courseId=${courseId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const courseId = 123;

      service.getSectionAndTopicsChatQuestion(courseId).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}chat/?courseId=${courseId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getCourseChatHistory', () => {
    it('should make a GET request to get course chat history', () => {
      const chatId = 1;
      const mockResponse = [{ message: 'Hello!' }];

      service.getCourseChatHistory(chatId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}chat-history/${chatId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const chatId = 1;

      service.getCourseChatHistory(chatId).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}chat-history/${chatId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('sendMessageInChat', () => {
    it('should make a POST request to send a message in chat', () => {
      const body = { chatId: 1, message: 'Hello!' };
      const mockResponse = { success: true };

      service.sendMessageInChat(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}chat/`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const body = { chatId: 1, message: 'Hello!' };

      service.sendMessageInChat(body).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}chat/`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('createTopicNote', () => {
    it('should make a POST request to create a topic note', () => {
      const body = { courseId: 123, topicId: 456, note: 'Important note.' };
      const mockResponse = { success: true };

      service.createTopicNote(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}topic-notes/`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const body = { courseId: 123, topicId: 456, note: 'Important note.' };

      service.createTopicNote(body).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}topic-notes/`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getTopicNotes', () => {
    it('should make a GET request to get topic notes', () => {
      const body = { courseId: 123, pageNo: 1, pageSize: 10 };
      const mockResponse = [{ noteId: 1, noteText: 'This is a note.' }];

      service.getTopicNotes(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}topic-notes/?courseId=${body.courseId}&pageNo=${body.pageNo}&pageSize=${body.pageSize}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const body = { courseId: 123, pageNo: 1, pageSize: 10 };

      service.getTopicNotes(body).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}topic-notes/?courseId=${body.courseId}&pageNo=${body.pageNo}&pageSize=${body.pageSize}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('deleteTopicNote', () => {
    it('should make a DELETE request to delete a topic note', () => {
      const body = { courseId: 123, topicId: 456, topicNoteId: 789 };
      const mockResponse = { success: true };

      service.deleteTopicNote(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}topic-notes/?courseId=${body.courseId}&topicId=${body.topicId}&topicNoteId=${body.topicNoteId}`
      );
      expect(req.request.method).toBe('DELETE');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const body = { courseId: 123, topicId: 456, topicNoteId: 789 };

      service.deleteTopicNote(body).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}topic-notes/?courseId=${body.courseId}&topicId=${body.topicId}&topicNoteId=${body.topicNoteId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('validateQuizAnswer', () => {
    it('should make a POST request to validate a quiz answer', () => {
      const body = { questionId: 1, answerId: 2 };
      const mockResponse = { isValid: true };

      service.validateQuizAnswer(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}quiz/validate-answer?questionId=${body.questionId}&answerId=${body.answerId}`
      );
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const body = { questionId: 1, answerId: 2 };

      service.validateQuizAnswer(body).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}quiz/validate-answer?questionId=${body.questionId}&answerId=${body.answerId}`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('markTopicComplete', () => {
    it('should make a POST request to mark a topic as complete', () => {
      const body = { userId: 1, topicId: 456 };
      const mockResponse = { success: true };

      service.markTopicComplete(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}user-course-progress/`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });

    it('should handle errors gracefully', () => {
      const body = { userId: 1, topicId: 456 };

      service.markTopicComplete(body).subscribe(
        () => fail('expected an error, not a response'),
        (error) => expect(error.status).toBe(500)
      );

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}user-course-progress/`
      );
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getCourseContent', () => {
    it('should make a GET request to get course content', () => {});
  });

  describe('passFavoriteCoursesFromLandingPageToNavbar', () => {
    it('should update favoriteCourses BehaviorSubject', () => {
      const courseList = [{ id: 1, title: 'Course 1' }];
      service.passFavoriteCoursesFromLandingPageToNavbar(courseList);

      service['favoriteCourses'].subscribe((value) => {
        expect(value).toEqual(courseList);
      });
    });
  });

  describe('searchResults$', () => {
    it('should emit new search results as an object', () => {
      const results = [{ id: 1, name: 'Result 1' }];
      const nlpResults = [{ id: 1, name: 'Result 1' }];
      service.setSearchResults(results, nlpResults); // assuming this sets both results and nlpResults
      service.searchResults$.subscribe((value) => {
        expect(value).toBeDefined();
      });
    });
  });

  describe('searchSuggestionsIds$', () => {
    it('should emit new search suggestion ids', () => {
      const ids = [1, 2, 3];
      service.setSearchKeyword(ids);

      service.$searchSuggestionsIds.subscribe((value) => {
        expect(value).toEqual(ids);
      });
    });
  });

  describe('passTopicToVideoPlayer', () => {
    it('should emit new topic', () => {
      const topic = { id: 1, title: 'Topic 1' };
      service.passTopicToVideoPlayer(topic);

      service.$selectedTopic.subscribe((value) => {
        expect(value).toEqual(topic);
      });
    });
  });

  describe('passPreviewVideoToVideoPlayer', () => {
    it('should emit new preview video', () => {
      const video = { id: 1, url: 'http://example.com/video' };
      service.passPreviewVideoToVideoPlayer(video);

      service.$previewVideo.subscribe((value) => {
        expect(value).toEqual(video);
      });
    });
  });

  describe('currentVideoTime$', () => {
    it('should emit a value when getTime is called', () => {
      service.getTime();

      service.currentVideoTime$.subscribe((value) => {
        expect(value).toBe(true);
      });
    });
  });

  describe('getCourseCategory', () => {
    it('should make a GET request to fetch course categories if logged in', () => {
      authService.isLoggedIn.and.returnValue(true);
      const mockResponse = [{ id: 1, name: 'Category 1' }];

      service.getCourseCategory().subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course-category/`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should make a GET request to fetch course categories if not logged in', () => {
      authService.isLoggedIn.and.returnValue(false);
      const mockResponse = [{ id: 1, name: 'Category 1' }];

      service.getCourseCategory().subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course-category/`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('getCoursesByCategory', () => {
    it('should make a POST request to fetch courses by category if logged in', () => {
      authService.isLoggedIn.and.returnValue(true);
      const body = { categoryId: 1 };
      const mockResponse = [{ id: 1, title: 'Course 1' }];

      service.getCoursesByCategory(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/course-by-category`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });

    it('should make a POST request to fetch courses by category if not logged in', () => {
      authService.isLoggedIn.and.returnValue(false);
      const body = { categoryId: 1 };
      const mockResponse = [{ id: 1, title: 'Course 1' }];

      service.getCoursesByCategory(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/course-by-category`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });
  });

  describe('getCourseDetails', () => {
    it('should make a GET request to fetch course details if logged in', () => {
      authService.isLoggedIn.and.returnValue(true);
      const courseId = 1;
      const mockResponse = { id: 1, title: 'Course 1' };

      service.getCourseDetails(courseId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/get/${courseId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should make a GET request to fetch course details if not logged in', () => {
      authService.isLoggedIn.and.returnValue(false);
      const courseId = 1;
      const mockResponse = { id: 1, title: 'Course 1' };

      service.getCourseDetails(courseId).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/get/${courseId}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('getRelatedCourses', () => {
    it('should make a POST request to fetch related courses if logged in', () => {
      authService.isLoggedIn.and.returnValue(true);
      const body = { courseId: 1 };
      const mockResponse = [{ id: 2, title: 'Related Course' }];

      service.getRelatedCourses(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/get-related-courses`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });

    it('should make a POST request to fetch related courses if not logged in', () => {
      authService.isLoggedIn.and.returnValue(false);
      const body = { courseId: 1 };
      const mockResponse = [{ id: 2, title: 'Related Course' }];

      service.getRelatedCourses(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}course/get-related-courses`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });
  });

  describe('getFavoriteCourses', () => {
    it('should make a GET request to fetch favorite courses with title filter', () => {
      const body = { pageNo: 1, pageSize: 10, title: 'Course' };
      const mockResponse = [{ id: 1, title: 'Favorite Course' }];

      service.getFavoriteCourses(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}favourite-course/?pageNo=${body.pageNo}&pageSize=${body.pageSize}&title=${body.title}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should make a GET request to fetch favorite courses without title filter', () => {
      const body = { pageNo: 1, pageSize: 10 };
      const mockResponse = [{ id: 1, title: 'Favorite Course' }];

      service.getFavoriteCourses(body).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(
        `${environment.baseUrl}favourite-course/?pageNo=${body.pageNo}&pageSize=${body.pageSize}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('pinAlternateInstructor', () => {
    it('should send a POST request with correct URL and parameters', () => {
      const toCourseId = 1;
      const toSectionId = 2;
      const fromCourseId = 3;
      const fromSectionId = 4;

      service
        .pinAlternateInstructor(
          toCourseId,
          toSectionId,
          fromCourseId,
          fromSectionId
        )
        .subscribe((response) => {
          expect(response).toBeTruthy();
        });

      const expectedUrl = `${
        environment.baseUrl
      }alternate-section/?courseId=${encodeURIComponent(
        toCourseId
      )}&sectionId=${encodeURIComponent(
        toSectionId
      )}&fromCourseId=${encodeURIComponent(
        fromCourseId
      )}&fromSectionId=${encodeURIComponent(fromSectionId)}`;

      const req = httpTestingController.expectOne(expectedUrl);
      expect(req.request.method).toBe('POST');
      req.flush({}); // Mocking empty response
    });
  });

  describe('unPinAlternateInstructor', () => {
    it('should send a DELETE request with correct URL and parameters', () => {
      const courseId = 1;
      const sectionId = 2;

      service
        .unPinAlternateInstructor(courseId, sectionId)
        .subscribe((response) => {
          expect(response).toBeTruthy();
        });

      const expectedUrl = `${
        environment.baseUrl
      }alternate-section/?courseId=${encodeURIComponent(
        courseId
      )}&sectionId=${encodeURIComponent(sectionId)}`;

      const req = httpTestingController.expectOne(expectedUrl);
      expect(req.request.method).toBe('DELETE');
      req.flush({}); // Mocking empty response
    });
  });

  describe('calculateCourseProgress', () => {
    it('should calculate total course progress correctly', () => {
      spyOn(service, 'calculateCourseInformationProgress').and.returnValue(20);
      spyOn(service, 'calculateSectionProgress').and.returnValue(10);

      const progress = service.calculateCourseProgress();
      expect(progress).toBe('30.00'); // 20 + 10 = 30
    });
  });

  describe('calculateCourseInformationProgress', () => {
    it('should calculate course information progress correctly', () => {
      service.course = {
        title: 'Course Title',
        description: 'Course Description',
        categoryId: 1,
        courseLevelId: 1,
        about: 'About course',
        thumbnailUrl: 'thumbnail.jpg',
        previewVideoURL: 'video.mp4',
        tags: [{ id: 1, active: true }],
        prerequisite: [''],
        courseOutcomes: ['Outcome 1'],
      };

      const progress = service.calculateCourseInformationProgress();
      expect(progress).toBeCloseTo(29.7);
    });
  });
  describe('calculateSectionProgress', () => {
    it('should calculate section progress correctly', () => {
      service.course = {
        sections: [
          {
            delete: false,
            title: 'Section Title',
            topics: [{ validate: true }],
          },
        ],
      };

      const progress = service.calculateSectionProgress();
      expect(progress).toBe(34); // 4 (for title) + 30 (for topics)
    });
  });
  describe('getVideoData', () => {
    it('should map video data correctly', () => {
      const video = {
        videoData: {
          videoId: 'vid123',
          videoFileName: 'video.mp4',
          delete: false,
          videoUrl: 'url',
          videoSummary: 'Summary',
          videoTranscript: 'Transcript',
          videoSubtitles: 'VttContent',
        },
        documentData: {
          documents: [
            {
              id: 1,
              delete: false,
              summary: 'Document summary',
              documentFileName: 'doc.pdf',
              documentUrl: 'doc-url',
            },
          ],
        },
      };

      const videoData = service.getVideoData(video);
      expect(videoData).toEqual({
        id: 'vid123',
        filename: 'video.mp4',
        delete: false,
        videoURL: 'url',
        summary: 'Summary',
        transcribe: 'Transcript',
        vttContent: 'VttContent',
        documents: [
          {
            id: 1,
            delete: false,
            summary: 'Document summary',
            docName: 'doc.pdf',
            docUrl: 'doc-url',
          },
        ],
      });
    });
  });
  describe('fetchTopicTypeId', () => {
    it('should return the correct topic type ID', () => {
      service.topicTypes = [
        { id: 1, name: 'Type A' },
        { id: 2, name: 'Type B' },
      ];

      const topicTypeId = service.fetchTopicTypeId('Type B');
      expect(topicTypeId).toBe(2);
    });
  });

  describe('createTopics', () => {
    it('should create topics with correct data', () => {
      spyOn(service, 'getVideoData').and.callFake((video) => ({
        id: video.videoData.videoId,
        filename: video.videoData.videoFileName,
        delete: video.videoData.delete,
        videoURL: video.videoData.videoUrl,
        summary: video.videoData.videoSummary,
        transcribe: video.videoData.videoTranscript,
        documents: [], // Stubbed for simplicity
      }));
      spyOn(service, 'getArticleData').and.callFake((article) => ({
        id: article.articleId,
        delete: article.delete,
        article: article.content,
        documents: article.articleDocumnetUrl
          ? [
              /* Stubbed documents */
            ]
          : null,
      }));
      spyOn(service, 'getQuizData').and.callFake((quiz) => ({
        id: quiz.quizId,
        delete: quiz.delete,
        title: quiz.title,
        questions: [], // Stubbed for simplicity
      }));
      spyOn(service, 'fetchTopicTypeId').and.returnValue(1);

      const section = {
        topics: [
          {
            topicId: 1,
            validate: true,
            selectedContentType: service.typeVideo,
            video: {
              videoData: {
                videoId: 'vid123',
                videoFileName: 'video.mp4',
                delete: false,
                videoUrl: 'url',
                videoSummary: 'Summary',
                videoTranscript: 'Transcript',
              },
            },
            article: {
              articleId: 'art123',
              delete: false,
              content: 'Article Content',
              articleDocumnetUrl: 'url',
              articleFileName: 'file.pdf',
              articleSummary: 'Summary',
            },
            quiz: {
              quizId: 'quiz123',
              delete: false,
              title: 'Quiz Title',
              questions: [],
            },
          },
        ],
      };

      const topics = service.createTopics(section);
      expect(topics.length).toBe(1);
      expect(topics[0].id).toBe(1);
      expect(topics[0].video.id).toBe('vid123');
      expect(topics[0].article.id).toBe('art123');
      expect(topics[0].quiz.id).toBe('quiz123');
    });
  });

  describe('getQuizData', () => {
    it('should map quiz data correctly', () => {
      const quiz = {
        quizId: 'quiz123',
        delete: false,
        title: 'Quiz Title',
        questions: [{ questionId: 'q1', text: 'Question 1' }],
      };

      const result = service.getQuizData(quiz);
      expect(result).toBeDefined();
    });
  });
  describe('getArticleDocument', () => {
    it('should map article document data correctly', () => {
      const article = {
        articleDocumnetId: 'doc123',
        delete: false,
        articleDocumnetUrl: 'url',
        articleFileName: 'file.pdf',
        articleSummary: 'Summary',
      };

      const result = service.getArticleDocument(article);
      expect(result).toEqual([
        {
          id: 'doc123',
          delete: false,
          docName: 'file.pdf',
          docUrl: 'url',
          summary: 'Summary',
        },
      ]);
    });
  });
  describe('getArticleData', () => {
    it('should map article data correctly', () => {
      const article = {
        articleId: 'art123',
        delete: false,
        content: 'Article Content',
        articleDocumnetUrl: 'url',
        articleFileName: 'file.pdf',
        articleSummary: 'Summary',
      };

      const result = service.getArticleData(article);
      expect(result).toEqual({
        id: 'art123',
        delete: false,
        article: 'Article Content',
        documents: [
          {
            id: undefined,
            delete: false,
            docName: 'file.pdf',
            docUrl: 'url',
            summary: 'Summary',
          },
        ],
      });
    });
  });
});
