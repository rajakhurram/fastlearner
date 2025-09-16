package com.vinncorp.fast_learner.mock.payment.checkout;

import com.vinncorp.fast_learner.config.GenericRestClient;
import com.vinncorp.fast_learner.dtos.payment.checkout.ChargePayment;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.coupon.CouponTestData;
import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.affiliate.AffiliatedCoursesRepository;
import com.vinncorp.fast_learner.repositories.affiliate.InstructorAffiliateRepository;
import com.vinncorp.fast_learner.response.payment_checkout.CreateTransactionResponse;
import com.vinncorp.fast_learner.response.payment_checkout.TransactionResponse;
import com.vinncorp.fast_learner.response.message.MessageTypeEnum;
import com.vinncorp.fast_learner.response.message.MessagesType;
import com.vinncorp.fast_learner.services.affiliate.affiliate_course_service.IAffiliateCourseService;
import com.vinncorp.fast_learner.services.payment.checkout.PaymentCheckoutService;
import com.vinncorp.fast_learner.services.coupon.ICouponService;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.payout.premium_course.PayoutContextService;
import com.vinncorp.fast_learner.services.payout.premium_course.SelfAffiliatePayoutService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CouponType;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import com.vinncorp.fast_learner.util.enums.CourseType;
import com.vinncorp.fast_learner.util.enums.PayoutType;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentCheckoutServiceTest {

    @InjectMocks
    private PaymentCheckoutService service;

    @Mock
    private IEnrollmentService enrollmentService;

    @Mock
    private IUserService userService;

    @Mock
    private ICourseService courseService;

    @Mock
    private PayoutContextService payoutContextService;

    @Mock
    private ICouponService couponService;

    @Mock
    private IAffiliateCourseService affiliateCourseService;

    @Mock
    private AffiliatedCoursesRepository affiliateCourseRepo;

    @Mock
    private InstructorAffiliateRepository instructorAffiliateRepo;

    @Mock
    private SelfAffiliatePayoutService selfAffiliatePayoutService;

    @Mock
    private GenericRestClient restClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test charge payment success without coupon")
    void testChargePayment_SuccessWithoutCoupon() throws IOException, EntityNotFoundException, InternalServerException, BadRequestException {
        var chargePayment= new ChargePayment("course123", 29L, null, null);
        var user = UserTestData.userData();
        var course = CourseTestData.courseData();
        course.setId(29L);
        course.setCourseStatus(CourseStatus.PUBLISHED);
        course.setCourseType(CourseType.PREMIUM_COURSE);
        course.setPrice(100.0);

        when(userService.findByEmail(anyString())).thenReturn(user);
        when(courseService.findById(anyLong())).thenReturn(course);

        var transaction = new TransactionResponse();
        transaction.setTransId("12345");

        var message = new com.vinncorp.fast_learner.response.message.Message();
        message.setCode("I00001");
        message.setText("Successfully charged");

        var messageType = new MessagesType();
        messageType.setMessage(List.of(message));
        messageType.setResultCode(MessageTypeEnum.OK);

        var createTransactionResponse = new CreateTransactionResponse();
        createTransactionResponse.setMessages(messageType);
        createTransactionResponse.setTransactionResponse(transaction);

        when(restClient.makeRequest(anyString(), any(), any(), any())).thenReturn(createTransactionResponse);

        Message<String> result = service.chargePayment(chargePayment, "test@example.com");

        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertTrue(result.getMessage().contains("Successfully charged"));
        verify(enrollmentService).enrolled(29L, "test@example.com", false);
        verify(payoutContextService).setPayoutStrategy(PayoutType.DIRECT);
        verify(payoutContextService).executePayout(course, user, "12345", null, null);
    }

    @Test
    @DisplayName("Test charge payment success with coupon")
    void testChargePayment_SuccessWithCoupon() throws IOException, EntityNotFoundException, InternalServerException, BadRequestException {
        var chargePayment= new ChargePayment("course123", 29L, "DISCOUNT10", null);
        var user = UserTestData.userData();
        var course = CourseTestData.courseData();
        course.setId(29L);
        course.setCourseStatus(CourseStatus.PUBLISHED);
        course.setCourseType(CourseType.PREMIUM_COURSE);
        course.setPrice(100.0);
        var coupon = CouponTestData.getPremiumCouponForAllCourses();

        when(userService.findByEmail(anyString())).thenReturn(user);
        when(courseService.findById(anyLong())).thenReturn(course);
        when(couponService.validateCoupon("DISCOUNT10", user, CouponType.PREMIUM, 29L)).thenReturn(coupon);

        var transaction = new TransactionResponse();
        transaction.setTransId("12345");

        var message = new com.vinncorp.fast_learner.response.message.Message();
        message.setCode("I00001");
        message.setText("Successfully charged");

        var messageType = new MessagesType();
        messageType.setMessage(List.of(message));
        messageType.setResultCode(MessageTypeEnum.OK);

        var createTransactionResponse = new CreateTransactionResponse();
        createTransactionResponse.setMessages(messageType);
        createTransactionResponse.setTransactionResponse(transaction);

        when(restClient.makeRequest(anyString(), any(), any(), any())).thenReturn(createTransactionResponse);

        Message<String> result = service.chargePayment(chargePayment, "test@example.com");

        assertEquals(HttpStatus.OK.value(), result.getStatus());
        assertTrue(result.getMessage().contains("Successfully charged"));
        verify(enrollmentService).enrolled(29L, "test@example.com", false);
    }

    @Test
    @DisplayName("Test charge payment payout as self affiliate")
    void testChargePayment_PayoutAsSelfAffiliate() throws IOException, EntityNotFoundException, InternalServerException, BadRequestException {
        var chargePayment= new ChargePayment("course123", 29L, "DISCOUNT10", "uuid-123");
        var user = UserTestData.userData();
        var course = CourseTestData.courseData();
        course.setId(29L);
        course.setCourseStatus(CourseStatus.PUBLISHED);
        course.setCourseType(CourseType.PREMIUM_COURSE);
        course.setPrice(100.0);

        when(userService.findByEmail(anyString())).thenReturn(user);
        when(courseService.findById(anyLong())).thenReturn(course);

        var transaction = new TransactionResponse();
        transaction.setTransId("12345");

        var message = new com.vinncorp.fast_learner.response.message.Message();
        message.setCode("I00001");
        message.setText("Successfully charged");

        var messageType = new MessagesType();
        messageType.setMessage(List.of(message));
        messageType.setResultCode(MessageTypeEnum.OK);

        var createTransactionResponse = new CreateTransactionResponse();
        createTransactionResponse.setMessages(messageType);
        createTransactionResponse.setTransactionResponse(transaction);

        when(restClient.makeRequest(anyString(), any(), any(), any())).thenReturn(createTransactionResponse);

        Tuple affiliatedCourses = mock(Tuple.class);
        when(affiliatedCourses.get("email")).thenReturn(user.getEmail());
        when(affiliatedCourses.get("course_id")).thenReturn(course.getId());

        when(affiliateCourseRepo.findByUuidAndCourseId(anyString(), anyLong())).thenReturn(affiliatedCourses);
        when(instructorAffiliateRepo.findByAffiliateId(any(), any())).thenReturn(mock(Tuple.class));
        when(affiliateCourseService.saveStudentOnboardingDetails(any(), any())).thenReturn(Boolean.TRUE);

        Message<String> result = service.chargePayment(chargePayment, anyString());

        assertEquals(HttpStatus.OK.value(), result.getStatus());
        verify(selfAffiliatePayoutService).executePayout(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Test charge payment with invalid course type")
    void testChargePayment_InvalidCourseType() throws EntityNotFoundException, IOException {
        var chargePayment = new ChargePayment("opaque-token", 29L, null, null);
        var course = CourseTestData.courseData();
        course.setId(29L);
        course.setCourseType(CourseType.FREE_COURSE);

        when(userService.findByEmail(anyString())).thenReturn(new User());
        when(courseService.findById(anyLong())).thenReturn(course);

        assertThrows(BadRequestException.class,
                () -> service.chargePayment(chargePayment, anyString()));
    }

    @Test
    @DisplayName("Test charge payment with transaction error")
    void testChargePayment_TransactionError() throws IOException, EntityNotFoundException, InternalServerException, BadRequestException {
        var chargePayment = new ChargePayment("opaque-token", 29L, null, null);
        var user = UserTestData.userData();
        var course = CourseTestData.courseData();
        course.setId(29L);
        course.setCourseType(CourseType.PREMIUM_COURSE);
        course.setPrice(100.0);

        when(userService.findByEmail(anyString())).thenReturn(user);
        when(courseService.findById(anyLong())).thenReturn(course);

        TransactionResponse.Errors.Error error = new TransactionResponse.Errors.Error();
        error.setErrorCode("E00027");
        error.setErrorText("The transaction was unsuccessful.");

        TransactionResponse.Errors errors = new TransactionResponse.Errors();
        errors.setError(List.of(error));

        var transaction = new TransactionResponse();
        transaction.setErrors(errors);

        var message = new com.vinncorp.fast_learner.response.message.Message();
        message.setCode("I00011");
        message.setText("Card declined");

        var messageType = new MessagesType();
        messageType.setMessage(List.of(message));
        messageType.setResultCode(MessageTypeEnum.OK);

        var createTransactionResponse = new CreateTransactionResponse();
        createTransactionResponse.setMessages(messageType);
        createTransactionResponse.setTransactionResponse(transaction);

        when(restClient.makeRequest(anyString(), any(), any(), any())).thenReturn(createTransactionResponse);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.chargePayment(chargePayment, anyString())
        );

        assertEquals(exception.getMessage(), "Transaction Error: " + error.getErrorText());
    }
}