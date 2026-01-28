package com.vinncorp.fast_learner.request.coupon;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponRequest {
    private Long id;
    @Size(min = 6, max = 12)
    private String coupon;
    private double discount;
    private Long subscriptionId;
    private Date startDate;
    private Date endDate;
    private String couponType;
    private List<Long> specifiedCourses;
    private List<String> specifiedUsers;
    private List<String> specifiedEmailDomains;
    @NotNull
    private int durationInMonth;
    private boolean allowAllCourse = false;
    private Boolean isActive;
}
