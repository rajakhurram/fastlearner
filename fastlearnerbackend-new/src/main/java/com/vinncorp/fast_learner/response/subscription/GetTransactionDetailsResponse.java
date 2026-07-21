package com.vinncorp.fast_learner.response.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetTransactionDetailsResponse extends ApiResponse{
    private TransactionDetailsType transaction;
    private String clientId;
    private String transrefId;
}
