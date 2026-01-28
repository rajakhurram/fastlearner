package com.vinncorp.fast_learner.repositories.quiz;

import com.vinncorp.fast_learner.models.quiz.QuizAttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptAnswerRepository extends JpaRepository<QuizAttemptAnswer, Long> {

    List<QuizAttemptAnswer> findByQuizAttemptId(Long quizAttemptId);

    @Query(value = """
   select * FROM quiz_attempt_answer 
   where quiz_attempt_id=:attemptId and quiz_question_id=:questionId
   """, nativeQuery = true)
    List<QuizAttemptAnswer> findByQuizAttemptIdAndQuestionId(@Param("attemptId") Long attemptId,
                                                             @Param("questionId") Long questionId);

}