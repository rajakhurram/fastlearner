package com.vinncorp.fast_learner.response.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties
public class GetTransactionListResponse extends ApiResponse {
    private ArrayOfTransactionSummaryType transactions;
    private Integer totalNumInResultSet;
}
