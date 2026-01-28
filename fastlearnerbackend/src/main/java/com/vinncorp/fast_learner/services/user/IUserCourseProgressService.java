package com.vinncorp.fast_learner.services.user;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.dtos.user.user_course_progress.CoursesProgress;
import com.vinncorp.fast_learner.models.user.UserCourseProgress;
import com.vinncorp.fast_learner.request.user.CreateUserCourseProgressRequest;
import com.vinncorp.fast_learner.controllers.youtube_video.user.ActiveStudentsResponse;
import com.vinncorp.fast_learner.util.Message;
import jakarta.persistence.Tuple;

import java.util.List;

public interface IUserCourseProgressService {
    Message<String> markComplete(CreateUserCourseProgressRequest request, String email)
            throws EntityNotFoundException, BadRequestException, InternalServerException;

    UserCourseProgress getPreviousTopicByUserAndCourse(Long courseId, String email);

    Message<Double> fetchCourseProgress(Long courseId, String email) throws EntityNotFoundException;

    List<CoursesProgress> getCoursesProgressByUser(List<Long> coursesId, Long userId);

    Tuple fetchCourseCompletion(String period, Long instructorId);

    void markCompletedAllTopicsOfASection(Long sectionId, Long userId);

    Message<List<ActiveStudentsResponse>> getAllActiveStudentsByCourseIdOrInstructorId(Long courseId, String email) throws EntityNotFoundException, BadRequestException;

    void deleteAllUserCourseProgressOfVideo(Long id);
}
