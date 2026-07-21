package com.vinncorp.fast_learner.response.affiliate;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AffiliationResponse {

    private Long instructorAffiliateId;
    private Long affiliateId;
    private String uuid;
    private String name;
    private String nickName;
    private String email;
    private double defaultReward;
    private Boolean isSelf;

}
