package com.vinncorp.fast_learner.services.quiz;

import com.vinncorp.fast_learner.dtos.quiz.QuizQuestionAnwserDto;
import com.vinncorp.fast_learner.dtos.quiz.QuizQuestionDto;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.quiz.Quiz;
import com.vinncorp.fast_learner.models.quiz.QuizAttempt;
import com.vinncorp.fast_learner.models.quiz.QuizAttemptAnswer;
import com.vinncorp.fast_learner.models.quiz.QuizQuestion;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.quiz.QuizAttemptAnswerRepository;
import com.vinncorp.fast_learner.repositories.quiz.QuizAttemptReportRepository;
import com.vinncorp.fast_learner.repositories.quiz.QuizAttemptRepository;
import com.vinncorp.fast_learner.repositories.quiz.QuizRepository;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.response.quiz.QuizAttemptResponse;
import com.vinncorp.fast_learner.response.quiz.QuizResponse;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.ReportStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizAttemptService implements IQuizAttemptService {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private UserRepository userRepository;

    private final QuizRepository quizRepository;
    private final QuizAttemptReportRepository quizAttemptReportRepository;

    private final QuizAttemptAnswerRepository quizAttemptAnswerRepository;

    @Override
    @Transactional(readOnly = true)
    public Message<QuizAttemptResponse> quizAttempt(Long quizId, String userEmail) throws InternalServerException {

        // 1️⃣ Find user
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return new Message<QuizAttemptResponse>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.name())
                    .setData(null)
                    .setMessage("User not found.");
        }
        User user = userOpt.get();

        // 2️⃣ Find quiz
        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        if (quizOpt.isEmpty()) {
            return new Message<QuizAttemptResponse>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.name())
                    .setData(null)
                    .setMessage("Quiz not found.");
        }
        Quiz quiz = quizOpt.get();


        // 3️⃣ Find attempt
        QuizAttempt attempt = quizAttemptRepository.findByQuizAndUser(quiz, user);
        if (attempt == null) {
            return new Message<QuizAttemptResponse>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.name())
                    .setData(null)
                    .setMessage("Quiz Attempt is not found.");
        }

        var attemptReportOptional = quizAttemptReportRepository.findFirstByQuizAttemptIdAndStatusOrderByCreatedAtDesc(attempt.getId(), ReportStatus.READY);

//        if(Objects.isNull(attemptReportOptional) || attemptReportOptional.isEmpty()) {
//            return new Message<QuizAttemptResponse>()
//                    .setStatus(HttpStatus.NOT_FOUND.value())
//                    .setCode(HttpStatus.NOT_FOUND.name())
//                    .setData(null)
//                    .setMessage("Quiz report not found");
//        }
        var attemptReport = attemptReportOptional.isEmpty()?"":attemptReportOptional.get().getHtml();

        List<QuizAttemptAnswer> attemptAnswers =
                quizAttemptAnswerRepository.findByQuizAttemptId(attempt.getId());

        List<QuizQuestionDto> questionDtos = attemptAnswers.stream()
                .map(attemptAnswer -> {

                    QuizQuestion question = attemptAnswer.getQuizQuestion();

                    QuizQuestionDto dto = new QuizQuestionDto();
                    dto.setQuestionText(question.getQuestionText());
                    dto.setQuestionImageUrl(question.getQuestionImageUrl());
                    dto.setExplanation(question.getExplanation());

                    // ✅ ALL OPTIONS (same as old logic)
                    List<QuizQuestionAnwserDto> answerDtos =
                            question.getQuizQuestionAnwsers() == null
                                    ? List.of()
                                    : question.getQuizQuestionAnwsers()
                                    .stream()
                                    .map(a -> new QuizQuestionAnwserDto(
                                            a.getId(),
                                            a.getAnswer(),
                                            a.getAnswerImageUrl(),
                                            a.isCorrectAnswer(),
                                            question.getQuestionType()
                                    ))
                                    .toList();

                    dto.setQuizQuestionAnwsers(answerDtos);

                    // ✅ STUDENT ANSWERS
                    dto.setSelectedAnswerIds(attemptAnswer.getSelectedAnswerIds());
                    dto.setAnswerText(attemptAnswer.getAnswerText());
                    dto.setIsCorrect(attemptAnswer.getIsCorrect());

                    return dto;
                })
                .toList();


        // 4️⃣ Map quiz questions + answers to DTOs
//        List<QuizQuestionDto> questionDtos = attempt.getQuiz().getQuizQuestions() == null ? List.of() :
//                attempt.getQuiz().getQuizQuestions()
//                        .stream()
//                        .map(q -> {
//                            QuizQuestionDto dto = new QuizQuestionDto();
//                            dto.setQuestionText(q.getQuestionText());
//                            dto.setExplanation(q.getExplanation());
//
//                            // Correct answers
//                            List<QuizQuestionAnwserDto> answerDtos = q.getQuizQuestionAnwsers() == null ? List.of() :
//                                    q.getQuizQuestionAnwsers()
//                                            .stream()
//                                            .map(a -> new QuizQuestionAnwserDto(a.getId(),a.getAnswer(), a.isCorrectAnswer(), a.getQuizQuestion().getQuestionType()))
//                                            .toList();
//                            dto.setQuizQuestionAnwsers(answerDtos);
//
//                            // Student answers
//                            attempt.getAttemptAnswers()
//                                    .stream()
//                                    .filter(a -> a.getQuizQuestion().getId().equals(q.getId()))
//                                    .findFirst()
//                                    .ifPresent(a -> {
//                                        dto.setSelectedAnswerIds(a.getSelectedAnswerIds());
//                                        dto.setAnswerText(a.getAnswerText());
//                                        dto.setIsCorrect(a.getIsCorrect());
//                                    });
//
//                            return dto;
//                        })
//                        .toList();


        // 5️⃣ Build QuizResponse
        QuizResponse quizResponse = new QuizResponse();
        quizResponse.setId(attempt.getQuiz().getId());
        quizResponse.setTitle(attempt.getQuiz().getTitle());
        quizResponse.setDurationInMinutes(attempt.getQuiz().getDurationInMinutes());
        quizResponse.setPassingCriteria(attempt.getQuiz().getPassingCriteria());
        quizResponse.setRandomQuestion(attempt.getQuiz().getRandomQuestion());
        quizResponse.setTestType(attempt.getQuiz().getTestType());
        quizResponse.setGenerateAIReport(attempt.getQuiz().getGenerateAIReport());
        quizResponse.setReportPrompt(attempt.getQuiz().getReportPrompt());
        quizResponse.setQuizQuestions(questionDtos);

        // 6️⃣ Build QuizAttemptResponse
        QuizAttemptResponse response = new QuizAttemptResponse();
        response.setTotalQuestion(attempt.getTotalQuestion());
        response.setTotalCorrectAnswer(attempt.getTotalCorrectAnswer());
        response.setObtainedPercentage(attempt.getObtainedPercentage());
        response.setTotalAttemptCount(attempt.getTotalAttemptCount());
        response.setAttemptDate(attempt.getAttemptDate());
        response.setQuiz(quizResponse);
        response.setIsAllowedToRetake(quiz.getIsAllowedToRetake());
        response.setHtml(attemptReport);
        response.setAttemptedQuestionCount(attempt.getAttemptedQuestionCount());
        response.setTopicComprehensive(quiz.getTopic().getTopicComprehensive());

        // 7️⃣ Return wrapped message
        return new Message<QuizAttemptResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setData(response)
                .setMessage("Quiz Attempt is fetched successfully.");
    }
}
