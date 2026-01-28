package com.vinncorp.fast_learner.services.authorize_net.additional_service;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.dtos.authorize_net.SubscriptionRequest;
import com.vinncorp.fast_learner.dtos.authorize_net.payment_profile.CustomerAndPaymentId;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.response.subscription.CreateSubscriptionResponse;
import com.vinncorp.fast_learner.response.subscription.GetSubscriptionResponse;
import com.vinncorp.fast_learner.response.subscription.TransactionSummaryType;

import java.util.Date;
import java.util.List;

public interface IAuthNetAdditionalSubscriptionService {

    CreateSubscriptionResponse createCouponBased(Subscription subscription, SubscriptionRequest requestDTO,
                                                    String customerProfileId, String customerPaymentProfileId,
                                                    String email, Long userId, Date trialEndDate,
                                                    TransactionHistory transactionHistory, Coupon coupon)
            throws InternalServerException, BadRequestException;

    CreateSubscriptionResponse createFirstTime(Subscription subscription, SubscriptionRequest requestDTO,
                                                  String customerProfileId, String customerPaymentProfileId,
                                                  String email, Long userId, Date trialEndDate,
                                                  TransactionHistory transactionHistory, Coupon coupon)
            throws InternalServerException, BadRequestException;

    void cancelAuthorizeNetSubscription(String subscriptionId) throws InternalServerException;

    GetSubscriptionResponse getSubscriptionById(String subscriptionId) throws InternalServerException;

    CustomerAndPaymentId updateAuthorizeNetSubscription(String email, String subscriptionId, String customerProfileId, String customerPaymentProfileId) throws InternalServerException;
}
