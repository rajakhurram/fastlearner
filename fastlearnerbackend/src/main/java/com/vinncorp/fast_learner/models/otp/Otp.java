package com.vinncorp.fast_learner.models.otp;

import com.vinncorp.fast_learner.models.user.User;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "otp")
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "value", nullable = false)
    private int value;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
