package com.vinncorp.fast_learner.mock.course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.course.CourseCategory;
import com.vinncorp.fast_learner.repositories.course.CourseCategoryRepository;
import com.vinncorp.fast_learner.services.course.CourseCategoryService;
import com.vinncorp.fast_learner.util.Message;
import lombok.extern.slf4j.Slf4j;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
public class CourseCategoryServiceMockTest {

    private final String EMAIL = "qasim@mailinator.com";
    @InjectMocks
    private CourseCategoryService courseCategoryService;
    @Mock
    private CourseCategoryRepository repo;
    CourseCategory courseCategory;

    @BeforeEach
    public void init() throws IOException {
        MockitoAnnotations.openMocks(this);
        courseCategory = new CourseCategory();
        courseCategory.setId(1L);
        courseCategory.setName("Development");
        courseCategory.setActive(true);
    }

    @Test
    @DisplayName("Test findById: Should return course category when found")
    void testFindByIdSuccess() throws EntityNotFoundException {

        when(repo.findById(1L)).thenReturn(Optional.of(courseCategory));

        CourseCategory result = courseCategoryService.findById(courseCategory.getId());
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test findById: Should throw EntityNotFoundException when category is not found")
    void testFindByIdNotFound() {

        when(repo.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> courseCategoryService.findById(1L));
        assertEquals("Course category not found.", exception.getMessage(), "Exception message should be 'Course category not found.'");
    }

    @Test
    @DisplayName("Test findById: Should handle null ID gracefully")
    void testFindByIdNullId() {

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            courseCategoryService.findById(null);
        });

        assertEquals("Course category not found.", exception.getMessage());

    }

    @DisplayName("Fetch all course category with valid data")
    @Test
    void fetchAllCourseCategory_success() throws EntityNotFoundException {
        List<CourseCategory> categories = Arrays.asList(courseCategory);
        when(repo.findAllByIsActive(true)).thenReturn(categories);

        Message<List<CourseCategory>> response = courseCategoryService.fetchAllCourseCategory();

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("Course category is fetched successfully.", response.getMessage());
        assertEquals(categories, response.getData());
    }

    @DisplayName("Fetch all course category with no categories found")
    @Test
    void fetchAllCourseCategory_noCategoriesFound() {
        when(repo.findAllByIsActive(true)).thenReturn(Collections.emptyList());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            courseCategoryService.fetchAllCourseCategory();
        });

        assertEquals("Course category is not found.", thrown.getMessage());
    }

    @DisplayName("Fetch all course category when repo through exception")
    @Test
    void fetchAllCourseCategory_repoThrowsException() {
        when(repo.findAllByIsActive(true)).thenThrow(new RuntimeException("Database is down"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            courseCategoryService.fetchAllCourseCategory();
        });

        assertEquals("Database is down", thrown.getMessage());
    }

}
