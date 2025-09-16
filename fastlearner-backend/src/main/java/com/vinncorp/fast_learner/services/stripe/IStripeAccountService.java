package com.vinncorp.fast_learner.services.stripe;

import com.stripe.exception.StripeException;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.response.stripe.AccountDetailResponse;
import com.vinncorp.fast_learner.util.Message;

public interface IStripeAccountService {

    // Method to create the onboarding link for the connected account
    Message<String> createAccountLink(String email) throws StripeException, EntityNotFoundException, InternalServerException;

    // Method to fetch account details by account id
    Message<AccountDetailResponse> fetchAccountDetailByEmail(String email) throws EntityNotFoundException, InternalServerException;

    Message<String> deleteAccountByEmail(String email) throws EntityNotFoundException, InternalServerException, BadRequestException;

    // Method to check if a connected account has sufficient balance for payout
    double availableBalance(String connectedAccountId) throws StripeException;
}
