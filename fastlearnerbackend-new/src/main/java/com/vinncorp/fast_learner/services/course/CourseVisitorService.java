package com.vinncorp.fast_learner.services.course;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.repositories.course.CourseVisitorRepository;
import com.vinncorp.fast_learner.services.notification.IMilestoneAchievementNotificationService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseVisitor;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.response.course.CourseVisitorResponse;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseVisitorService implements ICourseVisitorService{

    private final CourseVisitorRepository repo;
    private final IUserService userService;
    private final IMilestoneAchievementNotificationService milestoneAchievementNotificationService;
    private final RabbitMQProducer rabbitMQProducer;
    private final ICourseUrlService courseUrlService;
    @Transactional
    @Override
    public void save(Course course, User user) throws InternalServerException, BadRequestException {
        log.info("Saving course visitor into db...");
        if(Objects.isNull(course)){throw new BadRequestException("course cannot be null");}
        if(Objects.isNull(user)){throw new BadRequestException("user cannot be null");}

        var courseVisitor = CourseVisitor.builder()
                .visitor(user)
                .course(course)
                .instructorId(course.getCreatedBy())
                .visitedAt(LocalDateTime.now())
                .build();
        try {
            repo.save(courseVisitor);

            long totalCourseVisits = totalCourseVisit(course.getId());
            if (milestoneAchievementNotificationService.isMilestoneMet(totalCourseVisits)) {
                milestoneAchievementNotificationService.notifyCourseVisitMilestoneAchievement(
                        totalCourseVisits, course, user.getEmail()
                );
            }

            if (!user.getId().equals(course.getCreatedBy())) {
                CourseUrl courseUrl = courseUrlService.findActiveUrlByCourseIdAndStatus(
                        course.getId(), GenericStatus.ACTIVE
                );

                rabbitMQProducer.sendMessage(
                        course.getTitle(),
                        "student/course-details/" + courseUrl.getUrl(),
                        user.getEmail(),
                        course.getCreatedBy(),
                        course.getContentType(),
                        NotificationContentType.TEXT,
                        NotificationType.COURSE_VISIT_CONTENT,
                        course.getId()
                );
            }

        } catch (Exception e) {
            log.error("ERROR: " + e.getLocalizedMessage());
            throw new InternalServerException("Course visitor " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }

        log.info("Saved course visitor successfully.");
    }

    @Override
    public Message<List<CourseVisitorResponse>> fetchAllVisitors(Long courseId, String email) throws EntityNotFoundException {
        log.info("Fetching all visitors data...");
        User user = userService.findByEmail(email);

        List<Tuple> data = repo.findAllVisitorsByInstructorId(user.getId(), courseId);
        if(CollectionUtils.isEmpty(data))
            throw new EntityNotFoundException("No data found for the instructor: " + user.getFullName());

        return new Message<List<CourseVisitorResponse>>()
                .setCode(HttpStatus.OK.name())
                .setStatus(HttpStatus.OK.value())
                .setMessage("Fetched all visitors successfully.")
                .setData(CourseVisitorResponse.from(data));
    }

    @Override
    public long totalCourseVisit(long courseId) {
        log.info("Fetching no of total course visits with course id: " + courseId);
        return repo.countByCourseId(courseId);
    }
}
