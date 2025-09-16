package com.vinncorp.fast_learner.mock.subscription.subscribed_user;

import com.vinncorp.fast_learner.mock.coupon.CouponTestData;
import com.vinncorp.fast_learner.mock.subscription.SubscriptionTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class SubscribedUserTestData {

    public static SubscribedUser subscribedUser() {
        return SubscribedUser.builder()
                .id(1L)
                .user(UserTestData.userData())
                .customerProfileId("12983nk3nier")
                .paymentSubscriptionId("123456123")
                .subscribedId("123456123")
                .subscription(SubscriptionTestData.standardSubscription())
                .build();
    }

    public static SubscribedUser freeSubscribedUser() {
        var subscribedUser = subscribedUser();
        subscribedUser.setSubscribedId(null);
        subscribedUser.setPaymentSubscriptionId(null);
        subscribedUser.setCustomerProfileId(null);
        subscribedUser.setUser(UserTestData.userData());
        subscribedUser.setSubscription(SubscriptionTestData.freeSubscription());
        return subscribedUser;
    }

    public static SubscribedUser standardSubscribedUser() {
        var subscribedUser = subscribedUser();
        subscribedUser.setUser(UserTestData.userData());
        subscribedUser.setSubscription(SubscriptionTestData.standardSubscription());
        return subscribedUser;
    }

    public static SubscribedUser premiumSubscribedUser() {
        var subscribedUser = subscribedUser();
        subscribedUser.setUser(UserTestData.userData());
        subscribedUser.setSubscription(SubscriptionTestData.premiumSubscription());
        return subscribedUser;
    }

    public static SubscribedUser enterpriseSubscribedUser() {
        var subscribedUser = subscribedUser();
        subscribedUser.setUser(UserTestData.userData());
        subscribedUser.setSubscription(SubscriptionTestData.enterpriseSubscription());
        return subscribedUser;
    }

    public static SubscribedUser couponBasedStandardSubscribedUser() {
        var subscribedUser = subscribedUser();
        subscribedUser.setUser(UserTestData.userData());
        subscribedUser.setSubscription(SubscriptionTestData.standardSubscription());
        subscribedUser.setCoupon(CouponTestData.standardSubscriptionCoupon());
        subscribedUser.setCouponValidTill(Date.from(LocalDate.now().plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        return subscribedUser;
    }

    public static SubscribedUser couponBasedPremiumSubscribedUser() {
        var subscribedUser = subscribedUser();
        subscribedUser.setUser(UserTestData.userData());
        subscribedUser.setSubscription(SubscriptionTestData.premiumSubscription());
        subscribedUser.setCoupon(CouponTestData.premiumSubscriptionCoupon());
        subscribedUser.setCouponValidTill(Date.from(LocalDate.now().plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        return subscribedUser;
    }

    public static SubscribedUser couponBasedEnterpriseSubscribedUser() {
        var subscribedUser = subscribedUser();
        subscribedUser.setUser(UserTestData.userData());
        subscribedUser.setSubscription(SubscriptionTestData.enterpriseSubscription());
        subscribedUser.setCoupon(CouponTestData.enterpriseSubscriptionCoupon());
        subscribedUser.setCouponValidTill(Date.from(LocalDate.now().plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        return subscribedUser;
    }
}
