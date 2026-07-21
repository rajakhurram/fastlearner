package com.vinncorp.fast_learner.dtos.affiliate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AffiliateCourseRequest {
    @NotNull(message = "Please provide course id")
    private Long courseId;
    @NotNull(message = "Please provide affiliate id")
    private Long affiliateId;
    @NotNull(message = "Please provide reward")
    @DecimalMin(value = "1.0", inclusive = true, message = "Reward must be at least 1%")
    @DecimalMax(value = "90.0", inclusive = true, message = "Reward cannot be greater than 90%")
    private Double reward;
}
