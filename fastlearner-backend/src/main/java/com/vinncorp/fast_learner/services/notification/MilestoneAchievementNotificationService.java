package com.vinncorp.fast_learner.services.notification;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.util.enums.ContentType;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MilestoneAchievementNotificationService implements IMilestoneAchievementNotificationService {

    private final RabbitMQProducer rabbitMQProducer;
    private final ICourseUrlService courseUrlService;

    @Override
    public void notifyCourseMilestoneAchievements(long totalEnrolled, Course course, String email) throws EntityNotFoundException {
        CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE);
        log.info("Notifying course milestone achievements.");
        rabbitMQProducer.sendMessage(course.getTitle() + " has reached "+ totalEnrolled +" enrollments.",
                "student/course-details/"+courseUrl.getUrl(),
                email, course.getCreatedBy(), course.getContentType(), NotificationContentType.TEXT, NotificationType.ENROLLMENT_ACHIEVEMENT,
                course.getId());
    }

    @Override
    public void notifyToUserCourseMilestoneAchieved(double percentage, Course course, Long receiverId) throws EntityNotFoundException {
        log.info("Notifying course milestone achieved to user.");
        CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE);
        rabbitMQProducer.sendMessageToLoggedInUser(percentage + "% of "+ course.getTitle() +". Keep up the great work!",
                receiverId, course.getCreatedBy(),"student/course-details/"+courseUrl.getUrl(), course.getContentType(), NotificationContentType.TEXT, NotificationType.COURSE_MILESTONE_ACHIEVED, courseUrl.getCourse().getId());
    }

    @Override
    public void notifyToUserCourseEnrollment(Course course, String url, Long receiverId) {
        log.info("Notifying course enrollment to the user.");
        rabbitMQProducer.sendMessageToLoggedInUser(course.getTitle() + ". Start learning now!", receiverId, course.getCreatedBy(),
                "student/course-details/" + url, course.getContentType(),
                NotificationContentType.TEXT, NotificationType.ENROLLMENT_CONFIRMATION, course.getId());
    }

    @Override
    public void notifyToUserCourseCompletion(Course course, String url, Long receiverId) {
        log.info("Notifying course enrollment to the user.");
        rabbitMQProducer.sendMessageToLoggedInUser(course.getTitle() + ". Don’t forget to leave a review.", receiverId, course.getCreatedBy(),
                "student/course-details/"+url, course.getContentType(), NotificationContentType.TEXT, NotificationType.COURSE_COMPLETION, course.getId());
    }

    @Override
    public void notifyCourseVisitMilestoneAchievement(long totalCourseVisits, Course course, String email) throws EntityNotFoundException {
        log.info("Notifying course visit milestone achievements.");
        CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE);
        rabbitMQProducer.sendMessage(course.getTitle() + " has been viewed "+ totalCourseVisits +" times.",
                "student/course-details/"+courseUrl.getUrl(),
                email, course.getCreatedBy(), course.getContentType(), NotificationContentType.TEXT, NotificationType.COURSE_VISIT_ACHIEVEMENT,
                course.getId());
    }

    @Override
    public void notifyCertificationCompletion(String studentName, String courseTitle, String url, Long instructorId, Course course, String email) {
        log.info("Notifying certification completion.");
        String courseContentType = course.getContentType().equals(ContentType.COURSE) ? "course" : "test";
        rabbitMQProducer.sendMessage(studentName + " has successfully completed "+courseContentType+ courseTitle +".",
                "student/course-details/"+url,
                email, instructorId, course.getContentType(), NotificationContentType.TEXT, NotificationType.CERTIFICATION_COMPLETION,
                course.getId());
    }

    @Override
    public void notifyToUserCertificateAwarded(String courseTitle, Long id, Long instructorId, Long receiverId, Course course) {
        log.info("Notifying certification completion.");
        rabbitMQProducer.sendMessageToLoggedInUser(courseTitle + ". Get your certificate here "+ courseTitle +".",
                receiverId, instructorId, "student/generate-certificate?courseId="+id, course.getContentType(),
                NotificationContentType.TEXT, NotificationType.CERTIFICATE_AWARDED, course.getId());
    }

    /**
     * Specifying milestones:
     * 10 - For TESTING ONLY
     * 50, 100, 500, 1000, 2000, 5000 and then every 10000
     * */
    @Override
    public boolean isMilestoneMet(long count) {
        log.info("Milestone met for count: " + count);
        if (count == 10 || count == 50 || count == 100 || count == 500 || count == 1000 || count == 2000 || count == 5000 || count == 10000) {
            return true;
        }
        return (count % 10000) == 0;
    }

    @Override
    public double milestonePercentageRate(double percentage) {
        log.info("Milestone percentage rate checking.");
        if((percentage >= 20.0 && percentage < 21.0) ||
                (percentage >= 40.0 && percentage < 41.0) ||
                (percentage >= 60.0 && percentage < 61.0) ||
                (percentage >= 80.0 && percentage < 81.0) ||
                (percentage >= 99.0 && percentage < 100.0)
        ){
            return percentage;
        }
        return 0.0;
    }
}
