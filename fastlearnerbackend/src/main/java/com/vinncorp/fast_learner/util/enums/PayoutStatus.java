package com.vinncorp.fast_learner.util.enums;

public enum PayoutStatus {
    PENDING(1), PROCESSED(2), FAILED(3),ACTIVATED(4) ,SYSTEM_ERROR(5) ;

    private int value;

    PayoutStatus(int value) {
        this.value = value;
    }

    public int getValue(){ return value;}

    public static PayoutStatus fromValue(int value) {
        for (PayoutStatus payoutStatus : PayoutStatus.values()) {
            if (payoutStatus.value == value) {
                return payoutStatus;
            }
        }
        return null;
    }
}