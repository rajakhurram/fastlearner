package com.vinncorp.fast_learner.mock.topic;

import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.mock.section.SectionTestData;
import com.vinncorp.fast_learner.mock.subscription.subscribed_user.SubscribedUserTestData;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.models.section.UserAlternateSection;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.models.topic.TopicNotes;
import com.vinncorp.fast_learner.repositories.topic.TopicNotesRepository;
import com.vinncorp.fast_learner.request.topic.CreateUpdateTopicNotesRequest;
import com.vinncorp.fast_learner.response.topic.TopicNotesResponse;
import com.vinncorp.fast_learner.services.enrollment.EnrollmentService;
import com.vinncorp.fast_learner.services.section.IUserAlternateSectionService;
import com.vinncorp.fast_learner.services.subscription.SubscribedUserService;
import com.vinncorp.fast_learner.services.topic.TopicNotesService;
import com.vinncorp.fast_learner.services.topic.TopicService;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.util.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TopicNotesServiceMockTest {

    private static final String EMAIL = "qasim@mailinator.com";
    private static final Long COURSE_ID = 1L;
    private static Long TOPIC_ID = 1L;
    private static Long TOPIC_NOTES_ID = 1L;
    private static Integer PAGE_NO = 0;
    private static Integer PAGE_SIZE = 5;
    @InjectMocks
    private TopicNotesService topicNotesService;
    @Mock
    private EnrollmentService enrollmentService;
    @Mock
    private SubscribedUserService subscribedUserService;
    @Mock
    private TopicService topicService;
    @Mock
    UserService userService;
    @Mock
    private IUserAlternateSectionService userAlternateSectionService;
    @Mock
    private TopicNotesRepository repo;
    CreateUpdateTopicNotesRequest request;
    List<TopicNotes> notes;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        request = new CreateUpdateTopicNotesRequest();
        request.setCourseId(COURSE_ID);
        request.setTopicId(TOPIC_ID);
        request.setNote("This is a note.");

        TopicNotes topicNotes1 = new TopicNotes();
        topicNotes1.setNote("note 1");
        topicNotes1.setCreationDate(new Date());
        topicNotes1.setTopic(TopicTestData.topicData());

        TopicNotes topicNotes2 = new TopicNotes();
        topicNotes2.setNote("note 2");
        topicNotes2.setCreationDate(new Date());
        topicNotes2.setTopic(TopicTestData.topicData());

        notes = List.of(topicNotes1, topicNotes2);
    }

    @Test
    @DisplayName("Should delete topic note successfully")
    void testDeleteTopicNoteSuccess() throws BadRequestException, EntityNotFoundException, InternalServerException, IOException {
        Topic topic = TopicTestData.topicData();
        topic.setSection(Section.builder().isFree(true).build());
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(SubscribedUserTestData.subscribedUser());
        when(topicService.getTopicById(TOPIC_ID)).thenReturn(topic);
        when(repo.findByIdAndCreatedBy(TOPIC_NOTES_ID, SubscribedUserTestData.subscribedUser().getUser().getId()))
                .thenReturn(Optional.of(TopicNotes.builder().id(TOPIC_NOTES_ID).build()));
        Message<String> result = topicNotesService.deleteTopicNote(TOPIC_NOTES_ID, TOPIC_ID, COURSE_ID, EMAIL);
        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals("Topic note is deleted successfully.", result.getMessage());
        verify(enrollmentService, times(1)).isEnrolled(COURSE_ID, EMAIL);
        verify(subscribedUserService, times(1)).findByUser(EMAIL);
        verify(topicService, times(1)).getTopicById(TOPIC_ID);
        verify(repo, times(1)).findByIdAndCreatedBy(TOPIC_NOTES_ID, SubscribedUserTestData.subscribedUser().getUser().getId());
        verify(repo, times(1)).delete(any(TopicNotes.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when user is not enrolled in the course")
    void testDeleteTopicNoteUserNotEnrolled() {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(false);
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                topicNotesService.deleteTopicNote(TOPIC_NOTES_ID, TOPIC_ID, COURSE_ID, EMAIL)
        );
        assertEquals("You are not enrolled in this course please enroll in the course first.", exception.getMessage());
        verify(enrollmentService, times(1)).isEnrolled(COURSE_ID, EMAIL);
        verifyNoInteractions(subscribedUserService, topicService, repo);
    }

    @Test
    @DisplayName("Should throw BadRequestException when user does not have a subscribed plan")
    void testDeleteTopicNoteUserWithoutSubscribedPlan() throws EntityNotFoundException {
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(null);
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                topicNotesService.deleteTopicNote(TOPIC_NOTES_ID, TOPIC_ID, COURSE_ID, EMAIL)
        );
        assertEquals("No plan is subscribed by user: " + EMAIL, exception.getMessage());
        verify(enrollmentService, times(1)).isEnrolled(COURSE_ID, EMAIL);
        verify(subscribedUserService, times(1)).findByUser(EMAIL);
        verifyNoInteractions(topicService, repo);
    }

    @Test
    @DisplayName("Should throw BadRequestException when the topic is not accessible for the user")
    void testDeleteTopicNoteInaccessibleTopic() throws EntityNotFoundException, IOException {

        var course = CourseTestData.courseData();
        course.setId(COURSE_ID);

        var section = SectionTestData.sectionData();
        section.setFree(false);
        section.setCourse(course);

        Topic topic = Topic.builder()
                .id(TOPIC_ID)
                .section(section)
                .build();

        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(SubscribedUserTestData.standardSubscribedUser());
        when(topicService.getTopicById(TOPIC_ID)).thenReturn(topic);
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                topicNotesService.deleteTopicNote(TOPIC_NOTES_ID, TOPIC_ID, COURSE_ID, EMAIL)
        );
        assertEquals("You doesn't have access to this topic.", exception.getMessage());
        verify(enrollmentService, times(1)).isEnrolled(COURSE_ID, EMAIL);
        verify(subscribedUserService, times(1)).findByUser(EMAIL);
        verify(topicService, times(1)).getTopicById(TOPIC_ID);
        verifyNoInteractions(repo);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when topic note is not found for the user")
    void testDeleteTopicNoteNotFound() throws EntityNotFoundException {
        Topic topic = Topic.builder()
                .section(Section.builder().isFree(true).build())
                .build();
        when(enrollmentService.isEnrolled(COURSE_ID, EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(SubscribedUserTestData.subscribedUser());
        when(topicService.getTopicById(TOPIC_ID)).thenReturn(topic);
        when(repo.findByIdAndCreatedBy(TOPIC_NOTES_ID, SubscribedUserTestData.subscribedUser().getUser().getId()))
                .thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                topicNotesService.deleteTopicNote(TOPIC_NOTES_ID, TOPIC_ID, COURSE_ID, EMAIL)
        );
        assertEquals("Topic note is not found for the user.", exception.getMessage());
        verify(enrollmentService, times(1)).isEnrolled(COURSE_ID, EMAIL);
        verify(subscribedUserService, times(1)).findByUser(EMAIL);
        verify(topicService, times(1)).getTopicById(TOPIC_ID);
        verify(repo, times(1)).findByIdAndCreatedBy(TOPIC_NOTES_ID, SubscribedUserTestData.subscribedUser().getUser().getId());
        verify(repo, never()).delete(any(TopicNotes.class));
    }

    @Test
    @DisplayName("Should fetch all topic notes successfully")
    void testFetchAllTopicNotesSuccess() throws BadRequestException, EntityNotFoundException {
        Page<TopicNotes> pageData = new PageImpl<>(notes, PageRequest.of(PAGE_NO, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "creationDate")), notes.size());
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findByTopic_Section_Course_IdInAndCreatedBy(Collections.singletonList(COURSE_ID), UserTestData.userData().getId(), PageRequest.of(PAGE_NO, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "creationDate"))))
                .thenReturn(pageData);
        Message<TopicNotesResponse> result = topicNotesService.fetchAllTopicNotes(COURSE_ID, PAGE_NO, PAGE_SIZE, EMAIL);
        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals("Fetched all topic's notes successfully.", result.getMessage());
        assertEquals(2, result.getData().getTopicNotes().size());
        assertEquals(1, result.getData().getTotalPages());
        verify(userService, times(1)).findByEmail(EMAIL);
        verify(repo, times(1)).findByTopic_Section_Course_IdInAndCreatedBy(Collections.singletonList(COURSE_ID), UserTestData.userData().getId(), PageRequest.of(PAGE_NO, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "creationDate")));
    }

    @Test
    @DisplayName("Should return empty topic notes list when no notes are found")
    void testFetchAllTopicNotesNoNotesFound() throws EntityNotFoundException {
        Page<TopicNotes> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(PAGE_NO, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "creationDate")), 0);
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(repo.findByTopic_Section_Course_IdInAndCreatedBy(Collections.singletonList(COURSE_ID), UserTestData.userData().getId(), PageRequest.of(PAGE_NO, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "creationDate"))))
                .thenReturn(emptyPage);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                topicNotesService.fetchAllTopicNotes(COURSE_ID, PAGE_NO, PAGE_SIZE, EMAIL)
        );
        assertEquals("No topic notes present for the user.", exception.getMessage());
        verify(userService, times(1)).findByEmail(EMAIL);
        verify(repo, times(1)).findByTopic_Section_Course_IdInAndCreatedBy(Collections.singletonList(COURSE_ID), UserTestData.userData().getId(), PageRequest.of(PAGE_NO, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "creationDate")));
    }

    @Test
    @DisplayName("Should throw BadRequestException when user is not found")
    void testFetchAllTopicNotesUserNotFound() throws EntityNotFoundException {
        when(userService.findByEmail(EMAIL)).thenThrow(new EntityNotFoundException("User not found."));
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                topicNotesService.fetchAllTopicNotes(COURSE_ID, PAGE_NO, PAGE_SIZE, EMAIL)
        );
        assertEquals("User not found.", exception.getMessage());
        verify(userService, times(1)).findByEmail(EMAIL);
        verifyNoInteractions(repo);
    }

    @Test
    @DisplayName("Should handle pagination correctly when fetching topic notes")
    void testFetchAllTopicNotesPagination() throws BadRequestException, EntityNotFoundException, IOException {
        var course = CourseTestData.courseData();
        course.setId(COURSE_ID);

        var toCourse = CourseTestData.courseData();
        toCourse.setId(COURSE_ID+1);

        var section = SectionTestData.sectionData();
        section.setCourse(course);

        var toSection = SectionTestData.sectionData();
        toSection.setCourse(toCourse);

        Page<TopicNotes> pageData = new PageImpl<>(notes, PageRequest.of(PAGE_NO, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "creationDate")), 4);
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());

        UserAlternateSection userAlternateSection = new UserAlternateSection();
        userAlternateSection.setId(1L);
        userAlternateSection.setFromCourse(course);
        userAlternateSection.setCourse(course);
        userAlternateSection.setSection(section);


        when(userAlternateSectionService.findByCourseId(COURSE_ID, UserTestData.userData().getId()))
                .thenReturn(userAlternateSection);
        when(repo.findByTopic_Section_Course_IdInAndCreatedBy(any(List.class), anyLong(), any(Pageable.class)))
                .thenReturn(pageData);
        Message<TopicNotesResponse> result = topicNotesService.fetchAllTopicNotes(COURSE_ID, PAGE_NO, PAGE_SIZE, EMAIL);
        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals(2, result.getData().getTopicNotes().size());
        assertEquals(1, result.getData().getTotalPages());
        verify(userService, times(1)).findByEmail(EMAIL);
        verify(repo, times(1)).findByTopic_Section_Course_IdInAndCreatedBy(any(List.class), anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should create a new note successfully")
    void testTakeNotesCreateNoteSuccess() throws BadRequestException, EntityNotFoundException, InternalServerException, IOException {
        Topic topic = TopicTestData.topicData();

        when(enrollmentService.isEnrolled(request.getCourseId(), EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(SubscribedUserTestData.subscribedUser());
        when(topicService.getTopicById(request.getTopicId())).thenReturn(topic);
        when(repo.save(any(TopicNotes.class))).thenReturn(new TopicNotes());
        Message<String> result = topicNotesService.takeNotes(request, EMAIL);
        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals("Successfully created/update a note.", result.getMessage());
        verify(enrollmentService, times(1)).isEnrolled(request.getCourseId(), EMAIL);
        verify(subscribedUserService, times(1)).findByUser(EMAIL);
        verify(topicService, times(1)).getTopicById(request.getTopicId());
        verify(repo, times(1)).save(any(TopicNotes.class));
    }

    @Test
    @DisplayName("Should update an existing note successfully")
    void testTakeNotesUpdateNoteSuccess() throws BadRequestException, EntityNotFoundException, InternalServerException, IOException {
        request.setTopicNotesId(TOPIC_NOTES_ID);

        var course = CourseTestData.courseData();
        course.setCreatedBy(UserTestData.userData().getId());

        Section section = SectionTestData.sectionData();
        section.setCourse(course);
        section.setFree(true);

        Topic topic = Topic.builder().id(1L).section(section).build();
        TopicNotes existingNote = TopicNotes.builder()
                .id(1L)
                .note("Old note")
                .time("09:00")
                .topic(topic)
                .build();

        when(enrollmentService.isEnrolled(request.getCourseId(), EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(SubscribedUserTestData.subscribedUser());
        when(topicService.getTopicById(request.getTopicId())).thenReturn(topic);
        when(repo.findById(request.getTopicNotesId())).thenReturn(Optional.of(existingNote));
        when(repo.save(any(TopicNotes.class))).thenReturn(existingNote);
        Message<String> result = topicNotesService.takeNotes(request, EMAIL);
        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals("Successfully created/update a note.", result.getMessage());
        verify(enrollmentService, times(1)).isEnrolled(request.getCourseId(), EMAIL);
        verify(subscribedUserService, times(1)).findByUser(EMAIL);
        verify(topicService, times(1)).getTopicById(request.getTopicId());
        verify(repo, times(1)).findById(TOPIC_NOTES_ID);
        verify(repo, times(1)).save(existingNote);
    }

    @Test
    @DisplayName("Should throw BadRequestException if user is not enrolled in the course")
    void testTakeNotesUserNotEnrolled() {
        when(enrollmentService.isEnrolled(request.getCourseId(), EMAIL)).thenReturn(false);
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                topicNotesService.takeNotes(request, EMAIL)
        );
        assertEquals("You are not enrolled in this course please enroll in the course first.", exception.getMessage());
        verify(enrollmentService, times(1)).isEnrolled(request.getCourseId(), EMAIL);
        verifyNoInteractions(subscribedUserService, topicService, repo);
    }

    @Test
    @DisplayName("Should throw BadRequestException if no plan is subscribed by the user")
    void testTakeNotesNoPlanSubscribed() throws EntityNotFoundException {
        when(enrollmentService.isEnrolled(request.getCourseId(), EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(null);
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                topicNotesService.takeNotes(request, EMAIL)
        );
        assertEquals("No plan is subscribed by user: " + EMAIL, exception.getMessage());
        verify(enrollmentService, times(1)).isEnrolled(request.getCourseId(), EMAIL);
        verify(subscribedUserService, times(1)).findByUser(EMAIL);
        verifyNoInteractions(topicService, repo);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if topic note ID is invalid during update")
    void testTakeNotesInvalidTopicNoteId() throws EntityNotFoundException, IOException {
        request.setTopicNotesId(TOPIC_NOTES_ID);
        Topic topic = TopicTestData.topicData();
        when(enrollmentService.isEnrolled(request.getCourseId(), EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(SubscribedUserTestData.subscribedUser());
        when(topicService.getTopicById(request.getTopicId())).thenReturn(topic);
        when(repo.findById(request.getTopicNotesId())).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                topicNotesService.takeNotes(request, EMAIL)
        );
        assertEquals("Provided topic note id is not valid.", exception.getMessage());
        verify(repo, times(1)).findById(request.getTopicNotesId());
    }

    @Test
    @DisplayName("Should throw InternalServerException when note saving fails")
    void testTakeNotesSaveFailure() throws EntityNotFoundException, IOException {
        Topic topic = TopicTestData.topicData();
        when(enrollmentService.isEnrolled(request.getCourseId(), EMAIL)).thenReturn(true);
        when(subscribedUserService.findByUser(EMAIL)).thenReturn(SubscribedUserTestData.subscribedUser());
        when(topicService.getTopicById(request.getTopicId())).thenReturn(topic);
        when(repo.save(any(TopicNotes.class))).thenThrow(new RuntimeException("Database error"));
        InternalServerException exception = assertThrows(InternalServerException.class, () ->
                topicNotesService.takeNotes(request, EMAIL)
        );
        assertEquals("Topic note cannot be saved due to database error.", exception.getMessage());
        verify(repo, times(1)).save(any(TopicNotes.class));
    }
}
