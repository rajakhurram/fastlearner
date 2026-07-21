package com.vinncorp.fast_learner.response.course.nlp_search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Topic {
    private double similarity;
    @JsonProperty("topic_id")
    private int topicId;
    @JsonProperty("topic_name")
    private String topicName;
}
