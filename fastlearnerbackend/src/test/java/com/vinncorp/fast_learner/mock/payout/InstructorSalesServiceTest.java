package com.vinncorp.fast_learner.mock.payout;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.payout.InstructorSales;
import com.vinncorp.fast_learner.repositories.payout.InstructorSalesRepository;
import com.vinncorp.fast_learner.services.payout.InstructorSalesService;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstructorSalesServiceTest {

    @Mock
    private InstructorSalesRepository repo;

    @InjectMocks
    private InstructorSalesService instructorSalesService;

    private List<InstructorSales> instructorSalesList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        instructorSalesList = List.of(new InstructorSales(/* initialize with appropriate data */));
    }

    @Test
    @DisplayName("Test saveAll - Success")
    void testSaveAll_Success() throws InternalServerException {
        when(repo.saveAll(instructorSalesList)).thenReturn(Arrays.asList());

        instructorSalesService.saveAll(instructorSalesList);

        verify(repo, times(1)).saveAll(instructorSalesList);
    }

    @Test
    @DisplayName("Test saveAll - Exception handling")
    void testSaveAll_Exception() {
        doThrow(new RuntimeException("Database error")).when(repo).saveAll(instructorSalesList);

        assertThrows(InternalServerException.class, () -> instructorSalesService.saveAll(instructorSalesList));

        verify(repo, times(1)).saveAll(instructorSalesList);
    }

    @Test
    @DisplayName("Test findAllForPayoutProcess - Success")
    void testFindAllForPayoutProcess_Success() throws EntityNotFoundException {
        when(repo.findAllForPayoutProcess(PayoutStatus.PENDING)).thenReturn(instructorSalesList);

        List<InstructorSales> result = instructorSalesService.findAllForPayoutProcess(PayoutStatus.PENDING);

        assertEquals(instructorSalesList, result);
        verify(repo, times(1)).findAllForPayoutProcess(PayoutStatus.PENDING);
    }

    @Test
    @DisplayName("Test findAllForPayoutProcess - No sales found")
    void testFindAllForPayoutProcess_NoSalesFound() {
        when(repo.findAllForPayoutProcess(PayoutStatus.PENDING)).thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class, () -> instructorSalesService.findAllForPayoutProcess(PayoutStatus.PENDING));

        verify(repo, times(1)).findAllForPayoutProcess(PayoutStatus.PENDING);
    }

    @Test
    @DisplayName("Test findAllProceededPayouts - Success")
    void testFindAllProceededPayouts_Success() throws EntityNotFoundException {
        when(repo.findAllForPayoutProcess(PayoutStatus.PENDING)).thenReturn(instructorSalesList);

        List<InstructorSales> result = instructorSalesService.findAllForPayoutProcess(PayoutStatus.PENDING);

        assertEquals(instructorSalesList, result);
        verify(repo, times(1)).findAllForPayoutProcess(PayoutStatus.PENDING);
    }

    @Test
    @DisplayName("Test findAllProceededPayouts - No sales found")
    void testFindAllProceededPayouts_NoSalesFound() {
        when(repo.findAllForPayoutProcess(PayoutStatus.PENDING)).thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class, () -> instructorSalesService.findAllForPayoutProcess(PayoutStatus.PENDING));

        verify(repo, times(1)).findAllForPayoutProcess(PayoutStatus.PENDING);
    }

    @Test
    @DisplayName("Test findUnprocessedInstructor - Success")
    void testFindUnprocessedInstructor_Success() throws EntityNotFoundException {
        InstructorSales instructorSales = new InstructorSales();
        when(repo.findUnprocessedInstructorsOfCurrentMonth()).thenReturn(Optional.of(instructorSales));

        InstructorSales result = instructorSalesService.findUnprocessedInstructor();

        assertEquals(instructorSales, result);
        verify(repo, times(1)).findUnprocessedInstructorsOfCurrentMonth();
    }

    @Test
    @DisplayName("Test findUnprocessedInstructor - No unprocessed instructor found")
    void testFindUnprocessedInstructor_NotFound() {
        when(repo.findUnprocessedInstructorsOfCurrentMonth()).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> instructorSalesService.findUnprocessedInstructor());

        verify(repo, times(1)).findUnprocessedInstructorsOfCurrentMonth();
    }

    @Test
    @DisplayName("Test updateStatusOfPayout - Success")
    void testUpdateStatusOfPayout_Success() {
        String batchId = "batch123";

        when(repo.updatePendingToProcessedByBatchId(batchId)).thenReturn(0);

        instructorSalesService.updateStatusOfPayout(batchId);

        verify(repo, times(1)).updatePendingToProcessedByBatchId(batchId);
    }

    @Test
    @DisplayName("Test fetchMonthlyOrYearlySales - Fetch Yearly Sales")
    void testFetchMonthlyOrYearlySales_Yearly() {
        String period = "YEARLY";
        Long instructorId = 1L;
        Double expectedSales = 5000.0;

        when(repo.fetchSalesByYearly(period, instructorId)).thenReturn(expectedSales);

        Double result = instructorSalesService.fetchMonthlyOrYearlySales(period, instructorId);

        assertEquals(expectedSales, result);
        verify(repo, times(1)).fetchSalesByYearly(period, instructorId);
    }

    @Test
    @DisplayName("Test fetchMonthlyOrYearlySales - Fetch Monthly Sales")
    void testFetchMonthlyOrYearlySales_Monthly() {
        String period = "MONTHLY";
        Long instructorId = 1L;
        Double expectedSales = 1500.0;

        when(repo.fetchSalesByMonthly(period, instructorId)).thenReturn(expectedSales);

        Double result = instructorSalesService.fetchMonthlyOrYearlySales(period, instructorId);

        assertEquals(expectedSales, result);
        verify(repo, times(1)).fetchSalesByMonthly(period, instructorId);
    }

    @Test
    @DisplayName("Test fetchMonthlyOrYearlySales - Unknown Period")
    void testFetchMonthlyOrYearlySales_UnknownPeriod() {
        String period = "WEEKLY"; // An unknown period
        Long instructorId = 1L;

        Double result = instructorSalesService.fetchMonthlyOrYearlySales(period, instructorId);

        assertEquals(0.0, result);
        verify(repo, never()).fetchSalesByYearly(anyString(), anyLong());
        verify(repo, never()).fetchSalesByMonthly(anyString(), anyLong());
    }
}