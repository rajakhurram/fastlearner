package com.vinncorp.fast_learner.es_dto;

import lombok.*;

import java.util.List;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseContentResponse {
    private String id;
    private String title;
    private String outcome;
    private String description;
    private List<String> tags;
    private String thumbnailUrl;
    private String creatorName;
    private String userProfileUrl;
    private String profilePictureUrl;
    private String courseUrl;
    private Double rating;
    private int numberOfViewers;
    private Integer duration;
    private String courseType;
    private Double price;
    private Boolean isEnrolled = false;
    private String contentType;
    private Long testTotalQuestion;
    private List<SectionContentResponse> sections;
    private List<TopicContentResponse> topics;
}
