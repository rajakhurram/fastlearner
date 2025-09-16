package com.vinncorp.fast_learner.services.subscription;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionRepository;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService implements ISubscriptionService{

    private final SubscriptionRepository repo;
    private final IUserService userService;

    @Override
    public Message<List<Subscription>> fetchAllSubscription(String email) throws EntityNotFoundException {
        log.info("Fetching all subscriptions.");
        List<Subscription> subscriptions = repo.findAllByIsActive(true);
        if (CollectionUtils.isEmpty(subscriptions)) {
            log.error("No subscription plan found.");
            throw new EntityNotFoundException("No subscription plan found.");
        }
        log.info("All subscription is fetched successfully.");
        return new Message<List<Subscription>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("All subscription is fetched successfully.")
                .setData(subscriptions);
    }

    @Override
    public Message<Subscription> findBySubscriptionId(Long subscriptionId) throws EntityNotFoundException {
        log.info("Fetching subscription by subscription id: "+subscriptionId);

        Subscription subscription = repo.findById(subscriptionId)
                 .orElseThrow(() -> new EntityNotFoundException("No subscription found by provided subscription id."));

        return new Message<Subscription>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Subscription is fetched successfully.")
                .setData(subscription);
    }

    @Override
    public Message<Boolean> isSubscibed(String email) throws EntityNotFoundException {
        log.info("Verify the user's subscription.");
        User user = userService.findByEmail(email);
        if (user.isSubscribed()) {
            return new Message<Boolean>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.toString())
                    .setData(true)
                    .setMessage("User is subscribed.");
        }
        return new Message<Boolean>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setData(false)
                .setMessage("User is not subscribed a plan.");
    }
}
