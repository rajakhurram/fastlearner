package com.vinncorp.fast_learner.response.course;

import com.vinncorp.fast_learner.models.tag.Tag;
import com.vinncorp.fast_learner.models.course.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailForUpdateResponse {
    private Long courseId;
    private String title;
    private String about;
    private String courseDescription;
    private List<String> prerequisite;
    private List<String> courseOutcome;
    private String courseThumbnailUrl;
    private String previewVideoUrl;
    private String previewVideoVttContent;

    private List<Tag> tags;
    private Long levelId;
    private String level;
    private Long categoryId;
    private String categoryName;
    private String courseProgress;
    private boolean certificateEnabled;
    private String courseUrl;
    private String courseType;
    private Double price;
    private String contentType;

    public static CourseDetailForUpdateResponse from(Course course, List<Tag> tags) {
        return CourseDetailForUpdateResponse.builder()
                .courseId(course.getId())
                .title(course.getTitle())
                .about(course.getAbout())
                .courseDescription(course.getDescription())
                .prerequisite(Objects.nonNull(course.getPrerequisite()) ? List.of(course.getPrerequisite().split("~")) : null)
                .courseOutcome(Objects.nonNull(course.getCourseOutcome()) ? List.of(course.getCourseOutcome().split("~")) : null)
                .courseThumbnailUrl(course.getThumbnail())
                .previewVideoUrl(course.getPreviewVideoURL())
                .previewVideoVttContent(course.getPreviewVideoVttContent())
                .tags(tags)
                .levelId(Objects.isNull(course.getCourseLevel()) ? null : course.getCourseLevel().getId())
                .level(Objects.isNull(course.getCourseLevel()) ? null :course.getCourseLevel().getName())
                .categoryId(Objects.isNull(course.getCourseCategory()) ? null :course.getCourseCategory().getId())
                .categoryName(Objects.isNull(course.getCourseCategory()) ? null :course.getCourseCategory().getName())
                .courseProgress(course.getCourseProgress())
                .certificateEnabled(course.getCertificateEnabled())
                .courseType(Objects.nonNull(course.getCourseType()) ? course.getCourseType().name() : null)
                .price(course.getPrice())
                .contentType(course.getContentType().name())
                .build();
    }
}
