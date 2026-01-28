package com.vinncorp.fast_learner.models.affiliate;

import com.vinncorp.fast_learner.util.enums.GenericStatus;
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
@Table(name = "affiliate")
public class Affiliate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    private String stripeAccountId;

    @Column(name = "created_stripe_date")
    private Date createdStripeDate;
    @Enumerated(value = EnumType.STRING)
    private PayoutStatus onboardStatus;

}
