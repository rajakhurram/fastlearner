package com.vinncorp.fast_learner.dtos.payment.payment_profile;


import com.vinncorp.fast_learner.models.subscription.SubscribedUserProfile;
import com.vinncorp.fast_learner.response.subscription.GetCustomerPaymentProfileResponse;
import com.vinncorp.fast_learner.response.subscription.CustomerPaymentProfileMaskedType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Builder
@Data
public class PaymentProfileDetailResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String cardNumber;
    private String expiryMonth;
    private String expiryYear;
    private String cardType;
    private Boolean isSave;
    private String customerPaymentProfileId;


    public static PaymentProfileDetailResponse mapToPaymentProfileResponse(SubscribedUserProfile subscribedUserProfile, GetCustomerPaymentProfileResponse profileResponse){
       String[] date = convertDateToDesiredFormat(profileResponse.getPaymentProfile().getPayment().getCreditCard().getExpirationDate()).split("/");

        return PaymentProfileDetailResponse.builder()
                .id(subscribedUserProfile.getId())
                .firstName(profileResponse.getPaymentProfile().getBillTo().getFirstName())
                .lastName(profileResponse.getPaymentProfile().getBillTo().getLastName())
                .cardNumber(profileResponse.getPaymentProfile().getPayment().getCreditCard().getCardNumber())
                .cardType(profileResponse.getPaymentProfile().getPayment().getCreditCard().getCardType())
                .expiryMonth(date[0])
                .expiryYear(date[1])
                .isSave(subscribedUserProfile.getIsDefault())
                .customerPaymentProfileId(profileResponse.getPaymentProfile().getCustomerPaymentProfileId())
                .build();
    }

    public static List<PaymentProfileDetailResponse> mapToPaymentProfileResponse(List<CustomerPaymentProfileMaskedType> paymentProfiles,List<SubscribedUserProfile> subscribedUserProfiles){
        List<PaymentProfileDetailResponse> list =
                subscribedUserProfiles.stream().map(subscribedUserProfile ->{
                    Optional<CustomerPaymentProfileMaskedType> filterData = paymentProfiles.stream()
                            .filter(paymentProfile-> paymentProfile.getCustomerPaymentProfileId().equals(subscribedUserProfile.getCustomerPaymentProfileId()))
                            .findFirst();

                    return filterData.map(customerPaymentProfileMaskedType -> {
                        String[] date = convertDateToDesiredFormat(customerPaymentProfileMaskedType.getPayment().getCreditCard().getExpirationDate()).split("/");
                        return  PaymentProfileDetailResponse.builder()
                                .id(subscribedUserProfile.getId())
                                .firstName(customerPaymentProfileMaskedType.getBillTo().getFirstName())
                                .lastName(customerPaymentProfileMaskedType.getBillTo().getLastName())
                                .cardNumber(customerPaymentProfileMaskedType.getPayment().getCreditCard().getCardNumber())
                                .cardType(customerPaymentProfileMaskedType.getPayment().getCreditCard().getCardType())
                                .expiryMonth(date[0])
                                .expiryYear(date[1])
                                .customerPaymentProfileId(customerPaymentProfileMaskedType.getCustomerPaymentProfileId())
                                .isSave(subscribedUserProfile.getIsDefault())
                                .build();
                    }).orElse(null);
                }).toList();
        return  list;
    }

    private static String convertDateToDesiredFormat(String inputDate) {
            LocalDate date = LocalDate.parse(inputDate + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return date.format(DateTimeFormatter.ofPattern("MM/yy"));
    }

}


