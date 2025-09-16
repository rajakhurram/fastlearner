package com.vinncorp.fast_learner.services.subscription.subscribed_user_profile;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityAlreadyExistException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.SubscribedUserProfile;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailRequest;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailResponse;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ISubscribedUserProfileService {

    Message<PaymentProfileDetailResponse> getDefaultSubscribedUserProfile(String email) throws EntityNotFoundException, InternalServerException;

    Message<String> updatePaymentProfileDefaultStatus(Long id, Boolean status) throws EntityNotFoundException, InternalServerException;

    @Transactional(rollbackFor = InternalServerException.class)
    void saveSubscribedUserProfile(String customerProfileId, String customerPaymentProfileId, String email, Boolean isDefault) throws InternalServerException, EntityNotFoundException;

    SubscribedUserProfile save(SubscribedUserProfile customerPaymentProfile) throws InternalServerException;

    SubscribedUserProfile getSubscribedUserProfileById(Long id) throws  EntityNotFoundException;

    Message<PaymentProfileDetailResponse> getPaymentProfileById(Long id) throws EntityNotFoundException, InternalServerException;

    void markAllProfileSetAsNotDefaultById(Long id) throws InternalServerException;

    Message<List<PaymentProfileDetailResponse>> getAllPaymentProfiles(String email) throws EntityNotFoundException,InternalServerException;

    Message<String> addUpdateCustomerProfile(String email, PaymentProfileDetailRequest profileDetailRequest) throws InternalServerException, EntityNotFoundException, EntityAlreadyExistException, BadRequestException;

    Message<String> deleteSubscribedUserProfile(Long id) throws EntityNotFoundException, InternalServerException, BadRequestException;

    void deleteBySubscribedUserId(SubscribedUser subscribedUser);

    void deleteById(Long subscriptionId);

    SubscribedUserProfile getDefaultBySubscribedUserId(Long subscribedUserId, boolean defaultStatus);
}
