package com.vinncorp.fast_learner.mock.subscription.subscribed_user;

import com.vinncorp.fast_learner.dtos.payout.PaidUser;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.subscription.SubscriptionTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.permission.Permission;
import com.vinncorp.fast_learner.models.permission.SubscriptionPermission;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.subscription.SubscribedUserRepository;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionRepository;
import com.vinncorp.fast_learner.response.subscription.CurrentSubscriptionResponse;
import com.vinncorp.fast_learner.services.payment.additional_service.PaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.subscription.SubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.SubscriptionService;
import com.vinncorp.fast_learner.services.subscription.subscribed_user_profile.SubscribedUserProfileService;
import com.vinncorp.fast_learner.services.subscription_permission.ISubscriptionPermissionService;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.PaymentStatus;
import com.vinncorp.fast_learner.util.enums.PermissionName;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SubscribedUserServiceTest {

    @InjectMocks
    private SubscribedUserService service;

    @Mock
    private SubscribedUserRepository repo;

    @Mock
    private SubscriptionRepository subscriptionRepo;

    @Mock
    private PaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;

    @Mock
    private SubscribedUserProfileService subscribedUserProfileService;

    @Mock
    private UserService userService;

    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private ISubscriptionPermissionService subscriptionPermissionService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test: Save SubscribedUser - Success")
    public void testSaveSubscribedUser_Success() {
        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();

        when(repo.save(subscribedUser)).thenReturn(null);

        assertDoesNotThrow(() -> service.save(subscribedUser));
        verify(repo, times(1)).save(subscribedUser);
    }

    @Test
    @DisplayName("Test: Save SubscribedUser - InternalServerException")
    public void testSaveSubscribedUser_InternalServerException() {
        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();

        doThrow(new RuntimeException("Database error")).when(repo).save(any(SubscribedUser.class));

        InternalServerException thrownException = assertThrows(InternalServerException.class,
                () -> service.save(subscribedUser)
        );

        assertEquals("SubscribedUser " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, thrownException.getMessage());
        verify(repo, times(1)).save(subscribedUser);
    }

    @Test
    @DisplayName("Test: Complete Subscription - Success")
    public void testCompleteSubscription_Success() throws EntityNotFoundException, InternalServerException {
        String paypalSubscriptionId = "PAYPAL123";

        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        User user = UserTestData.userData();

        when(repo.findByUserEmail(user.getEmail())).thenReturn(Optional.of(subscribedUser));
        when(repo.save(any(SubscribedUser.class))).thenReturn(null);

        when(userService.findByEmail(user.getEmail())).thenReturn(user);
        when(userService.save(user)).thenReturn(null);

        Message<String> response = service.completeSubscription(paypalSubscriptionId, user.getEmail());

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Subscription is completed.", response.getMessage());
        assertEquals("Subscription is completed.", response.getData());

        assertTrue(subscribedUser.isActive());
        assertEquals(PaymentStatus.PAID, subscribedUser.getPaymentStatus());
        assertEquals(paypalSubscriptionId, subscribedUser.getPaypalSubscriptionId());

        verify(repo, times(1)).findByUserEmail(user.getEmail());
        verify(repo, times(1)).save(subscribedUser);
    }

    @Test
    @DisplayName("Test: Complete Subscription - User Not Found")
    public void testCompleteSubscription_UserNotFound() {
        String paypalSubscriptionId = "PAYPAL123";
        String email = "test@example.com";

        when(repo.findByUserEmail(email)).thenReturn(Optional.empty());

        EntityNotFoundException thrownException = assertThrows(EntityNotFoundException.class,
                () -> service.completeSubscription(paypalSubscriptionId, email)
        );

        assertEquals("User doesn't subscribed any plan.", thrownException.getMessage());
        verify(repo, times(1)).findByUserEmail(email);
        verify(repo, times(0)).save(any(SubscribedUser.class));
    }

    @Test
    @DisplayName("Test: Complete Subscription - Save Failure")
    public void testCompleteSubscription_SaveFailure() {
        String paypalSubscriptionId = "PAYPAL123";
        String email = "test@example.com";

        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();

        when(repo.findByUserEmail(email)).thenReturn(Optional.of(subscribedUser));
        doThrow(new RuntimeException("Database error")).when(repo).save(any(SubscribedUser.class));

        InternalServerException thrownException = assertThrows(InternalServerException.class,
                () -> service.completeSubscription(paypalSubscriptionId, email)
        );

        assertEquals("SubscribedUser " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, thrownException.getMessage());
        verify(repo, times(1)).findByUserEmail(email);
        verify(repo, times(1)).save(subscribedUser);
    }

    @Test
    @DisplayName("Test: Complete Payment Subscription - Success")
    public void testCompletePaymentSubscription_Success() throws EntityNotFoundException, InternalServerException {
        String paymentSubscriptionId = "AUTHNET123";

        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        User user = UserTestData.userData();

        when(repo.findByUserEmail(user.getEmail())).thenReturn(Optional.of(subscribedUser));
        when(userService.findByEmail(user.getEmail())).thenReturn(user);
        when(userService.save(user)).thenReturn(null);

        Message<String> response = service.completePaymentSubscription(paymentSubscriptionId, user.getEmail());

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Payment Subscription is completed.", response.getMessage());
        assertEquals("Payment Subscription is completed.", response.getData());

        assertEquals(paymentSubscriptionId, subscribedUser.getSubscribedId());
        assertEquals(paymentSubscriptionId, subscribedUser.getPaymentSubscriptionId());
        assertEquals(PaymentStatus.PAID, subscribedUser.getPaymentStatus());

        verify(repo, times(1)).findByUserEmail(user.getEmail());
    }

    @Test
    @DisplayName("Test: Complete Payment Subscription - User Not Found")
    public void testCompletePaymentSubscription_UserNotFound() {
        String paymentSubscriptionId = "AUTHNET123";

        User user = UserTestData.userData();

        when(repo.findByUserEmail(user.getEmail())).thenReturn(Optional.empty());

        EntityNotFoundException thrownException = assertThrows(EntityNotFoundException.class,
                () -> service.completePaymentSubscription(paymentSubscriptionId, user.getEmail())
        );

        assertEquals("User doesn't subscribed any plan", thrownException.getMessage());
        verify(repo, times(1)).findByUserEmail(user.getEmail());
    }

    @Test
    @DisplayName("Test: Complete Payment Subscription - Update User Failure")
    public void testCompletePaymentSubscription_UpdateUserFailure() throws EntityNotFoundException, InternalServerException {
        String paymentSubscriptionId = "AUTHNET123";

        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        User user = UserTestData.userData();

        when(repo.findByUserEmail(user.getEmail())).thenReturn(Optional.of(subscribedUser));
        when(userService.findByEmail(user.getEmail())).thenReturn(user);
        when(userService.save(user)).thenThrow(new InternalServerException("Database error"));

        InternalServerException thrownException = assertThrows(InternalServerException.class,
                () -> service.completePaymentSubscription(paymentSubscriptionId, user.getEmail())
        );

        assertEquals("Database error", thrownException.getMessage());
        verify(repo, times(1)).findByUserEmail(user.getEmail());
        verify(userService, times(1)).findByEmail(user.getEmail());
    }

    @Test
    @DisplayName("Test: Cancel Subscription - Success (Payment)")
    public void testCancelSubscription_Success_PayPal() throws EntityNotFoundException, BadRequestException, InternalServerException {
        String email = "test@example.com";
        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();

        when(subscriptionService.findBySubscriptionId(1L)).thenReturn(new Message<Subscription>().setData(new Subscription()));

        when(repo.findByUserEmail(email)).thenReturn(Optional.of(subscribedUser));

        doNothing().when(paymentAdditionalSubscriptionService).cancelPaymentSubscription(subscribedUser.getPaymentSubscriptionId());
        doNothing().when(subscribedUserProfileService).deleteBySubscribedUserId(subscribedUser);
        when(repo.save(subscribedUser)).thenReturn(null);

        Message<String> response = service.cancelSubscription(email);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Subscription is canceled successfully.", response.getMessage());
        assertEquals("Subscription is canceled successfully.", response.getData());

        assertNull(subscribedUser.getPaypalSubscriptionId());
        assertNull(subscribedUser.getPaymentSubscriptionId());

        verify(repo, times(1)).findByUserEmail(email);
        verify(paymentAdditionalSubscriptionService, times(1)).cancelPaymentSubscription(any());
        verify(subscribedUserProfileService, times(1)).deleteBySubscribedUserId(any());
        verify(repo, times(1)).save(subscribedUser);
    }

    @Test
    @DisplayName("Test: Cancel Subscription - No Active Subscription")
    public void testCancelSubscription_NoActiveSubscription() throws EntityNotFoundException, InternalServerException, BadRequestException {
        String email = "test@example.com";
        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        subscribedUser.setPaymentSubscriptionId(null);
        subscribedUser.setPaypalSubscriptionId(null);

        when(subscriptionService.findBySubscriptionId(1L)).thenReturn(new Message<Subscription>().setData(new Subscription()));
        when(repo.findByUserEmail(email)).thenReturn(Optional.of(subscribedUser));

        BadRequestException thrownException = assertThrows(BadRequestException.class,
                () -> service.cancelSubscription(email)
        );

        assertEquals("Free plan cannot be canceled.", thrownException.getMessage());

        verify(repo, times(1)).findByUserEmail(email);
        verify(paymentAdditionalSubscriptionService, times(0)).cancelPaymentSubscription(anyString());
        verify(repo, times(0)).save(subscribedUser);
    }

    @Test
    @DisplayName("Test: Cancel Subscription - User Not Found")
    public void testCancelSubscription_UserNotFound() throws EntityNotFoundException, InternalServerException, BadRequestException {
        String email = "test@example.com";

        when(subscriptionService.findBySubscriptionId(1L)).thenReturn(new Message<Subscription>().setData(new Subscription()));
        when(repo.findByUserEmail(email)).thenReturn(Optional.empty());

        EntityNotFoundException thrownException = assertThrows(EntityNotFoundException.class,
                () -> service.cancelSubscription(email)
        );

        assertEquals("Subscribed user is not found.", thrownException.getMessage());

        verify(repo, times(1)).findByUserEmail(email);
        verify(paymentAdditionalSubscriptionService, times(0)).cancelPaymentSubscription(anyString());
        verify(repo, times(0)).save(any(SubscribedUser.class));
    }

    @Test
    @DisplayName("Test: Get Current Subscription - Success (Paid Subscription)")
    public void testGetCurrentSubscription_Success_PaidSubscription() throws EntityNotFoundException {
        String email = "test@example.com";
        Subscription subscription = SubscriptionTestData.standardSubscription();

        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        subscribedUser.setSubscription(subscription);

        when(repo.findByUserEmail(email)).thenReturn(Optional.of(subscribedUser));

        Message<CurrentSubscriptionResponse> response = service.getCurrentSubscription(email);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Fetching current subscriptions successfully.", response.getMessage());
        assertEquals("Standard Plan", response.getData().getPlanName());
        assertEquals("Your plan will be automatically renewed after each month excluding free trial period. It will be charged as one payment of $10.0 USD after 1 months.", response.getData().getPlanMessage());
        assertEquals("10.0", response.getData().getPlanPrice());
        assertEquals("2 weeks free trial", response.getData().getFreeTrialMessage());

        verify(repo, times(1)).findByUserEmail(email);
    }

    @Test
    @DisplayName("Test: Get Current Subscription - Success (Free Plan)")
    public void testGetCurrentSubscription_Success_FreePlan() throws EntityNotFoundException {
        String email = "test@example.com";
        Subscription subscription = SubscriptionTestData.freeSubscription();

        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        subscribedUser.setSubscription(subscription);

        when(repo.findByUserEmail(email)).thenReturn(Optional.of(subscribedUser));

        Message<CurrentSubscriptionResponse> response = service.getCurrentSubscription(email);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Fetching current subscriptions successfully.", response.getMessage());
        assertEquals("Free Plan", response.getData().getPlanName());
        assertEquals("Your plan will be automatically renewed after each month excluding free trial period. It will be charged as one payment of $0.0 USD after 0 months.", response.getData().getPlanMessage());
        assertEquals("0.0", response.getData().getPlanPrice());
        assertEquals("2 weeks free trial", response.getData().getFreeTrialMessage());

        verify(repo, times(1)).findByUserEmail(email);
    }

    @Test
    @DisplayName("Test: Get Current Subscription - User Not Found")
    public void testGetCurrentSubscription_UserNotFound() throws EntityNotFoundException {
        String email = "test@example.com";

        when(repo.findByUserEmail(email)).thenReturn(Optional.empty());

        EntityNotFoundException thrownException = assertThrows(EntityNotFoundException.class,
                () -> service.getCurrentSubscription(email)
        );

        assertEquals("Subscribed user is not found.", thrownException.getMessage());

        verify(repo, times(1)).findByUserEmail(email);
    }



    //mock test
    @Test
    @DisplayName("Test: Get Current Subscription - Success (Paid Subscription with Permissions)")
    public void testGetCurrentSubscription_Success_PaidSubscriptionWithPermissions() throws EntityNotFoundException {
        String email = "test@example.com";

        // Mock subscription and subscribed user
        Subscription subscription = SubscriptionTestData.standardSubscription();
        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        subscribedUser.setSubscription(subscription);
        subscribedUser.setPaypalSubscriptionId("paypal-id");

        // Mock permissions
        Permission permission1 = new Permission();
        permission1.setName(PermissionName.PREMIUM_COURSE);

        Permission permission2 = new Permission();
        permission2.setName(PermissionName.AFFILIATE);

        SubscriptionPermission subscriptionPermission1 = mock(SubscriptionPermission.class);
        SubscriptionPermission subscriptionPermission2 = mock(SubscriptionPermission.class);

        when(subscriptionPermission1.getPermission()).thenReturn(permission1);
        when(subscriptionPermission2.getPermission()).thenReturn(permission2);

        // Mock repository and service calls
        when(repo.findByUserEmail(email)).thenReturn(Optional.of(subscribedUser));
        when(subscriptionPermissionService.findBySubscriptionAndIsActive(subscription.getId()))
                .thenReturn(Arrays.asList(subscriptionPermission1, subscriptionPermission2));

        // Execute the service method
        Message<CurrentSubscriptionResponse> response = service.getCurrentSubscription(email);

        // Assertions
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Fetching current subscriptions successfully.", response.getMessage());
        assertEquals("Standard Plan", response.getData().getPlanName());
        assertEquals("Your plan will be automatically renewed after each month excluding free trial period. It will be charged as one payment of $10.0 USD after 1 months.", response.getData().getPlanMessage());
        assertEquals("10.0", response.getData().getPlanPrice());
        assertEquals("2 weeks free trial", response.getData().getFreeTrialMessage());
        assertEquals(Arrays.asList("PREMIUM_COURSE", "AFFILIATE"), response.getData().getPermissions());

        // Verify interactions
        verify(repo, times(1)).findByUserEmail(email);
        verify(subscriptionPermissionService, times(1)).findBySubscriptionAndIsActive(subscription.getId());
    }

    // TODO ERROR: Resolve below test method
    @Test
    @DisplayName("Test: Get Current Subscription - Paid Subscription No Permissions")
    public void testGetCurrentSubscription_PaidSubscriptionNoPermissions() throws EntityNotFoundException {
        String email = "test@example.com";
        Subscription subscription = SubscriptionTestData.standardSubscription();
        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        subscribedUser.setSubscription(subscription);

        when(repo.findByUserEmail(email)).thenReturn(Optional.of(subscribedUser));
        when(subscriptionPermissionService.findBySubscriptionAndIsActive(subscription.getId()))
                .thenReturn(Collections.emptyList());

        Message<CurrentSubscriptionResponse> response = service.getCurrentSubscription(email);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Fetching current subscriptions successfully.", response.getMessage());
        assertEquals("Standard Plan", response.getData().getPlanName());
        assertTrue(response.getData().getPermissions().isEmpty());

        verify(repo, times(1)).findByUserEmail(email);
        verify(subscriptionPermissionService, times(1)).findBySubscriptionAndIsActive(subscription.getId());
    }

    @Test
    @DisplayName("Test: Find Subscribed User by Subscribed ID - Success")
    public void testFindBySubscribedId_Success() {
        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        subscribedUser.setSubscribedId("subscribed-1");

        when(repo.findBySubscribedId(subscribedUser.getSubscribedId())).thenReturn(subscribedUser);

        SubscribedUser result = service.findBySubscribedId(subscribedUser.getSubscribedId());

        assertNotNull(result);
        assertEquals("subscribed-1", result.getSubscribedId());
        verify(repo, times(1)).findBySubscribedId(subscribedUser.getSubscribedId());
    }

    @Test
    @DisplayName("Test: Find Subscribed User by Subscribed ID - Not Found")
    public void testFindBySubscribedId_NotFound() {
        String subscribedId = "subscribed123";

        when(repo.findBySubscribedId(subscribedId)).thenReturn(null);

        SubscribedUser result = service.findBySubscribedId(subscribedId);

        assertNull(result);
        verify(repo, times(1)).findBySubscribedId(subscribedId);
    }

    @Test
    @DisplayName("Test: Find Subscribed User by Customer Profile ID - Success")
    public void testFindByCustomerProfileId_Success() {
        String customerProfileId = "customer123";
        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setCustomerProfileId(customerProfileId);

        when(repo.findByCustomerProfileId(customerProfileId)).thenReturn(Optional.of(subscribedUser));

        SubscribedUser result = service.findByCustomerProfileId(customerProfileId);

        assertNotNull(result);
        assertEquals(customerProfileId, result.getCustomerProfileId());
        verify(repo, times(1)).findByCustomerProfileId(customerProfileId);
    }

    @Test
    @DisplayName("Test: Find Subscribed User by Customer Profile ID - Not Found")
    public void testFindByCustomerProfileId_NotFound() {
        String customerProfileId = "customer123";

        when(repo.findByCustomerProfileId(customerProfileId)).thenReturn(Optional.empty());

        SubscribedUser result = service.findByCustomerProfileId(customerProfileId);

        assertNull(result);
        verify(repo, times(1)).findByCustomerProfileId(customerProfileId);
    }

    @Test
    @DisplayName("Test: Fetch All Paid Subscribed Users After Trial Period - Success")
    public void testFetchAllPaidSubscriptionAfterTrialPeriod_Success() {
        List<Tuple> paidUsers = new ArrayList<>();
        Tuple tuple1 = mock(Tuple.class);
        Tuple tuple2 = mock(Tuple.class);
        when(tuple1.get("user_id")).thenReturn(1L);
        when(tuple1.get("subscription_fee")).thenReturn(15.0);
        when(tuple1.get("user_id")).thenReturn(2L);
        when(tuple1.get("subscription_fee")).thenReturn(15.0);

        List<PaidUser> expectedPaidUsers = new ArrayList<>();

        when(repo.fetchAllPaidUsers()).thenReturn(paidUsers);

        List<PaidUser> result = service.fetchAllPaidSubscriptionAfterTrialPeriod();

        assertNotNull(result);
        assertEquals(expectedPaidUsers.size(), result.size());
        verify(repo, times(1)).fetchAllPaidUsers();
    }

    @Test
    @DisplayName("Test: Fetch All Paid Subscribed Users After Trial Period - No Users Found")
    public void testFetchAllPaidSubscriptionAfterTrialPeriod_NoUsersFound() {
        when(repo.fetchAllPaidUsers()).thenReturn(Collections.emptyList());

        List<PaidUser> result = service.fetchAllPaidSubscriptionAfterTrialPeriod();

        assertTrue(result.isEmpty());
        verify(repo, times(1)).fetchAllPaidUsers();
    }

    @Test
    @DisplayName("Test: Fetch Subscribed User by Customer Profile ID - Success")
    public void testFetchByCustomerProfileId_Success() throws EntityNotFoundException {
        String customerProfileId = "customer123";
        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setCustomerProfileId(customerProfileId);

        when(repo.findByCustomerProfileId(customerProfileId)).thenReturn(Optional.of(subscribedUser));

        SubscribedUser result = service.fetchByCustomerProfileId(customerProfileId);

        assertNotNull(result);
        assertEquals(customerProfileId, result.getCustomerProfileId());
        verify(repo, times(1)).findByCustomerProfileId(customerProfileId);
    }

    @Test
    @DisplayName("Test: Fetch Subscribed User by Customer Profile ID - Not Found")
    public void testFetchByCustomerProfileId_NotFound() {
        String customerProfileId = "customer123";

        when(repo.findByCustomerProfileId(customerProfileId)).thenReturn(Optional.empty());

        EntityNotFoundException thrownException = assertThrows(EntityNotFoundException.class,
                () -> service.fetchByCustomerProfileId(customerProfileId)
        );

        assertEquals("No data found.", thrownException.getMessage());
        verify(repo, times(1)).findByCustomerProfileId(customerProfileId);
    }
}
