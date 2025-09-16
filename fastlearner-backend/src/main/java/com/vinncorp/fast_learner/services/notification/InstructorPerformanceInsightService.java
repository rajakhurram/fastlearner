package com.vinncorp.fast_learner.services.notification;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstructorPerformanceInsightService implements IInstructorPerformanceInsightService{

    private final RabbitMQProducer rabbitMQProducer;
    private final ICourseUrlService courseUrlService;

    @Override
    public void notifyCertificateCompletionRate(double percentage, Course course, String email) throws EntityNotFoundException {
        log.info("Notify certificate completion rate.");
        CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE);
        rabbitMQProducer.sendMessage(course.getTitle() + " has a "+ percentage +"% completion rate.",
                "student/course-details/"+courseUrl.getUrl(),
                email, course.getCreatedBy(), course.getContentType(), NotificationContentType.TEXT, NotificationType.COURSE_COMPLETION_RATE,
                course.getId());
    }

    @Override
    public void notifyToUserProgressUpdate(Course course, Long receiverId) throws EntityNotFoundException {
        log.info("Notifying progress update to user.");
        CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE);
        rabbitMQProducer.sendMessageToLoggedInUser(course.getTitle() + ". Keep going!", receiverId, course.getCreatedBy(),
                "student/course-details/"+courseUrl.getUrl(), course.getContentType(),
                NotificationContentType.TEXT, NotificationType.PROGRESS_UPDATE, courseUrl.getCourse().getId());
    }

    @Override
    public void notifyToUserOnNewSubscription(String subscriptionDuration, Long receiverId) {
        log.info("Notifying on subscription renewal to user.");
        rabbitMQProducer.sendMessageToLoggedInUser(subscriptionDuration + " subscription has been successfully renewed. Enjoy uninterrupted access to all courses.",
                receiverId, null, null, null, NotificationContentType.TEXT, NotificationType.NEW_SUBSCRIPTION, null);
    }

    @Override
    public void notifyToUserExclusiveCourseAccess(String subscriptionDuration, Long receiverId) {
        log.info("Notifying exclusive course access after registration to user.");
        rabbitMQProducer.sendMessageToLoggedInUser(subscriptionDuration + "] subscriber, you now have access to our exclusive courses. Start learning today!",
                receiverId, null, null, null, NotificationContentType.TEXT, NotificationType.EXCLUSIVE_COURSE_ACCESS, null);
    }

    @Override
    public boolean isPercentageRateMet(double percent) {
        log.info("Check if the course completion percentage rate met");
        if((percent >= 20.0 && percent < 21.0) ||
                        (percent >= 40.0 && percent < 41.0) ||
                        (percent >= 60.0 && percent < 61.0) ||
                        (percent >= 80.0 && percent < 81.0) ||
                        (percent >= 99.0 && percent <= 100.0)
        ){
            return true;
        }
        return false;
    }
}
