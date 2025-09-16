package com.vinncorp.fast_learner.dtos.favourite_course;


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
public class FavouriteCourseDetail {
    private Long courseId;
    private String title;
    private String courseUrl;
    private String userProfileUrl;
    private String description;
    private String level;
    private String courseThumbnailUrl;
    private String categoryName;
    private double review;
    private int noOfReviewers;
    private Long userId;
    private String creatorName;
    private String profilePicture;
    private Integer courseDuration;
    private int noOfTopics;
    private int status;
    private boolean isFavourite;
    private boolean isEnrolled;
    private String courseType;
    private Double price;
    private Boolean isAlreadyBought;
    private String contentType;
    private Long testTotalQuestion;

    public static List<FavouriteCourseDetail> from(Page<Tuple> pagedData) {
        return pagedData.stream().map(e -> FavouriteCourseDetail.builder()
                        .courseId((Long) e.get("course_id"))
                        .title((String) e.get("course_title"))
                        .courseUrl((String) e.get("course_url"))
                        .level((String) e.get("course_level"))
                        .userProfileUrl((String) e.get("profile_url"))
                        .description((String) e.get("course_description"))
                        .categoryName((String) e.get("category"))
                        .courseThumbnailUrl((String) e.get("course_thumbnail"))
                        .courseDuration(e.get("duration") != null ? Integer.parseInt("" + e.get("duration")) : 0)
                        .userId((Long) e.get("user_id"))
                        .creatorName((String) e.get("full_name"))
                        .profilePicture((String) e.get("profile_picture"))
                        .review((Double) e.get("max_rating"))
                        .noOfTopics(e.get("total_topics") == null ? 0 : Integer.parseInt("" + e.get("total_topics")))
                        .noOfReviewers(e.get("total_reviews") != null ? Integer.parseInt("" + e.get("total_reviews")) : 0)
                        .isFavourite(Boolean.parseBoolean("" + e.get("is_favourite")))
                        .isEnrolled(Boolean.parseBoolean("" + e.get("is_enrolled")))
                        .courseType((String) e.get("course_type"))
                        .price((Double) e.get("price"))
                        .isAlreadyBought(e.get("already_bought") != null && (e.get("already_bought") instanceof Boolean
                                ? (Boolean) e.get("already_bought")
                                : Boolean.parseBoolean("" + e.get("already_bought"))))
                        .contentType((String) e.get("content_type"))
                        .testTotalQuestion(e.getElements().stream().anyMatch(column -> "test_total_question".equals(column.getAlias())) ? (Long) e.get("test_total_question") : null)
                        .build())
                .collect(Collectors.toList());
    }
}
