package com.vinncorp.fast_learner.services.notification;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.course.Course;

public interface IStudentQnADiscussion {
    void notifyCourseQnADiscussion(String question, Course course, String email) throws EntityNotFoundException;

    void notifyToUserLikeDislikedReview(String redirectUrl, String studentName, String status, Course course, Long receiverId, Long instructorId);

    void notifyToUserQnAReply(String redirectUrl, String studentName, Course course, long receiverId, long instructorId);
}
