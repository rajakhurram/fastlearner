package com.vinncorp.fast_learner.request.coupon;

import com.vinncorp.fast_learner.util.enums.CouponAppliesTo;
import com.vinncorp.fast_learner.util.enums.CouponType;
import com.vinncorp.fast_learner.util.enums.DiscountUnit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CouponRequest {
    private Long id;
    @Size(min = 6, max = 12, message = "Promo code must be 6 to 12 characters")
    private String coupon;
    private double discount;
    @NotNull
    private DiscountUnit discountUnit;
    private Long subscriptionId;
    private Date startDate;
    private Date endDate;
    private List<Long> specifiedCourses;
    private List<String> specifiedUsers;
    private List<String> specifiedEmailDomains;
    @NotNull
    private int durationInMonth;
    private boolean allowAllCourse = false;
    @NotNull
    private CouponAppliesTo appliesTo;
    private Boolean isActive;
    private CouponType couponType;
    private String billingCycle;
}
