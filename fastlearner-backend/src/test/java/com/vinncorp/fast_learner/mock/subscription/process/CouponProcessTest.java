package com.vinncorp.fast_learner.mock.subscription.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.coupon.CouponTestData;
import com.vinncorp.fast_learner.mock.subscription.SubscriptionRequestTestData;
import com.vinncorp.fast_learner.mock.subscription.SubscriptionTestData;
import com.vinncorp.fast_learner.mock.subscription.subscribed_user.SubscribedUserTestData;
import com.vinncorp.fast_learner.mock.transaction_history.TransactionHistoryTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.SubscribedUserProfile;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionLogRepository;
import com.vinncorp.fast_learner.response.customer_profile.CustomerProfileIdType;
import com.vinncorp.fast_learner.response.message.MessageTypeEnum;
import com.vinncorp.fast_learner.response.message.MessagesType;
import com.vinncorp.fast_learner.response.subscription.CreateSubscriptionResponse;
import com.vinncorp.fast_learner.services.payment.additional_service.IPaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.payment.payment_profile.IPaymentProfileService;
import com.vinncorp.fast_learner.services.notification.IInstructorPerformanceInsightService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.process.CouponProcess;
import com.vinncorp.fast_learner.services.subscription.subscribed_user_profile.ISubscribedUserProfileService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.List;

class CouponProcessTest {
    @InjectMocks
    private CouponProcess couponProcess;

    @Mock
    private IPaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;

    @Mock
    private IInstructorPerformanceInsightService instructorPerformanceInsightService;

    @Mock
    private IPaymentProfileService paymentProfileService;

    @Mock
    private ISubscribedUserProfileService subscribedUserProfileService;

    @Mock
    private ISubscribedUserService subscribedUserService;

    @Mock
    private ITransactionHistoryService transactionHistoryService;

    @Mock
    private IUserService userService;

    @Mock
    private SubscriptionLogRepository subscriptionLogRepository;

    private CreateSubscriptionResponse createSubscriptionResponse;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        CustomerProfileIdType customerProfileIdType = new CustomerProfileIdType();
        customerProfileIdType.setCustomerPaymentProfileId("111111111");
        customerProfileIdType.setCustomerProfileId("222222222");

        var msg = new com.vinncorp.fast_learner.response.message.Message();
        msg.setCode("OK");
        msg.setText("Success");

        MessagesType messagesType = new MessagesType();
        messagesType.setResultCode(MessageTypeEnum.OK);
        messagesType.setMessage(List.of(msg));

        createSubscriptionResponse = new CreateSubscriptionResponse();
        createSubscriptionResponse.setMessages(messagesType);
        createSubscriptionResponse.setSubscriptionId("sub-101");
        createSubscriptionResponse.setProfile(customerProfileIdType);
    }

    @Test
    @DisplayName("Moving from FREE to STANDARD subscription when provided valid data")
    void testProcessSubscription_movingFromFreeToStandard_whenProvidedValidData() throws BadRequestException, EntityNotFoundException, InternalServerException {
        // Arrange
        SubscriptionRequest requestDTO = SubscriptionRequestTestData.subsReqForCouponBasedSubscription_standardSubscription();
        SubscribedUser subscribedUser = SubscribedUserTestData.freeSubscribedUser();
        Subscription nextSubscription = SubscriptionTestData.standardSubscription();
        User user = UserTestData.userData();

        SubscribedUserProfile profile = new SubscribedUserProfile();
        profile.setCustomerPaymentId("cust-id");
        profile.setCustomerPaymentProfileId("pay-id");
        profile.setIsDefault(true);

        when(subscribedUserProfileService.getDefaultBySubscribedUserId(any(), eq(true))).thenReturn(profile);

        when(transactionHistoryService.findByLatestTransactionHistoryByUserIdAndStatus(any(), eq(GenericStatus.ACTIVE)))
                .thenReturn(TransactionHistoryTestData.freeTransactionHistory());

        when(paymentAdditionalSubscriptionService.createCouponBased(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(createSubscriptionResponse);

        var message = couponProcess.processSubscription(requestDTO, subscribedUser, nextSubscription, user, CouponTestData.standardSubscriptionCoupon());

        assertEquals(HttpStatus.OK.value(), message.getStatus());

        verify(transactionHistoryService, times(2)).save(any(TransactionHistory.class));
    }

    @Test
    @DisplayName("Moving from coupon based STANDARD to coupon based PREMIUM subscription when provided valid data")
    void testProcessSubscription_movingFromStandardCouponToPremiumCouponBasedSubscription_whenProvidedValidData() throws BadRequestException, EntityNotFoundException, InternalServerException {
        // Arrange
        SubscriptionRequest requestDTO = SubscriptionRequestTestData.subsReqForCouponBasedSubscription_standardSubscription();
        SubscribedUser subscribedUser = SubscribedUserTestData.freeSubscribedUser();
        Subscription nextSubscription = SubscriptionTestData.standardSubscription();
        User user = UserTestData.userData();

        SubscribedUserProfile profile = new SubscribedUserProfile();
        profile.setCustomerPaymentId("cust-id");
        profile.setCustomerPaymentProfileId("pay-id");
        profile.setIsDefault(true);

        when(subscribedUserProfileService.getDefaultBySubscribedUserId(any(), eq(true))).thenReturn(profile);

        when(transactionHistoryService.findByLatestTransactionHistoryByUserIdAndStatus(any(), eq(GenericStatus.ACTIVE)))
                .thenReturn(TransactionHistoryTestData.couponBasedStandardTransactionHistory());

        when(transactionHistoryService.findByLatestUserAndSubscriptionStatus(any(), any()))
                .thenReturn(TransactionHistoryTestData.couponBasedStandardTransactionHistory());

        when(paymentAdditionalSubscriptionService.createCouponBased(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(createSubscriptionResponse);

        var message = couponProcess.processSubscription(requestDTO, subscribedUser, nextSubscription, user, CouponTestData.standardSubscriptionCoupon());

        assertEquals(HttpStatus.OK.value(), message.getStatus());

        verify(transactionHistoryService, times(1)).save(any(TransactionHistory.class));
    }

    @Test
    @DisplayName("Moving from paid STANDARD to coupon based PREMIUM subscription when provided valid data")
    void testProcessSubscription_movingFromPaidStandardToPremiumCouponBasedSubscription_whenProvidedValidData() throws BadRequestException, EntityNotFoundException, InternalServerException {
        // Arrange
        SubscriptionRequest requestDTO = SubscriptionRequestTestData.subsReqForCouponBasedSubscription_standardSubscription();
        SubscribedUser subscribedUser = SubscribedUserTestData.freeSubscribedUser();
        Subscription nextSubscription = SubscriptionTestData.standardSubscription();
        User user = UserTestData.userData();

        SubscribedUserProfile profile = new SubscribedUserProfile();
        profile.setCustomerPaymentId("cust-id");
        profile.setCustomerPaymentProfileId("pay-id");
        profile.setIsDefault(true);

        when(subscribedUserProfileService.getDefaultBySubscribedUserId(any(), eq(true))).thenReturn(profile);

        when(transactionHistoryService.findByLatestTransactionHistoryByUserIdAndStatus(any(), eq(GenericStatus.ACTIVE)))
                .thenReturn(TransactionHistoryTestData.standardTransactionHistory());

        when(transactionHistoryService.findByLatestUserAndSubscriptionStatus(any(), any()))
                .thenReturn(TransactionHistoryTestData.standardTransactionHistory());

        when(paymentAdditionalSubscriptionService.createCouponBased(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(createSubscriptionResponse);

        var message = couponProcess.processSubscription(requestDTO, subscribedUser, nextSubscription, user, CouponTestData.standardSubscriptionCoupon());

        assertEquals(HttpStatus.OK.value(), message.getStatus());

        verify(transactionHistoryService, times(1)).save(any(TransactionHistory.class));
    }
}

