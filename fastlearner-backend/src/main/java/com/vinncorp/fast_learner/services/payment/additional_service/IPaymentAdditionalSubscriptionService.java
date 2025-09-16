package com.vinncorp.fast_learner.services.payment.additional_service;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.CustomerAndPaymentId;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.response.subscription.CreateSubscriptionResponse;
import com.vinncorp.fast_learner.response.subscription.GetSubscriptionResponse;
import com.vinncorp.fast_learner.response.subscription.TransactionSummaryType;

import java.util.Date;
import java.util.List;

public interface IPaymentAdditionalSubscriptionService {

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

    //This method is used for card cvv and expiry validation
    Boolean validateCardVerification(String customerProfileId, String customerPaymentProfileId);

    CreateSubscriptionResponse upgradeFromFreePlan(Subscription subscription, SubscriptionRequest requestDTO,
                                                   String customerProfileId, String customerPaymentProfileId,
                                                   String email, Long userId, Date trialEndDate,
                                                   TransactionHistory transactionHistory, Double balanceAmount)
            throws InternalServerException, BadRequestException;

    void cancelPaymentSubscription(String subscriptionId) throws InternalServerException;

    List<TransactionSummaryType> getTransactionPageListByCustomerProfileId(String customerProfileId, String customerPaymentProfileId, int pageNo, int pageSize) throws InternalServerException, EntityNotFoundException;

    GetSubscriptionResponse getSubscriptionById(String subscriptionId) throws InternalServerException;

    CustomerAndPaymentId updatePaymentSubscription(String email, String subscriptionId, String customerProfileId, String customerPaymentProfileId) throws InternalServerException;
}
