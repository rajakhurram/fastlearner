package com.vinncorp.fast_learner.response.payment_checkout;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vinncorp.fast_learner.response.subscription.ApiResponse;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateTransactionResponse extends ApiResponse {
    private TransactionResponse transactionResponse;
}