package com.vinncorp.fast_learner.models.stripe;

import com.vinncorp.fast_learner.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_withdrawal_history")
public class PaymentWithdrawalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String bankName;

    private double amount;

    private String payoutId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Date withdrawalAt;
}
