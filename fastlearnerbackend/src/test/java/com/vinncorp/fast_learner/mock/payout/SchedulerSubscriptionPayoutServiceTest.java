package com.vinncorp.fast_learner.mock.payout;

import com.vinncorp.fast_learner.models.payout.PayoutWatchTime;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.payout.PayoutWatchTimeRepository;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.services.payout.SchedulerSubscriptionPayoutService;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SchedulerSubscriptionPayoutServiceTest {
    @Spy
    @InjectMocks
    private SchedulerSubscriptionPayoutService schedulerSubscriptionPayoutService;

    @Mock
    private PayoutWatchTimeRepository payoutWatchTimeRepository;

    @Mock
    private UserRepository userRepository;

    @DisplayName("Should process instructor payout successfully when valid payout records and Stripe account exist")
    @Test
    void testInstructorPayoutForSubscription_successfulPayout() throws Exception {
        // Arrange
        PayoutWatchTime payoutWatchTime = new PayoutWatchTime();
        payoutWatchTime.setId(1L);
        payoutWatchTime.setInstructorId(100L);
        payoutWatchTime.setAmountSharePerDay(50.0);

        User instructor = new User();
        instructor.setId(100L);
        instructor.setStripeAccountId("acct_123");

        List<PayoutWatchTime> payoutList = List.of(payoutWatchTime);

        when(payoutWatchTimeRepository.findPendingPayoutsWithinLastThreeMonths(anyString(), any(Date.class)))
                .thenReturn(payoutList);
        when(userRepository.findById(100L)).thenReturn(Optional.of(instructor));
        doReturn("payout_123").when(schedulerSubscriptionPayoutService).processPayout("acct_123", 50.0);
        doNothing().when(schedulerSubscriptionPayoutService)
                .markPayoutTransactionsProcessed(List.of(1L), "payout_123", PayoutStatus.PROCESSED, "SETTLED");

        // Act
        schedulerSubscriptionPayoutService.InstructorPayoutForSubscription();

        // Assert
        verify(payoutWatchTimeRepository, times(1)).findPendingPayoutsWithinLastThreeMonths(anyString(), any(Date.class));
        verify(userRepository, times(1)).findById(100L);
        verify(schedulerSubscriptionPayoutService, times(1)).processPayout("acct_123", 50.0);
        verify(schedulerSubscriptionPayoutService, times(1))
                .markPayoutTransactionsProcessed(List.of(1L), "payout_123", PayoutStatus.PROCESSED, "SETTLED");
    }

    // TODO ERROR: Resolve the below test method error
    @DisplayName("Should do nothing when no eligible payout records are found")
    @Test
    void testInstructorPayoutForSubscription_noEligiblePayouts() throws Exception {
        // Arrange
        when(payoutWatchTimeRepository.findPendingPayoutsWithinLastThreeMonths(anyString(), any(Date.class)))
                .thenReturn(Collections.emptyList());

        // Act
        schedulerSubscriptionPayoutService.InstructorPayoutForSubscription();

        // Assert
        verify(payoutWatchTimeRepository, times(1)).findPendingPayoutsWithinLastThreeMonths(anyString(), any(Date.class));
        verifyNoMoreInteractions(userRepository);
        verify(schedulerSubscriptionPayoutService, never()).processPayout(anyString(), anyDouble());
        verify(schedulerSubscriptionPayoutService, never()).markPayoutTransactionsProcessed(anyList(), anyString(), any(), anyString());
    }

    @DisplayName("Should skip payout when instructor is not found")
    @Test
    void testInstructorPayoutForSubscription_instructorNotFound() throws Exception {
        // Arrange
        PayoutWatchTime payoutWatchTime = new PayoutWatchTime();
        payoutWatchTime.setId(1L);
        payoutWatchTime.setInstructorId(100L);
        payoutWatchTime.setAmountSharePerDay(50.0);

        when(payoutWatchTimeRepository.findPendingPayoutsWithinLastThreeMonths(anyString(), any(Date.class)))
                .thenReturn(List.of(payoutWatchTime));
        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        // Act
        schedulerSubscriptionPayoutService.InstructorPayoutForSubscription();

        // Assert
        verify(payoutWatchTimeRepository, times(1)).findPendingPayoutsWithinLastThreeMonths(anyString(), any(Date.class));
        verify(userRepository, times(1)).findById(100L);
        verify(schedulerSubscriptionPayoutService, never()).processPayout(anyString(), anyDouble());
        verify(schedulerSubscriptionPayoutService, never()).markPayoutTransactionsProcessed(anyList(), anyString(), any(), anyString());
    }

    @DisplayName("Should skip payout when Stripe account is missing for instructor")
    @Test
    void testInstructorPayoutForSubscription_missingStripeAccount() throws Exception {
        // Arrange
        PayoutWatchTime payoutWatchTime = new PayoutWatchTime();
        payoutWatchTime.setId(1L);
        payoutWatchTime.setInstructorId(100L);
        payoutWatchTime.setAmountSharePerDay(50.0);

        User instructor = new User();
        instructor.setId(100L);
        instructor.setStripeAccountId(null); // No Stripe account

        when(payoutWatchTimeRepository.findPendingPayoutsWithinLastThreeMonths(anyString(), any(Date.class)))
                .thenReturn(List.of(payoutWatchTime));
        when(userRepository.findById(100L)).thenReturn(Optional.of(instructor));

        // Act
        schedulerSubscriptionPayoutService.InstructorPayoutForSubscription();

        // Assert
        verify(payoutWatchTimeRepository, times(1)).findPendingPayoutsWithinLastThreeMonths(anyString(), any(Date.class));
        verify(userRepository, times(1)).findById(100L);
        verify(schedulerSubscriptionPayoutService, never()).processPayout(anyString(), anyDouble());
        verify(schedulerSubscriptionPayoutService, never()).markPayoutTransactionsProcessed(anyList(), anyString(), any(), anyString());
    }

}
