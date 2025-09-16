package com.vinncorp.fast_learner.integration.subscription.process;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.payment.additional_service.PaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.subscription.SubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.SubscriptionService;
import com.vinncorp.fast_learner.services.subscription.process.SubscriptionStrategyService;
import com.vinncorp.fast_learner.services.transaction_history.TransactionHistoryService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.PlanType;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CancelProcessIntegrationTest {

    @Qualifier(value = "cancel")
    @Autowired
    private SubscriptionStrategyService cancelProcess;

    @Autowired
    private IUserService userService;
//    @Autowired
//    private ISubscriptionService subscriptionService;
//    @Autowired
//    private ISubscribedUserService subscribedUserService;
    @Mock
    private TransactionHistoryService transactionHistoryService;

    @Mock
    private PaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private SubscribedUserService subscribedUserService;

    @InjectMocks
    private SubscriptionService subscriptionServiceImpl;

    private SubscriptionRequest requestDTO;
    private SubscribedUser subscribedUser;
    private Subscription nextSubscription;
    private User user;
    private TransactionHistory transactionHistory;

    @Test
    public void testCancelSubscriptionProcess_whenProvidedDataIsValid() throws EntityNotFoundException, InternalServerException, BadRequestException {
        var user = userService.findByEmail("substest@yopmail.com");
        var subscribedUser = subscribedUserService.findByUser(user.getEmail());
        var m = cancelProcess.processSubscription(null, subscribedUser, null, user, null);

        assertThat(m).isNotNull();
        assertThat(m.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @BeforeEach
    void setUp() throws EntityNotFoundException, InternalServerException {
        user = new User();
        user.setId(1L);

        nextSubscription = new Subscription();
        nextSubscription.setId(1L);
        nextSubscription.setPlanType(PlanType.STANDARD);
        nextSubscription.setPrice(10.0);

        subscribedUser = new SubscribedUser();
        subscribedUser.setSubscribedId("12345");
        subscribedUser.setPaymentSubscriptionId("67890");
        subscribedUser.setSubscription(nextSubscription);
        subscribedUser.setEndDate(null);

        transactionHistory = new TransactionHistory();
        transactionHistory.setAuthSubscriptionId("67890");
        transactionHistory.setSubscriptionStatus(SubscriptionStatus.PENDING);
        transactionHistory.setStatus(GenericStatus.ACTIVE);
        transactionHistory.setSubscriptionNextCycle(Date.from(Instant.now().plusSeconds(86400)));
        transactionHistory.setTrialEndDate(Date.from(Instant.now().minusSeconds(86400)));

        when(transactionHistoryService.findByLatestTransactionHistoryBySubsIdAndStatus(any(), any()))
                .thenReturn(transactionHistory);
        when(transactionHistoryService.findByLatestUserAndSubscriptionStatus(anyLong(), any()))
                .thenReturn(transactionHistory);
        when(subscriptionService.findBySubscriptionId(anyLong()))
                .thenReturn(new Message<>(HttpStatus.OK.value(), "OK", "Success", nextSubscription));
        doNothing().when(paymentAdditionalSubscriptionService).cancelPaymentSubscription(anyString());
    }


    @Test
    void testProcessSubscription_AlreadyCancelled() {
        subscribedUser.setEndDate(new Date());

        Exception exception = assertThrows(InternalServerException.class, () ->
                cancelProcess.processSubscription(requestDTO, subscribedUser, nextSubscription, user, null));

//        assertTrue(exception.getMessage().contains("plan already cancelled"));
    }

    @Test
    void testProcessSubscription_FreePlanCannotBeCancelled() {
       Subscription subscription = new Subscription();
        subscription.setId(1L);
        subscription.setPlanType(PlanType.FREE);
        subscription.setPrice(0.0);
        subscribedUser.setSubscription(subscription);

        Exception exception = assertThrows(BadRequestException.class, () ->
                cancelProcess.processSubscription(requestDTO, subscribedUser, nextSubscription, user, null));

        assertEquals("Free plan cannot be canceled.", exception.getMessage());
    }
}
