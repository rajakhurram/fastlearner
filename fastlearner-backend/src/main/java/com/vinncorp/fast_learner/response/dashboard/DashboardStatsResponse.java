package com.vinncorp.fast_learner.response.dashboard;

import com.vinncorp.fast_learner.dtos.enrollment.EnrolledStudentDto;
import com.vinncorp.fast_learner.dtos.user.user_profile_visit.UserProfileVisitDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardStatsResponse {

    private Double revenue;
    private EnrolledStudentDto totalStudents;
    private UserProfileVisitDto totalProfileVisits;
    private Double completionRate;
    private Long totalParticipants;
}
