package com.vinncorp.fast_learner.services.payment;

import com.vinncorp.fast_learner.dtos.payment.BillingHistoryRequest;
import com.vinncorp.fast_learner.dtos.payment.BillingHistoryResponse;
import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IPaymentSubscriptionService {

    @Transactional(rollbackFor = {InternalServerException.class, BadRequestException.class})
    Message<String> create(SubscriptionRequest subscriptionRequest, String email)
            throws BadRequestException, EntityNotFoundException, InternalServerException;

    Message<List<BillingHistoryResponse>> getBillingHistory(BillingHistoryRequest request,String email) throws EntityNotFoundException, InternalServerException, BadRequestException;

    Message<String> updateSubscription(Long subscribedUserProfileId, String email) throws EntityNotFoundException, BadRequestException, InternalServerException;

    Message<String> freeSignUpSubscription(Long subscriptionId, String name) throws EntityNotFoundException, InternalServerException;
}
