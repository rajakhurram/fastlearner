package com.vinncorp.fast_learner.request.topic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUpdateTopicNotesRequest {

    private Long topicNotesId;
    private Long courseId;
    private Long topicId;
    private String note;
    private String time;
}
