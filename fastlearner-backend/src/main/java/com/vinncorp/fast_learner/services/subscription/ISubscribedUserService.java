package com.vinncorp.fast_learner.services.subscription;

import com.vinncorp.fast_learner.dtos.payout.PaidUser;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.response.subscription.CurrentSubscriptionResponse;
import com.vinncorp.fast_learner.util.Message;

import java.util.List;

public interface ISubscribedUserService {
    SubscribedUser save(SubscribedUser subscribedUser) throws InternalServerException;

    Message<String> completeSubscription(String paypalSubscriptionId, String name) throws EntityNotFoundException, InternalServerException;


    Message<String> completePaymentSubscription(String paymentSubscriptionId, String email) throws EntityNotFoundException, InternalServerException;

    Message<String> cancelSubscription(String name) throws EntityNotFoundException, BadRequestException, InternalServerException;

    SubscribedUser findByUser(String email) throws EntityNotFoundException;

    Message<CurrentSubscriptionResponse> getCurrentSubscription(String name) throws EntityNotFoundException;

    SubscribedUser findBySubscribedId(String subscribedId);

    SubscribedUser findByCustomerProfileId(String customerProfileId);

    List<PaidUser>  fetchAllPaidSubscriptionAfterTrialPeriod();

    SubscribedUser fetchByCustomerProfileId(String customerProfileId) throws EntityNotFoundException;

    List<SubscribedUser> findAllSubscribedUserWhichAreCancelled();

    List<SubscribedUser> fetchAllCouponBasedSubscriptions() throws EntityNotFoundException;

}
