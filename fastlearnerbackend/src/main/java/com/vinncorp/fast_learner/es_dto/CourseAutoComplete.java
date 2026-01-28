package com.vinncorp.fast_learner.es_dto;

import com.vinncorp.fast_learner.dtos.user.user_profile_visit.InstructorProfileSearchDto;
import com.vinncorp.fast_learner.es_models.Course;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseAutoComplete {

    private List<SearchCourses> searchCourses;
    private List<InstructorProfileSearchDto> instructorProfiles;
    private String id;
    private Long courseId;
    private String title;

    public static List<CourseAutoComplete> from(List<Course> content) {
        return content.stream()
                .filter(c -> c.getCourseStatus().equals(CourseStatus.PUBLISHED))
                .map(e -> CourseAutoComplete.builder()
                        .id(e.getId())
                        .courseId(e.getDbId())
                        .title(e.getTitle())
                        .build())
                .collect(Collectors.toList());
    }
}
