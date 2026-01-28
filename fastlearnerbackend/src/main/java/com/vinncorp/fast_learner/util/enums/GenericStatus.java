package com.vinncorp.fast_learner.util.enums;

public enum GenericStatus {
    ACTIVE(0), INACTIVE(1);

    private int value;

    GenericStatus(int value){
        this.value = value;
    }

    public int getValue(){
        return  this.value;
    }

    public static GenericStatus fromValue(int value){
        for(GenericStatus s: GenericStatus.values()){
            if(s.value == value){
                return  s;
            }
        }
        return  null;
    }

    public static boolean isValidStatus(String status) {
        try {
            GenericStatus.valueOf(status.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
