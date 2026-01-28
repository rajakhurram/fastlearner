package com.vinncorp.fast_learner.response.affiliate;


import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AffiliateDetailResponse {
    private Long affiliateId;
    private Long instructorAffiliateId;
    private String name;
    private String nickName;
    private String email;
    private double rewards;
    private double totalRevenue;
    private String onboardStatus;
    private Long totalOnboardedStudent;
    private Boolean isSelf;

    //
}
