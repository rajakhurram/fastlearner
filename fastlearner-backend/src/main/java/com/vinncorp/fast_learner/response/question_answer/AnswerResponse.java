package com.vinncorp.fast_learner.response.question_answer;

import com.vinncorp.fast_learner.dtos.question_answer.AnswerDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnswerResponse {
    private List<AnswerDetail> answerDetail;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private long totalPages;
}
