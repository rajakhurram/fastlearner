package com.vinncorp.fast_learner.services.payment.payment_profile;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.CustomerAndPaymentId;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailRequest;
import com.vinncorp.fast_learner.response.customer_profile.GetCustomerProfileResponse;
import com.vinncorp.fast_learner.response.subscription.CreateCustomerPaymentProfileResponse;
import com.vinncorp.fast_learner.response.subscription.GetSubscriptionResponse;
import com.vinncorp.fast_learner.response.subscription.GetTransactionDetailsResponse;
import com.vinncorp.fast_learner.response.subscription.GetCustomerPaymentProfileResponse;

public interface IPaymentProfileService {
    CreateCustomerPaymentProfileResponse createCustomerPaymentProfile(String customerProfileId, PaymentProfileDetailRequest detail, String email) throws InternalServerException;

    void updateCustomerPaymentProfile(String customerProfileId, String customerPaymentProfileId, PaymentProfileDetailRequest detail, String email) throws InternalServerException;

    GetCustomerPaymentProfileResponse getCustomerPaymentProfile(String customerProfileId, String customerPaymentProfileId) throws InternalServerException;

    GetCustomerProfileResponse getCustomerPaymentProfileList(String customerProfileId) throws  InternalServerException;

    void deleteCustomerPaymentProfile(String customerProfileId, String customerPaymentProfileId, SubscribedUser subscribedUser) throws InternalServerException;

    GetTransactionDetailsResponse getTransactionDetail(String transactionId) throws  InternalServerException;

    GetSubscriptionResponse getSubscription(String subscriptionId) throws  InternalServerException;

    CustomerAndPaymentId createCustomerProfile(String email, SubscriptionRequest requestDTO) throws InternalServerException;

    void deleteCustomerProfileById(String profileId) throws BadRequestException;
}
