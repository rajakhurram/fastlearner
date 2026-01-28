package com.vinncorp.fast_learner.util.enums;

public enum CourseReviewStatus {

    LIKED(1), DISLIKED(2);

    private int value;

    CourseReviewStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static CourseReviewStatus fromValue(int value) {
        for (CourseReviewStatus courseReviewStatus : CourseReviewStatus.values()) {
            if (courseReviewStatus.value == value) {
                return courseReviewStatus;
            }
        }
        return null;
    }
}
