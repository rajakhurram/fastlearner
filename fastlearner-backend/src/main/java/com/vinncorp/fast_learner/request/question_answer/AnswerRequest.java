package com.vinncorp.fast_learner.request.question_answer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerRequest {

    @NotNull(message = "text field is required")
    @NotBlank(message = "text field is required")
    private String text;
    private Long answerId;

    @NotNull(message = "questionId field is required")
    private Long questionId;

    @NotNull(message = "courseId field is required")
    private Long courseId;
}
