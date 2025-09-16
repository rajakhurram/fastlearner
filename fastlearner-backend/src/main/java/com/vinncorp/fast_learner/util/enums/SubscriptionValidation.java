package com.vinncorp.fast_learner.util.enums;

public enum SubscriptionValidation {
    PREMIUM_COURSE(1),
    ASSIGN_COURSE(2);

    SubscriptionValidation(int value) {
        this.value = value;
    }

    public static SubscriptionValidation fromValue(int value){
        for(SubscriptionValidation s:SubscriptionValidation.values()){
            if(s.getValue() == value){
                return  s;
            }
        }
        return  null;
    }

    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
