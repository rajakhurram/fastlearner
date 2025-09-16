package com.vinncorp.fast_learner.repositories.user_session;

import com.vinncorp.fast_learner.models.user_session.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession,Long> {

    UserSession findBySessionId(String sessionId);

}
