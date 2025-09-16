package com.vinncorp.fast_learner.services.question_answer;

import com.vinncorp.fast_learner.dtos.question_answer.QuestionDetail;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.question_answer.Question;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.repositories.question_answer.QuestionRepository;
import com.vinncorp.fast_learner.request.question_answer.QuestionRequest;
import com.vinncorp.fast_learner.response.question_answer.QuestionResponse;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.notification.IStudentQnADiscussion;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.topic.ITopicService;
import com.vinncorp.fast_learner.util.Message;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService implements IQuestionService{

    private final QuestionRepository repo;
    private final ISubscribedUserService subscribedUserService;
    private final IEnrollmentService enrollmentService;
    private final ITopicService topicService;
    private final ICourseService courseService;
    private final IStudentQnADiscussion studentQnADiscussion;

    @Override
    public Optional<Question> findById(Long questionId) {
        log.info("Fetching question by question id: "+questionId);
        return repo.findById(questionId);
    }

    @Override
    public Message<QuestionDetail> create(QuestionRequest request, String email)
            throws BadRequestException, EntityNotFoundException, InternalServerException {
        log.info("Creating a question.");
        if (!enrollmentService.isEnrolled(request.getCourseId(), email)) {
            throw new BadRequestException("You are not enrolled in this course please enrolled in the course first.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }

        Course course = courseService.findById(request.getCourseId());
        Topic topic = topicService.getTopicById(request.getTopicId());

        Question question = Question.builder()
                .questionText(request.getText())
                .topic(topic)
                .course(course)
                .build();
        question.setCreatedBy(subscribedUser.getUser().getId());
        question.setCreationDate(new Date());

        try {
            question = repo.save(question);
            studentQnADiscussion.notifyCourseQnADiscussion(request.getText(), course, email);
            return new Message<QuestionDetail>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Question is created successfully.")
                    .setData(QuestionDetail.from(this.repo.findQuestionById(question.getId()), subscribedUser.getUser().getFullName()));
        } catch (Exception e) {
            throw new InternalServerException("Question "+ InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Message<QuestionResponse> findAllQuestionsWithPagination(Long courseId, int pageNo, int pageSize, String email)
            throws BadRequestException, EntityNotFoundException {
        log.info("Fetch all question and answers with pagination.");
        if (!enrollmentService.isEnrolled(courseId, email)) {
            throw new BadRequestException("You are not enrolled in this course please enrolled in the course first.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }

        Page<Tuple> rawData = repo.findAllQuestionsByCourse(courseId, PageRequest.of(pageNo, pageSize));
        if (rawData.isEmpty()) {
            throw new EntityNotFoundException("No questions are present for this course.");
        }
        List<QuestionDetail> questionDetail = QuestionDetail.from(rawData.getContent(), subscribedUser.getUser().getFullName());

        var response = QuestionResponse.builder()
                .questionDetails(questionDetail)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(rawData.getTotalElements())
                .totalPages(rawData.getTotalPages())
                .build();

        return new Message<QuestionResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Fetched all questions successfully.")
                .setData(response);
    }



}
