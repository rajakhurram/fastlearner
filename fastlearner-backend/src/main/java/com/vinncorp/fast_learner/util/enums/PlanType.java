package com.vinncorp.fast_learner.util.enums;

public enum PlanType {

    FREE(0), STANDARD(1), PREMIUM(2), ULTIMATE(3);

    private int value;

    PlanType(int value) {
        this.value = value;
    }

    public int getValue(){ return value;}

    public static PlanType fromValue(int value) {
        for (PlanType planType : PlanType.values()) {
            if (planType.value == value) {
                return planType;
            }
        }
        return null;
    }
}
