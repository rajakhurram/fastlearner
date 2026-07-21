package com.vinncorp.fast_learner.util.enums;

public enum ValidationModeEnum {
    NONE("none"),
    TEST_MODE("testMode"),
    LIVE_MODE("liveMode"),
    OLD_LIVE_MODE("oldLiveMode");

    private String value;

    ValidationModeEnum(String value) {
        this.value = value;
    }

    public String getValue(){ return value;}

    public static ValidationModeEnum fromValue(String value) {
        for (ValidationModeEnum validationModeEnum : ValidationModeEnum.values()) {
            if (validationModeEnum.value == value) {
                return validationModeEnum;
            }
        }
        return null;
    }
}
