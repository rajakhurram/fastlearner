package com.vinncorp.fast_learner.dtos.course;

import com.vinncorp.fast_learner.models.course.Course;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CourseUrlDto {
    private String activeUrl;
    private Boolean canAccess;
    private Course course;
    private Boolean isAlreadyBought;
}
