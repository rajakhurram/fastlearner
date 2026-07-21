package com.vinncorp.fast_learner.util.enums;

public enum PayoutType {
    SELF(1), DIRECT(2), AFFILIATE(3);

    private int value;

    PayoutType(int value) {
        this.value = value;
    }

    public int getValue(){ return value;}

    public static PayoutType fromValue(int value) {
        for (PayoutType payoutType : PayoutType.values()) {
            if (payoutType.value == value) {
                return payoutType;
            }
        }
        return null;
    }
}
