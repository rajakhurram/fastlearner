package com.vinncorp.fast_learner.dtos.question_answer;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDetail {

    private Long answerId;
    private String answerText;
    private List<AnswerDetail> answerDetailList;
    private String userName;
    private String profileImage;
    private String userProfileUrl;

    public static List<AnswerDetail> from(Page<Tuple> rawData, String userName) {

        return rawData.stream().map(e ->
            AnswerDetail.builder()
                    .answerId((Long) e.get("answer_id"))
                    .answerText((String) e.get("answer_text"))
                    .userName((String) e.get("full_name"))
                    .profileImage((String) e.get("profile_picture"))
                    .userProfileUrl((String) e.get("profile_url"))
                    .build()
        ).toList();
    }
}
