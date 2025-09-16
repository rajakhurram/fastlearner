package com.vinncorp.fast_learner.integration.payment.additional_service;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityAlreadyExistException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.subscription.SubscriptionRequestTestData;
import com.vinncorp.fast_learner.repositories.subscription.SubscribedUserRepository;
import com.vinncorp.fast_learner.services.payment.IPaymentSubscriptionService;
import com.vinncorp.fast_learner.services.payment.additional_service.IPaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.subscription.subscribed_user_profile.ISubscribedUserProfileService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PaymentAdditionalSubscriptionIntegrationTest {

    @Autowired
    private IPaymentAdditionalSubscriptionService service;

    @Autowired
    private IPaymentSubscriptionService paymentSubscriptionService;

    @Autowired
    private SubscribedUserRepository subscribedUserRepository;

    @Autowired
    private ISubscribedUserProfileService subscribedUserProfileService;

    @Test
    @DisplayName("Cancel Payment subscription")
    @Order(1)
    public void testCancelPaymentSubscription_whenProvidedValidValue() throws InternalServerException, BadRequestException, EntityNotFoundException {
        var paymentDetail = SubscriptionRequestTestData.paymentDetail();
        var subscriptionRequest = SubscriptionRequest.builder()
                .subscriptionId(2L)
                .paymentDetail(paymentDetail)
                .build();

        // create a subscription
        var m = paymentSubscriptionService.create(subscriptionRequest, "student@mailinator.com");

        subscribedUserRepository
                .findByUserEmail("student@mailinator.com")
                .ifPresent(user -> {
                    try {
                        service.cancelPaymentSubscription(user.getSubscribedId());
                    } catch (InternalServerException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    @DisplayName("Update Payment subscription")
    @Order(2)
    public void testUpdateSubscription_whenProvidedValidValue() throws InternalServerException, BadRequestException, EntityNotFoundException, EntityAlreadyExistException {
        // Arrange data
        var paymentDetail = SubscriptionRequestTestData.paymentDetail();
        var subscriptionRequest = SubscriptionRequest.builder()
                .subscriptionId(2L)
                .paymentDetail(paymentDetail)
                .build();

        // create a subscription
        var newSubscription = paymentSubscriptionService.create(subscriptionRequest, "student@mailinator.com");

        // Create new payment profile and update the subscription to that payment profile
        var newCardInfo = subscribedUserProfileService.addUpdateCustomerProfile("student@mailinator.com", paymentDetail);


        // Act
        var m = service.updatePaymentSubscription("student@mailinator.com", "test", "customerProfileId", "customerPaymentProfileId");

        // Assert
        assertNotNull(m);
        assertEquals(m.getCustomerId(), "customerProfileId");
    }
}
