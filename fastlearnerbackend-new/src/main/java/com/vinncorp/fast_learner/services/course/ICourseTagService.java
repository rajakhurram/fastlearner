package com.vinncorp.fast_learner.services.course;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.course.CourseTag;

import java.util.List;

public interface ICourseTagService {
    void createAllCourseTags(List<CourseTag> courseTags) throws InternalServerException;

    void deleteAllCourseTagByTagIds(Long courseId);
}
