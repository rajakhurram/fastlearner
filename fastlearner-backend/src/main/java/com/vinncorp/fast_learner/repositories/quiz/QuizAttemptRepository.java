package com.vinncorp.fast_learner.repositories.quiz;

import com.vinncorp.fast_learner.models.quiz.Quiz;
import com.vinncorp.fast_learner.models.quiz.QuizAttempt;
import com.vinncorp.fast_learner.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt,Long> {

    @Query(value = """
           select * from quiz_attempt where user_id=:userId and quiz_id=:quizId
            """,nativeQuery = true)
    QuizAttempt findByQuizAndUser(@Param("quizId") Long quizId,@Param("userId") Long userId);
}
