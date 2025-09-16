package com.vinncorp.fast_learner.services.subscription.subscribed_user_profile;


import com.vinncorp.fast_learner.config.GenericRestClient;
import com.vinncorp.fast_learner.response.customer_profile.GetCustomerProfileResponse;
import com.vinncorp.fast_learner.response.subscription.CreateCustomerPaymentProfileResponse;
import com.vinncorp.fast_learner.response.subscription.GetSubscriptionResponse;
import com.vinncorp.fast_learner.services.payment.additional_service.IPaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailRequest;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailResponse;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityAlreadyExistException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.SubscribedUserProfile;
import com.vinncorp.fast_learner.repositories.subscription.SubscribedUserProfileRepository;
import com.vinncorp.fast_learner.services.payment.payment_profile.IPaymentProfileService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.response.subscription.GetCustomerPaymentProfileResponse;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.LogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscribedUserProfileService implements ISubscribedUserProfileService {

    private final SubscribedUserProfileRepository repo;
    private final ISubscribedUserService subscribedUserService;
    private final IPaymentProfileService paymentProfileService;
    private final IPaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;
    private final GenericRestClient restClient;

    @Override
    public Message<PaymentProfileDetailResponse> getDefaultSubscribedUserProfile(String email) throws EntityNotFoundException, InternalServerException {
        log.info("Fetching default subscribed user profile");
        SubscribedUser subscribedUser = subscribedUserService.findByUser(email.toLowerCase());

        SubscribedUserProfile subscribedUserProfile= repo.findByIsDefaultAndSubscribedUser(true,subscribedUser).orElseThrow(() -> {
            log.error("ERROR: No default profile found");
            return  new EntityNotFoundException("No profile is set to default");
        });

        if(Objects.isNull(subscribedUser.getCustomerProfileId()) || Objects.isNull(subscribedUserProfile.getCustomerPaymentProfileId()))
            throw new EntityNotFoundException("Custome profile id or customer payment profile id not found.");

        GetCustomerPaymentProfileResponse profileResponse = paymentProfileService.getCustomerPaymentProfile(subscribedUserProfile.getSubscribedUser().getCustomerProfileId(), subscribedUserProfile.getCustomerPaymentProfileId());

        return new Message<PaymentProfileDetailResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Successfully get the default payment profile.")
                .setData(PaymentProfileDetailResponse.mapToPaymentProfileResponse(subscribedUserProfile,profileResponse));

    }

    @Transactional(rollbackFor = InternalServerException.class)
    @Override
    public Message<String> updatePaymentProfileDefaultStatus(Long id, Boolean status) throws EntityNotFoundException, InternalServerException {
        log.info("Update payment profile default status for id "+id);

        SubscribedUserProfile subscribedUserProfile = repo.findById(id).orElseThrow(() -> {
            log.error("Subscribed user with id "+ id + LogMessage.NOT_EXIST);
          return new EntityNotFoundException("Subscribed user with id "+ id + LogMessage.NOT_EXIST);
        });


        if(status && !subscribedUserProfile.getIsDefault()){
            markAllProfileSetAsNotDefaultById(subscribedUserProfile.getSubscribedUser().getId());
        }

        subscribedUserProfile.setIsDefault(status);

        try {
            log.info("Successfully save profile in the database with id "+id);
            repo.save(subscribedUserProfile);
        }
        catch (Exception e){
            log.error("ERROR:"+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR+" for id "+id);
            throw new InternalServerException("Subscribed user profile"+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }


        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Successfully update the payment profile status.")
                .setData("Payment Profile default status updated successfully!");
    }

    @Transactional(rollbackFor = InternalServerException.class)
    @Override
    public void saveSubscribedUserProfile(String customerProfileId, String customerPaymentProfileId, String email, Boolean isDefault) throws InternalServerException, EntityNotFoundException {
        log.info("Creating subscribed user profile by user "+email);

        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);

        Optional<SubscribedUserProfile> subscribedUserProfile = repo.findByCustomerPaymentProfileId(customerPaymentProfileId);

        if(subscribedUser.getCustomerProfileId()==null){
            subscribedUser.setCustomerProfileId(customerProfileId);
            subscribedUserService.save(subscribedUser);
        }
        if(subscribedUserProfile.isPresent()){
            if(isDefault && !subscribedUserProfile.get().getIsDefault()){
                markAllProfileSetAsNotDefaultById(subscribedUser.getId());
            }
            save(subscribedUserProfile.get().getId(),subscribedUserProfile.get().getCustomerPaymentProfileId(),subscribedUser,isDefault);
        }
        else {
            if(isDefault){
                markAllProfileSetAsNotDefaultById(subscribedUser.getId());
            }
            save(null,customerPaymentProfileId,subscribedUser,isDefault);
        }

    }

    private void save(Long id,String customerPaymentProfileId, SubscribedUser user,Boolean isDefault) throws InternalServerException {
        SubscribedUserProfile userProfile = SubscribedUserProfile.builder()
                .id(id)
                .customerPaymentId(user.getCustomerProfileId())
                .customerPaymentProfileId(customerPaymentProfileId)
                .subscribedUser(user)
                .isDefault(isDefault)
                .build();
        try{
            log.info("Successfully save profile in the database with id "+customerPaymentProfileId);
            repo.save(userProfile);
        }
        catch (Exception e){
            log.error("ERROR:"+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR+" for id "+customerPaymentProfileId);
            throw new InternalServerException("Subscribed user profile"+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public SubscribedUserProfile save(SubscribedUserProfile customerPaymentProfile) throws InternalServerException {
        log.info("Saving subscribed user profile...");
        try{
            log.info("Successfully save profile in the database with id "+customerPaymentProfile.getId());
            return repo.save(customerPaymentProfile);
        }
        catch (Exception e){
            throw new InternalServerException("Subscribed user profile "+InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public SubscribedUserProfile getSubscribedUserProfileById(Long id) throws  EntityNotFoundException {
        log.info("Get subscribed user profile by id "+id);
        return repo.findById(id).orElseThrow(() -> {
            log.error("Subscribed user profile with id "+ id + LogMessage.NOT_EXIST);
            return new EntityNotFoundException("Subscribed user profile with id "+ id + LogMessage.NOT_EXIST);
        });
    }

    @Override
    public Message<PaymentProfileDetailResponse> getPaymentProfileById(Long id) throws EntityNotFoundException, InternalServerException{
        log.info("Fetching the payment profile by id "+id);

        SubscribedUserProfile subscribedUserProfile = getSubscribedUserProfileById(id);

        GetCustomerPaymentProfileResponse profileResponse = paymentProfileService.getCustomerPaymentProfile(subscribedUserProfile.getSubscribedUser().getCustomerProfileId(), subscribedUserProfile.getCustomerPaymentProfileId());

        return new Message<PaymentProfileDetailResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Successfully get the payment profile.")
                .setData(PaymentProfileDetailResponse.mapToPaymentProfileResponse(subscribedUserProfile,profileResponse));

    }

    @Override
    public void markAllProfileSetAsNotDefaultById(Long id) throws InternalServerException{
        log.info("Set is_default to false for all the profiles by subscribed user id "+id);
        try {
            repo.markAllAsNotDefaultById(id);
        }
        catch (Exception e){
            log.error("ERROR: Update profile default parameter"+e.getLocalizedMessage());
            throw new InternalServerException("Subscribed User Profile: Unable to update user profiles "+e.getLocalizedMessage());
        }
    }

    @Override
    public Message<List<PaymentProfileDetailResponse>> getAllPaymentProfiles(String email) throws EntityNotFoundException,InternalServerException{
         log.info("Fetching all the payment profiles of user "+email.toLowerCase());

        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        if(Objects.isNull(subscribedUser.getCustomerProfileId()))
            throw new EntityNotFoundException("No customer profile id found for the user.");

        List<SubscribedUserProfile> subscribedUserProfiles = repo.findAllBySubscribedUserOrderByIdDesc(subscribedUser);

        if(subscribedUserProfiles.isEmpty()){
            log.error("ERROR: No profiles found for user "+email);
            throw new EntityNotFoundException("No profile found for user "+ email);
        }

        GetCustomerProfileResponse profileResponseList = paymentProfileService.getCustomerPaymentProfileList(subscribedUser.getCustomerProfileId());


        return  new Message<List<PaymentProfileDetailResponse>>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Successfully get all the profiles.")
                .setData(PaymentProfileDetailResponse.mapToPaymentProfileResponse(profileResponseList.getProfile().getPaymentProfiles(),subscribedUserProfiles));
    }

    @Transactional(rollbackFor = InternalServerException.class)
    @Override
    public Message<String> addUpdateCustomerProfile(String email, PaymentProfileDetailRequest profileDetailRequest) throws InternalServerException, EntityNotFoundException, EntityAlreadyExistException, BadRequestException {
        log.info("Adding/Updating customer payment profile by user "+ email);

        if(Objects.isNull(profileDetailRequest.getId())){
            SubscribedUser user = subscribedUserService.findByUser(email);

            if(Objects.isNull(user.getCustomerProfileId())){
                log.error("ERROR: Customer profile id not exist of user "+user.getUser().getEmail());
                throw new BadRequestException("Customer profile id not exist of user "+user.getUser().getEmail());
            }

            CreateCustomerPaymentProfileResponse response = paymentProfileService.createCustomerPaymentProfile(user.getCustomerProfileId(),profileDetailRequest, email);

            if(Objects.equals(response.getMessages().getMessage().get(0).getCode(), LogMessage.DUPLICATE_PROFILE_CODE)) {
                log.error("ERROR: Payment profile: "+ response.getMessages().getMessage().get(0).getText());
                throw new EntityAlreadyExistException("Payment profile: "+response.getMessages().getMessage().get(0).getText());
            }

            // TODO DELETE: Delete below card verification code snippet
            Boolean isCardVerify = paymentAdditionalSubscriptionService.validateCardVerification(
                    response.getCustomerProfileId(),
                    response.getCustomerPaymentProfileId()
            );

            if (!isCardVerify) {
                log.error("Card verification failed: Invalid expiry date or CVV for customerProfileId: {}, paymentProfileId: {}",
                        response.getCustomerProfileId(),
                        response.getCustomerPaymentProfileId());
                throw new BadRequestException("Card verification failed: Invalid expiry date or CVV");
            }

            if(profileDetailRequest.getIsSave()){
                markAllProfileSetAsNotDefaultById(user.getId());
            }

            save(null,response.getCustomerPaymentProfileId(),user,profileDetailRequest.getIsSave());

            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.toString())
                    .setData("Successfully added customer payment profile with id "+response.getCustomerPaymentProfileId())
                    .setMessage("Successfully added customer payment profile.");

        }
        else {
            SubscribedUserProfile subscribedUserProfile = repo.findById(profileDetailRequest.getId()).orElseThrow(()-> {
                log.error("ERROR: Payment profile not exists with this id" +profileDetailRequest.getId());
                return new EntityNotFoundException("Payment profile not exists with this id "+profileDetailRequest.getId());
            });

            paymentProfileService.updateCustomerPaymentProfile(subscribedUserProfile.getSubscribedUser().getCustomerProfileId(),subscribedUserProfile.getCustomerPaymentProfileId(),profileDetailRequest, email);

            if(profileDetailRequest.getIsSave() && !subscribedUserProfile.getIsDefault()){
                markAllProfileSetAsNotDefaultById(subscribedUserProfile.getSubscribedUser().getId());
            }

            save(subscribedUserProfile.getId(), subscribedUserProfile.getCustomerPaymentProfileId(),subscribedUserProfile.getSubscribedUser(),profileDetailRequest.getIsSave());

            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.toString())
                    .setData("Successfully update customer payment profile with id "+subscribedUserProfile.getCustomerPaymentProfileId())
                    .setMessage("Successfully update customer payment profile.");

        }
    }

    @Override
    public Message<String> deleteSubscribedUserProfile(Long id) throws EntityNotFoundException, InternalServerException, BadRequestException {
        log.info("Deleting the subscribed user profile with id "+id);

        SubscribedUserProfile subscribedUserProfile = repo.findById(id).orElseThrow(()-> {
            log.error("ERROR: Payment profile not exists with this id" +id);
            return new EntityNotFoundException("Payment profile not exists with this id "+id);
        });

        deletingPaymentProfileFromPaymentServer(subscribedUserProfile);

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setData("Successfully delete subscribe user profile by "+subscribedUserProfile.getSubscribedUser().getUser().getEmail()+" and id"+id)
                .setMessage("Payment profile delete successfully.");
    }

    public void deletingPaymentProfileFromPaymentServer(SubscribedUserProfile subscribedUserProfile) throws InternalServerException, BadRequestException {
        // Check if the user has not a cancelled subscription and has payment subscription id other than static id i.e "0000000"
        if(Objects.isNull(subscribedUserProfile.getSubscribedUser().getEndDate()) &&
                ( Objects.nonNull(subscribedUserProfile.getSubscribedUser().getPaymentSubscriptionId()) &&
                        !subscribedUserProfile.getSubscribedUser().getPaymentSubscriptionId().equals("0000000"))) {

            GetSubscriptionResponse subscriptionResponse = paymentProfileService.getSubscription(subscribedUserProfile.getSubscribedUser().getPaymentSubscriptionId());

            if (Objects.equals(subscriptionResponse.getSubscription().getProfile().getPaymentProfile().getCustomerPaymentProfileId(), subscribedUserProfile.getCustomerPaymentProfileId())) {
                log.error("ERROR: Deleting the profile with id " + subscriptionResponse.getSubscription().getProfile().getPaymentProfile().getCustomerPaymentProfileId() + " because subscription is active with this payment profile.");
                throw new BadRequestException("You can not delete this profile, because you currently have subscription with this profile.");
            }
        }

        paymentProfileService.deleteCustomerPaymentProfile(
                subscribedUserProfile.getSubscribedUser().getCustomerProfileId(),
                subscribedUserProfile.getCustomerPaymentProfileId(),
                subscribedUserProfile.getSubscribedUser()
                );

        try{
            log.info("Successfully delete profile with id "+ subscribedUserProfile.getId());
            repo.deleteById(subscribedUserProfile.getId());
        }
        catch (Exception e){
            log.error("ERROR: Unable to delete profile with id "+ subscribedUserProfile.getId() +" "+e.getMessage());
            throw new InternalServerException("Unable to delete profile with id "+ subscribedUserProfile.getId());
        }
    }

    @Override
    public void deleteBySubscribedUserId(SubscribedUser subscribedUser) {
        log.info("Deleting all subscribedUsers payment profile.");
        List<SubscribedUserProfile> subscribedUserProfiles = repo.findAllBySubscribedUserOrderByIdDesc(subscribedUser);
        if(!CollectionUtils.isEmpty(subscribedUserProfiles)) {
            subscribedUserProfiles.forEach(e -> {
                try {
                    deletingPaymentProfileFromPaymentServer(e);
                } catch (InternalServerException ex) {
                    log.warn("Deleting payment profile internal server error msg: " + ex.getLocalizedMessage());
                } catch (BadRequestException ex) {
                    log.warn("Deleting payment profile bad request error msg: "+ ex.getLocalizedMessage());
                }
            });
        }
        repo.deleteAllBySubscribedUserId(subscribedUser.getId());
        log.info("Deleted all payment profile associated with the customer profile: "+ subscribedUser.getCustomerProfileId());
    }

    @Override
    public void deleteById(Long subscriptionId) {
        log.info("Deleting the subscribed user profile data.");
        repo.deleteById(subscriptionId);
    }

    @Override
    public SubscribedUserProfile getDefaultBySubscribedUserId(Long subscribedUserId, boolean defaultStatus) {
        log.info("Fetching default customer payment profile...");
        return repo.findByIsDefaultAndSubscribedUserId(defaultStatus, subscribedUserId);
    }
}
