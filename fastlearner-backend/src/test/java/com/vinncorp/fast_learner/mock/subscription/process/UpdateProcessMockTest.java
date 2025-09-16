package com.vinncorp.fast_learner.mock.subscription.process;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.CustomerAndPaymentId;
import com.vinncorp.fast_learner.models.subscription.*;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.payment.additional_service.PaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.payment.additional_service.UpdatePaymentSubscriptionService;
import com.vinncorp.fast_learner.services.payment.payment_profile.IPaymentProfileService;
import com.vinncorp.fast_learner.services.subscription.process.UpdateProcess;
import com.vinncorp.fast_learner.services.subscription.subscribed_user_profile.ISubscribedUserProfileService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.PlanType;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class UpdateProcessMockTest {

    @InjectMocks
    private UpdateProcess updateProcess;

    @Mock private IPaymentProfileService paymentProfileService;
    @Mock private ISubscribedUserProfileService subscribedUserProfileService;
    @Mock private ITransactionHistoryService transactionHistoryService;
    @Mock private UpdatePaymentSubscriptionService updatePaymentSubscriptionService;
    @Mock private PaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;

    private User user;
    private Subscription nextSubscription;
    private SubscribedUser subscribedUser;
    private TransactionHistory oldTransaction;
    private SubscribedUserProfile profile;
    private SubscriptionRequest request;
    private CustomerAndPaymentId customerAndPaymentId;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@fast.com");

        nextSubscription = new Subscription();
        nextSubscription.setPlanType(PlanType.STANDARD);
        nextSubscription.setPrice(200.0);

        subscribedUser = new SubscribedUser();
        subscribedUser.setId(1L);
        subscribedUser.setUser(user);
        subscribedUser.setSubscribedId("sub123");

        profile = new SubscribedUserProfile();
        profile.setIsDefault(true);

        oldTransaction = new TransactionHistory();
        oldTransaction.setId(99L);
        oldTransaction.setSubscription(new Subscription());
        oldTransaction.getSubscription().setPlanType(PlanType.STANDARD);
        oldTransaction.setTrialEndDate(new Date());
        oldTransaction.setSubscriptionNextCycle(new Date());
        oldTransaction.setStatus(GenericStatus.ACTIVE);

        request = new SubscriptionRequest();

        customerAndPaymentId = new CustomerAndPaymentId();
        customerAndPaymentId.setCustomerId("cust1");
        customerAndPaymentId.setPaymentId("pay1");
    }

    @Test
    public void testProcessSubscription_withExistingProfileAndPaidPlan() throws Exception {
        when(subscribedUserProfileService.getDefaultBySubscribedUserId(anyLong(), eq(true))).thenReturn(profile);
        when(transactionHistoryService.findByLatestTransactionHistoryByUserIdAndStatus(anyLong(), eq(GenericStatus.ACTIVE))).thenReturn(oldTransaction);
        when(updatePaymentSubscriptionService.update(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(new Message<String>().setMessage("Updated"));

        Message<String> response = updateProcess.processSubscription(request, subscribedUser, nextSubscription, user, null);

        assertEquals("Updated", response.getMessage());
        verify(paymentAdditionalSubscriptionService).cancelPaymentSubscription("sub123");
    }

    @Test
    public void testProcessSubscription_createProfileWhenMissing() throws Exception {
        when(subscribedUserProfileService.getDefaultBySubscribedUserId(anyLong(), eq(true))).thenReturn(null);
        when(paymentProfileService.createCustomerProfile(anyString(), any())).thenReturn(customerAndPaymentId);
        when(subscribedUserProfileService.save(any())).thenReturn(profile);
        when(transactionHistoryService.findByLatestTransactionHistoryByUserIdAndStatus(anyLong(), any())).thenReturn(oldTransaction);
        when(updatePaymentSubscriptionService.update(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(new Message<String>().setMessage("Created new profile"));

        Message<String> response = updateProcess.processSubscription(request, subscribedUser, nextSubscription, user, null);

        assertEquals("Created new profile", response.getMessage());
        verify(paymentProfileService).createCustomerProfile(eq("test@fast.com"), any());
    }

    @Test
    public void testProcessSubscription_freePlanCancelsPendingSubscription() throws Exception {
        oldTransaction.getSubscription().setPlanType(PlanType.FREE);
        TransactionHistory continueTx = new TransactionHistory();
        continueTx.setSubscriptionNextCycle(new Date());

        when(transactionHistoryService.findByLatestTransactionHistoryByUserIdAndStatus(anyLong(), any())).thenReturn(oldTransaction);
        when(transactionHistoryService.findByLatestUserAndSubscriptionStatus(anyLong(), eq(SubscriptionStatus.PENDING))).thenReturn(continueTx);
        when(subscribedUserProfileService.getDefaultBySubscribedUserId(anyLong(), eq(true))).thenReturn(profile);
        when(transactionHistoryService.save(any())).thenReturn(continueTx);
        when(updatePaymentSubscriptionService.update(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(new Message<String>().setMessage("Free upgraded"));

        Message<String> response = updateProcess.processSubscription(request, subscribedUser, nextSubscription, user, null);

        assertEquals("Free upgraded", response.getMessage());
        verify(transactionHistoryService).save(continueTx);
        assertNull(oldTransaction.getTrialEndDate());
    }
}
