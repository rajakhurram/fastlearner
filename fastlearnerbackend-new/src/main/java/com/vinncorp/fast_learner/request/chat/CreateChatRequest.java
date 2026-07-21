package com.vinncorp.fast_learner.request.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateChatRequest {

    @NotNull(message = "courseId field is required.")
    private Long courseId;

    @NotNull(message = "topicId field is required.")
    private Long topicId;

    @NotBlank(message = "question field is required.")
    private String question;

    @NotBlank(message = "time field is required.")
    private String time;
}
