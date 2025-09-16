package com.vinncorp.fast_learner.response.course;

import jakarta.persistence.Tuple;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
public class CourseByCategoryResponse {

    private Long courseId;
    private String categoryName;
    private String title;
    private String courseUrl;
    private String level;
    private String courseDescription;
    private Long creatorId;
    private String creatorName;
    private List<String> prerequisite;
    private List<String> courseOutcome;
    private Integer courseDuration;
    private Integer noOfTopics;
    private Double review;
    private int noOfReviewers;
    private String courseThumbnailUrl;
    private String previewVideoUrl;
    private String previewVideoVttContent;
    private String instructorImage;
    private String userProfileUrl;
    private boolean isEnrolled;
    private boolean isFavourite;
    private boolean hasCertificate;
    private String metaHeading;
    private String metaTitle;
    private String metaDescription;
    private String courseType;
    private Double price;
    private Boolean isAlreadyBought;
    private String contentType;
    private Long testTotalQuestion;



    public static List<CourseByCategoryResponse> from(List<Tuple> data) {

        return data.stream().map(e -> {
            CourseByCategoryResponse d = new CourseByCategoryResponse();
            d.setCourseId((Long) e.get("course_id"));
            d.setTitle((String) e.get("course_title"));
            d.setCourseUrl((String) e.get("course_url"));

            String prerequisite = (String) e.get("prerequisite");
            if (Objects.nonNull(prerequisite)) {
                d.setPrerequisite(Arrays.asList(prerequisite.split("~")));
            }

            String courseOutcome = (String) e.get("course_outcome");
            if (Objects.nonNull(courseOutcome)) {
                d.setCourseOutcome(Arrays.asList(courseOutcome.split("~")));
            }

            if (e.getElements().stream().anyMatch(column -> "is_favourite".equals(column.getAlias()))) {
                d.setFavourite(e.get("is_favourite") != null && Boolean.parseBoolean("" + e.get("is_favourite")));
            }

            if (e.getElements().stream().anyMatch(column -> "is_enrolled".equals(column.getAlias()))) {
                d.setEnrolled(e.get("is_enrolled") != null && Boolean.parseBoolean("" + e.get("is_enrolled")));
            }

            if (e.getElements().stream().anyMatch(column -> "content_type".equals(column.getAlias()))) {
                d.setContentType((String) e.get("content_type"));
            }

            d.setTestTotalQuestion(e.getElements().stream().anyMatch(column -> "test_total_question".equals(column.getAlias())) ? Long.valueOf(String.valueOf((BigDecimal) e.get("test_total_question"))) : null);


            d.setLevel((String) e.get("level"));
            d.setCourseDescription((String) e.get("course_description"));
            d.setCategoryName((String) e.get("category"));
            d.setCourseThumbnailUrl((String) e.get("course_thumbnail"));
            //d.setCourseDuration(e.get("course_duration_in_hours") != null ? Integer.parseInt("" +  e.get("course_duration_in_hours")): 0 );
            d.setPreviewVideoVttContent((String) e.get("preview_video_vtt_content"));
            d.setCreatorName((String) e.get("full_name"));
            d.setCreatorId((Long) e.get("user_id"));
            d.setInstructorImage((String) e.get("profile_picture"));
            if (e.getElements().stream().anyMatch(column -> "profile_url".equals(column.getAlias()))) {
                d.setUserProfileUrl((String) e.get("profile_url"));
            }
            d.setReview((Double) e.get("max_rating"));
            d.setNoOfReviewers(e.get("total_reviews") != null ? Integer.parseInt("" +  e.get("total_reviews")) : 0);
            d.setPreviewVideoUrl((String) e.get("video_url"));
            d.setHasCertificate(Objects.nonNull(e.get("has_certificate")) && Boolean.parseBoolean("" + e.get("has_certificate")));
            d.setMetaTitle((String) e.get("meta_title"));
            d.setMetaHeading((String) e.get("meta_heading"));
            d.setMetaDescription((String) e.get("meta_description"));
            d.setCourseType((String) e.get("course_type"));
            d.setPrice((Double) e.get("price"));
            d.setIsAlreadyBought(
                    e.get("already_bought") != null && Boolean.parseBoolean(e.get("already_bought").toString())
            );

            return d;
        }).collect(Collectors.toList());
    }
}
