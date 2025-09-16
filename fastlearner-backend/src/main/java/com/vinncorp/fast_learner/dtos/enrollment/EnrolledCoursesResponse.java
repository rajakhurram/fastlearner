package com.vinncorp.fast_learner.dtos.enrollment;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnrolledCoursesResponse {
    private Long studentId;
    private String fullName;
    private String email;
    private Long courseId;
    private String title;
    private String url;
    private Date enrolledDate;
    private Boolean isActive;


}

