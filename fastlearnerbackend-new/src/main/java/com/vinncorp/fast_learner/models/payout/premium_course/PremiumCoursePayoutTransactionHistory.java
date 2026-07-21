package com.vinncorp.fast_learner.models.payout.premium_course;

import com.vinncorp.fast_learner.models.affiliate.InstructorAffiliate;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import com.vinncorp.fast_learner.util.enums.PayoutType;
import com.vinncorp.fast_learner.util.enums.StripeAccountStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

import static jakarta.persistence.TemporalType.TIMESTAMP;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "premium_course_payout_transaction_history")
public class PremiumCoursePayoutTransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "payout_id")
    private String payoutId;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_type", nullable = false, updatable = false)
    private PayoutType payoutType;

    @Column(name = "created_date", updatable = false)
    @Temporal(TIMESTAMP)
    @CreatedDate
    protected Date creationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_status", nullable = false)
    private PayoutStatus payoutStatus;

    @Column(name = "amount", nullable = false, updatable = false)
    private double amount = 0.0;

    @Column(name = "stripe_response",length = 1000)
    private String stripeResponse;

    @ManyToOne
    @JoinColumn(name = "instructor_affiliate")
    private InstructorAffiliate instructorAffiliate;

    @Enumerated(EnumType.STRING)
    @Column(name = "stripe_account_status")
    private StripeAccountStatus stripeAccountStatus;
    @ManyToOne
    private Coupon coupon;
}
