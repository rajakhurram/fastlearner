package com.vinncorp.fast_learner.response.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkQuestionParseError {
    private int rowNumber;
    private String message;
}
