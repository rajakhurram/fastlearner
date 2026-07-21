package com.vinncorp.fast_learner.repositories.quiz;

import com.vinncorp.fast_learner.models.quiz.Quiz;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.util.enums.TestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByQuizQuestions_Id(Long questionId);

    @Query("SELECT q.testType FROM Quiz q WHERE q.topic.id = :topicId")
    Optional<TestType> findTestTypeByTopicId(@Param("topicId") Long topicId);
}