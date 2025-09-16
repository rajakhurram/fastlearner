package com.vinncorp.fast_learner.mock.course;

import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.util.enums.GenericStatus;

import java.io.IOException;

public class CourseUrlTestData {

    public static CourseUrl courseUrl() throws IOException {
        CourseUrl courseUrl = new CourseUrl();
        courseUrl.setId(1L);
        courseUrl.setUrl("https://example.com/course-url");
        courseUrl.setStatus(GenericStatus.ACTIVE);
        courseUrl.setCourse(CourseTestData.courseData());
        return courseUrl;
    }
}
