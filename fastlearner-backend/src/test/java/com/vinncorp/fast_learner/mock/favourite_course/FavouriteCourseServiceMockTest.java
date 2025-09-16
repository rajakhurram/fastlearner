package com.vinncorp.fast_learner.mock.favourite_course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.favourite_course.FavouriteCourse;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.repositories.favourite_course.FavouriteCourseRepository;
import com.vinncorp.fast_learner.response.favourite_course.FavouriteCourseResponse;
import com.vinncorp.fast_learner.services.course.CourseService;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.favourite_course.FavouriteCourseService;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static com.vinncorp.fast_learner.mock.course.CourseVisitorServiceMockTest.COURSE_ID;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FavouriteCourseServiceMockTest {

    private static String EMAIL = "qasim@mailinator.com";
    private static String COURSE_TITLE = "Java Programming";
    private static Long COURSE_ID = 1L;
    @Mock
    private UserService userService;
    @Mock
    private FavouriteCourseRepository repo;
    @Mock
    private CourseService courseService;
    @Mock
    private RabbitMQProducer rabbitMQProducer;
    @Mock
    private ICourseUrlService courseUrlService;
    @InjectMocks
    private FavouriteCourseService favouriteCourseService;
    private Course course;
    Tuple tuple;
    int pageSize = 10;
    int pageNo = 0;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        tuple = mock(Tuple.class);
        when(tuple.get("course_id")).thenReturn(1L);
        when(tuple.get("course_title")).thenReturn("Java Programming");
        when(tuple.get("course_description")).thenReturn("Learn Java");
        when(tuple.get("category")).thenReturn("Programming");
        when(tuple.get("course_thumbnail")).thenReturn("thumbnail_url");
        when(tuple.get("duration")).thenReturn(120);
        when(tuple.get("user_id")).thenReturn(UserTestData.userData().getId());
        when(tuple.get("full_name")).thenReturn("John Doe");
        when(tuple.get("profile_picture")).thenReturn("profile_url");
        when(tuple.get("max_rating")).thenReturn(4.5);
        when(tuple.get("total_topics")).thenReturn(10);
        when(tuple.get("total_reviews")).thenReturn(100);
        when(tuple.get("is_favourite")).thenReturn(true);
        when(tuple.get("is_enrolled")).thenReturn(true);

        course = Course.builder().id(COURSE_ID).title(COURSE_TITLE).build();

    }

    @Test
    @DisplayName("Should mark course as favourite successfully")
    void create_FavouriteCourseSuccess() throws EntityNotFoundException, InternalServerException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseService.findById(COURSE_ID)).thenReturn(course);
        when(repo.findByCourseIdAndCreatedBy(COURSE_ID, UserTestData.userData().getId())).thenReturn(Optional.empty());
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(COURSE_ID, GenericStatus.ACTIVE)).thenReturn(new CourseUrl().builder().url("test-url").build());

        Message<String> response = favouriteCourseService.create(COURSE_ID, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Course is marked as favourite.", response.getMessage());
        assertEquals("Course is marked as favourite.", response.getData());

        verify(repo, times(1)).save(any(FavouriteCourse.class));
//        verify(rabbitMQProducer, times(1)).sendMessage(eq(course.getTitle()), anyString(), eq(UserTestData.userData().getEmail()), eq(course.getCreatedBy()), eq(NotificationContentType.TEXT), eq(NotificationType.COURSE_FAVOURITE));
    }

    @Test
    @DisplayName("Should mark course as not favourite successfully")
    void create_NotFavouriteCourseSuccess() throws EntityNotFoundException, InternalServerException {

        FavouriteCourse savedFavouriteCourse = new FavouriteCourse();
        savedFavouriteCourse.setId(1L);
        savedFavouriteCourse.setCourse(course);
        savedFavouriteCourse.setCreatedBy(UserTestData.userData().getId());
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseService.findById(COURSE_ID)).thenReturn(course);
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(COURSE_ID, GenericStatus.ACTIVE)).thenReturn(new CourseUrl().builder().url("test-url").build());
        when(repo.findByCourseIdAndCreatedBy(COURSE_ID, UserTestData.userData().getId())).thenReturn(Optional.of(savedFavouriteCourse));

        Message<String> response = favouriteCourseService.create(COURSE_ID, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Course is marked as not favourite.", response.getMessage());
        assertEquals("Course is marked as not favourite.", response.getData());

        verify(repo, times(1)).delete(savedFavouriteCourse);
//        verify(rabbitMQProducer, times(1)).sendMessage(eq(course.getTitle()), anyString(), eq(UserTestData.userData().getEmail()), eq(course.getCreatedBy()), eq(NotificationContentType.TEXT), eq(NotificationType.COURSE_NOT_FAVOURITE));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user is not found")
    void create_UserNotFound() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenThrow(new EntityNotFoundException("User not found."));

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            favouriteCourseService.create(COURSE_ID, EMAIL);
        });

        assertEquals("User not found.", thrown.getMessage());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when course is not found")
    void create_CourseNotFound() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseService.findById(COURSE_ID)).thenThrow(new EntityNotFoundException("Course not found."));

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            favouriteCourseService.create(COURSE_ID, EMAIL);
        });

        assertEquals("Course not found.", thrown.getMessage());

        verify(repo, times(0)).findByCourseIdAndCreatedBy(anyLong(), anyLong());
        verify(repo, times(0)).save(any(FavouriteCourse.class));
//        verify(rabbitMQProducer, times(0)).sendMessage(anyString(), anyString(), anyString(), anyLong(), any(), any());
    }

    @Test
    @DisplayName("Should throw InternalServerException when an error occurs while saving favourite course")
    void create_InternalServerException() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(courseService.findById(COURSE_ID)).thenReturn(course);
        when(repo.findByCourseIdAndCreatedBy(COURSE_ID, UserTestData.userData().getId())).thenReturn(Optional.empty());
        when(courseUrlService.findActiveUrlByCourseIdAndStatus(COURSE_ID, GenericStatus.ACTIVE)).thenReturn(new CourseUrl().builder().url("test-url").build());
        doThrow(new RuntimeException("Database error")).when(repo).save(any(FavouriteCourse.class));

        InternalServerException thrown = assertThrows(InternalServerException.class, () -> {
            favouriteCourseService.create(COURSE_ID, EMAIL);
        });

        assertEquals("Favourite course cannot be saved due to database error.", thrown.getMessage());

        verify(repo, times(1)).save(any(FavouriteCourse.class));
//        verify(rabbitMQProducer, times(0)).sendMessage(anyString(), anyString(), anyString(), anyLong(), any(), any());
    }

    @Test
    @DisplayName("Should fetch favourite courses successfully")
    void getAllFavouriteCourses_Success() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());

        List<Tuple> tuples = Collections.singletonList(tuple);
        Page<Tuple> pagedData = new PageImpl<>(tuples, PageRequest.of(pageNo, pageSize), 1);
        when(repo.findFavouriteCoursesByTitle("%" + COURSE_TITLE + "%", UserTestData.userData().getId(), PageRequest.of(pageNo, pageSize))).thenReturn(pagedData);

        Message<FavouriteCourseResponse> response = favouriteCourseService.getAllFavouriteCourses(COURSE_TITLE, pageSize, pageNo, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(1, response.getData().getTotalElements());
        assertEquals(COURSE_TITLE, response.getData().getFavouriteCourses().get(0).getTitle());

        verify(userService, times(1)).findByEmail(EMAIL);
        verify(repo, times(1)).findFavouriteCoursesByTitle("%" + COURSE_TITLE + "%", UserTestData.userData().getId(), PageRequest.of(pageNo, pageSize));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no favourite courses are found")
    void getAllFavouriteCourses_NoCoursesFound() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());

        Page<Tuple> pagedData = new PageImpl<>(Collections.emptyList(), PageRequest.of(pageNo, pageSize), 0);
        when(repo.findFavouriteCoursesByTitle("%" + COURSE_TITLE + "%", UserTestData.userData().getId(), PageRequest.of(pageNo, pageSize))).thenReturn(pagedData);

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            favouriteCourseService.getAllFavouriteCourses(COURSE_TITLE, pageSize, pageNo, EMAIL);
        });

        assertEquals("No favourite courses present for the user: " + EMAIL, thrown.getMessage());

        verify(userService, times(1)).findByEmail(EMAIL);
        verify(repo, times(1)).findFavouriteCoursesByTitle("%" + COURSE_TITLE + "%", UserTestData.userData().getId(), PageRequest.of(pageNo, pageSize));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user is not found")
    void getAllFavouriteCourses_UserNotFound() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenThrow(new EntityNotFoundException("User not found."));

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            favouriteCourseService.getAllFavouriteCourses(COURSE_TITLE, pageSize, pageNo, EMAIL);
        });

        assertEquals("User not found.", thrown.getMessage());

        verify(userService, times(1)).findByEmail(EMAIL);
        verify(repo, times(0)).findFavouriteCoursesByTitle(anyString(), anyLong(), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should fetch favourite courses with null course title successfully")
    void getAllFavouriteCourses_NullCourseTitle() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        List<Tuple> tuples = Collections.singletonList(tuple);
        Page<Tuple> pagedData = new PageImpl<>(tuples, PageRequest.of(pageNo, pageSize), 1);
        when(repo.findFavouriteCoursesByTitle(null, UserTestData.userData().getId(), PageRequest.of(pageNo, pageSize))).thenReturn(pagedData);

        Message<FavouriteCourseResponse> response = favouriteCourseService.getAllFavouriteCourses(null, pageSize, pageNo, EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(1, response.getData().getTotalElements());
        assertEquals(COURSE_TITLE, response.getData().getFavouriteCourses().get(0).getTitle());

        verify(userService, times(1)).findByEmail(EMAIL);
        verify(repo, times(1)).findFavouriteCoursesByTitle(null, UserTestData.userData().getId(), PageRequest.of(pageNo, pageSize));
    }

}
