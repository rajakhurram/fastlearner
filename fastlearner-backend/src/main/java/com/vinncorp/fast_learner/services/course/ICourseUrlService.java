package com.vinncorp.fast_learner.services.course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import com.vinncorp.fast_learner.util.enums.GenericStatus;

import java.util.List;
public interface ICourseUrlService {
    CourseUrl findByUrlAndCourseStatuses(String courseUrl, List<CourseStatus> courseStatuses) throws EntityNotFoundException;

    CourseUrl findActiveUrlByCourseIdAndStatus(Long id, GenericStatus status) throws EntityNotFoundException;

    CourseUrl save(CourseUrl courseUrl) throws InternalServerException;
    void deleteCourseUrlByCourseId(Long courseId);

}
