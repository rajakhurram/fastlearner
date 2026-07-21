package com.vinncorp.fast_learner.mock.quiz;

import com.vinncorp.fast_learner.models.quiz.Quiz;
import com.vinncorp.fast_learner.models.quiz.QuizQuestion;
import com.vinncorp.fast_learner.models.quiz.QuizQuestionAnwser;

public class QuizTestData {

    public static Quiz quizData(){
        return Quiz.builder()
                .id(1L)
                .title("sample quiz")
                .delete(false)
                .build();
    }

    public static QuizQuestion quizQuestionData(){
        return QuizQuestion.builder()
                .id(1L)
                .questionText("What is Java?")
                .delete(false)
                .build();
    }

    public static QuizQuestionAnwser quizQuestionAnwser(){
        return  QuizQuestionAnwser.builder()
                .id(null)
                .delete(false)
                .quizQuestion(new QuizQuestion())
                .answer("Sample Answer")
                .isCorrectAnswer(true)
                .build();
    }

}
