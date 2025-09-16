package com.vinncorp.fast_learner.mock.subscription;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailRequest;

public class SubscriptionRequestTestData {

    public static final PaymentProfileDetailRequest paymentDetail() {
        PaymentProfileDetailRequest paymentDetail = new PaymentProfileDetailRequest();
        paymentDetail.setCardNumber("4111111111111111"); // use sandbox test card
        paymentDetail.setCvv("900");
        paymentDetail.setExpiryMonth("12");
        paymentDetail.setExpiryYear("2030");
        paymentDetail.setFirstName("Jane");
        paymentDetail.setLastName("Doe");
        return paymentDetail;
    }

    public static SubscriptionRequest subsReqForCouponBasedSubscription_standardSubscription() {
        SubscriptionRequest requestDTO = new SubscriptionRequest();
        requestDTO.setCoupon("STDSUBS");
        requestDTO.setSubscriptionId(2L);
        requestDTO.setPaymentDetail(paymentDetail());
        return requestDTO;
    }

    public static SubscriptionRequest subsReqForCouponBasedSubscription_premiumSubscription() {
        SubscriptionRequest requestDTO = new SubscriptionRequest();
        requestDTO.setCoupon("PREMSUBS");
        requestDTO.setSubscriptionId(4L);
        requestDTO.setPaymentDetail(paymentDetail());
        return requestDTO;
    }

    public static SubscriptionRequest subsReqForCouponBasedSubscription_enterpriseSubscription() {
        SubscriptionRequest requestDTO = new SubscriptionRequest();
        requestDTO.setCoupon("ENTRSUBS");
        requestDTO.setSubscriptionId(6L);
        requestDTO.setPaymentDetail(paymentDetail());
        return requestDTO;
    }
}
