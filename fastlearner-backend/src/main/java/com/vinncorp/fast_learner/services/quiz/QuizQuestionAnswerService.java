package com.vinncorp.fast_learner.services.quiz;

import com.vinncorp.fast_learner.dtos.quiz.QuizAnswer;
import com.vinncorp.fast_learner.dtos.quiz.QuizQuestion;
import com.vinncorp.fast_learner.dtos.quiz.RandomQuizProjection;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.quiz.Quiz;
import com.vinncorp.fast_learner.models.quiz.QuizAttempt;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.quiz.QuizAttemptRepository;
import com.vinncorp.fast_learner.repositories.quiz.QuizQuestionAnwserRepository;
import com.vinncorp.fast_learner.dtos.quiz.QuizQuestionAnswer;
import com.vinncorp.fast_learner.models.quiz.QuizQuestionAnwser;
import com.vinncorp.fast_learner.repositories.quiz.QuizQuestionRepository;
import com.vinncorp.fast_learner.repositories.quiz.QuizRepository;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.request.question_answer.ValidationAnswerRequest;
import com.vinncorp.fast_learner.response.quiz.AnswerResponse;
import com.vinncorp.fast_learner.response.quiz.QuestionAnswerResponse;
import com.vinncorp.fast_learner.response.quiz.QuizAnswerResponse;
import com.vinncorp.fast_learner.response.quiz.QuizQuestionAnswerResponse;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizQuestionAnswerService implements IQuizQuestionAnswerService {

    private final QuizQuestionAnwserRepository repo;

    private final UserRepository userRepository;

    private final QuizAttemptRepository quizAttemptRepository;

    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizRepository quizRepository;

    @Override
    public QuizQuestionAnwser save(QuizQuestionAnwser quizQuestionAnswer) throws InternalServerException, BadRequestException {
        log.info("Creating quiz question's answers.");
        if (quizQuestionAnswer.getDelete() && Objects.isNull(quizQuestionAnswer.getId())) {
            throw new BadRequestException("Answer id cannot be empty when delete");
        }
        try {
            if (Objects.nonNull(quizQuestionAnswer.getId()) && Objects.nonNull(quizQuestionAnswer.getDelete()) && quizQuestionAnswer.getDelete()) {
                repo.deleteById(quizQuestionAnswer.getId());
                return null;
            } else {
                return repo.save(quizQuestionAnswer);
            }
        } catch (Exception e) {
            log.error("ERROR: " + e.getLocalizedMessage());
            throw new InternalServerException("Quiz question answer" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Fetch all questions and answers of quiz by topicId, forStudent param is used for populating the correct answer
     * flag in the response because we don't want to return correct answer option when a student is taking a quiz, we
     * only have to send the correct answer option when an instructor want to modify his course.
     */
    @Override
    public QuizQuestionAnswer fetchAllQuestionAndAnswersByTopicId(Long topicId, boolean forStudent, Boolean random, Pageable pageable) {
        log.info("Fetching all the quiz's question and answers by section id.");
        QuizQuestionAnswer quizQuestionAnswer = new QuizQuestionAnswer();
        if (random) {
            Page<RandomQuizProjection> quizQuestions = repo.findAllQuizQuestionAndAnswersByTopicRandom(topicId, pageable);
            quizQuestionAnswer.setQuizQuestions(com.vinncorp.fast_learner.dtos.quiz.QuizQuestion.from(quizQuestions.getContent()));
            setPageMetaData(quizQuestionAnswer, quizQuestions);
        } else {
            Page<QuizQuestion> quizQuestions = repo.findAllQuizQuestionAndAnswersByTopic(topicId, pageable);
            quizQuestionAnswer.setQuizQuestions(quizQuestions.getContent());
            setPageMetaData(quizQuestionAnswer, quizQuestions);
        }
        this.setQuestionAnswers(quizQuestionAnswer, forStudent);
        quizQuestionAnswer.setTopicId(quizQuestionAnswer.getQuizQuestions().get(0).getTopicId());
        quizQuestionAnswer.setQuizId(quizQuestionAnswer.getQuizQuestions().get(0).getQuizId());
        return quizQuestionAnswer;
    }

    private void setPageMetaData(QuizQuestionAnswer target, Page<?> page) {
        target.setPageNumber(page.getNumber());
        target.setPageSize(page.getSize());
        target.setTotalPages(page.getTotalPages());
        target.setTotalElements(page.getTotalElements());
    }

    private void setQuestionAnswers(QuizQuestionAnswer quizQuestionAnswer, Boolean forStudent){
        quizQuestionAnswer.getQuizQuestions().forEach(quizQuestion -> {
            List<QuizAnswer> answers = this.repo.findByQuizQuestionIdCustom(quizQuestion.getQuestionId());
            if(forStudent){
                answers.forEach(answer -> answer.setIsCorrect(null));
            }
            quizQuestion.setQuizAnswers(answers);
        });
    }

    @Override
    public Message<QuizQuestionAnswerResponse> validateAnswer(Long questionId, Long answerId, String email)
            throws EntityNotFoundException, BadRequestException {
        log.info("Validating answer.");
        if (Objects.isNull(answerId)) {
            throw new BadRequestException("Please provide answer ID.");
        }

        List<QuizQuestionAnwser> quizQuestionAnswers = repo.findByQuizQuestionId(questionId);
        if (CollectionUtils.isEmpty(quizQuestionAnswers)) {
            throw new EntityNotFoundException("No data found for provided question and answer id.");
        }

        QuizQuestionAnwser quizQuestionAnwser = quizQuestionAnswers.stream().filter(e -> Objects.equals(e.getId(), answerId)).findAny()
                .orElseThrow(() -> new EntityNotFoundException("Question doesn't have any answer by provided id."));
        boolean isCorrect = quizQuestionAnwser.isCorrectAnswer();
        var correctAnswer = quizQuestionAnswers.stream().filter(QuizQuestionAnwser::isCorrectAnswer).findAny();
        if (correctAnswer.isEmpty())
            throw new EntityNotFoundException("Correct answer is not present for the question.");
        return new Message<QuizQuestionAnswerResponse>()
                .setCode(HttpStatus.OK.name())
                .setStatus(HttpStatus.OK.value())
                .setMessage(isCorrect ? "Answer is correct." : "Answer is incorrect.")
                .setData(QuizQuestionAnswerResponse.builder().isCorrect(isCorrect).correctAnswerId(correctAnswer.get().getId()).build());
    }

    @Override
    public Message<QuizAnswerResponse> validateAnswers(List<ValidationAnswerRequest> validationAnswerRequests, String name) throws BadRequestException {

        log.info("Checking if instructor exists with email: {}", name);
        Optional<User> instructorOpt = userRepository.findByEmail(name);
        if (!instructorOpt.isPresent()) {
            log.info("Instructor not found with email: {}", name);
            return new Message<QuizAnswerResponse>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.toString())
                    .setMessage("Instructor not found with email: " + name);
        }
        log.info("Validating answers for user: {}", name);

        List<QuestionAnswerResponse> questionAnswerResponses = new ArrayList<>();
        long totalCorrectAnswers = 0;
        Long totalAttemptQuestion = 0L;
        Long totalQuestion = 0L;
        if (validationAnswerRequests == null || validationAnswerRequests.isEmpty()) {
            throw new BadRequestException("Invalid Request Body: No question and answers provided");
        }
        // Fetching the passing criteria for the question
        Long passingPercentage = repo.findPassingCriteriaAndQuizIdByQuestionId(validationAnswerRequests.get(0).getQuestionId());

        if (passingPercentage == null) {
            throw new RuntimeException("No quiz data found for the provided question ID");
        }

        Double passingCriteria = passingPercentage == 0 ? 0.0 : passingPercentage;


        log.info("Passing criteria for the question: {}", passingCriteria);
        List<Long> listOfQuestions = new ArrayList<>();
        Quiz quiz = null;
        for (ValidationAnswerRequest request : validationAnswerRequests) {
            if (request.getAnswerId() != null) {
                totalAttemptQuestion++;
            }

            if (request.getAnswerId() == null) {
                request.setAnswerId(new ArrayList<>(List.of(0L)));
            }

            List<QuizQuestionAnwser> correctAnswers = repo.findByQuizQuestionId(request.getQuestionId());
            listOfQuestions.add(request.getQuestionId());

            List<Long> correctAnswerIds = correctAnswers.stream()
                    .filter(QuizQuestionAnwser::isCorrectAnswer)
                    .map(QuizQuestionAnwser::getId)
                    .collect(Collectors.toList());

            List<Long> correctlySelected = request.getAnswerId().stream()
                    .filter(correctAnswerIds::contains)
                    .collect(Collectors.toList());

            List<Long> incorrectlySelected = request.getAnswerId().stream()
                    .filter(id -> !correctAnswerIds.contains(id))
                    .collect(Collectors.toList());

            // 3. Missed correct answers: Correct answers the user did NOT select
            List<Long> missedCorrectAnswers = correctAnswerIds.stream()
                    .filter(id -> !correctAnswerIds.contains(id))
                    .collect(Collectors.toList());
            if (quiz == null) {
                quiz = correctAnswers.get(0).getQuizQuestion().getQuiz();
                totalQuestion = Long.valueOf(quiz.getRandomQuestion());
            }

            boolean isCorrect = incorrectlySelected.isEmpty() && missedCorrectAnswers.isEmpty();

            if (isCorrect) {
                totalCorrectAnswers++;
                log.info("Answer ID: {} is correct for question ID: {}", request.getAnswerId(), request.getQuestionId());
            } else {
                log.info("Answer ID: {} is incorrect for question ID: {}", request.getAnswerId(), request.getQuestionId());
            }

            List<AnswerResponse> answerResponseList = correctAnswers.stream()
                    .map(answer -> new AnswerResponse(answer.getId(), answer.getAnswer()))
                    .collect(Collectors.toList());


            QuestionAnswerResponse questionAnswerResponse = new QuestionAnswerResponse(
                    correctAnswers.get(0).getQuizQuestion() == null ? "Question not found" : correctAnswers.get(0).getQuizQuestion().getQuestionText(),
                    correctAnswerIds,
                    request.getAnswerId(),
                    isCorrect,
                    correctAnswers.get(0).getQuizQuestion().getExplanation(),
                    correctAnswers.get(0).getQuizQuestion().getQuestionType(),
                    answerResponseList
            );

            questionAnswerResponses.add(questionAnswerResponse);
        }

        List<Long> quizQuestions = quizQuestionRepository.findByQuizIdAndNotInQuestionId(quiz.getId(), listOfQuestions);
        if (!quizQuestions.isEmpty()) {
//            for (Long listQuestions:quizQuestions){
//                List<QuizQuestionAnwser> correctAnswers = repo.findByQuizQuestionId(listQuestions);
//                List<AnswerResponse> answerResponseList = correctAnswers.stream()
//                        .map(answer -> new AnswerResponse(answer.getId(), answer.getAnswer()))
//                        .collect(Collectors.toList());
//                QuestionAnswerResponse questionAnswerResponse = new QuestionAnswerResponse(
//                        correctAnswers.get(0).getQuizQuestion()==null?"Question not found":correctAnswers.get(0).getQuizQuestion().getQuestionText(),
////                        null,
//                        null,
//                        null,
//                        false,
//                        correctAnswers.get(0).getQuizQuestion().getExplanation(),
//                        correctAnswers.get(0).getQuizQuestion().getQuestionType(),
//                        answerResponseList
//                );
//                questionAnswerResponses.add(questionAnswerResponse);
//            }
        }

        double percentage = (totalQuestion > 0) ? ((double) totalCorrectAnswers / totalQuestion) * 100 : 0;
        log.info("Total correct answers: {} out of {}. Percentage: {}", totalCorrectAnswers, totalQuestion, percentage);
        // Create final quiz response
        QuizAnswerResponse quizAnswerResponse = new QuizAnswerResponse(
                percentage,
                passingCriteria,
                totalQuestion,
                totalCorrectAnswers,
                totalAttemptQuestion,
                questionAnswerResponses
        );
        Long attemptCount = 1L;
        QuizAttempt quizAttempt = quizAttemptRepository.findByQuizAndUser(quiz.getId(), instructorOpt.get().getId());
        if (quizAttempt != null) {
            attemptCount = quizAttempt.getTotalAttemptCount() + 1;
        }

        quizAttempt = QuizAttempt.builder()
                .id(quizAttempt == null ? null : quizAttempt.getId())
                .quiz(quiz)
                .totalCorrectAnswer(totalCorrectAnswers)
                .totalQuestion(totalQuestion)
                .user(instructorOpt.get())
                .obtainedPercentage(percentage)
                .attemptDate(LocalDate.now())
                .totalAttemptCount(attemptCount)
                .build();
        quizAttempt = quizAttemptRepository.save(quizAttempt);
        if (quizAttempt == null) {
            log.info("quiz attempt detail not save" + instructorOpt.get().getEmail());
        }

        log.info("Returning quiz response with percentage: {}, passing criteria: {}", percentage, passingCriteria);

        return new Message<QuizAnswerResponse>()
                .setCode(HttpStatus.OK.name())
                .setStatus(HttpStatus.OK.value())
                .setMessage("Quiz Response fetch successfully")
                .setData(quizAnswerResponse);
    }

}
