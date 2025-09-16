package com.vinncorp.fast_learner.models.payout;

import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "instructor_sales")
public class InstructorSales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "instructor_id")
    private long instructorId;

    @Column(name = "stripe_account_id")
    private String stripeAccountId;

    @Column(name = "total_sales")
    private double totalSales;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PayoutStatus status;

    @Column(name = "payout_batch_id")
    private String payoutBatchId;

    @CreatedDate
    @Column(name = "creation_date", nullable = false, updatable = false)
    private Date creationDate;

}
