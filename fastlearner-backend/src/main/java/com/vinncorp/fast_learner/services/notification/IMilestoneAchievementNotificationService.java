package com.vinncorp.fast_learner.services.notification;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.course.Course;

public interface IMilestoneAchievementNotificationService {
    void notifyCourseMilestoneAchievements(long totalEnrolled, Course course, String userEmail) throws EntityNotFoundException;

    void notifyToUserCourseMilestoneAchieved(double percentage, Course course, Long receiverId) throws EntityNotFoundException;

    void notifyToUserCourseEnrollment(Course course, String url, Long receiverId);

    void notifyToUserCourseCompletion(Course course, String url, Long receiverId);

    void notifyCourseVisitMilestoneAchievement(long totalCourseVisits, Course course, String email) throws EntityNotFoundException;

    void notifyCertificationCompletion(String studentName, String courseTitle, String url, Long instructorId, Course course, String email);

    void notifyToUserCertificateAwarded(String courseTitle, Long id, Long instructorId, Long receiverId, Course course);

    boolean isMilestoneMet(long count);

    double milestonePercentageRate(double percentage);
}
