package com.vinncorp.fast_learner.integration.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Transfer;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.services.stripe.IStripeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class StripeServiceIntegrationTest {

    @Autowired
    private IStripeService stripeService;

    @DisplayName("Test: Simple single payout - when provided valid data.")
    @Test
    public void testSimpleSinglePayout_whenProvidedValidData() throws StripeException {
        Transfer transfer = stripeService.sendFundsToConnectedAccount("acct_1Q4eSHQdUYfkxmV8", 900L);

        assertNotNull(transfer);
        assertNotNull(transfer.getId());
    }

    @DisplayName("Test: Send payout to external bank - when provided valid data.")
    @Test
    public void testSendPayoutToExternalAccount_whenProvidedValidData() throws StripeException, EntityNotFoundException {
        var payout = stripeService.sendPayoutToExternalAccount("Stripe Test Bank","qasim@vinncorp.com", 1L); // qasim@vinncorp.com-> acct_1Q0jN1H8XtWPbaVy

        assertNotNull(payout);
        assertEquals(payout.getStatus(), HttpStatus.OK.value());
    }
}
