package com.vinncorp.fast_learner.mock.subscription.process;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.CustomerAndPaymentId;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.mock.subscription.SubscriptionTestData;
import com.vinncorp.fast_learner.mock.subscription.subscribed_user.SubscribedUserTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.SubscribedUserProfile;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.payment.additional_service.PaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.payment.additional_service.DowngradePaymentSubscriptionService;
import com.vinncorp.fast_learner.services.payment.payment_profile.IPaymentProfileService;
import com.vinncorp.fast_learner.services.subscription.process.DowngradeProcess;
import com.vinncorp.fast_learner.services.subscription.subscribed_user_profile.ISubscribedUserProfileService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.PlanType;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DowngradeProcessMockTest {

    @InjectMocks
    private DowngradeProcess downgradeProcess;

    @Mock
    private IPaymentProfileService paymentProfileService;
    @Mock
    private ISubscribedUserProfileService subscribedUserProfileService;
    @Mock
    private ITransactionHistoryService transactionHistoryService;
    @Mock
    private DowngradePaymentSubscriptionService downgradePaymentSubscriptionService;
    @Mock
    private PaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;

    private User user;
    private SubscriptionRequest request;
    private SubscribedUser subscribedUser;
    private Subscription nextSubscription;
    private Subscription currentSubscription;
    private TransactionHistory oldHistory;
    private SubscribedUserProfile profile;
    private CustomerAndPaymentId customerAndPaymentId;

    @BeforeEach
    void setup() {
        user = UserTestData.userData();

        request = new SubscriptionRequest();

        currentSubscription = SubscriptionTestData.premiumSubscription();
        nextSubscription = SubscriptionTestData.premiumSubscription();

        subscribedUser = SubscribedUserTestData.subscribedUser();

        oldHistory = new TransactionHistory();
        oldHistory.setId(1L);
        oldHistory.setStatus(GenericStatus.ACTIVE);
        oldHistory.setSubscription(currentSubscription);
        oldHistory.setAuthSubscriptionId(subscribedUser.getSubscribedId());
        oldHistory.setSubscriptionStatus(SubscriptionStatus.SUCCESS);

        profile = new SubscribedUserProfile();
        profile.setCustomerPaymentId("cust-id");
        profile.setCustomerPaymentProfileId("pay-id");
        profile.setIsDefault(true);

        customerAndPaymentId = CustomerAndPaymentId.builder()
                .paymentId("pay-id")
                .customerId("cust-id")
                .build();
    }

    @Test
    @DisplayName("Test Process Subscription with existing profile")
    void testProcessSubscription_WithExistingProfile_ShouldDowngradeSuccessfully() throws Exception {
        when(subscribedUserProfileService.getDefaultBySubscribedUserId(any(), eq(true))).thenReturn(profile);
        oldHistory.getSubscription().setPlanType(PlanType.STANDARD);
        when(transactionHistoryService.findByLatestTransactionHistoryByUserIdAndStatus(any(), eq(GenericStatus.ACTIVE)))
                .thenReturn(oldHistory);
        when(downgradePaymentSubscriptionService.downgrade(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new Message<String>().setMessage("Success"));
        when(transactionHistoryService.findById(any())).thenReturn(oldHistory);

        subscribedUser.setSubscription(SubscriptionTestData.standardSubscription());
        Message<String> result = downgradeProcess.processSubscription(request, subscribedUser, nextSubscription, user, null);

        assertEquals("Success", result.getMessage());
        verify(paymentAdditionalSubscriptionService).cancelPaymentSubscription(oldHistory.getAuthSubscriptionId());
        verify(transactionHistoryService).save(any(TransactionHistory.class));
    }

    @Test
    @DisplayName("Test Process Subscription with missing profile")
    void testProcessSubscription_ProfileMissing_ShouldCreateProfile() throws Exception {
        when(subscribedUserProfileService.getDefaultBySubscribedUserId(any(), eq(true))).thenReturn(null);
        when(paymentProfileService.createCustomerProfile(eq(user.getEmail()), any()))
                .thenReturn(customerAndPaymentId);
        when(subscribedUserProfileService.save(any())).thenReturn(profile);
        oldHistory.getSubscription().setPlanType(PlanType.STANDARD);
        when(transactionHistoryService.findByLatestTransactionHistoryByUserIdAndStatus(any(), any()))
                .thenReturn(oldHistory);
        when(downgradePaymentSubscriptionService.downgrade(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new Message<String>().setMessage("Created"));
        when(transactionHistoryService.findById(any())).thenReturn(oldHistory);

        subscribedUser.setSubscription(SubscriptionTestData.standardSubscription());
        Message<String> result = downgradeProcess.processSubscription(request, subscribedUser, nextSubscription, user, null);

        assertEquals("Created", result.getMessage());
        verify(paymentProfileService).createCustomerProfile(eq(user.getEmail()), any());
        verify(subscribedUserProfileService).save(any());
    }

    @Test
    @DisplayName("Test Process Subscription with free plan")
    void testProcessSubscription_FreePlan_ShouldDeactivateAndContinue() throws Exception {
        oldHistory.getSubscription().setPlanType(PlanType.FREE);
        TransactionHistory pendingHistory = new TransactionHistory();
        pendingHistory.setSubscription(nextSubscription);

        when(subscribedUserProfileService.getDefaultBySubscribedUserId(any(), eq(true))).thenReturn(profile);
//        oldHistory.setSubscription();
        when(transactionHistoryService.findByLatestTransactionHistoryByUserIdAndStatus(any(), any())).thenReturn(oldHistory);
        when(transactionHistoryService.findByLatestUserAndSubscriptionStatus(any(), any())).thenReturn(pendingHistory);

        oldHistory.getSubscription().setPlanType(PlanType.FREE);
        when(transactionHistoryService.findById(any())).thenReturn(oldHistory);
        when(downgradePaymentSubscriptionService.downgrade(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new Message<String>().setMessage("Free Plan Downgrade"));

        subscribedUser.setSubscription(SubscriptionTestData.freeSubscription());
        Message<String> result = downgradeProcess.processSubscription(request, subscribedUser, nextSubscription, user, null);

        assertEquals("Free Plan Downgrade", result.getMessage());
    }

    @Test
    @DisplayName("Test Process Subscription with no transaction history")
    void testProcessSubscription_NoTransactionHistory_ShouldThrowException() throws EntityNotFoundException {
        when(subscribedUserProfileService.getDefaultBySubscribedUserId(any(), eq(true))).thenReturn(profile);
        when(transactionHistoryService.findByLatestTransactionHistoryByUserIdAndStatus(any(), any())).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                downgradeProcess.processSubscription(request, subscribedUser, nextSubscription, user, null));

        assertEquals("You are not allowed to change the subscription for now.Please contact support", exception.getMessage());
    }
}
