package com.vinncorp.fast_learner.models.payout.premium_course;

import com.vinncorp.fast_learner.util.enums.PayoutType;
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
@Table(name = "premium_course_payout_config")
public class PremiumCoursePayoutConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "percentage_cut", updatable = false, nullable = false)
    private double percentageCut;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_type", updatable = false, nullable = false)
    private PayoutType payoutType;

    @Column(name = "created_date", updatable = false)
    @Temporal(TIMESTAMP)
    @CreatedDate
    protected Date creationDate;

    private boolean isActive = true;
}
