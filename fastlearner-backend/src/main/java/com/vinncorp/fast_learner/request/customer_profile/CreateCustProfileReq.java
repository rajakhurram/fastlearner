package com.vinncorp.fast_learner.request.customer_profile;

import lombok.Data;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

@Data
public class CreateCustProfileReq {
    @NotBlank
    private String cardNo;
    @NotBlank
    private String cardExpiry;
    @NotBlank
    private String firstname;
    @NotBlank
    private String lastname;
    @NotBlank
    private String cvv;

    @NotBlank
    private String email;
}
