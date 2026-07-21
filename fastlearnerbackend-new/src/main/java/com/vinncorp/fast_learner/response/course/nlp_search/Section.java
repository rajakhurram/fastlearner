package com.vinncorp.fast_learner.response.course.nlp_search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Section {
    @JsonProperty("average_similarity")
    private double averageSimilarity;
    @JsonProperty("section_id")
    private int sectionId;
    @JsonProperty("section_name")
    private String sectionName;
    private int duration;
    private List<Topic> topics;
}
