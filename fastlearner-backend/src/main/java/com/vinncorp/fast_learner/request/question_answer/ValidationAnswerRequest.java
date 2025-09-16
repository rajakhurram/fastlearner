package com.vinncorp.fast_learner.request.question_answer;


import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ValidationAnswerRequest {

    private List<Long> answerId;
    private Long questionId;

}
