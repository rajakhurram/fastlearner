package com.vinncorp.fast_learner.mock.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.Transfer;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.payout.InstructorSales;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.payout.IInstructorSalesService;
import com.vinncorp.fast_learner.services.stripe.IPaymentWithdrawalHistoryService;
import com.vinncorp.fast_learner.services.stripe.StripeService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class StripeServiceMockTest {

    // Use spy to partially mock the StripeService
    private StripeService stripeService;

    @Mock
    private IInstructorSalesService instructorSalesService;

    @Mock
    private IPaymentWithdrawalHistoryService paymentWithdrawalHistoryService;

    @Mock
    private IUserService userService;

    @Mock
    private Transfer transfer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        StripeService realStripeService = new StripeService(null, paymentWithdrawalHistoryService, instructorSalesService, userService);
        stripeService = spy(realStripeService);
    }

    // Checking the push
    @Test
    void testTransferFundsToMultipleConnectedAccounts_successful() throws StripeException, EntityNotFoundException {
        // Arrange
        InstructorSales sale = new InstructorSales();
        sale.setStripeAccountId("acct_test");
        sale.setTotalSales(1000.0); // $1000
        sale.setStatus(PayoutStatus.PENDING);

        when(instructorSalesService.findAllForPayoutProcess(PayoutStatus.PENDING))
                .thenReturn(List.of(sale));

        // Mock the sendFundsToConnectedAccount method in the spy
        doReturn(transfer).when(stripeService).sendFundsToConnectedAccount("acct_test", 100000);

        // Act
        stripeService.transferFundsToMultipleConnectedAccounts();

        // Assert
        verify(instructorSalesService, times(1)).save(sale);
        assertEquals(PayoutStatus.PROCESSED, sale.getStatus());
    }

    @Test
    void testTransferFundsToMultipleConnectedAccounts_noPayouts() throws EntityNotFoundException {
        // Arrange
        when(instructorSalesService.findAllForPayoutProcess(PayoutStatus.PENDING))
                .thenReturn(Collections.emptyList());

        // Act
        stripeService.transferFundsToMultipleConnectedAccounts();

        // Assert
        verify(instructorSalesService, times(0)).save(any());
    }

    @Test
    void testSendPayoutToExternalAccount_noStripeAccount() throws EntityNotFoundException {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");

        when(userService.findByEmail("test@example.com")).thenReturn(user);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            stripeService.sendPayoutToExternalAccount("BankName", "test@example.com", 1000.0);
        });
    }
}