package com.vinncorp.fast_learner.mock.course;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.course.CourseTag;
import com.vinncorp.fast_learner.repositories.course.CourseTagRepository;
import com.vinncorp.fast_learner.services.course.CourseTagService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class CourseTagServiceMockTest {

    @InjectMocks
    private CourseTagService courseTagService;
    private static Long COURSE_ID = 1L;

    @Mock
    private CourseTagRepository repo;
    private List<CourseTag> courseTags;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        courseTags = List.of(
                CourseTag.builder().id(1L).build(),
                CourseTag.builder().id(2L).build()
        );
    }

    @Test
    @DisplayName("Test deleteAllCourseTagByTagIds - Success")
    void testDeleteAllCourseTagByTagIds_Success() {
        assertDoesNotThrow(() -> courseTagService.deleteAllCourseTagByTagIds(COURSE_ID));
        verify(repo, times(1)).deleteAllByCourseId(COURSE_ID);
    }

    @Test
    @DisplayName("Test deleteAllCourseTagByTagIds - Repository Exception Handling")
    void testDeleteAllCourseTagByTagIds_RepositoryExceptionHandling() {

        doThrow(new RuntimeException("Database error")).when(repo).deleteAllByCourseId(COURSE_ID);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseTagService.deleteAllCourseTagByTagIds(COURSE_ID);
        });

        assertEquals("Database error", exception.getMessage());

        verify(repo, times(1)).deleteAllByCourseId(COURSE_ID);
    }

    @Test
    @DisplayName("Test createAllCourseTags - Success")
    void testCreateAllCourseTags_Success() {
        when(repo.saveAll(courseTags)).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> courseTagService.createAllCourseTags(courseTags));
        verify(repo, times(1)).saveAll(courseTags);
    }

    @Test
    @DisplayName("Test createAllCourseTags - Repository Exception Handling")
    void testCreateAllCourseTags_RepositoryExceptionHandling() {
        doThrow(new RuntimeException("Database error")).when(repo).saveAll(courseTags);
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            courseTagService.createAllCourseTags(courseTags);
        });
        assertEquals("Course tag" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
    }

}
