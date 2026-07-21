package com.vinncorp.fast_learner.repositories.user;

import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {

    Optional<UserDetails> findByUser(User user);
}
