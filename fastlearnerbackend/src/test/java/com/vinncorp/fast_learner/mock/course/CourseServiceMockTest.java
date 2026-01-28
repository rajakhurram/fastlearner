package com.vinncorp.fast_learner.mock.course;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.dtos.course.CourseDropdown;
import com.vinncorp.fast_learner.dtos.topic.NoOfTopicInCourse;
import com.vinncorp.fast_learner.es_models.CourseContent;
import com.vinncorp.fast_learner.es_services.IESCourseService;
import com.vinncorp.fast_learner.es_services.course_content.IESCourseContentService;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.CreateCourseValidationException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.section.SectionTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.mock.user.user_profile.UserProfileTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseCategory;
import com.vinncorp.fast_learner.models.course.CourseLevel;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.quiz.QuizQuestion;
import com.vinncorp.fast_learner.models.role.Role;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.subscription.SubscriptionValidations;
import com.vinncorp.fast_learner.models.tag.Tag;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.models.topic.TopicType;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user.UserProfile;
import com.vinncorp.fast_learner.models.video.Video;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.request.course.CourseByCategoryRequest;
import com.vinncorp.fast_learner.request.course.CreateCourseRequest;
import com.vinncorp.fast_learner.request.course.RelatedCoursesRequest;
import com.vinncorp.fast_learner.request.course.SearchCourseRequest;
import com.vinncorp.fast_learner.request.quiz.CreateQuizQuestionAnswerRequest;
import com.vinncorp.fast_learner.response.course.*;
import com.vinncorp.fast_learner.response.course.nlp_search.CourseResponse;
import com.vinncorp.fast_learner.response.subscriptionpermission.SubscriptionPermissionResponse;
import com.vinncorp.fast_learner.services.article.IArticleService;
import com.vinncorp.fast_learner.services.course.*;
import com.vinncorp.fast_learner.services.course.course_review.CourseReviewService;
import com.vinncorp.fast_learner.services.docs.IDocumentService;
import com.vinncorp.fast_learner.services.enrollment.EnrollmentService;
import com.vinncorp.fast_learner.services.home_page.IHomePageService;
import com.vinncorp.fast_learner.services.quiz.IQuizQuestionAnswerService;
import com.vinncorp.fast_learner.services.quiz.IQuizQuestionService;
import com.vinncorp.fast_learner.services.quiz.IQuizService;
import com.vinncorp.fast_learner.services.section.ISectionService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionValidationsService;
import com.vinncorp.fast_learner.services.tag.ITagService;
import com.vinncorp.fast_learner.services.topic.ITopicService;
import com.vinncorp.fast_learner.services.topic.ITopicTypeService;
import com.vinncorp.fast_learner.services.user.IUserProfileService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.services.video.IVideoService;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.repositories.course.CourseRepository;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.*;
import jakarta.persistence.Tuple;
import org.apache.poi.ss.formula.functions.T;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CourseServiceMockTest {

    private final String EMAIL = "qasim@mailinator.com";
    private static String COURSE_TITLE = "Java Basics";
    private static String COURSE_URL = "java-basics";

    private static Long COURSE_ID = 1L;

    @InjectMocks
    private CourseService courseService;

    @Mock
    private IUserService userService;
    @Mock
    private ICourseCategoryService courseCategoryService;
    @Mock
    private ICourseLevelService courseLevelService;
    @Mock
    private CourseRepository repo;
    @Mock
    private IESCourseService esCourseService;
    @Mock
    private RabbitMQProducer rabbitMQProducer;

    @Mock
    private ITagService tagService;
    @Mock
    private Course course;
    @Mock
    private ISectionService sectionService;
    @Mock
    private ITopicTypeService topicTypeService;
    @Mock
    private ITopicService topicService;
    @Mock
    private IQuizService quizService;
    @Mock
    private IQuizQuestionService quizQuestionService;
    @Mock
    private IQuizQuestionAnswerService quizQuestionAnswerService;
    @Mock
    IVideoService videoService;
    @Mock
    IDocumentService documentService;
    @Mock
    IArticleService articleService;
    @Mock
    EnrollmentService enrollmentService;
    @Mock
    CourseVisitorService courseVisitorService;
    @Mock
    CourseReviewService courseReviewService;
    @Mock
    ICourseUrlService courseUrlService;
    @Mock
    IHomePageService homePageService;
    @Mock
    RestTemplate restTemplate;
    @Mock
    IUserProfileService userProfileService;
    @Mock
    IESCourseContentService esCourseContentService;
    @Mock
    ISubscribedUserService subscribedUserService;
    @Mock
    private UserRepository userRepository;

    @Mock
    private ISubscriptionValidationsService subscriptionValidationsService;

    private CourseCategory courseCategory;
    private CourseLevel courseLevel;
    private TopicType topicTypeVideo;
    private TopicType topicTypeQuiz;
    private TopicType topicTypeArticle;

    private Page<Tuple> mockPage;
    private Section mockSection;
    private Topic mockTopic;
    private Video mockVideo;
    private QuizQuestion quizQuestion;
    private Video video;
    private TopicType mockTopicType;
    private com.vinncorp.fast_learner.es_models.Course esCourse;
    List<Tuple> relatedCourseTuples;
    private Page<Tuple> courseDetails;
    private Tuple mockCourseTuple;
    private Tuple userStatsTuple;

    @Value("${search.related.courses.api}")
    private String NLP_SEARCH_PYTHON_SERVICE;
    @Value("${transcript.generation.auth-key}")
    private String AUTH_TOKEN;

    @BeforeEach
    public void init() throws IOException {
        MockitoAnnotations.openMocks(this);
        CreateCourseRequest createCourseRequest = this.getMockCourse();
        File createCourseRequestJsonFile = new File("src/main/resources/testing/CreateCourseRequest.json");
        ObjectMapper objectMapper = new ObjectMapper();
        createCourseRequest = objectMapper.readValue(createCourseRequestJsonFile, CreateCourseRequest.class);
        courseCategory = CourseCategory.builder().id(1L).name("Development").isActive(true).build();
        courseLevel = CourseLevel.builder().id(1L).name("Intermediate").isActive(true).build();
        topicTypeVideo = TopicType.builder().id(1L).name("Video").isActive(true).build();
        topicTypeArticle = TopicType.builder().id(2L).name("Article").isActive(true).build();
        topicTypeQuiz = TopicType.builder().id(4L).name("Quiz").isActive(true).build();

        StringBuilder documentVector = new StringBuilder();
        documentVector.append(createCourseRequest.getTitle()).append(' ');
        documentVector.append(createCourseRequest.getAbout()).append(' ');
        documentVector.append(courseCategory.getName()).append(' ');
        createCourseRequest.getTags().forEach(e -> documentVector.append(e.getName()).append(' '));

        course = Course.builder()
                .about(createCourseRequest.getAbout())
                .courseLevel(courseLevel)
                .courseCategory(courseCategory)
                .courseDurationInHours(createCourseRequest.getCourseDuration())
                .instructor(UserTestData.userData())
                .description(createCourseRequest.getDescription())
                .title(createCourseRequest.getTitle().trim())
                .prerequisite(!CollectionUtils.isEmpty(createCourseRequest.getPrerequisite()) ? String.join("~", createCourseRequest.getPrerequisite()).trim() : null)
                .courseOutcome(!CollectionUtils.isEmpty(createCourseRequest.getCourseOutcomes()) ? String.join("~", createCourseRequest.getCourseOutcomes()).trim() : null)
                .previewVideoURL(createCourseRequest.getPreviewVideoURL())
                .thumbnail(createCourseRequest.getThumbnailUrl())
                .documentVector(documentVector.toString())
                .courseStatus(CourseStatus.PUBLISHED)
                .build();
        course.setCreationDate(new Date());
        course.setCreatedBy(UserTestData.userData().getId());

        esCourse = new com.vinncorp.fast_learner.es_models.Course();
        esCourse.setDbId(course.getId());
        esCourse.setTitle(course.getTitle());
        esCourse.setDocVector(course.getDocumentVector());

        Tuple mockTuple = mock(Tuple.class);
        when(mockTuple.get("id")).thenReturn(1L);
        when(mockTuple.get("title")).thenReturn("Test Course");
        when(mockTuple.get("created_date")).thenReturn(new Date());
        when(mockTuple.get("total_duration_in_sec")).thenReturn(3600L);
        when(mockTuple.get("thumbnail")).thenReturn("thumbnail.png");
        when(mockTuple.get("last_mod_date")).thenReturn(new Date());
        when(mockTuple.get("no_of_students")).thenReturn(100L);
        when(mockTuple.get("is_active")).thenReturn(true);
        when(mockTuple.get("course_progress")).thenReturn("In Progress");

        List<Tuple> tuples = new ArrayList<>();
        tuples.add(mockTuple);

        mockPage = new PageImpl<>(tuples, PageRequest.of(0, 10), 1);

        mockSection = new Section();
        mockSection.setId(54L);
        mockSection.setName("Introduction");

        mockTopic = new Topic();
        mockTopic.setId(85L);
        mockTopic.setName("Self Introduction");

        mockVideo = new Video();
        mockVideo.setId(331L);
        mockVideo.setFilename("S1- T1 Self Intro.mp4");
        mockVideo.setSummary("summary");

        quizQuestion = new QuizQuestion();
        quizQuestion.setId(95L);
        quizQuestion.setDelete(false);
        quizQuestion.setQuestionText("what is java");
        quizQuestion.setQuestionType(QuestionType.SINGLE_CHOICE);

        video = new Video();
        video.setId(1L);
        video.setDelete(false);
        video.setTranscribe("");

        mockTopicType = new TopicType();
        mockTopicType.setId(1L);
        mockTopicType.setName("Video");

        Tuple relatedCourseTuple = mock(Tuple.class);
        when(relatedCourseTuple.get("id")).thenReturn(1L);
        when(relatedCourseTuple.get("title")).thenReturn("Course 1");
        relatedCourseTuples = Arrays.asList(relatedCourseTuple);

        mockCourseTuple = mock(Tuple.class);
        when(mockCourseTuple.get("max_rating")).thenReturn(3.5);
        when(mockCourseTuple.get("user_id")).thenReturn(UserTestData.userData().getId());
        courseDetails = new PageImpl<>(List.of(mockCourseTuple), PageRequest.of(0, 1), 1);

        userStatsTuple = mock(Tuple.class);
        when(userStatsTuple.get("courses")).thenReturn(5);
    }

    @Test
    @DisplayName("Should return success message when URL is unique for a new course")
    void testCheckUniqueCourseUrl_NewCourse_UniqueUrl() throws BadRequestException, EntityNotFoundException {

        String url = "unique-course-url";
        Long courseId = 0L;

        when(userService.findByEmail(anyString())).thenReturn(UserTestData.userData());
        when(courseUrlService.findByUrlAndCourseStatuses(anyString(), anyList()))
                .thenReturn(null);

        Message<String> result = courseService.checkUniqueCourseUrl(url, courseId, () -> EMAIL);

        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals(HttpStatus.OK.name(), result.getCode());
        assertEquals("Url is unique.", result.getMessage());
    }

    @Test
    @DisplayName("Should return success message when updating an existing course with its own URL")
    void testCheckUniqueCourseUrl_ExistingCourse_SameUrl() throws BadRequestException, EntityNotFoundException, IOException {

        String url = "existing-course-url";

        Course course = CourseTestData.courseData();
        course.setId(COURSE_ID);
        course.setCourseStatus(CourseStatus.PUBLISHED);

        CourseUrl courseUrl = new CourseUrl();
        courseUrl.setUrl(url);
        courseUrl.setCourse(course);

        when(userService.findByEmail(anyString())).thenReturn(UserTestData.userData());
        when(courseUrlService.findByUrlAndCourseStatuses(anyString(), anyList()))
                .thenReturn(courseUrl);

        Message<String> result = courseService.checkUniqueCourseUrl(url, COURSE_ID, () -> EMAIL);

        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals(HttpStatus.OK.name(), result.getCode());
        assertEquals("Url is unique.", result.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when URL is empty")
    void testCheckUniqueCourseUrl_EmptyUrl() {

        String url = null;
        Long courseId = 0L;

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            courseService.checkUniqueCourseUrl(url, courseId, ()-> EMAIL);
        });

        assertEquals("url cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when URL already exists for another course")
    void testCheckUniqueCourseUrl_UrlExistsForAnotherCourse() throws EntityNotFoundException {

        String url = "existing-course-url";
        Long courseId = 2L;

        Course otherCourse = new Course();
        otherCourse.setId(1L);
        otherCourse.setCourseStatus(CourseStatus.PUBLISHED);
        CourseUrl courseUrl = new CourseUrl();
        courseUrl.setCourse(otherCourse);

        when(userService.findByEmail(anyString())).thenReturn(UserTestData.userData());
        when(courseUrlService.findByUrlAndCourseStatuses(anyString(), anyList()))
                .thenReturn(courseUrl);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            courseService.checkUniqueCourseUrl(url, courseId, () -> EMAIL);
        });
        assertEquals("Not Available", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when URL exists but has a different status")
    void testCheckUniqueCourseUrl_UrlExistsWithDifferentStatus() throws EntityNotFoundException {

        String url = "existing-course-url";
        Long courseId = 2L;

        Course otherCourse = new Course();
        otherCourse.setId(1L);
        otherCourse.setCourseStatus(CourseStatus.PUBLISHED);
        CourseUrl courseUrl = new CourseUrl();
        courseUrl.setCourse(otherCourse);

        when(userService.findByEmail(anyString())).thenReturn(UserTestData.userData());
        when(courseUrlService.findByUrlAndCourseStatuses(anyString(), anyList()))
                .thenReturn(courseUrl);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            courseService.checkUniqueCourseUrl(url, courseId, () -> EMAIL);
        });
        assertEquals("Not Available", exception.getMessage());
    }

    @Test
    @DisplayName("Should successfully change course status")
    void testChangeCourseStatus_Success() throws Exception {
        Course course = CourseTestData.courseData();
        course.setCourseStatus(CourseStatus.DRAFT);

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(enrollmentService.totalNoOfEnrolledStudent(COURSE_ID)).thenReturn(5L);

        Message<String> response = courseService.changeCourseStatus(COURSE_ID, CourseStatus.PUBLISHED.toString(), EMAIL);

        assertEquals("Successfully changed course status", response.getData());
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());

        verify(repo, times(1)).save(course);
    }

    @Test
    @DisplayName("Should throw BadRequestException for invalid course status")
    void testChangeCourseStatus_InvalidCourseStatus() {
        String invalidStatus = "INVALID_STATUS";
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                courseService.changeCourseStatus(COURSE_ID, invalidStatus, EMAIL)
        );

        assertEquals("Invalid course status", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException for null course ID")
    void testChangeCourseStatus_NullCourseId() {
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                courseService.changeCourseStatus(null, CourseStatus.UNPUBLISHED.toString(), EMAIL)
        );
        assertEquals("Invalid course ID", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException for non-existing course")
    void testChangeCourseStatus_CourseNotFound() {
        when(repo.findById(COURSE_ID)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                courseService.changeCourseStatus(COURSE_ID, CourseStatus.UNPUBLISHED.toString(), EMAIL)
        );

        assertEquals("Course not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when trying to unpublish a course with more than 9 enrolled students")
    void testChangeCourseStatus_TooManyEnrolledStudents() throws IOException {
        Course course = new Course();
        course.setId(COURSE_ID);
        course.setCourseStatus(CourseStatus.PUBLISHED);
        course.setCourseType(CourseType.FREE_COURSE);

        when(repo.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(enrollmentService.totalNoOfEnrolledStudent(COURSE_ID)).thenReturn(10L);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                courseService.changeCourseStatus(COURSE_ID, CourseStatus.UNPUBLISHED.toString(), EMAIL)
        );

        assertEquals("You cannot unpublished this course because more than 9 students are currently enrolled.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw InternalServerException when repo save fails")
    void testChangeCourseStatus_InternalServerException() {
        Course course = new Course();
        course.setId(COURSE_ID);
        course.setCourseStatus(CourseStatus.DRAFT);
        course.setCourseType(CourseType.FREE_COURSE);

        when(repo.findById(COURSE_ID)).thenReturn(Optional.of(course));
        doThrow(new RuntimeException("Save failed")).when(repo).save(any(Course.class));

        InternalServerException exception = assertThrows(InternalServerException.class, () ->
                courseService.changeCourseStatus(COURSE_ID, CourseStatus.PUBLISHED.toString(), EMAIL)
        );

        assertEquals(InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when trying to change status from PUBLISHED to DELETE or DRAFT")
    void testChangeCourseStatus_CannotChangePublishedCourseStatus() {
        Course course = new Course();
        course.setId(COURSE_ID);
        course.setCourseStatus(CourseStatus.PUBLISHED);
        course.setCourseType(CourseType.FREE_COURSE);

        when(repo.findById(COURSE_ID)).thenReturn(Optional.of(course));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                courseService.changeCourseStatus(COURSE_ID, CourseStatus.DELETE.toString(), EMAIL)
        );

        assertEquals("Course is published", exception.getMessage());
    }

    @Test
    @DisplayName("Test getCourseDetailsById: Successfully fetch course details")
    void testGetCourseDetailsByIdSuccess() throws Exception {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());

        when(repo.findAllByCoursesCategoryOrCourseIdAndMostReviewed(any(), any(), eq(COURSE_ID), eq(UserTestData.userData().getId()), any()))
                .thenReturn(courseDetails);
        when(repo.findCoursesAndStudentEnrolledByUserId(UserTestData.userData().getId())).thenReturn(userStatsTuple);

        when(sectionService.fetchSectionDetailByCourseId(COURSE_ID)).thenReturn(Collections.emptyList());

        when(tagService.findByCourseId(COURSE_ID)).thenReturn(Collections.emptyList());

        when(repo.findById(COURSE_ID)).thenReturn(Optional.of(CourseTestData.courseData()));

        Message<CourseDetailResponse> response = courseService.getCourseDetailsById(COURSE_ID, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Course details fetched successfully.", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    @DisplayName("Test getCourseDetailsById: Successfully fetch course details without user email")
    void testGetCourseDetailsByIdWithoutEmail() throws Exception {

        when(repo.findAllByCoursesCategoryOrCourseIdAndMostReviewed(any(), any(), eq(COURSE_ID), isNull(), any()))
                .thenReturn(courseDetails);
        when(repo.findCoursesAndStudentEnrolledByUserId((Long) mockCourseTuple.get("user_id"))).thenReturn(mock(Tuple.class));

        Message<CourseDetailResponse> response = courseService.getCourseDetailsById(COURSE_ID, null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Course details fetched successfully.", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    @DisplayName("Test getCourseDetailsById: Successfully fetch course details with feedback")
    void testGetCourseDetailsByIdWithFeedback() throws Exception {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());

        when(repo.findAllByCoursesCategoryOrCourseIdAndMostReviewed(any(), any(), eq(COURSE_ID), eq(UserTestData.userData().getId()), any()))
                .thenReturn(courseDetails);
        when(repo.findCoursesAndStudentEnrolledByUserId(UserTestData.userData().getId())).thenReturn(mock(Tuple.class));

        when(sectionService.fetchSectionDetailByCourseId(COURSE_ID)).thenReturn(Collections.emptyList());

        when(tagService.findByCourseId(COURSE_ID)).thenReturn(Collections.emptyList());

        when(courseReviewService.findStudentFeedbackByCourseId(COURSE_ID, 0, 3)).thenReturn(mock(Message.class));
        when(repo.findById(COURSE_ID)).thenReturn(Optional.of(CourseTestData.courseData()));

        Message<CourseDetailResponse> response = courseService.getCourseDetailsById(COURSE_ID, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Course details fetched successfully.", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    @DisplayName("Test getCourseDetailsById: Course not found")
    void testGetCourseDetailsByIdCourseNotFound() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());

        when(repo.findAllByCoursesCategoryOrCourseIdAndMostReviewed(any(), any(), eq(COURSE_ID), eq(UserTestData.userData().getId()), any())).thenReturn(Page.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                courseService.getCourseDetailsById(COURSE_ID, EMAIL));

        assertEquals("No courses found.", exception.getMessage());
    }

    @Test
    @DisplayName("Should successfully fetch related courses with pagination when email is provided")
    void testGetRelatedCoursesByPagination_Success_WithEmail() throws EntityNotFoundException, IOException {

        RelatedCoursesRequest request = new RelatedCoursesRequest();
        request.setCourseId(COURSE_ID);
        request.setPageNo(0);
        request.setPageSize(10);

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());

        List<Long> recommendedCourseIds = Arrays.asList(1L, 2L, 3L);
        when(enrollmentService.findRecommendedCoursesIDs(request.getCourseId())).thenReturn(recommendedCourseIds);

        when(repo.findById(COURSE_ID)).thenReturn(Optional.of(CourseTestData.courseData()));
        Page<Tuple> pagedData = new PageImpl<>(relatedCourseTuples);
        when(repo.findRelatedCourses(anyLong(), anyString(), anyLong(), any(PageRequest.class)))
                .thenReturn(pagedData);

        Message<RelatedCoursesResponse> response = courseService.getRelatedCoursesByPagination(request, EMAIL);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("Related courses and alternate sections fetched successfully.", response.getMessage());
        assertEquals(1, response.getData().getTotalElements());
    }


    @Test
    @DisplayName("Should successfully fetch related courses with pagination when email is not provided")
    void testGetRelatedCoursesByPagination_Success_WithoutEmail() throws EntityNotFoundException, IOException {

        RelatedCoursesRequest request = new RelatedCoursesRequest();
        request.setCourseId(COURSE_ID);
        request.setPageNo(0);
        request.setPageSize(10);

        List<Long> recommendedCourseIds = Arrays.asList(1L, 2L, 3L);
        when(enrollmentService.findRecommendedCoursesIDs(request.getCourseId())).thenReturn(recommendedCourseIds);

        when(repo.findById(COURSE_ID)).thenReturn(Optional.of(CourseTestData.courseData()));

        Page<Tuple> pagedData = new PageImpl<>(relatedCourseTuples);
        when(repo.findRelatedCourses(any(), any(), any(), any()))
                .thenReturn(pagedData);

        Message<RelatedCoursesResponse> response = courseService.getRelatedCoursesByPagination(request, null);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("Related courses and alternate sections fetched successfully.", response.getMessage());
        assertEquals(1, response.getData().getTotalElements());
    }


    @Test
    @DisplayName("Should throw EntityNotFoundException when no related courses are found")
    void testGetRelatedCoursesByPagination_NoRelatedCoursesFound() throws EntityNotFoundException, IOException {

        RelatedCoursesRequest request = new RelatedCoursesRequest();
        request.setCourseId(1L);
        request.setPageNo(0);
        request.setPageSize(10);
        List<Long> recommendedCourseIds = Arrays.asList(1L, 2L, 3L);
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(enrollmentService.findRecommendedCoursesIDs(request.getCourseId())).thenReturn(recommendedCourseIds);
        when(repo.findById(request.getCourseId())).thenReturn(Optional.of(CourseTestData.courseData()));
        when(tagService.findByCourseId(request.getCourseId())).thenReturn(Arrays.asList(
                new Tag().builder().name("Tag1").build(),
                new Tag().builder().name("Tag2").build()
        ));
        when(repo.findRelatedCourses(anyString(), anyList(), any()))
                .thenReturn(anyList());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            courseService.getRelatedCoursesByPagination(request, EMAIL);
        });

        assertEquals("No related courses or alternate sections available.", exception.getMessage());
    }


    @Test
    @DisplayName("Should handle errors gracefully when user lookup fails")
    void testGetRelatedCoursesByPagination_UserLookupFails() throws EntityNotFoundException, IOException {

        RelatedCoursesRequest request = new RelatedCoursesRequest();
        request.setCourseId(1L);
        request.setPageNo(0);
        request.setPageSize(10);

        when(userService.findByEmail(EMAIL)).thenThrow(new RuntimeException("User service error"));

        List<Long> recommendedCourseIds = Arrays.asList(1L, 2L, 3L);
        when(enrollmentService.findRecommendedCoursesIDs(request.getCourseId())).thenReturn(recommendedCourseIds);

        when(repo.findById(request.getCourseId())).thenReturn(Optional.of(CourseTestData.courseData()));
        when(tagService.findByCourseId(request.getCourseId())).thenReturn(Arrays.asList(
                new Tag().builder().name("Tag1").build(),
                new Tag().builder().name("Tag2").build()
        ));

        Page<Tuple> pagedData = new PageImpl<>(relatedCourseTuples);
        when(repo.findRelatedCourses(anyLong(), any(), any(), any(PageRequest.class)))
                .thenReturn(pagedData);

        Message<RelatedCoursesResponse> response = courseService.getRelatedCoursesByPagination(request, EMAIL);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("Related courses and alternate sections fetched successfully.", response.getMessage());
        assertEquals(1, response.getData().getTotalElements());
    }


    @Test
    @DisplayName("Should successfully send course shared notification when valid course ID and email are provided")
    void testSendCourseSharedNotification_Success() throws EntityNotFoundException {

        Course course = new Course();
        course.setId(COURSE_ID);
        course.setTitle(COURSE_TITLE);
        course.setCreatedBy(UserTestData.userData().getId());

        when(userService.findByEmail(UserTestData.userData().getEmail())).thenReturn(UserTestData.userData());
        when(repo.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(COURSE_ID, GenericStatus.ACTIVE)).thenReturn(new CourseUrl().builder().url(COURSE_URL).build());

        courseService.sendCourseSharedNotification(COURSE_ID, UserTestData.userData().getEmail());

//        verify(rabbitMQProducer, times(1)).sendMessage(
//                eq(course.getTitle()),
//                eq("student/course-details/"+COURSE_URL),
//                eq(UserTestData.userData().getEmail()),
//                eq(course.getCreatedBy()),
//                eq(NotificationContentType.TEXT),
//                eq(NotificationType.COURSE_SHARE)
//        );
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when the user email is not found")
    void testSendCourseSharedNotification_UserNotFound() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenThrow(new EntityNotFoundException("User not found"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            courseService.sendCourseSharedNotification(COURSE_ID, EMAIL);
        });

        assertEquals("User not found", exception.getMessage());
        verify(repo, never()).findById(COURSE_ID);
//        verify(rabbitMQProducer, never()).sendMessage(anyString(), anyString(), anyString(), anyLong(), any(), any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when the course ID is not found")
    void testSendCourseSharedNotification_CourseNotFound() throws EntityNotFoundException {
        Course course = new Course();
        course.setId(COURSE_ID);
        course.setTitle(COURSE_TITLE);
        course.setCreatedBy(UserTestData.userData().getId());

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findById(COURSE_ID)).thenReturn(Optional.empty());
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(COURSE_ID, GenericStatus.ACTIVE)).thenReturn(new CourseUrl().builder().url("test-url").build());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            courseService.sendCourseSharedNotification(COURSE_ID, EMAIL);
        });

        assertEquals("Course not found by provided id.", exception.getMessage());
//        verify(rabbitMQProducer, never()).sendMessage(anyString(), anyString(), anyString(), anyLong(), any(), any());
    }

    @Test
    @DisplayName("Should handle unexpected errors during notification sending process")
    void testSendCourseSharedNotification_UnexpectedError() throws EntityNotFoundException {

        Course course = new Course();
        course.setId(COURSE_ID);
        course.setTitle(COURSE_TITLE);
        course.setCreatedBy(UserTestData.userData().getId());

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(COURSE_ID, GenericStatus.ACTIVE)).thenReturn(new CourseUrl().builder().url("test-url").build());
        doThrow(new RuntimeException("Unexpected error")).when(rabbitMQProducer).sendMessage(anyString(), anyString(), anyString(), anyLong(), any(), any(), any(), any());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseService.sendCourseSharedNotification(COURSE_ID, EMAIL);
        });

        assertEquals("Unexpected error", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when the course is not found")
    void testGetCourseByTitle_CourseNotFound() {

        when(repo.findByTitleAndPublished(anyString(), eq(CourseStatus.PUBLISHED.toString()))).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            courseService.getCourseByUrl(COURSE_URL, ()-> "");
        });

        assertEquals("Course not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when the course is inactive")
    void testGetCourseByTitle_InactiveCourse() {

        when(repo.findByTitleAndPublished(anyString(), eq(CourseStatus.PUBLISHED.toString()))).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            courseService.getCourseByUrl(COURSE_URL, ()-> "");
        });

        assertEquals("Course not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should return message when course title is unique for a new course")
    void testCheckUniqueCourseTitle_NewCourse_UniqueTitle() throws BadRequestException, EntityNotFoundException {

        String title = "New Course";
        Long courseId = 0L;
        when(repo.findByTitle(anyString())).thenReturn(Collections.emptyList());

        Message<String> result = courseService.checkUniqueCourseTile(title, courseId, ()-> EMAIL);

        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals(HttpStatus.OK.name(), result.getCode());
        assertEquals("Title is unique.", result.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when course title already exists")
    void testCheckUniqueCourseTitle_NewCourse_TitleExists() {
        String title = "Existing Course";
        Long courseId = 0L;
        Course existingCourse = new Course();
        existingCourse.setId(1L);
        existingCourse.setCourseStatus(CourseStatus.PUBLISHED);

        when(repo.findByTitle(anyString())).thenReturn(Collections.singletonList(existingCourse));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            courseService.checkUniqueCourseTile(title, courseId, () -> EMAIL);
        });
        assertEquals("Title already exists.", exception.getMessage());
        verify(repo, times(1)).findByTitle(title);
    }


    @Test
    @DisplayName("Should return message when updating an existing course with the same title")
    void testCheckUniqueCourseTitle_ExistingCourse_SameTitle() throws BadRequestException, EntityNotFoundException {

        String title = "Existing Course";
        Long courseId = 1L;
        Course existingCourse = new Course();
        existingCourse.setTitle(title);
        when(repo.findById(anyLong())).thenReturn(Optional.of(existingCourse));

        Message<String> result = courseService.checkUniqueCourseTile(title, courseId, ()-> EMAIL);

        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals(HttpStatus.OK.name(), result.getCode());
        assertEquals("Title is unique.", result.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when updating an existing course with a different title that already exists")
    void testCheckUniqueCourseTitle_ExistingCourse_DifferentTitleExists() {

        String newTitle = "New Course Title";
        Long courseId = 1L;

        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setTitle("Old Course Title");
        existingCourse.setCourseStatus(CourseStatus.PUBLISHED);
        Course conflictingCourse = new Course();
        conflictingCourse.setId(2L);
        conflictingCourse.setTitle(newTitle);
        conflictingCourse.setCourseStatus(CourseStatus.PUBLISHED);

        when(repo.findById(courseId)).thenReturn(Optional.of(existingCourse));
        when(repo.findByTitle(newTitle)).thenReturn(Collections.singletonList(conflictingCourse));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            courseService.checkUniqueCourseTile(newTitle, courseId, () -> EMAIL);
        });

        assertEquals("Title already exists.", exception.getMessage());
    }

    @Test
    @DisplayName("Should return message when updating an existing course with a new unique title")
    void testCheckUniqueCourseTitle_ExistingCourse_NewUniqueTitle() throws BadRequestException, EntityNotFoundException {

        String newTitle = "Unique New Course Title";
        Long courseId = 1L;

        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setTitle("Old Course Title");
        existingCourse.setCourseStatus(CourseStatus.PUBLISHED);

        when(repo.findById(anyLong())).thenReturn(Optional.of(existingCourse));
        when(repo.findByTitle(anyString())).thenReturn(Collections.emptyList());

        Message<String> result = courseService.checkUniqueCourseTile(newTitle, courseId, () -> EMAIL);

        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals(HttpStatus.OK.name(), result.getCode());
        assertEquals("Title is unique.", result.getMessage());
    }

    @DisplayName("Fetch all course title by instructor for performance with valid data ")
    @Test
    void fetchAllCoursesTitleByInstructorForPerformance_validInstructorWithCourses() throws EntityNotFoundException {
        List<Tuple> validTuplesList = Arrays.asList(mock(Tuple.class));

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findAllByInstructorId(UserTestData.userData().getId())).thenReturn(validTuplesList);

        Message<List<CourseDropdown>> response = courseService.fetchAllCoursesTitleByInstructorForPerformance(EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Successfully fetched the courses.", response.getMessage());
        assertFalse(response.getData().isEmpty());
    }

    @DisplayName("Fetch all course title by instructor for performance with empty course list ")
    @Test
    void fetchAllCoursesTitleByInstructorForPerformance_instructorWithEmptyCourseList() throws EntityNotFoundException {
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findAllByInstructorId(UserTestData.userData().getId())).thenReturn(Collections.emptyList());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> courseService.fetchAllCoursesTitleByInstructorForPerformance(EMAIL));

        assertEquals("No data found for this instructor.", exception.getMessage());
    }

    @DisplayName("Fetch all course title by instructor for performance with invalid instructor email ")
    @Test
    void fetchAllCoursesTitleByInstructorForPerformance_invalidInstructorEmail() throws EntityNotFoundException {
        when(userService.findByEmail(EMAIL)).thenThrow(new EntityNotFoundException("User not found"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> courseService.fetchAllCoursesTitleByInstructorForPerformance(EMAIL));

        assertEquals("User not found", exception.getMessage());
    }

    @DisplayName("Fetch all course title by instructor for performance when null data returned from repo ")
    @Test
    void fetchAllCoursesTitleByInstructorForPerformance_nullDataReturnedFromRepo() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findAllByInstructorId(UserTestData.userData().getId())).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> courseService.fetchAllCoursesTitleByInstructorForPerformance(EMAIL));

        assertEquals("No data found for this instructor.", exception.getMessage());
    }

    @DisplayName("Fetch course detail for first step with valid data")
    @Test
    void fetchCourseDetailForUpdateForFirstStep_validCourseAndUser() throws EntityNotFoundException, BadRequestException {

        Long courseId = 1L;
        Course course = new Course();
        course.setId(courseId);
        course.setContentType(ContentType.COURSE);
        course.setCreatedBy(UserTestData.userData().getId());
        course.setCourseType(CourseType.FREE_COURSE);
        List<com.vinncorp.fast_learner.models.tag.Tag> tags = Collections.singletonList(new Tag());

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findByIdAndCreatedBy(courseId, UserTestData.userData().getId())).thenReturn(Optional.of(course));
        when(tagService.findByCourseId(courseId)).thenReturn(tags);

        Message<CourseDetailForUpdateResponse> response = courseService.fetchCourseDetailForUpdateForFirstStep(courseId, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Course detail for first step is fetched successfully.", response.getMessage());
        assertNotNull(response.getData());
    }

    @DisplayName("Fetch course detail for first step with invalid course user")
    @Test
    void fetchCourseDetailForUpdateForFirstStep_courseNotFound() throws EntityNotFoundException {

        Long courseId = 1L;

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findByIdAndCreatedBy(courseId, UserTestData.userData().getId())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                courseService.fetchCourseDetailForUpdateForFirstStep(courseId, EMAIL));
        assertEquals("No course found for the logged in user.", exception.getMessage());
    }

    @DisplayName("Fetch course detail for first step with invalid user")
    @Test
    void fetchCourseDetailForUpdateForFirstStep_userNotFound() throws EntityNotFoundException {

        Long courseId = 1L;

        when(userService.findByEmail(EMAIL)).thenThrow(new RuntimeException("User not found"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                courseService.fetchCourseDetailForUpdateForFirstStep(courseId, EMAIL));
        assertEquals("User not found", exception.getMessage());
    }

    @DisplayName("Fetch course detail for first step with null course id")
    @Test
    void fetchCourseDetailForUpdateForFirstStep_nullCourseId() {

        Long courseId = null;

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                courseService.fetchCourseDetailForUpdateForFirstStep(courseId, EMAIL));
        assertEquals("Course ID cannot be null", exception.getMessage());
    }

    @DisplayName("Fetch course detail for first step with null email")
    @Test
    void fetchCourseDetailForUpdateForFirstStep_nullEmail() {

        Long courseId = 1L;

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                courseService.fetchCourseDetailForUpdateForFirstStep(courseId, null));
        assertEquals("Email cannot be null", exception.getMessage());
    }

    @DisplayName("Create course with valid data")
    @Test
    public void testCreateCourse_Success() throws EntityNotFoundException, InternalServerException, BadRequestException, IOException, CreateCourseValidationException {
        CreateCourseRequest createCourseRequest = this.getMockCourse();
        Course mockCourse = new Course();
        createCourseRequest.setCourseId(null);
        createCourseRequest.setCourseType(CourseType.FREE_COURSE.name());
        mockCourse.setId(1L);
        mockCourse.setCourseStatus(CourseStatus.PUBLISHED);
        mockCourse.setTitle("The Unsung Selldiers");

        CourseUrl courseUrl = new CourseUrl();
        courseUrl.setUrl("");

        com.vinncorp.fast_learner.es_models.Course esCourse = new com.vinncorp.fast_learner.es_models.Course();
        esCourse.setId(null);

        // Mock repository save method
        when(repo.save(any(Course.class))).thenReturn(mockCourse);
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseLevelService.findById(1L)).thenReturn(courseLevel);
        when(courseCategoryService.findById(1L)).thenReturn(courseCategory);
        doNothing().when(tagService).createAllNewAndAlreadyExistsTags(anyList(), any(Course.class));
//        doNothing().when(rabbitMQProducer).sendMessageToUsers(anyString(), anyLong(), anyString(), any(), any());

        when(esCourseService.save(any(com.vinncorp.fast_learner.es_models.Course.class))).thenReturn(new Message<>());
        when(sectionService.save(any(Section.class))).thenReturn(mockSection);
        when(topicTypeService.findById(anyLong())).thenReturn(mockTopicType);
        when(this.userProfileService.getUserProfile(1L)).thenReturn(UserProfileTestData.userProfile());
        when(this.courseUrlService.findActiveUrlByCourseIdAndStatus(mockCourse.getId(), GenericStatus.ACTIVE)).thenReturn(courseUrl);
        when(this.esCourseContentService.save(anyList())).thenReturn(anyList());
        Message<CreateCourseRequest> response = courseService.createCourse(createCourseRequest, null,EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("Course created successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("The Unsung Selldiers", response.getData().getTitle());
    }

    @DisplayName("Update course with valid data")
    @Test
    public void testUpdateCourse_Success() throws EntityNotFoundException, InternalServerException, BadRequestException, IOException, CreateCourseValidationException {
        CreateCourseRequest createCourseRequest = this.getMockCourse();
        Course mockCourse = new Course();
        mockCourse.setId(29L);
        mockCourse.setTitle("The Unsung Selldiers");
        mockCourse.setCourseStatus(CourseStatus.PUBLISHED);

        createCourseRequest.setCourseId(29L);
        createCourseRequest.setCourseType(CourseType.FREE_COURSE.name());

        CourseUrl courseUrl = new CourseUrl();
        courseUrl.setUrl("");

        com.vinncorp.fast_learner.es_models.Course esCourse = new com.vinncorp.fast_learner.es_models.Course();
        esCourse.setId(String.valueOf(29L));

        // Mock repository save method
        when(repo.save(any(Course.class))).thenReturn(mockCourse);
        when(repo.findById(anyLong())).thenReturn(Optional.of(mockCourse));
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseLevelService.findById(1L)).thenReturn(courseLevel);
        when(courseCategoryService.findById(1L)).thenReturn(courseCategory);
        doNothing().when(tagService).createAllNewAndAlreadyExistsTags(anyList(), any(Course.class));
//        doNothing().when(rabbitMQProducer).sendMessageToUsers(anyString(), anyLong(), anyString(), any(), any());

        when(esCourseService.findByDBId(anyLong())).thenReturn(esCourse);
        when(esCourseService.save(any(com.vinncorp.fast_learner.es_models.Course.class))).thenReturn(new Message<>());
        when(sectionService.save(any(Section.class))).thenReturn(mockSection);
        when(topicTypeService.findById(anyLong())).thenReturn(mockTopicType);
        when(this.userProfileService.getUserProfile(1L)).thenReturn(UserProfileTestData.userProfile());
        when(this.courseUrlService.findActiveUrlByCourseIdAndStatus(mockCourse.getId(), GenericStatus.ACTIVE)).thenReturn(courseUrl);
        when(this.esCourseContentService.save(anyList())).thenReturn(anyList());
        Message<CreateCourseRequest> response = courseService.createCourse(createCourseRequest,null, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("Course updated successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("The Unsung Selldiers", response.getData().getTitle());
    }

    @DisplayName("Update course with null title")
    @Test
    public void testUpdateCourse_NullTitleWhenPublish() throws EntityNotFoundException, InternalServerException, BadRequestException, IOException {
        CreateCourseRequest createCourseRequest = this.getMockCourse();
        createCourseRequest.setTitle(null);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            courseService.createCourse(createCourseRequest,null, EMAIL);
        });

        assertEquals("Please provide all required fields for course creation", exception.getMessage());
    }

    @DisplayName("Update course with null tags")
    @Test
    public void testUpdateCourse_NullTagsWhenPublish() throws EntityNotFoundException, InternalServerException, BadRequestException, IOException {

        CreateCourseRequest createCourseRequest = this.getMockCourse();
        createCourseRequest.setTags(null);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            courseService.createCourse(createCourseRequest,null, EMAIL);
        });

        assertEquals("Please provide all required fields for course creation", exception.getMessage());
    }

    @DisplayName("Create course with no sections")
    @Test
    public void testCreateCourse_NullSections() throws EntityNotFoundException, InternalServerException, BadRequestException, IOException, CreateCourseValidationException {

        CreateCourseRequest createCourseRequest = this.getMockCourse();
        createCourseRequest.setCourseId(null);
        createCourseRequest.setSections(null);
        createCourseRequest.setIsActive(false);
        createCourseRequest.setCourseType(CourseType.FREE_COURSE.name());

        Course mockCourse = new Course();
        mockCourse.setTitle("The Unsung Selldiers");
        mockCourse.setCourseStatus(CourseStatus.DRAFT);

        com.vinncorp.fast_learner.es_models.Course esCourse = new com.vinncorp.fast_learner.es_models.Course();
        esCourse.setId(null);

        // Mock repository save method
        when(repo.save(any(Course.class))).thenReturn(mockCourse);
        when(repo.findById(anyLong())).thenReturn(Optional.of(mockCourse));
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseLevelService.findById(1L)).thenReturn(courseLevel);
        when(courseCategoryService.findById(1L)).thenReturn(courseCategory);
        doNothing().when(tagService).createAllNewAndAlreadyExistsTags(anyList(), any(Course.class));
//        doNothing().when(rabbitMQProducer).sendMessageToUsers(anyString(), anyLong(), anyString(), any(), any());

        when(esCourseService.findByDBId(anyLong())).thenReturn(esCourse);
        when(esCourseService.save(any(com.vinncorp.fast_learner.es_models.Course.class))).thenReturn(new Message<>());

        Message<CreateCourseRequest> response = courseService.createCourse(createCourseRequest,null, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("Course created successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("The Unsung Selldiers", response.getData().getTitle());
    }

    @DisplayName("Create course with no topics")
    @Test
    public void testCreateCourse_NullTopics() throws EntityNotFoundException, InternalServerException, BadRequestException, IOException, CreateCourseValidationException {

        CreateCourseRequest createCourseRequest = this.getMockCourse();
        createCourseRequest.setCourseId(null);
        createCourseRequest.getSections().stream().forEach(section -> section.setTopics(null));
        createCourseRequest.setIsActive(false);
        createCourseRequest.setCourseType(CourseType.FREE_COURSE.name());

        Course mockCourse = new Course();
        mockCourse.setTitle("The Unsung Selldiers");
        mockCourse.setCourseStatus(CourseStatus.DRAFT);

        CourseUrl courseUrl = new CourseUrl();
        courseUrl.setUrl("");

        com.vinncorp.fast_learner.es_models.Course esCourse = new com.vinncorp.fast_learner.es_models.Course();
        esCourse.setId(null);

        // Mock repository save method
        when(repo.save(any(Course.class))).thenReturn(mockCourse);
        when(repo.findById(anyLong())).thenReturn(Optional.of(mockCourse));
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseLevelService.findById(1L)).thenReturn(courseLevel);
        when(courseCategoryService.findById(1L)).thenReturn(courseCategory);
        doNothing().when(tagService).createAllNewAndAlreadyExistsTags(anyList(), any(Course.class));
//        doNothing().when(rabbitMQProducer).sendMessageToUsers(anyString(), anyLong(), anyString(), any(), any());

        when(esCourseService.findByDBId(anyLong())).thenReturn(esCourse);
        when(esCourseService.save(any(com.vinncorp.fast_learner.es_models.Course.class))).thenReturn(new Message<>());
        when(sectionService.save(any(Section.class))).thenReturn(mockSection);
        when(topicTypeService.findById(anyLong())).thenReturn(mockTopicType);
        when(this.userProfileService.getUserProfile(1L)).thenReturn(UserProfileTestData.userProfile());
        when(this.courseUrlService.findActiveUrlByCourseIdAndStatus(mockCourse.getId(), GenericStatus.ACTIVE)).thenReturn(courseUrl);
        when(this.esCourseContentService.save(anyList())).thenReturn(anyList());

        Message<CreateCourseRequest> response = courseService.createCourse(createCourseRequest,null, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("Course created successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("The Unsung Selldiers", response.getData().getTitle());
    }

    @DisplayName("Create course with no video url")
    @Test
    public void testCreateCourse_NullVideoUrl() throws EntityNotFoundException, InternalServerException, BadRequestException, IOException {

        CreateCourseRequest createCourseRequest = this.getMockCourse();
        createCourseRequest.setCourseId(null);
        createCourseRequest.setIsActive(false);
        createCourseRequest.setCourseType(CourseType.FREE_COURSE.name());

        Course mockCourse = new Course();
        mockCourse.setTitle("The Unsung Selldiers");
        mockCourse.setCourseStatus(CourseStatus.DRAFT);

        createCourseRequest.getSections().stream()
                .flatMap(section -> section.getTopics().stream())
                .filter(topic -> topic.getTopicTypeId() == 1)
                .forEach(top -> top.getVideo().setVideoURL(""));

        com.vinncorp.fast_learner.es_models.Course esCourse = new com.vinncorp.fast_learner.es_models.Course();
        esCourse.setId(null);

        // Mock repository save method
        when(repo.save(any(Course.class))).thenReturn(mockCourse);
        when(repo.findById(anyLong())).thenReturn(Optional.of(mockCourse));
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseLevelService.findById(1L)).thenReturn(courseLevel);
        when(courseCategoryService.findById(1L)).thenReturn(courseCategory);
        doNothing().when(tagService).createAllNewAndAlreadyExistsTags(anyList(), any(Course.class));
//        doNothing().when(rabbitMQProducer).sendMessageToUsers(anyString(), anyLong(), anyString(), any(), any());
        when(sectionService.save(any(Section.class))).thenReturn(mockSection);
        when(topicTypeService.findById(1L)).thenReturn(topicTypeVideo);
        when(topicTypeService.findById(2L)).thenReturn(topicTypeArticle);
        when(topicTypeService.findById(4L)).thenReturn(topicTypeQuiz);
        when(topicService.save(any(Topic.class))).thenReturn(mockTopic);

        when(esCourseService.findByDBId(anyLong())).thenReturn(esCourse);
        when(esCourseService.save(any(com.vinncorp.fast_learner.es_models.Course.class))).thenReturn(new Message<>());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            Message<CreateCourseRequest> response = courseService.createCourse(createCourseRequest,null, EMAIL);
        });

        assertEquals("Video url cannot be empty", exception.getMessage());
    }

    @DisplayName("Create course with no quiz questions")
    @Test
    public void testCreateCourse_NullQuizQuestions() throws EntityNotFoundException, InternalServerException, BadRequestException, IOException {

        CreateCourseRequest createCourseRequest = this.getMockCourse();
        createCourseRequest.setCourseId(null);
        createCourseRequest.setIsActive(false);

        createCourseRequest.setCourseType(CourseType.FREE_COURSE.name());

        Course mockCourse = new Course();
        mockCourse.setTitle("The Unsung Selldiers");
        mockCourse.setCourseStatus(CourseStatus.DRAFT);

        createCourseRequest.getSections().stream()
                .flatMap(section -> section.getTopics().stream())
                .filter(topic -> topic.getTopicTypeId() == 4)
                .forEach(top -> top.getQuiz().setQuestions(new ArrayList<>()));

        com.vinncorp.fast_learner.es_models.Course esCourse = new com.vinncorp.fast_learner.es_models.Course();
        esCourse.setId(null);

        // Mock repository save method
        when(repo.save(any(Course.class))).thenReturn(mockCourse);
        when(repo.findById(anyLong())).thenReturn(Optional.of(mockCourse));
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseLevelService.findById(1L)).thenReturn(courseLevel);
        when(courseCategoryService.findById(1L)).thenReturn(courseCategory);
        doNothing().when(tagService).createAllNewAndAlreadyExistsTags(anyList(), any(Course.class));
//        doNothing().when(rabbitMQProducer).sendMessageToUsers(anyString(), anyLong(), anyString(), any(), any());
        when(sectionService.save(any(Section.class))).thenReturn(mockSection);
        when(topicTypeService.findById(1L)).thenReturn(topicTypeVideo);
        when(topicTypeService.findById(2L)).thenReturn(topicTypeArticle);
        when(topicTypeService.findById(4L)).thenReturn(topicTypeQuiz);
        when(topicService.save(any(Topic.class))).thenReturn(mockTopic);
        when(videoService.save(any(Video.class))).thenReturn(mockVideo);

        when(esCourseService.findByDBId(anyLong())).thenReturn(esCourse);
        when(esCourseService.save(any(com.vinncorp.fast_learner.es_models.Course.class))).thenReturn(new Message<>());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            Message<CreateCourseRequest> response = courseService.createCourse(createCourseRequest,null, EMAIL);
        });

        assertEquals("Quiz questions can not be empty", exception.getMessage());
    }

    @DisplayName("Create course with no quiz question answers")
    @Test
    public void testCreateCourse_NullQuizQuestionAnswers() throws EntityNotFoundException, InternalServerException, BadRequestException, IOException {

        CreateCourseRequest createCourseRequest = this.getMockCourse();
        Course mockCourse = new Course();
        createCourseRequest.setCourseId(null);
        mockCourse.setTitle("The Unsung Selldiers");
        createCourseRequest.setIsActive(false);
        createCourseRequest.setCourseType(CourseType.FREE_COURSE.name());

        createCourseRequest.getSections().stream()
                .flatMap(section -> section.getTopics().stream())
                .filter(topic -> topic.getTopicTypeId() == 4)
                .forEach(top -> top.getQuiz().getQuestions().stream().forEach(q -> q.setAnswers(new ArrayList<>())));

        com.vinncorp.fast_learner.es_models.Course esCourse = new com.vinncorp.fast_learner.es_models.Course();
        esCourse.setId(null);

        // Mock repository save method
        when(repo.save(any(Course.class))).thenReturn(mockCourse);
        when(repo.findById(anyLong())).thenReturn(Optional.of(mockCourse));
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseLevelService.findById(1L)).thenReturn(courseLevel);
        when(courseCategoryService.findById(1L)).thenReturn(courseCategory);
        doNothing().when(tagService).createAllNewAndAlreadyExistsTags(anyList(), any(Course.class));
//        doNothing().when(rabbitMQProducer).sendMessageToUsers(anyString(), anyLong(), anyString(), any(), any());
        when(sectionService.save(any(Section.class))).thenReturn(mockSection);
        when(topicTypeService.findById(1L)).thenReturn(topicTypeVideo);
        when(topicTypeService.findById(2L)).thenReturn(topicTypeArticle);
        when(topicTypeService.findById(4L)).thenReturn(topicTypeQuiz);
        when(topicService.save(any(Topic.class))).thenReturn(mockTopic);
        when(quizQuestionService.save(any(QuizQuestion.class))).thenReturn(quizQuestion);
        when(videoService.save(any(Video.class))).thenReturn(video);

        when(esCourseService.findByDBId(anyLong())).thenReturn(esCourse);
        when(esCourseService.save(any(com.vinncorp.fast_learner.es_models.Course.class))).thenReturn(new Message<>());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            Message<CreateCourseRequest> response = courseService.createCourse(createCourseRequest,null, EMAIL);
        });

        assertEquals("Quiz question answers cannot be empty", exception.getMessage());
    }

    @DisplayName("Create course with no quiz question correct answers")
    @Test
    public void testCreateCourse_NullQuizQuestionCorrectAnswer() throws EntityNotFoundException, InternalServerException, BadRequestException, IOException {

        CreateCourseRequest createCourseRequest = this.getMockCourse();
        Course mockCourse = new Course();
        createCourseRequest.setCourseId(null);
        mockCourse.setTitle("The Unsung Selldiers");
        createCourseRequest.setIsActive(false);
        createCourseRequest.setCourseType(CourseType.FREE_COURSE.name());

        createCourseRequest.getSections().stream()
                .flatMap(section -> section.getTopics().stream())
                .filter(topic -> topic.getTopicTypeId() == 4)
                .forEach(top -> top.getQuiz().getQuestions().stream().forEach(q -> q.getAnswers().forEach(a -> a.setIsCorrectAnswer(false))));

        com.vinncorp.fast_learner.es_models.Course esCourse = new com.vinncorp.fast_learner.es_models.Course();
        esCourse.setId(null);

        // Mock repository save method
        when(repo.save(any(Course.class))).thenReturn(mockCourse);
        when(repo.findById(anyLong())).thenReturn(Optional.of(mockCourse));
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseLevelService.findById(1L)).thenReturn(courseLevel);
        when(courseCategoryService.findById(1L)).thenReturn(courseCategory);
        doNothing().when(tagService).createAllNewAndAlreadyExistsTags(anyList(), any(Course.class));
//        doNothing().when(rabbitMQProducer).sendMessageToUsers(anyString(), anyLong(), anyString(), any(), any());
        when(sectionService.save(any(Section.class))).thenReturn(mockSection);
        when(topicTypeService.findById(1L)).thenReturn(topicTypeVideo);
        when(topicTypeService.findById(2L)).thenReturn(topicTypeArticle);
        when(topicTypeService.findById(4L)).thenReturn(topicTypeQuiz);
        when(topicService.save(any(Topic.class))).thenReturn(mockTopic);
        when(quizQuestionService.save(any(QuizQuestion.class))).thenReturn(quizQuestion);
        when(videoService.save(any(Video.class))).thenReturn(video);

        when(esCourseService.findByDBId(anyLong())).thenReturn(esCourse);
        when(esCourseService.save(any(com.vinncorp.fast_learner.es_models.Course.class))).thenReturn(new Message<>());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            Message<CreateCourseRequest> response = courseService.createCourse(createCourseRequest,null, EMAIL);
        });

        assertEquals("Quiz question answers must have at least one correct answer", exception.getMessage());
    }

    @DisplayName("Create course with no article content")
    @Test
    public void testCreateCourse_NullArticleContent() throws EntityNotFoundException, InternalServerException, BadRequestException, IOException {

        CreateCourseRequest createCourseRequest = this.getMockCourse();
        Course mockCourse = new Course();
        createCourseRequest.setCourseId(null);
        mockCourse.setTitle("The Unsung Selldiers");
        createCourseRequest.setIsActive(false);
        createCourseRequest.setCourseType(CourseType.FREE_COURSE.name());

        createCourseRequest.getSections().stream()
                .flatMap(section -> section.getTopics().stream())
                .filter(topic -> topic.getTopicTypeId() == 2)
                .forEach(top -> top.getArticle().setArticle(null));

        com.vinncorp.fast_learner.es_models.Course esCourse = new com.vinncorp.fast_learner.es_models.Course();
        esCourse.setId(null);

        // Mock repository save method
        when(repo.save(any(Course.class))).thenReturn(mockCourse);
        when(repo.findById(anyLong())).thenReturn(Optional.of(mockCourse));
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseLevelService.findById(1L)).thenReturn(courseLevel);
        when(courseCategoryService.findById(1L)).thenReturn(courseCategory);
        doNothing().when(tagService).createAllNewAndAlreadyExistsTags(anyList(), any(Course.class));
//        doNothing().when(rabbitMQProducer).sendMessageToUsers(anyString(), anyLong(), anyString(), any(), any());
        when(sectionService.save(any(Section.class))).thenReturn(mockSection);
        when(topicTypeService.findById(1L)).thenReturn(topicTypeVideo);
        when(topicTypeService.findById(2L)).thenReturn(topicTypeArticle);
        when(topicTypeService.findById(4L)).thenReturn(topicTypeQuiz);
        when(topicService.save(any(Topic.class))).thenReturn(mockTopic);
        when(quizQuestionService.save(any(QuizQuestion.class))).thenReturn(quizQuestion);
        when(videoService.save(any(Video.class))).thenReturn(video);

        when(esCourseService.findByDBId(anyLong())).thenReturn(esCourse);
        when(esCourseService.save(any(com.vinncorp.fast_learner.es_models.Course.class))).thenReturn(new Message<>());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            Message<CreateCourseRequest> response = courseService.createCourse(createCourseRequest,null, EMAIL);
        });

        assertEquals("Article content cannot be empty", exception.getMessage());
    }

    @DisplayName("Fetch courses by teacher with valid data")
    @Test
    public void testFindCoursesByTeacher_Success() throws EntityNotFoundException, BadRequestException {
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findAllCoursesWithFilter(anyLong(), anyInt(), anyString(), any(Pageable.class))).thenReturn(mockPage);

        Message<TeacherCoursesResponse> response = courseService.findCoursesByTeacher("search", 0, 0, 10, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Fetched teachers courses successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getCourses().size());
        assertEquals(1L, response.getData().getCourses().get(0).getId());
    }

    @DisplayName("Fetch courses by teacher with invalid data no course found")
    @Test
    public void testFindCoursesByTeacher_NoCoursesFound() throws EntityNotFoundException {
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findAllCoursesWithFilter(anyLong(), anyInt(), anyString(), any(Pageable.class))).thenReturn(Page.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            courseService.findCoursesByTeacher("search", 0, 0, 10, EMAIL);
        });

        assertEquals("No result found for provided filters.", exception.getMessage());
    }

    @DisplayName("Fetch teacher courses with invalid user")
    @Test
    public void testFindCoursesByTeacher_UserNotFound() throws EntityNotFoundException {
        when(userService.findByEmail(EMAIL)).thenThrow(new EntityNotFoundException("User not found"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            courseService.findCoursesByTeacher("search", 0, 0, 10, EMAIL);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @DisplayName("Fetch teacher courses with invalid page no")
    @Test
    public void testFindCoursesByTeacher_InvalidPageNumber() throws EntityNotFoundException {

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            courseService.findCoursesByTeacher("search", 0, -1, 10, EMAIL);
        });

        assertEquals("Page no or Page size cannot be negative", exception.getMessage());
    }

    @DisplayName("Fetch teacher courses with invalid page size")
    @Test
    public void testFindCoursesByTeacher_InvalidPageSize() throws EntityNotFoundException {

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            courseService.findCoursesByTeacher("search", 0, 0, -10, EMAIL);
        });

        assertEquals("Page no or Page size cannot be negative", exception.getMessage());
    }

    @DisplayName("Fetch courses by teacher with null input")
    @Test
    public void testFindByCourseTeacher_NullSearchInput() throws EntityNotFoundException, BadRequestException {
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findAllCoursesWithFilter(anyLong(), anyInt(), anyString(), any(Pageable.class))).thenReturn(mockPage);

        Message<TeacherCoursesResponse> response = courseService.findCoursesByTeacher(null, 0, 0, 10, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Fetched teachers courses successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getCourses().size());
        assertEquals(1L, response.getData().getCourses().get(0).getId());
    }

    @DisplayName("Fetch courses by teacher with invalid sort number")
    @Test
    public void testFindByCourseTeacher_InvalidSortNumber() throws EntityNotFoundException, BadRequestException {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            courseService.findCoursesByTeacher("search", 3, 0, 10, EMAIL);
        });

        assertEquals("Sort can only be 0 or 1", exception.getMessage());
    }

    @DisplayName("Fetch course by id with valid data")
    @Test
    public void fetchCourseById_whenProvidedValidValue() throws EntityNotFoundException {
        Course savedCourse = Course.builder().id(1L).title("Test Course").build();
        when(repo.findById(12L)).thenReturn(Optional.of(savedCourse));
        Course course = courseService.findById(12L);

        assertThat(course).isNotNull();
        assertThat(course.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Fetch courses by category with pagination with valid data")
    public void testGetCoursesByCategoryWithPagination_Success() throws Exception {
        String email = "user@example.com";
        CourseByCategoryRequest request = CourseTestData.getCourseByCategoryRequest();

        User user = UserTestData.userData();
        Tuple tuple1 = mock(Tuple.class);
        Tuple tuple2 = mock(Tuple.class);

        // Define the behavior of the tuple mock for the first tuple
        when(tuple1.get("course_id")).thenReturn(1L);
        when(tuple1.get("course_title")).thenReturn("Course 1");
        when(tuple1.get("course_description")).thenReturn("Description 1");
        when(tuple1.get("about")).thenReturn("About Course 1");
        when(tuple1.get("course_thumbnail")).thenReturn("thumbnail1.jpg");
        when(tuple1.get("video_url")).thenReturn("video1.mp4");
        when(tuple1.get("prerequisite")).thenReturn("Prerequisite 1");
        when(tuple1.get("course_outcome")).thenReturn("Outcome 1");
        when(tuple1.get("last_mod_date")).thenReturn(new Date());
        when(tuple1.get("level")).thenReturn("Beginner");
        when(tuple1.get("user_id")).thenReturn(100L);
        when(tuple1.get("full_name")).thenReturn("John Doe");
        when(tuple1.get("profile_picture")).thenReturn("profile_pic1.jpg");
        when(tuple1.get("about_me")).thenReturn("About Me 1");
        when(tuple1.get("headline")).thenReturn("Headline 1");
        when(tuple1.get("course_duration_in_hours")).thenReturn(10);
        when(tuple1.get("max_rating", Object.class)).thenReturn("4.5");
        when(tuple1.get("total_reviews")).thenReturn(100);
        when(tuple1.get("category")).thenReturn("Category 1");
        when(tuple1.get("is_enrolled")).thenReturn("true");
        when(tuple1.get("is_favourite")).thenReturn("false");
        when(tuple1.get("has_certificate")).thenReturn(true);
        when(tuple1.get("created_date")).thenReturn(new Date());
        when(tuple1.get("total_students")).thenReturn(200);
        when(tuple1.get("already_bought")).thenReturn(true);


        // Define the behavior of the tuple mock for the second tuple
        when(tuple2.get("course_id")).thenReturn(2L);
        when(tuple2.get("course_title")).thenReturn("Course 2");
        when(tuple2.get("course_description")).thenReturn("Description 2");
        when(tuple2.get("about")).thenReturn("About Course 2");
        when(tuple2.get("course_thumbnail")).thenReturn("thumbnail2.jpg");
        when(tuple2.get("video_url")).thenReturn("video2.mp4");
        when(tuple2.get("prerequisite")).thenReturn("Prerequisite 2");
        when(tuple2.get("course_outcome")).thenReturn("Outcome 2");
        when(tuple2.get("last_mod_date")).thenReturn(new Date());
        when(tuple2.get("level")).thenReturn("Intermediate");
        when(tuple2.get("user_id")).thenReturn(101L);
        when(tuple2.get("full_name")).thenReturn("Jane Doe");
        when(tuple2.get("profile_picture")).thenReturn("profile_pic2.jpg");
        when(tuple2.get("about_me")).thenReturn("About Me 2");
        when(tuple2.get("headline")).thenReturn("Headline 2");
        when(tuple2.get("course_duration_in_hours")).thenReturn(20);
        when(tuple2.get("max_rating", Object.class)).thenReturn("4.0");
        when(tuple2.get("total_reviews")).thenReturn(150);
        when(tuple2.get("category")).thenReturn("Category 2");
        when(tuple2.get("is_enrolled")).thenReturn("false");
        when(tuple2.get("is_favourite")).thenReturn("true");
        when(tuple2.get("has_certificate")).thenReturn(false);
        when(tuple2.get("created_date")).thenReturn(new Date());
        when(tuple2.get("total_students")).thenReturn(300);
        when(tuple1.get("already_bought")).thenReturn(true);


        List<Tuple> tuples = Arrays.asList(tuple1, tuple2);
        Page<Tuple> page = new PageImpl<>(tuples, PageRequest.of(0, 10), 2);

        when(userService.findByEmail(email)).thenReturn(user);
        when(courseCategoryService.findById(courseCategory.getId())).thenReturn(courseCategory);
        when(courseLevelService.findById(courseLevel.getId())).thenReturn(courseLevel);
        when(repo.findAllByCoursesCategoryOrCourseIdAndMostReviewed(
                courseCategory.getId(), courseLevel.getId(), null, user.getId(), PageRequest.of(request.getPageNo(), request.getPageSize())
        )).thenReturn(page);

        List<NoOfTopicInCourse> topicData = Arrays.asList(
                new NoOfTopicInCourse(1L, 5, 210),
                new NoOfTopicInCourse(2L, 3, 260)
        );

        when(topicService.getAllTopicByCourses(Arrays.asList(1L, 2L))).thenReturn(topicData);

        // Act
        Message<CourseByCategoryPaginatedResponse> response = courseService.getCoursesByCategoryWithPagination(request, email);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Courses fetched successfully.", response.getMessage());
        assertEquals(2, response.getData().getTotalElements());
        assertEquals(1, response.getData().getPages());
        assertEquals(0, response.getData().getPageNo());
        assertEquals(10, response.getData().getPageSize());
        assertEquals(5, response.getData().getData().get(0).getNoOfTopics());
        assertEquals(210, response.getData().getData().get(0).getCourseDuration());
        assertEquals(3, response.getData().getData().get(1).getNoOfTopics());
        assertEquals(260, response.getData().getData().get(1).getCourseDuration());
    }

    @Test
    @DisplayName("Fetch courses by category with pagination when no courses found")
    public void testGetCoursesByCategoryWithPagination_NoCoursesFound() throws EntityNotFoundException {
        String email = "user@example.com";
        CourseByCategoryRequest request = new CourseByCategoryRequest(courseCategory.getId(), courseLevel.getId(), 0, 10);

        User user = UserTestData.userData();

        when(userService.findByEmail(email)).thenReturn(user);
        when(courseCategoryService.findById(courseCategory.getId())).thenReturn(courseCategory);
        when(courseLevelService.findById(courseLevel.getId())).thenReturn(courseLevel);
        when(repo.findAllByCoursesCategoryOrCourseIdAndMostReviewed(
                courseCategory.getId(), courseLevel.getId(), null, user.getId(), PageRequest.of(request.getPageNo(), request.getPageSize())
        )).thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            courseService.getCoursesByCategoryWithPagination(request, email);
        });

        assertEquals("No courses found.", exception.getMessage());
    }

    @Test
    @DisplayName("Get courses by instructor for profile when provided valid data")
    public void testGetCoursesByInstructorForProfile_Success() throws EntityNotFoundException {
        User user = UserTestData.userData();

        Tuple tuple = mock(Tuple.class);
        when(tuple.get("course_id")).thenReturn(1L);
        when(tuple.get("course_title")).thenReturn("Course 1");
        when(tuple.get("course_description")).thenReturn("Description 1");
        when(tuple.get("about")).thenReturn("About Course 1");
        when(tuple.get("course_thumbnail")).thenReturn("thumbnail1.jpg");
        when(tuple.get("video_url")).thenReturn("video1.mp4");
        when(tuple.get("prerequisite")).thenReturn("Prerequisite 1");
        when(tuple.get("course_outcome")).thenReturn("Outcome 1");
        when(tuple.get("last_mod_date")).thenReturn(new Date());
        when(tuple.get("level")).thenReturn("Beginner");
        when(tuple.get("user_id")).thenReturn(100L);
        when(tuple.get("full_name")).thenReturn("John Doe");
        when(tuple.get("profile_picture")).thenReturn("profile_pic1.jpg");
        when(tuple.get("about_me")).thenReturn("About Me 1");
        when(tuple.get("headline")).thenReturn("Headline 1");
        when(tuple.get("course_duration_in_hours")).thenReturn(10);
        when(tuple.get("max_rating", Double.class)).thenReturn(4.5);
        when(tuple.get("total_reviews")).thenReturn(100);
        when(tuple.get("category")).thenReturn("Category 1");
        when(tuple.get("is_enrolled")).thenReturn("true");
        when(tuple.get("is_favourite")).thenReturn("false");
        when(tuple.get("has_certificate")).thenReturn(true);
        when(tuple.get("created_date")).thenReturn(new Date());
        when(tuple.get("total_students")).thenReturn(200);

        Page<Tuple> page = new PageImpl<>(Collections.singletonList(tuple));
        when(userService.findByEmail(anyString())).thenReturn(user);
        when(repo.findAllByInstructorAndLoggedInUser(anyLong(), any(), any(PageRequest.class))).thenReturn(page);

        // Act
        Message<CourseByCategoryPaginatedResponse> response = courseService.getCoursesByInstructorForProfile(
                1L, 0, 10, user.getEmail());

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(0, response.getData().getPageNo());
        assertEquals(10, response.getData().getPageSize());
        assertEquals(1, response.getData().getTotalElements());
        assertEquals("Courses fetched successfully.", response.getMessage());
    }

    @Test
    @DisplayName("Get courses by instructor for profile when provided empty course content")
    public void testGetCoursesByInstructorForProfile_NoCoursesFound() throws EntityNotFoundException {
        User user = UserTestData.userData();
        Page<Tuple> emptyPage = new PageImpl<>(Collections.emptyList());
        when(userService.findByEmail(anyString())).thenReturn(user);
        when(repo.findAllByInstructorAndLoggedInUser(anyLong(),any(), any(PageRequest.class))).thenReturn(emptyPage);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            courseService.getCoursesByInstructorForProfile(1L, 0, 10, user.getEmail());
        });
    }

    @Test
    public void testGetCoursesByInstructorForProfile_NullInstructorId() throws EntityNotFoundException {
        User user = UserTestData.userData();

        Tuple tuple = mock(Tuple.class);
        when(tuple.get("course_id")).thenReturn(1L);
        when(tuple.get("course_title")).thenReturn("Course 1");
        when(tuple.get("course_description")).thenReturn("Description 1");
        when(tuple.get("about")).thenReturn("About Course 1");
        when(tuple.get("course_thumbnail")).thenReturn("thumbnail1.jpg");
        when(tuple.get("video_url")).thenReturn("video1.mp4");
        when(tuple.get("prerequisite")).thenReturn("Prerequisite 1");
        when(tuple.get("course_outcome")).thenReturn("Outcome 1");
        when(tuple.get("last_mod_date")).thenReturn(new Date());
        when(tuple.get("level")).thenReturn("Beginner");
        when(tuple.get("user_id")).thenReturn(100L);
        when(tuple.get("full_name")).thenReturn("John Doe");
        when(tuple.get("profile_picture")).thenReturn("profile_pic1.jpg");
        when(tuple.get("about_me")).thenReturn("About Me 1");
        when(tuple.get("headline")).thenReturn("Headline 1");
        when(tuple.get("course_duration_in_hours")).thenReturn(10);
        when(tuple.get("max_rating", Double.class)).thenReturn(4.5);
        when(tuple.get("total_reviews")).thenReturn(100);
        when(tuple.get("category")).thenReturn("Category 1");
        when(tuple.get("is_enrolled")).thenReturn("true");
        when(tuple.get("is_favourite")).thenReturn("false");
        when(tuple.get("has_certificate")).thenReturn(true);
        when(tuple.get("created_date")).thenReturn(new Date());
        when(tuple.get("total_students")).thenReturn(200);

        Page<Tuple> page = new PageImpl<>(Collections.singletonList(tuple));
        when(userService.findByEmail(anyString())).thenReturn(user);
        when(repo.findAllByInstructorAndLoggedInUser(eq(user.getId()), any(), any(PageRequest.class)))
                .thenReturn(page);

        Message<CourseByCategoryPaginatedResponse> response = courseService.getCoursesByInstructorForProfile(
                null, 0, 10, user.getEmail());

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(0, response.getData().getPageNo());
        assertEquals(10, response.getData().getPageSize());
        assertEquals(1, response.getData().getTotalElements());
        assertEquals("Courses fetched successfully.", response.getMessage());
    }

    @Test
    @DisplayName("Course search success when provided valid data")
    void testSearchCourse_Success() throws EntityNotFoundException {
        User user = UserTestData.userData();
        SearchCourseRequest searchCourseRequest = CourseTestData.getSearchCourseRequest();
        searchCourseRequest.setIsNlpSearch(true);

        Tuple tuple = mock(Tuple.class);
        List<Tuple> tupleList = new ArrayList<>();
        tupleList.add(tuple);
        Page<Tuple> page = new PageImpl<>(tupleList, PageRequest.of(0, 10), 1);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", AUTH_TOKEN);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("query", "raw String");

        HttpEntity<Map> entity = new HttpEntity<>(requestBody, httpHeaders);

        CourseResponse courseResponse = new CourseResponse();
        courseResponse.setAverageSimilarity(85.5);
        courseResponse.setCourseId(1);
        courseResponse.setCourseTitle("Example Course");
        courseResponse.setCourseUrl("http://example.com/course/1");
        courseResponse.setDescription("This is an example course");
        courseResponse.setDuration(120);
        courseResponse.setInstructorId(100L);
        courseResponse.setInstructorName("John Doe");
        courseResponse.setInstructorProfilePicture("http://example.com/instructor/100/pic.jpg");
        courseResponse.setRating(4.5);
        courseResponse.setNoOfReviewers(200L);
        courseResponse.setThumbnail("http://example.com/course/1/thumb.jpg");
        courseResponse.setTopicTitle("Technology");

        List<CourseResponse> mockNlpSearchResponse = Collections.singletonList(courseResponse);
        ResponseEntity<List<CourseResponse>> mockResponseEntity = new ResponseEntity<>(mockNlpSearchResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(NLP_SEARCH_PYTHON_SERVICE),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(mockResponseEntity);

        when(userService.findByEmail(anyString())).thenReturn(user);
        when(repo.findAllBySearchFilter(anyString(), any(), any(), any(), anyBoolean(), anyList(), any(PageRequest.class)))
                .thenReturn(page);

        // Act
        Message<CourseBySearchFilterResponse> response = courseService.searchCourse(searchCourseRequest, user.getEmail());

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Courses fetched successfully.", response.getMessage());
        assertEquals(1, response.getData().getPages());
        assertEquals(10, response.getData().getPageSize());
        verify(repo, times(1)).findAllBySearchFilter(anyString(), any(), any(), any(), anyBoolean(), anyList(), any(PageRequest.class));
    }

    @Test
    @DisplayName("Course search not found when provided non existing course data")
    void testSearchCourse_NoCoursesFound() throws EntityNotFoundException {
        User user = UserTestData.userData();
        SearchCourseRequest searchCourseRequest = CourseTestData.getSearchCourseRequest();
        searchCourseRequest.setIsNlpSearch(false);
        Page<Tuple> emptyPage = Page.empty(PageRequest.of(0, 10));

        when(userService.findByEmail(anyString())).thenReturn(user);
        when(repo.findAllBySearchFilter(anyString(), any(), any(), any(), anyBoolean(), anyList(), any(PageRequest.class)))
                .thenReturn(emptyPage);

        assertThrows(EntityNotFoundException.class, () -> courseService.searchCourse(searchCourseRequest, user.getEmail()));
        verify(repo, times(1)).findAllBySearchFilter(anyString(), any(), any(), any(), anyBoolean(), anyList(), any(PageRequest.class));
    }

    @Test
    @DisplayName("Course search with not logged in user")
    void testSearchCourse_NullEmail() throws EntityNotFoundException {

        SearchCourseRequest searchCourseRequest = CourseTestData.getSearchCourseRequest();
        searchCourseRequest.setIsNlpSearch(true);

        Tuple tuple = mock(Tuple.class);
        List<Tuple> tupleList = new ArrayList<>();
        tupleList.add(tuple);
        Page<Tuple> page = new PageImpl<>(tupleList, PageRequest.of(0, 10), 1);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", AUTH_TOKEN);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("query", "raw String");

        HttpEntity<Map> entity = new HttpEntity<>(requestBody, httpHeaders);

        CourseResponse courseResponse = new CourseResponse();
        courseResponse.setAverageSimilarity(85.5);
        courseResponse.setCourseId(1);
        courseResponse.setCourseTitle("Example Course");
        courseResponse.setCourseUrl("http://example.com/course/1");
        courseResponse.setDescription("This is an example course");
        courseResponse.setDuration(120);
        courseResponse.setInstructorId(100L);
        courseResponse.setInstructorName("John Doe");
        courseResponse.setInstructorProfilePicture("http://example.com/instructor/100/pic.jpg");
        courseResponse.setRating(4.5);
        courseResponse.setNoOfReviewers(200L);
        courseResponse.setThumbnail("http://example.com/course/1/thumb.jpg");
        courseResponse.setTopicTitle("Technology");

        List<CourseResponse> mockNlpSearchResponse = Collections.singletonList(courseResponse);
        ResponseEntity<List<CourseResponse>> mockResponseEntity = new ResponseEntity<>(mockNlpSearchResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(NLP_SEARCH_PYTHON_SERVICE),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(mockResponseEntity);

        when(repo.findAllBySearchFilter(anyString(), any(), any(), any(), anyBoolean(), anyList(), any(PageRequest.class)))
                .thenReturn(page);

        Message<CourseBySearchFilterResponse> response = courseService.searchCourse(searchCourseRequest, null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Courses fetched successfully.", response.getMessage());
        assertEquals(1, response.getData().getPages());
        assertEquals(10, response.getData().getPageSize());

        verify(repo, times(1)).findAllBySearchFilter(anyString(), any(), any(), any(), anyBoolean(), anyList(), any(PageRequest.class));
        verify(userService, never()).findByEmail(anyString());
    }


    @Test
    @DisplayName("Course search when findByEmail return error")
    void testSearchCourse_ExceptionHandling() throws EntityNotFoundException {
        User user = UserTestData.userData();
        SearchCourseRequest searchCourseRequest = CourseTestData.getSearchCourseRequest();
        searchCourseRequest.setIsNlpSearch(true);

        when(userService.findByEmail(anyString())).thenThrow(new RuntimeException("Error fetching user"));
        Tuple tuple = mock(Tuple.class);
        List<Tuple> tupleList = new ArrayList<>();
        tupleList.add(tuple);
        Page<Tuple> page = new PageImpl<>(tupleList, PageRequest.of(0, 10), 1);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", AUTH_TOKEN);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("query", "raw String");

        HttpEntity<Map> entity = new HttpEntity<>(requestBody, httpHeaders);

        CourseResponse courseResponse = new CourseResponse();
        courseResponse.setAverageSimilarity(85.5);
        courseResponse.setCourseId(1);
        courseResponse.setCourseTitle("Example Course");
        courseResponse.setCourseUrl("http://example.com/course/1");
        courseResponse.setDescription("This is an example course");
        courseResponse.setDuration(120);
        courseResponse.setInstructorId(100L);
        courseResponse.setInstructorName("John Doe");
        courseResponse.setInstructorProfilePicture("http://example.com/instructor/100/pic.jpg");
        courseResponse.setRating(4.5);
        courseResponse.setNoOfReviewers(200L);
        courseResponse.setThumbnail("http://example.com/course/1/thumb.jpg");
        courseResponse.setTopicTitle("Technology");

        List<CourseResponse> mockNlpSearchResponse = Collections.singletonList(courseResponse);
        ResponseEntity<List<CourseResponse>> mockResponseEntity = new ResponseEntity<>(mockNlpSearchResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(NLP_SEARCH_PYTHON_SERVICE),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(mockResponseEntity);

        when(repo.findAllBySearchFilter(anyString(), any(), any(), any(), anyBoolean(), anyList(), any(PageRequest.class)))
                .thenReturn(page);

        // Act
        Message<CourseBySearchFilterResponse> response = courseService.searchCourse(searchCourseRequest, user.getEmail());

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Courses fetched successfully.", response.getMessage());
        verify(repo, times(1)).findAllBySearchFilter(anyString(), any(), any(), any(), anyBoolean(), anyList(), any(PageRequest.class));
    }

    private CreateCourseRequest getMockCourse() throws IOException {
        File createCourseRequestJsonFile = new File("src/main/resources/testing/CreateCourseRequest.json");
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(createCourseRequestJsonFile, CreateCourseRequest.class);
    }


    @Test
    @DisplayName("Should fetch all new courses successfully")
    void testGetAllNewCourse_Success() throws EntityNotFoundException {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);
        Principal principal = mock(Principal.class);
        List<Tuple> mockTuples = new ArrayList<>();

        // Mock user and repository behavior
        when(principal.getName()).thenReturn("testuser@example.com");
        User mockUser = mock(User.class);
        when(userService.findByEmail("testuser@example.com")).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn(1L);

        // Create and setup mock Tuple instances with all expected field values
        Tuple mockTuple1 = mock(Tuple.class);
        Tuple mockTuple2 = mock(Tuple.class);
        when(mockTuple1.get("course_id")).thenReturn(1L);
        when(mockTuple1.get("title")).thenReturn("Course 1");
        when(mockTuple1.get("description")).thenReturn("Description 1"); // Additional fields as required
        when(mockTuple2.get("course_id")).thenReturn(2L);
        when(mockTuple2.get("title")).thenReturn("Course 2");
        when(mockTuple2.get("description")).thenReturn("Description 2"); // Additional fields as required
        mockTuples.add(mockTuple1);
        mockTuples.add(mockTuple2);

        // Mock the Page object to return the tuples
        Page<Tuple> mockPage = mock(Page.class);
        when(mockPage.getContent()).thenReturn(mockTuples);
        when(mockPage.getTotalPages()).thenReturn(1);
        when(mockPage.getTotalElements()).thenReturn(2L);

        // Mock repository to return the mock page
        when(repo.findByCreationDateDesc(1L, pageable)).thenReturn(mockPage);

        // Act
        Message<CourseDetailByPaginatedResponse> response = courseService.getAllNewCourse(pageable, principal);

        // Check that data is populated as expected
        assertNotNull(response.getData().getData(), "Data list should not be null");

        // Assert specific content of the response
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("All new courses fetched successfully.", response.getMessage());

        // Verify interactions with mocks
        verify(repo, times(1)).findByCreationDateDesc(1L, pageable);
        verify(userService, times(1)).findByEmail("testuser@example.com");
    }

    @Test
    @DisplayName("Should handle exception while fetching courses")
    void testGetAllNewCourse_Exception() throws EntityNotFoundException {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Principal principal = mock(Principal.class);

        when(principal.getName()).thenReturn("testuser@example.com");
        when(userService.findByEmail("testuser@example.com")).thenReturn(null); // No user found for simplicity
        when(repo.findByCreationDateDesc(null, pageable)).thenThrow(new RuntimeException("Database error"));

        // Act
        Message<CourseDetailByPaginatedResponse> response = courseService.getAllNewCourse(pageable, principal);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.name(), response.getCode());
        assertEquals("An error occurred while fetching courses.", response.getMessage());

        // Verify interactions
        verify(repo).findByCreationDateDesc(null, pageable);
        verify(userService).findByEmail("testuser@example.com");
    }


    @Test
    @DisplayName("Should return correct pagination information")
    void testGetAllNewCourse_Pagination() throws EntityNotFoundException {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser@example.com");
        User mockUser = mock(User.class);
        when(userService.findByEmail("testuser@example.com")).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn(1L);

        Tuple mockTuple1 = mock(Tuple.class);
        Tuple mockTuple2 = mock(Tuple.class);
        when(mockTuple1.get("course_id")).thenReturn(1L);
        when(mockTuple1.get("title")).thenReturn("Course 1");
        when(mockTuple2.get("course_id")).thenReturn(2L);
        when(mockTuple2.get("title")).thenReturn("Course 2");

        List<Tuple> mockTuples = Arrays.asList(mockTuple1, mockTuple2);

        Page<Tuple> mockPage = mock(Page.class);
        when(mockPage.getContent()).thenReturn(mockTuples);
        when(mockPage.getTotalPages()).thenReturn(5);
        when(mockPage.getTotalElements()).thenReturn(10L);

        when(repo.findByCreationDateDesc(1L, pageable)).thenReturn(mockPage);

        // Act
        Message<CourseDetailByPaginatedResponse> response = courseService.getAllNewCourse(pageable, principal);

        // Assert
        assertEquals(0, response.getData().getPageNo());
        assertEquals(2, response.getData().getPageSize());
        assertEquals(5, response.getData().getPages());
        assertEquals(10L, response.getData().getTotalElements());
        verify(repo, times(1)).findByCreationDateDesc(1L, pageable);
    }



    @Test
    @DisplayName("Test empty result for free courses")
    void testGetAllFreeCourse_NoContent() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tuple> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(repo.findAllFreeCourses(pageable)).thenReturn(emptyPage);

        // Act
        Message<CourseDetailByPaginatedResponse> response = courseService.getAllFreeCourse(pageable);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("All free courses fetched successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(0, response.getData().getTotalElements()); // Check for empty response
    }

    @Test
    @DisplayName("Test exception handling when fetching free courses")
    void testGetAllFreeCourse_Exception() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(repo.findAllFreeCourses(pageable)).thenThrow(new RuntimeException("Database error"));

        // Act
        Message<CourseDetailByPaginatedResponse> response = courseService.getAllFreeCourse(pageable);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.name(), response.getCode());
        assertEquals("An error occurred while fetching free courses.", response.getMessage());
    }

//premium course

    @Test
    @DisplayName("Test empty result for premium courses")
    void testGetAllPremiumCourse_NoContent() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tuple> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(repo.findAllFreeCourses(pageable)).thenReturn(emptyPage);

        // Act
        Message<CourseDetailByPaginatedResponse> response = courseService.getAllPremiumCourses(pageable,null);

//         Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("All premium courses fetched successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(0, response.getData().getTotalElements()); // Check for empty response
    }

    @Test
    @DisplayName("fetching Premium courses")
    void testGetAllPremiumCourse_Exception() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(repo.findAllFreeCourses(pageable)).thenThrow(new RuntimeException("Database error"));

        // Act
        Message<CourseDetailByPaginatedResponse> response = courseService.getAllPremiumCourses(pageable,null);

//         Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("200 "+HttpStatus.OK.name(), response.getCode());
        assertEquals("All premium courses fetched successfully.", response.getMessage());
    }


    @Test
    @DisplayName("Should fetch all premium courses successfully with principal")
    void testGetAllPremiumCourses_Success_WithPrincipal() throws EntityNotFoundException {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);
        Principal principal = mock(Principal.class);
        List<Tuple> mockTuples = new ArrayList<>();

        // Mock user and repository behavior
        when(principal.getName()).thenReturn("testuser@example.com");
        User mockUser = mock(User.class);
        when(userService.findByEmail("testuser@example.com")).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn(1L);

        // Mock tuples and page data
        Tuple mockTuple1 = mock(Tuple.class);
        Tuple mockTuple2 = mock(Tuple.class);
        mockTuples.add(mockTuple1);
        mockTuples.add(mockTuple2);

        when(mockTuple1.get("course_id")).thenReturn(1L);
        when(mockTuple1.get("title")).thenReturn("Premium Course 1");
        when(mockTuple2.get("course_id")).thenReturn(2L);
        when(mockTuple2.get("title")).thenReturn("Premium Course 2");

        Page<Tuple> mockPage = mock(Page.class);
        when(mockPage.getContent()).thenReturn(mockTuples);
        when(mockPage.getTotalPages()).thenReturn(1);
        when(mockPage.getTotalElements()).thenReturn(2L);
        when(repo.findPremiumCourses(1L, pageable)).thenReturn(mockPage);

        // Act
        Message<CourseDetailByPaginatedResponse> response = courseService.getAllPremiumCourses(pageable, principal);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("All premium courses fetched successfully.", response.getMessage());

        assertNotNull(response.getData());
        assertEquals(2, response.getData().getPageSize());
        assertEquals(1, response.getData().getPages());
        assertEquals(2L, response.getData().getTotalElements());

        verify(repo, times(1)).findPremiumCourses(1L, pageable);
        verify(userService, times(1)).findByEmail("testuser@example.com");
    }

    @Test
    @DisplayName("Should handle missing fields gracefully")
    void testFromCourseData_MissingFields() {
        Tuple tuple = mock(Tuple.class);
        // Arrange
        when(tuple.get("course_id")).thenReturn(1L);
        when(tuple.get("title")).thenReturn(null);  // Missing title
        when(tuple.get("already_bought")).thenReturn(null);
        when(tuple.get("duration")).thenReturn(null); // Missing duration
        when(tuple.get("total_reviews")).thenReturn(null); // Missing total_reviews

        // Act
        CourseDetailResponse response = CourseDetailResponse.fromCourseData(tuple);

        // Assert
        assertEquals(1L, response.getCourseId());
        assertNull(response.getTitle());  // Title should be null
        assertFalse(response.getIsAlreadyBought());  // Default to false
        assertEquals(0, response.getCourseDuration());  // Default duration to 0
        assertEquals(0, response.getNoOfReviewers());  // Default reviewers to 0
    }

    @Test
    @DisplayName("Should handle exception during alreadyBought parsing")
    void testFromCourseData_ExceptionInAlreadyBought() {
        Tuple tuple = mock(Tuple.class);
        // Arrange
        when(tuple.get("already_bought")).thenReturn(null);  // Should lead to false

        // Act
        CourseDetailResponse response = CourseDetailResponse.fromCourseData(tuple);

        // Assert
        assertFalse(response.getIsAlreadyBought());
    }

// trending course
    @Test
    @DisplayName("Should return trending courses when available")
    void testGetAllTrendingCourses_Success_WithResults() throws EntityNotFoundException {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);

        // Create mock Tuple objects
        Tuple mockTuple1 = mock(Tuple.class);
        Tuple mockTuple2 = mock(Tuple.class);

        // Mocking first tuple (Course 1)
        when(mockTuple1.get("course_id")).thenReturn(1L);
        when(mockTuple1.get("title")).thenReturn("Course Title 1");
        when(mockTuple1.get("description")).thenReturn("Description 1");
        when(mockTuple1.get("course_level")).thenReturn("Beginner");
        when(mockTuple1.get("category_name")).thenReturn("Programming");
        when(mockTuple1.get("course_thumbnail")).thenReturn("http://thumbnail1.url");
        when(mockTuple1.get("duration")).thenReturn(10);
        when(mockTuple1.get("user_id")).thenReturn(2L);
        when(mockTuple1.get("full_name")).thenReturn("Instructor 1");
        when(mockTuple1.get("profile_picture")).thenReturn("http://profilepic1.url");
        when(mockTuple1.get("about_me")).thenReturn("About Instructor 1");
        when(mockTuple1.get("headline")).thenReturn("Headline 1");
        when(mockTuple1.get("max_rating")).thenReturn(4.5);
        when(mockTuple1.get("total_reviews")).thenReturn(100);
        when(mockTuple1.get("is_all_free")).thenReturn(true);
        when(mockTuple1.get("profile_url")).thenReturn("http://profileurl1.url");
        when(mockTuple1.get("created_date")).thenReturn(new java.util.Date());

        // Mocking second tuple (Course 2)
        when(mockTuple2.get("course_id")).thenReturn(3L);
        when(mockTuple2.get("title")).thenReturn("Course Title 2");
        when(mockTuple2.get("description")).thenReturn("Description 2");
        when(mockTuple2.get("course_level")).thenReturn("Intermediate");
        when(mockTuple2.get("category_name")).thenReturn("Data Science");
        when(mockTuple2.get("course_thumbnail")).thenReturn("http://thumbnail2.url");
        when(mockTuple2.get("duration")).thenReturn(20);
        when(mockTuple2.get("user_id")).thenReturn(4L);
        when(mockTuple2.get("full_name")).thenReturn("Instructor 2");
        when(mockTuple2.get("profile_picture")).thenReturn("http://profilepic2.url");
        when(mockTuple2.get("about_me")).thenReturn("About Instructor 2");
        when(mockTuple2.get("headline")).thenReturn("Headline 2");
        when(mockTuple2.get("max_rating")).thenReturn(4.8);
        when(mockTuple2.get("total_reviews")).thenReturn(200);
        when(mockTuple2.get("is_all_free")).thenReturn(false);
        when(mockTuple2.get("profile_url")).thenReturn("http://profileurl2.url");
        when(mockTuple2.get("created_date")).thenReturn(new java.util.Date());

        // Create a list of tuples and set up the mock behavior
        List<Tuple> tuples = List.of(mockTuple1, mockTuple2);
        Page<Tuple> mockPage = mock(Page.class);
        when(mockPage.getContent()).thenReturn(tuples);
        when(mockPage.stream()).thenReturn(tuples.stream());
        when(mockPage.hasContent()).thenReturn(true);
        when(mockPage.getTotalPages()).thenReturn(1);
        when(mockPage.getTotalElements()).thenReturn((long) tuples.size());

        // Mock repository call
        when(repo.findAllTrendingCourses(null, pageable)).thenReturn(mockPage);

        // Act
        Message<CourseDetailByPaginatedResponse> result = courseService.getAllTrendingCourses(pageable, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals("All trending courses fetched successfully.", result.getMessage());
        assertNotNull(result.getData());

        // Check CourseDetailByPaginatedResponse structure
        CourseDetailByPaginatedResponse responseData = result.getData();
        assertEquals(0, responseData.getPageNo());
        assertEquals(2, responseData.getPageSize());
        assertEquals(2, responseData.getTotalElements());
        assertEquals(1, responseData.getPages());

        // Check data content
        assertNotNull(responseData.getData());
        assertEquals(2, responseData.getData().size());

        // Check individual course details in the response data
        CourseDetailResponse course1 = responseData.getData().get(0);
        assertEquals("Course Title 1", course1.getTitle());
        assertEquals("Description 1", course1.getCourseDescription());
        assertEquals("Beginner", course1.getLevel());
        assertEquals("Programming", course1.getCategoryName());

        CourseDetailResponse course2 = responseData.getData().get(1);
        assertEquals("Course Title 2", course2.getTitle());
        assertEquals("Description 2", course2.getCourseDescription());
        assertEquals("Intermediate", course2.getLevel());
        assertEquals("Data Science", course2.getCategoryName());

        // Verifying method interactions
        verify(repo, times(1)).findAllTrendingCourses(null, pageable);
        verify(userService, times(0)).findByEmail(anyString()); // no principal, so no call to userService
    }


    private List<Tuple> createMockTuples() {
        List<Tuple> tuples = new ArrayList<>();

        // Create the first mock Tuple
        Tuple mockTuple1 = mock(Tuple.class);
        when(mockTuple1.get("course_id")).thenReturn(1L);
        when(mockTuple1.get("title")).thenReturn("Course Title 1");
        when(mockTuple1.get("description")).thenReturn("Description 1");
        when(mockTuple1.get("course_level")).thenReturn("Beginner");
        when(mockTuple1.get("category_name")).thenReturn("Programming");
        when(mockTuple1.get("course_thumbnail")).thenReturn("http://thumbnail1.url");
        when(mockTuple1.get("duration")).thenReturn(10);
        when(mockTuple1.get("user_id")).thenReturn(2L);
        when(mockTuple1.get("full_name")).thenReturn("Instructor 1");
        when(mockTuple1.get("profile_picture")).thenReturn("http://profilepic1.url");
        when(mockTuple1.get("about_me")).thenReturn("About Instructor 1");
        when(mockTuple1.get("headline")).thenReturn("Headline 1");
        when(mockTuple1.get("max_rating")).thenReturn(4.5);
        when(mockTuple1.get("total_reviews")).thenReturn(100);
        when(mockTuple1.get("is_all_free")).thenReturn(true);
        when(mockTuple1.get("profile_url")).thenReturn("http://profileurl1.url");
        when(mockTuple1.get("created_date")).thenReturn(new Date());

        // Create the second mock Tuple
        Tuple mockTuple2 = mock(Tuple.class);
        when(mockTuple2.get("course_id")).thenReturn(3L);
        when(mockTuple2.get("title")).thenReturn("Course Title 2");
        when(mockTuple2.get("description")).thenReturn("Description 2");
        when(mockTuple2.get("course_level")).thenReturn("Intermediate");
        when(mockTuple2.get("category_name")).thenReturn("Data Science");
        when(mockTuple2.get("course_thumbnail")).thenReturn("http://thumbnail2.url");
        when(mockTuple2.get("duration")).thenReturn(20);
        when(mockTuple2.get("user_id")).thenReturn(4L);
        when(mockTuple2.get("full_name")).thenReturn("Instructor 2");
        when(mockTuple2.get("profile_picture")).thenReturn("http://profilepic2.url");
        when(mockTuple2.get("about_me")).thenReturn("About Instructor 2");
        when(mockTuple2.get("headline")).thenReturn("Headline 2");
        when(mockTuple2.get("max_rating")).thenReturn(4.8);
        when(mockTuple2.get("total_reviews")).thenReturn(200);
        when(mockTuple2.get("is_all_free")).thenReturn(false);
        when(mockTuple2.get("profile_url")).thenReturn("http://profileurl2.url");
        when(mockTuple2.get("created_date")).thenReturn(new Date());

        tuples.add(mockTuple1);
        tuples.add(mockTuple2);

        return tuples;
    }
    @Test
    @DisplayName("Should return trending courses with user context (principal provided)")
    void testGetAllTrendingCourses_WithUserContext() throws EntityNotFoundException {
        Pageable pageable = PageRequest.of(0, 2);
        List<Tuple> tuples = createMockTuples();

        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user@example.com");

        User mockUser = new User();  // Assuming User is your entity class for users
        mockUser.setId(10L);
        when(userService.findByEmail("user@example.com")).thenReturn(mockUser);

        Page<Tuple> mockPage = mock(Page.class);
        when(mockPage.getContent()).thenReturn(tuples);
        when(mockPage.hasContent()).thenReturn(true);
        when(mockPage.getTotalPages()).thenReturn(1);
        when(mockPage.getTotalElements()).thenReturn((long) tuples.size());
        when(repo.findAllTrendingCourses(10L, pageable)).thenReturn(mockPage);

        Message<CourseDetailByPaginatedResponse> result = courseService.getAllTrendingCourses(pageable, mockPrincipal);

        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals("All trending courses fetched successfully.", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(2, tuples.size());
        verify(userService, times(1)).findByEmail("user@example.com");
    }


    @Test
    @DisplayName("Should handle missing user when principal is provided but user is not found")
    void testGetAllTrendingCourses_UserNotFoundWithPrincipal() throws EntityNotFoundException {
        Pageable pageable = PageRequest.of(0, 2);
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("missing_user@example.com");

        when(userService.findByEmail(null)).thenThrow(new EntityNotFoundException("User not found"));

        Message<CourseDetailByPaginatedResponse> result = courseService.getAllTrendingCourses(pageable, mockPrincipal);

        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals("All trending courses fetched successfully.", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().getData().size());
        verify(userService, times(1)).findByEmail("missing_user@example.com");
    }
    @Test
    @DisplayName("Should return empty result when no trending courses are found")
    void testGetAllTrendingCourses_NoResults() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Tuple> mockPage = mock(Page.class);

        when(mockPage.hasContent()).thenReturn(false);
        when(repo.findAllTrendingCourses(null, pageable)).thenReturn(mockPage);

        Message<CourseDetailByPaginatedResponse> result = courseService.getAllTrendingCourses(pageable, null);

        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals("All trending courses fetched successfully.", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().getData().size());
        assertEquals(0, result.getData().getTotalElements());
        assertEquals(0, result.getData().getPages());
    }

    @Test
    @DisplayName("Should handle exceptions when fetching trending courses")
    void testGetAllTrendingCourses_ExceptionHandling() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        when(repo.findAllTrendingCourses(null,pageable)).thenThrow(new RuntimeException("Database error"));

        // Act
        Message<CourseDetailByPaginatedResponse> result = courseService.getAllTrendingCourses(pageable,null);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
        assertEquals("An error occurred while fetching trending courses.", result.getMessage());
    }

    @Test
    @DisplayName("Should return course details when valid instructorId and courseType are provided")
    void shouldReturnCourseDetailsWhenValidInput() throws BadRequestException {
        Long instructorId = 1L;
        CourseType courseType = CourseType.PREMIUM_COURSE;
        List<CourseDetailByType> mockCourseDetails = List.of(
                new CourseDetailByType(1L, "Java Basics", "Learn Java", "thumbnail1.jpg","abc", 1L),
                new CourseDetailByType(2L, "Advanced Java", "Master Java", "thumbnail2.jpg","abc", 1L)
        );
        when(this.repo.findByCreatedByAndCourseType(instructorId, courseType))
                .thenReturn(mockCourseDetails);
        List<CourseDetailByType> result = courseService.getCourseDetailByInstructorIdAndType(instructorId, courseType);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Java Basics", result.get(0).getCourseTitle());
    }

    @Test
    @DisplayName("Should return empty list when no courses match the instructorId and courseType")
    void shouldReturnEmptyListWhenNoCoursesFound() throws BadRequestException {
        Long instructorId = 2L;
        CourseType courseType = CourseType.PREMIUM_COURSE;
        when(this.repo.findByCreatedByAndCourseType(instructorId, courseType))
                .thenReturn(Collections.emptyList());
        List<CourseDetailByType> result = courseService.getCourseDetailByInstructorIdAndType(instructorId, courseType);
        Assertions.assertTrue(result.isEmpty());
    }
    @Test
    @DisplayName("Should throw IllegalArgumentException when instructorId is null")
    void shouldThrowExceptionWhenInstructorIdIsNull() {
        Long instructorId = null;
        CourseType courseType = CourseType.PREMIUM_COURSE;
        Assertions.assertThrows(BadRequestException.class, () ->
                courseService.getCourseDetailByInstructorIdAndType(instructorId, courseType));
        verifyNoInteractions(this.repo);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when courseType is null")
    void shouldThrowExceptionWhenCourseTypeIsNull() {
        Long instructorId = 1L;
        CourseType courseType = null;
        Assertions.assertThrows(BadRequestException.class, () ->
                courseService.getCourseDetailByInstructorIdAndType(instructorId, courseType));
        verifyNoInteractions(this.repo);
    }

    @Test
    @DisplayName("Should return premium courses when valid search, name, and pageable are provided")
    void shouldReturnPremiumCoursesForValidInput() throws EntityNotFoundException {
        String search = "java";
        String name = EMAIL;
        Pageable pageable = PageRequest.of(0, 10);
        User user = UserTestData.userData();
        List<CourseDetailByType> courseDetails = List.of(
                new CourseDetailByType(1L, "Java Basics", "Learn Java", "thumbnail1.jpg","abc", 1L),
                new CourseDetailByType(2L, "Advanced Java", "Master Java", "thumbnail2.jpg","abc", 1L)
        );
        Page<CourseDetailByType> mockPage = new PageImpl<>(courseDetails, pageable, courseDetails.size());
        when(userService.findByEmail(name)).thenReturn(user);
        when(repo.findByCreatedByAndCourseTypeAndSearch(search, user.getId(), CourseType.PREMIUM_COURSE, pageable))
                .thenReturn(mockPage);
        Message<Page<CourseDetailByType>> result = courseService.getPremiumCourses(search, name, pageable);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatus());
        Assertions.assertEquals("Instructor premium courses fetched successfully.", result.getMessage());
        Assertions.assertEquals(2, result.getData().getContent().size());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no premium courses are found")
    void shouldThrowEntityNotFoundExceptionWhenNoCoursesFound() throws EntityNotFoundException {
        String search = "java";
        String name = EMAIL;
        Pageable pageable = PageRequest.of(0, 10);
        User user = UserTestData.userData();
        Page<CourseDetailByType> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(userService.findByEmail(name)).thenReturn(user);
        when(repo.findByCreatedByAndCourseTypeAndSearch(search, user.getId(), CourseType.PREMIUM_COURSE, pageable))
                .thenReturn(emptyPage);
        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class, () ->
                courseService.getPremiumCourses(search, name, pageable));
        Assertions.assertEquals("No data found", exception.getMessage());
    }
    @Test
    @DisplayName("Should fetch completed courses successfully for valid user and data exists")
    void shouldFetchCompletedCoursesSuccessfully() throws EntityNotFoundException {
        String email = "test@example.com";
        int pageNo = 0, pageSize = 10;

        User instructor = new User();
        instructor.setId(1L);
        instructor.setFullName("John Doe");
        instructor.setEmail("john.doe@example.com");
        instructor.setStripeAccountId("acct_123456");
        instructor.setPassword("securePassword123"); // Ideally, this would be hashed.
        instructor.setRole(new Role(1L, "Instructor")); // Assuming Role has an ID and a name.
        instructor.setProvider(AuthProvider.GOOGLE);
        instructor.setCreationDate(new Date());
        instructor.setLoginTimestamp(new Date());
        instructor.setSubscribed(true);
        instructor.setSalesRaise(1.2);
        instructor.setActive(true);

// Creating a UserProfile for the Instructor
        UserProfile userProfile = new UserProfile();
        userProfile.setId(1L);
        userProfile.setProfilePicture("https://example.com/profile-picture.jpg");
        userProfile.setProfileUrl("john-doe-profile");
        userProfile.setHeadline("Experienced Software Engineer");
        userProfile.setAboutMe("I have 10 years of experience in software development.");
        userProfile.setQualification("Master's in Computer Science");
        userProfile.setExperience("10 years in development and teaching.");
        userProfile.setSpecialization("Java, Spring Boot, and Microservices");
        userProfile.setShowProfile(true);
        userProfile.setShowCourses(true);

// Creating a CourseCategory
        CourseCategory courseCategory = new CourseCategory();
        courseCategory.setId(1L);
        courseCategory.setName("Programming");

// Creating a CourseLevel
        CourseLevel courseLevel = new CourseLevel();
        courseLevel.setId(1L);
        courseLevel.setName("Intermediate");

// Creating a Course
        Course course = new Course();
        course.setId(1L);
        course.setTitle("Java Programming for Beginners");
        course.setDescription("An introductory course to Java programming.");
        course.setInstructor(instructor);
        course.setCourseCategory(courseCategory);
        course.setCourseDurationInHours(40);
        course.setCourseLevel(courseLevel);
        course.setPrerequisite("Basic understanding of programming.");
        course.setCourseOutcome("Build a strong foundation in Java.");
        course.setDocumentVector("java programming beginner basics");
        course.setAbout("Learn Java from scratch.");
        course.setThumbnail("https://example.com/course-thumbnail.jpg");
        course.setPreviewVideoURL("https://example.com/preview.mp4");
        course.setPreviewVideoVttContent("sample vtt content");
        course.setCertificateEnabled(true);
        course.setCourseProgress("Not Started");
        course.setMetaHeading("Learn Java Programming");
        course.setMetaTitle("Java Programming Course");
        course.setMetaDescription("Master Java programming with this beginner-friendly course.");
        course.setCourseStatus(CourseStatus.PUBLISHED);
        course.setCourseType(CourseType.PREMIUM_COURSE);
        course.setPrice(99.99);

// Creating a CourseUrl
        CourseUrl courseUrl = new CourseUrl();
        courseUrl.setId(1L);
        courseUrl.setUrl("https://example.com/course-url");
        courseUrl.setStatus(GenericStatus.ACTIVE);
        courseUrl.setCourse(course);

        Page<Tuple> page = new PageImpl<>(List.of(createTuple(1L, new Date())));

        // Mock behavior
        Mockito.when(userService.findByEmail(email)).thenReturn(instructor);
        Mockito.when(userProfileService.getUserProfile(instructor.getId())).thenReturn(userProfile);
        Mockito.when(repo.getAllCompletedCourseByUser(instructor.getId(), PageRequest.of(pageNo, pageSize))).thenReturn(page);
        Mockito.when(repo.findById(1L)).thenReturn(Optional.of(course));
        Mockito.when(courseUrlService.findActiveUrlByCourseIdAndStatus(1L, GenericStatus.ACTIVE)).thenReturn(courseUrl);

        // Execute
        Message<CompletedCourseByPaginated> response = courseService.getCompletedCourse(pageNo, pageSize, email);

        // Assertions

        assertThat(response.getMessage()).isEqualTo("Completed courses fetched successfully.");
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }
    private Tuple createTuple(Long courseId, Date lastModDate) {
        Tuple tuple = Mockito.mock(Tuple.class);
        Mockito.when(tuple.get("course_id")).thenReturn(courseId);
        Mockito.when(tuple.get("last_mod_date")).thenReturn(lastModDate);
        return tuple;
    }

    @Test
    @DisplayName("Should return empty completed courses list when no courses are completed")
    void shouldReturnEmptyListWhenNoCompletedCoursesFound() throws EntityNotFoundException {
        String email = "test@example.com";
        int pageNo = 0, pageSize = 10;

        User instructor = new User();
        instructor.setId(1L);
        instructor.setFullName("John Doe");
        instructor.setEmail("john.doe@example.com");
        instructor.setStripeAccountId("acct_123456");
        instructor.setPassword("securePassword123"); // Ideally, this would be hashed.
        instructor.setRole(new Role(1L, "Instructor")); // Assuming Role has an ID and a name.
        instructor.setProvider(AuthProvider.GOOGLE);
        instructor.setCreationDate(new Date());
        instructor.setLoginTimestamp(new Date());
        instructor.setSubscribed(true);
        instructor.setSalesRaise(1.2);
        instructor.setActive(true);


        // Mock behavior
        Mockito.when(userService.findByEmail(email)).thenReturn(instructor);
        Mockito.when(repo.getAllCompletedCourseByUser(instructor.getId(), PageRequest.of(pageNo, pageSize)))
                .thenReturn(Page.empty());

        // Execute
        Message<CompletedCourseByPaginated> response = courseService.getCompletedCourse(pageNo, pageSize, email);

        // Assertions
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getData()).isEmpty();
        assertThat(response.getMessage()).isEqualTo("No completed courses found.");
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user email is not found")
    void shouldThrowExceptionWhenUserEmailNotFound() throws EntityNotFoundException {
        String email = "notfound@example.com";

        // Mock behavior
        Mockito.when(userService.findByEmail(email)).thenReturn(null);

        // Execute & Assertions
        assertThatThrownBy(() -> courseService.getCompletedCourse(0, 10, email))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found.");
    }

    @Test
    @DisplayName("Should handle exceptions from CourseUrlService gracefully")
    void shouldHandleCourseUrlServiceException() throws EntityNotFoundException {
        String email = "test@example.com";
        int pageNo = 0, pageSize = 10;

        User instructor = new User();
        instructor.setId(1L);
        instructor.setFullName("John Doe");
        instructor.setEmail("john.doe@example.com");
        instructor.setStripeAccountId("acct_123456");
        instructor.setPassword("securePassword123"); // Ideally, this would be hashed.
        instructor.setRole(new Role(1L, "Instructor")); // Assuming Role has an ID and a name.
        instructor.setProvider(AuthProvider.GOOGLE);
        instructor.setCreationDate(new Date());
        instructor.setLoginTimestamp(new Date());
        instructor.setSubscribed(true);
        instructor.setSalesRaise(1.2);
        instructor.setActive(true);

        Course course = new Course();
        course.setId(1L);
        course.setTitle("Java Programming for Beginners");
        course.setDescription("An introductory course to Java programming.");
        course.setInstructor(instructor);
        course.setCourseCategory(courseCategory);
        course.setCourseDurationInHours(40);
        course.setCourseLevel(courseLevel);
        course.setPrerequisite("Basic understanding of programming.");
        course.setCourseOutcome("Build a strong foundation in Java.");
        course.setDocumentVector("java programming beginner basics");
        course.setAbout("Learn Java from scratch.");
        course.setThumbnail("https://example.com/course-thumbnail.jpg");
        course.setPreviewVideoURL("https://example.com/preview.mp4");
        course.setPreviewVideoVttContent("sample vtt content");
        course.setCertificateEnabled(true);
        course.setCourseProgress("Not Started");
        course.setMetaHeading("Learn Java Programming");
        course.setMetaTitle("Java Programming Course");
        course.setMetaDescription("Master Java programming with this beginner-friendly course.");
        course.setCourseStatus(CourseStatus.PUBLISHED);
        course.setCourseType(CourseType.PREMIUM_COURSE);
        course.setPrice(99.99);

        Page<Tuple> page = new PageImpl<>(List.of(createTuple(1L, new Date())));

        // Mock behavior
        Mockito.when(userService.findByEmail(email)).thenReturn(instructor);
        Mockito.when(repo.getAllCompletedCourseByUser(instructor.getId(), PageRequest.of(pageNo, pageSize))).thenReturn(page);
        Mockito.when(repo.findById(1L)).thenReturn(Optional.of(course));
        Mockito.when(courseUrlService.findActiveUrlByCourseIdAndStatus(1L, GenericStatus.ACTIVE))
                .thenThrow(new EntityNotFoundException("No active URL found."));

        // Execute
        Message<CompletedCourseByPaginated> response = courseService.getCompletedCourse(pageNo, pageSize, email);

        // Assertions
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getData()).isEmpty();
        assertThat(response.getMessage()).isEqualTo("No completed courses found.");
    }

    // premium course availability

    @Test
    void testGetPremiumCourseAvailable_InstructorNotFound() throws EntityNotFoundException {
        String email = "instructor1@mailinator.com";

        // Mock userRepository to return Optional.empty() when email is not found
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Call the service method
        Message<SubscriptionPermissionResponse> response = courseService.getPremiumCourseAvailable(email);

        // Assert the response
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("Instructor not found with email: " + email, response.getMessage());

        // Verify interactions
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testGetPremiumCourseAvailable_NoSubscriptionValidationFound() throws EntityNotFoundException {
        String email = "instructor1@mailinator.com";

        // Mock the User object to simulate an existing instructor
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        // Mock SubscribedUser and Subscription to simulate a valid subscription
        SubscribedUser subscribedUser = new SubscribedUser();
        Subscription subscription = new Subscription();
        subscription.setId(3L);
        subscription.setDurationInWord("Per Year");
        subscription.setDuration(12);
        subscription.setPrice(20);
        subscription.setActive(true);
        subscription.setName("Annual Plan");
        subscribedUser.setSubscription(subscription);

        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);

        // Mock subscriptionValidationsService to return null for the validation check
        when(subscriptionValidationsService.findByValidationNameAndSubscriptionAndIsActive(
                "PREMIUM_COURSE", subscription, true)).thenReturn(null);

        // Call the service method
        Message<SubscriptionPermissionResponse> response = courseService.getPremiumCourseAvailable(email);

        // Assert the response
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("Subscription validation not found for PREMIUM_COURSE.", response.getMessage());

        // Verify interactions
        verify(userRepository, times(1)).findByEmail(email);
        verify(subscribedUserService, times(1)).findByUser(email);
        verify(subscriptionValidationsService, times(1))
                .findByValidationNameAndSubscriptionAndIsActive("PREMIUM_COURSE", subscription, true);
    }

    @Test
    void testGetPremiumCourseAvailable_ValidSubscription() throws EntityNotFoundException {
        String email = "test@example.com";

        // Mock User
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        // Mock SubscribedUser and Subscription
        SubscribedUser subscribedUser = new SubscribedUser();
        Subscription subscription = new Subscription();
        subscription.setId(2L);
        subscription.setDurationInWord("Per Month");
        subscription.setDuration(1);
        subscription.setPrice(15);
        subscription.setActive(true);
        subscription.setName("Standard Plan");
        subscribedUser.setSubscription(subscription);
        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);

        // Mock SubscriptionValidations
        SubscriptionValidations subscriptionValidations = new SubscriptionValidations();
        subscriptionValidations.setValue(10L);
        when(subscriptionValidationsService.findByValidationNameAndSubscriptionAndIsActive(
                "PREMIUM_COURSE", subscription, true)).thenReturn(subscriptionValidations);

        // Mock repository for published courses
        when(repo.findByInstructorAndPublished(mockUser.getId(), "PUBLISHED")).thenReturn(5L);

        // Call the service method
        Message<SubscriptionPermissionResponse> response = courseService.getPremiumCourseAvailable(email);

        // Assertions
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Premium course availability fetch successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertTrue(response.getData().getIsAvailablePremium());
        assertEquals(5L, response.getData().getRemainingPremiumCourse());
        assertEquals(5L, response.getData().getTotalPremiumCourse());

        // Verify interactions
        verify(userRepository, times(1)).findByEmail(email); // Adjusted verification
        verify(subscribedUserService, times(1)).findByUser(email);
        verify(subscriptionValidationsService, times(1))
                .findByValidationNameAndSubscriptionAndIsActive("PREMIUM_COURSE", subscription, true);
        verify(repo, times(1)).findByInstructorAndPublished(mockUser.getId(), "PUBLISHED");
    }


    @Test
    void testGetPremiumCourseAvailable_FreeSubscription() throws EntityNotFoundException {
        String email = "test@example.com";

        // Mocking the user
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        // Mocking subscribed user and subscription
        SubscribedUser subscribedUser = new SubscribedUser();
        Subscription subscription = new Subscription();
        subscription.setId(1L);
        subscription.setDurationInWord("Per Month");
        subscription.setDuration(1);
        subscription.setPrice(15);
        subscription.setActive(true);
        subscription.setName("Free Plan");
        subscribedUser.setSubscription(subscription);

        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);

        // Invoke the service method
        Message<SubscriptionPermissionResponse> response = courseService.getPremiumCourseAvailable(email);

        // Assertions
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Premium course availability fetch successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertFalse(response.getData().getIsAvailablePremium());
        assertEquals(0L, response.getData().getRemainingPremiumCourse());
        assertEquals(0L, response.getData().getTotalPremiumCourse());

        // Verify interactions
        verify(subscribedUserService, times(1)).findByUser(email);
        verifyNoInteractions(subscriptionValidationsService);
        verifyNoInteractions(repo);
    }

    //premium course availability test cases
    @Test
    @DisplayName("Should validate premium course creation when all conditions are met")
    void testPremiumCourseValidationBySubscription_ValidCase() throws EntityNotFoundException {
        // Mock data
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        SubscribedUser mockSubscribedUser = new SubscribedUser();
        Subscription mockSubscription = new Subscription();
        mockSubscription.setId(2L);
        mockSubscribedUser.setSubscription(mockSubscription);

        SubscriptionValidations mockSubscriptionValidations = new SubscriptionValidations();
        mockSubscriptionValidations.setValue(5L);

        when(subscribedUserService.findByUser("test@example.com")).thenReturn(mockSubscribedUser);
        when(subscriptionValidationsService.findByValidationNameAndSubscriptionAndIsActive(
                eq("PREMIUM_COURSE"), eq(mockSubscription), eq(true)))
                .thenReturn(mockSubscriptionValidations);
        when(repo.findByInstructorAndPublished(1L, "PUBLISHED")).thenReturn(3L);

        // Execute
        boolean result = courseService.premiumCourseValidationBySubscription(mockUser);

        // Verify
        assertTrue(result, "Premium course validation should pass when remaining courses are available");

        // Verify interactions
        verify(subscribedUserService).findByUser("test@example.com");
        verify(subscriptionValidationsService)
                .findByValidationNameAndSubscriptionAndIsActive("PREMIUM_COURSE", mockSubscription, true);
        verify(repo).findByInstructorAndPublished(1L, "PUBLISHED");
    }

    @Test
    @DisplayName("Should fail validation when no subscription validations exist")
    void testPremiumCourseValidationBySubscription_NoSubscriptionValidations() throws EntityNotFoundException {
        // Mock data
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        SubscribedUser mockSubscribedUser = new SubscribedUser();
        Subscription mockSubscription = new Subscription();
        mockSubscription.setId(2L);
        mockSubscribedUser.setSubscription(mockSubscription);

        when(subscribedUserService.findByUser("test@example.com")).thenReturn(mockSubscribedUser);
        when(subscriptionValidationsService.findByValidationNameAndSubscriptionAndIsActive(
                eq("PREMIUM_COURSE"), eq(mockSubscription), eq(true)))
                .thenReturn(null);

        // Execute
        boolean result = courseService.premiumCourseValidationBySubscription(mockUser);

        // Verify
        assertFalse(result, "Premium course validation should fail when no subscription validations exist");

        // Verify interactions
        verify(subscribedUserService).findByUser("test@example.com");
        verify(subscriptionValidationsService)
                .findByValidationNameAndSubscriptionAndIsActive("PREMIUM_COURSE", mockSubscription, true);
        verifyNoInteractions(repo);
    }

    @Test
    @DisplayName("Should fail validation for free subscriptions")
    void testPremiumCourseValidationBySubscription_FreeSubscription() throws EntityNotFoundException {
        // Mock data
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        SubscribedUser mockSubscribedUser = new SubscribedUser();
        Subscription mockSubscription = new Subscription();
        mockSubscription.setId(1L); // Free subscription ID
        mockSubscribedUser.setSubscription(mockSubscription);

        when(subscribedUserService.findByUser("test@example.com")).thenReturn(mockSubscribedUser);

        // Execute
        boolean result = courseService.premiumCourseValidationBySubscription(mockUser);

        // Verify
        assertFalse(result, "Premium course validation should fail for free subscriptions");

        // Verify interactions
        verify(subscribedUserService).findByUser("test@example.com");
        verifyNoInteractions(subscriptionValidationsService, repo);
    }

    @Test
    @DisplayName("Should fail validation when no subscribed user is found")
    void testPremiumCourseValidationBySubscription_NoSubscribedUser() throws EntityNotFoundException {
        // Mock data
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        when(subscribedUserService.findByUser("test@example.com")).thenReturn(null);

        // Execute
        boolean result = courseService.premiumCourseValidationBySubscription(mockUser);

        // Verify
        assertFalse(result, "Premium course validation should fail when no subscribed user is found");

        // Verify interactions
        verify(subscribedUserService).findByUser("test@example.com");
        verifyNoInteractions(subscriptionValidationsService, repo);
    }

    // validation on quiz question type
    @Test
    void testValidateAnswers_MultipleChoice_Valid() {
        List<CreateQuizQuestionAnswerRequest> answers = Arrays.asList(
                new CreateQuizQuestionAnswerRequest(1L, false, "Option 1", true),
                new CreateQuizQuestionAnswerRequest(2L, false, "Option 2", false)
        );
        assertDoesNotThrow(() -> courseService.validateAnswers(QuestionType.MULTIPLE_CHOICE, answers));
    }

    @Test
    void testValidateAnswers_MultipleChoice_NoCorrectAnswer() {
        List<CreateQuizQuestionAnswerRequest> answers = Arrays.asList(
                new CreateQuizQuestionAnswerRequest(1L, false, "Option 1", false),
                new CreateQuizQuestionAnswerRequest(2L, false, "Option 2", false)
        );
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> courseService.validateAnswers(QuestionType.MULTIPLE_CHOICE, answers));
        assertEquals("Multiple choice questions must have at least one correct answer.", exception.getMessage());
    }

    @Test
    void testValidateAnswers_SingleChoice_Valid() {
        List<CreateQuizQuestionAnswerRequest> answers = Arrays.asList(
                new CreateQuizQuestionAnswerRequest(1L, false, "Option 1", false),
                new CreateQuizQuestionAnswerRequest(2L, false, "Option 2", true)
        );
        assertDoesNotThrow(() -> courseService.validateAnswers(QuestionType.SINGLE_CHOICE, answers));
    }

    @Test
    void testValidateAnswers_SingleChoice_MultipleCorrectAnswers() {
        List<CreateQuizQuestionAnswerRequest> answers = Arrays.asList(
                new CreateQuizQuestionAnswerRequest(1L, false, "Option 1", true),
                new CreateQuizQuestionAnswerRequest(2L, false, "Option 2", true)
        );
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> courseService.validateAnswers(QuestionType.SINGLE_CHOICE, answers));
        assertEquals("Single choice questions must have exactly one correct answer.", exception.getMessage());
    }

    @Test
    void testValidateAnswers_TrueFalse_Valid() {
        List<CreateQuizQuestionAnswerRequest> answers = Arrays.asList(
                new CreateQuizQuestionAnswerRequest(1L, false, "True", true),
                new CreateQuizQuestionAnswerRequest(2L, false, "False", false)
        );
        assertDoesNotThrow(() -> courseService.validateAnswers(QuestionType.TRUE_FALSE, answers));
    }

    @Test
    void testValidateAnswers_TrueFalse_InvalidAnswers() {
        List<CreateQuizQuestionAnswerRequest> answers = Arrays.asList(
                new CreateQuizQuestionAnswerRequest(1L, false, "Yes", true),
                new CreateQuizQuestionAnswerRequest(2L, false, "No", false)
        );
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> courseService.validateAnswers(QuestionType.TRUE_FALSE, answers));
        assertEquals("True/False answers must be 'True' and 'False'.", exception.getMessage());
    }

    @Test
    void testValidateAnswers_TextField_Valid() {
        List<CreateQuizQuestionAnswerRequest> answers = List.of(
                new CreateQuizQuestionAnswerRequest(1L, false, "Answer", false)
        );
        assertDoesNotThrow(() -> courseService.validateAnswers(QuestionType.TEXT_FIELD, answers));
    }

    @Test
    void testValidateAnswers_TextField_Invalid() {
        List<CreateQuizQuestionAnswerRequest> answers = Arrays.asList(
                new CreateQuizQuestionAnswerRequest(1L, false, "Answer 1", false),
                new CreateQuizQuestionAnswerRequest(2L, false, "Answer 2", false)
        );
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> courseService.validateAnswers(QuestionType.TEXT_FIELD, answers));
        assertEquals("Text field questions must have only one answer.", exception.getMessage());
    }
}


