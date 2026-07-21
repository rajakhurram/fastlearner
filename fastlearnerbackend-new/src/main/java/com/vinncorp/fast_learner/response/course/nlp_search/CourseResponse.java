package com.vinncorp.fast_learner.response.course.nlp_search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CourseResponse {

    @JsonProperty("average_similarity")
    private double averageSimilarity;
    @JsonProperty("course_id")
    private int courseId;
    @JsonProperty("course_title")
    private String courseTitle;
    @JsonProperty("course_url")
    private String courseUrl;
    private String description;
    private int duration;
    @JsonProperty("instructor_id")
    private Long instructorId;
    @JsonProperty("instructor_name")
    private String instructorName;
    @JsonProperty("instructor_profile_picture")
    private String instructorProfilePicture;
    private double rating;
    @JsonProperty("number_of_viewers")
    private Long noOfReviewers;
    private List<Section> sections;
    private String thumbnail;
    @JsonProperty("topic_title")
    private String topicTitle;
}
