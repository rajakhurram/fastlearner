package com.vinncorp.fast_learner.mock.payment;

import com.vinncorp.fast_learner.dtos.payment.BillingHistoryRequest;
import com.vinncorp.fast_learner.dtos.payment.BillingHistoryResponse;
import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.coupon.CouponTestData;
import com.vinncorp.fast_learner.mock.subscription.SubscriptionTestData;
import com.vinncorp.fast_learner.mock.subscription.subscribed_user.SubscribedUserTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.SubscribedUserProfile;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.payment.PaymentSubscriptionService;
import com.vinncorp.fast_learner.services.payment.additional_service.IPaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionService;
import com.vinncorp.fast_learner.services.subscription.process.CreateProces;
import com.vinncorp.fast_learner.services.subscription.process.ICouponBasedSubscriptionService;
import com.vinncorp.fast_learner.services.subscription.process.SubscriptionContextService;
import com.vinncorp.fast_learner.services.subscription.subscribed_user_profile.ISubscribedUserProfileService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.PaymentStatus;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.time.ZoneId;


import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentSubscriptionServiceTest {

    @InjectMocks
    private PaymentSubscriptionService paymentSubscriptionService;

    @Mock
    private SubscriptionContextService subscriptionContextService;

    @Mock
    private IUserService userService;

    @Mock
    private ITransactionHistoryService transactionHistoryService;

    @Mock
    private ISubscribedUserService subscribedUserService;

    @Mock
    private ISubscribedUserProfileService subscribedUserProfileService;

    @Mock
    private IPaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;

    @Mock
    private ISubscriptionService subscriptionService;

    @Mock
    private ICouponBasedSubscriptionService couponBasedSubscriptionService;

    @Mock
    private CreateProces createProces;

    private String email;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        email = "testuser@example.com";
    }

    // validation on changing plan unit test case
    public static SubscribedUser createSubscribedUser() {
        // Create mock User object
        User user = new User();
        user.setId(123L); // User ID
        user.setFullName("testuser");
        user.setEmail("testuser@example.com");

        // Create mock Subscription object
        Subscription subscription = new Subscription();
        subscription.setId(456L); // Subscription ID
        subscription.setName("Premium Plan");
        subscription.setDuration(12); // Duration in months

        // Create a mock Date object for startDate and endDate
        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.JANUARY, 1);
        Date startDate = calendar.getTime();

        calendar.set(2023, Calendar.JANUARY, 1);
        Date endDate = calendar.getTime();

        // Create mock SubscribedUser object
        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setId(789L);  // SubscribedUser ID
        subscribedUser.setUser(user); // Set User object
        subscribedUser.setSubscription(subscription); // Set Subscription object
        subscribedUser.setPaypalSubscriptionId("paypal123"); // Paypal subscription ID
        subscribedUser.setStartDate(startDate); // Set start date
        subscribedUser.setEndDate(endDate); // Set end date
        subscribedUser.setPaymentStatus(PaymentStatus.PAID); // Payment Status
        subscribedUser.setPaymentSubscriptionId("Payment123");
        subscribedUser.setCustomerProfileId("customerProfile123");
        subscribedUser.setSubscribedId("98745534");
        subscribedUser.setActive(true); // Subscription is active

        return subscribedUser;
    }

    private TransactionHistory createTransactionHistory() {
        // Mock Subscription object
        Subscription subscription = SubscriptionTestData.standardSubscription();

        // Mock User object
        User user = UserTestData.userData();

        // Get today's date for the mock
        Date todayDate = java.sql.Date.valueOf(LocalDate.now()); // Used for 'created_at' and 'updated_date'

        // Create TransactionHistory mock object
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setId(789L); // Mock TransactionHistory ID
        transactionHistory.setSubscription(subscription); // Set Subscription object
        transactionHistory.setCreationAt(new Date()); // Set 'created_at' to today's date
        transactionHistory.setAuthSubscriptionId("authSubId123"); // Mock Auth Subscription ID
        transactionHistory.setSubscriptionAmount(subscription.getPrice()); // Mock subscription amount
        transactionHistory.setSubscriptionStatus(SubscriptionStatus.CONTINUE); // Mock Subscription Status
        transactionHistory.setResponseCode("00"); // Mock Response Code
        transactionHistory.setResponseText("Transaction successful"); // Mock Response Text
        transactionHistory.setCustomerPaymentProfileId("customerProfile123"); // Mock Customer Payment Profile ID
        transactionHistory.setUpdatedDate(todayDate); // Set 'updated_date' to today's date
        transactionHistory.setTrialEndDate(todayDate); // Set trial end date (can be same as today for simplicity)
        transactionHistory.setSubscriptionNextCycle(java.sql.Date.valueOf(LocalDate.now().plusMonths(1))); // Subscription next cycle (next month)
        transactionHistory.setUser(user); // Set User object
        transactionHistory.setOldTransactionId(0L); // Mock old transaction ID
        transactionHistory.setStatus(GenericStatus.ACTIVE); // Mock status
        transactionHistory.setPaymentStatus(PaymentStatus.PAID); // Mock Payment Status
        transactionHistory.setSettledDate(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toLocalDateTime()); // Settled date
        transactionHistory.setExternalTransactionId("extTransId123"); // Mock external transaction ID

        return transactionHistory;
    }

    @Test
    @DisplayName("Test createSubscription_WithCouponAndNoExistingSubscription")
    void testCreateSubscription_WithCouponAndNoExistingSubscription() throws Exception {
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCoupon("FREE2025");

        User user = new User();
        SubscribedUser subscribedUser = null;
        Message message = new Message<>();
        message.setData("Success");

        when(userService.findByEmail(email)).thenReturn(user);
        when(subscribedUserService.findByUser(email)).thenReturn(null);
        when(couponBasedSubscriptionService.processCouponBasedSubscription(request, null, user))
                .thenReturn(message);

        Message<String> response = paymentSubscriptionService.create(request, email);

        assertEquals("Success", response.getData());
        verify(couponBasedSubscriptionService).processCouponBasedSubscription(request, null, user);
    }

    @Test
    @DisplayName("Test createSubscription_FromCouponToPaid")
    void testCreateSubscription_FromCouponToPaid() throws Exception {
        SubscriptionRequest request = new SubscriptionRequest();
        request.setSubscriptionId(4L);

        Coupon coupon = CouponTestData.getPremiumCouponForAllCourses();

        User user = UserTestData.userData();
        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        subscribedUser.setCoupon(coupon);

        Message<String> message = new Message<>();
        message.setData("Success");

        Message<Subscription> message1 = new Message<>();
        message1.setMessage("Success");
        message1.setData(SubscriptionTestData.standardSubscription());

        when(userService.findByEmail(email)).thenReturn(user);
        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        doNothing().when(subscriptionContextService).setProcess(any());
        when(subscriptionService.findBySubscriptionId(anyLong())).thenReturn(message1);
        when(subscriptionContextService.process(any(), any(), any(), any(), any()))
                .thenReturn(message);
        when(couponBasedSubscriptionService.processCouponBasedSubscription(request, subscribedUser, user)).thenReturn(message);

        Message<String> response = paymentSubscriptionService.create(request, email);

        assertEquals("Success", response.getData());
    }

    @Test
    @DisplayName("Test createSubscription_AlreadySubscribedToSamePlan")
    void testCreateSubscription_AlreadySubscribedToSamePlan() throws EntityNotFoundException {
        SubscriptionRequest request = new SubscriptionRequest();
        request.setSubscriptionId(2L);

        User user = UserTestData.userData();

        Subscription currentSubscription = SubscriptionTestData.standardSubscription();

        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        subscribedUser.setSubscription(currentSubscription);

        Message message = new Message<>();
        message.setMessage("Success");
        message.setData(currentSubscription);

        when(userService.findByEmail(email)).thenReturn(user);
        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(subscriptionService.findBySubscriptionId(anyLong())).thenReturn(message);

        // TransactionHistory should be yesterday
        var transactionHistory = createTransactionHistory();
        transactionHistory.setCreationAt(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
        when(transactionHistoryService.findByLatestTransactionHistoryBySubsIdAndStatus(any(), any()))
                .thenReturn(transactionHistory);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentSubscriptionService.create(request, email));

        assertEquals("You already subscribed to this plan", ex.getMessage());
    }

    @Test
    @DisplayName("Test validation on changing plan - Throws when transaction created today")
    void testValidationOnChangingPlan_ThrowsWhenTransactionCreatedToday() throws EntityNotFoundException {
        SubscribedUser subscribedUser = new SubscribedUser();
        User user = new User();
        user.setId(1L);
        subscribedUser.setUser(user);
        subscribedUser.setPaymentSubscriptionId("sub-123");

        TransactionHistory history = new TransactionHistory();
        history.setCreationAt(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        when(transactionHistoryService.findByLatestTransactionHistoryBySubsIdAndStatus("sub-123", GenericStatus.ACTIVE))
                .thenReturn(history);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentSubscriptionService.validationOnChangingPlan(subscribedUser));

        assertEquals("The transaction was created today, plan change is not allowed.", ex.getMessage());
    }

    @Test
    @DisplayName("Test validation on changing plan - No active transaction history")
    void testValidationOnChangingPlan_NoActiveTransactionHistory() throws EntityNotFoundException, BadRequestException {
        // Mock the behavior of the service
        SubscribedUser subscribedUser= createSubscribedUser();
        subscribedUser.setPaymentSubscriptionId("subscribedId123");
        when(transactionHistoryService.findByLatestTransactionHistoryBySubsIdAndStatus(
                anyString(), eq(GenericStatus.ACTIVE)))
                .thenReturn(null);  // Simulate no active transaction

        // Test the method
        boolean result = paymentSubscriptionService.validationOnChangingPlan(subscribedUser);

        // Verify the result
        assertTrue(result);
        verify(transactionHistoryService, times(1))
                .findByLatestTransactionHistoryBySubsIdAndStatus("subscribedId123", GenericStatus.ACTIVE);
    }

    @Test
    @DisplayName("Test validation on changing plan - Transaction created today")
    void testValidationOnChangingPlan_TransactionCreatedToday() throws EntityNotFoundException, BadRequestException {
        // Create a mock SubscribedUser
        SubscribedUser subscribedUser =  createSubscribedUser();

        // Create a mock TransactionHistory object
        TransactionHistory transactionHistory = mock(TransactionHistory.class);

        // Mock the behavior of the transactionHistoryService to return the transactionHistory
        when(transactionHistoryService.findByLatestTransactionHistoryBySubsIdAndStatus(
                anyString(), eq(GenericStatus.ACTIVE)))
                .thenReturn(transactionHistory);


        // Mock the creation date of the transactionHistory to be today
        doReturn(Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()))
                .when(transactionHistory).getCreationAt();

        // Call the method under test
        assertThrows(BadRequestException.class,
                () -> paymentSubscriptionService.validationOnChangingPlan(subscribedUser),
                "The plan change should not be allowed when the transaction was created today.");

        // Verify that the method findByLatestTransactionHistoryBySubsIdAndStatus was called once with the expected arguments
        verify(transactionHistoryService, times(1))
                .findByLatestTransactionHistoryBySubsIdAndStatus(anyString(), any(GenericStatus.class));
    }

    @Test
    @DisplayName("Test validation on changing plan - Transaction created on a different day")
    void testValidationOnChangingPlan_TransactionCreatedOnDifferentDay() throws EntityNotFoundException, BadRequestException {
        // Create a mock SubscribedUser
        SubscribedUser subscribedUser = createSubscribedUser();

        // Mock the TransactionHistory object
        TransactionHistory transactionHistory = mock(TransactionHistory.class);

        // Mock the behavior of the transactionHistoryService to return the mocked transactionHistory
        when(transactionHistoryService.findByLatestTransactionHistoryBySubsIdAndStatus(
                anyString(), eq(GenericStatus.ACTIVE)))
                .thenReturn(transactionHistory);

        // Set up the mocked transaction to be created on a different day
        LocalDate differentDay = LocalDate.now().minusDays(1);
        doReturn(Date.from(differentDay.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()))
                .when(transactionHistory).getCreationAt(); // Using doReturn() for better stubbing

        // Test the method
        boolean result = paymentSubscriptionService.validationOnChangingPlan(subscribedUser);

        // Verify the result - since the transaction was created on a different day, the plan change should be allowed
        assertTrue(result);

        // Verify that the method findByLatestTransactionHistoryBySubsIdAndStatus was called once with the expected arguments
        verify(transactionHistoryService, times(1))
                .findByLatestTransactionHistoryBySubsIdAndStatus("PaymentSubId123", GenericStatus.ACTIVE);
    }

    @Test
    @DisplayName("Test validation on changing plan - SubscribedUser is null")
    void testValidationOnChangingPlan_SubscribedUserNull() throws EntityNotFoundException, BadRequestException {
        // Test with null subscribed user
        assertThrows(NullPointerException.class,
                () -> paymentSubscriptionService.validationOnChangingPlan(null));
    }

    @Test
    @DisplayName("Get billing history when provided valid input")
    void getBillingHistory_whenValidInputs_thenReturnsBillingHistory() throws Exception {
        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        subscribedUser.setPaymentSubscriptionId("123456123");
        when(subscribedUserService.findByUser("test@email.com")).thenReturn(subscribedUser);

        List<BillingHistoryResponse> billingHistory = List.of(new BillingHistoryResponse());
        mockStatic(BillingHistoryResponse.class);

        // Act
        Message<List<BillingHistoryResponse>> actualMessage = paymentSubscriptionService.getBillingHistory(new BillingHistoryRequest(), "test@email.com");

        // Assert
        assertNotNull(actualMessage);
        assertEquals(HttpStatus.OK.value(), actualMessage.getStatus());
        assertEquals(HttpStatus.OK.toString(), actualMessage.getCode());
        assertEquals("Billing history has been fetched successfully", actualMessage.getMessage());

        verify(subscribedUserService, times(1)).findByUser("test@email.com");
        verify(paymentAdditionalSubscriptionService, times(1)).getSubscriptionById(subscribedUser.getPaymentSubscriptionId());
    }

    @Test
    @DisplayName("Get billing history when provided valid input")
    void getBillingHistory_whenProvidedWrongEmail_thenThrowsEntityNotFoundException() throws EntityNotFoundException, InternalServerException {
        when(subscribedUserService.findByUser("test@email.com")).thenThrow(new EntityNotFoundException("User not found"));

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            paymentSubscriptionService.getBillingHistory(new BillingHistoryRequest(), "test@email.com");
        });

        assertEquals("User not found", exception.getMessage());

        verify(subscribedUserService, times(1)).findByUser("test@email.com");
        verify(paymentAdditionalSubscriptionService, times(0)).getSubscriptionById(anyString());
    }

    @Test
    @DisplayName("Get billing history when user doesn't have any payment profile")
    void getBillingHistory_whenCustomerProfileIdIsNull_thenThrowsBadRequestException() throws EntityNotFoundException, InternalServerException {
        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        subscribedUser.setPaymentSubscriptionId(null);
        when(subscribedUserService.findByUser("test@email.com")).thenReturn(subscribedUser);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            paymentSubscriptionService.getBillingHistory(new BillingHistoryRequest(), "test@email.com");
        });

        assertEquals("Customer profile id not exists", exception.getMessage());

        verify(subscribedUserService, times(1)).findByUser("test@email.com");
        verify(paymentAdditionalSubscriptionService, times(0)).getSubscriptionById(anyString());
    }

    @Test
    @DisplayName("Get billing history when payment gateway service is down")
    void getBillingHistory_whenPaymentServiceFails_thenThrowsInternalServerException() throws Exception {
        when(subscribedUserService.findByUser("test@email.com")).thenReturn(SubscribedUserTestData.subscribedUser());
        when(paymentAdditionalSubscriptionService.getSubscriptionById(SubscribedUserTestData.subscribedUser().getPaymentSubscriptionId())).thenThrow(new InternalServerException("Service failed"));

        // Act & Assert
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            paymentSubscriptionService.getBillingHistory(new BillingHistoryRequest(), "test@email.com");
        });

        assertEquals("Service failed", exception.getMessage());

        verify(subscribedUserService, times(1)).findByUser("test@email.com");
        verify(paymentAdditionalSubscriptionService, times(1)).getSubscriptionById(SubscribedUserTestData.subscribedUser().getPaymentSubscriptionId());
    }

    @Test
    @DisplayName("Updating subscription successfully.")
    void updateSubscription_Success() throws EntityNotFoundException, BadRequestException, InternalServerException {
        var subscribedUser = new SubscribedUser();
        subscribedUser.setId(1L);
        subscribedUser.setCustomerProfileId("cust123");
        subscribedUser.setPaymentSubscriptionId("sub123");

        var subscribedUserProfile = new SubscribedUserProfile();
        subscribedUserProfile.setId(1L);
        subscribedUserProfile.setCustomerPaymentId("cust123");
        subscribedUserProfile.setIsDefault(false);

        when(subscribedUserProfileService.getSubscribedUserProfileById(1L)).thenReturn(subscribedUserProfile);
        when(subscribedUserService.findByUser("test@example.com")).thenReturn(subscribedUser);
        when(paymentAdditionalSubscriptionService.updatePaymentSubscription(anyString(), anyString(), anyString(), anyString())).thenReturn(null);

        // Act
        Message<String> response = paymentSubscriptionService.updateSubscription(1L, "test@example.com");

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Successfully updated the subscription on provided card.", response.getMessage());
        verify(subscribedUserProfileService).markAllProfileSetAsNotDefaultById(1L);
        verify(subscribedUserProfileService).save(subscribedUserProfile);
    }

    @Test
    @DisplayName("Should throw error when the provided payment profile is already active for current subscription")
    void updateSubscription_ProfileAlreadyDefault() throws EntityNotFoundException {
        var subscribedUser = new SubscribedUser();
        subscribedUser.setId(1L);
        subscribedUser.setCustomerProfileId("cust123");
        subscribedUser.setPaymentSubscriptionId("sub123");

        var subscribedUserProfile = new SubscribedUserProfile();
        subscribedUserProfile.setId(1L);
        subscribedUserProfile.setCustomerPaymentId("cust123");
        subscribedUserProfile.setIsDefault(true);

        when(subscribedUserProfileService.getSubscribedUserProfileById(1L)).thenReturn(subscribedUserProfile);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            paymentSubscriptionService.updateSubscription(1L, "test@example.com");
        });

        assertEquals("The provided payment profile is already active for current subscription.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw error when there is no subscription id or customer profile id present in the database.")
    void updateSubscription_NoPaymentSubscription() throws EntityNotFoundException {
        var subscribedUser = new SubscribedUser();
        subscribedUser.setId(1L);
        subscribedUser.setCustomerProfileId("cust123");
        subscribedUser.setPaymentSubscriptionId(null);

        var subscribedUserProfile = new SubscribedUserProfile();
        subscribedUserProfile.setCustomerPaymentId("cust123");
        subscribedUserProfile.setId(1L);
        subscribedUserProfile.setIsDefault(false);

        when(subscribedUserProfileService.getSubscribedUserProfileById(1L)).thenReturn(subscribedUserProfile);
        when(subscribedUserService.findByUser("test@example.com")).thenReturn(subscribedUser);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            paymentSubscriptionService.updateSubscription(1L, "test@example.com");
        });

        assertEquals("Current subscription doesn't have a authorized subscription.", exception.getMessage());
    }

    @Test
    void testFreeSignUpSubscription_Success() throws Exception {
        User user = UserTestData.userData();
        Subscription subscription = SubscriptionTestData.freeSubscription();

        Message<String> expectedResponse = new Message<String>()
                .setStatus(200)
                .setMessage("Free subscription created");

        when(userService.findByEmail(email)).thenReturn(user);
        when(subscriptionService.findBySubscriptionId(subscription.getId())).thenReturn(new Message<Subscription>().setData(subscription));
        when(createProces.createFreeSubscription(subscription, user)).thenReturn(expectedResponse);

        Message<String> actualResponse = paymentSubscriptionService.freeSignUpSubscription(1L, email);

        assertEquals(expectedResponse, actualResponse);
        verify(userService).findByEmail(email);
        verify(subscriptionService).findBySubscriptionId(1L);
        verify(createProces).createFreeSubscription(subscription, user);
    }

    @Test
    void testFreeSignUpSubscription_UserNotFound() throws EntityNotFoundException {
        when(userService.findByEmail(email)).thenThrow(new EntityNotFoundException("User not found"));

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
            paymentSubscriptionService.freeSignUpSubscription(1L, email);
        });

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void testFreeSignUpSubscription_SubscriptionNotFound() throws EntityNotFoundException {
        User user = UserTestData.userData();
        when(userService.findByEmail(email)).thenReturn(user);
        when(subscriptionService.findBySubscriptionId(anyLong()))
                .thenThrow(new EntityNotFoundException("Subscription not found"));

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
            paymentSubscriptionService.freeSignUpSubscription(1L, email);
        });

        assertEquals("Subscription not found", ex.getMessage());
    }

    @Test
    void testFreeSignUpSubscription_CreateFreeFails() throws InternalServerException, EntityNotFoundException {
        User user = new User();
        Subscription subscription = new Subscription();

        when(userService.findByEmail(email)).thenReturn(user);
        when(subscriptionService.findBySubscriptionId(anyLong())).thenReturn(new Message<Subscription>().setData(subscription));
        when(createProces.createFreeSubscription(subscription, user))
                .thenThrow(new InternalServerException("Free subscription failed"));

        InternalServerException ex = assertThrows(InternalServerException.class, () -> {
            paymentSubscriptionService.freeSignUpSubscription(1L, email);
        });

        assertEquals("Free subscription failed", ex.getMessage());
    }
}
