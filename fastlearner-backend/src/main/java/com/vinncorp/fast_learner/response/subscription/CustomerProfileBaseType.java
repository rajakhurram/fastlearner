package com.vinncorp.fast_learner.response.subscription;

import lombok.Data;

@Data
public class CustomerProfileBaseType {
    protected String merchantCustomerId;
    protected String description;
    protected String email;
}
