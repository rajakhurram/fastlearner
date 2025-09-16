package com.vinncorp.fast_learner.dtos.payment.invoice;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetail {
    private Long id;
    private String email;
    private String name;
}
