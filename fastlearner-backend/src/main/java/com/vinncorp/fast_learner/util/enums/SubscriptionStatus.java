package com.vinncorp.fast_learner.util.enums;

public enum SubscriptionStatus {

    PENDING(0),SUCCESS(1), CONTINUE(2), DISCONTINUE(3);

    SubscriptionStatus(int value) {
        this.value = value;
    }

    public static SubscriptionStatus fromValue(int value){
        for(SubscriptionStatus s:SubscriptionStatus.values()){
            if(s.value == value){
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
