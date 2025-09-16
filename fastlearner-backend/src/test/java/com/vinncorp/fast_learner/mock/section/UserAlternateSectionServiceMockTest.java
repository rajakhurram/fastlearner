package com.vinncorp.fast_learner.mock.section;

import com.vinncorp.fast_learner.mock.subscription.subscribed_user.SubscribedUserTestData;
import com.vinncorp.fast_learner.models.tag.Tag;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.section.UserAlternateSection;
import com.vinncorp.fast_learner.repositories.section.UserAlternateSectionRepository;
import com.vinncorp.fast_learner.response.section.AlternateSectionResponse;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.section.ISectionService;
import com.vinncorp.fast_learner.services.section.UserAlternateSectionService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.tag.ITagService;
import com.vinncorp.fast_learner.services.user.IUserCourseProgressService;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.util.Message;
import jakarta.persistence.Tuple;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
public class UserAlternateSectionServiceMockTest {

    private static String EMAIL = "qasim@mailinator.com";
    private static String COURSE_TITLE = "Test Course";
    private static Long COURSE_ID = 1L;
    private static Long SECTION_ID = 1L;
    private static int PAGE_NO = 0;
    private static int PAGE_SIZE = 10;
    private static String SECTION_NAME = "section_name";
    private static Long FROM_SECTION_ID = 3L;
    private static Long FROM_COURSE_ID = 4L;
    @Mock
    private UserAlternateSectionRepository repo;
    @Mock
    private ITagService tagService;
    @Mock
    private ISectionService sectionService;
    @Mock
    private ICourseService courseService;
    @Mock
    private ISubscribedUserService subscribedUserService;
    @Mock
    private IEnrollmentService enrollmentService;
    @Mock
    private IUserCourseProgressService courseProgressService;
    @Mock
    private UserService userService;
    @InjectMocks
    private UserAlternateSectionService service;
    private Course course;
    private List<Tag> tags;
    private Page<Tuple> mockPageResult;
    private List<UserAlternateSection> alternateSections;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        course = new Course();
        course.setId(COURSE_ID);
        course.setCourseCategory(CourseTestData.courseData().getCourseCategory());
        course.setTitle(COURSE_TITLE);
        course.setCreatedBy(UserTestData.userData().getId());
        tags = List.of(new Tag(1L, "Tag1", true), new Tag(2L, "Tag2", true));

        Tuple tuple = mock(Tuple.class);
        when(tuple.get("course_id")).thenReturn(COURSE_ID);
        when(tuple.get("section_id")).thenReturn(SECTION_ID);
        when(tuple.get("instructor_id")).thenReturn(UserTestData.userData().getId());
        when(tuple.get("instructor_name")).thenReturn(UserTestData.userData().getFullName());
        when(tuple.get("profile_picture")).thenReturn(anyString());
        when(tuple.get("total_reviews")).thenReturn("4");
        when(tuple.get("avg_section_rating")).thenReturn("5.0");
        when(tuple.get("course_name")).thenReturn(COURSE_TITLE);
        List<Tuple> tupleList = Collections.singletonList(tuple);
        mockPageResult = new PageImpl<>(tupleList, PageRequest.of(PAGE_NO, PAGE_SIZE, Sort.by(SECTION_NAME)), tupleList.size());

        UserAlternateSection alternateSection = new UserAlternateSection();
        alternateSection.setId(1L);
        alternateSection.setCourse(CourseTestData.courseData());
        alternateSection.getCourse().setId(COURSE_ID);
        alternateSection.setSection(SectionTestData.sectionData());
        alternateSection.getSection().setId(2L);
        alternateSection.setUser(UserTestData.userData());

        alternateSections = new ArrayList<>();
        alternateSections.add(alternateSection);
    }

    @Test
    @DisplayName("Test unpinAlternateSection - Success")
    void testUnpinAlternateSection_Success() throws Exception {
        when(repo.findByCourse_IdAndFromSection_IdAndUser_Email(COURSE_ID, SECTION_ID, EMAIL)).thenReturn(alternateSections);

        Message<String> response = service.unpinAlternateSection(COURSE_ID, SECTION_ID, EMAIL);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Alternate section is unpinned successfully.", response.getData());
        verify(repo, times(1)).deleteAll(alternateSections);
    }

    @Test
    @DisplayName("Test unpinAlternateSection - No Sections Found")
    void testUnpinAlternateSection_NoSectionsFound() throws InternalServerException, EntityNotFoundException {
        when(repo.findByCourse_IdAndFromSection_IdAndUser_Email(COURSE_ID, SECTION_ID, EMAIL)).thenReturn(new ArrayList<>());

        Message<String> response = service.unpinAlternateSection(COURSE_ID, SECTION_ID, EMAIL);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Alternate section is unpinned successfully.", response.getData());
        verify(repo, never()).deleteAll(anyList());
    }

    @Test
    @DisplayName("Test unpinAlternateSection - Internal Server Error")
    void testUnpinAlternateSection_InternalServerError() {
        when(repo.findByCourse_IdAndFromSection_IdAndUser_Email(COURSE_ID, SECTION_ID, EMAIL)).thenReturn(alternateSections);
        doThrow(new RuntimeException("Database error")).when(repo).deleteAll(alternateSections);

        InternalServerException exception = assertThrows(InternalServerException.class, () -> service.unpinAlternateSection(COURSE_ID, SECTION_ID, EMAIL));
        assertEquals("Alternate section cannot be deleted from db.", exception.getMessage());
        verify(repo, times(1)).deleteAll(alternateSections);
    }

    @Test
    @DisplayName("Test pinAlternateSection - Success")
    void testPinAlternateSection_Success() throws Exception {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(SubscribedUserTestData.subscribedUser());
        when(repo.existsByCourse_IdAndFromSection_IdAndUser_Email(COURSE_ID, FROM_COURSE_ID, EMAIL)).thenReturn(false);
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(sectionService.findById(SECTION_ID)).thenReturn(SectionTestData.sectionData());
        when(sectionService.findById(FROM_SECTION_ID)).thenReturn(SectionTestData.sectionData());
        when(courseService.findById(COURSE_ID)).thenReturn(course);
        when(courseService.findById(FROM_COURSE_ID)).thenReturn(course);

        Message<String> response = service.pinAlternateSection(COURSE_ID, SECTION_ID, FROM_SECTION_ID, FROM_COURSE_ID, EMAIL);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Successfully pinned the section.", response.getData());
        verify(repo, times(1)).save(any(UserAlternateSection.class));
        verify(courseProgressService, times(1)).markCompletedAllTopicsOfASection(SECTION_ID, UserTestData.userData().getId());
    }

    @Test
    @DisplayName("Test pinAlternateSection - User Not Enrolled")
    void testPinAlternateSection_UserNotEnrolled() {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.pinAlternateSection(COURSE_ID, SECTION_ID, FROM_SECTION_ID, FROM_COURSE_ID, EMAIL));
        assertEquals("You are not enrolled in this course please enrolled in the course first.", exception.getMessage());
        verify(repo, never()).save(any(UserAlternateSection.class));
    }

    @Test
    @DisplayName("Test pinAlternateSection - No Subscription Found")
    void testPinAlternateSection_NoSubscriptionFound() throws EntityNotFoundException {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.pinAlternateSection(COURSE_ID, SECTION_ID, FROM_SECTION_ID, FROM_COURSE_ID, EMAIL));
        assertEquals("No plan is subscribed by user: " + EMAIL, exception.getMessage());
        verify(repo, never()).save(any(UserAlternateSection.class));
    }

    @Test
    @DisplayName("Test pinAlternateSection - Section Already Pinned")
    void testPinAlternateSection_SectionAlreadyPinned() throws EntityNotFoundException {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(SubscribedUserTestData.subscribedUser());
        when(repo.existsByCourse_IdAndFromSection_IdAndUser_Email(COURSE_ID, FROM_COURSE_ID, EMAIL)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.pinAlternateSection(COURSE_ID, SECTION_ID, FROM_SECTION_ID, FROM_COURSE_ID, EMAIL));
        assertEquals("Already pinned topic cannot be pinned.", exception.getMessage());
        verify(repo, never()).save(any(UserAlternateSection.class));
    }

    @Test
    @DisplayName("Test pinAlternateSection - Internal Server Error")
    void testPinAlternateSection_InternalServerError() throws EntityNotFoundException, IOException {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(SubscribedUserTestData.subscribedUser());
        when(repo.existsByCourse_IdAndFromSection_IdAndUser_Email(COURSE_ID, FROM_COURSE_ID, EMAIL)).thenReturn(false);
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(sectionService.findById(SECTION_ID)).thenReturn(SectionTestData.sectionData());
        when(sectionService.findById(FROM_SECTION_ID)).thenReturn(SectionTestData.sectionData());
        when(courseService.findById(COURSE_ID)).thenReturn(course);
        when(courseService.findById(FROM_COURSE_ID)).thenReturn(course);

        doThrow(new RuntimeException("Database error")).when(repo).save(any(UserAlternateSection.class));

        InternalServerException exception = assertThrows(InternalServerException.class, () -> service.pinAlternateSection(COURSE_ID, SECTION_ID, FROM_SECTION_ID, FROM_COURSE_ID, EMAIL));
        assertEquals("Users alternate section " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @Test
    @DisplayName("Test fetchAlternateSection - Success")
    void testFetchAlternateSection_Success() throws Exception {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(SubscribedUserTestData.subscribedUser());
        when(courseService.findById(COURSE_ID)).thenReturn(course);
        when(tagService.findByCourseId(COURSE_ID)).thenReturn(tags);
        when(sectionService.findById(SECTION_ID)).thenReturn(SectionTestData.sectionData());
        when(repo.findAlternateSections(anyLong(), anyString(), anyString(), anyLong(), any())).thenReturn(mockPageResult);

        Message<AlternateSectionResponse> response = service.fetchAlternateSection(COURSE_ID, SECTION_ID, PAGE_NO, PAGE_SIZE, EMAIL);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertNotNull(response.getData());
        assertEquals(PAGE_NO, response.getData().getPageNo());
        assertEquals(PAGE_SIZE, response.getData().getPageSize());
        verify(repo, times(1)).findAlternateSections(anyLong(), anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("Test fetchAlternateSection - User Not Enrolled")
    void testFetchAlternateSection_UserNotEnrolled() {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.fetchAlternateSection(COURSE_ID, SECTION_ID, PAGE_NO, PAGE_SIZE, EMAIL));
        assertEquals("You are not enrolled in this course please enrolled in the course first.", exception.getMessage());
        verify(repo, never()).findAlternateSections(anyLong(), anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("Test fetchAlternateSection - No Subscription Found for User")
    void testFetchAlternateSection_NoSubscriptionFound() throws EntityNotFoundException {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.fetchAlternateSection(COURSE_ID, SECTION_ID, PAGE_NO, PAGE_SIZE, EMAIL));
        assertEquals("No plan is subscribed by user: " + EMAIL, exception.getMessage());
        verify(repo, never()).findAlternateSections(anyLong(), anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("Test fetchAlternateSection - Alternate Sections Not Found")
    void testFetchAlternateSection_AlternateSectionsNotFound() throws Exception {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(SubscribedUserTestData.subscribedUser());
        when(courseService.findById(COURSE_ID)).thenReturn(course);
        when(tagService.findByCourseId(COURSE_ID)).thenReturn(tags);
        when(sectionService.findById(SECTION_ID)).thenReturn(SectionTestData.sectionData());
        when(repo.findAlternateSections(anyLong(), anyString(), anyString(), anyLong(), any())).thenReturn(Page.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.fetchAlternateSection(COURSE_ID, SECTION_ID, PAGE_NO, PAGE_SIZE, EMAIL));
        assertEquals("Alternate section not found.", exception.getMessage());
    }

}
