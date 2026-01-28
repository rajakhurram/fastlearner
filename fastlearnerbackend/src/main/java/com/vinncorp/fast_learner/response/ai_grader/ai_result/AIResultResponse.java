package com.vinncorp.fast_learner.response.ai_grader.ai_result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIResultResponse {
    private Long id;
    private String studentName;
    private Long studentId;
    private String studentEmail;
    private Double grade;
    private Double score;
    private String confidenceLevel;
    private Date creationDate;

    private Long assignmentId;
    private String assignmentTitle;   // Optional: Add more assignment fields if needed

    private String resultStatus;
    private String status;

    private Boolean emailSent;
    private String quizFileId;
    private String fileUrl;
    private String fileId;
    private String rubricFileId;
    private String evaluationCriteria;
    private String assessmentName;
    private String className;
}
