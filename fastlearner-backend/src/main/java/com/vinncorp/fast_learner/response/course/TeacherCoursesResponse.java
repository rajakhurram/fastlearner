package com.vinncorp.fast_learner.response.course;

import com.vinncorp.fast_learner.dtos.course.TeacherCourses;
import com.vinncorp.fast_learner.util.TimeUtil;
import jakarta.persistence.Tuple;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class TeacherCoursesResponse {

    private List<TeacherCourses> courses;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private long totalPages;


    public static TeacherCoursesResponse from(Page<Tuple> courses) {
        TeacherCoursesResponse res = new TeacherCoursesResponse();
        res.setCourses(courses.stream()
                .map(e -> {
                    String courseType = (String) e.get("course_type");

                    return TeacherCourses.builder()
                            .id((Long) e.get("id"))
                            .title((String) e.get("title"))
                            .createdDate((Date) e.get("created_date"))
                            .duration(Objects.nonNull(e.get("total_duration_in_sec")) ?
                                    TimeUtil.convertDurationToString(Long.parseLong("" + e.get("total_duration_in_sec"))) : "-")
                            .thumbnail((String) e.get("thumbnail"))
                            .lastUpdated((Date) e.get("last_mod_date"))
                            .totalStudents((Long) e.get("no_of_students"))
                            .courseStatus((String) e.get("course_status"))
                            .courseProgress((String) e.get("course_progress"))
                            .courseType(
                                    courseType != null ?
                                            (courseType.equals("STANDARD_COURSE") ? "STANDARD" :
                                                    (courseType.equals("PREMIUM_COURSE") ? "PREMIUM" :
                                                            (courseType.equals("FREE_COURSE") ? "Free" : null)))
                                            : null
                            )
                            .contentType((String) e.get("content_type"))
                         .testTotalQuestion(e.getElements().stream().anyMatch(column -> "test_total_question".equals(column.getAlias())) ? Long.valueOf(String.valueOf((BigDecimal) e.get("test_total_question"))) : null)
                            .build();
                })
                .toList());

        res.setPageNo(courses.getPageable().getPageNumber());
        res.setPageSize(courses.getPageable().getPageSize());
        res.setTotalElements(courses.getTotalElements());
        res.setTotalPages(courses.getTotalPages());

        return res;
    }
}
