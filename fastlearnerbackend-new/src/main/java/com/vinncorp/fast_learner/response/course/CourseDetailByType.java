package com.vinncorp.fast_learner.response.course;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CourseDetailByType {
    private Long courseId;
    private String courseTitle;
    private String courseDescription;
    private String thumbnailUrl;
    private String courseUrl;
    private Long activeUsersCount;

    public CourseDetailByType(Long courseId, String courseTitle, String courseDescription, String thumbnailUrl, String courseUrl) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.courseDescription = courseDescription;
        this.thumbnailUrl = thumbnailUrl;
        this.courseUrl = courseUrl;
    }
}
