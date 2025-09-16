package com.vinncorp.fast_learner.models.transaction_history;


import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.PaymentStatus;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transaction_history")
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date creationAt;
    @Column(name = "auth_subscription_id",nullable = false,updatable = false)
    private String authSubscriptionId;

    @Column(name = "subscription_amount")
    private Double subscriptionAmount;

    @Enumerated(value = EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;

    @Column(name = "response_code")
    private String responseCode;
    @Column(name = "response_text", columnDefinition = "text")
    private String responseText;
    @Column(name = "customer_payment_profile_id")
    private String customerPaymentProfileId;

    @Column(name = "updated_date")
    private Date updatedDate;

    @Column(name = "trial_end_date")
    private Date trialEndDate;

    @Column(name = "subscription_next_cycle")
    private Date subscriptionNextCycle;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "old_transaction_id")
    private Long oldTransactionId ;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GenericStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "settled_date")
    private LocalDateTime settledDate;

    @Column(name = "external_transaction_id")
    private String externalTransactionId;

    @Column(name = "deducted_amount")
    private Double deductedAmount;

    @ManyToOne
    private Coupon coupon;

    public static TransactionHistory from(TransactionHistory e){
        return TransactionHistory.builder()
                .id(e.getId())
                .authSubscriptionId(e.getAuthSubscriptionId())
                .creationAt(e.getCreationAt())
                .customerPaymentProfileId(e.getCustomerPaymentProfileId())
                .status(e.getStatus())
                .subscriptionAmount(e.getSubscriptionAmount())
                .trialEndDate(e.getTrialEndDate())
                .subscription(e.getSubscription())
                .customerPaymentProfileId(e.getCustomerPaymentProfileId())
                .subscriptionNextCycle(e.getSubscriptionNextCycle())
                .user(e.getUser())
                .subscriptionStatus(e.getSubscriptionStatus())
                .oldTransactionId(e.getOldTransactionId())
                .responseText(e.getResponseText())
                .updatedDate(e.getUpdatedDate())
                .build();
    }

}
