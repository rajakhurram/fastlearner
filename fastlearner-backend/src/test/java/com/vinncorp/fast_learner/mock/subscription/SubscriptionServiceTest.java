package com.vinncorp.fast_learner.mock.subscription;

import com.vinncorp.fast_learner.dtos.subscription.RemainingBalance;
import com.vinncorp.fast_learner.dtos.payment.webhook.PaymentWebhookRequest;
import com.vinncorp.fast_learner.dtos.payment.webhook.Payload;
import com.vinncorp.fast_learner.dtos.payment.webhook.Profile;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.transaction_history.TransactionHistoryTestData;
import com.vinncorp.fast_learner.models.Payment.WebhookLog;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.Payment.WebhookLogRepository;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionRepository;
import com.vinncorp.fast_learner.services.payment.payment_profile.PaymentProfileService;
import com.vinncorp.fast_learner.services.payment.webhook.PaymentWebhookService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.SubscriptionService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.services.subscription.process.UpgradeProcess;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @Mock
    private PaymentProfileService paymentProfileService;

    @Mock
    private SubscriptionRepository repo;

    @Mock
    private IUserService userService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private TransactionHistory mockTransactionHistory;

    private Subscription mockSubscription;

    @InjectMocks
    private UpgradeProcess upgradeProcess;

    @Mock
    private ISubscribedUserService subscribedUserService;

    @Mock
    private WebhookLogRepository webhookLogRepo;

    @Mock
    private ITransactionHistoryService transactionHistoryService;

    private PaymentWebhookService paymentWebhookService;

    @BeforeEach
    void setUp() {
        mockTransactionHistory = mock(TransactionHistory.class);
        mockSubscription = mock(Subscription.class);

        // Spy the concrete service (partial mock)
        subscriptionService = Mockito.spy(new SubscriptionService(repo, userService));

        // Inject manually
        paymentWebhookService = new PaymentWebhookService(subscriptionService,
                subscribedUserService, webhookLogRepo, transactionHistoryService, paymentProfileService);
        /*ReflectionTestUtils.setField(
                paymentWebhookService,
                "subscriptionService", // this must match the field name in your service
                subscriptionService
        );
        ReflectionTestUtils.setField(
                paymentWebhookService,
                "webhookLogRepo",
                webhookLogRepo
        );
        ReflectionTestUtils.setField(
                paymentWebhookService,
                "transactionHistoryService",
                transactionHistoryService
        );
        ReflectionTestUtils.setField(
                paymentWebhookService,
                "subscribedUserService",
                subscribedUserService
        );*/
    }

    @Test
    @DisplayName("Test : subscription termination")
    void testSubscriptionTermination() throws EntityNotFoundException, InternalServerException {
        // Arrange
        PaymentWebhookRequest request = new PaymentWebhookRequest();
        Payload payload = new Payload();
        payload.setEntityName("subscription");
        payload.setId("9479094");
        payload.setName("Standard Student Plan");
        payload.setAmount(5);
        payload.setStatus("canceled");

        Profile profile = new Profile();
        profile.setCustomerProfileId(522011716);
        profile.setCustomerPaymentProfileId(533839576);
        profile.setCustomerShippingAddressId(0);
        payload.setProfile(profile);

        request.setPayload(payload);
        request.setNotificationId("191e919b-bcb2-4dfa-8e2b-99c00796de5");
        request.setEventDate(String.valueOf(new Date()));

        WebhookLog mockWebhookLog = WebhookLog.builder().content(request.toString()).build();

        var subscription = SubscriptionTestData.standardSubscription();
        subscription.setId(1L);

        doReturn(new Message<Subscription>().setData(subscription))
                .when(subscriptionService).findBySubscriptionId(any());
        when(webhookLogRepo.save(any(WebhookLog.class))).thenReturn(mockWebhookLog);

        // Act
        paymentWebhookService.subscriptionTermination(request);

        // Assert
        verify(webhookLogRepo, times(1)).save(any(WebhookLog.class));
        verify(subscriptionService, times(1)).findBySubscriptionId(1L);
    }

    @Test
    @DisplayName("test : fetch all subscription when Success ")
    void testFetchAllSubscription_Success() throws EntityNotFoundException {
        // Arrange
        List<Subscription> subscriptions = new ArrayList<>();
        Subscription subscription = new Subscription();
        subscription.setActive(true);
        subscriptions.add(subscription);

        when(repo.findAllByIsActive(true)).thenReturn(subscriptions);

        // Act
        Message<List<Subscription>> response = subscriptionService.fetchAllSubscription("test@example.com");

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK.value(), response.getStatus(), "Status should be OK");
        assertEquals(HttpStatus.OK.toString(), response.getCode(), "Code should be OK");
        assertEquals("All subscription is fetched successfully.", response.getMessage(), "Message should match");
        assertEquals(subscriptions, response.getData(), "Data should match");
        verify(repo, times(1)).findAllByIsActive(true);
    }

    @Test
    @DisplayName("Test : No subscriptions found")
    void testFetchAllSubscription_NoSubscriptions() {
        // Arrange
        when(repo.findAllByIsActive(true)).thenReturn(new ArrayList<>());

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () ->
                        subscriptionService.fetchAllSubscription("test@example.com"),
                "Expected EntityNotFoundException to be thrown");

        assertEquals("No subscription plan found.", thrown.getMessage(), "Exception message should match");
    }



    @Test
    @DisplayName("Test: Fetch subscriptions with null email")
    void testFetchAllSubscription_NullEmail() throws EntityNotFoundException {
        // Arrange
        List<Subscription> subscriptions = new ArrayList<>();
        Subscription subscription = new Subscription();
        subscription.setActive(true);
        subscriptions.add(subscription);

        when(repo.findAllByIsActive(true)).thenReturn(subscriptions);

        // Act
        Message<List<Subscription>> response = subscriptionService.fetchAllSubscription(null);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK.value(), response.getStatus(), "Status should be OK");
        assertEquals(HttpStatus.OK.toString(), response.getCode(), "Code should be OK");
        assertEquals("All subscription is fetched successfully.", response.getMessage(), "Message should match");
        assertEquals(subscriptions, response.getData(), "Data should match");
        verify(repo, times(1)).findAllByIsActive(true);
    }

    @Test
    @DisplayName("Test: Fetch subscriptions with empty list but valid state")
    void testFetchAllSubscription_EmptyListButValidState() throws EntityNotFoundException {
        // Arrange
        List<Subscription> subscriptions = new ArrayList<>();
        when(repo.findAllByIsActive(true)).thenReturn(subscriptions);

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () ->
                        subscriptionService.fetchAllSubscription("test@example.com"),
                "Expected EntityNotFoundException to be thrown");

        assertEquals("No subscription plan found.", thrown.getMessage(), "Exception message should match");
    }

    @Test
    @DisplayName("Test: Fetch subscription by valid ID returns subscription")
    void testFindBySubscriptionId_Success() throws EntityNotFoundException {
        // Arrange
        Long subscriptionId = 1L;
        Subscription subscription = new Subscription();
        subscription.setId(subscriptionId);

        when(repo.findById(subscriptionId)).thenReturn(Optional.of(subscription));

        // Act
        Message<Subscription> response = subscriptionService.findBySubscriptionId(subscriptionId);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK.value(), response.getStatus(), "Status should be OK");
        assertEquals(HttpStatus.OK.toString(), response.getCode(), "Code should be OK");
        assertEquals("Subscription is fetched successfully.", response.getMessage(), "Message should match");
        assertEquals(subscription, response.getData(), "Data should match");
        verify(repo, times(1)).findById(subscriptionId);
    }
    @Test
    @DisplayName("Test: Fetch subscription by invalid ID throws EntityNotFoundException")
    void testFindBySubscriptionId_NotFound() {
        // Arrange
        Long subscriptionId = 1L;

        when(repo.findById(subscriptionId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () ->
                        subscriptionService.findBySubscriptionId(subscriptionId),
                "Expected EntityNotFoundException to be thrown");

        assertEquals("No subscription found by provided subscription id.", thrown.getMessage(), "Exception message should match");
    }

    @Test
    @DisplayName("Test: Fetch subscription with null ID ")
    void testFindBySubscriptionId_NullId() {
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                        subscriptionService.findBySubscriptionId(null),
                "Expected IllegalArgumentException to be thrown for null ID");
    }

    @Test
    @DisplayName("Test: Handle unexpected exception from repository")
    void testFindBySubscriptionId_UnexpectedException() {
        // Arrange
        Long subscriptionId = 1L;

        // Mock the repository to throw a runtime exception
        when(repo.findById(subscriptionId)).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                        subscriptionService.findBySubscriptionId(subscriptionId),
                "Expected RuntimeException to be thrown");

        assertEquals("Unexpected error", thrown.getMessage(), "Exception message should match");
    }
    @Test
    @DisplayName("Test: Repository returns unexpected type")
    void testFindBySubscriptionId_RepositoryReturnsUnexpectedType() {
        // Arrange
        Long subscriptionId = 1L;
        when(repo.findById(subscriptionId)).thenReturn((Optional) Optional.of("Unexpected type"));

        // Act & Assert
        assertThrows(ClassCastException.class, () ->
                        subscriptionService.findBySubscriptionId(subscriptionId),
                "Expected ClassCastException due to unexpected type");
    }

    @Test
    @DisplayName("Test: User is subscribed")
    void testIsSubscribed_UserIsSubscribed() throws EntityNotFoundException {
        // Arrange
        String email = "subscribed@example.com";
        User user = new User();
        user.setSubscribed(true);

        when(userService.findByEmail(email)).thenReturn(user);

        // Act
        Message<Boolean> response = subscriptionService.isSubscibed(email);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK.value(), response.getStatus(), "Status should be OK");
        assertEquals(HttpStatus.OK.toString(), response.getCode(), "Code should be OK");
        assertTrue(response.getData(), "User should be subscribed");
        assertEquals("User is subscribed.", response.getMessage(), "Message should match");
        verify(userService, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Test: User is not subscribed")
    void testIsSubscribed_UserIsNotSubscribed() throws EntityNotFoundException {
        // Arrange
        String email = "not_subscribed@example.com";
        User user = new User();
        user.setSubscribed(false);

        when(userService.findByEmail(email)).thenReturn(user);

        // Act
        Message<Boolean> response = subscriptionService.isSubscibed(email);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK.value(), response.getStatus(), "Status should be OK");
        assertEquals(HttpStatus.OK.toString(), response.getCode(), "Code should be OK");
        assertFalse(response.getData(), "User should not be subscribed");
        assertEquals("User is not subscribed a plan.", response.getMessage(), "Message should match");
        verify(userService, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Test: User not found by email")
    void testIsSubscribed_UserNotFound() throws EntityNotFoundException {
        // Arrange
        String email = "nonexistent@example.com";

        when(userService.findByEmail(email)).thenThrow(new EntityNotFoundException("User not found"));

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () ->
                        subscriptionService.isSubscibed(email),
                "Expected EntityNotFoundException to be thrown");

        assertEquals("User not found", thrown.getMessage(), "Exception message should match");
    }

    @Test
    @DisplayName("Test: Handle unexpected exception from userService")
    void testIsSubscribed_UnexpectedException() throws EntityNotFoundException {
        // Arrange
        String email = "error@example.com";

        when(userService.findByEmail(email)).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                        subscriptionService.isSubscibed(email),
                "Expected RuntimeException to be thrown");

        assertEquals("Unexpected error", thrown.getMessage(), "Exception message should match");
    }

    @Test
    void testRemainingBalanceBeforeTrialEnds() {
        // Arrange
        LocalDate trialEndDate = LocalDate.now().plusDays(10);
        mockTransactionHistoryDates(trialEndDate, trialEndDate.plusMonths(1));
        when(mockSubscription.getPrice()).thenReturn(100.0);
        when(mockTransactionHistory.getSubscription()).thenReturn(mockSubscription);
        when(mockTransactionHistory.getSubscription().getPrice()).thenReturn(100.0);


        // Act
        RemainingBalance result = upgradeProcess.calculateRemainingBalance(mockTransactionHistory, mockSubscription);

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.getRemainingBalance());
    }

    @Test
    void testRemainingBalanceDuringPaidCycle() {
        // Arrange
        LocalDate trialEndDate = LocalDate.now().minusDays(5);
        LocalDate subscriptionNextCycleDate = LocalDate.now().plusDays(25);
        mockTransactionHistoryDates(trialEndDate, subscriptionNextCycleDate);
        when(mockSubscription.getPrice()).thenReturn(100.0);
        when(mockTransactionHistory.getSubscription()).thenReturn(mockSubscription);
        when(mockTransactionHistory.getSubscription().getPrice()).thenReturn(100.0);

        when(mockSubscription.getPrice()).thenReturn(100.0);
        when(mockSubscription.getDuration()).thenReturn(1); // Monthly subscription

        // Act
        RemainingBalance result = upgradeProcess.calculateRemainingBalance(mockTransactionHistory, mockSubscription);

        // Assert
        assertNotNull(result);
        assertTrue(result.getRemainingBalance() > 0);
    }

    @Test
    void testRemainingBalanceExceedsNextSubscriptionPrice() {
        // Arrange
        LocalDate trialEndDate = LocalDate.now().minusDays(5);
        LocalDate subscriptionNextCycleDate = LocalDate.now().plusDays(25);
        mockTransactionHistoryDates(trialEndDate, subscriptionNextCycleDate);
        when(mockSubscription.getPrice()).thenReturn(100.0);
        when(mockSubscription.getDuration()).thenReturn(1);
        when(mockTransactionHistory.getSubscription()).thenReturn(mockSubscription);
        when(mockTransactionHistory.getSubscription().getPrice()).thenReturn(150.0);


        // Act
        RemainingBalance result = upgradeProcess.calculateRemainingBalance(mockTransactionHistory, mockSubscription);

        // Assert
        assertNotNull(result);
        assertTrue(result.getRemainingBalance() < mockSubscription.getPrice());
    }

    @Test
    void testRemainingBalanceLessThanNextSubscriptionPriceWithShortTrial() {
        // Arrange
        LocalDate trialEndDate = LocalDate.now().minusDays(25);
        LocalDate subscriptionNextCycleDate = LocalDate.now().plusDays(5);
        mockTransactionHistoryDates(trialEndDate, subscriptionNextCycleDate);

        when(mockSubscription.getPrice()).thenReturn(200.0);
        when(mockSubscription.getDuration()).thenReturn(1); // Monthly subscription
        when(mockTransactionHistory.getSubscription()).thenReturn(mockSubscription);
        when(mockTransactionHistory.getSubscription().getPrice()).thenReturn(100.0);

        // Act
        RemainingBalance result = upgradeProcess.calculateRemainingBalance(mockTransactionHistory, mockSubscription);

        // Assert
        assertNotNull(result);
        assertEquals(RemainingBalance.TRIAL_PERIOD.MONTHLY, result.getTrialPeriod());
    }

    @Test
    void testNoBalanceAvailable() {
        // Arrange
        LocalDate trialEndDate = LocalDate.now().minusDays(30);
        LocalDate subscriptionNextCycleDate = LocalDate.now().minusDays(15);

        var transactionHistory = TransactionHistoryTestData.standardTransactionHistory();
        transactionHistory.setTrialEndDate(toDate(trialEndDate));
        transactionHistory.setSubscriptionNextCycle(toDate(subscriptionNextCycleDate));

        // Act
        RemainingBalance result = upgradeProcess.calculateRemainingBalance(transactionHistory, transactionHistory.getSubscription());

        // Assert
        assertNull(result);
    }

    private void mockTransactionHistoryDates(LocalDate trialEndDate, LocalDate subscriptionNextCycleDate) {
        when(mockTransactionHistory.getTrialEndDate()).thenReturn(toDate(trialEndDate));
        when(mockTransactionHistory.getSubscriptionNextCycle()).thenReturn(toDate(subscriptionNextCycleDate));
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}