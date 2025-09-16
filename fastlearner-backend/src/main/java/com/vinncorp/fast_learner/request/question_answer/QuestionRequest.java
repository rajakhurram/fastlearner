package com.vinncorp.fast_learner.request.question_answer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionRequest {

    @NotNull(message = "text field is required")
    @NotBlank(message = "text field is required")
    private String text;

    @NotNull(message = "topicId field is required")
    private Long topicId;

    @NotNull(message = "courseId field is required")
    private Long courseId;
}
