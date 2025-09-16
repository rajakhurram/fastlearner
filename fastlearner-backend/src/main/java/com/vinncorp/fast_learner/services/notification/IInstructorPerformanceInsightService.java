package com.vinncorp.fast_learner.services.notification;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.course.Course;

public interface IInstructorPerformanceInsightService {
    void notifyCertificateCompletionRate(double percentage, Course course, String email) throws EntityNotFoundException;

    void notifyToUserProgressUpdate(Course course, Long receiverId) throws EntityNotFoundException;

    void notifyToUserOnNewSubscription(String subscriptionDuration, Long receiverId);

    void notifyToUserExclusiveCourseAccess(String subscriptionDuration, Long receiverId);

    boolean isPercentageRateMet(double percent);
}
