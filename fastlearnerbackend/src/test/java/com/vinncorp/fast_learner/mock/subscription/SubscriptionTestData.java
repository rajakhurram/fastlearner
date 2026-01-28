package com.vinncorp.fast_learner.mock.subscription;

import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.util.enums.PlanType;

public class SubscriptionTestData {

    public static Subscription standardSubscription() {
        Subscription subscription = new Subscription();
        subscription.setId(2L);
        subscription.setName("Standard Plan");
        subscription.setPrice(10.0);
        subscription.setDuration(1);
        subscription.setPlanType(PlanType.STANDARD);
        return subscription;
    }

    public static Subscription freeSubscription() {
        Subscription subscription = new Subscription();
        subscription.setId(1L);
        subscription.setName("Free Plan");
        subscription.setPrice(0.0);
        subscription.setDuration(0);
        subscription.setPlanType(PlanType.FREE);
        return subscription;
    }

    public static Subscription premiumSubscription() {
        Subscription subscription = new Subscription();
        subscription.setId(3L);
        subscription.setName("Premium Plan");
        subscription.setDuration(1);
        subscription.setPrice(100.0);
        subscription.setPlanType(PlanType.PREMIUM);
        return subscription;
    }

    public static Subscription enterpriseSubscription() {
        Subscription subscription = new Subscription();
        subscription.setId(4L);
        subscription.setName("Enterprise Plan");
        subscription.setDuration(1);
        subscription.setPrice(200.0);
        subscription.setPlanType(PlanType.ULTIMATE);
        return subscription;
    }
}
