package com.vinncorp.fast_learner.response.course;

import com.vinncorp.fast_learner.dtos.course.CourseFeedback;
import com.vinncorp.fast_learner.dtos.section.SectionDetail;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDetailResponse {

    private Long courseId;
    private String categoryName;
    private String title;
    private String about;
    private String courseDescription;
    private String level;
    private Long userId;
    private String creatorName;
    private String profilePicture;
    private String userProfileUrl;
    private String headline;
    private String aboutMe;
    private int totalCourses;
    private int totalStudents;
    private int totalEnrolled;
    private Date lastUpdate;
    private Integer courseDuration;
    private Integer noOfTopics;
    private List<String> prerequisite;
    private List<String> courseOutcome;
    private double review;
    private int noOfReviewers;
    private String courseThumbnailUrl;
    private String previewVideoUrl;
    private String previewVideoVttContent;
    private Boolean isFavourite;
    private Boolean isEnrolled;
    private List<SectionDetail> sectionDetails;
    private CourseFeedback courseFeedback;
    private List<String> tags;
    private Boolean hasCertificate;
    private String metaHeading;
    private String metaTitle;
    private String metaDescription;
    private Date createdDate;
    private Boolean isAlreadyBought;
    private String courseUrl;
    private String courseType;
    private Double price;
    private String contentType;
    private Long testTotalQuestion;



    public static CourseDetailResponse fromCourseData(Tuple courses) {
        Boolean isAlreadyBought = false;

        try {
            isAlreadyBought = courses.get("already_bought") != null && Boolean.parseBoolean(courses.get("already_bought").toString());
        } catch (Exception e) {
            // Handle or log the exception if needed
        }

        return CourseDetailResponse.builder()
                .courseId((Long) courses.get("course_id"))
                .title((String) courses.get("title"))
                .courseDescription((String) courses.get("description"))
                .level((String) courses.get("course_level"))
                .categoryName((String) courses.get("category_name"))
                .courseThumbnailUrl(courses.get("course_thumbnail")==null?null:(String) courses.get("course_thumbnail"))
                .isAlreadyBought(isAlreadyBought)
                .courseDuration(courses.get("duration") != null ? Integer.parseInt("" + courses.get("duration")) : 0)
                .userId((Long) courses.get("user_id"))
                .creatorName((String) courses.get("full_name"))
                .profilePicture((String) courses.get("profile_picture"))
                .aboutMe((String) courses.get("about_me"))
                .headline((String) courses.get("headline"))
                .review(courses.get("max_rating")==null ?0:(Double) courses.get("max_rating"))
                .noOfReviewers(courses.get("total_reviews") == null ? 0 : Integer.parseInt("" + courses.get("total_reviews")))
                .userProfileUrl((String) courses.get("profile_url"))
                .createdDate(courses.get("created_date") != null ? (Date) courses.get("created_date") : null)
                .courseUrl(courses.get("course_url") != null ? (String) courses.get("course_url") : null)
                .courseType((String) courses.get("course_type"))
                .price((Double) courses.get("price"))
                .contentType(courses.getElements().stream().anyMatch(column -> "content_type".equals(column.getAlias())) ? (String) courses.get("content_type"): null)
                .testTotalQuestion(courses.getElements().stream().anyMatch(column -> "test_total_question".equals(column.getAlias())) ? Long.valueOf(String.valueOf((BigDecimal) courses.get("test_total_question"))) : null)
                .build();

    }

    public static CourseDetailResponse from(Tuple e) {
        List<String> prerequisites = null;
        List<String> courseOutcomes = null;

        Boolean isAlreadyBought = false;

        try {
            isAlreadyBought = e.get("already_bought") != null && Boolean.parseBoolean(e.get("already_bought").toString());
        } catch (Exception es) {
            // Handle or log the exception if needed
        }
        String prerequisite = (String) e.get("prerequisite");
        if (Objects.nonNull(prerequisite)) {
            prerequisites = Arrays.asList(prerequisite.split("~"));
        }

        String courseOutcome = (String) e.get("course_outcome");
        if (Objects.nonNull(courseOutcome)) {
            courseOutcomes = Arrays.asList(courseOutcome.split("~"));
        }

        return CourseDetailResponse.builder()
                .courseId((Long) e.get("course_id"))
                .title((String)e.get("course_title"))
                .courseDescription((String)e.get("course_description"))
                .about((String)e.get("about"))
                .level((String) e.get("level"))
                .categoryName((String)e.get("category"))
                .courseThumbnailUrl((String) e.get("course_thumbnail"))
                .courseDuration(e.get("course_duration_in_hours") != null ? Integer.parseInt("" + e.get("course_duration_in_hours")) : 0)
                .userId((Long) e.get("user_id"))
                .creatorName((String)e.get("full_name"))
                .profilePicture((String)e.get("profile_picture"))
                .userProfileUrl((String) e.get("profile_url"))
                .aboutMe((String)e.get("about_me"))
                .headline((String)e.get("headline"))
                .review((Double) e.get("max_rating"))
                .noOfReviewers(e.get("total_reviews") == null ? 0 : Integer.parseInt("" + e.get("total_reviews")) )
                .previewVideoUrl((String)e.get("video_url"))
                .previewVideoVttContent((String) e.get("preview_video_vtt_content"))
                .prerequisite(prerequisites)
                .courseOutcome(courseOutcomes)
                .isEnrolled(Boolean.parseBoolean("" + e.get("is_enrolled")))
                .isFavourite(Boolean.parseBoolean("" + e.get("is_favourite")))
                .totalStudents(e.get("total_students") != null ? Integer.parseInt("" + e.get("total_students")) : 0)
                .lastUpdate((Date) e.get("last_mod_date"))
                .metaDescription((String) e.get(("meta_description")))
                .metaHeading((String) e.get("meta_heading"))
                .metaTitle((String) e.get("meta_title"))
                .hasCertificate(Objects.nonNull(e.get("has_certificate")) && Boolean.parseBoolean("" + e.get("has_certificate")))
                .courseType((String) e.get("course_type"))
                .price((Double) e.get("price"))
                .isAlreadyBought(isAlreadyBought)
                .contentType((String) e.get("content_type"))
                .testTotalQuestion(e.getElements().stream().anyMatch(column -> "test_total_question".equals(column.getAlias())) ? Long.valueOf(String.valueOf((BigDecimal) e.get("test_total_question"))) : null)
                .build();
    }
}
