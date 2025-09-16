package com.vinncorp.fast_learner.repositories.subscription;

import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.SubscribedUserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscribedUserProfileRepository extends JpaRepository<SubscribedUserProfile, Long> {
    Optional<SubscribedUserProfile> findByIsDefaultAndSubscribedUser(Boolean isDefault, SubscribedUser user);

    @Modifying
    @Transactional(propagation = Propagation.REQUIRED)
    @Query(value = """
            UPDATE subscribed_user_profile SET is_default = false WHERE subscribed_user_id = :subscribedUserId
            """,nativeQuery = true)
    void markAllAsNotDefaultById(Long subscribedUserId);

    Optional<SubscribedUserProfile> findByCustomerPaymentProfileId(String customerPaymentProfileId);

    List<SubscribedUserProfile> findAllBySubscribedUserOrderByIdDesc(SubscribedUser user);

    void deleteAllBySubscribedUserId(Long id);

    SubscribedUserProfile findByIsDefaultAndSubscribedUserId(boolean defaultStatus, Long subscribedUserId);
}
