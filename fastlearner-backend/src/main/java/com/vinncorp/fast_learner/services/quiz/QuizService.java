package com.vinncorp.fast_learner.services.quiz;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.repositories.quiz.QuizRepository;
import com.vinncorp.fast_learner.models.quiz.Quiz;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService implements IQuizService{

    private final QuizRepository repo;

    @Override
    public Quiz save(Quiz quiz) throws InternalServerException {
        log.info("Creating quiz.");
        try {
            if(Objects.nonNull(quiz.getId()) && Objects.nonNull(quiz.getDelete()) && quiz.getDelete()) {
                repo.deleteById(quiz.getId());
                return null;
            }else{
                return repo.save(quiz);
            }
        } catch (Exception e) {
            log.error("ERROR: "+e.getLocalizedMessage());
            throw new InternalServerException("Quiz"+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Message<String> deleteQuizById(Long id) throws InternalServerException{
        log.info("Deleting a quiz from course...");
        try {
            repo.deleteById(id);
            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setData("Quiz is deleted successfully.")
                    .setMessage("Quiz is deleted successfully.");
        } catch (Exception e) {
            log.error("Deleting quiz is not successful.");
            throw new InternalServerException("Quiz " + InternalServerException.NOT_DELETE_INTERNAL_SERVER_ERROR);
        }
    }
}
