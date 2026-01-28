package com.vinncorp.fast_learner.util.enums;

public enum AIResultSort {
    ASCENDING(0), DESCENDING(1), A_TO_Z(2);

    private int value;

    AIResultSort(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static AIResultSort fromValue(int value) {
        for (AIResultSort s : AIResultSort.values()) {
            if (s.value == value) {
                return s;
            }
        }
        return null;
    }

    public static boolean isValidStatus(String status) {
        try {
            AIResultSort.valueOf(status.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
