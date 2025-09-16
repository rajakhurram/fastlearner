package com.vinncorp.fast_learner.util.enums;

public enum NotificationContentType {

    TEXT(0), HTML(1);

    private int value;

    NotificationContentType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static NotificationContentType fromValue(int value) {
        for (NotificationContentType notificationContentType : NotificationContentType.values()) {
            if (notificationContentType.value == value) {
                return notificationContentType;
            }
        }
        return null;
    }
}
