package com.vinncorp.fast_learner.request.section;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vinncorp.fast_learner.request.tag.CreateTagRequest;
import com.vinncorp.fast_learner.request.topic.CreateTopicRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class CreateSectionRequest {

    private Long id;
    private Boolean delete;
    private String title;
    private Boolean isFree;
    private int level;

    private List<CreateTagRequest> sectionTags;

    private List<CreateTopicRequest> topics;
}
