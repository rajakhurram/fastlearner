package com.vinncorp.fast_learner.dtos.course;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelatedCourses {

    private Long courseId;
    private Long instructorId;
    private String title;
    private String courseUrl;
    private String level;
    private Integer courseDuration;
    private String userProfileUrl;
    private String description;
    private String categoryName;
    private String instructorName;
    private String instructorImage;
    private String thumbnail;
    private double review;
    private int totalReviewers;
    private int totalTopics;
    private boolean isFavourite;
    private boolean isEnrolled;
    private String courseType;
    private Boolean isAlreadyBought;
    private Double price;
    private String contentType;
    private Long testTotalQuestion;

    public static List<RelatedCourses> from(List<Tuple> tuples) {
        return tuples.stream().map(e -> RelatedCourses.builder()
                .courseId((Long) e.get("course_id"))
                .instructorId((Long) e.get("instructor_id"))
                .title((String) e.get("course_name"))
                .courseDuration(e.get("duration") != null ? Integer.parseInt("" + e.get("duration")) : 0)
                .courseUrl((String) e.get("course_url"))
                .level((String) e.get("course_level"))
                .userProfileUrl((String) e.get("profile_url"))
                .description((String) e.get("description"))
                .thumbnail((String) e.get("thumbnail"))
                .instructorName((String) e.get("instructor_name"))
                .categoryName((String) e.get("course_category"))
                .review(e.get("avg_course_rating") != null ? (Double) e.get("avg_course_rating") : 0.0)
                .totalReviewers(e.get("total_reviews") != null ? Integer.parseInt("" + e.get("total_reviews")) : 0)
                .totalTopics(e.get("total_topics") != null ? Integer.parseInt("" + e.get("total_topics")) : 0)
//                .isEnrolled(e.get("is_enrolled") != null && Boolean.parseBoolean("" + e.get("is_enrolled")))
//                .isFavourite(e.get("is_favourite") != null && Boolean.parseBoolean("" + e.get("is_favourite")))
                .instructorImage((String) e.get("profile_picture"))
                .price((Double) e.get("price"))
                .courseType((String) e.get("course_type"))
                .isAlreadyBought(e.get("already_bought") != null && Boolean.parseBoolean(e.get("already_bought").toString()))
                .contentType((String) e.get("content_type"))
                .testTotalQuestion(e.getElements().stream().anyMatch(column -> "test_total_question".equals(column.getAlias())) ? Long.valueOf(String.valueOf((BigDecimal) e.get("test_total_question"))) : null)
                .build()).collect(Collectors.toList());
    }

}

