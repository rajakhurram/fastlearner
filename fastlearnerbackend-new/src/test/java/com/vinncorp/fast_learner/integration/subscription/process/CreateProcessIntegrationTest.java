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
public class CreateProcessIntegrationTest {

    @Qualifier(value = "create")
    @Autowired
    private SubscriptionStrategyService createProcess;

    @Autowired
    private IUserService userService;
    @Autowired
    private ISubscriptionService subscriptionService;
    @Autowired
    private ISubscribedUserService subscribedUserService;

    @Test
    public void testCreateSubscriptionProcess_whenProvidedDataIsValid() throws EntityNotFoundException, InternalServerException, BadRequestException {
        var r = SubscriptionTestData.subscriptionRequest();
        r.setSubscriptionId(5L);
        var subscription = subscriptionService.findBySubscriptionId(r.getSubscriptionId()).getData();
        var user = userService.findByEmail("john@mailinator.com");
        var m = createProcess.processSubscription(r, null, subscription, user, null);

        assertThat(m).isNotNull();
        assertThat(m.getStatus()).isEqualTo(HttpStatus.OK.value());
    }
}
