package com.vinncorp.fast_learner.repositories.otp;

import com.vinncorp.fast_learner.models.otp.AuthenticationOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthenticationRepository extends JpaRepository<AuthenticationOtp, Long> {
    AuthenticationOtp findByEmail(String email);

    AuthenticationOtp findByOtp(int otp);
}
