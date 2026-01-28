package com.vinncorp.fast_learner.mock.payout;
import com.vinncorp.fast_learner.dtos.payout.PaidUser;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.payout.InstructorSales;
import com.vinncorp.fast_learner.repositories.user.UserCourseProgressRepository;
import com.vinncorp.fast_learner.services.payout.IPayoutWatchTimeService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.payout.IInstructorSalesService;
import com.vinncorp.fast_learner.services.payout.PayoutCalculationService;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PayoutCalculationServiceTest {

    @Mock
    private IPayoutWatchTimeService payoutWatchTimeService;

    @Mock
    private IInstructorSalesService instructorSalesService;

    @Mock
    private ISubscribedUserService subscribedUserService;

    @Mock
    private UserCourseProgressRepository userCourseProgressRepo;

    @InjectMocks
    private PayoutCalculationService payoutCalculationService;

    @Captor
    ArgumentCaptor<List<InstructorSales>> instructorSalesCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        payoutCalculationService = new PayoutCalculationService(payoutWatchTimeService,
                instructorSalesService, subscribedUserService, userCourseProgressRepo);
    }

    @Test
    @DisplayName("Calculating payout instructor wise.")
    void testCalculateAllInstructorsPayout() throws InternalServerException {
        List<PaidUser> paidUsers = Arrays.asList(
                new PaidUser(1L, 9),
                new PaidUser(2L, 9)
        );

        when(subscribedUserService.fetchAllPaidSubscriptionAfterTrialPeriod()).thenReturn(paidUsers);

        List<Tuple> instructorSalesTuples1 = new ArrayList<>();
        List<Tuple> instructorSalesTuples2 = new ArrayList<>();

        Tuple tuple1 = mock(Tuple.class);
        Tuple tuple2 = mock(Tuple.class);
        Tuple tuple3 = mock(Tuple.class);
        when(tuple1.get("instructor_id")).thenReturn(1L);
        when(tuple1.get("relative_seek_time")).thenReturn(50.0);
        when(tuple1.get("paypal_email")).thenReturn("instructor1@example.com");

        when(tuple2.get("instructor_id")).thenReturn(2L);
        when(tuple2.get("relative_seek_time")).thenReturn(75.0);
        when(tuple2.get("paypal_email")).thenReturn("instructor2@example.com");

        when(tuple3.get("instructor_id")).thenReturn(1L);
        when(tuple3.get("relative_seek_time")).thenReturn(25.0);
        when(tuple3.get("paypal_email")).thenReturn("instructor1@example.com");

        instructorSalesTuples1.add(tuple1);
        instructorSalesTuples2.add(tuple2);
        instructorSalesTuples2.add(tuple3);

        when(userCourseProgressRepo.fetchAllInstructorSalesByStudentId(1L, 9.0)).thenReturn(instructorSalesTuples1);
        when(userCourseProgressRepo.fetchAllInstructorSalesByStudentId(2L, 9.0)).thenReturn(instructorSalesTuples2);

        payoutCalculationService.calculateAllInstructorsPayout();

        verify(instructorSalesService, times(1)).saveAll(instructorSalesCaptor.capture());

        List<InstructorSales> capturedSales = instructorSalesCaptor.getValue();
        assertEquals(2, capturedSales.size());

        InstructorSales instructorSales1 = capturedSales.stream().filter(s -> s.getInstructorId() == 1L).findFirst().orElse(null);
        InstructorSales instructorSales2 = capturedSales.stream().filter(s -> s.getInstructorId() == 2L).findFirst().orElse(null);

        assertNotNull(instructorSales1);
        assertEquals(75.0, instructorSales1.getTotalSales());
        assertEquals(null, instructorSales1.getStripeAccountId());
        assertEquals(PayoutStatus.PENDING, instructorSales1.getStatus());

        assertNotNull(instructorSales2);
        assertEquals(75.0, instructorSales2.getTotalSales());
        assertEquals(null, instructorSales2.getStripeAccountId());
        assertEquals(PayoutStatus.PENDING, instructorSales2.getStatus());
    }

    @Test
    @DisplayName("Processing user when no instructor found.")
    void testProcessUserWithNoInstructors() {
        PaidUser user = new PaidUser(1L, 100.0);
        Date creationDate = new Date();

        when(userCourseProgressRepo.fetchAllInstructorSalesByStudentId(user.getUserId(), user.getSubscriptionFee())).thenReturn(Collections.emptyList());

        payoutCalculationService.processUser(user, creationDate);

        verify(userCourseProgressRepo, times(1)).fetchAllInstructorSalesByStudentId(user.getUserId(), user.getSubscriptionFee());
        assertTrue(CollectionUtils.isEmpty(payoutCalculationService.instructorSales));
    }

    @Test
    @DisplayName("Populating the calculated data in internal map.")
    void testSaveInMap() {
        Date creationDate = new Date();
        Tuple tuple = mock(Tuple.class);

        when(tuple.get("instructor_id")).thenReturn(1L);
        when(tuple.get("relative_seek_time")).thenReturn(50.0);
        when(tuple.get("paypal_email")).thenReturn("instructor1@example.com");

        payoutCalculationService.saveInMap(tuple, creationDate);

        InstructorSales instructorSales = payoutCalculationService.instructorSales.get(1L);
        assertNotNull(instructorSales);
        assertEquals(50.0, instructorSales.getTotalSales());
        assertEquals(null, instructorSales.getStripeAccountId());
        assertEquals(PayoutStatus.PENDING, instructorSales.getStatus());
    }

    @Test
    @DisplayName("Populating the process data into database.")
    void testPopulateIntoDb() throws InternalServerException {
        InstructorSales instructorSales1 = InstructorSales.builder()
                .creationDate(new Date())
                .instructorId(1L)
                .stripeAccountId("instructor1@example.com")
                .status(PayoutStatus.PENDING)
                .totalSales(100.0)
                .build();

        InstructorSales instructorSales2 = InstructorSales.builder()
                .creationDate(new Date())
                .instructorId(2L)
                .stripeAccountId("instructor2@example.com")
                .status(PayoutStatus.PENDING)
                .totalSales(200.0)
                .build();

        payoutCalculationService.instructorSales.put(1L, instructorSales1);
        payoutCalculationService.instructorSales.put(2L, instructorSales2);

        payoutCalculationService.populateIntoDb();

        verify(instructorSalesService, times(1)).saveAll(instructorSalesCaptor.capture());
        List<List<InstructorSales>> allCapturedSales = instructorSalesCaptor.getAllValues();

        assertEquals(1, allCapturedSales.size());
        assertTrue(allCapturedSales.get(0).contains(instructorSales1));
        assertTrue(allCapturedSales.get(0).contains(instructorSales2));
    }
}
