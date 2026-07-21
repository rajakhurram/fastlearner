package com.vinncorp.fast_learner.util.enums;

public enum SubscriptionProcessType {
    CREATE(1), UPGRADE(2), DOWNGRADE(3), UPDATE(4), CANCEL(5), COUPON(6);

    private int value;

    SubscriptionProcessType(int value) {
        this.value = value;
    }

    public int getValue(){ return value;}

    public static SubscriptionProcessType fromValue(int value) {
        for (SubscriptionProcessType subProcessType : SubscriptionProcessType.values()) {
            if (subProcessType.value == value) {
                return subProcessType;
            }
        }
        return null;
    }
}
