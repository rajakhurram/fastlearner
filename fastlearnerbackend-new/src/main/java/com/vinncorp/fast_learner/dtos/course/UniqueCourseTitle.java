package com.vinncorp.fast_learner.dtos.course;

import lombok.*;

@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UniqueCourseTitle {
    private Long courseId;
    private String courseTitle;
    private String courseUrl;
}
