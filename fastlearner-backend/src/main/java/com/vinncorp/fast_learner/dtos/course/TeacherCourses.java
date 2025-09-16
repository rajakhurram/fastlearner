package com.vinncorp.fast_learner.dtos.course;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherCourses {
    private Long id;
    private Date createdDate;
    private String thumbnail;
    private String title;
    private String duration;
    private Long totalStudents;
    private Date lastUpdated;
    private String courseProgress;
    private String courseStatus;
    private String courseType;
    private String contentType;
    private Long testTotalQuestion;
}
