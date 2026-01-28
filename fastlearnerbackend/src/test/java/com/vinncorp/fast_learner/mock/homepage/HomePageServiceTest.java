package com.vinncorp.fast_learner.mock.homepage;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.course.CourseCategory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.request.homepage.ViewAllReq;
import com.vinncorp.fast_learner.response.course.CourseDetailByPaginatedResponse;
import com.vinncorp.fast_learner.response.course.CourseDetailResponse;
import com.vinncorp.fast_learner.services.course.CourseCategoryService;
import com.vinncorp.fast_learner.services.course.CourseService;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.home_page.HomePageService;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.util.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class HomePageServiceTest {

    @InjectMocks
    private HomePageService homePageService;

    @Mock
    private UserService userService;

    @Mock
    private ICourseService courseService;

    @Mock
    private CourseCategoryService courseCategoryService;

    private Principal principal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        principal = mock(Principal.class);
    }

    @Test
    @DisplayName("Test getByType with valid categories and rating")
    void testGetByType_withValidCategoriesAndRating() throws EntityNotFoundException {
        // Arrange
        ViewAllReq viewAllReq = new ViewAllReq();
        viewAllReq.setCategoriesId(Arrays.asList(1L, 3L));
        viewAllReq.setCourseType("TRENDING");
        viewAllReq.setRating(4L);
        viewAllReq.setFeature(null);

        when(courseCategoryService.findById(1L)).thenReturn(new CourseCategory(1L, "Development","development",true));
        when(courseCategoryService.findById(3L)).thenReturn(new CourseCategory(3L, "Design","design",true));
        when(principal.getName()).thenReturn("test@example.com");
        User mockUser = new User();
        mockUser.setId(1L);
        when(userService.findByEmail("test@example.com")).thenReturn(mockUser);

        Message<CourseDetailByPaginatedResponse> expectedResponse = new Message<>();
        expectedResponse.setData(
                CourseDetailByPaginatedResponse.builder()
                        .data(List.of(CourseDetailResponse.builder().courseId(1L).build()))
                        .build()
        );
        when(courseService.findCoursesByFilter(anyList(), anyString(), anyString(), anyString(), anyString(),
                anyDouble(), anyDouble(), anyLong(), anyString(), any())).thenReturn(expectedResponse);

        // Act
        Message<CourseDetailByPaginatedResponse> response = homePageService.getByType(viewAllReq, 0, 10, principal);

        assertNotNull(response);

        verify(courseService).findCoursesByFilter(anyList(), anyString(), anyString(), anyString(), anyString(),
        anyDouble(), anyDouble(), anyLong(), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Test getByType with no categories and default rating")
    void testGetByTypeWithNoCategoriesAndDefaultRating() throws EntityNotFoundException {
        // Arrange
        ViewAllReq viewAllReq = new ViewAllReq();
        viewAllReq.setCategoriesId(Collections.emptyList());
        viewAllReq.setCourseType("FREE");
        viewAllReq.setRating(null);

        when(principal.getName()).thenReturn(null);

        Pageable pageable = PageRequest.of(0, 10);
        Message<CourseDetailByPaginatedResponse> expectedResponse = new Message<>();
        when(courseService.findCoursesByFilter(anyList(), anyString(), anyString(), anyString(), anyString(),
                anyDouble(), anyDouble(), anyLong(), anyString(), any(Pageable.class))).thenReturn(expectedResponse);

        // Act
        Message<CourseDetailByPaginatedResponse> response = homePageService.getByType(viewAllReq, 0, 10, null);

        // Assert
        assertNotNull(response);
        verify(courseService).findCoursesByFilter(anyList(), anyString(), anyString(), anyString(), anyString(),
                anyDouble(), anyDouble(), anyLong(), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Test getByType with invalid category ID")
    void testGetByTypeWithInvalidCategoryId() throws EntityNotFoundException {
        // Arrange
        ViewAllReq viewAllReq = new ViewAllReq();
        viewAllReq.setCategoriesId(Collections.singletonList(99L));

        when(courseCategoryService.findById(99L)).thenThrow(new EntityNotFoundException("Category not found"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> homePageService.getByType(viewAllReq, 0, 10, null));
        assertTrue(exception.getMessage().contains("Category with ID 99 not found"));
    }

    @Test
    @DisplayName("Test getByType with highest rating")
    void testGetByTypeWithHighestRating() throws EntityNotFoundException {
        // Arrange
        ViewAllReq viewAllReq = new ViewAllReq();
        viewAllReq.setCategoriesId(Arrays.asList(1L));
        viewAllReq.setRating(5L);

        when(courseCategoryService.findById(1L)).thenReturn(new CourseCategory(1L, "Development","development",true));

        Pageable pageable = PageRequest.of(0, 10);
        Message<CourseDetailByPaginatedResponse> expectedResponse = new Message<>();
        when(courseService.findCoursesByFilter(anyList(), anyString(), anyString(), anyString(), anyString(),
                anyDouble(), anyDouble(), anyLong(), anyString(), any(Pageable.class)))
                .thenReturn(expectedResponse);

        // Act
        Message<CourseDetailByPaginatedResponse> response = homePageService.getByType(viewAllReq, 0, 10, null);

        // Assert
        assertNotNull(response);
        verify(courseService).findCoursesByFilter(anyList(), anyString(), anyString(), anyString(), anyString(),
                anyDouble(), anyDouble(), anyLong(), anyString(), any(Pageable.class));
    }
}
