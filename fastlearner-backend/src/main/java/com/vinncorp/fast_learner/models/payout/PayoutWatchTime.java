package com.vinncorp.fast_learner.models.payout;

import com.vinncorp.fast_learner.util.enums.PaymentStatus;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import com.vinncorp.fast_learner.util.enums.PayoutType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payout_watch_time")
public class PayoutWatchTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "time_spend")
    private long timeSpend;

    @Column(name = "amount_share_per_day")
    private Double amountSharePerDay;

    @Column(name = "no_of_days")
    private Integer noOfDays;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "instructor_id")
    private Long instructorId;

    @Column(name = "stripe_response",length = 1000)
    private String stripeResponse;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "payout_calculated_at")
    private Date payoutCalculatedAt;

    @Enumerated(value = EnumType.STRING)
    private PayoutStatus payoutStatus;

    @Column(name = "payout_for_current_month")
    private int payoutForCurrentMonth;

    @Column(name = "payout_for_current_year")
    private int payoutForCurrentYear;

    @Column(name = "external_transaction_id")
    private String externalTransactionId;

    @Column(name = "settled_date")
    private LocalDateTime settledDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;
}
