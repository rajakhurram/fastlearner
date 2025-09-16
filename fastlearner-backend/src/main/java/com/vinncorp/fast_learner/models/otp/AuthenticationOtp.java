package com.vinncorp.fast_learner.models.otp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class AuthenticationOtp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int otp;
    private String email;
    private String name;
    private String password;
    private boolean subscribeNewsletter = true;
    private LocalDateTime localDateTime;
}
