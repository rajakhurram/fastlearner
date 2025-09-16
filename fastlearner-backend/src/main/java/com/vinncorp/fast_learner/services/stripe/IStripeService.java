package com.vinncorp.fast_learner.services.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.Transfer;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.util.Message;

public interface IStripeService {
    // Transfer funds from your platform account to the connected account
    Transfer sendFundsToConnectedAccount(String connectedAccountId, long amount) throws StripeException;

    // Create a payout from the connected account's balance to their bank or debit card
    Message<String> sendPayoutToExternalAccount(String bankName, String email, double amount) throws EntityNotFoundException;
}
