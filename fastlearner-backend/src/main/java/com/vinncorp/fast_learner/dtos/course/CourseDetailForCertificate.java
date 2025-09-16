package com.vinncorp.fast_learner.dtos.course;


import com.vinncorp.fast_learner.util.TimeUtil;
import jakarta.persistence.Tuple;
import lombok.*;

import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDetailForCertificate {

    private Long courseId;
    private String courseTitle;
    private String coursePreviewImage;
    private Long totalSections;
    private Long totalTopics;
    private String courseDuration;
    private Long instructorId;
    private String instructorName;
    private String instructorImage;

    public static CourseDetailForCertificate from(Tuple data) {
        return CourseDetailForCertificate.builder()
                .courseId((Long) data.get("id"))
                .courseTitle((String) data.get("title"))
                .totalSections((Long) data.get("sections"))
                .totalTopics((Long) data.get("topics"))
                .courseDuration(Objects.nonNull(data.get("course_duration")) ? TimeUtil.convertDurationToString(Integer.parseInt("" + data.get("course_duration"))) : "NILL")
                .instructorId((Long) data.get("instructor_id"))
                .instructorName((String) data.get("instructor_name"))
                .instructorImage((String) data.get("instructor_image"))
                .coursePreviewImage((String) data.get("thumbnail"))
                .build();
    }
}
