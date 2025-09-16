package com.vinncorp.fast_learner.integration.subscription.process;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.integration.subscription.SubscriptionTestData;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionService;
import com.vinncorp.fast_learner.services.subscription.process.SubscriptionStrategyService;
import com.vinncorp.fast_learner.services.user.IUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UpdateProcessIntegrationTest {

    @Qualifier(value = "update")
    @Autowired
    private SubscriptionStrategyService updateProcess;

    @Autowired
    private IUserService userService;
    @Autowired
    private ISubscriptionService subscriptionService;
    @Autowired
    private ISubscribedUserService subscribedUserService;

    @Test
    public void testUpgradeFromFreeToPaid_whenProvidedDataIsValid() throws EntityNotFoundException, InternalServerException, BadRequestException {
        var user = userService.findByEmail("substest@yopmail.com");
        var subscription = subscriptionService.findBySubscriptionId(2L).getData();
        var subscribedUser = subscribedUserService.findByUser(user.getEmail());
        var m = updateProcess.processSubscription(SubscriptionTestData.subscriptionRequest(), subscribedUser, subscription, user, null);

        assertThat(m).isNotNull();
        assertThat(m.getStatus()).isEqualTo(HttpStatus.OK.value());
    }
}
