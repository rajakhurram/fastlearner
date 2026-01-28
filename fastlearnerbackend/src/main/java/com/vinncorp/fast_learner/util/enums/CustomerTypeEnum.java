package com.vinncorp.fast_learner.util.enums;


public enum CustomerTypeEnum {
    INDIVIDUAL("individual"),
    BUSINESS("business");
    private String value;

    CustomerTypeEnum(String value) {
        this.value = value;
    }

    public String getValue(){ return value;}

    public static CustomerTypeEnum fromValue(String value) {
        for (CustomerTypeEnum customerTypeEnum : CustomerTypeEnum.values()) {
            if (customerTypeEnum.value == value) {
                return customerTypeEnum;
            }
        }
        return null;
    }
}
