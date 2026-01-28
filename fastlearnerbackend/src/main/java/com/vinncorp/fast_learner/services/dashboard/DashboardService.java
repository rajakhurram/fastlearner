package com.vinncorp.fast_learner.services.dashboard;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.dtos.enrollment.EnrolledStudentDto;
import com.vinncorp.fast_learner.dtos.user.user_profile_visit.UserProfileVisitDto;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.response.dashboard.DashboardStatsResponse;
import com.vinncorp.fast_learner.services.payout.IInstructorSalesService;
import com.vinncorp.fast_learner.services.user.IUserCourseProgressService;
import com.vinncorp.fast_learner.services.user.IUserProfileService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService{

    private final IEnrollmentService enrollmentService;
    private final IUserProfileService userProfileService;
    private final IUserService userService;
    private final IUserCourseProgressService userCourseProgressService;
    private final IInstructorSalesService instructorSalesService;

    @Override
    public Message<DashboardStatsResponse> fetchStats(String filterBy, String email) throws EntityNotFoundException, BadRequestException {
        log.info("Fetching dashboard stats.");
        if(
            StringUtils.isEmpty(filterBy)
            || (!StringUtils.equalsIgnoreCase(filterBy, "Monthly")
            && !StringUtils.equalsIgnoreCase(filterBy, "Yearly")
            && !StringUtils.equalsIgnoreCase(filterBy, "PREVIOUS_MONTH")
            && !StringUtils.equalsIgnoreCase(filterBy, "PREVIOUS_YEAR"))) {
            throw new BadRequestException("Filter should be This month, Last month, This year or Last year only.");
        }
        
        User user = userService.findByEmail(email);
        EnrolledStudentDto totalStudents = enrollmentService.totalNoOfEnrolledStudent(filterBy.toLowerCase(), user.getId());
        UserProfileVisitDto totalProfileVisits = userProfileService.findNoOfUsersVisitedProfileBy(filterBy, user.getId());
        Tuple tuple = userCourseProgressService.fetchCourseCompletion(filterBy.toLowerCase(), user.getId());
        DashboardStatsResponse dashboardStatsResponse = new DashboardStatsResponse();

        if (tuple != null) {
            long enrolled = (Long) tuple.get("enrolled");
            long completed = (Long) tuple.get("completed");
            if(enrolled != 0)
                dashboardStatsResponse.setCompletionRate(((double) completed / (double) enrolled) * 100);

            else
                dashboardStatsResponse.setCompletionRate((double) 0L);
            dashboardStatsResponse.setTotalParticipants(enrolled);
        }
        else {
            dashboardStatsResponse.setCompletionRate(0.0);
            dashboardStatsResponse.setTotalParticipants(0L);
        }
        dashboardStatsResponse.setRevenue(instructorSalesService.fetchMonthlyOrYearlySales(filterBy.toLowerCase(), user.getId()));
        dashboardStatsResponse.setTotalStudents(totalStudents);
        dashboardStatsResponse.setTotalProfileVisits(totalProfileVisits);

        return new Message<DashboardStatsResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Fetched dashboard stats data.")
                .setData(dashboardStatsResponse);
    }
}
