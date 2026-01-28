package com.vinncorp.fast_learner.mock.transaction_history;

import com.vinncorp.fast_learner.mock.coupon.CouponTestData;
import com.vinncorp.fast_learner.mock.subscription.SubscriptionTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class TransactionHistoryTestData {

    public static TransactionHistory transactionHistory() {
        return TransactionHistory.builder()
                .id(1L)
                .user(UserTestData.userData())
                .status(GenericStatus.ACTIVE)
                .creationAt(new Date())
                .subscriptionStatus(SubscriptionStatus.SUCCESS)
                .build();
    }

    public static TransactionHistory freeTransactionHistory() {
        var m = transactionHistory();
        m.setSubscription(SubscriptionTestData.freeSubscription());
        return m;
    }

    public static TransactionHistory standardTransactionHistory() {
        var m = transactionHistory();
        m.setSubscription(SubscriptionTestData.standardSubscription());
        m.setAuthSubscriptionId("123456789");
        m.setCustomerPaymentProfileId("123456789");
        m.setResponseCode("OK");
        m.setResponseText("Successful.");
        m.setSubscriptionAmount(SubscriptionTestData.standardSubscription().getPrice());
        m.setSubscriptionStatus(SubscriptionStatus.PENDING);
        m.setSubscriptionNextCycle(Date.from(LocalDate.now().plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        m.setTrialEndDate(new Date());
        return m;
    }

    public static TransactionHistory premiumTransactionHistory() {
        var m = standardTransactionHistory();
        m.setSubscriptionAmount(SubscriptionTestData.premiumSubscription().getPrice());
        m.setSubscriptionNextCycle(Date.from(LocalDate.now().plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        m.setTrialEndDate(new Date());
        return m;
    }

    public static TransactionHistory enterpriseTransactionHistory() {
        var m = standardTransactionHistory();
        m.setSubscriptionAmount(SubscriptionTestData.enterpriseSubscription().getPrice());
        m.setSubscriptionNextCycle(Date.from(LocalDate.now().plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        m.setTrialEndDate(new Date());
        return m;
    }

    public static TransactionHistory couponBasedStandardTransactionHistory() {
        var m = standardTransactionHistory();
        m.setCoupon(CouponTestData.standardSubscriptionCoupon());
        return m;
    }

    public static TransactionHistory cancelledStandardTransactionHistory() {
        var m = standardTransactionHistory();
        m.setSubscriptionStatus(SubscriptionStatus.CONTINUE);
        m.setStatus(GenericStatus.INACTIVE);
        return m;
    }

    public static TransactionHistory cancelledPremiumTransactionHistory() {
        var m = premiumTransactionHistory();
        m.setSubscriptionStatus(SubscriptionStatus.CONTINUE);
        m.setStatus(GenericStatus.INACTIVE);
        return m;
    }

    public static TransactionHistory cancelledEnterpriseTransactionHistory() {
        var m = enterpriseTransactionHistory();
        m.setSubscriptionStatus(SubscriptionStatus.CONTINUE);
        m.setStatus(GenericStatus.INACTIVE);
        return m;
    }
}
