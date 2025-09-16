package com.vinncorp.fast_learner.es_dto;

import com.vinncorp.fast_learner.es_models.Course;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchCourses {

    private String id;
    private Long courseId;
    private String title;
    private String courseUrl;
    private String thumbnail;

    public static List<SearchCourses> from(List<Course> content, ICourseService courseService) {
        return content.stream()
                .filter(course -> course.getCourseStatus().equalsIgnoreCase(CourseStatus.PUBLISHED.toString()))
                .map(e -> SearchCourses.builder()
                        .id(e.getId())
                        .courseId(e.getDbId())
                        .title(e.getTitle())
                        .courseUrl(e.getCourseUrl())
                        .thumbnail(e.getThumbnail())
                        .build())
                .collect(Collectors.toList());
    }
}
