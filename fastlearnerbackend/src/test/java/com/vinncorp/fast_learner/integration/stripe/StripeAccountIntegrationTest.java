package com.vinncorp.fast_learner.integration.stripe;

import com.stripe.exception.StripeException;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.services.stripe.IStripeAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class StripeAccountIntegrationTest {

    @Autowired
    private IStripeAccountService service;

    @Test
    public void testCreateAccountLink_whenProvidedValidData() throws StripeException, InternalServerException, EntityNotFoundException {
        var link = service.createAccountLink("");

        assertNotNull(link);
        assertNotNull(link.getData());
    }

    @Test
    public void testFetchAccountDetailById_whenProvidedValidData() throws EntityNotFoundException, InternalServerException {
        var account = service.fetchAccountDetailByEmail("qasim@vinncorp.com");

        assertNotNull(account);
        assertNotNull(account.getData());
    }

    @Test
    public void testDeleteAccountByEmail_whenProvidedValidData() throws EntityNotFoundException, InternalServerException, BadRequestException {
        var account = service.deleteAccountByEmail("instructor1@mailinator.com");

        assertNotNull(account);
    }

    @Test
    public void testAvailableBalance_whenProvidedValidData() throws StripeException {
        var availableBalance = service.availableBalance("acct_1Q3F7xHKjIZPRjoB");

        assertNotEquals(availableBalance, 0.00);
    }
}
