package com.vinncorp.fast_learner.mock.course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.course.CourseLevel;
import com.vinncorp.fast_learner.repositories.course.CourseLevelRepository;
import com.vinncorp.fast_learner.services.course.CourseLevelService;
import com.vinncorp.fast_learner.util.Message;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
public class CourseLevelServiceMockTest {

    private final String EMAIL = "qasim@mailinator.com";
    @InjectMocks
    private CourseLevelService courseLevelService;
    @Mock
    private  CourseLevelRepository repo;
    private CourseLevel courseLevel;

    @BeforeEach
    public void init() throws IOException {
        MockitoAnnotations.openMocks(this);
        courseLevel = new CourseLevel();
        courseLevel.setId(1L);
        courseLevel.setName("Beginner");
        courseLevel.setActive(true);
    }

    @Test
    @DisplayName("Test findById: Should return course level when found")
    void testFindByIdSuccess() throws EntityNotFoundException {

        when(repo.findById(1L)).thenReturn(Optional.of(courseLevel));

        CourseLevel result = courseLevelService.findById(courseLevel.getId());

        assertNotNull(result);
    }

    @Test
    @DisplayName("Test findById: Should throw EntityNotFoundException when level is not found")
    void testFindByIdNotFound() {

        when(repo.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> courseLevelService.findById(1L));
        assertEquals("Course level is not found.", exception.getMessage(), "Exception message should be 'Course level is not found.'");
    }

    @Test
    @DisplayName("Test findById: Should handle null ID gracefully")
    void testFindByIdNullId() {

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            courseLevelService.findById(null);
        });

        assertEquals("Course level is not found.", exception.getMessage());

    }

    @DisplayName("Fetch all course level with valid data")
    @Test
    void fetchAllCourseLevel_success() throws EntityNotFoundException {
        List<CourseLevel> courseLevels = Arrays.asList(new CourseLevel(), new CourseLevel());
        when(repo.findByIsActive(true)).thenReturn(courseLevels);

        Message<List<CourseLevel>> response = courseLevelService.fetchAllCourseLevel(EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("Course level fetched successfully.", response.getMessage());
        assertEquals(courseLevels, response.getData());
    }

    @DisplayName("Fetch all course level when no course level found")
    @Test
    void fetchAllCourseLevel_noCourseLevelsFound() {
        when(repo.findByIsActive(true)).thenReturn(Collections.emptyList());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> courseLevelService.fetchAllCourseLevel("test@example.com"));

        assertEquals("No course level is found.", exception.getMessage());
    }

    @DisplayName("Fetch all course level when repository exception")
    @Test
    void fetchAllCourseLevel_repositoryException() {
        when(repo.findByIsActive(true)).thenThrow(new DataAccessException("DB error") {});

        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> courseLevelService.fetchAllCourseLevel("test@example.com"));

        assertEquals("DB error", exception.getMessage());
    }

}
