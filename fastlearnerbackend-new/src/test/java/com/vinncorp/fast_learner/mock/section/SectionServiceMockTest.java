package com.vinncorp.fast_learner.mock.section;

import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.mock.subscription.subscribed_user.SubscribedUserTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.mock.user.user_course_progress.UserCourseProgressTestData;
import com.vinncorp.fast_learner.repositories.section.SectionRepository;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.dtos.section.SectionDetail;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.response.section.SectionDetailForUpdateResponse;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.quiz.IQuizQuestionAnswerService;
import com.vinncorp.fast_learner.services.section.SectionService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.user.IUserCourseProgressService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseType;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
public class SectionServiceMockTest {

    private static final String EMAIL = "qasim@mailinator.com";
    private static final Long COURSE_ID = 12L;
    private static final Long SECTION_ID = 16L;

    @InjectMocks
    private SectionService sectionService;

    @Mock
    private IQuizQuestionAnswerService quizQuestionAnswerService;
    @Mock
    private ISubscribedUserService subscribedUserService;
    @Mock
    private IEnrollmentService enrollmentService;
    @Mock
    private IUserCourseProgressService userCourseProgressService;
    @Mock
    private SectionRepository repo;
    @Mock
    private IUserService userService;
    @Mock
    private ICourseService courseService;
    @Mock
    private ICourseUrlService courseUrlService;

    private User user;
    private Course course;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        course = CourseTestData.courseData();
        course.setId(COURSE_ID);
    }

    @DisplayName("Fetch all sections for update with valid course and user.")
    @Test
    void fetchAllSectionForUpdate_validCourseAndUser() throws EntityNotFoundException, BadRequestException {

        Tuple tuple = mock(Tuple.class);
        when(tuple.get("id")).thenReturn("1");
        when(tuple.get("name")).thenReturn("Section 1");
        when(tuple.get("sequence")).thenReturn("1");
        when(tuple.get("isFree")).thenReturn("true");
        List<Tuple> data = Collections.singletonList(tuple);
        List<SectionDetailForUpdateResponse> sectionDetails = Collections.singletonList(new SectionDetailForUpdateResponse());

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findAllByCourseIdAndUserId(COURSE_ID, UserTestData.userData().getId())).thenReturn(data);
        when(SectionDetailForUpdateResponse.from(data)).thenReturn(sectionDetails);

        Message<List<SectionDetailForUpdateResponse>> response = sectionService.fetchAllSectionForUpdate(COURSE_ID, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Successfully fetched all sections for update.", response.getMessage());
        assertThat(response.getData()).isNotNull();
    }

    @DisplayName("Fetch all sections for update with invalid data.")
    @Test
    void fetchAllSectionForUpdate_courseNotAccessible() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findAllByCourseIdAndUserId(COURSE_ID, UserTestData.userData().getId())).thenReturn(Collections.emptyList());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                sectionService.fetchAllSectionForUpdate(COURSE_ID, EMAIL));
        assertEquals("The user cannot access this course.", exception.getMessage());
    }

    @DisplayName("Fetch all sections for update with invalid user.")
    @Test
    void fetchAllSectionForUpdate_userNotFound() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenThrow(new EntityNotFoundException("User not found"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                sectionService.fetchAllSectionForUpdate(COURSE_ID, EMAIL));
        assertEquals("User not found", exception.getMessage());
    }

    @DisplayName("Fetch all sections for update with null course id.")
    @Test
    void fetchAllSectionForUpdate_nullCourseId() {

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                sectionService.fetchAllSectionForUpdate(null, EMAIL));
        assertEquals("Course ID cannot be null", exception.getMessage());
    }

    @DisplayName("Fetch all sections for update with null email.")
    @Test
    void fetchAllSectionForUpdate_nullEmail() {

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                sectionService.fetchAllSectionForUpdate(COURSE_ID, null));
        assertEquals("Email cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Getting all section by course id with valid data.")
    public void getAllSectionsByCourseId_whenProvidedCorrectData()
            throws EntityNotFoundException, IOException, BadRequestException {

        log.info("Testing method: SectionService.getAllSectionsByCourseId(" + COURSE_ID + ", " + EMAIL + ")");

        course.setCourseType(CourseType.FREE_COURSE);
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(SubscribedUserTestData.subscribedUser());
        when(courseService.findById(COURSE_ID)).thenReturn(course);
        when(repo.findAllByCourseId(COURSE_ID, UserTestData.userData().getId())).thenReturn(SectionTestData.createSectionByCourseData());
        when(userCourseProgressService.getPreviousTopicByUserAndCourse(COURSE_ID, EMAIL)).thenReturn(UserCourseProgressTestData.userCourseProgressData());
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(COURSE_ID, GenericStatus.ACTIVE)).thenReturn(new CourseUrl().builder().url("test-url").build());
        var m = sectionService.getAllSectionsByCourseId(COURSE_ID, EMAIL);

        assertThat(m).isNotNull();
        assertThat(m.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(m.getMessage()).isEqualTo("Fetched all sections by course and user successfully.");
        assertThat(m.getCode()).isEqualTo(HttpStatus.OK.name());
        assertThat(m.getData()).isNotNull();
    }

    @Test
    @DisplayName("Fetch sections details by course id for correct value.")
    public void fetchSectionDetailByCourseId_whenProvidingCorrectData() throws EntityNotFoundException {
        log.info("Testing method: fetchSectionDetailByCourseId(" + COURSE_ID + ")");
        when(repo.fetchSectionDetailByCourseId(any())).thenReturn(SectionTestData.createSectionDetailData());
        List<SectionDetail> sectionDetailList = sectionService.fetchSectionDetailByCourseId(COURSE_ID);
        assertThat(sectionDetailList).isNotNull();
    }

    @Test
    @DisplayName("Fetch section by id for correct value.")
    public void fetchSection_whenProvidedCorrectData() throws EntityNotFoundException {
        log.info("Testing method: findById(" + SECTION_ID + ")");

        when(repo.findById(any())).thenReturn(Optional.of(Section.builder().id(SECTION_ID).isActive(true).build()));

        Section section = sectionService.findById(SECTION_ID);
        assertThat(section).isNotNull();
        assertThat(section.getId()).isEqualTo(SECTION_ID);
    }

    @Test
    @DisplayName("Should save a new section successfully")
    void shouldSaveNewSectionSuccessfully() throws InternalServerException {
        Section section = Section.builder()
                .name("New Section")
                .isFree(true)
                .sequenceNumber(1)
                .isActive(true)
                .build();

        when(repo.save(any(Section.class))).thenReturn(section);

        Section savedSection = sectionService.save(section);

        assertNotNull(savedSection);
        assertEquals("New Section", savedSection.getName());
        verify(repo, times(1)).save(section);
    }


    @Test
    @DisplayName("Should delete the section when delete is true and section has an ID")
    void shouldDeleteSectionWhenDeleteIsTrueAndSectionHasId() throws InternalServerException {
        Long sectionId = 1L;
        Section section = Section.builder()
                .id(sectionId)
                .delete(true)
                .build();

        doNothing().when(repo).deleteById(sectionId);

        Section result = sectionService.save(section);

        assertNull(result);
        verify(repo, times(1)).deleteById(sectionId);
        verify(repo, never()).save(section);
    }

    @Test
    @DisplayName("Should update an existing section successfully")
    void shouldUpdateExistingSectionSuccessfully() throws InternalServerException {
        Long sectionId = 1L;
        Section section = Section.builder()
                .id(sectionId)
                .name("Updated Section")
                .isFree(false)
                .sequenceNumber(2)
                .isActive(false)
                .build();

        when(repo.save(any(Section.class))).thenReturn(section);

        Section updatedSection = sectionService.save(section);

        assertNotNull(updatedSection);
        assertEquals("Updated Section", updatedSection.getName());
        assertFalse(updatedSection.isFree());
        verify(repo, times(1)).save(section);
    }

    @Test
    @DisplayName("Should throw InternalServerException when an exception occurs during saving")
    void shouldThrowInternalServerExceptionWhenExceptionOccursDuringSaving() {
        Section section = Section.builder()
                .name("Faulty Section")
                .isFree(true)
                .sequenceNumber(1)
                .isActive(true)
                .build();

        when(repo.save(any(Section.class))).thenThrow(new RuntimeException("Database error"));

        InternalServerException thrown = assertThrows(InternalServerException.class, () -> {
            sectionService.save(section);
        });

        assertEquals("Section" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, thrown.getMessage());
        verify(repo, times(1)).save(section);
    }

    @Test
    @DisplayName("Should throw InternalServerException when an exception occurs during deletion")
    void shouldThrowInternalServerExceptionWhenExceptionOccursDuringDeletion() {
        Long sectionId = 1L;
        Section section = Section.builder()
                .id(sectionId)
                .delete(true)
                .build();

        doThrow(new RuntimeException("Database error")).when(repo).deleteById(sectionId);

        InternalServerException thrown = assertThrows(InternalServerException.class, () -> {
            sectionService.save(section);
        });

        assertEquals("Section" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, thrown.getMessage());
        verify(repo, times(1)).deleteById(sectionId);
        verify(repo, never()).save(section);
    }

}

