package com.vinncorp.fast_learner.dtos.user.user_course_progress;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserCourseProgressSection {

    private String name;
    private List<UserCourseProgressTopic> topics;
    private int sequence;
}
