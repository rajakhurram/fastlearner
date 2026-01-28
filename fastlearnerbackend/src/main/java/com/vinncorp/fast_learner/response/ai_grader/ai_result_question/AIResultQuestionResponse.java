package com.vinncorp.fast_learner.response.ai_grader.ai_result_question;

import com.vinncorp.fast_learner.response.ai_grader.ai_result.AIResultResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AIResultQuestionResponse {
    private Long id;
    private Long questionNumber;
    private String studentAnswer;
    private String correctAnswer;
    private Double score;
    private Double totalMarks;
    private String questionStatus;
    private String feedback;
    private String confidenceLevel;
    private Date creationDate;
    private Long aiResultId;
    private String status;

}
