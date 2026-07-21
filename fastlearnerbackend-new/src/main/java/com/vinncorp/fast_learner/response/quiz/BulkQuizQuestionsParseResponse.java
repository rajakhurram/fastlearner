package com.vinncorp.fast_learner.response.quiz;

import com.vinncorp.fast_learner.request.quiz.CreateQuizQuestionRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkQuizQuestionsParseResponse {
    private List<CreateQuizQuestionRequest> questions;
    private List<BulkQuestionParseError> errors;
    private String excelFormatGuide;
    private int totalRowsParsed;
    private int successCount;
    private int errorCount;
}
