package com.vinncorp.fast_learner.dtos.super_admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminEnrollmentListDto {
    private String enrollmentId;     // ENR-001
    private Long rawId;
    private String studentName;
    private String studentEmail;
    private Long courseId;
    private String courseTitle;
    private Date enrolledDate;
    private int progressPercent;     // 0-100
    private String status;           // Active, Completed, Inactive
}
