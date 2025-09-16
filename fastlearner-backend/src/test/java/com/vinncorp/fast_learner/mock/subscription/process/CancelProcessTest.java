package com.vinncorp.fast_learner.mock.subscription.process;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.mock.subscription.SubscriptionTestData;
import com.vinncorp.fast_learner.mock.subscription.subscribed_user.SubscribedUserTestData;
import com.vinncorp.fast_learner.mock.transaction_history.TransactionHistoryTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.services.payment.additional_service.IPaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionService;
import com.vinncorp.fast_learner.services.subscription.process.CancelProcess;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;

class CancelProcessTest {

    @InjectMocks
    private CancelProcess cancelProcess;

    @Mock
    private IPaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;

    @Mock
    private ISubscribedUserService subscribedUserService;

    @Mock
    private ISubscriptionService subscriptionService;

    @Mock
    private ITransactionHistoryService transactionHistoryService;

    @Captor
    private ArgumentCaptor<SubscribedUser> subscribedUserCaptor;

    @Captor
    private ArgumentCaptor<TransactionHistory> transactionCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test cancel standard plan success")
    public void testCancelStandardPlan_Success() throws Exception {
        var transactionHistory = TransactionHistoryTestData.standardTransactionHistory();
        var subscription = SubscriptionTestData.standardSubscription();
        var subscribedUser = SubscribedUserTestData.standardSubscribedUser();
        var user = UserTestData.userData();

        when(transactionHistoryService.findByLatestTransactionHistoryBySubsIdAndStatus(any(), any()))
                .thenReturn(transactionHistory);
        when(subscriptionService.findBySubscriptionId(1L)).thenReturn(new Message<Subscription>().setData(subscription));

        Message<String> response = cancelProcess.processSubscription(null, subscribedUser, null, user, null);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertTrue(response.getMessage().contains("cancelled"));
        verify(paymentAdditionalSubscriptionService).cancelPaymentSubscription(subscribedUser.getPaymentSubscriptionId());
        verify(subscribedUserService).save(any(SubscribedUser.class));
    }

    @Test
    @DisplayName("Test cancel coupon based standard subscription when provided valid data")
    public void testCancelCouponBasedStandardSubscription_whenProvidedValidData() throws Exception {
        var subscribedUser = SubscribedUserTestData.couponBasedStandardSubscribedUser();
        var transactionHistory = TransactionHistoryTestData.couponBasedStandardTransactionHistory();
        var subscription = SubscriptionTestData.standardSubscription();
        var user = UserTestData.userData();

        when(transactionHistoryService.findByLatestTransactionHistoryByUserIdAndStatus(any(), any()))
                .thenReturn(transactionHistory);
        when(subscriptionService.findBySubscriptionId(1L)).thenReturn(new Message<Subscription>().setData(subscription));

        Message<String> response = cancelProcess.processSubscription(null, subscribedUser, null, user, null);

        assertNotNull(response);
        assertEquals("Coupon based subscription cancelled successfully.", response.getMessage());
    }

    @Test
    @DisplayName("Test cancel already cancelled plan")
    public void testCancelAlreadyCancelledPlan() throws Exception {
        var subscribedUser = SubscribedUserTestData.standardSubscribedUser();
        subscribedUser.setEndDate(new Date());

        var transactionHistory = TransactionHistoryTestData.standardTransactionHistory();
        var user = UserTestData.userData();

        when(transactionHistoryService.findByLatestUserAndSubscriptionStatus(1L, SubscriptionStatus.PENDING)).thenReturn(transactionHistory);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> cancelProcess.processSubscription(null, subscribedUser, null, user, null));

        assertTrue(ex.getMessage().contains("already cancelled"));
    }

    @Test
    @DisplayName("Test cancel free plan")
    public void testCancelFreePlan_ShouldThrowBadRequest() {
        var subscribedUser = SubscribedUserTestData.freeSubscribedUser();
        var user = UserTestData.userData();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> cancelProcess.processSubscription(null, subscribedUser, null, user, null));

        assertEquals("Free plan cannot be canceled.", ex.getMessage());
    }

    @Test
    @DisplayName("Test update database with respect to end date of subscription")
    void testUpdateDatabaseWithRespectToEndDateOfSubscription() throws Exception {
        var freeSub = SubscriptionTestData.freeSubscription();
        var user = UserTestData.userData();
        var subscribedUser = SubscribedUserTestData.freeSubscribedUser();

        var canceledTransaction = TransactionHistoryTestData.cancelledStandardTransactionHistory();

        var freeTransaction = TransactionHistoryTestData.freeTransactionHistory();

        when(subscriptionService.findBySubscriptionId(1L))
                .thenReturn(new Message<Subscription>().setData(freeSub));

        when(subscribedUserService.findAllSubscribedUserWhichAreCancelled())
                .thenReturn(List.of(subscribedUser));

        when(transactionHistoryService.findByLatestTransactionHistoryBySubsIdAndStatusAndSubscriptionStatus(
                any(), any(), any())).thenReturn(canceledTransaction);

        when(transactionHistoryService.findByLatestTransactionHistoryByUserIdAndStatus(any(), any()))
                .thenReturn(freeTransaction);

        cancelProcess.updateDatabaseWithRespectToEndDateOfSubscription();

        verify(transactionHistoryService, times(2)).save(transactionCaptor.capture());
        List<TransactionHistory> savedTransactions = transactionCaptor.getAllValues();
        assertEquals(SubscriptionStatus.DISCONTINUE, savedTransactions.get(0).getSubscriptionStatus());
        assertEquals(SubscriptionStatus.CONTINUE, savedTransactions.get(1).getSubscriptionStatus());

        verify(subscribedUserService).save(subscribedUserCaptor.capture());
        SubscribedUser updatedUser = subscribedUserCaptor.getValue();
        assertEquals(freeSub, updatedUser.getSubscription());
        assertNull(updatedUser.getSubscribedId());
    }

    @Test
    @DisplayName("Test update database with empty users")
    void testUpdateDatabaseWithEmptyUsers() throws Exception {
        var subscription = SubscriptionTestData.freeSubscription();

        when(subscriptionService.findBySubscriptionId(anyLong())).thenReturn(new Message<Subscription>().setData(subscription));
        when(subscribedUserService.findAllSubscribedUserWhichAreCancelled())
                .thenReturn(Collections.emptyList());

        cancelProcess.updateDatabaseWithRespectToEndDateOfSubscription();

        verifyNoInteractions(transactionHistoryService);
    }
}

