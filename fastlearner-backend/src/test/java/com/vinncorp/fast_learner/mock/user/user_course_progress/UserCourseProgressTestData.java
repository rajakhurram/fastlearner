package com.vinncorp.fast_learner.mock.user.user_course_progress;

import com.vinncorp.fast_learner.mock.section.SectionTestData;
import com.vinncorp.fast_learner.mock.topic.TopicTestData;
import com.vinncorp.fast_learner.models.user.UserCourseProgress;
import com.vinncorp.fast_learner.request.user.CreateUserCourseProgressRequest;

import java.io.IOException;

public class UserCourseProgressTestData {

    public static UserCourseProgress userCourseProgressData() throws IOException {
        UserCourseProgress userCourseProgress = new UserCourseProgress();
        userCourseProgress.setId(1L);
        userCourseProgress.setSection(SectionTestData.sectionData());
        userCourseProgress.setTopic(TopicTestData.topicData());

        return userCourseProgress;
    }

    public static CreateUserCourseProgressRequest createUserCourseProgressRequest() {
        CreateUserCourseProgressRequest request = new CreateUserCourseProgressRequest();
        request.setTopicId(1L);
        request.setIsCompleted(true);
        request.setSeekTime(100L);
        return request;
    }
}
