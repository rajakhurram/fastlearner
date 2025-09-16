package com.vinncorp.fast_learner.util.enums;

public enum StripeAccountStatus {
    CONNECTED(1), DISCONNECTED(2);

    private int value;

    StripeAccountStatus(int value) {
        this.value = value;
    }

    public int getValue(){ return value;}

    public static StripeAccountStatus fromValue(int value) {
        for (StripeAccountStatus stripeAccountStatus : StripeAccountStatus.values()) {
            if (stripeAccountStatus.value == value) {
                return stripeAccountStatus;
            }
        }
        return null;
    }
}
