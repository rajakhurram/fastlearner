package com.vinncorp.fast_learner.request.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserCourseProgressRequest {
    private Long id;
    private Boolean isCompleted;
    private Long topicId;
    private Long seekTime;
    private boolean addVideoTime;
}
