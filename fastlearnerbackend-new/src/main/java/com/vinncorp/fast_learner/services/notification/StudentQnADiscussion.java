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
public class StudentQnADiscussion implements IStudentQnADiscussion{

    private final RabbitMQProducer rabbitMQProducer;
    private final ICourseUrlService courseUrlService;

    @Override
    public void notifyCourseQnADiscussion(String question, Course course, String email) throws EntityNotFoundException {
        log.info("Notifying Course Q&A or Discussion.");
        CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE);
        rabbitMQProducer.sendMessage(course.getTitle() + ": "+ question +"",
                "student/course-content/"+courseUrl.getUrl(),
                email, course.getCreatedBy(), course.getContentType(), NotificationContentType.TEXT, NotificationType.COURSE_QnA_DISCUSSION,
                course.getId());
    }

    @Override
    public void notifyToUserLikeDislikedReview(String redirectUrl, String studentName, String status, Course course, Long receiverId, Long instructorId) {
        log.info("Notifying Liked/Disliked review of a user.");

        if (receiverId.equals(instructorId)) {
            log.info("User liked their own review. No notification sent.");
            return;
        }
        rabbitMQProducer.sendMessageToLoggedInUser(studentName + " " + status.toLowerCase() + " your review on " + course.getTitle() + ".",
                receiverId, instructorId, "student/course-content/" + redirectUrl, course.getContentType(), NotificationContentType.TEXT,
                NotificationType.NOTIFY_LIKE_DISLIKED_REVIEW, course.getId()
        );
    }

    @Override
    public void notifyToUserQnAReply(String redirectUrl, String studentName, Course course, long receiverId, long instructorId) {
        log.info("Notifying Q&A Reply of a user.");
        rabbitMQProducer.sendMessageToLoggedInUser(studentName + " replied to your comment in the " +
                "discussion of " + course.getTitle() + ". Join the conversation!", receiverId, instructorId,
                "student/course-content/" + redirectUrl, course.getContentType(), NotificationContentType.TEXT
                , NotificationType.QnA_REPLY, course.getId());
    }
}
