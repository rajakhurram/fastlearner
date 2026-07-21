package com.vinncorp.fast_learner.models.coupon;

import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.util.enums.CouponAppliesTo;
import com.vinncorp.fast_learner.util.enums.CouponType;
import com.vinncorp.fast_learner.util.enums.DiscountUnit;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coupon")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_date")
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "discount", nullable = false)
    private Double discount;

    @Column(name = "redeem_code", unique = true, nullable = false)
    private String redeemCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type")
    private CouponType couponType;

    @Column(name = "billing_cycle")
    private String billingCycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_unit")
    private DiscountUnit discountUnit;

    @Column(name = "is_restricted")
    private boolean isRestricted;

    @Column
    private Boolean allowAllCourse;

    @Column
    private int durationInMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to")
    private CouponAppliesTo appliesTo;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
