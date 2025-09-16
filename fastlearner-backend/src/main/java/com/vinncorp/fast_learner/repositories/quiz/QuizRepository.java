package com.vinncorp.fast_learner.repositories.quiz;

import com.vinncorp.fast_learner.models.quiz.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}