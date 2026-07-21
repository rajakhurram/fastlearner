package com.vinncorp.fast_learner.util.enums;

public enum TestType {

    TEST(0), SURVEY(1);

    private int value;

    TestType(int value) {
        this.value = value;
    }

    public int getValue(){ return value;}

    public static TestType fromValue(int value) {
        for (TestType testType : TestType.values()) {
            if (testType.value == value) {
                return testType;
            }
        }
        return null;
    }
}
