package com.vinncorp.fast_learner.request.ai_grader.ai_result_question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateManualQuestionRequest {
    private Long aiResultId;
    private Long questionNumber;
    private String questionText;
    private Double obtainedMarks;
    private Double outOfMarks;
    private String feedback;
    private String studentAnswer;
    private String correctAnswer;
}
