package com.vinncorp.fast_learner.util.enums;

public enum PaymentStatus {
    PAID(1), UNPAID(2), TRIALED(3);

    private int value;

    PaymentStatus(int value) {
        this.value = value;
    }

    public int getValue(){ return value;}

    public static PaymentStatus fromValue(int value) {
        for (PaymentStatus paymentStatus : PaymentStatus.values()) {
            if (paymentStatus.value == value) {
                return paymentStatus;
            }
        }
        return null;
    }
}
