package com.vinncorp.fast_learner.repositories.quiz;

import com.vinncorp.fast_learner.models.quiz.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    @Query(value = """
            select * from quiz_question where quiz_id =:quizId
            """,nativeQuery = true)
    List<QuizQuestion> findByQuizId(Long quizId);

    @Query(value = """
    SELECT id 
    FROM quiz_question 
    WHERE quiz_id = :quizId 
    AND id NOT IN (:listOfQuestions)
    """, nativeQuery = true)
    List<Long> findByQuizIdAndNotInQuestionId(@Param("quizId") Long quizId, @Param("listOfQuestions") List<Long> listOfQuestions);
}