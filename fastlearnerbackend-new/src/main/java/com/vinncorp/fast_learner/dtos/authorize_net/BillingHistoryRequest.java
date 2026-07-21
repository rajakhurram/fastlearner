package com.vinncorp.fast_learner.dtos.authorize_net;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class BillingHistoryRequest {

    @Min(value = 1 ,message = "Page No should be equal or greater than 0")
    private int pageNo;

    @Min(value = 1 ,message = "Page Size should be equal or greater than 1")
    private int pageSize;
}
