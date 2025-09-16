package com.vinncorp.fast_learner.mock.subscription.process;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.coupon.CouponTestData;
import com.vinncorp.fast_learner.mock.subscription.SubscriptionTestData;
import com.vinncorp.fast_learner.mock.subscription.subscribed_user.SubscribedUserTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.subscription.SubscriptionLog;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionLogRepository;
import com.vinncorp.fast_learner.response.customer_profile.CustomerProfileIdType;
import com.vinncorp.fast_learner.response.subscription.CreateSubscriptionResponse;
import com.vinncorp.fast_learner.services.payment.additional_service.IPaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.notification.IInstructorPerformanceInsightService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.process.CreateProces;
import com.vinncorp.fast_learner.services.subscription.subscribed_user_profile.ISubscribedUserProfileService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

public class CreateProcessMockTest {

    @Mock
    private IUserService userService;

    @Mock
    private ISubscribedUserService subscribedUserService;

    @Mock
    private ISubscribedUserProfileService subscribedUserProfileService;
    @Mock
    private IPaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;

    @Mock
    private IInstructorPerformanceInsightService instructorPerformanceInsightService;

    @Mock
    private SubscriptionLogRepository subscriptionLogRepo;

    @InjectMocks
    private CreateProces createProces;

    private SubscribedUser subscribedUser;
    private Subscription subscription;
    private User user;
    private Coupon coupon;
    private SubscriptionRequest subscriptionRequest;
    private CreateSubscriptionResponse response;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setSubscriptionId(123L);
        subscriptionRequest.setPrevAuthSubscriptionId("prev-auth-sub-id");

        subscribedUser = SubscribedUserTestData.subscribedUser();
        subscription = SubscriptionTestData.standardSubscription();
        user = UserTestData.userData();
        coupon = CouponTestData.premiumSubscriptionCoupon();


        CustomerProfileIdType customerProfileIdType = new CustomerProfileIdType();
        customerProfileIdType.setCustomerPaymentProfileId("111111111");
        customerProfileIdType.setCustomerProfileId("222222222");

        response = new CreateSubscriptionResponse();
        response.setSubscriptionId("sub-101");
        response.setProfile(customerProfileIdType);
    }

    // Subscribing for free subscription is already handled in authnet subscription service
    @Test
    @DisplayName("Test process subscription first time to PAID subscription")
    void testProcessSubscription_firstTimeToPaidSubscription_Success() throws Exception {
        when(paymentAdditionalSubscriptionService.createFirstTime(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(response);

        Message<String> result = createProces.processSubscription(subscriptionRequest, null, subscription, user, null);

        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertTrue(result.getMessage().contains("has been subscribed by"));
        verify(subscribedUserService).save(any());
        verify(userService).save(user);
        verify(subscriptionLogRepo).save(any(SubscriptionLog.class));
        verify(instructorPerformanceInsightService).notifyToUserOnNewSubscription("Monthly", user.getId());
    }

    @Test
    @DisplayName("Test process subscription when Payment subscription throw error")
    void testProcessSubscription_CreateSubscriptionFails() throws Exception {
        when(paymentAdditionalSubscriptionService.createFirstTime(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new InternalServerException("Failure"));

        assertThrows(InternalServerException.class, () ->
                createProces.processSubscription(subscriptionRequest, null, subscription, user, null)
        );
    }


}
