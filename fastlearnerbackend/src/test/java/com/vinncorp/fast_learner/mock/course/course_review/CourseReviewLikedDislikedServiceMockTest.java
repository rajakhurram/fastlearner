package com.vinncorp.fast_learner.mock.course.course_review;

import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.course.course_review.CourseReviewLikedDisliked;
import com.vinncorp.fast_learner.repositories.course.course_review.CourseReviewLikedDislikedRepository;
import com.vinncorp.fast_learner.services.course.course_review.CourseReviewLikedDislikedService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

public class CourseReviewLikedDislikedServiceMockTest {

    private static Long COURSE_REVIEW_ID = 1L;
    @Mock
    private CourseReviewLikedDislikedRepository repo;
    @InjectMocks
    private CourseReviewLikedDislikedService service;
    private CourseReviewLikedDisliked courseReviewLikedDisliked;

    @BeforeEach
    public void init(){
        MockitoAnnotations.openMocks(this);
        courseReviewLikedDisliked = new CourseReviewLikedDisliked();
    }

    @Test
    @DisplayName("Test save - Success")
    void testSave_Success() {
        when(repo.save(courseReviewLikedDisliked)).thenReturn(courseReviewLikedDisliked);
        service.save(courseReviewLikedDisliked);
        verify(repo, times(1)).save(courseReviewLikedDisliked);
    }

    @Test
    @DisplayName("Test save - Null CourseReviewLikedDisliked Object")
    void testSave_NullObject() {
        service.save(null);
        verify(repo, never()).save(any(CourseReviewLikedDisliked.class));
    }

    @Test
    @DisplayName("Test save - Repository Exception Handling")
    void testSave_RepositoryExceptionHandling() {
        doThrow(new RuntimeException("Database error")).when(repo).save(courseReviewLikedDisliked);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.save(courseReviewLikedDisliked);
        });
        assertEquals("Database error", exception.getMessage());
        verify(repo, times(1)).save(courseReviewLikedDisliked);
    }

    @Test
    @DisplayName("Test getByCourseReviewId - Success")
    void testGetByCourseReviewId_Success() {
        CourseReviewLikedDisliked mockedResponse = new CourseReviewLikedDisliked();
        when(repo.findByCourseReviewIdAndCreatedBy(COURSE_REVIEW_ID, UserTestData.userData().getId())).thenReturn(mockedResponse);
        CourseReviewLikedDisliked result = service.getByCourseReviewId(COURSE_REVIEW_ID, UserTestData.userData().getId());
        assertNotNull(result);
        assertEquals(mockedResponse, result);
        verify(repo, times(1)).findByCourseReviewIdAndCreatedBy(COURSE_REVIEW_ID, UserTestData.userData().getId());
    }

    @Test
    @DisplayName("Test getByCourseReviewId - No Data Found")
    void testGetByCourseReviewId_NoDataFound() {
        when(repo.findByCourseReviewIdAndCreatedBy(COURSE_REVIEW_ID, UserTestData.userData().getId())).thenReturn(null);
        CourseReviewLikedDisliked result = service.getByCourseReviewId(COURSE_REVIEW_ID, UserTestData.userData().getId());
        assertNull(result);
        verify(repo, times(1)).findByCourseReviewIdAndCreatedBy(COURSE_REVIEW_ID, UserTestData.userData().getId());
    }

    @Test
    @DisplayName("Test getByCourseReviewId - Repository Exception Handling")
    void testGetByCourseReviewId_RepositoryExceptionHandling() {
        when(repo.findByCourseReviewIdAndCreatedBy(COURSE_REVIEW_ID, UserTestData.userData().getId())).thenThrow(new RuntimeException("Database error"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getByCourseReviewId(COURSE_REVIEW_ID, UserTestData.userData().getId());
        });
        assertEquals("Database error", exception.getMessage());
        verify(repo, times(1)).findByCourseReviewIdAndCreatedBy(COURSE_REVIEW_ID, UserTestData.userData().getId());
    }

}
