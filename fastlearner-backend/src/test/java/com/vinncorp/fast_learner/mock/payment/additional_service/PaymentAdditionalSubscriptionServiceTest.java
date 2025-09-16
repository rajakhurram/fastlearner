package com.vinncorp.fast_learner.mock.payment.additional_service;

import com.vinncorp.fast_learner.config.GenericRestClient;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionLogRepository;
import com.vinncorp.fast_learner.services.payment.additional_service.PaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.payment.payment_profile.PaymentProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentAdditionalSubscriptionServiceTest {

    @Mock
    private PaymentProfileService paymentProfileService;

    @Mock
    private SubscriptionLogRepository subscriptionLogRepo;

    @Mock
    private GenericRestClient restClient;

    @InjectMocks
    private PaymentAdditionalSubscriptionService service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void cancelPaymentSubscription_NullResponse_ThrowsException() {
        String subscriptionId = "test-subscription-id";

        InternalServerException exception = assertThrows(InternalServerException.class, () ->
                service.cancelPaymentSubscription(subscriptionId)
        );

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

    @Test
    void getSubscriptionById_NullResponse_ThrowsException() {
        String subscriptionId = "test-subscription-id";

        InternalServerException exception = assertThrows(InternalServerException.class, () ->
                service.getSubscriptionById(subscriptionId)
        );

        assertEquals("Something went wrong with Payment api", exception.getMessage());
    }

}
