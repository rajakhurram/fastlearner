package com.vinncorp.fast_learner.response.ai_grader.ai_result_question;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AIResultQueRespByPaginated {
    List<AIResultQuestionResponse> aiResultQueResponseList;
    private String fileUrl;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int pages;
}
