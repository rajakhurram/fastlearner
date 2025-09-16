package com.vinncorp.fast_learner.dtos.quiz;

import com.vinncorp.fast_learner.util.enums.QuestionType;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class QuizQuestion {

    private Long topicId;
    private Long quizId;
    private Long questionId;
    private String questionText;
    private QuestionType questionType;
    private String explanation;
    private List<QuizAnswer> quizAnswers = new ArrayList<>();

    public QuizQuestion() {
    }

    public QuizQuestion(Long questionId, String questionText , QuestionType questionType) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.questionType = questionType;
    }

    public QuizQuestion(Long topicId, Long quizId, Long questionId, String questionText, QuestionType questionType, String explanation) {
        this.topicId = topicId;
        this.quizId = quizId;
        this.questionId = questionId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.explanation = explanation;
    }

    public static List<QuizQuestion> from(List<RandomQuizProjection> randomQuizProjections){
        return randomQuizProjections.stream()
                .map(rq -> new QuizQuestion(rq.getTopicId(), rq.getQuizId(), rq.getQuestionId(), rq.getQuestionText(), QuestionType.valueOf(rq.getQuestionType()), rq.getExplanation())).collect(Collectors.toList());
    }

}
