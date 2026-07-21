package com.vinncorp.fast_learner.dtos.quiz;

import com.vinncorp.fast_learner.repositories.quiz.QuizQuestionAnwserRepository;
import com.vinncorp.fast_learner.util.enums.QuestionType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Getter
@Setter
@Slf4j
public class QuizQuestionAnswer {
    private Long topicId;
    private Long quizId;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private Long totalElements;
    List<QuizQuestion> quizQuestions;

    public static List<QuizQuestionAnswer> from(List<Object[]> dataList, Boolean forStudent) {
        Map<String, QuizQuestionAnswer> map = new HashMap<>();
        Map<String, QuizQuestion> questions = new HashMap<>();
        List<QuizQuestionAnswer> quizQuestionAnswers = new ArrayList<>();
        List<QuizQuestion> quizQuestions = new ArrayList<>();

        for (Object[] columns : dataList) {
            try {
                String topicId = String.valueOf(columns[0]);
                String quizId = String.valueOf(columns[1]);
                String questionId = String.valueOf(columns[2]);
                String questionText = String.valueOf(columns[3]);
//                String answerId = String.valueOf(columns[4]);
//                String answerText = String.valueOf(columns[5]);
//                Boolean isCorrect = forStudent ? null : Boolean.parseBoolean(String.valueOf(columns[6]));
                String questionType = String.valueOf(columns[4]);
                String explanation = String.valueOf(columns[5]);

                // Create or get QuizQuestionAnswer object
                QuizQuestionAnswer qqa = map.get(topicId);
                if (qqa == null) {
                    qqa = new QuizQuestionAnswer();
                    qqa.setTopicId(!topicId.equalsIgnoreCase("null") ? Long.parseLong(topicId) : null);
                    qqa.setQuizId(!quizId.equalsIgnoreCase("null") ? Long.parseLong(quizId) : null);
                    qqa.setQuizQuestions(new ArrayList<>());
                    quizQuestionAnswers.add(qqa);
                    map.put(topicId, qqa);
                }

                // Create or get QuizQuestion object
                QuizQuestion qq = questions.get(questionId);
                if (Objects.isNull(qq)) {
                    qq = new QuizQuestion();
                    qq.setQuestionId(!questionId.equalsIgnoreCase("null") ? Long.parseLong(questionId) : null);
                    qq.setQuestionText(questionText);
                    qq.setQuestionType(QuestionType.valueOf(questionType)); // Set the questionType
                    qq.setExplanation(explanation);
                    qq.setQuizAnswers(new ArrayList<>());
                    quizQuestions.add(qq);
                    questions.put(questionId, qq);
                    qqa.getQuizQuestions().add(qq);
                }

                // Create QuizAnswer object
                QuizAnswer qa = new QuizAnswer();
//                qa.setAnswerId(!answerId.equalsIgnoreCase("null") ? Long.parseLong(answerId) : null);
//                qa.setAnswerText(answerText);
//                qa.setIsCorrect(isCorrect);

                // Add answer to the question
                qq.getQuizAnswers().add(qa);
            } catch (NumberFormatException e) {
                // Log and skip invalid rows
                System.err.println("Invalid data row: " + Arrays.toString(columns) + " - " + e.getMessage());
            }
        }

        return quizQuestionAnswers;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuizQuestionAnswer that = (QuizQuestionAnswer) o;
        return topicId.equals(that.topicId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicId);
    }
}
