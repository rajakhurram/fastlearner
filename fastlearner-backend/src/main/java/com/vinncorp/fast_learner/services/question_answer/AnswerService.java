package com.vinncorp.fast_learner.services.question_answer;

import com.vinncorp.fast_learner.dtos.question_answer.AnswerDetail;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.question_answer.Answer;
import com.vinncorp.fast_learner.models.question_answer.Question;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.repositories.question_answer.AnswerRepository;
import com.vinncorp.fast_learner.request.question_answer.AnswerRequest;
import com.vinncorp.fast_learner.response.question_answer.AnswerResponse;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.notification.IStudentQnADiscussion;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerService implements IAnswerService{

    private final AnswerRepository repo;
    private final IQuestionService questionService;
    private final ISubscribedUserService subscribedUserService;
    private final IEnrollmentService enrollmentService;
    private final IStudentQnADiscussion studentQnADiscussion;
    private final ICourseUrlService courseUrlService;

    @Override
    public Message<String> create(AnswerRequest request, String email)
            throws BadRequestException, EntityNotFoundException, InternalServerException {
        log.info("Saving answer.");

        if(Objects.isNull(request.getCourseId())){
            throw new BadRequestException("Course id Cannot be Null");
        }

        if (!enrollmentService.isEnrolled(request.getCourseId(), email)) {
            throw new BadRequestException("You are not enrolled in this course please enrolled in the course first.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }
        Answer savedAnswer = null;
        if(Objects.nonNull(request.getAnswerId()))
             savedAnswer = repo.findById(request.getAnswerId()).orElse(null);
        Optional<Question> question = questionService.findById(request.getQuestionId());
        Answer answer = Answer.builder()
                .answerText(request.getText())
                .answer(savedAnswer)
                .question(question.orElse(null))
                .build();
        answer.setCreatedBy(subscribedUser.getUser().getId());
        answer.setCreationDate(new Date());

        try {
            repo.save(answer);
            CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(request.getCourseId(), GenericStatus.ACTIVE);
            question.ifPresent(value -> studentQnADiscussion
                    .notifyToUserQnAReply(courseUrl.getUrl(), subscribedUser.getUser().getFullName(),
                            value.getCourse(), value.getCreatedBy(), value.getCreatedBy())
            );
            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Answer saved successfully.")
                    .setData("Answer saved successfully.");
        } catch (Exception e) {
            throw new InternalServerException("Answer "+ InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Message<AnswerResponse> getAllAnswerWithPagination(Long courseId, Long questionId, int pageNo, int pageSize, String email)
            throws BadRequestException, EntityNotFoundException {
        log.info("Fetching all answers by course and question.");
        if (!enrollmentService.isEnrolled(courseId, email)) {
            throw new BadRequestException("You are not enrolled in this course please enrolled in the course first.");
        }
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if (subscribedUser == null) {
            throw new BadRequestException("No plan is subscribed by user: " + email);
        }

        Page<Tuple> rawData = repo.findAllAnswerByCourseIdAndQuestionId(courseId, questionId, PageRequest.of(pageNo, pageSize));
        if (rawData.isEmpty()) {
            throw new EntityNotFoundException("No answers are present.");
        }

        List<AnswerDetail> answerDetails = AnswerDetail.from(rawData, subscribedUser.getUser().getFullName());

        var response = AnswerResponse.builder()
                .answerDetail(answerDetails)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(rawData.getTotalElements())
                .totalPages(rawData.getTotalPages())
                .build();
        return new Message<AnswerResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("All answer are fetched successfully.")
                .setData(response);
    }
}
