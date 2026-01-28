package com.vinncorp.fast_learner.response.docs;

import com.vinncorp.fast_learner.dtos.docs.DocumentSummary;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSummaryResponse {

    private String code;
    private String message;
    private DocumentSummary data;
    private int status;
}
