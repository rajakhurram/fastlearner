package com.vinncorp.fast_learner.dtos.affiliate;

import com.vinncorp.fast_learner.response.course.CourseDetailByType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AffiliatePremiumCourse {
    private Double reward;
    private List<CourseDetailByType> courseDetailsByType;
}
