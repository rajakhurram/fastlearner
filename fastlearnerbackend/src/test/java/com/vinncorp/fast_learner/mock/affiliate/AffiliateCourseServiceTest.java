package com.vinncorp.fast_learner.mock.affiliate;

import com.vinncorp.fast_learner.dtos.affiliate.AffiliateCourseDto;
import com.vinncorp.fast_learner.dtos.affiliate.AffiliateCourseRequest;
import com.vinncorp.fast_learner.exception.*;
import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.mock.subscription.SubscriptionTestData;
import com.vinncorp.fast_learner.mock.subscription.subscribed_user.SubscribedUserTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.affiliate.AffiliatedCourses;
import com.vinncorp.fast_learner.models.affiliate.InstructorAffiliate;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.affiliate.AffiliatedCoursesRepository;
import com.vinncorp.fast_learner.services.affiliate.affiliate_course_service.AffiliateCourseService;
import com.vinncorp.fast_learner.services.affiliate.instructor_affiliate_service.IInstructorAffiliateService;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import com.vinncorp.fast_learner.util.enums.CourseType;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AffiliateCourseServiceTest {

    @Mock
    ICourseUrlService courseUrlService;
    @Mock
    private ISubscribedUserService subscribedUserService;
    @Mock
    IUserService userService;
    @Mock
    ICourseService courseService;
    @Mock
    IInstructorAffiliateService instructorAffiliateService;
    @Mock
    AffiliatedCoursesRepository repo;
    @InjectMocks
    AffiliateCourseService affiliateCourseService;
    private static final Long AFFILIATE_ID = 1L;
    private static final String USER_EMAIL = "testuser@example.com";
    private static final GenericStatus STATUS = GenericStatus.ACTIVE;
    private static final Pageable PAGEABLE = PageRequest.of(0, 10);
    private static final Long AFFILIATE_COURSE_ID = 1L;
    private static final Long COURSE_ID = 1L;
    private static final CourseStatus COURSE_STATUS = CourseStatus.PUBLISHED;
    private static final Long USER_ID = 1L;

    private User mockUser;
    private InstructorAffiliate mockInstructorAffiliate;
    private AffiliatedCourses mockAffiliatedCourse;
    private Page<AffiliateCourseDto> mockPage;

    @BeforeEach
    public void init() throws IOException {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(USER_EMAIL);

        mockInstructorAffiliate = new InstructorAffiliate();
        mockInstructorAffiliate.setId(1L);

        mockAffiliatedCourse = new AffiliatedCourses();
        mockAffiliatedCourse.setId(AFFILIATE_COURSE_ID);
        mockAffiliatedCourse.setInstructorAffiliate(mockInstructorAffiliate);
        mockAffiliatedCourse.setStatus(GenericStatus.ACTIVE);

        AffiliateCourseDto mockAffiliateCourseDto = new AffiliateCourseDto();
        List<AffiliateCourseDto> courseList = Collections.singletonList(mockAffiliateCourseDto);
        mockPage = new PageImpl<>(courseList, PageRequest.of(0, 10), 1);
        when(repo.getAllAffiliatesByCourseAndStatus(anyLong(), anyLong(), anyString(), any(), any(), any(Pageable.class)))
                .thenReturn(mockPage);
    }

    @Test
    @DisplayName("Should assign course to affiliate when valid requests are provided")
    void shouldAssignCourseToAffiliateForValidRequests() throws EntityNotFoundException, BadRequestException, InternalServerException, LimitExceedException {
        User user = UserTestData.userData();
        String name = user.getEmail();
        Course course = new Course();
        course.setId(1L);
        course.setCourseType(CourseType.PREMIUM_COURSE);
        course.setCreatedBy(1L);
        InstructorAffiliate instructorAffiliate = new InstructorAffiliate();
        instructorAffiliate.setId(1L);
        instructorAffiliate.setAffiliateUuid("uuid-123");
        CourseUrl courseUrl = new CourseUrl();
        courseUrl.setUrl("http://example.com/course");
        AffiliateCourseRequest request = new AffiliateCourseRequest();
        request.setCourseId(1L);
        request.setAffiliateId(1L);
        request.setReward(10.0);
        List<AffiliateCourseRequest> requests = List.of(request);
        when(userService.findByEmail(name)).thenReturn(user);
        when(instructorAffiliateService.getByInstructorIdAndAffiliateId(user.getId(), request.getAffiliateId()))
                .thenReturn(instructorAffiliate);
        when(courseService.findById(request.getCourseId())).thenReturn(course);
        var subscribedUser = SubscribedUserTestData.enterpriseSubscribedUser();
        subscribedUser.setUser(user);
        when(subscribedUserService.findByUser(user.getEmail())).thenReturn(subscribedUser);
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE))
                .thenReturn(courseUrl);
        when(repo.saveAll(Mockito.anyList())).thenReturn(null);
        Message<String> result = affiliateCourseService.assignCourseToAffiliate(requests, name);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatus());
        Assertions.assertEquals("Course assigned successfully", result.getMessage());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no affiliate is found")
    void shouldThrowEntityNotFoundExceptionWhenAffiliateNotFound() throws EntityNotFoundException, BadRequestException, IOException {
        User user = UserTestData.userData();
        AffiliateCourseRequest request = new AffiliateCourseRequest();
        request.setCourseId(1L);
        request.setAffiliateId(1L);
        request.setReward(10.0);
        Course course = CourseTestData.courseData();
        course.setId(1L);
        course.setCourseType(CourseType.PREMIUM_COURSE);
        List<AffiliateCourseRequest> requests = List.of(request);
        when(userService.findByEmail(user.getEmail())).thenReturn(user);
        when(instructorAffiliateService.getByInstructorIdAndAffiliateId(user.getId(), request.getAffiliateId()))
                .thenReturn(null);
        var subscribedUser = SubscribedUserTestData.enterpriseSubscribedUser();
        subscribedUser.setUser(user);
        when(subscribedUserService.findByUser(user.getEmail())).thenReturn(subscribedUser);
        when(courseService.findById(request.getCourseId())).thenReturn(course);
        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class, () ->
                affiliateCourseService.assignCourseToAffiliate(requests, user.getEmail()));
        Assertions.assertEquals("Affiliate not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when course is not premium")
    void assignCourseToAffiliate_nonPremiumCourse() throws EntityNotFoundException, IOException, BadRequestException {
        User user = UserTestData.userData();

        Course course = CourseTestData.courseData();
        course.setCourseType(CourseType.FREE_COURSE);

        AffiliateCourseRequest req = new AffiliateCourseRequest();
        req.setCourseId(1L);
        req.setAffiliateId(2L);

        when(userService.findByEmail(any())).thenReturn(user);
        when(repo.getCountOfAssignedAffiliatesByInstructorAndCourse(any(), any(), any(), any())).thenReturn(0L);

        Subscription subscription = SubscriptionTestData.enterpriseSubscription();
        SubscribedUser subscribedUser = SubscribedUserTestData.enterpriseSubscribedUser();

        when(subscribedUserService.findByUser(any())).thenReturn(subscribedUser);

        when(instructorAffiliateService.getByInstructorIdAndAffiliateId(any(), any())).thenReturn(new InstructorAffiliate());
        when(courseService.findById(any())).thenReturn(course);

        // Act & Assert
        assertThrows(BadRequestException.class, () ->
                affiliateCourseService.assignCourseToAffiliate(List.of(req), "test@example.com"));
    }

    @Test
    @DisplayName("Should throw BadRequestException when course is already assigned")
    void assignCourseToAffiliate_courseAlreadyAssigned() throws IOException, EntityNotFoundException, BadRequestException {
        User user = UserTestData.userData();

        Course course = CourseTestData.courseData();
        course.setCourseType(CourseType.PREMIUM_COURSE);
        course.setCreatedBy(1L);

        InstructorAffiliate ia = new InstructorAffiliate();
        ia.setId(999L);

        AffiliateCourseRequest req = new AffiliateCourseRequest();
        req.setCourseId(1L);
        req.setAffiliateId(2L);

        when(userService.findByEmail(any())).thenReturn(user);
        when(repo.getCountOfAssignedAffiliatesByInstructorAndCourse(any(), any(), any(), any())).thenReturn(0L);

        Subscription subscription = SubscriptionTestData.enterpriseSubscription();
        SubscribedUser subscribedUser = SubscribedUserTestData.standardSubscribedUser();
        subscribedUser.setSubscription(subscription);
        when(subscribedUserService.findByUser(any())).thenReturn(subscribedUser);

        when(instructorAffiliateService.getByInstructorIdAndAffiliateId(any(), any())).thenReturn(ia);
        when(courseService.findById(any())).thenReturn(course);
        when(repo.getByInstructorAffiliateIdAndCourseIdAndStatus(1L, 1L, GenericStatus.ACTIVE)).thenReturn(Optional.of(new AffiliateCourseDto()));

        assertThrows(BadRequestException.class, () ->
                affiliateCourseService.assignCourseToAffiliate(List.of(req), "test@example.com"));
    }

    @Test
    @DisplayName("Should return AffiliateCourseDto when data exists for given parameters")
    void shouldReturnAffiliateCourseDtoWhenDataExists() throws BadRequestException {
        Long instructorAffiliateId = 1L;
        Long courseId = 101L;
        GenericStatus status = GenericStatus.ACTIVE;
        AffiliateCourseDto expectedDto = new AffiliateCourseDto();
        when(repo.getByInstructorAffiliateIdAndCourseIdAndStatus(instructorAffiliateId, courseId, status))
                .thenReturn(Optional.of(expectedDto));
        AffiliateCourseDto actualDto = affiliateCourseService.getByInstructorAffiliateIdAndCourseIdAndStatus(
                instructorAffiliateId, courseId, status);
        assertNotNull(actualDto);
        assertEquals(expectedDto, actualDto);
        verify(repo, times(1)).getByInstructorAffiliateIdAndCourseIdAndStatus(instructorAffiliateId, courseId, status);
    }

    @Test
    @DisplayName("Should return null when no data exists for given parameters")
    void shouldReturnNullWhenNoDataExists() throws BadRequestException {
        Long instructorAffiliateId = 1L;
        Long courseId = 101L;
        GenericStatus status = GenericStatus.ACTIVE;
        when(repo.getByInstructorAffiliateIdAndCourseIdAndStatus(instructorAffiliateId, courseId, status))
                .thenReturn(Optional.empty());
        AffiliateCourseDto actualDto = affiliateCourseService.getByInstructorAffiliateIdAndCourseIdAndStatus(
                instructorAffiliateId, courseId, status);
        assertNull(actualDto);
        verify(repo, times(1)).getByInstructorAffiliateIdAndCourseIdAndStatus(instructorAffiliateId, courseId, status);
    }

    @Test
    @DisplayName("Should throw BadRequestException when instructorAffiliateId is null")
    void shouldThrowExceptionWhenInstructorAffiliateIdIsNull() {
        Long instructorAffiliateId = null;
        Long courseId = 101L;
        GenericStatus status = GenericStatus.ACTIVE;
        assertThrows(BadRequestException.class, () -> {
            affiliateCourseService.getByInstructorAffiliateIdAndCourseIdAndStatus(instructorAffiliateId, courseId, status);
        });
    }

    @Test
    @DisplayName("Should throw BadRequestException when courseId is null")
    void shouldThrowExceptionWhenCourseIdIsNull() {
        Long instructorAffiliateId = 1L;
        Long courseId = null;
        GenericStatus status = GenericStatus.ACTIVE;
        assertThrows(BadRequestException.class, () -> {
            affiliateCourseService.getByInstructorAffiliateIdAndCourseIdAndStatus(instructorAffiliateId, courseId, status);
        });
    }

    @Test
    @DisplayName("Should throw BadRequestException when status is null")
    void shouldThrowExceptionWhenStatusIsNull() {
        Long instructorAffiliateId = 1L;
        Long courseId = 101L;
        GenericStatus status = null;
        assertThrows(BadRequestException.class, () -> {
            affiliateCourseService.getByInstructorAffiliateIdAndCourseIdAndStatus(instructorAffiliateId, courseId, status);
        });
    }

    @Test
    @DisplayName("Test successful retrieval of courses by affiliate")
    void testSuccessfulCourseRetrieval() throws EntityNotFoundException, BadRequestException {
        Page<AffiliateCourseDto> mockPage = mock(Page.class);
        when(userService.findByEmail(USER_EMAIL)).thenReturn(mockUser);
        when(instructorAffiliateService.getByInstructorIdAndAffiliateId(mockUser.getId(), AFFILIATE_ID))
                .thenReturn(mockInstructorAffiliate);
        when(repo.getByInstructorAffiliateIdAndStatus(mockInstructorAffiliate.getId(),
//                STATUS,
                PAGEABLE))
                .thenReturn(mockPage);
        when(mockPage.getContent()).thenReturn(Collections.singletonList(new AffiliateCourseDto()));
        Message<Page<AffiliateCourseDto>> response = affiliateCourseService.getAllCoursesByAffiliate(AFFILIATE_ID, STATUS, PAGEABLE, USER_EMAIL);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Courses fetch successfully", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    @DisplayName("Test retrieval of courses with valid user and affiliate")
    void testCourseRetrievalWithValidUserAndAffiliate() throws EntityNotFoundException, BadRequestException {
        Page<AffiliateCourseDto> mockPage = mock(Page.class);
        when(userService.findByEmail(USER_EMAIL)).thenReturn(mockUser);
        when(instructorAffiliateService.getByInstructorIdAndAffiliateId(mockUser.getId(), AFFILIATE_ID))
                .thenReturn(mockInstructorAffiliate);
        when(repo.getByInstructorAffiliateIdAndStatus(mockInstructorAffiliate.getId(),
//                STATUS,
                PAGEABLE))
                .thenReturn(mockPage);
        when(mockPage.getContent()).thenReturn(Collections.singletonList(new AffiliateCourseDto()));
        Message<Page<AffiliateCourseDto>> response = affiliateCourseService.getAllCoursesByAffiliate(AFFILIATE_ID, STATUS, PAGEABLE, USER_EMAIL);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Courses fetch successfully", response.getMessage());
    }

    @Test
    @DisplayName("Test exception when affiliate is not found")
    void testAffiliateNotFoundException() throws EntityNotFoundException, BadRequestException {
        when(userService.findByEmail(USER_EMAIL)).thenReturn(mockUser);
        when(instructorAffiliateService.getByInstructorIdAndAffiliateId(mockUser.getId(), AFFILIATE_ID))
                .thenReturn(null);
        assertThrows(EntityNotFoundException.class, () ->
                affiliateCourseService.getAllCoursesByAffiliate(AFFILIATE_ID, STATUS, PAGEABLE, USER_EMAIL));
    }

    @Test
    @DisplayName("Test exception when user is not found by email")
    void testUserNotFoundException() throws EntityNotFoundException {
        when(userService.findByEmail(USER_EMAIL)).thenThrow(new EntityNotFoundException());
        assertThrows(EntityNotFoundException.class, () ->
                affiliateCourseService.getAllCoursesByAffiliate(AFFILIATE_ID, STATUS, PAGEABLE, USER_EMAIL));
    }

    @Test
    @DisplayName("Test exception when no courses are found")
    void testNoCoursesFoundException() throws BadRequestException, EntityNotFoundException {
        Page<AffiliateCourseDto> mockPage = mock(Page.class);
        when(userService.findByEmail(USER_EMAIL)).thenReturn(mockUser);
        when(instructorAffiliateService.getByInstructorIdAndAffiliateId(mockUser.getId(), AFFILIATE_ID))
                .thenReturn(mockInstructorAffiliate);
        when(repo.getByInstructorAffiliateIdAndStatus(mockInstructorAffiliate.getId(),
//                STATUS,
                PAGEABLE))
                .thenReturn(mockPage);
        when(mockPage.getContent()).thenReturn(Collections.emptyList()); // No courses found

        assertThrows(EntityNotFoundException.class, () ->
                affiliateCourseService.getAllCoursesByAffiliate(AFFILIATE_ID, STATUS, PAGEABLE, USER_EMAIL));
    }

    @Test
    @DisplayName("Test EntityNotFoundException when affiliateId is invalid")
    void testBadRequestExceptionInvalidAffiliateId() throws EntityNotFoundException, BadRequestException {
        when(userService.findByEmail(USER_EMAIL)).thenReturn(mockUser);
        when(instructorAffiliateService.getByInstructorIdAndAffiliateId(mockUser.getId(), AFFILIATE_ID))
                .thenReturn(null);
        assertThrows(EntityNotFoundException.class, () ->
                affiliateCourseService.getAllCoursesByAffiliate(AFFILIATE_ID, STATUS, PAGEABLE, USER_EMAIL));
    }

    @Test
    @DisplayName("Test exception when user is not an instructor for the given affiliate")
    void testUserNotInstructorForAffiliate() throws EntityNotFoundException, BadRequestException {
        when(userService.findByEmail(USER_EMAIL)).thenReturn(mockUser);
        when(instructorAffiliateService.getByInstructorIdAndAffiliateId(mockUser.getId(), AFFILIATE_ID))
                .thenReturn(null);
        assertThrows(EntityNotFoundException.class, () ->
                affiliateCourseService.getAllCoursesByAffiliate(AFFILIATE_ID, STATUS, PAGEABLE, USER_EMAIL));
    }

    @Test
    @DisplayName("Test successful deletion of affiliate course")
    void testSuccessfulDeleteAffiliateCourse() throws AuthenticationException, BadRequestException, EntityNotFoundException {
        when(userService.findByEmail(USER_EMAIL)).thenReturn(mockUser);
        when(instructorAffiliateService.getByInstructorIdAndAffiliateId(mockUser.getId(), AFFILIATE_ID))
                .thenReturn(mockInstructorAffiliate);
        when(repo.findById(AFFILIATE_COURSE_ID)).thenReturn(Optional.of(mockAffiliatedCourse));
        Message<String> response = affiliateCourseService.deleteAffiliateCourse(AFFILIATE_ID, AFFILIATE_COURSE_ID, USER_EMAIL);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Course deleted successfully", response.getMessage());
        assertEquals(GenericStatus.INACTIVE, mockAffiliatedCourse.getStatus());
        verify(repo).save(mockAffiliatedCourse);
    }

    @Test
    @DisplayName("Delete affiliate Test exception when affiliate is not found")
    void testDeleteAffiliateCourseWhenAffiliateNotFoundException() throws EntityNotFoundException, BadRequestException {
        when(userService.findByEmail(USER_EMAIL)).thenReturn(mockUser);
        when(instructorAffiliateService.getByInstructorIdAndAffiliateId(mockUser.getId(), AFFILIATE_ID))
                .thenReturn(null);
        assertThrows(EntityNotFoundException.class, () ->
                affiliateCourseService.deleteAffiliateCourse(AFFILIATE_ID, AFFILIATE_COURSE_ID, USER_EMAIL));
    }

    @Test
    @DisplayName("Test exception when affiliate course is not found")
    void testAffiliateCourseNotFoundException() throws EntityNotFoundException, BadRequestException {
        when(userService.findByEmail(USER_EMAIL)).thenReturn(mockUser);
        when(instructorAffiliateService.getByInstructorIdAndAffiliateId(mockUser.getId(), AFFILIATE_ID))
                .thenReturn(mockInstructorAffiliate);
        when(repo.findById(AFFILIATE_COURSE_ID)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                affiliateCourseService.deleteAffiliateCourse(AFFILIATE_ID, AFFILIATE_COURSE_ID, USER_EMAIL));
    }

    @Test
    @DisplayName("Test exception when user is unauthorized to delete affiliate course")
    void testAuthenticationExceptionUnauthorized() throws EntityNotFoundException, BadRequestException {
        InstructorAffiliate differentInstructorAffiliate = new InstructorAffiliate();
        differentInstructorAffiliate.setId(2L);
        mockAffiliatedCourse.setInstructorAffiliate(differentInstructorAffiliate);
        when(userService.findByEmail(USER_EMAIL)).thenReturn(mockUser);
        when(instructorAffiliateService.getByInstructorIdAndAffiliateId(mockUser.getId(), AFFILIATE_ID))
                .thenReturn(mockInstructorAffiliate);
        when(repo.findById(AFFILIATE_COURSE_ID)).thenReturn(Optional.of(mockAffiliatedCourse));
        assertThrows(AuthenticationException.class, () ->
                affiliateCourseService.deleteAffiliateCourse(AFFILIATE_ID, AFFILIATE_COURSE_ID, USER_EMAIL));
    }

    @Test
    @DisplayName("Test exception when user is not found by email")
    void testUserNotFoundByEmail() throws EntityNotFoundException {
        when(userService.findByEmail(USER_EMAIL)).thenThrow(new EntityNotFoundException());
        assertThrows(EntityNotFoundException.class, () ->
                affiliateCourseService.deleteAffiliateCourse(AFFILIATE_ID, AFFILIATE_COURSE_ID, USER_EMAIL));
    }

    @Test
    @DisplayName("Test successful retrieval of affiliated course by ID")
    void testGetByIdSuccess() {
        when(repo.findById(AFFILIATE_COURSE_ID)).thenReturn(Optional.of(mockAffiliatedCourse));
        AffiliatedCourses result = affiliateCourseService.getById(AFFILIATE_COURSE_ID);
        assertNotNull(result);
        verify(repo).findById(AFFILIATE_COURSE_ID);
    }

    @Test
    @DisplayName("Test that null is returned when affiliated course does not exist")
    void testGetByIdCourseNotFound() {
        when(repo.findById(AFFILIATE_COURSE_ID)).thenReturn(Optional.empty());
        AffiliatedCourses result = affiliateCourseService.getById(AFFILIATE_COURSE_ID);
        assertNull(result);
        verify(repo).findById(AFFILIATE_COURSE_ID);
    }

    @Test
    @DisplayName("Test that null is returned when an invalid course ID is passed")
    void testGetByIdInvalidCourseId() {
        when(repo.findById(-999L)).thenReturn(Optional.empty());
        AffiliatedCourses result = affiliateCourseService.getById(-999L);
        assertNull(result);
        verify(repo).findById(-999L);
    }

    @Test
    @DisplayName("Test that the method returns a course when the ID exists")
    void testGetByIdExistingCourse() {
        when(repo.findById(AFFILIATE_COURSE_ID)).thenReturn(Optional.of(mockAffiliatedCourse));
        AffiliatedCourses result = affiliateCourseService.getById(AFFILIATE_COURSE_ID);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test that the method returns null for a non-existing course ID")
    void testGetByIdNonExistingCourse() {
        when(repo.findById(AFFILIATE_COURSE_ID)).thenReturn(Optional.empty());
        AffiliatedCourses result = affiliateCourseService.getById(AFFILIATE_COURSE_ID);
        assertNull(result);
    }

    @Test
    @DisplayName("Should return page of AffiliateCourseDto when valid data exists")
    void testGetAllAffiliatesByCourse_success() throws EntityNotFoundException {
        // Arrange
        String email = "instructor@example.com";
        Long courseId = 1L;

        User user = new User();
        user.setId(10L);
        user.setEmail(email);

        Pageable pageable = PageRequest.of(0, 10);

        AffiliateCourseDto dto = new AffiliateCourseDto();
        dto.setAffiliateId(100L); // ✅ At least one non-null field
        dto.setAffiliateName("John Doe");

        Page<AffiliateCourseDto> page = new PageImpl<>(List.of(dto), pageable, 1);

        when(userService.findByEmail(email)).thenReturn(user);
        when(repo.getAllAffiliatesByCourseAndStatus(
                user.getId(), courseId, user.getEmail(),
                GenericStatus.ACTIVE, GenericStatus.ACTIVE, pageable))
                .thenReturn(page);

        // Act
        Message<Page<AffiliateCourseDto>> result = affiliateCourseService.getAllAffiliatesByCourse(
                courseId, email, GenericStatus.ACTIVE, GenericStatus.ACTIVE, pageable
        );

        // Assert
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals("Course affiliates fetched successfully", result.getMessage());
        assertFalse(result.getData().getContent().isEmpty());
        assertEquals(dto.getAffiliateId(), result.getData().getContent().get(0).getAffiliateId());
    }

    @Test
    @DisplayName("Test that EntityNotFoundException is thrown when no affiliates are found for the course")
    void testGetAllAffiliatesByCourseNoAffiliates() throws EntityNotFoundException {
        Page<AffiliateCourseDto> emptyPage = mock(Page.class);
        when(emptyPage.getContent()).thenReturn(Collections.emptyList());
        when(userService.findByEmail(USER_EMAIL)).thenReturn(UserTestData.userData());
        when(repo.getAllAffiliatesByCourseAndStatus(USER_ID, COURSE_ID, USER_EMAIL, STATUS, STATUS, PageRequest.of(0, 10)))
                .thenReturn(emptyPage);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            affiliateCourseService.getAllAffiliatesByCourse(COURSE_ID, USER_EMAIL, STATUS, STATUS, PageRequest.of(0, 10));
        });
        assertEquals("Data not found", exception.getMessage());
    }

    @Test
    @DisplayName("Test that EntityNotFoundException is thrown when the user is not found")
    void testGetAllAffiliatesByCourseUserNotFound() throws EntityNotFoundException {
        when(userService.findByEmail(USER_EMAIL)).thenThrow(new EntityNotFoundException("User not found"));
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            affiliateCourseService.getAllAffiliatesByCourse(COURSE_ID, USER_EMAIL, STATUS, STATUS, PageRequest.of(0, 10));
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("Test that EntityNotFoundException is thrown when the repository returns no results")
    void testGetAllAffiliatesByCourseNoResults() throws EntityNotFoundException {
        when(userService.findByEmail(USER_EMAIL)).thenReturn(UserTestData.userData());
        Page<AffiliateCourseDto> emptyPage = mock(Page.class);
        when(emptyPage.getContent()).thenReturn(Collections.emptyList());
        when(repo.getAllAffiliatesByCourseAndStatus(USER_ID, COURSE_ID, USER_EMAIL, STATUS, STATUS, PageRequest.of(0, 10)))
                .thenReturn(emptyPage);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            affiliateCourseService.getAllAffiliatesByCourse(COURSE_ID, USER_EMAIL, STATUS, STATUS, PageRequest.of(0, 10));
        });
        assertEquals("Data not found", exception.getMessage());
    }


}
