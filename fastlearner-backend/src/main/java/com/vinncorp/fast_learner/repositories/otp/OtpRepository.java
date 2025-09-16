package com.vinncorp.fast_learner.repositories.otp;

import com.vinncorp.fast_learner.models.otp.Otp;
import com.vinncorp.fast_learner.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Otp findByUserIdAndValue(long id, int value);

    Optional<Otp> findByUserId(Long user);
}
