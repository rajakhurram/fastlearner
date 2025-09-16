package com.vinncorp.fast_learner.models.subscription;

import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.util.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "subscribed_user")
public class SubscribedUser {

    /**
     * Below constructor only use for deep copy of the subscribed user object
     * */
    public SubscribedUser(SubscribedUser subscribedUser) {
        this.id = subscribedUser.getId();
        this.user = subscribedUser.getUser();
        this.subscription = subscribedUser.getSubscription();
        this.startDate = subscribedUser.getStartDate();
        this.endDate = subscribedUser.getEndDate();
        this.paymentStatus = subscribedUser.getPaymentStatus();
        this.paymentSubscriptionId = subscribedUser.getPaymentSubscriptionId();
        this.customerProfileId = subscribedUser.getCustomerProfileId();
        this.subscribedId = subscribedUser.getSubscribedId();
        this.coupon = subscribedUser.getCoupon();
        this.couponValidTill = subscribedUser.getCouponValidTill();
        this.isActive = subscribedUser.getUser().isActive();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(name = "paypal_subscription_id")
    private String paypalSubscriptionId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_date")
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date")
    private Date endDate;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "payment_subscription_id")
    private String paymentSubscriptionId;

    @Column(name = "customer_profile_id")
    private String customerProfileId;
    @Column(name = "subscribed_id")
    private String subscribedId;

    @ManyToOne
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "coupon_valid_till")
    private Date couponValidTill;

    private boolean isActive = true;

}