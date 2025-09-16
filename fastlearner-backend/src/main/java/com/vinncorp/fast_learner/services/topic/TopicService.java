package com.vinncorp.fast_learner.services.topic;

import com.vinncorp.fast_learner.dtos.quiz.QuizQuestionAnswer;
import com.vinncorp.fast_learner.dtos.topic.NoOfTopicInCourse;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.video.Video;
import com.vinncorp.fast_learner.repositories.topic.TopicRepository;
import com.vinncorp.fast_learner.response.topic.TopicDetailForUpdateResponse;
import com.vinncorp.fast_learner.response.topic.TopicDetailResponse;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.quiz.IQuizQuestionAnswerService;
import com.vinncorp.fast_learner.services.section.ISectionService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.services.video.IVideoService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.ContentType;
import com.vinncorp.fast_learner.util.enums.CourseType;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class TopicService implements ITopicService{

    private final TopicRepository repo;
    private final ISubscribedUserService subscribedUserService;
    private final IEnrollmentService enrollmentService;
    private final IVideoService videoService;
    private final IQuizQuestionAnswerService quizQuestionAnswerService;
    private final ISectionService sectionService;
    private final IUserService userService;

    public TopicService(TopicRepository repo, ISubscribedUserService subscribedUserService,
                        IEnrollmentService enrollmentService, IVideoService videoService,
                        IQuizQuestionAnswerService quizQuestionAnswerService, @Lazy ISectionService sectionService,
                        IUserService userService) {
        this.repo = repo;
        this.subscribedUserService = subscribedUserService;
        this.enrollmentService = enrollmentService;
        this.videoService = videoService;
        this.quizQuestionAnswerService = quizQuestionAnswerService;
        this.sectionService = sectionService;
        this.userService = userService;
    }

    @Override
    public Message<String> deleteTopicById(Long id) throws InternalServerException {
        log.info("Deleting a topic from course...");
        try {
            repo.deleteById(id);
            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setData("Topic is deleted successfully.")
                    .setMessage("Topic is deleted successfully.");
        } catch (Exception e) {
            log.error("Deleting topic is not successful.");
            throw new InternalServerException("Topic " + InternalServerException.NOT_DELETE_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Topic save(Topic topic) throws InternalServerException {
        log.info("Creating topic.");
        try {
            if (Objects.nonNull(topic.getId()) && Objects.nonNull(topic.getDelete()) && topic.getDelete()) {
                repo.deleteById(topic.getId());
                return null;
            }else {
                return repo.save(topic);
            }
        } catch (Exception e) {
            log.error("ERROR: "+e.getLocalizedMessage());
            throw new InternalServerException("Topic" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<NoOfTopicInCourse> getAllTopicByCourses(List<Long> courseIdList) throws EntityNotFoundException {
        log.info("Fetching no of topics present in a course.");
        List<Tuple> data = repo.findAllTopicsByCourseIdList(courseIdList);
        if (data.isEmpty()) {
            throw new EntityNotFoundException("No topics found.");
        }
        return NoOfTopicInCourse.from(data);
    }

    @Override
    public Topic getTopicById(Long topicId) throws EntityNotFoundException {
        log.info("Fetching topic by topic id: "+topicId);
        return repo.findById(topicId).orElseThrow(() -> new EntityNotFoundException("No topic found by topic id: "+ topicId));
    }

    @Override
    public Message<List<TopicDetailResponse>> getAllTopicBySection(Long courseId, Long sectionId, String email)
            throws BadRequestException, EntityNotFoundException {
        log.info("Fetching all topics by section id: "+ sectionId);
        if (!enrollmentService.isEnrolled(courseId, email)) {
            throw new BadRequestException("You are not enrolled in this course please enroll in the course first.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }
        boolean isFree = Objects.nonNull(subscribedUser.getSubscribedId());
        Section isFreeSection = sectionService.findById(sectionId);
        if(!isFreeSection.getCourse().getCreatedBy().equals(subscribedUser.getUser().getId())){
            if(isFreeSection.getCourse().getCourseType() != CourseType.PREMIUM_COURSE) {
                if (!isFree && !isFreeSection.isFree()) {
                    throw new BadRequestException("User have to get a paid subscription, this section isn't free.");
                }
            }
        }

        List<Tuple> topics = repo.findAllBySectionId(sectionId, subscribedUser.getUser().getId());
        if (CollectionUtils.isEmpty(topics)) {
            throw new EntityNotFoundException("No topics found for this section.");
        }


        List<TopicDetailResponse> topicDetails = TopicDetailResponse.from(topics);

        for(TopicDetailResponse topicDetail : topicDetails) {
            if(topicDetail.getTopicType().equalsIgnoreCase("QUIZ")){
                topicDetail.setQuizQuestionAnswer(this.fetchAllQuestionAndAnswersByTopicId(topicDetail.getTopicId(), true, false, PageRequest.of(0, 10)));
            }
        }

        return new Message<List<TopicDetailResponse>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Fetching all topics of a section.")
                .setData(topicDetails);
    }

    @Override
    public Message<String> getSummaryOfVideoByTopicId(Long topicId, String email)
            throws EntityNotFoundException, BadRequestException {
        log.info("Fetching summary of a video by topic id: "+ topicId);

        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }

        Video video = videoService.getVideoByTopicId(topicId);

        if (!enrollmentService.isEnrolled(video.getTopic().getSection().getCourse().getId(), email)) {
            throw new BadRequestException("You are not enrolled in this course please enrolled in the course first.");
        }

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Fetched summary successfully.")
                .setData(video.getSummary());
    }


    @Override
    public Message<List<TopicDetailForUpdateResponse>> getAllTopicBySectionForUpdate(Long sectionId, String email) throws EntityNotFoundException, BadRequestException {
        log.info("Fetching all topics by sections for update.");
        if(Objects.isNull(sectionId)){throw new BadRequestException("Section ID cannot be null");}
        if(Objects.isNull(email)){throw new BadRequestException("Email cannot be null");}
        User user = userService.findByEmail(email);
        Section section = sectionService.findById(sectionId);
        if (!user.getId().equals(section.getCourse().getCreatedBy())) {
            throw new BadRequestException("This has no permission for update for this section.");
        }

        List<Tuple> topics = repo.findAllBySectionId(sectionId, user.getId());
        if (CollectionUtils.isEmpty(topics)) {
            throw new EntityNotFoundException("No topics found for this section.");
        }

        List<TopicDetailForUpdateResponse> topicDetails = TopicDetailForUpdateResponse.from(topics);

        for(TopicDetailForUpdateResponse topicDetail : topicDetails) {
            if(topicDetail.getTopicType().equalsIgnoreCase("QUIZ")){
                topicDetail.setQuizQuestionAnswer(this.fetchAllQuestionAndAnswersByTopicId(topicDetail.getTopicId(), false, false, PageRequest.of(0, 10)));
            }
        }

        return new Message<List<TopicDetailForUpdateResponse>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Fetching all topics of a section.")
                .setData(topicDetails);
    }

    @Override
    public List<Topic> fetchAllTopicsBySectionId(Long sectionId) throws EntityNotFoundException {
        log.info("Fetching all topics by section id: " + sectionId);
        List<Topic> topics = repo.findBySectionId(sectionId);
        if(CollectionUtils.isEmpty(topics))
            throw new EntityNotFoundException("No topics found for provided section.");
        return topics;
    }

    private QuizQuestionAnswer fetchAllQuestionAndAnswersByTopicId(Long topicId, boolean forStudent, boolean random, Pageable pageable) {
        return quizQuestionAnswerService.fetchAllQuestionAndAnswersByTopicId(topicId, forStudent, random, pageable);
    }
}
