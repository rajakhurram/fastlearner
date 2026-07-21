package com.vinncorp.fast_learner.mock.authorize_net.additional_service;

import com.vinncorp.fast_learner.config.GenericRestClient;
import com.vinncorp.fast_learner.dtos.authorize_net.SubscriptionRequest;
import com.vinncorp.fast_learner.dtos.authorize_net.payment_profile.CustomerAndPaymentId;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.subscription.SubscriptionTestData;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.subscription.SubscriptionLog;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionLogRepository;
import com.vinncorp.fast_learner.services.authorize_net.additional_service.AuthNetAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.authorize_net.payment_profile.PaymentProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthNetAdditionalSubscriptionServiceTest {

    @Mock
    private PaymentProfileService paymentProfileService;

    @Mock
    private SubscriptionLogRepository subscriptionLogRepo;

    @Mock
    private GenericRestClient restClient;

    @InjectMocks
    private AuthNetAdditionalSubscriptionService service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void cancelAuthorizeNetSubscription_NullResponse_ThrowsException() {
        String subscriptionId = "test-subscription-id";

        InternalServerException exception = assertThrows(InternalServerException.class, () ->
                service.cancelAuthorizeNetSubscription(subscriptionId)
        );

        assertEquals("Something went wrong with Authorize.Net api", exception.getMessage());
    }

    @Test
    void getSubscriptionById_NullResponse_ThrowsException() {
        String subscriptionId = "test-subscription-id";

        InternalServerException exception = assertThrows(InternalServerException.class, () ->
                service.getSubscriptionById(subscriptionId)
        );

        assertEquals("Something went wrong with Authorize.Net api", exception.getMessage());
    }

}
