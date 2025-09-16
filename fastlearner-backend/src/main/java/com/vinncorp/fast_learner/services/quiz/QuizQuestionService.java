package com.vinncorp.fast_learner.services.quiz;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.repositories.quiz.QuizQuestionRepository;
import com.vinncorp.fast_learner.models.quiz.QuizQuestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizQuestionService implements IQuizQuestionService{

    private final QuizQuestionRepository repo;

    @Override
    public QuizQuestion save(QuizQuestion quizQuestion) throws InternalServerException {
        log.info("Creating quiz question.");
        try {
            if (Objects.nonNull(quizQuestion.getId()) && Objects.nonNull(quizQuestion.getDelete()) && quizQuestion.getDelete()) {
                repo.deleteById(quizQuestion.getId());
                return null;
            }else {
                return repo.save(quizQuestion);
            }
        } catch (Exception e) {
            log.error("ERROR: "+e.getLocalizedMessage());
            throw new InternalServerException("Quiz question"+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }
}
