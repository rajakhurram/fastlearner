package com.vinncorp.fast_learner.services.user;

import com.vinncorp.fast_learner.dtos.user.user_course_progress.CoursesProgress;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user.UserCourseCompletion;
import com.vinncorp.fast_learner.models.user.UserCourseProgress;
import com.vinncorp.fast_learner.repositories.user.UserCourseCompletionRepository;
import com.vinncorp.fast_learner.repositories.user.UserCourseProgressRepository;
import com.vinncorp.fast_learner.request.user.CreateUserCourseProgressRequest;
import com.vinncorp.fast_learner.controllers.youtube_video.user.ActiveStudentsResponse;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.notification.IInstructorPerformanceInsightService;
import com.vinncorp.fast_learner.services.notification.IMilestoneAchievementNotificationService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.topic.ITopicService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserCourseProgressService implements IUserCourseProgressService{

    private final UserCourseProgressRepository repo;
    private final UserCourseCompletionRepository userCourseCompletionRepo;

    private final IEnrollmentService enrollmentService;
    private final ISubscribedUserService subscribedUserService;
    private final ITopicService topicService;
    private final IUserService userService;
    private final IInstructorPerformanceInsightService instructorPerformanceInsightService;
    private final IMilestoneAchievementNotificationService milestoneAchievementNotificationService;
    private final ICourseUrlService courseUrlService;

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = InternalServerException.class)
    @Override
    public Message<String> markComplete(CreateUserCourseProgressRequest request, String email)
            throws EntityNotFoundException, BadRequestException, InternalServerException {
        log.info("Topic completion is started.");
        Topic topic = topicService.getTopicById(request.getTopicId());
        if (!enrollmentService.isEnrolled(topic.getSection().getCourse().getId(), email)) {
            throw new BadRequestException("User is not enrolled in this course.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);

        UserCourseProgress userCourseProgress = repo.findByTopic_IdAndStudent_Email(request.getTopicId(), email).orElse(null);

        if (userCourseProgress == null) {
            userCourseProgress = new UserCourseProgress();
            userCourseProgress.setCompleted(request.getIsCompleted());
            userCourseProgress.setSeekTime(request.getIsCompleted() ? topic.getDurationInSec() : request.getSeekTime());
            userCourseProgress.setTopic(topic);
            userCourseProgress.setCourse(topic.getSection().getCourse());
            userCourseProgress.setSection(topic.getSection());
            userCourseProgress.setStudent(subscribedUser.getUser());
            userCourseProgress.setCreationDate(new Date());
            userCourseProgress.setLastModifiedDate(new Date());
            double percentage = fetchCourseProgress(userCourseProgress.getCourse().getId(), email).getData();
            if (instructorPerformanceInsightService.isPercentageRateMet(percentage)) {
                instructorPerformanceInsightService.notifyCertificateCompletionRate(percentage, userCourseProgress.getCourse(), email);
            }
        }else {
            userCourseProgress.setCompleted(request.getIsCompleted());
            userCourseProgress.setSeekTime(request.getIsCompleted() ? topic.getDurationInSec() : request.getSeekTime());
            userCourseProgress.setTopic(topic);
            userCourseProgress.setCourse(topic.getSection().getCourse());
            userCourseProgress.setSection(topic.getSection());
            userCourseProgress.setStudent(subscribedUser.getUser());
            userCourseProgress.setLastModifiedDate(new Date());
        }
        try {
            repo.save(userCourseProgress);
            CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(topic.getSection().getCourse().getId(), GenericStatus.ACTIVE);
            double percentage = fetchCourseProgress(userCourseProgress.getCourse().getId(), email).getData();
            if(percentage == 50)
                instructorPerformanceInsightService.notifyToUserProgressUpdate(userCourseProgress.getCourse(), subscribedUser.getUser().getId());
            if (percentage >= 100.0) {
                milestoneAchievementNotificationService.notifyToUserCourseCompletion(userCourseProgress.getCourse(), courseUrl.getUrl(),
                        subscribedUser.getUser().getId());
            }
            if(milestoneAchievementNotificationService.milestonePercentageRate(percentage) != 0.0)
                milestoneAchievementNotificationService.notifyToUserCourseMilestoneAchieved(percentage, userCourseProgress.getCourse(),
                        subscribedUser.getUser().getId());

        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Creating multiple course progress for same course, topic and student.");
        }catch (Exception e) {
            throw new InternalServerException("User course progress " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }

        // Checking if the user course progress is greater than the derived value in user course completion model then
        // user course completion data should be created.
        createCourseCompletionDataIfCourseProgressIsValid(email, subscribedUser, userCourseProgress);

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage(request.getIsCompleted() ? "Topic is marked completed." : "Unmarked the topic.")
                .setData(request.getIsCompleted() ? "Topic is marked completed." : "Unmarked the topic.");
    }

    private void createCourseCompletionDataIfCourseProgressIsValid(String email, SubscribedUser subscribedUser, UserCourseProgress userCourseProgress)
            throws EntityNotFoundException, InternalServerException {
        Double percentage = fetchCourseProgress(userCourseProgress.getCourse().getId(), email).getData();
        if (percentage > UserCourseCompletion.MINIMUM_TOPIC_COMPLETION_PERCENTAGE) {
            UserCourseCompletion userCourseCompletion = userCourseCompletionRepo
                    .findByCourseIdAndUserId(userCourseProgress.getCourse().getId(), subscribedUser.getUser().getId());
            if(Objects.isNull(userCourseCompletion)){
                userCourseCompletion = UserCourseCompletion.builder()
                        .creationDate(new Date())
                        .course(userCourseProgress.getCourse())
                        .user(subscribedUser.getUser())
                        .build();
            }else{
                userCourseCompletion.setCreationDate(new Date());
            }
            try {
                userCourseCompletionRepo.save(userCourseCompletion);
            } catch (Exception e) {
                throw new InternalServerException("User course completion " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    public UserCourseProgress getPreviousTopicByUserAndCourse(Long courseId, String email) {
        log.info("Fetching last attempted topic of course: "+ courseId);
        return repo.findFirstByCourse_IdAndStudent_EmailOrderByLastModifiedDateDesc(courseId, email)
                .orElse(null);
    }

    @Override
    public Message<Double> fetchCourseProgress(Long courseId, String email) throws EntityNotFoundException {
        log.info("Fetching course progress.");
        User user = userService.findByEmail(email);
        Double percentage = repo.fetchCourseProgress(courseId, user.getId());
        return new Message<Double>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Successfully fetch course progress")
                .setData(percentage);
    }

    @Override
    public List<CoursesProgress> getCoursesProgressByUser(List<Long> coursesId, Long userId) {
        log.info("Fetching all courses progresses by user...");
        var m = repo.findCourseProgressByListOfCoursesAndUser(coursesId, userId);
        return CoursesProgress.from(m);
    }

    @Override
    public Tuple fetchCourseCompletion(String period, Long instructorId) {
        log.info("Fetching course completion by id: "+instructorId);
        return userCourseCompletionRepo.fetchCourseCompletion(period.toLowerCase(), instructorId);
    }

    @Override
    public void markCompletedAllTopicsOfASection(Long sectionId, Long userId) {
        log.info("Marking all the topics of a section with id: "+ sectionId);
        try {
            List<Topic> topics = topicService.fetchAllTopicsBySectionId(sectionId);

        } catch (EntityNotFoundException e) {
            log.error("No topics found for the provided section.");
        }
    }

    @Override
    public Message<List<ActiveStudentsResponse>> getAllActiveStudentsByCourseIdOrInstructorId(Long courseId, String email) throws EntityNotFoundException, BadRequestException {
        log.info("Fetching all active students month-wise.");
        if(Objects.isNull(email)){throw new BadRequestException("Email cannot be null.");}
        User user = userService.findByEmail(email);
        List<Tuple> data = this.repo.findAllActiveStudents(courseId, user.getId());

        if(CollectionUtils.isEmpty(data))
            throw new EntityNotFoundException("No active students found.");

        return new Message<List<ActiveStudentsResponse>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Fetched all active students successfully.")
                .setData(ActiveStudentsResponse.from(data));
    }

    @Override
    @Transactional
    public void deleteAllUserCourseProgressOfVideo(Long topicId) {
        log.info("Deleting all course progress for this topic id: " + topicId);
        repo.deleteAllByTopicId(topicId);
    }
}
