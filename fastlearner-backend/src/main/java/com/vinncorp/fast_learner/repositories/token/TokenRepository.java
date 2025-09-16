package com.vinncorp.fast_learner.repositories.token;

import com.vinncorp.fast_learner.models.token.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Token findByToken(String token);

    boolean existsByToken(String token);

    Token findByCreatedBy(Long id);
}
