package com.vinncorp.fast_learner.util.enums;


public enum CustomerProfileTypeEnum {
    REGULAR("regular"),
    GUEST("guest");
    private String value;

    CustomerProfileTypeEnum(String value) {
        this.value = value;
    }

    public String getValue(){ return value;}

    public static CustomerProfileTypeEnum fromValue(String value) {
        for (CustomerProfileTypeEnum customerProfileTypeEnum : CustomerProfileTypeEnum.values()) {
            if (customerProfileTypeEnum.value == value) {
                return customerProfileTypeEnum;
            }
        }

        return null;
    }
}
