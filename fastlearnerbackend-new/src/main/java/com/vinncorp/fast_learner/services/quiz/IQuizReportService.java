package com.vinncorp.fast_learner.services.quiz;

import com.vinncorp.fast_learner.models.quiz.QuizAttemptReport;
import com.vinncorp.fast_learner.util.Message;

public interface IQuizReportService {

    Message<QuizAttemptReport> getByQuizAttemptId(String quizAttemptId);
}
