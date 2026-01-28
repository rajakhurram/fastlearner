package com.vinncorp.fast_learner.request.course;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vinncorp.fast_learner.request.section.CreateSectionRequest;
import com.vinncorp.fast_learner.request.tag.CreateTagRequest;
import com.vinncorp.fast_learner.util.enums.CourseType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateCourseRequest {

    private Long courseId;
    private String courseType;
    private Double price;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName; // for response
    private Long courseLevelId;
    private String courseLevelName; // for response
    private String about;
    private String thumbnailUrl;
    private String previewVideoURL;
    private String previewVideoVttContent;
    private List<String> prerequisite;
    private Integer courseDuration;
    private List<String> courseOutcomes;
    private List<CreateSectionRequest> sections;
    private List<CreateTagRequest> tags;
    private Boolean certificateEnabled;
    private Boolean isActive;
    private String courseProgress;
    private String courseUrl;
    private String contentType;
}
