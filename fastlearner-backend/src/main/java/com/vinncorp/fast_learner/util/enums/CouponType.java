package com.vinncorp.fast_learner.util.enums;

public enum CouponType {
    PREMIUM(1), SUBSCRIPTION(2), BOTH(3);

    private int value;

    CouponType(int value) {
        this.value = value;
    }

    public int getValue(){ return value;}

    public static CouponType fromValue(int value) {
        for (CouponType couponType : CouponType.values()) {
            if (couponType.value == value) {
                return couponType;
            }
        }
        return null;
    }
}
