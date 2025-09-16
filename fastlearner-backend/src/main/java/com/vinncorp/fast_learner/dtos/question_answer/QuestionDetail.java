package com.vinncorp.fast_learner.dtos.question_answer;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDetail {

    private Long questionId;
    private String questionText;
    private Long topicId;
    private String topicName;
    private Long courseId;
    private String userName;
    private String profileImage;
    private String userProfileUrl;
    private Long createdBy;
    private long totalReplies;
    private List<AnswerDetail> answerDetail;

    public static List<QuestionDetail> from(List<Tuple> rawData, String username) {
        return rawData.stream()
                .map(e -> QuestionDetail.builder()
                        .questionId((Long) e.get("question_id"))
                        .questionText((String) e.get("question_text"))
                        .totalReplies(e.get("no_of_answers") != null ? Integer.parseInt("" + e.get("no_of_answers")) : 0)
                        .topicId((Long) e.get("topic_id"))
                        .topicName((String) e.get("topic_name"))
                        .courseId((Long) e.get("course_id"))
                        .userName((String) e.get("full_name"))
                        .profileImage((String) e.get("profile_picture"))
                        .userProfileUrl((String) e.get("profile_url"))
                        .createdBy((Long) e.get("created_by"))
                        .build())
                .collect(Collectors.toList());
    }

    public static QuestionDetail from(Tuple rawData, String username) {
                       return QuestionDetail.builder()
                        .questionId((Long) rawData.get("question_id"))
                        .questionText((String) rawData.get("question_text"))
                        .totalReplies(rawData.get("no_of_answers") != null ? Integer.parseInt("" + rawData.get("no_of_answers")) : 0)
                        .topicId((Long) rawData.get("topic_id"))
                        .topicName((String) rawData.get("topic_name"))
                        .courseId((Long) rawData.get("course_id"))
                        .userName((String) rawData.get("full_name"))
                        .profileImage((String) rawData.get("profile_picture"))
                        .createdBy((Long) rawData.get("created_by"))
                        .build();
    }
}
