package com.vinncorp.fast_learner.request.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class OpaqueDataType {
    private String dataDescriptor;
    private String dataValue;
}
