package com.vinncorp.fast_learner.mock.dashboard;

import com.vinncorp.fast_learner.dtos.enrollment.EnrolledStudentDto;
import com.vinncorp.fast_learner.dtos.user.user_profile_visit.UserProfileVisitDto;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.response.dashboard.DashboardStatsResponse;
import com.vinncorp.fast_learner.services.dashboard.DashboardService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.payout.IInstructorSalesService;
import com.vinncorp.fast_learner.services.user.IUserCourseProgressService;
import com.vinncorp.fast_learner.services.user.IUserProfileService;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.util.Message;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DashboardServiceMockTest {

    private static String EMAIL = "qasim@mailinator.com";
    @Mock
    private IEnrollmentService enrollmentService;
    @Mock
    private IUserProfileService userProfileService;
    @Mock
    private UserService userService;
    @Mock
    private IUserCourseProgressService userCourseProgressService;
    @Mock
    private IInstructorSalesService instructorSalesService;
    @InjectMocks
    private DashboardService dashboardService;
    private EnrolledStudentDto mockEnrolledStudentDto;
    private UserProfileVisitDto mockUserProfileVisitDto;
    private Tuple mockTuple;

    @BeforeEach
    public void setup() {
       MockitoAnnotations.openMocks(this);
    }

    @DisplayName("Fetch with valid parameter")
    @Test
    public void testFetchStats_Success() throws EntityNotFoundException, BadRequestException {

        mockEnrolledStudentDto = new EnrolledStudentDto();
        mockEnrolledStudentDto.setTotalValue(0);
        mockEnrolledStudentDto.setValues(new ArrayList<>());

        mockUserProfileVisitDto = new UserProfileVisitDto();
        mockUserProfileVisitDto.setTotalValue(0L);
        mockUserProfileVisitDto.setValues(new ArrayList<>());

        mockTuple = mock(Tuple.class);
        when(mockTuple.get("enrolled")).thenReturn(200L);
        when(mockTuple.get("completed")).thenReturn(150L);

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        when(enrollmentService.totalNoOfEnrolledStudent(anyString(), anyLong())).thenReturn(mockEnrolledStudentDto);
        when(userProfileService.findNoOfUsersVisitedProfileBy(anyString(), anyLong())).thenReturn(mockUserProfileVisitDto);
        when(userCourseProgressService.fetchCourseCompletion(anyString(), anyLong())).thenReturn(mockTuple);
        when(instructorSalesService.fetchMonthlyOrYearlySales(anyString(), anyLong())).thenReturn(10000.0);

        Message<DashboardStatsResponse> response = dashboardService.fetchStats("Monthly", EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Fetched dashboard stats data.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(75.0, response.getData().getCompletionRate(), 0.01);
        assertEquals(200L, response.getData().getTotalParticipants());
        assertEquals(10000.0, response.getData().getRevenue(), 0.01);
        assertEquals(mockEnrolledStudentDto, response.getData().getTotalStudents());
        assertEquals(mockUserProfileVisitDto, response.getData().getTotalProfileVisits());
    }

    @DisplayName("Fetch with invalid parameter")
    @Test
    public void testFetchStats_InvalidFilter() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            dashboardService.fetchStats("InvalidFilter", EMAIL);
        });

        assertEquals("Filter should be This month, Last month, This year or Last year only.", exception.getMessage());
    }

    @DisplayName("Fetch with invalid user")
    @Test
    public void testFetchStats_UserNotFound() throws EntityNotFoundException {
        when(userService.findByEmail(anyString())).thenThrow(new EntityNotFoundException("User not found"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            dashboardService.fetchStats("Monthly", EMAIL);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @DisplayName("Fetch with no course completion")
    @Test
    public void testFetchStats_NoCourseCompletionData() throws EntityNotFoundException, BadRequestException {

        mockEnrolledStudentDto = new EnrolledStudentDto();
        mockEnrolledStudentDto.setTotalValue(0);
        mockEnrolledStudentDto.setValues(new ArrayList<>());

        mockUserProfileVisitDto = new UserProfileVisitDto();
        mockUserProfileVisitDto.setTotalValue(0L);
        mockUserProfileVisitDto.setValues(new ArrayList<>());

        when(userService.findByEmail(anyString())).thenReturn(UserTestData.userData());
        when(enrollmentService.totalNoOfEnrolledStudent(anyString(), anyLong())).thenReturn(mockEnrolledStudentDto);
        when(userProfileService.findNoOfUsersVisitedProfileBy(anyString(), anyLong())).thenReturn(mockUserProfileVisitDto);
        when(userCourseProgressService.fetchCourseCompletion(anyString(), anyLong())).thenReturn(null);
        when(instructorSalesService.fetchMonthlyOrYearlySales(anyString(), anyLong())).thenReturn(10000.0);

        Message<DashboardStatsResponse> response = dashboardService.fetchStats("Monthly", EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Fetched dashboard stats data.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(0.0, response.getData().getCompletionRate());
        assertEquals(0, response.getData().getTotalParticipants());
        assertEquals(10000.0, response.getData().getRevenue());
        assertEquals(mockEnrolledStudentDto, response.getData().getTotalStudents());
        assertEquals(mockUserProfileVisitDto, response.getData().getTotalProfileVisits());
    }

}
