package com.vinncorp.fast_learner.mock.payout;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.mock.subscription.SubscriptionTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.payout.PayoutWatchTime;
import com.vinncorp.fast_learner.models.payout.premium_course.PremiumCoursePayoutConfig;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.payout.PayoutWatchTimeRepository;
import com.vinncorp.fast_learner.repositories.payout.premium_course.PremiumCoursePayoutConfigRepository;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.payout.PayoutWatchTimeService;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseType;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import com.vinncorp.fast_learner.util.enums.PayoutType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class PayoutWatchTimeServiceTest {

    @InjectMocks
    private PayoutWatchTimeService service;

    @Mock
    private ICourseService courseService;

    @Mock
    private IUserService userService;

    @Mock
    private ITransactionHistoryService transactionHistoryService;

    @Mock
    private PremiumCoursePayoutConfigRepository payoutConfigRepo;

    @Mock
    private PayoutWatchTimeRepository repo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should successfully save new data")
    void testCreateWatchTime_SuccessfullySavesNewData() throws EntityNotFoundException, BadRequestException, InternalServerException, IOException {
        long watchTime = 3600L; // 1 hour in seconds
        String email = "test@example.com";

        Course course = CourseTestData.courseData();
        course.setId(1L);

        User student = UserTestData.userData();
        Subscription subscription = SubscriptionTestData.standardSubscription();

        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setSubscriptionNextCycle(Date.from(LocalDate.of(2025, 5, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        transactionHistory.setTrialEndDate(Date.from(LocalDate.of(2025, 04, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        transactionHistory.setSubscriptionAmount(20.0);
        transactionHistory.setSubscription(subscription);
        transactionHistory.getSubscription().setId(2L);

        PremiumCoursePayoutConfig payoutConfig = new PremiumCoursePayoutConfig();
        payoutConfig.setPercentageCut(0.45);

        when(courseService.findById(course.getId())).thenReturn(course);
        when(userService.findByEmail(email)).thenReturn(student);
        when(transactionHistoryService.fetchCurrentSubscription(student.getId())).thenReturn(transactionHistory);
        when(payoutConfigRepo.findByPayoutTypeAndIsActiveTrue(PayoutType.DIRECT)).thenReturn(payoutConfig);
        when(repo.findAllByPayoutForCurrentMonthAndPayoutForCurrentYearAndStudentIdAndInstructorId(
                anyInt(), anyInt(), anyLong(), anyLong())).thenReturn(Collections.emptyList());

        PayoutWatchTime payoutWatchTime = new PayoutWatchTime();
        payoutWatchTime.setId(1L);
        payoutWatchTime.setTimeSpend(3000L);
        payoutWatchTime.setStudentId(1L);
        payoutWatchTime.setInstructorId(1L);
        payoutWatchTime.setStripeResponse("stripeResponse");
        payoutWatchTime.setNoOfDays(30);
        payoutWatchTime.setPayoutStatus(PayoutStatus.PENDING);


        when(repo.findAllByStudentIdAndCreatedAt(anyLong(), any())).thenReturn(List.of(payoutWatchTime));

        // Act
        Message<String> response = service.create(course.getId(), watchTime, email);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Successfully saved the watch time and distributed payout.", response.getMessage());

        ArgumentCaptor<PayoutWatchTime> captor = ArgumentCaptor.forClass(PayoutWatchTime.class);
        Mockito.verify(repo, times(2)).save(captor.capture());

        PayoutWatchTime savedWatchTime = captor.getValue();
        assertEquals(watchTime + 3000L, savedWatchTime.getTimeSpend());
        assertEquals(student.getId(), savedWatchTime.getStudentId());
        assertEquals(course.getInstructor().getId(), savedWatchTime.getInstructorId());
        assertEquals(course.getInstructor().getStripeAccountId(), null);
    }

    @Test
    @DisplayName("Should throw BadRequestException if course type is invalid")
    void testCreateWatchTime_InvalidCourseType_ThrowsBadRequestException() throws IOException, EntityNotFoundException {
        long watchTime = 3600L;
        String email = "test@example.com";

        Course course = CourseTestData.courseData();
        course.setId(1L);
        course.setCourseType(CourseType.FREE_COURSE); // Invalid course type

        when(courseService.findById(course.getId())).thenReturn(course);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> service.create(course.getId(), watchTime, email));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if no payout config is found")
    void testCreateWatchTime_NoPayoutConfig_ThrowsEntityNotFoundException() throws EntityNotFoundException, IOException {
        long watchTime = 3600L;
        String email = "test@example.com";

        Course course = CourseTestData.courseData();
        course.setId(1L);

        User student = new User();
        student.setId(20L);

        when(courseService.findById(course.getId())).thenReturn(course);
        when(userService.findByEmail(email)).thenReturn(student);
        when(transactionHistoryService.fetchCurrentSubscription(student.getId()))
                .thenReturn(new TransactionHistory());
        when(payoutConfigRepo.findByPayoutTypeAndIsActiveTrue(PayoutType.DIRECT))
                .thenReturn(null); // No payout config found

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.create(course.getId(), watchTime, email));
    }

    @Test
    @DisplayName("Should calculate the number of days correctly")
    void testCalculateNoOfDays_TrailEndBefore25th_CurrentMonth() {
        // Mock current date and trial end date
        LocalDate currentDate = LocalDate.of(2024, 1, 28); // Current date
        Date trialEndDate = new Date(2024 - 1900, 0, 24); // Trial end date (24th Jan 2024)
        Date subscriptionNextCycle = new Date(2025 - 1900, 1, 28);
        // Call the method under test
        int result = service.calculateNoOfDays(currentDate, trialEndDate, subscriptionNextCycle);

        // Assert the result
        assertEquals(31, result, "The calculated days from the 25th are incorrect.");
    }

    @Test
    @DisplayName("Should calculate the number of days correctly")
    void testCalculateNoOfDays_TrailEndAfter25th_CurrentMonth() {
        LocalDate currentDate = LocalDate.of(2024, 1, 28); // Current date
        Date trialEndDate = new Date(2024 - 1900, 0, 26); // Trial end date (26th Jan 2024)
        Date subscriptionNextCycle = new Date(2025 - 1900, 1, 28);
        // Call the method under test
        int result = service.calculateNoOfDays(currentDate, trialEndDate, subscriptionNextCycle);

        assertEquals(30, result, "The calculated days from the trial end date are incorrect.");
    }

    @Test
    @DisplayName("Should calculate the number of days correctly")
    void testCalculateNoOfDays_TrailEndBefore25th_PreviousMonth() {
        LocalDate currentDate = LocalDate.of(2024, 2, 2); // Current date
        Date trialEndDate = new Date(2024 - 1900, 0, 24); // Trial end date (24th Jan 2024)
        Date subscriptionNextCycle = new Date(2025 - 1900, 1, 28);
        // Call the method under test
        int result = service.calculateNoOfDays(currentDate, trialEndDate, subscriptionNextCycle);

        assertEquals(31, result);
    }

    @Test
    @DisplayName("Should calculate the number of days correctly")
    void testCalculateNoOfDays_TrailEndAfter25th_PreviousMonth() {
        MockitoAnnotations.openMocks(this);

        LocalDate currentDate = LocalDate.of(2024, 2, 2); // Current date
        Date trialEndDate = new Date(2024 - 1900, 0, 26); // Trial end date (26th Jan 2024)
        Date subscriptionNextCycle = new Date(2025 - 1900, 1, 28);
        // Call the method under test
        int result = service.calculateNoOfDays(currentDate, trialEndDate, subscriptionNextCycle);

        assertEquals(30, result);
    }
}
