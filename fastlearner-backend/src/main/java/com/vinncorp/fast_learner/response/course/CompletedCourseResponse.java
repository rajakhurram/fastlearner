package com.vinncorp.fast_learner.response.course;

import com.vinncorp.fast_learner.dtos.course.CourseFeedback;
import com.vinncorp.fast_learner.dtos.section.SectionDetail;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompletedCourseResponse {

    private Long courseId;

    private String title;
    private Long userId;
    private String creatorName;
    private String profilePicture;
    private String userProfileUrl;
    private String courseThumbnailUrl;
    private Date completedCourseDate;
    private String courseUrl;




}
