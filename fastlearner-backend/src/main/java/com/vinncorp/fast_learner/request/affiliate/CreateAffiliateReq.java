package com.vinncorp.fast_learner.request.affiliate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class CreateAffiliateReq {

    private Long instructorAffiliateId;
    @NotBlank(message = "Email should not be blank")
    private String email;
    @NotBlank(message = "Name should not be blank")
    private String name;
    @NotBlank(message = "Nickname should not be blank")
    private String nickName;
    @NotNull(message = "Default Reward should not be null")
    @Positive(message = "Default Reward should be positive")
    private Double defaultReward;



}
