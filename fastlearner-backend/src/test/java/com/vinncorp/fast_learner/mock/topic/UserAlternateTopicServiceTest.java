package com.vinncorp.fast_learner.mock.topic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.mock.section.SectionTestData;
import com.vinncorp.fast_learner.mock.subscription.subscribed_user.SubscribedUserTestData;
import com.vinncorp.fast_learner.mock.topic.TopicTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.tag.Tag;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.models.topic.UserAlternateTopic;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.topic.UserAlternateTopicRepository;
import com.vinncorp.fast_learner.response.topic.AlternativeTopicResponse;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.section.ISectionService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.tag.ITagService;
import com.vinncorp.fast_learner.services.topic.ITopicService;
import com.vinncorp.fast_learner.services.topic.UserAlternateTopicService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class UserAlternateTopicServiceTest {
    @Mock
    private ICourseService courseService;

    @Mock
    private IEnrollmentService enrollmentService;

    @Mock
    private ISectionService sectionService;

    @Mock
    private ISubscribedUserService subscribedUserService;

    @Mock
    private ITagService tagService;

    @Mock
    private ITopicService topicService;

    @Mock
    private IUserService userService;

    @Mock
    private UserAlternateTopicRepository repo;

    @InjectMocks
    private UserAlternateTopicService service;

    @Test
    @DisplayName("Test: fetchAlternativeTopics - when provided valid data (Success)")
    void testFetchAlternativeTopics_Success() throws Exception {
        long courseId = 1L, topicId = 2L;
        int pageNo = 0, pageSize = 5;
        String email = "test@example.com";

        Course course = CourseTestData.courseData();
        course.setId(courseId);

        Topic topic = TopicTestData.topicData();

        SubscribedUser subscribedUser = SubscribedUserTestData.standardSubscribedUser();

        Tag tag1 = new Tag(1L, "tag1", true), tag2 = new Tag(2L, "tag2", true);
        Tuple tuple = mock(Tuple.class);
        // Mock data
        when(tuple.get("course_id")).thenReturn(1L);
        when(tuple.get("course_name")).thenReturn("Java Fundamentals");
        when(tuple.get("course_category")).thenReturn("Programming");
        when(tuple.get("instructor_id")).thenReturn(101L);
        when(tuple.get("instructor_name")).thenReturn("John Doe");
        when(tuple.get("profile_picture")).thenReturn("profile.jpg");
        when(tuple.get("section_id")).thenReturn(11L);
        when(tuple.get("section_name")).thenReturn("Introduction");
        when(tuple.get("topic_id")).thenReturn(111L);
        when(tuple.get("topic_name")).thenReturn("Variables and Data Types");
        when(tuple.get("total_reviews")).thenReturn(25L);
        when(tuple.get("avg_section_rating")).thenReturn(4.6); // Can be Double or BigDecimal
        when(tuple.get("tag_names")).thenReturn("java,beginner,programming");

        List<Tuple> content = List.of(tuple);
        Page<Tuple> page = new PageImpl<>(content, PageRequest.of(pageNo, pageSize), 1);

        when(enrollmentService.isEnrolled(courseId, email)).thenReturn(true);
        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(courseService.findById(courseId)).thenReturn(course);
        when(tagService.findByCourseId(courseId)).thenReturn(List.of(tag1, tag2));
        when(topicService.getTopicById(topicId)).thenReturn(topic);
        when(repo.findAlternativeTopics(eq(courseId), anyString(), any(), anyLong(), any())).thenReturn(page);

        Message<AlternativeTopicResponse> result = service.fetchAlternativeTopics(courseId, topicId, pageNo, pageSize, email);

        assertNotNull(result);
        assertEquals("Fetched alternative topics successfully.", result.getMessage());
        assertEquals(HttpStatus.OK.value(), result.getStatus());
        verify(repo).findAlternativeTopics(eq(courseId), any(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("Test: fetchAlternativeTopics - when not enrolled")
    void testFetchAlternativeTopics_NotEnrolled_ThrowsBadRequest() {
        when(enrollmentService.isEnrolled(1L, "test@example.com")).thenReturn(false);
        assertThrows(BadRequestException.class, () ->
                service.fetchAlternativeTopics(1L, 2L, 0, 5, "test@example.com"));
    }

    @Test
    @DisplayName("Test: fetchAlternativeTopics - when no plan")
    void testFetchAlternativeTopics_NoPlan_ThrowsBadRequest() throws EntityNotFoundException {
        when(enrollmentService.isEnrolled(1L, "test@example.com")).thenReturn(true);
        when(subscribedUserService.findByUser("test@example.com")).thenReturn(null);
        assertThrows(BadRequestException.class, () ->
                service.fetchAlternativeTopics(1L, 2L, 0, 5, "test@example.com"));
    }

    @Test
    @DisplayName("Test: fetchAlternativeTopics - when no result")
    void testFetchAlternativeTopics_EmptyResult_ThrowsEntityNotFound() throws EntityNotFoundException, IOException {
        Course course = CourseTestData.courseData();
        course.setId(1L);
        Topic topic = TopicTestData.topicData();

        SubscribedUser subscribedUser = SubscribedUserTestData.standardSubscribedUser();

        when(enrollmentService.isEnrolled(1L, "test@example.com")).thenReturn(true);
        when(subscribedUserService.findByUser("test@example.com")).thenReturn(subscribedUser);
        when(courseService.findById(1L)).thenReturn(course);
        when(tagService.findByCourseId(1L)).thenReturn(Collections.emptyList());
        when(topicService.getTopicById(2L)).thenReturn(topic);
        when(repo.findAlternativeTopics(anyLong(), anyString(), anyString(), anyLong(), any())).thenReturn(Page.empty());

        assertThrows(EntityNotFoundException.class, () ->
                service.fetchAlternativeTopics(1L, 2L, 0, 5, "test@example.com"));
    }

    @Test
    @DisplayName("Test: pinAlternateTopic - when provided valid data (Success)")
    void testPinAlternateTopic_Success() throws Exception {
        long courseId = 1L, sectionId = 2L, fromTopicId = 3L, fromCourseId = 4L;
        String email = "test@example.com";

        User user = UserTestData.userData();
        Course course = CourseTestData.courseData();
        course.setId(courseId);
        Section section = SectionTestData.sectionData();
        section.setId(sectionId);
        Topic topic = TopicTestData.topicData();
        topic.setId(fromTopicId);
        Course fromCourse = CourseTestData.courseData();
        fromCourse.setId(fromCourseId);

        when(repo.existsByCourse_IdAndFromTopic_IdAndUser_Email(courseId, fromTopicId, email)).thenReturn(false);
        when(userService.findByEmail(email)).thenReturn(user);
        when(topicService.getTopicById(fromTopicId)).thenReturn(topic);
        when(courseService.findById(courseId)).thenReturn(course);
        when(courseService.findById(fromCourseId)).thenReturn(fromCourse);
        when(sectionService.findById(sectionId)).thenReturn(section);
        when(repo.save(any(UserAlternateTopic.class))).thenReturn(new UserAlternateTopic());

        Message<String> result = service.pinAlternateTopic(courseId, sectionId, fromTopicId, fromCourseId, email);

        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals("Successfully pinned the topic.", result.getMessage());
    }

    @Test
    @DisplayName("Test: pinAlternateTopic - when already pinned")
    void testPinAlternateTopic_AlreadyPinned_ThrowsBadRequest() {
        when(repo.existsByCourse_IdAndFromTopic_IdAndUser_Email(1L, 3L, "test@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () ->
                service.pinAlternateTopic(1L, 2L, 3L, 4L, "test@example.com"));
    }

    @Test
    @DisplayName("Test: pinAlternateTopic - when save fails")
    void testPinAlternateTopic_SaveFails_ThrowsInternalServerException() throws EntityNotFoundException {
        long courseId = 1L, sectionId = 2L, fromTopicId = 3L, fromCourseId = 4L;
        String email = "test@example.com";

        when(repo.existsByCourse_IdAndFromTopic_IdAndUser_Email(courseId, fromTopicId, email)).thenReturn(false);
        when(userService.findByEmail(email)).thenReturn(new User());
        when(topicService.getTopicById(fromTopicId)).thenReturn(new Topic());
        when(courseService.findById(courseId)).thenReturn(new Course());
        when(courseService.findById(fromCourseId)).thenReturn(new Course());
        when(sectionService.findById(sectionId)).thenReturn(new Section());
        doThrow(RuntimeException.class).when(repo).save(any());

        assertThrows(InternalServerException.class, () ->
                service.pinAlternateTopic(courseId, sectionId, fromTopicId, fromCourseId, email));
    }

    @Test
    @DisplayName("Test: unpinAlternateTopic - when provided valid data (Success)")
    void testUnpinAlternateTopic_Success() throws Exception {
        long courseId = 1L, topicId = 2L;
        String email = "test@example.com";
        UserAlternateTopic alt = new UserAlternateTopic();

        when(repo.findByCourse_IdAndFromTopic_IdAndUser_Email(courseId, topicId, email))
                .thenReturn(Optional.of(alt));

        Message<String> result = service.unpinAlternateTopic(courseId, topicId, email);

        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertEquals("Alternate topic is deleted successfully.", result.getMessage());
        verify(repo).delete(alt);
    }

    @Test
    @DisplayName("Test: unpinAlternateTopic - when not found")
    void testUnpinAlternateTopic_NotFound_ThrowsEntityNotFound() {
        when(repo.findByCourse_IdAndFromTopic_IdAndUser_Email(1L, 2L, "test@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                service.unpinAlternateTopic(1L, 2L, "test@example.com"));
    }

    @Test
    @DisplayName("Test: unpinAlternateTopic - when delete fails")
    void testUnpinAlternateTopic_DeleteFails_ThrowsInternalServerException() {
        UserAlternateTopic alt = new UserAlternateTopic();
        when(repo.findByCourse_IdAndFromTopic_IdAndUser_Email(1L, 2L, "test@example.com"))
                .thenReturn(Optional.of(alt));
        doThrow(RuntimeException.class).when(repo).delete(alt);

        assertThrows(InternalServerException.class, () ->
                service.unpinAlternateTopic(1L, 2L, "test@example.com"));
    }

}

