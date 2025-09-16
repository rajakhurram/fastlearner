package com.vinncorp.fast_learner.services.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Balance;
import com.stripe.net.RequestOptions;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.response.stripe.AccountDetailResponse;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeAccountService implements IStripeAccountService {

    @Value("${stripe.secret.key}")
    private String SECRET_KEY;

    @Value("${stripe.success.url}")
    private String SUCCESS_URL;

    @Value("${stripe.failure.url}")
    private String FAILURE_URL;

    private final IUserService userService;

    // Method to create the onboarding link for the connected account
    @Override
    public Message<String> createAccountLink(String email) throws StripeException, EntityNotFoundException, InternalServerException {
        log.info("Creating connected account for instructor...");
        Stripe.apiKey = SECRET_KEY;
        User user = userService.findByEmail(email);
        AccountCreateParams accountParam = AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.EXPRESS)  // Use Express for partial control
                .setCountry("US")  // The country of the instructor
                .setEmail(email)  // Instructor's email
                .build();

        Account account = Account.create(accountParam);

        log.info("Account ID: " + account.getId());
        user.setStripeAccountId(account.getId());
        userService.save(user);

        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(account.getId())  // Connected account ID (instructor's account)
                .setRefreshUrl(FAILURE_URL)  // Redirect on failure
                .setReturnUrl(SUCCESS_URL)  // Redirect on success
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();
        var acc = AccountLink.create(params);

        return new Message<String>()
                .setMessage("Successfully connected account redirection link.")
                .setCode(HttpStatus.OK.name())
                .setStatus(HttpStatus.OK.value())
                .setData(acc.getUrl());  // Returns the onboarding link
    }

    // Method to fetch account details by account id
    @Override
    public Message<AccountDetailResponse> fetchAccountDetailByEmail(String email) throws EntityNotFoundException, InternalServerException {
        log.info("Fetching account details of email: " + email);
        Stripe.apiKey = SECRET_KEY;
        User user = userService.findByEmail(email);
        try {
            if(Objects.isNull(user.getStripeAccountId()) || user.getStripeAccountId().isBlank())
                throw new EntityNotFoundException("No stripe account is connected.");

            double amount = availableBalance(user.getStripeAccountId());

            Account account = Account.retrieve(user.getStripeAccountId());
            if(account == null || !account.getPayoutsEnabled()) {
                deleteAccountByEmail(email);
                user.setStripeAccountId(null);
                userService.save(user);
                throw new EntityNotFoundException("Account not properly set please try to reconnect the account with proper attributes.");
            }
            return new Message<AccountDetailResponse>()
                    .setCode(HttpStatus.OK.name())
                    .setStatus(HttpStatus.OK.value())
                    .setMessage("Account details fetched successfully.")
                    .setData(AccountDetailResponse.mappedTo(account, user, amount));
        } catch (StripeException e) {
            log.error("Stripe Account Fetching Error: " + e.getMessage());
        } catch (InternalServerException e) {
            throw new InternalServerException(e.getMessage());
        } catch (BadRequestException e) {
            log.error("Deleting account is not processed because account has some amounts left.");
        }
        throw new EntityNotFoundException("Account details not found.");
    }

    @Override
    public Message<String> deleteAccountByEmail(String email) throws EntityNotFoundException, InternalServerException, BadRequestException {
        log.info("Fetching account details of email: " + email);
        Stripe.apiKey = SECRET_KEY;
        User user = userService.findByEmail(email);
        if(Objects.isNull(user.getStripeAccountId()) || user.getStripeAccountId().isBlank())
            throw new EntityNotFoundException("No stripe account is connected.");


        try {
            if(availableBalance(user.getStripeAccountId()) > 0)
                throw new BadRequestException("Account cannot be detached before withdrawing all money.");

            Account account = Account.retrieve(user.getStripeAccountId());
            account.delete();

            user.setStripeAccountId(null);
            userService.save(user);
            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Successfully removed the account.")
                    .setData("Successfully removed the account.");
        } catch (StripeException e) {
            log.error("Stripe Account Fetching Error: " + e.getMessage());
        } catch (InternalServerException e) {
            throw new InternalServerException(e.getMessage());
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        return new Message<String>()
                .setStatus(HttpStatus.BAD_REQUEST.value())
                .setCode(HttpStatus.BAD_REQUEST.name())
                .setMessage("Account removal issue arise.")
                .setData("Account removal issue arise.");
    }

    // Method to check if a connected account has sufficient balance for payout
    @Override
    public double availableBalance(String connectedAccountId) throws StripeException {
        // Retrieve the balance for the connected account
        Balance balance = Balance.retrieve(RequestOptions.builder().setStripeAccount(connectedAccountId).build());

        // Get available balance in the default currency (assuming USD for this example)
        long availableBalance = balance.getAvailable().stream()
                .filter(bal -> "usd".equalsIgnoreCase(bal.getCurrency())) // Filter by currency
                .mapToLong(Balance.Available::getAmount)  // Get amount in cents
                .sum();
        if(availableBalance <= 0)
            return 0.00;
        return availableBalance/100.0;
    }

}
