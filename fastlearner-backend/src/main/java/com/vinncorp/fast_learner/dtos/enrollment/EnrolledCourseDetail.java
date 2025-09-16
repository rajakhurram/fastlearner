package com.vinncorp.fast_learner.dtos.enrollment;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnrolledCourseDetail {
    private Long courseId;
    private String title;
    private String level;
    private String courseUrl;
    private String userProfileUrl;
    private String description;
    private String courseThumbnailUrl;
    private String categoryName;
    private double review;
    private int noOfReviewers;
    private Long userId;
    private String creatorName;
    private String profilePicture;
    private Integer courseDuration;
    private Double courseProgress;
    private Long noOfTopics;
    private int status;
    private boolean isFavourite;
    private boolean isEnrolled;
    private String courseType;
    private Double price;
    private Boolean isAlreadyBought;
    private Boolean certificateEnabled;
    private String contentType;
    private Long testTotalQuestion;

    public static List<EnrolledCourseDetail> from(Page<Tuple> pagedData) {
        return pagedData.stream().map(e -> EnrolledCourseDetail.builder()

                        .courseId((Long) e.get("course_id"))
                        .title((String) e.get("course_title"))
                        .courseUrl((String) e.get("course_url"))
                        .level((String) e.get("course_level"))
                        .description((String) e.get("course_description"))
                        .categoryName((String) e.get("category"))
                        .courseThumbnailUrl((String) e.get("course_thumbnail"))
                        .courseDuration(e.get("duration") != null ? Integer.parseInt("" + e.get("duration")) : 0)
                        .certificateEnabled((Boolean) e.get("certificate_enabled"))
                        .userId((Long) e.get("user_id"))
                        .creatorName((String) e.get("full_name"))
                        .profilePicture((String) e.get("profile_picture"))
                        .userProfileUrl((String) e.get("profile_url"))
                        .review((Double) e.get("max_rating"))
                        .noOfReviewers(e.get("total_reviews") != null ? Integer.parseInt(""+ e.get("total_reviews")) : 0)
                        .isFavourite(e.get("is_favourite") != null && Boolean.parseBoolean("" + e.get("is_favourite")))
                        .isEnrolled(e.get("is_enrolled") != null && Boolean.parseBoolean("" + e.get("is_enrolled")))
                        .courseType((String) e.get("course_type"))
                        .price((Double) e.get("price"))
                        .isAlreadyBought(e.get("already_bought") != null && (e.get("already_bought") instanceof Boolean
                                ? (Boolean) e.get("already_bought")
                                : Boolean.parseBoolean("" + e.get("already_bought"))))
                        .contentType((String) e.get("content_type"))
                        .testTotalQuestion(e.getElements().stream().anyMatch(column -> "test_total_question".equals(column.getAlias())) ? (Long) e.get("test_total_question") : null)
                        .noOfTopics(e.get("no_of_topics") != null ? (Long) e.get("no_of_topics") : 0)
                        .build())
                .collect(Collectors.toList());
    }
}
