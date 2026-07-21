package com.vinncorp.fast_learner.response.subscription;

import lombok.Data;

import java.util.List;

@Data
public class ArrayOfFraudFilterType {
    protected List<String> fraudFilter;
}
