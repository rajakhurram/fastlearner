package com.vinncorp.fast_learner.util.enums;

public enum PermissionName {
    AFFILIATE(0),PREMIUM_COURSE(1), EDIT_PREMIUM_COURSE(2);

    PermissionName(int value) {
        this.value = value;
    }

    public static SubscriptionStatus fromValue(int value){
        for(SubscriptionStatus s:SubscriptionStatus.values()){
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
