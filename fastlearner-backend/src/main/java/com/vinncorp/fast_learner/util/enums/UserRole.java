package com.vinncorp.fast_learner.util.enums;

public enum UserRole {
    STUDENT(1), INSTRUCTOR(2);

    private int value;

    UserRole(int value) {
        this.value = value;
    }

    public int getValue(){ return value;}

    public static UserRole fromValue(int value) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.value == value) {
                return userRole;
            }
        }
        return null;
    }
}
