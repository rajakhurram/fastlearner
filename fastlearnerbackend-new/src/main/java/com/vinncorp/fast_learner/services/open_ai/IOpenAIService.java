package com.vinncorp.fast_learner.services.open_ai;

import com.vinncorp.fast_learner.request.quiz.QuizQuestionReport;
import com.vinncorp.fast_learner.response.quiz.QuizReportResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IOpenAIService {
    Boolean validateAnswer(String question,String correctAnswer, String userAnswer);
    QuizReportResult fetchQuizTopicReport(List<QuizQuestionReport> quizQuestionReports);
}
