package com.vinncorp.fast_learner.dtos.enrollment;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrolledStudentValue {

    private Date monthDate;
    private Long value;
}
